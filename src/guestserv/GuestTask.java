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

import core.Handler;
import core.Proc;
import core.HashNumeric;
import nickserv.NickServ;
import server.ServSock;
import user.User;
import user.UserMode;
import java.util.Random;
import java.util.TimerTask;

/**
 *
 * @author DreamHealer
 */
class GuestTask extends TimerTask {
    private User        user;
    private Random      rand;
    private int         index;

    public GuestTask ( User user )  { 
        this.user   = user; 
        this.rand   = new Random ( );
    }

    @Override
    public void run ( )  {            
        User u;
        if (  ( u = Handler.findUser ( user.getString ( User.NAME )  )  )  != null &&
                ! u.isIdented ( NickServ.findNick ( u.getString ( User.NAME )  )  )  )  {
            boolean search = true;
            while ( search )  {
                this.index = this.rand.nextInt ( GuestServ.max ) +10000;
                if ( Handler.findUser ( "Guest"+this.index )  == null )  {
                    search = false;
                }
            }
            u.getSID().resetTimers ( );
            ServSock.sendCmd (":"+Proc.getConf().get (HashNumeric.NAME )+" SVSNICK "+u.getString ( User.NAME )+" Guest"+this.index+" 0" );
            u.getModes().set ( UserMode.IDENT, false );
        }
    }
}

