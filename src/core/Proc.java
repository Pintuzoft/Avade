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

import chanserv.ChanServ;
import server.ServSock;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import memoserv.MemoServ;
import nickserv.NickServ;
import operserv.OperServ;
import rootserv.RootServ;
import user.User;

/**
 *
 * @author DreamHealer
 */
public class Proc extends HashNumeric {
    private static Version              version = new Version ( ); /* Services-1.0.5 */
 
   // private ServSock                    conn;
    private static ServSock             conn;
    private static long                 lastConnectAttempt = 0;
    private static long                 stopDeadline = Long.MAX_VALUE; /* when STOP gives up on pending work */
    private static final long           RECONNECT_DELAY = 30000; /* ms between attempts to relink */

    private Handler                     handler;
    private static volatile boolean     run;
    private static volatile boolean     signalStop;     /* stopped by a signal (kill), not by RootServ STOP */
    private static volatile boolean     exiting;
    private static final CountDownLatch stopped = new CountDownLatch ( 1 );
    private static Log                  logger; 
    private static long                 start;
    private static long                 servicesStart;
    private long                        ticker; 
    private long                        minMaintenance;
    private long                        hourMaintenance;
    private long                        secMaintenance;
    private long                         minuteDelay;
    private long                         hourDelay; 
    private long                         secondDelay;

    private String                      read; 
    private static Config               config;
    
    /**
     *
     * @throws IOException
     */
    public Proc ( )  throws IOException {
        loadConf ( );
        logger                  = new Log ( );
        run                     = true;
        this.ticker             = System.nanoTime ( ); /* current time */
        this.checkVersion ( );  /* Check version and apply changes if neccessary */
        this.connect ( );
        
        this.handler            = new Handler ( );

        start                   = System.nanoTime();
        servicesStart           = System.currentTimeMillis();
        this.secMaintenance     = start;
        this.minMaintenance     = start;
        this.hourMaintenance    = start;
        this.secondDelay        = 1_000_000_000L; /* Every second */
        this.minuteDelay        = 60 * 1_000_000_000L; /* Every minute */
        this.hourDelay          = 60 * 60 * 1_000_000_000L; /* Every hour */
        this.stopOnSignal ( );
        this.runLoop ( );
    }
    
    /* A plain kill (SIGTERM) or ctrl-c stops services the same way as
       RootServ STOP: pending changes are written to the database first */
    private void stopOnSignal ( ) {
        Runtime.getRuntime().addShutdownHook ( new Thread ( ) {
            @Override
            public void run ( ) {
                if ( exiting ) {
                    return;
                }
                System.out.println ( "Got a signal to stop, writing pending changes to the database.." );
                signalStop = true;
                Proc.stopServices ( );
                try {
                    stopped.await ( 75, TimeUnit.SECONDS );
                } catch ( InterruptedException ex ) {
                    /* nothing more to do */
                }
            }
        } );
    }

