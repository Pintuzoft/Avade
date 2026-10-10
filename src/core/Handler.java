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

import user.User;
import server.ServSock;
import server.Server;
import channel.Chan;
import channel.Topic;
import chanserv.CSLogEvent;
import chanserv.ChanInfo;
import rootserv.RootServ;
import operserv.CloneLimit;
import operserv.OperServ;
import mail.MXDatabase;
import memoserv.MemoServ;
import chanserv.ChanServ;
import command.Queue;
import guestserv.GuestServ;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import nickserv.NickInfo;
import nickserv.NickServ;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import operserv.ServicesBan;

/**
 *
 * @author DreamHealer
 */
public class Handler extends HashNumeric { 
    private static RootServ                     root;
    private static OperServ                     oper;
    private static NickServ                     nick;
    private static ChanServ                     chan;
    private static MemoServ                     memo;
    private static GuestServ                    guest;
    private static Service                      global;

    private Services                            services;
    private Trigger                             trigger;
    private static HashMap<BigInteger, User>    uList = new HashMap<>();
//    private static HashMap<BigInteger, ServicesID>    splitSIDs = new HashMap<>();
//    private static ArrayList<ServicesID>        splitSIDs = new ArrayList<>();
    private static ArrayList<ServicesID>        updServicesID = new ArrayList<>();
    private static HashMap<BigInteger,Chan>     cList = new HashMap<>();
    private static ArrayList<Server>            sList = new ArrayList<>();
    private static HashMap<BigInteger, ServicesID>    sidList = new HashMap<>();
    private static Database                     db; 
    private String[]                            data; 
    private String                              source;
    private static Date                         date;
    private HashString                          command;
    
    
    private static SimpleTimeZone               timeZone;
    private static Locale                       locale;
    private static SimpleDateFormat             sdf;
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 

    /* Maintenance Delays */
    private final static int DB_DELAY = 1; /* Delay in minutes */
    private long                            dbDelay; 
    private String                          buf; 
    private Queue                           cmdQueue;
    private static boolean                  sanity;
    private static int                      burstPings; /* PINGs seen since link, burst ends after the 2nd */
    private static boolean                  syncFinished;
    
    /**
     *
     */
    public Handler ( )  { 
        db              = new Database ( );
        Handler.initServices ( );
        if ( Handler.isDataLoaded ( ) ) {
            Database.loadSIDs ( );
        }
 //       Handler.printSIDs();
        this.trigger    = new Trigger ( );
        this.services   = new Services ( );
        date            = new Date ( );
        timeZone        = new SimpleTimeZone ( 0, "GMT" );
        locale          = new Locale ( "en", "GB" );
        sdf             = new SimpleDateFormat ( "EEE dd MMMM yyyy HH:mm:ss zzz", locale );
        sdf.setTimeZone ( timeZone );
        dbDelay         = System.currentTimeMillis ( ) + ( 60 * DB_DELAY );
        this.cmdQueue   = new Queue ( )  {};
    }
    
    /**
     *
     */
    public static void initServices ( )  {
        initService ( ROOTSERV, root );
        initService ( OPERSERV, oper );
        initService ( GUESTSERV, guest );
        initService ( NICKSERV, nick );
        initService ( CHANSERV, chan );
        initService ( MEMOSERV, memo );
        initService ( GLOBAL, global );
        sanity = true;
        Database.activateConnection();
    }
    
    /**
     *
     */
    /**
     * Forget everything we know about the network (users, channels and
     * servers), used before we relink to the hub and get a new burst
     */
    private static void addServer ( Server server ) {
        /* A SERVER line we could not parse has no name */
        if ( server.getName ( ) != null ) {
            sList.add ( server );
            if ( syncFinished ) {
                sendUhmSalt ( );    /* a server that linked later needs the salt too */
                if ( oper != null ) {
                    oper.sendJoinRequests ( server );
                }
            }
        }
    }

    /**
     * @return true when all registered nicks and channels are loaded. Until
     *         then services must not touch anyone's identification or access.
     */
    private static int uhmType = 0;
    private static int uhmUmodeH = 0;

    /* SVSUHM <type> [umodeH]: without the second value the ircd keeps what it had */
    private static void setUhm ( String type, String umodeH ) {
        try {
            uhmType = Integer.parseInt ( type );
            if ( umodeH != null ) {
                uhmUmodeH = Integer.parseInt ( umodeH );
            }
        } catch ( NumberFormatException ex ) {
            uhmType = 0;
        }
    }

    /**
     * What the ircd shows as the host of this user when the network masks
     * hosts with the avade_uhm module (SVSUHM type 1).
     * @param host
     * @param ip
     * @return the masked host, or null when hosts are not masked or we have no salt
     */
    public static String maskedHost ( String host, String ip ) {
        String salt = Proc.getConf().getUhmSalt ( );
        if ( uhmType != 1 || salt == null || host == null ) {
            return null;
        }
        return HostMask.mask ( salt, Proc.getConf().getUhmPrefix ( ), host, ip );
    }

    /**
     * Give the salt of the host-masking to the modules on all servers. The
     * ircd passes the line on to every server, sending it again is harmless.
     */
    public static void sendUhmSalt ( ) {
        String salt = Proc.getConf().getUhmSalt ( );
        if ( salt != null && oper != null ) {
            oper.sendCmd ( "MODULE CGLOBAL avade_uhm SALT "+salt+" "+Proc.getConf().getUhmPrefix ( ) );
        }
    }

    /**
     * @return how users may use umode +H (SVSUHM): 0 = not at all,
     *         1 = set for everyone when they connect, 2 = allowed, not automatic
     */
    public static int getUhmUmodeH ( ) {
        return uhmUmodeH;
    }

    /**
     * The network setting changed (OperServ UHM), the ircd tells all servers but not us
     * @param type
     * @param umodeH
     */
    public static void setUhm ( int type, int umodeH ) {
        uhmType     = type;
        uhmUmodeH   = umodeH;
    }

    /**
     * @return the user host-masking type of the network (SVSUHM), 0 = none.
     *         When the ircd masks hosts we must never show a user's real host.
     */
    public static int getUhmType ( ) {
        return uhmType;
    }

    public static boolean isDataLoaded ( ) {
        return NickServ.isLoaded ( ) && ChanServ.isLoaded ( );
    }

    private static long lastLoadAttempt = 0;
    private static long lastUnknownJoin = 0;    /* ms, the last time we told about one */

    /* Nicks or channels failed to load at start, try again every 30 seconds */
    private void retryLoadIfNeeded ( ) {
        if ( isDataLoaded ( ) || nick == null || chan == null ||
             System.currentTimeMillis ( ) - lastLoadAttempt < 30000 ) {
            return;
        }
        lastLoadAttempt = System.currentTimeMillis ( );
        if ( ! NickServ.isLoaded ( ) ) {
            nick.retryLoad ( );
        }
        if ( NickServ.isLoaded ( ) && ! ChanServ.isLoaded ( ) ) {
            chan.retryLoad ( );
        }
        if ( isDataLoaded ( ) ) {
            this.onDataLoaded ( );
        }
    }

