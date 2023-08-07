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
package nickserv;

import chanserv.CSAcc;
import chanserv.CSAccessLogEvent;
import chanserv.CSDatabase;
import chanserv.CSLogEvent;
import chanserv.ChanInfo;
import chanserv.ChanServ;
import command.Command;
import core.CommandInfo;
import core.Handler;
import core.HashString;
import core.Proc;
import core.Service;
import core.StringMatch;
import core.TextFormat;
import java.math.BigInteger;
import server.ServSock;
import user.User;
import java.util.ArrayList;
import java.util.HashMap;
import mail.SendMail;
import memoserv.MSDatabase;

/**
 *
 * @author DreamHealer
 */
public class NickServ extends Service {
    private static boolean                  is = false;  

    private NSExecutor                      executor;       /* Object that parse and execute commands */
    private NSHelper                        helper;         /* Object that parse and respond to help queries */
    
    /* Mainentence */
    private long                            expireUnAuth;     /* expire authed nicks */
    
    /* Oper stuff */
    private NSSnoop                         snoop;          /* Object that parse and respond to help queries */ 
    private static HashMap<BigInteger,NickInfo>      niList  = new HashMap<> ( ); /* List of focused regged nicknames */  
    private static TextFormat               f       = new TextFormat ( );

    private static ArrayList<NSAuth>        newAuthList = new ArrayList<>();
    private static ArrayList<NSAuth>        newFullAuthList = new ArrayList<>();
    private static ArrayList<NickInfo>      changeList = new ArrayList<>();
    private static ArrayList<NickInfo>      regList = new ArrayList<>();
    private static ArrayList<NickInfo>      deleteList = new ArrayList<>();
    private static ArrayList<NSLogEvent>    logs = new ArrayList<>();

    /**
     *
     */
    public NickServ ( )  {
        super ( "NickServ" );
        initNickServ ( );    
    }
 
    private void initNickServ ( )  {
        setState ( true );
        this.snoop      = new NSSnoop ( this ); 
        this.executor   = new NSExecutor ( this, this.snoop );
        this.helper     = new NSHelper ( this, this.snoop );
        this.loadNicks ( ); /* Load all nicks */
        this.expireUnAuth = System.currentTimeMillis ( ) + ( 1000 * 60 * 60 ); /* check unauthed nicks in 1 hour */
        this.setCommands();
    }
     
    private void loadNicks ( )  {
       niList = NSDatabase.loadAllNicks ( );
       NSDatabase.loadAllSettings();
       NSDatabase.loadAllNickExp();
       MSDatabase.loadAllMemos();
       
    }

    /**
     *
     */
    public void setCommands ( )  {
        cmdList = new ArrayList<> ( );
        cmdList.add ( new CommandInfo ( "HELP",     0,   "Show help information" )                          );
        cmdList.add ( new CommandInfo ( "REGISTER", 0,   "Register a nickname" )                            );
        cmdList.add ( new CommandInfo ( "AUTH",     0,   "Authorize a mail or pass" )                       );
        cmdList.add ( new CommandInfo ( "IDENTIFY", 0,   "Identify as owner of a nick" )                    );
        cmdList.add ( new CommandInfo ( "GHOST",    0,   "Kill the ghost using your nick" )                 );
        cmdList.add ( new CommandInfo ( "SET",      0,   "Set nick options" )                               );
        cmdList.add ( new CommandInfo ( "INFO",     0,   "Show nick info" )                                 );
        cmdList.add ( new CommandInfo ( "DROP",     0,   "Drop / end nick registration" )                   );
        cmdList.add ( new CommandInfo ( "LIST",     CMDAccess ( LIST ),     "List registered nicks" )       );
        cmdList.add ( new CommandInfo ( "MARK",     CMDAccess ( MARK ),     "Mark nick" )                   );
        cmdList.add ( new CommandInfo ( "FREEZE",   CMDAccess ( FREEZE ),   "Freeze nick" )                 );
        cmdList.add ( new CommandInfo ( "HOLD",     CMDAccess ( HOLD ),     "Hold nick" )                   );
        cmdList.add ( new CommandInfo ( "NOGHOST",  CMDAccess ( NOGHOST ),  "Deactivate ghost for nick" )   );
        cmdList.add ( new CommandInfo ( "GETPASS",  CMDAccess ( GETPASS ),  "Get nick password" )           );
        cmdList.add ( new CommandInfo ( "GETEMAIL", CMDAccess ( GETEMAIL ), "Get nick email" )              );
        cmdList.add ( new CommandInfo ( "DELETE",   CMDAccess ( DELETE ),   "Get nick email" )              );
    }
    
