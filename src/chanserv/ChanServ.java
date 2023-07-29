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
package chanserv;

import nickserv.NickInfo;
import channel.Chan;
import core.CommandInfo;
import core.Handler;
import core.HashString;
import core.Proc;
import core.Service;
import core.StringMatch;
import core.TextFormat;
import java.math.BigInteger;
import user.User;
import java.util.ArrayList;
import java.util.HashMap;
import user.UserCheck;

/**
 *
 * @author DreamHealer
 */
public class ChanServ extends Service {
    private static boolean      is = false; 
    private static ChanServ     service;
    private CSExecutor          executor;    /* Object that parse and execute commands */
    private CSHelper            helper;        /* Object that parse and respond to help queries */ 
    private static CSSnoop      snoop;        /* Object that parse and respond to help queries */
    private static ArrayList<CSLogEvent> logs = new ArrayList<>();
    private static ArrayList<CSAccessLogEvent> accessLogs = new ArrayList<>();
    private static ArrayList<ChanInfo> regList = new ArrayList<>();
    private static ArrayList<ChanInfo> changeList = new ArrayList<>();
    private static ArrayList<ChanInfo> deleteList = new ArrayList<>();
    
    private static HashMap<BigInteger,ChanInfo> ciList = new HashMap<>(); /* List of regged channels */

    private static ArrayList<UserCheck> chUserCheckList = new ArrayList<>();
    
    private TextFormat          f;
    
    /**
     * ChanServ
     */
    public ChanServ ( )  {
        super ( "ChanServ" );
        initChanServ ( );
    }
 
    private void initChanServ ( )  {
        is              = true;
        snoop           = new CSSnoop ( this ); 
        this.executor   = new CSExecutor ( this, this.snoop );
        this.helper     = new CSHelper ( this, this.snoop ); 
        this.f          = new TextFormat ( );
        this.loadChans ( );
        this.setCommands();
        ChanServ.service = this;
    }
    
