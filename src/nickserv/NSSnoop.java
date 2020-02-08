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

import core.Proc;
import monitor.Snoop;
import user.User;

/**
 *
 * @author DreamHealer
 */
public class NSSnoop extends Snoop {

    public NSSnoop ( NickServ service )  {
        super ( );
        this.service            = service;
        this.chan               = Proc.getConf().get ( SNOOPNICKSERV );
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
            case SYNTAX_ERROR :         return "SYNTAX_ERROR";
            case SYNTAX_REG_ERROR :     return "SYNTAX_REG_ERROR";
            case INVALID_EMAIL :        return "INVALID_EMAIL";
            case NICK_ALREADY_REGGED :  return "NICK_ALREADY_REGGED";
            case INVALID_NICK :         return "INVALID_NICK";
            case REGISTER :             return "REGISTER";
            
            case SYNTAX_ID_ERROR :      return "SYNTAX_ID_ERROR";
            case NICK_NOT_REGISTERED :  return "NICK_NOT_REGISTERED";
            case PASSWD_ERROR :         return "PASSWD_ERROR";
            case IS_FROZEN :            return "IS_FROZEN";
            case IS_THROTTLED :         return "IS_THROTTLED";
            
            case IS_MARKED :            return "IS_MARKED";
            
            case ACCESS_DENIED_OPER :   return "ACCESS_DENIED_OPER";
            case ACCESS_DENIED_SA :     return "ACCESS_DENIED_SA";
            case LIST :                 return "LIST";

            case SIDENTIFY :            return "SIDENTIFY";

            case SYNTAX_GHOST_ERROR :   return "SYNTAX_GHOST_ERROR";
            case NO_SUCH_NICK :         return "NO_SUCH_NICK";
            case IS_NOGHOST :           return "IS_NOGHOST";

            case INFO :                 return "INFO";
            
            case SET :                  return "SET";

            case AUTH :                 return "AUTH";
            case AUTHMAIL :             return "AUTHMAIL";
            case AUTHPASS :             return "AUTHPASS";
            case NO_AUTH_FOUND :        return "NO_AUTH_FOUND";
            
            case SETEMAIL :             return "SETEMAIL";
            case SETPASSWD :            return "SETPASSWD";
                
            
            default : return "UnDefined";
        }
    } 
} 
