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

import core.CommandInfo;
import core.Handler;
import core.Proc;
import core.StringMatch;
import core.Service;
import java.text.DateFormat;
import user.User;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import nickserv.NickInfo;
import server.Server;

/**
 *
 * @author DreamHealer
 */
public class OperServ extends Service {
    private static boolean is = false; 

    private static ArrayList<ServicesBan> akills = new ArrayList<> ( );   /* AutoKills */
    private static ArrayList<ServicesBan> ignores = new ArrayList<> ( );   /* Services Ignores */
    private static ArrayList<ServicesBan> sqlines = new ArrayList<> ( );   /* SQLines */
    private static ArrayList<ServicesBan> sglines = new ArrayList<> ( );   /* SGLines */
    private static ArrayList<SpamFilter> spamfilters = new ArrayList<> ( );   /* SPAMFILTER list */
    
    private static ArrayList<ServicesBan> addServicesBans = new ArrayList<>(); /* Add new services bans */
    private static ArrayList<ServicesBan> remServicesBans = new ArrayList<>(); /* Remove services bans */
    private static ArrayList<ServicesBan> addLogServicesBans = new ArrayList<>(); /* Add new services bans logs */

    private static ArrayList<NetServer> servers = new ArrayList<> ( );   /* Server list */
    private static ArrayList<NetServer> remServers = new ArrayList<> ( );   /* Remove Server list */
    private static ArrayList<NetServer> addServers = new ArrayList<> ( );   /* Add Server list */
    private static ArrayList<SpamFilter> addSpamFilters = new ArrayList<>(); /* Add new spamfilters */
    private static ArrayList<SpamFilter> remSpamFilters = new ArrayList<>(); /* Remove spamfilters */
      
    private static ArrayList<Oper> staff = new ArrayList<> ( );   /* Staff list */
    private static ArrayList<Oper> addStaff = new ArrayList<> ( );   /* Staff list */
    private static ArrayList<Oper> remStaff = new ArrayList<> ( );   /* Staff list */

    private static ArrayList<OSLogEvent> logs = new ArrayList<> ( );   /* LogEvent list */

    
    private ArrayList<User> chList = new ArrayList<>();     /* users who are schedualed for ban checks */
    
    private OSExecutor                  executor;   /* Object that parse and execute commands */
    private OSHelper                    helper;     /* Object that parse and respond to help queries */
    private OSSnoop                     snoop;      /* Object for monitoring and reporting */
    private SimpleDateFormat            sdf;
    
    private Oper operNick = new Oper ( "OperServ", 4, "OperServ" );
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 

    private static ArrayList<Timer> timerList = new ArrayList<>();
    
    
    public OperServ ( )  {
        super ( "OperServ" );
        this.initOperServ ( );
    }

    private void initOperServ ( )  {
        is              = true;
        this.snoop      = new OSSnoop ( this );
        this.executor   = new OSExecutor ( this, this.snoop );
        this.helper     = new OSHelper ( this, this.snoop );  
        akills          = OSDatabase.getServicesBans ( AKILL );
        ignores         = OSDatabase.getServicesBans ( IGNORE );
        sqlines         = OSDatabase.getServicesBans ( SQLINE );
        sglines         = OSDatabase.getServicesBans ( SGLINE );
        spamfilters     = OSDatabase.getSpamFilters ( );
        staff           = OSDatabase.getAllStaff ( );
        servers           = OSDatabase.getServerList ( );
        setCommands ( );
    }

    public void updConf ( ) {
        setCommands ( );
    }
 
