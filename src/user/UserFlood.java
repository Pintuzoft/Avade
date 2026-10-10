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
package user;

import core.HashNumeric;
import core.Handler;
import core.Service;

/**
 *
 * @author fredde
 */
public class UserFlood extends HashNumeric {
    private User user;
    private int counter;
    private int warns;
    private long lastWarn;
    /* Someone who is not identified to any nick gets BURST commands at once
       and then one every REFILL milliseconds. That is plenty to identify,
       register and read the help. It stops a connection that only floods
       (INFO in a loop, passwords to guess) from costing a row in the log
       and a password hash for every line the ircd lets through. The rest
       is not answered. */
    private static final int    BURST  = 8;
    private static final long   REFILL = 4000;
    private double  budget   = BURST;
    private long    lastFill = System.currentTimeMillis ( );
    private long    lastTold;
    
    /**
     *
     * @param user
     */
    public UserFlood ( User user ) {
        this.user = user;
        this.counter = 0;
        this.warns = 0;
        this.lastWarn = System.currentTimeMillis ( );
    }
  
    /**
     * A command for NickServ, ChanServ or MemoServ
     * @param service
     * @return true when the command must not be handled: the user is not
     *         identified and sends commands faster than that is allowed
     */
    public boolean tooFast ( Service service ) {
        if ( this.user.isOper ( ) || ( this.user.getSID ( ) != null && ! this.user.getSID().getNiList().isEmpty ( ) ) ) {
            return false;
        }
        long now = System.currentTimeMillis ( );
        this.budget   = Math.min ( BURST, this.budget + ( now - this.lastFill ) / (double) REFILL );
        this.lastFill = now;
        if ( this.budget >= 1 ) {
            this.budget -= 1;
            return false;
        }
        if ( now - this.lastTold > 10000 ) {
            this.lastTold = now;
            service.sendMsg ( this.user, "You are sending commands to the services too fast. Wait a few seconds, or identify to your nick first." );
        }
        return true;
    }

    /**
     *
     * @param service
     * @return true when the user was killed for flooding (and is gone)
     */
    public boolean incCounter ( Service service ) {

        if ( this.user.isOper() ) {
            return false;
        }
        
        this.counter++;
        if ( this.counter > 3 ) {
            this.warns++;
            this.lastWarn = System.currentTimeMillis ( );
            this.counter = 0;
            if ( this.warns > 3 ) {
                service.sendRaw ( "KILL "+user.getString ( NAME )+" :Stop flooding services." );
                /* The ircd never tells us about a kill of our own */
                Handler.deleteUser ( this.user );
                return true;
            } else {
                service.sendMsg ( this.user, "Stop flooding services, thank you!.");
            }
        }
        return false;
    }
    
    /**
     *
     */
    public void maintenence () {
        long now = System.currentTimeMillis ( );
        
        if ( this.user.isOper() ) {
            return;
        }
        
        if ( this.counter > 0 ) {
            this.counter--;
        }
        
        if ( now - this.lastWarn > 120000 && this.warns > 0 ) {
            this.warns--;
        }
    }
}