    /* Everything we held back while the data was missing */
    private void onDataLoaded ( ) {
        Proc.log ( "Nicks and channels loaded, checking all users" );
        oper.sendGlobOp ( "Nicks and channels loaded from the database, checking all users" );
        Database.loadSIDs ( );
        /* The setting could not be read either, and the servers may still
           send join requests from before */
        OperServ.loadJoinRequests ( );
        if ( isSynced ( ) ) {
            oper.sendJoinRequests ( null );
        }
        for ( User u : new ArrayList<> ( uList.values ( ) ) ) {
            ServicesID sid = null;
            if ( u.getServiceStamp ( ) > 999 && ( sid = findSid ( u.getServiceStamp ( ) ) ) != null && sid != u.getSID ( ) ) {
                /* The one the user got while the data was missing has no
                   user any more: forget it, it would never expire */
                if ( u.getSID ( ) != null ) {
                    u.getSID().resetTimers ( );
                    u.getSID().remUser ( );
                    sidList.remove ( u.getSID().getCode ( ) );
                    updServicesID.remove ( u.getSID ( ) );
                }
                u.setSID ( sid );
                sid.addUser ( u );
            }
            NickInfo ni = NickServ.findNick ( u.getName ( ) );
            /* +r is trusted when we have nothing on the session, see doNick */
            if ( ni != null && sid == null && u.getModes().is ( IDENT ) ) {
                u.getSID().add ( ni );
            }
            NickServ.fixIdentState ( u );
        }
        for ( Chan c : new ArrayList<> ( cList.values ( ) ) ) {
            c.addCheckUsers ( );
        }
        if ( isSynced ( ) ) {
            root.fixMaster ( );
        }
    }

    public static void resetNetwork ( ) {
        for ( User u : uList.values ( ) ) {
            if ( u.getSID ( ) != null ) {
                u.getSID().remUser ( );
            }
        }
        uList.clear ( );
        cList.clear ( );
        sList.clear ( );
        ChanServ.clearCheckUsers ( );
        resetSync ( );
    }

    /**
     * Introduce the loaded services on the network again after a relink.
     * Registered nicks and channels stay loaded, reloading them would give
     * new objects that identified users and services IDs don't point at.
     */
    public static void reintroduceServices ( ) {
        Service[] services = { root, oper, nick, chan, memo, guest, global };
        for ( Service s : services ) {
            if ( s != null ) {
                s.introduce ( );
            }
        }
        /* Load anything that was never loaded */
        Handler.initServices ( );
    }

    /* A service that is killed (a nick collision, a server KILL) comes back
       at once, not first at the next relink */
    private static void reintroduceIfService ( String target ) {
        HashString name = new HashString ( target );
        Service[] services = { root, oper, nick, chan, memo, guest, global };
        for ( Service s : services ) {
            if ( s != null && s.getName().is ( name ) ) {
                s.introduce ( );
            }
        }
    }

    
    /**
     *
     * @param type
     * @param service
     */
    public static void initService ( HashString type, Service service ) {
        if ( service == null ) {
            if ( type.is(ROOTSERV) ) {
                root = new RootServ ( );
            
            } else if ( type.is(OPERSERV) ) {
                oper = new OperServ ( );
            
            } else if ( type.is(NICKSERV) ) {
                nick = new NickServ ( );
            
            } else if ( type.is(CHANSERV) ) {
                chan = new ChanServ ( );
            
            } else if ( type.is(MEMOSERV) ) {
                memo = new MemoServ ( );
            
            } else if ( type.is(GUESTSERV) ) {
                guest = new GuestServ ( );
            
            } else if ( type.is(GLOBAL) ) {
                global = new Service ( "Global" ) {};
            }
        } 
    }
    