    public void setCommands ( )  {
        cmdList = new ArrayList<> ( );
        cmdList.add ( new CommandInfo ( "HELP",      1,                         "Show help information" )                 );
        cmdList.add ( new CommandInfo ( "UINFO",     CMDAccess ( UINFO ),       "Show user information" )                 );
        cmdList.add ( new CommandInfo ( "CINFO",     CMDAccess ( CINFO ),       "Show channel information" )              );
        cmdList.add ( new CommandInfo ( "ULIST",     CMDAccess ( ULIST ),       "Show user list" )                        );
        cmdList.add ( new CommandInfo ( "CLIST",     CMDAccess ( CLIST ),       "Show user list" )                        );
        cmdList.add ( new CommandInfo ( "SLIST",     CMDAccess ( SLIST ),       "Show server list" )                      );
        cmdList.add ( new CommandInfo ( "UPTIME",    CMDAccess ( UPTIME ),      "Show uptime" )                           );
        cmdList.add ( new CommandInfo ( "AKILL",     CMDAccess ( AKILL ),       "Manage the AKill list" )                 );
        cmdList.add ( new CommandInfo ( "STAFF",     CMDAccess ( STAFF ),       "Manage the Staff list" )                 );
        cmdList.add ( new CommandInfo ( "SEARCHLOG", CMDAccess ( SEARCHLOG ),   "Search the logs for nick or channel" )   );
        cmdList.add ( new CommandInfo ( "AUDIT",     CMDAccess ( AUDIT ),       "Search access logs" )                    );
        cmdList.add ( new CommandInfo ( "COMMENT",   CMDAccess ( COMMENT ),     "Add comment about nick or chan" )        );
        cmdList.add ( new CommandInfo ( "GLOBAL",    CMDAccess ( GLOBAL ),      "Send message to all connected users" )   );
        cmdList.add ( new CommandInfo ( "IGNORE",    CMDAccess ( IGNORE ),      "Manage the services ignore list" )       );
        cmdList.add ( new CommandInfo ( "BANLOG",    CMDAccess ( BANLOG ),      "Search services ban log" )               );
        cmdList.add ( new CommandInfo ( "SQLINE",    CMDAccess ( SQLINE ),      "Manage the services Q-line list" )       );
        cmdList.add ( new CommandInfo ( "SGLINE",    CMDAccess ( SGLINE ),      "Manage the services G-line list" )       );
        cmdList.add ( new CommandInfo ( "SPAMFILTER",CMDAccess ( SPAMFILTER ),  "Manage the services SpamFilter list" )   );
        cmdList.add ( new CommandInfo ( "JUPE",      CMDAccess ( JUPE ),        "Jupiter a server" )                      );
        cmdList.add ( new CommandInfo ( "SERVER",    CMDAccess ( SERVER ),      "Handle server list" )                    );
        cmdList.add ( new CommandInfo ( "FORCENICK", CMDAccess ( FORCENICK ),   "Forcefully change a users nickname" )    );
        cmdList.add ( new CommandInfo ( "BAHAMUT",   CMDAccess ( BAHAMUT ),     "Print bahamut compatibility version" )   );
    }
   
    public static ArrayList<CommandInfo> getCMDList ( int command ) {
        return Handler.getOperServ().getCommandList ( command );
    }
    
    public static boolean enoughAccess ( User user, int hashName ) {
        return Handler.getOperServ().checkAccess ( user, hashName );
    }
    
    public boolean checkAccess ( User user, int hashName ) {
        int access              = user.getAccess ( );
        if ( hashName == AUTOKILL ) {
            hashName = AKILL;
        }
        CommandInfo cmdInfo     = this.findCommandInfo ( hashName );
        if ( access >= cmdInfo.getAccess ( ) )  {
            return true;
        }
        return false;
    }
     
    public void hourMaintenance ( ) {
        this.expireBans ( );
    }
    public int secMaintenance ( ) {
        int todoAmount = 0;
        this.checkUserList ( );
        todoAmount += this.checkAddServers ( );
        todoAmount += this.checkRemServers ( );
        todoAmount += this.checkAddServicesBans ( );
        todoAmount += this.checkRemServicesBans ( );
        todoAmount += this.checkAddSpamFilters ( );
        todoAmount += this.checkRemSpamFilters ( );
        todoAmount += this.checkAddStaff ( );
        todoAmount += this.checkRemStaff ( );
        todoAmount += this.checkLogEvents ( );
         
        return todoAmount;
    }
    
    public int minMaintenance ( ) {
        int todoAmount = 0;
        todoAmount += this.expireSpamFilter ( );
        return todoAmount;
    }
    
    public int expireSpamFilter ( ) {
        ArrayList<SpamFilter> remList = new ArrayList<>();
        for ( SpamFilter sf : spamfilters ) {
            if ( sf.hasExpired ( ) ) {
                this.sendServ ( "SF "+sf.getPattern()+" 0" );
                this.sendGlobOp ( "SpamFilter expired for "+sf.getPattern()+" (Instated by: "+sf.getInstater()+" on: "+sf.getStamp()+" reason: "+sf.getReason()+")" );
                remList.add ( sf );
                remSpamFilters.add ( sf );              
            }
        }
        for ( SpamFilter sf : remList ) {
            spamfilters.remove ( sf );
        }
        return spamfilters.size();
    }
    
