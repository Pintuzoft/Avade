/* 
 * Copyright (C) 2018 Fredrik Karlsson aka DreamHealer & avade.net
 *
 * This program is free software"; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation"; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY"; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program"; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package chanserv;

import core.Proc;
import monitor.Snoop;
import user.User;

/**
 *
 * @author DreamHealer
 */
public class CSSnoop extends Snoop {

    public CSSnoop ( ChanServ service )  {
        super ( );
        String channel      = Proc.getConf().get ( SNOOPCHANSERV );
        this.service        = service;
        this.chan           = channel;
    }
 
    public void msg ( boolean ok, String target, User user, String[] cmd )  { 
        this.log ( ok, target, user, cmd );
        this.sendTo ( ok, user, cmd );
    }
    public void msg ( boolean ok, int error, String target, User user, String[] cmd )  {
        String errstr = err2str(error);
        // System.out.println("DEBUG: msg: "+errstr+" ("+error+")");
        if ( ok ) {
            this.log ( ok, target, user, cmd ); 
        }
        this.sendTo ( ok, user, cmd, errstr ); 
    }
    private static String err2str ( int error ) {
        switch ( error ) {
            case SYNTAX_ERROR :              return "SYNTAX_ERROR";
            case SYNTAX_ID_ERROR :           return "SYNTAX_ID_ERROR";
            case SYNTAX_REG_ERROR :          return "SYNTAX_REG_ERROR";

            case CMD_NOT_FOUND_ERROR :       return "CMD_NOT_FOUND_ERROR";
            case SHOW_HELP :                 return "SHOW_HELP";

            case ACCESS_DENIED :             return "ACCESS_DENIED";
            case NOT_ENOUGH_ACCESS :         return "NOT_ENOUGH_ACCESS";
            case SETTING_NOT_FOUND :         return "SETTING_NOT_FOUND";
            case NICK_NOT_REGISTERED :       return "NICK_NOT_REGISTERED";
            case NICK_NOT_AUTHED :           return "NICK_NOT_AUTHED";
            case NICK_NOT_IDENTIFIED :       return "NICK_NOT_IDENTIFIED";
            case CHAN_NOT_REGISTERED :       return "CHAN_NOT_REGISTERED";
            case CHAN_ALREADY_REGGED :       return "CHAN_ALREADY_REGGED";
            case CHAN_NOT_EXIST :            return "CHAN_NOT_EXIST";
            case NICK_NOT_EXIST :            return "NICK_NOT_EXIST";
            case USER_NOT_ONLINE :           return "USER_NOT_ONLINE";

            case USER_NOT_OP :               return "USER_NOT_OP";

            case NICK_HAS_NOOP :             return "NICK_HAS_NOOP";

            case WILL_NOW :                  return "WILL_NOW";
            case WILL_NOW_NOT :              return "WILL_NOW_NOT";

            case IS_NOW :                    return "IS_NOW";
            case IS_NOT :                    return "IS_NOT";

            case PASSWD_ERROR :              return "PASSWD_ERROR";
            case INVALID_EMAIL :             return "INVALID_EMAIL";
            case INVALID_PASSWORD :          return "INVALID_PASSWORD";

            case PASSWD_ACCEPTED :           return "PASSWD_ACCEPTED";

            case DB_ERROR :                  return "DB_ERROR";
            case DB_NICK_ERROR :             return "DB_NICK_ERROR";
            case REGISTER_DONE :             return "REGISTER_DONE";
            case REGISTER_SEC :              return "REGISTER_SEC";

            case NICK_IS_FOUNDER :           return "NICK_IS_FOUNDER";
            case NICK_IS_SOP :               return "NICK_IS_SOP";
            case NICK_IS_OP :                return "NICK_IS_OP";
            case NICK_NOT_FOUND :            return "NICK_NOT_FOUND";
            case NICK_CHANGED :              return "NICK_CHANGED";
            case NICK_NOT_PRESENT :          return "NICK_NOT_PRESENT";

            case NICK_NOT_IDENTED_OP :       return "NICK_NOT_IDENTED_OP";
            case NICK_NEVEROP :              return "NICK_NEVEROP";

            case NICK_ADDED :                return "NICK_ADDED";
            case NICK_NOT_ADDED :            return "NICK_NOT_ADDED";
            case NICK_DELETED :              return "NICK_DELETED";
            case NICK_NOT_DELETED :          return "NICK_NOT_DELETED";
            case NICK_VERBOSE_ADDED :        return "NICK_VERBOSE_ADDED";
            case NICK_VERBOSE_DELETED :      return "NICK_VERBOSE_DELETED";
            case NICK_INVITED :              return "NICK_INVITED";

            case NICK_OP :                   return "NICK_OP";
            case NICK_DEOP :                 return "NICK_DEOP";
            case NICK_VERBOSE_OP :           return "NICK_VERBOSE_OP";
            case NICK_VERBOSE_DEOP :         return "NICK_VERBOSE_DEOP";

            case NICK_MDEOP_CHAN :           return "NICK_MDEOP_CHAN";
            case NICK_MDEOP :                return "NICK_MDEOP";
            case NICK_MKICK_CHAN :           return "NICK_MKICK_CHAN";
            case NICK_MKICK :                return "NICK_MKICK";

            case LIST_NOT_WIPED :            return "LIST_NOT_WIPED";
            case LIST_WIPED :                return "LIST_WIPED";
            case LIST_VERBOSE_WIPED :        return "LIST_VERBOSE_WIPED";

            case CHAN_IS_FROZEN :            return "CHAN_IS_FROZEN";
            case CHAN_IS_CLOSED :            return "CHAN_IS_CLOSED";

            case CHAN_SET_FLAG :             return "CHAN_SET_FLAG"; 
            case CHANFLAG_EXIST :            return "CHANFLAG_EXIST"; 

            case ALREADY_ON_LIST :           return "ALREADY_ON_LIST"; 

            case CHAN_GETPASS :              return "CHAN_GETPASS";
            case CHAN_INFO :                 return "CHAN_INFO";
            case CHAN_UNBAN :                return "CHAN_UNBAN";
            case ACCESS_LIST :               return "ACCESS_LIST";
            case ACCESS_ADDED :              return "ACCESS_ADDED";
            case ACCESS_DELETED :            return "ACCESS_DELETED";
            case ACCESS_DENIED_SRA :         return "ACCESS_DENIED_SRA";
            
            case IS_MARKED :                 return "IS_MARKED"; 
            case IS_THROTTLED :              return "IS_THROTTLED";

            case SHOWACCESSLOG :             return "SHOWACCESSLOG"; 
            case SHOWACCESSLOGOPER :         return "SHOWACCESSLOGOPER"; 
            case SHOWTOPICLOG :              return "SHOWTOPICLOG"; 

            case CHANNELDROPPED :            return "CHANNELDROPPED"; 
            case CHANNELDELETED :            return "CHANNELDELETED"; 

            case NO_SUCH_CHANFLAG :          return "NO_SUCH_CHANFLAG"; 
            case BAD_CHANFLAG_VALUE :        return "BAD_CHANFLAG_VALUE"; 

            case XOP_NOT_FOUND :             return "XOP_NOT_FOUND"; 
            case XOP_ADD_FAIL :              return "XOP_ADD_FAIL"; 
            case XOP_ALREADY_PRESENT :       return "XOP_ALREADY_PRESENT"; 

            case SET_DESCRIPTION :           return "SET_DESCRIPTION"; 
            case SET_TOPICLOCK :             return "SET_TOPICLOCK"; 
            case SET_MODELOCK :              return "SET_MODELOCK"; 
            case SET_KEEPTOPIC :             return "SET_KEEPTOPIC"; 
            case SET_IDENT :                 return "SET_IDENT"; 
            case SET_OPGUARD :               return "SET_OPGUARD"; 
            case SET_RESTRICT :              return "SET_RESTRICT"; 
            case SET_VERBOSE :               return "SET_VERBOSE"; 
            case SET_MAILBLOCK :             return "SET_MAILBLOCK"; 
            case SET_LEAVEOPS :              return "SET_LEAVEOPS"; 
            case SET_AUTOAKICK :             return "SET_AUTOAKICK";
            case SHOW_LIST :                 return "SHOW_LIST";
            
            default : return "UnDefined";
        }
    } 
}
