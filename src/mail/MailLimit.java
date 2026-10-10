/* 
 * Copyright (C) 2026 Fredrik Karlsson aka DreamHealer & avade.net
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
package mail;

import core.Proc;
import java.util.HashMap;
import operserv.OperServ;
import user.User;

/**
 * REGISTER and SET EMAIL send a mail to an address the user types. Without a
 * limit that is a way to send mail to anyone, as often as the ircd lets a
 * client talk, from the address of the network. So only so many of them are
 * taken from one ip address in an hour (maillimit in services.conf).
 *
 * @author DreamHealer
 */
public class MailLimit {
    private static final long HOUR = 60 * 60 * 1000L;
    /* ip -> { when its hour started, mails since then } */
    private static final HashMap<String,long[]> asked = new HashMap<> ( );

    /**
     * @param user
     * @return true when no more mails are sent for this user right now
     */
    public static boolean reached ( User user ) {
        int limit = Proc.getConf().getMailLimit ( );
        if ( limit <= 0 || user.isOper ( ) || OperServ.isWhiteListed ( user.getMask ( ) ) ) {
            return false;
        }
        long[] row = asked.get ( user.getIp ( ) );
        return row != null && System.currentTimeMillis ( ) - row[0] < HOUR && row[1] >= limit;
    }

    /**
     * A mail was sent for this user
     * @param user
     */
    public static void count ( User user ) {
        long now = System.currentTimeMillis ( );
        if ( asked.size ( ) > 10000 ) {
            /* (the hours that are over, so the list is never more than the
               addresses of the last hour) */
            asked.values().removeIf ( row -> now - row[0] >= HOUR );
        }
        long[] row = asked.get ( user.getIp ( ) );
        if ( row == null || now - row[0] >= HOUR ) {
            asked.put ( user.getIp ( ), new long[] { now, 1 } );
        } else {
            row[1]++;
        }
    }
}
