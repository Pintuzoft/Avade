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
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import nickserv.NickInfo;
import nickserv.NickServ;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import operserv.ServicesBan;

/**
 *
 * @author DreamHealer
 */
public class Handler extends HashNumeric { 
    private static RootServ                 root;
    private static OperServ                 oper;
    private static NickServ                 nick;
    private static ChanServ                 chan;
    private static MemoServ                 memo;
    private static GuestServ                guest;
    private static Service                  global;

    private Services                        services;
    private Snoop                           snoop;
    private Config                          config; 
    private Trigger                         trigger;
    private static ArrayList<User>          uList = new ArrayList<>();
    private static ArrayList<ServicesID>    splitSIDs = new ArrayList<>();
    private static ArrayList<ServicesID>    updServicesID = new ArrayList<>();
    private static ArrayList<Chan>          cList = new ArrayList<>();
    private static ArrayList<Server>        sList = new ArrayList<>();
    private static ArrayList<ServicesID>    sidList = new ArrayList<>();
    private static Database                 db; 
    private String[]                        data; 
    private String                          source;
    private static Date                     date;

    private static SimpleTimeZone           timeZone;
    private static Locale                   locale;
    private static SimpleDateFormat         sdf;
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 

    /* Maintenance Delays */
    private final static int DB_DELAY = 1; /* Delay in minutes */
    private long                            dbDelay; 
    private String                          buf; 
    private Queue                           cmdQueue;
    private static boolean                  sanity;
    
