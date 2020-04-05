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
package mail;

import core.Proc;
import core.HashNumeric;
import memoserv.MemoInfo;
import nickserv.NSAuth;
import nickserv.NickInfo;

/**
 *
 * @author DreamHealer
 */
public class SendMail extends HashNumeric {
    
    public SendMail ( ) { }
    
    /* REGISTER MAIL */
    public static void sendNickRegisterMail ( NickInfo ni, NSAuth auth ) {
        Mail mail = new Mail (
            ni.getString ( MAIL ), 
            mailStr ( NICKREG_SUBJECT, "" ),
            auth.getAuth(),
            mailStr ( 
                NICKREG_BODY, 
                ni.getName ( ), 
                Proc.getConf().get ( AUTHURL ),
                auth.getAuth()
            )
        );
        MXDatabase.sendMail ( mail );
    }
    
    /* NEW MEMO */
    public static void sendNewMemo ( NickInfo ni, MemoInfo mi ) {   
        Mail mail = new Mail ( 
            ni.getString ( MAIL ), 
            mailStr ( NEWMEMO_SUBJECT, "" ), 
            null,
            mailStr ( 
                NEWMEMO_BODY,
                null,
                ni.getName ( ), 
                mi.getSender ( )
            )  
        );
        MXDatabase.sendMail ( mail );
    }
      
    /* EXPIRE NICK */
    public static void sendExpNick ( NickInfo ni ) {   
        Mail mail = new Mail ( 
            ni.getString ( MAIL ), 
            null,
            mailStr ( EXPNICK_SUBJECT, "" ), 
            mailStr ( EXPNICK_BODY, ni.getName ( ) )  
        );
        MXDatabase.sendMail ( mail );
    }
    
    /* MAIL STRINGS */
    private static String mailStr ( int code, String... args )  {
        switch ( code )  {
            case NICKREG_BODY :
                return "Hello "+args[0]+"\n\nYou recently registered the "+
                       "nickname: "+args[0]+" using this email address. \n"+
                       "To fully register your nickname please follow this "+
                       "link: "+args[1]+args[2]+"\n\nRegards\n\n"+
                       "/"+Proc.getConf().get ( NETNAME ); 
           
            case NICKREG_SUBJECT :
                return "Nick registration mail"; 
           
            case NEWMEMO_BODY : 
                return "Hello "+args[0]+"\n\nYou have recieved a new memo "+
                       "from "+args[0]+".\nTo read the memo please connect, "+
                       "identify to your nickname and type:\n\n"+
                       "/MemoServ LIST and /MemoServ READ <#num>\n\n"+
                       "Regards\n\n"+
                       "/"+Proc.getConf().get ( NETNAME );
          
            case NEWMEMO_SUBJECT :
                return "New memo"; 
            
            case EXPNICK_BODY :
                return "Hello "+args[0]+"\n\nYour nickname "+args[0]+" "+
                       "is about to expire.\nTo avoid getting your nick "+
                       "expired please reconnect to The Avade IRC Network "+
                       "and identify to your nickname.\n\nRegards\n\n"+
                       "/"+Proc.getConf().get ( NETNAME ); 
           
            case EXPNICK_SUBJECT : 
                return "Nick expiration mail"; 
                
            default :
                return "";
                
        }
    }
    
    private static final int NICKREG_BODY           = 50;
    private static final int NICKREG_SUBJECT        = 51;
    
    private static final int NEWMEMO_BODY           = 52;
    private static final int NEWMEMO_SUBJECT        = 53;
       
    private static final int EXPNICK_BODY           = 54;
    private static final int EXPNICK_SUBJECT        = 55;
    
}