    public int checkAddServicesBans ( ) {
        if ( addServicesBans.isEmpty() || ! OSDatabase.checkConn() ) {
            return addServicesBans.size();
        }
        ArrayList<ServicesBan> sList = new ArrayList<>();
        for ( ServicesBan sb : addServicesBans ) {
            if ( OSDatabase.addServicesBan ( sb ) ) {
                sList.add ( sb );
            }
        }
        for ( ServicesBan sf : sList ) {
            addServicesBans.remove ( sf );
        }
        return addServicesBans.size();
    }
    
    public int checkRemServicesBans ( ) {
        if ( remServicesBans.isEmpty() || ! OSDatabase.checkConn() ) {
            return remServicesBans.size();
        } 
        ArrayList<ServicesBan> sList = new ArrayList<>();
        for ( ServicesBan sb : remServicesBans ) {
            if ( OSDatabase.delServicesBan ( sb ) ) {
                sList.add ( sb );
            }
        }
        for ( ServicesBan sf : sList ) {
            remServicesBans.remove ( sf );
        }
        return remServicesBans.size();
    }
    
    public int checkAddSpamFilters ( ) {
        if ( addSpamFilters.isEmpty() || ! OSDatabase.checkConn() ) {
            return addSpamFilters.size();
        } 
        ArrayList<SpamFilter> sList = new ArrayList<>();
        for ( SpamFilter sf : addSpamFilters ) {
            if ( OSDatabase.addSpamFilter ( sf ) ) {
                sList.add ( sf );
            }
        }
        for ( SpamFilter sf : sList ) {
            addSpamFilters.remove ( sf );
        }
        return addSpamFilters.size();
    }
    
    public int checkRemSpamFilters ( ) {
        if ( remSpamFilters.isEmpty() || ! OSDatabase.checkConn() ) {
            return remSpamFilters.size();
        } 
        ArrayList<SpamFilter> sList = new ArrayList<>();
        for ( SpamFilter sf : remSpamFilters ) {
            if ( OSDatabase.remSpamFilter ( sf ) ) {
                sList.add ( sf );
            }
        }
        for ( SpamFilter sf : sList ) {
            remSpamFilters.remove ( sf );
        }
        return remSpamFilters.size();
    }
    
    public int checkRemServers ( ) {
        if ( remServers.isEmpty() || ! OSDatabase.checkConn() ) {
            return remServers.size();
        }
        ArrayList<NetServer> sList = new ArrayList<>();
        for ( NetServer server : remServers ) {
            if ( OSDatabase.delServer ( server.getName() ) ) {
                sList.add ( server );
            }
        }
        for ( NetServer server : sList ) {
            remServers.remove ( server );
        }
        return remServers.size();
    }
    
    public int checkAddServers ( ) {
        if ( addServers.isEmpty() || ! OSDatabase.checkConn() ) {
            return addServers.size();
        }
        ArrayList<NetServer> sList = new ArrayList<>();
        for ( NetServer server : addServers ) {
            if ( OSDatabase.addServer ( server.getName() ) ) {
                sList.add ( server );
            }
        }
        for ( NetServer server : sList ) {
            addServers.remove ( server );
        }
        return addServers.size();
    }
    
    public int checkAddStaff ( ) {
        if ( addStaff.isEmpty() || ! OSDatabase.checkConn() ) {
            return addStaff.size();
        }
        ArrayList<Oper> oList = new ArrayList<>();
        for ( Oper oper : addStaff ) {
            if ( OSDatabase.addStaff ( oper ) ) {
                oList.add ( oper );
            }
        }
        for ( Oper oper : oList ) {
            addStaff.remove ( oper );
        }
        return addStaff.size();
    }
       
    public int checkRemStaff ( ) {
        if ( remStaff.isEmpty() || ! OSDatabase.checkConn() ) {
            return remStaff.size();
        }
        ArrayList<Oper> oList = new ArrayList<>();
        for ( Oper oper : remStaff ) {
            if ( OSDatabase.delStaff ( oper ) ) {
                oList.add ( oper );
            }
        }
        for ( Oper oper : oList ) {
            remStaff.remove ( oper );
        }
        return remStaff.size();
    }
          
    public int checkLogEvents ( ) {
        if ( logs.isEmpty() || ! OSDatabase.checkConn() ) {
            return logs.size();
        }
        ArrayList<OSLogEvent> lList = new ArrayList<>();
        for ( OSLogEvent log : logs ) {
            if ( OSDatabase.logEvent ( log ) > 0 ) {
                lList.add ( log );
            }
        }
        for ( OSLogEvent log : lList ) {
            logs.remove ( log );
        }
        return logs.size();
    }
    
