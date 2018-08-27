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
package chanserv;

import nickserv.NickInfo;
import channel.Chan;
import core.CommandInfo;
import core.Handler;
import core.Proc;
import core.Service;
import core.StringMatch;
import core.TextFormat;
import user.User;
import java.util.ArrayList;
import operserv.OperServ;

/**
 *
 * @author DreamHealer
 */
public class ChanServ extends Service {
    private static boolean      is = false; 
    private static ChanServ     service;
    private CSExecutor          executor;    /* Object that parse and execute commands */
    private CSHelper            helper;        /* Object that parse and respond to help queries */ 
    private CSSnoop             snoop;        /* Object that parse and respond to help queries */
    private static ArrayList<CSLogEvent> logs = new ArrayList<>();
    private static ArrayList<CSAccessLogEvent> accessLogs = new ArrayList<>();
    private static ArrayList<ChanInfo> regList = new ArrayList<>();
    private static ArrayList<ChanInfo> changeList = new ArrayList<>();
    private static ArrayList<ChanInfo> deleteList = new ArrayList<>();
    
    private static ArrayList<ChanInfo> ciList = new ArrayList<>( ); /* List of focused regged channels */

    
    
    private TextFormat          f;
    
    public ChanServ ( )  {
        super ( "ChanServ" );
        initChanServ ( );
    }
 
    private void initChanServ ( )  {
        is              = true;
        this.snoop      = new CSSnoop ( this ); 
        this.executor   = new CSExecutor ( this, this.snoop );
        this.helper     = new CSHelper ( this, this.snoop ); 
        this.f          = new TextFormat ( );
        this.loadChans ( );
        this.setCommands();
        ChanServ.service = this;
    }
    
    public void setCommands ( )  {
        cmdList = new ArrayList<> ( );
        cmdList.add ( new CommandInfo ( "HELP",         0,                          "Show help information" )               );
        cmdList.add ( new CommandInfo ( "REGISTER",     0,                          "Register a channel" )                  );
        cmdList.add ( new CommandInfo ( "IDENTIFY",     0,                          "Identify as founder of a channel" )    );
        cmdList.add ( new CommandInfo ( "SET",          0,                          "Show user list" )                      );
        cmdList.add ( new CommandInfo ( "INFO",         0,                          "Show uptime" )                         );
        cmdList.add ( new CommandInfo ( "AOP",          0,                          "Manage the AOP list" )                 );
        cmdList.add ( new CommandInfo ( "SOP",          0,                          "Manage the SOP list" )                 );
        cmdList.add ( new CommandInfo ( "AKICK",        0,                          "Manage the AKick list" )               );
        cmdList.add ( new CommandInfo ( "OP",           0,                          null )                                  );
        cmdList.add ( new CommandInfo ( "DEOP",         0,                          null )                                  );
        cmdList.add ( new CommandInfo ( "UNBAN",        0,                          null )                                  );
        cmdList.add ( new CommandInfo ( "INVITE",       0,                          null )                                  );
        cmdList.add ( new CommandInfo ( "WHY",          0,                          null )                                  );
        cmdList.add ( new CommandInfo ( "CHANLIST",     0,                          null )                                  );
        cmdList.add ( new CommandInfo ( "MDEOP",        0,                          null )                                  );
        cmdList.add ( new CommandInfo ( "MKICK",        0,                          null )                                  );
        cmdList.add ( new CommandInfo ( "DROP",         0,                          null )                                  );
        cmdList.add ( new CommandInfo ( "ACCESSLOG",    0,                          "View the AOP/SOP/AKICK logs" )         );
        cmdList.add ( new CommandInfo ( "LIST",         CMDAccess ( LIST ),         "List registered channels" )            );
        cmdList.add ( new CommandInfo ( "CHANLIST",     CMDAccess ( CHANLIST ),     "List chans associated with nick" )     );
        cmdList.add ( new CommandInfo ( "MARK",         CMDAccess ( MARK ),         "Mark channel" )                        );
        cmdList.add ( new CommandInfo ( "FREEZE",       CMDAccess ( FREEZE ),       "Freeze channel" )                      );
        cmdList.add ( new CommandInfo ( "CLOSE",        CMDAccess ( CLOSE ),        "Close channel" )                       );
        cmdList.add ( new CommandInfo ( "HOLD",         CMDAccess ( HOLD ),         "Hold channel" )                        );
        cmdList.add ( new CommandInfo ( "AUDITORIUM",   CMDAccess ( AUDITORIUM ),   "Set Auditorium setting" )              );
        cmdList.add ( new CommandInfo ( "GETPASS",      CMDAccess ( GETPASS ),      "Get channel password" )                );
        cmdList.add ( new CommandInfo ( "DELETE",       CMDAccess ( DELETE ),       "Force DROP a channel" )                );
    }
    
