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
package chanserv;

import channel.Topic;
import core.Config;
import core.Database;
import core.Handler;
import core.LogEvent;
import core.Proc;
import java.sql.PreparedStatement;
import nickserv.NickInfo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import nickserv.NickServ;
import operserv.OSLogEvent;
import user.User;


/**
 *
 * @author DreamHealer
 */


/*mysql> desc chan;
+-------------+--------------+------+-----+---------+-------+
| Field       | Type         | Null | Key | Default | Extra |
+-------------+--------------+------+-----+---------+-------+
| name        | varchar ( 33 )   | NO   | PRI |         |       |
| founder     | varchar ( 32 )   | YES  |     | NULL    |       |
| pass        | varchar ( 32 )   | YES  |     | NULL    |       |
| description | varchar ( 128 )  | YES  |     | NULL    |       |
| topic       | varchar ( 512 )  | YES  |     | NULL    |       |
| regstamp    | int ( 11 )       | YES  |     | NULL    |       |
| stamp       | int ( 11 )       | YES  |     | NULL    |       |
+-------------+--------------+------+-----+---------+-------+
7 rows in set  ( 0.00 sec ) 
* 
* mysql> desc chansetting;
+------------+-------------+------+-----+---------+-------+
| Field      | Type        | Null | Key | Default | Extra |
+------------+-------------+------+-----+---------+-------+
| name       | varchar ( 33 )  | NO   | PRI |         |       |
| keeptopic  | tinyint ( 1 )   | YES  |     | NULL    |       |
| topiclock  | tinyint ( 1 )   | YES  |     | NULL    |       |
| ident      | tinyint ( 1 )   | YES  |     | NULL    |       |
| opguard    | tinyint ( 1 )   | YES  |     | NULL    |       |
| restricted | tinyint ( 1 )   | YES  |     | NULL    |       |
| verbose    | tinyint ( 1 )   | YES  |     | NULL    |       |
| mailblock  | tinyint ( 1 )   | YES  |     | NULL    |       |
| leaveops   | tinyint ( 1 )   | YES  |     | NULL    |       |
| private    | tinyint ( 1 )   | YES  |     | NULL    |       |
+------------+-------------+------+-----+---------+-------+
10 rows in set  ( 0.00 sec ) 
* 
* mysql> desc cflags;
+----------+--------------+------+-----+---------+-------+
| Field    | Type         | Null | Key | Default | Extra |
+----------+--------------+------+-----+---------+-------+
| name     | varchar ( 33 )   | YES  |     | NULL    |       |
| type     | varchar ( 16 )   | YES  |     | NULL    |       |
| reason   | varchar ( 256 )  | YES  |     | NULL    |       |
| instater | varchar ( 32 )   | YES  |     | NULL    |       |
| stamp    | int ( 11 )       | YES  |     | NULL    |       |
| expire   | int ( 11 )       | YES  |     | NULL    |       |
+----------+--------------+------+-----+---------+-------+
6 rows in set  ( 0.00 sec ) 
*/




public class CSDatabase extends Database {
    private static Statement s;
    private static ResultSet res;
    private static ResultSet res2;
    private static ResultSet res3;
    private static PreparedStatement ps;

      /* NickServ Methods */
    public static int createChan ( ChanInfo ci )  {
        Config config = Proc.getConf ( );
        if ( ! activateConnection ( )  )  {
            return -2;
        } else if ( ci == null ) {
            return -3;
        } else if ( config == null ) {
            return -4;
        } else {
            
            try {
                String salt = config.get ( SECRETSALT );
                String query = "INSERT INTO chan ( name, founder, pass, description, regstamp, stamp )  "
                             + "VALUES  ( ?, ?, AES_ENCRYPT(?,?), ?, ?, ? )";
                ps = sql.prepareStatement ( query );
                ps.setString  ( 1, ci.getString ( NAME ) );
                ps.setString  ( 2, ci.getFounder().getName ( )  );
                ps.setString  ( 3, ci.getPass ( ) );
                ps.setString  ( 4, salt );             
                ps.setString  ( 5, ci.getString ( DESCRIPTION )  );
                ps.setString  ( 6, ci.getString ( REGTIME ) );
                ps.setString  ( 7, ci.getString ( LASTSEEN ) );
                ps.execute ( );
                ps.close ( );
                
                idleUpdate ( "createChan ( ) " );
            } catch  ( SQLException ex )  {
                /* Nick already exists? return -1 */
                Proc.log ( CSDatabase.class.getName ( ) , ex );
                return -1;
            }
        }
        /* Nick was added */
        return 1;
    }
     