    private void checkUserList ( ) {
        int res;
        ServicesBan ban;
        String newName;
        ArrayList<User> checked = new ArrayList<>();
        if ( ! OSDatabase.checkConn() ) {
            return;
        }
        
        int[] commands = new int[] { AKILL, SGLINE, SQLINE };
        Random rand = new Random ( );
        
        
        for ( User u : this.chList ) {
            ban = null;
            for ( int command : commands ) {
                if ( ban == null ) {
                    switch ( command ) {
                        case AKILL :
                            ban = findBan ( command, u.getFullMask() );
                            if ( ban != null ) {
                                this.ban ( u, ban );
                                Handler.deleteUser ( u );
                            }                            
                            break;

                        case SQLINE :
                            ban = findBan ( command, u.getString ( NAME ) );
                            if ( ban != null ) {
                                newName = "SQLined"+rand.nextInt(99999);
                                this.ban ( u, ban );
                                this.sendServ ( "SVSNICK "+u.getString ( NAME )+" "+newName+" :0" );
                                u.setName ( newName );
                            }
                            break;

                        case SGLINE :
                            ban = findBan ( command, u.getString ( REALNAME ) );
                            if ( ban != null ) {
                                this.ban ( u, ban );
                                Handler.deleteUser ( u );
                            }                            
                            break;
                            
                        default : 
                            ban = null;
                    }
                }
                checked.add ( u );
            }
        }
        this.chList.removeAll ( checked );
        
        for ( User u2 : this.chList ) {
            System.out.println("QUEUE: "+u2.getString ( NAME ) );
        }
        
    }
    
    private void expireBans ( ) {
        int[] commands = new int[] { AKILL, SGLINE, SQLINE };
        ArrayList<ServicesBan> delList = new ArrayList<>( );
        ArrayList<ServicesBan> list = null;
        for ( int command : commands ) {
            list = getBanList ( command );
            if ( list != null ) {
                for ( ServicesBan a : list ) {
                    if ( a.hasExpired ( ) ) {
                        delList.add(a);
                    }
                }
            }
        }
        
        for ( ServicesBan del : delList ) {
            this.unBan ( del );
            remServicesBans.add ( del );
            this.sendServ( "GLOBOPS :"+del.getBanTypeStr()+" for "+del.getMask()+" has expired. [Instated by: "+del.getInstater()+" at: "+del.getTime()+"]");
     //       this.sendGlobOp ( del.getBanTypeStr()+" for "+del.getMask()+" has expired. [Instated by: "+del.getInstater()+" at: "+del.getTime()+"]" );
            getBanList(del.getType()).remove ( del );
        }
        
    }
    
    private ArrayList<ServicesBan> getBanList ( int hash ) {
        switch ( hash ) {
            case AKILL :
                return akills;
            case SQLINE :
                return sqlines;
            case SGLINE :
                return sglines;
            default :
                return null;
        }
    }
    private void expireBansOLD ( )  {
        if ( ! OSDatabase.checkConn ( )  )  {
            return;
        }
        int[] commands = new int[] { AKILL, SGLINE, SQLINE };
        
        ArrayList<ServicesBan> banList = new ArrayList<>( );
        ArrayList<ServicesBan> list = null;
        for ( int command : commands ) {
            switch ( command ) {
                case AKILL :
                    list = akills;
                    break;
                case SQLINE :
                    list = sqlines;
                    break;
                case SGLINE :
                    list = sglines;
                    break;
                default :
                    list = null;
            }
            
            if ( list != null ) {
                for ( ServicesBan a : OSDatabase.getExpiredBans ( command )  )  {
                    for ( ServicesBan ak : list )  {
                        if ( a.getMaskHash ( )  == ak.getMaskHash ( )  )  {
                            banList.add ( ak );
                        }
                    }
                }
            }

        }
        
        for ( ServicesBan a : banList )  {
            if ( OSDatabase.delServicesBan ( a ) ) {
                this.unBan ( a );
//                this.sendGlobOp ( output ( AKILL_EXPIRE, a.getMask ( ) , ""+a.getID ( ) , a.getInstater ( )  )  );
                list.remove(a);
            } 
        }
    }
    
