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
import core.HashString;
import memoserv.MemoInfo;
import nickserv.NSAuth;
import nickserv.NickInfo;

/**
 *
 * @author DreamHealer
 */
public class SendMail extends HashNumeric {
    
    /**
     *
     */
    public SendMail ( ) {
        /* nothingness */
    }
    
    /* REGISTER MAIL */

    /**
     *
     * @param ni
     * @param auth
     */

    public static void sendNickRegisterMail ( NickInfo ni, NSAuth auth ) {
        Mail mail = new Mail (
            auth.getValue(), 
            mailStr ( NICKREG_SUBJECT, "" ),
            auth.getAuth(),
            mailStr ( 
                NICKREG_BODY, 
                ni.getNameStr(), 
                Proc.getConf().get(AUTHURL).getString(),
                auth.getAuth()
            )
        );
        MXDatabase.sendMail ( mail );
    }
    
    /* AUTH MAIL */

    /**
     * The mail with the code that confirms a new mail address or a new
     * password. Sent when the code is stored, so the link in it works.
     * @param ni
     * @param auth
     */
    public static void sendAuthMail ( NickInfo ni, NSAuth auth ) {
        String link = Proc.getConf().get(AUTHURL).getString()+auth.getAuth();
        String sign = "\n\nRegards\n\n/"+Proc.getConf().get ( NETNAME );
        String to;
        String subject;
        String body;

        if ( auth.is(MAIL) && ! ni.isAuth ( ) ) {
            /* First address of a new nick */
            sendNickRegisterMail ( ni, auth );
            return;

        } else if ( auth.is(MAIL) ) {
            to      = auth.getValue ( );
            subject = "Confirm your new email address";
            body    = "Hello "+ni.getNameStr()+"\n\nYou asked to change the email address of the "+
                      "nickname: "+ni.getNameStr()+" to this address.\n"+
                      "To confirm the change please follow this link: "+link+sign;

        } else if ( ni.isAuth ( ) ) {
            to      = ni.getString ( MAIL );
            subject = "Confirm your new password";
            body    = "Hello "+ni.getNameStr()+"\n\nYou asked to change the password of the "+
                      "nickname: "+ni.getNameStr()+".\n"+
                      "To confirm the change please follow this link: "+link+"\n\n"+
                      "If you did not ask for this you can ignore this mail, the password stays as it is."+sign;

        } else {
            return; /* no confirmed address to send to */
        }
        MXDatabase.sendMail ( new Mail ( to, subject, auth.getAuth ( ), body ) );
    }
    
    /* RESET PASSWORD */

    /**
     * RESETPASS: the code that lets the owner choose a new password. Only
     * sent to a confirmed address
     * @param ni
     * @param code
     */
    public static void sendResetMail ( NickInfo ni, String code ) {
        if ( ! ni.isAuth ( ) ) {
            return;
        }
        String body = "Hello "+ni.getNameStr()+"\n\nSomeone asked to reset the password of the nickname: "+
                      ni.getNameStr()+".\n"+
                      "To choose a new password, type this on IRC within 2 hours:\n\n"+
                      "    /NickServ RESETPASS "+ni.getNameStr()+" "+code+" <new password>\n\n"+
                      "If you did not ask for this you can ignore this mail, the password stays as it is."+
                      "\n\nRegards\n\n/"+Proc.getConf().get ( NETNAME );
        MXDatabase.sendMail ( new Mail ( ni.getString ( MAIL ), "Reset your password", null, body ) );
    }

    /* NEW MEMO */

    /**
     *
     * @param ni
     * @param mi
     */

    public static void sendNewMemo ( NickInfo ni, MemoInfo mi ) {   
        /* Only to a confirmed address, and not if the owner said no (SET MAILBLOCK) */
        if ( ! ni.isAuth ( ) || ni.isSet ( MAILBLOCKED ) ) {
            return;
        }
        Mail mail = new Mail ( 
            ni.getString ( MAIL ), 
            mailStr ( NEWMEMO_SUBJECT, "" ), 
            null,
            mailStr ( 
                NEWMEMO_BODY,
                ni.getName().getString(), 
                mi.getSender()
            )  
        );
        MXDatabase.sendMail ( mail );
    }
      
    /* EXPIRE NICK */

    /**
     *
     * @param ni
     */

    public static void sendExpNick ( NickInfo ni ) {   
        Mail mail = new Mail ( 
            ni.getString ( MAIL ), 
            mailStr ( EXPNICK_SUBJECT, "" ), 
            null,
            mailStr ( EXPNICK_BODY, ni.getName().getString() )  
        );
        MXDatabase.sendMail ( mail );
    }
    
    /* MAIL STRINGS */
    private static String mailStr ( HashString it, String... args )  {
        if ( it.is(NICKREG_BODY) ) {
            return  "Hello "+args[0]+"\n\nYou recently registered the "+
                    "nickname: "+args[0]+" using this email address. \n"+
                    "To fully register your nickname please follow this "+
                    "link: "+args[1]+args[2]+"\n\nRegards\n\n"+
                    "/"+Proc.getConf().get ( NETNAME ); 
        
        } else if ( it.is(NICKREG_SUBJECT) ) {
            return  "Nick registration mail"; 
        
        } else if ( it.is(NEWMEMO_BODY) ) {
            return  "Hello "+args[0]+"\n\nYou have recieved a new memo "+
                    "from "+args[1]+".\nTo read the memo please connect, "+
                    "identify to your nickname and type:\n\n"+
                    "/MemoServ LIST and /MemoServ READ <#num>\n\n"+
                    "Regards\n\n"+
                    "/"+Proc.getConf().get ( NETNAME );
        
        } else if ( it.is(NEWMEMO_SUBJECT) ) {
            return  "New memo";
        
        } else if ( it.is(EXPNICK_BODY) ) {
            return  "Hello "+args[0]+"\n\nYour nickname "+args[0]+" "+
                    "is about to expire.\nTo avoid getting your nick "+
                    "expired please reconnect to "+Proc.getConf().get ( NETNAME )+" "+
                    "and identify to your nickname.\n\nRegards\n\n"+
                    "/"+Proc.getConf().get ( NETNAME );
        
        } else if ( it.is(EXPNICK_SUBJECT) ) {
            return  "Nick expiration mail";

        } else {
            return "";
        }
         
    } 
    
}