    @SuppressWarnings ( "WaitWhileNotSynced" ) 
    private void runLoop ( )  {
        int counter = 0;
        long hourAgo = 0;
        long minAgo = 0;
        long secAgo = 0;
        int maxSleep = 500;
        int minSleep = 0;
        int todoAmount = 1;
        int sleep = 150;
        int sleepStep = 100;
        int commandChain = 0;
         
        while ( run || ( todoAmount > 0 && System.currentTimeMillis ( ) < stopDeadline ) )  {
            /* Proc loop */
            
            /* Dynamic Sleep */
            if ( commandChain == 0 ) {
                if ( sleep < maxSleep ) {
                    sleep += sleepStep;
                }
            } else {
                sleep = 0;
            }
            
            /* Delayed tasks (guest nicks, reminders..) run here on the main thread */
            if ( Scheduler.runDue ( ) > 0 ) {
                commandChain++;
            }
            
            this.read = ( Proc.conn != null ? Proc.conn.readLine() : null );
             
            if ( this.read != null )  {
                /* We found some new data, send it to the handler */ 
                this.handler.process ( this.read );
                commandChain++;
               
            } else {
                /* We didnt find any new data so lets take a nap */
                try {
                    /* Without a link the read above returns at once, never spin */
                    if ( sleep < 50 && ( Proc.conn == null || Proc.conn.isClosed ( ) ) ) {
                        sleep = 50;
                    }
                    Thread.sleep ( sleep );          
                } catch  ( Exception ex )  {
                    Logger.getLogger ( Proc.class.getName ( ) ) .log ( Level.SEVERE, null, ex );
                }
                if ( commandChain > 0 ) {
                    commandChain = 0;
                }
            }      
            /* HOUR */
            hourAgo = System.nanoTime ( )- this.hourDelay;
            if ( this.hourMaintenance < hourAgo )  {
                this.handler.runHourMaintenance ( );
                this.hourMaintenance = System.nanoTime ( );
            }
            
            /* MINUTE */
            minAgo = System.nanoTime ( ) - this.minuteDelay;
            if ( this.minMaintenance < minAgo )  {
                this.handler.runMinuteMaintenance ( );
                if ( Proc.conn != null && Proc.conn.timedOut() ) { /* Did we time out? */
                    Proc.log ( "Link to hub timed out, reconnecting" );
                    Proc.conn.disconnect();
                } else if ( Proc.conn != null && ! Proc.conn.isClosed ( ) && Proc.conn.quiet ( ) ) {
                    ServSock.sendCmd ( "PING :"+Proc.getConf().get ( NAME ) );
                }
                this.minMaintenance = System.nanoTime ( );
            }
            /* SECOND */
            secAgo = System.nanoTime() - this.secondDelay;
            if ( this.secMaintenance < secAgo )  {
                if ( Proc.conn == null || Proc.conn.isClosed() ) {
                    this.reconnectIfDue ( );
                }
                todoAmount = this.handler.runSecMaintenance ( );
                if ( Handler.sanityCheck ( ) ) {
                    Handler.initServices ( );
                }
                this.secMaintenance = System.nanoTime ( );
            }
            if ( todoAmount > 0 ) {
                commandChain++;
            }
        }
        /* Write everything that is pending, but never wait forever (the
           database could be down) */
        Handler.getRootServ().sendGlobOp ( "Writing pending changes to the database.." );
        int left = 0;
        while ( System.currentTimeMillis ( ) < stopDeadline + 30000 ) {
            left = this.handler.runHourMaintenance ( ) + 
                   this.handler.runMinuteMaintenance ( ) + 
                   this.handler.runSecMaintenance ( );
            if ( left == 0 ) {
                break;
            }
            try {
                Thread.sleep ( 200 );
            } catch ( InterruptedException ex ) {
                break;
            }
        }
        if ( left > 0 ) {
            Proc.log ( "Stopping with "+left+" changes that could not be written to the database" );
            Handler.getRootServ().sendGlobOp ( "WARNING: "+left+" changes could not be written to the database" );
        }
        
        Handler.getRootServ().sendGlobOp ( "SERVICES IS NOW STOPPED!..." );
        if ( Proc.conn != null ) {
            Proc.conn.disconnect();
        }
        exiting = true;
        stopped.countDown ( );
        if ( ! signalStop ) {
            System.exit ( 0 );
        }
        /* else: the JVM is already going down and waits for us to return */
    }

    /**
     *
     */
    public static void stopServices ( ) {
        run = false;
        stopDeadline = System.currentTimeMillis ( ) + 30000;
    }
    
    /**
     *
     * @return
     */
    public static long getStartTime ( ) {
        return start;
    }
    
    private void connect ( ) {
        lastConnectAttempt = System.currentTimeMillis ( );
        try { 
            conn = new ServSock ( );
        } catch ( Exception e ) {
            conn = null;
            Proc.log ( "Could not connect to the hub, retrying in "+( RECONNECT_DELAY / 1000 )+" seconds" );
        } 
    }