    public void parse ( User user, String[] cmd )  {
        user.getUserFlood().incCounter ( this );

        cmd[3] = cmd[3].substring ( 1 );
        if ( user == null ) {
            System.out.println("DEBUG: parse() -> no user");
            return;
        } else if ( ! user.isOper ( ) )  {
            this.sendMsg ( user, "IRC Operator Services are for IRC Operators only .. *sigh*" );
            return;
        } else if ( ! OperServ.enoughAccess ( user, cmd[3].toUpperCase().hashCode() ) ) {
            this.sendMsg ( user, "Access denied!." );
            return;
        }
        
        switch ( cmd[3].toUpperCase().hashCode ( ) ) {
            case HELP :
                this.helper.parse ( user, cmd );
                break;
               
            default:
                this.executor.parse ( user, cmd ); 
           
        }
    }
     // public void sendSnoop ( String msg )  { this.snoop.msg ( msg ); }
 
    public static ArrayList<Oper> findRootAdmins ( )  {
        if ( ! is ) {
            return null;
        }
        return getRootAdmins ( );
    }
    public static ArrayList<Oper> findCSOps ( )  {
        if ( ! is ) {
            return null;
        }
        return getCSops ( );
    }
    
    public static ArrayList<Oper> findServicesAdmins ( )  {
        if ( ! is )  {
            return null;
        }
        return getServicesAdmins ( );
    }

    
/*    public boolean isBanned ( User u )  {
        if ( u == null )  { return true; } 
        for ( ServicesBan ban : akills )  {
            if ( ban.match ( u.getString ( USER ) +"@"+u.getString ( HOST ) ) )  {
                ban ( ban ); 
                return true;
            }
        }
        return false;
    }
 */   
    public void ban ( User user, ServicesBan ban )  {
        switch ( ban.getType() ) {
            case AKILL :
                this.unBan ( ban );
                this.sendServ ("AKILL "+
                    ban.getHost ( ) +" "+
                    ban.getUser ( ) +" "+ 
                    ban.getExpireSec ( ) +" "+
                    ban.getInstater ( ) +" "+
                    ( System.currentTimeMillis ( ) / 1000 ) +
                    " :"+ban.getReason ( ) +" [Ticket: "+ban.getID ( ) +"]" 
                );
                break;
                     
            case SQLINE :
                this.unBan ( ban );
                this.sendServ ( "SQLINE "+ban.getMask()+" :"+ban.getReason() );
                break;
                
            case SGLINE :
                this.unBan ( ban );
                this.sendServ ( "SGLINE "+ban.getMask().length()+" :"+ban.getMask()+":"+ban.getReason() );
                this.sendServ ( "KILL "+user.getString ( NAME )+" :gcos violation [Ticket: SG"+ban.getID()+"]" );
                break;
            
        }
    }
    private void unBan ( ServicesBan ban )  {
        switch ( ban.getType() ) {
            case AKILL :
                this.sendServ ( "RAKILL " + ban.getHost ( ) + " " + ban.getUser ( ) );
                break;
                
            case SQLINE :
                this.sendServ ( "UNSQLINE :"+ban.getMask() );
                break;
                         
            case SGLINE :
                this.sendServ ( "UNSGLINE :"+ban.getMask() );
                break;
                
        }
    }
    
   
    public static ArrayList<ServicesBan> getListByCommand ( int command ) {
        switch ( command ) {
            case AKILL :
                return akills;
                
            case IGNORE :
                return ignores;
                           
            case SQLINE :
                return sqlines;
                         
            case SGLINE :
                return sglines;
                
            default :
                return new ArrayList<>();
                
        }
    }
    
    private int delServicesBan ( ServicesBan ban )  {
        if ( OSDatabase.delServicesBan ( ban ) ) {
            OSDatabase.logServicesBan ( DEL, ban );
            switch ( ban.getType() ) {
                case SQLINE :
                case SGLINE :
                case AKILL :
                    this.unBan ( ban );
            }
            return 1;
        }
        return 0;
    }
  
