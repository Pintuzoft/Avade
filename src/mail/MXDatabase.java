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
package mail;

import core.Database;
import core.Proc;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author DreamHealer
 */
public class MXDatabase extends Database {
    private static Statement s;
    private static ResultSet res;
    private static ResultSet res2;
    
    /* NickServ Methods */
    public static int sendMail ( Mail mail )  { 
        if ( ! activateConnection ( ) )  {
            return -2;
        }
        /* Try add the nick */
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
