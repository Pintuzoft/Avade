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
package operserv;

import core.CommandInfo;
import core.Handler;
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
        
        switch ( cmd[4].toUpperCase().hashCode ( ) ) {
            case HELP :
                this.help ( user );
                break;
                
            case AKILL :
                this.akill ( user );
                break;
                
            case SEARCHLOG :
                this.searchLog ( user );
                break;
                       
            case COMMENT :
                this.comment ( user );
                break;
                
            case AUDIT :
                this.audit ( user );
                break;
                   
            case STAFF :
                this.staff ( user );
                break;
                       
            case GLOBAL :
                this.global ( user );
                break;
                    
            case BANLOG :
                this.banlog ( user );
                break;
                
            case SQLINE :
                this.sqline ( user );
                break;
                
            case SGLINE :
                this.sgline ( user );
                break;
                       
            case JUPE :
                this.jupe ( user );
                break;
                         
            case SERVER :
                this.server ( user );
                break;
                
            default :
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
            this.service.sendMsg ( user, "   "+f.b ( ) +"Channel Services Commands"+f.b ( )                                     );
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
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /OperServ AKILL <ADD|TIME|DEL|LIST> [<20m|12h|7d>] <user@mask> <REASON>"+f.b ( ) +""   );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   AKill is a powerful command and allows services admins to remove unwanted"                                 );
        this.service.sendMsg ( user, "   clients from the network. You are unable to lay akills that matches opers"                                 );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                                             );
        this.service.sendMsg ( user, "   Rather place timed akills to let services have a smaller better maintained database"                       );
        this.service.sendMsg ( user, "   of current akilled hosts."                                                                                 );
        this.showEnd ( user );
    }
    
    public void comment ( User user )  { 
        this.showStart ( user, "AKill" );
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
}

 