    public void sendServicesBan ( ServicesBan ban )  {
                
        if ( ban != null )  {
            // getListByCommand(ban.getType()).add ( ban );
          //  addServicesBans.add ( ban );
            
//            OSDatabase.logServicesBan ( ADD, ban );
            //:services.avade.net AKILL 172.16.4.* fredde 1378430849 DreamHealer 1374430849 :Test akill
            switch ( ban.getType() ) {
                case AKILL :
                    this.sendServ ( 
                        "AKILL "+
                        ban.getHost ( ) +" "+
                        ban.getUser ( ) +" "+
                        ban.getExpireSec ( ) +" "+
                        ban.getInstater ( ) +" "+
                        ( System.currentTimeMillis ( ) / 1000 ) +
                        " :"+ban.getReason ( )+"[Ticket: AK"+ban.getID ( ) +"] "
                    );
                    break;
                    
                case SQLINE :
                    this.unBan ( ban );
                    this.sendServ ( "SQLINE "+ban.getMask()+" :"+ban.getReason() );
                    int index = 1;
                    String nick;
                    User buf;
                    String prefix = "SQLined";
                    for ( User u : Handler.getUserList() ) {
                        nick = u.getString ( NAME );
                        if ( StringMatch.nickWild ( nick, ban.getMask() ) ) {
                            while ( ( buf = Handler.findUser ( prefix+index ) ) != null ) {
                                index++;
                            }
                            this.sendServ ( "SVSNICK "+u.getString(NAME)+" "+prefix+index+" :0" );
                            index++;
                        }
                    }
                    break;
                 
                case SGLINE :
                    this.unBan ( ban );
                    this.sendServ ( "SGLINE "+ban.getMask().length()+" :"+ban.getMask()+":"+ban.getReason() );
                    for ( User u : Handler.getUserList() ) {
                        if ( StringMatch.wild ( u.getString ( REALNAME ), ban.getMask ( ) ) ) {
                            this.sendServ ( "KILL "+u.getString(NAME)+" :gcos violation [Ticket: SG"+ban.getID()+"]" );
                        }
                    }

                    break;
                    
            }
        }
    }
    
  /*  public static ArrayList<ServicesBan> getBans ( int command )  {
        switch ( command ) {
            case AKILL :
                return akills;
                
            default :
                return new ArrayList<>();
        }
    }*/
      
    public static void is ( boolean state ) { 
        is = state; 
    }
    public void setState ( boolean state ) { 
        OperServ.is = state;
    }

    public static boolean isUp ( ) { 
        return is; 
    }

    public void checkUser ( User user ) {
        this.chList.add ( user );
    }
    
    public static ServicesBan findBanByID ( int command, String banId ) {
        if ( banId == null ) {
            return null;
        }
        int hash = banId.toUpperCase().hashCode();
        for ( ServicesBan ban : getListByCommand ( command ) ) {
            if ( ban.getHash() == hash ) {
                return ban;
            }
        }
        return null;
    }
    
    
    public static ArrayList<ServicesBan> findBansByPattern ( int command, String pattern )  {
        ArrayList<ServicesBan> bList = new ArrayList<>();
        switch ( command ) {
            case AKILL :
                for ( ServicesBan ban : akills )  {
                    if ( StringMatch.maskWild ( ban.getMask ( ), pattern )  )  {
                        bList.add ( ban );
                    }
                }
                break;
            
            case IGNORE :
                for ( ServicesBan ban : ignores ) {
                    if ( StringMatch.maskWild ( ban.getMask(), pattern) ) {
                        bList.add ( ban );
                    }   
                }
                break;
                
            case SQLINE :
                for ( ServicesBan ban : sqlines ) {
                    if ( StringMatch.nickWild ( ban.getMask(), pattern) ) {
                        bList.add ( ban );
                    }   
                }
                break;
                
            case SGLINE :
                for ( ServicesBan ban : sglines ) {
                    if ( StringMatch.wild ( ban.getMask(), pattern) ) {
                        bList.add ( ban );
                    }   
                }
                break;
                
            default :
                
        }
        return bList;
    }
    
    public static ServicesBan findBan ( int command, String usermask )  {
        switch ( command ) {
            case AKILL :
                for ( ServicesBan a : akills )  {
                    if ( StringMatch.maskWild ( a.getMask ( ), usermask )  )  {
                        return a;
                    }
                }
                break;
            
            case IGNORE :
                for ( ServicesBan b : ignores ) {
                    if ( StringMatch.maskWild ( b.getMask(), usermask) ) {
                        return b;
                    }   
                }
                break;
                
            case SQLINE :
                for ( ServicesBan b : sqlines ) {
                    if ( StringMatch.nickWild ( b.getMask(), usermask) ) {
                        return b;
                    }   
                }
                break;
                
            case SGLINE :
                for ( ServicesBan b : sglines ) {
                    if ( StringMatch.wild ( b.getMask(), usermask) ) {
                        return b;
                    }   
                }
                break;
                
            default :
                
        }
        return null;
    }
     
/*    public String output ( int code, String... args )  {
        switch ( code )  {
            case AKILL_EXPIRE :
                return "Akill "+args[0]+" [Ticket:"+args[1]+"] [by:"+args[2]+"] has expired.";
                
            case AKILL_FAIL_EXPIRE :
                return "Akill "+args[0]+" [Ticket:"+args[1]+"] [by:"+args[2]+"] failed to expired.";
                
            default:
                return ""; 
            
        }
    }     
    private static final int AKILL_EXPIRE           = 1001;
    private static final int AKILL_FAIL_EXPIRE      = 1002;
*/
    