    /**
     *
     * @param read
     */
    public void process ( String read )  {
        User uBuf;
        NickInfo nBuf;
        this.data   = null; 
        this.data   = read.split ( " " ); 
        /* Never echo private messages or notices, they carry passwords for
           the services, nor the password of the link */
        if ( this.data.length < 2 || 
             ! ( this.data[1].equalsIgnoreCase ( "PRIVMSG" ) || 
                 this.data[1].equalsIgnoreCase ( "NOTICE" )  || 
                 this.data[0].equalsIgnoreCase ( "PASS" ) ) ) {
            System.out.println ( read );
        }
         
        try { 
            if ( this.data[0].isEmpty ( ) ) { 
                return;
            }
        } catch ( Exception e )  {
            Proc.log ( Handler.class.getName ( ) , e );
            return;
        }
         
        try {
            if ( this.data[0].contains ( ":" )  )  {
                /* We are reading from a server or user */
                this.source = this.data[0].substring ( 1 );
                if ( this.source.contains ( "." )  )  { 
                    /* Server stuff */
                    this.command = new HashString ( this.data[1] );
                    
                    if ( command.is(SERVER) ) {
                        addServer ( new Server ( this.data ) );
                        root.sendPanic();
                        
                    } else if ( command.is(SJOIN) ) {
                        doChan ( true );

                    } else if ( command.is(TOPIC) ) {
                        doTopic ( this.data );

                    } else if ( command.is(LUSERSLOCK) ) {
                        doFinishSync ( this.data );

                    } else if ( command.is(GLOBOPS) ) {
                        doGlobOps ( this.data );

                    } else if ( command.is(OS) ) {
                        doOS ( this.data );

                    } else if ( command.is(SVSUHM) ) {
                        /* :server SVSUHM <type> [umodeH] */
                        setUhm ( this.data.length > 2 ? this.data[2] : "0", this.data.length > 3 ? this.data[3] : null );

                    } else if ( command.is(SQUIT) ) {
                        /* :hub SQUIT leaf :reason */
                        this.doSquit ( this.data[2] );

                    } else if ( command.is(KILL) ) {
                        /* :server KILL nick :reason (collisions, opers) */
                        User u;
                        if ( ( u = Handler.findUser ( this.data[2] ) ) != null ) {
                            deleteUser ( u );
                        } else {
                            reintroduceIfService ( this.data[2] );
                        }

                    } else if ( command.is(KICK) ) {
                        doKick ( null );

                    } else if ( command.is(MODE) && isChanName ( this.data[2] ) ) {
                        doMode ( null );
                    }
                    
                } else {
                    /* User stuff */ 
                    
                    if ( this.data.length < 3 || this.data[2].isEmpty ( )  )  { 
                        return; /* nothing we use is that short (":nick AWAY" when coming back) */
                    }
                    
                    User user; 
                    user = findUser ( this.source );
                    
                    this.command = new HashString ( this.data[1] );
                    
                    if ( user == null && this.command.is(SJR) ) {
                        /* Someone we have no record of (we never saw the NICK
                           line, which must not happen) asks to join. We know
                           no host to match bans with and can not tell what the
                           user is identified to, so there is nothing to decide
                           with: no way in, like for anyone we can not clear.
                           Without an answer the ircd would say nothing at all,
                           so tell the user what helps, and the staff that it
                           happened. */
                        if ( this.data.length > 3 ) {
                            chan.sendServ ( "474 "+this.source+" "+this.data[3]+" :Cannot join channel (services have no record of your connection)" );
                            long now = System.currentTimeMillis ( );
                            if ( now - lastUnknownJoin > 60000 ) {
                                lastUnknownJoin = now;
                                chan.sendRaw ( "NOTICE "+this.source+" :Services have lost track of your connection and can not check your joins. Please reconnect to the network." );
                                Proc.log ( "Join request from "+this.source+", who is unknown to services, for "+this.data[3]+": refused" );
                                oper.sendGlobOp ( "Join request from "+this.source+", who is unknown to services: refused. Please report this, it should not happen." );
                            }
                        }
                        return;
                    }
                    if ( user == null && ! this.command.is(KILL) ) {
                        return; /* not a user we know of */
                    }
                     
                    if ( this.command.is(PRIVMSG) ) {
                        /* Ignored users are not answered, but everything else
                           they do (quit, nick, modes..) must still be tracked */
                        if ( ! oper.isIgnored ( user ) ) {
                            doPrivmsg ( user );
                        }
                    
                    } else if ( this.command.is(JOIN) ) {
                        /* The only JOIN servers get: the user left all channels */
                        if ( this.data[2].equals ( "0" ) ) {
                            user.partAll ( );
                        }
                    
                    } else if ( this.command.is(MODE) ) {
                        doMode ( user );
                    
                    } else if ( this.command.is(SJOIN) ) {
                        doSJoin ( user );
                    
                    } else if ( this.command.is(SJR) ) {
                        /* :nick SJR <invited> #chan [:key], the user waits for our answer */
                        chan.joinRequest ( user, this.data );
                    
                    } else if ( this.command.is(TOPIC) ) {
                        doTopic ( user, this.data );
                    
                    } else if ( this.command.is(PART) ) {
                        doPart ( user );
                    
                    } else if ( this.command.is(KICK) ) {
                        doKick ( user );
                    
                    } else if ( this.command.is(QUIT) ) {
                        deleteUser ( user );
                    
                    } else if ( this.command.is(KILL) ) {
                            User u;
                            if ( ( u = Handler.findUser ( this.data[2] ) ) != null ) {
                                deleteUser ( u );
                            } else {
                                reintroduceIfService ( this.data[2] );
                            }
                    
                    } else if ( this.command.is(NICK) ) {
                        doNick ( user );
                        
                    } else if ( this.command.is(MOTD) ) {
                        this.services.parse ( user, this.data );
                    
                    } else if ( this.command.is(VERSION) ) {
                        this.services.parse ( user, this.data );
                    
                    } else if ( this.command.is(INFO) ) {
                        this.services.parse ( user, this.data );
                    
                    } else if ( this.command.is(STATS) ) {
                        this.services.parse ( user, this.data );
                    }
                     
                    // :DreamHealer NICK fr :1321662151
                    // :DreamHea1er QUIT :Quit: leaving         
                    // :DreamHealer MODE #friends 1320518761 -o DreamHealer

                }

            } else {
                /* We are getting  */

                this.command = new HashString ( this.data[0] );
                
                if ( this.command.is(SERVER) ) {
                    addServer ( new Server ( this.data ) );
                    root.sendPanic();
                
                } else if ( this.command.is(SJOIN) ) {
                    this.doChan ( false );
                
                } else if ( this.command.is(SQUIT) ) {
                    this.doSquit ( this.data[1] );

                } else if ( this.command.is(SVSUHM) ) {
                    /* SVSUHM <type> <umodeH>, sent by the hub when we link */
                    setUhm ( this.data.length > 1 ? this.data[1] : "0", this.data.length > 2 ? this.data[2] : null );
                
                } else if ( this.command.is(SVINFO) ) {
                    this.doSVInfo ( );
                
                } else if ( this.command.is(PING) ) {
                    this.doPing ( );
                
                } else if ( this.command.is(NICK) ) {
                    this.doNick ( );
                
                } else if ( this.command.is(SF) ) {
                    this.doSF ( );
                
                } else if ( this.command.is(ERROR) ) {
                    this.doError ( );
                }
                 
            }
        } catch ( Exception e )  {
            Proc.log ( Handler.class.getName ( ) , e );
        }
        
    }
    
    /*****************************/
    
    private void doChan ( boolean check ) {
        Chan c;
        ChanInfo ci;
        if ( ( c = Handler.findChan ( this.data[3] ) ) != null ) {
            boolean reset = false;
            try {
                reset = c.lostTo ( Long.parseLong ( this.data[2] ) );
            } catch ( NumberFormatException ex ) {
                /* keep the one we have */
            }
            c.addUserList(data, 5);
            if ( reset ) {
                /* Everyone lost their status: give it back to those with access */
                c.addCheckUsers ( );
            }
        } else {
            c = new Chan ( this.data );
            c.addUserList(data, 5);
            if ( c.empty ( ) ) {
                /* Nobody we know: the one who made it is gone already (killed
                   by us while this line was on its way). Nothing would ever
                   remove a channel without members. */
                return;
            }
            cList.put ( c.getName().getCode(), c );
            if ( check ) {
                chan.checkSettings ( c );
            }
            
        }
        
    }

    /**
     * A channel that someone created through a join request (AJ). The ircd
     * tells services nothing about it, so add it the way its SJOIN would.
     * @param name
     * @param stamp the TS we gave the channel
     * @param user the one who joined, and got op
     */
    public static void newChan ( String name, long stamp, User user ) {
        String[] sjoin = { ":"+Proc.getConf().get ( NAME ), "SJOIN", ""+stamp, name, "+", ":@"+user.getNameStr ( ) };
        Chan c = new Chan ( sjoin );
        c.addUserList ( sjoin, 5 );
        cList.put ( c.getName().getCode(), c );
        chan.checkSettings ( c );
    }
    
    //     :Guest12203 PRIVMSG NickServ@services.sshd.biz :identify asd.
    private void doPrivmsg ( User user )  {
        int at = this.data[2].lastIndexOf ( "@" );
        HashString service = new HashString ( at > 0 ? this.data[2].substring ( 0, at ) : this.data[2] );
        
        if ( service.is(ROOTSERV) ) {
            if ( RootServ.isUp ( )  )  { 
                Handler.root.parse ( user, this.data );
            } else { 
                this.sendNoSuchNick ( user, "RootServ" ); 
            }

        } else if ( service.is(OPERSERV) ) {
            oper.parse ( user, this.data );

        } else if ( service.is(CHANSERV) ) {
            if ( ChanServ.isUp ( ) ) { 
                chan.parse ( user, this.data );
            } else { 
                this.sendNoSuchNick ( user, "ChanServ" ); 
            } 
            
        } else if ( service.is(NICKSERV) ) {
            if ( NickServ.isUp ( ) ) { 
                nick.parse ( user, this.data ); 
            } else { 
                this.sendNoSuchNick ( user, "NickServ" );
            }
            
        } else if ( service.is(MEMOSERV) ) {
            if ( MemoServ.isUp ( ) ) { 
                memo.parse ( user, this.data ); 
            } else { 
                this.sendNoSuchNick ( user, "MemoServ" ); 
            } 
        }
         
    }
    
    private void doOS ( String[] data ) {
        HashString command = new HashString ( data[2] );
        if ( command.is(SFAKILL) ) {
            oper.addSFAkill ( data );
        } 
    }
      
    private void doSF ( ) {
        // SF *hello*hello*hello* 315441 :test
        if ( ! oper.isSpamFiltered ( data[1] ) ) {
            oper.sendServ ( "SF "+data[1]+" 0" );
        }
    }
    
