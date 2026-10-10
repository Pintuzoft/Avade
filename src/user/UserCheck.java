/* 
 * Copyright (C) 2019 Fredrik Karlsson aka DreamHealer & avade.net
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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package user;

import channel.Chan;

/**
 *
 * @author Fredrik Karlsson aka DreamHealer - avade.net
 */
public class UserCheck {
    private User user;
    private Chan chan;
    
    /**
     *
     * @param chan
     * @param user
     */
    public UserCheck ( Chan chan, User user ) {
        this.chan = chan;
        this.user = user;
    }

    /**
     *
     * @return
     */
    public User getUser ( ) {
        return user;
    }

    /**
     *
     * @return
     */
    public Chan getChan ( ) {
        return chan;
    }
    
}