    public int getAkillCount() {
        return akills.size();
    }

    public int getIgnoreCount() {
        return ignores.size();
    }

    public boolean isIgnored(User user) {
        if ( user == null ) {
            return false;
        }
        ServicesBan ban;
        if ( ( ban = findBan ( IGNORE, user.getFullMask() ) ) != null ) {
            return true;
        }
        return false;
    }

    /**
     *
     * @param type
     * @return
     */
    public static ArrayList<NetServer> getServers ( int type, boolean missing ) {
        ArrayList<NetServer> sList = new ArrayList<>();
        int pHash;
        int sHash;
        int hash;
        switch ( type ) {
            case HUB :
                for ( NetServer server : servers ) {
                    hash = server.getName().toUpperCase().hashCode();
                    for ( NetServer server2 : servers ) {
                        pHash = server2.getPrimary().toUpperCase().hashCode();
                        sHash = server2.getSecondary().toUpperCase().hashCode();
                        if ( hash == pHash || hash == sHash ) {
                            sList.add ( server );
                        }
                    }
                }
                break;
                
            case LEAF :
                sList.addAll ( servers );
                ArrayList<NetServer> remList = new ArrayList<>();
                for ( NetServer server : servers ) {
                    hash = server.getName().toUpperCase().hashCode();
                    for ( NetServer server2 : servers ) {
                        pHash = server2.getPrimary().toUpperCase().hashCode();
                        sHash = server2.getSecondary().toUpperCase().hashCode();
                        if ( hash == pHash || hash == sHash ) {
                            sList.remove ( server );
                        }
                    }
                }
                break;
                
            default :
                
        }
        if ( missing ) {
            ArrayList<Server> online = Handler.getServerList ( );
            ArrayList<NetServer> rem = new ArrayList<>();

            for ( Server server : online ) {
                for ( NetServer server2 : sList ) {
                    if ( server2.getHashCode ( ) == server.getHashName ( ) ) {
                        rem.add ( server2 );
                    }
                }
                for ( NetServer r : rem ) {
                    sList.remove ( r );
                }
            }
        }
        return sList;         
    }
    
    public static boolean addDelServer ( String name ) {
        int hash = name.toUpperCase().hashCode();
        NetServer rem = null;
        for ( NetServer server : servers ) {
            if ( server.getHashCode() == hash ) {
                rem = server;
            }
        }
        if ( rem != null ) {
            servers.remove ( rem );
            remServers.add ( rem );
            return true;
        }
        return false;
    }
    public static void addServer ( String name ) {
        int hash = name.toUpperCase().hashCode();
        for ( NetServer s : servers ) {
            if ( s.getHashCode() == hash ) {
                return;
            }
        }
        NetServer server = new NetServer ( name, null, null );
        addServers.add ( server );
    }
    
    
    public static ArrayList<Oper> getStaffPlus ( int hash ) {
        ArrayList<Oper> oList = new ArrayList<> ( );
        int access = hashToAccess ( hash );
        for ( Oper o : staff ) {
            if ( o.getAccess() >= access ) {
                oList.add ( o );
            }
        }
        return oList;
    }
    
    public static int hashToAccess ( int hash ) {
        switch ( hash ) {
            case IRCOP :
                return 1;

            case SA :
                return 2;

            case CSOP :
                return 3;

            case SRA :
                return 4;

            case MASTER :
                return 5;

            default :
                return 0;
        }
    }
    
    public static ArrayList<Oper> getStaffByAccess ( int access )  {
        ArrayList<Oper> oList = new ArrayList<> ( );
        for ( Oper o : staff ) {
            if ( o.getAccess() == access ) {
                oList.add ( o );
            }
        }
        return oList;
    }
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
    
