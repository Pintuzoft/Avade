/* 
 * Copyright (C) 2018 Fredrik Karlsson aka DreamHealer & avade.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package nickserv;

import command.Command;
import core.Config;
import core.Expire;
import core.Database;
import core.LogEvent;
import core.Proc;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import operserv.Oper;
import operserv.OperServ;
import user.User;

/**
 *
 * @author DreamHealer
 */
public class NSDatabase extends Database {
    private static Statement            s;
    private static ResultSet            res;
    private static ResultSet            res2;
    private static PreparedStatement    ps;
 
    /* Create hashcode in database for all nicknames that doesnt have one */
    public static int fixDBHash ( )  {
        String name;
        LinkedList<String> names = new LinkedList<> ( );
        
        if ( ! activateConnection ( ) ) {
            return -2;
        }
        
        try {
            String query = "SELECT name FROM nick WHERE hashcode IS NULL;";
            ps = sql.prepareStatement ( query );
            res = ps.executeQuery ( );
                 
            while ( res.next ( )  )  {
                names.add ( res.getString ( 1 )  );
            }
            res.close ( );
            ps.close ( );
                 
            for ( int index=0; index<names.size ( ); index++ )  {
                name = names.get ( index );
                query = "UPDATE nick SET hashcode = ? WHERE name = ?;";
                ps = sql.prepareStatement ( query );
                ps.setInt      ( 1, name.toUpperCase ( ) .hashCode ( )  );
                ps.setString   ( 2, name );
                ps.executeUpdate ( );
                ps.close ( );
            } 
        } catch  ( SQLException ex )  {
            Logger.getLogger ( NSDatabase.class.getName ( )  ) .log ( Level.SEVERE, null, ex );
            return -1;
        }
        return 1;
    }
    
    
      /* NickServ Methods */
    public static int createNick ( NickInfo ni )  { 
        Config config = Proc.getConf ( );
        if ( ! activateConnection ( )  )  {
            return -2;
        } else if ( ni == null )  {
            return -3;
        } else if ( config == null ) {
            return -4;
        }
        /* Try add the nick */
        try {
            String salt = config.get ( SECRETSALT );
            
            /* NICK */
            String query = "insert into nick  ( name, hashcode, mask, regstamp, stamp )  "
                          +"values  ( ?, ?, ?, now(), now() )";
            ps = sql.prepareStatement ( query );
            ps.setString   ( 1, ni.getString ( NAME ) );
            ps.setInt      ( 2, ni.getHashName ( ) );
            ps.setString   ( 3, ni.getString ( USER ) +"@"+ni.getString ( IP ) );
            ps.execute ( );
            ps.close ( );

            /* PASS */
            query = "insert into passlog (nick,pass,stamp) "+
                    "values (?,aes_encrypt(?,?),now())";
            ps = sql.prepareStatement ( query );
            ps.setString ( 1, ni.getName() );
            ps.setString ( 2, ni.getPass() );
            ps.setString ( 3, salt );
            ps.execute();
            ps.close();
            
            /* MAIL */
            //query = "insert into maillog (nick,mail,stamp) "+
            //        "values (?,aes_encrypt(?,?),now())";
            //ps = sql.prepareStatement ( query );
            //ps.setString ( 1, ni.getName() );
            //ps.setString ( 2, ni.getEmail() );
            //ps.setString ( 3, salt );
            //ps.execute();
            //ps.close();
            
            /* SETTINGS */
            query = "insert into nicksetting  ( name,noop,neverop,mailblock,showemail,showhost )  "
                  + "values ( ?, ?, ?, ?, ?, ? );";
            ps = sql.prepareStatement ( query );
            ps.setString   ( 1, ni.getString ( NAME )                                    );
            ps.setInt      ( 2, ni.getSettings().is ( NOOP ) ?1:0                     );
            ps.setInt      ( 3, ni.getSettings().is ( NEVEROP ) ?1:0                  );
            ps.setInt      ( 4, ni.getSettings().is ( MAILBLOCKED ) ?1:0              );
            ps.setInt      ( 5, ni.getSettings().is ( SHOWEMAIL ) ?1:0                );
            ps.setInt      ( 6, ni.getSettings().is ( SHOWHOST ) ?1:0                 );
            ps.execute ( );
            ps.close ( ); 

            idleUpdate ( "createNick ( ) " );
        } catch  ( SQLException ex )  {
            /* Nick already exists? return -1 */
            Proc.log ( NSDatabase.class.getName ( ) , ex );
            return -1;
        }
        return 1;
    }
    