    private void doSquit ( String name )  {
        Server s = findServer ( name );
        if ( s != null )  {
            if ( s.getLink ( )  != null )  {
                s.getLink().remServer ( s ); /* remove server from leaf list on hub */
            }
            s.recursiveDelete ( );
        }
        sList.remove ( s );
    }
    private void doSVInfo ( )  {
        Proc.log ( "DEBUG: connection established in: "+ ( System.nanoTime() - Proc.getStartTime()  ) +"ns" );
    }
    private void doPing ( )  {
        this.pong ( this.data[1] );
        /* bahamut ends the user/channel burst with a PING, then sends the
           topic burst followed by another PING. After the 2nd we are synced */
        if ( burstPings < 2 ) {
            burstPings++;
            if ( burstPings == 2 ) {
                this.finishSync ( );
            }
        }
    }

    /**
     * Called when we (re)link to the hub
     */
    public static void resetSync ( ) {
        burstPings = 0;
        syncFinished = false;
    }

    /**
     * @return true when the hub has finished bursting users, channels and topics
     */
    public static boolean isSynced ( ) {
        return burstPings >= 2;
    }
    
    
//    private static ServicesID findSplitSID ( long servicesID ) {
//        HashString target = new HashString ( ""+servicesID );
//        ServicesID sid = splitSIDs.get ( target );
//        if ( sid != null ) {
//            splitSIDs.remove ( sid.getCode() );
//        }
//        return sid;
//    }

    
    /* New nick on the network */
    private void doNick ( )  {
        User u = new User ( this.data );
        ServicesID sid = null;
        //NICK DreamHealer 1 1532897366 +oiCra fredde DreamHealer.ircop testnet.avade.net 965942 167772447 :a figment of your own imagination
        try {
            /* The services ID (servicestamp) we gave the user with SVSMODE +d,
               it lets us restore what the user was identified to after a
               split or a services restart */
            long serviceID = Long.parseLong ( this.data[8] );
            if ( serviceID > 999 ) {
                u.setSID ( Handler.findSid ( serviceID ) );
            }
            
        } catch ( NumberFormatException ex ) {
            Proc.log ( Handler.class.getName ( ), ex );
        }
        
        boolean known = ( u.getSID ( ) != null );
        if ( u.getSID() == null ) {
            u.setSID ( new ServicesID ( ) );
            Handler.newSid ( u.getSID() );
        }  
        u.getSID().addUser ( u );
        
        NickInfo ni = NickServ.findNick ( u.getName ( ) );
        
        /* Only services can set +r, so it is trusted for the current nick
           when we have nothing on the session (its row was lost). A session
           we know says itself what it is identified to: the nick can have
           been dropped and registered by someone else while this user was
           split away. */
        if ( ni != null && ( u.getSID().isIdentified ( ni ) || ( ! known && u.getModes().is ( IDENT ) ) ) ) {
            u.getSID().add ( ni );
        } else if ( Handler.isDataLoaded ( ) ) {
            /* (without the registered nicks we can't tell, leave +r alone) */
            Handler.getNickServ().sendCmd ( "SVSMODE "+u.getString ( NAME )+" 0 -r" );
            u.getModes().set ( IDENT, false );
        }
        
        /* Known from here on: what follows looks the user up (a frozen nick
           unidentifies everyone on it), and a failure there must not leave
           someone the ircd has that we do not */
        uList.put ( u.getName().getCode(), u );
        NickServ.fixIdentState ( u );
        
        Server s = findServer ( this.data[7] );
        if ( s != null ) {
            s.addUser ( u );
        }

        checkTrigger ( u );
        this.oper.checkUser ( u ); /* Add user in OperServ check queue (akills etc) */
    }
    
    private static void checkTrigger ( User user ) {
        int ipCount = 0;
        int rangeCount = 0;
        User u = null;
        if ( OperServ.isWhiteListed(user.getMask()) ) {
            return;
        }
        if ( user.getHostInfo().isUnknown ( ) ) {
            /* No IP from the ircd, counting would lump all such users together */
            return;
        }
        for ( HashMap.Entry<BigInteger,User> entry : uList.entrySet() ) {
            u = entry.getValue();
            if ( u.ipMatch ( user.getHostInfo().getIpHash() ) ) {
                if ( u.getModes().is(OPER) ) {
                    return;
                }
                ++ipCount;
            } 
            if ( u.rangeMatch ( user.getHostInfo().getRangeHash() ) ) {
                if ( u.getModes().is(OPER) ) {
                    return;
                }
                ++rangeCount;
            }
        }
        /* A clone limit set with OperServ CLONE replaces the trigger for that
           ip/range, and like in the ircd a host limit exempts from the site */
        CloneLimit ipLimit = OperServ.findCloneLimit ( user.getIp ( ) );
        CloneLimit rangeLimit = OperServ.findCloneLimit ( user.getHostInfo().getRange ( ) );
        if ( ipLimit != null ) {
            rangeCount = 0;
            if ( ipCount <= ipLimit.getLimit ( ) ) {
                ipCount = 0;
            }
        }
        if ( rangeLimit != null && rangeCount <= rangeLimit.getLimit ( ) ) {
            rangeCount = 0;
        }
        String reason;
        if ( Trigger.isWarn() ) {
            /* WARN */
            if ( ipCount > Trigger.getWarnIP() ) {
                if ( ipCount % 10 == 0 ) {
                    oper.sendGlobOp ( "Warning! possible clones: "+ipCount+" clients from ip: *!*@"+user.getIp() );
                }
            } else if ( rangeCount > Trigger.getWarnRange() ) {
                if ( rangeCount % 10 == 0 ) {
                    oper.sendGlobOp ( "Warning! possible clones: "+rangeCount+" clients from range: *!*@"+user.getHostInfo().getRange() );
                }
            }
        }
        /* ACTION */
        if ( Trigger.getAction().is(AKILL) ) {
            if ( ipCount > Trigger.getActionIP() ) {
                String stamp = dateFormat.format ( new Date ( ) );
                String percent;
                HashString id;
                HashString mask;
                String expire = Handler.expireToDateString ( stamp, "30m" );
                reason = "Cloning. Too many clients found from this IP. 30 min ban.";
                id = new HashString ( ""+System.nanoTime() );
                mask = new HashString ( "*!*@"+user.getIp() );
                ServicesBan ban = new ServicesBan ( AKILL, id, false, mask, reason, "OperServ", null, expire );
                percent = String.format("%.02f", (float) ipCount / Handler.getUserList().size() * 100 );
                if ( ! OperServ.isWhiteListed ( ban.getMask() ) ) {
                    OperServ.addServicesBan ( ban );
                    Handler.getOperServ().sendServicesBan ( ban );
                    oper.sendGlobOp ( "AKILL: *!*@"+user.getIp()+" placed for cloning. Affecting "+ipCount+" users ["+percent+"%]" );
                }
            } else if ( rangeCount > Trigger.getActionRange() ) {
                String stamp = dateFormat.format ( new Date ( ) );
                String percent;
                HashString id;
                HashString mask;
                String expire = Handler.expireToDateString ( stamp, "30m" );
                id = new HashString ( ""+System.nanoTime() );
                mask = new HashString ( "*!*@"+user.getHostInfo().getRange() );
                reason = "Cloning. Too many clients found from this IP-range. 30 min ban.";
                ServicesBan ban = new ServicesBan ( AKILL, id, false, mask, reason, "OperServ", null, expire );
                percent = String.format("%.02f", (float) rangeCount / Handler.getUserList().size() * 100 );
                if ( ! OperServ.isWhiteListed ( ban.getMask() ) ) {
                    OperServ.addServicesBan ( ban );
                    Handler.getOperServ().sendServicesBan ( ban );
                    oper.sendGlobOp ( "AKILL: *!*@"+user.getHostInfo().getRange()+" placed for cloning. Affecting "+rangeCount+" users ["+percent+"%]" );
                }
            }
        } else if ( Trigger.getAction().is(KILL) ) {
           if ( ipCount > Trigger.getActionIP() ) {
                reason = "Cloning. Too many clients found from this IP.";
                oper.sendGlobOp ( "KILL: "+user.getFullMask()+" for cloning." );
                oper.sendRaw ( "KILL "+user.getName()+" :"+reason );
                Handler.deleteUser ( user );
            } else if ( rangeCount > Trigger.getActionRange() ) {
                reason = "Cloning. Too many clients found from this IP-range.";
                oper.sendGlobOp ( "KILL: "+user.getFullMask()+" for cloning." );
                oper.sendRaw ( "KILL "+user.getName()+" :"+reason );
                Handler.deleteUser ( user );
            }
        }

        
        
        
        
        
        
        
    }
    
    
    /* user wants to change nick */
    private void doNick ( User user )  {
        NickInfo ni;
        User u;
        if ( user == null ) {
            return;
        }
        
        if ( this.data.length >= 3 ) {
            uList.remove ( user.getName().getCode() );
            user.setName ( this.data[2] );
            uList.put ( user.getName().getCode(), user );
        }
        if ( this.data.length >= 4 ) {
            /* :old NICK new :<ts> */
            try {
                user.setNickStamp ( Long.parseLong ( this.data[3].startsWith ( ":" ) ? this.data[3].substring ( 1 ) : this.data[3] ) );
            } catch ( NumberFormatException ex ) {
                /* keep the one we have */
            }
        }
        ni = NickServ.findNick ( user.getName ( )  );
        user.getModes().set ( IDENT, user.isIdented ( ni ) );
        nick.fixIdentState ( user );
        for ( Chan c : user.getChans ( ) ) {
            ChanServ.addCheckUser ( c, user );
        }
        this.oper.checkUser ( user ); /* Add user in OperServ check queue (akills etc) */
    }
    