    /**
     *
     * @param access
     * @return
     */
    public static ArrayList<CommandInfo> getCMDList ( HashString access ) {
        return Handler.getNickServ().getCommandList ( access );
    }

    /**
     *
     * @param user
     * @param hashName
     * @return
     */
    public static boolean enoughAccess ( User user, HashString hashName ) {
        return Handler.getNickServ().checkAccess ( user, hashName );
    }
    
    /**
     *
     * @param user
     * @param hashName
     * @return
     */
    public boolean checkAccess ( User user, HashString hashName )  {
        int access              = user.getAccess ( );
        CommandInfo cmdInfo     = this.findCommandInfo ( hashName );
        if ( access < cmdInfo.getAccess ( )  )  {
            if ( user.isAtleast(IRCOP) ) {
                Handler.getNickServ().sendMsg ( user, "   Command "+cmdInfo.getName ( ) +" are for "+cmdInfo.getAccessStr ( ) +" only .. *sigh*" );
            } else {
                Handler.getNickServ().sendMsg ( user, "Error: no such command." );
            }
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
        //:DreamHea1er PRIVMSG NickServ@services.sshd.biz :help
        if ( cmd == null || cmd[3].isEmpty ( )  )  { 
            return; 
        }
        
        //user.getUserFlood().incCounter ( this );
        
        cmd[3] = cmd[3].substring ( 1 );
        HashString command = new HashString ( cmd[3] );
        if ( command.is(HELP) ) {
            this.helper.parse ( user, cmd );
        } else {
            this.executor.parse ( user, cmd );
        }
         
    }
      
    /* Registered entities */

    /**
     *
     * @param source
     * @return
     */

    public static NickInfo findNick ( String source )  {
        HashString name;
        NickInfo ni;
        
        if ( source.contains ( ":" )  )  {
            name = new HashString ( source.substring(1) );
        } else {
            name = new HashString ( source );
        }
        return findNick ( name );
    }
    
    /* Registered entities */

    /**
     *
     * @param name
     * @return
     */

    public static NickInfo findNick ( HashString name )  {
        return niList.get(name.getCode());
    }

    static ArrayList<NickInfo> searchNicks ( String string ) {
        NickInfo ni;
        ArrayList<NickInfo> nicks = new ArrayList<>();
        for ( HashMap.Entry<BigInteger,NickInfo> entry : niList.entrySet() ) {
            ni = entry.getValue();
            if ( StringMatch.wild ( ni.getName().getString().toUpperCase(), string.toUpperCase() ) ) {
                nicks.add ( ni );
            }
        }
        return nicks;
    }
    
    /**
     *
     * @param ni
     */
    public static void addNick ( NickInfo ni ) { 
        if ( ! is ) {
            return;
        } 
        niList.put ( ni.getName().getCode(), ni ); 
    }

    /**
     *
     */
    public static void listNicks ( ) {
        if ( ! is ) {
            return;
        }
        for ( HashMap.Entry<BigInteger,NickInfo> entry : niList.entrySet() ) {
            System.out.println ( "NICKLIST: "+entry.getValue().getString ( FULLMASK ) );
        }
    }
 
    /* Ownership messages */

    /**
     *
     * @param ni
     * @param message
     */

    public static void notifyIdentifiedUsers ( NickInfo ni, String message ) {
        if ( ni == null ) {
            return;
        }
        for ( User user : Handler.findUsersByNick ( ni ) ) {
            Handler.getNickServ().sendMsg ( user, message );
        }
    }
    
    static void unIdentifyAllButOne ( NickInfo ni ) {
        if ( ni == null ) {
            return;
        }
        for ( User user : Handler.findUsersByNick ( ni ) ) {
            if ( ! user.hasAccess(ni.getName()) ) {
                user.getSID().del ( ni );
                Handler.getNickServ().sendMsg ( user, "You have been unidentified from nick: "+ni.getName() );
            }
        }
    }
    
    static void unIdentifyAllFromNick ( NickInfo ni ) {
        if (ni == null) {
            return;
        }
        for ( User user : Handler.findUsersByNick ( ni ) ) {
            user.getSID().del ( ni );
            Handler.addUpdateSID ( user.getSID() );
            if ( user.is(ni.getName()) ) {
                ServSock.sendCmd ( ":"+Proc.getConf().get(NAME)+" SVSMODE "+user.getName()+" 0 -r" );
                user.setMode ( IDENT, false );
                Handler.getGuestServ().addNick ( user, ni );
            }
            Handler.getNickServ().sendMsg ( user, "You have been unidentified from nick: "+ni.getName() );
        }
    }
    
    /**
     *
     * @return
     */
    public static int secMaintenance ( ) {
        NickInfo ni = null;
        for ( HashMap.Entry<BigInteger,NickInfo> entry : niList.entrySet() ) {
            ni = entry.getValue();
            if ( ni.isState ( OLD ) &&
                 ni.getExp().isTimeToSendAnotherMail ( ) ) {
                SendMail.sendExpNick ( ni );
                ni.getExp().incMailCount ( );
                NSDatabase.saveNickExp ( ni );
            }
        }
        return 0;
    }
    
    /**
     *
     * @return
     */
    public static int maintenance ( ) {
        int todoAmount = 0;
        todoAmount += writeLogs ( );
        todoAmount += handleRegNicks ( );
        todoAmount += handleChangedNicks ( );
        todoAmount += handleNewAuths ( );
        todoAmount += handleFullNewAuths ( );
        todoAmount += handleDeletedNicks ( );
        return todoAmount;
    }
  
    private static int writeLogs ( ) {
        if ( NSDatabase.activateConnection ( ) && logs.size() > 0 ) {
            ArrayList<NSLogEvent> eLogs = new ArrayList<>();
            for ( NSLogEvent log : logs.subList ( 0, getIndexFromSize ( logs.size() ) ) ) {
                if ( NSDatabase.logEvent ( log ) > 0 ) {
                    eLogs.add ( log );
                }
            }
            for ( NSLogEvent log : eLogs ) {
                logs.remove ( log );
            }
        }
        return logs.size();
    }
    
    
    /*** HANDLE PENDING NICKS ***/
    
    /* Insert new auths */
    private static int handleNewAuths ( ) {
        if ( NSDatabase.activateConnection() && newAuthList.size() > 0 ) {
            ArrayList<NSAuth> auths = new ArrayList<>();
            for ( NSAuth auth : newAuthList ) {
                if ( ( auth.is(MAIL) && NSDatabase.addMail ( auth ) ) ||
                       auth.is(PASS) && NSDatabase.addPass ( auth ) ) {
                    auths.add ( auth );
                }
            }
            for ( NSAuth auth : auths ) {
                newAuthList.remove ( auth );
            }
        }
        return newAuthList.size();
    }
        /* Insert new auths */
    private static int handleFullNewAuths ( ) {
        if ( ! NSDatabase.activateConnection() || newFullAuthList.isEmpty() ) {
            return newFullAuthList.size();
        }
        NSAuth auth = newFullAuthList.get ( 0 );
        if ( NSDatabase.addFullAuth ( auth ) ) {
            newFullAuthList.remove ( auth );
        }
        return newFullAuthList.size();
    }
   
    private static int handleRegNicks ( ) {
        if ( ! NSDatabase.activateConnection() || regList.isEmpty() ) {
            return regList.size();
        }
        NickInfo ni = regList.get ( 0 );
        if ( NSDatabase.createNick ( ni ) == 1 ) {
            regList.remove ( ni );
        }
        return regList.size();
    }
    
    /* Update all nicks in the change list */
    private static int handleChangedNicks ( ) {
        if ( ! NSDatabase.activateConnection() || changeList.isEmpty() ) {
            return changeList.size();
        }
        NickInfo ni = changeList.get ( 0 );
        if ( NSDatabase.updateNick ( ni ) == 1 ) {
            ni.getChanges().clean();
            changeList.remove ( ni );
        }
        return changeList.size();
    }
       
    /* Update all nicks in the change list */
    private static int handleDeletedNicks ( ) {
        if ( ! NSDatabase.activateConnection() || deleteList.isEmpty() ) {
            return deleteList.size();
        }
        NickInfo ni = deleteList.get(0);
        if ( NSDatabase.deleteNick ( ni ) ) {
            deleteList.remove ( ni );
        }
        return deleteList.size();
    }

    /*************/
    
    private static ArrayList<NickInfo> getWorkList ( HashString it ) {
        if      ( it.is(REGISTER) )         { return regList;                   }
        else if ( it.is(CHANGE) )           { return changeList;                }
        else if ( it.is(DELETE) )           { return deleteList;                }
        else {
            return new ArrayList<>();
        }
    }
    
    /**
     *
     * @param list
     * @param ni
     */
    public static void addToWorkList ( HashString list, NickInfo ni ) {
        for ( NickInfo ni2 : getWorkList ( list ) ) {
            if ( ni2.is(ni) ) {
                return;
            }
        }
        getWorkList(list).add ( ni );
    }
  
    /* Handle LOGS */ 

    /**
     *
     * @param log
     */
 
    public static void addLog ( NSLogEvent log ) {
        logs.add ( log );
    }
     
    static void addNewAuth ( NSAuth mail ) {
        newAuthList.add ( mail );
    }
    static void addNewFullAuth ( NSAuth mail ) {
        newFullAuthList.add ( mail );
    }
    
    /**
     *
     * @param user
     * @param cmd
     */
    public void snoopAndLog ( User user, String[] cmd )  {
        try { 
            this.accessDenied ( user );
        } catch ( Exception e )  {
            Proc.log ( NickServ.class.getName ( ) , e );
        }
    }

    /* Send advertisement for this unregged nick */

    /**
     *
     * @param u
     */

    public void adNick ( User u )  {
        this.sendMsg ( 
            u, 
            "The nick "+f.b ( ) +u.getString ( NAME ) +f.b ( ) +" is currently not registered"
        );
    }
    
    /* Send statement to identify nickname */

    /**
     *
     * @param u
     */

    
    public static void adRegNick ( User u )  {
        Handler.getNickServ().sendMsg ( u, "The nick "+f.b ( ) +u.getString ( NAME ) +f.b ( ) +" is already registered" );
        Handler.getNickServ().sendMsg ( u, "You now have 60 seconds to identify as the owner of nickname "+f.b ( ) +u.getString ( NAME ) +f.b ( )  );
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
    
    /* auth a nick and send message to all identified nicks currently online */

    /**
     *
     * @param ni
     * @param command
     * @return
     */

    public boolean authorizeMail ( NickInfo ni, Command command )  {
        User user;
        
        if ( ni == null || command == null ) {
            return false;
        }         
        
        user = Handler.findUser ( ni.getName ( ) );
        
        if ( this.executor.authMail ( ni, command ) ) {
            fixIdentState ( user );
            return true;
        }
        return false;
    }
 
    /* auth a nick and send message to all identified nicks currently online */

    /**
     *
     * @param ni
     * @param command
     * @return
     */

    public boolean authorizePass ( NickInfo ni, Command command )  {
        if ( ni == null || command == null ) {
            return false;
        }         
 
        if ( this.executor.authPass ( ni, command ) ) {
            return true;
        }
        return false;
    }
    
    /**
     *
     * @param ni
     */
    public static void deleteNick ( NickInfo ni )  {
        NickInfo target = null;
        NickInfo nBuf = null;
        for ( HashMap.Entry<BigInteger,NickInfo> entry : niList.entrySet() ) {
            nBuf = entry.getValue();
            if ( nBuf.is(ni) ) {
                target = nBuf;
            }
        }
        if ( target != null )  {
            niList.remove ( target );
        }
    }
    
    /**
     *
     * @param u
     */
    public static void fixIdentState ( User u )  {
        NickInfo ni;

        if ( u == null ) {
            return;
        }
        
        ni = NickServ.findNick ( u.getName ( ) );
        
        if ( ni != null ) {
            if ( ni.isSet(FROZEN) && u.getSID().isIdentified(ni) ) {
                /* Nick is frozen so lets unidentify the user */
                NickServ.unIdentifyAllFromNick ( ni );
            }
            if ( u.getSID().isIdentified ( ni ) ) {
                u.getSID().resetTimers ( );
                if ( ni.isAuth ( ) ) {
                    /* Send ident mode and serviceID to user */                         
                    ServSock.sendCmd ( ":"+Proc.getConf().get ( NAME ) +" SVSMODE "+u.getString ( NAME ) +" 0 +rd "+u.getSID().getID ( ) ); 
                  
                } else {
                    ServSock.sendCmd ( ":"+Proc.getConf().get ( NAME ) +" SVSMODE "+u.getString ( NAME ) +" 0 +d "+u.getSID().getID ( ) ); 
                    /* Send serviceID to user */                          
                }
                u.getModes().set ( IDENT, true );
                Handler.getMemoServ().checkNick ( ni, u );

            } else {
                ServSock.sendCmd ( ":"+Proc.getConf().get ( NAME ) +" SVSMODE "+u.getString ( NAME ) +" 0 -r" );
                Handler.getGuestServ().addNick ( u, ni );      
            }

        } else {
            /* Oreggat nick */
            ServSock.sendCmd ( ":"+Proc.getConf().get ( NAME ) +" SVSMODE "+u.getString ( NAME ) +" 0 -r" );
            Handler.getNickServ().adNick ( u ); /* let nickserv advertise registration */
            u.resetState ( );
        }
     
    }
    
    /**
     *
     * @param ni
     */
    public void dropNick ( NickInfo ni ) {
//        NickInfo ni = NickServ.findNick(in.getNameStr());
        /* Message all currently idented users then unident them */
        ArrayList<User> uList = Handler.findUsersByNick ( ni );
        ArrayList<ChanInfo> cList;
        ArrayList<ChanInfo> remList = new ArrayList<>();
        CSAcc acc = null;
        HashString[] lists = { SOP, AOP, AKICK };
        //HashMap<Integer,Integer> map = new HashMap ( );
        //map.put ( AOP, "DELAOP".hashCode() );
        //map.put ( SOP, "DELSOP".hashCode() );
        //map.put ( AKICK, "DELAKICK".hashCode() );
        remList.addAll ( ni.getChanAccess ( FOUNDER ) );
        for ( ChanInfo ci : remList ) {
            Handler.getChanServ().dropChan ( ci );
            CSLogEvent log = new CSLogEvent ( ci.getName(), EXPIREFOUNDER, "", "" );
            CSDatabase.logEvent ( log );
        }
        
        for ( HashString list : lists ) {
            remList.addAll ( ni.getChanAccess ( list ) );
            for ( ChanInfo ci : remList ) {
                if ( ( acc = ci.getAccess ( list, ni ) ) != null ) {
                    ci.delAccess ( list, acc );
                    CSAccessLogEvent log = new CSAccessLogEvent ( ci.getName(), list, ni.getNameStr() );
                    ChanServ.addAccessLog ( log );
                }
            }
            remList.clear ( );
        }
         
        for ( User user : uList ) {
            this.sendMsg ( user, "You have now been unidentified from nick: "+ni.getName ( ) );
            this.sendMsg ( user, "Nick "+ni.getName()+" which you are identified to has now been dropped");
            user.unIdentify ( ni );
        }

        User user;
        if ( ( user = Handler.findUser ( ni.getName() ) ) != null ) {
            this.sendCmd( "SVSMODE "+user.getString ( NAME )+" 0 -r" );
        }
        
        /* Remove nick from mem */
        niList.remove ( ni.getName().getCode() );

        /* Put the nick in the delete list */
        NickServ.addToWorkList ( DELETE, ni );
        
    }    

    /**
     *
     * @return
     */
    public int getNickRegStats ( ) {
        return regList.size ( );
    }

    /**
     *
     * @return
     */
    public int getChangesStats ( ) {
        return changeList.size ( );
    }
    
}