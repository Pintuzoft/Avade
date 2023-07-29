/*
 * Copyright (C) 2018 Fredrik Karlsson aka DreamHealer - avade.net
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
import security.Hash;

/**
 *
 * @author Fredrik Karlsson aka DreamHealer - avade.net
 */
public class NSAuth {
    private HashString type;
    private String value;
    private HashString nick;
    private String auth;
    private String stamp;
    
    /**
     *
     * @param type
     * @param nick
     * @param value
     */
    public NSAuth ( HashString type, HashString nick, String value ) {
        this.type = type;
        this.nick = nick;
        this.value = value;
        this.hash();
    }
    
    /**
     *
     * @param type
     * @param nick
     * @param value
     * @param auth
     * @param stamp
     */
    public NSAuth ( HashString type, HashString nick, String value, String auth, String stamp ) {
        this.type = type;
        this.nick = nick;
        this.value = value;
        this.auth = auth;
        this.stamp = stamp;
    }

    /**
     *
     * @param type
     * @param nick
     * @param value
     * @param auth
     * @param stamp
     */
    public NSAuth ( HashString type, String nick, String value, String auth, String stamp ) {
        this.type = type;
        this.nick = new HashString ( nick );
        this.value = value;
        this.auth = auth;
        this.stamp = stamp;
    }
    
    /**
     *
     */
    public void hash ( ) {
        String buf = this.nick+this.value+System.currentTimeMillis()+( Hash.md5 ( ""+System.nanoTime() ) );
        this.auth = Hash.md5 ( buf );
    }

    /**
     *
     * @return
     */
    public HashString getType ( ) {
        return this.type;
    }

    /**
     *
     * @return
     */
    public String getValue ( ) {
        return this.value;
    }

    /**
     *
     * @return
     */
    public HashString getNick ( ) {
        return this.nick;
    }

    /**
     *
     * @return
     */
    public String getNickStr ( ) {
        return this.nick.getString();
    }

    /**
     *
     * @return
     */
    public String getAuth ( ) {
        return this.auth;
    }
    
    /**
     *
     * @return
     */
    public String getStamp ( ) {
        return this.stamp;
    }
    
    /**
     *
     * @param it
     * @return
     */
    public boolean is ( HashString it ) {
        return it.is(type);
    }
}
