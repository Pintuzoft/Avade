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
package user;

import operserv.*;
import core.Database;
import core.Proc;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author DreamHealer
 */
public class HostDatabase extends Database {
    private static Statement        s;
    private static ResultSet        res;
    private static ResultSet        res2;

    public static String getHost ( String ip )  {
        if ( ! activateConnection ( ) ) {
            return null;
        }
        try { 
            String query = "SELECT host "
                         + "FROM host "
                         + "WHERE ip = ? "
                         + "LIMIT 1;";
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, ip );
            res = ps.executeQuery ( );

            if ( res.next ( )  )  {
                System.out.println ( "Record found" );
                String buf = res.getString ( "host" );
                res.close ( );
                ps.close ( );                    
                return buf;

            } else {
                System.out.println ( "Record not found" );
            }
            res.close ( );
            ps.close ( );   
            idleUpdate ( "getHost ( ) " );         

        } catch  ( SQLException ex )  {
            Proc.log ( OSDatabase.class.getName ( ) , ex );   
        } 
        return null;
    }

    /* add a new host into the database. if it already exists just update it */
    public static void addHost ( String ip, String host )  {
        if ( ! activateConnection ( )  )  {
            return;
        }
        try {
            String query = "INSERT INTO host  ( ip, host, stamp )  "
                         + "VALUES  ( ?, ?, UNIX_TIMESTAMP ( )  )  "
                         + "ON DUPLICATE KEY "
                         + "UPDATE host = ?, stamp = UNIX_TIMESTAMP ( );";
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, ip );
            ps.setString  ( 2, host );
            ps.setString  ( 3, host );
            ps.execute ( );
            ps.close ( ); 
            idleUpdate ( "addHost ( ) " );         

        } catch  ( SQLException ex )  {
            Proc.log ( OSDatabase.class.getName ( ) , ex );   
        }
    }
}