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
 * You should have received b copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package operserv;

import channel.Chan;
import chanserv.ChanInfo;
import core.Executor;
import core.Handler;
import core.StringMatch;
import core.Proc;
import static core.HashNumeric.ADD;
import static core.HashNumeric.DEL;
import static core.HashNumeric.LIST;
import static core.HashNumeric.NAME;
import core.Service;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import nickserv.NickInfo;
import nickserv.NickServ;
import server.Server;
import user.User;

/**
 *
 * @author DreamHealer
 */
public class OSExecutor extends Executor {
    private OSSnoop snoop;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public OSExecutor ( OperServ service, OSSnoop snoop )  {
        super ( );
        this.service        = service;
        this.snoop          = snoop; 
    }

    public void parse ( User user, String[] cmd )  {
        this.found = true; /* Assume that everything will go correctly */
        int command = cmd[3].toUpperCase().hashCode ( );
        switch ( command ) {
            case UINFO :
                this.doUInfo ( user, cmd );
                break;
                
            case CINFO :
                this.doCInfo ( user, cmd );
                break;
                
            case SINFO :
                this.doSInfo ( user, cmd );
                break;
                
            case ULIST :
                this.doUList ( user ); 
                break;
            
            case SLIST :
                this.doSList ( user ); 
                break;
                
            case UPTIME :
                this.doUpTime ( user );
                break;
             
            case SQLINE :
            case SGLINE :
            case IGNORE :
            case AKILL :
                this.doServicesBan ( command, user, cmd );
                break;
                
            case STAFF :
                this.doStaff ( user, cmd );
                break;
                           
            case SEARCHLOG :
                this.doSearchLog ( user, cmd );
                break;
                    
            case COMMENT :
                this.comment ( user, cmd );
                break;
                            
            case GLOBAL :
                this.global ( user, cmd );
                break;
                
            case AUDIT :
                this.doAudit ( user, cmd );
                break;
                     
            case BANLOG :
                this.doBanlog ( user, cmd );
                break;
                       
            case JUPE :
                this.doJupe ( user, cmd );
                break;
                       
            case SERVER :
                this.doServer ( user, cmd );
                break;
                      
            case SPAMFILTER :
                this.doSpamFilter ( user, cmd );
                break;
                      
            default:
                this.found = false; 
                this.noMatch ( user, cmd[3] );
            
        }
        this.snoop.msg ( this.found, user, cmd ); 
    }


    private void doUInfo ( User user, String[] cmd )  {
        /* UINFO requested so lets send all we know about that user */
        User u;
 
        if ( cmd.length < 5 )  { /* size is 5  ( 0-4 )  */
            this.service.sendMsg ( user, "Syntax: /OperServ UINFO Nickname" );
            return;
        }
 
        if (  ( u = Handler.findUser ( cmd[4] )  )  != null )  {
            this.service.sendMsg ( user, "*** User INFO ***"                                                        );
            this.service.sendMsg ( user, "      User: "+u.getString ( NAME )                                        );
            this.service.sendMsg ( user, "  Hostmask: "+u.getString ( USER ) +"@"+u.getString ( HOST )              );
            this.service.sendMsg ( user, "  ID Nicks: "+u.getIDNicks ( )                                            );
            this.service.sendMsg ( user, "  ID Chans: "+u.getIDChans ( )                                            );
            if ( u.getSID ( )  != null )  {
                this.service.sendMsg ( user, "ServicesID: "+u.getSID ( ) .getID ( )                                 );
                if ( u.getSID ( ) .getOper ( )  != null )  {
                    this.service.sendMsg ( user, "   ID Oper: "+u.getSID ( ) .getOper ( ) .getString ( NAME )       );
                }
            }
            this.service.sendMsg ( user, "        IP: " + u.getString ( IP )                                        );
            this.service.sendMsg ( user, "     Modes: ident ( "+u.getModes ( ) .is ( IDENT ) +" ) , oper ( "+u.getModes ( ) .is ( OPER ) +" ) , admin ( "+u.getModes ( ) .is ( ADMIN ) +" ) , sadmin ( "+u.getModes ( ) .is ( SADMIN ) +" ) " );
            this.service.sendMsg ( user, "    Server: "+u.getServ ( ) .getName ( )                                  );
            this.service.sendMsg ( user, "*** End ***"                                                              );
        } else {
            this.service.sendMsg ( user, "Error: nick is offline"                                                   );
        }

    }

    private void doSInfo ( User user, String[] cmd )  {
        /* UINFO requested so lets send all we know about that user */
        Server s;
        
        if ( cmd.length < 5 )  { /* size is 5  ( 0-4 )  */
            this.service.sendMsg ( user, "Syntax: /OperServ UINFO server" );
            return;
        }
        
        if (  ( s = Handler.findServer ( cmd[4] )  )  != null )  {
            this.service.sendMsg ( user, "*** Server INFO ***"              );
            this.service.sendMsg ( user, "     Name: "+s.getName ( )        );
            this.service.sendMsg ( user, "    Users: "+s.size ( )           );
            this.service.sendMsg ( user, "*** End ***"                      );
        } else {
            this.service.sendMsg ( user, "Error: server is offline"         );
        }

    }
 
    private void doCInfo ( User user, String[] cmd )  {
        /* UINFO requested so lets send all we know about that user */
        Chan c;
        String users    = new String ( );
        String voice    = new String ( );
        String oped     = new String ( );
        String modes    = new String ( );
        
        if ( cmd.length < 5 )  {/* size is 5  ( 0-4 )  */
            this.service.sendMsg ( user, "Syntax: /OperServ CINFO #Chan" );
            return;
        }
        
        if (  ( c = Handler.findChan ( cmd[4] )  )  != null )  {

            /* GET OP LIST */ 
            for ( User cu : c.getList ( OP )  )  {
                if ( oped.isEmpty ( )  )  {
                    oped = cu.getString ( NAME );
                } else {
                    oped += " "+cu.getString ( NAME );
                }
                System.out.println ( "debug ( oped ) : "+oped );
            }

            /* GET VOICE LIST */
            for ( User cu : c.getList ( VOICE )  )  {
                if ( voice.isEmpty ( )  )  {
                    voice = cu.getString ( NAME );
                } else {
                    voice += " "+cu.getString ( NAME );
                }
                System.out.println ( "debug ( voice ) : "+voice );
            }

            /* GET USER LIST */
            for ( User cu : c.getList ( USER )  )  {
                if ( users.isEmpty ( )  )  {
                    users = cu.getString ( NAME );
                } else {
                    users += " "+cu.getString ( NAME );
                }
                System.out.println ( "debug ( users ) : "+users );
            }
            modes = c.getModes ( ) .getModes ( );
            this.service.sendMsg ( user, "*** Chan INFO ***"                    );
            this.service.sendMsg ( user, "    Name: "+c.getString ( NAME )      );
            this.service.sendMsg ( user, "   Modes: "+modes                     );
            this.service.sendMsg ( user, "      Op: "+oped                      );
            this.service.sendMsg ( user, "   Voice: "+voice                     );
            this.service.sendMsg ( user, "    User: "+users                     );
            this.service.sendMsg ( user, "*** End ***"                          );

        } else {
            this.service.sendMsg ( user, "Error: chan is offline"               );
        } 
    }

