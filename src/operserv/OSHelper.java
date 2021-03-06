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
package operserv;

import core.CommandInfo;
import core.Handler;
import core.HashString;
import core.Helper;
import core.TextFormat;
import user.User;

/**
 *
 * @author DreamHealer
 */
class OSHelper extends Helper {
    private OSSnoop                     snoop;
    private TextFormat                  f;
     
    public OSHelper ( OperServ service, OSSnoop snoop )  {
        super ( service );
        this.snoop          = snoop;
        this.f              = new TextFormat ( );
     }

    public void parse ( User user, String[] cmd )  { 
        this.found = true; 
        
        if ( cmd.length < 5)  { 
            this.help ( user ); 
            return;
        }
        
        HashString command = new HashString ( cmd[4] );
        
        if ( command.is(HELP) ) {
            this.help ( user ); 
        
        } else if ( command.is(AKILL) ) {
            this.akill ( user );
        
        } else if ( command.is(MAKILL) ) {
            this.makill ( user );
        
        } else if ( command.is(SEARCHLOG) ) {
            this.searchLog ( user );
        
        } else if ( command.is(SNOOPLOG) ) {
            this.snoopLog ( user );
        
        } else if ( command.is(COMMENT) ) {
            this.comment ( user );
        
        } else if ( command.is(AUDIT) ) {
            this.audit ( user );
        
        } else if ( command.is(STAFF) ) {
            this.staff ( user );
        
        } else if ( command.is(GLOBAL) ) {
            this.global ( user );
        
        } else if ( command.is(BANLOG) ) {
            this.banlog ( user );
        
        } else if ( command.is(SQLINE) ) {
            this.sqline ( user );
        
        } else if ( command.is(SGLINE) ) {
            this.sgline ( user );
        
        } else if ( command.is(JUPE) ) {
            this.jupe ( user );
        
        } else if ( command.is(SERVER) ) {
            this.server ( user );
        
        } else if ( command.is(SPAMFILTER) ) {
           this.spamfilter ( user ); 
        
        } else if ( command.is(FORCENICK) ) {
            this.forcenick ( user );
        
        } else if ( command.is(BAHAMUT) ) {
            this.bahamut ( user );
        
        } else {
            this.found = false; this.noMatch ( user, cmd[4] );
        }
         
        this.snoop.msg ( this.found, user, cmd );
    }
    
    /* HELP */
    public void help ( User user )  {
        int access = user.getAccess ( );
        this.showStart ( user, "" );
        this.service.sendMsg ( user, "   OperServ allows you to manage the network, services nicks and channels."               );
        this.service.sendMsg ( user, "   The purpose of Oper Services is to aid you in your work as network staff"              );
        this.service.sendMsg ( user, "   actively as a tool in order to stir and bend the network objects in fair"              );
        this.service.sendMsg ( user, "   judgement. Remember that you as an IRC Operator represent the whole network"           );
        this.service.sendMsg ( user, "   For more help regarding a specific command please type:"                               );
        this.service.sendMsg ( user, "       "+f.b ( ) +"/OperServ HELP <command>"+f.b ( )                                      );

        if ( access > 0 )  {
            this.service.sendMsg ( user, "   "                                                                                      );
            this.service.sendMsg ( user, "   "+f.b ( ) +"IRC Operator Commands"+f.b ( )                                             );
            for ( CommandInfo ci : OperServ.getCMDList ( IRCOP )  )  {
                Handler.getOperServ ( ) .sendMsg ( user, "       "+f.b ( ) +ci.getName ( ) +f.b ( ) +ci.getPatch ( ) +ci.getDescription ( )  );            
            }
        }
            
        if ( access > 1 )  {
            this.service.sendMsg ( user, "   " );
            this.service.sendMsg ( user, "   "+f.b ( ) +"Services Admin Commands"+f.b ( )                                       );
            for ( CommandInfo ci : OperServ.getCMDList ( SA )  )  {
                this.service.sendMsg ( user, "       "+f.b ( ) +ci.getName ( ) +f.b ( ) +ci.getPatch ( ) +ci.getDescription ( )  );
            }
        }

        if ( access > 2 )  {
            this.service.sendMsg ( user, "   " );
            this.service.sendMsg ( user, "   "+f.b ( ) +"Channel Services Operator Commands"+f.b ( )                              );
            for ( CommandInfo ci : OperServ.getCMDList ( CSOP )  )  {
                this.service.sendMsg ( user, "       "+f.b ( ) +ci.getName ( ) +f.b ( ) +ci.getPatch ( ) +ci.getDescription ( )  );
            }

        }

        if ( access > 3 )  {
            this.service.sendMsg ( user, "   " );
            this.service.sendMsg ( user, "   "+f.b ( ) +"Services Root Admin Commands"+f.b ( )                                  );
            for ( CommandInfo ci : OperServ.getCMDList ( SRA )  )  {
                this.service.sendMsg ( user, "       "+f.b ( ) +ci.getName ( ) +f.b ( ) +ci.getPatch ( ) +ci.getDescription ( )  );
            }
        }
        this.showEnd ( user );
    }
    
