/* 
 * Copyright (C) 2018 Fredrik Karlsson aka DreamHealer - avade.net
 *
 * This program hasAccess free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program hasAccess distributed in the hope that it will be useful,
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
import core.HashString;
import core.Proc;
import core.StringMatch;
import core.Service;
import java.math.BigInteger;
import java.text.DateFormat;
import user.User;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
    private static ArrayList<ServicesBan> delLogServicesBans = new ArrayList<>(); /* Add new services bans logs */

    private static ArrayList<NetServer> servers = new ArrayList<> ( );   /* Server list */
    private static ArrayList<NetServer> remServers = new ArrayList<> ( );   /* Remove Server from list */
    private static ArrayList<NetServer> addServers = new ArrayList<> ( );   /* Add Server into list */
    private static ArrayList<NetServer> updServers = new ArrayList<> ( );   /* Update Server in list */
    private static ArrayList<SpamFilter> addSpamFilters = new ArrayList<>(); /* Add new spamfilters */
    private static ArrayList<SpamFilter> remSpamFilters = new ArrayList<>(); /* Remove spamfilters */
      
    private static ArrayList<Oper> staff = new ArrayList<> ( );   /* Staff list */
    private static ArrayList<Oper> addStaff = new ArrayList<> ( );   /* Staff list */
    private static ArrayList<Oper> remStaff = new ArrayList<> ( );   /* Staff list */

    private static ArrayList<OSLogEvent> logs = new ArrayList<> ( );   /* LogEvent list */

    
    private ArrayList<User> chList = new ArrayList<>();     /* users who are schedualed for ban checks */
    
    private OSExecutor executor;   /* Object that parse and execute commands */
    private OSHelper helper;     /* Object that parse and respond to help queries */
    private OSSnoop snoop;      /* Object for monitoring and reporting */
    private SimpleDateFormat sdf;
    
    private Oper operNick = new Oper ( "OperServ", 4, "OperServ" );
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 

    private static ArrayList<Timer> timerList = new ArrayList<>();
    
    /**
     *
     */
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
        servers         = OSDatabase.getServerList ( );
        setCommands ( );
    }

    /**
     *
     */
    public void setCommands ( )  {
        cmdList = new ArrayList<> ( );
        cmdList.add ( new CommandInfo ( "HELP",      1,                         "Show help information" )                       );
        cmdList.add ( new CommandInfo ( "UINFO",     CMDAccess ( UINFO ),       "Show user information" )                       );
        cmdList.add ( new CommandInfo ( "CINFO",     CMDAccess ( CINFO ),       "Show channel information" )                    );
        cmdList.add ( new CommandInfo ( "NINFO",     CMDAccess ( CINFO ),       "Show nick information" )                       );
        cmdList.add ( new CommandInfo ( "ULIST",     CMDAccess ( ULIST ),       "Show user list" )                              );
        cmdList.add ( new CommandInfo ( "CLIST",     CMDAccess ( CLIST ),       "Show user list" )                              );
        cmdList.add ( new CommandInfo ( "SLIST",     CMDAccess ( SLIST ),       "Show server list" )                            );
        cmdList.add ( new CommandInfo ( "UPTIME",    CMDAccess ( UPTIME ),      "Show uptime" )                                 );
        cmdList.add ( new CommandInfo ( "AKILL",     CMDAccess ( AKILL ),       "Manage the AKill list" )                       );
        cmdList.add ( new CommandInfo ( "STAFF",     CMDAccess ( STAFF ),       "Manage the Staff list" )                       );
        cmdList.add ( new CommandInfo ( "SEARCHLOG", CMDAccess ( SEARCHLOG ),   "Search the logs for nick or channel" )         );
        cmdList.add ( new CommandInfo ( "SNOOPLOG",  CMDAccess ( SNOOPLOG ),    "Search the snoop logs for nick or channel" )   );
        cmdList.add ( new CommandInfo ( "AUDIT",     CMDAccess ( AUDIT ),       "Search access logs" )                          );
        cmdList.add ( new CommandInfo ( "COMMENT",   CMDAccess ( COMMENT ),     "Add comment about nick or chan" )              );
        cmdList.add ( new CommandInfo ( "GLOBAL",    CMDAccess ( GLOBAL ),      "Send message to all connected users" )         );
        cmdList.add ( new CommandInfo ( "IGNORE",    CMDAccess ( IGNORE ),      "Manage the services ignore list" )             );
        cmdList.add ( new CommandInfo ( "BANLOG",    CMDAccess ( BANLOG ),      "Search services ban log" )                     );
        cmdList.add ( new CommandInfo ( "SQLINE",    CMDAccess ( SQLINE ),      "Manage the services Q-line list" )             );
        cmdList.add ( new CommandInfo ( "SGLINE",    CMDAccess ( SGLINE ),      "Manage the services G-line list" )             );
        cmdList.add ( new CommandInfo ( "SPAMFILTER",CMDAccess ( SPAMFILTER ),  "Manage the services SpamFilter list" )         );
        cmdList.add ( new CommandInfo ( "JUPE",      CMDAccess ( JUPE ),        "Jupiter a server" )                            );
        cmdList.add ( new CommandInfo ( "SERVER",    CMDAccess ( SERVER ),      "Handle server list" )                          );
        cmdList.add ( new CommandInfo ( "FORCENICK", CMDAccess ( FORCENICK ),   "Forcefully change a users nickname" )          );
        cmdList.add ( new CommandInfo ( "BAHAMUT",   CMDAccess ( BAHAMUT ),     "Print bahamut compatibility version" )         );
        cmdList.add ( new CommandInfo ( "MAKILL",    CMDAccess ( MAKILL ),      "Mass Akill command" )                          );
    }
   
    /**
     *
     * @param command
     * @return
     */
    public static ArrayList<CommandInfo> getCMDList ( HashString command ) {
        return Handler.getOperServ().getCommandList ( command );
    }
    
    /**
     *
     * @param user
     * @param hashName
     * @return
     */
    public static boolean enoughAccess ( User user, HashString hashName ) {
        return Handler.getOperServ().checkAccess ( user, hashName );
    }
    
    /**
     *
     * @param user
     * @param hashName
     * @return
     */
    public boolean checkAccess ( User user, HashString hashName ) {
        int access = user.getAccess ( );
        if ( hashName.is(AUTOKILL) ) {
            hashName = AKILL;
        }
        CommandInfo cmdInfo = this.findCommandInfo ( hashName );
        if ( cmdInfo != null && access >= cmdInfo.getAccess ( ) )  {
            return true;
        }
        return false;
    }
     
    /**
     *
     */
    public void hourMaintenance ( ) {
        /* nothingness */
    }

    /**
     *
     * @return
     */
    public int secMaintenance ( ) {
        int todoAmount = 0;
        this.checkUserList ( );
        todoAmount += this.checkAddServers ( );
        todoAmount += this.checkRemServers ( );
        todoAmount += this.checkUpdServers ( );
        todoAmount += this.checkAddServicesBans ( );
        todoAmount += this.checkRemServicesBans ( );
        todoAmount += this.checkAddSpamFilters ( );
        todoAmount += this.checkRemSpamFilters ( );
        todoAmount += this.checkAddStaff ( );
        todoAmount += this.checkRemStaff ( );
        todoAmount += this.checkLogEvents ( );
        todoAmount += this.checkaddLogServicesBans ( );
        todoAmount += this.checkdelLogServicesBans ( );
         
        return todoAmount;
    }
    
    /**
     *
     * @return
     */
    public int minMaintenance ( ) {
        int todoAmount = 0;
        this.expireBans ( );
        return todoAmount;
    }
    
    /**
     *
     * @return
     */
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
    
    /**
     *
     * @return
     */
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
    
    /**
     *
     * @return
     */
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
    
    /**
     *
     * @return
     */
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
    
    /**
     *
     * @return
     */
    public int checkRemServers ( ) {
        if ( remServers.isEmpty() || ! OSDatabase.checkConn() ) {
            return remServers.size();
        }
        ArrayList<NetServer> sList = new ArrayList<>();
        for ( NetServer server : remServers ) {
            if ( OSDatabase.delServer ( server ) ) {
                sList.add ( server );
            }
        }
        for ( NetServer server : sList ) {
            remServers.remove ( server );
        }
        return remServers.size();
    }
    
    /**
     *
     * @return
     */
    public int checkAddServers ( ) {
        if ( addServers.isEmpty() || ! OSDatabase.checkConn() ) {
            return addServers.size();
        }
        ArrayList<NetServer> sList = new ArrayList<>();
        for ( NetServer server : addServers ) {
            if ( OSDatabase.addServer ( server ) ) {
                sList.add ( server );
            }
        }
        for ( NetServer server : sList ) {
            addServers.remove ( server );
        }
        return addServers.size();
    }

    /**
     *
     * @return
     */
    public int checkUpdServers ( ) {
        if ( updServers.isEmpty() || ! OSDatabase.checkConn() ) {
            return updServers.size();
        }
        ArrayList<NetServer> sList = new ArrayList<>();
        for ( NetServer server : updServers ) {
            if ( OSDatabase.updServer ( server ) ) {
                sList.add ( server );
            }
        }
        for ( NetServer server : sList ) {
            updServers.remove ( server );
        }
        return updServers.size();
    }
    
    /**
     *
     * @return
     */
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
       
    /**
     *
     * @return
     */
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
          
    /**
     *
     * @return
     */
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
    
       /**
     *
     * @return
     */
    public int checkaddLogServicesBans ( ) {
        if ( addLogServicesBans.isEmpty() || ! OSDatabase.checkConn() ) {
            return addLogServicesBans.size();
        }
        ArrayList<ServicesBan> bList = new ArrayList<>();
        for ( ServicesBan ban : addLogServicesBans ) {
            if ( OSDatabase.logServicesBan ( ADD, ban ) ) {
                bList.add ( ban );
            }
        }
        addLogServicesBans.removeAll(bList);
        return delLogServicesBans.size();
    }

       /**
     *
     * @return
     */
    public int checkdelLogServicesBans ( ) {
        if ( delLogServicesBans.isEmpty() || ! OSDatabase.checkConn() ) {
            return delLogServicesBans.size();
        }
        ArrayList<ServicesBan> bList = new ArrayList<>();
        for ( ServicesBan ban : delLogServicesBans ) {
            if ( OSDatabase.logServicesBan ( DEL, ban ) ) {
                bList.add ( ban );
            }
        }
        delLogServicesBans.removeAll(bList);
        return delLogServicesBans.size();
    }

    private void checkUserList ( ) {
        int res;
        ServicesBan ban;
        String newName;
        ArrayList<User> checked = new ArrayList<>();
        if ( ! OSDatabase.checkConn() ) {
            return;
        }
        
        HashString[] commands = { AKILL, SGLINE, SQLINE };
        Random rand = new Random ( );
        
        /* this looks unnecessary? */
        for ( User u : this.chList ) {
            ban = null;
            for ( HashString command : commands ) {
                if ( ban == null ) {
                            
                    if ( command.is(AKILL) ) {
                        ban = findBan ( command, u.getFullMask() );
                        if ( ban != null ) {
                            this.ban ( u, ban );
                            Handler.deleteUser ( u );
                        }   
                    
                    } else if ( command.is(SQLINE) ) {
                        ban = findBan ( command, u.getString ( NAME ) );
                        if ( ban != null ) {
                            newName = "SQLined"+rand.nextInt(99999);
                            this.ban ( u, ban );
                            this.sendServ ( "SVSNICK "+u.getString ( NAME )+" "+newName+" :0" );
                            u.setName ( newName );
                        }
                    
                    } else if ( command.is(SGLINE) ) {
                        ban = findBan ( command, u.getString ( REALNAME ) );
                        if ( ban != null ) {
                            this.ban ( u, ban );
                            Handler.deleteUser ( u );
                        }   
                    
                    } else {
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
        HashString[] commands = { AKILL, SGLINE, SQLINE };
        ArrayList<ServicesBan> delList = new ArrayList<>( );
        ArrayList<ServicesBan> list = null;
        for ( HashString command : commands ) {
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
            delLogServicesBans.add( del );
            this.sendServ( "GLOBOPS :"+del.getBanTypeStr()+" for "+del.getMask()+" has expired. [Instated by: "+del.getInstater()+" at: "+del.getTime()+"]");
     //       this.sendGlobOp ( del.getBanTypeStr()+" for "+del.getMask()+" has expired. [Instated by: "+del.getInstater()+" at: "+del.getTime()+"]" );
            getBanList(del.getType()).remove ( del );
        }
        
    }
    
    private ArrayList<ServicesBan> getBanList ( HashString name ) {
        if      ( name.is(AKILL) )          { return akills;        }
        else if ( name.is(SQLINE) )         { return sqlines;       }
        else if ( name.is(SGLINE) )         { return sglines;       }
        else {
            return null;
        }
    }
    
    /**
     *
     * @param user
     * @param cmd
     */
    public void parse ( User user, String[] cmd )  {
        user.getUserFlood().incCounter ( this );
        if ( cmd == null || cmd[3].isEmpty ( )  )  { 
            return; 
        }
        
        cmd[3] = cmd[3].substring ( 1 );
        HashString command = new HashString ( cmd[3] );
        
        if ( user == null ) {
            System.out.println("DEBUG: parse() -> no user");
            return;
        } else if ( ! user.isOper ( ) )  {
            this.sendMsg ( user, "IRC Operator Services are for IRC Operators only .. *sigh*" );
            return;
        } else if ( ! OperServ.enoughAccess ( user, command ) ) {
            this.sendMsg ( user, "Access denied!." );
            return;
        }
        
        if ( command.is(HELP) ) {
            this.helper.parse ( user, cmd );
        } else {
            this.executor.parse ( user, cmd );
        }
        
    }
     // public void sendSnoop ( String msg )  { this.snoop.msg ( msg ); }
 
    /**
     *
     * @return
     */
    public static ArrayList<Oper> findRootAdmins ( )  {
        if ( ! is ) {
            return null;
        }
        return getRootAdmins ( );
    }

    /**
     *
     * @return
     */
    public static ArrayList<Oper> findCSOps ( )  {
        if ( ! is ) {
            return null;
        }
        return getCSops ( );
    }
    
    /**
     *
     * @return
     */
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

    /**
     *
     * @param user
     * @param ban
     */
   
    public void ban ( User user, ServicesBan ban )  {
        if ( ban.is(AKILL) ) {
            this.unBan ( ban );
            this.sendServ ("AKILL "+
                ban.getHost ( ) +" "+
                ban.getUser ( ) +" "+ 
                ban.getExpireSec ( ) +" "+
                ban.getInstater ( ) +" "+
                ( System.currentTimeMillis ( ) / 1000 ) +
                " :"+ban.getReason ( ) +" [Ticket: "+ban.getID ( ) +"]" 
            );            
        
        } else if ( ban.is(SQLINE) ) {
            this.unBan ( ban );
            this.sendServ ( "SQLINE "+ban.getMask()+" :"+ban.getReason() );            
        
        } else if ( ban.is(SGLINE) ) {
            this.unBan ( ban );
            this.sendServ ( "SGLINE "+ban.getMask().length()+" :"+ban.getMask()+":"+ban.getReason() );
            this.sendServ ( "KILL "+user.getString ( NAME )+" :gcos violation [Ticket: SG"+ban.getID()+"]" );            
        }
         
    }
    private void unBan ( ServicesBan ban )  {
        if ( ban.is(AKILL) ) {
            this.sendServ ( "RAKILL " + ban.getHost ( ) + " " + ban.getUser ( ) );            
        
        } else if ( ban.is(SQLINE) ) {
            this.sendServ ( "UNSQLINE :"+ban.getMask() );            
        
        } else if ( ban.is(SGLINE) ) {
            this.sendServ ( "UNSGLINE :"+ban.getMask() );            
        }
         
    }
    
    /**
     *
     * @param command
     * @return
     */
    public static ArrayList<ServicesBan> getListByCommand ( HashString command ) {
        if ( command.is(AKILL) )                { return akills;    }
        else if ( command.is(IGNORE) )          { return ignores;   }
        else if ( command.is(SQLINE) )          { return sqlines;   }
        else if ( command.is(SGLINE) )          { return sglines;   }
        else {
            return new ArrayList<>();
        }
    }
    
    /* remove this */
    private void delServicesBan ( ServicesBan ban )  {
        remServicesBans.add( ban );
        delLogServicesBans.add( ban );
        if ( ban.is(SQLINE) ||
             ban.is(SGLINE) ||
             ban.is(AKILL) ) {
            this.unBan ( ban );
        }
    }
  
    /**
     *
     * @param ban
     */
    public void sendServicesBan ( ServicesBan ban )  {
        User u = null;   
        if ( ban != null )  {
            // getListByCommand(ban.getType()).add ( ban );
          //  addServicesBans.add ( ban );
            
//            OSDatabase.logServicesBan ( ADD, ban );
            //:services.avade.net AKILL 172.16.4.* fredde 1378430849 DreamHealer 1374430849 :Test akill
            
            if ( ban.is(AKILL) ) {
                this.sendServ ( 
                    "AKILL "+
                    ban.getHost ( ) +" "+
                    ban.getUser ( ) +" "+
                    ban.getExpireSec ( ) +" "+
                    ban.getInstater ( ) +" "+
                    ( System.currentTimeMillis ( ) / 1000 ) +
                    " :"+ban.getReason ( )+" [Ticket: "+ban.getID ( ) +"] "
                );
            
            } else if ( ban.is(SQLINE) ) {
                this.unBan ( ban );
                this.sendServ ( "SQLINE "+ban.getMask()+" :"+ban.getReason() );
                int index = 1;
                String nick;
                User buf;
                String prefix = "SQLined";
                
                for ( HashMap.Entry<BigInteger,User> entry : Handler.getUserList().entrySet() ) {
                    u = entry.getValue();
                    nick = u.getString ( NAME );
                    if ( StringMatch.nickWild ( nick, ban.getMask().getString() ) ) {
                        while ( ( buf = Handler.findUser ( prefix+index ) ) != null ) {
                            index++;
                        }
                        this.sendServ ( "SVSNICK "+u.getString(NAME)+" "+prefix+index+" :0" );
                        index++;
                    }
                }                
            
            } else if ( ban.is(SGLINE) ) {
                this.unBan ( ban );
                this.sendServ ( "SGLINE "+ban.getMask().length()+" :"+ban.getMask()+":"+ban.getReason() );
                for ( HashMap.Entry<BigInteger,User> entry : Handler.getUserList().entrySet() ) {
                    u = entry.getValue();
                    if ( StringMatch.wild ( u.getString ( REALNAME ), ban.getMask().getString() ) ) {
                        this.sendServ ( "KILL "+u.getString(NAME)+" :gcos violation [Ticket: SG"+ban.getID()+"]" );
                    }
                }                
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

    /**
     *
     * @param state
     */

      
    public static void is ( boolean state ) { 
        is = state; 
    }

    /**
     *
     * @param state
     */
    public static void setState ( boolean state ) { 
        is = state;
    }

    /**
     *
     * @return
     */
    public static boolean isUp ( ) { 
        return is; 
    }

    /**
     *
     * @param user
     */
    public void checkUser ( User user ) {
        this.chList.add ( user );
    }
    
    /**
     *
     * @param command
     * @param banId
     * @return
     */
    public static ServicesBan findBanByID ( HashString command, String banId ) {
        return findBanByID ( command, new HashString ( banId ) );
    }

    /**
     *
     * @param command
     * @param banId
     * @return
     */
    public static ServicesBan findBanByID ( HashString command, HashString banId ) {
        if ( banId == null ) {
            return null;
        }
        for ( ServicesBan ban : getListByCommand ( command ) ) {
            if ( ban.is(banId) ) {
                return ban;
            }
        }
        return null;
    }
    
    /**
     *
     * @param command
     * @param pattern
     * @return
     */
    public static ArrayList<ServicesBan> findBansByPattern ( HashString command, String pattern )  {
        ArrayList<ServicesBan> bList = new ArrayList<>();
        
        if ( command.is(AKILL) ) {
            for ( ServicesBan ban : akills )  {
                if ( StringMatch.maskWild ( ban.getMask ( ), pattern )  )  {
                    bList.add ( ban );
                }
            }  
        
        } else if ( command.is(IGNORE) ) {
            for ( ServicesBan ban : ignores ) {
                if ( StringMatch.maskWild ( ban.getMask(), pattern) ) {
                    bList.add ( ban );
                }   
            }            
        
        } else if ( command.is(SQLINE) ) {
            for ( ServicesBan ban : sqlines ) {
                if ( StringMatch.nickWild ( ban.getMask().getString(), pattern) ) {
                    bList.add ( ban );
                }   
            }            
        
        } else if ( command.is(SQLINE) ) {
            for ( ServicesBan ban : sglines ) {
                if ( StringMatch.wild ( ban.getMask().getString(), pattern) ) {
                    bList.add ( ban );
                }   
            }            
        }
        return bList;
    }
    
    /**
     *
     * @param command
     * @param usermask
     * @return
     */
    public static ServicesBan findBan ( HashString command, String usermask )  {
        if ( command.is(AKILL) ) {
            for ( ServicesBan a : akills )  {
                if ( StringMatch.maskWild ( a.getMask( ), usermask )  )  {
                    return a;
                }
            }
        } else if ( command.is(IGNORE) ) {
            for ( ServicesBan b : ignores ) {
                if ( StringMatch.maskWild ( b.getMask(), usermask) ) {
                    return b;
                }   
            }
        } else if ( command.is(SQLINE) ) {
            for ( ServicesBan b : sqlines ) {
                if ( StringMatch.nickWild ( b.getMask().getString(), usermask) ) {
                    return b;
                }   
            }
        } else if ( command.is(SGLINE) ) {
            for ( ServicesBan b : sglines ) {
                if ( StringMatch.wild ( b.getMask().getString(), usermask) ) {
                    return b;
                }   
            }
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

    /**
     *
     * @return
     */

    
    public int getAkillCount() {
        return akills.size();
    }

    /**
     *
     * @return
     */
    public int getIgnoreCount() {
        return ignores.size();
    }

    /**
     *
     * @param user
     * @return
     */
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
     * @param name
     * @return
     */
    public static NetServer getServer ( String name ) {
        return getServer ( new HashString ( name ) );
    }

    /**
     *
     * @param name
     * @return
     */
    public static NetServer getServer ( HashString name ) {
        for ( NetServer server : servers ) {
            if ( server.is(name) ) {
                return server;
            }
        }
        return null;
    }
    
    /**
     *
     * @param type
     * @param missing
     * @return
     */
    public static ArrayList<NetServer> getServers ( HashString type, boolean missing ) {
        ArrayList<NetServer> sList = new ArrayList<>();
        
        if ( type.is(HUB) ) {
            for ( NetServer server : servers ) {
                for ( NetServer server2 : servers ) {
                    if ( server.is(server2.getPrimary()) || server.is(server2.getSecondary() ) ) {
                        sList.add ( server );
                    }
                }
            }            
        
        } else if ( type.is(LEAF) ) {
            sList.addAll ( servers );
            ArrayList<NetServer> remList = new ArrayList<>();
            for ( NetServer server : servers ) {
                for ( NetServer server2 : servers ) {
                    if ( server.is(server2.getPrimary()) || server.is(server2.getSecondary() ) ) {
                        sList.remove ( server );
                    }
                }
            }            
        }
         
        if ( missing ) {
            ArrayList<Server> online = Handler.getServerList ( );
            ArrayList<NetServer> rem = new ArrayList<>();

            for ( Server server : online ) {
                for ( NetServer server2 : sList ) {
                    if ( server2.is(server.getName()) ) {
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
    
    /**
     *
     * @param name
     * @return
     */
    public static boolean addDelServer ( String name ) {
        HashString hash = new HashString ( name );
        NetServer rem = null;
        for ( NetServer server : servers ) {
            if ( server.is(hash) ) {
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

    /**
     *
     * @param name
     */
    public static void addServer ( HashString name ) {
        for ( NetServer server : servers ) {
            if ( server.is(name) ) {
                return;
            }
        }
        NetServer server = new NetServer ( name.getString(), null, null );
        addServers.add ( server );
    }

    /**
     *
     * @param server
     */
    public static void addUpdServer ( NetServer server ) {
        updServers.add ( server );
    }
    
    /**
     *
     * @param hash
     * @return
     */
    public static ArrayList<Oper> getStaffPlus ( HashString hash ) {
        ArrayList<Oper> oList = new ArrayList<> ( );
        int access = hashToAccess ( hash );
        for ( Oper o : staff ) {
            if ( o.getAccess() >= access ) {
                oList.add ( o );
            }
        }
        return oList;
    }
    
    /**
     *
     * @param hash
     * @return
     */
    public static int hashToAccess ( HashString hash ) {
        if ( hash.is(IRCOP) ) {
            return 1;
        } else if ( hash.is(SA) ) {
            return 2;
        } else if ( hash.is(CSOP) ) {
            return 3;
        } else if ( hash.is(SRA) ) {
            return 4;
        } else if ( hash.is(MASTER) ) {
            return 5;
        } else {
            return 0;
        }
    }
    
    /**
     *
     * @param access
     * @return
     */
    public static ArrayList<Oper> getStaffByAccess ( int access )  {
        ArrayList<Oper> oList = new ArrayList<> ( );
        for ( Oper o : staff ) {
            if ( o.getAccess() == access ) {
                oList.add ( o );
            }
        }
        return oList;
    }

    /**
     *
     * @return
     */
    public static ArrayList<Oper> getMaster ( ) {
        return getStaffByAccess ( 5 );
    }

    /**
     *
     * @return
     */
    public static ArrayList<Oper> getRootAdmins ( ) { 
        return getStaffByAccess ( 4 ); 
    }

    /**
     *
     * @return
     */
    public static ArrayList<Oper> getCSops ( ) {
        return getStaffByAccess ( 3 );
    } 

    /**
     *
     * @return
     */
    public static ArrayList<Oper> getServicesAdmins ( ) {
        return getStaffByAccess ( 2 );
    }

    /**
     *
     * @return
     */
    public static ArrayList<Oper> getIRCops() {
        return getStaffByAccess ( 1 );
    }
    
    /**
     *
     * @param name
     * @return
     */
    public static Oper getOper ( HashString name ) {
        for ( Oper oper : staff ) {
            if ( oper.is(name) ) {
                return oper;
            }
        }
        return new Oper ( );
    }
    
    /**
     *
     * @param ban
     */
    public static void addServicesBan ( ServicesBan ban ) {
        addServicesBans.add ( ban );
        addLogServicesBans.add ( ban );
        getListByCommand(ban.getType()).add ( ban );
    }

    /**
     *
     * @param ban
     */
    public static void remServicesBan ( ServicesBan ban ) {
        ServicesBan rem = null;
        remServicesBans.add ( ban );
        for ( ServicesBan ban2 : getListByCommand ( ban.getType() ) ) {
            if ( ban.is(ban2) ) {
                rem = ban2;
            }
        }
        if ( rem != null ) {
            getListByCommand(ban.getType()).remove ( rem );
            Handler.getOperServ().delServicesBan ( rem );            
        }
    }

    /**
     *
     * @return
     */
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

    /**
     *
     */
    public void sendSpamFilter ( ) {
        for ( SpamFilter sf : spamfilters ) {
            this.sendServ ( "SF "+sf.getPattern()+" "+sf.getBitFlags()+" :"+sf.getReason() );
        }
    }

    /**
     *
     * @param pattern
     * @return
     */
    public static SpamFilter findSpamFilter ( String pattern ) {
        HashString hash = new HashString(pattern);
        for ( SpamFilter sf : spamfilters ) {
            if ( sf.getPattern().is(hash) ) {
                return sf;
            }
        }
        return null;
    }

    /**
     *
     * @param data
     */
    public void addSFAkill ( String[] data ) {
        //:testnet.avade.net OS SFAKILL fredde 1539289892 hello to you too!
        //                 0  1       2      3          4 5+
        String[] buf = data[3].split("@");
        String host = buf[1];
        String reason = Handler.cutArrayIntoString ( data, 5 );
        ServicesBan ban = this.findBan ( AKILL, "*@"+host );
        
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
        //String expire = Handler.expireToTime ( "30d" );
        ban = new ServicesBan ( 
                AKILL, 
                new HashString ( ""+System.nanoTime() ), 
                false,
                new HashString ( "*!*@"+host ), 
                reason, 
                "OperServ", 
                null,
                "30d");
        this.addServicesBan ( ban );
        this.sendServicesBan ( ban );
    }

    /**
     *
     * @param usermask
     * @return
     */
    public static boolean isWhiteListed ( String usermask ) {
        return isWhiteListed ( new HashString ( usermask ) );
    }
    
    /**
     *
     * @param usermask
     * @return
     */
    public static boolean isWhiteListed ( HashString usermask ) {
        for ( Map.Entry<BigInteger,HashString> white : Proc.getConf().getWhiteList().entrySet() ) {
            if ( StringMatch.maskWild ( usermask.getString(), "*"+white.getValue() ) ) {
                return true;
            }
        }
        return false;
    }
    
    static void addOper ( Oper oper ) {
        Oper rem = null;
        HashString hash = oper.getName();
        for ( Oper o : staff ) {
            System.out.println("addOper: "+hash+":"+o.getName().hashCode());
            if ( o.getName().is(hash) ) {
                rem = o;
            }
        }
        if ( rem != null ) {
            staff.remove ( rem );
        }
        staff.add ( oper );
        addStaff.add ( oper );
    }
    
    /**
     *
     * @param ni
     */
    public static void delOper ( NickInfo ni ) {
        Oper remove = null;
        for ( Oper oper : staff ) {
            if ( oper.is(ni.getName()) ) {
                remove = oper;
            }
        }
        if ( remove != null ) {
            staff.remove ( remove );
            remStaff.add ( remove );
        }
    }

    /**
     *
     * @param log
     */
    public static void addLogEvent ( OSLogEvent log ) {
        logs.add ( log );
    }
    
    /**
     *
     * @param task
     */
    public static void addTimer ( Timer task ) {
        timerList.add ( task );
    }
    
    /**
     *
     * @param task
     */
    public static void remTimer ( Timer task ) {
        timerList.remove ( task );
    }

    /**
     *
     * @param string
     * @return
     */
    public boolean isSpamFiltered(String string) {
        HashString hash = new HashString ( string );
        for ( SpamFilter sf : spamfilters ) {
            if ( sf.getPattern().is(hash) ) {
                return true;
            }
        }
        return false;
    }
    
}
   