    public static ArrayList<CommandInfo> getCMDList ( int command ) {
        return Handler.getChanServ().getCommandList ( command );
    }
    
    public static boolean enoughAccess ( User user, int hashName ) {
        return Handler.getChanServ().checkAccess ( user, hashName );
    }
    
    public boolean checkAccess ( User user, int hashName )  {
        int access              = user.getAccess ( );
        CommandInfo cmdInfo     = this.findCommandInfo ( hashName );
        if ( access < cmdInfo.getAccess ( )  )  {
            Handler.getChanServ().sendMsg ( user, "Error: no such command!." );
            return false;
        }
        return true;
    }
     
    private void loadChans ( )  {
        ciList = CSDatabase.getAllChans ( );
    }
    
    public void parse ( User user, String[] cmd )  {
        //:DreamHea1er PRIVMSG NickServ@services.sshd.biz :help
        try {
            if ( cmd[3].isEmpty ( )  )  { 
                return; 
            }
        } catch ( Exception e )  {
            Proc.log ( ChanServ.class.getName ( ) , e );
        }
        
        user.getUserFlood().incCounter ( this );
        
        cmd[3] = cmd[3].substring ( 1 );
        switch ( cmd[3].toUpperCase ( ).hashCode ( )  )  {
            case HELP :         { this.helper.parse ( user, cmd );     break; }
            default: {
                this.executor.parse ( user, cmd );
            }
        } 
    }
      
    public void snoopAndLog ( User user, String[] cmd )  {
        try { 
            this.snoop.msg ( false, "NickServ", user, cmd );
            this.accessDenied ( user );
        } catch ( Exception e )  {
            Proc.log ( ChanServ.class.getName ( ) , e );
        }
    }

    /* Send advertisement for this unregged nick */
    public void adChan ( User u )  {
        this.sendMsg ( u, "The Chan "+f.b ( ) +u.getString ( NAME ) +f.b ( ) +" is currently not registered"      );
        this.sendMsg ( u, "To register the channel please type:"                                                    );
        this.sendMsg ( u, "    /ChanServ REGISTER <#channel> <Password> <description>"                              );
    }
    
    
    public void checkAllUsers ( ChanInfo ci )  {
        Chan c;
        if (  ( c = Handler.findChan ( ci.getName ( )  )  )  != null )  {
            for ( User u : c.getList ( ALL ) ) {
                this.checkUser ( c, u );                
            }
        }
    }
    
    public void checkUser ( Chan c, User user )  {
        ChanInfo ci;
        NickInfo ni;
        CSAccess access;
        int akick;
        
        try { 
            if ( ( ci = findChan ( c.getString ( NAME ) ) ) != null ) { /* Channel is registered */
                if  ( ci.is ( FROZEN ) || ci.is ( CLOSED ) ) {
                    return;
                }
                if ( ( ni = ci.getNickByUser ( user ) ) != null ) { /* Idented nick */
                    if ( ci.isAccess ( AOP, ni )  || ci.isAccess ( SOP, ni )  || ci.isFounder ( ni )  )  { /* Nick based */
                        if ( ! ni.getSettings().is ( NEVEROP )  )  {
                            opUser ( c, user );
                        }
                        return;
                    } else if ( ci.isAccess ( AOP, user )  || ci.isAccess ( SOP, user )  )  { /* User based */
                        if ( ! ni.getSettings().is ( NEVEROP )  )  {
                            opUser ( c, user );
                        }
                        return;
                    }
                } else {
                    /* No access */
                    if ( ci.getSettings().is ( RESTRICT )  )  {
                        /* chan is restricted */
                        this.banUser ( c, user, null );
                        this.kickUser ( c, user );
                        
                    } else if ( ci.getSettings().is ( OPGUARD )  )  {
                        /* opguard is active */
                        this.deOpUser ( c, user );
                    }
                }

                akick = ci.isAkick ( user );
                if ( akick > 0 )  {
                     access = ci.getAkickAccess ( user );
                     if ( akick == 1 )  {
                        this.banUser ( c, user, "*!"+access.getNick ( ) .getString ( USER ) +"@"+access.getNick ( ) .getString ( HOST )  );
                    } else if ( akick == 2 )  {
                        this.banUser ( c, user, access.getMask ( )  );
                    }
                    this.kickUser ( c, user );
                }
            }
        } catch ( Exception e )  {
            Proc.log ( ChanServ.class.getName ( ) , e );
        }
    }
    public void banUser ( Chan c, User user, String mask )  {
        // :Pintuz MODE #avade 0 +o Pintuz
        if ( mask == null )  {
            this.sendCmd ( "MODE "+c.getString ( NAME ) +" +b *!"+user.getString ( USER ) +"@"+user.getString ( HOST )  );
        } else {
            this.sendCmd ( "MODE "+c.getString ( NAME ) +" +b "+mask );
        }
    }
    
