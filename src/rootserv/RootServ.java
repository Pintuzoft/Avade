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
package rootserv;

import core.CommandInfo;
import core.Handler;
import core.Proc;
import core.Service;
import java.util.ArrayList;
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
    private RSExecutor                      executor;       /* Object that parse and execute commands */
    private RSHelper                        helper;         /* Object that parse and respond to help queries */
    private RSSnoop                         snoop;          /* Object for monitoring and reporting */
    
    private static boolean                  updConf; 
    
    public RootServ ( )  {
        super ( "RootServ" );
        this.initRootServ ( );
    }
    
    private void initRootServ ( )  {
        is              = true;
        this.snoop      = new RSSnoop ( this );
        this.executor   = new RSExecutor ( this, this.snoop );
        this.helper     = new RSHelper ( this, this.snoop );
        this.setCommands ( );
    }
    
    public static void updConf ( ) { 
        updConf = true;
    }
 
    public void setCommands ( )  {
        cmdList = new ArrayList<> ( );
        cmdList.add ( new CommandInfo ( "REHASH",   CMDAccess ( REHASH ) ,   "Reload the config file" )  );
        cmdList.add ( new CommandInfo ( "SRAW",     CMDAccess ( SRAW ) ,     "Send raw messages from services to the network" )  );
        cmdList.add ( new CommandInfo ( "SRA",      CMDAccess ( SRA ) ,      "Manage the Services Root Admin list" )  );
    }  
    
    
    /* Returns the list of added commands with its access and info */
    public static ArrayList<CommandInfo> getCMDList ( int access ) {
        return Handler.getRootServ().getCommandList ( access );
    }
     
    public static boolean enoughAccess ( User user, int hashName ) {
        return Handler.getRootServ().checkAccess ( user, hashName );
    }
    
    public boolean checkAccess ( User user, int hashName )  {
        int access              = user.getAccess ( );
        CommandInfo cmdInfo     = this.findCommandInfo ( hashName );
        if ( access < cmdInfo.getAccess ( )  )  {
            Handler.getRootServ().sendMsg ( user, "   Command "+cmdInfo.getName ( ) +" are for "+cmdInfo.getAccessStr ( ) +" only .. *sigh*" );
            return false;
        }
        return true;
    }
    
    
    public void parse ( User user, String[] cmd )  { 
        cmd[3] = cmd[3].substring ( 1 );
        
        if ( ! user.isAtleast ( SRA ) ) {
            return;
        }

        /* :DreamHealer PRIVMSG OperServ@stats.sshd.biz :help */
         
        switch ( cmd[3].toUpperCase().hashCode ( ) ) {
            case HELP :
                this.helper.parse ( user, cmd );
                break;
                
            default: 
                this.executor.parse ( user, cmd );
            
        } 
    }
    
    public void fixMaster ( ) {
        String master = Proc.getConf().get ( MASTER );
        NickInfo ni = NickServ.findNick ( master );
        User user;
        boolean newNick = false;
        
        if ( master.isEmpty() ) {
            System.out.println ( "Couldnt find Master nickname in configuration file." );
            System.exit ( 1 );
        }
        user = Handler.findUser ( master );
        
        if ( ni == null ) {
            ni = new NickInfo ( master );
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
                log = new OSLogEvent ( old.getName(), "DELMASTER", "new!master@services", "Services config" );
                OSDatabase.logEvent ( log );
                log = new OSLogEvent ( old.getName(), "ADDSRA", "new!master@services", "Services config" );
                OSDatabase.logEvent ( log );
            }
            log = new OSLogEvent ( ni.getName(), "ADDMASTER", "new!master@services", "Services config" );
            OSDatabase.logEvent ( log );
            if ( user != null ) {
                ni.setOper ( OSDatabase.getOper ( master ) );
                Handler.getRootServ().sendMsg ( user, "Nick: "+master+" is now set as Master of AServices." );
                if ( newNick ) {
                    Handler.getRootServ().sendMsg ( user, "Before anything!.. Please set a valid email on the Master nick and change password." );
                    Handler.getRootServ().sendMsg ( user, "NOTE: losing access of the master nick can cause inconvenience as only the master can manage the SRA list, and no SRA can add a new master." );
                }
            }
        } 
    }
    
    public static void is ( boolean state ) {
        is = state;
    }
    public void setState ( boolean state ) {
        RootServ.is = state;
    }

    public static boolean isUp ( ) { 
        return is;
    }
 
}   