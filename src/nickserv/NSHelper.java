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

import core.CommandInfo;
import core.Config;
import core.Handler;
import core.HashString;
import core.Helper;
import core.Proc;
import core.TextFormat;
import operserv.Oper;
import user.User;

/**
 *
 * @author DreamHealer
 */
public class NSHelper extends Helper {
    private NSSnoop         snoop;
   // private boolean found;
    private TextFormat      f;
  
    public NSHelper ( NickServ service, NSSnoop snoop )  {
        super ( service );
//        this.service = service;
        this.snoop          = snoop;
        this.f              = new TextFormat ( );
    }

    public void parse ( User user, String[] cmd ) {
        HashString command;
        try {
            if ( cmd[4].isEmpty ( ) ) {
                this.help ( user ); 
                return; 
            }
        } catch ( Exception e ) {
            this.help ( user );
            return;
        }
            
        command = new HashString ( cmd[4] );
        
        if ( command.is(REGISTER) ) {
            this.doRegister ( user );
        
        } else if ( command.is(IDENTIFY) ) {
            this.doIdentify ( user );
        
        } else if ( command.is(SIDENTIFY) ) {
            this.doIdentify ( user );        
        
        } else if ( command.is(GHOST) ) {
            this.doGhost ( user );
        
        } else if ( command.is(SET) ) {
            this.doSet ( user, cmd );
        
        } else if ( command.is(AUTH) ) {
            this.doAuth ( user );
        
        } else if ( command.is(INFO) ) {
            this.doInfo ( user );
        
        } else if ( command.is(FREEZE) ) {
            this.freeze ( user );
        
        } else if ( command.is(HOLD) ) {
            this.hold ( user );
        
        } else if ( command.is(MARK) ) {
            this.mark ( user );
        
        } else if ( command.is(NOGHOST) ) {
            this.noghost ( user );
        
        } else {
            this.noMatch ( user, cmd[4] );
        }
         
    }
    
    public void doSet ( User user, String[] cmd )  {
        /* NickServ HELP REGISTER */ 
        HashString command;
        
        if ( cmd == null || cmd.length < 6 )  {
            this.setMain ( user );
            return; 
        }
        
        command = new HashString ( cmd[5] );
        
        if ( command.is(NOOP) ) {
            this.setNoOp ( user );
        
        } else if ( command.is(NEVEROP) ) {
            this.setNeverOp ( user );
        
        } else if ( command.is(MAILBLOCK) ) {
            this.setMailBlock ( user );
        
        } else if ( command.is(SHOWEMAIL) ) {
            this.setShowEmail ( user );
        
        } else if ( command.is(SHOWHOST) ) {
            this.setShowHost ( user );
        
        } else if ( command.is(EMAIL) ) {
            this.setEmail ( user );
        
        } else if ( command.is(PASSWD) ) {
            this.setPasswd ( user );
        
        } else {
            this.setMain ( user );
        }
        
    }
    
    public void help ( User user ) {
        int access = user.getAccess ( );
        this.showStart ( user, "Help" );
        this.service.sendMsg ( user, "   NickServ allows you to \"register\" a nickname and prevent others"                     );
        this.service.sendMsg ( user, "   from using it. The following commands allow for registration and"                      );
        this.service.sendMsg ( user, "   maintenance of nicknames. To use them type: /NickServ <command>"                       );
        this.service.sendMsg ( user, "   For more information on a specific command, type:"                                     );
        this.service.sendMsg ( user, "       /NickServ HELP <command>"                                                          );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "       "+f.b ( ) +"REGISTER"+f.b ( ) +"    Register a nickname"                           );
        this.service.sendMsg ( user, "       "+f.b ( ) +"IDENTIFY"+f.b ( ) +"    Identify as owner of your nick"                );
        this.service.sendMsg ( user, "       "+f.b ( ) +"GHOST"+f.b ( ) +"       Kill ghosted client holding your nick"         );
        this.service.sendMsg ( user, "       "+f.b ( ) +"SET"+f.b ( ) +"         Set nick settings"                             );
        this.service.sendMsg ( user, "       "+f.b ( ) +"INFO"+f.b ( ) +"        Identify as owner of your nick"                );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   For further options available for your nickname use: /NickServ HELP SET"               );
        this.service.sendMsg ( user, "   "                                                                                      );
        
