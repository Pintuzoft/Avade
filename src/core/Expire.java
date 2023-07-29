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

/**
 *
 * @author fredde
 */
public class Expire {
    private long lastSent;
    private int mailCount;
    private static int maxMailCount = 5;
    
    public Expire ( ) {
        this.lastSent = 0;
        this.mailCount = 0;
    }
     
    public void setLastSent ( long in )         { this.lastSent = in;       }
    public void setMailCount ( int in )         { this.mailCount = in;      }
    
    public long getLastSent ( )                 { return this.lastSent;     }
    public int getMailCount ( )                 { return this.mailCount;    }

    public void incMailCount ( ) { 
        this.lastSent = ( System.currentTimeMillis ( ) / 1000 );
        this.mailCount++;
    }

    public void reset ( ) {
        this.lastSent = 0;
        this.mailCount = 0;
    }
    
    public void print ( ) {
        System.out.println ( "NickExp-Debug: lastMail:"+this.lastSent );
        System.out.println ( "NickExp-Debug: mailCount:"+this.mailCount );
    }
    
    /* Return true if its time to send another mail and we havent sent too many  ( 5 )  */
    public boolean isTimeToSendAnotherMail ( ) {
        long lastMail = ( System.currentTimeMillis ( ) / 1000 - this.getLastSent ( ) );
        long maxTime  = ( 60 * 60 * 24 ); /* Wait 24 hours between mails */
        return  ( this.mailCount <= maxMailCount && lastMail > maxTime );
    }
    
    public boolean shouldExpire ( ) {
        long lastMail = ( System.currentTimeMillis ( ) / 1000 - this.getLastSent ( ) );
        long maxTime  = ( 60 * 60 * 24 * 2 ); /* Wait 2 days after last mail */
        return ( this.mailCount > maxMailCount && lastMail > maxTime );
    }
}
