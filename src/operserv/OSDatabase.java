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

import core.Database;
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
public class OSDatabase extends Database {
    private static Statement            s;
    private static ResultSet            res;
    private static ResultSet            res2;
    private static PreparedStatement    ps;
 
    /**
     *
     * @return
     */
    public static ArrayList<Oper> getAllStaff ( )  {
        Oper oper;
        if ( ! activateConnection ( )  )  {
            return null;
        }
        ArrayList<Oper> staff = new ArrayList<>();
        try { 
            String query = "select name,access,instater "+
                           "from oper ";
                            
            ps = sql.prepareStatement ( query );
            res = ps.executeQuery ( );

            while ( res.next ( )  )  {
                oper = new Oper ( res.getString(1), res.getInt(2), res.getString(3) );
                staff.add ( oper );
            } 
            res.close ( );
            ps.close ( );
            idleUpdate ( "getOper ( ) " );
       
        } catch  ( SQLException ex )  {
            Proc.log ( OSDatabase.class.getName ( ) , ex );   
        }  
        return staff;
    }
 
    /* NickServ Methods */
    /*public boolean storeChan ( ChanInfo chan )  {


        return true;
    }

    public boolean updateChan ( ChanInfo chan )  {


        return true;
    }

    public boolean deleteChan ( ChanInfo chan )  {


        return true;
    }*/
    /* End NickServ */

    /**
     *
     * @param ban
     * @return
     */

  
    public static boolean addServicesBan ( ServicesBan ban )  {
        String list = getListByHash ( ban.getType() );
        if ( ! activateConnection ( ) )  {
            return false;
        }
        try { 
            ban.printData();
            String query = "insert into "+list+" ( id,mask,reason,instater,stamp,expire ) VALUES "
                          +" ( ?, ?, ?, ?, ?, ? );";
            System.out.println(query);
            ps = sql.prepareStatement ( query );
            ps.setString ( 1, ban.getID().getString() );
            ps.setString ( 2, ban.getMask().getString() );
            ps.setString ( 3, ban.getReason ( ) );
            ps.setString ( 4, ban.getInstater ( ) );
            ps.setString ( 5, ban.getTime ( ) );
            ps.setString ( 6, ban.getExpire ( ) );
            ps.execute ( );
            ps.close ( );
            idleUpdate ( "addAkill ( ) " );
           
        } catch  ( SQLException ex )  {
            Proc.log ( OSDatabase.class.getName ( ) , ex );
            return false;
        } 
        return true;
    }

    /**
     *
     * @param command
     * @param ban
     * @return 
     */
    public static boolean logServicesBan ( HashString command, ServicesBan ban ) {
        String id;
        String flag;
        if ( ! activateConnection ( ) )  {
            return false;
        }
        if ( ban.getType().is(AKILL) ) {
                id = "AK"+ban.getID ( );
                flag = ( command.is(DEL) ? "AK-" : "AK+" );            
        
        } else if ( ban.getType().is(IGNORE) ) {
                id = "IG"+ban.getID ( );
                flag = ( command.is(DEL) ? "IG-" : "IG+" );            
        
        } else if ( ban.getType().is(SQLINE) ) {
                id = "SQ"+ban.getID ( );
                flag = ( command.is(DEL) ? "SQ-" : "SQ+" );            
        
        } else if ( ban.getType().is(SGLINE) ) {
                id = "SQ"+ban.getID ( );
                flag = ( command.is(DEL) ? "SG-" : "SG+" );            
        
        } else {
            return false;
        }
        
        try {
            String query = "insert into banlog ( ticket,flag,usermask,oper,stamp,data ) VALUES "
                          +" ( ?, ?, ?, ?, now(), ?) ";
            ps = sql.prepareStatement ( query );
            ps.setString   ( 1, id );
            ps.setString   ( 2, flag );
            ps.setString   ( 3, ban.getMask().getString() );
            ps.setString   ( 4, ban.getInstater() );
            ps.setString   ( 5, ban.getReason() );
            ps.execute ( );
            ps.close ( );
 
            idleUpdate ( "addAkill ( ) " );
           
        } catch  ( SQLException ex )  {
            Proc.log ( OSDatabase.class.getName ( ) , ex );
            return false;
        } 
        return true;
    }