    /* NickServ Methods */
    public static int updateNick ( NickInfo ni )  {
        Config config = Proc.getConf ( );
        ni.getChanges().printChanges();
        if ( ! ni.getChanges().changed ( ) ) {
            return 1;
        }
        
        if ( ! activateConnection ( ) ) {
            return -2;
        } else if ( ni == null )  {
            return -3;
        } else if ( config == null ) {
            return -4;
        } 
        
//        System.out.println("updateNick0: "+ni.getString ( USER ) );
//        System.out.println("updateNick1: "+ni.getString ( IP ) );
//        System.out.println("updateNick2: "+ni.getString ( NAME ) );
        
        /* Try change the nick */
        try {
            String salt = config.get ( SECRETSALT ); 
            String query;
            String mask = ni.getString(USER)+"@"+ni.getString(IP);
            if ( ni.getChanges().hasChanged ( FULLMASK ) ||
                 ni.getChanges().hasChanged ( LASTUSED ) ) {
                query = "update nick "
                       +"set mask = ?, stamp = ? "
                       +"where name = ?";
                ps = sql.prepareStatement ( query );
                ps.setString   ( 1, mask );
                ps.setString   ( 2, ni.getString ( LASTUSED ) );
                ps.setString   ( 3, ni.getString ( NAME ) );
                ps.executeUpdate ( );
                ps.close ( );
            }
            
            String changes = new String ( );
            
            if ( ni.getChanges().hasChanged ( NOOP ) )
                changes = addToQuery ( changes, "noop" );
            if ( ni.getChanges().hasChanged ( NEVEROP ) )
                changes = addToQuery ( changes, "neverop");
            if ( ni.getChanges().hasChanged ( MAILBLOCK ) )
                changes = addToQuery ( changes, "mailblock");
            if ( ni.getChanges().hasChanged ( SHOWEMAIL ) )
                changes = addToQuery ( changes, "showemail");
            if ( ni.getChanges().hasChanged ( SHOWHOST ) )
                changes = addToQuery ( changes, "showhost");
            if ( ni.getChanges().hasChanged ( MARK ) )
                changes = addToQuery ( changes, "mark");
            if ( ni.getChanges().hasChanged ( FREEZE ) )
                changes = addToQuery ( changes, "freeze");
            if ( ni.getChanges().hasChanged ( HOLD ) )
                changes = addToQuery ( changes, "hold");
            if ( ni.getChanges().hasChanged ( NOGHOST ) )
                changes = addToQuery ( changes, "noghost");
                        
            if ( changes.length() > 0 ) {
                
                query = "update nicksetting "
                      + "set "+changes+" "
                      + "where name = ?";
               
                ps = sql.prepareStatement ( query );
                 int index = 1;
                 ni.getChanges().printChanges();
                if ( ni.getChanges().hasChanged ( NOOP ) )
                    ps.setInt ( index++, ni.getSettings().is ( NOOP ) ? 1 : 0 );
                if ( ni.getChanges().hasChanged ( NEVEROP ) )
                    ps.setInt ( index++, ni.getSettings().is ( NEVEROP ) ? 1 : 0 );
                if ( ni.getChanges().hasChanged ( MAILBLOCK ) )
                    ps.setInt ( index++, ni.getSettings().is ( MAILBLOCK ) ? 1 : 0 );
                if ( ni.getChanges().hasChanged ( SHOWEMAIL ) )
                    ps.setInt ( index++, ni.getSettings().is ( SHOWEMAIL ) ? 1 : 0 );
                if ( ni.getChanges().hasChanged ( SHOWHOST ) )
                    ps.setInt ( index++, ni.getSettings().is ( SHOWHOST ) ? 1 : 0 );

                if ( ni.getChanges().hasChanged ( MARK ) ) {
                    if ( ! ni.getSettings().is ( MARK ) ) {
                        ps.setNull ( index++, Types.VARCHAR );
                    } else {
                        ps.setString ( index++, ni.getSettings().getInstater( MARK ) );
                    }
                }
                if ( ni.getChanges().hasChanged ( FREEZE ) ) {
                    if ( ! ni.getSettings().is ( FREEZE ) ) {
                        ps.setNull ( index++, Types.VARCHAR );
                    } else {
                        ps.setString ( index++, ni.getSettings().getInstater( FREEZE ) );
                    }                
                }

                if ( ni.getChanges().hasChanged ( HOLD ) ) {
                    if ( ! ni.getSettings().is ( HOLD ) ) {
                        ps.setNull ( index++, Types.VARCHAR );
                    } else {
                        ps.setString ( index++, ni.getSettings().getInstater( HOLD ) );
                    }                
                }
                if ( ni.getChanges().hasChanged ( NOGHOST ) ) {
                    if ( ! ni.getSettings().is ( NOGHOST ) ) {
                        ps.setNull ( index++, Types.VARCHAR );
                    } else {
                        ps.setString ( index++, ni.getSettings().getInstater ( NOGHOST ) );
                    }                
                }

                ps.setString   ( index, ni.getString ( NAME )                                    );
                ps.executeUpdate ( );
            }
            ps.close ( ); 
 
            idleUpdate ( "updateNick ( ) " );
            NSDatabase.saveNickExp ( ni );

        } catch  ( SQLException ex )  {
            /* Was not updated? return -1 */
            Proc.log ( NSDatabase.class.getName ( ) , ex );
            return -1;
        }
        return 1;
    }
    
