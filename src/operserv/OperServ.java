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
import core.StringMatch;
import core.Proc;
import core.Service;
import nickserv.NickInfo;
import user.User;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;
import server.Server;

/**
 *
 * @author DreamHealer
 */
public class OperServ extends Service {
    private static boolean              is = false; 
    
    private static ArrayList<ServicesBan> akills = new ArrayList<> ( );   /* AutoKills */
    private static ArrayList<ServicesBan> ignores = new ArrayList<> ( );   /* Services Ignores */
    private static ArrayList<ServicesBan> sqlines = new ArrayList<> ( );   /* SQLines */
    private static ArrayList<ServicesBan> sglines = new ArrayList<> ( );   /* SGLines */
    private static ArrayList<ServicesBan> spams = new ArrayList<> ( );   /* SPAM list */
    
    private static ArrayList<NetServer> servers = new ArrayList<> ( );   /* Server list */
    private static ArrayList<NetServer> remServers = new ArrayList<> ( );   /* Remove Server list */
    private static ArrayList<NetServer> addServers = new ArrayList<> ( );   /* Remove Server list */

    private static ArrayList<Oper> staff = new ArrayList<> ( );   /* Staff list */
    
    private ArrayList<User> chList = new ArrayList<>();     /* users who are schedualed for ban checks */
    
