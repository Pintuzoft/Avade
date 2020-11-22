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
 * You should have received ban2 copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package operserv;

import channel.Chan;
import chanserv.ChanInfo;
import core.Executor;
import core.Handler;
import core.Proc;
import static core.HashNumeric.ADD;
import static core.HashNumeric.DEL;
import static core.HashNumeric.LIST;
import static core.HashNumeric.NAME;
import core.HashString;
import core.Service;
import guestserv.GuestServ;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
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
        HashString command = new HashString ( cmd[3] );
        
        if ( command.is(UINFO) ) {
            this.doUInfo ( user, cmd );
        
        } else if ( command.is(CINFO) ) {
            this.doCInfo ( user, cmd );
        
        } else if ( command.is(NINFO) ) {
            this.doNInfo ( user, cmd );
        
        } else if ( command.is(SINFO) ) {
            this.doSInfo ( user, cmd );
        
        } else if ( command.is(ULIST) ) {
            this.doUList ( user );
        
        } else if ( command.is(CLIST) ) {
            this.doCList ( user );
        
        } else if ( command.is(SLIST) ) {
            this.doSList ( user );
        
        } else if ( command.is(UPTIME) ) {
            this.doUpTime ( user );
        
        } else if ( command.is(SQLINE) ||
                    command.is(SGLINE) ||
                    command.is(IGNORE) ||
                    command.is(AKILL) ||
                    command.is(AUTOKILL) ) {
            if ( command.is(AUTOKILL) ) {
                command = AKILL;
            }
            this.doServicesBan ( command, user, cmd );
        
        } else if ( command.is(STAFF) ) {
            this.doStaff ( user, cmd );
        
        } else if ( command.is(SEARCHLOG) ) {
            this.doSearchLog ( user, cmd );
        
        } else if ( command.is(SNOOPLOG) ) {
            this.doSnoopLog ( user, cmd );
        
        } else if ( command.is(COMMENT) ) {
            this.comment ( user, cmd );
        
        } else if ( command.is(GLOBAL) ) {
            this.global ( user, cmd );
        
        } else if ( command.is(AUDIT) ) {
            this.doAudit ( user, cmd );
        
        } else if ( command.is(BANLOG) ) {
            this.doBanlog ( user, cmd );
        
        } else if ( command.is(JUPE) ) {
            this.doJupe ( user, cmd );
        
        } else if ( command.is(SERVER) ) {
            this.doServer ( user, cmd );
        
        } else if ( command.is(SPAMFILTER) ) {
            this.doSpamFilter ( user, cmd );
        
        } else if ( command.is(FORCENICK) ) {
            this.forcenick ( user, cmd );
        
        } else if ( command.is(BAHAMUT) ) {
            this.bahamut ( user, cmd );
        
        } else if ( command.is(MAKILL) ) {
            this.makill ( user, cmd );
        
        } else {
            this.found = false; 
            this.noMatch ( user, cmd[3] );
        }
         
        this.snoop.msg ( this.found, user, cmd ); 
    }

    private void bahamut ( User user, String[] cmd ) {
        this.service.sendMsg ( user, "This version of services works best with the following version of bahamut:" );
        this.service.sendMsg ( user, "    "+Proc.getVersion().getBahamut() );
    }

    private void doUInfo ( User user, String[] cmd )  {
        /* UINFO requested so lets send all we know about that user */
        User u;
 
        if ( cmd.length < 5 )  { /* size hasAccess 5  ( 0-4 )  */
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
                if ( u.getSID().getOper ( )  != null )  {
                    this.service.sendMsg ( user, "   ID Oper: "+u.getSID().getOper().getString ( NAME ) );
                }
            }
            this.service.sendMsg ( user, "        IP: " + u.getString ( IP )                                        );
            this.service.sendMsg ( user, "     Modes: ident ( "+u.getModes().is ( IDENT )+" ), oper ( "+u.getModes().is ( OPER )+" ) , admin ( "+u.getModes().is ( ADMIN )+" ) , sadmin ( "+u.getModes().is ( SADMIN )+" ) " );
            this.service.sendMsg ( user, "    Server: "+u.getServ().getName ( )                                     );
            for ( Chan c : user.getChans() ) {
                this.service.sendMsg ( user, "   Channel: "+c.getString ( NAME )                                    );
            }
            this.service.sendMsg ( user, "*** End ***"                                                              );
        } else {
            this.service.sendMsg ( user, "Error: nick is offline"                                                   );
        }

    }

    private void doSInfo ( User user, String[] cmd )  {
        /* UINFO requested so lets send all we know about that user */
        Server s;
        
        if ( cmd.length < 5 )  { /* size hasAccess 5  ( 0-4 )  */
            this.service.sendMsg ( user, "Syntax: /OperServ UINFO server" );
            return;
        }
        
        if ( ( s = Handler.findServer ( cmd[4] ) ) != null ) {
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
        int counter = 0;
        String users    = new String ( );
        String voice    = new String ( );
        String oped     = new String ( );
        String modes    = new String ( );
        
        if ( cmd.length < 5 )  {/* size hasAccess 5  ( 0-4 )  */
            this.service.sendMsg ( user, "Syntax: /OperServ CINFO #Chan" );
            return;
        }
        
        if (  ( c = Handler.findChan ( cmd[4] )  )  != null )  {
            modes = c.getModes().getModes ( );
            this.service.sendMsg ( user, "*** Chan INFO ***"                    );
            this.service.sendMsg ( user, "    Name: "+c.getString ( NAME )      );
            this.service.sendMsg ( user, "   Modes: "+modes                     );
            /* GET OP LIST */ 
            for ( User cu : c.getList ( OP )  )  {
                if ( oped.isEmpty ( )  )  {
                    oped = cu.getString ( NAME );
                } else {
                    oped += " "+cu.getString ( NAME );
                }
                if ( ++counter > 10 ) {
                    this.service.sendMsg ( user, "      Op: "+oped                      );
                    counter = 0;
                    oped = "";
                }
            }
            this.service.sendMsg ( user, "      Op: "+oped                      );

            /* GET VOICE LIST */
            counter = 0;
            for ( User cu : c.getList ( VOICE )  )  {
                if ( voice.isEmpty ( )  )  {
                    voice = cu.getString ( NAME );
                } else {
                    voice += " "+cu.getString ( NAME );
                }
                if ( ++counter > 10 ) {
                    this.service.sendMsg ( user, "   Voice: "+voice                      );
                    counter = 0;
                    voice = "";
                }
            }
            this.service.sendMsg ( user, "   Voice: "+voice                      );

            /* GET USER LIST */
            counter = 0;
            for ( User cu : c.getList ( USER )  )  {
                if ( users.isEmpty ( )  )  {
                    users = cu.getString ( NAME );
                } else {
                    users += " "+cu.getString ( NAME );
                }
                if ( ++counter > 10 ) {
                    this.service.sendMsg ( user, "   Users: "+users                      );
                    counter = 0;
                    users = "";
                }
            }
            this.service.sendMsg ( user, "   Users: "+users                      );

            
            this.service.sendMsg ( user, "*** End ***"                          );

        } else {
            this.service.sendMsg ( user, "Error: chan is offline"               );
        } 
    }

    private void doNInfo ( User user, String[] cmd ) {
        //:DreamHealer PRIVMSG OperServ@stats.avade.net :ninfo pintuz
        //           0       1                        2      3      4
        NickInfo ni;
        if ( ! OperServ.enoughAccess ( user, NINFO ) ) {
            return;
        }
        if ( cmd.length < 5 ) {
            this.service.sendMsg ( user, "Syntax: /OperServ NINFO <nick>" );
            return;
        }
        
        ni = NickServ.findNick(cmd[4]);
        if ( ni == null ) {
            this.service.sendMsg ( user, "Error: Nick "+cmd[4]+" is not registered" );
            return;
        }
        
        this.service.sendMsg ( user, "*** NInfo: "+ni.getName() );
        this.service.sendMsg ( user, "      user: "+ni.getString(USER) );
        this.service.sendMsg ( user, "      host: "+ni.getString(HOST) );
        this.service.sendMsg ( user, "        IP: "+ni.getString(IP) );
        this.service.sendMsg ( user, "      mail: "+( ni.getEmail ( ) != null ? ni.getEmail ( ) : "" ) );
        this.service.sendMsg ( user, "      pass: "+ni.getPass ( ) );
        this.service.sendMsg ( user, "  settings: "+ni.getSettings().getInfoStr() );
        this.service.sendMsg ( user, "   regtime: "+ni.getString(REGTIME) );
        this.service.sendMsg ( user, "  lastused: "+ni.getString(LASTUSED) );
        this.service.sendMsg ( user, "      oper: "+(ni.getOper() != null?ni.getOper().getString(ACCSTRING):"" ) );
        this.service.sendMsg ( user, "  throttle: "+ni.getThrottle().isThrottled() );
        this.service.sendMsg ( user, "      auth: "+ni.getAuth() );
        this.service.sendMsg ( user, "*** End of NInfo *** " );
        
    }
    
    private void doUList ( User user )  {
        Server s = Handler.findServer ( Proc.getConf().get ( CONNNAME ) );
        if ( ! OperServ.enoughAccess ( user, UINFO ) ) {
            return;
        }
        s.recursiveUserList ( user, " " );
    }
    
    private void doCList ( User user )  {
        this.service.sendMsg ( user, "*** Channel List ***" );
        for ( Chan c : Handler.getChanList() ) {
            this.service.sendMsg ( user, " - "+c.getString(NAME) );
        }
        this.service.sendMsg ( user, "*** End of List ***" );
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

    private void doServicesBan ( HashString command, User user, String[] cmd )  {
        // :DreamHealer PRIVMSG OperServ@stats.avade.net   :ban add fredde@172.* testing
        // 0            1       2                          3      4   5            6
        
        System.out.println("Bans: "+Handler.getOperServ().getAkillCount()+":"+Handler.getOperServ().getIgnoreCount());
        
        if ( cmd.length > 4 && cmd[4].charAt(0) == '-' ) {
            String[] buf = new String[6];
            buf[0] = cmd[0];
            buf[1] = cmd[1];
            buf[2] = cmd[2];
            buf[3] = cmd[3];
            buf[4] = "DEL";
            StringBuilder builder = new StringBuilder ( cmd[4] );
            buf[5] = builder.deleteCharAt(0).toString();
            cmd = buf;            
        }

        CMDResult result = this.validateCommandData ( user, command, cmd );
        
        String cmdName = ServicesBan.getCommandByHash ( command );
        
        if ( result.is(SYNTAX_ERROR) ) {
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, cmdName+" <add|del|list|info> <pattern> [<time>] [<reason>]" ) );
                return;            
        
        } else if ( result.is(SYNTAX_ERROR_DEL) ) {
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, cmdName+" DEL <pattern>" ) );
                return;            
        
        } else if ( result.is(SYNTAX_ERROR_LIST) ) {
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, cmdName+" LIST <pattern>" ) );
                return;            
        
        } else if ( result.is(SYNTAX_ERROR_INFO) ) {
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, cmdName+" INFO <Ban ID>" ) );
                return;            
        
        } else if ( result.is(SYNTAX_ERROR_ADD) ) {
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, cmdName+" ADD <pattern> <time> <reason>" ) );
                return;            
        
        } else if ( result.is(BADTIME) ) {
                this.service.sendMsg ( user, output ( BADTIME, "" ) );
                return;            
        
        } else if ( result.is(BADREASON) ) {
                this.service.sendMsg ( user, output ( BADREASON, "" ) );
                return;            
        
        } else if ( result.is(BAN_EXIST) ) {
                this.service.sendMsg (user, output (BAN_EXIST, cmdName, result.getString1() ) );
                return;            
        
        } else if ( result.is(BAN_NO_EXIST) ) {
                this.service.sendMsg (user, output (BAN_NO_EXIST, cmdName, result.getString1() ) );
                return;            
        
        } else if ( result.is(ACCESS_DENIED) ) {
                this.service.sendMsg ( user, output ( ACCESS_DENIED, "" ) );
                return;            
        
        } else if ( result.is(WHITELISTED) ) {
                this.service.sendMsg (user, output (WHITELISTED, result.getString1() ) );
                return;            
        }  
         
        String string1 = result.getString1();
        NickInfo oper = user.getOper().getNick();
        ServicesBan ban;
        ArrayList<User> uList = new ArrayList<>();

        HashString subcommand = new HashString ( cmd[4] );
     
        if ( subcommand.is(LIST) ) {
                ArrayList<ServicesBan> bList = OperServ.findBansByPattern ( command, string1 );
                this.service.sendMsg ( user, output ( BAN_LIST_START, cmdName, string1 ) );
                for ( ServicesBan b : bList ) {
                    this.service.sendMsg ( user, output ( BAN_LIST, b.getMask().getString(), b.getID().getString(), b.getInstater(), b.getReason() ) );
                } 
                this.service.sendMsg ( user, output ( BAN_LIST_STOP, "" ) );            
        } else if ( subcommand.is(INFO) ) {
                ban = result.getServicesBan();
                if ( ban != null ) {
                    this.service.sendMsg ( user, "*** "+cmdName+" INFO:" );
                    this.service.sendMsg ( user, "   BanID: "+ban.getID() );
                    this.service.sendMsg ( user, "    Mask: "+ban.getMask() );
                    this.service.sendMsg ( user, "Instater: "+ban.getInstater() );
                    this.service.sendMsg ( user, "    Time: "+ban.getTime() );
                    this.service.sendMsg ( user, "  Expire: "+ban.getExpire() );
                    this.service.sendMsg ( user, "  Reason: "+ban.getReason() );        
                    this.service.sendMsg ( user, "*** End of Info ***" );
                    
                } else {
                    this.service.sendMsg ( user, "Error: BanID "+string1+"" );
                }            
        } else if ( subcommand.is(DEL) ) {
                ban = result.getServicesBan();
                if ( ban != null ) {
                    Handler.getOperServ().remServicesBan ( ban );
                    this.service.sendGlobOp ( cmdName+" for: "+ban.getMask()+" has been removed by: "+oper.getName() );
                }            
        } else if ( subcommand.is(ADD) ||
                    subcommand.is(TIME) ) {
                // String1: mask, String2: time, String3: reason
                String mask = result.getString1();
                String time = result.getString2();
                String reason = result.getString3();
                String stamp = dateFormat.format ( new Date ( ) );
                String percent;
                boolean foundOperMatch = false;
                String expire = Handler.expireToDateString ( stamp, time );
                
                System.out.println("debug(2): mask:"+mask);
                System.out.println("debug(2): time:"+time);
                System.out.println("debug(2): reason:"+reason);
                System.out.println("debug(2): stamp:"+stamp);
                System.out.println("debug(2): expire:"+expire);
                
                
                //    public ServicesBan ( int type, String id, String mask, String reason, String instater, String time, String expire )  {
                ban = new ServicesBan ( 
                    command, 
                    new HashString ( ""+System.nanoTime() ),
                    false, 
                    new HashString ( mask ), 
                    reason, 
                    oper.getString(NAME), 
                    null, 
                    expire 
                );
                
                if ( command.is(SQLINE) ) {
                    uList = Handler.findUsersByNick ( mask );                    
                
                } else if ( command.is(SGLINE) ) {
                    uList = Handler.findUsersByGcos ( mask );                    
                
                } else if ( command.is(AKILL) ) {
                    uList = Handler.findUsersByBan ( ban );                    
                
                } else  {
                    uList = Handler.findUsersByMask ( mask );                    
                }
                
                
                System.out.println("matching users: "+uList.size());
                
                for ( User u : uList ) {
                    if ( u.isAtleast ( IRCOP ) ) {
                        this.service.sendMsg ( user, output ( BAN_MATCH_OPER, cmdName, mask, "" ) );
                        return;
                    }
                }
                
                percent = String.format("%.02f", (float) uList.size() / Handler.getUserList().size() * 100 );
                Handler.getOperServ().addServicesBan ( ban );
                Handler.getOperServ().sendServicesBan ( ban );
                this.service.sendGlobOp ( output ( BAN_ADD_GLOB, cmdName.toLowerCase(), mask, ban.getInstater(), ""+uList.size(), percent, time ) );
                
                // -OperServ(stats@dal.net)- *!*@159.65.148.178 has been added to my autokill list for 30 minutes.
                this.service.sendMsg ( user, mask+" has been added to the akill list for "+time+" min." );
                
                // -OperServ(stats@dal.net)- This autokill's id hasAccess 1563280267K-k and the authorization id hasAccess 1563280267K-16159. Please send your reports@dal.net email as soon as possible.
                this.service.sendMsg ( user, "The akill id is "+ban.getID()+". Please use the akill id "+ban.getID()+" and send your reports@avade.net email as soon as possible." );
               
        }  
         
    }


    private ServicesBan getBan ( HashString command, HashString banId )  {
        ServicesBan ban = null;
        for ( ServicesBan ban2 : Handler.getOperServ().getListByCommand ( command ) ) {
            if ( ban2.getID().is(banId) )  {
                ban = ban2;
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
        HashString buf;
        if ( cmd.length < 5 ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "SEARCHLOG <nick|chan> [<FULL>]" )  );
            return;
        }
        
        HashString target = new HashString ( cmd[4] );
        boolean full = false;
        
        if ( cmd.length > 5 ) {
            buf = new HashString ( cmd[5] );
            if ( buf.is(FULL) ) {
                full = true;
            }
        }
        
        ArrayList<OSLogEvent> lsList = OSDatabase.getSearchLogList ( target, full );
        ArrayList<Comment> cList = OSDatabase.getCommentList ( target, full );
        
        this.service.sendMsg ( user, "*** Logs for "+target+( ! full ? " (1year)" : "" )+":" );
        for ( OSLogEvent log : lsList ) {
            this.service.sendMsg ( user, output ( SHOWLOG, log.getStamp(), log.getFlag().getString(), log.getName().getString(), log.getMask(), log.getOper() ) );
        }
        
        this.service.sendMsg ( user, "*** Comments"+( ! full ? " (1year)" : "" )+":" );
        for ( Comment comment : cList ) {
            this.service.sendMsg ( user, output ( SHOWCOMMENT, comment.getStamp(), comment.getInstater(), comment.getComment() ) );
        }
        this.service.sendMsg ( user, "*** End of log/comments ***" );
    }
    
    private void doSnoopLog ( User user, String[] cmd ) {
        // :DreamHea1er PRIVMSG OperServ@services.sshd.biz :SNOOPLOG <nick|chan> [FULL]
        //            0       1                          2          3           4      5 < 6
        HashString buf;
        if ( cmd.length < 5 ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "SNOOPLOG <nick|chan> [<FULL>]" )  );
            return;
        }
        
        HashString target = new HashString ( cmd[4] );
        boolean full = false;
        
        if ( cmd.length > 5 ) {
            buf = new HashString ( cmd[5] );
            if ( buf.is(FULL) ) {   
                full = true;
            }
        }
        
        ArrayList<OSSnoopLogEvent> lsList = OSDatabase.getSnoopLogList ( target, full );
        
        this.service.sendMsg ( user, "*** Logs for "+target+( ! full ? " (1year)" : "" )+":" );
        for ( OSSnoopLogEvent log : lsList ) {
            this.service.sendMsg ( user, output ( SHOWSNOOPLOG, log.getStamp(), log.getTarget(), log.getBody() ) );
        }

        this.service.sendMsg ( user, "*** End of log ***" );
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
        String instater = user.getOper().getName().getString();
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
        
        OSLogEvent log = new OSLogEvent ( new HashString ( "-" ), GLOBAL, user, user.getOper().getNick() );
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
        
        for ( OSLogEvent log : lsList ) {
            this.service.sendMsg ( user, output ( SHOWAUDIT, log.getStamp(), log.getFlag().getString(), log.getName().getString(), log.getMask(), log.getOper(), log.getData() ) );
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
        
        for ( OSLogEvent log : lsList ) {
            this.service.sendMsg ( user, output ( SHOWBANLOG, log.getStamp(), log.getFlag().getString(), log.getName().getString(), log.getMask(), log.getOper(), log.getData() ) );
        }
        this.service.sendMsg ( user, "*** End of Audit ***" ); 
        
    }
    
    private void doJupe ( User user, String[] cmd ) {
        // :DreamHea1er PRIVMSG OperServ@services.sshd.biz :jupe <SERVERNAME> 
        //            0       1                          2     3 4           < 5
        if ( cmd.length < 5 ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "JUPE <servername.netname.net>" ) );
            return;
        }
        
        HashString serverName = new HashString ( cmd[4] );
        
        if ( ! serverName.contains ( "." ) ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "JUPE <servername.netname.net> (all servers need to contain dots in their names)" ) );
            return;
        } else if ( serverName.is ( Proc.getConf().get(CONNNAME) ) ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "JUPE <servername.netname.net> (services hub cannot be juped)" ) );
            return;
        }
        String name = cmd[4];
        Handler.getOperServ().sendServ("SERVER "+name+" 1 :Jupitered by: "+user.getOper().getName() );
        this.service.sendMsg (user, "Server "+name+" has been Jupitered." );
    }
    

    private void doStaff ( User user, String[] cmd ) {
                
        CMDResult result = this.validateCommandData ( user, STAFF, cmd );
        
        if ( result.is(SYNTAX_ERROR) ) {
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "STAFF <LIST|SRA|CSOP|SA|IRCOP> [<ADD|DEL>] [<nick>]" ) );
                return;            
        
        } else if ( result.is(SUB_SYNTAX_ERROR) ) {
                this.service.sendMsg ( user, output ( SUB_SYNTAX_ERROR, "LIST, SRA, CSOP, SA and IRCOP" ) );
                return;            
        
        } else if ( result.is(SUB2_SYNTAX_ERROR) ) {
                this.service.sendMsg ( user, output ( SUB2_SYNTAX_ERROR, "ADD and DEL" ) );
                return;            
        
        } else if ( result.is(NICK_NOT_REGISTERED) ) {
                this.service.sendMsg (user, output (NICK_NOT_REGISTERED, result.getString1 ( ) ) );
                return;            
        
        } else if ( result.is(ACCESS_DENIED) ) {
                this.service.sendMsg ( user, output ( ACCESS_DENIED, "" ) );
                return;            
        
        } else if ( result.is(NOT_ENOUGH_ACCESS) ) {
                this.service.sendMsg (user, output (NOT_ENOUGH_ACCESS, result.getString1 ( ) ) );
                return;            
        
        } else if ( result.is(STATS) ) {
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
        
        NickInfo ni = result.getNick();
        int access = Oper.hashToAccess (result.getSub ( ) );
        String accessStr = Oper.hashToStr (result.getSub ( ) );
        HashString command = result.getSub2();
        Oper oper;
        OSLogEvent log;
        
        if ( command.is(DEL) ) {
                oper = ni.getOper ( );
                this.service.sendMsg ( user, output ( STAFF_DEL, accessStr, ni.getNameStr() ) );
                this.service.sendGlobOp ( output ( GLOB_STAFF_DEL, accessStr, user.getOper().getNameStr(), ni.getNameStr() ) );
                this.msgUsersByNick ( ni, output ( NICK_NO_LONGER_STAFF, accessStr, ni.getNameStr() ) ); /* Msg all users identified to nick */
                log = new OSLogEvent ( ni.getName(), new HashString ( "del"+accessStr ), user, user.getOper().getNick() );
                OperServ.addLogEvent ( log );
                OperServ.delOper ( ni );
                ni.setOper ( new Oper ( ) );            
        
        } else if ( command.is(ADD) ) {
                oper = new Oper ( ni.getNameStr(), access, user.getOper().getNameStr() );
                this.service.sendMsg ( user, output ( STAFF_ADD, accessStr, ni.getNameStr() ) );
                this.service.sendGlobOp ( output ( GLOB_STAFF_ADD, accessStr, user.getOper().getNameStr(), ni.getNameStr() ) );
                this.msgUsersByNick ( ni, output ( NICK_NOW_STAFF, accessStr, ni.getNameStr() ) ); /* Msg all users identified to nick */
                log = new OSLogEvent ( ni.getName(), new HashString ( "add"+accessStr ), user, user.getOper().getNick() );
                OperServ.addLogEvent ( log );
                OperServ.addOper ( oper );
                ni.setOper ( oper );            
        }
        
        for ( User u : Handler.findUsersByNick ( ni ) ) {
            Handler.forceOperModes ( u );
        }

    }
  
    private void doSpamFilter(User user, String[] cmd) {
               
        CMDResult result = this.validateCommandData ( user, SPAMFILTER, cmd );
        
        if ( result.is(SYNTAX_ERROR) ) {
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "SPAMFILTER <add|del|list> [<string>] [<flags>] [<time>] [<reason>]" ) );
                return;            
        
        } else if ( result.is(SYNTAX_ERROR_DEL) ) {
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "SPAMFILTER DEL <string>" ) );
                return;            
        
        } else if ( result.is(SYNTAX_ERROR_ADD) ) {
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "SPAMFILTER ADD <string> <flags> <time> <reason>" ) );
                return;            
        
        } else if ( result.is(BADFLAGS) ) {
                this.service.sendMsg (user, output (BADFLAGS, result.getString1 ( ) ) );
                return;            
        
        } else if ( result.is(BADTIME) ) {
                this.service.sendMsg (user, output (BADTIME, result.getString1 ( ) ) );
                return;            
        
        } else if ( result.is(BADREASON) ) {
                this.service.sendMsg ( user, output ( BADREASON, "" ) );
                return;            
        
        } else if ( result.is(FILTER_EXISTS) ) {
                this.service.sendMsg (user, output (FILTER_EXISTS, result.getString1 ( ) ) );
                return;            
        }  
        
        String pattern;
        SpamFilter sFilter;
        
        if ( result.is(SHOWLIST) ) {
                this.service.sendMsg ( user, "*** SpamFilter LIST ***" );
                for ( SpamFilter sf : OperServ.getSpamFilters ( ) ) {
                    this.service.sendMsg ( user, output ( SPAMFILTER_LIST, sf.getPattern().getString(), sf.getFlags(), sf.getInstater(), sf.getExpire(), sf.getReason() ) );
                }
                this.service.sendMsg ( user, "*** End of List ***" );            
        
        } else if ( result.is(DEL) ) {
                pattern = result.getString1();
                if ( ( sFilter = OperServ.findSpamFilter ( pattern ) ) != null ) {
                    OperServ.remSpamFilter ( sFilter );
                    this.service.sendServ ( "SF "+pattern+" 0" );
                    this.service.sendGlobOp ( user.getOper().getNick().getName()+" removed SpamFilter: "+pattern  );
                }            
        
        } else if ( result.is(ADD) ) {
                pattern = result.getString1();
                String flags = result.getString2();
                String time = result.getString3();
                String reason = result.getString4();
                String stamp = dateFormat.format ( new Date ( ) );
                String expire = Handler.expireToDateString ( stamp, time );
                sFilter = new SpamFilter ( System.nanoTime(), pattern, flags, user.getOper().getNick().getNameStr(), reason, stamp, expire );
                
                
                //  sendto_one(acptr, "SF %s %ld :%s", sf->text, sf->flags, sf->reason);
                OperServ.addSpamFilter ( sFilter );
                this.service.sendServ ( "SF "+pattern+" "+sFilter.getBitFlags()+" :"+reason );
                this.service.sendGlobOp ( user.getOper().getNick().getName()+" added SpamFilter: "+pattern+" Flags: "+flags+" Expire: "+time+" Reason: "+reason  );            
        }
        
    }

    private void forcenick ( User user, String[] cmd ) {
               
        CMDResult result = this.validateCommandData ( user, FORCENICK, cmd );
        
        if ( result.is(SYNTAX_ERROR) ) {
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "FORCENICK <nick> [<new-nick>]" ) );
                return;            
        
        } else if ( result.is(NICK_NOT_FOUND) ) {
                this.service.sendMsg (user, output (NICK_NOT_FOUND, result.getString1 ( ) ) );
                return;            
        
        } else if ( result.is(NICK_ALREADY_PRESENT) ) {
                this.service.sendMsg (user, output (NICK_ALREADY_PRESENT, result.getString1 ( ) ) );
                return;            
        
        } else if ( result.is(NICK_IS_OPER) ) {
                this.service.sendMsg (user, output (NICK_IS_OPER, result.getString1 ( ) ) );
                return;            
        }
         
        String pattern;
        NickInfo oper = result.getNick();
        User u = Handler.findUser (result.getString1() );
        String newNick = result.getString2();
        Random rand = new Random ( );
        int index = 0;
        
        if ( newNick == null ) {
            boolean search = true;
            while ( search )  {
                index = rand.nextInt ( GuestServ.max ) +10000;
                if ( Handler.findUser ( "Guest"+index )  == null )  {
                    search = false;
                }
            }
            newNick = "Guest"+index;
            Handler.getOperServ().sendGlobOp ( oper.getName()+" has issued FORCENICK on: "+u.getName() );
        
        } else {
            Handler.getOperServ().sendGlobOp ( oper.getName()+" has issued FORCENICK on: "+u.getName()+" -> "+newNick );
        }
        Handler.getOperServ().sendServ ( "SQLINE "+u.getName()+" :You cannot use this nick." );
        Handler.getOperServ().sendServ ( "SVSNICK "+u.getName()+" "+newNick+" 0" );
        Timer timer = new Timer ( );
        timer.schedule( new UnSQlineTask ( u.getNameStr() ), 15000);
        
        OperServ.addTimer ( timer );
        
        
        u.setName(newNick);
    }
      
          
    private void doServer(User user, String[] cmd) {
        // :DreamHea1er PRIVMSG OperServ@services.sshd.biz :SERVER <DEL> <SERVERNAME> 
        // :DreamHea1er PRIVMSG OperServ@services.sshd.biz :SERVER <SET> <SERVERNAME> <PRIMARY> <SERVERNAME> 
        //            0       1                          2       3     4            5         6            7    < 9 
        CMDResult result = this.validateCommandData ( user, SERVER, cmd );
        
        if ( result.is(SYNTAX_ERROR) ) {
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "SERVER <LIST|MISSING|SET|DEL|> [<servername>] [<PRIMARY|SECONDARY>] [<servername>]" ) );
                return;             
        
        } else if ( result.is(SYNTAX_ERROR_DEL) ) {
                this.service.sendMsg ( user, output ( SYNTAX_ERROR_DEL, "SERVER DEL <servername>" ) );
                return;            
        
        } else if ( result.is(SYNTAX_ERROR_SET) ) {
                this.service.sendMsg ( user, output ( SYNTAX_ERROR_SET, "SERVER SET <servername> <PRIMARY|SECONDARY> <servername>" ) );
                return;            
        
        } else if ( result.is(NO_SUCH_SERVER) ) {
                this.service.sendMsg (user, output (NO_SUCH_SERVER, result.getString1() ) );
                return;            
        }
          
        ArrayList<NetServer> sList;
        
        if ( result.is(LIST) ) {
                this.service.sendMsg ( user, "*** Server List:");
                this.service.sendMsg ( user, "  Hub(s):");
                for ( NetServer server : OperServ.getServers ( HUB, false ) ) {
                    this.service.sendMsg ( user, "    "+server.getName ( )+" ---> "+server.getPrimary()+", "+server.getSecondary() );
                }
                this.service.sendMsg ( user, "  Leaf(s):");
                for ( NetServer server : OperServ.getServers ( LEAF, false ) ) {
                    this.service.sendMsg ( user, "    "+server.getName ( )+" ---> "+server.getPrimary()+", "+server.getSecondary() );
                }
    //               this.service.sendMsg ( user, "(P = Primary hub, S = Secondary hub)");
                this.service.sendMsg ( user, "*** End of List ***");            
        
        } else if ( result.is(DEL) ) {
                if ( cmd.length == 6 && OperServ.addDelServer (result.getString1() ) ) {
                    this.service.sendMsg ( user, "Server "+cmd[5]+" was successfully removed from list.");
                } else {
                    this.service.sendMsg ( user, "Error: Server "+cmd[5]+" was not removed from list.");
                }            
        
        } else if ( result.is(MISSING) ) {
                this.service.sendMsg ( user, "*** Missing Servers:");
                this.service.sendMsg ( user, "  Hub(s):");
                for ( NetServer server : OperServ.getServers ( HUB, true ) ) {
                    this.service.sendMsg ( user, "    "+server.getName ( )+" ---> "+server.getPrimary()+", "+server.getSecondary() );
                }
                this.service.sendMsg ( user, "  Leaf(s):");
                for ( NetServer server : OperServ.getServers ( LEAF, true ) ) {
                    this.service.sendMsg ( user, "    "+server.getName ( )+" ---> "+server.getPrimary()+", "+server.getSecondary() );
                }
    //                this.service.sendMsg ( user, "(P = Primary hub, S = Secondary hub)");
                this.service.sendMsg ( user, "*** End of List ***");            
        
        } else if ( result.is(SET) ) {
                NetServer server = result.getServer();
                String name = result.getString1();
                HashString sub2 = result.getSub2();
                if ( sub2.is(PRIMARY) ) {
                    server.setPrimary ( name );
                    OperServ.addUpdServer ( server );
                    this.service.sendMsg ( user, "Primary server info for server: "+server.getName()+" has now been set to: "+server.getPrimary() );

                } else if ( sub2.is(SECONDARY) ) {
                    server.setSecondary ( name );
                    OperServ.addUpdServer ( server );
                    this.service.sendMsg ( user, "Secondary server info for server: "+server.getName()+" has now been set to: "+server.getSecondary() );
                }             
        
        } else {
            this.service.sendMsg ( user, "Error: unknown command");            
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
    -OperServ- ch = channel message/notice
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
        HashString ch;
        for ( int index = 0; index < flags.length(); index++ ) {
            ch = new HashString ( String.valueOf(flags.charAt(index)) );
            if ( ch.is(s) ||
                 ch.is(S) ||
                 ch.is(r) ||
                 ch.is(m) ||
                 ch.is(p) ||
                 ch.is(n) ||
                 ch.is(k) ||
                 ch.is(q) ||
                 ch.is(t) ||
                 ch.is(a) ||
                 ch.is(c) ||
                 ch.is(P) ||
                 ch.is(W) ||
                 ch.is(L) ||
                 ch.is(R) ||
                 ch.is(B) ||
                 ch.is(K) ||
                 ch.is(A) ||
                 ch.is(NUM_1) ||
                 ch.is(NUM_2) ||
                 ch.is(NUM_3) ||
                 ch.is(NUM_4) ) {
                return true;
           
            } 
        }
        return false;
    }

    private void makill(User user, String[] cmd) {
        CMDResult result = this.validateCommandData ( user, MAKILL, cmd );
        
        if ( result.is(STATS) ) {
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "MAKILL <add> [<nick!user@host> <nick!user@host> ...]" ) );
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "MAKILL <commit> <length> <reason>" ) );
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "MAKILL <reset>" ) );            
        
        } else if ( result.is(STATS) ) {
                this.service.sendMsg ( user, output ( BADTIME, "" ) );            
        
        } else if ( result.is(STATS) ) {
                this.service.sendMsg ( user, output ( BADREASON, "" ) );            
        
        } else if ( result.is(STATS) ) {
                this.service.sendMsg ( user, output ( ACCESS_DENIED, "" ) );            
        }
         
        ServicesBan ban = null;

        if ( result.getSub().is(COMMIT) ) {
                String time = result.getString1 ( );
                String reason = result.getString2 ( );
                String expire = result.getString3 ( );
                NickInfo oper = user.getOper().getNick();
                String stamp = dateFormat.format ( new Date ( ) );
                
                ArrayList<User> uList = new ArrayList<>();
                ArrayList<User> affectedUsers;
                int numBans = 0;
                boolean shouldAdd;
                
                for ( String str : result.getMAkill() ) {
                    shouldAdd = true;
                    //    public ServicesBan ( int type, String id, String mask, String reason, String instater, String time, String expire )  {
                    ban = new ServicesBan ( 
                        AKILL, 
                        new HashString ( ""+System.nanoTime() ), 
                        false, 
                        new HashString ( str ), 
                        reason, 
                        oper.getNameStr(), 
                        null, 
                        expire
                    );
                    affectedUsers = Handler.findUsersByBan ( ban );
                    for ( User u : affectedUsers ) {
                        if ( u.isAtleast ( IRCOP ) ) {
                            this.service.sendMsg ( user, output ( BAN_MATCH_OPER, "Akill", str, "" ) );
                            shouldAdd = false;
                        } 
                    }
                    
                    if ( shouldAdd ) {
                        numBans++;
                        uList.addAll ( affectedUsers );
                        Handler.getOperServ().addServicesBan ( ban );
                        Handler.getOperServ().sendServicesBan ( ban );
                    }
                }
                
                String percent = String.format("%.02f", (float) uList.size() / Handler.getUserList().size() * 100 );

                if ( ban != null ) {
                    this.service.sendGlobOp ( output ( MAKILL_ADD_GLOB, ban.getInstater(), ""+numBans, time, ""+uList.size(), percent ) );
                } else {
                    this.service.sendGlobOp ( output ( MAKILL_NOBAN_GLOB, "", "" ) );
                }            
        }
         
    }


    
    private CMDResult validateCommandData ( User user, HashString command, String[] cmd ) {
        CMDResult result = new CMDResult ( );
        User u;
        User u2;
        NickInfo oper;
        NickInfo ni;
        HashString sub;
        HashString sub2 = null;
        HashString sub3;
        HashString sub4;
        int flag;
        String time;
        String text;
        String targets;
        String stamp;
        String reason;
        String expire;
        ServicesBan ban = null;
        NetServer server1 = null;
        NetServer server2;
        String[] buf = new String[6];
        ArrayList<ServicesBan> bans = new ArrayList<>();
        
        if ( command.is(MAKILL) ) {
                // :DreamHea1er PRIVMSG OperServ@services.sshd.biz :MAKILL ADD nick!user@host ....
                // :DreamHea1er PRIVMSG OperServ@services.sshd.biz :MAKILL COMMIT 30 reason here
                //            0       1                          2      3       4  5      6    7   = 6

                sub = new HashString ( cmd.length > 4 ? cmd[4] : "0" );

                if ( isShorterThanLen ( 6, cmd ) ) {
                    result.setStatus ( SYNTAX_ERROR );         
                } 

                result.setSub ( sub );

                if ( sub.is(ADD) ) {
                        for ( int i = 5; i < cmd.length; i++ ) {
                            if ( cmd[i].contains("!") && cmd[i].contains("@") ) {
                                if ( OperServ.findBan ( AKILL, cmd[i] ) != null ) {
                                    /* Ban exist */
                                    this.service.sendMsg ( user, output ( BAN_EXIST, "AKill", cmd[i] ) );

                                } else if ( user.getOper().makillDuplicate ( cmd[i] ) ) {
                                    /* Ban exist in makill */
                                    this.service.sendMsg ( user, output ( MAKILL_DUPLICATE, "MAKill", cmd[i] ) );

                                } else if ( OperServ.isWhiteListed ( cmd[i] ) ) {
                                    /* hasAccess whitelisted */
                                    this.service.sendMsg ( user, output ( WHITELISTED, cmd[i] ) );

                                } else {
                                    /* Add as valid ban */
                                    user.getOper().addMAkill ( cmd[i] );
                                }
                            }
                        }
                        this.service.sendMsg ( user, output ( ADDED_MAKILL, ""+user.getOper().getMAkill().size() ) );

                } else if ( sub.is(RESET) ) {
                        user.getOper().clearMakill();
                        this.service.sendMsg ( user, output ( RESET_MAKILL ) );

                } else if ( sub.is(COMMIT) ) {
                        time = cmd.length > 5 ? cmd[5] : "30m";
                        reason = cmd.length > 6 ? Handler.cutArrayIntoString ( cmd, 6 ) : "Banned";
                        stamp = dateFormat.format ( new Date ( ) );

                        if ( isShorterThanLen ( 7, cmd ) ) {
                            result.setStatus ( SYNTAX_ERROR );
                        } else if ( ( expire = Handler.expireWithCharToDateString ( stamp, time ) ) == null ) {
                            result.setStatus ( BADTIME );

                        } else if ( reason == null ) {
                            result.setStatus ( BADREASON );

                        } else {
                            for ( String str : user.getOper().getMAkill() ) {
                                if ( OperServ.findBan ( AKILL, str ) != null ) {
                                    /* Ban exist */
                                    this.service.sendMsg ( user, output ( BAN_EXIST, "AKill", str ) );

                                } else if ( OperServ.isWhiteListed ( str ) ) {
                                    /* hasAccess whitelisted */
                                    this.service.sendMsg ( user, output ( WHITELISTED, str ) );

                                } else {
                                    /* Add as valid ban */
                                    result.addMAKill ( str );
                                    result.setString1 ( time );
                                    result.setString2 ( reason );
                                    result.setString3 ( expire );
                                }
                            }
                            user.getOper().clearMakill();
                        }
                } 
        
        } else if ( command.is(AKILL) ||
                    command.is(SQLINE) ||
                    command.is(SGLINE) ||
                    command.is(IGNORE) ) {
                // :DreamHea1er PRIVMSG OperServ@services.sshd.biz :AKILL -*!*@1.2.3.4
                // :DreamHea1er PRIVMSG OperServ@services.sshd.biz :AKILL list *!*@1.2.3.4
                // :DreamHea1er PRIVMSG OperServ@services.sshd.biz :AKILL time 30 *!*@1.2.3.4 spamming hasAccess not allowed
                //            0       1                          2      3    4  5           6 7                       = 8+
                sub = (cmd.length > 4 ? new HashString ( cmd[4] ) : new HashString ( "0" ) );
                if ( sub.is(TIME) ) {
                    sub = ADD;
                } 
                time = cmd.length > 5 ? cmd[5] : "30";
                reason = cmd.length > 7 ? Handler.cutArrayIntoString ( cmd, 7 ) : "Banned";
                stamp = dateFormat.format ( new Date ( ) );
                                
                if ( isShorterThanLen ( 5, cmd ) ) {
                    result.setStatus ( SYNTAX_ERROR );
                
                    
                } else if ( sub.is(LIST) && isShorterThanLen ( 6, cmd ) ) {
                    result.setStatus( SYNTAX_ERROR_LIST );
                    
                } else if ( sub.is(LIST) && cmd.length > 5 ) {
                    result.setString1 ( cmd[5] );
                    result.setStatus ( LIST );
                            
                    
                    
                    
                } else if ( sub.is(INFO) && isShorterThanLen ( 6, cmd ) ) {
                    result.setStatus ( SYNTAX_ERROR_INFO );

                } else if ( sub.is(INFO) && ( ban = OperServ.findBanByID ( command, cmd[5] ) ) == null ) {
                    result.setString1 ( cmd[5] );
                    result.setStatus ( BAN_NO_EXIST );
                    
                } else if ( sub.is(INFO) && ban != null ) {
                    result.setServicesBan ( ban );
                    result.setStatus ( INFO );
                    
                    
                    
                    
                } else if ( sub.is(DEL) && isShorterThanLen ( 6, cmd ) ) {
                    result.setStatus( SYNTAX_ERROR_DEL );
                    
                } else if ( sub.is(DEL) && 
                            ( ( ban = OperServ.findBanByID ( command, cmd[5] ) ) == null &&
                              ( ban = OperServ.findBan ( command, cmd[5] ) ) == null ) ) {
                    result.setString1 ( cmd[5] );
                    result.setStatus ( BAN_NO_EXIST );
                
                } else if ( sub.is(DEL) && ( oper = NickServ.findNick ( ban.getInstater() ) ) != null && 
                            ! ( oper.getAccess() < user.getAccess() || user.isIdented(oper) || user.isAtleast(SRA) ) ) {
                    result.setStatus ( ACCESS_DENIED );
                
                } else if ( sub.is(DEL) && ban != null ) {
                    result.setServicesBan ( ban );
                    result.setStatus ( DEL );
                



                } else if ( isShorterThanLen ( 8, cmd ) ) {
                    result.setStatus ( SYNTAX_ERROR_ADD );
                    
                } else if ( ! ( cmd[6].contains("!") && cmd[6].contains("@") ) ) {
                    result.setStatus ( SYNTAX_ERROR_ADD );
                    
                } else if ( ( expire = Handler.expireToDateString ( stamp, time ) ) == null ) {
                    result.setStatus ( BADTIME );
                
                } else if ( reason == null ) {
                    result.setStatus ( BADREASON );
                
                } else if ( OperServ.findBan ( command, cmd[6] ) != null ) {
                    result.setString1 ( cmd[6] );
                    result.setStatus ( BAN_EXIST );
                
                } else if ( OperServ.isWhiteListed ( cmd[6] ) ) {
                    result.setStatus ( WHITELISTED );
                } else {
                    result.setString1 ( cmd[6] );
                    result.setString2 ( cmd[5] );
                    result.setString3 ( reason );
                    result.setStatus ( ADD );
                }     
        
        } else if ( command.is(SPAMFILTER) ) {
                // :DreamHea1er PRIVMSG OperServ@services.sshd.biz :SPAMFILTER add hello?hello?hello flags 1d spamming hasAccess not allowed
                //            0       1                          2           3   4                 5     6  7       8+              = 9+
                sub = (cmd.length > 4 ? new HashString ( cmd[4] ) : new HashString ( "0" ));
                time = cmd.length > 7 ? cmd[7] : "30m";
                flag = cmd.length > 6 ? cmd[6].toUpperCase().hashCode() : 0;
                reason = cmd.length > 8 ? Handler.cutArrayIntoString ( cmd, 8 ) : "SpamFiltered"; 
                stamp = dateFormat.format ( new Date ( ) );
                expire = Handler.expireToDateString ( stamp, time );
                 
                if ( isShorterThanLen ( 5, cmd ) ) {
                    result.setStatus ( SYNTAX_ERROR );
                } else if ( sub.is(LIST) ) {
                    result.setStatus ( SHOWLIST );
                } else if ( sub.is(DEL) && isShorterThanLen ( 6, cmd ) ) {
                    result.setStatus ( SYNTAX_ERROR_DEL );
                } else if ( sub.is(DEL) ) {
                    result.setString1 ( cmd[5] );
                    result.setStatus ( DEL );
                } else if ( isShorterThanLen ( 9, cmd ) ) {
                    result.setStatus ( SYNTAX_ERROR_ADD );
                } else if ( ! this.isFlagsGood ( cmd[6] ) ) {
                    result.setStatus ( BADFLAGS );
                } else if ( expire == null ) {
                    result.setStatus ( BADTIME );
                } else if ( reason == null ) {
                    result.setStatus ( BADREASON );
                } else if ( OperServ.findSpamFilter ( cmd[5] ) != null ) {
                    result.setString1 ( cmd[5] );
                    result.setStatus ( FILTER_EXISTS );
                } else {
                    result.setString1 ( cmd[5] );
                    result.setString2 ( cmd[6] );
                    result.setString3 ( cmd[7] );
                    result.setString4 ( reason );
                    result.setStatus ( ADD );
                }            
        
        } else if ( command.is(STAFF) ) {
                // :DreamHea1er PRIVMSG OperServ@services.sshd.biz :STAFF LIST
                // :DreamHea1er PRIVMSG OperServ@services.sshd.biz :STAFF CSOP ADD Pintuz
                // :DreamHea1er PRIVMSG OperServ@services.sshd.biz :STAFF CSOP DEL Pintuz
                //  0           1       2                           3     4    5    6       = 7
                if ( isShorterThanLen( 5, cmd ) ) {
                    result.setStatus ( SYNTAX_ERROR );
                } else if ( (sub = new HashString(cmd[4])).is(LIST) ) {
                    result.setSub ( sub );
                    result.setStatus ( SHOWLIST );
                } else if ( isShorterThanLen ( 7, cmd) ) {
                    result.setStatus ( SYNTAX_ERROR );
                } else if ( sub != SRA && sub != CSOP && sub != SA && sub != IRCOP ) {
                    result.setSub ( sub );
                    result.setStatus ( SUB_SYNTAX_ERROR );
                } else if ( (sub2 = new HashString(cmd[5])).is(ADD) && ! sub2.is(DEL) ) {
                    result.setStatus ( SUB2_SYNTAX_ERROR );
                } else if ( ( ni = NickServ.findNick ( cmd[6] ) ) == null ) {
                    result.setString1 ( user.getString ( NAME ) );
                    result.setStatus ( NICK_NOT_REGISTERED );
                } else if ( user.getAccess() <= ni.getAccess() ) {
                    result.setStatus ( ACCESS_DENIED );
                } else if ( user.getAccess() <= Oper.hashToAccess ( sub ) ) {
                    String access = Oper.accessToStr ( ( Oper.hashToAccess ( sub ) + 1 ) );
                    result.setString1 ( access );
                    result.setStatus ( NOT_ENOUGH_ACCESS );
                } else {
                    result.setNick ( ni );
                    result.setSub ( sub );
                    result.setSub2 ( sub2 );
                }             
       
        } else if ( command.is(FORCENICK) ) {
                // :DreamHea1er PRIVMSG OperServ@services.sshd.biz :FORCENICK nick
                // :DreamHea1er PRIVMSG OperServ@services.sshd.biz :FORCENICK nick newnick
                //  0           1       2                           3         4    5        = 6
                if ( isShorterThanLen( 5, cmd ) ) {
                    result.setStatus ( SYNTAX_ERROR );
                } else if ( ( ni = user.getOper().getNick() ) == null ) {
                    result.setStatus ( ACCESS_DENIED );
                } else if ( ( u = Handler.findUser ( cmd[4] ) ) == null ) {
                    result.setStatus ( NICK_NOT_FOUND );
                } else if ( u.isAtleast ( IRCOP ) ) {
                    result.setString1 ( u.getNameStr() );
                    result.setStatus ( NICK_IS_OPER );
                } else if ( cmd.length == 6 && ( u2 = Handler.findUser ( cmd[5] ) ) != null ) {
                    result.setString1 ( u2.getNameStr() );
                    result.setStatus ( NICK_ALREADY_PRESENT );               
                } else {
                    result.setNick ( ni );
                    result.setString1 ( u.getNameStr() );
                    if ( cmd.length == 6 ) {
                        result.setString2 ( cmd[5] );
                    }
                }            
        
        } else if ( command.is(SERVER) ) {
                // :DreamHea1er PRIVMSG OperServ@services.sshd.biz :SERVER list                             = 5
                // :DreamHea1er PRIVMSG OperServ@services.sshd.biz :SERVER  del  server                     = 6
                // :DreamHea1er PRIVMSG OperServ@services.sshd.biz :SERVER  set  server  primary  server
                //  0           1       2                           3         4       5        6       7    = 8
                if ( isShorterThanLen( 5, cmd ) ) {
                    result.setStatus ( SYNTAX_ERROR );
                } else if ( (sub = new HashString(cmd[4])) == null || 
                            ( ! sub.is(LIST) && ! sub.is(DEL) && ! sub.is(SET) && ! sub.is(MISSING) ) ) {
                    result.setStatus ( SYNTAX_ERROR );                
                } else if ( sub.is(DEL) && isShorterThanLen( 6, cmd ) ) {
                    result.setStatus ( SYNTAX_ERROR_DEL );                    
                } else if ( sub.is(DEL) && ( server1 = OperServ.getServer ( cmd[5] ) ) == null ) {
                    result.setString1 ( cmd[5] );
                    result.setStatus ( NO_SUCH_SERVER );                    
                } else if ( sub.is(DEL) ) {
                    result.setServer ( server1 );
                    result.setStatus ( DEL );
                } else if ( sub.is(SET) && isShorterThanLen( 8, cmd ) ) {
                    result.setStatus ( SYNTAX_ERROR_SET );                    
                } else if ( sub.is(SET) && ( server1 = OperServ.getServer ( cmd[5] ) ) == null ) {
                    result.setString1 ( cmd[5] );
                    result.setStatus ( NO_SUCH_SERVER );                    
                } else if ( sub.is(SET) && 
                            ( (sub2 = new HashString(cmd[6])) == null || 
                               ! sub2.is(PRIMARY) && ! sub2.is(SECONDARY) ) ) {
                    result.setStatus ( SYNTAX_ERROR_SET );                    
                } else {
                    result.setStatus ( sub );
                    
                    if ( sub.is(DEL) ) {
                        result.setServer ( server1 );
                    
                    } else if ( sub.is(SET) ) {
                        result.setServer ( server1 );
                        result.setSub2 ( sub2 );
                        result.setString1 ( cmd[7] );
                    }
                     
                }            
        }
        return result;
    }
 
    
    
    
    /* Send ban2 message to all users identified to specific nickname */
    private void msgUsersByNick ( NickInfo ni, String msg )  {
        for ( User user : Handler.findUsersByNick ( ni )  )  {
            this.service.sendMsg ( user, msg );
        }
    }
      
    public String output ( HashString code, String... args )  {
        if ( code.is(SYNTAX_ERROR) ) {
            return "Syntax: /OperServ "+args[0];            
        
        } else if ( code.is(SYNTAX_ERROR_DEL) ) {
            return "Syntax: /OperServ "+args[0];            
        
        } else if ( code.is(SYNTAX_ERROR_SET) ) {
            return "Syntax: /OperServ "+args[0];            
        
        } else if ( code.is(NO_SUCH_SERVER) ) {
            return "Error: server not found: "+args[0];            
        
        } else if ( code.is(SUB_SYNTAX_ERROR) ) {
            return "Sub-commands available: "+args[0];            
        
        } else if ( code.is(SUB2_SYNTAX_ERROR) ) {
            return "Sub2-commands available: "+args[0];            
        
        } else if ( code.is(ACCESS_DENIED) ) {
            return "Access denied.";            
        
        } else if ( code.is(NOT_ENOUGH_ACCESS) ) {
            return "Access denied. You need to be atleast "+args[0]+" to perform that action.";            
        
        } else if ( code.is(BAN_ADD) ) {
            return "Permanent "+args[0]+" for "+args[1]+" was successfully placed. Affecting "+args[2]+" users ["+args[3]+"%]";            
        
        } else if ( code.is(BAN_ADD_GLOB) ) {
            return args[0]+" for "+args[1]+" by "+args[2]+" affecting "+args[3]+" users ["+args[4]+"%] for "+args[5]+" min." ;            
        
        } else if ( code.is(BAN_EXIST) ) {
            return args[0]+" already exists for "+args[1]+".";            
        
        } else if ( code.is(BAN_NO_EXIST) ) {
            return args[0]+" for "+args[1]+" not found.";            
        
        } else if ( code.is(BAN_TIME_GLOB) ) {
            return args[0]+" for "+args[1]+" was successfully placed by "+args[2]+". Affecting "+args[3]+" users ["+args[4]+"%]";            
        
        } else if ( code.is(BAN_DEL) ) {
            return args[0]+" for "+args[1]+" was successfully removed.";            
        
        } else if ( code.is(BAN_DEL_GLOB) ) {
            return args[0]+" for "+args[1]+" was successfully removed by "+args[2]+".";             
        
        } else if ( code.is(BAN_LIST) ) {
            return "  "+args[0]+"  ID:"+args[1]+"  Instater:"+args[2]+"  Reason: "+args[3];             
        
        } else if ( code.is(BAN_LIST_START) ) {
            return args[0]+" List "+ (  ( ! args[1].isEmpty ( ) ) ?"matching: "+args[1]:"" );             
        
        } else if ( code.is(BAN_LIST_STOP) ) {
            return "*** End of List ***";             
        
        } else if ( code.is(AKILL_LIST_NONE) ) {
            return "Error: No matching akill(s) found.";             
        
        } else if ( code.is(BAN_TIME_ERROR) ) {
            return "Error: "+args[0]+" time "+args[1]+" is not a valid command use num followed by m, h or d.  ( ex. 10d ) ";             
        
        } else if ( code.is(BAN_MATCH_OPER) ) {
            return "Error: "+args[0]+" for "+args[1]+" matches oper "+args[2]+"";             
        
        } else if ( code.is(NICK_NOT_REGGED) ) {
            return "Nick "+args[0]+" is not registered";             
        
        } else if ( code.is(STAFF_ADD) ) {
            return "Nick "+args[1]+" was added to the "+args[0]+" list";             
        
        } else if ( code.is(STAFF_NOT_ADD) ) {
            return "Nick "+args[1]+" was NOT added to the "+args[0]+" list";             
        
        } else if ( code.is(STAFF_DEL) ) {
            return "Nick "+args[1]+" was deleted from the "+args[0]+" list";             
        
        } else if ( code.is(STAFF_NOT_DEL) ) {
            return "Nick "+args[1]+" was NOT deleted from the "+args[0]+" list";             
        
        } else if ( code.is(NICK_NOW_STAFF) ) {
            return "Nick "+args[1]+" to which you have identified has been added to the "+args[0]+" list";             
        
        } else if ( code.is(NICK_NO_LONGER_STAFF) ) {
            return "Nick "+args[1]+" to which you have identified has been removed from the "+args[0]+" list";             
        
        } else if ( code.is(GLOB_STAFF_ADD) ) {
            return args[1]+" has added "+args[2]+" to the "+args[0]+" list";             
        
        } else if ( code.is(GLOB_STAFF_DEL) ) {
            return args[1]+" has removed "+args[2]+" from "+args[0]+" list";             
        
        } else if ( code.is(SHOWLOG) ) {
            return "["+args[0]+"] "+args[1]+" "+(args[1].length()==2?"":" ")+args[1]+" "+(args[1].length()==2?"":" ")+args[3]+" "+( args[4] != null && args[4].length() > 0 ? "["+args[4]+"]" : "" );             
        
        } else if ( code.is(SHOWSNOOPLOG) ) {
            return "["+args[0]+"] "+args[1]+" : "+args[2];             
        
        } else if ( code.is(SHOWAUDIT) ) {
            return "["+args[0]+"] "+args[1]+" "+args[1]+" "+args[2]+" "+args[3]+" "+( args[4] != null ? "["+args[4]+"]" : "" )+(args[5] != null ? ": "+args[5] : "");             
        
        } else if ( code.is(SHOWBANLOG) ) {
            return "["+args[0]+"] "+args[1]+" "+args[1]+" [Ticket:"+args[2]+"] "+args[3]+" ["+args[4]+"] "+(args[5] != null ? ": "+args[5] : "");             
        
        } else if ( code.is(SHOWCOMMENT) ) {
            return "["+args[0]+"] "+args[1]+": "+args[2]+"";             
        
        } else if ( code.is(ADD_COMMENT) ) {
            return "Comment has "+args[0];             
        
        } else if ( code.is(BADFLAGS) ) {
            return "Error: Bad flags, consult the help text for syntax.";             
        
        } else if ( code.is(BADTIME) ) {
            return "Error: Bad expire time string, consult the help text for valid syntax.";             
        
        } else if ( code.is(BADREASON) ) {
            return "Error: Bad reason string, consult the help text for syntax.";             
        
        } else if ( code.is(ADDED_MAKILL) ) {
            return "MAKILL now contains "+args[0]+" masks.";             
        
        } else if ( code.is(RESET_MAKILL) ) {
            return "MAKILL has been reset";             
        
        } else if ( code.is(MAKILL_ADD_GLOB) ) {
            return args[0]+" akilled "+args[1]+" hosts for "+args[2]+" min affecting "+args[3]+" users ["+args[4]+"%].";             
        
        } else if ( code.is(MAKILL_NOBAN_GLOB) ) {
            return "MAKILL has no bans present";             
        
        } else if ( code.is(MAKILL_DUPLICATE) ) {
            return "MAKILL for "+args[1]+" is a duplicate.";             
        
        } else if ( code.is(FILTER_EXISTS) ) {
            return "Error: There is already a spamfilter matching: "+args[0]+".";             
        
        } else if ( code.is(SPAMFILTER_LIST) ) {
            return "  "+args[0]+": "+args[1]+" "+args[2]+" ["+args[3]+"]: "+args[4];
        
        } else if ( code.is(NICK_NOT_FOUND) ) {
            return "Error: Nick "+args[0]+" was not found.";             
        
        } else if ( code.is(NICK_ALREADY_PRESENT) ) {
            return "Error: Nick "+args[0]+" already exists on the network.";
        
        } else if ( code.is(NICK_IS_OPER) ) {
            return "Error: Nick "+args[0]+" is an IRCop";             
        
        } else if ( code.is(WHITELISTED) ) {
            return "Error: pattern "+args[0]+" is matching a whitelisted mask and cannot be banned.";             
        
        } else {
            return "";
        }
         
    }

 

}