    /* Add key to query */
    private static String addToQuery ( String data, String key ) {
        if ( data.length() > 0 ) {
            return data+", "+key+" = ?";
        } else {
            return data+key+" = ?";
        }
    }
    
    public static int setVar ( NickInfo ni, int var, boolean enable )  {
        String variable = new String ( );
        if ( ! activateConnection ( )  )  {
            return -2;
        } else if ( ni == null )  {
            return -3;
        }
        switch ( var )  { 
            case NOOP :
                variable = "noop";
                break;
                
            case NEVEROP :
                variable = "neverop";
                break;
                
            case MAILBLOCKED :
                variable = "mailblock";
                break;
                
            case SHOWEMAIL :
                variable = "showemail";
                break;
                
            case SHOWHOST :
                variable = "showhost";
                break;
                 
            default : 
                
        }

        /* Try add the vars */          
        try {
            String query = "update nicksetting "
                         + "set "+variable+" = ? "
                         + "where name = ?";
            ps = sql.prepareStatement ( query );
            ps.setInt      ( 1, enable ? 1 : 0 );
            ps.setString   ( 2, ni.getString ( NAME )  );
            ps.executeUpdate ( );
            idleUpdate ( "setVar ( ) " );

        } catch ( SQLException ex ) {
            /* Was not updated? return -1 */
            Proc.log ( NSDatabase.class.getName ( ) , ex );
            return -1;
        }
        return 1;
    }
    public static int setVar ( NickInfo ni, int var, String nick )  {
        String variable;
        switch ( var ) {
            case MARK :
                variable = "mark";
                break;
                
            case FREEZE :
                variable = "freeze";
                break;
                
            case HOLD :
                variable = "hold";
                break;
        
            default :
                return -1;
                
        }
         /* Try add the vars */          
        try {
            String query = "update nicksetting "
                         + "set "+variable+" = ? "
                         + "where name = ?";
            ps = sql.prepareStatement ( query );
            if ( nick.length() == 0 ) {
                ps.setNull ( 1, Types.VARCHAR );
            } else {
                ps.setString ( 1, nick );
            }
            ps.setString   ( 2, ni.getString ( NAME )  );
            ps.executeUpdate ( );
            idleUpdate ( "setVar ( ) " );

        } catch ( SQLException ex ) {
            /* Was not updated? return -1 */
            Proc.log ( NSDatabase.class.getName ( ) , ex );
            return -1;
        }
        return 1;
        
        
    }
   