    /**
     * setCommands
     */
    public void setCommands ( )  {
        cmdList = new ArrayList<> ( );
        cmdList.add ( new CommandInfo ( "HELP",         0,                          "Show help information" )               );
        cmdList.add ( new CommandInfo ( "REGISTER",     0,                          "Register a channel" )                  );
        cmdList.add ( new CommandInfo ( "IDENTIFY",     0,                          "Identify as founder of a channel" )    );
        cmdList.add ( new CommandInfo ( "SET",          0,                          "Manage channel options" )              );
        cmdList.add ( new CommandInfo ( "CHANFLAG",     0,                          "Manage eXtended channel flags" )       );
        cmdList.add ( new CommandInfo ( "INFO",         0,                          "Show information about a channel" )    );
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
        cmdList.add ( new CommandInfo ( "LISTOPS",      0,                          "View the AOP/SOP/AKICK lists" )         );
        cmdList.add ( new CommandInfo ( "TOPICLOG",     CMDAccess ( TOPICLOG ),     "View the topic logs" )         );
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
    
    /**
     *
     * @param command
     * @return
     */
    public static ArrayList<CommandInfo> getCMDList ( HashString command ) {
        return Handler.getChanServ().getCommandList ( command );
    }
    
    /**
     *
     * @param user
     * @param hash
     * @return
     */
    public static boolean enoughAccess ( User user, HashString hash ) {
        return Handler.getChanServ().checkAccess ( user, hash );
    }
    
    /**
     *
     * @param user
     * @param hash
     * @return
     */
    public boolean checkAccess ( User user, HashString hash )  {
        int access              = user.getAccess ( );
        CommandInfo cmdInfo     = this.findCommandInfo ( hash );
        if ( access < cmdInfo.getAccess ( )  )  {
            Handler.getChanServ().sendMsg ( user, "Error: no such command!." );
            return false;
        }
        return true;
    }
     
    private void loadChans ( )  {
        ciList = CSDatabase.getAllChans ( );
        //CSDatabase.loadChanAccess ( SOP );
        //CSDatabase.loadChanAccess ( AOP );
        //CSDatabase.loadChanAccess ( AKICK );
        CSDatabase.loadAllChanAccess();
    }
    
    public static void attachSettings ( HashString name, ChanSetting settings ) {
        ciList.get(name.getCode()).setSettings ( settings );
    }
    
    
    /**
     *
     * @param user
     * @param cmd
     */
    public void parse ( User user, String[] cmd )  {
        //:DreamHea1er PRIVMSG NickServ@services.sshd.biz :help
        try {
            if ( cmd[3].isEmpty ( ) ) { 
                return; 
            }
        } catch ( Exception e ) {
            Proc.log ( ChanServ.class.getName ( ), e );
        }
        
//        user.getUserFlood().incCounter ( this );
         
        cmd[3] = cmd[3].substring ( 1 );
        HashString command = new HashString ( cmd[3] );
        if ( command.is(HELP) ) {
            this.helper.parse ( user, cmd );
        } else {
            this.executor.parse ( user, cmd );
        }
    }
      
    /**
     *
     * @param user
     * @param cmd
     */
    public void snoopAndLog ( User user, String[] cmd )  {
        try { 
            HashString serv = new HashString ( "NickServ" );
            snoop.msg ( false, serv, user, cmd );
            this.accessDenied ( user );
        } catch ( Exception e )  {
            Proc.log ( ChanServ.class.getName ( ) , e );
        }
    }

    /* Send advertisement for this unregged nick */

    /**
     *
     * @param u
     */

    public void adChan ( User u )  {
        this.sendMsg ( u, "The Chan "+f.b ( ) +u.getString ( NAME ) +f.b ( ) +" is currently not registered"      );
        this.sendMsg ( u, "To register the channel please type:"                                                    );
        this.sendMsg ( u, "    /ChanServ REGISTER <#channel> <Password> <description>"                              );
    }
    
    /**
     *
     * @param ci
     */
    public void checkAllUsers ( ChanInfo ci )  {
        Chan c;
        if ( ( c = Handler.findChan ( ci.getName() ) ) != null ) {
            for ( User u : c.getList ( ALL ) ) {
                this.checkUser ( c, u );                
            }
        }
    }   
    /**
     *
     * @param ci
     */
    public void checkAllUsers ( Chan c )  {
        if ( c != null ) {
            for ( User u : c.getList ( ALL ) ) {
                this.checkUser ( c, u );                
            }
        }
    }
    
    /**
     *
     * @param c
     * @param user
     */
    public void checkUser ( Chan c, User user )  {
        ChanInfo ci;
        NickInfo ni;
        CSAcc acc;
        
        /* Relay channel */
        if ( c.isRelay() ) {
            if ( ( ci = findChan ( c.getRelay() ) ) != null && ci.isRelayed() ) {
                if ( ci.isAtleastAop ( user ) ) {
                    Handler.getChanServ().opUser ( c, user );
                } else {
                    banUser ( c, user, "*!*@"+user.getHost() );
                    kickUser ( c, user, "Restricted for "+c.getRelay()+" staff" );
                }
            }
            
        /* Registered channel */
        } else if ( ( ci = findChan ( c.getName() ) ) != null ) {
            
            /* SAJoin */
            if ( c.isSaJoin() ) {
                c.toggleSaJoin();
                return;
            }
                
            /* Frozen/Closed */
            if ( ci.isSet ( FROZEN ) || ci.isSet ( CLOSED ) ) {
                return;
                
            /* Restricted */
            }  else if ( ci.isSet ( RESTRICT ) ) {
                banUser ( c, user, null );
                kickUser ( c, user, "Restricted channel" );
            }
            
            /* User Access */
            if ( ci.isAtleastAop ( user ) ) {
                ni = ci.getNickByUser ( user );
                ci.setLastUsed ( );
                if ( ni == null || ( ni != null && ! ni.is ( NEVEROP ) ) ) {
                    opUser ( c, user );
                    ci.updateLastOped ( user );
                }
            
            /* AKick */
            } else if ( ci.isAkick ( user ) ) {
                acc = ci.getAkickAccess ( user );
                banUser ( c, user, ( acc.getMask() != null ? acc.getMask().getString() : null ) );
                kickUser ( c, user, "AutoKicked" );
                
            /* OPGuard */
            } else if ( c.isOp ( user ) && ci.isSet ( OPGUARD ) ) {
                this.deOpUser ( c, user );
            } 
        }
    }
     
    /**
     *
     * @param c
     */
    public void checkSettings ( Chan c )  {
        ChanInfo ci;
        if ( c == null ) {
            return;
        }
        if ( ( ci = ChanServ.findChan ( c.getString ( NAME ) ) ) != null ) {
            if ( ci.isSet ( CLOSED ) ) {
                ci.kickAll ( "Channel is CLOSED" );
            } else {
                ci.getChanFlag().syncChangedValuesWithNetwork();
                if ( ci.isSet ( TOPICLOCK )  || ci.isSet ( KEEPTOPIC ) ) {
                    if ( ci.getTopic ( ) != null ) {
                        c.setTopic ( ci.getTopic ( )  );
                        this.sendCmd ( "TOPIC "+c.getString ( NAME ) +" "+ci.getTopic().getSetter ( ) +" "+ci.getTopic().getStamp ( ) +" :"+ci.getTopic().getText ( ) );
                    }
                }
                if ( ci.isSet ( AUDITORIUM ) ) {
                    this.sendCmd ( "MODE "+ci.getName ( ) +" 0 :+A" );
                }
                this.checkModes ( c, ci );
            }
        }
    }

    /**
     *
     * @param c
     * @param ci
     */
    public void checkModes ( Chan c, ChanInfo ci )  {
        if ( ci == null || c == null || ci.getSettings() == null || ci.getSettings().getModeLock() == null ) {
            return;
        }
        String missing = null;
        if ( ( missing = ci.getSettings().getModeLock().getMissingModes ( c, ci ) ) != null ) {
            c.getModes().setModeString ( missing );
            this.sendCmd ( "MODE "+ci.getName ( ) +" 0 :"+missing );
        }
    }

    /**
     *
     * @param u
     * @param c
     * @return
     */
    public boolean checkTopic ( User u, Chan c )  {
        ChanInfo ci;
        NickInfo ni;
        boolean updTopic = false;
        if ( c == null ) {
            return false;
        }

        if ( ( ci = ChanServ.findChan ( c.getString ( NAME ) ) ) != null ) {   /* If chan isSet regged */
            if ( ci.isSet ( TOPICLOCK )  )  {                    /* If topiclock isSet set */
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
                        if ( c.getTopic().getStamp ( )  != ci.getTopic().getStamp ( ) ) {
                            ci.setTopic ( c.getTopic ( ) );
                        }
                        return true;
                    } else {
                        c.setTopic ( ci.getTopic ( ) );
                        this.sendCmd ( "TOPIC "+c.getString ( NAME ) +" "+ci.getTopic().getSetter ( ) +" "+ci.getTopic().getStamp ( ) +" :"+ci.getTopic().getText ( ) );
                        return false;
                    }
            
                } else {
                    c.setTopic ( ci.getTopic ( )  );
                    this.sendCmd ( "TOPIC "+c.getString ( NAME ) +" "+ci.getTopic().getSetter ( ) +" "+ci.getTopic().getStamp ( ) +" :"+ci.getTopic().getText ( ) );
                    return false;
                }
            } else {
                ci.setTopic ( c.getTopic ( ) );
            }
            return true;
        }
        return false;
    }
  
    /**
     *
     * @param c
     * @param user
     * @param mask
     */
    public void banUser ( Chan c, User user, String mask )  {
        // :Pintuz MODE #avade 0 +o Pintuz
        if ( mask == null )  {
            this.sendCmd ( "MODE "+c.getString(NAME)+" +b *!"+user.getString(USER)+"@"+user.getString(HOST) );
        } else {
            this.sendCmd ( "MODE "+c.getString(NAME)+" +b "+mask );
        }
    }
    
    /**
     *
     * @param c
     * @param user
     */
    public void kickUser ( Chan c, User user, String reason )  {
        // :Pintuz MODE #avade 0 +o Pintuz
        if ( c.nickIsPresent ( user.getName() ) ) {
            if ( reason == null ) {
                reason = user.getNameStr();
            }
            c.remUser ( user );
            this.sendCmd ( "KICK "+c.getNameStr()+" :"+user.getNameStr() );
        }
    }
    
    /**
     *
     * @param c
     * @param user
     */
    public void opUser ( Chan c, User user )  {
        // :Pintuz MODE #avade 0 +o Pintuz
        ChanInfo ci;
        if ( ! c.isOp ( user )  )  {
            this.sendCmd ( "MODE "+c.getString ( NAME )+" +o "+user.getString ( NAME )  );
            c.chModeUser ( user, OP, OP, false );
            if ( ( ci = ChanServ.findChan ( c.getString(NAME) ) ) != null ) {
                ci.setLastUsed();
                ci.changed(LASTUSED);
            }
        }
    }
    
    /**
     *
     * @param c
     * @param user
     */
    public void deOpUser ( Chan c, User user )  {
        // :Pintuz MODE #avade 0 -o Pintuz
        if ( c.isOp ( user )  )  {
            this.sendCmd ( "MODE "+c.getString ( NAME ) +" -o "+user.getString ( NAME )  );
            c.chModeUser ( user, OP, USER, false );
        }
    }
  
    /**
     *
     * @param c
     * @param user
     */
    public void unBanUser ( Chan c, User user )  {
        this.sendCmd ( "SVSMODE "+c.getString ( NAME ) +" -b "+user.getString ( NAME )  );
    }
    
    /**
     *
     * @param c
     * @param user
     */
    public void invite ( Chan c, User user )  {
        this.sendCmd ( "INVITE "+user.getString ( NAME ) +" :"+c.getString ( NAME )  ); 
    }
     
    /* STATIC */

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
    public void setState ( boolean state ) {
        ChanServ.is = state;
    }

    /**
     *
     * @return
     */
    public static boolean isUp ( ) {
        return is;
    }
    
    /* Registered entities */

    /**
     *
     * @param name
     * @return
     */

    public static ChanInfo findChan ( HashString name )  {
        return ciList.get(name.getCode());
    }

    /**
     *
     * @param in
     * @return
     */
    public static ChanInfo findChan ( String in )  {
        HashString name = new HashString ( in );
        return findChan ( name );
    }

    
    static ArrayList<ChanInfo> searchChans ( String string ) {
        ArrayList<ChanInfo> chans = new ArrayList<>();
        ChanInfo ci = null;
        for ( HashMap.Entry<BigInteger,ChanInfo> entry : ciList.entrySet() ) {
            ci = entry.getValue();
            if ( StringMatch.wild ( ci.getName().getString().toUpperCase(), string.toUpperCase() ) ||
                 StringMatch.wild ( ci.getString (TOPIC).toUpperCase(), string.toUpperCase() ) ) {
                chans.add ( ci );
            }
        }
        return chans;
    }
    
    /**
     *
     * @param ci
     */
    public static void addChan ( ChanInfo ci ) {
        if ( ! is ) {
            return;
        } 
        ciList.put ( ci.getName().getCode(), ci );
        ci.getFounder().addToAccessList ( FOUNDER, ci );
    }

    /**
     *
     * @param ci
     */
    public static void delChan ( ChanInfo ci ) { 
        if ( ! is )  { return; } 
        ChanInfo target = ciList.remove(ci.getName().getCode());
        
        if ( target != null ) {
            target.getFounder().remFromAccessList ( FOUNDER, target );
        }
/*        
        ChanInfo cBuf = null;
        for ( HashMap.Entry<BigInteger,ChanInfo> entry : ciList.entrySet() ) {
            cBuf = entry.getValue();
            if ( cBuf.is(ci) ) {
                target = cBuf;
            }
        }
        if ( target != null )  {
            ciList.remove ( target );
            target.getFounder().remFromAccessList ( FOUNDER, target );
        }*/
    }

    /**
     *
     * @param ni
     * @param cListAccess
     */
    public static void removeNickFromChanListAccess ( NickInfo ni, ArrayList<ChanInfo> cListAccess )  {
        ChanInfo ci; 
        for ( ChanInfo cBuf : cListAccess )  {
            if ( ( ci = ChanServ.findChan ( cBuf.getName ( ) ) ) != null ) {
                ci.removeFromAll ( ni );
            }
        } 
    }
    
    /**
     *
     * @param ni
     * @param cListFounder
     */
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

    /**
     *
     * @param ci
     */
    public static void deopAll ( ChanInfo ci )  {
        Chan c;
        if  ( ci != null &&  ( c = Handler.findChan ( ci.getName ( )  )  )  != null )  {
            c.getList ( ALL ).forEach ( ( u ) -> {
                ChanServ.service.deOpUser ( c, u );
            });
        } 
    }

    /**
     *
     * @param c
     */
    public static void deopAll ( Chan c )  {
        c.getList ( ALL ).forEach ( ( u ) -> {
            ChanServ.service.deOpUser ( c, u );
        }); 
    }
    
    /**
     *
     * @param c
     */
    public static void kickAll ( Chan c )  { 
        ChanServ.kickAll ( c );
    }
    
    /**
     *
     * @return
     */
    public static int secMaintenance ( )  {
        int todoAmount = 0;
        todoAmount += snoop.maintenance();
        todoAmount += writeLogs ( );
        todoAmount += writeAccessLogs ( );
        todoAmount += handleRegList ( );
        todoAmount += handleUpdateList ( );
        todoAmount += handleDeleteList ( );
        checkUserList ( );
        return todoAmount;
    }
    public static int maintenance ( )  {
        int todoAmount = 0;
        
        return todoAmount;
    }
 
    private static void checkUserList ( ) {
        ArrayList<UserCheck> checked = new ArrayList<>();
        for ( UserCheck uc : chUserCheckList ) {
            Handler.getChanServ().checkUser ( uc.getChan(), uc.getUser() );
            checked.add(uc);
        }
        for ( UserCheck uc : checked ) {
            chUserCheckList.remove ( uc );
        }
    }
    
    /**
     *
     * @param chan
     * @param user
     */
    public static void addCheckUser ( Chan chan, User user ) {
        if ( chan == null || user == null ) {
            return;
        }
        for ( UserCheck uc : chUserCheckList ) {
            if ( uc.getChan().is(chan) &&
                 uc.getUser().is(user) ) {
                return;
            }
        }
        chUserCheckList.add ( new UserCheck ( chan, user ) );
    }
    
    /**
     *
     * @param log
     */
    public static void addLog ( CSLogEvent log ) {
        logs.add ( log );
    }
       
    /**
     *
     * @param log
     */
    public static void addAccessLog ( CSAccessLogEvent log ) {
        accessLogs.add ( log );
    }
    
    private static int writeLogs ( ) {
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
        return logs.size();
    }
    private static int writeAccessLogs ( ) {
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
        return accessLogs.size();
    }

    /* Write all pending channels to database */
    private static int handleRegList ( ) {
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
        return regList.size();
    }
    
    private static int handleUpdateList ( ) {
        if ( CSDatabase.activateConnection() && changeList.size() > 0 ) {
            ArrayList<ChanInfo> chans = new ArrayList<>();
            for ( ChanInfo ci : changeList.subList ( 0, getIndexFromSize ( changeList.size() ) ) ) {
                if ( CSDatabase.updateChan ( ci ) == 1 ) {
                    chans.add ( ci );
                }
                ci.maintenence ( );
            }
            for ( ChanInfo ci : chans ) {
                changeList.remove ( ci );
            }
        }
        return changeList.size();
    }
    /* Write all pending channels to database */
    private static int handleDeleteList ( ) {
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
        return deleteList.size();
    }
    
    private static ArrayList<ChanInfo> getWorkList ( HashString it ) {
        if      ( it.is(REGISTER) )             { return regList;               }
        else if ( it.is(CHANGE) )               { return changeList;            }
        else if ( it.is(DELETE) )               { return deleteList;            }
        else {
            return new ArrayList<>();
        }
    }
    
    /**
     *
     * @param list
     * @param ci
     */
    public static void addToWorkList ( HashString list, ChanInfo ci ) {
        for ( ChanInfo ci2 : getWorkList ( list ) ) {
            if ( ci.is(ci2) ) {
                return;
            }
        }
        ci.setLastUsed();
        getWorkList(list).add ( ci );
    }
    
    /* Message all currently idented users then unident them */

    /**
     *
     * @param ci
     */

    public void dropChan ( ChanInfo ci ) {
        ArrayList<User> uList = Handler.findIdentifiedUsersByChan ( ci );
        for ( User user : uList ) {
            this.sendMsg ( user, "You have now been unidentified from channel: "+ci.getName());
            user.unIdentify ( ci );
        }
        uList.addAll ( Handler.findUsersByNick ( ci.getFounder() ) );
        for ( User user : uList ) {
            this.sendMsg ( user, "Channel "+ci.getName()+" which you have been found to be associated with has now been dropped");
        }
        ciList.remove ( ci );
        
        /* All initial work has been done lets remove it from the database */
        ChanServ.addToWorkList ( DELETE, ci );
        ci.getFounder().remFromAccessList ( FOUNDER, ci );
    }

    /**
     *
     * @return
     */
    public int getChanRegStats() {
        return regList.size();
    }

    /**
     *
     * @return
     */
    public int getChangesStats() {
        return changeList.size();
    }
     
}
