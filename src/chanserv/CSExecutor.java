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

import channel.Chan;
import channel.Topic;
import core.Config;
import core.Executor;
import core.Handler;
import core.Proc;
import core.TextFormat;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import nickserv.NickInfo;
import nickserv.NickServ;
import user.User;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

/**
 * Executing user commands
 * @author DreamHealer
 */
 public class CSExecutor extends Executor {
    private CSSnoop snoop;
    private TextFormat f;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

 
    public CSExecutor ( ChanServ service, CSSnoop snoop )  {
        super ( );
        this.service = service;
        this.snoop = snoop;
        this.f = new TextFormat ( );
    }

    public void parse ( User user, String[] cmd )  {
         
        try {
            if ( cmd[3].isEmpty ( )  )  {
                this.help ( user );
                return; 
            }
        } catch ( Exception e )  {
            this.help ( user );
            return;
        }
        switch ( cmd[3].toUpperCase ( ) .hashCode ( )  )  {
            /* User Commands */
            case REGISTER :
                this.register ( user, cmd );
                break;
                
            case IDENTIFY :
                this.identify ( user, cmd );
                break;
                
            case DROP :
                this.drop ( user, cmd );
                break;
                
            case SET :
                this.set ( user, cmd );
                break;
                
            case CHANFLAG :
                this.chanFlag ( user, cmd );
                break;
                
            case INFO :
                this.info ( user, cmd );
                break;
                
            case SOP :
                this.access ( SOP, user, cmd );
                break;
               
            case AOP :
                this.access ( AOP, user, cmd );
                break;
                
            case AKICK :
                this.access ( AKICK, user, cmd );
                break;
                
            case OP :
                this.op ( user, cmd );
                break;
                
            case DEOP :
                this.deop ( user, cmd ); 
                break; 
                
            case MDEOP :
                this.mDeop ( user, cmd );
                break;
                
            case MKICK :
                this.mKick ( user, cmd );
                break;
               
            case UNBAN :
                this.unban ( user, cmd );
                break;
                
            case INVITE :
                this.invite ( user, cmd );
                break;
                
            case WHY :
                this.why ( user, cmd );
                break; 
                         
            case ACCESSLOG :
                this.accesslog ( user, cmd );
                break; 
           
            case CHANLIST :
                this.chanList ( user, cmd );
                break;
                
            /* Oper Only Commands */
            case TOPICLOG :
                this.topiclog ( user, cmd );
                break; 

            case LIST :
                this.list ( user, cmd ); 
                break;
                
            case MARK :
                this.changeFlag ( MARK, user, cmd );
                break;
                                
            case FREEZE :
                this.changeFlag ( FREEZE, user, cmd );
                break;
                        
            case CLOSE :
                this.changeFlag ( CLOSE, user, cmd );
                break;
            
            case HOLD :
                this.changeFlag ( HOLD, user, cmd );
                break;
                         
            case AUDITORIUM :
                this.changeFlag ( AUDITORIUM, user, cmd );
                break;
            
            case DELETE :
                this.delete ( user, cmd );
                break;
                         
            case GETPASS :
                this.getPass ( user, cmd );
                break;
                
            default :
                this.noMatch ( user, cmd[3] );   
                break;
            
        }
    }
    
    @SuppressWarnings("empty-statement")
    public void chanList ( User user, String[] cmd ) {
        //:Pintuz PRIVMSG ChanServ@services.avade.net :chanlist <nick>
        //  0      1        2                          3         4      5          = 6
        NickInfo ni;
        ArrayList<String> lists = new ArrayList<>();
        lists.add ( "Founder" );
        lists.add ( "Sop" );
        lists.add ( "Aop" );
        
        if ( cmd.length == 5 && user.isAtleast ( CSOP ) ) {
            if  ( ( ni = NickServ.findNick ( cmd[4] ) ) != null ) {
                lists.add ( "AKick" );
            } else {
                this.service.sendMsg ( user, "Nick not registered." );
                return;
            }
        } else if ( ( ni = NickServ.findNick ( user.getHash() ) ) == null || ! user.isIdented ( ni ) ) {
            this.service.sendMsg ( user, "Access denied." );
            return;
        }
  
        this.service.sendMsg ( user, "*** ChanList for: "+ni.getName()+" ***" );
        
        for ( String str : lists ) {
            int list = str.toUpperCase().hashCode();

            if ( ni.getChanAccess(list).size() > 0 ) {
                if ( list == AKICK ) {
                    this.service.sendMsg ( user, " " );
                    this.service.sendMsg ( user, "--- IRCop ---" );
                }
                this.service.sendMsg ( user, str+":" );
                for ( ChanInfo ci : ni.getChanAccess(list) ) {
                    this.service.sendMsg ( user, "  - "+ci.getName() );
                }
            }
        }
        this.service.sendMsg ( user, "*** End of List ***" );

    }
     
    public void op ( User user, String[] cmd )  {
        //:Pintuz PRIVMSG ChanServ@services.avade.net :op #avade dreamhealer
        //  0      1        2                          3    4      5          = 6 
        
        CmdData cmdData = this.validateCommandData ( user, OP, cmd );

        switch ( cmdData.getStatus ( ) ) {
            case SYNTAX_ERROR :
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "OP <#Chan> [<nick>]" ) ); 
                return;
                
            case CHAN_NOT_EXIST :
                this.service.sendMsg ( user, output ( CHAN_NOT_EXIST, cmdData.getString1 ( ) ) ); 
                return;
                
            case CHAN_NOT_REGISTERED :
                this.service.sendMsg ( user, output ( CHAN_NOT_REGISTERED, cmdData.getChan().getString ( NAME ) ) ); 
                return;
                
            case CHAN_IS_CLOSED : 
                this.service.sendMsg ( user, output ( CHAN_IS_CLOSED, cmdData.getChanInfo().getName ( ) ) ); 
                return;
                
            case CHAN_IS_FROZEN : 
                this.service.sendMsg ( user, output ( CHAN_IS_FROZEN, cmdData.getChanInfo().getName ( ) ) ); 
                return;
                
            case ACCESS_DENIED : 
                this.service.sendMsg ( user, output ( ACCESS_DENIED, "" ) ); 
                return;
                
            case NICK_NOT_PRESENT :
                this.service.sendMsg ( user, output ( NICK_NOT_PRESENT, cmdData.getString1 ( ) ) ); 
                return;
                  
            default :
        
        }
        Chan c = cmdData.getChan ( );
        ChanInfo ci = cmdData.getChanInfo ( );
        NickInfo ni = cmdData.getNick ( );
        User target = cmdData.getTarget ( );
        NickInfo tNick;

        /* Lets execute */
        if ( target == null )  {
            target = user;
        }
        int hashCode = target.getHash ( );
        if ( ni.getHashName ( ) == hashCode || user.getHash ( ) == hashCode )  {
            /* oping himself */
            this.service.sendMsg ( user, output ( NICK_OP, target.getString ( NAME ) , ci.getName ( ) ) ); 
            Handler.getChanServ ( ) .opUser (c, target );

        } else {
            /* oping someone else */
            if ( ci.getSettings().is ( IDENT ) ) {
                /* ident is on */
                if ( ( tNick = ci.getNickByUser ( target ) ) == null )  {
                    /* no access nick */
                    this.service.sendMsg ( user, output (NICK_NOT_IDENTED_OP, target.getString ( NAME ) ) );

                } else if ( tNick.getSettings().is ( NEVEROP ) ) {
                    /* never wants op */
                    this.service.sendMsg (user, output (NICK_NEVEROP, target.getString ( NAME )  )  );

                } else {
                    if ( ci.getSettings().is ( VERBOSE ) ) {
                        this.service.sendOpMsg (ci, output (NICK_VERBOSE_OP, user.getString ( NAME ), target.getString ( NAME ), ci.getName ( ) ) );
                    }
                    this.service.sendMsg (user, output (NICK_OP, target.getString ( NAME ), ci.getName ( ) ) );
                    Handler.getChanServ().opUser (c, target );
                }
            } else {
                /* ident is off */
                if ( ci.getSettings().is ( VERBOSE )  )  {
                    this.service.sendOpMsg (ci, output (NICK_NEVEROP, user.getString ( NAME ), target.getString ( NAME ), ci.getName ( ) ) );
                }
                this.service.sendMsg (user, output (NICK_OP, target.getString ( NAME ), ci.getName ( ) ) );
                Handler.getChanServ().opUser (c, target );
            }
        } 
        
    }
    public void deop ( User user, String[] cmd )  {
        //:Pintuz PRIVMSG ChanServ@services.avade.net :op #avade dreamhealer
        //  0      1        2                          3    4      5          = 6
  
        CmdData cmdData = this.validateCommandData ( user, DEOP, cmd );

        switch ( cmdData.getStatus ( ) ) {
            
            case SYNTAX_ERROR :
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "DEOP <#Chan> [<nick>]" ) ); 
                return;
                
            case CHAN_NOT_EXIST :
                this.service.sendMsg ( user, output ( CHAN_NOT_EXIST, cmdData.getString1 ( ) ) ); 
                return;
                
            case CHAN_NOT_REGISTERED :
                this.service.sendMsg ( user, output ( CHAN_NOT_REGISTERED, cmdData.getChan ( ) .getString ( NAME ) ) ); 
                return;
                
            case CHAN_IS_CLOSED : 
                this.service.sendMsg ( user, output ( CHAN_IS_CLOSED, cmdData.getChanInfo ( ).getName ( ) ) ); 
                return;
                
            case CHAN_IS_FROZEN : 
                this.service.sendMsg ( user, output ( CHAN_IS_FROZEN, cmdData.getChanInfo ( ).getName ( ) ) ); 
                return;
                
            case ACCESS_DENIED : 
                this.service.sendMsg ( user, output ( ACCESS_DENIED, "" ) ); 
                return;
                
            case NICK_NOT_PRESENT :
                this.service.sendMsg ( user, output ( NICK_NOT_PRESENT, cmdData.getString1 ( ) ) ); 
                return;
                              
            default :
                
        }
        Chan c = cmdData.getChan ( );
        ChanInfo ci = cmdData.getChanInfo ( );
        NickInfo ni = cmdData.getNick ( );
        User target = cmdData.getTarget ( );
        /* Lets execute */
        if ( target == null )  {
            target = user;
        }
        int hashCode = target.getHash ( );
        if ( ni.getHashName ( )  == hashCode || user.getHash ( )  == hashCode )  {
            /* oping himself */
            Handler.getChanServ().deOpUser ( c, user );

        } else {
            /* oping someone else */ 

            if ( ci.getSettings().is ( VERBOSE )  )  {
                this.service.sendOpMsg ( ci, output ( NICK_VERBOSE_DEOP, user.getString ( NAME ), target.getString ( NAME ), ci.getName ( ) ) );
            }
            this.service.sendMsg ( user, output ( NICK_DEOP, target.getString ( NAME ), ci.getName ( ) ) );
            Handler.getChanServ().deOpUser ( c, target );
        } 
        
    }
 
    
    
    public void help ( User user )  {
        this.service.sendMsg ( user, output ( CMD_NOT_FOUND_ERROR, "" )  );
        this.service.sendMsg ( user, output ( SHOW_HELP, this.service.getName ( )  )  );
    }

    public void register ( User user, String[] cmd )  { /* DONE? */
        // :DreamHea1er PRIVMSG ChanServ@services.sshd.biz :register chan pass description
        //       0         1              2                     3      4    5      6        = 7
         
        int result;
        CmdData cmdData = this.validateCommandData ( user, REGISTER, cmd );
  
        switch ( cmdData.getStatus ( ) ) {
            case SYNTAX_ERROR :
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "REGISTER <#Chan> <password> <description>" ) ); 
                return;
                
            case NICK_NOT_REGISTERED :
                this.service.sendMsg ( user, output ( NICK_NOT_REGISTERED, cmdData.getUser ( ).getString ( NAME ) ) ); 
                return;
                
            case CHAN_NOT_EXIST :
                this.service.sendMsg ( user, output ( CHAN_NOT_EXIST, cmdData.getString1 ( ) ) ); 
                return;
                
            case CHAN_ALREADY_REGGED : 
                this.service.sendMsg ( user, output ( CHAN_ALREADY_REGGED, cmdData.getChanInfo ( ).getName ( ) ) ); 
                return;
            
            case USER_NOT_OP :
                this.service.sendMsg ( user, output ( USER_NOT_OP, cmdData.getUser ( ).getString ( NAME ) ) ); 
                return;
                              
            default :
                
        }
        Chan c = cmdData.getChan ( );
        ChanInfo ci = cmdData.getChanInfo ( );
        NickInfo ni = cmdData.getNick ( );
        String description = Handler.cutArrayIntoString ( cmd, 6 );
        Topic topic; 
        
        if ( c.getTopic() != null ) {
            topic = c.getTopic();
        } else {
            topic = new Topic ("", ni.getName(), System.currentTimeMillis());
        }
        
        
        ci = new ChanInfo ( c.getString(NAME), ni, cmd[5], description, topic );
        ChanServ.addToWorkList ( REGISTER, ci );
        ChanServ.addChan ( ci );
        
        ci.getSettings().setModeLock("+nt");
        ci.getChanges().change ( MODELOCK );
        ci.getSettings().set ( TOPICLOCK, OFF );
        ci.getChanges().change ( TOPICLOCK );
        ci.getSettings().set ( IDENT, ON );
        ci.getChanges().change ( IDENT );
        ci.getSettings().set ( OPGUARD, ON );
        ci.getChanges().change ( OPGUARD );
        ci.getChanges().change ( TOPIC );
        ci.setChanFlag( new CSFlag ( ci.getName() ) );
        
        ChanServ.addToWorkList ( CHANGE, ci );
        
        
        
        CSLogEvent log = new CSLogEvent ( ci.getName(), REGISTER, ci.getFounder().getString ( FULLMASK ), ci.getFounder().getName() );
        ChanServ.addLog ( log );

        CSAccessLogEvent csaLog = new CSAccessLogEvent ( ci.getName(), FOUNDER, "", ci.getFounder().getString ( FULLMASK ) );
        ChanServ.addAccessLog ( csaLog );
        
        this.service.sendMsg ( user, output ( REGISTER_DONE, ci.getString ( NAME )  )  );
        this.service.sendMsg ( user, f.b ( ) +output ( REGISTER_SEC, "" ) +f.b ( )  );
        user.getSID().add ( ci ); /* identified to the channel */
        this.snoop.msg ( true, ci.getName ( ) , user, cmd );
    }

    public void identify ( User user, String[] cmd )  {
        // :DreamHea1er PRIVMSG NickServ@services.sshd.biz :identify #chan moew   = 6
        //       0         1               2                    3      4     5
        CmdData cmdData = this.validateCommandData ( user, IDENTIFY, cmd );
        
        switch ( cmdData.getStatus ( ) ) {
            case SYNTAX_ERROR :
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "IDENTIFY <#Chan> <password>" ) ); 
                return;
                
            case CHAN_NOT_REGISTERED :
                this.service.sendMsg ( user, output ( CHAN_NOT_REGISTERED, cmdData.getString1 ( ) ) ); 
                return;
                
            case CHAN_IS_FROZEN :
                this.service.sendMsg ( user, output ( CHAN_IS_FROZEN, cmdData.getChanInfo ( ).getName ( ) ) ); 
                return;
            
            case CHAN_IS_CLOSED :
                this.service.sendMsg ( user, output ( CHAN_IS_CLOSED, cmdData.getChanInfo ( ).getName ( ) ) ); 
                return;
            
            case INVALID_PASSWORD :
                this.service.sendMsg ( user, output ( INVALID_PASSWORD, "" ) ); 
                return; 
                
            default :
                
        }
        ChanInfo ci = cmdData.getChanInfo ( );
        this.service.sendMsg ( user, output ( PASSWD_ACCEPTED, ci.getString ( NAME )  )  ); 
        user.getSID().add ( ci );
          
    } 
 
    private void drop ( User user, String[] cmd ) {
        // :DreamHea1er PRIVMSG NickServ@services.sshd.biz :drop #chan pass   = 6
        //       0         1               2                 3      4     5        
    
        CmdData cmdData = this.validateCommandData ( user, DROP, cmd );
        
        switch ( cmdData.getStatus ( ) ) {
            case SYNTAX_ERROR :
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "DROP <#Chan> <password>" ) ); 
                return;
                
            case CHAN_NOT_REGISTERED :
                this.service.sendMsg ( user, output ( CHAN_NOT_REGISTERED, cmdData.getString1 ( ) ) ); 
                return;
                
            case CHAN_IS_FROZEN :
                this.service.sendMsg ( user, output ( CHAN_IS_FROZEN, cmdData.getChanInfo ( ).getName ( ) ) ); 
                return;
            
            case CHAN_IS_CLOSED :
                this.service.sendMsg ( user, output ( CHAN_IS_CLOSED, cmdData.getChanInfo ( ).getName ( ) ) ); 
                return;
            
            case INVALID_PASSWORD :
                this.service.sendMsg ( user, output ( INVALID_PASSWORD, "" ) ); 
                return; 
            
            case IS_MARKED :
                this.service.sendMsg ( user, output ( IS_MARKED, cmdData.getChanInfo().getName ( ) ) ); 
                return;     
            
            default :
                
        }
        ChanInfo ci = cmdData.getChanInfo ( );
        Handler.getChanServ().dropChan ( ci );
        this.service.sendMsg ( user, output ( CHANNELDROPPED, ci.getString ( NAME ) ) );
        CSLogEvent log = new CSLogEvent ( ci.getName(), DROP, user.getFullMask(), "" );
        ChanServ.addLog ( log );
        
    }

    
    private void delete ( User user, String[] cmd ) {
        // :DreamHea1er PRIVMSG NickServ@services.sshd.biz :delete #chan   = 5
        //       0         1               2                 3      4           
      
        CmdData cmdData = this.validateCommandData ( user, DELETE, cmd );
        
        switch ( cmdData.getStatus ( ) ) {
            case SYNTAX_ERROR :
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "DELETE <#chan>" )  );
                return;
                
            case ACCESS_DENIED :
                this.service.sendMsg ( user, output ( ACCESS_DENIED, cmdData.getString1 ( ) ) ); 
                return;     
                           
            case CHAN_NOT_REGISTERED :
                this.service.sendMsg ( user, output ( CHAN_NOT_REGISTERED, cmd[4] ) );
                return;
                       
            case IS_MARKED :
                this.service.sendMsg ( user, output ( IS_MARKED, cmd[4] ) );
                return;
             
            default :
                
        }
        ChanInfo ci = cmdData.getChanInfo ( );
        Handler.getChanServ().dropChan ( ci );
        this.service.sendMsg ( user, output ( CHANNELDELETED, ci.getString ( NAME ) ) );
        CSLogEvent log = new CSLogEvent ( ci.getName(), DELETE, user.getFullMask(), user.getOper().getName() );
        ChanServ.addLog ( log );
    }

    
    
    private void info ( User user, String[] cmd ) {
        // :DreamHea1er PRIVMSG NickServ@services.sshd.biz :info dreamhealer
        //      0          1            2                     3       4     = 5
        
        CmdData cmdData = this.validateCommandData ( user, INFO, cmd );

        switch ( cmdData.getStatus ( ) ) {
        
            case SYNTAX_ERROR :
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "INFO <#Chan>" ) ); 
                return;
            
            case CHAN_NOT_REGISTERED :
                this.service.sendMsg ( user, output ( CHAN_NOT_REGISTERED, cmdData.getString1 ( ) ) ); 
                return;
                    
            case CHAN_IS_FROZEN :
                this.service.sendMsg ( user, output ( CHAN_IS_FROZEN, cmdData.getChanInfo ( ).getName ( ) ) ); 
                return;
                       
            case CHAN_IS_CLOSED :
                this.service.sendMsg ( user, output ( CHAN_IS_CLOSED, cmdData.getChanInfo ( ).getName ( ) ) ); 
                return;
            
            default :
                
        }
        /* We found the nickname */
        ChanInfo ci = cmdData.getChanInfo ( );
        NickInfo founder = ci.getFounder ( ); 
        if ( founder == null )
            System.out.println("founder = null");
        
        this.showStart ( true, user, ci, f.b ( ) +"Info for: "+f.b ( )  ); 
     
        this.service.sendMsg ( user, "     Founder: "+founder.getString ( NAME ) +" ("+founder.getString ( USER )+"@"+founder.getString ( HOST )+") " );
        this.service.sendMsg ( user, "   Mode Lock: "+ci.getSettings().getModeLock().getModes ( ) );
        this.service.sendMsg ( user, "       Topic: "+ci.getString ( TOPIC )+" ("+ci.getString(TOPICNICK)+")");
        this.service.sendMsg ( user, " Description: "+ci.getString ( DESCRIPTION ) );
        this.service.sendMsg ( user, "    Settings: "+ci.getSettings().getInfoStr ( ) );
        this.service.sendMsg ( user, "  Registered: "+ci.getString ( REGTIME ) );
        this.service.sendMsg ( user, "    Lastseen: "+ci.getString ( LASTSEEN ) );
        this.service.sendMsg ( user, "    Time now: "+dateFormat.format ( new Date ( ) ) );
  
        if ( user.isAtleast ( IRCOP ) ) {
            if ( ci.is ( FROZEN ) || ci.is ( MARKED ) || ci.is ( HELD ) || ci.is ( CLOSED ) || ci.is ( AUDITORIUM ) ) 
                this.service.sendMsg ( user, f.b()+"   --- IRCop ---" );
            if ( ci.is ( FROZEN ) )
                this.service.sendMsg ( user, f.b()+"      FROZEN: "+ci.getSettings().getInstater ( FREEZE ) );
            if ( ci.is ( MARKED ) )
                this.service.sendMsg ( user, f.b()+"      MARKED: "+ci.getSettings().getInstater ( MARK ) );
            if ( ci.is ( CLOSE ) )
                this.service.sendMsg ( user, f.b()+"      CLOSED: "+ci.getSettings().getInstater ( CLOSE ) );
            if ( ci.is ( HELD ) )
                this.service.sendMsg ( user, f.b()+"        HELD: "+ci.getSettings().getInstater ( HOLD ) );
            if ( ci.is ( AUDITORIUM ) ) 
                this.service.sendMsg ( user, f.b()+"  AUDITORIUM: "+ci.getSettings().getInstater ( AUDITORIUM ) );
        }
        
        this.showEnd ( user, "Info" );

    
    }

    private void access ( int access, User user, String[] cmd ) {
        switch ( cmd[5].toUpperCase().hashCode ( ) ) {
            case LIST :
                doListAccess ( user, cmd, access );
                break;
                
            case WIPE :
                doWipeAccess ( user, cmd, access );
                break;
                
            default:
                doAccess ( access, user, cmd );
            
        } 
    }
     
    public void doWipeAccess ( User user, String[] cmd, int access )  {
        //:Pintuz PRIVMSG ChanServ@services.avade.net :aop #avade list
        //   0       1        2                         3    4     5   = 6
        ChanInfo ci;
        NickInfo ni;
        
        if ( ! CSDatabase.checkConn ( )  )  {
            Handler.getChanServ ( ) .sendMsg ( user, "Database offline. Please try again in a little while." );
            return;
        }
        
        if ( cmd.length < 6 || ! ( access == AOP || access == SOP || access == AKICK )  )  {
            /* too short or wrong*/
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "AKICK <#chan> <ADD|DEL|LIST> [<nick|#NUM>]" )  );
        } 
        
        ci = ChanServ.findChan ( cmd[4] );
        ni = ci.getNickByUser ( user );
       
        if ( ni == null )  {
            /* no nick with access */
            this.service.sendMsg ( user, output ( ACCESS_DENIED, "NickServ" ) );

        } else if ( ci == null )  {
            /* no channel */
            this.service.sendMsg ( user, output ( CHAN_NOT_REGISTERED, cmd[4] ) );
        
        } else if ( ! ci.isFounder ( ni )  )  {
            /* does not have access */
            this.service.sendMsg ( user, output ( ACCESS_DENIED, ci.getName ( ) ) );

        } else {
            String acc = null;
            acc = accessToString ( access );
            
            if ( CSDatabase.wipeAccessList ( ci, access )  )  {
                ci.wipeAccessList ( access );
                this.service.sendMsg ( user, output ( LIST_WIPED, acc, ci.getName ( ) ) );
                this.service.sendOpMsg ( ci, output ( LIST_VERBOSE_WIPED, ni.getName ( ), acc, ci.getName ( ) ) );
                this.snoop.msg ( true, ci.getName ( ), user, cmd );

            } else {
                this.service.sendMsg ( user, output ( LIST_NOT_WIPED, acc, ci.getName ( ) ) );

            }
        }
    }
    
    public void doListAccess ( User user, String[] cmd, int access )  {
        //:Pintuz PRIVMSG ChanServ@services.avade.net :aop #avade list
        //   0       1        2                         3    4     5   = 6
        ChanInfo ci;
        NickInfo ni;
        
        if ( cmd.length < 6 || ! ( access == AOP || access == SOP || access == AKICK )  )  {
            /* too short */
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "SOP <#chan> <ADD|DEL|LIST> [<nick|#NUM>]" )  );

        } else { 
            ci = ChanServ.findChan ( cmd[4] );
            ni = ci.getNickByUser ( user );

            if ( ni == null )  {
                /* no nick with access */
                this.service.sendMsg ( user, output ( ACCESS_DENIED, "NickServ" )  );

            } else if ( ci == null )  {
                /* no channel */
                this.service.sendMsg ( user, output ( CHAN_NOT_REGISTERED, cmd[4] )  );

            } else if ( ! ci.isFounder ( ni )  && ! ci.isAccess ( SOP, ni )  && ! ci.isAccess ( AOP, ni )  )  {
                /* does not have access */
                this.service.sendMsg ( user, output ( ACCESS_DENIED, ci.getName ( )  )  );

            } else {
                /* lets show the list */
                String accStr = new String ( );
                accStr = accessToString ( access );

                this.showStart ( true, user, ci, f.b ( ) +accStr+" list for: "+f.b ( )  ); 
                ArrayList<CSAcc> list = ci.getAccessList ( access );
                 
                int j = 0;
                for ( CSAcc acc : list )  {     
                    if ( acc.getNick ( ) != null )  {
                        this.service.sendMsg ( user,  " - "+acc.getNick().getString ( NAME )+" ("+acc.getNick ( ) .getString ( FULLMASK ) +") (nick)" );
                    } else {
                        this.service.sendMsg ( user,  " - "+acc.getMask ( )+" (mask)" );
                    }
                }
                this.showEnd ( user, accStr+" list" );
            }
        } 
    }
    private String getListName ( int access ) {
        switch ( access ) {
            case SOP :
                return "Sop";
            case AOP : 
                return "Aop";
            case AKICK : 
                return "AKick";
            default :
                return "";
        }
    }    
    private int getAddList ( int access ) {
        switch ( access ) {
            case SOP :
                return ADDSOP;
            case AOP : 
                return ADDAOP;
            case AKICK : 
                return ADDAKICK;
            default :
                return 0;
        }
    }
    private int getDelList ( int access ) {
        switch ( access ) {
            case SOP :
                return DELSOP;
            case AOP : 
                return DELAOP;
            case AKICK : 
                return DELAKICK;
            default :
                return 0;
        }
    }
    private void doAccess ( int access, User user, String[] cmd ) {
        //:Pintuz PRIVMSG ChanServ@services.avade.net :akick #avade add <nick|fullmask>
        //:DreamH PRIVMSG ChanServ@services.avade.net :akick #friends del 2
        //   0       1        2                         3     4       5   6    = 7
  
        CmdData cmdData = this.validateCommandData ( user, access, cmd );
        String listName = getListName ( access );
        
        switch ( cmdData.getStatus ( ) ) {
            case SYNTAX_ERROR :
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, listName+" <#Chan> <add|del|list> <nick|mask|#num>" ) ); 
                return;
                 
            case NICK_NOT_REGISTERED :
                this.service.sendMsg ( user, output ( NICK_NOT_REGISTERED, cmdData.getString1 ( ) ) ); 
                return;                 
            
            case CHAN_NOT_REGISTERED :
                this.service.sendMsg ( user, output ( CHAN_NOT_REGISTERED, cmdData.getChan().getString ( NAME ) ) ); 
                return;
                
            case CHAN_IS_FROZEN : 
                this.service.sendMsg ( user, output ( CHAN_IS_FROZEN, cmdData.getChanInfo().getName ( ) ) ); 
                return;

            case CHAN_IS_CLOSED : 
                this.service.sendMsg ( user, output ( CHAN_IS_CLOSED, cmdData.getChanInfo().getName() ) ); 
                return;
                
            case ACCESS_DENIED :
                this.service.sendMsg ( user, output ( ACCESS_DENIED, cmdData.getString1() ) ); 
                return;
                
            case NOT_ENOUGH_ACCESS :
                this.service.sendMsg ( user, output ( NOT_ENOUGH_ACCESS, "" ) ); 
                return;
                    
            case XOP_NOT_FOUND :
                this.service.sendMsg ( user, output ( XOP_NOT_FOUND, "" ) ); 
                return;
                   
            case XOP_ADD_FAIL :
                this.service.sendMsg ( user, output ( XOP_ADD_FAIL, "" ) ); 
                return;
                     
            case XOP_ALREADY_PRESENT :
                this.service.sendMsg ( user, output ( XOP_ALREADY_PRESENT, cmdData.getString1(), this.getListStr ( access ) ) ); 
                return;
            
            default :
                
        }
       
        ChanInfo ci = cmdData.getChanInfo();
        Chan c = Handler.findChan(ci.getName());
        NickInfo ni = cmdData.getNick();
        NickInfo ni2;
        String what;
        String mask = null;
        int command = cmdData.getCommand();
        int subcommand = cmdData.getSubCommand();
        CSAcc acc = cmdData.getAcc();
         
        if ( ( ni2 = cmdData.getNick2() ) == null ) {
            mask = cmdData.getString1(); 
        } 
        
        if ( acc != null && acc.getNick() != null ) {
            what = acc.getNick().getName();
        } else if ( acc != null && acc.getMask() != null ) {
            what = acc.getMask();
        } else {
            what = "-";
        }
        
        
        switch ( subcommand ) {
            case ADD :
                ci.removeFromAll ( acc );
                ci.addAccess ( command, acc );
                ci.addAccessLog ( new CSAccessLogEvent ( ci.getName(), this.getAddList(access), what, user ) );
                this.service.sendMsg ( user, output ( NICK_ADDED, what, listName ) );
                if ( ci.getSettings().is ( VERBOSE ) ) {
                    this.service.sendOpMsg ( ci, output ( NICK_VERBOSE_ADDED, ni.getString ( NAME ), what, listName ) );
                }
                if ( command == AKICK ) {
                    c.addCheckUsers();
                }
                ci.changed();
                break;
                
            case DEL :
                ci.delAccess ( access, acc );
                ci.addAccessLog ( new CSAccessLogEvent ( ci.getName(), this.getDelList(access), what, user ) );
                this.service.sendMsg ( user, output ( NICK_DELETED, what, listName ) );
                if ( ci.getSettings().is ( VERBOSE ) ) {
                    this.service.sendOpMsg ( ci, output ( NICK_VERBOSE_DELETED, ni.getString ( NAME ), what, listName ) );
                }
                ci.changed();
                break;
                
            default :
                
        }
        
    }
    
    private void unban ( User user, String[] cmd )  {
        //:Pintuz PRIVMSG ChanServ@services.avade.net :unban #avade fredde
        //   0       1        2                         3     4      5     = 6 
         
        CmdData cmdData = this.validateCommandData ( user, UNBAN, cmd );
 
        switch ( cmdData.getStatus ( ) ) {
            case SYNTAX_ERROR :
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "INFO <#Chan>" ) ); 
                return;
          
            case CHAN_NOT_EXIST :
                this.service.sendMsg ( user, output ( CHAN_NOT_EXIST, cmdData.getChan ( ).getString ( NAME ) ) ); 
                return;          
            
            case CHAN_NOT_REGISTERED :
                this.service.sendMsg ( user, output ( CHAN_NOT_REGISTERED, cmdData.getString1 ( ) ) ); 
                return;
          
            case NICK_NOT_EXIST :
                this.service.sendMsg ( user, output ( NICK_NOT_EXIST, cmdData.getString1 ( ) ) ); 
                return;
                
            case NICK_ACCESS_DENIED :
                this.service.sendMsg ( user, output ( NICK_ACCESS_DENIED, cmdData.getChanInfo ( ).getName ( ) ) ); 
                return;
                    
            case CHAN_IS_FROZEN :
                this.service.sendMsg ( user, output ( CHAN_IS_FROZEN, cmdData.getChanInfo ( ).getName ( ) ) ); 
                return;
                       
            case CHAN_IS_CLOSED :
                this.service.sendMsg ( user, output ( CHAN_IS_CLOSED, cmdData.getChanInfo ( ).getName ( ) ) ); 
                return;
                
            default :
                
        }
        Chan c = cmdData.getChan ( );
        ChanInfo ci = cmdData.getChanInfo ( );
        NickInfo ni = cmdData.getNick ( );
        User target = cmdData.getTarget ( );

        if ( ci.getSettings().is ( VERBOSE ) ) {
            this.service.sendOpMsg ( ci, output ( NICK_VERBOSE_ADDED, ni.getString ( NAME ) , target.getString ( NAME ) , "UnBan" )  );
        } 
        Handler.getChanServ().unBanUser ( c, target );
    }

    private void invite ( User user, String[] cmd )  {
        //:Pintuz PRIVMSG ChanServ@services.avade.net :unban #avade
        //   0       1        2                         3     4        = 5
        
        CmdData cmdData = this.validateCommandData ( user, INVITE, cmd );

        switch ( cmdData.getStatus ( ) ) {
            case SYNTAX_ERROR :
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "INVITE <#Chan>" ) ); 
                return;
          
            case CHAN_NOT_EXIST :
                this.service.sendMsg ( user, output ( CHAN_NOT_EXIST, cmdData.getChan ( ).getString ( NAME ) ) ); 
                return;          
            
            case CHAN_NOT_REGISTERED :
                this.service.sendMsg ( user, output ( CHAN_NOT_REGISTERED, cmdData.getString1 ( ) ) ); 
                return;
          
            case NICK_ACCESS_DENIED :
                this.service.sendMsg ( user, output ( NICK_ACCESS_DENIED, cmdData.getChanInfo ( ).getName ( ) ) ); 
                return;
                    
            case CHAN_IS_FROZEN :
                this.service.sendMsg ( user, output ( CHAN_IS_FROZEN, cmdData.getChanInfo ( ).getName ( ) ) ); 
                return;
                       
            case CHAN_IS_CLOSED :
                this.service.sendMsg ( user, output ( CHAN_IS_CLOSED, cmdData.getChanInfo ( ).getName ( ) ) ); 
                return;
                
            default :
                
        }
        Chan c = cmdData.getChan ( );
        ChanInfo ci = cmdData.getChanInfo ( );
        NickInfo ni = cmdData.getNick ( ); 
        if ( ci.getSettings().is ( VERBOSE ) ) {
            this.service.sendOpMsg ( ci, output ( NICK_VERBOSE_ADDED, ni.getString ( NAME ), ci.getName ( ), "invite" )  );
        } 
        Handler.getChanServ().invite ( c, user );        
    }
     
    /* Tells a channel staff how a user has access to a channel */
    private void why ( User user, String[] cmd )  {
        //:Pintuz PRIVMSG ChanServ@services.avade.net :why #avade Pintuz  
        //   0       1        2                         3     4     5     = 6 
  
        CmdData cmdData = this.validateCommandData ( user, WHY, cmd );

        switch ( cmdData.getStatus ( ) ) {
            case SYNTAX_ERROR :
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "WHY <#Chan> <nick>" ) ); 
                return;
                 
            case CHAN_NOT_REGISTERED :
                this.service.sendMsg ( user, output ( CHAN_NOT_REGISTERED, cmdData.getChan ( ) .getString ( NAME ) ) ); 
                return;
                
            case NICK_NOT_EXIST : 
                this.service.sendMsg ( user, output ( NICK_NOT_EXIST, cmdData.getString1 ( ) ) ); 
                return;
                
            case ACCESS_DENIED :
                this.service.sendMsg ( user, output ( ACCESS_DENIED, "" ) ); 
                return;
                
            default : 
                
        }
        ChanInfo ci = cmdData.getChanInfo ( );
        User target = cmdData.getTarget ( );
        String access = ci.getAccessByUser ( target );
        String aNick = ci.getAccessHolderByUser ( target );

        if ( aNick == null )  {
            this.service.sendMsg ( user, "User "+target.getString ( NAME ) +" has no access to channel "+ci.getName ( )  );
        } else {
            this.service.sendMsg ( user, "User "+target.getString ( NAME ) +" has "+access+" access to channel "+ci.getName ( ) +" through the nickname "+aNick );
        }
    }
      
    /**
     *
     * @param user
     * @param cmd
     */
    public void set ( User user, String[] cmd )  {
        /* :DreamHealer PRIVMSG NickServ@services.sshd.biz :set #chan CMD ON          */
        /*      0          1               2                 3     4   5   6 = 7      */

        CmdData cmdData = this.validateCommandData ( user, SET, cmd ); 
        
        switch ( cmdData.getStatus ( ) ) {
            case SYNTAX_ERROR :
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "SET <#Chan> <option> <on|off>" ) ); 
                return;
                 
            case NICK_NOT_REGISTERED :
                this.service.sendMsg ( user, output ( NICK_NOT_REGISTERED, cmdData.getString1 ( ) ) ); 
                return;                 
            
            case CHAN_NOT_REGISTERED :
                this.service.sendMsg ( user, output ( CHAN_NOT_REGISTERED, cmdData.getChan ( ) .getString ( NAME ) ) ); 
                return;
                
            case CHAN_IS_FROZEN : 
                this.service.sendMsg ( user, output ( CHAN_IS_FROZEN, cmdData.getChanInfo ( ).getName ( ) ) ); 
                return;

            case CHAN_IS_CLOSED : 
                this.service.sendMsg ( user, output ( CHAN_IS_CLOSED, cmdData.getChanInfo ( ).getName ( ) ) ); 
                return;
                
            case ACCESS_DENIED :
                this.service.sendMsg ( user, output ( ACCESS_DENIED, cmdData.getString1 ( ) ) ); 
                return;
                
            default :
                
        }
        ChanInfo ci = cmdData.getChanInfo ( );
        boolean flag = false;
        int setting = 0;

        switch ( cmd[6].toUpperCase().hashCode ( ) ) {
            case ON :
                flag = true;
                break;

            case OFF :
                flag = false;
                setting = OFF;
                break;

            case AOP :
            case SOP : 
            case FOUNDER :
                setting = cmd[6].toUpperCase().hashCode ( );
                break;

            default :
        } 
        // chan  ( name, pass, desc, topic, regstamp, stamp ) 
        // chansetting  ( name,keeptopic,topiclock,ident,opguard,restrict,verbose,mailblock,leaveops,private ) 
        switch ( cmd[5].toUpperCase().hashCode ( ) ) {
            case DESCRIPTION :
                doDescription ( user, ci, cmd );
                ci.getChanges().change ( DESCRIPTION );
                ChanServ.addToWorkList ( CHANGE, ci );
                break;

            case TOPICLOCK :
                doTopicLock ( user, ci, setting );
                ci.getChanges().change ( TOPICLOCK );
                ChanServ.addToWorkList ( CHANGE, ci );
                break;
            
            case MODELOCK :
                doModeLock ( user, ci, cmd );
                ci.getChanges().change ( MODELOCK );
                ChanServ.addToWorkList ( CHANGE, ci );
                break;

            case KEEPTOPIC :
                this.sendWillOutput ( user, flag, "keep your topic if channel goes empty.", "forget the topic if the channel goes empty." );
                ci.getSettings().set ( KEEPTOPIC, flag );
                ci.getChanges().change ( KEEPTOPIC );
                ChanServ.addToWorkList ( CHANGE, ci );
                break;

            case IDENT :
                this.sendWillOutput ( user, flag, "require channel ops to identify to their nicks.", "require channel ops to identify to their nicks." );
                ci.getSettings().set ( IDENT, flag );
                ci.getChanges().change ( IDENT );
                ChanServ.addToWorkList ( CHANGE, ci );
                break;

            case OPGUARD :
                this.sendWillOutput ( user, flag, "guard channel ops.", "require ops to identify to their nicks." );
                ci.getSettings().set ( OPGUARD, flag );
                ci.getChanges().change ( OPGUARD );
                ChanServ.addToWorkList ( CHANGE, ci );
                break;

            case RESTRICT :
                this.sendWillOutput ( user, flag, "restrict users from entering the channel.", "restrict users from entering the channel." );
                ci.getSettings().set ( RESTRICT, flag );
                ci.getChanges().change ( RESTRICT );
                ChanServ.addToWorkList ( CHANGE, ci );
                break;

            case VERBOSE :
                this.sendWillOutput ( user, flag, "notify current ops of channel changes.", "notify current ops of channel changes." );
                ci.getSettings().set ( VERBOSE, flag );
                ci.getChanges().change ( VERBOSE );
                ChanServ.addToWorkList ( CHANGE, ci );
                break;

            case MAILBLOCK :
                this.sendWillOutput ( user, flag, "deny the channel password to be mailed to the founders email.", "deny the channel password to be mailed to the founders mail." );
                ci.getSettings().set ( MAILBLOCK, flag );
                ci.getChanges().change ( MAILBLOCK );
                ChanServ.addToWorkList ( CHANGE, ci );
                break;

            case LEAVEOPS :
                this.sendWillOutput ( user, flag, "leave ops ( @ )  to the first user entering the channel after its been empty.", "leave ops(@)." );
                ci.getSettings().set ( LEAVEOPS, flag );
                ci.getChanges().change ( LEAVEOPS );
                ChanServ.addToWorkList ( CHANGE, ci );
                break;
 
            default :
                this.service.sendMsg ( user, output ( SETTING_NOT_FOUND, cmd[4] ) );

        }
    }
    private void getPass ( User user, String[] cmd ) {
        CmdData cmdData = this.validateCommandData ( user, GETPASS, cmd );
        switch ( cmdData.getStatus ( ) ) {
            case SYNTAX_ERROR :
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "GETPASS <#chan>" )  );
                return;
                
            case ACCESS_DENIED :
                this.service.sendMsg ( user, output ( ACCESS_DENIED, cmdData.getString1 ( ) ) ); 
                return;     
                           
            case CHAN_NOT_REGISTERED :
                this.service.sendMsg ( user, output ( CHAN_NOT_REGISTERED, cmd[4] ) );
                return;
                       
            case IS_MARKED :
                this.service.sendMsg ( user, output ( IS_MARKED, cmd[4] ) );
                return;
             
            default : 
        
        }
        ChanInfo ci = cmdData.getChanInfo ( ); 
        NickInfo oper = user.getOper().getNick ( );
        int command = cmdData.getCommand ( );
        CSLogEvent log = new CSLogEvent ( ci.getName(), command, user.getFullMask(), oper.getName() );
        ChanServ.addLog ( log );
        this.service.sendMsg ( user, output ( CHAN_GETPASS, ci.getPass() ) );
        this.service.sendGlobOp ( oper.getName()+" used GETPASS on: "+ci.getName() );
    }
    
    private void changeFlag ( int flag, User user, String[] cmd )  {
        //:Pintuz PRIVMSG ChanServ@services.avade.net :freeze #avade  30d
        //   0       1         2                         3       4    5      6    = 7
        
        CmdData cmdData = this.validateCommandData ( user, flag, cmd ); 

        switch ( cmdData.getStatus ( ) ) {
            case SYNTAX_ERROR :
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, ChanSetting.hashToStr ( flag )+" <[-]chan>" ) ); 
                return;
                 
            case ACCESS_DENIED :
                return;                 
            
            case CHAN_NOT_REGISTERED :
                this.service.sendMsg ( user, output ( CHAN_NOT_REGISTERED, cmdData.getString1 ( ) ) ); 
                return;
                
            case CHANFLAG_EXIST :
                this.service.sendMsg ( user, output ( CHANFLAG_EXIST, cmdData.getChanInfo().getName ( ), cmdData.getString1 ( ) ) ); 
                return;     
                      
            case IS_MARKED :
                this.service.sendMsg ( user, output ( IS_MARKED, cmdData.getChanInfo().getName ( ) ) ); 
                return;     
            
            default :
        
        }
        NickInfo oper = user.getOper().getNick ( );
        ChanInfo ci = cmdData.getChanInfo ( );
        int command = cmdData.getCommand ( );
        CSLogEvent log;
        ChanInfo relay;
        NickInfo instater;
        
        System.err.println("DEBUG!!!!!: flag:"+flag+":"+ci.getSettings().modeString ( flag ));
        
        switch ( command ) {
            case UNAUDITORIUM :
                instater = NickServ.findNick ( ci.getSettings().getInstater ( flag ) );
                if ( ! user.isIdented ( instater ) && ! user.isAtleast ( SRA ) ) {
                    this.service.sendMsg ( user, "Error: flag can only be removed by: "+instater.getName()+" or a SRA+." );
                    return;
                }
                if ( ( relay = ChanServ.findChan ( ci.getName()+"-relay" ) ) != null ) {
                    Handler.getChanServ().dropChan ( relay );
                }
                this.service.sendMsg ( user, "Please note that the auditorium channel mode handles joins/parts differently than normal and will cause "+
                                             "users becoming out of sync. Its for that reason recommended to masskick the channel after removing the mode to sort the possible desync." );
                this.service.sendRaw( ":ChanServ MODE "+ci.getName()+" 0 :-A");
            case UNMARK :
            case UNFREEZE :
            case REOPEN :
            case UNHOLD :
                instater = NickServ.findNick ( ci.getSettings().getInstater ( flag ) );
                if ( ! user.isIdented ( instater ) && ! user.isAtleast ( SRA ) ) {
                    this.service.sendMsg ( user, "Error: flag can only be removed by: "+instater.getName()+" or a SRA+." );
                    return;
                }
                ci.getSettings().set ( flag, "" );
                ci.getChanges().change ( flag );
                ChanServ.addToWorkList ( CHANGE, ci );
                log = new CSLogEvent ( ci.getName(), command, user.getFullMask(), oper.getName() );
                ChanServ.addLog ( log );
                this.service.sendMsg ( user, output ( CHAN_SET_FLAG, ci.getName(), "Un"+ci.getSettings().modeString ( flag ) ) );
                this.service.sendGlobOp ( "Channel: "+ci.getName()+" has been Un"+ci.getSettings().modeString ( flag )+" by: "+oper.getName() );
                break;
                
            case AUDITORIUM :
                if ( ( relay = ChanServ.findChan ( ci.getName()+"-relay" ) ) != null ) {
                    this.service.sendMsg ( user, "Error: Relay channel: "+ci.getName()+"-relay is already registered." );
                    return;
                }
                Random rand = new Random ( );
                String pass = "R"+rand.nextInt(99999999);
                Topic topic = new Topic ( "Relay channel for "+ci.getName(), "ChanServ", System.currentTimeMillis() / 1000 );
                relay = new ChanInfo ( ci.getName()+"-relay", user.getOper().getNick(), pass, "Relay channel for "+ci.getName(), topic );
                ChanSetting settings = new ChanSetting ();
                settings.setModeLock("+spt-n");
                relay.setTopic ( topic );
                relay.setSettings ( settings );
                ChanServ.addToWorkList ( REGISTER, ci );
                ChanServ.addChan ( relay );
                this.service.sendMsg ( user, "Relay channel: "+relay.getName()+" has been registered to you. This is the channel " );
                this.service.sendMsg ( user, "where regular user chat will end up instead of the main channel. Keep this channel secret." );
                this.service.sendMsg ( user, "Relay chan password: "+pass );
                this.service.sendMsg ( user, " " );
                this.service.sendMsg ( user, "Please note that the auditorium channel mode handles joins/parts differently than normal and" );
                this.service.sendMsg ( user, "will cause users becoming out of sync. Its for that reason recommended to masskick the channel" );
                this.service.sendMsg ( user, "after removing the mode to sort the possible desync." );
                this.service.sendRaw( ":ChanServ MODE "+ci.getName()+" 0 :+A");
            case MARK :
            case FREEZE :
            case CLOSE :
            case HOLD :
                ci.getSettings().set ( flag, oper.getName() );
                ci.getChanges().change ( flag );
                ChanServ.addToWorkList ( CHANGE, ci );
                log = new CSLogEvent ( ci.getName(), command, user.getFullMask(), oper.getName() );
                ChanServ.addLog ( log );
                this.service.sendMsg ( user, output ( CHAN_SET_FLAG, ci.getName(), ci.getSettings().modeString ( flag ) )  );
                this.service.sendGlobOp ( "Channel: "+ci.getName()+" has been "+ci.getSettings().modeString ( flag )+" by: "+oper.getName() );
                break;
                
            default :  
                
        }
    }
      
    private void doDescription ( User user, ChanInfo ci, String[] cmd ) {
        String buf = Handler.cutArrayIntoString ( cmd, 5 );
        ci.setDescription ( buf );
    }
       
    private void doTopicLock ( User user, ChanInfo ci, int enable )  {
        switch ( enable )  {
            case OFF :
                this.sendWillOutput ( user, false, "", "lock the topic." );
                break;
                
            case AOP :
                this.sendWillOutput ( user, true, "lock your topic for AOP or higher only.", "" );
                break;
                
            case SOP :
                this.sendWillOutput ( user, true, "lock your topic for SOP or higher only.", "" );
                break;
                
            case FOUNDER :
                this.sendWillOutput ( user, true, "lock your topic for FOUNDER only.", "" );
                break;
                
            default :
                
        }
        ci.getSettings().set ( TOPICLOCK, enable ); 
    }

    
    private void doModeLock ( User user, ChanInfo ci, String[] cmd )  {
        Chan c = Handler.findChan(ci.getName());
        ci.getSettings().setModeLock ( cmd[6] );
        this.service.sendMsg ( user, output ( MODELOCK, ci.getName ( ), cmd[6] ) );
        if ( c != null ) {
            this.service.sendRaw( ":ChanServ MODE "+ci.getName()+" 0 :"+ci.getSettings().getModeLock().getMissingModes ( c, ci ) );
        }
    }
 
    private void sendIsOutput ( User user, boolean enable, String str )  {
        int flag = enable ? IS_NOW : IS_NOT;
        this.service.sendMsg ( user, output ( flag, str ) );
    }
    
    private void sendWillOutput ( User user, boolean enable, String will, String willNot )  {
        int flag = enable ? WILL_NOW : WILL_NOW_NOT;
        this.service.sendMsg ( user, output ( flag, will ) );
    }
    
    private void showStart ( boolean online, User user, ChanInfo ci, String str )  {
        this.service.sendMsg ( user, str+ci.getString ( NAME )  ); 
    }
   
    private void showEnd ( User user, String str ) { 
        this.service.sendMsg ( user, "*** End of "+str+" ***" );
    }
 
    private String accessToString ( int access )  {
        String acc;
        switch ( access )  {
            case AKICK :
                return "akick";
                 
            case SOP :
                return "sop";
                  
            case AOP :
                return "aop";
                 
            default :
                
        }
        return "";
    }
    
    public void mDeop ( User user, String[] cmd )  {
        //:Pintuz PRIVMSG ChanServ@services.avade.net :mdeop #avade 
        //  0      1        2                          3     4              = 5

        CmdData cmdData = this.validateCommandData ( user, MDEOP, cmd );
         
        switch ( cmdData.getStatus ( ) )  {
            case SYNTAX_ERROR :
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "MDEOP <#Chan>" ) ); 
                return;
                
            case CHAN_NOT_EXIST :
                this.service.sendMsg ( user, output ( CHAN_NOT_EXIST, cmdData.getString1 ( ) ) ); 
                return;
                
            case CHAN_NOT_REGISTERED :
                this.service.sendMsg ( user, output ( CHAN_NOT_REGISTERED, cmdData.getChan ( ) .getString ( NAME ) ) ); 
                return;
                
            case ACCESS_DENIED :
                this.service.sendMsg ( user, output ( ACCESS_DENIED, "" ) ); 
                return;
                
            case NOT_ENOUGH_ACCESS :
                this.service.sendMsg ( user, output ( NOT_ENOUGH_ACCESS, cmdData.getString1 ( ) ) ); 
                return;
                
            default : 
                
        }
        Chan c = cmdData.getChan ( );
        ChanInfo ci = cmdData.getChanInfo ( );
        NickInfo ni;
        boolean isOper = false;
        if ( ( ni = cmdData.getOper ( ) ) == null ) {
            ni = cmdData.getNick ( );
        } else {
            isOper = true;
        }

        if  ( ci != null && ci.getSettings().is ( VERBOSE )  )  {
            this.service.sendOpMsg ( ci, output ( NICK_MDEOP_CHAN, ni.getString ( NAME ), ci.getName ( ) ) );
        }                                      
        this.service.sendMsg ( user, output ( NICK_MDEOP, c.getString ( NAME )  )  );
        this.snoop.msg ( true, cmd[4]+" ["+ni.getString ( NAME ) +"]", user, cmd );
        CSLogEvent log = new CSLogEvent ( ci.getName(), MDEOP, user.getFullMask(), ( isOper ? ni.getName() : null ) );
        ChanServ.addLog ( log );
        ChanServ.deopAll ( c ); 
    }
    
    public void mKick ( User user, String[] cmd )  {
        //:Pintuz PRIVMSG ChanServ@services.avade.net :mkick #avade 
        //  0      1        2                          3     4              = 5
 
        CmdData cmdData = this.validateCommandData ( user, MKICK, cmd );
         
        switch ( cmdData.getStatus ( ) ) {
            case SYNTAX_ERROR :
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "MKICK <#Chan>" ) ); 
                return;
                
            case CHAN_NOT_EXIST :
                this.service.sendMsg ( user, output ( CHAN_NOT_EXIST, cmdData.getString1 ( ) ) ); 
                return;
                
            case CHAN_NOT_REGISTERED :
                this.service.sendMsg ( user, output ( CHAN_NOT_REGISTERED, cmdData.getChan ( ) .getString ( NAME ) ) ); 
                return;
                
            case ACCESS_DENIED : 
                this.service.sendMsg ( user, output ( ACCESS_DENIED, "" ) ); 
                return;
                
            case NOT_ENOUGH_ACCESS :
                this.service.sendMsg ( user, output ( NOT_ENOUGH_ACCESS, cmdData.getString1 ( ) ) ); 
                return;
                
            default :
                
        }
        Chan c = cmdData.getChan ( );
        ChanInfo ci = cmdData.getChanInfo ( );
        NickInfo ni;
        boolean isOper = false;
        if ( ( ni = cmdData.getOper ( ) ) == null ) {
            ni = cmdData.getNick ( );
        } else {
            isOper = true;
        }
        
        if ( ci.getSettings().is ( VERBOSE ) )  {
            this.service.sendOpMsg ( ci, output ( NICK_MKICK_CHAN, ni.getString ( NAME ) , ci.getName ( )  )  );
        }                    
        this.service.sendMsg ( user, output ( NICK_MKICK, c.getString ( NAME ) ) );
        CSLogEvent log = new CSLogEvent ( ci.getName(), MKICK, user.getFullMask(), ( isOper ? ni.getName() : null )  );
        ChanServ.addLog ( log );
        this.snoop.msg ( true, cmd[4]+" ["+ni.getString ( NAME ) +"]", user, cmd );
        ci.kickAll ( "Masskick" );
    }
    
    public void list ( User user, String[] cmd )  { /* DONE? */
        // :DreamHea1er PRIVMSG NickServ@services.sshd.biz :list dream*
        //      0          1            2                     3    4     = 5
        if ( ! ChanServ.enoughAccess ( user, LIST ) ) {
            return; 
        }
        
        CmdData cmdData = this.validateCommandData ( user, LIST, cmd );
        
        switch ( cmdData.getStatus ( ) ) {
            case SYNTAX_ERROR :
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "LIST <pattern>" ) ); 
                return;
            
            case ACCESS_DENIED : 
                this.service.sendMsg ( user, output ( ACCESS_DENIED, "" ) );
                return;
            
            default :
                
        }
        ArrayList<ChanInfo> cList = ChanServ.searchChans ( cmd[4] );
        String buf;

        this.service.sendMsg ( user, f.b ( ) +"List:"+f.b ( )  );

        for ( ChanInfo ci : cList ) {
            buf = f.b ( ) +"    "+ci.getName ( );
            buf += ( ci.getTopic ( ) != null ) ? " : "+ci.getTopic().getTopic ( ) : "";
            this.service.sendMsg ( user, buf );
        }
        this.showEnd ( user, "Info" );
    }
    
    
    private void accesslog(User user, String[] cmd) {
        // :DreamHea1er PRIVMSG NickServ@services.sshd.biz :ACCESSLOG #chan
        // 0            1       2                          3          4       = 5
        
        if ( ! CSDatabase.checkConn() ) {
            this.service.sendMsg ( user, "Error: Database not available, try again later." );
        }
        
        CmdData cmdData = this.validateCommandData ( user, ACCESSLOG, cmd );
        
        switch ( cmdData.getStatus ( ) ) {
            case SYNTAX_ERROR :
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "ACCESSLOG <chan>" ) ); 
                return;
                
            case ACCESS_DENIED : 
                this.service.sendMsg ( user, output ( ACCESS_DENIED, "" ) );
                return;
                
            case CHAN_NOT_REGISTERED :
                this.service.sendMsg ( user, output ( CHAN_NOT_REGISTERED, cmdData.getString1() ) );
                return;
                
            case CHAN_IS_CLOSED :
                this.service.sendMsg ( user, output ( CHAN_IS_CLOSED, cmdData.getChanInfo().getName() ) );
                return;
                
            default :
                
        }
        NickInfo ni = cmdData.getNick ( );
        ChanInfo ci = cmdData.getChanInfo ( );
        ArrayList<CSAccessLogEvent> csaList = CSDatabase.getChanAccLogList ( user, ci );
        
        this.service.sendMsg(user, "*** Access Log for "+ci.getName()+":");
        for ( CSAccessLogEvent log : csaList ) {
            if ( user.isAtleast ( IRCOP ) ) {
                this.service.sendMsg ( user, output ( SHOWACCESSLOGOPER, log.getStamp(), log.getFlag(), log.getTarget(), log.getUsermask(), log.getInstater() ) );
            } else {
                this.service.sendMsg ( user, output ( SHOWACCESSLOG, log.getStamp(), log.getFlag(), log.getTarget(), log.getInstater() ) );
            }
        }
        this.service.sendMsg ( user, "*** End of Logs ***" );
         
    }
 
    private void topiclog ( User user, String[] cmd ) {
        // :DreamHea1er PRIVMSG NickServ@services.sshd.biz :ACCESSLOG #chan
        // 0            1       2                          3          4       = 5
        if ( ! CSDatabase.checkConn() ) {
            this.service.sendMsg ( user, "Error: Database not available, try again later." );
        }
        CmdData cmdData = this.validateCommandData ( user, TOPICLOG, cmd );

        switch ( cmdData.getStatus ( ) ) {
            case SYNTAX_ERROR :
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "ACCESSLOG <chan>" ) ); 
                return;

            case CHAN_NOT_REGISTERED :
                this.service.sendMsg ( user, output ( CHAN_NOT_REGISTERED, cmdData.getString1() ) );
                return;
                
            case CHAN_IS_CLOSED :
                this.service.sendMsg ( user, output ( CHAN_IS_CLOSED, cmdData.getChanInfo().getName() ) );
                return;
                
            default :
                
        }
        NickInfo ni = cmdData.getNick ( );
        ChanInfo ci = cmdData.getChanInfo ( );
        ArrayList<Topic> tList = CSDatabase.getTopicList ( ci );
        this.service.sendMsg(user, "*** Access Log for "+ci.getName()+":");
        for ( Topic topic : tList ) {
            this.service.sendMsg ( user, output ( SHOWTOPICLOG, topic.getTimeStr(), topic.getSetter(), topic.getTopic() ) );
        }
        
        this.service.sendMsg ( user, "*** End of Logs ***" );

    }
    
    
    private void chanFlag(User user, String[] cmd) {
        // :DreamHea1er PRIVMSG NickServ@services.sshd.biz :CHANFLAG #chan MAX_BANS 500
        // 0            1       2                          3         4     5        6  = 7        
        CmdData cmdData = this.validateCommandData ( user, CHANFLAG, cmd );
        switch ( cmdData.getStatus ( ) ) {
            case SYNTAX_ERROR :
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "CHANFLAG <chan> <flag> <value>" ) ); 
                return;

            case CHAN_NOT_REGISTERED :
                this.service.sendMsg ( user, output ( CHAN_NOT_REGISTERED, cmdData.getString1() ) );
                return;
                
            case CHAN_IS_FROZEN :
                this.service.sendMsg ( user, output ( CHAN_IS_CLOSED, cmdData.getChanInfo().getName() ) );
                return;
                       
            case CHAN_IS_CLOSED :
                this.service.sendMsg ( user, output ( CHAN_IS_CLOSED, cmdData.getChanInfo().getName() ) );
                return;
                
            case ACCESS_DENIED : 
                this.service.sendMsg ( user, output ( ACCESS_DENIED, "" ) );
                return;
            
            case NO_SUCH_CHANFLAG : 
                this.service.sendMsg ( user, output ( NO_SUCH_CHANFLAG, cmdData.getString1 ( ) ) );
                return;
            
            case BAD_CHANFLAG_VALUE : 
                this.service.sendMsg ( user, output ( BAD_CHANFLAG_VALUE ) );
                return;
            
            default :
                
        }
        ChanInfo ci = cmdData.getChanInfo ( );
        NickInfo ni = cmdData.getNick ( );
        String commandStr = cmdData.getCommandStr ( );
        String commandVal = cmdData.getCommandVal ( );
        int command = cmdData.getCommand ( );
        String value = cmdData.getString2 ( );

        switch ( command ) {
            case JOIN_CONNECT_TIME :
            case TALK_CONNECT_TIME :
            case TALK_JOIN_TIME :
            case MAX_BANS :
                short sho;
                if ( commandVal == null ) 
                    commandVal = "0";
                try {
                    sho = Short.parseShort ( commandVal );
                } catch ( NumberFormatException ex ) {
                    this.service.sendMsg ( user, "Error: Invalid value." );
                    return;
                }
                ci.getChanFlag().setShortFlag ( command, sho );
                ci.getChanges().change ( command );
                ChanServ.addToWorkList ( CHANGE, ci );
                this.service.sendServ ( "SVSXCF "+ci.getName()+" "+commandStr+":"+commandVal );
                this.service.sendMsg ( user, "ChanFlag "+commandStr+" has now been set to: "+commandVal );
                break;

            case NO_NOTICE :
            case NO_CTCP :
            case NO_PART_MSG :
            case NO_QUIT_MSG :
            case EXEMPT_OPPED :
            case EXEMPT_VOICED :
            case EXEMPT_IDENTD :
            case EXEMPT_REGISTERED :
            case EXEMPT_INVITES :
                boolean boo = ( commandVal.equalsIgnoreCase ( "ON" ) );
                ci.getChanFlag().setBooleanFlag ( command, boo );
                ci.getChanges().change ( command );
                ChanServ.addToWorkList ( CHANGE, ci );
                this.service.sendServ ( "SVSXCF "+ci.getName()+" "+commandStr+":"+commandVal );
                this.service.sendMsg ( user, "ChanFlag "+commandStr+" has now been set to: "+commandVal );
                break;
                
            case GREETMSG :
                String message = Handler.cutArrayIntoString ( cmd, 6 );
                ci.getChanFlag().setGreetmsg ( message );
                ci.getChanges().change ( command );
                ChanServ.addToWorkList ( CHANGE, ci );
                this.service.sendServ ( "SVSXCF "+ci.getName()+" "+commandStr+" :"+message );
                this.service.sendMsg ( user, "ChanFlag "+commandStr+" has now been set to: "+message );
                break;
                
            case LIST :
                CSFlag cf = ci.getChanFlag ( );
                this.service.sendMsg ( user, "*** ChanFlag LIST for "+ci.getName()+" ***" );
                this.service.sendMsg ( user, "  - JOIN_CONNECT_TIME: "+cf.getJoin_connect_time() );
                this.service.sendMsg ( user, "  - TALK_CONNECT_TIME: "+cf.getTalk_connect_time() );
                this.service.sendMsg ( user, "  - TALK_JOIN_TIME: "+cf.getTalk_join_time() );
                this.service.sendMsg ( user, "  - MAX_BANS: "+cf.getMax_bans() );
                this.service.sendMsg ( user, "  - NO_NOTICE: "+( cf.isNo_notice() ? "ON" : "OFF" ) );
                this.service.sendMsg ( user, "  - NO_CTCP: "+( cf.isNo_ctcp()? "ON" : "OFF" ) );
                this.service.sendMsg ( user, "  - NO_PART_MSG: "+( cf.isNo_part_msg()? "ON" : "OFF" ) );
                this.service.sendMsg ( user, "  - NO_QUIT_MSG: "+( cf.isNo_quit_msg()? "ON" : "OFF" ) );
                this.service.sendMsg ( user, "  - EXEMPT_OPPED: "+( cf.isExempt_opped()? "ON" : "OFF" ) );
                this.service.sendMsg ( user, "  - EXEMPT_VOICED: "+( cf.isExempt_voiced()? "ON" : "OFF" ) );
                this.service.sendMsg ( user, "  - EXEMPT_IDENTD: "+( cf.isExempt_identd()? "ON" : "OFF" ) );
                this.service.sendMsg ( user, "  - EXEMPT_REGISTERED: "+( cf.isExempt_registered() ? "ON" : "OFF" ) );
                this.service.sendMsg ( user, "  - EXEMPT_INVITES: "+( cf.isExempt_invites()? "ON" : "OFF" ) );
                this.service.sendMsg ( user, "  - GREETMSG: "+( cf.isGreetmsg() ? cf.getGreetmsg() : "NONE" ) );
                this.service.sendMsg ( user, "*** End of List ***" );
                break;
                
            default :
                
        }
        
        
    }

    private CSAcc getAcc ( int command, ChanInfo ci, NickInfo ni ) {
        return ci.getAccess ( command, ni );
    }
    private CSAcc getAcc ( int command, ChanInfo ci, String mask ) {
        return ci.getAccess ( command, mask );
    }

    
    private CmdData validateCommandData ( User user, int command, String[] cmd )  {
        Chan c;
        Chan c2;
        ChanInfo ci;
        NickInfo ni;
        NickInfo ni2 = null;
        String nick = new String ( );
        String pass = new String ( ); 
        CmdData cmdData = new CmdData ( );
        User target = null;
        int subcommand;
        int num;
        String mask = null;
        CSAcc acc = null;
        CSAcc acc2 = null;
        boolean checkNick   = false;
        boolean checkPass   = false;
        boolean needAccess  = false;
        String str1 = null;
        String str2 = null;
        Config conf = Proc.getConf();
        /*
        
        */
        
        switch ( command ) {
            
            case CHANFLAG :
                // :DreamHea1er PRIVMSG NickServ@services.sshd.biz :CHANFLAG #chan MAX_BANS 500
                // 0            1       2                          3         4     5        6  = 7   
                if ( isShorterThanLen ( 6, cmd) ) {
                    cmdData.setStatus ( SYNTAX_ERROR );
                } else if ( ( ci = ChanServ.findChan ( cmd[4] ) ) == null ) {
                    cmdData.setString1 ( cmd[4] );
                    cmdData.setStatus ( CHAN_NOT_REGISTERED );
                } else if ( ci.getSettings().is ( CLOSED ) ) {
                    cmdData.setChanInfo ( ci );
                    cmdData.setStatus ( CHAN_IS_CLOSED );
                } else if ( ci.getSettings().is ( FROZEN ) ) {
                    cmdData.setChanInfo ( ci );
                    cmdData.setStatus ( CHAN_IS_FROZEN );
                } else if ( ( ni = ci.getNickByUser ( user ) ) == null && ! user.isAtleast ( SA ) ) {
                    cmdData.setString1 ( ci.getName() ); 
                    cmdData.setStatus ( ACCESS_DENIED );
                } else if ( ! ci.isFounder ( ni ) && ! user.isAtleast ( SA ) ) {
                    cmdData.setString1 ( ci.getName() );
                    cmdData.setStatus ( ACCESS_DENIED );
                } else if ( ! CSFlag.isFlag ( cmd[5] ) ) {
                    cmdData.setString1 ( cmd[5] );
                    cmdData.setStatus ( NO_SUCH_CHANFLAG );
                } else if ( ! CSFlag.isOkValue ( cmd[5], ( cmd.length > 6 ? cmd[6] : "" ) ) ) {
                    cmdData.setStatus ( BAD_CHANFLAG_VALUE );
                } else {
                    cmdData.setChanInfo ( ci );
                    cmdData.setCommandStr ( cmd[5] );
                    cmdData.setCommand ( cmd[5].toUpperCase().hashCode() );
                    cmdData.setNick ( ni );
                    cmdData.setCommandVal ( cmd.length > 6 ? cmd[6] : "" );
                }
                break; 
                
            case ACCESSLOG :
                if ( isShorterThanLen ( 5, cmd) ) {
                    cmdData.setStatus ( SYNTAX_ERROR );
                } else if ( ( ci = ChanServ.findChan ( cmd[4] ) ) == null ) {
                    cmdData.setString1 ( cmd[4] );
                    cmdData.setStatus ( CHAN_NOT_REGISTERED );
                } else if ( ci.getSettings().is ( CLOSED ) ) {
                    cmdData.setChanInfo ( ci );
                    cmdData.setStatus ( CHAN_IS_CLOSED );
                } else if ( ( ni = ci.getNickByUser ( user ) ) == null && ! user.isAtleast ( SA ) ) {
                    cmdData.setString1 ( ci.getName() ); 
                    cmdData.setStatus ( ACCESS_DENIED );
                } else if ( ! ci.isAtleastAop ( ni ) && ! user.isAtleast ( SA ) ) {
                    cmdData.setString1 ( ci.getName() );
                    cmdData.setStatus ( ACCESS_DENIED );
                } else {
                    cmdData.setChanInfo ( ci );
                    cmdData.setNick ( ni );
                }
                break; 
                
             case TOPICLOG :
                if ( isShorterThanLen ( 5, cmd) ) {
                    cmdData.setStatus ( SYNTAX_ERROR );
                } else if ( ( ci = ChanServ.findChan ( cmd[4] ) ) == null ) {
                    cmdData.setString1 ( cmd[4] );
                    cmdData.setStatus ( CHAN_NOT_REGISTERED );
                } else if ( ci.getSettings().is ( CLOSED ) ) {
                    cmdData.setChanInfo ( ci );
                    cmdData.setStatus ( CHAN_IS_CLOSED );
                } else {
                    cmdData.setChanInfo ( ci );
                }
                break; 
                
            case SOP :
            case AOP :
            case AKICK : 
                //:DreamHealer PRIVMSG ChanServ@services.avade.net :akick #friends add *!*@10.0.1/24
                //           0       1                           2      3        4   5             6
                if ( isShorterThanLen ( 7, cmd ) ) {
                    cmdData.setStatus ( SYNTAX_ERROR );
                } 
                ci = ChanServ.findChan ( cmd[4] );
                ni = ci.getNickByUser ( user );
                
                if ( ( ni2 = NickServ.findNick ( cmd[6] ) ) == null ) {
                    mask = cmd[6];
                }
                
                subcommand = cmd[5].toUpperCase().hashCode ( );
                if ( subcommand != ADD && subcommand != DEL ) {
                    cmdData.setStatus ( SYNTAX_ERROR );
                } else if ( ni2 == null && mask == null ) {
                    cmdData.setString1 ( cmd[6] );
                    cmdData.setStatus ( NICK_NOT_REGISTERED );
                } else if ( ci == null ) {
                    cmdData.setString1 ( cmd[4] );
                    cmdData.setStatus ( CHAN_NOT_REGISTERED );
                } else if ( ci.getSettings().is ( FROZEN ) ) {
                    cmdData.setChanInfo ( ci );
                    cmdData.setStatus ( CHAN_IS_FROZEN );  
                } else if ( ci.getSettings().is ( CLOSED ) ) {
                    cmdData.setChanInfo ( ci );
                    cmdData.setStatus ( CHAN_IS_CLOSED );  
                } else if ( ni == null || ! ci.isAtleastSop ( ni ) ) {
                    cmdData.setString1 ( user.getString ( NAME ) );
                    cmdData.setStatus ( ACCESS_DENIED );
                } else if ( ni2 != null && ! ni2.isAuth ( ) ) {
                    cmdData.setNick2 ( ni2 );
                    cmdData.setStatus ( NICK_NOT_AUTHED );
                } else if ( ni2 != null && 
                            ci.getAccessByNick ( ni ) <= ci.getAccessByNick ( ni2 ) ) {
                    cmdData.setStatus ( NOT_ENOUGH_ACCESS );
                } else if ( ( acc = getAcc ( command, ci, ni2 ) ) == null && 
                            ( acc = getAcc ( command, ci, mask ) ) == null &&
                            subcommand == DEL ) {
                    cmdData.setStatus ( XOP_NOT_FOUND );
                } else if ( subcommand == ADD && acc != null ) {
                    cmdData.setString1 ( (ni2 != null ? ni2.getName() : mask) );
                    cmdData.setStatus ( XOP_ALREADY_PRESENT );
                } else if ( subcommand == ADD && 
                            ( ( ni2 != null && ( acc = new CSAcc ( ni2, command ) ) == null ) ||
                              ( mask != null && ( acc = new CSAcc ( mask, command ) ) == null ) ) ) {  
                    cmdData.setStatus ( XOP_ADD_FAIL );
                } else {
                    cmdData.setChanInfo ( ci );
                    cmdData.setNick ( ni );
                    if ( ni2 != null ) {
                        cmdData.setNick2 ( ni2 );
                    } else {
                        cmdData.setString1 ( mask );
                    }
                    cmdData.setAcc ( acc );
                    cmdData.setCommand ( command );
                    cmdData.setSubCommand ( subcommand );
                }
                break;            
                   
            case LIST :
                if ( isShorterThanLen ( 5, cmd ) ) {
                    cmdData.setStatus ( SYNTAX_ERROR );
                } else if ( ! user.isAtleast ( IRCOP ) ) {
                    cmdData.setStatus ( ACCESS_DENIED );
                }
                break;
                
            case SET :
                if ( isShorterThanLen ( 7, cmd ) ) {
                    cmdData.setStatus ( SYNTAX_ERROR );
                } else if ( ( ni = NickServ.findNick ( user.getString ( NAME ) ) ) == null ) {
                    cmdData.setString1 ( user.getString ( NAME ) );
                    cmdData.setStatus ( NICK_NOT_REGISTERED );
                } else if ( ( ci = ChanServ.findChan ( cmd[4] ) ) == null ) {
                    cmdData.setString1 ( cmd[4] );
                    cmdData.setStatus ( CHAN_NOT_REGISTERED );
                } else if ( ci.is ( FROZEN )  && ! user.isAtleast ( CSOP )  ) {
                    cmdData.setChanInfo ( ci );
                    cmdData.setStatus ( CHAN_IS_FROZEN );
                } else if ( ci.is ( CLOSED )  && ! user.isAtleast ( CSOP )  ) {
                    cmdData.setChanInfo ( ci );
                    cmdData.setStatus ( CHAN_IS_CLOSED );
                } else if ( ! ci.isFounder ( user ) ) {
                    cmdData.setChanInfo ( ci );
                    cmdData.setStatus ( ACCESS_DENIED );
                } else {
                    cmdData.setChanInfo ( ci );
                    cmdData.setNick ( ni );
                }
                break;
                
            case WHY :
                if ( isShorterThanLen ( 6, cmd ) ) {
                    cmdData.setStatus ( SYNTAX_ERROR );
                } else if ( ( ci = ChanServ.findChan ( cmd[4] ) ) == null ) {
                    cmdData.setString1 ( cmd[4] );
                    cmdData.setStatus ( CHAN_NOT_REGISTERED );
                } else if ( ( target = Handler.findUser ( cmd[5] ) ) == null ) {
                    cmdData.setString1 ( cmd[5] );
                    cmdData.setStatus ( NICK_NOT_EXIST );
                } else if ( ( ni = ci.getNickByUser ( user ) ) == null && ! user.isAtleast ( IRCOP )) {
                    cmdData.setChanInfo ( ci );
                    cmdData.setStatus ( ACCESS_DENIED );
                } else {
                    cmdData.setChanInfo ( ci );
                    cmdData.setNick ( ni );
                    cmdData.setTarget ( target );
                }
                break;
            
            case UNBAN :
                if ( isShorterThanLen ( 6, cmd ) )  {
                    target = user;
                    cmdData.setTarget ( target );
                } else if ( isShorterThanLen ( 7, cmd ) ) {
                    if ( ( target = Handler.findUser ( cmd[5] ) ) == null ) {                        
                        cmdData.setString1 ( cmd[5] );
                        cmdData.setStatus ( NICK_NOT_EXIST );
                        break;
                    } else {
                        cmdData.setTarget( target );
                    }
                } 
                /* RUN NEXT */
                
            case INVITE :
                if ( isShorterThanLen ( 5, cmd ) ) {
                    cmdData.setStatus ( SYNTAX_ERROR );
                } else if ( ( c = Handler.findChan ( cmd[4] ) ) == null ) {
                    cmdData.setString1 ( cmd[4] );
                    cmdData.setStatus ( CHAN_NOT_EXIST );
                } else if ( ( ci = ChanServ.findChan ( cmd[4] ) ) == null ) {
                    cmdData.setString1 ( cmd[4] );
                    cmdData.setStatus ( CHAN_NOT_REGISTERED );
                } else if ( ( ni = ci.getNickByUser ( user ) ) == null ) {
                    cmdData.setChanInfo ( ci );
                    cmdData.setStatus ( NICK_ACCESS_DENIED ); 
                } else if ( ci.getSettings().is ( FROZEN ) ) {
                    cmdData.setChanInfo ( ci );
                    cmdData.setStatus ( CHAN_IS_FROZEN );  
                } else if ( ci.getSettings().is ( CLOSED ) ) {
                    cmdData.setChanInfo ( ci );
                    cmdData.setStatus ( CHAN_IS_CLOSED );  
                } else {
                    cmdData.setChan ( c );
                    cmdData.setChanInfo ( ci );
                    cmdData.setNick ( ni );
                }
                break;
            
            case INFO :
                if ( isShorterThanLen ( 5, cmd ) ) {
                    cmdData.setStatus ( SYNTAX_ERROR );
                } else if ( ( ci = ChanServ.findChan ( cmd[4] ) ) == null ) {
                    cmdData.setString1 ( cmd[4] );
                    cmdData.setStatus ( CHAN_NOT_REGISTERED );    
                } else if ( ci.getSettings().is ( FROZEN ) && ! user.isAtleast ( IRCOP ) ) {
                    cmdData.setChanInfo ( ci );
                    cmdData.setStatus ( CHAN_IS_FROZEN );  
                } else if ( ci.getSettings().is ( CLOSED ) && ! user.isAtleast ( IRCOP ) ) {
                    cmdData.setChanInfo ( ci );
                    cmdData.setStatus ( CHAN_IS_CLOSED );  
                } else {
                    cmdData.setChanInfo(ci);
                }
                break;
            
            case DROP :
                if ( isShorterThanLen ( 6, cmd ) ) {
                    cmdData.setStatus ( SYNTAX_ERROR );
                } else if ( ( ci = ChanServ.findChan ( cmd[4] ) ) == null ) {
                    cmdData.setString1 ( cmd[4] );
                    cmdData.setStatus ( CHAN_NOT_REGISTERED );                
                } else if ( ci.getSettings().is ( FROZEN ) ) {
                    cmdData.setChanInfo ( ci );
                    cmdData.setStatus ( CHAN_IS_FROZEN ); 
                } else if ( ci.getSettings().is ( CLOSED ) ) {
                    cmdData.setChanInfo ( ci );
                    cmdData.setStatus ( CHAN_IS_CLOSED );
                } else if ( ! ci.identify ( user, cmd[5] ) ) {
                    cmdData.setChanInfo ( ci );
                    cmdData.setStatus ( INVALID_PASSWORD ); 
                } else if ( ci.is ( MARK ) ) {
                    cmdData.setChanInfo ( ci );
                    cmdData.setStatus ( IS_MARKED );
                } else {
                    cmdData.setChanInfo(ci);
                }
                break;

            case IDENTIFY :
                if ( isShorterThanLen ( 6, cmd ) ) {
                    cmdData.setStatus ( SYNTAX_ERROR );
                } else if ( ( ci = ChanServ.findChan ( cmd[4] ) ) == null ) {
                    cmdData.setString1 ( cmd[4] );
                    cmdData.setStatus ( CHAN_NOT_REGISTERED );                
                } else if ( ci.getSettings().is ( FROZEN ) ) {
                    cmdData.setChanInfo ( ci );
                    cmdData.setStatus ( CHAN_IS_FROZEN ); 
                } else if ( ci.getSettings().is ( CLOSED ) ) {
                    cmdData.setChanInfo ( ci );
                    cmdData.setStatus ( CHAN_IS_CLOSED );
                } else if ( ! ci.identify ( user, cmd[5] ) ) {
                    cmdData.setChanInfo ( ci );
                    cmdData.setStatus ( INVALID_PASSWORD ); 
                } else {
                    cmdData.setChanInfo(ci);
                }
                break;
                
            case REGISTER :
                ni = NickServ.findNick ( user.getString ( NAME ) );
                if ( isShorterThanLen ( 7, cmd ) ) {
                    cmdData.setStatus ( SYNTAX_ERROR );
                } else if ( ni == null ) {
                    cmdData.setUser ( user );
                    cmdData.setStatus ( NICK_NOT_REGISTERED );
                } else if ( ! user.isIdented ( ni ) ) {
                    cmdData.setNick ( ni );
                    cmdData.setStatus ( NICK_NOT_IDENTIFIED );
                } else if ( ( c = Handler.findChan ( cmd[4] ) ) == null ) {
                    cmdData.setString1 ( cmd[4] );
                    cmdData.setStatus ( CHAN_NOT_EXIST );   
                } else if ( ( ci = ChanServ.findChan ( cmd[4] ) ) != null ) {
                    cmdData.setChanInfo ( ci );
                    cmdData.setStatus ( CHAN_ALREADY_REGGED );      
                } else if ( ! c.isOp ( user ) ) {
                    cmdData.setUser ( user );
                    cmdData.setStatus ( USER_NOT_OP );
                } else {
                    cmdData.setChan(c);
                    cmdData.setChanInfo(ci);
                    cmdData.setNick(ni);
                }
                break;
                
            case OP :
            case DEOP : 
                if ( isShorterThanLen ( 5, cmd ) ) {
                    cmdData.setStatus ( SYNTAX_ERROR );
                } else if ( ( c = Handler.findChan ( cmd[4] ) ) == null ) {
                    cmdData.setString1 ( cmd[4] );
                    cmdData.setStatus ( CHAN_NOT_EXIST );                
                } else if ( ( ci = ChanServ.findChan ( cmd[4] ) ) == null ) {
                    cmdData.setChan ( c );
                    cmdData.setStatus ( CHAN_NOT_REGISTERED );
                } else if ( ci.getSettings().is ( FROZEN ) ) {
                    cmdData.setChanInfo ( ci );
                    cmdData.setStatus ( CHAN_IS_FROZEN ); 
                } else if ( ci.getSettings().is ( CLOSED ) ) {
                    cmdData.setChanInfo ( ci );
                    cmdData.setStatus ( CHAN_IS_CLOSED );
                } else if ( ( ni = ci.getTopNickByUser ( user ) ) == null ) {
                    cmdData.setChanInfo ( ci );
                    cmdData.setStatus ( ACCESS_DENIED );                
                } else if ( cmd.length > 5 && ( ( target = Handler.findUser ( cmd[5] ) ) == null || ! c.nickIsPresent ( cmd[5] ) ) ) {
                    cmdData.setChan ( c );
                    cmdData.setStatus ( NICK_NOT_PRESENT );
                } else {
                    cmdData.setChan ( c );
                    cmdData.setChanInfo ( ci );
                    cmdData.setNick ( ni );
                    cmdData.setTarget ( target );
                }
                break;
           
            case MDEOP : 
            case MKICK : 
                if ( isShorterThanLen ( 4, cmd )  )  {
                    cmdData.setStatus ( SYNTAX_ERROR );                 
                } else if ( ( c = Handler.findChan ( cmd[4] ) ) == null ) {
                    cmdData.setString1 ( cmd[4] );
                    cmdData.setStatus ( CHAN_NOT_EXIST );                
                } else if ( ( ci = ChanServ.findChan ( cmd[4] ) ) == null ) {
                    cmdData.setChan ( c );
                    cmdData.setStatus ( CHAN_NOT_REGISTERED );
                } else if ( user.isAtleast ( CSOP ) && ( ni = user.getSID().getTopOperNick ( ) ) != null ) {
                    cmdData.setChan ( c );
                    cmdData.setChanInfo ( ci );
                    cmdData.setOper ( ni );
                } else if ( ci.getSettings().is ( FROZEN ) ) {
                    cmdData.setChanInfo ( ci );
                    cmdData.setStatus ( CHAN_IS_FROZEN ); 
                } else if ( ci.getSettings().is ( CLOSED ) ) {
                    cmdData.setChanInfo ( ci );
                    cmdData.setStatus ( CHAN_IS_CLOSED );
                } else if ( ( ni = ci.getTopNickByUser ( user ) ) == null ) {
                    cmdData.setChanInfo ( ci );
                    cmdData.setStatus ( ACCESS_DENIED );
                } else { 
                    cmdData.setChan ( c );
                    cmdData.setChanInfo ( ci );
                    cmdData.setNick ( ni );
                    if ( ci.isFounder ( ni ) ) {
                        break;
                    } else if ( ci.isAccess ( SOP, ni ) ) {
                        for ( User u : c.getList ( ALL ) ) {
                            if ( ci.isFounder ( u ) ) { 
                                cmdData.setString1 ( u.getString ( NAME ) );
                                cmdData.setStatus ( NOT_ENOUGH_ACCESS );
                            }
                        }
                    } else if ( ci.isAccess ( AOP, ni ) ) {
                        for ( User u : c.getList ( ALL ) ) {
                            if ( ci.isFounder ( u )  || ci.isAccess ( SOP, u ) ) { 
                                cmdData.setString1 ( u.getString ( NAME ) );
                                cmdData.setStatus ( NOT_ENOUGH_ACCESS );
                            }
                        }
                    } else {
                        cmdData.setStatus ( ACCESS_DENIED );
                    }
                }
                break;
                
            /* IRCOP COMMANDS */
            case AUDITORIUM :
            case MARK :
            case FREEZE :
            case CLOSE :
            case HOLD :
                cmdData.setCommand ( command );
                if ( ! ChanServ.enoughAccess ( user, command ) ) {
                    cmdData.setStatus ( ACCESS_DENIED );
                } else if ( isShorterThanLen ( 5, cmd ) ) {
                    cmdData.setStatus ( SYNTAX_ERROR );
                } else if ( ( ci = ChanServ.findChan ( cmd[4].replace ( "-", "" ) ) ) == null ) {
                    cmdData.setString1 ( cmd[4].replace ( "-", "" ) );
                    cmdData.setStatus ( CHAN_NOT_REGISTERED );
                } else if ( ci.is ( MARK ) && ( command != MARK || command == MARK && cmd[4].charAt(0) != '-' ) ) {
                    cmdData.setChanInfo ( ci );
                    cmdData.setStatus ( IS_MARKED );   
                } else if ( ci.is ( command ) && command != MARK && cmd[4].charAt(0) != '-' ) {
                    cmdData.setChanInfo ( ci );
                    cmdData.setString1 ( this.getCommandStr ( command ) );
                    cmdData.setStatus ( CHANFLAG_EXIST );
                } else if ( ! ci.is ( command ) && cmd[4].charAt(0) == '-' ) {
                    cmdData.setChanInfo ( ci );
                    cmdData.setString1 ( this.getCommandStr ( this.getAntiCommand ( command ) ) );
                    cmdData.setStatus ( CHANFLAG_EXIST );
                } else {
                    cmdData.setChanInfo ( ci );
                    if ( cmd[4].charAt ( 0 ) == '-' ) {
                        cmdData.setCommand ( this.getAntiCommand ( command ) );
                    }
                }
                break;
                
            case DELETE :
            case GETPASS :
                cmdData.setCommand ( command );
                if ( ! ChanServ.enoughAccess ( user, command ) ) {
                    cmdData.setStatus ( ACCESS_DENIED );
                } else if ( isShorterThanLen ( 5, cmd ) ) {
                    cmdData.setStatus ( SYNTAX_ERROR );
                } else if ( ( ci = ChanServ.findChan ( cmd[4] ) ) == null ) {
                    cmdData.setString1 ( cmd[4] );
                    cmdData.setStatus ( CHAN_NOT_REGISTERED );
                } else if ( ci.is ( MARK ) ) {
                    cmdData.setChanInfo ( ci );
                    cmdData.setStatus ( IS_MARKED );
                } else {
                    cmdData.setChanInfo ( ci );
                }
                break;
                
            default : 
                
        } 
        return cmdData;
    }
    
    private int getAntiCommand ( int command ) {
        switch ( command ) {
            case MARK :
                return UNMARK;
            case FREEZE :
                return UNFREEZE;
            case CLOSE :
                return REOPEN;
            case HOLD :
                return UNHOLD;
            case AUDITORIUM :
                return UNAUDITORIUM;
            default :
                return 0;
        }
    }
    private String getCommandStr ( int command ) {
        switch ( command ) {
            case MARK :
                return "Marked";    
            case FREEZE :
                return "Frozen";
            case CLOSE :
                return "Closed";
            case HOLD :
                return "Held";
            case UNMARK :
                return "UnMarked";    
            case UNFREEZE :
                return "UnFrozen";
            case REOPEN :
                return "Open";
            case UNHOLD :
                return "UnHeld";
            case AUDITORIUM :
                return "Auditorium";
            case UNAUDITORIUM :
                return "UnAuditorium";
            default :
                return "";
        }
    }
    
    private String getListStr ( int command ) {
        switch ( command ) {
            case SOP :
                return "SOP";
                
            case AOP :
                return "AOP";
                
            case AKICK :
                return "AKick";
                
            default :
                return "Undefined";
        }
    }
    
    private int isAddOrDel ( String str ) {
        if ( str == null ) {
            return 0;
        }
        int command = str.toUpperCase ( ) .hashCode ( );
        switch ( command ) {
            case ADD :
            case DEL : 
                return command;
            
            default :
                return 0;
        }
    }

    public String output ( int code, String... args )  {
        switch ( code )  {
            case SYNTAX_ERROR :
                return "Syntax: /ChanServ "+args[0]+"";
                
            case SYNTAX_ID_ERROR :
                return "Syntax: /ChanServ IDENTIFY <channame> <password>"; 
                
            case SYNTAX_REG_ERROR :
                return "Syntax: /ChanServ REGISTER <#chan> <password> <description>";
                
            case CMD_NOT_FOUND_ERROR :
                return "Syntax Error! For information regarding commands please issue:";
                
            case SHOW_HELP :
                return "    /"+args[0]+" HELP";
                
            case PASSWD_ERROR :
                return "Error: Wrong password for chan: "+args[0];
                
            case INVALID_EMAIL :
                return "Error: "+args[0]+" is not a valid email-adress";
                           
            case INVALID_PASSWORD :
                return "Error: invalid password.";
                
            case ACCESS_DENIED :
                return "Access denied.";
                
            case NICK_ACCESS_DENIED :
                return "Access denied to channel "+args[0]+".";
                            
            case NICK_NOT_IDENTIFIED :
                return "Access denied. Please identify to nick "+args[0]+" before proceeding.";
                
            case NOT_ENOUGH_ACCESS :
                return "Access denied. Not enough access.";
                
            case SETTING_NOT_FOUND :
                return "Error: Setting "+args[0]+" not found.";
                
            case NICK_NOT_REGISTERED :
                return "Error: nick "+args[0]+" is not registered";
                
            case NICK_NOT_AUTHED :
                return "Error: nick "+args[0]+" is not authorized";
                
            case CHAN_NOT_REGISTERED :
                return "Error: chan "+args[0]+" is not registered";
                
            case CHAN_ALREADY_REGGED :
                return "Error: chan "+f.b ( ) +args[0]+f.b ( ) +" is already registered."; 
                
            case CHAN_NOT_EXIST :
                return "Error: chan "+f.b ( ) +args[0]+f.b ( ) +" does not exist.";
                
            case NICK_NOT_EXIST :
                return "Error: nick "+f.b ( ) +args[0]+f.b ( ) +" is not online.";
                
            case USER_NOT_ONLINE :
                return "Error: no such user "+f.b ( ) +args[0]+f.b ( ) +".";
                
            case USER_NOT_OP :
                return "Error: You need to be op ( @ )  in "+f.b ( ) +args[0]+f.b ( ) +" to perform that command.";
                
            case NICK_HAS_NOOP :
                return "Error: "+args[0]+" does not wish to be added to any channel list  ( noop ) ";
                
            case WILL_NOW :
                return "Your chan will now "+args[0];
                
            case WILL_NOW_NOT :
                return "Your chan will now not "+args[0];
                
            case IS_NOW : 
                return "Your chan is now "+args[0]+".";
                
            case IS_NOT :
                return "Your chan is not "+args[0]+" anymore.";
                
            case PASSWD_ACCEPTED :
                return "Password accepted for chan "+args[0]+". You are now identified.";
                
            case DB_ERROR :
                return "Error: Database error. Please try register again in a few minutes.";
                
            case DB_NICK_ERROR :
                return "Error: Database Chan error. Please try again.";
                
            case REGISTER_DONE :
                return "Chan "+args[0]+" was successfully registered to you. Please remember your password.";
                
            case REGISTER_SEC :
                return "IMPORTANT: Never share your passwords, not even with network staff.";
                
            case NICK_IS_FOUNDER :
                return "Error: "+f.b ( ) +args[0]+f.b ( ) +" is the founder."; 
                
            case NICK_IS_SOP :
                return "Error: "+f.b ( ) +args[0]+f.b ( ) +" is already "+args[1]+"."; 
                
            case NICK_IS_OP :
                return "Error: "+f.b ( ) +args[0]+f.b ( ) +" already has access to the channel."; 
                
            case NICK_NOT_FOUND :
                return "Error: "+f.b ( ) +args[0]+f.b ( ) +" not found on "+args[1]+" list.";
                
            case NICK_CHANGED :
                return f.b ( ) +args[0]+f.b ( ) +" "+args[1]+" the "+args[2]+" list.";
                
            case NICK_NOT_IDENTED_OP :
                return f.b ( ) +args[0]+f.b ( ) +" has not identified for a nickname on Sop or Aop lists."; 
                
            case NICK_NEVEROP :
                return f.b ( ) +args[0]+f.b ( ) +" never want to be oped ( @ ) .";
                
            case NICK_NOT_PRESENT :
                return f.b ( ) +args[0]+f.b ( ) +" is not present in the channel.";
                
            case NICK_ADDED :
                return f.b ( ) +args[0]+f.b ( ) +" has been added to the "+args[1]+" list."; 
                
            case NICK_NOT_ADDED :
                return f.b ( ) +args[0]+f.b ( ) +" has not been added to the "+args[1]+" list.";
                
            case NICK_DELETED :
                return f.b ( ) +args[0]+f.b ( ) +" has been deleted from the "+args[1]+" list.";
                
            case NICK_NOT_DELETED :
                return f.b ( ) +args[0]+f.b ( ) +" has not been deleted from the "+args[1]+" list."; 
                
            case NICK_VERBOSE_ADDED :
                return f.b ( ) +args[0]+f.b ( ) +" has added "+args[1]+" to the "+args[2]+" list.";
                
            case NICK_VERBOSE_DELETED :
                return f.b ( ) +args[0]+f.b ( ) +" has removed "+args[1]+" from the "+args[2]+" list.";
                
            case NICK_VERBOSE_OP :
                return f.b ( ) +args[0]+f.b ( ) +" has oped ( @ )  "+args[1]+" in "+args[2]+".";
                
            case NICK_VERBOSE_DEOP :
                return f.b ( ) +args[0]+f.b ( ) +" has deOped ( @ )  "+args[1]+" in "+args[2]+".";
                
            case NICK_OP :
                return f.b ( ) +args[0]+f.b ( ) +" has been oped ( @ )  in "+args[1]+"."; 
                
            case NICK_DEOP :
                return f.b ( ) +args[0]+f.b ( ) +" has been deOped ( @ )  in "+args[1]+".";
                
            case NICK_MDEOP_CHAN :
                return f.b ( ) +args[0]+f.b ( ) +" used MDEOP on "+args[1]+".";
                
            case NICK_MDEOP :
                return "Channel "+f.b ( ) +args[0]+f.b ( ) +" has been Mass-DeOped ( @ ) .";
                
            case NICK_MKICK_CHAN :
                return f.b ( ) +args[0]+f.b ( ) +" used MKICK on "+args[1]+".";
                
            case NICK_MKICK :
                return "Channel "+f.b ( ) +args[0]+f.b ( ) +" has been Mass-Kicked.";
                
            case LIST_NOT_WIPED :
                return args[0]+" list of "+args[1]+" was not wiped.";
                
            case LIST_WIPED :
                return args[0]+" list of "+args[1]+" has been wiped.";
                
            case LIST_VERBOSE_WIPED :
                return args[0]+" has wiped the "+args[1]+" list of "+args[2]+".";
                
            case CHAN_IS_FROZEN :
                return "Channel "+args[0]+" is Frozen by an IRC operator.";
                
            case CHAN_IS_CLOSED :
                return "Channel "+args[0]+" is Closed by an IRC operator.";
                
            case MODELOCK :
                return "Channel "+args[0]+" has now locked its modes as: "+args[1]+".";
                
            case CHAN_SET_FLAG :
                return "Channel "+args[0]+" is now "+args[1]+".";
                
            case ALREADY_ON_LIST :
                return args[0]+" is already on "+args[1]+" list";
                     
            case CHAN_GETPASS :
                return "Password is: "+args[0]+".";
                    
            case IS_MARKED :
                return "Error: Chan "+args[0]+" is MARKed by a network staff.";
                          
            case SHOWACCESSLOG :
                return "["+args[0]+"] "+args[1]+" "+args[2]+" ["+args[3]+"]";
                
            case SHOWACCESSLOGOPER :
                return "["+args[0]+"] "+args[1]+" "+args[2]+" - "+args[3]+" ["+args[4]+"]";
                
            case SHOWTOPICLOG :
                return "["+args[0]+"] "+args[1]+" : "+args[2];
                
            case CHANNELDROPPED :
                return "Channel: "+args[0]+" was successfully dropped.";
                
            case CHANNELDELETED :
                return "Channel: "+args[0]+" was successfully deleted.";
                
            case CHANFLAG_EXIST :
                return "Chan "+args[0]+" is already "+args[1]+".";
                
            case NO_SUCH_CHANFLAG :
                return "No such chanflag available: "+args[0]+".";
                
            case BAD_CHANFLAG_VALUE :
                return "Error: Invalid chanflag value.";
                        
            case XOP_NOT_FOUND :
                return "Error: Entry not found.";
                       
            case XOP_ADD_FAIL :
                return "Error: Failed to add entry.";
                
            case XOP_ALREADY_PRESENT :
                return "Error: "+args[0]+" is already present on "+args[1]+" list.";
                
            default : 
                return "";
                
        }
    }
   
     
    private final static int SYNTAX_ERROR               = 1001;
    private final static int SYNTAX_ID_ERROR            = 1002;
    private final static int SYNTAX_REG_ERROR           = 1003;

    private final static int CMD_NOT_FOUND_ERROR        = 1021;
    private final static int SHOW_HELP                  = 1022;

    private final static int ACCESS_DENIED              = 1101;
    private final static int NOT_ENOUGH_ACCESS          = 1102;
    private final static int SETTING_NOT_FOUND          = 1151;
    private final static int NICK_NOT_REGISTERED        = 1152;
    private final static int NICK_NOT_AUTHED            = 1153;
    private final static int NICK_NOT_IDENTIFIED        = 1154;
    private final static int CHAN_NOT_REGISTERED        = 1155;
    private final static int CHAN_ALREADY_REGGED        = 1156;
    private final static int CHAN_NOT_EXIST             = 1157;
    private final static int NICK_NOT_EXIST             = 1158;
    private final static int USER_NOT_ONLINE            = 1159;

    private final static int USER_NOT_OP                = 1161;
    
    private final static int NICK_HAS_NOOP              = 1171;

    
    private final static int WILL_NOW                   = 1201;
    private final static int WILL_NOW_NOT               = 1202;
    
    private final static int IS_NOW                     = 1221;
    private final static int IS_NOT                     = 1222;
    
    private final static int PASSWD_ERROR               = 1301;
    private final static int INVALID_EMAIL              = 1302;
    private final static int INVALID_PASSWORD           = 1303;
    
    private final static int PASSWD_ACCEPTED            = 1351;
 
    private final static int DB_ERROR                   = 1401;
    private final static int DB_NICK_ERROR              = 1402;
    
    private final static int REGISTER_DONE              = 1501;
    private final static int REGISTER_SEC               = 1502;

    private final static int NICK_IS_FOUNDER            = 1601;
    private final static int NICK_IS_SOP                = 1602;
    private final static int NICK_IS_OP                 = 1603;
    private final static int NICK_NOT_FOUND             = 1604;
    private final static int NICK_CHANGED               = 1605;
    private final static int NICK_NOT_PRESENT           = 1606;
    
    private final static int NICK_NOT_IDENTED_OP        = 1616;
    private final static int NICK_NEVEROP               = 1617;

    private final static int NICK_ADDED                 = 2001;
    private final static int NICK_NOT_ADDED             = 2002;
    private final static int NICK_DELETED               = 2003;
    private final static int NICK_NOT_DELETED           = 2004;
    private final static int NICK_VERBOSE_ADDED         = 2005;
    private final static int NICK_VERBOSE_DELETED       = 2006;

    private final static int NICK_OP                    = 2051;
    private final static int NICK_DEOP                  = 2052;
    private final static int NICK_VERBOSE_OP            = 2053;
    private final static int NICK_VERBOSE_DEOP          = 2054;
    
    private final static int NICK_MDEOP_CHAN            = 2061;
    private final static int NICK_MDEOP                 = 2062;
    private final static int NICK_MKICK_CHAN            = 2063;
    private final static int NICK_MKICK                 = 2064;
    
    private final static int LIST_NOT_WIPED             = 2101;
    private final static int LIST_WIPED                 = 2102;
    private final static int LIST_VERBOSE_WIPED         = 2103;

    private final static int CHAN_IS_FROZEN             = 2201;
    private final static int CHAN_IS_CLOSED             = 2202;
   
    private final static int CHAN_SET_FLAG              = 2211; 
    private final static int CHANFLAG_EXIST             = 2212; 
    
    private final static int ALREADY_ON_LIST            = 2221; 
    
    private final static int CHAN_GETPASS               = 2301;
    
    private final static int IS_MARKED                  = 2401; 
 
    private final static int SHOWACCESSLOG              = 2501; 
    private final static int SHOWACCESSLOGOPER          = 2502; 
    private final static int SHOWTOPICLOG               = 2511; 

    private final static int CHANNELDROPPED             = 2601; 
    private final static int CHANNELDELETED             = 2602; 

    private final static int NO_SUCH_CHANFLAG           = 2651; 
    private final static int BAD_CHANFLAG_VALUE         = 2652; 

    private final static int XOP_NOT_FOUND              = 2701; 
    private final static int XOP_ADD_FAIL               = 2702; 
    private final static int XOP_ALREADY_PRESENT        = 2703; 

 }