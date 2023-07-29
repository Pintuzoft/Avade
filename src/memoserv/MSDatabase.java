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
package memoserv;

import nickserv.NSDatabase;
import nickserv.NickServ;
import nickserv.NickInfo;
import core.Database;
import core.Handler;
import core.HashString;
import core.Proc;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author DreamHealer
 */

public class MSDatabase extends Database {
    private static Statement            s;
    private static ResultSet            res;
    private static ResultSet            res2;
    private static PreparedStatement    ps;

    /* NickServ Methods */
    public static MemoInfo storeMemo ( MemoInfo memo )  {
        if ( ! activateConnection ( )  )  {
            return null;
        }
        
        try {
            String query = "INSERT INTO memo  ( name,sender,message,stamp,readflag )  "
                         + "VALUES  ( ?, ?, ?, UNIX_TIMESTAMP ( ) , ? ) ";

            ps = sql.prepareStatement ( query );
            ps.setString   ( 1, memo.getName ( )  );
            ps.setString   ( 2, memo.getSender ( )  );
            ps.setString   ( 3, memo.getMessage ( )  );
            ps.setInt      ( 4, memo.isRead ( ) ?1:0 );
            ps.execute ( );
            ps.close ( );

            query = "SELECT id,stamp "
                  + "FROM memo "
                  + "WHERE id=last_insert_id ( );";
            ps = sql.prepareStatement ( query );
            res = ps.executeQuery ( );

            if ( res.next ( )  )  {
                memo.setID ( res.getInt ( "id" )  );
                memo.setStamp ( res.getInt ( "stamp" )  );
            }
            res.close ( );
            ps.close ( );
            idleUpdate ( "storeMemo ( ) " );
        } catch  ( SQLException ex )  {
            /* Nick already exists? return -1 */
            Proc.log ( NSDatabase.class.getName ( ) , ex );
            return null;
        }
        return memo;
    }
     

    public static ArrayList<MemoInfo> getMemosByNick ( HashString nick )  {
        ArrayList<MemoInfo> mList = new ArrayList<> ( );
        if ( ! activateConnection ( )  )  {
            return mList;
        }
        try {
            String query = "SELECT id,name,sender,message,stamp,readflag "
                         + "FROM memo "
                         + "WHERE name = ?;";
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, nick.getString() );
            res = ps.executeQuery ( );

            while ( res.next ( )  )  { 
                mList.add ( new MemoInfo ( res.getInt ( "id" ) ,res.getString ( "name" ) ,res.getString ( "sender" ) ,res.getString ( "message" ) ,res.getLong ( "stamp" ) ,res.getBoolean ( "readflag" )  )  );
            }
            res.close ( );
            ps.close ( );
            idleUpdate ( "getMemosByNick ( ) " );
        } catch  ( SQLException ex )  {
            Proc.log ( NSDatabase.class.getName ( ) , ex );
        }
        return mList;
    }
    
    public static void loadAllMemos ( )  {
        ArrayList<MemoInfo> mList = new ArrayList<> ( );
        NickInfo ni;
        if ( ! activateConnection ( )  )  {
            return;
        }
        try {
            String query = "SELECT id,name,sender,message,stamp,readflag FROM memo order by stamp;";
            ps = sql.prepareStatement ( query );
            res = ps.executeQuery ( );

            while ( res.next ( )  )  {
                Proc.log("0:");
                if ( (ni = NickServ.findNick(res.getString("name"))) != null ) {
                Proc.log("1:"+res.getString("name")+":"+res.getString("message"));
                    ni.getMemos().add ( new MemoInfo ( res.getInt ( "id" ) ,res.getString ( "name" ) ,res.getString ( "sender" ) ,res.getString ( "message" ) ,res.getLong ( "stamp" ) ,res.getBoolean ( "readflag" )  )  );
                Proc.log("2:"+ni.getNameStr()+":"+ni.getMemos().size());
                }
            }
            res.close ( );
            ps.close ( );
            idleUpdate ( "getMemosByNick ( ) " );
        } catch  ( SQLException ex )  {
            Proc.log ( NSDatabase.class.getName ( ) , ex );
        }
        return;
    }

   
    public static boolean delMemo ( MemoInfo memo )  {
        if ( ! activateConnection ( ) )  { 
            return false;
        }
        try {
            String query = "DELETE FROM memo "
                         + "WHERE id = ?";
            ps = sql.prepareStatement ( query );
            ps.setInt  ( 1, memo.getID ( )  );
            ps.execute ( );
            ps.close ( );

            idleUpdate ( "delMemo ( ) " ); 
            return true;
        } catch  ( SQLException ex )  {
            Proc.log ( NSDatabase.class.getName ( ) , ex );
        }
        return false;
    }
    
    public static boolean readMemo ( MemoInfo memo )  {
        if ( memo == null || ! activateConnection ( ) )  {
            return false;
        } 
        try {
            String query = "UPDATE memo "
                         + "SET readflag = ? "
                         + "WHERE id = ?;";
            ps = sql.prepareStatement ( query );
            ps.setInt      ( 1, 1 );
            ps.setInt      ( 2, memo.getID ( )  );
            ps.execute ( );
            ps.close ( );

            idleUpdate ( "delMemo ( ) " );
            return true;
        } catch ( SQLException ex )  {
            Proc.log ( NSDatabase.class.getName ( ) , ex );
        }
        return false;
    }
}
 