    public void checkSettings ( Chan c )  {
        ChanInfo ci;
        if ( c == null ) {
            return;
        }
        if ( ( ci = ChanServ.findChan ( c.getString ( NAME ) ) ) != null ) {
            if ( ci.is ( CLOSED ) ) {
                ci.kickAll ( "Channel is CLOSED" );
            } else {
                if ( ci.is ( TOPICLOCK )  || ci.is ( KEEPTOPIC )  )  {
                    c.setTopic ( ci.getTopic ( )  );
                    this.sendCmd ( "TOPIC "+c.getString ( NAME ) +" "+ci.getTopic().getSetter ( ) +" "+ci.getTopic().getTime ( ) +" :"+ci.getTopic().getTopic ( ) );
                }
                if ( ci.is ( AUDITORIUM ) ) {
                    this.sendCmd ( "MODE "+ci.getName ( ) +" 0 :+A" );
                }
                this.checkModes ( c, ci );
            }
        }
    }
    public void checkModes ( Chan c, ChanInfo ci )  {
        if ( ci == null || c == null || ci.getSettings() == null || ci.getSettings().getModeLock() == null ) {
            return;
        }
        String missing = ci.getSettings().getModeLock().getMissingModes ( c, ci );
        c.getModes().setModeString ( missing );
        this.sendCmd ( "MODE "+ci.getName ( ) +" 0 :"+missing );
    }

    public boolean checkTopic ( User u, Chan c )  {
        ChanInfo ci;
        NickInfo ni;
        boolean updTopic = false;
        if ( c == null ) {
            return false;
        }

        if ( ( ci = ChanServ.findChan ( c.getString ( NAME ) ) ) != null ) {   /* If chan is regged */
            if ( ci.is ( TOPICLOCK )  )  {                    /* If topiclock is set */
                if (  ( ni = ci.getNickByUser ( u )  )  != null ) {            /* Nick has some access to the chan */
                    if ( ci.getSettings().isTopicLock ( FOUNDER ) ) {
                        if ( ci.isFounder ( ni )  )  {
                            updTopic = true;
                        }
                    } else if ( ci.getSettings().isTopicLock ( SOP ) ) {
                        if ( ci.isAccess ( SOP, ni )  || ci.isFounder ( ni ) ) {
                            updTopic = true; 
                        }
                    } else if ( ci.getSettings().isTopicLock ( AOP ) ) {
                        if ( ci.isAccess ( AOP, ni )  || ci.isAccess ( SOP, ni )  || ci.isFounder ( ni ) ) {
                            updTopic = true; 
                        }
                    }
               
                    if ( updTopic )  {
                        if ( c.getTopic().getTime ( )  != ci.getTopic().getTime ( ) ) {
                            ci.setTopic ( c.getTopic ( ) );
                        }
                        return true;
                    } else {
                        c.setTopic ( ci.getTopic ( ) );
                        this.sendCmd ( "TOPIC "+c.getString ( NAME ) +" "+ci.getTopic().getSetter ( ) +" "+ci.getTopic().getTime ( ) +" :"+ci.getTopic().getTopic ( ) );
                        return false;
                    }
            
                } else {
                    c.setTopic ( ci.getTopic ( )  );
                    this.sendCmd ( "TOPIC "+c.getString ( NAME ) +" "+ci.getTopic().getSetter ( ) +" "+ci.getTopic().getTime ( ) +" :"+ci.getTopic().getTopic ( ) );
                    return false;
                }
            } else {
                ci.setTopic ( c.getTopic ( ) );
            }
            return true;
        }
        return false;
    }
  
    
    public void kickUser ( Chan c, User user )  {
        // :Pintuz MODE #avade 0 +o Pintuz
        if ( c.nickIsPresent ( user.getString ( NAME )  )  )  {
            this.sendCmd ( "KICK "+c.getString ( NAME ) +" :"+user.getString ( NAME )  );
        }
    }
    
