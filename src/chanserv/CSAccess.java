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
package chanserv;

import core.HashNumeric;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import nickserv.NickInfo;
import nickserv.NickServ;

/**
 *
 * @author DreamHealer
 */
public class CSAccess extends HashNumeric {
    private NickInfo    nick;
    private int         access;
    private String      mask;
    private int         hash;
    private String      stamp;
    private DateFormat  dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public CSAccess ( NickInfo ni, int access )  {
        this.nick = ni;
        this.hash = ni.getHashName ( );
        this.access = access;
    }
    
    public CSAccess ( String nick, int access ) {
        if ( ( this.nick = NickServ.findNick ( nick ) ) == null ) {
            this.mask = nick;
            this.hash = this.mask.toUpperCase().hashCode();
        } else {
            this.hash = this.nick.getHashName();
        }
        this.access = access;
    }
     
    public NickInfo getNick ( ) { 
        return this.nick;
    } 
    
    public boolean isHash ( int hash ) {
        return ( this.hash == hash );
    }
    
    public int getHash ( ) {
        return this.hash;
    }
    
    public String getMask ( ) { 
        return this.mask;
    }
    
    public void setStamp ( Date date ) {
        this.dateFormat.format ( date );
    }
    
    /* This is only read by the database */
    public String getStamp ( ) {
        if ( stamp == null || stamp.length() == 0 ) {
            this.stamp = this.dateFormat.format ( new Date ( ) );
        }
        return stamp;
    }

    public int getAccess ( ) {
        return access;
    }
}
