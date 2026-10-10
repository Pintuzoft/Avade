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
import java.util.LinkedList;

/**
 *
 * @author DreamHealer
 */
public class CMDDatabase extends Database {
    private static ResultSet            res;
    private static PreparedStatement    preparedStmt;
 
    /**
     *
     * @return
     */
    public static LinkedList<Command> getCommands ( )  {
        NickInfo ni;
        ChanInfo ci;
        Command command;
    
        LinkedList<Command> cList = new LinkedList<> ( );
        LinkedList<String> unusable = new LinkedList<> ( );
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
               
                HashString type = new HashString ( res.getString(3) ); 

                if ( type.is(NICKINFO) && res.getString ( 2 ) != null &&
                     ( ni = NickServ.findNick ( res.getString ( 2 ) ) ) != null ) {
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
                } else {
                    /* Nothing we can do with it (no such nick, an empty
                       field): it would be read again every five seconds */
                    unusable.add ( res.getString ( 1 ) );
                }
                
            }  
            res.close ( );
            preparedStmt.close ( ); 
            idleUpdate ( "getCommands ( )" ); 
            for ( String id : unusable ) {
                Proc.log ( "Web command "+id+" names no registered nick, removed" );
                deleteCommand ( id );
            }
         
        } catch  ( SQLException ex )  {
            Proc.log ( CMDDatabase.class.getName ( ), ex );
        }
        return cList;
    }
    
    /**
     *
     * @param id
     */
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
