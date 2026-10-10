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
package guestserv;

import core.Scheduler;
import core.Service;
import nickserv.NickInfo;
import user.User;
import java.util.Random;

/**
 *
 * @author DreamHealer
 */
public class GuestServ extends Service {
    private Random rand;

    /**
     *
     */
    public static int max = 89999;
    
    /**
     *
     */
    public GuestServ ( )  {
        super ( "GuestServ" );
        this.rand = new Random ( );
    }
    
    /**
     *
     * @param user
     * @param ni
     */
    public void addNick ( User user, NickInfo ni )  {
        if ( ! user.isIdented ( ni ) ) {
            /* Send Reminder */
            user.getSID().addAdTimer ( Scheduler.schedule ( new GuestAdTask ( user ), 1*1000 ) );
        
            /* Change nick after 1 minute */
            user.getSID().addTimer ( Scheduler.schedule ( new GuestTask ( user ), 61*1000 ) );
        }
    }
} 