    public static Oper getOper ( String name ) {
        int hash = name.toUpperCase().hashCode();
        for ( Oper oper : staff ) {
            if ( oper.getHashCode() == hash ) {
                return oper;
            }
        }
        return new Oper ( );
    }
    
    public static void addServicesBan ( ServicesBan ban ) {
        addServicesBans.add ( ban );
        addLogServicesBans.add ( ban );
        getListByCommand(ban.getType()).add ( ban );
    }
    public static void remServicesBan ( ServicesBan ban ) {
        ServicesBan rem = null;
        remServicesBans.add ( ban );
        for ( ServicesBan b : getListByCommand ( ban.getType() ) ) {
            if ( ban.getMaskHash() == b.getMaskHash() ) {
                rem = b;
            }
        }
        if ( rem != null ) {
            getListByCommand(ban.getType()).remove ( rem );
            Handler.getOperServ().delServicesBan ( rem );            
        }
    }

    
    public static ArrayList<SpamFilter> getSpamFilters ( ) {
        return spamfilters;
    }
    
    static void addSpamFilter ( SpamFilter sFilter ) {
        addSpamFilters.add ( sFilter );
        spamfilters.add ( sFilter );
    }
    static void remSpamFilter ( SpamFilter sFilter ) {
        remSpamFilters.add ( sFilter );
    }

    public void sendSpamFilter ( ) {
        for ( SpamFilter sf : spamfilters ) {
            this.sendServ ( "SF "+sf.getPattern()+" "+sf.getBitFlags()+" :"+sf.getReason() );
        }
    }
    public static SpamFilter findSpamFilter ( String pattern ) {
        int hash = pattern.toUpperCase().hashCode();
        for ( SpamFilter sf : spamfilters ) {
            if ( sf.getHashPattern() == hash ) {
                return sf;
            }
        }
        return null;
    }

      
    public void addSFAkill ( String[] data ) {
        //:testnet.avade.net OS SFAKILL fredde 1539289892 hello to you too!
        //                 0  1       2      3          4 5+

        User user = Handler.findUser ( data[3] );
         
        String reason = Handler.cutArrayIntoString ( data, 5 );
        ServicesBan ban = this.findBan ( AKILL, user.getFullMask() );
        
        if ( ban != null ) {
            
            this.sendServ ( 
                "AKILL "+
                ban.getHost ( ) +" "+
                ban.getUser ( ) +" "+
                (60*60*24) +" "+
                ban.getInstater ( ) +" "+
                ( System.currentTimeMillis ( ) / 1000 ) +
                " :"+ban.getReason ( )+"[Ticket: AK"+ban.getID ( ) +"] "
            );
            return;
        }
        String expire = Handler.expireToTime ( "30d" );
        this.addServicesBan ( new ServicesBan ( AKILL, ""+System.nanoTime(), false,"*!*@"+user.getString(IP), reason, "OperServ", null, expire ) );
    }

    public static boolean isWhiteListed ( String usermask ) {
        for ( String white : Proc.getConf().getWhiteList() ) {
            if ( StringMatch.maskWild ( usermask, "*"+white ) ) {
                return true;
            }
        }
        return false;
    }
    
    static void addOper ( Oper oper ) {
        Oper rem = null;
        int hash = oper.getName().hashCode();
        for ( Oper o : staff ) {
            System.out.println("addOper: "+hash+":"+o.getName().hashCode());
            if ( o.getName().hashCode() == hash ) {
                rem = o;
            }
        }
        if ( rem != null ) {
            staff.remove ( rem );
        }
        staff.add ( oper );
        addStaff.add ( oper );
    }
    
    public static void delOper ( NickInfo ni ) {
        Oper oper = null;
        for ( Oper o : staff ) {
            if ( o.getHashCode() == ni.hashCode() ) {
                oper = o;
            }
        }
        if ( oper != null ) {
            staff.remove ( oper );
            remStaff.add ( oper );
        }
    }
    public static void addLogEvent ( OSLogEvent log ) {
        logs.add ( log );
    }
    
    public static void addTimer ( Timer task ) {
        timerList.add ( task );
    }
    
    public static void remTimer ( Timer task ) {
        timerList.remove ( task );
    }

    public boolean isSpamFiltered(String string) {
        int hash = string.toUpperCase().hashCode();
        for ( SpamFilter sf : spamfilters ) {
            if ( sf.getHashPattern() == hash ) {
                return true;
            }
        }
        return false;
    }
    
}
   