    /* NickServ Methods */
    public static int updateChan ( ChanInfo ci )  { 
        Config config = Proc.getConf ( );
        if ( ! activateConnection ( )  )  {
            /* No SQL connection */
            return -2;
        } else if ( ci == null ) {
            return -3;
        } else if ( config == null ) {
            return -4;
        } else {
            /* Try add the chan */          
            try {
                String query;
                if ( ci.getChanges().hasChanged ( FOUNDER ) ||
                     ci.getChanges().hasChanged ( DESCRIPTION ) ||
                     ci.getChanges().hasChanged ( LASTOPED ) ) {
                    String salt = config.get ( SECRETSALT );
                    query = "UPDATE chan SET "+
                            "founder = ?, pass = AES_ENCRYPT(?,?), description = ?, stamp = FROM_UNIXTIME(UNIX_TIMESTAMP()) "+
                            "WHERE name = ?";
                    ps = sql.prepareStatement ( query );
                    ps.setString  ( 1, ci.getFounder ( ) .getName ( )  );
                    ps.setString  ( 2, ci.getPass ( ) );
                    ps.setString  ( 3, salt );
                    ps.setString  ( 4, ci.getString ( DESCRIPTION )  );
                    ps.setString  ( 5, ci.getString ( NAME )  ); 
                    ps.executeUpdate ( );
                    ps.close ( );
                }
                
                String changes = new String ( );
            
                if ( ci.getChanges().hasChanged ( KEEPTOPIC ) )
                    changes = addToQuery ( changes, "keeptopic" );
                if ( ci.getChanges().hasChanged ( IDENT ) )
                    changes = addToQuery ( changes, "ident" );
                if ( ci.getChanges().hasChanged ( OPGUARD ) )
                    changes = addToQuery ( changes, "opguard" );
                if ( ci.getChanges().hasChanged ( RESTRICT ) )
                    changes = addToQuery ( changes, "restricted" );
                if ( ci.getChanges().hasChanged ( VERBOSE ) )
                    changes = addToQuery ( changes, "verbose" );
                if ( ci.getChanges().hasChanged ( MAILBLOCK ) )
                    changes = addToQuery ( changes, "mailblock" );
                if ( ci.getChanges().hasChanged ( LEAVEOPS ) )
                    changes = addToQuery ( changes, "leaveops" );
                if ( ci.getChanges().hasChanged ( MODELOCK ) )
                    changes = addToQuery ( changes, "modelock" );
                if ( ci.getChanges().hasChanged ( TOPICLOCK ) )
                    changes = addToQuery ( changes, "topiclock" );
                if ( ci.getChanges().hasChanged ( MARK ) )
                    changes = addToQuery ( changes, "mark" );
                if ( ci.getChanges().hasChanged ( FREEZE ) )
                    changes = addToQuery ( changes, "freeze" );
                if ( ci.getChanges().hasChanged ( CLOSE ) )
                    changes = addToQuery ( changes, "close" );
                if ( ci.getChanges().hasChanged ( HOLD ) )
                    changes = addToQuery ( changes, "hold" );
                if ( ci.getChanges().hasChanged ( AUDITORIUM ) )
                    changes = addToQuery ( changes, "auditorium" );
                
                
                if ( changes.length() > 0 ) {
                
                    query = "update chansetting "+
                            "set "+changes+" "+
                            "where name = ?";
                    
                    System.out.println("DEBUG: "+query);
                    ps = sql.prepareStatement ( query );
                    int index = 1;
                    ci.getChanges().printChanges();
                    if ( ci.getChanges().hasChanged ( KEEPTOPIC ) )
                        ps.setInt ( index++, ci.getSettings().is ( KEEPTOPIC ) ? 1 : 0 );
                    if ( ci.getChanges().hasChanged ( IDENT ) )
                        ps.setInt ( index++, ci.getSettings().is ( IDENT ) ? 1 : 0 );
                    if ( ci.getChanges().hasChanged ( OPGUARD ) )
                        ps.setInt ( index++, ci.getSettings().is ( OPGUARD ) ? 1 : 0 );
                    if ( ci.getChanges().hasChanged ( RESTRICT ) )
                        ps.setInt ( index++, ci.getSettings().is ( RESTRICT ) ? 1 : 0 );
                    if ( ci.getChanges().hasChanged ( VERBOSE ) )
                        ps.setInt ( index++, ci.getSettings().is ( VERBOSE ) ? 1 : 0 );
                    if ( ci.getChanges().hasChanged ( MAILBLOCK ) )
                        ps.setInt ( index++, ci.getSettings().is ( MAILBLOCK ) ? 1 : 0 );
                    if ( ci.getChanges().hasChanged ( LEAVEOPS ) )
                        ps.setInt ( index++, ci.getSettings().is ( LEAVEOPS ) ? 1 : 0 );
                    if ( ci.getChanges().hasChanged ( MODELOCK ) )
                        ps.setString ( index++, ci.getSettings().getModeLock().getModes ( ) );
                    if ( ci.getChanges().hasChanged ( TOPICLOCK ) )
                        ps.setString ( index++, hashToTopiclockString ( ci.getSettings().getTopicLock ( ) ) );
                     
                    if ( ci.getChanges().hasChanged ( MARK ) ) { 
                        if ( ! ci.getSettings().is ( MARKED ) ) {
                            ps.setNull ( index++, Types.VARCHAR );
                        } else {
                            ps.setString ( index++, ci.getSettings().getInstater ( MARK ) );
                        }
                    }
                    if ( ci.getChanges().hasChanged ( FREEZE ) ) { 
                        if ( ! ci.getSettings().is ( FROZEN ) ) {
                            ps.setNull ( index++, Types.VARCHAR );
                        } else {
                            ps.setString ( index++, ci.getSettings().getInstater ( FREEZE ) );
                        }
                    }
                    if ( ci.getChanges().hasChanged ( CLOSE ) ) {
                        if ( ! ci.getSettings().is ( CLOSED ) ) {
                            ps.setNull ( index++, Types.VARCHAR );
                        } else {
                            ps.setString ( index++, ci.getSettings().getInstater ( CLOSE ) );
                        }
                    }
                    if ( ci.getChanges().hasChanged ( HELD ) ) {
                        if ( ! ci.getSettings().is ( HELD ) ) {
                            ps.setNull ( index++, Types.VARCHAR );
                        } else {
                            ps.setString ( index++, ci.getSettings().getInstater ( HOLD ) );
                        }
                    }
                    if ( ci.getChanges().hasChanged ( AUDITORIUM ) ) {
                        if ( ! ci.getSettings().is ( AUDITORIUM ) ) {
                            ps.setNull ( index++, Types.VARCHAR );
                        } else {
                            ps.setString ( index++, ci.getSettings().getInstater ( AUDITORIUM ) );
                        }
                    }
                    
                    ps.setString   ( index++, ci.getString ( NAME ) );
                    ps.executeUpdate ( );
                    ps.close ( );
                    
                } 
                
                if ( ci.getChanges().hasChanged ( TOPIC ) ) {
                    if ( ci.getTopic().getTopic().length() > 0 ) {
                        query = "INSERT INTO topiclog ( name,setter,stamp,topic ) "+
                                "VALUES  ( ?, ?, ?, ? )";
                        ps = sql.prepareStatement ( query );
                        ps.setString ( 1, ci.getName ( ) );
                        ps.setString ( 2, ci.getTopic().getSetter ( ) );
                        ps.setString ( 3, ci.getTopic().getTimeStr ( ) );
                        ps.setString ( 4, ci.getTopic().getTopic ( ) );
                        ps.execute ( );
                        ps.close ( );
                    }
                }
                
                ci.getChanges().clean ( );
                
                idleUpdate ( "updateChan ( ) " );
            } catch  ( SQLException ex )  {
                /* Was not updated? return -1 */
                Proc.log ( CSDatabase.class.getName ( ) , ex );
                return -1;
            }
        }
        /* Nick was added */
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
    private static String hashToTopiclockString ( int hash ) {
        switch ( hash ) {
            case FOUNDER :
                return "founder";
                 
            case SOP :
                return "sop";
                 
            case AOP :
                return "aop";
                 
            default :
                return "off";

        }
    }
      
    public static boolean accesslogEvent ( CSAccessLogEvent log ) {
        
        if ( ! activateConnection ( )  )  {
            return false;
        }
         
        try {
            String query = "insert into chanacclog ( name, target, access, instater, usermask, stamp ) "+
                           "values ( ?, ?, ?, ?, ?, now() ) ";
            ps = sql.prepareStatement ( query );
            ps.setString   ( 1, log.getName() );
            ps.setString   ( 2, log.getTarget() );
            ps.setString   ( 3, log.getFlag() );
            ps.setString   ( 4, log.getInstater() );
            ps.setString   ( 5, log.getUsermask() );
            ps.execute ( );
            ps.close ( ); 
            return true;
            
        } catch ( Exception ex ) {
            Proc.log ( CSDatabase.class.getName ( ) , ex );
        }
        return false;
    }
     
    public static int addChanAccess ( ChanInfo ci, CSAcc op, int access )  {

        if ( ! activateConnection ( )  )  {
            /* No SQL connection */
            return -2;

        } else if ( ci == null )  {
            /* No valid nick was sent */
            return -3;
        } else {
            /* Try add the chan */          
            try {                     
          
                String acc = new String ( );
                switch ( access )  {
                    case AKICK :
                        acc = "akick";
                        break; 
                        
                    case SOP :
                        acc = "sop";
                        break;
                        
                    case AOP :
                        acc = "aop";
                        break;
                        
                    default :
                        
                }
                String query = "INSERT INTO chanaccess ( name,access,nick )  "
                             + "VALUES  ( ?, ?, ? ) "
                             + "ON DUPLICATE KEY "
                             + "UPDATE access = ?";
                ps = sql.prepareStatement ( query );
                ps.setString   ( 1, ci.getName ( )  );
                ps.setString   ( 2, acc );
                ps.setString   ( 3, op.getNick ( ) !=null ? op.getNick().getName ( ) : op.getMask ( ) );
                ps.setString   ( 4, acc );
                ps.execute ( );
                ps.close ( );
                 

                idleUpdate ( "addChanAccess ( ) " );
            } catch  ( SQLException ex )  {
                /* Was not updated? return -1 */
                Proc.log ( CSDatabase.class.getName ( ) , ex );
                return -1;
            }
        }
        /* Nick was added */
        return 1;
    }
  
       
    public static int removeChanAccess ( ChanInfo ci, CSAcc access )  {

        if ( ! activateConnection ( )  )  {
            return -2;

        } else if ( ci == null )  {
            return -3;
            
        } else {
            /* Try add the chan */          
            try {    
                String query = "DELETE FROM chanaccess "
                             + "WHERE name = ? "
                             + "AND nick = ?";
                ps = sql.prepareStatement ( query );
                ps.setString   ( 1, ci.getName ( )  );
                ps.setString   ( 2, access.getNick ( ) !=null ? access.getNick().getName ( ) : access.getMask ( ) );
                ps.execute ( );
                ps.close ( );
                 
                idleUpdate ( "removeChanAccess ( ) " );
            } catch  ( SQLException ex )  {
                /* Was not updated? return -1 */
                Proc.log ( CSDatabase.class.getName ( ) , ex );
                return -1;
            }
        }
        /* Nick was added */
        return 1;
    }
     
    public static ChanInfo getChan ( String name )  {
        ChanInfo ci;
        ChanSetting settings;
        Topic topic;
        String[] buf;
        Config config = Proc.getConf ( );

        // chan  ( name, pass, desc, topic, regstamp, stamp ) 
// chansetting  ( name,keeptopic,topiclock,ident,opguard,restrict,verbose,mailblock,leaveops ) 
        if ( ! activateConnection ( )  )  {
            return null;
        }
        
        try {
            String salt = config.get ( SECRETSALT );
           
            String query = "SELECT name,founder,AES_DECRYPT(pass,?),description,regstamp,stamp "
                         + "FROM chan "
                         + "WHERE name = ?";
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, salt );
            ps.setString  ( 2, name );
            res = ps.executeQuery ( );

            if ( res.next ( )  )  { 
                settings        = getSettings ( res.getString ( 1 )  ); 
                topic           = getChanTopic ( name );
                if ( topic == null )  {
                    topic = new Topic ( "","",Long.parseLong ( "0" )  );
                }
                ci = new ChanInfo ( res.getString ( 1 ), res.getString ( 2 ) ,res.getString ( 3 ),
                                    res.getString ( 4 ), topic, res.getString ( 5 ),
                                    res.getString ( 6 ), settings );

                ci.setAccessList ( SOP, getChanAccess ( ci, SOP )  );
                ci.setAccessList ( AOP, getChanAccess ( ci, AOP )  );
                ci.setAccessList ( AKICK, getChanAccess ( ci, AKICK )  );

                res.close ( );
                ps.close ( );
                return ci;

            }
            res.close ( );
            ps.close ( );
            idleUpdate ( "getChan ( ) " );
            
        } catch  ( SQLException | NumberFormatException ex )  {
            Proc.log ( CSDatabase.class.getName ( ) , ex );    
        }
        
        return null;
    }
    
