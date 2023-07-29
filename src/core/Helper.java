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
package core;

import user.User;

/**
 *
 * @author DreamHealer
 */
public abstract class Helper extends HashNumeric {

    /**
     *
     */
    protected Service       service;

    /**
     *
     */
    protected boolean       found;

    /**
     *
     */
    protected CommandInfo   cmdInfo;
    
    /**
     *
     * @param service
     */
    protected Helper ( Service service )                    { this.service = service; }

    /**
     *
     * @param user
     * @param str
     */
    public void showStart ( User user, String str ) {
        str += str.length ( ) > 0 ? " ": "";
        this.service.sendMsg ( user, "*** "+this.service.name+" Help "+str+"***" ); 
    }
    
    /**
     *
     * @param user
     */
    public void showEnd ( User user ) { 
        this.service.sendMsg ( user, "*** End Help ***" ); 
    }
    
    /**
     *
     * @param user
     * @param str
     */
    public void noMatch ( User user, String str ) {
        str += str.length ( ) > 0 ? " " : "";
        this.service.sendMsg ( user, "Sorry, no help for: "+str  ); 
    }

}
