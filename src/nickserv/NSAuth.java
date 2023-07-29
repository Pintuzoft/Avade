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
 * @author Fredrik Karlsson aka DreamHealer & avade.net
 */
public class NSAuth {
    private HashString type;
    private String value;
    private HashString nick;
    private String auth;
    private String stamp;
    
    public NSAuth ( HashString type, HashString nick, String value ) {
        this.type = type;
        this.nick = nick;
        this.value = value;
        this.hash();
    }
    
    public NSAuth ( HashString type, HashString nick, String value, String auth, String stamp ) {
        this.type = type;
        this.nick = nick;
        this.value = value;
        this.auth = auth;
        this.stamp = stamp;
    }
    public NSAuth ( HashString type, String nick, String value, String auth, String stamp ) {
        this.type = type;
        this.nick = new HashString ( nick );
        this.value = value;
        this.auth = auth;
        this.stamp = stamp;
    }
    
    public void hash ( ) {
        String buf = this.nick+this.value+System.currentTimeMillis()+( Hash.md5 ( ""+System.nanoTime() ) );
        this.auth = Hash.md5 ( buf );
    }

    public HashString getType ( ) {
        return this.type;
    }
    public String getValue ( ) {
        return this.value;
    }

    public HashString getNick ( ) {
        return this.nick;
    }
    public String getNickStr ( ) {
        return this.nick.getString();
    }

    public String getAuth ( ) {
        return this.auth;
    }
    
    public String getStamp ( ) {
        return this.stamp;
    }
    
    public boolean is ( HashString it ) {
        return it.is(type);
    }
}
