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
package rootserv;

import core.Proc;
import core.Service;
import core.HashString;
import monitor.Snoop;
import user.User;

/**
 *
 * @author DreamHealer
 */
public class RSSnoop extends Snoop {

    /**
     *
     * @param service
     */
    public RSSnoop ( Service service )  {
        super ( );
        HashString channel  = Proc.getConf().get(SNOOPROOTSERV);
        this.service        = service;
        this.chan           = channel;
    }
 
    /**
     *
     * @param ok
     * @param user
     * @param cmd
     */
    public void msg ( boolean ok, User user, String[] cmd ) {
        this.sendTo ( ok, user, cmd );
    }

}
