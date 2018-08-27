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
import operserv.OSDatabase;

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
            
            String query = "insert into nick  ( name, hashcode, mask, pass, auth, regstamp, stamp )  "
                          +"values  ( ?, ?, ?, aes_encrypt(?,?), ?, now(), now() )";
            ps = sql.prepareStatement ( query );
            ps.setString   ( 1, ni.getString ( NAME )                                     );
            ps.setInt      ( 2, ni.getHashName ( )                                        );
            ps.setString   ( 3, ni.getString ( USER ) +"@"+ni.getString ( IP )            );
            ps.setString   ( 4, ni.getPass ( )                                            );
            ps.setString   ( 5, salt                                                      );
            ps.setString   ( 6, ni.getString ( AUTH )                                     );
            ps.execute ( );
            ps.close ( );

            query = "insert into maillog (nick,mail,stamp) "+
                    "values (?,aes_encrypt(?,?),now())";
            ps = sql.prepareStatement ( query );
            ps.setString ( 1, ni.getName() );
            ps.setString ( 2, ni.getEmail() );
            ps.setString ( 3, salt );
            ps.execute();
            ps.close();
            
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
        
        if ( ! activateConnection ( ) ) {
            return -2;
        } else if ( ni == null )  {
            return -3;
        } else if ( config == null ) {
            return -4;
        } 
        
        System.out.println("updateNick0: "+ni.getString ( USER ) );
        System.out.println("updateNick1: "+ni.getString ( IP ) );
        System.out.println("updateNick2: "+ni.getString ( NAME ) );
        
        /* Try update the nick */
        try {
            String salt = config.get ( SECRETSALT );
            String query = "update nick "
                          +"set mask = ?, pass = AES_ENCRYPT(?,?), stamp = now() "
                          +"where name = ?";
            ps = sql.prepareStatement ( query );
            ps.setString   ( 1, ni.getString ( USER ) +"@"+ni.getString ( IP )            );
            ps.setString   ( 2, ni.getPass ( )                                            );
            ps.setString   ( 3, salt                                                      );
            ps.setString   ( 4, ni.getString ( NAME )                                     );
            ps.executeUpdate ( );
            ps.close ( );

            query = "update nicksetting "
                  + "set noop = ?, neverop = ?, mailblock = ?, showemail = ?, showhost = ?, auth = ?, mark = ?, freeze = ?, hold = ? "
                  + "where name = ?";
            ps = sql.prepareStatement ( query );
            ps.setInt      ( 1, ni.getSettings().is ( NOOP ) ? 1 : 0                     );
            ps.setInt      ( 2, ni.getSettings().is ( NEVEROP ) ? 1 : 0                  );
            ps.setInt      ( 3, ni.getSettings().is ( MAILBLOCKED ) ? 1 : 0              );
            ps.setInt      ( 4, ni.getSettings().is ( SHOWEMAIL ) ? 1 : 0                );
            ps.setInt      ( 5, ni.getSettings().is ( SHOWHOST ) ? 1 : 0                 );
            ps.setInt      ( 6, ni.getSettings().is ( AUTH ) ? 1 : 0                     );
            
            if ( ! ni.getSettings().is ( MARK ) ) {
                ps.setNull ( 7, Types.VARCHAR );
            } else {
                ps.setString   ( 7, ni.getSettings().getInstater( MARK ) );
            }
            
            if ( ! ni.getSettings().is ( FREEZE ) ) {
                ps.setNull ( 8, Types.VARCHAR );
            } else {
                ps.setString   ( 8, ni.getSettings().getInstater( FREEZE ) );
            }
            
            if ( ! ni.getSettings().is ( HOLD ) ) {
                ps.setNull ( 9, Types.VARCHAR );
            } else {
                ps.setString   ( 9, ni.getSettings().getInstater( HOLD ) );
            }

            ps.setString   ( 10, ni.getString ( NAME )                                    );
            ps.executeUpdate ( );
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
        String variable = new String ( );
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
   
    /* Get nick from hashcode */
    /* Get nick from hashcode */
    public static ArrayList<NickInfo> getNickList ( String pattern )  {
        ArrayList<NickInfo> nList = new ArrayList<> ( );
        NickInfo ni;
        String[] buf, buf2;
        NickSetting settings;
        Expire exp;
        Config config = Proc.getConf ( );
        if ( pattern.isEmpty ( ) ) {
            return null;
        }
        if ( ! activateConnection ( ) ) {
            return null;
        }
        
        pattern.replaceAll ( "\\'", "" );
        pattern = pattern.replaceAll ( "\\*", "(.*)" );
        pattern = pattern.replaceAll ( "\\?", "(.?){0,1}" );
        
        try {
            String salt = config.get ( SECRETSALT );
            String query = "select n.name,"+
                           "  n.hashcode,"+
                           "  n.mask,"+
                           "  aes_decrypt(n.pass,?) as pass,"+
                           "  (select aes_decrypt(mail,?) from maillog where nick=n.name order by stamp desc limit 1) as mail,"+
                           "  n.auth,"+
                           "  n.regstamp,"+
                           "  n.stamp "+
                           "from nick as n "+
                           "where name rlike ? "+
                           "or mask rlike ? "+
                           "order by name asc";

            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, salt            );
            ps.setString  ( 2, salt            );
            ps.setString  ( 3, "^"+pattern+"$" );
            ps.setString  ( 4, "^"+pattern+"$" );
            res = ps.executeQuery ( );

            while ( res.next ( )  )  { 
                buf = res.getString ( 3 ) .split ( Pattern.quote ( "@" )  );
                //if ( buf.length == 1 )  { buf = new String[] { buf[0], "Unknown" }; }
                settings = getSettings ( res.getString ( 1 )  ); 
                exp      = getNickExp ( res.getString ( 1 )  );
                exp      = ( exp!=null?exp:new Expire ( )  );
                ni = new NickInfo ( 
                    res.getString ( 1 ),
                    buf[0],
                    buf[1],
                    res.getString ( 4 ), 
                    res.getString ( 5 ),
                    res.getString ( 6 ),
                    res.getString ( 7 ), 
                    res.getString ( 8 ),
                    settings,
                    exp
                );
                nList.add ( ni ); 
            }
            res.close ( );
            ps.close ( );
        } catch  ( NumberFormatException | SQLException ex )  {
            Proc.log ( NSDatabase.class.getName ( ) , ex );
        }
        return nList;
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
            Proc.log ( NSDatabase.class.getName ( ) , e );
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
            String query = "select noop,neverop,mailblock,showemail,showhost,auth,mark,freeze,hold "
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
                if ( res2.getBoolean ( "auth" )  )           { settings.set ( AUTH, true );            }
                settings.set ( MARK, res2.getString ( "mark" ) );
                settings.set ( FREEZE, res2.getString ( "freeze" ) );
                settings.set ( HOLD, res2.getString ( "hold" ) );
            } 
            res2.close ( );
            ps.close ( );
            idleUpdate ( "getSettings ( ) " );
        } catch  ( SQLException ex )  {
            Proc.log ( NSDatabase.class.getName ( ) , ex );
        } 
        return settings;
    }
  
    public static boolean authNick ( NickInfo ni )  {
        if ( ! activateConnection ( )  )  {
            return false;
        }

        try {
            String query = "update nicksetting "
                         + "set auth = ? "
                         + "where name = ?";
            ps = sql.prepareStatement ( query );
            ps.setInt      ( 1, 1 );
            ps.setString   ( 2, ni.getName ( )  );
            ps.executeUpdate ( );
            ps.close ( );

            idleUpdate ( "authNick ( ) " );
            return true;
        } catch ( SQLException e )  {
            Proc.log ( NSDatabase.class.getName ( ) , e );
        }
        return false;
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
            ps.setString   (  1, ni.getName ( )   );
            ps.setLong     (  2, ni.getNickExp ( ) .getLastSent ( )   );
            ps.setInt      (  3, ni.getNickExp ( ) .getMailCount ( )   );
            ps.setLong     (  4, ni.getNickExp ( ) .getLastSent ( )   );
            ps.setInt      (  5, ni.getNickExp ( ) .getMailCount ( )   );
            ps.executeUpdate ( );
            ps.close ( );

            idleUpdate  (  "saveNickExp ( ) "  );
            return true;
        } catch  (  SQLException e  )  {
            Proc.log  (  NSDatabase.class.getName ( ) , e  );
        }
        return false;
    }

  
    public static ArrayList<NickInfo> getUnAuthNickList ( )  {
        NickSetting settings;
        String[] buf;
        ArrayList<NickInfo> niList = new ArrayList<> ( );
        Expire exp;
        Config config = Proc.getConf ( );

        if  ( ! activateConnection ( ) ) {
            return niList;
        }
            
        try {
            String salt = config.get ( SECRETSALT );

            String query = "select n.name,"+
                           "  n.hashcode,"+
                           "  n.mask,"+
                           "  aes_decrypt(n.pass,?) as pass,"+
                           "  (select aes_decrypt(mail,?) from maillog where nick=n.name order by stamp desc limit 1) as mail,"+
                           "  n.auth,"+
                           "  n.regstamp,"+
                           "  n.stamp "+
                           "from nick as n "+
                           "where auth != null "+
                           "and regstamp > ( UNIX_TIMESTAMP ( ) + ( 60*60*24*3 ) )";

            ps = sql.prepareStatement (  query  );
            ps.setString   (  1, salt   );
            ps.setString   (  2, salt   );
            res = ps.executeQuery ( );

            if  (  res.next ( )   )  { 
                buf             = res.getString ( 3 ) .split ( Pattern.quote ( "@" )  );
                //if ( buf.length == 1 )  { buf = new String[] { buf[0], "Unknown" }; }

                settings        = getSettings ( res.getString ( 1 )  ); 
                exp             = getNickExp ( res.getString ( 1 )  );

                niList.add ( 
                    new NickInfo ( 
                        res.getString ( 1 ),
                        buf[0],
                        buf[1],
                        res.getString ( 4 ),
                        res.getString ( 5 ),
                        res.getString ( 6 ),
                        res.getString ( 7 ),
                        res.getString ( 8 ),
                        settings,
                        exp
                    )
                );

            }
            res.close ( );
            ps.close ( );
        } catch  ( SQLException ex )  {
            Proc.log ( NSDatabase.class.getName ( ) , ex );
        }
        return niList;
    }

    
    static ArrayList<NSMail> getMailsByNick ( String nick ) {
        ArrayList<NSMail> eList = new ArrayList<>();
        
        if ( ! activateConnection ( ) ) {
            return eList;
        }
        
        String query = "select nick,aes_decrypt(mail,?) as mail,auth,stamp "+
                       "from maillog "+
                       "where nick = ? "+
                       "order by stamp asc";
        
        try {
            String salt = Proc.getConf().get ( SECRETSALT );
            ps = sql.prepareStatement ( query );
            ps.setString ( 1, salt );
            ps.setString ( 2, nick );
            res = ps.executeQuery ( );
            while ( res.next ( ) ) {
                eList.add ( new NSMail ( 
                    res.getString("nick"), 
                    res.getString("mail"), 
                    res.getString("auth"), 
                    res.getString("stamp") )
                );
            }
            res.close();
            ps.close();
            
        } catch ( Exception ex ) {
            Proc.log ( NSDatabase.class.getName ( ) , ex );
        }
        return eList;
    }

    static boolean addMail ( NSMail mail ) {
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
            ps.setString ( 2, mail.getMail() );
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
                           "  aes_decrypt(n.pass,?) as pass,"+
                           "  (select aes_decrypt(mail,?) from maillog where nick=n.name order by stamp desc limit 1) as mail,"+
                           "  n.auth,"+
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
                    res.getString ( 8 ),
                    settings,
                    exp
                );
                ni.setOper ( OSDatabase.getOper ( ni.getName() ) );
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
   