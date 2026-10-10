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
package mail;

import core.Database;
import core.Proc;
import core.WorkGuard;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author DreamHealer
 */
public class MXDatabase extends Database {
    
    /* NickServ Methods */

    /**
     *
     * @param mail
     * @return
     */

    /* Mails that could not be put in the mailbox table because the database
       was away. They go there when it is back (flush, every second), so a
       code that was promised in a mail is sent, only later. */
    private static final ArrayList<Mail> waiting = new ArrayList<> ( );

    /**
     * @param mail
     * @return 1 when the mail is in the mailbox, -2 when it waits for the database
     */
    public static int sendMail ( Mail mail )  { 
        if ( ! waiting.isEmpty ( ) || ! activateConnection ( ) || insert ( mail ) != 1 )  {
            /* (behind the ones that already wait: they keep their order) */
            waiting.add ( mail );
            return -2;
        }
        return 1;
    }

    /**
     * @return the mails that wait for the database
     */
    public static int waiting ( ) {
        return waiting.size ( );
    }

    /**
     * Put the mails that wait in the mailbox
     * @return how many still wait
     */
    public static int flush ( ) {
        if ( waiting.isEmpty ( ) || ! activateConnection ( ) ) {
            return waiting.size ( );
        }
        while ( ! waiting.isEmpty ( ) ) {
            Mail mail = waiting.get ( 0 );
            if ( insert ( mail ) == 1 ) {
                WorkGuard.done ( mail );
                waiting.remove ( 0 );
            } else if ( WorkGuard.failed ( mail, "mail to the mailbox" ) ) {
                waiting.remove ( 0 );
            } else {
                break; /* the database said no, try again next time */
            }
        }
        return waiting.size ( );
    }

    private static int insert ( Mail mail )  { 
        try {
            String query = "INSERT INTO mailbox  ( mail,subject,body,stamp,status )  "
                         + "VALUES  ( ?, ?, ?, UNIX_TIMESTAMP ( ) , ? );";
            ps = sql.prepareStatement ( query );
            ps.setString   ( 1, mail.getTo ( )           );
            ps.setString   ( 2, mail.getSubject ( )      );
            ps.setString   ( 3, mail.getBody ( )         );
            ps.setInt      ( 4, 1                        );
            ps.execute ( );
            ps.close ( );

            idleUpdate ( "sendMail ( ) " );
        } catch  ( SQLException ex )  { 
            Proc.log ( MXDatabase.class.getName ( ) , ex );
            return -1;
        }
        /* mail was added */
        return 1;
    }
 
}
