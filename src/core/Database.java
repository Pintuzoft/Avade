/* 
 * Copyright (C) 2018 Fredrik Karlsson aka DreamHealer & avade.net
 *
 * This program isSet free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program isSet distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package core;

import chanserv.CSDatabase;
import chanserv.ChanInfo;
import chanserv.ChanServ;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import monitor.SnoopLog;
import nickserv.NickInfo;
import nickserv.NickServ;

/**
 *
 * @author DreamHealer
 */
public class Database extends HashNumeric { 

    /**
     *
     */
    protected static Connection sql;

    /**
     *
     */
    protected static long lastUsed;

    /**
     *
     */
    protected static boolean debug = false;

    /**
     *
     */
    public static PreparedStatement ps;
    private static long lastConnectAttempt;
    private static long lastGlobops;
    private static int attempts;
    private static ResultSet res;

    /**
     *
     */
    public Database ( )  {
        try {
            if ( sql != null )  {
                connect ( ); 
            }
        } catch ( Exception e )  {
            // discard
        } 
    }

    /**
     *
     */
    protected static void runMaintenance ( )  {
        /* Are we not connected? then reconnect */
        if ( ! checkConn ( ) ) {
            connect ( );
        }
    }

    /**
     *
     */
    protected static void connect ( )  {
        try {
            if ( ( sql == null || ! sql.isValid ( 1 ) ) &&
                System.currentTimeMillis() - lastConnectAttempt >= 5000 ) {
                    sql = DriverManager.getConnection ( 
                            "jdbc:mysql://"+Proc.getConf().get(MYSQLHOST)+":"+Integer.parseInt( Proc.getConf().get(MYSQLPORT).getString() )+"/"+Proc.getConf().get(MYSQLDB).getString(), 
                            Proc.getConf().get(MYSQLUSER).getString(), 
                            Proc.getConf().get(MYSQLPASS).getString()
                    );           
                    attempts = 0;
                    if ( Handler.getOperServ() != null ) {
                        Handler.getOperServ().sendGlobOp ( "Database connection established - "+getServiceStats ( ) );
                    }
                } 
       
        } catch  ( SQLException | NumberFormatException ex )  {
            sql = null;
            attempts++;
            if ( System.currentTimeMillis() - lastGlobops >= 10000 ) {
                if ( attempts == 1 ) {
                    if ( Handler.getOperServ() != null ) {
                        Handler.getOperServ().sendGlobOp ( "Database connection lost." );
                    }
                } else {
                    if ( Handler.getOperServ() != null ) {
                        Handler.getOperServ().sendGlobOp ( "Database re-connection attempt failed - "+getServiceStats ( ) );
                    }
                }
                lastGlobops = System.currentTimeMillis();
            }
            lastConnectAttempt = System.currentTimeMillis();
        }
    }

    /**
     *
     * @return
     */
    protected static String getServiceStats ( ) {
        int chanRegs = Handler.getChanServ().getChanRegStats ( );
        int chanChanges = Handler.getChanServ().getChangesStats ( );
        int nickRegs = Handler.getNickServ().getNickRegStats ( );
        int nickChanges = Handler.getNickServ().getChangesStats ( );
        return "(new/changed): Channels:"+chanRegs+"/"+chanChanges+" Nicks:"+nickRegs+"/"+nickChanges;
    }
    
    /**
     *
     * @param where
     */
    protected static void idleUpdate ( String where )  {
        if ( debug )  {
            System.out.println ( "DEBUG: "+where );
        }
        lastUsed = System.currentTimeMillis ( ); 
    }
   
    
   /* STATIC INT */

    /**
     *
     */

    public final static int SERVER          = 1;

    /**
     *
     */
    public final static int MODE            = 2; 
    
    /**
     *
     */
    public final static int REGISTERED      = 11;

    /**
     *
     */
    public final static int REGONLY         = 12;

    /**
     *
     */
    public final static int TOPICLIMIT      = 13;

    /**
     *
     */
    public final static int NOPRIVMSG       = 14;

    /**
     *
     */
    public final static int INVITEONLY      = 15;

    /**
     *
     */
    public final static int KEY             = 16;

    /**
     *
     */
    public final static int SECRET          = 17;

    /**
     *
     */
    public final static int MODREG          = 19;

    /**
     *
     */
    public final static int LIMIT           = 20;

    /**
     *
     */
    public final static int JOINRATE        = 21;

    /**
     *
     */
    public final static int NOCTRL          = 22;

    /**
     *
     */
    public final static int OPERONLY        = 23;

    /**
     *
     */
    public final static int MODERATED       = 24;
    
    private final static int MODE_COUNT     = 24;
    