    private void doSJoin ( User user )  {
        /* User joined a channel */
        Chan c;
        ChanInfo ci;
        
        if ( user == null ) {
            return;
        }
        if ( (c = findChan ( this.data[3] )) == null ) {
            /* ":nick SJOIN ts #chan" for a channel we do not have. The ircd
               sends that form for a channel that exists, so we missed it:
               learn it from this join, its modes are not known */
            String[] sjoin = { this.data[0], "SJOIN", this.data[2], this.data[3], "+", ":"+user.getNameStr ( ) };
            c = new Chan ( sjoin );
            cList.put ( c.getName().getCode(), c );
            chan.checkSettings ( c );
            if ( findChan ( this.data[3] ) == null ) {
                return; /* a closed channel: everyone was removed from it */
            }
        }
        if ( ! c.nickIsPresent ( user.getName ( ) ) ) {
            c.countJoin ( );
        }
        c.addUser ( USER, user );
        user.addChan ( c );
        if ( ! c.isSaJoin() ) {
            ChanServ.addCheckUser ( c, user );
        } else {
            c.toggleSaJoin();
        }
    }
     
    private void doPart ( User user )  {
        /* User parted a channel */
        Chan c = findChan ( this.data[2] );
        if ( c != null )  {
            c.remUser ( user );
            user.remChan ( c );
            deleteEmpty ( c );
        }
    }
    
    private void doKick ( User user )  {
         /* User parted a channel */
        User target;
        Chan c = findChan ( this.data[2] );
        if ( c != null )  {
            target = findUser ( this.data[3] );
            if ( target != null )  {
                c.remUser ( target );
                target.remChan ( c );
                deleteEmpty ( c );
            }
        }
    }

    /*****************************/

    private void doMode ( User user )  {
        if ( Handler.isChanName ( this.data[2] ) ) {
            Chan c = findChan ( this.data[2] );
            if ( c != null ) {
                ChanInfo ci = ChanServ.findChan ( c.getName ( ) );
                c.getModes().set ( MODE, this.data );
                c.chMode ( this.data );
                Handler.getChanServ().checkModes ( c, ci );
            }
        } else {
            user.getModes().set ( MODE, this.data );
            this.forceOperModes ( user );
        }
    }

    /**
     *
     * @param name
     * @return
     */
    public static boolean isChanName ( String name ) {
        return name.substring(0,1).matches ( Pattern.quote ( "#" ) );
    }
    private void doGlobOps ( String[] data ) {
        // :testnet.avade.net GLOBOPS :DreamHealer used SAJOIN (#fredde +b)
        //                  0       1            2    3      4       5+
        CSLogEvent log;
        /* Only "<nick> used SAJOIN/SAMODE (#chan ...)" globops are of interest */
        if ( this.data.length < 7 || ! this.data[3].equals ( "used" ) ) {
            return;
        }
        User user = Handler.findUser ( this.data[2].substring ( 1 ) );
        HashString command = new HashString ( this.data[4] );
        Chan chan = Handler.findChan ( this.data[5].replace ( "(", "" ).replace ( ")", "" ) );
        String string = Handler.cutArrayIntoString ( this.data, 6 ).replace(")", "");
        String oper = ( user != null && user.getOper() != null ? user.getOper().getNameStr() : this.data[2].substring ( 1 ) );
        
        if ( chan == null ) {
            return;
        }
        if ( command.is(SAJOIN) ) {
            log = new CSLogEvent ( chan.getString(NAME), SAJOIN, string, oper );
            ChanServ.addLog ( log );
            chan.toggleSaJoin();
        
        } else if ( command.is(SAMODE) ) {
            log = new CSLogEvent ( chan.getString(NAME), SAMODE, string, oper );
            ChanServ.addLog ( log );
        }
         
    }

    /* Take array and cut it into string starting at position. Good for 
       reading in reasons, comments, messages */

    /**
     *
     * @param data
     * @param pos
     * @return
     */

    public static String cutArrayIntoString ( String[] data, int pos ) {
        if ( data == null || data.length <= pos ) {
            return null;
        }
        String buf = String.join ( " ", data );
        String[] arr = buf.split ( " ", pos+1 );
        return arr[pos];
    }
    