    private OSExecutor                  executor;   /* Object that parse and execute commands */
    private OSHelper                    helper;     /* Object that parse and respond to help queries */
    private OSSnoop                     snoop;      /* Object for monitoring and reporting */
    private SimpleDateFormat            sdf;
    
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
        spams           = OSDatabase.getServicesBans ( SPAM );
        staff           = OSDatabase.getAllStaff ( );
        servers           = OSDatabase.getServerList ( );
        setCommands ( );
    }

    public void updConf ( ) {
        setCommands ( );
    }
 
    public void setCommands ( )  {
        cmdList = new ArrayList<> ( );
        cmdList.add ( new CommandInfo ( "HELP",     1,                      "Show help information" )                );
        cmdList.add ( new CommandInfo ( "UINFO",    CMDAccess ( UINFO ),    "Show user information" )                 );
        cmdList.add ( new CommandInfo ( "CINFO",    CMDAccess ( CINFO ),    "Show channel information" )              );
        cmdList.add ( new CommandInfo ( "ULIST",    CMDAccess ( ULIST ),    "Show user list" )                        );
        cmdList.add ( new CommandInfo ( "SLIST",    CMDAccess ( ULIST ),    "Show server list" )                      );
        cmdList.add ( new CommandInfo ( "UPTIME",   CMDAccess ( UPTIME ),   "Show uptime" )                           );
        cmdList.add ( new CommandInfo ( "AKILL",    CMDAccess ( AKILL ),    "Manage the AKill list" )                 );
        cmdList.add ( new CommandInfo ( "STAFF",    CMDAccess ( STAFF ),    "Manage the Staff list" )                 );
        cmdList.add ( new CommandInfo ( "SEARCHLOG",CMDAccess ( SEARCHLOG ),"Search the logs for nick or channel" )   );
        cmdList.add ( new CommandInfo ( "AUDIT",    CMDAccess ( AUDIT ),    "Search access logs" )                    );
        cmdList.add ( new CommandInfo ( "COMMENT",  CMDAccess ( COMMENT ),  "Add comment about nick or chan" )        );
        cmdList.add ( new CommandInfo ( "GLOBAL",   CMDAccess ( GLOBAL ),   "Send message to all connected users" )   );
        cmdList.add ( new CommandInfo ( "IGNORE",   CMDAccess ( IGNORE ),   "Manage the services ignore list" )       );
        cmdList.add ( new CommandInfo ( "BANLOG",   CMDAccess ( BANLOG ),   "Search services ban log" )               );
        cmdList.add ( new CommandInfo ( "SQLINE",   CMDAccess ( SQLINE ),   "Manage the services Q-line list" )       );
        cmdList.add ( new CommandInfo ( "SGLINE",   CMDAccess ( SGLINE ),   "Manage the services G-line list" )       );
        cmdList.add ( new CommandInfo ( "SPAM",     CMDAccess ( SPAM ),     "Manage the network SPAM list" )          );
        cmdList.add ( new CommandInfo ( "JUPE",     CMDAccess ( JUPE ),     "Jupiter a server" )                      );
        cmdList.add ( new CommandInfo ( "SERVER",   CMDAccess ( SERVER ),   "Jupiter a server" )                      );
    }
   
    public static ArrayList<CommandInfo> getCMDList ( int command ) {
        return Handler.getOperServ().getCommandList ( command );
    }
    
    public static boolean enoughAccess ( User user, int hashName ) {
        return Handler.getOperServ().checkAccess ( user, hashName );
    }
    
    public boolean checkAccess ( User user, int hashName ) {
        int access              = user.getAccess ( );
        CommandInfo cmdInfo     = this.findCommandInfo ( hashName );
        if ( access >= cmdInfo.getAccess ( ) )  {
            return true;
        }
        return false;
    }
     
    public void hourMaintenance ( ) {
        this.expireBans ( );
    }
    public void secMaintenance ( ) {  
        this.checkUserList ( );
        this.checkAddServers ( );
        this.checkRemServers ( );
        
    }
    public void minMaintenance ( ) {  
             
    }
    
    public void checkRemServers ( ) {
        if ( remServers.size() == 0 || ! OSDatabase.checkConn() ) {
            return;
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
    }
    
    public void checkAddServers ( ) {
        if ( addServers.size() == 0 || ! OSDatabase.checkConn() ) {
            return;
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
                            ban = this.findBan ( command, u.getFullMask() );
                            if ( ban != null ) {
                                this.ban ( u, command, ban );
                                Handler.deleteUser ( u );
                            }                            
                            break;

                        case SQLINE :
                            ban = this.findBan ( command, u.getString ( NAME ) );
                            if ( ban != null ) {
                                newName = "SQLined"+rand.nextInt(99999);
                                this.ban ( u, command, ban );
                                this.sendServ ( "SVSNICK "+u.getString ( NAME )+" "+newName+" :0" );
                                u.setName ( newName );
                            }
                            break;

                        case SGLINE :
                            ban = this.findBan ( command, u.getString ( REALNAME ) );
                            if ( ban != null ) {
                                this.ban ( u, command, ban );
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
    
    
    private void expireBans ( )  {
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
                        if ( a.getHashCode ( )  == ak.getHashCode ( )  )  {
                            banList.add ( ak );
                        }
                    }
                }
            }

        }
        
        for ( ServicesBan a : banList )  {
            if ( OSDatabase.delServicesBan ( a ) ) {
                this.unBan ( a.getType(), a );
                this.sendGlobOp ( output ( AKILL_EXPIRE, a.getMask ( ) , ""+a.getId ( ) , a.getInstater ( )  )  );
                list.remove(a);
            } 
        }
    }
    
    public void parse ( User user, String[] cmd )  {
        
        user.getUserFlood().incCounter ( this );

        cmd[3] = cmd[3].substring ( 1 );
        if ( ! user.isOper ( ) )  {
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
    public void ban ( User user, int command, ServicesBan ban )  {
        switch ( command ) {
            case AKILL :
                this.unBan ( command, ban );
                this.sendServ ("AKILL "+
                    ban.getHost ( ) +" "+
                    ban.getUser ( ) +" "+ 
                    (60*60*24) +" "+
                    ban.getInstater ( ) +" "+
                    ( System.currentTimeMillis ( ) / 1000 ) +
                    " :"+ban.getReason ( ) +" [Ticket: "+ban.getId ( ) +"]" 
                );
                break;
                     
            case SQLINE :
                this.unBan ( command, ban );
                this.sendServ ( "SQLINE "+ban.getMask()+" :"+ban.getReason() );
                break;
                
            case SGLINE :
                this.unBan ( command, ban );
                this.sendServ ( "SGLINE "+ban.getMask().length()+" :"+ban.getMask()+":"+ban.getReason() );
                this.sendServ ( "KILL "+user.getString ( NAME )+" :gcos violation [Ticket: SG"+ban.getId()+"]" );
                break;
            
        }
    }
    private void unBan ( int command, ServicesBan ban )  {
        switch ( command ) {
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
    
   
    public ArrayList<ServicesBan> getListByCommand ( int command ) {
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
    
    public int delServicesBan ( int command, ServicesBan ban )  {
        if ( OSDatabase.delServicesBan ( ban ) ) {
            OSDatabase.logServicesBan ( DEL, ban );
            this.getListByCommand(command).remove ( ban );
            switch ( command ) {
                case SQLINE :
                case SGLINE :
                case AKILL :
                    this.unBan ( command, ban );
            }
            return 1;
        }
        return 0;
    }

    public void addServicesBan ( int command, Oper oper, String usermask, String expire, String reason )  {
         
        ServicesBan ban = OSDatabase.addServicesBan ( new ServicesBan ( command, 0, usermask, reason, oper.getString ( NAME ) , null, expire ) );
        
        if ( ban != null )  {
            this.getListByCommand(command).add ( ban );
            OSDatabase.logServicesBan ( ADD, ban );
            //:services.avade.net AKILL 172.16.4.* fredde 1378430849 DreamHealer 1374430849 :Test akill
            switch ( ban.getType() ) {
                case AKILL :
                    this.sendServ ( 
                        "AKILL "+
                        ban.getHost ( ) +" "+
                        ban.getUser ( ) +" "+
                        (60*60*24) +" "+
                        ban.getInstater ( ) +" "+
                        ( System.currentTimeMillis ( ) / 1000 ) +
                        " :"+ban.getReason ( )+"[Ticket: AK"+ban.getId ( ) +"] "
                    );
                    break;
                    
                case SQLINE :
                    this.unBan ( command, ban );
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
                            this.sendServ ( "SVSNICK "+u.getString ( NAME )+" "+prefix+index+" :0" );
                            index++;
                        }
                    }
                    break;
                 
                case SGLINE :
                    this.unBan ( command, ban );
                    this.sendServ ( "SGLINE "+ban.getMask().length()+" :"+ban.getMask()+":"+ban.getReason() );
                    for ( User u : Handler.getUserList() ) {
                        System.out.println("DEBUG: "+u.getString(NAME)+":"+u.getString(REALNAME)+":"+ban.getMask());
                        if ( StringMatch.wild ( u.getString ( REALNAME ), ban.getMask ( ) ) ) {
                            this.sendServ ( "KILL "+u.getString ( NAME )+" :gcos violation [Ticket: SG"+ban.getId()+"]" );
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

    // fredde@1.2.3.*
    // fredde@1.2.3.4
    
    public void checkUser ( User user ) {
        this.chList.add ( user );
    }
    
    
    public ServicesBan findBan ( int command, String usermask )  {
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
     
    public String output ( int code, String... args )  {
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
        if ( ( ban = this.findBan ( IGNORE, user.getFullMask() ) ) != null ) {
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
        System.out.println("DEBUG: servers: "+servers.size());
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
    
}
   