        if ( access > 0 ) {
            this.service.sendMsg ( user, "   "+f.b ( ) +"--- IRC Operator ---"+f.b ( )                                             );

            for ( CommandInfo ci : NickServ.getCMDList ( IRCOP )  )  {
                Handler.getOperServ ( ) .sendMsg ( user, "       "+f.b ( ) +ci.getName ( ) +f.b ( ) +ci.getPatch ( ) +ci.getDescription ( )  );            
            }

            if ( access > 1 )  {
                this.service.sendMsg ( user, "   " );
                this.service.sendMsg ( user, "   "+f.b ( ) +"--- SA ---"+f.b ( )                                       );
                for ( CommandInfo ci : NickServ.getCMDList ( SA )  )  {
                    this.service.sendMsg ( user, "       "+f.b ( ) +ci.getName ( ) +f.b ( ) +ci.getPatch ( ) +ci.getDescription ( )  );
                }
             }

            if ( access > 2 )  {
                this.service.sendMsg ( user, "   " );
                this.service.sendMsg ( user, "   "+f.b ( ) +"--- CSop ---"+f.b ( )                                     );
                for ( CommandInfo ci : NickServ.getCMDList ( CSOP )  )  {
                    this.service.sendMsg ( user, "       "+f.b ( ) +ci.getName ( ) +f.b ( ) +ci.getPatch ( ) +ci.getDescription ( )  );
                }

            }

            if ( access > 3 )  {
                this.service.sendMsg ( user, "   " );
                this.service.sendMsg ( user, "   "+f.b ( ) +"--- SRA ---"+f.b ( )                                  );
                for ( CommandInfo ci : NickServ.getCMDList ( SRA )  )  {
                    this.service.sendMsg ( user, "       "+f.b ( ) +ci.getName ( ) +f.b ( ) +ci.getPatch ( ) +ci.getDescription ( )  );
                }
            }
            
            Config conf = Proc.getConf();
            System.out.println( "debug: mark: "+conf.getInt(MARK)+":"+NickServ.getCMDList ( SRA ).size() );
        }
        this.showEnd ( user );
    }

    public void doRegister ( User user ) {
        /* NickServ HELP REGISTER */

        this.showStart ( user, "Register" );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /NickServ REGISTER <password> <email>"+f.b ( ) +""                 );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   By registering your nickname you will become the owner of your nickname"               );
        this.service.sendMsg ( user, "   and introduced to a set of features which includes owning your nick even"              );
        this.service.sendMsg ( user, "   when you are not online, able to register channels and able to get added"              );
        this.service.sendMsg ( user, "   to other channel access lists by other channel founders."                              );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   After issuing this command and therefor registering a not already registered"          );
        this.service.sendMsg ( user, "   nickname services will send an auth-code to the email which were part of the"          );
        this.service.sendMsg ( user, "   registration process. You will have to follow the instructions in the mail"            );
        this.service.sendMsg ( user, "   in order to fully register your nickname."                                             );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                         );
        this.service.sendMsg ( user, "   Do not use an easy-to-guess password, rather mix letters with digits and other"        );
        this.service.sendMsg ( user, "   characters in order to make your password more secure."                                );
        this.service.sendMsg ( user, "   Do not share your password ( s )  with anyone, not even your friends. Friends has"     );
        this.service.sendMsg ( user, "   broken up for even less than a hijacked nickname on IRC."                              );
        this.showEnd ( user );
    }

     public void doIdentify ( User user ) {
        this.showStart ( user, "Identify" );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /NickServ IDENTIFY <password>"+f.b ( ) +""                         );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /NickServ IDENTIFY <nick> <password>"+f.b ( ) +""                  );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   Identifying to a nickname will tell services that you are the owner of that"           );
        this.service.sendMsg ( user, "   perticular nickname. By identifying to a nickname you will be given all access"        );
        this.service.sendMsg ( user, "   to the nickname and its access to channels or services."                               );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                         );
        this.service.sendMsg ( user, "   Issue this command only if you are the owner of the perticular nickname as by"         );
        this.service.sendMsg ( user, "   issueing the command you state yourself as trying to identify to a nickname not"       );
        this.service.sendMsg ( user, "   perticularally identify as the owner of it. Only and just only when you successfully"  );
        this.service.sendMsg ( user, "   identified to the nickname you have identified yourself as the owner of it."           );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   Missuse of this command will get you banned from the network."                         );
        this.showEnd ( user );
    }
   
     public void doGhost ( User user ) {
        this.showStart ( user, "Ghost" );
        this.service.sendMsg ( user, "   "                                                                                              );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /NickServ GHOST <nick> <password>"+f.b ( ) +""                             );
        this.service.sendMsg ( user, "   "                                                                                              );
        this.service.sendMsg ( user, "   Identifying to a nickname and tell services that you are the owner of that"                    );
        this.service.sendMsg ( user, "   perticular nickname and requesting services to kill the user currently holding it"             );
        this.service.sendMsg ( user, "   By identifying to a nickname you will be given all access to the nickname and its"             );
        this.service.sendMsg ( user, "   access to channels or services."                                                               );
        this.service.sendMsg ( user, "   "                                                                                              );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                                 );
        this.service.sendMsg ( user, "   Dont share your passwords with anyone. Others could take your nickname by using this command"  );
        this.service.sendMsg ( user, "   with your password and kill your connection to the network."                                   );
        this.service.sendMsg ( user, "   "                                                                                              );
        this.service.sendMsg ( user, "   Missuse of this command will get you banned from the network."                                 );
        this.showEnd ( user );
    }
 
    public void doInfo ( User user ) {
        this.showStart ( user, "Info" );
        this.service.sendMsg ( user, "   "                                                                                              );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /NickServ INFO <nick>"+f.b ( ) +""                                         );
        this.service.sendMsg ( user, "   "                                                                                              );
        this.service.sendMsg ( user, "   Show known information about a specific nickname."                                             );
        this.service.sendMsg ( user, "   "                                                                                              );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                                 );
        this.service.sendMsg ( user, "   If sensitive information is showing up on your nick info"                                      );
        this.service.sendMsg ( user, "   please use "+f.b ( ) +"/NickServ HELP SET"+f.b ( ) +" to find a solution for it."              );
        this.showEnd ( user );
    }
     
    public void doAuth ( User user ) {
        this.showStart ( user, "Info" );
        this.service.sendMsg ( user, "   "                                                                                              );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /NickServ AUTH <code>"+f.b ( ) +""                                         );
        this.service.sendMsg ( user, "   "                                                                                              );
        this.service.sendMsg ( user, "   Authorize a newly added mail or password for current nickname. Note that it will only"         );
        this.service.sendMsg ( user, "   allow current nick to authorize an auth object this means theres no way to auth a new"         );
        this.service.sendMsg ( user, "   password or mail using a different nickname."                                                  );
        this.service.sendMsg ( user, "   "                                                                                              );
        this.service.sendMsg ( user, "   After the pass or mail has been fully authed it will be added to the nick. If there are"       );
        this.service.sendMsg ( user, "   several users online currently identified to the nick when a new pass is authorized then"      );
        this.service.sendMsg ( user, "   users except for the one actually currently holding the nick will be unidentified from"        );
        this.service.sendMsg ( user, "   the nick and they will need to identify to the nick again to re-gain any access they "         );
        this.service.sendMsg ( user, "   got from the nick."                                                                            );
        this.service.sendMsg ( user, "   "                                                                                              );
        this.service.sendMsg ( user, "   Please note that all users identified to the nick will be notified by services telling"        );
        this.service.sendMsg ( user, "   them that a new mail or pass has been fully added to the nick."                                );
        this.service.sendMsg ( user, "   "                                                                                              );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                                 );
        this.service.sendMsg ( user, "   Theres a point of not sharing your mail or password around. Add an unknown mail to your"       );
        this.service.sendMsg ( user, "   nick and a password you dont use anywhere else. Setting a new pass and authing it will"        );
        this.service.sendMsg ( user, "   make anyone currently logged in using it to get logged out from it."                           );
        this.showEnd ( user );
    }
 
    public void setMain ( User user )  {
        /* NickServ HELP REGISTER */
        this.showStart ( user, "Set" );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /NickServ SET <command> <ON|OFF>"+f.b ( ) +""                                          );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   This command is for changing the options of a nickname"                                                    );
        this.service.sendMsg ( user, "   Available options:"                                                                                        );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "       "+f.b ( ) +"NOOP"+f.b ( ) +"       Disable ability for adding nick to channelaccess"                   );
        this.service.sendMsg ( user, "       "+f.b ( ) +"NEVEROP"+f.b ( ) +"    Disable ability for getting op ( @ ) /voice ( + )  automatically"   );
        this.service.sendMsg ( user, "       "+f.b ( ) +"MAILBLOCK"+f.b ( ) +"  Disable ability to send mails to nick"                              );
        this.service.sendMsg ( user, "       "+f.b ( ) +"SHOWEMAIL"+f.b ( ) +"  Show email in info"                                                 );
        this.service.sendMsg ( user, "       "+f.b ( ) +"SHOWHOST"+f.b ( ) +"   Show real host in info"                                             );
        this.service.sendMsg ( user, "       "+f.b ( ) +"EMAIL"+f.b ( ) +"      Set a new email on nick"                                            );
        this.service.sendMsg ( user, "       "+f.b ( ) +"PASSWD"+f.b ( ) +"     Set a new password on nick"                                         );
        this.service.sendMsg ( user, "   "                                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                                             );
        this.service.sendMsg ( user, "   Do not remove settings you do not know the functions of as they could seriously"                           );
        this.service.sendMsg ( user, "   add layers of security on your nickname."                                                                  );
        this.showEnd ( user );
    }
       
    public void setNoOp ( User user )  {
        this.showStart ( user, "Set NoOp" );
        this.service.sendMsg ( user, "   "                                                                  );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /NickServ SET NOOP ON"+f.b ( ) +""             );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /NickServ SET NOOP OFF"+f.b ( ) +""            );
        this.service.sendMsg ( user, "   "                                                                  );
        this.service.sendMsg ( user, "   Setting NoOp on or off."                                           );
        this.service.sendMsg ( user, "   "                                                                  );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                     );
        this.service.sendMsg ( user, "   No recommendations yet."                                           );
        this.showEnd ( user );
    }

    public void setNeverOp ( User user )  {
        this.showStart ( user, "Set NeverOp" );
        this.service.sendMsg ( user, "   "                                                                  );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /NickServ SET NEVEROP ON"+f.b ( ) +""          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /NickServ SET NEVEROP OFF"+f.b ( ) +""         );
        this.service.sendMsg ( user, "   "                                                                  );
        this.service.sendMsg ( user, "   Setting NeverOp on or off."                                        );
        this.service.sendMsg ( user, "   "                                                                  );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                     );
        this.service.sendMsg ( user, "   Recommending this option to be off."                               );
        this.showEnd ( user );
    }

    public void setMailBlock ( User user )  {
        this.showStart ( user, "Set MailBlock" );
        this.service.sendMsg ( user, "   "                                                                  );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /NickServ SET MAILBLOCK ON"+f.b ( ) +""        );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /NickServ SET MAILBLOCK OFF"+f.b ( ) +""       );
        this.service.sendMsg ( user, "   "                                                                  );
        this.service.sendMsg ( user, "   Setting MailBlock on or off."                                      );
        this.service.sendMsg ( user, "   "                                                                  );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                     );
        this.service.sendMsg ( user, "   No recommendations yet."                                           );
        this.showEnd ( user );    }

    public void setShowEmail ( User user )  {
        this.showStart ( user, "Set ShowEmail" );
        this.service.sendMsg ( user, "   "                                                                  );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /NickServ SET SHOWEMAIL ON"+f.b ( ) +""        );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /NickServ SET SHOWEMAIL OFF"+f.b ( ) +""       );
        this.service.sendMsg ( user, "   "                                                                  );
        this.service.sendMsg ( user, "   Setting ShowEmail on or off."                                      );
        this.service.sendMsg ( user, "   "                                                                  );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                     );
        this.service.sendMsg ( user, "   Recommending this option being off."                               );
        this.showEnd ( user );  
    }

    public void setShowHost ( User user )  {
        this.showStart ( user, "Set ShowHost" );
        this.service.sendMsg ( user, "   "                                                                  );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /NickServ SET SHOWHOST ON"+f.b ( ) +""         );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /NickServ SET SHOWHOST OFF"+f.b ( ) +""        );
        this.service.sendMsg ( user, "   "                                                                  );
        this.service.sendMsg ( user, "   Setting ShowHost on or off."                                       );
        this.service.sendMsg ( user, "   "                                                                  );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                     );
        this.service.sendMsg ( user, "   Recommending this option being off."                               );
        this.showEnd ( user );
    } 
    
    private void setEmail ( User user ) {
        this.showStart ( user, "Set Email" );
        this.service.sendMsg ( user, "   "                                                                  );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /NickServ SET EMAIL <pass> <email>"+f.b ( ) +"");
        this.service.sendMsg ( user, "   "                                                                  );
        this.service.sendMsg ( user, "   This command will set a new email to the current nickname. After"  );
        this.service.sendMsg ( user, "   the new mail has been set you will be sent a auth mail. You need"  );
        this.service.sendMsg ( user, "   to follow the instructions in that mail to fully set the new"      );
        this.service.sendMsg ( user, "   mail. Make sure to use a valid email as validation is required."   );
        this.service.sendMsg ( user, "   "                                                                  );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                     );
        this.service.sendMsg ( user, "   Do not share your email."                               );
        this.showEnd ( user );
    }
     
    private void setPasswd ( User user ) {
        this.showStart ( user, "Set Passwd" );
        this.service.sendMsg ( user, "   "                                                                    );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /NickServ SET PASSWD <pass> <email>"+f.b ( ) +"" );
        this.service.sendMsg ( user, "   "                                                                    );
        this.service.sendMsg ( user, "   This command will set a new pass to the current nickname. After"     );
        this.service.sendMsg ( user, "   the new pass has been set you will be sent a auth mail. You need"    );
        this.service.sendMsg ( user, "   to follow the instructions in that mail to fully set the new"        );
        this.service.sendMsg ( user, "   pass. Make sure to use a valid email as validation through mail"     );
        this.service.sendMsg ( user, "   is required."                                                        );
        this.service.sendMsg ( user, "   "                                                                    );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                       );
        this.service.sendMsg ( user, "   Do not share your email."                                            );
        this.showEnd ( user );
    }
     
    
    
    /*** OPER COMMANDS ***/
    
    public void delete ( User user, String[] cmd )  { 
        Oper oper = user.getSID().getOper ( );
        if ( oper == null || ! oper.isAtleast ( SRA )  )  {
            this.service.sendMsg ( user, "Access Denied!" );
            return;
        }
        this.showStart ( user, "Delete" );
        this.service.sendMsg ( user, "   "                                                                       );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: Delete <nick>"+f.b ( ) +""                          );
        this.service.sendMsg ( user, "   "                                                                       );
        this.service.sendMsg ( user, "   This command will delete a nickname from the database."                 );
        this.service.sendMsg ( user, "   "                                                                       );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security Recommendations"+f.r ( )                           );
        this.service.sendMsg ( user, "   Deleting anything from the database should be used with causion"        );
        this.service.sendMsg ( user, "   and only if absolutely required."                                       );
        this.showEnd ( user ); 
    } 
    private void freeze ( User user ) {
        if ( ! NickServ.enoughAccess ( user, FREEZE ) ) {
            return;
        }
        this.showStart ( user, "Freeze" );
        this.service.sendMsg ( user, "   "                                                                                         );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /NickServ FREEZE [-]<nick>"+f.b ( ) +""                               );
        this.service.sendMsg ( user, "   "                                                                                         );
        this.service.sendMsg ( user, "   This command will set the freeze flag or remove it from a nick. When the flag is"         );
        this.service.sendMsg ( user, "   set the nick will not work properly anymore. NickServ will unidentify all users from "    );
        this.service.sendMsg ( user, "   the nick and it will be unuseable."                                                       );
        this.service.sendMsg ( user, "   "                                                                                         );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                            );
        this.service.sendMsg ( user, "   Freeze is a major inconvenience to the user, so please refrain from using the "           );
        this.service.sendMsg ( user, "   command unless absolutely required. This command is logged."                              );
        this.showEnd ( user );      
    }

    private void hold ( User user ) {
        if ( ! NickServ.enoughAccess ( user, HOLD ) ) {
            return;
        }
        this.showStart ( user, "Hold" );
        this.service.sendMsg ( user, "   "                                                                                         );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /NickServ HOLD [-]<nick>"+f.b ( ) +""                                 );
        this.service.sendMsg ( user, "   "                                                                                         );
        this.service.sendMsg ( user, "   This command will set the hold flag or remove it from a nick. When the flag is"           );
        this.service.sendMsg ( user, "   set the nick will not expire."                                                            );
        this.service.sendMsg ( user, "   "                                                                                         );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                            );
        this.service.sendMsg ( user, "   Holding a nick can be nice however please make sure the nick remain active"               );
        this.service.sendMsg ( user, "   and healthy. Keeping a nick registered for selfish reasons is bad for the network."       );
        this.service.sendMsg ( user, "   This command is logged."                                                                  );
        this.showEnd ( user );      
    }

    private void mark(User user) {
        if ( ! NickServ.enoughAccess ( user, MARK ) ) {
            return;
        }
        this.showStart ( user, "Mark" );
        this.service.sendMsg ( user, "   "                                                                                         );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /NickServ MARK [-]<nick>"+f.b ( ) +""                                 );
        this.service.sendMsg ( user, "   "                                                                                         );
        this.service.sendMsg ( user, "   This command will set the mark flag or remove it from a nick. When the flag is"           );
        this.service.sendMsg ( user, "   set the nick will be locked from ownership commands including sendpass and getpass"       );
        this.service.sendMsg ( user, "   "                                                                                         );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                            );
        this.service.sendMsg ( user, "   Marking a nick should only be done if there is a conflict of ownership. The flag"         );
        this.service.sendMsg ( user, "   will stop users and opers from using ownership commands on the nick. This command"        );
        this.service.sendMsg ( user, "   is logged."                                                                               );
        this.showEnd ( user );  
    }

    private void noghost ( User user ) {
        if ( ! NickServ.enoughAccess ( user, NOGHOST ) ) {
            return;
        }
        this.showStart ( user, "Mark" );
        this.service.sendMsg ( user, "   "                                                                                         );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /NickServ NOGHOST [-]<nick>"+f.b ( ) +""                              );
        this.service.sendMsg ( user, "   "                                                                                         );
        this.service.sendMsg ( user, "   This command will set the NoGhost flag or remove it from a nick. When the flag is"        );
        this.service.sendMsg ( user, "   set services will not allow the nick to be ghosted by anyone and send a globops"          );
        this.service.sendMsg ( user, "   message showing who is trying to ghost it."                                               );
        this.service.sendMsg ( user, "   "                                                                                         );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                            );
        this.service.sendMsg ( user, "   NoGhost a nick should only be done if there is a conflict of ownership where someone"     );
        this.service.sendMsg ( user, "   is trying to steal it by ghosting the nick while the user is trying to resolve"           );
        this.service.sendMsg ( user, "   ownership issues. While NoGhost is set the nick is fully useable."                        );
        this.showEnd ( user );  
    }

 
} 