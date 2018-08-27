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
package monitor;

import core.Database;
import core.Proc;
import core.HashNumeric;
import core.Service;
import user.User;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author DreamHealer
 */
public class Snoop extends HashNumeric {
    protected String            chan;
    protected Service           service;

    public Snoop ( )  { }

    public String fixArray ( boolean ok, User user, String[] arr )  {
         
        String str = new String ( );
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
        str += " -  ( "+ ( user != null ? user.getFullMask ( ) : "-" ) +" ) ";
        return str;
    }

    protected void sendTo ( boolean ok, User user, String[] msg )  {
        this.service.send ( 
            Service.RAW, 
            ":"+this.service.getName ( ) +" PRIVMSG "+this.chan+" :"+this.fixArray ( ok, user, msg )
        );
    }
    public void log ( boolean ok, String target, User user, String[] msg )  {
        Database.log ( 
            target, 
            this.fixArray ( ok, user, msg )
        );
    }
}