    /**
     *
     */
    protected static void disconnect ( )  {
        try {
            if ( sql != null )  {
                sql.close ( );
                System.out.println ( "Database: closed connection;" );
            }
        } catch  ( SQLException ex )  {
             Proc.log ( Database.class.getName ( ) , ex );
        }
    }
    
    /**
     *
     * @return
     */
    public static boolean activateConnection ( )  {
        if ( ! checkConn ( )  )  { 
            connect ( ); 
        }        
        return checkConn ( );
    }
    
    /**
     *
     * @return
     */
    public static boolean checkConn ( )  {
        try {
            return ! ( sql == null || ! sql.isValid ( 1 ) );
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /* Database changes */
    static boolean change ( String query ) throws SQLException {
        System.out.println("Applying db-change: "+query);
        ps = sql.prepareStatement ( query );
        ps.execute ( );
        ps.close ( );     
        return true;
    }

    /*  mysql> desc log;
        +--------+-------------+------+-----+---------+----------------+
        | Field  | Type        | Null | Key | Default | Extra          |
        +--------+-------------+------+-----+---------+----------------+
        | id     | int ( 11 )      | NO   | PRI | NULL    | auto_increment |
        | target | varchar ( 33 )  | YES  |     | NULL    |                |
        | body   | text        | YES  |     | NULL    |                |
        | stamp  | int ( 11 )      | YES  |     | NULL    |                |
        +--------+-------------+------+-----+---------+----------------+
        4 rows in set  ( 0.00 sec ) */
    
    /* LOGGING */

    /**
     *
     * @param target
     * @param body
     */

    public static void log ( String target, String body ) {
        if ( ! activateConnection ( ) ) {
            /* No SQL connection */ 
            Proc.log ( "ERROR LOGGING!" );
            return;
        } else {
             try { 
                String query = "INSERT INTO log ( target, body, stamp ) "
                             + "VALUES ( ?, ?, NOW() )";
                ps = sql.prepareStatement ( query );
                ps.setString  ( 1, target );
                ps.setString  ( 2, body );
                ps.execute ( );
                ps.close ( );
                  
                idleUpdate ( "log ( )" );
            } catch  ( SQLException ex )  {
                Proc.log ( Database.class.getName ( ), ex );
            }
        } 
    }
    
    /**
     *
     * @param log
     * @return
     */
    public static boolean SnoopLog ( SnoopLog log ) {
        if ( ! activateConnection ( ) ) {
            /* No SQL connection */ 
            Proc.log ( "ERROR LOGGING!" );
            return false;
        } else {
             try { 
                String query = "INSERT INTO log ( target, body, stamp ) "
                             + "VALUES ( ?, ?, ? )";
                ps = sql.prepareStatement ( query );
                ps.setString  ( 1, log.getTarget().getString() );
                ps.setString  ( 2, log.getMessage() );
                ps.setString  ( 3, log.getStamp() );
                ps.execute ( );
                ps.close ( );
                  
                idleUpdate ( "log ( )" );
                return true;
            } catch  ( SQLException ex )  {
                Proc.log ( Database.class.getName ( ), ex );
                return false;
            }
        }

    }

    
    
    /* LOG EVENT */

    /**
     *
     * @param table
     * @param log
     * @return
     */

    static public int logEvent ( String table, LogEvent log ) {
        int id = 0;
        String query;
        
        if ( ! activateConnection ( ) ) {
            return -2;
        } else if ( log == null ) {
            return -3;
        }
        
        try {
            if ( log.isOper() ) {
                query = "insert into "+table+" "+
                        "(name,flag,usermask,oper,stamp) "+
                        "values (?,?,?,?,?)";
                ps = sql.prepareStatement ( query, PreparedStatement.RETURN_GENERATED_KEYS );
                ps.setString ( 1, log.getName().getString() );
                ps.setString ( 2, log.getFlag().getString() );
                ps.setString ( 3, log.getMask() );
                ps.setString ( 4, log.getOper() );
                ps.setString ( 5, log.getStamp() );
            } else {
                query = "insert into "+table+" "+
                        "(name,flag,usermask,stamp) "+
                        "values (?,?,?,?)";
                ps = sql.prepareStatement ( query, PreparedStatement.RETURN_GENERATED_KEYS );
                ps.setString ( 1, log.getName().getString() );
                ps.setString ( 2, log.getFlag().getString() );
                ps.setString ( 3, log.getMask() );
                ps.setString ( 4, log.getStamp() );
            }
            ps.execute ( );
            
            ResultSet rs=ps.getGeneratedKeys();
            
            if ( rs.next ( ) ) {
                id=rs.getInt ( 1 );
            }
            ps.close ( );
            idleUpdate ( "logEvent ( ) " );
            
        } catch ( SQLException ex ) {
            Proc.log ( CSDatabase.class.getName ( ) , ex );
            return -1;
        }
        return id;
    }

    /**
     *
     * @param table
     * @param id
     */
    static public void delLogEvent ( String table, int id ) {
        if ( ! activateConnection ( )  )  {
            return;
        }
        try {
            String query = "delete from "+table+" "+
                           "where id = ?;";
            ps = sql.prepareStatement ( query );
            ps.setInt ( 1, id );
            ps.execute ( );
            ps.close ( ); 
        } catch ( SQLException e )  {
            Proc.log ( CSDatabase.class.getName ( ) , e );
            System.out.println ( "Error deleting id:"+id+" from chanlog." );
        }
    }
    
    /**
     *
     * @param sid
     * @return
     */
    public static boolean updateServicesID ( ServicesID sid ) {
        if ( ! activateConnection() || sid == null ) {
            return false;
        }
        String query;
        String nicks = nicksToString ( 128, sid.getNiList() );
        String chans = chansToString ( 128, sid.getCiList() );
        try {
            query = "insert into servicesid "+
                    "( id, stamp, nicks, chans ) "+
                    "values ( ?, now(), ?, ? ) "+
                    "on duplicate key update stamp=now(), nicks = ?, chans = ? ";
            ps = sql.prepareStatement ( query );  
            ps.setLong   ( 1, sid.getID() );
            ps.setString ( 2, nicks );
            ps.setString ( 3, chans );
            ps.setString ( 4, nicks );
            ps.setString ( 5, chans );
            ps.execute ( );
            ps.close ( ); 
            
        } catch ( SQLException ex ) {
            return false;
        }
        return true;             
    }

    /**
     *
     */
    public static void loadSIDs() {
        if ( ! activateConnection() ) {
            return;
        }
        String query;
        ServicesID sid;
        ArrayList<NickInfo> nList;
        ArrayList<ChanInfo> cList;
        try {
            query = "select id,stamp,nicks,chans "+
                    "from servicesid";
            ps = sql.prepareStatement ( query );
            res = ps.executeQuery ( );
            while ( res.next() ) {
                sid = new ServicesID ( res.getLong("id") );
                sid.updateStamp();
                if ( res.getString("nicks") != null ) {
                    nList = stringToNicks ( res.getString("nicks") );
                    sid.setNiList ( nList );
                }
                if ( res.getString("chans") != null ) {
                    cList = stringToChans ( res.getString("chans") );
                    sid.setCiList ( cList );
                }
                Handler.newSid ( sid );
            }
            ps.close ( );
            
        } catch ( SQLException ex ) {
            Proc.log ( Database.class.getName ( ) , ex );

        }
    }
    
    
    static String getDBVersion() {
        String version = null;
        if ( ! activateConnection() ) {
            return version;
        }
        
        String query;
        try {
            query = "select value "+
                    "from settings "+
                    "where name = 'version'";
            ps = sql.prepareStatement ( query );
            res = ps.executeQuery ( );
            if ( res.next() ) {
                version = res.getString ( 1 );
            }
            ps.close ( );
            
        } catch ( SQLException ex ) {
            Proc.log ( Database.class.getName ( ) , ex );
            version = "1.1701-1"; /* Base version */
        }
        return version;
    }

    
    private static ArrayList<NickInfo> stringToNicks ( String in ) {
        ArrayList<NickInfo> nList = new ArrayList<>();
        NickInfo ni;
        String[] nicks = in.split(",");
        for ( String nick : nicks ) {
            if ( ( ni = NickServ.findNick ( nick ) ) != null ) {
                nList.add ( ni );
            }
        }
        return nList;
    }
    private static ArrayList<ChanInfo> stringToChans ( String in ) {
        ArrayList<ChanInfo> cList = new ArrayList<>();
        ChanInfo ci;
        String[] chans = in.split(",");
        for ( String chan : chans ) {
            if ( ( ci = ChanServ.findChan ( chan ) ) != null ) {
                cList.add ( ci );
            }
        }
        return cList;
    }
    
    private static String nicksToString ( int max, ArrayList<NickInfo> list ) {
        String buf = "";
        for ( NickInfo ni : list ) {
            if ( ( buf.length() + ni.getName().length() + 1 ) < max ) {
                if ( buf.length() > 0 ) {
                    buf += ","+ni.getName();
                } else {
                    buf = ni.getNameStr();
                }
            }
        }
        return buf;
    }
    private static String chansToString ( int max, ArrayList<ChanInfo> list ) {
        String buf = "";
        for ( ChanInfo ci : list ) {
            if ( ( buf.length() + ci.getName().length() + 1 ) < max ) {
                if ( buf.length() > 0 ) {
                    buf += ","+ci.getName();
                } else {
                    buf = ci.getNameStr();
                }
            }
        }
        return buf;
    }
     
    
    
    
}