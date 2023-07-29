/* 
 * Copyright (C) 2018 Fredrik Karlsson aka DreamHealer - avade.net
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
import core.Database;
import core.HashString;
import core.Proc;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import nickserv.NickInfo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nickserv.NickServ;
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
| autoakick  | tinyint ( 1 )   | YES  |     | NULL    |       |
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

    /**
     *
     * @param ci
     * @return
     */

    public static int createChan ( ChanInfo ci )  {
        if ( ! activateConnection ( )  )  {
            return -2;
        } else if ( ci == null ) {
            return -3;
        } else {
            
            try {
                HashString salt = Proc.getConf().get ( SECRETSALT );
                String query = "insert into chan ( name, founder, pass, description, regstamp, stamp )  "
                             + "values ( ?, ?, AES_ENCRYPT(?,?), ?, ?, ? )";
                ps = sql.prepareStatement ( query );
                ps.setString  ( 1, ci.getString ( NAME ) );
                ps.setString  ( 2, ci.getFounder().getName().getString()  );
                ps.setString  ( 3, ci.getPass ( ) );
                ps.setString  ( 4, salt.getString() );             
                ps.setString  ( 5, ci.getString ( DESCRIPTION )  );
                ps.setString  ( 6, ci.getString ( REGTIME ) );
                ps.setString  ( 7, ci.getString ( LASTUSED ) );
                ps.execute ( );
                ps.close ( );
                 
                query = "insert into chanflag "+
                        "(name,join_connect_time,talk_connect_time,talk_join_time,"+
                        "max_bans,no_notice,no_ctcp,no_part_msg,no_quit_msg,"+
                        "exempt_opped,exempt_voiced,exempt_identd,exempt_registered,"+
                        "exempt_invites,greetmsg) "+
                        "values (?,0,0,0,200,0,0,0,0,0,0,0,0,0,null)";
                ps = sql.prepareStatement ( query );
                ps.setString  ( 1, ci.getString ( NAME ) );
                ps.execute ( );
                ps.close ( );
                
                
                
                query = "insert into chansetting "+
                        "values (?,1,'OFF',1,1,0,0,0,0,0,'+nt',null,null,null,null,null)";
                ps = sql.prepareStatement ( query );
                ps.setString  ( 1, ci.getString ( NAME ) );
                ps.execute ( );
                ps.close ( );
                
                idleUpdate ( "createChan ( ) " );
            } catch  ( SQLException ex )  {
                /* Nick already exists? return -1 */
                Proc.log ( CSDatabase.class.getName ( ), ex );
                return -1;
            }
        }
        /* Nick was added */
        return 1;
    }
     
    /* NickServ Methods */

    
    private static String compileSettingChanges ( ChanInfo ci ) {
        String changes = "";
        if ( ci.getChanges().hasChanged ( KEEPTOPIC ) ) {
            changes = addToQuery ( changes, "keeptopic" );
        }
        if ( ci.getChanges().hasChanged ( IDENT ) ) {
            changes = addToQuery ( changes, "ident" );
        }
        if ( ci.getChanges().hasChanged ( OPGUARD ) ) {
            changes = addToQuery ( changes, "opguard" );
        }
        if ( ci.getChanges().hasChanged ( RESTRICT ) ) {
            changes = addToQuery ( changes, "restricted" );
        }
        if ( ci.getChanges().hasChanged ( VERBOSE ) ) {
            changes = addToQuery ( changes, "verbose" );
        }
        if ( ci.getChanges().hasChanged ( MAILBLOCK ) ) {
            changes = addToQuery ( changes, "mailblock" );
        }
        if ( ci.getChanges().hasChanged ( LEAVEOPS ) ) {
            changes = addToQuery ( changes, "leaveops" );
        }
        if ( ci.getChanges().hasChanged ( AUTOAKICK ) ) {
            changes = addToQuery ( changes, "autoakick" );
        }
        if ( ci.getChanges().hasChanged ( MODELOCK ) ) {
            changes = addToQuery ( changes, "modelock" );
        }
        if ( ci.getChanges().hasChanged ( TOPICLOCK ) ) {
            changes = addToQuery ( changes, "topiclock" );
        }
        if ( ci.getChanges().hasChanged ( MARK ) ) {
            changes = addToQuery ( changes, "mark" );
        }
        if ( ci.getChanges().hasChanged ( FREEZE ) ) {
            changes = addToQuery ( changes, "freeze" );
        }
        if ( ci.getChanges().hasChanged ( CLOSE ) ) {
            changes = addToQuery ( changes, "close" );
        }
        if ( ci.getChanges().hasChanged ( HOLD ) ) {
            changes = addToQuery ( changes, "hold" );
        }
        if ( ci.getChanges().hasChanged ( AUDITORIUM ) ) {
            changes = addToQuery ( changes, "auditorium" );
        }
        return changes;
    }
    
    private static String compileFlagChanges ( ChanInfo ci ) {
        String changes = "";
        if ( ci.getChanges().hasChanged ( JOIN_CONNECT_TIME ) ) {
            changes = addToQuery ( changes, "join_connect_time" );
        }
        if ( ci.getChanges().hasChanged ( TALK_CONNECT_TIME ) ) {
            changes = addToQuery ( changes, "talk_connect_time" );
        }
        if ( ci.getChanges().hasChanged ( TALK_JOIN_TIME ) ) {
            changes = addToQuery ( changes, "talk_join_time" );
        }
        if ( ci.getChanges().hasChanged ( MAX_BANS ) ) {
            changes = addToQuery ( changes, "max_bans" );
        }
        if ( ci.getChanges().hasChanged ( NO_NOTICE ) ) {
            changes = addToQuery ( changes, "no_notice" );
        }
        if ( ci.getChanges().hasChanged ( NO_CTCP ) ) {
            changes = addToQuery ( changes, "no_ctcp" );
        }
        if ( ci.getChanges().hasChanged ( NO_PART_MSG ) ) {
            changes = addToQuery ( changes, "no_part_msg" );
        }
        if ( ci.getChanges().hasChanged ( EXEMPT_OPPED ) ) {
            changes = addToQuery ( changes, "exempt_opped" );
        }
        if ( ci.getChanges().hasChanged ( EXEMPT_VOICED ) ) {
            changes = addToQuery ( changes, "exempt_voiced" );
        }
        if ( ci.getChanges().hasChanged ( EXEMPT_IDENTD ) ) {
            changes = addToQuery ( changes, "exempt_identd" );
        }
        if ( ci.getChanges().hasChanged ( EXEMPT_REGISTERED ) ) {
            changes = addToQuery ( changes, "exempt_registered" );
        }
        if ( ci.getChanges().hasChanged ( EXEMPT_INVITES ) ) {
            changes = addToQuery ( changes, "exempt_invites" );
        }
        if ( ci.getChanges().hasChanged ( GREETMSG ) ) {
            changes = addToQuery ( changes, "greetmsg" );
        }
        return changes;
    }
    
    private static int updateChanInfo ( ChanInfo ci ) {
        HashString salt = Proc.getConf().get ( SECRETSALT );
        String query = "update chan set founder = ?, pass = aes_encrypt(?,?), description = ?, stamp = ? where name = ?";
        try {
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, ci.getFounder().getNameStr() );
            ps.setString  ( 2, ci.getPass ( ) );
            ps.setString  ( 3, salt.getString() );
            ps.setString  ( 4, ci.getString ( DESCRIPTION ) );
            ps.setString  ( 5, ci.getString ( LASTUSED ) );
            ps.setString  ( 6, ci.getNameStr() ); 
            ps.executeUpdate ( );
            ps.close ( );
        } catch  ( SQLException ex )  {
            /* Was not updated? return -1 */
            Proc.log ( CSDatabase.class.getName ( ) , ex );
            return -1;
        }
        return 1;
    }
    private static int updateChanSettings ( ChanInfo ci, String changes ) {
        int index = 1;
        String query = "update chansetting set "+changes+" where name = ?";

        try {
            ps = sql.prepareStatement ( query );
            if ( ci.getChanges().hasChanged ( KEEPTOPIC ) ) {
                ps.setBoolean ( index++, ci.getSettings().is ( KEEPTOPIC ) );
            }
            if ( ci.getChanges().hasChanged ( IDENT ) ) {
                ps.setBoolean ( index++, ci.getSettings().is ( IDENT ) );
            }
            if ( ci.getChanges().hasChanged ( OPGUARD ) ) {
                ps.setBoolean ( index++, ci.getSettings().is ( OPGUARD ) );
            }
            if ( ci.getChanges().hasChanged ( RESTRICT ) ) {
                ps.setBoolean ( index++, ci.getSettings().is ( RESTRICT ) );
            }
            if ( ci.getChanges().hasChanged ( VERBOSE ) ) {
                ps.setBoolean ( index++, ci.getSettings().is ( VERBOSE ) );
            }
            if ( ci.getChanges().hasChanged ( MAILBLOCK ) ) {
                ps.setBoolean ( index++, ci.getSettings().is ( MAILBLOCK ) );
            }
            if ( ci.getChanges().hasChanged ( LEAVEOPS ) ) {
                ps.setBoolean ( index++, ci.getSettings().is ( LEAVEOPS ) );
            } 
            if ( ci.getChanges().hasChanged ( AUTOAKICK ) ) {
                ps.setBoolean ( index++, ci.getSettings().is ( AUTOAKICK ) );
            }
            if ( ci.getChanges().hasChanged ( MODELOCK ) ) {
                ps.setString ( index++, ci.getSettings().getModeLock().getModes ( ) );
            }
            if ( ci.getChanges().hasChanged ( TOPICLOCK ) ) {
                ps.setString ( index++, hashToTopiclockString ( ci.getSettings().getTopicLock ( ) ) );
            }

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

            ps.setString   ( index, ci.getString ( NAME ) );
            ps.executeUpdate ( );
            ps.close ( );
        } catch  ( SQLException ex )  {
            /* Was not updated? return -1 */
            Proc.log ( CSDatabase.class.getName ( ) , ex );
            return -1;
        }
        return 1;
    }
    
    private static int addTopicLog ( ChanInfo ci ) {
        String query = "insert into topiclog ( name,setter,stamp,topic ) values ( ?, ?, ?, ? )";
        try {
            ps = sql.prepareStatement ( query );
            ps.setString ( 1, ci.getName().getString() );
            ps.setString ( 2, ci.getTopic().getSetter ( ) );
            ps.setString ( 3, ci.getTopic().getTimeStr ( ) );
            ps.setString ( 4, ci.getTopic().getText ( ) );
            ps.execute ( );
            ps.close ( );
        } catch  ( SQLException ex )  {
            /* Was not updated? return -1 */
            Proc.log ( CSDatabase.class.getName ( ) , ex );
            return -1;
        }
        return 1;
    }
    private static int updateFlagChanges ( ChanInfo ci, String changes ) {
        int index = 1;
        CSFlag cf = ci.getChanFlag ( );
        String query = "update chanflag set "+changes+" where name = ?";

        try {
            ps = sql.prepareStatement ( query );
            if ( ci.getChanges().hasChanged ( JOIN_CONNECT_TIME ) ) {
                ps.setShort ( index++, cf.getJoinconnecttime ( ) );
            }
            if ( ci.getChanges().hasChanged ( TALK_CONNECT_TIME ) ) {
                ps.setShort ( index++, cf.getTalkconnecttime ( ) );
            }
            if ( ci.getChanges().hasChanged ( TALK_JOIN_TIME ) ) {
                ps.setShort ( index++, cf.getTalkjointime ( ) );
            }
            if ( ci.getChanges().hasChanged ( MAX_BANS ) ) {
                ps.setShort ( index++, cf.getMaxbans ( ) );
            }
            if ( ci.getChanges().hasChanged ( NO_NOTICE ) ) {
                ps.setBoolean ( index++, cf.isNonotice ( ) );
            }
            if ( ci.getChanges().hasChanged ( NO_CTCP ) ) {
                ps.setBoolean ( index++, cf.isNoctcp ( ) );
            }
            if ( ci.getChanges().hasChanged ( NO_PART_MSG ) ) {
                ps.setBoolean ( index++, cf.isNopartmsg ( ) );
            }
            if ( ci.getChanges().hasChanged ( EXEMPT_OPPED ) ) {
                ps.setBoolean ( index++, cf.isExemptopped ( ) );
            }
            if ( ci.getChanges().hasChanged ( EXEMPT_VOICED ) ) {
                ps.setBoolean ( index++, cf.isExemptvoiced ( ) );
            }
            if ( ci.getChanges().hasChanged ( EXEMPT_IDENTD ) ) {
                ps.setBoolean ( index++, cf.isExemptidentd ( ) );
            }
            if ( ci.getChanges().hasChanged ( EXEMPT_REGISTERED ) ) {
                ps.setBoolean ( index++, cf.isExemptregistered ( ) );
            }
            if ( ci.getChanges().hasChanged ( EXEMPT_INVITES ) ) {
                ps.setBoolean ( index++, cf.isExemptinvites ( ) );
            }

            if ( ci.getChanges().hasChanged ( GREETMSG ) ) {
                if ( ! cf.isGreetmsg ( ) ) {
                    ps.setNull ( index++, Types.VARCHAR );
                } else {
                    ps.setString ( index++, cf.getGreetmsg ( ) );
                }
            }
            ps.setString ( index, ci.getString ( NAME ) );
            ps.executeUpdate ( );
            ps.close ( );
        } catch  ( SQLException ex )  {
            /* Was not updated? return -1 */
            Proc.log ( CSDatabase.class.getName ( ) , ex );
            return -1;
        }
        return 1;
    }
    /**
     *
     * @param ci
     * @return
     */

    public static int updateChan ( ChanInfo ci )  { 
        if ( ! activateConnection ( )  )  {
            /* No SQL connection */
            return -2;
        } else if ( ci == null ) {
            return -3;
        } else {

            if ( ci.getChanges().hasChanged ( FOUNDER ) ||
                 ci.getChanges().hasChanged ( DESCRIPTION ) ||
                 ci.getChanges().hasChanged ( LASTUSED ) ) {
                updateChanInfo ( ci );
            }
            
            String changes = compileSettingChanges ( ci );

            if ( changes.length() > 0 ) {
                updateChanSettings ( ci, changes );
            } 
                
            if ( ci.getChanges().hasChanged ( TOPIC ) && 
                    ci.getTopic() != null && 
                    ci.getTopic().getText() != null && 
                    ci.getTopic().getText().length() > 0 ) {
                addTopicLog ( ci );
            }
                
            changes = compileFlagChanges ( ci );

            if ( changes.length() > 0 ) {
                updateFlagChanges ( ci, changes );
            }

            ci.getChanges().cleanUp ( );

            idleUpdate ( "updateChan ( ) " );
        
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
    private static String hashToTopiclockString ( HashString it ) {
        if      ( it.is(FOUNDER) )          { return "founder";                 }
        else if ( it.is(SOP) )              { return "sop";                     }
        else if ( it.is(AOP) )              { return "aop";                     }
        else {
            return "off";
        }
    }
      
    /**
     *
     * @param log
     * @return
     */
    public static boolean accesslogEvent ( CSAccessLogEvent log ) {
        
        if ( ! activateConnection ( )  )  {
            return false;
        }
         
        try {
            String query = "insert into chanacclog ( name, target, access, instater, usermask, stamp ) "+
                           "values ( ?, ?, ?, ?, ?, now() ) ";
            ps = sql.prepareStatement ( query );
            ps.setString   ( 1, log.getNameStr() );
            ps.setString   ( 2, log.getTarget() );
            ps.setString   ( 3, log.getFlagStr() );
            ps.setString   ( 4, log.getInstater() );
            ps.setString   ( 5, log.getUsermask() );
            ps.execute ( );
            ps.close ( ); 
            return true;
            
        } catch ( SQLException ex ) {
            Proc.log ( CSDatabase.class.getName ( ) , ex );
        }
        return false;
    }
     
    /**
     *
     * @param ci
     * @param acc
     * @return
     */
    public static int updateChanAccessLastOped ( ChanInfo ci, CSAcc acc ) {
        String query;
        String target;
        if ( ! activateConnection ( ) ) {
            /* No SQL connection */
            return -2;

        } else if ( ci == null )  {
            /* No valid chan was sent */
            return -3;
        } else {
            try {
                if ( acc.isNick() ) {
                    query = "update chanaccess set lastoped = ? where name = ? and nick = ?";
                    target = acc.getNick().getNameStr();
                } else {
                    query = "update chanaccess_mask set lastoped = ? where name = ? and mask = ?";
                    target = acc.getMaskStr();
                }
                
                ps = sql.prepareStatement ( query );
                ps.setString   ( 1, acc.getLastOped() );
                ps.setString   ( 2, ci.getName().getString() );
                ps.setString   ( 3, target );
                ps.execute ( );
                ps.close ( );
                 
                idleUpdate ( "updateChanAccessLastOped ( ) " );
            } catch  ( SQLException ex )  {
                /* Was not updated? return -1 */
                Proc.log ( CSDatabase.class.getName ( ), ex );
                return -1;
            }
            
        }
        return 1;
    }

    /**
     *
     * @param ci
     * @param op
     * @return
     */
    public static int addChanAccess ( ChanInfo ci, CSAcc op )  {
        HashString access = op.getAccess();
        if ( ! activateConnection ( )  )  {
            /* No SQL connection */
            return -2;

        } else if ( ci == null )  {
            /* No valid nick was sent */
            return -3;
        } else {
            /* Try add the chan */          
            try {                     
          
                String acc = "";
                if ( access.is(AKICK) ) {
                    acc = "akick";
                } else if ( access.is(SOP) ) {
                    acc = "sop";
                } else if ( access.is(AOP) ) {
                    acc = "aop";
                }
                 
                String query;
                String target;
                if ( op.isNick() ) {
                    query = "insert into chanaccess ( name, access, nick ) "+
                            "values ( ?, ?, ? ) "+
                            "on duplicate key "+
                            "update access = ?";
                    target = op.getNick().getNameStr();
                } else {
                    query = "insert into chanaccess_mask ( name, access, mask ) "+
                            "values ( ?, ?, ? ) "+
                            "on duplicate key "+
                            "update access = ?";
                    target = op.getMaskStr();
                }
                
                ps = sql.prepareStatement ( query );
                ps.setString   ( 1, ci.getName().getString() );
                ps.setString   ( 2, acc );
                ps.setString   ( 3, target );
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
  
    /**
     *
     * @param ci
     * @param access
     * @return
     */
    public static int removeChanAccess ( ChanInfo ci, CSAcc access )  {
        String query;
        String target;
        if ( ! activateConnection ( )  )  {
            return -2;

        } else if ( ci == null )  {
            return -3;
            
        } else {
            /* Try add the chan */          
            try {
                if ( access.isNick() ) {
                    query = "delete from chanaccess where name = ? and nick = ?";
                    target = access.getNick().getNameStr();
                } else {
                    query = "delete from chanaccess_mask where name = ? and mask = ?";
                    target = access.getMaskStr();
                }
                
                ps = sql.prepareStatement ( query );
                ps.setString   ( 1, ci.getName().getString() );
                ps.setString   ( 2, target );
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
    
    /**
     *
     * @param ni
     * @param access
     * @return
     */
    public static List<NickChanAccess> getNickChanAccessByNick ( NickInfo ni, String access )  {
        ArrayList<NickChanAccess> ncaList = new ArrayList<> ( );
        if ( ! activateConnection ( )  )  {
            return ncaList;
        }
        try {
            String query = "select n.name,ca.name,ca.access "+
                           "from nick as n "+
                           "join chanaccess as ca on ca.nick = n.name "+
                           "where n.name = ? "+
                           "and ca.access = ?";
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, ni.getName().getString() );
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
 
    /**
     *
     * @param name
     * @return
     */
    public static Topic getChanTopic ( String name )  {
        Topic topic = null;
       
        if ( ! activateConnection ( )  )  {
            return topic;
        }
        
        String query;
        try { 
            query = "select topic,setter,unix_timestamp(stamp) from topiclog where name = ? order by stamp desc limit 1";
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
     
    static ArrayList<Topic> getTopicList(ChanInfo ci) {
        ArrayList<Topic> tList = new ArrayList<>();
        if ( ! activateConnection ( )  )  {
            return tList;
        }
        
        String query;
        try {
            query = "select topic,setter,unix_timestamp(stamp),stamp from topiclog where name = ? order by stamp asc limit 100";
            ps = sql.prepareStatement ( query );
            ps.setString ( 1, ci.getName().getString() );
            res3 = ps.executeQuery ( );
            while ( res3.next( ) ) {
                tList.add(
                    new Topic (
                        res3.getString ( 1 ), 
                        res3.getString ( 2 ),
                        Long.parseLong ( res3.getString ( 3 ) ),
                        res3.getString ( 4 )
                    )
                );
            }
            res3.close ( );
            ps.close ( );
            idleUpdate ( "getTopicList ( ) " );
            
        } catch ( NumberFormatException | SQLException ex ) {
            Proc.log ( CSDatabase.class.getName ( ) , ex );
        }
        return tList;
    }

    /**
     *
     * @param ci
     * @param access
     * @return
     */
    public static HashMap<BigInteger,CSAcc> getChanAccess ( ChanInfo ci, HashString access )  {
        NickInfo ni;
        String stamp;
        HashMap<BigInteger,CSAcc> opList = new HashMap<>();
        if ( ! activateConnection ( )  )  {
            return opList;
        }
        
        CSAcc chanOp;
        String acc = "";

        if ( access.is(AKICK) ) {
            acc = "akick";
        } else if ( access.is(SOP) ) {
            acc = "sop";
        } else if ( access.is(AOP) ) {
            acc = "aop";
        }

        String query = "select nick,lastoped from chanaccess where name = ? and access = ? union all select mask,lastoped from chanaccess_mask where name = ? and access = ?";
        try {
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, ci.getName().getString() );
            ps.setString  ( 2, acc );
            ps.setString  ( 3, ci.getName().getString() );
            ps.setString  ( 4, acc );
            res3 = ps.executeQuery ( );

            while ( res3.next ( ) ) {
                ni = NickServ.findNick( res3.getString ( 1 ) );
                stamp = res3.getString ( 2 );
                if ( ni != null ) {
                    chanOp = new CSAcc ( ni, access, stamp );
                    opList.put ( ni.getName().getCode(), chanOp );

                } else {
                    chanOp = new CSAcc ( res3.getString(1), access, stamp );
                    opList.put ( chanOp.getMask().getCode(), chanOp );

                }
            }
            res3.close ( );
            ps.close ( );
            idleUpdate ( "getChanAccess ( ) " );
        } catch ( SQLException | NumberFormatException e )  {
                Proc.log ( CSDatabase.class.getName ( ), e );
        }
        return opList;
    }
    
   
    /* Delete a channel */

    /**
     *
     * @param ci
     * @return
     */

    public static boolean deleteChan ( ChanInfo ci )  {
        if ( ! activateConnection ( )  )  {
            return false;
        }
        
        try {
            String query = "delete from chan where name = ?";
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, ci.getName().getString()  );
            ps.execute ( );
            ps.close ( );

            query = "delete from chanaccess where name = ?";
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, ci.getName().getString() );
            ps.execute ( );
            ps.close ( );

            query = "delete from chansetting where name = ?";
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, ci.getName().getString() );
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
            String query = "select keeptopic,topiclock,ident,opguard,"+
                           "restricted,verbose,mailblock,leaveops,autoakick,"+
                           "modelock,mark,freeze,close,hold,auditorium "+
                           "from chansetting "+
                           "where name = ?;";
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, name );
            res2 = ps.executeQuery ( );

            if ( res2.next ( )  )  {
                if ( res2.getBoolean ( "keeptopic" )  == true )     { settings.set ( KEEPTOPIC, true ); }
                
                HashString topiclock = new HashString ( res2.getString ( "topiclock" ) );
                if ( topiclock.is(FOUNDER) ||
                     topiclock.is(SOP) ||
                     topiclock.is(AOP) ) {
                    settings.set ( TOPICLOCK, topiclock );
                } else {
                    settings.set ( TOPICLOCK, OFF );
                }
                  
                settings.set ( IDENT,       res2.getBoolean ( "ident" )         );
                settings.set ( OPGUARD,     res2.getBoolean ( "opguard" )       );
                settings.set ( RESTRICT,    res2.getBoolean ( "restricted" )    );
                settings.set ( VERBOSE,     res2.getBoolean ( "verbose" )       );
                settings.set ( MAILBLOCK,   res2.getBoolean ( "mailblock" )     );
                settings.set ( LEAVEOPS,    res2.getBoolean ( "leaveops" )      );
                settings.set ( AUTOAKICK,   res2.getBoolean ( "autoakick" )     );
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
  
    static boolean wipeAccessList ( ChanInfo ci, HashString access )  {
        if ( ! activateConnection ( )  )  {
            return false;
        }
        try {
            String acc = "";
            if ( access.is(SOP) ) { 
                acc = "sop";
            } else if ( access.is(AOP) ) {
                acc = "aop";
            }
 
            String query = "delete from chanaccess "
                         + "where name = ? "
                         + "and access = ?";
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, ci.getName().getString() );
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
 
    /**
     *
     * @return
     */
    public static HashMap<BigInteger,ChanInfo> getAllChans ( )  {
        ChanInfo ci;
        HashMap<BigInteger,ChanInfo> cList = new HashMap<>();
        ChanSetting settings;
        CSFlag chanFlag = null;
        Topic topic;
        String[] buf;
        long now;
        long now2;
        int index = 1;
        if ( ! activateConnection ( )  )  {
            return cList;
        }
        try { 
            now = System.nanoTime();
            HashString salt = Proc.getConf().get ( SECRETSALT );
            String query = "select c.name,c.founder,AES_DECRYPT(c.pass,?) as pass,c.description,c.regstamp,c.stamp,"
                         + "cs.keeptopic,cs.topiclock,cs.ident,cs.opguard,cs.restricted,cs.verbose,cs.mailblock,cs.leaveops,cs.autoakick,"
                         + "cs.modelock,cs.mark,cs.freeze,cs.close,cs.hold,cs.auditorium,"
                         + "tl.topic,tl.setter,unix_timestamp(tl.stamp) as tlunixstamp,tl.stamp as tlstamp,"
                         + "cf.join_connect_time,cf.talk_connect_time,cf.talk_join_time,cf.max_bans,cf.no_notice,cf.no_ctcp,cf.no_part_msg,cf.no_quit_msg,"
                         + "cf.exempt_opped,cf.exempt_voiced,cf.exempt_identd,cf.exempt_registered,cf.exempt_invites,cf.greetmsg "
                         + "from chan as c "
                         + "left join (select name,setter,stamp,topic from topiclog order by stamp desc limit 1) as tl on tl.name=c.name "
                         + "left join chansetting as cs on cs.name=c.name "
                         + "left join chanflag as cf on cf.name=c.name ";
                        
            System.out.println(query);
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, salt.getString() ); 
            res = ps.executeQuery ( );

            System.out.print("Loading Chans: ");
            int $count = 0;
            
            while ( res.next ( ) )  {
                if ( index % 100000 == 0 ) {
                    System.out.println(index);
                } else if ( index % 1000 == 0 ) {
                    System.out.print(".");
                }
                index++;
                //settings = getSettings ( res.getString ( 1 ) ); 
                //chanFlag = getChanFlag ( new HashString ( res.getString ( 1 ) ) );
                CSFlag flags = new CSFlag ( 
                                        new HashString ( res.getString ( "name" ) ), 
                                        res.getShort("join_connect_time"),
                                        res.getShort("talk_connect_time"),
                                        res.getShort("talk_join_time"),
                                        res.getShort("max_bans"),
                                        res.getBoolean("no_notice"),
                                        res.getBoolean("no_ctcp"),
                                        res.getBoolean("no_part_msg"),
                                        res.getBoolean("no_quit_msg"),
                                        res.getBoolean("exempt_opped"),
                                        res.getBoolean("exempt_voiced"),
                                        res.getBoolean("exempt_identd"),
                                        res.getBoolean("exempt_registered"),
                                        res.getBoolean("exempt_invites"),
                                        res.getString("greetmsg") );
                settings = new ChanSetting ( );
                if ( res.getBoolean ( "keeptopic" ) == true ) {
                    settings.set ( KEEPTOPIC, true );
                }
                HashString name = new HashString ( res.getString ( "name" ) );
                HashString topiclock = new HashString ( res.getString ( "topiclock" ) );
                if ( topiclock.is(FOUNDER) ||
                     topiclock.is(SOP) ||
                     topiclock.is(AOP) ) {
                    settings.set ( TOPICLOCK, topiclock );
                } else {
                    settings.set ( TOPICLOCK, OFF );
                }
                  
                settings.set ( IDENT,       res.getBoolean ( "ident" )         );
                settings.set ( OPGUARD,     res.getBoolean ( "opguard" )       );
                settings.set ( RESTRICT,    res.getBoolean ( "restricted" )    );
                settings.set ( VERBOSE,     res.getBoolean ( "verbose" )       );
                settings.set ( MAILBLOCK,   res.getBoolean ( "mailblock" )     );
                settings.set ( LEAVEOPS,    res.getBoolean ( "leaveops" )      );
                settings.set ( AUTOAKICK,   res.getBoolean ( "autoakick" )     );
                /* Oper only */
                settings.set ( MARK,        res.getString ( "mark" )           );
                settings.set ( FREEZE,      res.getString ( "freeze" )         );
                settings.set ( CLOSE,       res.getString ( "close" )          );
                settings.set ( HOLD,        res.getString ( "hold" )           );
                settings.set ( AUDITORIUM,  res.getString ( "auditorium" )     );
                settings.setModeLock ( res.getString ( "modelock" )            );
                ci = new ChanInfo ( 
                    res.getString ( "name" ), 
                    res.getString ( "founder" ), 
                    res.getString ( "pass" ),
                    res.getString ( "description" ),
                    new Topic ( res.getString("topic"), res.getString("setter"), res.getLong("tlunixstamp"), res.getString("tlstamp") ),
                    res.getString ( "regstamp" ), 
                    res.getString ( "stamp" ),
                    settings
                );
                if ( flags != null ) {
                    ci.setChanFlag ( flags );                
                } else {
                    ci.setChanFlag ( new CSFlag ( res.getString ( "name" ) ) );
                }
                //ci.setAccessList ( SOP, getChanAccess ( ci, SOP ) );
                //ci.setAccessList ( AOP, getChanAccess ( ci, AOP ) );
                //ci.setAccessList ( AKICK, getChanAccess ( ci, AKICK ) );
                ci.getFounder().addToAccessList ( FOUNDER, ci );
                cList.put ( ci.getName().getCode(), ci );
                $count++;
            }
            now2 = System.nanoTime();
            System.out.print(".. "+$count+" chans loaded [took "+(now2-now)+"ns]\n");
            res.close ( );
            ps.close ( );
            idleUpdate ( "getAllChans ( ) " );
            
        } catch  ( SQLException | NumberFormatException ex )  {
            Proc.log ( CSDatabase.class.getName ( ) , ex );    
        } 
        System.out.println(index);
        return cList;
    
    }
  
    /**
     *
     * @param access
     */
    public static void loadChanAccess ( HashString access )  {
        long now;
        long now2;
        NickInfo ni;
        ChanInfo ci;
        CSAcc acc;
        String mask;
        try { 
            now = System.nanoTime();
            String query = "select * from chanaccess "
                         + "where access = ?";
                        
            System.out.println(query);
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, access.getString() ); 
            res = ps.executeQuery ( );

            System.out.print("Loading "+access.getString()+"s: ");
            int $count = 0;
            
            while ( res.next ( ) )  {
                ni = NickServ.findNick ( res.getString ( "nick" ) );
                ci = ChanServ.findChan ( res.getString ( "name" ) );
                acc = new CSAcc ( ni, access, res.getString ( "lastoped" ) );
                ci.addAccess ( access, acc );
                $count++;
            }
            res.close ( );
            ps.close ( );
            query = "select * from chanaccess_mask "
                  + "where access = ?";
                        
            System.out.println(query);
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, access.getString() ); 
            res = ps.executeQuery ( );
            while ( res.next ( ) )  {
                mask = res.getString ( "mask" );
                ci = ChanServ.findChan ( res.getString ( "name" ) );
                acc = new CSAcc ( mask, access, res.getString ( "lastoped" ) );
                ci.addAccess ( access, acc );
                $count++;
            }
            now2 = System.nanoTime();
            System.out.print(".. "+$count+" "+access.getString()+"s loaded [took "+(now2-now)+"ns]\n");
            res.close ( );
            ps.close ( );

        } catch ( Exception ex ) {
            Proc.log ( CSDatabase.class.getName ( ) , ex );
        }
    }
    
    /**
     *
     */
    public static void loadAllChanAccess ( )  {
        long now;
        long now2;
        NickInfo ni;
        ChanInfo ci;
        CSAcc acc;
        String mask;
        HashString access;
        try { 
            now = System.nanoTime();
            String query = "select * from chanaccess;";
                        
            System.out.println(query);
            ps = sql.prepareStatement ( query );
            res = ps.executeQuery ( );

            int $count = 0;
            
            while ( res.next ( ) )  {
                ci = ChanServ.findChan(res.getString("name") );
                ni = NickServ.findNick( res.getString("nick") );
                access = new HashString ( res.getString("access") );
                acc = new CSAcc ( ni, access, res.getString ( "lastoped" ) );
                ci.addAccess ( access, acc );
                $count++;
            }
            res.close ( );
            ps.close ( );
            query = "select * from chanaccess_mask;";
                        
            System.out.println(query);
            ps = sql.prepareStatement ( query );
            res = ps.executeQuery ( );
            while ( res.next ( ) )  {
                mask = res.getString ( "mask" );
                ci = ChanServ.findChan ( res.getString ( "name" ));
                access = new HashString ( res.getString("access") );
                acc = new CSAcc ( mask, access, res.getString ( "lastoped" ) );
                ci.addAccess ( access, acc );
                $count++;
            }
            now2 = System.nanoTime();
            System.out.print(".. "+$count+" Channel Accesses loaded [took "+(now2-now)+"ns]\n");
            res.close ( );
            ps.close ( );

        } catch ( Exception ex ) {
            Proc.log ( CSDatabase.class.getName ( ) , ex );
        }
    }
  
    
    /**
     *
     * @param pattern
     * @return
     */
    public static ArrayList<ChanInfo> getChanList ( String pattern )  {
        ArrayList<ChanInfo> cList = new ArrayList<> ( );
        ChanInfo ci; 
        ChanSetting settings;
        CSFlag chanFlag = null;
        
        if ( pattern.isEmpty ( )  )  {
            return null;
        }
        
        pattern = pattern.replaceAll ( "\\'", "" );
        pattern = pattern.replaceAll ( "\\*", " ( .* ) " );
        pattern = pattern.replaceAll ( "\\?", " ( .? ) {0,1}" );
        
//public ChanInfo ( String name, String founder, String pass, String desc, Topic topic, long regStamp, long lastSeen, ChanSetting settings, ChanFlags flags )  {
        if ( ! activateConnection ( ) ) {
            return cList;
        }
        try { 
            HashString salt = Proc.getConf().get ( SECRETSALT );
            String query = "select c.name,c.founder,AES_DECRYPT(c.pass,?),c.description,c.regstamp,c.stamp "
                         + "from chan as c "
                         + "where c.name rlike ? "
                         + "or c.description rlike ? "
                         + "order by c.name asc";
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, salt.getString() );
            ps.setString  ( 2, "^"+pattern+"$" );
            ps.setString  ( 3, "^"+pattern+"$" );
            res = ps.executeQuery ( );
            
            while ( res.next ( )  )  { 
                settings = getSettings ( res.getString ( 1 ) );
                chanFlag = getChanFlag ( new HashString ( res.getString ( 1 ) ) );
                ci = new ChanInfo ( res.getString ( 1 ) , res.getString ( 2 ) , res.getString ( 3 ), 
                                    res.getString ( 4 ) , getChanTopic ( res.getString ( 1 ) ), res.getString ( 5 ), 
                                    res.getString ( 6 ) , settings );
                ci.setAccessList ( SOP, getChanAccess ( ci, SOP ) );
                ci.setAccessList ( AOP, getChanAccess ( ci, AOP ) );
                ci.setAccessList ( AKICK, getChanAccess ( ci, AKICK ) );
                ci.getFounder().addToAccessList ( FOUNDER, ci );
                if ( chanFlag != null ) {
                   ci.setChanFlag ( chanFlag );
                } else {
                   ci.setChanFlag ( new CSFlag ( res.getString ( 1 ) ) );
                }
                cList.add ( ci ); 
            } 
            res.close ( );
            ps.close ( );
             
        } catch  ( SQLException ex )  {
            Proc.log ( CSDatabase.class.getName ( ) , ex );
        }
        return cList;
    }
   
    private static CSFlag getChanFlag ( HashString name ) {
        CSFlag cf = null;
        String query;
        try {
            query = "select join_connect_time, talk_connect_time, talk_join_time, max_bans,"+
                           "no_notice, no_ctcp, no_part_msg, no_quit_msg, exempt_opped, "+
                           "exempt_voiced, exempt_identd, exempt_registered, exempt_invites, "+
                           "greetmsg "+
                    "from chanflag "+
                    "where name = ?";
//            System.out.println(query);
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, name.getString() );
            res2 = ps.executeQuery ( );
            if ( res2.next ( ) ) {
                cf = new CSFlag ( name, res2.getShort(1),res2.getShort(2),res2.getShort(3),res2.getShort(4),
                                  res2.getBoolean(5),res2.getBoolean(6),res2.getBoolean(7),res2.getBoolean(8),res2.getBoolean(9),
                                  res2.getBoolean(10),res2.getBoolean(11),res2.getBoolean(12),res2.getBoolean(13),
                                  res2.getString(14) );
            }
        } catch ( SQLException ex ) {
            Proc.log ( CSDatabase.class.getName ( ) , ex );
        }
        return cf;
    }
    
    /**
     *
     * @param user
     * @param ci
     * @return
     */
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
            ps.setString  ( 1, ci.getName().getString() );
            res = ps.executeQuery ( );
            
            while ( res.next ( ) ) {
                csaList.add( 
                    new CSAccessLogEvent( 
                        new HashString ( res.getString ( 1 ) ), 
                        new HashString ( res.getString ( 2 ) ), 
                        res.getString ( 3 ), 
                        res.getString ( 4 ), 
                        res.getString ( 5 ), 
                        res.getString ( 6 ) 
                    )
                );
            }
            res.close ( );
            ps.close ( );
            
        } catch ( SQLException ex ) {
            Proc.log ( CSDatabase.class.getName ( ) , ex );
        }
        
        return csaList;
    }
    
    /**
     *
     * @param log
     * @return
     */
    public static int logEvent ( CSLogEvent log ) {
        return Database.logEvent ("chanlog", log );
    }
    
    /**
     *
     * @param id
     */
    public static void delLogEvent ( int id ) {
        Database.delLogEvent ( "chanlog", id );
    }


}
