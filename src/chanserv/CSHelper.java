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

import core.CommandInfo;
import core.Handler;
import static core.HashNumeric.CSOP;
import static core.HashNumeric.IRCOP;
import static core.HashNumeric.SA;
import core.Helper;
import core.TextFormat;
import user.User;

/**
 *
 * @author DreamHealer
 */
public class CSHelper extends Helper {
    private CSSnoop snoop;
   // private boolean found;
    private TextFormat f;
  
    public CSHelper ( ChanServ service, CSSnoop snoop )  {
        super ( service );
//        this.service = service;
        this.snoop = snoop;
        this.f = new TextFormat ( );
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
                this.register ( user );
                break;
                
            case IDENTIFY :
                this.identify ( user );
                break;
                
            case DROP :
                this.drop ( user );
                break;
                
            case SET :
                this.set ( user, cmd );
                break;
                
            case INFO :
                this.info ( user );
                break; 
                
            case SOP :
                this.sop ( user );
                break;
                
            case AOP :
                this.aop ( user );
                break;
                
            case AKICK :
                this.akick ( user );
                break;
                
            case OP :
                this.op ( user );
                break;
                
            case DEOP :
                this.deOp ( user );
                break;
                
            case UNBAN :
                this.unBan ( user );
                break;
                
            case INVITE :
                this.invite ( user );
                break;
                
            case WHY :
                this.why ( user );
                break;
                
            case CHANLIST :
                this.chanList ( user );
                break;
                
            case MDEOP :
                this.mDeOp ( user );
                break;
                
            case MKICK :
                this.mKick ( user );
                break;
                       
            case ACCESSLOG :
                this.accesslog ( user );
                break;
                
            case TOPICLOG :
                this.topiclog ( user );
                break;
                
            case FREEZE :
                this.freeze ( user );
                break;
                
            case CLOSE :
                this.close ( user );
                break;
                
            case HOLD :
                this.hold ( user );
                break;
                
            case MARK :
                this.mark ( user );
                break;
                
            case AUDITORIUM :
                this.auditorium ( user );
                break;
            
            case DELETE :
                this.delete ( user );
                break;
                
            default :
                this.noMatch ( user, cmd[4] );
        
        } 
    }
    
    private void unknownCommand ( User user ) {
        this.service.sendMsg ( user, "Error: No such command found." );
    }

    
    public void help ( User user )  {
        /* NickServ HELP */
        int access = user.getAccess ( );

        this.showStart ( user, "Help" );
        this.service.sendMsg ( user, "   ChanServ allows you to \"register\" channels and gives users the"              );
        this.service.sendMsg ( user, "   ability to administrate it. The following commands allow for "                 );
        this.service.sendMsg ( user, "   registration and administration of channels."                                  );
        this.service.sendMsg ( user, "   For information regarding a specific command, type:"                           );
        this.service.sendMsg ( user, "       /ChanServ HELP <command>"                                                  );
        this.service.sendMsg ( user, "   "                                                                              );
        this.service.sendMsg ( user, "   Commands:"                                                                );

        for ( CommandInfo ci : ChanServ.getCMDList ( USER )  )  {
            if ( ci.getDescription() != null ) {
                Handler.getOperServ().sendMsg ( user, "       "+f.b ( ) +ci.getName ( ) +f.b ( ) +ci.getPatch ( ) +ci.getDescription ( )  );
            }
        }
        this.service.sendMsg ( user, "   "                                                                              );
        this.service.sendMsg ( user, "   Other Commands:"                                                               );
        String buf = "";
        int count = 0;
        for ( CommandInfo ci : ChanServ.getCMDList ( USER ) ) {
            if ( ci.getDescription() == null ) {
                buf += ci.getName()+ci.getPatch();
                count++;
            }
            if ( count > 3 ) {
                Handler.getOperServ().sendMsg ( user, "       "+f.b()+buf+f.b()  );
                count = 0;
                buf = "";
            }
        }

        if ( buf.length() > 0 ) {
            Handler.getOperServ().sendMsg ( user, "       "+f.b()+buf+f.b()  );
        }
        
        

        if ( access > 0 ) {
            this.service.sendMsg ( user, "   "+f.b ( ) +"--- IRC Operator ---"+f.b ( )                                             );

            for ( CommandInfo ci : ChanServ.getCMDList ( IRCOP )  )  {
                Handler.getOperServ ( ) .sendMsg ( user, "       "+f.b ( ) +ci.getName ( ) +f.b ( ) +ci.getPatch ( ) +ci.getDescription ( )  );            
            }

            if ( access > 1 )  {
                this.service.sendMsg ( user, "   " );
                this.service.sendMsg ( user, "   "+f.b ( ) +"--- SA ---"+f.b ( )                                       );
                for ( CommandInfo ci : ChanServ.getCMDList ( SA )  )  {
                    this.service.sendMsg ( user, "       "+f.b ( ) +ci.getName ( ) +f.b ( ) +ci.getPatch ( ) +ci.getDescription ( )  );
                }
             }

            if ( access > 2 )  {
                this.service.sendMsg ( user, "   " );
                this.service.sendMsg ( user, "   "+f.b ( ) +"--- CSop ---"+f.b ( )                                     );
                for ( CommandInfo ci : ChanServ.getCMDList ( CSOP )  )  {
                    this.service.sendMsg ( user, "       "+f.b ( ) +ci.getName ( ) +f.b ( ) +ci.getPatch ( ) +ci.getDescription ( )  );
                }

            }

            if ( access > 3 )  {
                this.service.sendMsg ( user, "   " );
                this.service.sendMsg ( user, "   "+f.b ( ) +"--- SRA ---"+f.b ( )                                  );
                for ( CommandInfo ci : ChanServ.getCMDList ( SRA )  )  {
                    this.service.sendMsg ( user, "       "+f.b ( ) +ci.getName ( ) +f.b ( ) +ci.getPatch ( ) +ci.getDescription ( )  );
                }
            }
        }
        this.showEnd ( user );
    }

    
    public void register ( User user )  {
        this.showStart ( user, "Register" );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /ChanServ REGISTER <#chan> <password> <description>"+f.b ( ) +""     );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   By registering your channel you will become the founder of the channel"                    );
        this.service.sendMsg ( user, "   and introduced to a set of features which includes keeping your chan even"                 );
        this.service.sendMsg ( user, "   when you are not online, able to administrate channel access lists"                        );
        this.service.sendMsg ( user, "   based on your own criterias."                                                              );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   After issuing this command and therefor registering a not already registered"              );
        this.service.sendMsg ( user, "   channel services will keep the channel for you and allow you to administer it."            );
        this.service.sendMsg ( user, "   You need to identify to a registered nickname before being able to register a channel"     );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                           );
        this.service.sendMsg ( user, "   Do not use an easy-to-guess password, rather mix letters with digits and other"            );
        this.service.sendMsg ( user, "   characters in order to make your password more secure."                                    );
        this.service.sendMsg ( user, "   Do not share your password ( s )  with anyone, not even your friends. Friends has"         );
        this.service.sendMsg ( user, "   broken up for even less than a hijacked channel on IRC."                                   );
        this.showEnd ( user );
    }

     public void identify ( User user )  {
        this.showStart ( user, "Identify" );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /ChanServ IDENTIFY <#chan> <password>"+f.b ( ) +""                   );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   Identifying to a channel will tell services that you are the founder of that"              );
        this.service.sendMsg ( user, "   perticular channel. By identifying to a channel you will be given all access"              );
        this.service.sendMsg ( user, "   to the channel including the access lists and the ability to change settings."             );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                           );
        this.service.sendMsg ( user, "   Issue this command only if you are the founder of the perticular channel as by"            );
        this.service.sendMsg ( user, "   issueing the command you state yourself as trying to identify to a channel not"            );
        this.service.sendMsg ( user, "   perticularally identify as the founder of it. Only and just only when you successfully"    );
        this.service.sendMsg ( user, "   identified to the channel you have identified yourself as the founder of it."              );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   Missuse of this command will get you banned from the network."                             );
        this.showEnd ( user );
    }
 
    private void drop ( User user ) {
        this.showStart ( user, "Drop" );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /ChanServ DROP <#chan> <pass>"+f.b ( ) +""                             );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   The drop command will let a user end a channel registration. Once the command has been"    );
        this.service.sendMsg ( user, "   issued all users currently identified to the channel will get unidentified, and the chan"  );
        this.service.sendMsg ( user, "   will be removed from ChanServ and the database."                                           );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                             );
        this.service.sendMsg ( user, "   Dont share your passwords, chances are someone will drop your channel or take control"     );
        this.service.sendMsg ( user, "   of it."                                                                                          );
        this.showEnd ( user );  
    }
         
    public void info ( User user )  {
        this.showStart ( user, "Info" );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /ChanServ INFO <#chan>"+f.b ( ) +""                                  );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   Show known information about a specific channel."                                          );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                           );
        this.service.sendMsg ( user, "   If sensitive information is showing up on your channel info"                               );
        this.service.sendMsg ( user, "   please use "+f.b ( ) +"/ChanServ HELP SET"+f.b ( ) +" to find a solution for it."        );
        this.showEnd ( user );
    }
 
    public void sop ( User user )  {
        this.showStart ( user, "Sop" );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /ChanServ SOP <#chan> <ADD|DEL|LIST> [<nick|mask|#NUM>]"+f.b ( ) +"" );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   Add, delete or list SuperOps in the channel."                                              );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                           );
        this.service.sendMsg ( user, "   Make sure you can trust the users who you give Sop access as these users can add/delete"   );
        this.service.sendMsg ( user, "   AOP and AKICK and kick/ban any user from the channel."                                     );
        this.showEnd ( user );
    }
 
    public void aop ( User user )  {
        this.showStart ( user, "Aop" );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /ChanServ AOP <#chan> <ADD|DEL|LIST> [<nick|mask|#NUM>]"+f.b ( ) +"" );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   Add, delete or list AutoOps in the channel."                                               );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                           );
        this.service.sendMsg ( user, "   Make sure you can trust the users who you give Aop access as these users can kick/ban"     );
        this.service.sendMsg ( user, "   any user from the channel."                                                                );
        this.showEnd ( user );
    }
    
    public void akick ( User user )  {
        this.showStart ( user, "Akick" );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /ChanServ AKICK <#chan> <ADD|DEL|LIST> [<nick|mask|#NUM>]"+f.b( )+"" );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   Add, delete or list AutoKicked users in the channel."                                      );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                           );
        this.service.sendMsg ( user, "   Make sure that permanently banned users are akicked to ensure"                             );
        this.service.sendMsg ( user, "   the safety of your channel."                                                               );
        this.showEnd ( user );
    }
 
    public void op ( User user )  {
        this.showStart ( user, "Op" );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /ChanServ OP <#chan> <nick>"+f.b ( ) +""                             );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   Gives a user op ( @ )  in the channel by chanserv based on your access level."             );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                           );
        this.service.sendMsg ( user, "   No recommendations at this point"                                                          );
        this.showEnd ( user );
    }
 
    public void deOp ( User user )  {
        this.showStart ( user, "DeOp" );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /ChanServ DEOP <#chan> <nick>"+f.b ( ) +""                           );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   Removes op ( @ )  from a user in the channel by chanserv based on your access level."      );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                           );
        this.service.sendMsg ( user, "   No recommendations at this point"                                                          );
        this.showEnd ( user );
    }
 
    public void unBan ( User user )  {
        this.showStart ( user, "UnBan" );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /ChanServ UNBAN <#chan> [<nick>]"+f.b ( ) +""                        );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   Unbans a user from a channel the user has access to"                                       );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                           );
        this.service.sendMsg ( user, "   A good way to regain control of a channel"                                                 );
        this.showEnd ( user );
    }
 
    public void invite ( User user )  {
        this.showStart ( user, "Invite" );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /ChanServ INVITE <#chan>"+f.b ( ) +""                                );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   Sends an invite to a channel that will evade all channel modes that are keeping you out"   );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                           );
        this.service.sendMsg ( user, "   A good way to regain access to a closed channel"                                           );
        this.showEnd ( user );
    }
  
    public void why ( User user )  {
        this.showStart ( user, "Why" );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /ChanServ WHY <#chan> <nick>"+f.b ( ) +""                            );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   Will look up and tell which access a user has to a channel"                                );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                           );
        this.service.sendMsg ( user, "   Keep this information to yourself"                                                         );
        this.showEnd ( user );
    }
  
    public void chanList ( User user )  { 
        this.showStart ( user, "ChanList" ); 
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /ChanServ CHANLIST"+f.b ( ) +""                                      );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   Will list all channels you have access to and show access channel identified nick"         );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                           );
        this.service.sendMsg ( user, "   Keep this information to yourself"                                                         ); 
        this.showEnd ( user );
    }
  
    public void mKick ( User user )  { 
        this.showStart ( user, "MKick" ); 
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /ChanServ MKICK <#Channel>"+f.b ( ) +""                              );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   Will kick all users in the given channel but only if you hold the highest rank"            );
        this.service.sendMsg ( user, "   currently inside the channel. A founder will always be able to mkick a channel while"      );
        this.service.sendMsg ( user, "   an AOP only will be able to mkick the channel as long as there are no SOP's or"            );
        this.service.sendMsg ( user, "   the founder present in the channel."                                                       );
        this.service.sendMsg ( user, "   Please note that the channel will be held for 60 seconds after issuing a MKICK."           );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                           );
        this.service.sendMsg ( user, "   Misusing this command could get your channel frozen."                                      ); 
        this.showEnd ( user );
    }
       
    public void mDeOp ( User user )  { 
        this.showStart ( user, "MDeOp" ); 
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /ChanServ MDEOP <#Channel>"+f.b ( ) +""                              );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   Will deop ( @ )  all users in the given channel but only if you hold the highest rank"     );
        this.service.sendMsg ( user, "   currently inside the channel. A founder will always be able to mdeop a channel while"      );
        this.service.sendMsg ( user, "   an AOP only will be able to mdeop the channel as long as there are no SOP's or"            );
        this.service.sendMsg ( user, "   the founder present in the channel."                                                       );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                           );
        this.service.sendMsg ( user, "   Misusing this command could get your channel frozen."                                      ); 
        this.showEnd ( user );
    }
    
    public void set ( User user, String[] cmd )  {
        /* NickServ HELP REGISTER */ 
        
        try {
            if ( cmd[5].isEmpty ( )  )  {
                this.setMain ( user );
                return; 
            }
        } catch ( Exception e )  {
            this.setMain ( user );
            return;
        }
         
        switch ( cmd[5].toUpperCase ( ) .hashCode ( )  )  {
            case KEEPTOPIC :
                this.setKeepTopic ( user );
                break;
                
            case TOPICLOCK :
                this.setTopicLock ( user );
                break;
                
            default :
        }
    
    } 
          
    public void setMain ( User user )  {
        /* NickServ HELP REGISTER */
        this.showStart ( user, "Set" );
        this.service.sendMsg ( user, "   "                                                                                                              );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /ChanServ SET <#chan> <command> <ON|OFF|<value>>"+f.b ( ) +""                              );
        this.service.sendMsg ( user, "   "                                                                                                              );
        this.service.sendMsg ( user, "   This command is used for changing one of the following options of a channel"                                   );
        this.service.sendMsg ( user, "   "                                                                                                              );
        this.service.sendMsg ( user, "       "+f.b ( ) +"DESCRIPTION"+f.b ( ) +"    Sets a new channel description"                                     );
        this.service.sendMsg ( user, "       "+f.b ( ) +"KEEPTOPIC"+f.b ( ) +"      Keep the last topic if the channel goes empty"                      );
        this.service.sendMsg ( user, "       "+f.b ( ) +"TOPICLOCK"+f.b ( ) +"      Locks chan topic to specific staff access"                          );
        this.service.sendMsg ( user, "       "+f.b ( ) +"IDENT"+f.b ( ) +"          Only chan staff using identified nicks can access the chan"         );
        this.service.sendMsg ( user, "       "+f.b ( ) +"OPGUARD"+f.b ( ) +"        Ops will only be allowed to users on the access lists"              );
        this.service.sendMsg ( user, "       "+f.b ( ) +"RESTRICT"+f.b ( ) +"       Channel will only allow aop+ nicks into the channel"                );
        this.service.sendMsg ( user, "       "+f.b ( ) +"VERBOSE"+f.b ( ) +"        Will make changes to the channel verbose"                           );
        this.service.sendMsg ( user, "       "+f.b ( ) +"MAILBLOCK"+f.b ( ) +"      Prevent services from sending password to founders mail"            );
        this.service.sendMsg ( user, "       "+f.b ( ) +"LEAVEOPS"+f.b ( ) +"       First user in will be allowed to be op(@) "                         );
        this.service.sendMsg ( user, "   "                                                                                                              );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                                                 );
        this.service.sendMsg ( user, "   Do not remove settings you do not know the functions of as they could seriously"                               );
        this.service.sendMsg ( user, "   add layers of security on your channel."                                                                       );
        this.showEnd ( user );
    }
      
    
    public void setDescription ( User user )  {
        this.showStart ( user, "Set Description" );
        this.service.sendMsg ( user, "   "                                                                                                              );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /ChanServ SET <#chan> DESCRIPTION <new description>"+f.b ( ) +""                           );
        this.service.sendMsg ( user, "   "                                                                                                              );
        this.service.sendMsg ( user, "   Setting a new description on a channel. The description will show up in the"                                   );
        this.service.sendMsg ( user, "   channel info and will represent the values of the channel and its staff."                                      );
        this.service.sendMsg ( user, "   "                                                                                                              );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                                                 );
        this.service.sendMsg ( user, "   Do not set personal or otherwise sensitive information in your description."                                   );
        this.service.sendMsg ( user, "   Description should only describe the channel. A good informative description can."                             );
        this.service.sendMsg ( user, "   attract new users."                                                                                            );
        this.showEnd ( user );    
    }
    
    public void setKeepTopic ( User user )  {
        this.showStart ( user, "Set KeepTopic" );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /ChanServ SET <#chan> KEEPTOPIC ON"+f.b ( ) +""                        );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /ChanServ SET <#chan> KEEPTOPIC OFF"+f.b ( ) +""                       );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   Setting keeptopic on or off. Enabling this setting will make services"                     );
        this.service.sendMsg ( user, "   remember the last topic and reset it if the channel goes empty."                           );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                             );
        this.service.sendMsg ( user, "   Recommended setting for this option is ON."                                                );
        this.showEnd ( user );
    }

    public void setTopicLock ( User user )  {
        this.showStart ( user, "Set TopicLock" );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /ChanServ SET <#chan> TOPICLOCK <AOP|SOP|FOUNDER>"+f.b ( ) +""         );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   Setting topiclock locks the topic for a specific access. Setting this"                     );
        this.service.sendMsg ( user, "   feature will make services force topic to be set according to access."                     );
        this.service.sendMsg ( user, "   If set to SOP only SOP and FOUNDER can set a new topic. Services will also"                );
        this.service.sendMsg ( user, "   remember the last topic if the channel goes empty."                                        );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                             );
        this.service.sendMsg ( user, "   There are no recommendations for this option however on channels with alot."               );
        this.service.sendMsg ( user, "   of new users there might be a good idea to force the topic to avoid complications."        );
        this.showEnd ( user );
    }

  

    public void setIdent ( User user )  {
        this.showStart ( user, "Set Ident" );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /ChanServ SET <#chan> IDENT <ON|OFF>"+f.b ( ) +""                      );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   Setting ident means requiring channel ops (@)  to first identify to their nicks"           );
        this.service.sendMsg ( user, "   before able to gain op ( @ )  in the channel through chanserv."                            );
        this.service.sendMsg ( user, "   Users who arent on the aop or sop lists will not be able to hold op (@) in the channel."   );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                             );
        this.service.sendMsg ( user, "   For more security the ident setting is a very good option. It will however not"            );
        this.service.sendMsg ( user, "   protect the channel from loco users with access to the channel."                           );
        this.showEnd ( user );  
    }

    public void setOpGuard ( User user )  {
        this.showStart ( user, "Set OpGuard" );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /ChanServ SET <#chan> OPGUARD <ON|OFF>"+f.b ( ) +""                    );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   Setting opguard will protect aops, sops and founder from getting deoped"                   );
        this.service.sendMsg ( user, "   in the channel. Chanserv will automatically reop those who were deoped by others"          );
        this.service.sendMsg ( user, "   as long as the op was not deoped by an higher authority."                                  );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                             );
        this.service.sendMsg ( user, "   This is a nice option for channel ops ( @ )  vs takeovers, however the feature might"      );
        this.service.sendMsg ( user, "   confuse channel bots."                                                                     );
        this.showEnd ( user );  
    }

    public void setRestrict ( User user )  {
        this.showStart ( user, "Set Restrict" );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /ChanServ SET <#chan> RESTRICT <ON|OFF>"+f.b ( ) +""                   );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   The restrict option will close the channel from use by other than those"                   );
        this.service.sendMsg ( user, "   listed on a channel access list or the founder. Users without access will be"              );
        this.service.sendMsg ( user, "   banned temp."                                                                              );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                             );
        this.service.sendMsg ( user, "   This setting is not suited for open channels. Its only use is to restrict the"             );
        this.service.sendMsg ( user, "   channel to users with access to the channel."                                              );
        this.showEnd ( user );  
    }

    public void setVerbose ( User user )  {
        this.showStart ( user, "Set Verbose" );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /ChanServ SET <#chan> VERBOSE <ON|OFF>"+f.b ( ) +""                    );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   The verbose option will cause chanserv to notify current channel ops ( @ )  of"            );
        this.service.sendMsg ( user, "   changes to the channel access lists."                                                      );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                             );
        this.service.sendMsg ( user, "   This setting has only an informational value but could be nice to be used"                 );
        this.service.sendMsg ( user, "   in order to quickly notify the changes to the ops ( @ ) ."                                 );
        this.showEnd ( user );  
    }

    public void setMailBlock ( User user )  {
        this.showStart ( user, "Set MailBlock" );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /ChanServ SET <#chan> MAILBLOCK <ON|OFF>"+f.b ( ) +""                  );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   Mailblock will deny services to send ownership mails to the founder email."                );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                             );
        this.service.sendMsg ( user, "   The mailblock is a good setting to assure the channel password to not come"                );
        this.service.sendMsg ( user, "   into the hands of a user who has access to the founder email."                             );
        this.showEnd ( user );  
    }

    public void setLeaveOps ( User user )  {
        this.showStart ( user, "Set LeaveOps" );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /ChanServ SET <#chan> LEAVEOPS <ON|OFF>"+f.b ( ) +""                   );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   The leaveops setting will let the first user gain op ( @ )  in the channel after"          );
        this.service.sendMsg ( user, "   the channel has gone empty making sure theres atleast one op ( @ )  in the channel."       );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                             );
        this.service.sendMsg ( user, "   In order to make sure that op ( @ )  does not come into the wrong hands this setting"      );
        this.service.sendMsg ( user, "   is not recommended to be enabled."                                                         );
        this.showEnd ( user );  
    }

    public void setPrivate ( User user )  {
        this.showStart ( user, "Set Private" );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /ChanServ SET <#chan> PRIVATE <ON|OFF>"+f.b ( ) +""                    );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   The private option will keep the channel as private and secret ( +ps )  while keeping the" );
        this.service.sendMsg ( user, "   topic off the channel info."                                                               );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                             );
        this.service.sendMsg ( user, "   To avoid the channel from being used by unwanted users and to make sure the topic"         );
        this.service.sendMsg ( user, "   is secret in the channel info its recommended. For ordinary channels its not recommended." );
        this.showEnd ( user );  
    }

    private void accesslog ( User user ) {
        this.showStart ( user, "AccessLog" );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /ChanServ ACCESSLOG <#chan>"+f.b ( ) +""                               );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   The access log is for channel staff to keep track of when and who added AOP, SOP,"         );
        this.service.sendMsg ( user, "   AKICK and when the Founder has been changed in the channel. Information is control."       );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                             );
        this.service.sendMsg ( user, "   This command is available to all AOP+ in a channel and IRCop's."                           );
        this.showEnd ( user );  
    }
    private void topiclog ( User user ) {
        this.showStart ( user, "TopicLog" );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /ChanServ TOPICLOG <#chan>"+f.b ( ) +""                               );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   The topic log is for IRC operators to keep track of when and who added specific topics"    );
        this.service.sendMsg ( user, "   in a channel. Old topics are otherwise forgotten if a new topic is set so this is a"       );
        this.service.sendMsg ( user, "   nice command to check the temperature of a channel or finding abuse or network violations" );
        this.service.sendMsg ( user, "   shared through the topics of a channel."                                                        );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                             );
        this.service.sendMsg ( user, "   This command may show sensitive information. Only share information which the user can"    );
        this.service.sendMsg ( user, "   retrieve themselfs and only IRCop's can access this information."                     );
        this.showEnd ( user );  
    }

    private void freeze ( User user ) {
        if ( ! ChanServ.enoughAccess ( user, FREEZE ) ) {
            return;
        }
        this.showStart ( user, "Freeze" );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /ChanServ FREEZE [-]<#chan>"+f.b ( ) +""                               );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   This command will set the freeze flag or remove it from a channel. When the flag is"       );
        this.service.sendMsg ( user, "   set the channel will not work properly anymore. ChanServ will not give op(@) and any "     );
        this.service.sendMsg ( user, "   ChanServ command will stop working."                                                       );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                             );
        this.service.sendMsg ( user, "   Freeze is a major inconvenience to the users and the staff of the channel, so please"      );
        this.service.sendMsg ( user, "   refrain from using the command unless absolutely required. This command is logged."        );
        this.showEnd ( user );      
    }

    private void close ( User user ) {
        if ( ! ChanServ.enoughAccess ( user, CLOSE ) ) {
            return;
        }
        this.showStart ( user, "Close" );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /ChanServ CLOSE [-]<#chan>"+f.b ( ) +""                                );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   This command will set the close flag or remove it from a channel. When the flag is"        );
        this.service.sendMsg ( user, "   set the channel will not work anymore. The channel will be kept empty and any user"        );
        this.service.sendMsg ( user, "   found in it will be removed."                                                              );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                             );
        this.service.sendMsg ( user, "   Closing a channel should be the last resort of action taken. Perhaps you have access"      );
        this.service.sendMsg ( user, "   to this command but you might want to consult someone before using the command. This"      );
        this.service.sendMsg ( user, "   command is logged."      );
        this.showEnd ( user );      
    }

    private void hold ( User user ) {
        if ( ! ChanServ.enoughAccess ( user, HOLD ) ) {
            return;
        }
        this.showStart ( user, "Hold" );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /ChanServ HOLD [-]<#chan>"+f.b ( ) +""                                 );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   This command will set the hold flag or remove it from a channel. When the flag is"         );
        this.service.sendMsg ( user, "   set the channel will not expire."                                                          );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                             );
        this.service.sendMsg ( user, "   Holding a channel can be nice however please make sure the channel remain active"          );
        this.service.sendMsg ( user, "   and healthy. Keeping a channel registered for selfish reasons is bad for the network."     );
        this.service.sendMsg ( user, "   This command is logged."                                                                   );
        this.showEnd ( user );      
    }

    private void mark(User user) {
        if ( ! ChanServ.enoughAccess ( user, MARK ) ) {
            return;
        }
        this.showStart ( user, "Mark" );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /ChanServ MARK [-]<#chan>"+f.b ( ) +""                                 );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   This command will set the mark flag or remove it from a channel. When the flag is"         );
        this.service.sendMsg ( user, "   set the channel will be locked from ownership commands including sendpass and getpass"     );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                             );
        this.service.sendMsg ( user, "   Marking a channel should only be done if there is a conflict of ownership. The flag"       );
        this.service.sendMsg ( user, "   will stop users and opers from using ownership commands on the channel. This command"      );
        this.service.sendMsg ( user, "   is logged."                                                                   );
        this.showEnd ( user );  
    }

    private void auditorium ( User user ) {
        if ( ! ChanServ.enoughAccess ( user, AUDITORIUM ) ) {
            return;
        }
        this.showStart ( user, "Auditorium" );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /ChanServ AUDITORIUM [-]<#chan>"+f.b ( ) +""                           );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   This command will set the auditorium flag or remove it from a channel. When the flag is"   );
        this.service.sendMsg ( user, "   set the channel will be set with chanmode +A. The mode will hide joins/parts from the"     );
        this.service.sendMsg ( user, "   channel from users and voice/op is required to speak. When a user is oped/voiced the"      );
        this.service.sendMsg ( user, "   regular users will see the user join the channel. And when a user is deoped/devoiced"      );
        this.service.sendMsg ( user, "   a regular user is persieving it as the user parting the channel. This behavior is not"     );
        this.service.sendMsg ( user, "   normal and clients will get confused. The setting/mode should only be used in special"     );
        this.service.sendMsg ( user, "   occasions as network events like if network staff is holding an open public conference"    );
        this.service.sendMsg ( user, "   like a lecture where alot of users will participate."                                      );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   Upon setting the Auditorium mode the relay channel will be registered to the issuer."      );
        this.service.sendMsg ( user, "   So if you set channel #avade with the Auditorium mode the channel #avade-relay will"       );
        this.service.sendMsg ( user, "   automatically be registered to you. The channel will be set with +sp but will be open"     );
        this.service.sendMsg ( user, "   for anyone to join to make it easier to manage. Feel free to restrict this channel."       );
        this.service.sendMsg ( user, "   When the Auditorium mode is removed the relay channel will be dropped."                    );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                             );
        this.service.sendMsg ( user, "   The auditorium mode should not be used on a normal channel as its behavior by filtering"   );
        this.service.sendMsg ( user, "   join/part for regular users will make clients desynced and users will not be able to"      );
        this.service.sendMsg ( user, "   see everyone in the channel when its removed. To counter this behavior its recommended"    );
        this.service.sendMsg ( user, "   to issue a masskick on the channel after the mode is removed to make sure everyones"       );
        this.service.sendMsg ( user, "   clients will get all the joins and parts properly and so they can see all users."          );
        this.service.sendMsg ( user, "   When it comes to the relay channel, make sure you put in some security on it perhaps"      );
        this.service.sendMsg ( user, "   set it invite only or Oper only +O or perhaps even set restrict on."                       );
        this.showEnd ( user );  
    }

    private void delete(User user) {
        if ( ! ChanServ.enoughAccess ( user, DELETE ) ) {
            return;
        }
        this.showStart ( user, "Delete" );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /ChanServ DELETE <#chan>"+f.b ( ) +""                                  );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   The DELETE command is one of the most powerful commands in services. Its suppose to fix"   );
        this.service.sendMsg ( user, "   registration issues that users might encounter such as the channel registration only"      );
        this.service.sendMsg ( user, "   gets half way i.e regged but not properly added to services. Or if a channel simply"       );
        this.service.sendMsg ( user, "   should be dropped for other reasons. The delete command is basically a force drop command" );
        this.service.sendMsg ( user, "   "                                                                                          );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                             );
        this.service.sendMsg ( user, "   Dont go around deleting channels without cause. Users regged their channels and put"       );
        this.service.sendMsg ( user, "   their time and effort into them. If a channel been doing something illegal or against"     );
        this.service.sendMsg ( user, "   network policys then rather close the channel than delete it."                             );
        this.showEnd ( user );  
    }
}
