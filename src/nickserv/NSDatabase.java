/* 
 * Copyright (C) 2018 Fredrik Karlsson aka DreamHealer & avade.net
 *
 * This program hasAccess free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program hasAccess distributed in the hope that it will be useful,
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
import core.Handler;
import core.HashString;
import core.LogEvent;
import core.Proc;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
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
  
    /* NickServ Methods */

    /**
     *
     * @param ni
     * @return
     */

    public static int createNick ( NickInfo ni )  { 
        if ( ! activateConnection ( )  )  {
            return -2;
        } else if ( ni == null )  {
            return -3;
        }
        /* Try add the nick */
        try {
            HashString salt = Proc.getConf().get ( SECRETSALT );
            /* NICK */
            String query = "insert into nick  ( name,  mask, regstamp, stamp )  "
                          +"values  ( ?, ?, ?, ? )";
            ps = sql.prepareStatement ( query );
            ps.setString   ( 1, ni.getNameStr() );
            ps.setString   ( 2, ni.getString(USER)+"@"+ni.getString ( IP ) );
            ps.setString   ( 3, ni.getString ( REGTIME ) );
            ps.setString   ( 4, ni.getString ( REGTIME ) );
            ps.execute ( );
            ps.close ( );

            /* PASS */
            query = "insert into passlog (nick,pass,stamp) "+
                    "values (?,aes_encrypt(?,?),now())";
            ps = sql.prepareStatement ( query );
            ps.setString ( 1, ni.getNameStr() );
            ps.setString ( 2, ni.getPass() );
            ps.setString ( 3, salt.getString() );
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
            ps.setString   ( 1, ni.getNameStr()                                       );
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

    /**
     *
     * @param ni
     * @return
     */

    public static int updateNick ( NickInfo ni )  {
        ni.getChanges().printChanges();
        if ( ! ni.getChanges().changed ( ) ) {
            return 1;
        }
        
        if ( ! activateConnection ( ) ) {
            return -2;
        } else if ( ni == null )  {
            return -3;
        } 
        
//        System.out.println("updateNick0: "+ni.getString ( USER ) );
//        System.out.println("updateNick1: "+ni.getString ( IP ) );
//        System.out.println("updateNick2: "+ni.getString ( NAME ) );
        
        /* Try change the nick */
        try {
            HashString salt = Proc.getConf().get ( SECRETSALT ); 
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
                ps.setString   ( 3, ni.getNameStr() );
                ps.executeUpdate ( );
                ps.close ( );
            }
            
            String changes = new String ( );
            
            if ( ni.hasChanged ( NOOP ) ) {
                changes = addToQuery ( changes, "noop" );
            }
            if ( ni.hasChanged ( NEVEROP ) ) {
                changes = addToQuery ( changes, "neverop");
            }
            if ( ni.hasChanged ( MAILBLOCK ) ) {
                changes = addToQuery ( changes, "mailblock");
            }
            if ( ni.hasChanged ( SHOWEMAIL ) ) {
                changes = addToQuery ( changes, "showemail");
            }
            if ( ni.hasChanged ( SHOWHOST ) ) {
                changes = addToQuery ( changes, "showhost");
            }
            if ( ni.hasChanged ( MARK ) ) {
                changes = addToQuery ( changes, "mark");
            }
            if ( ni.hasChanged ( FREEZE ) ) {
                changes = addToQuery ( changes, "freeze");
            }
            if ( ni.hasChanged ( HOLD ) ) {
                changes = addToQuery ( changes, "hold");
            }
            if ( ni.hasChanged ( NOGHOST ) ) {
                changes = addToQuery ( changes, "noghost");
            }
                        
            if ( changes.length() > 0 ) {
                
                query = "update nicksetting "
                      + "set "+changes+" "
                      + "where name = ?";
               
                ps = sql.prepareStatement ( query );
                 int index = 1;
                 ni.getChanges().printChanges();
                if ( ni.hasChanged ( NOOP ) ) {
                    ps.setInt ( index++, ni.isSet ( NOOP ) ? 1 : 0 );
                }
                if ( ni.hasChanged ( NEVEROP ) ) {
                    ps.setInt ( index++, ni.isSet ( NEVEROP ) ? 1 : 0 );
                }
                if ( ni.hasChanged ( MAILBLOCK ) ) {
                    ps.setInt ( index++, ni.isSet ( MAILBLOCK ) ? 1 : 0 );
                }
                if ( ni.hasChanged ( SHOWEMAIL ) ) {
                    ps.setInt ( index++, ni.isSet ( SHOWEMAIL ) ? 1 : 0 );
                }
                if ( ni.hasChanged ( SHOWHOST ) ) {
                    ps.setInt ( index++, ni.isSet ( SHOWHOST ) ? 1 : 0 );
                }

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

                ps.setString   ( index, ni.getNameStr()                                    );
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
    
    /**
     *
     * @param ni
     * @param var
     * @param enable
     * @return
     */
    public static int setVar ( NickInfo ni, HashString var, boolean enable )  {
        String variable = new String ( );
        if ( ! activateConnection ( )  )  {
            return -2;
        } else if ( ni == null )  {
            return -3;
        }
         
        if ( var.is(NOOP) ) {
            variable = "noop";
        } else if ( var.is(NEVEROP) ) {
            variable = "neverop";
        } else if ( var.is(MAILBLOCKED) ) {
            variable = "mailblock";
        } else if ( var.is(SHOWEMAIL) ) {
            variable = "showemail";
        } else if ( var.is(SHOWHOST) ) {
            variable = "showhost";
        }
          
        /* Try add the vars */          
        try {
            String query = "update nicksetting "
                         + "set "+variable+" = ? "
                         + "where name = ?";
            ps = sql.prepareStatement ( query );
            ps.setInt      ( 1, enable ? 1 : 0 );
            ps.setString   ( 2, ni.getNameStr()  );
            ps.executeUpdate ( );
            idleUpdate ( "setVar ( ) " );

        } catch ( SQLException ex ) {
            /* Was not updated? return -1 */
            Proc.log ( NSDatabase.class.getName ( ) , ex );
            return -1;
        }
        return 1;
    }

    /**
     *
     * @param ni
     * @param var
     * @param nick
     * @return
     */
    public static int setVar ( NickInfo ni, HashString var, String nick )  {
        String variable;
        
        if ( var.is(MARK) ) {
            variable = "mark";
        } else if ( var.is(FREEZE) ) {
            variable = "freeze";
        } else if ( var.is(HOLD) ) {
            variable = "hold";
        } else {
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
            ps.setString   ( 2, ni.getNameStr()  );
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
        HashString salt = Proc.getConf().get ( SECRETSALT );
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
            ps.setString ( 1, salt.getString() );
            ps.setString ( 2, code );
            ps.setString ( 3, user.getNameStr() );
            ps.setString ( 4, salt.getString() );
            ps.setString ( 5, code );
            ps.setString ( 6, user.getNameStr() );
            res2 = ps.executeQuery ( );
            
            if ( res2.next() ) {
                if ( res2.getString("mail") != null ) {
                    auth = new NSAuth ( MAIL, res2.getString("nick"), res2.getString("mail"), res2.getString("auth"), res2.getString("stamp") );
                } else {
                    auth = new NSAuth ( PASS, res2.getString("nick"), res2.getString("pass"), res2.getString ("auth"), res2.getString("stamp") );
                }
            }
            res2.close();
            ps.close();
            
        } catch ( SQLException ex ) {
            Proc.log ( NSDatabase.class.getName ( ), ex );
        }
        return auth;
    }
     
    /**
     *
     * @param ni
     * @return
     */
    public static boolean deleteNick ( NickInfo ni )  { 
        if ( ! activateConnection ( ) || ni == null || ni.getName ( ) == null )  {
            return false;
        }
        try {
            String query = "delete from nick where name = ?;";
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, ni.getNameStr() );
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
    
    /**
     *
     */
    public static void loadAllNickExp ( )  {
        NickInfo ni;
        if  ( ! activateConnection ( ) ) {
            return;
        }
        try {
            String query = "select name,lastsent,mailcount from nickexp;";
            ps = sql.prepareStatement ( query );
            res2 = ps.executeQuery ( );

            if  ( res2.next ( ) )  {
                if ( (ni = NickServ.findNick(res2.getString("name"))) != null ) {
                    ni.getExp().setLastSent ( Long.parseLong ( res2.getString ( "lastsent" )  )  );
                    ni.getExp().setMailCount ( res2.getInt ( "mailcount" )  );
                }
            }
        } catch ( NumberFormatException | SQLException ex )  {
            Proc.log ( NSDatabase.class.getName ( ) , ex );
        }
        return;
    }

    /**
     *
     */
    public static void loadAllSettings ( )  {
        NickInfo ni;

        if ( ! activateConnection ( ) ) {
            return;
        }
        try {
            String query = "select name,noop,neverop,mailblock,showemail,showhost,mark,freeze,hold,noghost from nicksetting";
            ps = sql.prepareStatement ( query );
            res2 = ps.executeQuery ( );

            while ( res2.next ( ) ) {
                if ( ( ni = NickServ.findNick(res2.getString("name")) ) != null ) {
                    if ( res2.getBoolean ( "noop" )  )           { ni.getSettings().set ( NOOP, true );            }
                    if ( res2.getBoolean ( "neverop" )  )        { ni.getSettings().set ( NEVEROP, true );         }
                    if ( res2.getBoolean ( "mailblock" )  )      { ni.getSettings().set ( MAILBLOCK, true );       }
                    if ( res2.getBoolean ( "showemail" )  )      { ni.getSettings().set ( SHOWEMAIL, true );       }
                    if ( res2.getBoolean ( "showhost" )  )       { ni.getSettings().set ( SHOWHOST, true );        }
                    ni.getSettings().set ( MARK, res2.getString ( "mark" ) );
                    ni.getSettings().set ( FREEZE, res2.getString ( "freeze" ) );
                    ni.getSettings().set ( HOLD, res2.getString ( "hold" ) );
                    ni.getSettings().set ( NOGHOST, res2.getString ( "noghost" ) );
                }
            } 
            res2.close ( );
            ps.close ( );
            idleUpdate ( "loadAllSettings ( ) " );
        } catch  ( SQLException ex )  {
            Proc.log ( NSDatabase.class.getName ( ), ex );
        } 
        return;
    }
  
    /**
     *
     * @param ni
     * @param command
     * @return
     */
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
            ps.setString ( 1, ni.getNameStr() );
            ps.setString ( 2, command.getExtra ( ) );
            ps.executeUpdate ( );
            ps.close ( );

            idleUpdate ( "authMail ( )" );
            return true;
        } catch ( SQLException e )  {
            Proc.log ( NSDatabase.class.getName ( ), e );
        }
        return false;
    }
    
    /**
     *
     * @param ni
     * @param command
     * @return
     */
    public static boolean authPass ( NickInfo ni, Command command )  {
        if ( ! activateConnection ( ) ) {
            return false;
        }
        String query;
        try {
            query = "update passlog "+
                    "set auth = null "+
                    "where nick = ? "+
                    "and auth = ?";
            ps = sql.prepareStatement ( query );
            ps.setString ( 1, ni.getNameStr() );
            ps.setString ( 2, command.getExtra ( ) );
            ps.executeUpdate ( );
            ps.close ( );

            idleUpdate ( "authMail ( ) " );
            return true;
        } catch ( SQLException e )  {
            Proc.log ( NSDatabase.class.getName ( ) , e );
        }
        return false;
    }
    
    static String tableByHash ( HashString hash ) {
        if ( hash.is(MAIL) ) {
            return "maillog";
        } else if ( hash.is(PASS) ) {
            return "passlog";
        } else {
            return "";
        }
    }
    static String fieldByHash ( HashString hash ) {
        if ( hash.is(MAIL) ) {
            return "mail";
        } else if ( hash.is(PASS) ) {
            return "pass";
        } else {
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
            ps.setString ( 2, auth.getNick().getString() );
            ps.executeUpdate ( );
            ps.close ( );

            return true;
            
        } catch ( SQLException ex ) {
            Proc.log ( NSDatabase.class.getName ( ), ex );
            return false;
        }
    }
    
    /**
     *
     * @param ni
     * @return
     */
    public static boolean saveNickExp ( NickInfo ni )  {
        if  ( ! activateConnection ( )   )  {
            return false;
        }    
        try {
            String query = "insert into nickexp "
                         + " ( name, lastsent, mailcount ) "
                         + "values  ( ?, ?, ? ) "
                         + "on duplicate key "
                         + "update lastsent = ?, mailcount = ?";
             
            ps = sql.prepareStatement ( query );
            ps.setString   ( 1, ni.getNameStr() );
            ps.setLong     ( 2, ni.getNickExp().getLastSent ( ) );
            ps.setInt      ( 3, ni.getNickExp().getMailCount ( ) );
            ps.setLong     ( 4, ni.getNickExp().getLastSent ( ) );
            ps.setInt      ( 5, ni.getNickExp().getMailCount ( ) );
            ps.executeUpdate ( );
            ps.close ( );

            idleUpdate ( "saveNickExp()" );
            return true;
        } catch ( SQLException ex ) {
            Proc.log ( NSDatabase.class.getName(), ex );
        }
        return false;
    }
    
    static ArrayList<NSAuth> getAuthsByNick ( HashString hash, HashString nick ) {
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
            HashString salt = Proc.getConf().get ( SECRETSALT );
            ps = sql.prepareStatement ( query );
            ps.setString ( 1, salt.getString() );
            ps.setString ( 2, nick.getString() );
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
            Proc.log ( NSDatabase.class.getName ( ), ex );
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
                       "order by stamp desc "+
                       "limit 1";
        
        try {
            HashString salt = Proc.getConf().get ( SECRETSALT );
            ps = sql.prepareStatement ( query );
            ps.setString ( 1, salt.getString() );
            ps.setString ( 2, nick );
            res = ps.executeQuery ( );
            if ( res.next ( ) ) {
                mail = ( res.getString("mail") != "null" ? res.getString("mail") : null );
            }
            res.close();
            ps.close();
            
        } catch ( SQLException ex ) {
            Proc.log ( NSDatabase.class.getName ( ), ex );
        }
        return mail;
    }
    
    static String getPassByNick ( String nick ) {
        String pass = null;
        if ( ! activateConnection ( ) ) {
            return pass;
        }
        
        String query = "select aes_decrypt(pass,?) as pass "+
                       "from passlog "+
                       "where nick = ? "+
                       "and auth is null "+
                       "order by stamp desc "+
                       "limit 1";
        
        try {
            HashString salt = Proc.getConf().get ( SECRETSALT );
            ps = sql.prepareStatement ( query );
            ps.setString ( 1, salt.getString() );
            ps.setString ( 2, nick );
            res = ps.executeQuery ( );
            if ( res.next ( ) ) {
                pass = res.getString("pass");
            }
            res.close();
            ps.close();
            
        } catch ( SQLException ex ) {
            Proc.log ( NSDatabase.class.getName ( ) , ex );
        }
        return pass;
    }
    
    static boolean addMail ( NSAuth mail ) {
        if ( ! activateConnection() ) {
            return false;
        }
        String query = "insert into maillog "+
                       "(nick,mail,auth,stamp) "+
                       "values ( ?, aes_encrypt(?,?), ?, now() )";
        try {
            HashString salt = Proc.getConf().get ( SECRETSALT );
            ps = sql.prepareStatement ( query );
            ps.setString ( 1, mail.getNick().getString() );
            ps.setString ( 2, mail.getValue() );
            ps.setString ( 3, salt.getString() );
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
            HashString salt = Proc.getConf().get ( SECRETSALT );
            ps = sql.prepareStatement ( query );
            ps.setString ( 1, pass.getNick().getString() );
            ps.setString ( 2, pass.getValue() );
            ps.setString ( 3, salt.getString() );
            ps.setString ( 4, pass.getAuth() );
            ps.execute();
            ps.close();
            
        } catch ( SQLException ex ) {
            Proc.log ( NSDatabase.class.getName ( ) , ex );
            return false;
        }
        return true;
    }
    
    static HashMap<BigInteger,NickInfo> loadAllNicks ( )  {
        HashMap<BigInteger,NickInfo> nList = new HashMap<>();
        NickInfo ni;
        String[] buf;
        NickSetting settings;
        Expire exp;
        long now;
        long now2;

        if ( ! activateConnection ( ) ) {
            return nList;
        }
        
        try {
            now = System.nanoTime();
            HashString salt = Proc.getConf().get ( SECRETSALT );
            
            String query = "select n.name,"+
                           "  n.mask,"+
                           "  (select aes_decrypt(pass,?) from passlog where nick=n.name and stamp >= n.regstamp and auth is null order by stamp desc limit 1) as pass,"+
                           "  (select aes_decrypt(mail,?) from maillog where nick=n.name and stamp >= n.regstamp and auth is null order by stamp desc limit 1) as mail,"+
                           "  n.regstamp,"+
                           "  n.stamp "+
                           "from nick as n "+
                           "order by name asc";
            
            ps = sql.prepareStatement ( query );
            ps.setString ( 1, salt.getString() );
            ps.setString ( 2, salt.getString() );
            res = ps.executeQuery ( );

            System.out.print("Loading Nicks: ");
            int $count = 0;
             
            while ( res.next ( ) ) { 
                buf = res.getString("mask").split ( Pattern.quote ( "@" ) );
                settings = new NickSetting ( );
                exp = new Expire ( );
                //exp = getNickExp ( res.getString ( 1 ) );
                //exp = ( exp != null ? exp : new Expire ( ) );
                if ( buf.length > 1 && buf[1] == null ) {
                    buf[1] = "Unknown";
                }
                 
                ni = new NickInfo ( 
                    res.getString ( "name" ),
                    buf[0],
                    buf[1],
                    res.getString ( "pass" ),
                    res.getString ( "mail" ),
                    res.getString ( "regstamp" ),
                    res.getString ( "stamp" ),
                    settings,
                    exp
                );
//                System.out.println("Loading nick: "+ni.getName());
                Oper oper =  OperServ.getOper ( ni.getName() );
//                System.out.println(" - Oper: "+oper.getName() );
//                System.out.println(" - Access: "+oper.getAccess() );
//                System.out.println(" - Instater: "+oper.getString ( INSTATER ) );
                
                if ( ni.getEmail() != null ) {
                    ni.getSettings().set ( AUTH, true );
                }
                ni.setOper ( OperServ.getOper ( ni.getName() ) );
                nList.put ( ni.getName().getCode(), ni );
                $count++; 
            } 
            now2 = System.nanoTime();
            System.out.print(".. "+$count+" nicks loaded [took "+(now2-now)+"ns]\n");
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
   