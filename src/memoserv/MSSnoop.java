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

import core.Proc;
import monitor.Snoop;
import user.User;

/**
 *
 * @author DreamHealer
 */
class MSSnoop extends Snoop {
      
    public MSSnoop ( MemoServ service )  {
        super ( );
        String channel          = Proc.getConf().get ( SNOOPMEMOSERV );
        this.service            = service;
        this.chan               = channel;
    }

    @Override
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
        str += " -  ( "+user.getFullMask ( ) +" ) ";
        return str;
    }
    
    public void msg ( boolean ok, String target, User user, String[] cmd )  { 
        this.log ( ok, target, user, cmd ); 
        this.sendTo ( ok, user, cmd ); 
    }
}