    /**
     *
     * @param user
     */
    public static void forceOperModes ( User user ) {
        if ( Proc.getConf().getBoolean ( FORCEMODES ) ) {
            if ( user.getModes().is ( OPER ) && ! user.isAtleast ( IRCOP ) ) {
                user.getModes().set ( OPER, false );
                user.getModes().set ( SADMIN, false );
                user.getModes().set ( ADMIN, false );
                Handler.getOperServ().sendRaw ( ":"+Proc.getConf().get ( NAME )+" SVSMODE "+user.getString ( NAME )+" 0 -ockydegbaAfnmhWjK" );
                Handler.getOperServ().sendRaw ( ":"+Proc.getConf().get ( NAME )+" GLOBOPS :forcefully removed oper mode (o) from: "+user.getFullMask()+"." );
            } else if ( user.getModes().is ( SADMIN ) && ! user.isAtleast ( SA ) ) {
                user.getModes().set ( SADMIN, false );
                Handler.getOperServ().sendRaw ( ":"+Proc.getConf().get ( NAME )+" SVSMODE "+user.getString ( NAME )+" 0 -a" );
                Handler.getOperServ().sendRaw ( ":"+Proc.getConf().get ( NAME )+" GLOBOPS :forcefully removed services admin mode (a) from: "+user.getFullMask()+"." );
            }
        }
    }
    
    private void pong ( String target )  { 
        try { 
            this.sendCmd ( ":"+Proc.getConf().get ( NAME ) +" PONG "+Proc.getConf().get ( NAME ) +" "+target ); 
        } catch ( Exception e ) {
            Proc.log ( Handler.class.getName ( ) , e );
        } 
    }

    private void sendCmd ( String s )  { 
        try { 
            ServSock.sendCmd ( s ); 
        } catch ( Exception e )  {
            Proc.log ( Handler.class.getName ( ) , e );
        } 
    }

    /**
     *
     * @param sid
     */
    public static void newSid ( ServicesID sid )  {
        try { 
            sidList.put ( sid.getCode(), sid ); 
        } catch ( Exception e )  {
            Proc.log ( Handler.class.getName(), e );
        }
    }
    
    /**
     *
     * @param name
     * @return
     */
    public static User findUser ( String name ) {
        return findUser ( new HashString ( name ) );
    }
    
    /**
     *
     * @param name
     * @return
     */
    public static User findUser ( HashString name ) {
        //for ( HashMap.Entry<BigInteger,User> entry : uList.entrySet() ) {
        //    if ( entry.getValue().is(name) )  {
        //        return entry.getValue();
        //    }
        //}
         
        //return null;
        
        return ( uList.containsKey(name.getCode()) ? uList.get(name.getCode()) : null );
    }
    
    /**
     *
     * @param name
     * @return
     */
    public static Chan findChan ( String name ) {
        return findChan ( new HashString ( name ) );
    }
    
    /**
     *
     * @param name
     * @return
     */
    public static Chan findChan ( HashString name )  {
        //for ( HashMap.Entry<BigInteger,Chan> entry : cList.entrySet() ) {
        //    if ( entry.getValue().is(name) )  {
        //        return entry.getValue();
        //    }
        //}
        //return null;
        
        return ( cList.containsKey(name.getCode()) ? cList.get(name.getCode()) : null );
    }
    
    /**
     *
     * @param source
     * @return
     */
    public static Server findServer ( String source )  {
        HashString name = new HashString ( source );
        return findServer ( name );
    }
    
    /**
     *
     * @param name
     * @return
     */
    public static Server findServer ( HashString name )  {
        for ( Server server : sList )  {
            if ( server.is(name) )  {
                return server;
            }
        }
        return null;
    }

    /**
     *
     * @param chan
     */
    public static void deleteEmpty ( Chan chan )  {
        /* If the channel isSet empty lets remove it from memory */
        try {
            if ( chan.empty ( ) )  {
                cList.remove(chan.getName().getCode());
            }
        } catch ( Exception e )  { 
            Proc.log ( Handler.class.getName ( ) , e );
        }
    }

    /**
     *
     * @param user
     */
    public static void squitUser ( User user )  {
        try {
            user.getSID().updateStamp();
            user.partAll ( );
            removeUser ( user );
        } catch ( Exception e )  { 
            Proc.log ( Handler.class.getName ( ) , e );
        }
    }
    
    /**
     *
     * @param user
     */
    public static void removeUser ( User user ) {
        try {
            user.getSID().remUser();
            /* (not whoever has the nick now, when this one is long gone) */
            uList.remove ( user.getName().getCode(), user );
        } catch ( Exception e ) {
            Proc.log ( Handler.class.getName ( ) , e );
        }
    }
    
    /**
     *
     * @param user
     */
    public static void deleteUser ( User user )  {
        try {
            if ( user.getSID() != null ) {
                user.getSID().remUser ( ); 
            }
            user.partAll ( );
            user.quitServer ( );
        } catch ( Exception e )  { 
            Proc.log ( Handler.class.getName ( ) , e );
        }
        try {
            /* Always forget the user, even if the cleanup above failed */
            removeUser ( user );
        } catch ( Exception e )  { 
            Proc.log ( Handler.class.getName ( ) , e );
        }
    }

    /**
     *
     * @param server
     */
    public static void deleteServer ( Server server )  {
        try {
            sList.remove ( server ); 
        } catch ( Exception e )  { 
            Proc.log ( Handler.class.getName ( ) , e );
        }
    }

    /**
     *
     * @return
     */
    public static SimpleDateFormat getSdf ( ) { 
        return sdf; 
    }
    
    /**
     *
     * @param id
     * @return
     */
    public static ServicesID findSid ( long id )  {
        HashString target = new HashString ( ""+id );
        try {
            ServicesID sid = sidList.get ( target.getCode() );
            if ( sid != null ) {
                return sid;
            }
        } catch ( Exception e )  { 
            Proc.log ( Handler.class.getName ( ) , e );
        }
        return null;
    }
   
    /* MAINTENANCE METHODS */

    /**
     *
     * @return
     */

    public int runSecMaintenance() {
        int todoAmount = 0;
        /* Each part on its own: an error in one is logged and the others
           still run. Nothing here may end the main loop. */
        try {
            this.retryLoadIfNeeded ( );
        } catch ( Exception e ) {
            Proc.log ( Handler.class.getName ( ), e );
        }
        try {
            this.cmdQueue.maintenance ( );  /* throttles itself to every 5 seconds */
        } catch ( Exception e ) {
            Proc.log ( Handler.class.getName ( ), e );
        }
        try {
            for ( User u : new ArrayList<> ( uList.values ( ) ) ) {
                u.secMaintenence ( );
            }
        } catch ( Exception e ) {
            Proc.log ( Handler.class.getName ( ), e );
        }
        /* Database updates. Services work from memory, and everything that
           changed waits in a list until the database takes it. The nicks go
           first: staff, channel access and memos point at them, and after a
           time without the database all of it is written in one round. */
        try {
            todoAmount += NickServ.secMaintenance ( );
            /* Registrations, changes, mail codes and logs of NickServ */
            todoAmount += NickServ.maintenance ( );
        } catch ( Exception e ) {
            Proc.log ( Handler.class.getName ( ), e );
        }
        try {
            todoAmount += MemoServ.maintenance ( );
            todoAmount += MXDatabase.flush ( );
        } catch ( Exception e ) {
            Proc.log ( Handler.class.getName ( ), e );
        }
        try {
            todoAmount += oper.secMaintenance ( );
        } catch ( Exception e ) {
            Proc.log ( Handler.class.getName ( ), e );
        }
        try {
            todoAmount += ChanServ.secMaintenance ( );
        } catch ( Exception e ) {
            Proc.log ( Handler.class.getName ( ), e );
        }
        try {
            todoAmount += updateServicesIDs ( );
        } catch ( Exception e ) {
            Proc.log ( Handler.class.getName ( ), e );
        }
        return todoAmount;
    }
      