    public void opUser ( Chan c, User user )  {
        // :Pintuz MODE #avade 0 +o Pintuz
        if ( ! c.isOp ( user )  )  {
            this.sendCmd ( "MODE "+c.getString ( NAME )+" +o "+user.getString ( NAME )  );
            c.chModeUser ( user.getString ( NAME ) , OP, OP, false );
        }
    }
    
    public void deOpUser ( Chan c, User user )  {
        // :Pintuz MODE #avade 0 -o Pintuz
        if ( c.isOp ( user )  )  {
            this.sendCmd ( "MODE "+c.getString ( NAME ) +" -o "+user.getString ( NAME )  );
            c.chModeUser ( user.getString ( NAME ) , OP, USER, false );
        }
    }
  
    public void unBanUser ( Chan c, User user )  {
        this.sendCmd ( "SVSMODE "+c.getString ( NAME ) +" -b "+user.getString ( NAME )  );
    }
    
    public void invite ( Chan c, User user )  {
        this.sendCmd ( "INVITE "+user.getString ( NAME ) +" :"+c.getString ( NAME )  ); 
    }
     
    /* STATIC */
    public static void is ( boolean state ) {
        is = state;
    }
    public void setState ( boolean state ) {
        ChanServ.is = state;
    }

    public static boolean isUp ( ) {
        return is;
    }
    
    /* Registered entities */
    public static ChanInfo findChan ( String source )  {
        if ( ! is )  {
            return null;
        }
        int hash = source.toUpperCase ( ) .hashCode ( );
        for ( ChanInfo ci : ciList )  {
            if ( hash == ci.getHashName ( )  )  {
                return ci;
            }
        } 
        return null; 
    }

    
    static ArrayList<ChanInfo> searchChans ( String string ) {
        ArrayList<ChanInfo> chans = new ArrayList<>();
        for ( ChanInfo ci : ciList ) {
            if ( StringMatch.wild ( ci.getName(), string ) ) {
                chans.add ( ci );
            }
        }
        return chans;
    }
    
    public static void addChan ( ChanInfo ci ) {
        if ( ! is ) {
            return;
        } 
        ciList.add ( ci );
        ci.getFounder().addToAccessList ( FOUNDER, ci );
    }
    public static void delChan ( ChanInfo ci ) { 
        if ( ! is )  {return;} 
        ChanInfo target = null;
        for ( ChanInfo cBuf : ciList )  {
            if ( cBuf.getHashName ( )  == ci.getHashName ( )  )  {
                target = cBuf;
            }
        }
        if ( target != null )  {
            ciList.remove ( target );
            target.getFounder().remFromAccessList ( FOUNDER, target );
        }
    }

     
    public static void removeNickFromChanListAccess ( NickInfo ni, ArrayList<ChanInfo> cListAccess )  {
        ChanInfo ci; 
        for ( ChanInfo cBuf : cListAccess )  {
            if ( ( ci = ChanServ.findChan ( cBuf.getName ( ) ) ) != null ) {
                ci.removeFromAll ( ni );
            }
        } 
    }
    
    public static void expireFounderNicks ( NickInfo ni, ArrayList<ChanInfo> cListFounder )  {
        ChanInfo ci;
        for ( ChanInfo cBuf : cListFounder )  {
            if (  ( ci = ChanServ.findChan ( cBuf.getName ( )  )  )  != null )  {
                ChanServ.delChan ( ci );
                ChanServ.addToWorkList ( DELETE, ci );
                ChanServ.service.sendOpMsg ( ci, "Channel "+ci.getName ( ) +" has expired due to that the founder nick has expired." );
                ChanServ.deopAll ( ci );
            }
        } 
    }
    public static void deopAll ( ChanInfo ci )  {
        Chan c;
        if  ( ci != null &&  ( c = Handler.findChan ( ci.getName ( )  )  )  != null )  {
            c.getList ( ALL ).forEach ( ( u ) -> {
                ChanServ.service.deOpUser ( c, u );
            });
        } 
    }
    public static void deopAll ( Chan c )  { 
        c.getList ( ALL ).forEach ( ( u ) -> {
            ChanServ.service.deOpUser ( c, u );
        }); 
    }
    
    public static void kickAll ( Chan c )  { 
        ChanServ.kickAll ( c );
    }
    
    
    public static void maintenance ( )  {
        writeLogs ( );
        writeAccessLogs ( );
        handleRegList ( );
        handleUpdateList ( );
        handleDeleteList ( );
    }
    
    public static void addLog ( CSLogEvent log ) {
        logs.add ( log );
    }
       
