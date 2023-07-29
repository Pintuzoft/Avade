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
package operserv;

import static core.HashNumeric.*;
 

/**
 *
 * @author fredde
 */


/*
     MariaDB [aservices_dev]> desc chanacclog;
+-----------+-------------+------+-----+-------------------+----------------+
| Field     | Type        | Null | Key | Default           | Extra          |
+-----------+-------------+------+-----+-------------------+----------------+
| id        | int(11)     | NO   | PRI | NULL              | auto_increment |
| name      | varchar(32) | YES  |     | NULL              |                |
| nick      | varchar(32) | YES  |     | NULL              |                |
| oldaccess | varchar(8)  | YES  |     | NULL              |                |
| access    | varchar(8)  | YES  |     | NULL              |                |
| instater  | varchar(32) | YES  |     | NULL              |                |
| stamp     | datetime    | YES  |     | CURRENT_TIMESTAMP |                |
+-----------+-------------+------+-----+-------------------+----------------+
7 rows in set (0.00 sec)
*/

public class LogAccess {
    private String name;
    private String nick;
    private String oldaccess;
    private String access;
    private String instater;
    private String stamp;
    private boolean isChan;
    
    /**
     *
     * @param name
     * @param nick
     * @param oldaccess
     * @param access
     * @param instater
     * @param stamp
     */
    public LogAccess ( String name, String nick, String oldaccess, String access, String instater, String stamp ) {
        this.name = name;
        this.nick = nick;
        this.oldaccess = oldaccess;
        this.access = access;
        this.instater = instater;
        this.stamp = stamp.substring ( 0, 19 );
        this.isChan = ( name.charAt(0) == '#' );
        
    }

    /**
     *
     * @return
     */
    public String getName() {
        return name;
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
    public String getOldaccess() {
        return oldaccess;
    }

    /**
     *
     * @return
     */
    public String getAccess() {
        return access;
    }

    /**
     *
     * @return
     */
    public String getInstater() {
        return instater;
    }

    /**
     *
     * @return
     */
    public String getStamp() {
        return stamp;
    }

    /**
     *
     * @return
     */
    public boolean isChan() {
        return isChan;
    }
     
}
