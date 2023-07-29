/* 
 * Copyright (C) 2018 Fredrik Karlsson aka DreamHealer & avade.net
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
package rootserv;

import core.CommandInfo;
import core.Handler;
import core.HashString;
import core.Proc;
import core.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import nickserv.NSDatabase;
import nickserv.NickInfo;
import nickserv.NickServ;
import operserv.OSLogEvent;
import operserv.OSDatabase;
import operserv.OperServ;
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
    private static Timer                    panicTimer;
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
        HashString command = new HashString ( cmd[3].substring(1) );
        
        if ( ! user.isAtleast ( SRA ) ) {
            return;
        }

        /* :DreamHealer PRIVMSG OperServ@stats.sshd.biz :help */
        
        cmd[3] = cmd[3].substring ( 1 );
        
        if ( command.is(HELP) ) {
            this.helper.parse ( user, cmd );
        
        } else {
            this.executor.parse ( user, cmd );
        }
         
    }
 
    /**
     *
     */
    public static void adPanic ( ) {
        panicTimer = new Timer ( );
        panicTimer.schedule ( new TimerTask ( ) {
            @Override
                public void run() {
                    Handler.getRootServ().sendGlobOp ( "WARNING! Services PANIC state is currently set to: "+RootServ.getPanicStr ( NONE ) );
                    RootServ.adPanic();
                } 
            }, 900000
        );
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
            panicTimer.cancel();
            panicTimer = null;
        }
        
    }
    
    /**
     *
     * @return
     */
    public static HashString getPanic ( ) {
        return panic;
    }
    
    /**
     *
     * @param panic
     * @return
     */
    public static String getPanicStr ( HashString panic ) {
        if ( panic.is(OPER) ) {
            return "OPER [only IRCops can access services]";
        
        } else if ( panic.is(IDENT) ) {
            return "IDENT [only identified users (+r) can access services]";
        
        } else if ( panic.is(USER) ) {
            return "USER [everyone can access services]";
        
        } else {
            return getPanicStr ( panic );
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
        NickInfo ni = NickServ.findNick ( master );
        User user;
        boolean newNick = false;
        
        if ( master == null ) {
            System.out.println ( "Couldnt find Master nickname in configuration file." );
            System.exit ( 1 );
        }
        user = Handler.findUser ( master );
        
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
                log = new OSLogEvent ( old.getName(), new HashString ( "DELMASTER" ), "new!master@services", "Services config" );
                OSDatabase.logEvent ( log );
                log = new OSLogEvent ( old.getName(), new HashString ( "ADDSRA" ), "new!master@services", "Services config" );
                OSDatabase.logEvent ( log );
            }
            log = new OSLogEvent ( ni.getName(), new HashString ( "ADDMASTER" ), "new!master@services", "Services config" );
            OSDatabase.logEvent ( log );
            if ( user != null ) {
                ni.setOper ( OperServ.getOper ( master ) );
                Handler.getRootServ().sendMsg ( user, "Nick: "+master+" is now set as Master of AServices." );
                if ( newNick ) {
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