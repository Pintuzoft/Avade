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
package rootserv;

import core.Scheduler;
import core.CommandInfo;
import core.Handler;
import core.HashString;
import core.Proc;
import core.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import nickserv.NSDatabase;
import nickserv.NickInfo;
import nickserv.NickServ;
import operserv.OSLogEvent;
import operserv.OSDatabase;
import operserv.Oper;
import user.User;

/**
 *
 * @author DreamHealer
 */
public class RootServ extends Service {
    private static boolean                  is = false;
    private static HashString               panic;
    private RSExecutor                      executor;       /* Object that parse and execute commands */
    private RSHelper                        helper;         /* Object that parse and respond to help queries */
    private RSSnoop                         snoop;          /* Object for monitoring and reporting */
    private static ScheduledFuture<?>       panicTimer;
    private static boolean                  updConf; 
    
    /**
     *
     */
    public RootServ ( )  {
        super ( "RootServ" );
        this.initRootServ ( );
    }
    
    private void initRootServ ( )  {
        is              = true;
        panic           = USER;
        this.snoop      = new RSSnoop ( this );
        this.executor   = new RSExecutor ( this, this.snoop );
        this.helper     = new RSHelper ( this, this.snoop );
        this.setCommands ( );
    }
    
    /**
     *
     */
    public static void updConf ( ) { 
        updConf = true;
    }
     
    /**
     *
     */
    public void setCommands ( )  {
        cmdList = new ArrayList<> ( );
        cmdList.add ( new CommandInfo ( "PANIC",      CMDAccess ( PANIC ),      "Manage the services panic state" )  );
        cmdList.add ( new CommandInfo ( "REHASH",     CMDAccess ( REHASH ),     "Reload the config file" )  );
        cmdList.add ( new CommandInfo ( "SHOWCONFIG", CMDAccess ( SHOWCONFIG ), "Print current services config" )  );
        cmdList.add ( new CommandInfo ( "SRAW",       CMDAccess ( SRAW ),       "Send raw messages from services to the network" )  );
        cmdList.add ( new CommandInfo ( "SRA",        CMDAccess ( SRA ),        "Manage the Services Root Admin list" )  );
        cmdList.add ( new CommandInfo ( "STOP",       CMDAccess ( STOP ),       "Correct way to stop services" )  );
    }  
    
    
    /* Returns the list of added commands with its access and info */

    /**
     *
     * @param access
     * @return
     */

    public static List<CommandInfo> getCMDList ( HashString access ) {
        return Handler.getRootServ().getCommandList ( access );
    }
     
    /**
     *
     * @param user
     * @param name
     * @return
     */
    public static boolean enoughAccess ( User user, HashString name ) {
        return Handler.getRootServ().checkAccess ( user, name );
    }
    
    /**
     *
     * @param user
     * @param name
     * @return
     */
    public boolean checkAccess ( User user, HashString name )  {
        int access              = user.getAccess ( );
        CommandInfo cmdInfo     = this.findCommandInfo (name );
        if ( access < cmdInfo.getAccess ( )  )  {
            Handler.getRootServ().sendMsg ( user, "   Command "+cmdInfo.getName ( ) +" are for "+cmdInfo.getAccessStr ( ) +" only .. *sigh*" );
            return false;
        }
        return true;
    }
    
    /**
     *
     * @param user
     * @param cmd
     */
    public void parse ( User user, String[] cmd )  { 
        if ( ! user.isAtleast ( SRA ) ) {
            return;
        }

        /* :DreamHealer PRIVMSG OperServ@stats.sshd.biz :help */
        
        cmd[3] = cmd[3].substring ( 1 );
        HashString command = new HashString ( cmd[3] );
        
        if ( command.is(HELP) ) {
            this.helper.parse ( user, cmd );
        
        } else {
            this.executor.parse ( user, cmd, command );
        }
         
    }
 
