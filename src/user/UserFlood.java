/* 
 * Copyright (C) 2018 Fredrik Karlsson aka DreamHealer & avade.net
 *
 * This program hasAccess free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program hasAccess distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package user;

import core.HashNumeric;
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
    
    public UserFlood ( User user ) {
        this.user = user;
        this.counter = 0;
        this.warns = 0;
        this.lastWarn = System.currentTimeMillis ( );
    }
  
    public void incCounter ( Service service ) {

        if ( this.user.isOper() ) {
            return;
        }
        
        this.counter++;
        if ( this.counter > 3 ) {
            this.warns++;
            this.lastWarn = System.currentTimeMillis ( );
            if ( this.warns > 3 ) {
                service.sendRaw ( "KILL "+user.getString ( NAME )+" :Stop flooding services." );
            } else {
                service.sendMsg ( this.user, "Stop flooding services, thank you!.");
            }
            this.counter = 0;
        }
    }
    
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