    static NSAuth fetchAuth ( User user, String code ) {
        NSAuth auth = null;
        if ( ! activateConnection ( ) )  {
            return auth;
        }
        String query;
        String salt = Proc.getConf().get ( SECRETSALT );
        try {
            query = "select nick,aes_decrypt(mail,?) as mail,null as pass,auth,stamp "+
                    "from maillog "+
                    "where auth = ? "+
                    "and nick = ? "+
                    
                    "union "+
                    
                    "select nick,null as mail,aes_decrypt(pass,?) as pass,auth,stamp "+
                    "from passlog "+
                    "where auth = ? "+
                    "and nick = ?";
            
            ps = sql.prepareStatement ( query );
            ps.setString ( 1, salt );
            ps.setString ( 2, code );
            ps.setString ( 3, user.getString ( NAME ) );
            ps.setString ( 4, salt );
            ps.setString ( 5, code );
            ps.setString ( 6, user.getString ( NAME ) );
            res2 = ps.executeQuery ( );
            
            if ( res2.next() ) {
                if ( res2.getString("mail") != null ) {
                    auth = new NSAuth ( MAIL, res2.getString ( "nick" ), res2.getString ( "mail" ), res2.getString ( "auth" ), res2.getString ( "stamp" ) );
                } else {
                    auth = new NSAuth ( PASS, res2.getString ( "nick" ), res2.getString ( "pass" ), res2.getString ( "auth" ), res2.getString ( "stamp" ) );
                }
            }
            res2.close();
            ps.close();
            
        } catch ( SQLException ex ) {
            Proc.log ( NSDatabase.class.getName ( ), ex );
        }
        return auth;
    }
     
     
    public static boolean deleteNick ( NickInfo ni )  { 
        if ( ! activateConnection ( ) || ni == null || ni.getName ( ) == null )  {
            return false;
        }
        try {
            String query = "delete from nick where name = ?;";
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, ni.getName ( )  );
            ps.execute ( );
            ps.close ( ); 
            return true;
        } catch ( SQLException e )  {
            Proc.log ( NSDatabase.class.getName ( ), e );
        }
        return false;
    }
    /* End NickServ */

    private static Expire getNickExp ( String nick )  {
        Expire nExp = new Expire ( );
        if  ( ! activateConnection ( ) ) {
            return nExp;
        }
        try {
            String query = "select lastsent,mailcount "
                         + "from nickexp "
                         + "where name = ? "
                         + "limit 1";
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, nick );
            res2 = ps.executeQuery ( );

            if  ( res2.next ( )  )  {
                nExp.setLastSent ( Long.parseLong ( res2.getString ( 1 )  )  );
                nExp.setMailCount ( res2.getInt ( 2 )  );
            }
        } catch ( NumberFormatException | SQLException ex )  {
            Proc.log ( NSDatabase.class.getName ( ) , ex );
        }
        return nExp;
    }
    
    
    private static NickSetting getSettings ( String nick )  {
        NickSetting settings;
        settings = new NickSetting ( );

        if ( ! activateConnection ( )  )  {
            return settings;
        }
        try {
            String query = "select noop,neverop,mailblock,showemail,showhost,mark,freeze,hold,noghost "
                         + "from nicksetting "
                         + "where name = ? "
                         + "limit 1";
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, nick );
            res2 = ps.executeQuery ( );

            if ( res2.next ( )  )  {
                if ( res2.getBoolean ( "noop" )  )           { settings.set ( NOOP, true );            }
                if ( res2.getBoolean ( "neverop" )  )        { settings.set ( NEVEROP, true );         }
                if ( res2.getBoolean ( "mailblock" )  )      { settings.set ( MAILBLOCK, true );       }
                if ( res2.getBoolean ( "showemail" )  )      { settings.set ( SHOWEMAIL, true );       }
                if ( res2.getBoolean ( "showhost" )  )       { settings.set ( SHOWHOST, true );        }
                settings.set ( MARK, res2.getString ( "mark" ) );
                settings.set ( FREEZE, res2.getString ( "freeze" ) );
                settings.set ( HOLD, res2.getString ( "hold" ) );
                settings.set ( NOGHOST, res2.getString ( "noghost" ) );
            } 
            res2.close ( );
            ps.close ( );
            idleUpdate ( "getSettings ( ) " );
        } catch  ( SQLException ex )  {
            Proc.log ( NSDatabase.class.getName ( ) , ex );
        } 
        return settings;
    }
  
    public static boolean authMail ( NickInfo ni, Command command )  {
        if ( ! activateConnection ( ) ) {
            return false;
        }
        String query;
        try {
            query = "update maillog "+
                    "set auth = null "+
                    "where nick = ? "+
                    "and auth = ?";
            ps = sql.prepareStatement ( query );
            ps.setString   ( 1, ni.getName ( )  );
            ps.setString   ( 2, command.getExtra ( ) );
            ps.executeUpdate ( );
            ps.close ( );

            idleUpdate ( "authMail ( ) " );
            return true;
        } catch ( SQLException e )  {
            Proc.log ( NSDatabase.class.getName ( ) , e );
        }
        return false;
    }
    
    static String tableByHash ( int hash ) {
        switch ( hash ) {
            case MAIL :
                return "maillog";
            case PASS :
                return "passlog";
                
            default :
                return "";
        }
    }
    static String fieldByHash ( int hash ) {
        switch ( hash ) {
            case MAIL :
                return "mail";
            case PASS :
                return "pass"; 
                
            default :
                return "";
        }
    }
    
    static boolean addFullAuth ( NSAuth auth ) {
        if ( ! activateConnection ( ) ) {
            return false;
        }
        String table = tableByHash ( auth.getType() );
        String query;
        try {
            query = "update "+table+" "+
                    "set auth = null "+
                    "where auth = ? "+
                    "and nick = ?";
            ps = sql.prepareStatement ( query );
            ps.setString ( 1, auth.getAuth ( ) );
            ps.setString ( 2, auth.getNick ( ) );
            ps.executeUpdate ( );
            ps.close ( );

            return true;
            
        } catch ( SQLException ex ) {
            Proc.log ( NSDatabase.class.getName ( ), ex );
            return false;
        }
    }
    
    public static boolean saveNickExp ( NickInfo ni )  {
        if  ( ! activateConnection ( )   )  {
            return false;
        }    
        try {
            String query = "insert into nickexp "
                         + " ( name,lastsent,mailcount )  "
                         + "values  ( ?,?,? )  "
                         + "on duplicate key "
                         + "update lastsent = ?, mailcount = ?";
            ps = sql.prepareStatement ( query );
            ps.setString   ( 1, ni.getName ( ) );
            ps.setLong     ( 2, ni.getNickExp().getLastSent ( ) );
            ps.setInt      ( 3, ni.getNickExp().getMailCount ( ) );
            ps.setLong     ( 4, ni.getNickExp().getLastSent ( ) );
            ps.setInt      ( 5, ni.getNickExp().getMailCount ( ) );
            ps.executeUpdate ( );
            ps.close ( );

            idleUpdate  (  "saveNickExp ( ) "  );
            return true;
        } catch ( SQLException ex ) {
            Proc.log ( NSDatabase.class.getName ( ), ex );
        }
        return false;
    }
    
    static ArrayList<NSAuth> getAuthsByNick ( int hash, String nick ) {
        ArrayList<NSAuth> aList = new ArrayList<>();
        
        if ( ! activateConnection ( ) ) {
            return aList;
        }
        String query;
        String table = tableByHash ( hash );
        String field = fieldByHash ( hash );
        query= "select nick,aes_decrypt("+field+",?) as value,auth,stamp "+
                "from "+table+" "+
                "where nick = ? "+
                "order by stamp asc";
 
        try {
            String salt = Proc.getConf().get ( SECRETSALT );
            ps = sql.prepareStatement ( query );
            ps.setString ( 1, salt );
            ps.setString ( 2, nick );
            res = ps.executeQuery ( );
            while ( res.next ( ) ) {
                aList.add ( new NSAuth (
                    MAIL,
                    res.getString("nick"), 
                    res.getString("value"), 
                    res.getString("auth"), 
                    res.getString("stamp") )
                );
            }
            res.close();
            ps.close();
            
        } catch ( SQLException ex ) {
            Proc.log ( NSDatabase.class.getName ( ) , ex );
        }
        return aList;
    }
    
    static String getMailByNick ( String nick ) {
        String mail = null;
        if ( ! activateConnection ( ) ) {
            return mail;
        }
        
        String query = "select aes_decrypt(mail,?) as mail "+
                       "from maillog "+
                       "where nick = ? "+
                       "and auth is null "+
                       "order by stamp asc "+
                       "limit 1";
        
        try {
            String salt = Proc.getConf().get ( SECRETSALT );
            ps = sql.prepareStatement ( query );
            ps.setString ( 1, salt );
            ps.setString ( 2, nick );
            res = ps.executeQuery ( );
            if ( res.next ( ) ) {
                mail = res.getString("mail");
            }
            res.close();
            ps.close();
            
        } catch ( SQLException ex ) {
            Proc.log ( NSDatabase.class.getName ( ) , ex );
        }
        return mail;
    }
    static boolean addMail ( NSAuth mail ) {
        if ( ! activateConnection() ) {
            return false;
        }
        String query = "insert into maillog "+
                       "(nick,mail,auth,stamp) "+
                       "values ( ?, aes_encrypt(?,?), ?, now() )";
        try {
            String salt = Proc.getConf().get ( SECRETSALT );
            ps = sql.prepareStatement ( query );
            ps.setString ( 1, mail.getNick() );
            ps.setString ( 2, mail.getValue() );
            ps.setString ( 3, salt );
            ps.setString ( 4, mail.getAuth() );
            ps.execute();
            ps.close();
            
        } catch ( SQLException ex ) {
            Proc.log ( NSDatabase.class.getName ( ) , ex );
            return false;
        }
        return true;
    }
        
    static boolean addPass ( NSAuth pass ) {
        if ( ! activateConnection() ) {
            return false;
        }
        String query = "insert into passlog "+
                       "(nick,pass,auth,stamp) "+
                       "values ( ?, aes_encrypt(?,?), ?, now() )";
        try {
            String salt = Proc.getConf().get ( SECRETSALT );
            ps = sql.prepareStatement ( query );
            ps.setString ( 1, pass.getNick() );
            ps.setString ( 2, pass.getValue() );
            ps.setString ( 3, salt );
            ps.setString ( 4, pass.getAuth() );
            ps.execute();
            ps.close();
            
        } catch ( SQLException ex ) {
            Proc.log ( NSDatabase.class.getName ( ) , ex );
            return false;
        }
        return true;
    }
    
    static ArrayList<NickInfo> getAllNicks ( )  {
        ArrayList<NickInfo> nList = new ArrayList<> ( );
        NickInfo ni;
        String[] buf;
        NickSetting settings;
        Expire exp;
        Config config = Proc.getConf ( );
        long now;
        long now2;

        if ( ! activateConnection ( ) ) {
            return nList;
        }
        
        try {
            now = System.currentTimeMillis();
            String salt = config.get ( SECRETSALT );
            
            String query = "select n.name,"+
                           "  n.hashcode,"+
                           "  n.mask,"+
                           "  (select aes_decrypt(pass,?) from passlog where nick=n.name and stamp >= n.regstamp and auth is null order by stamp desc limit 1) as pass,"+
                           "  (select aes_decrypt(mail,?) from maillog where nick=n.name and stamp >= n.regstamp and auth is null order by stamp desc limit 1) as mail,"+
                           "  n.regstamp,"+
                           "  n.stamp "+
                           "from nick as n "+
                           "order by name asc";
            
            ps = sql.prepareStatement ( query );
            ps.setString ( 1, salt );
            ps.setString ( 2, salt );
            res = ps.executeQuery ( );

            System.out.print("Loading Nicks: ");
            int $count = 0;
             
            while ( res.next ( ) ) { 
                buf = res.getString(3).split ( Pattern.quote ( "@" ) );
                System.out.println("debug: buf[0]: "+buf[0]);
                System.out.println("debug: buf[1]: "+buf[1]);
                settings = getSettings ( res.getString ( 1 ) );
                exp = getNickExp ( res.getString ( 1 ) );
                exp = ( exp != null ? exp : new Expire ( ) );
                if ( buf[1] == null ) {
                    buf[1] = "Unknown";
                }
                 
                ni = new NickInfo ( 
                    res.getString ( 1 ),
                    buf[0],
                    buf[1],
                    res.getString ( 4 ),
                    res.getString ( 5 ),
                    res.getString ( 6 ),
                    res.getString ( 7 ),
                    settings,
                    exp
                );
                System.out.println("Loading nick: "+ni.getName());
                Oper oper =  OperServ.getOper ( ni.getName() );
                System.out.println(" - Oper: "+oper.getName() );
                System.out.println(" - Access: "+oper.getAccess() );
                System.out.println(" - Instater: "+oper.getString ( INSTATER ) );
                
                ni.setOper ( OperServ.getOper ( ni.getName() ) );
                nList.add ( ni );
                $count++; 
            } 
            now2 = System.currentTimeMillis();
            System.out.print(".. "+$count+" nicks loaded [took "+(now2-now)+"ms]\n");
            res.close ( );
            ps.close ( );
        } catch ( SQLException ex )  {
            Proc.log ( NSDatabase.class.getName ( ) , ex );
        }
        return nList;
    }
    
    
    /* LOG EVENT */
    static int logEvent ( NSLogEvent log ) {
        return Database.logEvent( "nicklog", ( LogEvent ) log );
    }
    static void delLogEvent ( int id ) {
        Database.delLogEvent( "nicklog", id );
    }

}
   