    public static void addAccessLog ( CSAccessLogEvent log ) {
        accessLogs.add ( log );
    }
    
    private static void writeLogs ( ) {
        if ( CSDatabase.activateConnection ( ) && logs.size() > 0 ) {
            ArrayList<CSLogEvent> eLogs = new ArrayList<>();
            for ( CSLogEvent log : logs.subList ( 0, getIndexFromSize ( logs.size() ) ) ) {
                if ( CSDatabase.logEvent ( log ) > 0 ) {
                    eLogs.add ( log );
                }
            }
            for ( CSLogEvent log : eLogs ) {
                logs.remove ( log );
            }
        }
    }
    private static void writeAccessLogs ( ) {
        if ( CSDatabase.activateConnection ( ) && accessLogs.size() > 0 ) {
            ArrayList<CSAccessLogEvent> eLogs = new ArrayList<>();
            for ( CSAccessLogEvent log : accessLogs.subList ( 0, getIndexFromSize ( accessLogs.size() ) ) ) {
                if ( CSDatabase.accesslogEvent ( log ) ) {
                    eLogs.add ( log );
                }
            }
            for ( CSAccessLogEvent log : eLogs ) {
                accessLogs.remove ( log );
            }
        }
    }

    /* Write all pending channels to database */
    private static void handleRegList ( ) {
        if ( CSDatabase.activateConnection() && regList.size() > 0 ) {
            ArrayList<ChanInfo> chans = new ArrayList<>();            
            for ( ChanInfo ci : regList.subList ( 0, getIndexFromSize ( regList.size() ) ) ) {
                if ( CSDatabase.createChan ( ci ) == 1 ) {
                    chans.add ( ci );
                }
            }
            for ( ChanInfo ci : chans ) {
                regList.remove ( ci );
            }
        }
    }
    
    private static void handleUpdateList ( ) {
        if ( CSDatabase.activateConnection() && changeList.size() > 0 ) {
            ArrayList<ChanInfo> chans = new ArrayList<>();
            for ( ChanInfo ci : changeList.subList ( 0, getIndexFromSize ( changeList.size() ) ) ) {
                if ( CSDatabase.updateChan ( ci ) == 1 &&
                     CSDatabase.updateChanSettings ( ci ) == 1 &&
                     CSDatabase.updateChanTopic ( ci ) == 1 ) {
                    chans.add ( ci );
                }
                ci.maintenence ( );
            }
            for ( ChanInfo ci : chans ) {
                changeList.remove ( ci );
            }
        }
    }
    /* Write all pending channels to database */
    private static void handleDeleteList ( ) {
        if ( CSDatabase.activateConnection() && deleteList.size() > 0 ) {
            ArrayList<ChanInfo> chans = new ArrayList<>();            
            for ( ChanInfo ci : deleteList.subList ( 0, getIndexFromSize ( deleteList.size() ) ) ) {
                if ( CSDatabase.deleteChan ( ci ) ) {
                    chans.add ( ci );
                }
            }
            for ( ChanInfo ci : chans ) {
                deleteList.remove ( ci );
            }
        }
    }
    
    private static ArrayList<ChanInfo> getWorkList ( int name ) {
        switch ( name ) {
            case REGISTER :
                return regList;
                
            case CHANGE :
                return changeList;
                
            case DELETE :
                return deleteList;
                
            default :
                return new ArrayList<>();
        }
    }
    
    public static void addToWorkList ( int list, ChanInfo ci ) {
        for ( ChanInfo ci2 : getWorkList ( list ) ) {
            if ( ci.getHashName() == ci2.getHashName() ) {
                return;
            }
        }
        getWorkList(list).add ( ci );
    }
    
    /* Message all currently idented users then unident them */
    public void dropChan ( ChanInfo ci ) {
        ArrayList<User> uList = Handler.findIdentifiedUsersByChan ( ci );
        for ( User user : uList ) {
            this.sendMsg ( user, "You have now been unidentified from channel: "+ci.getName());
            user.unIdentify ( ci );
        }
        uList.addAll ( Handler.findIdentifiedUsersByNick ( ci.getFounder() ) );
        for ( User user : uList ) {
            this.sendMsg ( user, "Channel "+ci.getName()+" which you have been found to be associated with has now been dropped");
        }
        ciList.remove ( ci );
        
        /* All initial work has been done lets remove it from the database */
        ChanServ.addToWorkList ( DELETE, ci );
        ci.getFounder().remFromAccessList ( FOUNDER, ci );
    }
     
}
