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
package guestserv;

import core.Service;
import nickserv.NickInfo;
import user.User;
import java.util.Calendar;
import java.util.Random;
import java.util.Timer;

/**
 *
 * @author DreamHealer
 */
public class GuestServ extends Service {
    private Random rand;
    private int index;

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
            Timer adTimer = new Timer ( true );
            Calendar now = Calendar.getInstance ( );
            
            /* Send Reminder */
            now.setTimeInMillis ( now.getTimeInMillis ( ) + ( 1*1000 ) );
            adTimer.schedule ( new GuestAdTask ( user ) , now.getTime ( ) );
            System.out.println("0: user:"+(user==null?"null":"not null"));
            System.out.println("1: sid:"+(user.getSID()==null?"null":"not null"));
            user.getSID().addAdTimer ( adTimer );
        
            /* Change nick after 1 minute */
            Timer timer = new Timer ( true );
            now.setTimeInMillis ( now.getTimeInMillis ( ) + ( 60*1000 ) );
            timer.schedule ( new GuestTask ( user ), now.getTime ( ) );
            user.getSID().addTimer ( timer );
        }
    }
} 