    /* Relink to the hub when the link is gone, without hammering it */
    private void reconnectIfDue ( ) {
        if ( System.currentTimeMillis ( ) - lastConnectAttempt < RECONNECT_DELAY ) {
            return;
        }
        Proc.log ( "Link to hub lost, reconnecting" );
        this.connect ( );
        if ( conn != null ) {
            /* New link: forget the old network state before the new burst is
               read and introduce the services again. The services stay loaded
               (with all registered nicks and chans) the whole time */
            Handler.resetNetwork ( );
            Handler.reintroduceServices ( );
        }
    }

    /**
     * Drop the link to the hub, the main loop reconnects
     */
    public static void reConnect ( ) {
        if ( conn != null ) {
            conn.disconnect ( );
        }
    }


    private static void loadConf ( )  { 
         try { 
             config = new Config ( );
        } catch ( Exception ex ) { 
            Logger.getLogger ( Proc.class.getName ( )  ) .log ( Level.SEVERE, null, ex ); 
        } 
        if ( config == null || ! config.isValid ( ) ) {
            System.out.println ( "Error: services.conf is missing or not valid, see the errors above." );
            System.exit ( 1 );
        }
    }

    /**
     *
     * @param user
     * @return
     */
    public static boolean rehashConf ( User user )  {
        Config conf = new Config ( );
        
        if ( conf.isValid ( )  )  {
            config = conf;
            if ( RootServ.isUp ( ) ) {
                Handler.getRootServ().setCommands();
            }
            if ( OperServ.isUp ( ) ) {
                Handler.getOperServ().setCommands();
            }
            if ( ChanServ.isUp ( ) ) {
                Handler.getChanServ().setCommands();
            }
            if ( NickServ.isUp ( ) ) {
                Handler.getNickServ().setCommands();
            }
           
            return true;
        }
        return false;
    }
     
    /**
     *
     * @return
     */
    public static Config getConf ( )  { 
        return config; 
    }
 
    /**
     *
     * @return
     */
    public static String getUptime ( )  {
        try {
            long duration   =  ( System.currentTimeMillis ( )  - Proc.servicesStart ) /1000;

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
                         seconds+" Sec(s)";
        
        } catch ( Exception e ) { 
            Proc.log ( Proc.class.getName ( ) , e ); 
        }
        return "";
    }
    
    /**
     *
     * @param className
     * @param e
     */
    public static void log ( String className, Exception e )  {
        Logger.getLogger(className).log ( Level.SEVERE, null, e );
        if ( e instanceof SQLException ) {
            String state = ((SQLException)e).getSQLState();
            if ( e instanceof java.sql.SQLTimeoutException || 
                 e instanceof java.sql.SQLRecoverableException ||
                 e instanceof java.sql.SQLNonTransientConnectionException ||
                 e instanceof java.sql.SQLTransientConnectionException ||
                 ( state != null && state.startsWith ( "08" ) ) ) {
                /* The connection itself is broken */
                Database.invalidate ( );
            }
            e.printStackTrace(System.err);
            System.err.println("SQLState: "+((SQLException)e).getSQLState());
            System.err.println("Error Code: "+((SQLException)e).getErrorCode());
            System.err.println("Message: "+e.getMessage());

            Throwable t = e.getCause();
            while(t != null) {
                System.out.println("Cause: " + t);
                t = t.getCause();
            }
        }
    }
    
    /**
     *
     * @param message
     */
    public static void log ( String message )   { logger.out ( message );   }

    /**
     *
     * @return
     */
    public static Version getVersion ( )        { return version;           }
    
    
    static ServSock getConn ( ) {
        return conn;
    }

    private void checkVersion() {
        /* Without the database we know no registered nicks or channels and
           would unidentify everyone, so wait for it before linking */
        while ( ! Database.activateConnection ( ) ) {
            Proc.log ( "Database not available, waiting for it before linking to the hub" );
            try {
                Thread.sleep ( 10000 );
            } catch ( InterruptedException ex ) {
                Thread.currentThread().interrupt ( );
                return;
            }
        }
        DBChanges changes = new DBChanges ( Proc.version );
        
    }

}