    /* Only used internally */
    public static ArrayList<ChanInfo> getChansByNick ( NickInfo ni )  {
        ChanInfo ci;
        ArrayList<ChanInfo> cList = new ArrayList<> ( );
        ChanSetting settings;
        Topic topic;
        String[] buf;
        Config config = Proc.getConf ( );
        if ( ! activateConnection ( )  )  {
            return cList;
        }
        
        try {
            String salt = config.get ( SECRETSALT );
           
            String query = "SELECT C.name,C.founder,AES_DECRYPT(C.pass,?),C.description,C.regstamp,C.stamp "
                         + "FROM chan AS C "
                         + "LEFT JOIN chanaccess AS CA ON CA.name = C.name "
                         + "WHERE CA.nick = ?";
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, salt );
            ps.setString  ( 2, ni.getName ( ) );
            res = ps.executeQuery ( );

            while ( res.next ( )  )  { 
                settings        = getSettings ( res.getString ( 1 )  ); 

                ci = new ChanInfo ( res.getString ( 1 ), res.getString ( 2 ), res.getString ( 3 ),
                                    res.getString ( 4 ), new Topic ( "","",0 ), res.getString ( 5 ),
                                    res.getString ( 6 ), settings );
                ci.setAccessList ( SOP, getChanAccess ( ci, SOP )  );
                ci.setAccessList ( AOP, getChanAccess ( ci, AOP )  );
                ci.setAccessList ( AKICK, getChanAccess ( ci, AKICK )  );
                cList.add ( ci ); 
            }
            res.close ( );
            ps.close ( );
            idleUpdate ( "getChansByNick ( ) " );
            
        } catch  ( SQLException | NumberFormatException ex )  {
            Proc.log ( CSDatabase.class.getName ( ) , ex );    
        } 
        return cList;
    }
    
    public static ArrayList<NickChanAccess> getNickChanAccessByNick ( NickInfo ni, String access )  {
        ArrayList<NickChanAccess> ncaList = new ArrayList<> ( );
        if ( ! activateConnection ( )  )  {
            return ncaList;
        }
        try {
            String query = "SELECT N.name,CA.name,CA.access "+
                           "FROM nick AS N "+
                           "JOIN chanaccess AS CA ON CA.nick = N.name "+
                           "WHERE N.name = ? "+
                           "AND CA.access = ?";
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, ni.getName ( )  );
            ps.setString  ( 2, access );
            res = ps.executeQuery ( );
            while  ( res.next ( )  )  {
                ncaList.add ( new NickChanAccess ( res.getString ( 1 ) , res.getString ( 2 ) , res.getString ( 3 )  )  );
            }
            res.close ( );
            ps.close ( );
            idleUpdate ( "getNickChanAccessByNick ( ) " );
        } catch  ( SQLException | NumberFormatException ex )  {
            Proc.log ( CSDatabase.class.getName ( ) , ex );     
        }
        return ncaList;
    }
    
     /* Only used internally */
    public static ArrayList<ChanInfo> getChansByFounderNick ( NickInfo ni )  {
        ChanInfo ci;
        ArrayList<ChanInfo> cList = new ArrayList<> ( );
        ChanSetting settings;
        Topic topic;
        String[] buf;
        Config config = Proc.getConf ( );
        if ( ! activateConnection ( )  )  {
            return cList;
        }
        try {    
            String salt = config.get ( SECRETSALT );
            String query = "SELECT C.name,C.founder,AES_DECRYPT(C.pass,?),C.description,C.regstamp,C.stamp "
                         + "FROM chan AS C "
                         + "WHERE C.founder = ?";
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, salt );
            ps.setString  ( 2, ni.getName ( ) );
            res = ps.executeQuery ( );

            while ( res.next ( )  )  { 
                settings        = getSettings ( res.getString ( 1 )  ); 

                ci = new ChanInfo ( res.getString ( 1 ), res.getString ( 2 ), res.getString ( 3 ),
                                    res.getString ( 4 ), new Topic ( "","",0 ), res.getString ( 5 ),
                                    res.getString ( 6 ), settings );
                ci.setAccessList ( SOP, getChanAccess ( ci, SOP )  );
                ci.setAccessList ( AOP, getChanAccess ( ci, AOP )  );
                ci.setAccessList ( AKICK, getChanAccess ( ci, AKICK )  );
                cList.add ( ci ); 
            }
            res.close ( );
            ps.close ( );
            idleUpdate ( "getChansByNick ( ) " );
            
        } catch  ( SQLException | NumberFormatException ex )  {
            Proc.log ( CSDatabase.class.getName ( ) , ex );    
        } 
        return cList;
    }
    
    public static Topic getChanTopic ( String name )  {
        Topic topic = null;
        String[] buf;
        
        System.out.println("0:");
        if ( ! activateConnection ( )  )  {
            return topic;
        }
        
        String query;
        try { 
            query = "SELECT topic,setter,unix_timestamp(stamp) "+
                    "FROM topiclog "+
                    "WHERE name = ? "+
                    "order by stamp desc "+
                    "limit 1";
            ps = sql.prepareStatement ( query );
            ps.setString ( 1, name );
            res3 = ps.executeQuery ( );
            
            if ( res3.next ( )  ) {
                topic = new Topic ( 
                        res3.getString ( 1 ), 
                        res3.getString ( 2 ),
                        Long.parseLong ( res3.getString ( 3 ) )
                ); 
            }
            
            res3.close ( );
            ps.close ( );
            idleUpdate ( "getChanTopic ( ) " );
            
        } catch  ( SQLException | NumberFormatException ex )  {
            Proc.log ( CSDatabase.class.getName ( ) , ex );    
            return null;
        }
        return topic;
    }
     
    public static ArrayList<CSAcc> getChanAccess ( ChanInfo ci, int access )  {
        String[] buf;
        ArrayList<CSAcc> opList = new ArrayList<> ( );
        if ( ! activateConnection ( )  )  {
            return opList;
        }
        try {
            CSAcc chanOp;
            String acc = new String ( );
            switch ( access )  {
                case AKICK :
                    acc = "akick";
                    break;

                case SOP :
                    acc = "sop";
                    break;

                case AOP :
                    acc = "aop";
                    break;

                default :
            } 

            String query = "SELECT nick "
                         + "FROM chanaccess "
                         + "WHERE name = ? "
                         + "AND access = ?";
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, ci.getName ( )  );
            ps.setString  ( 2, acc );
            res3 = ps.executeQuery ( );

            while ( res3.next ( )  )  { 
                try {
                    NickInfo ni = NickServ.findNick( res3.getString ( 1 ) );
                    if ( ni != null ) {
                        chanOp = new CSAcc ( ni, access );
                    } else {
                        System.out.println("DEBUG!!!!!: "+res3.getString ( 1 ) );
                        chanOp = new CSAcc ( res3.getString ( 1 ), access );
                    }
                    opList.add ( chanOp );

                } catch ( SQLException | NumberFormatException e )  {
                    Proc.log ( CSDatabase.class.getName ( ) , e );
                }
            }
            res3.close ( );
            ps.close ( );
            idleUpdate ( "getChanAccess ( ) " );
            return opList;
            
        } catch  ( SQLException ex )  {
            Proc.log ( CSDatabase.class.getName ( ) , ex );
        } 
        return null;
    }
    
   
    /* Delete a channel */
    public static boolean deleteChan ( ChanInfo ci )  {
        if ( ! activateConnection ( )  )  {
            return false;
        }
        
        try {
            String query = "DELETE FROM chan WHERE name = ?";
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, ci.getName ( )  );
            ps.execute ( );
            ps.close ( );

            query = "DELETE FROM chanaccess WHERE name = ?";
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, ci.getName ( )  );
            ps.execute ( );
            ps.close ( );

            query = "DELETE FROM chansetting WHERE name = ?";
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, ci.getName ( )  );
            ps.execute ( );
            ps.close ( );
             
        } catch  ( SQLException ex )  {
            Proc.log ( CSDatabase.class.getName ( ) , ex );    
            return false;
        }
        return true;

     
    }
    /* End NickServ */

    private static ChanSetting getSettings ( String name )  {
        ChanSetting settings;
        settings = new ChanSetting ( );
        if ( ! activateConnection ( )  )  {
            return settings;
        }
        try {
            String query = "SELECT keeptopic,topiclock,ident,opguard,"+
                           "restricted,verbose,mailblock,leaveops,"+
                           "modelock,mark,freeze,close,hold,auditorium "+
                           "FROM chansetting "+
                           "WHERE name = ?;";
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, name );
            res2 = ps.executeQuery ( );

            if ( res2.next ( )  )  {
                if ( res2.getBoolean ( "keeptopic" )  == true )     { settings.set ( KEEPTOPIC, true ); }
                switch ( res2.getString ( "topiclock" ) .toUpperCase ( ) .hashCode ( )  )  {
                    case FOUNDER :
                    case SOP :
                    case AOP :
                        settings.set( TOPICLOCK, res2.getString ( "topiclock" ) .toUpperCase ( ) .hashCode ( ) );
                        break;

                    default :
                        settings.set ( TOPICLOCK, OFF );

                }

                settings.set ( IDENT,       res2.getBoolean ( "ident" )         );
                settings.set ( OPGUARD,     res2.getBoolean ( "opguard" )       );
                settings.set ( RESTRICT,    res2.getBoolean ( "restricted" )    );
                settings.set ( VERBOSE,     res2.getBoolean ( "verbose" )       );
                settings.set ( MAILBLOCK,   res2.getBoolean ( "mailblock" )     );
                settings.set ( LEAVEOPS,    res2.getBoolean ( "leaveops" )      );
                /* Oper only */
                settings.set ( MARK,        res2.getString ( "mark" )           );
                settings.set ( FREEZE,      res2.getString ( "freeze" )         );
                settings.set ( CLOSE,       res2.getString ( "close" )          );
                settings.set ( HOLD,        res2.getString ( "hold" )           );
                settings.set ( AUDITORIUM,  res2.getString ( "auditorium" )     );
                settings.setModeLock ( res2.getString ( "modelock" )            );

            }
            res2.close ( );
            ps.close ( );
            idleUpdate ( "getSettings ( chan ) " );
             
        } catch  ( SQLException ex )  {
            Proc.log ( CSDatabase.class.getName ( ) , ex );    
        } 
        return settings;
    }
  
    static boolean wipeAccessList ( ChanInfo ci, int access )  {
        if ( ! activateConnection ( )  )  {
            return false;
        }
        try {
            String acc = new String ( );
            switch ( access )  {
                case SOP :
                    acc = "sop";
                    break;

                case AOP :
                    acc = "aop";
                    break;

                default :

            }

            String query = "DELETE FROM chanaccess "
                         + "WHERE name = ? "
                         + "AND access = ?";
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, ci.getName ( )  );
            ps.setString  ( 2, acc );
            ps.execute ( );
            ps.close ( );

            idleUpdate ( "wipeAccessList ( ) " );
            
        } catch  ( SQLException ex )  {
            Proc.log ( CSDatabase.class.getName ( ) , ex );    
            return false;
        }
        return true;
    } 
 
    static ArrayList<ChanInfo> getAllChans ( )  {
        ChanInfo ci;
        ArrayList<ChanInfo> cList = new ArrayList<> ( );
        ChanSetting settings;
        Topic topic;
        String[] buf;
        Config config = Proc.getConf ( );
        long now;
        long now2;
        if ( ! activateConnection ( )  )  {
            return cList;
        }
        try { 
            now = System.currentTimeMillis();
            String salt = config.get ( SECRETSALT );
            String query = "SELECT name,founder,AES_DECRYPT(pass,?) as pass,description,regstamp,stamp "
                         + "FROM chan " 
                         + "ORDER BY name";
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, salt ); 
            res = ps.executeQuery ( );

            System.out.print("Loading Chans: ");
            int $count = 0;
            while ( res.next ( ) )  { 
                settings        = getSettings ( res.getString ( 1 )  ); 
                ci = new ChanInfo ( res.getString ( 1 ), res.getString ( 2 ), res.getString ( 3 ),
                                    res.getString ( 4 ), getChanTopic ( res.getString ( 1 ) ),
                                    res.getString ( 5 ), res.getString ( 6 ), settings );
                ci.setAccessList ( SOP, getChanAccess ( ci, SOP ) );
                ci.setAccessList ( AOP, getChanAccess ( ci, AOP ) );
                ci.setAccessList ( AKICK, getChanAccess ( ci, AKICK ) );
                ci.getFounder().addToAccessList ( FOUNDER, ci );
                cList.add ( ci );
                $count++;
            }
            now2 = System.currentTimeMillis();
            System.out.print(".. "+$count+" chans loaded [took "+(now2-now)+"ms]\n");
            res.close ( );
            ps.close ( );
            idleUpdate ( "getAllChans ( ) " );
            
        } catch  ( SQLException | NumberFormatException ex )  {
            Proc.log ( CSDatabase.class.getName ( ) , ex );    
        } 
        return cList;
    
    }
    
    public static ArrayList<ChanInfo> getChanList ( String pattern )  {
        ArrayList<ChanInfo> cList = new ArrayList<> ( );
        ChanInfo ci; 
        ChanSetting settings;
        Config config = Proc.getConf ( );
        
        if ( pattern.isEmpty ( )  )  {
            return null;
        }
        
        pattern.replaceAll ( "\\'", "" );
        pattern = pattern.replaceAll ( "\\*", " ( .* ) " );
        pattern = pattern.replaceAll ( "\\?", " ( .? ) {0,1}" );
        
//public ChanInfo ( String name, String founder, String pass, String desc, Topic topic, long regStamp, long lastSeen, ChanSetting settings, ChanFlags flags )  {
        if ( ! activateConnection ( ) ) {
            return cList;
        }
        try { 
            String salt = config.get ( SECRETSALT );
            String query = "SELECT C.name,C.founder,AES_DECRYPT(C.pass,?),C.description,C.regstamp,C.stamp "
                         + "FROM chan AS C "
                         + "WHERE C.name RLIKE ? "
                         + "OR C.description RLIKE ? "
                         + "ORDER BY C.name ASC";
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, salt );
            ps.setString  ( 2, "^"+pattern+"$" );
            ps.setString  ( 3, "^"+pattern+"$" );
            res = ps.executeQuery ( );

            while ( res.next ( )  )  { 
                settings        = getSettings ( res.getString ( 1 )  ); 
                ci = new ChanInfo ( res.getString ( 1 ) , res.getString ( 2 ) , res.getString ( 3 ), 
                                    res.getString ( 4 ) , getChanTopic ( res.getString ( 1 ) ), res.getString ( 5 ), 
                                    res.getString ( 6 ), settings );
                ci.setAccessList ( SOP, getChanAccess ( ci, SOP ) );
                ci.setAccessList ( AOP, getChanAccess ( ci, AOP ) );
                ci.setAccessList ( AKICK, getChanAccess ( ci, AKICK ) );
                ci.getFounder().addToAccessList ( FOUNDER, ci);
                cList.add ( ci ); 
            } 
            res.close ( );
            ps.close ( );
             
        } catch  ( Exception ex )  {
            Proc.log ( CSDatabase.class.getName ( ) , ex );
        }
        return cList;
    }
 
    public static ArrayList<CSAccessLogEvent> getChanAccLogList ( User user, ChanInfo ci ) {
        ArrayList<CSAccessLogEvent> csaList = new ArrayList<>();
        String query;
        if ( ci == null || user == null ) {
            return csaList;            
        }
        if ( ! activateConnection ( ) ) {
            return csaList;
        }
        // | id | name              | target       | access | instater    | stamp               |
        try {
            if ( user.isAtleast ( IRCOP ) ) {
                query = "select ca.name,ca.access,ca.target,ca.instater,ca.usermask,ca.stamp "+
                           "from chanacclog as ca "+
                           "left join chan as c on c.name = ca.name "+
                           "where "+
                           "ca.name = ? ";
            } else {
                query = "select ca.name,ca.access,ca.target,ca.instater,ca.usermask,ca.stamp "+
                           "from chanacclog as ca "+
                           "left join chan as c on c.name = ca.name "+
                           "where "+
                           "ca.name = ? "+
                           "and ca.stamp >= c.regstamp";
            }
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, ci.getName() );
            res = ps.executeQuery ( );
            
            while ( res.next ( ) ) {
                csaList.add( new CSAccessLogEvent(res.getString ( 1 ), res.getString ( 2 ), res.getString ( 3 ), res.getString ( 4 ), res.getString ( 5 ), res.getString ( 6 ) ) );
            }
            res.close ( );
            ps.close ( );
            
        } catch ( Exception ex ) {
            Proc.log ( CSDatabase.class.getName ( ) , ex );
        }
        
        return csaList;
    }
    
    
    public static int logEvent ( CSLogEvent log ) {
        return Database.logEvent ( "chanlog", ( LogEvent ) log );
    }
    
    public static void delLogEvent ( int id ) {
        Database.delLogEvent ( "chanlog", id );
    }


}
