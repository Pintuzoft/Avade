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
package monitor;

import core.Database;
import core.HashNumeric;
import core.HashString;
import core.Service;
import java.util.ArrayList;
import user.User;

/**
 *
 * @author DreamHealer
 */
public class Snoop extends HashNumeric {

    /**
     *
     */
    protected HashString chan;

    /**
     *
     */
    protected Service service;

    /**
     *
     */
    protected static ArrayList<SnoopLog> logs = new ArrayList<>();
            
    /**
     *
     */
    public Snoop ( )  { 
        /* nothingness */
    }

    /**
     *
     * @param ok
     * @param user
     * @param arr
     * @return
     */
    public String fixArray ( boolean ok, User user, String[] arr )  {
         
        String str = "";
        int index = 3;
        int start = 3;

        while ( index < arr.length )  {
            if ( index == start )  {
                str += ( ! ok ? "*" : "" );
                str += "["+arr[index].toUpperCase ( ) +"]:";
            
            } else {
                str += " "+arr[index];
            }
            ++index;
        }
        str += " - ("+ ( user != null ? user.getFullMask ( ) : "-" ) +")";
        return str;
    }

    /**
     *
     * @return
     */
    public int maintenance ( ) {
        int todoAmount = 0;
        todoAmount += writeLogs ( );
        return todoAmount;
    }
    
    private static int writeLogs ( ) {
        if ( Database.activateConnection ( ) && !logs.isEmpty() ) {
            ArrayList<SnoopLog> eLogs = new ArrayList<>();
            for ( SnoopLog log : logs.subList ( 0, getIndexFromSize ( logs.size() ) ) ) {
                if ( Database.SnoopLog ( log ) ) {
                    eLogs.add ( log );
                }
            }
            for ( SnoopLog log : eLogs ) {
                logs.remove ( log );
            }
        }
        return logs.size();
    }
    
    /**
     *
     * @param size
     * @return
     */
    protected static int getIndexFromSize ( int size ) {
        return size > 5 ? 5 : size;
    }
    
    /**
     *
     * @param ok
     * @param user
     * @param msg
     */
    protected void sendTo ( boolean ok, User user, String[] msg )  {
        this.service.send ( 
            Service.RAW, 
            ":"+this.service.getName()+
                    " PRIVMSG "+this.chan+" :"+this.fixArray ( ok, user, msg )
        );
    }

    /**
     *
     * @param ok
     * @param user
     * @param msg
     * @param error
     */
    protected void sendTo ( boolean ok, User user, String[] msg, String error )  {
        this.service.send ( 
            Service.RAW, 
            ":"+this.service.getName()+" PRIVMSG "+this.chan+" :"+this.fixArray ( ok, user, msg )+" ["+error+"]"
        );
    }

    /**
     *
     * @param ok
     * @param target
     * @param user
     * @param message
     */
    public void log ( boolean ok, HashString target, User user, String[] message )  {
        logs.add ( new SnoopLog ( target, this.fixArray ( ok, user, message ) ) );
    }
}
