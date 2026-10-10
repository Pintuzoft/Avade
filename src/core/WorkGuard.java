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
package core;

import java.util.HashMap;
import java.util.IdentityHashMap;

/**
 * Keeps the database work queues moving. An item that fails to be written
 * while the database is up (bad data, a bug in a query) would otherwise stay
 * first in its queue forever and block everything behind it.
 *
 * @author DreamHealer
 */
public class WorkGuard {
    private static final int MAX_FAILURES = 5;
    private static final IdentityHashMap<Object,Integer> failures = new IdentityHashMap<>();
    private static final HashMap<String,Integer> keyFailures = new HashMap<>();

    /**
     * Call when an item was written
     * @param item
     */
    public static void done ( Object item ) {
        failures.remove ( item );
    }

    /**
     * Same as failed(), for items that are loaded again as new objects
     * every time (identified by a key such as a database id)
     * @param key
     * @param what
     * @return true if the item should be given up on
     */
    public static boolean failedKey ( String key, String what ) {
        if ( ! Database.lastErrorWasData ( ) || ! Database.checkConn ( ) ) {
            return false;
        }
        int count = keyFailures.getOrDefault ( key, 0 ) + 1;
        if ( count >= MAX_FAILURES ) {
            keyFailures.remove ( key );
            Proc.log ( "Database: giving up on "+what+" after "+count+" failed attempts" );
            return true;
        }
        keyFailures.put ( key, count );
        return false;
    }

    /**
     * @param key
     */
    public static void doneKey ( String key ) {
        keyFailures.remove ( key );
    }

    /**
     * Call when writing an item failed
     * @param item
     * @param what description used in the log
     * @return true if the item should be dropped from its queue
     */
    public static boolean failed ( Object item, String what ) {
        if ( ! Database.lastErrorWasData ( ) || ! Database.checkConn ( ) ) {
            /* Database is down, or up and not able to write (shutting down,
               read only, disk full, no access): keep it and try again when
               it is back. Only an item the database refuses for what is in
               it counts. */
            return false;
        }
        int count = failures.getOrDefault ( item, 0 ) + 1;
        if ( count >= MAX_FAILURES ) {
            failures.remove ( item );
            Proc.log ( "Database: giving up on "+what+" after "+count+" failed writes" );
            return true;
        }
        failures.put ( item, count );
        return false;
    }
}
