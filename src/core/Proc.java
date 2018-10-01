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

import server.ServSock;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;
import operserv.OperServ;

/**
 *
 * @author DreamHealer
 */
public class Proc extends HashNumeric {
    private static Version              version = new Version ( ); /* Services-1.0.5 */
 
   // private ServSock                    conn;
    private static ServSock             conn;

    private Handler                     handler;
    private boolean                     run; 
    private static Log                  logger; 
    private static long                 start;
    private long                        ticker; 
    private long                        minMaintenance;
    private long                        hourMaintenance;
    private long secMaintenance;
    private int                         minuteDelay;
    private int                         hourDelay; 
    private int secondDelay;

    private String                      read; 
    private static Config               config;
    
    public Proc ( )  throws IOException {
        loadConf ( );
        logger                  = new Log ( );
        this.run                = true;
        this.ticker             = System.nanoTime ( ); /* current time */
        this.checkVersion ( );  /* Check version and apply changes if neccessary */
        this.connect ( );
        
        this.handler            = new Handler ( );

        start                   = System.currentTimeMillis ( );

        this.secMaintenance     = start;
        this.minMaintenance     = start;
        this.hourMaintenance    = start;
        this.secondDelay        = 1000; /* Every second */
        this.minuteDelay        = 60 * 1000; /* Every minute */
        this.hourDelay          = 60 * 60 * 1000; /* Every hour */

        this.runLoop ( );
    }
    
    @SuppressWarnings ( "WaitWhileNotSynced" ) 
    private void runLoop ( )  {
        int counter = 0;
        long hourAgo = 0;
        long minAgo = 0;
        long secAgo = 0;
       
        while ( this.run )  {
            /* Proc loop */
         
            
            this.read = this.conn.readLine();
            

            if ( this.read != null )  {
                /* We found some new data, send it to the handler */ 
                this.handler.process ( this.read );
               
            } else {
                /* We didnt find any new data so lets take a nap */
                try {
                    Thread.sleep ( 100 );          
                   
                } catch  ( InterruptedException ex )  {
                    Logger.getLogger ( Proc.class.getName ( ) ) .log ( Level.SEVERE, null, ex );
                }
                
            }      
            /* HOUR */
            hourAgo = System.currentTimeMillis ( ) - this.hourDelay;
            if ( this.hourMaintenance < hourAgo )  {
                this.handler.runHourMaintenance ( );
                this.hourMaintenance = System.currentTimeMillis ( );
            }
            
            /* MINUTE */
            minAgo = System.currentTimeMillis ( ) - this.minuteDelay;
            if ( this.minMaintenance < minAgo )  {
                this.handler.runMinuteMaintenance ( );
                if ( this.conn.timedOut() ) { /* Did we time out? */
                    System.out.println("1:");
                    this.conn.disconnect();
                    this.connect();
                    this.handler.unloadServices ( );
                    this.handler.initServices ( );
                }
                this.minMaintenance = System.currentTimeMillis ( );
            }
            /* SECOND */
            secAgo = System.currentTimeMillis ( ) - this.secondDelay;
            if ( this.secMaintenance < secAgo )  {
                this.handler.runSecondMaintenance ( );
                if ( this.handler.sanityCheck ( ) ) {
                    this.handler.initServices ( );
                }
                this.secMaintenance = System.currentTimeMillis ( );
            }

        }
       // System.out.println("DEBUG!!!: OUTSIDE loop!!");

    }

    public static long getStartTime ( ) {
        return start;
    }

    private void connect ( ) {
        try { 
            conn = new ServSock ( );
        } catch ( Exception e ) {
            Proc.log ( Proc.class.getName ( ) , e ); 
        } 
    }

    public static void reConnect ( ) {
        try { 
            Handler.unloadServices();
            conn = new ServSock ( );
            Handler.initServices();
        } catch ( Exception e ) {
            Proc.log ( Proc.class.getName ( ) , e ); 
        } 
    }


    private static void loadConf ( )  { 
         try { 
             config = new Config ( );
        } catch ( Exception ex ) { 
            Logger.getLogger ( Proc.class.getName ( )  ) .log ( Level.SEVERE, null, ex ); 
        } 
    }
    public static boolean rehashConf ( )  {
        Config conf = new Config ( );
        if ( conf.isValid ( )  )  {
            config = conf;
            if ( OperServ.isUp ( )  )  {
                Handler.getOperServ().updConf();
            }
            return true;
        }
        return false;
    }
     
    public static Config getConf ( )  { 
        return config; 
    }
 
    public static String getUptime ( )  {
        try {
            long duration   =  ( System.currentTimeMillis ( )  - Proc.start ) /1000;

            long year, month, week, day, hour, minute, second;
            long years, months, weeks, days, hours, minutes, seconds;

            second      = 1;
            minute      = 60  * second;
            hour        = 60  * minute;
            day         = 24  * hour;
            week        = 7   * day;
            month       = 30  * day;
            year        = 365 * day;


            years       = duration / year;
            duration    = duration % year;

            months      = duration / month;
            duration    = duration % month;

            weeks       = duration / week;
            duration    = duration % week;


            days        = duration / day;
            duration    = duration % day;

            hours       = duration / hour;
            duration    = duration % hour;

            minutes     = duration / minute;
            duration    = duration % minute;

            seconds     = duration;


            return   ""+ ( years    > 0  ? years+" Year(s), "    : "" ) +
                         ( months   > 0  ? months+" Month(s), "  : "" ) +
                         ( weeks    > 0  ? weeks+" Week(s), "    : "" ) +
                         ( days     > 0  ? days+" Day(s), "      : "" ) +
                         ( hours    > 0  ? hours+" Hour(s), "    : "" ) +
                         ( minutes  > 0  ? minutes+" Min(s), "   : "" ) +
                         ( seconds  < 10 ? "0"+seconds+" Sec(s)" : seconds+" Sec(s)" );
        
        } catch ( Exception e ) { 
            Proc.log ( Proc.class.getName ( ) , e ); 
        }
        return "";
    }
    
    public static void log ( String className, Exception e )  {
        Logger.getLogger ( className ) .log ( Level.SEVERE, null, e );   
    }
    
    public static void log ( String message )   { logger.out ( message );   }
    public static Version getVersion ( )        { return version;           }
    
    
    static ServSock getConn ( ) {
        return conn;
    }

    private void checkVersion() {
        DBChanges changes = new DBChanges ( Proc.version );
        
    }

}
