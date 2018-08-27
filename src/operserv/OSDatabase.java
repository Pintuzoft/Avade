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
import core.Handler;
import core.Proc;
import java.sql.PreparedStatement;
import nickserv.NickInfo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import server.Server;
import user.User;

/**
 *
 * @author DreamHealer
 */
public class OSDatabase extends Database {
    private static Statement            s;
    private static ResultSet            res;
    private static ResultSet            res2;
    private static PreparedStatement    ps;

    public static Oper getOper ( String nick )  {
        Oper oper = new Oper ( );
        if ( ! activateConnection ( )  )  {
            return null;
        }
        try { 
            String query = "SELECT name,access,instater FROM oper WHERE name = ? LIMIT 1;";
            ps = sql.prepareStatement ( query );
            ps.setString  ( 1, nick );
            res = ps.executeQuery ( );

            if ( res.next ( )  )  {
                oper = new Oper ( res );
                res.close ( );
                ps.close ( );
              
            } else {
                res.close ( );
                ps.close ( );
            }
            idleUpdate ( "getOper ( ) " );
       
        } catch  ( SQLException ex )  {
            Proc.log ( OSDatabase.class.getName ( ) , ex );   
        }  
        return oper;
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
 
    public static ArrayList<Oper> getStaffByAccess ( int access )  {
        ArrayList<Oper> oList = new ArrayList<> ( );
        if ( ! activateConnection ( ) )  {
            return oList;
        }
        try {
            String query = "SELECT name,instater FROM oper WHERE access = ?;";
            ps = sql.prepareStatement ( query );
            ps.setInt  ( 1, access );
            res2 = ps.executeQuery ( );

            while ( res2.next ( ) ) {
                oList.add ( new Oper ( res2.getString ( "name" ) , access, res2.getString ( "instater" )  )  );
            }
            res2.close ( );
            ps.close ( );
            idleUpdate ( "getStaffByAccess ( ) " );
            
         } catch  ( SQLException ex )  {
            Proc.log ( OSDatabase.class.getName ( ) , ex );   
        } 
        return oList;
    }
/* mysql> desc ban;
+----------+--------------+------+-----+---------+----------------+
| Field    | Type         | Null | Key | Default | Extra          |
+----------+--------------+------+-----+---------+----------------+
| id       | int ( 11 )       | NO   | PRI | NULL    | auto_increment |
| mask     | varchar ( 64 )   | YES  |     | NULL    |                |
| reason   | varchar ( 256 )  | YES  |     | NULL    |                |
| instater | varchar ( 33 )   | YES  |     | NULL    |                |
| stamp    | int ( 11 )       | YES  |     | NULL    |                |
| expire   | int ( 11 )       | YES  |     | NULL    |                |
+----------+--------------+------+-----+---------+----------------+
6 rows in set  ( 0.23 sec ) 
 */
   
    
     
    static boolean isWhiteListed ( String in ) {
        if ( ! activateConnection ( ) )  {
            return false;
        }
        try {
            String usermask = in.replace ( '*', '%' );
            String query;
            for ( String white : Proc.getConf().getWhiteList() ) {
                query = "SELECT ? like ?";
                ps = sql.prepareStatement ( query );
                ps.setString   ( 1, "@"+white );
                ps.setString   ( 2, usermask );
                res2 = ps.executeQuery ( );
                 
                if ( res2.next ( ) && res2.getInt ( 1 ) == 1 )  {
                    res2.close();
                    ps.close ( );
                    System.out.println ( "DEBUG: found match!!!" );
                    return true;
                }
                ps.close ( );
            }
        } catch ( SQLException ex ) {
            Logger.getLogger(OSDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    /* Match complete ips */
    public static int checkBanByUser ( int hash, User user ) {
        String list = getListByHash ( hash );
        ServicesBan ban = null;
            
        if ( ! activateConnection ( ) )  {
            return -1;
        }
        
        try {
            String query =  "select id,replace(mask, '%','*') as mask,reason,instater,stamp,expire "+
                            "from "+list+" "+
                            "where ? like mask "+
                            "limit 1";
            ps = sql.prepareStatement ( query );
            ps.setString ( 1, user.getFullMask() );
            res2 = ps.executeQuery ( );
            
             
            if ( res2.next ( )  )  {
                ban = new ServicesBan ( hash, res2.getInt ( "id" ), res2.getString ( "mask" ), 
                                    res2.getString ( "reason" ), res2.getString ( "instater" ), 
                                    res2.getString ( "stamp" ), res2.getString ( "expire" ) );
                Handler.getOperServ().ban ( user, hash, ban );
                ps.close ( );
                res2.close ( );
                return 1;
            }    
            ps.close ( );

            res2.close ( );
        } catch (SQLException ex) {
            Logger.getLogger(OSDatabase.class.getName()).log(Level.SEVERE, null, ex);
            return -2;
        }
        return 0;
    }
    
    public static ServicesBan addServicesBan ( ServicesBan ban )  {
        String list = getListByHash ( ban.getType() );
        if ( ! activateConnection ( ) )  {
            return null;
        }
        try {
            int id;
           
            String query = "insert into "+list+" ( mask,reason,instater,stamp,expire ) VALUES "
                          +" ( replace(?,'*','%'), ?, ?, now() , now() + "+ban.getExpire()+" ) ";
            System.out.println(query);
            ps = sql.prepareStatement ( query );
            ps.setString   (1, ban.getMask ( )  );
            ps.setString   (2, ban.getReason ( )  );
            ps.setString   (3, ban.getInstater ( )  );
            ps.execute ( );
            ps.close ( );

            query = "SELECT id,stamp,expire "+
                    "FROM "+list+" "+
                    "WHERE id=last_insert_id ( )";
            ps = sql.prepareStatement ( query );
            res2 = ps.executeQuery ( );

            if ( res2.next ( ) ) {
                ban.setId ( res2.getInt ( "id" )  );
                ban.setTime ( res2.getString ( "stamp" )  );
                ban.setExpire ( res2.getString ( "expire" )  );
            } else {
                ban = null;
            }
            ps.close ( );
            res2.close ( );
            idleUpdate ( "addAkill ( ) " );
           
        } catch  ( SQLException ex )  {
            Proc.log ( OSDatabase.class.getName ( ) , ex );   
        } 
        return ban;
    }

    public static void logServicesBan ( int command, ServicesBan ban ) {
        String id;
        String flag;
        if ( ! activateConnection ( ) )  {
            return;
        }
        
        switch ( ban.getType() ) {
            case AKILL :
                id = "AK"+ban.getId ( );
                flag = ( command == DEL ? "AK-" : "AK+" );
                break;
                
            case IGNORE :
                id = "IG"+ban.getId ( );
                flag = ( command == DEL ? "IG-" : "IG+" );
                break;
                
            case SQLINE :
                id = "SQ"+ban.getId ( );
                flag = ( command == DEL ? "SQ-" : "SQ+" );
                break;
                      
            case SGLINE :
                id = "SQ"+ban.getId ( );
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
            ps.setInt  ( 1, ban.getId ( )  );
            ps.execute ( );
            ps.close ( );

            System.out.println ( "debug ( delServicesBan ( "+ban.getId ( ) +" )  ) " ); 
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
            String query = "select id,replace(mask,'%','*') as mask,reason,instater,stamp,expire "+
                           "from "+list+" "+
                           "order by id desc";
            ps = sql.prepareStatement ( query );
            res2 = ps.executeQuery ( );
 
            while ( res2.next ( )  )  {
                banList.add ( new ServicesBan ( hash, res2.getInt ( "id" ), res2.getString ( "mask" ), 
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
            String query = "select id,replace(mask,'%','*') as mask,reason,instater,stamp,expire "+
                           "from "+list+" "+
                           "where id = ?";
            
            ps = sql.prepareStatement ( query );
            ps.setInt ( 1, tID );
            res2 = ps.executeQuery ( );
            if ( res2.next ( ) ) {
                ban = new ServicesBan ( banType, res2.getInt ( "id" ), res2.getString ( "mask" ), 
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
            String query = "select id,replace(mask,'%','*') as mask,reason,instater,stamp,expire "+
                           "from "+list+" "+
                           "where expire < now() "+
                           "order by id asc";
            ps = sql.prepareStatement ( query );
            res2 = ps.executeQuery ( );

            while ( res2.next ( )  )  {
                banList.add ( new ServicesBan ( hash, res2.getInt ( "id" ), res2.getString ( "mask" ),
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

    static ArrayList<String> getServerList ( boolean missing ) {
        ArrayList<String> sList = new ArrayList<>();
        if ( ! activateConnection ( ) ) {
            return sList;
        }   
        String query;
        try {
            if ( missing )  {
                query = "select name from server where lastseen < now() - interval 2 minute order by name asc";
            } else {
                query = "select name from server order by name asc";                
            }
            ps = sql.prepareStatement ( query );
            res = ps.executeQuery ( );
            
            while ( res.next ( ) ) {
                sList.add( res.getString ( 1 ) );
            }
            res2.close ( );
            ps.close ( );
            
        } catch ( SQLException ex ) {
            Proc.log ( OSDatabase.class.getName ( ) , ex );
        }
        return sList;
    }
     
    static boolean delServer ( String name ) {
        boolean deleted;
        if ( ! activateConnection ( ) || ! serverExistInList ( name ) ) {
            return false;
        }
         
        String query = "delete from server where name = ?";
        try {
            ps = sql.prepareStatement ( query );
            ps.setString ( 1, name );
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

    public static void addServer ( String name ) {
        if ( ! activateConnection ( ) ) {
            return;
        }
        
        String query = "insert into server ( name, lastseen ) "+
                       "values (?, now()) "+
                       "on duplicate key update lastseen=now()";
        try {
            ps = sql.prepareStatement ( query );
            ps.setString ( 1, name );
            ps.execute();

            res2.close ( );
            ps.close ( );
            
        } catch ( SQLException ex ) {
            Proc.log ( OSDatabase.class.getName ( ) , ex );
        }
    }
    
    static boolean serverExistInList ( String name ) {
        boolean found;
        if ( ! activateConnection ( ) ) {
            return false;
        }
        
        String query = "select name from server where name = ?";
        
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

    static void updateServers ( ) {
        if ( ! activateConnection ( ) ) {
            return;
        }
        
        String names = "";
        for ( Server server : Handler.getServerList ( ) ) {
            if ( names.length() == 0 ) {
                names += "'"+server.getName()+"'";
            } else {
                names += ",'"+server.getName()+"'";
            }
        }
        
        String query = "update server "+
                       "set lastseen=now() "+
                       "where name in ( "+names+" )";
        try {
            ps = sql.prepareStatement ( query );
            ps.execute ( );
            res2.close ( );
            ps.close ( );
            
        } catch ( Exception ex ) {
            Proc.log ( OSDatabase.class.getName ( ) , ex );
        } 
    }
    
    static ArrayList<Comment> getCommentList ( String target, boolean full ) {
        ArrayList<Comment> cList = new ArrayList<>();
        Comment comment;
        String query;
        
        if ( ! activateConnection ( ) )  {
            return cList;
        }
        
        query = "select name,instater,comment,stamp from comment "+
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

   
     
    static public Oper addStaff ( User user, NickInfo ni, int access ) {
        Oper oper;
        if ( ! activateConnection ( )  )  {
            return null;
        }
        try {
            String query = "INSERT INTO oper ( name,access,instater ) VALUES ( ?, ?, ? ) "
                          +"ON DUPLICATE KEY UPDATE access = ?,instater = ?;";
            ps = sql.prepareStatement ( query );
            ps.setString   ( 1, ni.getName ( ) );
            ps.setInt      ( 2, access );
            ps.setString   ( 3, user.getOper().getName ( ) );
            ps.setInt      ( 4, access );
            ps.setString   ( 5, user.getOper().getName ( ) );
            ps.execute ( );
            ps.close ( );
            oper = getOper ( ni.getName() );
            idleUpdate ( "addStaff ( ) " );
        
        } catch  ( SQLException ex )  {
            Proc.log ( OSDatabase.class.getName ( ) , ex );
            return null;
        } 
        return oper;
    }
    
    
    public static Oper delStaff ( NickInfo ni, int access )  {
        Oper oper;
        if ( ! activateConnection ( )  )  {
            return null;
        }
        try {
            String query = "DELETE FROM oper WHERE name = ? AND access = ?;";
            ps = sql.prepareStatement ( query );
            ps.setString   ( 1, ni.getName ( )       );
            ps.setInt      ( 2, access               );
            ps.execute ( );
            ps.close ( );
            oper = getOper ( ni.getName() );
            idleUpdate ( "delCSop ( ) " );
                     
        } catch  ( SQLException ex )  {
            Proc.log ( OSDatabase.class.getName ( ) , ex );
            return null;
        } 
        return oper;
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
    public static ArrayList<Oper> getMaster ( ) {
        return getStaffByAccess ( 5 );
    }
    public static ArrayList<Oper> getRootAdmins ( ) { 
        return getStaffByAccess ( 4 ); 
    }
    public static ArrayList<Oper> getCSops ( ) {
        return getStaffByAccess ( 3 );
    } 
    public static ArrayList<Oper> getServicesAdmins ( ) {
        return getStaffByAccess ( 2 );
    }
    public static ArrayList<Oper> getIRCops() {
        return getStaffByAccess ( 1 );
    }
    
    public static ArrayList<Oper> getSRAPlus ( )             { 
        ArrayList<Oper> oList = new ArrayList<> ( );
        oList.addAll ( OSDatabase.getMaster ( )  ); 
        oList.addAll ( OSDatabase.getRootAdmins ( )  ); 
        return oList;
    }
    
    public static ArrayList<Oper> getCSopsPlus ( )             { 
        ArrayList<Oper> oList = new ArrayList<> ( );
        oList.addAll ( OSDatabase.getMaster ( )  ); 
        oList.addAll ( OSDatabase.getRootAdmins ( )  ); 
        oList.addAll ( OSDatabase.getCSops ( )  );
        return oList;
    }
    
    public static ArrayList<Oper> getServicesAdminsPlus ( )             { 
        ArrayList<Oper> oList = new ArrayList<>( );
        System.out.println ( "DEBUG: getServicesAdminsPlus ( );" );
        oList.addAll ( OSDatabase.getMaster ( )  ); 
        oList.addAll ( OSDatabase.getRootAdmins ( )  ); 
        oList.addAll ( OSDatabase.getCSops ( )  );
        oList.addAll ( OSDatabase.getServicesAdmins ( )  );
        return oList;
    }

  
    /* LOG EVENT */
    static public int logEvent ( OSLogEvent log ) {
        int id = Database.logEvent( "operlog", log );
        return id;
    }
    static public void delLogEvent ( int id ) {
        Database.delLogEvent( "operlog", id );
    }

 

 
}