    public Handler ( )  { 
        this.config = Proc.getConf ( );
        db              = new Database ( );
        Handler.initServices ( );
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
    
    public static void initService ( int type, Service service ) {
        if ( service == null ) {
            switch ( type ) {
                case ROOTSERV :
                    root = new RootServ ( );
                    
                case OPERSERV :
                    oper = new OperServ ( );
                    
                case NICKSERV :
                    nick = new NickServ ( );
                    
                case CHANSERV :
                    chan = new ChanServ ( );
                    
                case MEMOSERV :
                    memo = new MemoServ ( );
                          
                case GUESTSERV :
                    guest = new GuestServ ( );
                             
                case GLOBAL :
                    global = new Service ( "Global" ) {};
                             
                default :
                    
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

                    switch ( this.data[1].hashCode ( )  )  {
                        case SERVER :
                            sList.add ( new Server ( this.data ) );
                            root.sendPanic();
                            break;
                            
                        case SJOIN :
                            doChan ( true );
                            break;
                            
                        case TOPIC :
                            doTopic ( this.data );
                            break;
                            
                        case LUSERSLOCK :
                            doFinishSync ( this.data );
                            break;
                            
                        case GLOBOPS :
                            doGlobOps ( this.data );
                            break;
                            
                        case OS :
                            doOS ( this.data );
                            break;
                            
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
                    switch ( this.data[1].hashCode ( )  )  {
                        case PRIVMSG :
                            doPrivmsg ( user );
                            break;
                            
                        case MODE :
                            doMode ( user );
                            break;
                            
                        case SJOIN :
                            doSJoin ( user );
                            break;
                            
                        case TOPIC :
                            doTopic ( user, this.data );
                            break;
                            
                        case PART :
                            doPart ( user );
                            break;
                            
                        case KICK :
                            doKick ( user );
                            break;
                            
                        case QUIT :
                            deleteUser ( user );
                            break;
                            
                        case KILL :
                            //this.nullService ( user ); /* null if service */
                            User u;
                            if ( ( u = Handler.findUser ( this.data[2] ) ) != null ) {
                                deleteUser ( u );
                            }
                            break;
                            
                        case NICK :
                            doNick ( user );
                            break;
                            
                        case MOTD :
                            this.services.parse ( user, this.data );
                            break;
                            
                        case VERSION :
                            this.services.parse ( user, this.data );
                            break;
                            
                        case INFO :
                            this.services.parse ( user, this.data );
                            break;
                            
                        case STATS :
                            this.services.parse ( user, this.data );
                            break;
                            
                        default : 
                            
                    }
 
                    // :DreamHealer NICK fr :1321662151
                    // :DreamHea1er QUIT :Quit: leaving         
                    // :DreamHealer MODE #friends 1320518761 -o DreamHealer

                }

            } else {
                /* We are getting  */
                switch ( this.data[0].hashCode ( )  )  {
                        case SERVER :
                            sList.add ( new Server ( this.data ) );
                            root.sendPanic();
                            break;
                            
                        case SJOIN :
                            this.doChan ( false );
                            break;
                            
                        case SQUIT :
                            this.doSquit ( );
                            break;
                            
                        case SVINFO :
                            this.doSVInfo ( );
                            break;
                            
                        case PING :
                            this.doPing ( );
                            break;
                            
                        case NICK :
                            this.doNick ( );
                            break;
                                  
                        case SF :
                            this.doSF ( );
                            break;
                            
                        case ERROR :
                            this.doError ( );
                            break;
                            
                        default : 
                            
                }
                 
            }
        } catch ( Exception e )  {
            Proc.log ( Handler.class.getName ( ) , e );
        }
    }
    
    /*****************************/
    
    private void doChan  ( boolean check )  {
        Chan c;
        if ( ( c = Handler.findChan ( this.data[3] ) ) != null ) {
            c.addUserList(data);
        } else {
            c = new Chan ( this.data ); 
            cList.add ( c ); 
            if  ( check )  {
                chan.checkSettings ( c );
            }
        }
    }
    
    //     :Guest12203 PRIVMSG NickServ@services.sshd.biz :identify asd.
    private void doPrivmsg ( User user )  {
        this.buf = this.data[2].toUpperCase ( ) .substring ( 0, this.data[2].lastIndexOf ( "@" )  );
        
        switch ( this.buf.hashCode ( )  )  {
            case ROOTSERV :
                if ( RootServ.isUp ( )  )  { 
                    Handler.root.parse ( user, this.data );
                } else { 
                    this.sendNoSuchNick ( user, "RootServ" ); 
                }
                break;
                
            case OPERSERV :
                oper.parse ( user, this.data );
                break;
                
            case CHANSERV :
                if ( ChanServ.isUp ( ) ) { 
                    chan.parse ( user, this.data );
                } else { 
                    this.sendNoSuchNick ( user, "ChanServ" ); 
                } 
                break;
                
            case NICKSERV :
                if ( NickServ.isUp ( ) ) { 
                    nick.parse ( user, this.data ); 
                } else { 
                    this.sendNoSuchNick ( user, "NickServ" );
                }
                break;
                
            case MEMOSERV :
                if ( MemoServ.isUp ( ) ) { 
                    memo.parse ( user, this.data ); 
                } else { 
                    this.sendNoSuchNick ( user, "MemoServ" ); 
                }
                break;
                
            default : 
                
        }
    }
    
    private void doOS ( String[] data ) {
        switch ( data[2].toUpperCase().hashCode() ) {
            case SFAKILL :
                oper.addSFAkill ( data );
                break;
                
            default :
                
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
        Proc.log ( "DEBUG: connection established in: "+ ( System.currentTimeMillis ( ) -Proc.getStartTime ( )  ) +"ms" );
    }
    private void doPing ( )  {
        this.pong ( this.data[1] );
    }
    
    
    private static ServicesID findSplitSID ( long servicesID ) {
        ServicesID sid = null;
        for ( ServicesID s : splitSIDs ) {
            if ( s.getID() == servicesID ) {
                sid = s;
            }
        }
        if ( sid != null ) {
            splitSIDs.remove ( sid );
        }
        return sid;
    }

    
    /* New nick on the network */
    private void doNick ( )  {
        User u = new User ( this.data );
        ServicesID sid = null;

        //NICK DreamHealer 1 1532897366 +oiCra fredde DreamHealer.ircop testnet.avade.net 965942 167772447 :a figment of your own imagination
        try {
            long serviceID = Long.parseLong ( this.data[8] );
            if ( serviceID > 999 ) {
                u.setSID ( Handler.findSplitSID ( serviceID ) );
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
        uList.add ( u );
        
        Server s = findServer ( this.data[7] );
        if ( s != null ) {
            s.addUser ( u );
        }
//        NickInfo ni;
  //      if ( () == null ) {
    //    }
        checkTrigger ( u );
        this.oper.checkUser ( u ); /* Add user in OperServ check queue (akills etc) */
    }
    
    private static void checkTrigger ( User user ) {
        int ipCount = 0;
        int rangeCount = 0;
        for ( User u : uList ) {
            if ( u.ipMatch ( user.getHostInfo().getIpHash() ) ) {
                ++ipCount;
            } 
            if ( u.rangeMatch ( user.getHostInfo().getRangeHash() ) ) {
                ++rangeCount;
            }
        }
        String reason;
        switch ( Trigger.getAction() ) {
            case AKILL :
                String stamp = dateFormat.format ( new Date ( ) );
                String percent;
                boolean foundOperMatch = false;
                String expire = Handler.expireToDateString ( stamp, "30m" );
                if ( ipCount > Trigger.getActionIP() ) {
                    reason = "Cloning. Too many clients found from this IP. 30 min ban.";
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
                if ( ipCount > Trigger.getActionRange() ) {
                    reason = "Cloning. Too many clients found from this IP-range. 30 min ban.";
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
                break;
            
            case KILL :
                if ( ipCount > Trigger.getActionIP() ) {
                    reason = "Cloning. Too many clients found from this IP.";
                    oper.sendGlobOp ( "KILL: "+user.getFullMask()+" for cloning." );
                    oper.sendRaw ( "KILL "+user.getName()+" :"+reason );
                    Handler.deleteUser ( user );
                }
                break;
                
            default:
                
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
        if ( user == null ) {
            return;
        }
        if ( this.data.length >= 3 ) {
            user.setName ( this.data[2] );
        }
        ni = NickServ.findNick ( user.getString ( NAME )  );
        user.getModes().set ( IDENT, user.isIdented ( ni ) );
        nick.fixIdentState ( user );
        for ( Chan c : user.getChans ( ) ) {
            ChanServ.addCheckUser ( c, user );
//            chan.checkUser ( c, user );
        }
        this.oper.checkUser ( user ); /* Add user in OperServ check queue (akills etc) */
    }
    
    private void doSJoin ( User user )  {
        /* User joined a channel */
        Chan c = findChan ( this.data[3] );
        if ( c != null ) {
            c.addUser ( USER, user );
            user.addChan ( c );
        } else {
            this.doChan ( true );
        }
        if ( ! c.isSaJoin() ) {
            ChanServ.addCheckUser ( c, user );
//            chan.checkUser ( c, user );
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
        int command = this.data[4].hashCode();
        Chan chan = Handler.findChan(this.data[5].substring ( 1 ));
        //String chan = this.data[5].substring ( 1 );
        String string = Handler.cutArrayIntoString ( this.data, 6 ).replace(")", "");
        
        switch ( command ) {
            case SAJOIN :
                log = new CSLogEvent ( chan.getString(NAME), SAJOIN, string, user.getOper().getName() );
                ChanServ.addLog ( log );
                chan.toggleSaJoin();
                break;
                
            case SAMODE :
                log = new CSLogEvent ( chan.getString(NAME), SAMODE, string, user.getOper().getName() );
                ChanServ.addLog ( log );
                break;
                
            default :
                
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
            this.sendCmd ( ":"+config.get ( NAME ) +" PONG "+config.get ( NAME ) +" "+target ); 
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
            sidList.add ( sid ); 
        } catch ( Exception e )  {
            Proc.log ( Handler.class.getName ( ) , e );
        }
    }
    

    public static User findUser ( String source ) {
        if  ( uList == null )  {
            return null;
        }
        int code = source.toUpperCase().hashCode ( );
        for ( User u : uList ) {
  //          System.out.println("NICK:"+u.getString(NAME));
//            System.out.println("d: u:"+u.getString(NAME)+":"+u.hashCode()+":source:"+source+":"+code);
            if ( u.hashCode() == code ) {
                return u;
            }
        } 
        return null;
    }
    
    public static Chan findChan ( String source )  {
        int hashCode = source.toUpperCase().hashCode ( );
        for ( Chan c : cList )  {
            if ( c.getHashName ( ) == hashCode )  {
                return c;
            }
        }
        return null;
    }
     
    public Server getServer ( String source )  {
        return findServer ( source );
    }
    
    public static Server findServer ( String source )  {
        int hashCode = source.toUpperCase().hashCode ( );
        for ( Server s : sList )  {
            if ( s.getHashName ( )  == hashCode )  {
                return s;
            }
        }
        return null;
    }

    private void deleteEmpty ( Chan chan )  {
        /* If the channel is empty lets remove it from memory */
        try {
            if ( chan.empty ( ) )  {
                 cList.remove ( chan );
            }
        } catch ( Exception e )  { 
            Proc.log ( Handler.class.getName ( ) , e );
        }
    }

    public static void squitUser ( User user )  {
   //     System.out.println("squitUser("+user.getString(NAME)+");");
        try {
            user.getSID().setSplitExpire ( );
            user.partAll ( );
            splitSIDs.add ( user.getSID ( ) );
            uList.remove ( user );
        } catch ( Exception e )  { 
            Proc.log ( Handler.class.getName ( ) , e );
        }
    }

    public static void deleteUser ( User user )  {
     //   System.out.println("deleteUser("+user.getString(NAME)+");");
        try {
            user.getSID().remUser ( ); 
            user.partAll ( );
            user.quitServer ( );
            uList.remove ( user );
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
            Config conf = Proc.getConf ( );
            Server sHub = this.getServer ( conf.get ( CONNNAME )  );
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

    public static SimpleDateFormat getSdf ( )           { return sdf; }
    

    public static ServicesID findSid ( long id )  {
        try {
            ServicesID split = null;
            for ( ServicesID s : splitSIDs ) {
                if ( s.getID ( ) == id ) {
                    split = s;
                }
            }
            if ( split != null ) {
                splitSIDs.remove ( split );
                return split;
            }
            for ( ServicesID s : sidList )  {
                if ( s.getID ( ) == id )  {
                    return s;
                }
            } 
        } catch ( Exception e )  { 
            Proc.log ( Handler.class.getName ( ) , e );
        }
        return null;
    }
   
    /* MAINTENANCE METHODS */
    public int runSecondMaintenance() {
        int todoAmount = 0;
        todoAmount += oper.secMaintenance ( );
        for ( User user : uList ) {
            user.secMaintenence ( );
        }
        /* Database updates */
        todoAmount += updateServicesIDs();
        todoAmount += NickServ.maintenance ( );
        todoAmount += ChanServ.maintenance ( );
        return todoAmount;
    }
    private int updateServicesIDs ( ) {
        if ( updServicesID.isEmpty() || ! Database.activateConnection() ) {
            return updServicesID.size();
        }
        ArrayList<ServicesID> sids = new ArrayList<>(); 
        for ( ServicesID sid : updServicesID ) {
            if ( ( sid.getNiList().size() == 0 && sid.getCiList().size() == 0 ) ||
                Database.updateServicesID ( sid ) ) {
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
            initServices ( ); /* make sure everything is running */
            todoAmount += oper.minMaintenance ( );
            db.runMaintenance ( );
            
 //           this.checkNiStates ( );
            this.sidCleaner ( );
            this.cmdQueue.maintenance ( );
        
        } catch ( Exception e )  { 
            Proc.log ( Handler.class.getName ( ) , e );
        }
        return todoAmount;
    }
    public int runHourMaintenance ( )  {
        ArrayList<ServicesID> splitExpire = new ArrayList<>();
        int todoAmount = 0;
        try {
            initServices ( ); /* make sure everything is running */
            todoAmount += NickServ.maintenance ( );
            todoAmount += ChanServ.maintenance ( );
            for ( ServicesID s : splitSIDs ) {
                if ( s.timeToExpire ( ) ) {
                    splitExpire.add ( s );
                }
            }
            for ( ServicesID s : splitExpire ) {
                splitSIDs.remove ( s );
            }
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
            for ( ServicesID s : sidList )  {
                if ( s.hasExpired ( )  )  {
                    buf2.add ( s );
                }
            }
            for ( ServicesID r : buf2 )  {
                sidList.remove ( r );
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
            //chan.checkTopic ( user, c );
        }
        ChanInfo ci = ChanServ.findChan ( data[2] );
        if ( ci != null )  {
            /* The channel is registered, lets check if access is needed then set a new topic on it */
              
            /* Save topic only if its goes through the checks */
            if ( chan.checkTopic ( user, c ) ) {
                ci.getChanges().change ( TOPIC );
                ChanServ.addToWorkList ( CHANGE, ci );
            }
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
        ServSock.sendCmd ( ":"+config.get ( NAME ) +" 371 "+user.getString ( NAME ) +" :"+name+" has been disabled, try again later." ); 
    }
     
    public static ArrayList<User> findUsersByNick ( NickInfo ni )  {
        ArrayList<User> ul = new ArrayList<> ( );
        for  (  User user : uList  )  {
            if  (  user.isIdented ( ni )   )  {
                ul.add ( user );
            }
        } 
        return ul;
    }
     
    public static ArrayList<User> findUsersByMask ( String mask )  {
        ArrayList<User> ul = new ArrayList<> ( );
        for ( User user : uList )  {
            if ( StringMatch.maskWild ( user.getName()+"!"+user.getString(USER)+"@"+user.getString ( HOST ) , mask )         ||
                 StringMatch.maskWild ( user.getName()+"!"+user.getString(USER)+"@"+user.getString ( REALHOST ) , mask )     ||
                 StringMatch.maskWild ( user.getName()+"!"+user.getString(USER)+"@"+user.getString ( IP ) , mask )  )  {
                 ul.add ( user );
            }  
        } 
        return ul;        
    }
    
    
    public static ArrayList<User> findUsersByBan ( ServicesBan ban ) {
        ArrayList<User> ul = new ArrayList<>();
        if ( ban.getCidr() == null ) {
            ul = findUsersByMask ( ban.getMask() );
        } else {
            for ( User u : uList ) {
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
        for ( User user : uList ) {
            if ( StringMatch.nickWild ( user.getString ( NAME ), nick ) ) {
                ul.add ( user );
            }
        }
        return ul;
    }

    public static ArrayList<User> findUsersByGcos ( String gcos ) {
        ArrayList<User> ul = new ArrayList<> ( );
        for ( User user : uList ) {
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
        state = String.valueOf ( data.charAt ( data.length ( ) - 1 ) );
        switch ( state.toUpperCase().hashCode ( ) ) {
            case CHAR_m : 
                timeUnit = "MINUTE";
                break;
                
            case CHAR_h : 
                timeUnit = "HOUR";
                break;
                
            case CHAR_d : 
                timeUnit = "DAY";
                break;
                         
            case CHAR_y : 
                timeUnit = "YEAR";
                break;
                
            default: 
                return "INTERVAL 0 DAYS";
            
        }
        return  "INTERVAL "+( amount * multiply )+" "+timeUnit;
    }
 
    public static Date expireToDate ( Date date, String data ) {
        String strBuf;
        int ms = 0;
        int amount;
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        System.out.println("debug(0): datetime:"+dateFormat.format(date)+", data:"+data);
 
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
       
        System.out.println("debug: amount:"+amount+", ms:"+ms);
        date.setTime ( date.getTime() + ( amount*ms ) );
        return date;
    }
    
    public static String expireToDateString ( String datetime, String data ) {
        String strBuf;
        int ms = 60*1000;
        int amount;
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        System.out.println("debug(1): datetime:"+datetime+", data:"+data);

        Date date;
        try {
            date = dateFormat.parse ( datetime );
            amount = Integer.parseInt ( data );            
        } catch ( NumberFormatException | ParseException ex ) {
            Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
        System.out.println("debug: amount:"+amount+", ms:"+ms);
        date.setTime ( date.getTime() + ( amount*ms ) );
        return dateFormat.format ( date );
    }
    
    private void doFinishSync(String[] data) {
        /* Fix Master after we synched */ 
        root.fixMaster ( );
        oper.sendSpamFilter ( );
    }

    
    public static ArrayList<User> getUserList ( ) {
        return uList;
    }

    public static boolean sanityCheck() {
        return sanity;
    }

    private void nullService ( User user ) {
        int nameHash = user.getString ( NAME ).toUpperCase().hashCode();
        switch ( nameHash ) {
            case ROOTSERV :
                root = null;
                break;
                
            case OPERSERV :
                oper = null;
                break;
                
            case NICKSERV :
                nick = null;
                break;
                
            case CHANSERV :
                chan = null;
                break;
                
            case MEMOSERV :
                memo = null;
                break;
                
            case GUESTSERV :
                guest = null;
                break;
                
            case GLOBAL :
                global = null;
                break;
                
        }
    }

    private void doError ( ) {
        int sub1 = this.data[1].replace(":", "").toUpperCase().hashCode();
        int sub2 = this.data[2].replace(":", "").toUpperCase().hashCode();
        
        if ( sub1 == CLOSING && sub2 == LINK ) {
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
        for ( User user : uList ) {
            if ( user.isIdented ( ci ) ) {
                iList.add ( user );
            }
        }
        return iList;
    }
 
    public static void addSplitSID ( ServicesID sid ) {
        splitSIDs.add ( sid );
    }
    
    public static void printSIDs ( ) {
        for ( ServicesID sid : splitSIDs ) {
            System.out.println("splitSID: id:"+sid.getID() );
        }
    }

    public static ArrayList<ServicesID> getSIDs ( ) {
        return sidList;
    }
    public static ArrayList<ServicesID> getSplitSIDs ( ) {
        return splitSIDs;
    }

    public static ArrayList<Chan> getChanList ( ) {
        return cList;
    }
 
}
