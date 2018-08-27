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
package core;

import chanserv.CSDatabase;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import operserv.OSLogEvent;

/**
 *
 * @author DreamHealer
 */
public class Database extends HashNumeric { 
    protected static Connection sql;
 //   private static boolean connected;    
    protected static long lastUsed;
    protected static boolean debug = false;
    public static PreparedStatement ps;
    private static long lastConnectAttempt;
    private static long lastGlobops;
    private static int attempts;
    
    public Database ( )  {
        try {
            if ( sql != null )  {
                Class.forName ( "com.mysql.jdbc.Driver" ) .newInstance ( );
                connect ( ); 
            }
        } catch  ( ClassNotFoundException | IllegalAccessException | InstantiationException ex )  {
            Proc.log ( Database.class.getName ( ) , ex );
        } 
    }

    protected static void runMaintenance ( )  {
        /* Are we not connected? then reconnect */
        if ( ! checkConn ( ) ) {
            connect ( );
        }
    }

    protected static void connect ( )  {
        try {
            if ( sql == null || ! sql.isValid ( 1 )  )  {
                if ( System.currentTimeMillis() - lastConnectAttempt >= 5000 ) {
                    Config conf = Proc.getConf ( );
                    sql = DriverManager.getConnection ( "jdbc:mysql://"+conf.get ( MYSQLHOST ) +":"+Integer.parseInt ( conf.get ( MYSQLPORT )  ) +"/"+conf.get (MYSQLDB ) , conf.get ( MYSQLUSER ) , conf.get ( MYSQLPASS )  );           
                    attempts = 0;
                    if ( Handler.getOperServ() != null ) {
                        Handler.getOperServ().sendGlobOp ( "Database connection established." );
                    }
                } 
            }
        } catch  ( SQLException | NumberFormatException ex )  {
            sql = null;
            attempts++;
            if ( System.currentTimeMillis() - lastGlobops >= 60000 ) {
                if ( attempts == 1 ) {
                    Handler.getOperServ().sendGlobOp ( "Database connection lost." );
                } else {
                    Handler.getOperServ().sendGlobOp ( "Database re-connection attempt failed." );
                }
                lastGlobops = System.currentTimeMillis();
            }
            lastConnectAttempt = System.currentTimeMillis();
        }
    }
    
    protected static void idleUpdate ( String where )  {
        if ( debug )  {
            System.out.println ( "DEBUG: "+where );
        }
        lastUsed = System.currentTimeMillis ( ); 
    }
   
    
   /* STATIC INT */
    public final static int SERVER          = 1;
    public final static int MODE            = 2; 
    
    public final static int REGISTERED      = 11;
    public final static int REGONLY         = 12;
    public final static int TOPICLIMIT      = 13;
    public final static int NOPRIVMSG       = 14;
    public final static int INVITEONLY      = 15;
    public final static int KEY             = 16;
    public final static int SECRET          = 17;
    public final static int MODREG          = 19;
    public final static int LIMIT           = 20;
    public final static int JOINRATE        = 21;
    public final static int NOCTRL          = 22;
    public final static int OPERONLY        = 23;
    public final static int MODERATED       = 24;
    
    private final static int MODE_COUNT     = 24;
    
     
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
    
    public static boolean activateConnection ( )  {
        if ( ! checkConn ( )  )  { 
            connect ( ); 
        }        
        return checkConn ( );
    }
    
    public static boolean checkConn ( )  {
        try {
            return ! ( sql == null || ! sql.isValid ( 1 ) );
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
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
    public static void log ( String target, String body )  {

        if ( ! activateConnection ( )  )  {
            /* No SQL connection */ 
            Proc.log ( "ERROR LOGGING!" );
            return;
        } else {
             try { 
                String query = "INSERT INTO log  ( target,body,stamp )  "
                             + "VALUES  ( ?, ?, UNIX_TIMESTAMP ( )  ) ";
                ps = sql.prepareStatement ( query );
                ps.setString  ( 1, target );
                ps.setString  ( 2, body );
                ps.execute ( );
                ps.close ( );
                  
                idleUpdate ( "log ( ) " );
            } catch  ( SQLException ex )  {
                /* Nick already exists? return -1 */
                Proc.log ( Database.class.getName ( ) , ex );
            }
        } 
    }
    
    /* LOG EVENT */
    static public int logEvent ( String table, LogEvent log ) {
        int id = 0;
        String query;
        
        if ( ! activateConnection ( ) ) {
            return -2;
        } else if ( log == null ) {
            return -3;
        }
        
        System.out.println("DEBUG: name: "+log.getName());
        System.out.println("DEBUG: flag: "+log.getFlag());
        System.out.println("DEBUG: mask: "+log.getMask());
        System.out.println("DEBUG: oper: "+log.getOper());
        
        try {
            if ( log.isOper() ) {
                query = "insert into "+table+" "+
                        "(name,flag,usermask,oper,stamp) "+
                        "values (?,?,?,?,?)";
                ps = sql.prepareStatement ( query, PreparedStatement.RETURN_GENERATED_KEYS );
                ps.setString ( 1, log.getName() );
                ps.setString ( 2, log.getFlag() );
                ps.setString ( 3, log.getMask() );
                ps.setString ( 4, log.getOper() );
                ps.setString ( 5, log.getStamp() );
            } else {
                query = "insert into "+table+" "+
                        "(name,flag,usermask,stamp) "+
                        "values (?,?,?,?)";
                ps = sql.prepareStatement ( query, PreparedStatement.RETURN_GENERATED_KEYS );
                ps.setString ( 1, log.getName() );
                ps.setString ( 2, log.getFlag() );
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
    
    
}