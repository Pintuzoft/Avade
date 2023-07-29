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
 * Throttle class will simply take a hit and return true if its 
 * throttled making it easy to just add this class to different 
 * functionality.
 * 
 * @author DreamHealer
 */
public class Throttle {
    protected long lastHit;
    protected int hits;
    protected static int maxhits    = 3;      /* max hits until throttle kicks in */
    protected static int range      = 5;      /* seconds until out of range */
    protected static int maxtime    = 300;    /* seconds until throttle is removed */
    
    public Throttle ( ) { 
        this.lastHit = System.currentTimeMillis();
        this.hits = 0;
    } 
    
    public boolean isThrottled ( ) {
        /* Is it throttled return true */
        if ( this.hits > maxhits ) {
            this.lastHit = now ( );
            return true;
        }
        
        /* maxtime was reach so we reset it */
        if ( now ( ) - this.lastHit > ( maxtime * 1000 ) ) {
            this.hits = 0;
        }
        
        /* add a hit if time since lastHit hit was within range */
        if (  - this.lastHit < ( range * 1000 ) ) {
            this.hits++;
        }
        
        /* return false to indicate its not throttled */
        this.lastHit = now ( );
        return false;
    }
    private long now ( ) {
        return System.currentTimeMillis();
    }
}