    public void akill ( User user )  { 
        this.showStart ( user, "AKill" );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /OperServ AKILL <ADD|TIME|DEL|LIST> <minutes> <nick!user@host> <REASON>"+f.b ( ) +""   );
        this.service.sendMsg ( user, "   "+f.b ( ) +"    Ex: /OperServ AKILL ADD 180 *!*@1.2.3.4 Flooding is not permitted"+f.b ( )+""              );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   AKill is a powerful command and allow staff remove unwanted clients from"                                  );
        this.service.sendMsg ( user, "   the network. You are unable to place akills that matches opers or white listed"                            );
        this.service.sendMsg ( user, "   ips. Note that nick is passed using the command but the field is never used."                              );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                                             );
        this.service.sendMsg ( user, "   Place akills based on ip as the ircd's will use less resources on ips"                                     );
        this.showEnd ( user );
    }
        
    public void makill ( User user )  { 
        this.showStart ( user, "MAKill" );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /OperServ MAKILL <ADD> <nick!user@host> <nick!user@host> ..."+f.b ( ) +""              );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /OperServ MAKILL <COMMIT> <LENGTH> <REASON>"+f.b ( ) +""                               );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /OperServ MAKILL <RESET>"+f.b ( ) +""                                                  );
        this.service.sendMsg ( user, "   "+f.b ( ) +"    Ex: /OperServ AKILL ADD *!*@1.2.3.4 *!*@1.2.3.5 *!*@1.2.3.6"+f.b ( )+""                    );
        this.service.sendMsg ( user, "   "+f.b ( ) +"    Ex: /OperServ AKILL COMMIT 30d Flooding is not permitted"+f.b ( )+""                       );
        this.service.sendMsg ( user, "   "+f.b ( ) +"    Ex: /OperServ AKILL RESET"+f.b ( )+""                                                      );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   MAKill is a MASS-AKILL command and is powerful and allow staff to remove unwanted clients"                 );
        this.service.sendMsg ( user, "   from the network. You are unable to place akills that matches opers or white listed"                       );
        this.service.sendMsg ( user, "   ips. Note that nick is passed using the command but the field is never used. Also note that"               );
        this.service.sendMsg ( user, "   this command is mainly for use by scripts and to minimize the amount of akill calls an oper"               );
        this.service.sendMsg ( user, "   has to make to services to akill a huge list of masks."                                                    );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   Note that this command was created to be able to akill by the masses. The add command"                     );
        this.service.sendMsg ( user, "   will add the masks into a list and return statistic information about the current amount"                  );
        this.service.sendMsg ( user, "   of masks in memory. The command will return error if an ip is not added to"                                );
        this.service.sendMsg ( user, "   the list correctly for some reason like if its whitelisted or already akilled."                            );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                                             );
        this.service.sendMsg ( user, "   Place akills based on ips as the ircd's will use less resources on ips."                                   );
        this.showEnd ( user );
    }
    
    public void comment ( User user )  { 
        this.showStart ( user, "Comment" );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /OperServ COMMENT <nick|chan> <comment>"+f.b ( ) +""                                   );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   Adds a comment about a nick or channel. Note that you can only add comments"                               );
        this.service.sendMsg ( user, "   not remove them so make sure you dont add lies."                                                           );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   The comment command will show up in SEARCHLOG and will assist staff to view"                               );
        this.service.sendMsg ( user, "   and read about staff interactions with the nick or chan."                                                  );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   Please note that FREEZE or CLOSE etc does not allow an oper to add a reason"                               );
        this.service.sendMsg ( user, "   so make sure you add a comment if you freeze a nick or close a channel as it"                              );
        this.service.sendMsg ( user, "   will help keeping track of why you did what you did and show up in searchlog."                             );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                                             );
        this.service.sendMsg ( user, "   Avoid adding irrelevant comments to avoid too much information getting attached"                           );
        this.service.sendMsg ( user, "   to a nick or channel."                                                                                     );
        this.showEnd ( user );
    }
        
    public void searchLog ( User user )  { 
        this.showStart ( user, "SearchLog" );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /OperServ SEARCHLOG <nick|#chan> [<FULL>]"+f.b ( ) +""                                 );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   The log search command is a powerful tool for operators to search through"                                 );
        this.service.sendMsg ( user, "   the major events of a nick or channel. The output will show ownership events"                              );
        this.service.sendMsg ( user, "   like register, expire or drop but also ircop interference with the nick or chan."                          );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   By default the command will show 1 year worth of logs and by using the FULL"                               );
        this.service.sendMsg ( user, "   command you can see the entire history of the nick or channel."                                            );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   Flag     Nick               Chan"                                                                          );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "     a      Auth"                                                                                             );
        this.service.sendMsg ( user, "     A+                        Auditorium"                                                                    );
        this.service.sendMsg ( user, "     A-                        UnAuditorium"                                                                  );
        this.service.sendMsg ( user, "     C+                        Closed"                                                                        );
        this.service.sendMsg ( user, "     C-                        Reopened"                                                                      );
        this.service.sendMsg ( user, "     D      Drop/delete        Drop/delete"                                                                   );
        this.service.sendMsg ( user, "     E      Expire             Expire"                                                                        );
        this.service.sendMsg ( user, "     Ea     Expire-auth"                                                                                      );
        this.service.sendMsg ( user, "     Ef                        Expire founder"                                                                );
        this.service.sendMsg ( user, "     Ei                        Expire inactivity"                                                             );
        this.service.sendMsg ( user, "     F+     Freeze             Freeze"                                                                        );
        this.service.sendMsg ( user, "     F-     UnFreeze           UnFreeze"                                                                      );
        this.service.sendMsg ( user, "     H+     Held               Held"                                                                          );
        this.service.sendMsg ( user, "     H-     UnHeld             UnHeld"                                                                        );
        this.service.sendMsg ( user, "     GE     GetEmail"                                                                                         );
        this.service.sendMsg ( user, "     GP     GetPass            GetPass"                                                                       );
        this.service.sendMsg ( user, "     Md                        Mass-deop"                                                                     );
        this.service.sendMsg ( user, "     Mk                        Mass-kick"                                                                     );
        this.service.sendMsg ( user, "     R      Register           Register"                                                                      );
        this.service.sendMsg ( user, "     SJ                        SAJOIN"                                                                        );
        this.service.sendMsg ( user, "     SP     SendPass           SendPass"                                                                      );
        this.service.sendMsg ( user, "     T                         Topic wipe (CSop)"                                                             );
        this.service.sendMsg ( user, "     W      Wipe Access list   Wipe aop/sop/akick lists"                                                      );
        this.service.sendMsg ( user, "     Wa                        Wipe AOP list"                                                                 );
        this.service.sendMsg ( user, "     Ws                        Wipe SOP list"                                                                 );
        this.service.sendMsg ( user, "     Wk                        Wipe AKICK list"                                                               );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   IRCop interference will mark the event with the oper nickname"                                             );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                                             );
        this.service.sendMsg ( user, "   Never share the information that is delivered by this command as it will show ips"                         );
        this.service.sendMsg ( user, "   and other perhaps sensitive information regarding the nick or channel."                                    );
        this.showEnd ( user );
    }    
    
    public void snoopLog ( User user )  { 
        this.showStart ( user, "SnoopLog" );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /OperServ SNOOPLOG <nick|#chan> [<FULL>]"+f.b ( ) +""                                  );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   The log search command is a powerful tool for operators to search through"                                 );
        this.service.sendMsg ( user, "   the more granular events of a nick or channel. The output will show used commands"                         );
        this.service.sendMsg ( user, "   like identify, op/deop and even info done on the nick or chan."                                            );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   By default the command will show 1 year worth of logs and by using the FULL"                               );
        this.service.sendMsg ( user, "   command you can see the entire history of the nick or channel."                                            );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                                             );
        this.service.sendMsg ( user, "   Never share the information that is delivered by this command as it will show ips"                         );
        this.service.sendMsg ( user, "   and other perhaps sensitive information regarding the nick or channel."                                    );
        this.showEnd ( user );
    } 

    public void audit ( User user )  { 
        this.showStart ( user, "Audit" );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /OperServ Audit [<opernick>]"+f.b ( ) +""                                              );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   The Audit command will show oper interference with nicks and channels. It will"                            );
        this.service.sendMsg ( user, "   also show oper list changes, so if an oper is added to example CSOP list the "                             );
        this.service.sendMsg ( user, "   audit log will show the nick added, what access and whom added the oper."                                  );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   There is two ways to run this command. First is without oper nick. This will"                              );
        this.service.sendMsg ( user, "   show all oper logs from 1 year ago and forward from both nick, chan and oper log."                         );
        this.service.sendMsg ( user, "   The second is when used with a oper nick. This will list the opers entire history"                         );
        this.service.sendMsg ( user, "   of nick, chan interference and oper access logs"                                                           );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   For flag definition list for nicks and chans check /os help searchlog"                                     );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   Flag          Oper"                                                                                        );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "     MASTER+     Added as Services Master"                                                                    );
        this.service.sendMsg ( user, "     MASTER-     Removed as Services Master"                                                                  );
        this.service.sendMsg ( user, "     SRA+        Added as Services Root Admin"                                                                );
        this.service.sendMsg ( user, "     SRA-        Removed as Services Root Admin"                                                              );
        this.service.sendMsg ( user, "     CSOP+       Added as Channel Service Operator"                                                           );
        this.service.sendMsg ( user, "     CSOP-       Removed as Channel Service Operator"                                                         );
        this.service.sendMsg ( user, "     SA+         Added as Services Admin"                                                                     );
        this.service.sendMsg ( user, "     SA-         Removed as Services Admin"                                                                   );
        this.service.sendMsg ( user, "     IRCOP+      Added as IRC Operator"                                                                       );
        this.service.sendMsg ( user, "     IRCOP-      Removed as IRC Operator"                                                                     );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                                             );
        this.service.sendMsg ( user, "   Never share the information that is delivered by this command as it will show ips"                         );
        this.service.sendMsg ( user, "   and other perhaps sensitive information regarding the nick or channel."                                    );
        this.showEnd ( user );
    } 

    private void staff(User user) {
        this.showStart ( user, "Audit" );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /OperServ STAFF <LIST|SRA|CSOP|SA|IRCOP> [<ADD|DEL>] [<opernick>]"+f.b ( ) +""         );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   The STAFF command is the only command to add or delete staff in services. You may"                         );
        this.service.sendMsg ( user, "   manage the staff list hierarchically where you can manage all access less than your own"                   );
        this.service.sendMsg ( user, "   so the Master can manage SRA and below, SRA can manage CSop and below, CSop can manage"                    );
        this.service.sendMsg ( user, "   SA and below, and lastly SA can manage IRCop."                                                             );
        this.service.sendMsg ( user, "   "                                                                                                          );    
        this.service.sendMsg ( user, "   Examples:"                                                                                                 );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   List staff:    /OperServ STAFF LIST"                                                                       );
        this.service.sendMsg ( user, "   Add IRCop:     /OperServ STAFF IRCOP ADD DreamHealer"                                                      );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   Move the staff up in ranks:"                                                                               );
        this.service.sendMsg ( user, "                  /OperServ STAFF SA ADD DreamHealer"                                                         );
        this.service.sendMsg ( user, "                  /OperServ STAFF CSOP ADD DreamHealer"                                                       );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   Different accesses unlocks different commands. IRCop's can see nick/chan logs. SA's is"                    );
        this.service.sendMsg ( user, "   allowed to use usermode +a, Akill and see staff audit logs. CSop's can access getpass/getemail"            );
        this.service.sendMsg ( user, "   and freeze/close nicks and chans. SRA's can add people as CSop, rehash services config and jupe"           );
        this.service.sendMsg ( user, "   servers, and send raw services commands. Finally the Master is the only one capable of "                   );
        this.service.sendMsg ( user, "   adding SRA's."                                                                                             );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                                             );
        this.service.sendMsg ( user, "   Only add people you trust. If you hesitate, then dont add them."                                           );
        this.showEnd ( user );
    }
    
    private void global ( User user ) {
        this.showStart ( user, "Global" );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /OperServ GLOBAL <Message here>"+f.b ( ) +""                                           );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   The GLOBAL command is a way to send a message to all online users without"                                 );
        this.service.sendMsg ( user, "   having to expose your nick as the author of that message. The advantage is"                                );
        this.service.sendMsg ( user, "   to stay anonymous and avoid all replies sent from users either in form of"                                 );
        this.service.sendMsg ( user, "   actual replies or away messages. On a network with alot of users this can"                                 );
        this.service.sendMsg ( user, "   mean thousands of replies, or thousands of new query windows your client"                                  );
        this.service.sendMsg ( user, "   has to handle."                                                                                            );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                                             );
        this.service.sendMsg ( user, "   Only use GLOBAL messages if you have something relevant to tell everyone as"                               );
        this.service.sendMsg ( user, "   it is still a notice and each user has to handle it. Its also important to not"                            );
        this.service.sendMsg ( user, "   send any personal or sensitive data."                                                                      );
        this.showEnd ( user );
    }

    private void banlog ( User user ) {
        this.showStart ( user, "BanLog" );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /OperServ BANLOG <nick|ticket|mask>"+f.b ( ) +""                                       );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   The BANLOG is for storing old services bans placed on the network. The idea"                               );
        this.service.sendMsg ( user, "   is to be able to look back on events and see what happened, to do this we"                                 );
        this.service.sendMsg ( user, "   save information regarding services bans. If a services ban are removed"                                   );
        this.service.sendMsg ( user, "   its still listed in the BANLOG."                                                                           );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                                             );
        this.service.sendMsg ( user, "   Dont share information to users who isnt able to access the information"                                   );
        this.service.sendMsg ( user, "   themselfs. This relates to reason and who placed the ban."                                                 );
        this.showEnd ( user );
    }
    
    public void sqline ( User user )  { 
        this.showStart ( user, "SQLine" );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /OperServ SQLINE <ADD|TIME|DEL|LIST> [<20m|12h|7d>] <nick|#chan> <REASON>"+f.b ( ) +"" );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   SQLine is a powerful command blocking users from using nicks matching a specific"                          );
        this.service.sendMsg ( user, "   pattern or creating/joining channels matching a name pattern."                                             );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                                             );
        this.service.sendMsg ( user, "   Dont add unneccessary SQLines. Its a powerful command and will block users from"                           );
        this.service.sendMsg ( user, "   using nick or channels matching SQLine patterns."                                                          );
        this.showEnd ( user );
    }
    
    public void sgline ( User user )  { 
        this.showStart ( user, "SGLine" );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /OperServ SGLINE <ADD|TIME|DEL|LIST> [<20m|12h|7d>] <pattern> <REASON>"+f.b ( ) +""    );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   SGLine is a way to ban realname / GCos patterns. A user matching a SGLine will be "                        );
        this.service.sendMsg ( user, "   automatically booted from the network. Upon a user is found matching a SGLine a new"                       );
        this.service.sendMsg ( user, "   SGLine will be sent to the servers and the user will be removed from the network."                         );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                                             );
        this.service.sendMsg ( user, "   SGLine is a powerful tool. Dont add unneccessary SGLines to the list to avoid users"                       );
        this.service.sendMsg ( user, "   who hasnt done anything wrong booted from the network."                                                    );
        this.showEnd ( user );
    }
    
    private void jupe ( User user ) {
        this.showStart ( user, "Jupe" );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /OperServ JUPE <servername>"+f.b ( ) +""                                               );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   Jupe provides a simple temporary way to link in servers through services. The point"                       );
        this.service.sendMsg ( user, "   with it is not to make the network look larger (thats just lame), but provide a way to"                    );
        this.service.sendMsg ( user, "   stop problematic servers from reconnecting the network and stop the noise from it."                        );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                                             );
        this.service.sendMsg ( user, "   Jupe is a powerful command that can cause netsplits on the network so dont play with"                      );
        this.service.sendMsg ( user, "   it. Services hub is excempted from jupes as juping services hub would split services."                     );
        this.showEnd ( user );
    }

    private void server ( User user ) {
        this.showStart ( user, "Server" );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /OperServ SERVER <LIST¦MISSING¦DEL> [<servername>]"+f.b ( ) +""                        );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   This command was created to make it easier for IRCop's to keep track on which servers"                     );
        this.service.sendMsg ( user, "   is currently missing on the network. The larger the network is the more useful this "                      );
        this.service.sendMsg ( user, "   command will be. If a server has been missing for more than 2 minutes it will then "                       );
        this.service.sendMsg ( user, "   show up on the missing list. The 2 minutes can probably be coded away but as servers"                      );
        this.service.sendMsg ( user, "   (bahamut) is based on TS traffic and a desynced server could cause some odd things"                        );
        this.service.sendMsg ( user, "   I wouldnt recommend reconnect servers too quickly so having a 2 minutes buffer might"                      );
        this.service.sendMsg ( user, "   be fine, so if server show up on the missing list it should be fine to reconnect them."                    );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   Servers will get automagically added to this list. When a server delinks your network"                     );
        this.service.sendMsg ( user, "   however its required that the server be deleted from the list."                                            );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   A tip is to reconnect hubs first as these server could have leafs connected already."                      );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                                             );
        this.service.sendMsg ( user, "   Server command is another information command created to assist IRCop's in routing"                        );
        this.service.sendMsg ( user, "   so keep the information it provides for yourself, there should not be any reason to"                       );
        this.service.sendMsg ( user, "   give this information to users as it could be used against you."                                           );
        this.showEnd ( user );
    }
    private void spamfilter ( User user ) {
        this.showStart ( user, "SpamFilter" );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /OperServ SPAMFILTER <LIST|ADD|DEL> <string> <flags> <reason>"+f.b ( ) +""             );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   Example: /OperServ SPAMFILTER ADD join?my?site?http* 2 Advertising is not allowed"                                                                                                          );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   The spamfilter command is a way to combat spam on the network. The SF command will let"                    );
        this.service.sendMsg ( user, "   a bahamut server warn, block and/or akill users who is spamming specified strings to"                      );
        this.service.sendMsg ( user, "   users or channels on the server/network. If unsure about the flags keep to the bundled ones."              );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   Example Strings:"                                                                                          );
        this.service.sendMsg ( user, "     <bot> hello hello hello!             : hello?hello?hello!"                                               );
        this.service.sendMsg ( user, "     <bot> join my site http://blabla     : join?my?site?http*"                                               );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   Bundled flags:"                                                                                            );
        this.service.sendMsg ( user, "     1 : warn private massads (spnWR)"                                                                        );
        this.service.sendMsg ( user, "     2 : block+akill private massads (spnWRBA)"                                                               );
        this.service.sendMsg ( user, "     3 : warn channel massads (scWR)"                                                                         );
        this.service.sendMsg ( user, "     4 : block+akill channel massads (scWRBA)"                                                                );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   Behavior flags:"                                                                                           );
        this.service.sendMsg ( user, "     s : strip control codes"                                                                                 );
        this.service.sendMsg ( user, "     S : strip non-alphanumeric characters and spaces"                                                        );
        this.service.sendMsg ( user, "     r : regexp"                                                                                              );
        this.service.sendMsg ( user, "     m : match registered nicks"                                                                              );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   Target flags:"                                                                                             );
        this.service.sendMsg ( user, "     p : private message"                                                                                     );
        this.service.sendMsg ( user, "     n : notice"                                                                                              );
        this.service.sendMsg ( user, "     k : kick message"                                                                                        );
        this.service.sendMsg ( user, "     q : quit message"                                                                                        );
        this.service.sendMsg ( user, "     t : topic"                                                                                               );
        this.service.sendMsg ( user, "     a : away message"                                                                                        );
        this.service.sendMsg ( user, "     c : channel message/notice"                                                                              );
        this.service.sendMsg ( user, "     p : part message"                                                                                        );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   Action flags:"                                                                                             );
        this.service.sendMsg ( user, "     W : warn user"                                                                                           );
        this.service.sendMsg ( user, "     L : lag user"                                                                                            );
        this.service.sendMsg ( user, "     R : report to opers (umode +m)"                                                                          );
        this.service.sendMsg ( user, "     B : block message"                                                                                       );
        this.service.sendMsg ( user, "     K : kill the user"                                                                                       );
        this.service.sendMsg ( user, "     A : akill the user"                                                                                      );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                                             );
        this.service.sendMsg ( user, "   This functionality adds automation and its powerful so dont play around with it, only"                     );
        this.service.sendMsg ( user, "   add strings in to actually help with the noise and block or akill users who matches"                       );
        this.service.sendMsg ( user, "   the strings. Always try add long strings to avoid false positive matches."                                 );
        this.showEnd ( user );
    }
    
    private void forcenick ( User user ) {
        this.showStart ( user, "ForceNick" );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /OperServ FORCENICK <nick> [<new-nick>]"+f.b ( ) +""                                   );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   This command will forcefully change a nick to a new nick. This is only intended for"                       );
        this.service.sendMsg ( user, "   actual incidents where users are using their nick name to advertise or insult others"                      );
        this.service.sendMsg ( user, "   and brings a quick way to resolve issues with users nicknames without having to remove"                    );
        this.service.sendMsg ( user, "   them from the network."                                                                                    );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   When used without a new nick the user will get a GuestXXXXX nick."                                         );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                                             );
        this.service.sendMsg ( user, "   The command will change the nick of a user to a new nick. The command will not logout"                     );
        this.service.sendMsg ( user, "   an identified nick so its recommended to only use it on an unregged nick. The command will"                );
        this.service.sendMsg ( user, "   place a short sqline during the process to ensure the nick is not instantly changing back."                );
        this.service.sendMsg ( user, "   Opers are excempted from this command so you cannot change the nick of an oper."                           );
        this.showEnd ( user );
    }

    private void bahamut(User user) {
        this.showStart ( user, "Bahamut" );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /OperServ BAHAMUT"+f.b ( ) +""                                                         );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   This command will output the version of bahamut best suited for this version of services"                  );
        this.service.sendMsg ( user, "   though services will connect fine to older versions of bahamut. To make sure all features"                 );
        this.service.sendMsg ( user, "   provided by services is working correctly please run the version of bahamut stated by using"               );
        this.service.sendMsg ( user, "   this command."                                                                                             );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                                             );
        this.service.sendMsg ( user, "   This command will output the version of bahamut you should be running. Try keeping the servers"            );
        this.service.sendMsg ( user, "   running on your network up to par with that version to make sure all available features is working"        ); 
        this.showEnd ( user );
    }
}