    /**
     *
     * @param servicesId
     */
    public static void addUpdateSID ( ServicesID servicesId ) {
        for ( ServicesID sid : updServicesID ) {
            if ( sid.getID() == servicesId.getID() ) {
                return;
            }
        }
        updServicesID.add ( servicesId );
    }
    
    private int updateServicesIDs ( ) {
        if ( updServicesID.isEmpty() || ! Database.activateConnection() ) {
            return updServicesID.size();
        }
        ArrayList<ServicesID> sids = new ArrayList<>(); 
        for ( ServicesID sid : updServicesID ) {
            if ( Database.updateServicesID ( sid ) ) {
                sids.add ( sid );
            }
        } 
        for ( ServicesID sid : sids ) {
            updServicesID.remove ( sid );
        }
        return updServicesID.size();
    }

    /**
     *
     * @return
     */
    public int runMinuteMaintenance ( )  {
        int todoAmount = 0;
        try {
            initServices ( ); /* make sure everything isSet running */
            todoAmount += oper.minMaintenance ( );
            db.runMaintenance ( );
            todoAmount += NickServ.maintenance ( );
            todoAmount += ChanServ.maintenance ( );
            this.sidCleaner ( );
            Proc.rotateOutput ( );
        
        } catch ( Exception e )  { 
            Proc.log ( Handler.class.getName ( ) , e );
        }
        return todoAmount;
    }

    /**
     *
     * @return
     */
    public int runHourMaintenance ( )  {
        int todoAmount = 0;
        try {
            initServices ( ); /* make sure everything isSet running */
            todoAmount += NickServ.maintenance ( );
            todoAmount += ChanServ.maintenance ( );
        } catch ( Exception e )  { 
            Proc.log ( Handler.class.getName ( ) , e );
        }
        return todoAmount;
    }


//    private void checkNiStates ( )  {
//        NickInfo ni;
/*        try {
            for ( User u : uList )  { 
                System.out.println ( "DEBUG: checkNiStates ( "+u.getString ( User.NAME ) +" );" );
     
                if (  ( ni = NickServ.findNick ( u.getName ( )  )  )  != null && !u.isIdented ( ni )  )  {     
                    System.out.println ( "DEBUG: checkNiStates ( "+u.getString ( User.NAME ) +"/not idented );" );

                    this.nick.warnIdent ( u ); /* send warning */
  /*                  if ( u.getState ( )  >= 4 )  {
                        /* Change nickname if we hit 4 or more */
    /*                    this.guest.forceNick ( u );
                        u.resetState ( );
                    } else {
                        u.setState ( );               
                    }
                } else {
                    System.out.println ( "DEBUG: checkNiStates ( "+u.getString ( User.NAME ) +"/idented );" );

                }
            }
        } catch ( Exception e )  { 
            Logger.getLogger ( Handler.class.getName ( )  ) .log ( Level.SEVERE, null, e );
        }*/
//    }

    
    /* A services ID that nobody has used for a day is forgotten, and its
       row in the database goes with it. The table got a row per identified
       connection, all of them were read at every start, and nothing ever
       removed one */
    private void sidCleaner ( )  {
        try {
            ArrayList<ServicesID> expired = new ArrayList<> ( );
            ArrayList<ServicesID> rows = new ArrayList<> ( );
            for ( ServicesID s : sidList.values() )  {
                if ( s.hasExpired ( )  )  {
                    expired.add ( s );
                    if ( s.isStored ( ) ) {
                        rows.add ( s );
                    }
                    if ( rows.size ( ) >= 500 ) {
                        break;      /* the rest next minute, never one long statement */
                    }
                }
            }
            if ( ! Database.deleteServicesIDs ( rows ) ) {
                /* The database is down. Forgotten here they would never be
                   removed there, so they stay until it is back */
                expired.removeAll ( rows );
            }
            for ( ServicesID r : expired )  {
                sidList.remove ( r.getCode() );
                updServicesID.remove ( r );
            }
        } catch ( Exception e )  { 
            Proc.log ( Handler.class.getName ( ) , e );
        }
    }

    private void doTopic ( String[] data )  {
        //:irc.avade.net TOPIC #avade Pintuz 1366384642 :testTopic
        //     0           1      2      3        4          5
        Chan c = findChan ( data[2] );
        String topicData = Handler.cutArrayIntoString ( data, 5 );
        Topic topic = new Topic ( topicData, data[3], Long.parseLong ( data[4] )  );
        if ( c != null )  {
            c.setTopic ( topic );
            chan.checkServerTopic ( c );
        }  
    }
    private void doTopic ( User user, String[] data )  {
        //:Pintuz TOPIC #avade Pintuz!fredde@192.168.6.243 1366388927 :oij       
        //     0   1      2      3                             4        5  =6
        Chan c = findChan ( data[2] );
        String topicData = Handler.cutArrayIntoString ( data, 5 );
        Topic topic = new Topic ( topicData, data[3], Long.parseLong ( data[4] )  );
        if ( c != null )  {
            c.setTopic ( topic );
        }
        ChanInfo ci = ChanServ.findChan ( data[2] );
        if ( ci != null &&
            chan.checkTopic ( user, c ) ) {
            ci.getChanges().change ( TOPIC );
            ChanServ.addToWorkList ( CHANGE, ci );
        }
        
    }
    
    /**
     *
     * @return
     */
    public static RootServ getRootServ ( ) { 
        return root;
    }

    /**
     *
     * @return
     */
    public static ChanServ getChanServ ( ) { 
        return chan;
    }

    /**
     *
     * @return
     */
    public static NickServ getNickServ ( ) { 
        return nick;
    }

    /**
     *
     * @return
     */
    public static MemoServ getMemoServ ( ) { 
        return memo;
    } 

    /**
     *
     * @return
     */
    public static GuestServ getGuestServ ( ) { 
        return guest;
    }

    /**
     *
     * @return
     */
    public static OperServ getOperServ ( ) { 
        return oper;
    }

    /**
     *
     * @return
     */
    public static Service getGlobal ( ) { 
        return global;
    }

    private void sendNoSuchNick ( User user, String name )  {
        ServSock.sendCmd ( ":"+Proc.getConf().get ( NAME ) +" 371 "+user.getString ( NAME ) +" :"+name+" has been disabled, try again later." ); 
    }
     
    /**
     *
     * @param ni
     * @return
     */
    public static ArrayList<User> findUsersByNick ( NickInfo ni )  {
        ArrayList<User> ul = new ArrayList<> ( );
        for ( HashMap.Entry<BigInteger,User> entry : uList.entrySet() ) {
            if ( entry.getValue().isIdented(ni) ) {
                ul.add ( entry.getValue() );
            }
        }
        return ul;
    }
     
    /**
     *
     * @param mask
     * @return
     */
    public static ArrayList<User> findUsersByMask ( String mask )  {
        return findUsersByMask ( new HashString ( mask ) );
    }

