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
package core;

import operserv.OperServ;
import monitor.Snoop;
import memoserv.MemoServ;
import mail.MXDatabase;
import chanserv.CSDatabase;
import chanserv.ChanInfo;
import chanserv.ChanServ;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
    private static long lastValidated;          /* when we last asked the server if the connection works */
    private static volatile boolean connValid;
    private static final long VALIDATE_INTERVAL = 5000;    /* ms */
    private static final long RECONNECT_INTERVAL = 30000;  /* ms between attempts while the database is down */
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
            if ( ! checkConn ( ) &&
                System.currentTimeMillis() - lastConnectAttempt >= ( attempts == 0 ? 0 : RECONNECT_INTERVAL ) ) {
                    lastConnectAttempt = System.currentTimeMillis();
                    closeQuietly ( );
                    /* Timeouts so a database that stops answering can never
                       block services (and make the hub ping us out) for long.
                       dontTrackOpenResources: the connection keeps no list of
                       its statements, so one that is not closed after an error
                       is garbage like any other object. With the list they
                       piled up until the connection was replaced */
                    sql = DriverManager.getConnection ( 
                            "jdbc:mysql://"+Proc.getConf().get(MYSQLHOST)+":"+Integer.parseInt( Proc.getConf().get(MYSQLPORT).getString() )+"/"+Proc.getConf().get(MYSQLDB).getString()
                            +"?characterEncoding=UTF-8&connectionCollation=utf8mb4_swedish_ci"
                            +"&connectTimeout=5000&socketTimeout=20000&tcpKeepAlive=true&dontTrackOpenResources=true", 
                            Proc.getConf().get(MYSQLUSER).getString(), 
                            Proc.getConf().get(MYSQLPASS).getString()
                    );           
                    connValid = true;
                    lastValidated = System.currentTimeMillis();
                    attempts = 0;
                    if ( Handler.getOperServ() != null ) {
                        Handler.getOperServ().sendGlobOp ( "Database connection established. To be written: "+getServiceStats ( ) );
                    }
                } 
       
        } catch  ( SQLException | NumberFormatException ex )  {
            sql = null;
            connValid = false;
            attempts++;
            if ( System.currentTimeMillis() - lastGlobops >= 10000 ) {
                if ( attempts == 1 ) {
                    if ( Handler.getOperServ() != null ) {
                        Handler.getOperServ().sendGlobOp ( "Database connection lost." );
                    }
                } else {
                    if ( Handler.getOperServ() != null ) {
                        Handler.getOperServ().sendGlobOp ( "Database is not reachable, services work from memory. Waiting to be written: "+getServiceStats ( ) );
                    }
                }
                lastGlobops = System.currentTimeMillis();
            }
            lastConnectAttempt = System.currentTimeMillis();
        }
    }

    /**
     * @return what waits to be written, in words for the staff:
     *         "1 new nick, 2 changed channels, 1 memo", or "nothing"
     */
    protected static String getServiceStats ( ) {
        StringBuilder text = new StringBuilder ( );
        count ( text, Handler.getNickServ().getNickRegStats ( ),    "new nick" );
        count ( text, Handler.getNickServ().getChangesStats ( ),    "changed nick" );
        count ( text, NickServ.waitingDeletes ( ),                  "dropped nick" );
        count ( text, NickServ.waitingAuths ( ),                    "mail or password change" );
        count ( text, Handler.getChanServ().getChanRegStats ( ),    "new channel" );
        count ( text, Handler.getChanServ().getChangesStats ( ),    "changed channel" );
        count ( text, ChanServ.waitingDeletes ( ),                  "dropped channel" );
        count ( text, ChanServ.waitingAccess ( ),                   "access change" );
        count ( text, MemoServ.waiting ( ),                         "memo" );
        count ( text, MXDatabase.waiting ( ),                       "mail" );
        count ( text, OperServ.waiting ( ),                         "oper change" );
        count ( text, Handler.waitingSIDs ( ),                      "session" );
        count ( text, NickServ.waitingLogs ( ) + ChanServ.waitingLogs ( ) + OperServ.waitingLogs ( ) + Snoop.waiting ( ), "log row" );
        return ( text.length ( ) > 0 ? text.toString ( ) : "nothing" );
    }

    private static void count ( StringBuilder text, int count, String what ) {
        if ( count > 0 ) {
            text.append ( text.length ( ) > 0 ? ", " : "" ).append ( count ).append ( " " ).append ( what ).append ( count == 1 ? "" : "s" );
        }
    }
    
    /**
     *
     * @param where
     */
    /* All or nothing for a register that is several inserts: without it a
       failure half way leaves a row that makes every retry fail on the
       primary key, and a nick or channel that cannot be loaded */
    /* The last error of the database, for WorkGuard: it tells an item the
       database will never take from a server that can not do it right now */
    private static String lastState = null;
    private static int    lastCode  = 0;
    /* Errors of MariaDB that are about the server and not about what was
       sent: disk full, storage engine, too many connections, out of memory,
       shutdown, table full, lock wait and lock table, read only (three of
       them), and no access (database, login, table, column, privilege,
       locked account). The same write works when that has passed. */
    private static final int[] SERVERERRORS = {
        1021, 1030, 1040, 1041, 1053, 1114, 1205, 1206, 1290, 1792, 1836,
        1044, 1045, 1142, 1143, 1227, 4151
    };

    public static void setLastError ( String state, int code ) {
        lastState = ( state != null ? state : "" );
        lastCode  = code;
    }

    /**
     * @return true when the last failed write was about the item (too long,
     *         a duplicate, a missing column, or it failed without an error).
     *         False when it was the connection (SQLState 08), a deadlock
     *         (40) or one of the server errors above: the item is fine and
     *         is kept. Asking forgets the error.
     */
    public static boolean lastErrorWasData ( ) {
        String state = lastState;
        int code     = lastCode;
        lastState    = null;
        lastCode     = 0;
        if ( state == null ) {
            return true;
        }
        if ( state.startsWith ( "08" ) || state.startsWith ( "40" ) ) {
            return false;
        }
        for ( int server : SERVERERRORS ) {
            if ( code == server ) {
                return false;
            }
        }
        return true;
    }

    protected static void begin ( ) throws SQLException {
        sql.setAutoCommit ( false );
    }
    
    protected static void commit ( ) throws SQLException {
        sql.commit ( );
        sql.setAutoCommit ( true );
    }
    
    protected static void rollback ( ) {
        try {
            sql.rollback ( );
            sql.setAutoCommit ( true );
        } catch ( SQLException ex ) {
            /* the connection is gone, a new one starts in autocommit */
        }
    }
    
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
        return connValid;
    }
    
    /**
     *
     * @return
     */
    public static boolean checkConn ( )  {
        if ( sql == null ) {
            return false;
        }
        /* Asking the server costs a round trip, only do it every few seconds */
        if ( connValid && System.currentTimeMillis() - lastValidated < VALIDATE_INTERVAL ) {
            return true;
        }
        if ( ! connValid ) {
            return false;
        }
        lastValidated = System.currentTimeMillis();
        try {
            connValid = sql.isValid ( 2 );
        } catch (SQLException ex) {
            connValid = false;
        }
        return connValid;
    }

    /**
     * The connection failed (network error or timeout), stop using it and
     * reconnect. Called from Proc.log for connection related SQLExceptions.
     */
    public static void invalidate ( )  {
        connValid = false;
    }

    private static void closeQuietly ( )  {
        if ( sql != null ) {
            try {
                sql.close ( );
            } catch ( SQLException ex ) {
                /* already broken */
            }
        }
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
            Proc.log ( Database.class.getName ( ) , ex );
            return false;
        }
        sid.setStored ( true );
        return true;             
    }

    /**
     * Remove the rows of services IDs that nobody uses any more
     * @param sids a few hundred at most, they go in one statement
     * @return false if they could not be removed
     */
    public static boolean deleteServicesIDs ( ArrayList<ServicesID> sids ) {
        if ( sids.isEmpty ( ) ) {
            return true;
        }
        if ( ! activateConnection ( ) ) {
            return false;
        }
        StringBuilder marks = new StringBuilder ( "?" );
        for ( int i = 1; i < sids.size ( ); i++ ) {
            marks.append ( ",?" );
        }
        try ( PreparedStatement del = sql.prepareStatement ( "delete from servicesid where id in ("+marks+")" ) ) {
            for ( int i = 0; i < sids.size ( ); i++ ) {
                del.setLong ( i + 1, sids.get ( i ).getID ( ) );
            }
            del.executeUpdate ( );
        } catch ( SQLException ex ) {
            Proc.log ( Database.class.getName ( ) , ex );
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