    private void doUList ( User user )  {
        Server s = Handler.findServer ( Proc.getConf().get ( CONNNAME ) );
        if ( ! OperServ.enoughAccess ( user, UINFO ) ) {
            return;
        }
        s.recursiveUserList ( user, " " );
    }
    private void doSList ( User user )  {
        if ( ! OperServ.enoughAccess ( user, UINFO ) ) {
            return;
        }
        this.service.sendMsg ( user, "*** Server List:" ); 
        for ( Server server : Handler.getServerList() ) {
            this.service.sendMsg ( user, "  "+server.getName() );
        }
        this.service.sendMsg ( user, "*** End of List ***" );
    }
    private void doUpTime ( User user )  { 
        this.service.sendMsg ( user, "Uptime: "+Proc.getUptime ( ) ); 
    }

    private void doServicesBan ( int command, User user, String[] cmd )  {
        // :DreamHealer PRIVMSG OperServ@stats.avade.net   :ban add fredde@172.* testing
        // 0            1       2                          3      4   5            6
        
        System.out.println("Bans: "+Handler.getOperServ().getAkillCount()+":"+Handler.getOperServ().getIgnoreCount());
        
        
        String cmdName = ServicesBan.getCommandByHash ( command );
        
        if ( cmd.length < 5 )  {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, cmdName+" <ADD|TIME|DEL|LIST|INFO> [<20m|12h|7d>] <user@mask> <REASON>" ) );
            return;
        } 
         
        switch ( cmd[4].toUpperCase().hashCode ( ) ) {
            case ADD:
                this.doAddBan ( command, user, cmd );
                break;
                
            case DEL:
                this.doDelBan ( command, user, cmd );
                break;
                
            case TIME:
                this.doTimeBan ( command, user, cmd ); 
                break;
                
            case LIST :
                this.doListBan ( command, user, cmd );
                break;
            
            case INFO :
                this.doInfoBan ( command, user, cmd );
                break;
            
        }
    }
    private void doListBan ( int command, User user, String[] cmd )  {
        // :DreamHealer PRIVMSG OperServ@stats.avade.net   :ban list fredde@172.*
        // 0            1       2                          3      4    5    
        
        String cmdName = ServicesBan.getCommandByHash ( command );
        
        if ( ! OSDatabase.checkConn ( ) ) {
            Handler.getOperServ().sendMsg ( user, "Database error. Please try again in a little while." );
            return;
        }
        
        if ( cmd.length < 6 )  {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, cmdName+" <LIST> <user@mask>" )  );
            return;
        } 
            
        String usermask = cmd[5];
        this.service.sendMsg ( user, output ( BAN_LIST_START, cmdName, usermask ) );
        for ( ServicesBan a : OSDatabase.getServicesBans ( command ) ) {
            if ( StringMatch.maskWild ( usermask, a.getMask() ) ) {
                this.service.sendMsg ( user, output ( BAN_LIST, ""+a.getID ( ), a.getMask ( ), ""+a.getExpire ( ), a.getInstater ( ), a.getReason ( ) ) );
            }
        } 
        this.service.sendMsg ( user, output ( BAN_LIST_STOP, "" ) );
    }

    private void doInfoBan ( int command, User user, String[] cmd ) {
        String cmdName = ServicesBan.getCommandByHash ( command );

        if ( ! OSDatabase.checkConn ( ) ) {
            Handler.getOperServ().sendMsg ( user, "Database error. Please try again in a little while." );
            return;
        }
        
        if ( cmd.length < 6 )  {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, cmdName+" <INFO> <ticket>" )  );
            return;
        } 
        
        switch (cmd[5].toUpperCase().substring(0,2).hashCode() ) {
            case AK :
                cmdName = "AKILL";
                break;
                
            case IG :
                cmdName = "IGNORE";
                break;
                
            case SQ :
                cmdName = "SQLINE";
                break;
            
            case SG :
                cmdName = "SGLINE";
                break;
        }
        
        String ticket = cmd[5].toUpperCase();
        ServicesBan ban;
        
        if ( ( ban = OSDatabase.getServicesBanByTicket ( ticket ) ) != null ) {
            this.service.sendMsg ( user, "*** "+cmdName+" INFO:" );
            this.service.sendMsg ( user, "  Ticket: "+ticket );
            this.service.sendMsg ( user, "    Mask: "+ban.getMask() );
            this.service.sendMsg ( user, "Instater: "+ban.getInstater() );
            this.service.sendMsg ( user, "    Time: "+ban.getTime() );
            this.service.sendMsg ( user, "  Expire: "+ban.getExpire() );
            this.service.sendMsg ( user, "  Reason: "+ban.getReason() );        
            this.service.sendMsg ( user, "*** End of Info ***" );
        } else {
            this.service.sendMsg ( user, "Error: No "+cmdName+" found with ticket: "+ticket );
        }
        
    }

    private void doAddBan ( int command, User user, String[] cmd )  {
        // :DreamHealer PRIVMSG OperServ@stats.avade.net   :ban add fredde@172.* testing
        // :DreamHea1er PRIVMSG OperServ@services.sshd.biz :AKILL DEL   user@mask reason
        // :DreamHea1er PRIVMSG OperServ@services.sshd.biz :AKILL ADD   user@mask reason
        // :DreamHea1er PRIVMSG OperServ@services.sshd.biz :AKILL TIME  2d        user@mask reason
        //      0          1            2                     3    4     5            6       7
        Oper oper;
        
        String cmdName = ServicesBan.getCommandByHash ( command );
        
        if ( ! OSDatabase.checkConn ( ) ) {
            Handler.getOperServ().sendMsg ( user, "Database error. Please try again in a little while." );
            return;
        }
        
        if ( cmd.length < 5 ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, cmdName+" <ADD|DEL|TIME|LIST|INFO> <nick|#chan|user@mask> <REASON>" ) );
            return;
        }
        
        int sub = cmd[5].toUpperCase().hashCode();
        
        if ( cmd.length < 7 || ( cmd.length < 8 && sub == TIME ) ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, cmdName+" <ADD> <user@mask> <REASON>" )  );
             
        } else if ( command == SQLINE && cmd[5].contains ( "@" ) ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, cmdName+" <ADD> <nick|#chan> <REASON>" )  );
 
        } else if ( command == SGLINE && ( cmd.length < 7 || ( cmd.length < 8 && sub == TIME ) ) ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, cmdName+" <ADD> <word> <REASON>" )  );
        
        } else if ( ( oper = user.getSID().getOper ( ) ) == null ) {
            this.service.sendMsg ( user, output ( ACCESS_DENIED, "" ) );

        } else {
            String      usermask;
            String      reason;
            String      expire;
            int         res;
            String      percent;
            
            usermask    = cmd[5];
            expire      = Handler.expireToTime ( "30d" );
            reason      = Handler.cutArrayIntoString ( cmd, 6 );
            res         = this.addBan ( command, user, oper, usermask, expire, reason );
            if ( res >= 0 )  {
                percent = String.format("%.02f", (float) res / Handler.getUserList().size() * 100 ); 
                this.service.sendMsg ( user, output ( BAN_ADD, cmdName, usermask, ""+res, percent ) );
                this.service.sendGlobOp ( output ( BAN_ADD_GLOB, cmdName, usermask, user.getOper().getName(), ""+res, percent ) );
            } else if ( res == -1 )  {
                this.service.sendMsg ( user, output ( BAN_EXIST, cmdName, usermask )  );                        
            }
        }
    }

    private void doTimeBan ( int command, User user, String[] cmd )  {
        //  :DreamHealer PRIVMSG OperServ@stats.avade.net   :ban add fredde@172.* testing
        // :DreamHea1er PRIVMSG OperServ@services.sshd.biz :AKILL DEL   user@mask reason
        // :DreamHea1er PRIVMSG OperServ@services.sshd.biz :AKILL ADD   user@mask reason
        // :DreamHea1er PRIVMSG OperServ@services.sshd.biz :AKILL TIME  2d        user@mask reason
        //      0          1            2                     3    4     5            6       7
        Oper oper;
        
        String cmdName = ServicesBan.getCommandByHash ( command );
        
        if ( ! OSDatabase.checkConn ( )  )  {
            Handler.getOperServ().sendMsg ( user, "Database error. Please try again in a little while." );
            return;
        }
        
        if ( cmd.length < 8 && ( command == AKILL || command == IGNORE ) )  { 
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, cmdName+" <TIME> <20m|4h|3d> <user@mask> <REASON>" )  );
              
        } else if ( cmd.length < 8 && ( command == SQLINE ) )  { 
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, cmdName+" <TIME> <20m|4h|3d> <nick|#chan> <REASON>" )  );
         
        } else if ( ( oper = user.getSID().getOper ( ) ) == null ) {
            this.service.sendMsg ( user, output ( ACCESS_DENIED, "" ) );

        } else {
            
            String      usermask;
            String      reason;
            String      expire;
            int         res = 0;
            String      percent;
            
            usermask    = cmd[6];
            if ( ( expire = Handler.expireToTime ( cmd[5] ) ) == null ) {
                /* time element is wrong / typoed */
                this.service.sendMsg ( user, output ( BAN_TIME_ERROR, cmdName, cmd[5] )  );

                
            } else {
                reason = Handler.cutArrayIntoString ( cmd, 7 );

                res = this.addBan ( command, user, oper, usermask, expire, reason );
                if ( res >= 0 )  {
                    percent = String.format("%.02f", (float) res / Handler.getUserList().size() * 100 );
                    this.service.sendMsg ( user, output ( BAN_TIME, cmdName, usermask, ""+res, percent ) );
                    this.service.sendGlobOp ( output ( BAN_TIME_GLOB, cmdName, usermask, user.getOper().getName(), ""+res, percent ) );
                } else if ( res == -1 )  {
                    this.service.sendMsg ( user, output ( BAN_EXIST, cmdName, usermask ) );
                    
                } else if ( res == -2 )  {
                    /* lets be silent here */     
                }
            } 
        }
    }
     private void doDelBan ( int command, User user, String[] cmd )  {
         //  :DreamHealer PRIVMSG OperServ@stats.avade.net   :ban add fredde@172.* testing
        // :DreamHea1er PRIVMSG OperServ@services.sshd.biz :AKILL DEL   user@mask reason
        // :DreamHea1er PRIVMSG OperServ@services.sshd.biz :AKILL ADD   user@mask reason
        // :DreamHea1er PRIVMSG OperServ@services.sshd.biz :AKILL TIME  2d        user@mask reason
        //      0          1            2                     3    4     5            6       7
        Oper oper;
        
        String cmdName = ServicesBan.getCommandByHash ( command );
        
        if ( ! OSDatabase.checkConn ( )  )  {
            Handler.getOperServ().sendMsg ( user, "Database error. Please try again in a little while." );
            return;
        }
        
        if ( cmd.length < 6 && ( command == AKILL || command == IGNORE ) )  { 
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, cmdName+" <DEL> <#num|user@mask>" )  );
         
        } else if ( cmd.length < 6 && ( command == SQLINE ) ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, cmdName+" <DEL> <#num|nick|#chan>" )  );

        } else if ( ( oper = user.getSID().getOper ( ) ) == null ) {
            this.service.sendMsg ( user, output ( ACCESS_DENIED, "" ) );

        } else { 
            String usermask;
            int res = 0;
            try {
                int num         = Integer.parseInt ( cmd[5] );
                ServicesBan ban = this.getBan ( command, num );
                usermask        = ban.getMask ( );

            } catch ( NumberFormatException ex )  {
                usermask = cmd[5];
            }
            res = this.delBan ( command, usermask );  
            if ( res == 1 )  {
                this.service.sendMsg ( user, output ( BAN_DEL, cmdName, usermask )  );
                this.service.sendGlobOp ( output ( BAN_DEL_GLOB, cmdName, usermask, user.getOper().getName() ) );
            } else if ( res == -1 )  {
                this.service.sendMsg ( user, output (BAN_NO_EXIST, cmdName, usermask )  );                        
            } 
        }
    }
    private int addBan ( int command, User user, Oper oper, String usermask, String expire, String reason )  {
        ServicesBan current;
        ArrayList<User> uList = new ArrayList<>( );
        boolean foundOperMatch = false;
        String cmdName = ServicesBan.getCommandByHash ( command );
        if ( OperServ.isWhiteListed ( usermask ) )
            /* ban matches an address on whitelist */
            return -3;
        if ( ( current = Handler.getOperServ().findBan ( command, usermask ) ) != null ) {
            /* ban already exists */
            return -1;
        } 
        
        switch ( command ) {
            case SQLINE :
                uList = Handler.findUsersByNick ( usermask );
                break;
                
            case SGLINE :
                uList = Handler.findUsersByGcos ( usermask );
                break;
                
            case AKILL :
            case IGNORE :
            default :
                uList = Handler.findUsersByMask ( usermask );
                    
        }
                
        for ( User u : uList ) {
            if ( u.isOper ( ) ) {
                this.service.sendMsg ( user, output ( BAN_MATCH_OPER, cmdName, usermask, u.getString ( NAME ) ) );
                foundOperMatch = true;
            }
        }
        if ( foundOperMatch )  {
            return -2;
        }
        
        Handler.getOperServ().addServicesBan ( command, oper, usermask, expire, reason );      
        return uList.size();        
    }
    private int delBan ( int command, String usermask )  {
        ServicesBan ban = null;
        for ( ServicesBan b : Handler.getOperServ().getListByCommand ( command )  )  {
            if ( b.matchNoWild ( usermask )  )  {
                ban = b;
            }
        }
        if ( ban != null ) {
            return Handler.getOperServ().delServicesBan ( command, ban );
        }
        return -1;
        
    }
    private ServicesBan getBan ( int command, int num )  {
        ServicesBan ban = null;
        for ( ServicesBan b : Handler.getOperServ().getListByCommand ( command ) ) {
            if ( b.getID ( ) == num )  {
                ban = b;
            }
        }
        return ban;
    }