    /**
     *
     * @param ban
     * @return
     */
    public static boolean delServicesBan ( ServicesBan ban )  {
        String list = getListByHash ( ban.getType() );
        if ( ! activateConnection ( ) ) {
            return false;
        }
        try {
            int id; 
            String query = "DELETE FROM "+list+" "+
                           "WHERE id = ?";
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, ban.getID().getString()  );
            ps.execute ( );
            ps.close ( );

            idleUpdate ( "delServicesBan ( ) " );
            
        } catch  ( SQLException ex )  {
            Proc.log ( OSDatabase.class.getName ( ) , ex );   
            return false;
        } 
        return true;
    }
   
    /**
     *
     * @param hash
     * @return
     */
    public static ArrayList<ServicesBan> getServicesBans ( HashString hash )  {
        String list = getListByHash ( hash );
        ArrayList<ServicesBan> banList = new ArrayList<> ( );
        if ( ! activateConnection ( ) || list.length() == 0 )  {
            return banList;
        }
        try { 
            String query = "select id,mask,reason,instater,stamp,expire "+
                           "from "+list+" "+
                           "order by id desc";
            ps = sql.prepareStatement ( query );
            res2 = ps.executeQuery ( );
 
            while ( res2.next ( )  )  {
                banList.add ( new ServicesBan ( 
                        hash, 
                        new HashString ( res2.getString ( "id" ) ),
                        true,
                        new HashString ( res2.getString ( "mask" ) ),
                        res2.getString ( "reason" ),
                        res2.getString ( "instater" ),
                        res2.getString ( "stamp" ),
                        res2.getString ( "expire" ) 
                    )
                );
            }
            res2.close ( );
            ps.close ( );
            idleUpdate ( "getServicesBans ( ) " );
            
        } catch  ( SQLException ex )  {
            Proc.log ( OSDatabase.class.getName ( ) , ex );   
        } 
        return banList;
    }
    
    /**
     *
     * @return
     */
    public static ArrayList<SpamFilter> getSpamFilters ( ) {
        ArrayList<SpamFilter> sfList = new ArrayList<>();
        String query;
        if ( ! activateConnection ( ) )  {
            return sfList;
        }
        try {
            query = "select id,pattern,flags,instater,reason,stamp "+
                    "from spamfilter "+
                    "order by pattern asc";
            ps = sql.prepareStatement ( query );
            res2 = ps.executeQuery ( );
            
            while ( res2.next ( ) ) {
                sfList.add (
                    new SpamFilter (
                        res2.getLong ( "id" ),
                        res2.getString ( "pattern" ),
                        res2.getString ( "flags" ),
                        res2.getString ( "instater" ),
                        res2.getString ( "reason" ),
                        res2.getString ( "stamp" )
                    )
                );
            }
            res2.close ( );
            ps.close ( );
            
        } catch ( SQLException ex ) {
            Proc.log ( OSDatabase.class.getName ( ), ex );
        }
        return sfList;
    }

    static ServicesBan getServicesBanByTicket ( String ticket ) {
        ServicesBan ban = null;
        HashString type = new HashString ( ticket.toUpperCase().substring ( 0, 2 ) );
        HashString banType;
        int tID;
        try {
            tID = Integer.parseInt ( ticket.substring ( 2 ) );
        } catch ( NumberFormatException ex ) {
            Proc.log ( OSDatabase.class.getName ( ) , ex ); 
            return null;
        }

        if ( type.is(AK) ) {
            banType = AKILL;
        } else if ( type.is(IG) ) {
            banType = IGNORE;
        } else if ( type.is(SQ) ) {
            banType = SQLINE;
        } else {
            return null;
        }
         
        String list = getListByHash ( banType );

        try { 
            String query = "select id,mask,reason,instater,stamp,expire "+
                           "from "+list+" "+
                           "where id = ?";
            
            ps = sql.prepareStatement ( query );
            ps.setInt ( 1, tID );
            res2 = ps.executeQuery ( );
            if ( res2.next ( ) ) {
                ban = new ServicesBan ( 
                    banType, 
                    new HashString ( res2.getString ( "id" ) ), 
                    true, 
                    new HashString ( res2.getString ( "mask" ) ), 
                    res2.getString ( "reason" ), 
                    res2.getString ( "instater" ), 
                    res2.getString ( "stamp" ), 
                    res2.getString ( "expire" ) 
                );
            }
            res2.close ( );
            ps.close ( );
            
        } catch ( SQLException ex ) {
            Proc.log ( OSDatabase.class.getName ( ) , ex );
        }
        return ban;
    }

    /**
     *
     * @param hash
     * @return
     */
    public static ArrayList<ServicesBan> getExpiredBans ( HashString hash ) {
        String list = getListByHash ( hash );
        ArrayList<ServicesBan> banList = new ArrayList<> ( ); 
        if ( ! activateConnection ( ) || list.length() == 0 )  {
            return banList;
        }
        try {
            String query = "select id,mask,reason,instater,stamp,expire "+
                           "from "+list+" "+
                           "where expire < now() "+
                           "order by id asc";
            ps = sql.prepareStatement ( query );
            res2 = ps.executeQuery ( );

            while ( res2.next ( )  )  {
                banList.add ( 
                    new ServicesBan ( 
                        hash, 
                        new HashString ( res2.getString ( "id" ) ), 
                        true, 
                        new HashString ( res2.getString ( "mask" ) ),
                        res2.getString ( "reason" ), 
                        res2.getString ( "instater" ),
                        res2.getString ( "stamp" ),
                        res2.getString ( "expire" )  
                    ) 
                );
            }
            res2.close ( );
            ps.close ( );
            idleUpdate ( "getExpiredBans ( ) " );
         
        } catch  ( SQLException ex )  {
            Proc.log ( OSDatabase.class.getName ( ) , ex );   
        } 
        return banList;
    }
    
    /**
     *
     * @param hash
     * @return
     */
    public static String getListByHash ( HashString hash ) {
        if ( hash.is(AKILL) )               { return "akill";       } 
        else if ( hash.is(IGNORE) )         { return "ignorelist";  }
        else if ( hash.is(SQLINE) )         { return "sqline";      }
        else if ( hash.is(SGLINE) )         { return "sgline";      } 
        else {
            return "";
        } 
    }
    
    
    static ArrayList<OSLogEvent> getSearchLogList ( HashString target, boolean full ) {
        ArrayList<OSLogEvent> lsList = new ArrayList<>();
        String table;
        
        if ( ! activateConnection ( ) )  {
            return lsList;
        }
        
        if ( target.getString().charAt (0) == '#' ) {
            table = "chanlog";
        } else {
            table = "nicklog";
        }
        
        try {
            String query = "select name,flag,usermask,oper,stamp "+
                           "from "+table+" "+
                           "where name = ? "+
                           ( ! full ? "and stamp > now() - interval 1 year " : "" )+
                           "order by stamp asc;";
            ps = sql.prepareStatement ( query );
            ps.setString ( 1, target.getString() );
            res = ps.executeQuery ( );

            while ( res.next ( )  )  {
                lsList.add ( 
                    new OSLogEvent ( 
                        new HashString ( res.getString(1) ),
                        new HashString ( res.getString(2) ),
                        res.getString(3),
                        res.getString(4),
                        (res.getString(5)!=null?res.getString(5):null) 
                    )
                );
            }
            res2.close ( );
            ps.close ( );
            idleUpdate ( "getLogSearchList ( ) " );
         
        } catch  ( SQLException ex )  {
            Proc.log ( OSDatabase.class.getName ( ) , ex );   
        } 
        return lsList;
    }
    static ArrayList<OSSnoopLogEvent> getSnoopLogList ( HashString target, boolean full ) {
        ArrayList<OSSnoopLogEvent> lsList = new ArrayList<>();
        String table;
        
        if ( ! activateConnection ( ) )  {
            return lsList;
        }
        
        try {
            String query = "select target,body,stamp "+
                           "from log "+
                           "where target = ? "+
                           ( ! full ? "and stamp > now() - interval 1 year " : "" )+
                           "order by stamp asc;";
            ps = sql.prepareStatement ( query );
            ps.setString ( 1, target.getString() );
            res = ps.executeQuery ( );

            while ( res.next ( )  )  {
                lsList.add ( new OSSnoopLogEvent ( 
                    res.getString(1),
                    res.getString(2),
                    (res.getString(3)!=null?res.getString(3):null) )
                );
            }
            res2.close ( );
            ps.close ( );
            idleUpdate ( "getLogSearchList ( ) " );
         
        } catch  ( SQLException ex )  {
            Proc.log ( OSDatabase.class.getName ( ) , ex );   
        } 
        return lsList;
    }

    static ArrayList<NetServer> getServerList ( ) {
        ArrayList<NetServer> sList = new ArrayList<>();
        if ( ! activateConnection ( ) ) {
            return sList;
        }   
        String query;
        try {
            query = "select name,primaryhub,secondaryhub "+
                    "from server "+
                    "order by name asc";                
           
            ps = sql.prepareStatement ( query );
            res = ps.executeQuery ( );
            NetServer server;
            while ( res.next ( ) ) {
                server = new NetServer ( res.getString ( 1 ),
                                         res.getString ( 2 ),
                                         res.getString ( 3 ) );
                sList.add ( server );
            }
            res2.close ( );
            ps.close ( );
            
        } catch ( SQLException ex ) {
            Proc.log ( OSDatabase.class.getName ( ) , ex );
        }
        return sList;
    }
     
    static boolean delServer ( NetServer server ) {
        boolean deleted;
        if ( ! activateConnection ( ) || ! serverExistInList ( server.getName() ) ) {
            return false;
        }
         
        String query = "delete from server where name = ?";
        try {
            ps = sql.prepareStatement ( query );
            ps.setString ( 1, server.getNameStr() );
            ps.execute ( );
            res2.close ( );
            ps.close ( );
            deleted = true;
            
        } catch ( SQLException ex ) {
            Proc.log ( OSDatabase.class.getName ( ) , ex );
            deleted = false;
        }
        
        return deleted;
    }

    /**
     *
     * @param server
     * @return
     */
    public static boolean addServer ( NetServer server ) {
        if ( ! activateConnection ( ) ) {
            return false;
        }
        
        String query = "insert into server ( name ) "+
                       "values ( ? ) "+
                       "on duplicate key update name = ?";
        try {
            ps = sql.prepareStatement ( query );
            ps.setString ( 1, server.getNameStr() );
            ps.setString ( 2, server.getNameStr() );
            ps.execute();

            res2.close ( );
            ps.close ( );
            return true;
            
        } catch ( SQLException ex ) {
            return false;
        }
    }
    
    static boolean updServer ( NetServer server ) {
        if ( ! activateConnection ( ) ) {
            return false;
        }
        String query = "update server "+
                       "set primaryhub = ?,secondaryhub = ? "+
                       "where name = ?";
    
        try {
            ps = sql.prepareStatement ( query );
            ps.setString ( 1, server.getPrimaryStr() );
            ps.setString ( 2, server.getSecondaryStr() );
            ps.setString ( 3, server.getNameStr() );
            ps.execute();

            res2.close ( );
            ps.close ( );
            return true;
            
        } catch ( SQLException ex ) {
            return false;
        }
    }
 
    
    
    static boolean serverExistInList ( HashString name ) {
        boolean found;
        if ( ! activateConnection ( ) ) {
            return false;
        }
        
        String query = "select name "+
                       "from server "+
                       "where name = ?";
        
        try {   
            ps = sql.prepareStatement ( query );
            ps.setString ( 1, name.getString() );
            res = ps.executeQuery ( );
            found = res.next();
            res2.close ( );
            ps.close ( );
            
        } catch ( SQLException ex ) {
            Proc.log ( OSDatabase.class.getName ( ) , ex );
            found = false;
        }
        return found;
    }
 
    static ArrayList<Comment> getCommentList ( HashString target, boolean full ) {
        ArrayList<Comment> cList = new ArrayList<>();
        Comment comment;
        String query;
        
        if ( ! activateConnection ( ) )  {
            return cList;
        }
        
        query = "select name,instater,comment,stamp "+
                "from comment "+
                "where name = ? "+
                ( ! full ? "and stamp > now() - interval 1 year " : "")+
                "order by stamp asc";
         
        try {
            ps = sql.prepareStatement ( query );
            ps.setString ( 1, target.getString() );
            res = ps.executeQuery ( );
         
            while ( res.next ( )  )  {
                comment = new Comment ( res.getString(1), res.getString(2), res.getString(3), res.getString(4) );
                cList.add ( comment );
            }
            res2.close ( );
            ps.close ( );
            idleUpdate ( "getCommentList ( ) " );

        } catch  ( SQLException ex )  {
            Proc.log ( OSDatabase.class.getName ( ) , ex );   
        } 
        return cList;
    }
    
    static ArrayList<OSLogEvent> getAuditList ( String target ) {
        ArrayList<OSLogEvent> lsList = new ArrayList<>();
        OSLogEvent log;
        String query;
        
        if ( ! activateConnection ( ) )  {
            return lsList;
        }
        System.out.println("OSDatabase: "+target);
        try {
            if ( target == null || target.length() < 1 ) {
                query = "select name,flag,usermask,oper,stamp,null as global from nicklog "+
                        "where stamp > now() - interval 1 year "+
                        "and oper is not null "+
                        "union all "+
                        "select name,flag,usermask,oper,stamp,null as global from chanlog "+
                        "where stamp > now() - interval 1 year "+
                        "and oper is not null "+
                        "union all "+
                        "select name,flag,usermask,oper,stamp,data from operlog "+
                        "where stamp > now() - interval 1 year "+
                        "order by stamp asc;";
                ps = sql.prepareStatement ( query );

            } else {
                query = "select name,flag,usermask,oper,stamp,null as global from nicklog "+
                        "where oper = ? "+
                        "union all "+
                        "select name,flag,usermask,oper,stamp,null as global from chanlog "+
                        "where oper = ? "+
                        "union all "+
                        "select name,flag,usermask,oper,stamp,data from operlog "+
                        "where name = ? "+
                        "or oper = ? "+
                        "order by stamp asc;";
                ps = sql.prepareStatement ( query );
                ps.setString ( 1, target );
                ps.setString ( 2, target );
                ps.setString ( 3, target );
                ps.setString ( 4, target );
            }
            res = ps.executeQuery ( );
            while ( res.next ( )  )  {
                log = new OSLogEvent ( 
                    new HashString ( res.getString(1) ), 
                    new HashString ( res.getString(2) ),
                    res.getString(3), 
                    res.getString(4),
                    res.getString(5) 
                );
                log.setData ( res.getString ( 6 ) ); 
                lsList.add ( log );
            }
            res.close ( );
            ps.close ( );
            idleUpdate ( "getAuditList ( ) " );

        } catch  ( SQLException ex )  {
            Proc.log ( OSDatabase.class.getName ( ) , ex );   
        } 
        return lsList;
    }
    
    static ArrayList<OSLogEvent> getBanLogList ( String target ) {
        ArrayList<OSLogEvent> lsList = new ArrayList<>();
        OSLogEvent log;
        String query;
        
        if ( ! activateConnection ( ) )  {
            return lsList;
        }
        
        try {
            if ( target == null || target.length() < 1 ) {
                query = "select ticket,flag,usermask,oper,stamp,data "+
                        "from banlog "+
                        "where stamp > now() - interval 1 year "+
                        "order by stamp asc";
                ps = sql.prepareStatement ( query );
                
            } else {
                query = "select ticket,flag,replace ( usermask,'%','*' ) as usermask,oper,stamp,data "+
                        "from banlog "+
                        "where ( oper = ? or "+
                                "usermask = replace(?,'*','%') or "+
                                "replace(?,'*','%') = usermask ) or "+
                                "ticket = ? "+
                        "and stamp > now() - interval 1 year "+
                        "order by stamp asc";
                ps = sql.prepareStatement ( query );
                ps.setString ( 1, target );
                ps.setString ( 2, target );
                ps.setString ( 3, target );
                ps.setString ( 4, target );
            }
            res = ps.executeQuery ( );
            while ( res.next ( ) ) {
                log = new OSLogEvent ( 
                    new HashString ( res.getString(1) ), 
                    new HashString ( res.getString(2) ),
                    res.getString(3), 
                    res.getString(4),
                    res.getString(5) 
                );
                log.setData ( res.getString ( 6 ) ); 
                lsList.add ( log );
            }
            
        } catch ( SQLException ex ) {
            Proc.log ( OSDatabase.class.getName ( ), ex );
        }
        
        return lsList;
    }

    /**
     *
     * @param oper
     * @return
     */
    public static boolean addStaff ( Oper oper ) {
        if ( ! activateConnection ( ) ) {
            return false;
        }
        try {
            String query = "INSERT INTO oper ( name,access,instater ) VALUES ( ?, ?, ? ) "
                          +"ON DUPLICATE KEY UPDATE access = ?,instater = ?;";
            ps = sql.prepareStatement ( query );
            ps.setString   ( 1, oper.getNameStr( ) );
            ps.setInt      ( 2, oper.getAccess ( ) );
            ps.setString   ( 3, oper.getString ( INSTATER ) );
            ps.setInt      ( 4, oper.getAccess ( ) );
            ps.setString   ( 5, oper.getString ( INSTATER ) );
            ps.execute ( );
            ps.close ( );
            idleUpdate ( "addStaff ( ) " );
        
        } catch  ( SQLException ex )  {
            Proc.log ( OSDatabase.class.getName ( ) , ex );
            return false;
        } 
        return true;
    }
    
    /**
     *
     * @param oper
     * @return
     */
    public static boolean delStaff ( Oper oper )  {
        if ( ! activateConnection ( ) ) {
            return false;
        }
        try {
            String query = "DELETE FROM oper WHERE name = ? AND access = ?;";
            ps = sql.prepareStatement ( query );
            ps.setString   ( 1, oper.getNameStr ( ) );
            ps.setInt      ( 2, oper.getAccess ( ) );
            ps.execute ( );
            ps.close ( );
            idleUpdate ( "delStaff ( ) " );
                     
        } catch ( SQLException ex ) {
            Proc.log ( OSDatabase.class.getName ( ), ex );
            return false;
        } 
        return true;
    }
    
    static boolean addComment(Comment comment) {
        String query;
        if ( ! activateConnection ( )  )  {
            return false;
        }
        query = "insert into comment (name,instater,comment,stamp) "+
                "values (?,?,?,now())";
        
        try {
            ps = sql.prepareStatement ( query );
            ps.setString   ( 1, comment.getName ( ) );
            ps.setString   ( 2, comment.getInstater ( ) );
            ps.setString   ( 3, comment.getCommentStr ( ) );
            ps.execute ( );
            ps.close ( );
        } catch ( SQLException ex ) {
            Proc.log ( OSDatabase.class.getName ( ) , ex );
            return false;
        }
        return true;
    }
    /*
    +----------+--------------+------+-----+---------+----------------+
    | Field    | Type         | Null | Key | Default | Extra          |
    +----------+--------------+------+-----+---------+----------------+
    | id       | int(11)      | NO   | PRI | NULL    | auto_increment |
    | pattern  | varchar(128) | YES  |     | NULL    |                |
    | flags    | varchar(32)  | YES  |     | NULL    |                |
    | reason   | varchar(256) | YES  |     | NULL    |                |
    | instater | varchar(33)  | YES  |     | NULL    |                |
    | stamp    | datetime     | YES  |     | NULL    |                |
    | expire   | datetime     | YES  |     | NULL    |                |
    +----------+--------------+------+-----+---------+----------------+

    */
    static boolean addSpamFilter ( SpamFilter sf ) {
        String query;
        if ( ! activateConnection() ) {
            return false;
        }
        query = "insert into spamfilter "+
                "(id,pattern,flags,instater,reason,stamp) "+
                "values (?,?,?,?,?,?)";
        
        try {
            ps = sql.prepareStatement ( query );
            ps.setLong     ( 1, sf.getID ( ) );
            ps.setString   ( 2, sf.getPattern().getString() );
            ps.setString   ( 3, sf.getFlags ( ) );
            ps.setString   ( 4, sf.getInstater ( ) );
            ps.setString   ( 5, sf.getReason ( ) );
            ps.setString   ( 6, sf.getStamp ( ) );
            ps.execute ( );
            ps.close ( );
            
        } catch ( SQLException ex ) {
            Proc.log ( OSDatabase.class.getName ( ) , ex );
            return false;
        }
        return true;
    }
 
    static boolean remSpamFilter ( SpamFilter sf ) {
        String query;
        if ( ! activateConnection() ) {
            return false;
        }
        query = "delete from spamfilter "+
                "where id = ? ";
        
        try {
            ps = sql.prepareStatement ( query );
            ps.setLong ( 1, sf.getID ( ) );
            ps.execute ( );
            ps.close ( );
            
        } catch ( SQLException ex ) {
            Proc.log ( OSDatabase.class.getName ( ) , ex );
            return false;
        }
        return true;
    }
    
    
/*    static private void logGlobal(int id, OSLogEvent log) {
        String query;
        if ( ! activateConnection ( )  )  {
            return;
        }
        query = "insert into globallog ( id, global ) "+
                "values ( ?, ?)";
        
        try {
            ps = sql.prepareStatement ( query );
            ps.setInt       ( 1, id );
            ps.setString    ( 2, log.getGlobal() );
            ps.execute ( );
            ps.close ( );
        } catch ( SQLException ex ) {
            Proc.log ( OSDatabase.class.getName ( ) , ex );
        }
    }
  */   
  
    /* LOG EVENT */

    /**
     *
     * @param log
     * @return
     */

    public static int logEvent ( OSLogEvent log ) {
        int id = Database.logEvent( "operlog", log );
        return id;
    }

    /**
     *
     * @param id
     */
    public static void delLogEvent ( int id ) {
        Database.delLogEvent( "operlog", id );
    }

 
}