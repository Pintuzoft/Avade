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

import core.CommandInfo;
import core.Config;
import core.Handler;
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

    public void parse ( User user, String[] cmd )  {
        
        try {
            if ( cmd[4].isEmpty ( )  )  { 
                this.help ( user ); 
                return; 
            }
        } catch ( Exception e )  {
            this.help ( user );
            return;
        }
            
        switch ( cmd[4].toUpperCase ( ) .hashCode ( )  )  {
            case REGISTER :
                this.doRegister ( user );
                break;
                
            case IDENTIFY : 
                this.doIdentify ( user );
                break;
                
            case SIDENTIFY :
                this.doIdentify ( user );
                break;
                
            case GHOST :
                this.doGhost ( user );
                break;
                
            case SET :
                this.doSet ( user, cmd );
                break;
                
            case INFO :
                this.doInfo ( user );
                break;
                
            default:  
                this.noMatch ( user, cmd[4] );
            
        } 
    }
    
    public void doSet ( User user, String[] cmd )  {
        /* NickServ HELP REGISTER */ 
        
        if ( cmd == null || cmd.length < 6 )  {
            this.setMain ( user );
            return; 
        }
         
        switch ( cmd[5].toUpperCase ( ) .hashCode ( ) ) { 
            case NOOP :
                this.setNoOp ( user );
                break;
                
            case NEVEROP :
                this.setNeverOp ( user );
                break;
                
            case MAILBLOCK :
                this.setMailBlock ( user );
                break;
            case SHOWEMAIL :
                this.setShowEmail ( user );
                break; 
                
            case SHOWHOST :
                this.setShowHost ( user );
                break;
                
            case EMAIL :
                this.setEmail ( user );
                break;
                
            default :
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
     
    
    
    /*** OPER COMMANDS ***/
    
    public void delete ( User user, String[] cmd )  { 
        Oper oper = user.getSID().getOper ( );
        if ( oper == null || ! oper.isAtleast ( SRA )  )  {
            this.service.sendMsg ( user, "Access Denied!" );
            return;
        }
        this.showStart ( user, "Delete" );
        this.service.sendMsg ( user, "   "                                                                                              );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: Delete <nick>"+f.b ( ) +""                                                 );
        this.service.sendMsg ( user, "   "                                                                                              );
        this.service.sendMsg ( user, "   This command will delete a nickname from the database."                                        );
        this.service.sendMsg ( user, "   "                                                                                              );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security Recommendations"+f.r ( )                                                  );
        this.service.sendMsg ( user, "   Deleting anything from the database should be used with causion"                               );
        this.service.sendMsg ( user, "   and only if absolutely required."                                          );
        this.showEnd ( user ); 
    } 

 
} 