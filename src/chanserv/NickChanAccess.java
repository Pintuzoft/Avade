/* 
 * Copyright (C) 2018 Fredrik Karlsson aka DreamHealer - avade.net
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
package chanserv;

/**
 *
 * @author fredde
 */
public class NickChanAccess {
    private String nick;
    private String chan;
    private String access;
    
    /**
     *
     * @param nick
     * @param chan
     * @param access
     */
    public NickChanAccess  ( String nick, String chan, String access )  {
        this.nick       = nick;
        this.chan       = chan;
        this.access     = access;
    }

    /**
     *
     * @return
     */
    public String getNick() {
        return nick;
    }

    /**
     *
     * @return
     */
    public String getChan() {
        return chan;
    }

    /**
     *
     * @return
     */
    public String getAccess() {
        return access;
    }
     
}