    /**
     *
     */
    public static void adPanic ( ) {
        /* Remind every 15 minutes while in panic, only one reminder at a time */
        Scheduler.cancel ( panicTimer );
        panicTimer = Scheduler.schedule ( ( ) -> {
            Handler.getRootServ().sendGlobOp ( "WARNING! Services PANIC state is currently set to: "+RootServ.getPanicStr ( NONE ) );
            RootServ.adPanic();
        }, 900000 );
    }
    
    /**
     *
     * @param state
     */
    public static void setPanic ( HashString state ) {
        panic = state;
        Handler.getRootServ().sendPanic();
        
        if ( state.is(OPER) ||
             state.is(IDENT) ) {
            RootServ.adPanic ( );
        
        } else if ( state.is(USER) ) {
            Scheduler.cancel ( panicTimer );
            panicTimer = null;
        }
        
    }

    
    /**
     *
     * @param state OPER, IDENT or USER, anything else gives the current state
     * @return
     */
    public static String getPanicStr ( HashString state ) {
        if ( state.is(OPER) ) {
            return "OPER [only IRCops can access services]";
        
        } else if ( state.is(IDENT) ) {
            return "IDENT [only identified users (+r) can access services]";
        
        } else if ( state.is(USER) ) {
            return "USER [everyone can access services]";
        
        } else if ( panic.is(OPER) || panic.is(IDENT) ) {
            return getPanicStr ( panic );
        
        } else {
            return getPanicStr ( USER );
        }
        
    }
    
    /**
     *
     */
    public void sendPanic ( ) {
        int state;
        
        if ( panic.is(OPER) ) {
            state = 2;
        
        } else if ( panic.is(IDENT) ) {
            state = 1;
        
        } else {
            state = 0;
        }
         
        this.sendServ ( "SVSPANIC "+state );
    }

    /**
     *
     */
    public void fixMaster ( ) {
        HashString master = Proc.getConf().get(MASTER);
        NickInfo ni;
        User user;
        boolean newNick = false;
        
        if ( ! Handler.isDataLoaded ( ) ) {
            /* The master nick would look unregistered and be created again */
            return;
        }
        
        if ( master == null ) {
            Proc.log ( "Couldnt find Master nickname in configuration file." );
            System.exit ( 1 );
        }
        ni   = NickServ.findNick ( master );
        user = Handler.findUser ( master );
        
        if ( ni == null && user == null ) {
            /* Master nick not registered and not online, nothing to do yet */
            return;
        }
        if ( ni == null ) {
            ni = new NickInfo ( master.getString() );
            NSDatabase.createNick ( ni );
            NickServ.addNick ( ni );
            user.getSID().add ( ni );
            //    Handler.getNickServ().authorizeNick ( ni );
            NickServ.fixIdentState ( user ); 
            newNick = true;
        }
         
        if ( ! RSDatabase.isMaster ( master ) ) {
            OSLogEvent log;
            ArrayList<NickInfo> nList = RSDatabase.setMaster ( master );
            for ( NickInfo old : nList ) {
                old.setOper ( new Oper ( old.getNameStr(), 4, "Services config" ) );
                log = new OSLogEvent ( old.getName(), DELMASTER, "new!master@services", "Services config" );
                OSDatabase.logEvent ( log );
                log = new OSLogEvent ( old.getName(), ADDSRA, "new!master@services", "Services config" );
                OSDatabase.logEvent ( log );
            }
            log = new OSLogEvent ( ni.getName(), ADDMASTER, "new!master@services", "Services config" );
            OSDatabase.logEvent ( log );
            /* Also when the master is not online right now, or the role would
               only start to work after the next restart */
            ni.setOper ( new Oper ( ni.getNameStr(), 5, "Services config" ) );
            if ( user != null ) {
                Handler.getRootServ().sendMsg ( user, "Nick: "+master+" is now set as Master of AServices." );
                if ( newNick ) {
                    /* the password was shown when the nick was made, only the hash is kept */
                    this.sendMsg ( user, "Before anything!.. Please set a valid email on the Master nick and change password." );
                    this.sendMsg ( user, "NOTE: losing access of the master nick can cause inconvenience as only the master can manage the SRA list, and no SRA can add a new master." );
                }
            }
        } 
    }
    
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
 
}   