    /**
     *
     * @param mask
     * @return
     */
    public static ArrayList<User> findUsersByMask ( HashString mask )  {
        ArrayList<User> ul = new ArrayList<> ( );
        User user = null;
        for ( HashMap.Entry<BigInteger,User> entry : uList.entrySet() ) {
            user = entry.getValue();
            if ( StringMatch.matches ( user.getName()+"!"+user.getString(USER)+"@"+user.getString ( HOST ) , mask.getString() )         ||
                 StringMatch.matches ( user.getName()+"!"+user.getString(USER)+"@"+user.getString ( REALHOST ) , mask.getString() )     ||
                 StringMatch.matches ( user.getName()+"!"+user.getString(USER)+"@"+user.getString ( IP ) , mask.getString() )  )  {
                 ul.add ( user );
            }  
        } 
        return ul;        
    }
    
    /**
     *
     * @param ban
     * @return
     */
    public static ArrayList<User> findUsersByBan ( ServicesBan ban ) {
        ArrayList<User> ul = new ArrayList<>();
        User u = null;
        if ( ban.getCidr() == null ) {
            ul = findUsersByMask ( ban.getMask() );
        } else {
            for ( HashMap.Entry<BigInteger,User> entry : uList.entrySet() ) {
                u = entry.getValue();
                try {
                    if ( ban.getCidr().isInRange ( u.getIp() ) ) {
                        ul.add ( u );
                    }
                } catch (UnknownHostException ex) {
                    Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return ul;
    }

    /**
     *
     * @param nick
     * @return
     */
    public static ArrayList<User> findUsersByNick ( String nick ) {
        ArrayList<User> ul = new ArrayList<> ( );
        User user = null;
        for ( HashMap.Entry<BigInteger,User> entry : uList.entrySet() ) {
            user = entry.getValue();
            if ( StringMatch.matches ( user.getString ( NAME ), nick ) ) {
                ul.add ( user );
            }
        }
        return ul;
    }

    /**
     *
     * @param gcos
     * @return
     */
    public static ArrayList<User> findUsersByGcos ( String gcos ) {
        ArrayList<User> ul = new ArrayList<> ( );
        User user = null;
        for ( HashMap.Entry<BigInteger,User> entry : uList.entrySet() ) {
            user = entry.getValue();
            if ( StringMatch.matches ( user.getString ( REALNAME ), gcos ) ) {
                ul.add ( user );
            }
        }
        return ul;
    }

 
    /**
     *
     * @param date
     * @param data
     * @return
     */
    public static Date expireToDate ( Date date, String data ) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if ( data != null && data.contains("-") ) {
            /* Already a date */
            try {
                return dateFormat.parse ( data );
            } catch ( ParseException ex ) {
                Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
        long seconds = ( data == null ? 30 * 60 : parseDuration ( data ) );
        if ( seconds < 0 ) {
            return null;
        }
        return new Date ( ( date != null ? date.getTime() : System.currentTimeMillis() ) + seconds * 1000L );
    }

    /**
     * Parse a duration: a plain number is minutes, or a number followed by
     * m (minutes), h (hours), d (days), w (weeks) or y (years)
     * @param data
     * @return seconds, or -1 if it is not a valid duration
     */
    public static long parseDuration ( String data ) {
        if ( data == null || ! data.matches ( "[0-9]{1,9}[mhdwyMHDWY]?" ) ) {
            return -1;
        }
        char unit = Character.toLowerCase ( data.charAt ( data.length() - 1 ) );
        long amount;
        long multiply;
        if ( Character.isDigit ( unit ) ) {
            amount = Long.parseLong ( data );
            multiply = 60;
        } else {
            amount = Long.parseLong ( data.substring ( 0, data.length() - 1 ) );
            switch ( unit ) {
                case 'h' : multiply = 60L*60;          break;
                case 'd' : multiply = 60L*60*24;       break;
                case 'w' : multiply = 60L*60*24*7;     break;
                case 'y' : multiply = 60L*60*24*365;   break;
                default  : multiply = 60;              break;
            }
        }
        long seconds = amount * multiply;
        /* More than ten years is a typo, and the date would not fit the database */
        return seconds > 10L*365*24*60*60 ? -1 : seconds;
    }
    
    /**
     *
     * @param datetime
     * @param data
     * @return
     */
    public static String expireToDateString ( String datetime, String data ) {
        return expireWithCharToDateString ( datetime, data );
    }
    
    /**
     *
     * @param datetime
     * @param data
     * @return
     */
    public static String expireWithCharToDateString ( String datetime, String data ) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date;
        try {
            date = dateFormat.parse ( datetime );
        } catch (ParseException ex) {
            Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        long seconds = parseDuration ( data );
        if ( seconds < 0 ) {
            return null;
        }
        date.setTime ( date.getTime() + seconds * 1000L );
        return dateFormat.format ( date );
    }
    
    private void doFinishSync(String[] data) {
        this.finishSync ( );
    }

    /* Run once per link when the burst is done. Hubs send LUSERSLOCK, but
       every server ends the burst with the 2nd PING, so use whichever comes first */
    private void finishSync ( ) {
        if ( syncFinished ) {
            return;
        }
        syncFinished = true;
        /* Fix Master after we synched */ 
        root.fixMaster ( );
        oper.sendSpamFilter ( );
        oper.sendCloneLimits ( );
        oper.sendJoinRequests ( null );
        sendUhmSalt ( );
    }

    /**
     *
     * @return
     */
    public static HashMap<BigInteger,User> getUserList ( ) {
        return uList;
    }

    /**
     *
     * @return
     */
    public static boolean sanityCheck() {
        return sanity;
    }

    private void doError ( ) {
        /* The hub only sends ERROR right before it closes the link */
        Proc.log ( "Hub sent: "+String.join ( " ", this.data ) );
        this.reInitServices ( );
    }

    /**
     *
     * @return
     */
    public static ArrayList<Server> getServerList ( ) {
        return sList;
    }
    
    private void reInitServices() {
        Proc.reConnect();
    }

    /**
     * A nick is dropped: nobody stays identified to it. That goes for the
     * services IDs without a user too (split away, or gone for less than the
     * days an ID is kept): whoever registers the name next must not get the
     * old sessions with it.
     * @param ni
     */
    public static void unIdentifyAll ( NickInfo ni ) {
        for ( ServicesID sid : sidList.values ( ) ) {
            sid.unIdentify ( ni );
        }
    }

    /**
     * The same for a channel that is dropped
     * @param ci
     */
    public static void unIdentifyAll ( ChanInfo ci ) {
        for ( ServicesID sid : sidList.values ( ) ) {
            sid.unIdentify ( ci );
        }
    }

    /**
     *
     * @param ci
     * @return
     */
    public static ArrayList<User> findIdentifiedUsersByChan ( ChanInfo ci ) {
        ArrayList<User> iList = new ArrayList<>();
        User user = null;
        for ( HashMap.Entry<BigInteger,User> entry : uList.entrySet() ) { 
            user = entry.getValue();
            if ( user.isIdented ( ci ) ) {
                iList.add ( user );
            }
        }
        return iList;
    }

    /**
     *
     * @return
     */
    public static HashMap<BigInteger, ServicesID> getSIDs ( ) {
        return sidList;
    }

    /**
     *
     * @return
     */
    public static HashMap<BigInteger,Chan> getChanList ( ) {
        return cList;
    }
 
}
