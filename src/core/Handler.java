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

import user.User;
import server.ServSock;
import server.Server;
import monitor.Snoop;
import channel.Chan;
import channel.Topic;
import chanserv.CSLogEvent;
import chanserv.ChanInfo;
import rootserv.RootServ;
import operserv.OperServ;
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
    private Snoop                               snoop;
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
    private HashString bufhash;
    
    public Handler ( )  { 
        db              = new Database ( );
        Handler.initServices ( );
        Database.loadSIDs ( );
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
    
    public static void unloadServices ( ) {
        root = null;
        oper = null;
        nick = null;
        chan = null;
        memo = null;
        global = null;
        guest = null;
    }
    
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
    
    public void process ( String read )  {
        User uBuf;
        NickInfo nBuf;
        this.data   = null; 
        System.out.println ( read );
        this.data   = read.split ( " " ); 
         
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
                        sList.add ( new Server ( this.data ) );
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
                    }
                    
                } else {
                    /* User stuff */ 
                    
                    if ( this.data == null || this.data[2].isEmpty ( )  )  { 
                        return; 
                    }
                    
                    User user; 
                    user = findUser ( this.source );
                     
                    if ( oper.isIgnored ( user ) ) {
                        return;
                    }
                    
                    this.command = new HashString ( this.data[1] );
                     
                    if ( this.command.is(PRIVMSG) ) {
                        doPrivmsg ( user );
                    
                    } else if ( this.command.is(MODE) ) {
                        doMode ( user );
                    
                    } else if ( this.command.is(SJOIN) ) {
                        doSJoin ( user );
                    
                    } else if ( this.command.is(TOPIC) ) {
                        doTopic ( user, this.data );
                    
                    } else if ( this.command.is(PART) ) {
                        doPart ( user );
                    
                    } else if ( this.command.is(KICK) ) {
                        doKick ( user );
                    
                    } else if ( this.command.is(QUIT) ) {
                        deleteUser ( user );
                    
                    } else if ( this.command.is(KILL) ) {
                        //this.nullService ( user ); /* null if service */
                            User u;
                            if ( ( u = Handler.findUser ( this.data[2] ) ) != null ) {
                                deleteUser ( u );
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
                    sList.add ( new Server ( this.data ) );
                    root.sendPanic();
                
                } else if ( this.command.is(SJOIN) ) {
                    this.doChan ( false );
                
                } else if ( this.command.is(SQUIT) ) {
                    this.doSquit ( );
                
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
        if ( ( c = Handler.findChan ( this.data[3] ) ) != null ) {
            c.addUserList(data, 5);
        } else {
            c = new Chan ( this.data ); 
            cList.put ( c.getName().getCode(), c ); 
            if ( check ) {
                chan.checkSettings ( c );
            }
        }
    }
    
    //     :Guest12203 PRIVMSG NickServ@services.sshd.biz :identify asd.
    private void doPrivmsg ( User user )  {
        HashString service = new HashString ( this.data[2].substring ( 0, this.data[2].lastIndexOf ( "@" ) ) );
        
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
    
    private void doSquit ( )  {
        Server s = findServer ( this.data[1] );
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
            long serviceID = Long.parseLong ( this.data[8] );
            if ( serviceID > 999 ) {
                u.setSID ( Handler.findSid ( serviceID ) );
            }
            
        } catch ( NumberFormatException ex ) {
            Proc.log ( Handler.class.getName ( ), ex );
        }
        
        if ( u.getSID() == null ) {
            u.setSID ( new ServicesID ( ) );
        }  
        
        NickInfo ni = NickServ.findNick ( u.getString ( NAME ) );
        
        if ( ni != null && u.getModes().is ( IDENT ) ) {
            u.getSID().add ( ni );
        } else {
            Handler.getNickServ().sendCmd ( "SVSMODE "+u.getString ( NAME )+" 0 -r" );
            u.getModes().set ( IDENT, false );
        }
        
        NickServ.fixIdentState ( u );
        uList.put ( u.getName().getCode(), u );
        
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
            Proc.log("WHITELISTED!!!");
            return;
        }
        Proc.log("NOT WHITELISTED!!!");
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
                mask = new HashString ( "*!*@"+user.getIp() );
                reason = "Cloning. Too many clients found from this IP-range. 30 min ban.";
                ServicesBan ban = new ServicesBan ( AKILL, id, false, mask, reason, "OperServ", null, expire );
                percent = String.format("%.02f", (float) rangeCount / Handler.getUserList().size() * 100 );
                if ( ! OperServ.isWhiteListed ( ban.getMask() ) ) {
                    OperServ.addServicesBan ( ban );
                    Handler.getOperServ().sendServicesBan ( ban );
                    oper.sendGlobOp ( "AKILL: *!*@"+user.getIp()+" placed for cloning. Affecting "+rangeCount+" users ["+percent+"%]" );
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

        
        
        
        
        
        
/*        if ( action.is(AKILL) ) {
                String stamp = dateFormat.format ( new Date ( ) );
                String percent;
                boolean foundOperMatch = false;
                HashString id;
                HashString mask;
                String expire = Handler.expireToDateString ( stamp, "30m" );
                if ( ipCount > Trigger.getActionIP ( ) ) {
                    reason = "Cloning. Too many clients found from this IP. 30 min ban.";
                    id = new HashString ( ""+System.nanoTime() );
                    mask = new HashString ( "*!*@"+user.getIp() );

                    
                    ServicesBan ban = new ServicesBan ( AKILL, id, false, mask, reason, "OperServ", null, expire );
                    percent = String.format("%.02f", (float) ipCount / Handler.getUserList().size() * 100 );
                    if ( ! OperServ.isWhiteListed ( ban.getMask() ) ) {
                        Handler.getOperServ().addServicesBan ( ban );
                        Handler.getOperServ().sendServicesBan ( ban );
                        oper.sendGlobOp ( "AKILL: *!*@"+user.getIp()+" placed for cloning. Affecting "+ipCount+" users ["+percent+"%]" );
                    }
                } else if ( Trigger.isWarn() && ipCount > Trigger.getWarnIP() ) {
                    if ( ipCount == ( Trigger.getWarnIP() + 1 ) ||
                         ipCount % 10 == 0 ) {
                        oper.sendGlobOp ( "Warning! possible clones: "+ipCount+" clients from ip: *!*@"+user.getIp() );
                    }
                }
                if ( ipCount > Trigger.getActionRange() ) {
                    id = new HashString ( ""+System.nanoTime() );
                    mask = new HashString ( "*!*@"+user.getIp() );
                    reason = "Cloning. Too many clients found from this IP-range. 30 min ban.";
                    ServicesBan ban = new ServicesBan ( AKILL, id, false, mask, reason, "OperServ", null, expire );
                    percent = String.format("%.02f", (float) ipCount / Handler.getUserList().size() * 100 );
                    if ( ! OperServ.isWhiteListed ( ban.getMask() ) ) {
                        Handler.getOperServ().addServicesBan ( ban );
                        Handler.getOperServ().sendServicesBan ( ban );
                        oper.sendGlobOp ( "AKILL: *!*@"+user.getIp()+" placed for cloning. Affecting "+ipCount+" users ["+percent+"%]" );
                    }
                } else if ( Trigger.isWarn() && ipCount > Trigger.getWarnIP() ) {
                    if ( ipCount == ( Trigger.getWarnIP() + 1 ) ||
                         ipCount % 10 == 0 ) {
                        oper.sendGlobOp ( "Warning! possible clones: "+ipCount+" clients from ip: *!*@"+user.getIp() );
                    }
                }
        
        } else if ( action.is(KILL) ) {
                if ( ipCount > Trigger.getActionIP() ) {
                    reason = "Cloning. Too many clients found from this IP.";
                    oper.sendGlobOp ( "KILL: "+user.getFullMask()+" for cloning." );
                    oper.sendRaw ( "KILL "+user.getName()+" :"+reason );
                    Handler.deleteUser ( user );
                }
        }
         
  /*      if ( Trigger.getAction() == AKILL && ipCount > Trigger.getActionIP() ) {
                String stamp = dateFormat.format ( new Date ( ) );
                String reason = "Cloning. Too many clients found from this IP. 30 min ban.";
                String percent;
                boolean foundOperMatch = false;
                String expire = Handler.expireToDateString ( stamp, "30m" );
                ServicesBan ban = new ServicesBan ( AKILL, ""+System.nanoTime(), false, "*!*@"+user.getIp(), reason, "OperServ", null, expire );
                percent = String.format("%.02f", (float) ipCount / Handler.getUserList().size() * 100 );
                if ( ! OperServ.isWhiteListed ( ban.getMask() ) ) {
                    Handler.getOperServ().addServicesBan ( ban );
                    Handler.getOperServ().sendServicesBan ( ban );
                    oper.sendGlobOp ( "AKILL: *!*@"+user.getIp()+" placed for cloning. Affecting "+ipCount+" users ["+percent+"%]" );
                }
        } else if ( Trigger.isWarn() && ipCount > Trigger.getWarnIP() ) {
            if ( ipCount == ( Trigger.getWarnIP() + 1 ) ||
                 ipCount % 10 == 0 ) {
                oper.sendGlobOp ( "Warning! possible clones: "+ipCount+" clients from ip: *!*@"+user.getIp() );
            }
        }
        
        if ( Trigger.getAction() == AKILL && rangeCount > Trigger.getActionRange()) {
                String stamp = dateFormat.format ( new Date ( ) );
                String reason = "Cloning. Too many clients found from this IPRANGE. 30 min ban.";
                String percent;
                boolean foundOperMatch = false;
                String expire = Handler.expireToDateString ( stamp, "30m" );
                ServicesBan ban = new ServicesBan ( AKILL, ""+System.nanoTime(), false, "*!*@"+user.getHostInfo().getRange(), reason, "OperServ", null, expire );
                percent = String.format("%.02f", (float) rangeCount / Handler.getUserList().size() * 100 );
                if ( ! OperServ.isWhiteListed ( ban.getMask() ) ) {
                    Handler.getOperServ().addServicesBan ( ban );
                    Handler.getOperServ().sendServicesBan ( ban );
                    oper.sendGlobOp ( "AKILL: *!*@"+user.getHostInfo().getRange()+" placed for cloning. Affecting "+rangeCount+" users ["+percent+"%]" );
                }
        } else if ( rangeCount > Trigger.getWarnRange() ) {
            if ( rangeCount == ( Trigger.getWarnRange() + 1 ) ||
                 rangeCount % 10 == 0 ) {
                oper.sendGlobOp ( "Warning! possible clones: "+rangeCount+" clients from range: *!*@"+user.getHostInfo().getRange() );
            }
        }
    */    
        
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
        
        ni = NickServ.findNick ( user.getString ( NAME )  );
        user.getModes().set ( IDENT, user.isIdented ( ni ) );
        nick.fixIdentState ( user );
        for ( Chan c : user.getChans ( ) ) {
            ChanServ.addCheckUser ( c, user );
        }
        this.oper.checkUser ( user ); /* Add user in OperServ check queue (akills etc) */
    }
    
    private void doSJoin ( User user )  {
        /* User joined a channel */
        System.out.println("0:");
        Chan c = findChan ( this.data[3] );
        System.out.println("1:");
        if ( c != null ) {
        System.out.println("2:");
            c.addUser ( USER, user );
        System.out.println("3:");
            user.addChan ( c );
        System.out.println("4:");
        } else {
        System.out.println("5:");
            this.doChan ( true );
        System.out.println("6:");
        }
        System.out.println("7:");
        if ( ! c.isSaJoin() ) {
        System.out.println("8:");
            ChanServ.addCheckUser ( c, user );
        System.out.println("9:");
        } else {
        System.out.println("10:");
            c.toggleSaJoin();
        System.out.println("11:");
        }
        System.out.println("12:");
    }
     
    private void doPart ( User user )  {
        /* User parted a channel */
        Chan c = findChan ( this.data[2] );
        if ( c != null )  {
            c.remUser ( user );
            user.remChan ( c );
            this.deleteEmpty ( c );
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
                this.deleteEmpty ( c );
            }
        }
    }

    /*****************************/

    private void doMode ( User user )  {
        if ( Handler.isChanName ( this.data[2] ) ) {
            Chan c = findChan ( this.data[2] );
            if ( c != null ) {
                ChanInfo ci = ChanServ.findChan ( c.getString ( NAME ) );
                c.getModes().set ( MODE, this.data );
                c.chMode ( this.data );
                Handler.getChanServ().checkModes ( c, ci );
            }
        } else {
            user.getModes().set ( MODE, this.data );
            this.forceOperModes ( user );
        }
    }
    public static boolean isChanName ( String name ) {
        return name.substring(0,1).matches ( Pattern.quote ( "#" ) );
    }
    private void doGlobOps ( String[] data ) {
        // :testnet.avade.net GLOBOPS :DreamHealer used SAJOIN (#fredde +b)
        //                  0       1            2    3      4       5+
        System.out.println("debug: doGlobOps()");
        CSLogEvent log;
        User user = Handler.findUser ( this.data[2].substring ( 1 ) );
        HashString command = new HashString ( this.data[4] );
        Chan chan = Handler.findChan(this.data[5].substring ( 1 ));
        String string = Handler.cutArrayIntoString ( this.data, 6 ).replace(")", "");
        
        if ( command.is(SAJOIN) ) {
            log = new CSLogEvent ( chan.getString(NAME), SAJOIN, string, user.getOper().getNameStr() );
            ChanServ.addLog ( log );
            chan.toggleSaJoin();
        
        } else if ( command.is(SAMODE) ) {
            log = new CSLogEvent ( chan.getString(NAME), SAMODE, string, user.getOper().getNameStr() );
            ChanServ.addLog ( log );
        }
         
    }

    /* Take array and cut it into string starting at position. Good for 
       reading in reasons, comments, messages */
    public static String cutArrayIntoString ( String[] data, int pos ) {
        if ( data == null || data.length < pos ) {
            return null;
        }
        String buf = String.join ( " ", data );
        String[] arr = buf.split ( " ", pos+1 );
        return arr[pos];
    }
    
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

    public static void newSid ( ServicesID sid )  {
        try { 
            sidList.put ( sid.getCode(), sid ); 
        } catch ( Exception e )  {
            Proc.log ( Handler.class.getName(), e );
        }
    }
    
    public static User findUser ( String name ) {
        return findUser ( new HashString ( name ) );
    }
    
    public static User findUser ( HashString name ) {
        //for ( HashMap.Entry<BigInteger,User> entry : uList.entrySet() ) {
        //    if ( entry.getValue().is(name) )  {
        //        return entry.getValue();
        //    }
        //}
         
        //return null;
        
        return ( uList.containsKey(name.getCode()) ? uList.get(name.getCode()) : null );
    }
    
    public static Chan findChan ( String name ) {
        return findChan ( new HashString ( name ) );
    }
    
    public static Chan findChan ( HashString name )  {
        //for ( HashMap.Entry<BigInteger,Chan> entry : cList.entrySet() ) {
        //    if ( entry.getValue().is(name) )  {
        //        return entry.getValue();
        //    }
        //}
        //return null;
        
        return ( cList.containsKey(name.getCode()) ? cList.get(name.getCode()) : null );
    }
    
    public static Server findServer ( String source )  {
        HashString name = new HashString ( source );
        return findServer ( name );
    }
    
    public static Server findServer ( HashString name )  {
        for ( Server server : sList )  {
            if ( server.is(name) )  {
                return server;
            }
        }
        return null;
    }

    private void deleteEmpty ( Chan chan )  {
        /* If the channel isSet empty lets remove it from memory */
        try {
            if ( chan.empty ( ) )  {
                cList.remove ( chan );
            }
        } catch ( Exception e )  { 
            Proc.log ( Handler.class.getName ( ) , e );
        }
    }

    public static void squitUser ( User user )  {
        try {
            user.getSID().updateStamp();
            user.partAll ( );
            removeUser ( user );
        } catch ( Exception e )  { 
            Proc.log ( Handler.class.getName ( ) , e );
        }
    }
    
    public static void removeUser ( User user ) {
        try {
            user.getSID().remUser();
            uList.remove ( user.getName().getCode() );
        } catch ( Exception e ) {
            Proc.log ( Handler.class.getName ( ) , e );
        }
    }
    
    public static void deleteUser ( User user )  {
        try {
            user.getSID().remUser ( ); 
            user.partAll ( );
            user.quitServer ( );
            removeUser ( user );
        } catch ( Exception e )  { 
            Proc.log ( Handler.class.getName ( ) , e );
        }
    }

    public static void deleteServer ( Server server )  {
        try {
            sList.remove ( server ); 
        } catch ( Exception e )  { 
            Proc.log ( Handler.class.getName ( ) , e );
        }
    }

    public void doRecursiveUList ( User u )  { 
        try {
            Server sHub = findServer ( Proc.getConf().get ( HUBNAME ) );
            if ( sHub != null )  {
                sHub.recursiveUserList ( u, "" );
            }
        } catch ( Exception e )  { 
            Proc.log ( Handler.class.getName ( ) , e );
        }
    }

    public static Database getDB ( )  {
        return db; 
    }

    public static SimpleDateFormat getSdf ( ) { 
        return sdf; 
    }
    

    public static ServicesID findSid ( long id )  {
        HashString target = new HashString ( ""+id );
        try {
            ServicesID sid = sidList.get ( target );
            if ( sid != null ) {
                return sid;
            }
        } catch ( Exception e )  { 
            Proc.log ( Handler.class.getName ( ) , e );
        }
        return null;
    }
   
    /* MAINTENANCE METHODS */
    public int runSecMaintenance() {
        int todoAmount = 0;
        for ( HashMap.Entry<BigInteger,User> entry : uList.entrySet() ) {
            entry.getValue().secMaintenence ( );
        }
        /* Database updates */
        todoAmount += oper.secMaintenance ( );
        todoAmount += ChanServ.secMaintenance ( );
        todoAmount += NickServ.secMaintenance ( );
        todoAmount += updateServicesIDs ( );
        return todoAmount;
    }
      
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
    public int runMinuteMaintenance ( )  {
        int todoAmount = 0;
        try {
            initServices ( ); /* make sure everything isSet running */
            todoAmount += oper.minMaintenance ( );
            db.runMaintenance ( );
            todoAmount += NickServ.maintenance ( );
            todoAmount += ChanServ.maintenance ( );
            this.sidCleaner ( );
            this.cmdQueue.maintenance ( );
        
        } catch ( Exception e )  { 
            Proc.log ( Handler.class.getName ( ) , e );
        }
        return todoAmount;
    }
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
     
                if (  ( ni = NickServ.findNick ( u.getString ( User.NAME )  )  )  != null && !u.isIdented ( ni )  )  {     
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

    
    private void sidCleaner ( )  {
        try {
            ArrayList<ServicesID> buf2 = new ArrayList<> ( );
            for ( ServicesID s : sidList.values() )  {
                if ( s.hasExpired ( )  )  {
                    buf2.add ( s );
                }
            }
            for ( ServicesID r : buf2 )  {
                sidList.remove ( r.getCode() );
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
        }  
    }
    private void doTopic ( User user, String[] data )  {
        //:Pintuz TOPIC #avade Pintuz!fredde@192.168.6.243 1366388927 :oij       
        //     0   1      2      3                             4        5  =6
        Chan c = findChan ( data[2] );
        String topicData = Handler.cutArrayIntoString ( data, 5 );
        Topic topic = new Topic ( topicData, user.getString ( FULLMASK ), Long.parseLong ( data[4] )  );
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
    
    public static RootServ getRootServ ( ) { 
        return root;
    }
    public static ChanServ getChanServ ( ) { 
        return chan;
    }
    public static NickServ getNickServ ( ) { 
        return nick;
    }
    public static MemoServ getMemoServ ( ) { 
        return memo;
    } 
    public static GuestServ getGuestServ ( ) { 
        return guest;
    }
    public static OperServ getOperServ ( ) { 
        return oper;
    }
    public static Service getGlobal ( ) { 
        return global;
    }

    private void sendNoSuchNick ( User user, String name )  {
        ServSock.sendCmd ( ":"+Proc.getConf().get ( NAME ) +" 371 "+user.getString ( NAME ) +" :"+name+" has been disabled, try again later." ); 
    }
     
    public static ArrayList<User> findUsersByNick ( NickInfo ni )  {
        ArrayList<User> ul = new ArrayList<> ( );
        for ( HashMap.Entry<BigInteger,User> entry : uList.entrySet() ) {
            if ( entry.getValue().isIdented(ni) ) {
                ul.add ( entry.getValue() );
            }
        }
        return ul;
    }
     
    public static ArrayList<User> findUsersByMask ( String mask )  {
        return findUsersByMask ( new HashString ( mask ) );
    }
    public static ArrayList<User> findUsersByMask ( HashString mask )  {
        ArrayList<User> ul = new ArrayList<> ( );
        User user = null;
        for ( HashMap.Entry<BigInteger,User> entry : uList.entrySet() ) {
            user = entry.getValue();
            if ( StringMatch.maskWild ( user.getName()+"!"+user.getString(USER)+"@"+user.getString ( HOST ) , mask.getString() )         ||
                 StringMatch.maskWild ( user.getName()+"!"+user.getString(USER)+"@"+user.getString ( REALHOST ) , mask.getString() )     ||
                 StringMatch.maskWild ( user.getName()+"!"+user.getString(USER)+"@"+user.getString ( IP ) , mask.getString() )  )  {
                 ul.add ( user );
            }  
        } 
        return ul;        
    }
    
    
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

    
   
    public static ArrayList<User> findUsersByNick ( String nick ) {
        ArrayList<User> ul = new ArrayList<> ( );
        User user = null;
        for ( HashMap.Entry<BigInteger,User> entry : uList.entrySet() ) {
            user = entry.getValue();
            if ( StringMatch.nickWild ( user.getString ( NAME ), nick ) ) {
                ul.add ( user );
            }
        }
        return ul;
    }

    public static ArrayList<User> findUsersByGcos ( String gcos ) {
        ArrayList<User> ul = new ArrayList<> ( );
        User user = null;
        for ( HashMap.Entry<BigInteger,User> entry : uList.entrySet() ) {
            user = entry.getValue();
            if ( StringMatch.wild ( user.getString ( REALNAME ), gcos ) ) {
                ul.add ( user );
            }
        }
        return ul;
    }
        
    public static String expireToTime ( String data )  {
        String          strBuf;
        String          state;
        String          timeUnit;
        int             multiply=1;
        int             amount=0;
         
        /* take all data except last char */
        strBuf = data.substring ( 0, data.length ( ) - 1 ); 
          
        /* Try the value as an integer */
        try { 
            amount = Integer.parseInt ( strBuf );
        } catch ( NumberFormatException e )  {
            return "";
        }
        
        /* take only the last char */
        HashString ch = new HashString ( String.valueOf(data.charAt(data.length()-1)) );
         
        if ( ch.is(m) ) {
            timeUnit = "MINUTE";
        
        } else if ( ch.is(h) ) {
            timeUnit = "HOUR";
        
        } else if ( ch.is(d) ) {
            timeUnit = "DAY";
        
        } else if ( ch.is(y) ) {
            timeUnit = "YEAR";
        
        } else {
            return "INTERVAL 0 DAYS";
        }
         
        return  "INTERVAL "+( amount * multiply )+" "+timeUnit;
    }
 
    public static Date expireToDate ( Date date, String data ) {
        String strBuf;
        int ms = 0;
        int amount;
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 
        if ( data == null ) {
            data = "30";

        } else if ( data.contains("-") ) {
            try {
                date = dateFormat.parse ( data );
            } catch ( ParseException ex ) {
                Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
            return date;
        }
        
        strBuf = data.substring ( 0, data.length ( ) - 1 );
        try {
            amount = Integer.parseInt ( strBuf );            
        } catch ( NumberFormatException ex ) {
            return null;
        }
       
        date.setTime ( date.getTime() + ( amount*ms ) );
        return date;
    }
    
    public static String expireToDateString ( String datetime, String data ) {
        int ms = 60*1000;
        int amount;
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date date;
        try {
            date = dateFormat.parse ( datetime );
            amount = Integer.parseInt ( data );            
        } catch ( NumberFormatException | ParseException ex ) {
            Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
        date.setTime ( date.getTime() + ( amount*ms ) );
        return dateFormat.format ( date );
    }
    
    public static String expireWithCharToDateString ( String datetime, String data ) {
        String          strBuf;
        String          state;
        int             multiply;
        int             amount;
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date;
        
        try {
            date = dateFormat.parse ( datetime );
        } catch (ParseException ex) {
            Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        if ( StringMatch.isInt ( data ) ) {
            state = "m";
            amount = Integer.parseInt ( data );
            
        } else {
            strBuf = data.substring ( 0, data.length() - 1 );
            amount = Integer.parseInt ( strBuf );
            state = ""+data.charAt ( data.length() - 1 );
        }
        
        HashString ch = new HashString ( state );
        
        if ( ch.is(m) ) {
            multiply = 60;
        
        } else if ( ch.is(h) ) {
            multiply = 60*60;
        
        } else if ( ch.is(d) ) {
            multiply = 60*60*24;
        
        } else {
            multiply = 60;
        }
         
        date.setTime ( date.getTime() + ( amount * multiply * 1000 ) );
        return dateFormat.format ( date );
    }
    
    private void doFinishSync(String[] data) {
        /* Fix Master after we synched */ 
        root.fixMaster ( );
        oper.sendSpamFilter ( );
    }

    
    public static HashMap<BigInteger,User> getUserList ( ) {
        return uList;
    }

    public static boolean sanityCheck() {
        return sanity;
    }

    private void nullService ( User user ) {
        HashString name = user.getName();
        
        if ( name.is(ROOTSERV) ) {
            root = null;
        
        } else if ( name.is(OPERSERV) ) {
            oper = null;
        
        } else if ( name.is(NICKSERV) ) {
            nick = null;
        
        } else if ( name.is(CHANSERV) ) {
            chan = null;
        
        } else if ( name.is(MEMOSERV) ) {
            memo = null;
        
        } else if ( name.is(GUESTSERV) ) {
            guest = null;
        
        } else if ( name.is(GLOBAL) ) {
            global = null;
        }
         
    }

    private void doError ( ) {
        HashString sub1 = new HashString ( this.data[1].replace(":", "") );
        HashString sub2 = new HashString ( this.data[2].replace(":", "") );
        
        if ( sub1 == CLOSING && 
             sub2 == LINK ) {
            this.reInitServices ( );
        }
    }

    public static ArrayList<Server> getServerList ( ) {
        return sList;
    }
    
    private void reInitServices() {
        Proc.reConnect();
    }

    
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

    public static HashMap<BigInteger, ServicesID> getSIDs ( ) {
        return sidList;
    }

    public static HashMap<BigInteger,Chan> getChanList ( ) {
        return cList;
    }
 
}