/*    private String dataToReason ( String[] cmd, int start )  {
        String reason = new String ( );
        int index = 0;
        for ( String buf : cmd )  {
            if ( index++ > start )  {
                if ( reason.length ( ) == 0 )  {
                    reason = buf;
                } else {
                    reason += " "+buf;
                }
            }
        }
        return reason;
    }
 */
    private void doSearchLog ( User user, String[] cmd ) {
        // :DreamHea1er PRIVMSG OperServ@services.sshd.biz :SEARCHLOG <nick|chan> [FULL]
        //            0       1                          2          3           4      5 < 6
        NickInfo ni;
        ChanInfo ci;
        
        if ( cmd.length < 5 ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "SEARCHLOG <nick|chan> [<FULL>]" )  );
            return;
        }
        
        String target = cmd[4];
        boolean full = false;
        
        if ( cmd.length > 5 && cmd[5].toUpperCase().hashCode() == FULL ) {
            full = true;
        }
        
        ArrayList<OSLogEvent> lsList = OSDatabase.getSearchLogList ( target, full );
        ArrayList<Comment> cList = OSDatabase.getCommentList ( target, full );
        
        this.service.sendMsg ( user, "*** Logs for "+target+( ! full ? " (1year)" : "" )+":" );
        for ( OSLogEvent log : lsList ) {
            this.service.sendMsg ( user, output ( SHOWLOG, log.getStamp(), log.getFlag(), log.getName(), log.getMask(), log.getOper() )  );
        }
        
        this.service.sendMsg ( user, "*** Comments"+( ! full ? " (1year)" : "" )+":" );
        for ( Comment comment : cList ) {
            this.service.sendMsg ( user, output ( SHOWCOMMENT, comment.getStamp(), comment.getInstater(), comment.getComment() ) );
        }
        this.service.sendMsg ( user, "*** End of log/comments ***" );
    }

    private void comment(User user, String[] cmd) {
        // :DreamHea1er PRIVMSG OperServ@services.sshd.biz :COMMENT <nick|chan> <comment here can be many words>
        //            0       1                          2        3           4                                5 = 6+
        
        if ( cmd.length < 6 ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "COMMENT <nick|chan> <Comment here>" )  );
            return;
        }
        String target = cmd[4];
        String string = Handler.cutArrayIntoString ( cmd, 5 );
        String instater = user.getOper().getName();
        Comment comment = new Comment ( target, instater, string, null );
        
        if ( OSDatabase.addComment ( comment ) ) {
            this.service.sendMsg ( user, output ( ADD_COMMENT, "successfully been added for "+target+"." ) );
        } else {
            this.service.sendMsg ( user, output ( ADD_COMMENT, "failed to be added for "+target+"." ) );
        }
    }
 
    private void global(User user, String[] cmd) {
        // :DreamHea1er PRIVMSG OperServ@services.sshd.biz :global <message here>
        // 0            1       2                          3       4                = 5+
        if ( cmd.length < 5 ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "GLOBAL <Message here>" ) );
            return;
        }
        
        String string = Handler.cutArrayIntoString ( cmd, 4 );
        Service global = Handler.getGlobal ( );
        
        OSLogEvent log = new OSLogEvent ( "-", GLOBAL, user, user.getOper().getNick() );
        log.setData ( string );
        OSDatabase.logEvent ( log );
        for ( User u : Handler.getUserList ( ) ) {
            global.sendMsg ( u, "[Global Notice]: "+string );
        }
    }

    
    private void doAudit(User user, String[] cmd) {
        // :DreamHea1er PRIVMSG OperServ@services.sshd.biz :SEARCHLOG <chan>
        //            0       1                          2          3      4 < 6
        String target = null;
        
        if ( cmd.length > 4 ) {
            target = cmd[4];
            this.service.sendMsg ( user, "*** Audit for "+target+":" );
        } else {
            this.service.sendMsg ( user, "*** Audit:" );
        }
        ArrayList<OSLogEvent> lsList;
        lsList = OSDatabase.getAuditList ( target );
        System.out.println ( "DEBUG: "+lsList.size() );
        for ( OSLogEvent log : lsList ) {
            this.service.sendMsg ( user, output ( SHOWAUDIT, log.getStamp(), log.getFlag(), log.getName(), log.getMask(), log.getOper(), log.getData() ) );
        }
        this.service.sendMsg ( user, "*** End of Audit ***" );
        
    }
   
    private void doBanlog(User user, String[] cmd) {
        String target = null;
        
        if ( ! OSDatabase.checkConn ( ) ) {
            Handler.getOperServ().sendMsg ( user, "Database error. Please try again in a little while." );
            return;
        } 
        
        if ( cmd.length > 4 ) {
            target = cmd[4];
            this.service.sendMsg ( user, "*** BanLog for "+target+":" );
        } else {
            this.service.sendMsg ( user, "*** BanLog:" );
        }
        ArrayList<OSLogEvent> lsList;
        lsList = OSDatabase.getBanLogList ( target );
        System.out.println ( "DEBUG: "+lsList.size() );
        for ( OSLogEvent log : lsList ) {
            System.out.println("DEBUG: logID: "+log.getID() );
            this.service.sendMsg ( user, output ( SHOWBANLOG, log.getStamp(), log.getFlag(), log.getName(), log.getMask(), log.getOper(), log.getData() ) );
        }
        this.service.sendMsg ( user, "*** End of Audit ***" ); 
        
    }
    
    private void doJupe ( User user, String[] cmd ) {
        // :DreamHea1er PRIVMSG OperServ@services.sshd.biz :jupe <SERVERNAME> 
        //            0       1                          2     3 4           < 6
        if ( cmd.length < 5 ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "JUPE <servername.netname.net>" ) );
            return;
        } else if ( ! cmd[4].contains ( "." ) ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "JUPE <servername.netname.net> (all servers need to contain dots in their names)" ) );
            return;
        } else if ( cmd[4].equalsIgnoreCase ( Proc.getConf().get ( CONNNAME ) ) ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "JUPE <servername.netname.net> (services hub cannot be juped)" ) );
            return;
        }
        String name = cmd[4];
        Handler.getOperServ().sendServ("SERVER "+name+" 1 :Jupitered by: "+user.getOper().getName() );
        this.service.sendMsg (user, "Server "+name+" has been Jupitered." );
    }
    
    private void doServer(User user, String[] cmd) {
        // :DreamHea1er PRIVMSG OperServ@services.sshd.biz :SERVER <DEL> <SERVERNAME> 
        //            0       1                          2       3     4            5     < 7
        if ( cmd.length < 5 ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "SERVER <DEL|MISSING|LIST> [<servername>]" ) );
            return;
        }  
        
        ArrayList<NetServer> sList;
        
        switch ( cmd[4].toUpperCase().hashCode() ) {
            case LIST :
                this.service.sendMsg ( user, "*** Server List:");
                this.service.sendMsg ( user, "  Hub(s):");
                for ( NetServer server : OperServ.getServers ( HUB, false ) ) {
                    this.service.sendMsg ( user, "    "+server.getName ( )+" -> P:"+server.getPrimary()+" S:"+server.getSecondary() );
                }
                this.service.sendMsg ( user, "  Leaf(s):");
                for ( NetServer server : OperServ.getServers ( LEAF, false ) ) {
                    this.service.sendMsg ( user, "    "+server.getName ( )+" -> P:"+server.getPrimary()+" S:"+server.getSecondary() );
                }
                this.service.sendMsg ( user, "(P = Primary hub, S = Secondary hub)");
                this.service.sendMsg ( user, "*** End of List ***");
                break;
                
            case DEL :
                if ( cmd.length == 6 && OperServ.addDelServer ( cmd[5] ) ) {
                    this.service.sendMsg ( user, "Server "+cmd[5]+" was successfully removed from list.");
                } else {
                    this.service.sendMsg ( user, "Error: Server "+cmd[5]+" was not removed from list.");
                }
                break;
                
            case MISSING :
                this.service.sendMsg ( user, "*** Missing Servers:");
                this.service.sendMsg ( user, "  Hub(s):");
                for ( NetServer server : OperServ.getServers ( HUB, true ) ) {
                    this.service.sendMsg ( user, "    "+server.getName ( )+" -> P:"+server.getPrimary()+" S:"+server.getSecondary() );
                }
                this.service.sendMsg ( user, "  Leaf(s):");
                for ( NetServer server : OperServ.getServers ( LEAF, true ) ) {
                    this.service.sendMsg ( user, "    "+server.getName ( )+" -> P:"+server.getPrimary()+" S:"+server.getSecondary() );
                }
                this.service.sendMsg ( user, "(P = Primary hub, S = Secondary hub)");
                this.service.sendMsg ( user, "*** End of List ***");
                break;
                
            default :
                this.service.sendMsg ( user, "Error: unknown command");
                break;

        }
    }
    
    private void doStaff ( User user, String[] cmd ) {
                
        CmdData cmdData = this.validateCommandData ( user, STAFF, cmd );
        
        switch ( cmdData.getStatus ( ) ) {
            case SYNTAX_ERROR :
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "STAFF <LIST|SRA|CSOP|SA|IRCOP> [<ADD|DEL>] [<nick>]" ) );
                return;
                
            case SUB_SYNTAX_ERROR :
                this.service.sendMsg ( user, output ( SUB_SYNTAX_ERROR, "LIST, SRA, CSOP, SA and IRCOP" ) );
                return;
                
            case SUB2_SYNTAX_ERROR :
                this.service.sendMsg ( user, output ( SUB2_SYNTAX_ERROR, "ADD and DEL" ) );
                return;
                
            case NICK_NOT_REGISTERED :
                this.service.sendMsg ( user, output ( NICK_NOT_REGISTERED, cmdData.getString1 ( ) ) );
                return;

            case ACCESS_DENIED :
                this.service.sendMsg ( user, output ( ACCESS_DENIED, "" ) );
                return;
                    
            case NOT_ENOUGH_ACCESS :
                this.service.sendMsg ( user, output ( NOT_ENOUGH_ACCESS, cmdData.getString1 ( ) ) );
                return;
                
            case SHOWLIST :
                ArrayList<Oper> sraList = OperServ.getRootAdmins();
                ArrayList<Oper> csopList = OperServ.getCSops();
                ArrayList<Oper> saList = OperServ.getServicesAdmins();
                ArrayList<Oper> ircopList = OperServ.getIRCops();
                
                this.service.sendMsg(user, "--- Services Root Admins (SRA) ---");
                for ( Oper oper : sraList ) {
                    this.service.sendMsg(user, "  "+oper.getName() );
                }
                
                this.service.sendMsg(user, "--- Channel Services Operators (CSop) ---");
                for ( Oper oper : csopList ) {
                    this.service.sendMsg(user, "  "+oper.getName() );
                }
                
                this.service.sendMsg(user, "--- Services Admins (SA) ---");
                for ( Oper oper : saList ) {
                    this.service.sendMsg(user, "  "+oper.getName() );
                }
                
                this.service.sendMsg(user, "--- IRC Operators (IRCOP) ---");
                for ( Oper oper : ircopList ) {
                    this.service.sendMsg(user, "  "+oper.getName() );
                }
                
                this.service.sendMsg(user, "*** End of List ***");
                return;

        }
        
        NickInfo ni = cmdData.getNick();
        int access = Oper.hashToAccess ( cmdData.getSub ( ) );
        String accessStr = Oper.hashToStr ( cmdData.getSub ( ) );
        int command = cmdData.getSub2();
        Oper oper;
        OSLogEvent log;
        
        switch ( command ) {
            case DEL :
                oper = ni.getOper ( );
                this.service.sendMsg ( user, output ( STAFF_DEL, accessStr, ni.getName ( ) ) );
                this.service.sendGlobOp ( output ( GLOB_STAFF_DEL, accessStr, user.getOper().getName(), ni.getName ( ) ) );
                this.msgUsersByNick ( ni, output ( NICK_NO_LONGER_STAFF, accessStr, ni.getName ( ) ) ); /* Msg all users identified to nick */
                log = new OSLogEvent ( ni.getName(), "del"+accessStr, user.getFullMask(), user.getOper().getName() );
                OperServ.addLogEvent ( log );
                OperServ.delOper ( ni );
                ni.setOper ( new Oper ( ) );
                break;
                
            case ADD :
                oper = new Oper ( ni.getName(), access, user.getOper().getName() );
                this.service.sendMsg ( user, output ( STAFF_ADD, accessStr, ni.getName ( ) ) );
                this.service.sendGlobOp ( output ( GLOB_STAFF_ADD, accessStr, user.getOper().getName(), ni.getName ( ) ) );
                this.msgUsersByNick ( ni, output ( NICK_NOW_STAFF, accessStr, ni.getName ( ) ) ); /* Msg all users identified to nick */
                log = new OSLogEvent ( ni.getName(), "add"+accessStr, user.getFullMask(), user.getOper().getName() );
                OperServ.addLogEvent ( log );
                OperServ.addOper ( oper );
                ni.setOper ( oper );
                break;
                 
        }
        
        for ( User u : Handler.findUsersByNick ( ni ) ) {
            Handler.forceOperModes ( u );
        }

    }
  
    private void doSpamFilter(User user, String[] cmd) {
               
        CmdData cmdData = this.validateCommandData ( user, SPAMFILTER, cmd );
        switch ( cmdData.getStatus ( ) ) {
            case SYNTAX_ERROR :
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "SPAMFILTER <add|del|list> <string> <flag> <reason>" ) );
                return;
        
            case BADFLAGS :
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, cmdData.getString1 ( ) ) );
                return;
                
            case FILTER_EXISTS :
                this.service.sendMsg ( user, output ( FILTER_EXISTS, cmdData.getString1 ( ) ) );
                return;
                
            default :
                
        }
        String pattern;
        SpamFilter sFilter;
        switch ( cmdData.getStatus ( ) ) {
            case SHOWLIST :
                this.service.sendMsg ( user, "*** SpamFilter LIST ***" );
                for ( SpamFilter sf : OperServ.getSpamFilters ( ) ) {
                    this.service.sendMsg ( user, output ( SPAMFILTER_LIST, sf.getPattern(), sf.getFlags(), sf.getInstater(), sf.getExpire(), sf.getReason() ) );
                }
                this.service.sendMsg ( user, "*** End of List ***" );
                break;
                
            case DEL :
                pattern = cmdData.getString1();
                if ( ( sFilter = OperServ.findSpamFilter ( pattern ) ) != null ) {
                    OperServ.remSpamFilter ( sFilter );
                    this.service.sendServ ( "SF "+pattern+" 0" );
                    this.service.sendGlobOp ( user.getOper().getNick().getName()+" removed SpamFilter: "+pattern  );
                }
                break;
                
            case ADD :
                pattern = cmdData.getString1();
                String flags = cmdData.getString2();
                String time = cmdData.getString3();
                String reason = cmdData.getString4();
                String stamp = dateFormat.format ( new Date ( ) );
                String expire = Handler.expireToDateString ( stamp, time );
                sFilter = new SpamFilter ( System.nanoTime(), pattern, flags, user.getOper().getNick().getName(), reason, stamp, expire );
                
                
                //  sendto_one(acptr, "SF %s %ld :%s", sf->text, sf->flags, sf->reason);
                OperServ.addSpamFilter ( sFilter );
                this.service.sendServ ( "SF "+pattern+" "+sFilter.getBitFlags()+" :"+reason );
                this.service.sendGlobOp ( user.getOper().getNick().getName()+" added SpamFilter: "+pattern+" Flags: "+flags+" Expire: "+time+" Reason: "+reason  );
                break;
                
            default :
            
        }
        
    }
  
    /*
    -OperServ- Behavior Flags:
    -OperServ- s = strip control codes (will strip colors/bolds/underlines/etc)
    -OperServ- S = strip non-alphanumeric characters (will strip spaces too)
    -OperServ- r = regexp (limited to SRAs)
    -OperServ- m = match registered nicks (limited to SRAs)
    -OperServ- Target flags:
    -OperServ- p = private message
    -OperServ- n = notice
    -OperServ- k = kick message
    -OperServ- q = quit message
    -OperServ- t = topic
    -OperServ- a = away message
    -OperServ- c = channel message/notice
    -OperServ- P = part message
    -OperServ- Action flags:
    -OperServ- W = warn user
    -OperServ- L = lag user
    -OperServ- R = report to opers (with umode +m)
    -OperServ- B = block message
    -OperServ- K = kill the user
    -OperServ- A = akill the user
    -OperServ- Special flags:
    -OperServ- 1 = Shortcut to warn private massads (contain the flags: spnWR)
    -OperServ- 2 = Shortcut to block+akill private massads (contain the flags: spnWRBA)
    -OperServ- 3 = Shortcut to warn channel massads (contain the flags: scWR)
    -OperServ- 4 = Shortcut to block+akill channel massads (contain the flags: scWRBA)
    */
    
    private boolean isFlagsGood ( String flags ) {
        for ( int index = 0; index < flags.length(); index++ ) {
            switch ( String.valueOf(flags.charAt(index)).hashCode() ) {
                case s :
                case S :
                case r :
                case m :
                case p :
                case n :
                case k :
                case q :
                case t :
                case a :
                case c :
                case P :
                case W :
                case L :
                case R :
                case B :
                case K :
                case A :
                case NUM_1 :
                case NUM_2 :
                case NUM_3 :
                case NUM_4 :
                    break;
                    
                default :
                    return false;
            }
        }
        return true;        
    }
    
    private CmdData validateCommandData ( User user, int command, String[] cmd ) {
        CmdData cmdData = new CmdData ( );
        NickInfo ni;
        int sub;
        int sub2;
        int flag;
        String time;
        
        switch ( command )  {
            
            case SPAMFILTER :
                // :DreamHea1er PRIVMSG OperServ@services.sshd.biz :SPAMFILTER add hello?hello?hello flags 1d spamming is not allowed
                //            0       1                          2           3   4                 5     6  7       8+              = 9+
                sub = cmd.length > 4 ? cmd[4].toUpperCase().hashCode() : 0;
                time = cmd.length > 7 ? cmd[7] : "1000y";
                flag = cmd.length > 6 ? cmd[6].toUpperCase().hashCode() : 0;
                if ( sub == LIST ) {
                    cmdData.setStatus ( SHOWLIST );
                } else if ( cmd.length == 6 && sub == DEL ) {
                    cmdData.setString1 ( cmd[5] );
                    cmdData.setStatus ( DEL );
                } else if ( isShorterThanLen ( 6, cmd ) ) {
                    cmdData.setStatus ( SYNTAX_ERROR );
                } else if ( cmd.length > 6 && ! this.isFlagsGood ( cmd[6] ) ) {
                    cmdData.setString1 ( cmd[6] );
                    cmdData.setStatus ( BADFLAGS );
                } else if ( OperServ.findSpamFilter ( cmd[5] ) != null ) {
                    cmdData.setString1 ( cmd[5] );
                    cmdData.setStatus ( FILTER_EXISTS );
                } else {
                    cmdData.setString1 ( cmd[5] );
                    cmdData.setString2 ( cmd[6] );
                    cmdData.setString3 ( cmd[7] );
                    cmdData.setString4 ( Handler.cutArrayIntoString ( cmd, 8 ) );
                    cmdData.setStatus ( ADD );
                }
                break;
                
            case STAFF :
                // :DreamHea1er PRIVMSG OperServ@services.sshd.biz :STAFF LIST
                // :DreamHea1er PRIVMSG OperServ@services.sshd.biz :STAFF CSOP ADD Pintuz
                // :DreamHea1er PRIVMSG OperServ@services.sshd.biz :STAFF CSOP DEL Pintuz
                //  0           1       2                           3     4    5    6       = 7
                if ( isShorterThanLen( 5, cmd ) ) {
                    cmdData.setStatus ( SYNTAX_ERROR );
                } else if ( ( sub = cmd[4].toUpperCase().hashCode ( ) ) == LIST ) {
                    cmdData.setSub ( sub );
                    cmdData.setStatus ( SHOWLIST );
                } else if ( isShorterThanLen ( 7, cmd) ) {
                    cmdData.setStatus ( SYNTAX_ERROR );
                } else if ( sub != SRA && sub != CSOP && sub != SA && sub != IRCOP ) {
                    cmdData.setSub ( sub );
                    cmdData.setStatus ( SUB_SYNTAX_ERROR );
                } else if ( ( sub2 = cmd[5].toUpperCase().hashCode() ) != ADD && sub2 != DEL ) {
                    cmdData.setStatus ( SUB2_SYNTAX_ERROR );
                } else if ( ( ni = NickServ.findNick ( cmd[6] ) ) == null ) {
                    cmdData.setString1 ( user.getString ( NAME ) );
                    cmdData.setStatus ( NICK_NOT_REGISTERED );
                } else if ( user.getAccess() <= ni.getAccess() ) {
                    cmdData.setStatus ( ACCESS_DENIED );
                } else if ( user.getAccess() <= Oper.hashToAccess ( sub ) ) {
                    String access = Oper.accessToStr ( ( Oper.hashToAccess ( sub ) + 1 ) );
                    cmdData.setString1 ( access );
                    cmdData.setStatus ( NOT_ENOUGH_ACCESS );
                } else {
                    cmdData.setNick ( ni );
                    cmdData.setSub ( sub );
                    cmdData.setSub2 ( sub2 );
                } 
                break;
                
            default :
                
        }
        
        return cmdData;
    }
 
    /* Send b message to all users identified to specific nickname */
    private void msgUsersByNick ( NickInfo ni, String msg )  {
        for ( User user : Handler.findUsersByNick ( ni )  )  {
            this.service.sendMsg ( user, msg );
        }
    }
      
    public String output ( int code, String... args )  {
        switch ( code )  {
            case SYNTAX_ERROR :
                return "Syntax: /OperServ "+args[0];
                   
            case SUB_SYNTAX_ERROR :
                return "Sub-commands available: "+args[0];
            
            case SUB2_SYNTAX_ERROR :
                return "Sub2-commands available: "+args[0];
                
            case ACCESS_DENIED :
                return "Access denied.";
                  
            case NOT_ENOUGH_ACCESS :
                return "Access denied. You need to be atleast "+args[0]+" to perform that action.";
                
            case BAN_ADD :
                return "Permanent "+args[0]+" for "+args[1]+" was successfully placed. Affecting "+args[2]+" users ["+args[3]+"%]";
                
            case BAN_ADD_GLOB :
                return args[0]+" for "+args[1]+" was successfully placed by "+args[2]+". Affecting "+args[3]+" users ["+args[4]+"%]";
                
            case BAN_EXIST :
                return args[0]+" already exists for "+args[1]+".";
                
            case BAN_NO_EXIST :
                return "An "+args[0]+" for "+args[1]+" not found.";
                
            case BAN_TIME :
                return "Timed "+args[0]+" for "+args[1]+" was successfully placed. Affecting "+args[2]+" users ["+args[3]+"%]";
                
            case BAN_TIME_GLOB :
                return args[0]+" for "+args[1]+" was successfully placed by "+args[2]+". Affecting "+args[3]+" users ["+args[4]+"%]";

            case BAN_DEL :
                return args[0]+" for "+args[1]+" was successfully removed.";
                
            case BAN_DEL_GLOB :
                return args[0]+" for "+args[1]+" was successfully removed by "+args[2]+".";
                
            case BAN_LIST :
                return args[0]+". "+args[1]+" "+args[2]+" ["+args[3]+"]: "+args[4]; 
                
            case BAN_LIST_START :
                return args[0]+" List "+ (  ( ! args[1].isEmpty ( ) ) ?"matching: "+args[1]:"" );
                
            case BAN_LIST_STOP :
                return "*** End of List ***";
                         
            case AKILL_LIST_NONE :
                return "Error: No matching akill(s) found.";
                
            case BAN_TIME_ERROR :
                return "Error: "+args[0]+" time "+args[1]+" is not a valid command use num followed by m, h or d.  ( ex. 10d ) ";
                
            case BAN_MATCH_OPER :
                return "Error: "+args[0]+" for "+args[1]+" matches oper "+args[2]+"";
                
            case NICK_NOT_REGGED :
                return "Nick "+args[0]+" is not registered";
                
            case STAFF_ADD :
                return "Nick "+args[1]+" was added to the "+args[0]+" list";
                
            case STAFF_NOT_ADD :
                return "Nick "+args[1]+" was NOT added to the "+args[0]+" list";
                
            case STAFF_DEL :
                return "Nick "+args[1]+" was deleted from the "+args[0]+" list";
                
            case STAFF_NOT_DEL :
                return "Nick "+args[1]+" was NOT deleted from the "+args[0]+" list";
                
            case NICK_NOW_STAFF :
                return "Nick "+args[1]+" to which you have identified has been added to the "+args[0]+" list";
                
            case NICK_NO_LONGER_STAFF :
                return "Nick "+args[1]+" to which you have identified has been removed from the "+args[0]+" list";
                
            case GLOB_STAFF_ADD :
                return args[1]+" has added "+args[2]+" to the "+args[0]+" list";
                
            case GLOB_STAFF_DEL :
                return args[1]+" has removed "+args[2]+" from "+args[0]+" list";
 
            case SHOWLOG :
                return "["+args[0]+"] "+args[1]+" "+(args[1].length()==2?"":" ")+args[1]+" "+(args[1].length()==2?"":" ")+args[3]+" "+( args[4] != null && args[4].length() > 0 ? "["+args[4]+"]" : "" );
            
            case SHOWAUDIT :
                return "["+args[0]+"] "+args[1]+" "+args[1]+" "+args[2]+" "+args[3]+" "+( args[4] != null ? "["+args[4]+"]" : "" )+(args[5] != null ? ": "+args[5] : "");
                             
            case SHOWBANLOG :
                return "["+args[0]+"] "+args[1]+" "+args[1]+" [Ticket:"+args[2]+"] "+args[3]+" ["+args[4]+"] "+(args[5] != null ? ": "+args[5] : "");
                     
            case SHOWCOMMENT :
                return "["+args[0]+"] "+args[1]+": "+args[2]+"";
                       
            case ADD_COMMENT :
                return "Comment has "+args[0];
                     
            case BADFLAGS :
                return "Error: Bad flags found in: "+args[0]+" consult the help text for valid flags.";
                   
            case FILTER_EXISTS :
                return "Error: There is already a spamfilter matching: "+args[0]+".";
             
            case SPAMFILTER_LIST :
                return "  "+args[0]+": "+args[1]+" "+args[2]+" ["+args[3]+"]: "+args[4]; 
               
                
            default:
                return "";
                
        }
    }

    private static final int SYNTAX_ERROR           = 1001;
    private static final int ACCESS_DENIED          = 1002;
    private static final int NOT_ENOUGH_ACCESS      = 1003;
    private static final int BAN_ADD              = 1011;
    private static final int BAN_ADD_GLOB         = 1012;
    private static final int BAN_EXIST            = 1013;
    private static final int BAN_TIME             = 1014;
    private static final int BAN_TIME_GLOB        = 1015;
    private static final int BAN_NO_EXIST         = 1016;
    private static final int BAN_DEL              = 1017;
    private static final int BAN_DEL_GLOB         = 1018;
    private static final int BAN_LIST             = 1019;
    private static final int BAN_LIST_START       = 1020;
    private static final int BAN_LIST_STOP        = 1021;
    private static final int BAN_TIME_ERROR       = 1022;
    private static final int BAN_MATCH_OPER       = 1023;
    private static final int AKILL_LIST_NONE        = 1024;
    

    private static final int STAFF_ADD              = 1101;
    private static final int STAFF_NOT_ADD          = 1102;
    private static final int STAFF_DEL              = 1103;
    private static final int STAFF_NOT_DEL          = 1104;
    private static final int NICK_NOT_REGGED        = 1105;
    private static final int NICK_NOW_STAFF         = 1106;
    private static final int NICK_NO_LONGER_STAFF   = 1107;

    private static final int GLOB_STAFF_ADD          = 1201;
    private static final int GLOB_STAFF_DEL          = 1202;

    private static final int SHOWLOG                = 2001;
    private static final int SHOWAUDIT              = 2002;
    private static final int SHOWAUDITGLOBAL        = 2003;
    
    private static final int SHOWCOMMENT            = 2011;
    private static final int ADD_COMMENT            = 2012;

    private static final int SUB_SYNTAX_ERROR       = 3001;
    private static final int SUB2_SYNTAX_ERROR      = 3002;

    private final static int NICK_NOT_REGISTERED    = 3011;
    private final static int SHOWLIST               = 3051;
    private final static int SHOWBANLOG             = 3052;

    private final static int BADFLAGS               = 3101;
    private final static int FILTER_EXISTS          = 3102;
    private final static int SPAMFILTER_LIST        = 3103;


}
