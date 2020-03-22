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
  
    public static boolean addServicesBan ( ServicesBan ban )  {
        String list = getListByHash ( ban.getType() );
        if ( ! activateConnection ( ) )  {
            return false;
        }
        try { 
            ban.printData();
            String query = "insert into "+list+" ( id,mask,reason,instater,stamp,expire ) VALUES "
                          +" ( ?, ?, ?, ?, ?, ? ) ";
            System.out.println(query);
            ps = sql.prepareStatement ( query );
            ps.setString ( 1, ban.getID ( ) );
            ps.setString ( 2, ban.getMask ( ) );
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

    public static void logServicesBan ( int command, ServicesBan ban ) {
        String id;
        String flag;
        if ( ! activateConnection ( ) )  {
            return;
        }
        
        switch ( ban.getType() ) {
            case AKILL :
                id = "AK"+ban.getID ( );
                flag = ( command == DEL ? "AK-" : "AK+" );
                break;
                
            case IGNORE :
                id = "IG"+ban.getID ( );
                flag = ( command == DEL ? "IG-" : "IG+" );
                break;
                
            case SQLINE :
                id = "SQ"+ban.getID ( );
                flag = ( command == DEL ? "SQ-" : "SQ+" );
                break;
                      
            case SGLINE :
                id = "SQ"+ban.getID ( );
                flag = ( command == DEL ? "SG-" : "SG+" );
                break;
                
            default :
                return;
        }
         
        try {
            String query = "insert into banlog ( ticket,flag,usermask,oper,stamp,data ) VALUES "
                          +" ( ?, ?, ?, ?, now(), ?) ";
            ps = sql.prepareStatement ( query );
            ps.setString   ( 1, id );
            ps.setString   ( 2, flag );
            ps.setString   ( 3, ban.getMask() );
            ps.setString   ( 4, ban.getInstater() );
            ps.setString   ( 5, ban.getReason() );
            ps.execute ( );
            ps.close ( );
 
            idleUpdate ( "addAkill ( ) " );
           
        } catch  ( SQLException ex )  {
            Proc.log ( OSDatabase.class.getName ( ) , ex );   
        } 
    }

   
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
            ps.setString  ( 1, ban.getID ( )  );
            ps.execute ( );
            ps.close ( );

            System.out.println ( "debug ( delServicesBan ( "+ban.getID ( ) +" )  ) " ); 
            idleUpdate ( "delServicesBan ( ) " );
            return true;
          
        } catch  ( SQLException ex )  {
            Proc.log ( OSDatabase.class.getName ( ) , ex );   
        } 
        return false;
    }
   
    public static ArrayList<ServicesBan> getServicesBans ( int hash )  {
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
                banList.add ( new ServicesBan ( hash, res2.getString ( "id" ), true, res2.getString ( "mask" ), 
                                                res2.getString ( "reason" ), res2.getString ( "instater" ), 
                                                res2.getString ( "stamp" ), res2.getString ( "expire" ) ) );
            }
            res2.close ( );
            ps.close ( );
            idleUpdate ( "getServicesBans ( ) " );
            
        } catch  ( SQLException ex )  {
            Proc.log ( OSDatabase.class.getName ( ) , ex );   
        } 
        return banList;
    }
    
    public static ArrayList<SpamFilter> getSpamFilters ( ) {
        ArrayList<SpamFilter> sfList = new ArrayList<>();
        String query;
        if ( ! activateConnection ( ) )  {
            return sfList;
        }
        try {
            query = "select id,pattern,flags,instater,reason,stamp,expire "+
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
                        res2.getString ( "stamp" ),
                        res2.getString ( "expire" )
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
        String tType = ticket.toUpperCase().substring ( 0, 2 );
        int banType;
        int tID;
        try {
            tID = Integer.parseInt ( ticket.substring ( 2 ) );
        } catch ( NumberFormatException ex ) {
            Proc.log ( OSDatabase.class.getName ( ) , ex ); 
            return null;
        }

        switch ( tType.hashCode() ) {
            case AK :
                banType = AKILL;
                break;
                
            case IG :
                banType = IGNORE;
                break;
                  
            case SQ :
                banType = SQLINE;
                break;
                
            default :
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
                ban = new ServicesBan ( banType, res2.getString ( "id" ), true, res2.getString ( "mask" ), 
                                                 res2.getString ( "reason" ), res2.getString ( "instater" ), 
                                                 res2.getString ( "stamp" ), res2.getString ( "expire" ) );
            }
            res2.close ( );
            ps.close ( );
            
        } catch ( SQLException ex ) {
            Proc.log ( OSDatabase.class.getName ( ) , ex );
        }
        return ban;
    }

    public static ArrayList<ServicesBan> getExpiredBans ( int hash ) {
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
                banList.add ( new ServicesBan ( hash, res2.getString ( "id" ), true, res2.getString ( "mask" ),
                                                res2.getString ( "reason" ), res2.getString ( "instater" ),
                                                res2.getString ( "stamp" ) , res2.getString ( "expire" )  )  );
            }
            res2.close ( );
            ps.close ( );
            idleUpdate ( "getExpiredBans ( ) " );
         
        } catch  ( SQLException ex )  {
            Proc.log ( OSDatabase.class.getName ( ) , ex );   
        } 
        return banList;
    }
    
    
    public static String getListByHash ( int hash ) {
        switch ( hash ) {
            case AKILL :
                return "akill";
            
            case IGNORE :
                return "ignorelist";
                
            case SQLINE :
                return "sqline";
                      
            case SGLINE :
                return "sgline";
                
            default :
                return "";
        }
    }
    
    
    static ArrayList<OSLogEvent> getSearchLogList ( String target, boolean full ) {
        ArrayList<OSLogEvent> lsList = new ArrayList<>();
        String table;
        
        if ( ! activateConnection ( ) )  {
            return lsList;
        }
        
        if ( target.charAt (0) == '#' ) {
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
            ps.setString ( 1, target );
            res = ps.executeQuery ( );

            while ( res.next ( )  )  {
                lsList.add ( new OSLogEvent ( 
                        res.getString(1),
                        res.getString(2),
                        res.getString(3),
                        res.getString(4),
                        (res.getString(5)!=null?res.getString(5):null) )
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
    static ArrayList<OSSnoopLogEvent> getSnoopLogList ( String target, boolean full ) {
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
            ps.setString ( 1, target );
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
         
        String query = "delete from server "+
                       "where name = ?";
        try {
            ps = sql.prepareStatement ( query );
            ps.setString ( 1, server.getName() );
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

    public static boolean addServer ( NetServer server ) {
        if ( ! activateConnection ( ) ) {
            return false;
        }
        
        String query = "insert into server ( name ) "+
                       "values ( ? ) "+
                       "on duplicate key update name = ?";
        try {
            ps = sql.prepareStatement ( query );
            ps.setString ( 1, server.getName() );
            ps.setString ( 2, server.getName() );
            ps.execute();

            res2.close ( );
            ps.close ( );
            return true;
            
        } catch ( SQLException ex ) {
//            Proc.log ( OSDatabase.class.getName ( ) , ex );
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
            ps.setString ( 1, server.getPrimary() );
            ps.setString ( 2, server.getSecondary() );
            ps.setString ( 3, server.getName() );
            ps.execute();

            res2.close ( );
            ps.close ( );
            return true;
            
        } catch ( SQLException ex ) {
//            Proc.log ( OSDatabase.class.getName ( ) , ex );
            return false;
        }
    }
 
    
    
    static boolean serverExistInList ( String name ) {
        boolean found;
        if ( ! activateConnection ( ) ) {
            return false;
        }
        
        String query = "select name "+
                       "from server "+
                       "where name = ?";
        
        try {   
            ps = sql.prepareStatement ( query );
            ps.setString ( 1, name );
            res = ps.executeQuery ( );
            found = res.next();
            res2.close ( );
            ps.close ( );
            
        } catch ( Exception ex ) {
            Proc.log ( OSDatabase.class.getName ( ) , ex );
            found = false;
        }
        return found;
    }
 
    static ArrayList<Comment> getCommentList ( String target, boolean full ) {
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
            ps.setString ( 1, target );
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
                log = new OSLogEvent ( res.getString(1), res.getString(2),
                                       res.getString(3), res.getString(4),
                                       res.getString(5) );
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
                log = new OSLogEvent ( res.getString(1), res.getString(2),
                                       res.getString(3), res.getString(4),
                                       res.getString(5) );
                log.setData ( res.getString ( 6 ) ); 
                lsList.add ( log );
            }
            
        } catch ( SQLException ex ) {
            Proc.log ( OSDatabase.class.getName ( ), ex );
        }
        
        return lsList;
    }

    static public boolean addStaff ( Oper oper ) {
        if ( ! activateConnection ( ) ) {
            return false;
        }
        try {
            String query = "INSERT INTO oper ( name,access,instater ) VALUES ( ?, ?, ? ) "
                          +"ON DUPLICATE KEY UPDATE access = ?,instater = ?;";
            ps = sql.prepareStatement ( query );
            ps.setString   ( 1, oper.getName ( ) );
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
    
    public static boolean delStaff ( Oper oper )  {
        if ( ! activateConnection ( ) ) {
            return false;
        }
        try {
            String query = "DELETE FROM oper WHERE name = ? AND access = ?;";
            ps = sql.prepareStatement ( query );
            ps.setString   ( 1, oper.getName ( ) );
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
            ps.setString   ( 3, comment.getComment ( ) );
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
                "(id,pattern,flags,instater,reason,stamp,expire) "+
                "values (?,?,?,?,?,?,?)";
        
        try {
            ps = sql.prepareStatement ( query );
            ps.setLong     ( 1, sf.getID ( ) );
            ps.setString   ( 2, sf.getPattern ( ) );
            ps.setString   ( 3, sf.getFlags ( ) );
            ps.setString   ( 4, sf.getInstater ( ) );
            ps.setString   ( 5, sf.getReason ( ) );
            ps.setString   ( 6, sf.getStamp ( ) );
            ps.setString   ( 7, sf.getExpire ( ) );
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
    static public int logEvent ( OSLogEvent log ) {
        int id = Database.logEvent( "operlog", log );
        return id;
    }
    static public void delLogEvent ( int id ) {
        Database.delLogEvent( "operlog", id );
    }

 
}