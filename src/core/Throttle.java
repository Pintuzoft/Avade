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
 * along with this program; if not, see <https://www.gnu.org/licenses/>.
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

    /**
     *
     */
    protected long lastHit;

    /**
     *
     */
    protected int hits;

    /**
     *
     */
    protected static int maxhits    = 3;      /* max hits until throttle kicks in */

    /**
     *
     */
    protected static int maxtime    = 300;    /* seconds a failure is remembered / throttle lasts */
    
    /**
     *
     */
    public Throttle ( ) { 
        this.lastHit = 0;
        this.hits = 0;
    } 
    
    /**
     * Check if throttled. Does not count as an attempt.
     * @return
     */
    public boolean isThrottled ( ) {
        this.expire ( );
        return this.hits > maxhits;
    }
    
    /**
     * Register a failed attempt.
     */
    public void hit ( ) {
        this.expire ( );
        this.hits++;
        this.lastHit = now ( );
    }
    
    /**
     * Reset after a successful attempt.
     */
    public void reset ( ) {
        this.hits = 0;
    }
    
    /* Forget failures older than maxtime */
    private void expire ( ) {
        if ( now ( ) - this.lastHit > ( maxtime * 1000L ) ) {
            this.hits = 0;
        }
    }
    
    private long now ( ) {
        return System.currentTimeMillis();
    }
}