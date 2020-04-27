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
package command;

import nickserv.NickInfo;
import nickserv.NickServ;
import chanserv.ChanInfo;
import core.Database;
import core.HashString;
import core.Proc;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

/**
 *
 * @author DreamHealer
 */
public class CMDDatabase extends Database {
    private static Statement            s;
    private static ResultSet            res;
    private static ResultSet            res2;
    private static PreparedStatement    preparedStmt;
 
    public static LinkedList<Command> getCommands ( )  {
        NickInfo ni;
        ChanInfo ci;
        Command command;
    
        LinkedList<Command> cList = new LinkedList<> ( );
        if ( ! activateConnection ( )  )  {
            return cList;
        }
        try {
            String query = "select id,target,targettype,command,extra,extra2 "
                         + "from command "
                         + "order by id ASC;";
            preparedStmt = sql.prepareStatement ( query );
            res = preparedStmt.executeQuery ( );

            while ( res.next ( ) ) {
                try {
                    HashString type = new HashString ( res.getString(3) ); 
                    
                    if ( type.is(NICKINFO) ) {
                        if ( ( ni = NickServ.findNick ( res.getString ( 2 ) ) ) != null ) {
                            cList.add ( 
                                new Command ( 
                                    res.getString ( 1 ), 
                                    ni, 
                                    type, 
                                    new HashString ( res.getString(4) ), 
                                    res.getString(5),
                                    res.getString(6)
                                )
                            );
                        }
                    }
                     
                } catch ( SQLException e ) {
                    Proc.log ( CMDDatabase.class.getName ( ), e );
                }    
            }  
            res.close ( );
            preparedStmt.close ( ); 
            idleUpdate ( "getCommands ( )" ); 
         
        } catch  ( SQLException ex )  {
            Proc.log ( CMDDatabase.class.getName ( ), ex );
        }
        System.out.println("debug: getCommands(end): "+cList.size());
        return cList;
    }
    
      
    public static void deleteCommand ( String id )  {
        if ( ! activateConnection ( )  )  {
            return;
        }
        try {
            String query = "delete from command "
                         + "where id = ?";
            preparedStmt = sql.prepareStatement ( query );
            preparedStmt.setString  ( 1, id );
            preparedStmt.execute ( );
            preparedStmt.close ( );

            idleUpdate ( "deleteCommand ( ) " );

        } catch  ( SQLException ex )  {
            Proc.log ( CMDDatabase.class.getName ( ) , ex );
        }
    }
  
}
