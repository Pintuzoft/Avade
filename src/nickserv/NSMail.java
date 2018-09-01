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

import security.Hash;

/**
 *
 * @author Fredrik Karlsson aka DreamHealer & avade.net
 */
public class NSMail {
    private String mail;
    private String nick;
    private String auth;
    private String stamp;
    
    public NSMail ( String nick, String mail ) {
        this.nick = nick;
        this.mail = mail;
        this.hash();
    }
    
    public NSMail ( String nick, String mail, String auth, String stamp ) {
        this.nick = nick;
        this.mail = mail;
        this.auth = auth;
        this.stamp = stamp;
    }
    
    public void hash ( ) {
        String buf = this.nick+this.mail+System.currentTimeMillis();
        this.auth = Hash.md5 ( buf );
    }

    public String getMail ( ) {
        return mail;
    }

    public String getNick ( ) {
        return nick;
    }

    public String getAuth ( ) {
        return auth;
    }
    
    public String getStamp ( ) {
        return this.stamp;
    }
    
}
