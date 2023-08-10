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

    /**
     *
     */
    public CMDResult ( )  {
        this.nick       = null;
        this.status     = new HashString ("");
    }
    
    /**
     *
     * @param nick
     */
    public void setNick ( NickInfo nick ) { 
        this.nick = nick;
    }
    
    /**
     *
     * @param str
     */
    public void setString1 ( String str ) { 
        this.str1 = str;
    }
    
    /**
     *
     * @param status
     */
    public void setStatus ( HashString status ) { 
        this.status = status;
    }    
    
    /**
     *
     * @param auth
     */
    public void setAuth ( NSAuth auth ) { 
        this.auth = auth;
    }    
    
    /**
     *
     * @return nick
     */
    public NickInfo getNick ( ) { 
        return this.nick;
    }
    
    /**
     *
     * @return status
     */
    public HashString getStatus ( ) { 
        return this.status;
    }
    
    /**
     *
     * @param hash
     * @return true/false
     */
    public boolean is ( HashString hash ) {
        return status.is(hash);
    }
    
    /**
     *
     * @return str1
     */
    public String getString1 ( ) { 
        return this.str1;
    }
    
    /**
     *
     * @return auth
     */
    public NSAuth getAuth ( ) { 
        return this.auth;
    }

    /**
     *
     * @return command
     */
    public HashString getCommand ( ) {
        return this.command;
    }

    
    /**
     *
     * @param command
     */    
    void setCommand ( HashString command ) {
        this.command = command;
    }
}
