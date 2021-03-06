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

import core.HashString;
import user.User;

/**
 *
 * @author fredde
 */
public class CMDResult {
    private NickInfo        nick;
    private HashString      status;
    private String          str1;
    private NSAuth          auth;
    private HashString      command;

    public CMDResult ( )  {
        this.nick       = null;
        this.status     = new HashString ("");
    }
    
    public void setNick ( NickInfo nick ) { 
        this.nick = nick;
    }
    
    public void setString1 ( String str ) { 
        this.str1 = str;
    }
    
    public void setStatus ( HashString status ) { 
        this.status = status;
    }    
    
    public void setAuth ( NSAuth auth ) { 
        this.auth = auth;
    }    
    
    public NickInfo getNick ( ) { 
        return this.nick;
    }
    
    public HashString getStatus ( ) { 
        return this.status;
    }
    
    public boolean is ( HashString hash ) {
        return status.is(hash);
    }
    
    public String getString1 ( ) { 
        return this.str1;
    }
    
    public NSAuth getAuth ( ) { 
        return this.auth;
    }

    public HashString getCommand ( ) {
        return this.command;
    }

    void setCommand ( HashString command ) {
        this.command = command;
    }
}
