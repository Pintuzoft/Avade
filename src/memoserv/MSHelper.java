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
package memoserv;

import core.HashString;
import core.Helper;
import core.TextFormat;
import user.User;

/**
 *
 * @author DreamHealer
 */
public class MSHelper extends Helper {
    private MSSnoop         snoop;
    private TextFormat      f;
  
    /**
     *
     * @param service
     * @param snoop
     */
    public MSHelper ( MemoServ service, MSSnoop snoop )  {
        super ( service ); 
        this.snoop      = snoop;
        this.f          = new TextFormat ( );
    }

    /**
     *
     * @param user
     * @param cmd
     */
    public void parse ( User user, String[] cmd )  {
        HashString command;
        try {
            if ( cmd[4].isEmpty ( )  )  { 
                this.help ( user ); 
                return; 
            }
        } catch ( Exception e )  {
            this.help ( user );
            return;
        }

        command = new HashString ( cmd[4] );
        
        if ( command.is(HELP) ) {
            this.help ( user );
        
        } else if ( command.is(SEND) ) {
            this.send ( user );
        
        } else if ( command.is(CSEND) ) {
            this.csend ( user );
        
        } else if ( command.is(LIST) ) {
            this.list ( user );
        
        } else if ( command.is(READ) ) {
            this.read ( user, cmd );
        
        } else if ( command.is(DEL) ) {
            this.del ( user );
        
        } else {
            this.noMatch ( user, cmd[4] );
        }
         
    }

    /**
     *
     * @param user
     */
    public void help ( User user )  {
        /* MemoServ HELP */

        this.showStart ( user, "Help" );

        this.service.sendMsg ( user, "   MemoServ allows you to send short messages to other users regardless"                  );
        this.service.sendMsg ( user, "   they are online or not. To use them type: /NickServ <command>"                         );
        this.service.sendMsg ( user, "   For more information on a specific command, type:"                                     );
        this.service.sendMsg ( user, "       /MemoServ HELP <command>"                                                          );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "       "+f.b ( ) +"SEND"+f.b ( ) +"        Send a memo to a user"                         );
        this.service.sendMsg ( user, "       "+f.b ( ) +"CSEND"+f.b ( ) +"       Send a memo to a channel"                      );
        this.service.sendMsg ( user, "       "+f.b ( ) +"LIST"+f.b ( ) +"        List your inbox"                               );
        this.service.sendMsg ( user, "       "+f.b ( ) +"READ"+f.b ( ) +"        Read a memo"                                   );
        this.service.sendMsg ( user, "       "+f.b ( ) +"DEL"+f.b ( ) +"         Delete a memo"                                 );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   For further options available for your nickname use: /MemoServ HELP SET"               );

        this.showEnd ( user );   
    }

    /**
     *
     * @param user
     */
    public void send ( User user )  {
        /* MemoServ SEND */ 
        this.showStart ( user, "Send" );

        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /MemoServ SEND <nickname> <message>"+f.b ( ) +""                   );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   This command allows you to send a memo to another user on the network."                );
        this.service.sendMsg ( user, "   The reciever does not have to be online for the memo to arrive in their inbox"         );
        this.service.sendMsg ( user, "   and if the reciever didnt tell services to do otherwise the reciever will also"        );
        this.service.sendMsg ( user, "   notify the user by sending an email."                                                  );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                         );
        this.service.sendMsg ( user, "   Do not share any personal information like phone numbers, address, email, name or"     );
        this.service.sendMsg ( user, "   your social security number. If you really want to share personal information with"    );
        this.service.sendMsg ( user, "   the reciever of the memo you are doing so on your own risk."                           );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   Missuse of this command will get you banned from the network."                         );

        this.showEnd ( user );
    }

    /**
     *
     * @param user
     */
    public void csend ( User user )  {
        /* MemoServ SEND */ 
        this.showStart ( user, "CSend" );

        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /MemoServ CSEND <#Chan> <message>"+f.b ( ) +""                     );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   This command allows you to send a memo to a channel list on the network."              );
        this.service.sendMsg ( user, "   The recievers does not have to be online for the memo to arrive in their inbox"        );
        this.service.sendMsg ( user, "   and if the recievers didnt tell services to do otherwise the recievers will also"      );
        this.service.sendMsg ( user, "   get notified through email."                                                           );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                         );
        this.service.sendMsg ( user, "   Do not share any personal information like phone numbers, address, email, name or"     );
        this.service.sendMsg ( user, "   your social security number. If you really want to share personal information with"    );
        this.service.sendMsg ( user, "   the reciever of the memo you are doing so on your own risk."                           );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   Missuse of this command will get you banned from the network."                         );

        this.showEnd ( user );    }

    /**
     *
     * @param user
     */
    public void list ( User user )  {
        /* MemoServ LIST */ 
        this.showStart ( user, "List" );

        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /MemoServ LIST"+f.b ( ) +""                                        );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   This command allows you to list your memo inbox. this will show read and unread"       );
        this.service.sendMsg ( user, "   memos that are saved in your inbox."                                                   );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                         );
        this.service.sendMsg ( user, "   Do not share any personal information like phone numbers, address, email, name or"     );
        this.service.sendMsg ( user, "   your social security number. If you really want to share personal information with"    );
        this.service.sendMsg ( user, "   the reciever of the memo you are doing so on your own risk."                           );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   Missuse of this command will get you banned from the network."                         );

        this.showEnd ( user );    }

    /**
     *
     * @param user
     * @param cmd
     */
    public void read ( User user, String[] cmd )  {
        /* MemoServ READ */ 
        this.showStart ( user, "Read" );

        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /MemoServ READ <#num>"+f.b ( ) +""                                 );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   This command allows you to read a memo listed in the inbox. It will make MemoServ"     );
        this.service.sendMsg ( user, "   send the available information about a specific memo information includes senders"     );
        this.service.sendMsg ( user, "   nickname, date & time and the message included by the sender."                         );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                         );
        this.service.sendMsg ( user, "   Do not share any personal information like phone numbers, address, email, name or"     );
        this.service.sendMsg ( user, "   your social security number. If you really want to share personal information with"    );
        this.service.sendMsg ( user, "   the reciever of the memo you are doing so on your own risk."                           );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   Missuse of this command will get you banned from the network."                         );

        this.showEnd ( user );    }

    /**
     *
     * @param user
     */
    public void del ( User user )  {
        /* MemoServ DEL */ 
        this.showStart ( user, "Del" );

        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /MemoServ DEL <#num>"+f.b ( ) +""                                  );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   This command allows you to delete a memo from the inbox. After the memo is deleted"    );
        this.service.sendMsg ( user, "   it will be nonretrievable and gone forever from the database of MemoServ."             );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                         );
        this.service.sendMsg ( user, "   Never store important information in memos as after using the del command on a memo"   );
        this.service.sendMsg ( user, "   the information that memo held will not be available for retrieval by any means."      );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   Missuse of this command will get you banned from the network."                         );

        this.showEnd ( user );    }
 
}
