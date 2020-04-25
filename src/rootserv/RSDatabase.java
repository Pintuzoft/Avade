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
package rootserv;

import operserv.*;
import core.Database;
import core.HashString;
import core.Proc;
import java.sql.PreparedStatement;
import nickserv.NickInfo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import nickserv.NickServ;

/**
 *
 * @author DreamHealer
 */
public class RSDatabase extends Database {
    private static Statement s;
    private static ResultSet res;
    private static ResultSet res2;

  /*
    mysql> select * from oper;                                                                                                                                                                                                                   
+-------------+--------+-------------+                                                                                                                                                                                                       
| name        | access | instater    |                                                                                                                                                                                                       
+-------------+--------+-------------+                                                                                                                                                                                                       
| DreamHealer |      4 | DreamHealer |                                                                                                                                                                                                       
| fredde      |      4 | DreamHealer |                                                                                                                                                                                                       
+-------------+--------+-------------+                                                                                                                                                                                                       
2 rows in set  ( 0.00 sec )        
    */
    public static boolean addSra ( NickInfo sra, NickInfo target )  {
        if ( ! activateConnection ( ) ) {
            return false;
        }
        try {
            int id;

            String query = "INSERT INTO oper  ( name, access, instater )  VALUES  ( ?, ?, ? )  "
                          +"ON DUPLICATE KEY UPDATE access = ?, instater = ?;"; 
            PreparedStatement preparedStmt = sql.prepareStatement ( query );
            preparedStmt.setString   ( 1, target.getNameStr()       );
            preparedStmt.setInt      ( 2, 4                         );
            preparedStmt.setString   ( 3, sra.getNameStr()          );                
            preparedStmt.setInt      ( 4, 4                         );
            preparedStmt.setString   ( 5, sra.getNameStr()          );
            preparedStmt.execute ( );
            preparedStmt.close ( );

            idleUpdate ( "addSra ( ) " );
         
        } catch  ( Exception ex )  {
            Proc.log ( OSDatabase.class.getName ( ) , ex );
            return false;
        } 
        return true;
    }
    
    public static boolean delSra ( NickInfo ni )  {
        if ( ! activateConnection ( )  )  {
            return false;
        }
        try {
            int id;
            
            String query = "DELETE FROM oper WHERE name = ?;";
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, ni.getNameStr()  );
            ps.execute ( );
            ps.close ( );
            
           
            
            idleUpdate ( "delSra ( ) " );
            return true;
        
        } catch  ( Exception ex )  {
            Proc.log ( OSDatabase.class.getName ( ) , ex );   
        } 
        return false;
    }

    static boolean isMaster ( HashString master ) {
        if ( ! activateConnection ( ) ) {
            return false;
        }
               
        try {            
            String query = "SELECT name FROM oper WHERE name = ? AND access = ?";
            PreparedStatement preparedStmt = sql.prepareStatement ( query );
            preparedStmt.setString  ( 1, master.getString() );
            preparedStmt.setInt     ( 2, 5  );
            res = preparedStmt.executeQuery ( );

            idleUpdate ( "isMaster ( ) " );

            if ( res.next ( ) )  {
                preparedStmt.close ( );
                return true;
            } 
            preparedStmt.close ( );
            return false;
        
        } catch  ( SQLException ex )  {
            Proc.log ( OSDatabase.class.getName ( ) , ex );   
        } 
        return false;
    }
    
    /* Set new master and return all old masters */
    static ArrayList<NickInfo> setMaster ( HashString master ) {
        ArrayList<NickInfo> nList = new ArrayList<>();
        
        if ( ! activateConnection ( ) ) {
            return nList;
        }
        try {            

            /* Get old master */
            String query = "select name from oper where access = 5";
            PreparedStatement preparedStmt = sql.prepareStatement ( query );
            res = preparedStmt.executeQuery ( );
            while ( res.next ( ) )  {
                nList.add ( NickServ.findNick ( res.getString ( 1 ) ) );
            }
            res.close ( );
            preparedStmt.close ( );
            
            
            /* Downgrade any old master to SRA */
            query = "UPDATE oper SET access = 4 WHERE access = 5";
            ps = sql.prepareStatement ( query );
            ps.execute ( );
            //preparedStmt.close ( );
            
            /* Set new master */
            query = "INSERT INTO oper ( name, access ) VALUES ( ?, ? ) "+
                    "ON DUPLICATE KEY UPDATE access = ?";
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, master.getString() );
            ps.setInt     ( 2, 5 );
            ps.setInt     ( 3, 5 );
            ps.execute ( );
            ps.close ( );
            
            idleUpdate ( "setMaster ( ) " );
            
        } catch  ( SQLException ex )  {
            Proc.log ( OSDatabase.class.getName ( ) , ex );
        } 
        return nList;
    }
}