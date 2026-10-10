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
 * along with this program; if not, see <https://www.gnu.org/licenses/>.
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
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
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
| dynaop     | tinyint ( 1 )   | YES  |     | NULL    |       |
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
    private static ResultSet res;
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
                begin ( );
                /* the pass is a hash */
                String query = "insert into chan ( name, founder, pass, description, regstamp, stamp )  "
                             + "values ( ?, ?, ?, ?, ?, ? )";
                ps = sql.prepareStatement ( query );
                ps.setString  ( 1, ci.getString ( NAME ) );
                ps.setString  ( 2, ci.getFounder().getName().getString()  );
                ps.setString  ( 3, ci.getPass ( ) );
                ps.setString  ( 4, ci.getString ( DESCRIPTION )  );
                ps.setString  ( 5, ci.getString ( REGTIME ) );
                ps.setString  ( 6, ci.getString ( LASTUSED ) );
                ps.execute ( );
                ps.close ( );
                 
                query = "insert into chanflag "+
                        "(name,join_connect_time,talk_connect_time,talk_join_time,"+
                        "max_bans,max_invites,max_msg_time,no_notice,no_ctcp,no_part_msg,no_quit_msg,"+
                        "exempt_opped,exempt_voiced,exempt_identd,exempt_registered,"+
                        "exempt_invites,exempt_webirc,hide_mode_lists,no_nick_change,no_utf8,greetmsg) "+
                        "values (?,0,0,0,200,100,'0:0',0,0,0,0,0,0,0,0,0,0,0,0,0,null)";
                ps = sql.prepareStatement ( query );
                ps.setString  ( 1, ci.getString ( NAME ) );
                ps.execute ( );
                ps.close ( );
                
                
                
                query = "insert into chansetting "+
                        "values (?,1,'OFF',1,1,0,0,0,0,0,0,'+nt',null,null,null,null,null)";
                ps = sql.prepareStatement ( query );
                ps.setString  ( 1, ci.getString ( NAME ) );
                ps.execute ( );
                ps.close ( );
                commit ( );
                
                idleUpdate ( "createChan ( ) " );
            } catch  ( SQLException ex )  {
                rollback ( );
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
        if ( ci.getChanges().hasChanged ( DYNAOP ) ) {
            changes = addToQuery ( changes, "dynaop" );
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
        if ( ci.getChanges().hasChanged ( MAX_INVITES ) ) {
            changes = addToQuery ( changes, "max_invites" );
        }
        if ( ci.getChanges().hasChanged ( MAX_MSG_TIME ) ) {
            changes = addToQuery ( changes, "max_msg_time" );
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
        if ( ci.getChanges().hasChanged ( NO_QUIT_MSG ) ) {
            changes = addToQuery ( changes, "no_quit_msg" );
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
        if ( ci.getChanges().hasChanged ( EXEMPT_WEBIRC ) ) {
            changes = addToQuery ( changes, "exempt_webirc" );
        }
        if ( ci.getChanges().hasChanged ( HIDE_MODE_LISTS ) ) {
            changes = addToQuery ( changes, "hide_mode_lists" );
        }
        if ( ci.getChanges().hasChanged ( NO_NICK_CHANGE ) ) {
            changes = addToQuery ( changes, "no_nick_change" );
        }
        if ( ci.getChanges().hasChanged ( NO_UTF8 ) ) {
            changes = addToQuery ( changes, "no_utf8" );
        }
        if ( ci.getChanges().hasChanged ( USER_VERBOSE ) ) {
            changes = addToQuery ( changes, "user_verbose" );
        }
        if ( ci.getChanges().hasChanged ( OPER_VERBOSE ) ) {
            changes = addToQuery ( changes, "oper_verbose" );
        }
        if ( ci.getChanges().hasChanged ( SJR ) ) {
            changes = addToQuery ( changes, "sjr" );
        }
        if ( ci.getChanges().hasChanged ( GREETMSG ) ) {
            changes = addToQuery ( changes, "greetmsg" );
        }
        return changes;
    }
    
    private static int updateChanInfo ( ChanInfo ci ) {
        String query = "update chan set founder = ?, pass = ?, description = ?, stamp = ? where name = ?";
        try {
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, ci.getFounder().getNameStr() );
            ps.setString  ( 2, ci.getPass ( ) );
            ps.setString  ( 3, ci.getString ( DESCRIPTION ) );
            ps.setString  ( 4, ci.getString ( LASTUSED ) );
            ps.setString  ( 5, ci.getNameStr() ); 
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
            if ( ci.getChanges().hasChanged ( DYNAOP ) ) {
                ps.setBoolean ( index++, ci.getSettings().is ( DYNAOP ) );
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
            if ( ci.getChanges().hasChanged ( HOLD ) ) {
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
        String query = "insert into topiclog ( name,setter,stamp,topic ) values ( ?, ?, from_unixtime(?), ? )";
        try {
            ps = sql.prepareStatement ( query );
            ps.setString ( 1, ci.getName().getString() );
            ps.setString ( 2, ci.getTopic().getSetter ( ) );
            ps.setLong ( 3, ci.getTopic().getStamp ( ) );
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
            if ( ci.getChanges().hasChanged ( MAX_INVITES ) ) {
                ps.setShort ( index++, cf.getMaxinvites ( ) );
            }
            if ( ci.getChanges().hasChanged ( MAX_MSG_TIME ) ) {
                ps.setString ( index++, cf.getMaxmsgtime());
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
            if ( ci.getChanges().hasChanged ( NO_QUIT_MSG ) ) {
                ps.setBoolean ( index++, cf.isNoquitmsg ( ) );
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
            if ( ci.getChanges().hasChanged ( EXEMPT_WEBIRC ) ) {
                ps.setBoolean ( index++, cf.isExemptwebirc ( ) );
            }
            if ( ci.getChanges().hasChanged ( HIDE_MODE_LISTS ) ) {
                ps.setBoolean ( index++, cf.isHidemodelists ( ) );
            }
            if ( ci.getChanges().hasChanged ( NO_NICK_CHANGE ) ) {
                ps.setBoolean ( index++, cf.isNonickchange());
            }
            if ( ci.getChanges().hasChanged ( NO_UTF8 ) ) {
                ps.setBoolean ( index++, cf.isNoutf8());
            }
            if ( ci.getChanges().hasChanged ( USER_VERBOSE ) ) {
                ps.setBoolean ( index++, cf.isUserverbose());
            }
            if ( ci.getChanges().hasChanged ( OPER_VERBOSE ) ) {
                ps.setBoolean ( index++, cf.isOperverbose());
            }
            if ( ci.getChanges().hasChanged ( SJR ) ) {
                ps.setBoolean ( index++, cf.isSjr());
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
        }
        /* All parts or none: a part that fails must not be forgotten, and
           the parts that worked must not be written twice at the retry */
        boolean ok = true;
        try {
            begin ( );
            if ( ci.getChanges().hasChanged ( FOUNDER ) ||
                 ci.getChanges().hasChanged ( DESCRIPTION ) ||
                 ci.getChanges().hasChanged ( LASTUSED ) ) {
                ok = ( updateChanInfo ( ci ) != -1 );
            }
            
            String changes = compileSettingChanges ( ci );
            if ( ok && changes.length() > 0 ) {
                ok = ( updateChanSettings ( ci, changes ) != -1 );
            } 
                
            /* An empty topic is logged too, or the old one comes back
               after a restart */
            if ( ok && 
                 ci.getChanges().hasChanged ( TOPIC ) && 
                 ci.getTopic() != null && 
                 ci.getTopic().getText() != null ) {
                ok = ( addTopicLog ( ci ) != -1 );
            }
                
            changes = compileFlagChanges ( ci );
            if ( ok && changes.length() > 0 ) {
                ok = ( updateFlagChanges ( ci, changes ) != -1 );
            }
            
            if ( ok ) {
                commit ( );
            }
        } catch ( SQLException ex ) {
            Proc.log ( CSDatabase.class.getName ( ) , ex );
            ok = false;
        }
        if ( ! ok ) {
            rollback ( );
            return -1;
        }
        ci.getChanges().cleanUp ( );
        idleUpdate ( "updateChan ( ) " );
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
    /* The value stored in chanaccess.access / chanaccess_mask.access */
    private static String accessToDbString ( HashString access ) {
        if      ( access.is(AKICK) )        { return "akick";                   }
        else if ( access.is(SOP) )          { return "sop";                     }
        else if ( access.is(AOP) )          { return "aop";                     }
        else if ( access.is(HOP) )          { return "hop";                     }
        else if ( access.is(VOP) )          { return "vop";                     }
        return "";
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
          
                String acc = accessToDbString ( access );
                 
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

     
    static ArrayList<Topic> getTopicList(ChanInfo ci) {
        ArrayList<Topic> tList = new ArrayList<>();
        if ( ! activateConnection ( )  )  {
            return tList;
        }
        
        String query;
        try {
            query = "select * from (select topic,setter,unix_timestamp(stamp) as ustamp,stamp from topiclog where name = ? order by stamp desc limit 100) as tl order by stamp asc";
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
            
            /* Not tied to chan in the database: without this a channel that
               is registered again gets the topic of the old one */
            query = "delete from topiclog where name = ?";
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

  
    static boolean wipeAccessList ( ChanInfo ci, HashString access )  {
        if ( ! activateConnection ( )  )  {
            return false;
        }
        try {
            String acc = accessToDbString ( access );
 
            String query = "delete from chanaccess "
                         + "where name = ? "
                         + "and access = ?";
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, ci.getName().getString() );
            ps.setString  ( 2, acc );
            ps.execute ( );
            ps.close ( );

            query = "delete from chanaccess_mask "
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
            return null;
        }
        try { 
            now = System.nanoTime();
            String query = "select c.name,c.founder,c.pass,c.description,c.regstamp,c.stamp,"
                         + "cs.keeptopic,cs.topiclock,cs.ident,cs.opguard,cs.restricted,cs.verbose,cs.mailblock,cs.leaveops,cs.autoakick,cs.dynaop,"
                         + "cs.modelock,cs.mark,cs.freeze,cs.close,cs.hold,cs.auditorium,"
                         + "tl.topic,tl.setter,unix_timestamp(tl.stamp) as tlunixstamp,tl.stamp as tlstamp,"
                         + "cf.join_connect_time,cf.talk_connect_time,cf.talk_join_time,cf.max_bans,cf.max_invites,cf.max_msg_time,cf.no_notice,cf.no_ctcp,cf.no_part_msg,cf.no_quit_msg,"
                         + "cf.exempt_opped,cf.exempt_voiced,cf.exempt_identd,cf.exempt_registered,cf.exempt_invites,cf.exempt_webirc,cf.hide_mode_lists,no_nick_change,cf.no_utf8,cf.user_verbose,cf.oper_verbose,cf.sjr,cf.greetmsg "
                         + "from chan as c "
                         + "left join (select t.name,t.setter,t.stamp,t.topic from topiclog as t "
                         + "join (select name,max(stamp) as mstamp from topiclog group by name) as m on m.name=t.name and m.mstamp=t.stamp) as tl on tl.name=c.name "
                         + "left join chansetting as cs on cs.name=c.name "
                         + "left join chanflag as cf on cf.name=c.name ";
                        
            ps = sql.prepareStatement ( query );
            res = ps.executeQuery ( );

            System.out.print("Loading Chans: ");
            int $count = 0;
            
            while ( res.next ( ) )  {
                if ( index % 100000 == 0 ) {
                } else if ( index % 1000 == 0 ) {
                    System.out.print(".");
                }
                index++;
                CSFlag flags = new CSFlag ( 
                        new HashString ( res.getString ( "name" ) ), 
                        res.getShort("join_connect_time"),
                        res.getShort("talk_connect_time"),
                        res.getShort("talk_join_time"),
                        res.getShort("max_bans"),
                        res.getShort("max_invites"),
                        res.getString("max_msg_time"),
                        res.getBoolean("no_notice"),
                        res.getBoolean("no_ctcp"),
                        res.getBoolean("no_part_msg"),
                        res.getBoolean("no_quit_msg"),
                        res.getBoolean("exempt_opped"),
                        res.getBoolean("exempt_voiced"),
                        res.getBoolean("exempt_identd"),
                        res.getBoolean("exempt_registered"),
                        res.getBoolean("exempt_invites"),
                        res.getBoolean("exempt_webirc"),
                        res.getBoolean("hide_mode_lists"),
                        res.getBoolean("no_nick_change"),
                        res.getBoolean("no_utf8"),
                        res.getString("greetmsg") );
                flags.setVerbose ( res.getBoolean ( "user_verbose" ), res.getBoolean ( "oper_verbose" ) );
                flags.setSjr ( res.getBoolean ( "sjr" ) );
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
                settings.set ( DYNAOP,      res.getBoolean ( "dynaop" )        );
                /* Oper only */
                settings.set ( MARK,        res.getString ( "mark" )           );
                settings.set ( FREEZE,      res.getString ( "freeze" )         );
                settings.set ( CLOSE,       res.getString ( "close" )          );
                settings.set ( HOLD,        res.getString ( "hold" )           );
                settings.set ( AUDITORIUM,  res.getString ( "auditorium" )     );
                settings.setModeLock ( res.getString ( "modelock" )            );
                topic = new Topic ( res.getString("topic"), res.getString("setter"), res.getLong("tlunixstamp"), res.getString("tlstamp") );
                if ( topic.isJunk ( ) ) {
                    topic = new Topic ( "", "", 0 );
                }
                ci = new ChanInfo ( 
                    res.getString ( "name" ), 
                    res.getString ( "founder" ), 
                    res.getString ( "pass" ),
                    res.getString ( "description" ),
                    topic,
                    res.getString ( "regstamp" ), 
                    res.getString ( "stamp" ),
                    settings
                );
                if ( flags != null ) {
                    ci.setChanFlag ( flags );                
                } else {
                    ci.setChanFlag ( new CSFlag ( res.getString ( "name" ) ) );
                }
                if ( cList.containsKey ( ci.getName().getCode() ) ) {
                    continue;
                }
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
            return null;    
        } 
        return cList;
    
    }

    
    /* The access column as the list constant, one shared object for all rows */
    private static HashString listOf ( String access ) {
        if ( access == null )                           { return null;      }
        HashString hash = new HashString ( access );
        if      ( hash.is(SOP) )                        { return SOP;       }
        else if ( hash.is(AOP) )                        { return AOP;       }
        else if ( hash.is(HOP) )                        { return HOP;       }
        else if ( hash.is(VOP) )                        { return VOP;       }
        else if ( hash.is(AKICK) )                      { return AKICK;     }
        return null;
    }

    /**
     *
     */
    public static boolean loadAllChanAccess ( )  {
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
                        
            ps = sql.prepareStatement ( query );
            res = ps.executeQuery ( );

            int $count = 0;
            
            while ( res.next ( ) )  {
                ci = ChanServ.findChan(res.getString("name") );
                ni = NickServ.findNick( res.getString("nick") );
                access = listOf ( res.getString("access") );
                if ( ci == null || ni == null || access == null ) {
                    continue; /* a row for a channel or nick that is gone */
                }
                acc = new CSAcc ( ni, access, res.getString ( "lastoped" ) );
                ci.loadAccess ( access, acc );
                $count++;
            }
            res.close ( );
            ps.close ( );
            query = "select * from chanaccess_mask;";
                        
            ps = sql.prepareStatement ( query );
            res = ps.executeQuery ( );
            while ( res.next ( ) )  {
                mask = res.getString ( "mask" );
                ci = ChanServ.findChan ( res.getString ( "name" ));
                access = listOf ( res.getString("access") );
                if ( ci == null || mask == null || access == null ) {
                    continue;
                }
                acc = new CSAcc ( mask, access, res.getString ( "lastoped" ) );
                ci.loadAccess ( access, acc );
                $count++;
            }
            now2 = System.nanoTime();
            System.out.print(".. "+$count+" Channel Accesses loaded [took "+(now2-now)+"ns]\n");
            res.close ( );
            ps.close ( );

        } catch ( Exception ex ) {
            Proc.log ( CSDatabase.class.getName ( ) , ex );
            return false;
        }
        return true;
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
