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
import core.HashString;
import core.Proc;
import core.StringMatch;
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

    /**
     *
     * @param service
     * @param snoop
     */
    public CSExecutor ( ChanServ service, CSSnoop snoop )  {
        super ( );
        this.service = service;
        this.snoop = snoop;
        this.f = new TextFormat ( );
    }

    /**
     *
     * @param user
     * @param cmd
     */
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
        
        HashString command = new HashString ( cmd[3] );
        
        if      ( command.is(REGISTER) )    { this.register ( user, cmd );                  }
        else if ( command.is(IDENTIFY) )    { this.identify ( user, cmd );                  }
        else if ( command.is(DROP) )        { this.drop ( user, cmd );                      }
        else if ( command.is(SET) )         { this.set ( user, cmd );                       }
        else if ( command.is(CHANFLAG) )    { this.chanFlag ( user, cmd );                  }
        else if ( command.is(INFO) )        { this.info ( user, cmd );                      }
        else if ( command.is(SOP) )         { this.access ( SOP, user, cmd );               }
        else if ( command.is(AOP) )         { this.access ( AOP, user, cmd );               }
        else if ( command.is(AKICK) )       { this.access ( AKICK, user, cmd );             }
        else if ( command.is(OP) )          { this.op ( user, cmd );                        }
        else if ( command.is(DEOP) )        { this.deop ( user, cmd );                      }
        else if ( command.is(MDEOP) )       { this.mDeop ( user, cmd );                     }
        else if ( command.is(MKICK) )       { this.mKick ( user, cmd );                     }
        else if ( command.is(UNBAN) )       { this.unban ( user, cmd );                     }
        else if ( command.is(INVITE) )      { this.invite ( user, cmd );                    }
        else if ( command.is(WHY) )         { this.why ( user, cmd );                       }
        else if ( command.is(ACCESSLOG) )   { this.accesslog ( user, cmd );                 }
        else if ( command.is(CHANLIST) )    { this.chanList ( user, cmd );                  }
        else if ( command.is(LISTOPS) )     { this.listOps ( user, cmd );                   }
        else if ( command.is(TOPICLOG) )    { this.topiclog ( user, cmd );                  }
        else if ( command.is(LIST) )        { this.list ( user, cmd );                      }
        else if ( command.is(MARK) )        { this.changeFlag ( MARK, user, cmd );          }
        else if ( command.is(FREEZE) )      { this.changeFlag ( FREEZE, user, cmd );        }
        else if ( command.is(CLOSE) )       { this.changeFlag ( CLOSE, user, cmd );         }
        else if ( command.is(HOLD) )        { this.changeFlag ( HOLD, user, cmd );          }
        else if ( command.is(AUDITORIUM) )  { this.changeFlag ( AUDITORIUM, user, cmd );    }
        else if ( command.is(DELETE) )      { this.delete ( user, cmd );                    }
        else if ( command.is(GETPASS) )     { this.getPass ( user, cmd );                   }
        else {
            this.noMatch ( user, cmd[3] );
        }
    }
    
    /**
     *
     * @param user
     * @param cmd
     */
    public void chanList ( User user, String[] cmd ) {
        //:Pintuz PRIVMSG ChanServ@services.avade.net :chanlist <nick>
        //  0      1        2                          3         4      5          = 6
        NickInfo ni;
        ArrayList<HashString> lists = new ArrayList<>();
        lists.add ( new HashString ( "Founder" ) );
        lists.add ( new HashString ( "Sop" ) );
        lists.add ( new HashString ( "Aop" ) );
        
        if ( cmd.length == 5 && user.isAtleast ( CSOP ) ) {
            if  ( ( ni = NickServ.findNick ( cmd[4] ) ) != null ) {
                lists.add ( new HashString ( "AKick" ) );
            } else {
                this.service.sendMsg ( user, "Nick not registered." );
                return;
            }
        } else if ( ( ni = NickServ.findNick ( user.getName() ) ) == null || ! user.isIdented ( ni ) ) {
            this.service.sendMsg ( user, "Access denied." );
            return;
        }
  
        this.service.sendMsg ( user, "*** ChanList for: "+ni.getName()+" ***" );
        
        for ( HashString list : lists ) {

            if ( ni.getChanAccess(list).size() > 0 ) {
                if ( list == AKICK ) {
                    this.service.sendMsg ( user, " " );
                    this.service.sendMsg ( user, "--- IRCop ---" );
                }
                this.service.sendMsg ( user, list+":" );
                for ( ChanInfo ci : ni.getChanAccess(list) ) {
                    this.service.sendMsg ( user, "  - "+ci.getName() );
                }
            }
        }
        this.service.sendMsg ( user, "*** End of List ***" );
        this.snoop.msg ( true, ni.getName ( ), user, cmd );
    }
     
    /**
     *
     * @param user
     * @param cmd
     */
    public void op ( User user, String[] cmd )  {
        //:Pintuz PRIVMSG ChanServ@services.avade.net :op #avade dreamhealer
        //  0      1        2                          3    4      5          = 6 
        
        CMDResult result = this.validateCommandData ( user, OP, cmd );

        if ( result.was(SYNTAX_ERROR) ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "OP <#Chan> [<nick>]" ) );
            this.snoop.msg ( false, SYNTAX_ERROR, user.getName(), user, cmd );
            return;
        
        } else if ( result.was(CHAN_NOT_EXIST) ) {
            this.service.sendMsg (user, output ( CHAN_NOT_EXIST, result.getString1().getString() ) ); 
            this.snoop.msg (false, CHAN_NOT_EXIST, result.getString1 ( ), user, cmd );
            return;
        
        } else if ( result.was(CHAN_NOT_REGISTERED) ) {
            this.service.sendMsg (user, output ( CHAN_NOT_REGISTERED, result.getString1().getString() ) ); 
            this.snoop.msg (false, CHAN_NOT_REGISTERED, result.getString1 ( ), user, cmd );
            return;
        
        } else if ( result.was(CHAN_IS_CLOSED) ) {
            this.service.sendMsg (user, output ( CHAN_IS_CLOSED, result.getChanInfo().getNameStr() ) ); 
            this.snoop.msg (false, CHAN_IS_CLOSED, result.getChanInfo().getName ( ), user, cmd );
            return;
        
        } else if ( result.was(CHAN_IS_FROZEN) ) {
            this.service.sendMsg (user, output (CHAN_IS_FROZEN, result.getChanInfo().getNameStr() ) ); 
            this.snoop.msg (false, CHAN_IS_FROZEN, result.getChanInfo().getName ( ), user, cmd );
            return;
        
        } else if ( result.was(ACCESS_DENIED) ) {
            this.service.sendMsg ( user, output ( ACCESS_DENIED, "" ) ); 
            this.snoop.msg ( false, ACCESS_DENIED, user.getName(), user, cmd );
            return;
            
        } else if ( result.was(NICK_NOT_PRESENT) ) {
            this.service.sendMsg (user, output (NICK_NOT_PRESENT, result.getString1().getString() ) ); 
            this.snoop.msg (false, NICK_NOT_PRESENT, result.getString1 ( ), user, cmd );
            return;
        }
         
        Chan c = result.getChan ( );
        ChanInfo ci = result.getChanInfo ( );
        NickInfo ni = result.getNick ( );
        User targetUser = result.getTarget ( );
        NickInfo targetNick;

        /* Lets execute */
        if ( targetUser == null )  {
            targetUser = user;
        }
        if ( targetUser.is(ni) || targetUser.is(user) )  {
            /* oping himself */
            this.service.sendMsg (user, output (NICK_OP, targetUser.getNameStr(), ci.getNameStr() ) ); 
            this.snoop.msg (true, NICK_OP, targetUser.getName(), user, cmd );
            Handler.getChanServ().opUser (c, targetUser );

        } else {
            /* oping someone else */
            if ( ci.isSet ( IDENT ) ) {
                /* ident isSet on */
                if ( ( targetNick = ci.getNickByUser (targetUser ) ) == null )  {
                    /* no access nick */
                    this.service.sendMsg (user, output (NICK_NOT_IDENTED_OP, targetUser.getNameStr() ) );
                    this.snoop.msg (false, NICK_NOT_IDENTED_OP, targetUser.getName(), user, cmd );

                } else if ( targetNick.isSet ( NEVEROP ) ) {
                    /* never wants op */
                    this.service.sendMsg (user, output (NICK_NEVEROP, targetUser.getNameStr() ) );
                    this.snoop.msg (false, NICK_NEVEROP, targetUser.getName(), user, cmd );

                } else {
                    if ( ci.isSet ( VERBOSE ) ) {
                        this.service.sendOpMsg (ci, output (NICK_VERBOSE_OP, user.getNameStr(), targetUser.getNameStr(), ci.getNameStr() ) );
                    }
                    this.service.sendMsg (user, output (NICK_OP, targetUser.getNameStr(), ci.getNameStr() ) );
                    this.snoop.msg (true, NICK_OP, targetUser.getName(), user, cmd );
                    Handler.getChanServ().opUser (c, targetUser );
                }
            } else {
                /* ident isSet off */
                if ( ci.isSet ( VERBOSE )  )  {
                    this.service.sendOpMsg (ci, output (NICK_NEVEROP, user.getNameStr(), targetUser.getNameStr(), ci.getNameStr() ) );
                }
                this.service.sendMsg (user, output (NICK_OP, targetUser.getNameStr(), ci.getNameStr() ) );
                this.snoop.msg (true, NICK_OP, targetUser.getName(), user, cmd );
                Handler.getChanServ().opUser (c, targetUser );
            }
        } 
        ci.changed(LASTUSED);        
    }

    /**
     *
     * @param user
     * @param cmd
     */
    public void deop ( User user, String[] cmd )  {
        //:Pintuz PRIVMSG ChanServ@services.avade.net :op #avade dreamhealer
        //  0      1        2                          3    4      5          = 6
  
        CMDResult result = this.validateCommandData ( user, DEOP, cmd );
        
        if ( result.was(SYNTAX_ERROR) ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "DEOP <#Chan> [<nick>]" ) ); 
            this.snoop.msg ( false, SYNTAX_ERROR, user.getName(), user, cmd );
            return;
        
        } else if ( result.was(CHAN_NOT_EXIST) ) {
            this.service.sendMsg (user, output (CHAN_NOT_EXIST, result.getString1().getString() ) ); 
            this.snoop.msg (false, CHAN_NOT_EXIST, result.getString1 ( ), user, cmd );
            return;
            
        } else if ( result.was(CHAN_NOT_REGISTERED) ) {
            this.service.sendMsg (user, output (CHAN_NOT_REGISTERED, result.getString1().getString() ) ); 
            this.snoop.msg (false, CHAN_NOT_REGISTERED, result.getString1 ( ), user, cmd );
            return;
            
        } else if ( result.was(CHAN_IS_CLOSED) ) {
            this.service.sendMsg (user, output (CHAN_IS_CLOSED, result.getChanInfo().getNameStr() ) ); 
            this.snoop.msg (false, CHAN_IS_CLOSED, result.getChanInfo().getName ( ), user, cmd );
            return;
            
        } else if ( result.was(CHAN_IS_FROZEN) ) {
            this.service.sendMsg (user, output (CHAN_IS_FROZEN, result.getChanInfo().getNameStr() ) ); 
            this.snoop.msg (false, CHAN_IS_FROZEN, result.getChanInfo().getName ( ), user, cmd );
            return;
            
        } else if ( result.was(ACCESS_DENIED) ) {
            this.service.sendMsg ( user, output ( ACCESS_DENIED, "" ) ); 
            this.snoop.msg ( false, ACCESS_DENIED, user.getName(), user, cmd );
            return;
            
        } else if ( result.was(NICK_NOT_PRESENT) ) {
            this.service.sendMsg (user, output (NICK_NOT_PRESENT, result.getString1().getString() ) ); 
            this.snoop.msg (false, NICK_NOT_PRESENT, result.getString1 ( ), user, cmd );
            return;
        }        
         
        Chan c = result.getChan ( );
        ChanInfo ci = result.getChanInfo ( );
        NickInfo ni = result.getNick ( );
        User targetUser = result.getTarget ( );
        /* Lets execute */
        if ( targetUser == null )  {
            targetUser = user;
        }
        if ( targetUser.is(ni) || targetUser.is(user) )  {
            /* deoping himself */
            Handler.getChanServ().deOpUser ( c, user );
            this.snoop.msg ( true, NICK_DEOP, user.getName(), user, cmd );

        } else {
            /* oping someone else */ 

            if ( ci.isSet ( VERBOSE )  )  {
                this.service.sendOpMsg (ci, output ( NICK_VERBOSE_DEOP, user.getNameStr(), targetUser.getNameStr(), ci.getNameStr() ) );
            }
            this.service.sendMsg (user, output ( NICK_DEOP, targetUser.getNameStr(), ci.getNameStr() ) );
            Handler.getChanServ().deOpUser (c, targetUser );
            this.snoop.msg (true, NICK_DEOP, targetUser.getName(), user, cmd );
        }
        ci.changed(LASTUSED);        
    }
 
    /**
     *
     * @param user
     */
    public void help ( User user )  {
        this.service.sendMsg ( user, output ( CMD_NOT_FOUND_ERROR, "" )  );
        this.service.sendMsg ( user, output ( SHOW_HELP, this.service.getNameStr() ) );
    }

    /**
     *
     * @param user
     * @param cmd
     */
    public void register ( User user, String[] cmd )  { /* DONE? */
        // :DreamHea1er PRIVMSG ChanServ@services.sshd.biz :register chan pass description
        //       0         1              2                     3      4    5      6        = 7
         
        CMDResult result = this.validateCommandData ( user, REGISTER, cmd );
        
        if ( result.was(SYNTAX_ERROR) ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "REGISTER <#Chan> <password> <description>" ) ); 
            this.snoop.msg ( false, SYNTAX_ERROR, user.getName(), user, cmd );
            return; 
            
        } else if ( result.was(NICK_NOT_REGISTERED) ) {
            this.service.sendMsg (user, output (NICK_NOT_REGISTERED, user.getNameStr() ) ); 
            this.snoop.msg (false, NICK_NOT_REGISTERED, user.getNameStr(), user, cmd );
            return;
            
        } else if ( result.was(CHAN_NOT_EXIST) ) {
            this.service.sendMsg (user, output (CHAN_NOT_EXIST, result.getString1().getString() ) ); 
            this.snoop.msg (false, CHAN_NOT_EXIST, result.getString1 ( ), user, cmd );
            return;
            
        } else if ( result.was(CHAN_IS_RELAY) ) {
            this.service.sendMsg (user, output (CHAN_IS_RELAY, cmd[4] ) ); 
            this.snoop.msg (false, CHAN_IS_RELAY, cmd[4], user, cmd );
            return;
                   
        } else if ( result.was(CHAN_ALREADY_REGGED) ) {
            this.service.sendMsg (user, output (CHAN_ALREADY_REGGED, result.getChanInfo().getNameStr() ) ); 
            this.snoop.msg (false, CHAN_ALREADY_REGGED, result.getChanInfo().getNameStr(), user, cmd );
            return;
            
        } else if ( result.was(USER_NOT_OP) ) {
            this.service.sendMsg (user, output (USER_NOT_OP, result.getChan().getNameStr() ) ); 
            this.snoop.msg (false, USER_NOT_OP, result.getChan().getNameStr(), user, cmd );
            return;
        }
        
        Chan c = result.getChan ( );
        ChanInfo ci = result.getChanInfo ( );
        NickInfo ni = result.getNick ( );
        String description = Handler.cutArrayIntoString ( cmd, 6 );
        Topic topic; 
        
        if ( c.getTopic() != null ) {
            topic = c.getTopic();
        } else {
            topic = new Topic ("", ni.getNameStr(), System.currentTimeMillis());
        }
        
        ci = new ChanInfo ( c.getNameStr(), ni, cmd[5], description, topic );
        ChanServ.addToWorkList ( REGISTER, ci );
        ChanServ.addChan ( ci );
        
        ci.setModeLock("+nt");
        ci.set ( TOPICLOCK, OFF );
        ci.set ( IDENT, ON );
        ci.set ( OPGUARD, ON );
        ci.getChanges().change ( TOPIC );
        ci.setChanFlag( new CSFlag ( ci.getNameStr() ) );
        
        CSLogEvent log = new CSLogEvent ( ci.getName(), REGISTER, ci.getFounder().getString ( FULLMASK ), ci.getFounder().getNameStr() );
        ChanServ.addLog ( log );

        CSAccessLogEvent csaLog = new CSAccessLogEvent ( ci.getName(), FOUNDER, "", ci.getFounder().getString ( FULLMASK ) );
        ChanServ.addAccessLog ( csaLog );
        
        this.service.sendMsg ( user, output ( REGISTER_DONE, ci.getNameStr()  )  );
        this.service.sendMsg ( user, f.b ( ) +output ( REGISTER_SEC, "" ) +f.b ( )  );
        user.getSID().add ( ci ); /* identified to the channel */
        cmd[5] = "pass_redacted";
        this.snoop.msg ( true, REGISTER_DONE, ci.getName ( ), user, cmd );
        ci.changed(LASTUSED);
    }

    /**
     *
     * @param user
     * @param cmd
     */
    public void identify ( User user, String[] cmd )  {
        // :DreamHea1er PRIVMSG NickServ@services.sshd.biz :identify #chan moew   = 6
        //       0         1               2                    3      4     5
        CMDResult result = this.validateCommandData ( user, IDENTIFY, cmd );
        
        if ( cmd.length >= 6 ) {
            cmd[5] = "pass_redacted";
        }
        
        if ( result.was(SYNTAX_ERROR) ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "IDENTIFY <#Chan> <password>" ) ); 
            this.snoop.msg ( false, SYNTAX_ERROR, user.getName ( ), user, cmd );
            return;
        
        } else if ( result.was(CHAN_NOT_REGISTERED) ) {
            this.service.sendMsg (user, output (CHAN_NOT_REGISTERED, result.getString1().getString() ) ); 
            this.snoop.msg (false, CHAN_NOT_REGISTERED, result.getString1 ( ), user, cmd );
            return;
        
        } else if ( result.was(CHAN_IS_FROZEN) ) {
            this.service.sendMsg (user, output (CHAN_IS_FROZEN, result.getChanInfo().getNameStr ( ) ) ); 
            this.snoop.msg (false, CHAN_IS_FROZEN, result.getChanInfo().getName ( ), user, cmd );
            return;
        
        } else if ( result.was(CHAN_IS_CLOSED) ) {
            this.service.sendMsg (user, output (CHAN_IS_CLOSED, result.getChanInfo().getNameStr ( ) ) ); 
            this.snoop.msg (false, CHAN_IS_CLOSED, result.getChanInfo().getName ( ), user, cmd );
            return;
        
        } else if ( result.was(IS_THROTTLED) ) {
            this.service.sendMsg (user, output (IS_THROTTLED, result.getChanInfo().getNameStr ( ) ) );
            this.snoop.msg (false, IS_THROTTLED, result.getChanInfo().getName ( ), user, cmd );
            return;
        
        } else if ( result.was(INVALID_PASSWORD) ) {
            this.service.sendMsg ( user, output ( INVALID_PASSWORD, "" ) ); 
            this.snoop.msg ( false, INVALID_PASSWORD, user.getName ( ), user, cmd );
            return; 
        }
         
        ChanInfo ci = result.getChanInfo ( );
        this.service.sendMsg ( user, output ( PASSWD_ACCEPTED, ci.getNameStr()  )  ); 
        user.getSID().add ( ci );
        this.snoop.msg ( true, PASSWD_ACCEPTED, ci.getName(), user, cmd );
        ci.changed(LASTUSED);          
    } 
 
    private void drop ( User user, String[] cmd ) {
        // :DreamHea1er PRIVMSG NickServ@services.sshd.biz :drop #chan pass   = 6
        //       0         1               2                 3      4     5        
    
        CMDResult result = this.validateCommandData ( user, DROP, cmd );
        
        if ( result.was(SYNTAX_ERROR) ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "DROP <#Chan> <password>" ) ); 
            this.snoop.msg ( false, INVALID_PASSWORD, user.getName ( ), user, cmd );
            return;
            
        } else if ( result.was(CHAN_NOT_REGISTERED) ) {
            this.service.sendMsg (user, output (CHAN_NOT_REGISTERED, result.getString1().getString() ) ); 
            this.snoop.msg (false, CHAN_NOT_REGISTERED, result.getString1 ( ), user, cmd );
            return;
            
        } else if ( result.was(CHAN_IS_FROZEN) ) {
            this.service.sendMsg (user, output (CHAN_IS_FROZEN, result.getChanInfo().getNameStr ( ) ) ); 
            this.snoop.msg (false, CHAN_IS_FROZEN, result.getChanInfo().getName ( ), user, cmd );
            return;
            
        } else if ( result.was(CHAN_IS_CLOSED) ) {
            this.service.sendMsg (user, output (CHAN_IS_CLOSED, result.getChanInfo().getNameStr ( ) ) ); 
            this.snoop.msg (false, CHAN_IS_CLOSED, result.getChanInfo().getName ( ), user, cmd );
            return;
            
        } else if ( result.was(INVALID_PASSWORD) ) {
            this.service.sendMsg ( user, output ( INVALID_PASSWORD, "" ) ); 
            this.snoop.msg ( false, INVALID_PASSWORD, user.getName ( ), user, cmd );
            return;
            
        } else if ( result.was(IS_MARKED) ) {
            this.service.sendMsg (user, output (IS_MARKED, result.getChanInfo().getNameStr ( ) ) ); 
            this.snoop.msg (false, IS_MARKED, result.getChanInfo().getName ( ), user, cmd );
            return;
        }
         
        ChanInfo ci = result.getChanInfo ( );
        Handler.getChanServ().dropChan ( ci );
        cmd[5] = "pass_redacted";
        this.service.sendMsg ( user, output ( CHANNELDROPPED, ci.getNameStr() ) );
        CSLogEvent log = new CSLogEvent ( ci.getName(), DROP, user.getFullMask(), "" );
        ChanServ.addLog ( log );
        this.snoop.msg ( true, CHANNELDROPPED, ci.getName(), user, cmd );

    }

    
    private void delete ( User user, String[] cmd ) {
        // :DreamHea1er PRIVMSG NickServ@services.sshd.biz :delete #chan   = 5
        //       0         1               2                 3      4           
      
        CMDResult result = this.validateCommandData ( user, DELETE, cmd );
        
        if ( result.was(SYNTAX_ERROR) ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "DELETE <#chan>" ) );
            this.snoop.msg ( false, SYNTAX_ERROR, user.getName(), user, cmd );
            return;
            
        } else if ( result.was(ACCESS_DENIED) ) {
            this.service.sendMsg (user, output (ACCESS_DENIED, result.getString1().getString() ) ); 
            this.snoop.msg ( false, ACCESS_DENIED, user.getName(), user, cmd );
            return;

        } else if ( result.was(CHAN_NOT_REGISTERED) ) {
            this.service.sendMsg ( user, output ( CHAN_NOT_REGISTERED, cmd[4] ) );
            this.snoop.msg ( false, CHAN_NOT_REGISTERED, new HashString ( cmd[4] ), user, cmd );
            return;

        } else if ( result.was(IS_MARKED) ) {
            this.service.sendMsg ( user, output ( IS_MARKED, cmd[4] ) );
            this.snoop.msg ( false, IS_MARKED, new HashString ( cmd[4] ), user, cmd );
            return;

        }
    
        ChanInfo ci = result.getChanInfo ( );
        Handler.getChanServ().dropChan ( ci );
        this.service.sendMsg ( user, output ( CHANNELDELETED, ci.getNameStr() ) );
        CSLogEvent log = new CSLogEvent ( ci.getName(), DELETE, user.getFullMask(), user.getOper().getNameStr() );
        ChanServ.addLog ( log );
        this.snoop.msg ( true, CHANNELDELETED, ci.getName(), user, cmd );
    }

    
    
    private void info ( User user, String[] cmd ) {
        // :DreamHea1er PRIVMSG NickServ@services.sshd.biz :info dreamhealer
        //      0          1            2                     3       4     = 5
        
        CMDResult result = this.validateCommandData ( user, INFO, cmd );

        if ( result.was(SYNTAX_ERROR) ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "INFO <#Chan>" ) );
            this.snoop.msg ( false, SYNTAX_ERROR, user.getName(), user, cmd );
            return;
            
        } else if ( result.was(CHAN_NOT_REGISTERED) ) {
            this.service.sendMsg (user, output (CHAN_NOT_REGISTERED, result.getString1().getString() ) ); 
            this.snoop.msg (false, CHAN_NOT_REGISTERED, result.getString1 ( ), user, cmd );
            return;
            
        } else if ( result.was(CHAN_IS_FROZEN) ) {
            this.service.sendMsg (user, output (CHAN_IS_FROZEN, result.getChanInfo().getNameStr ( ) ) ); 
            this.snoop.msg (false, CHAN_IS_FROZEN, result.getChanInfo().getName ( ), user, cmd );
            return;
            
        } else if ( result.was(CHAN_IS_CLOSED) ) {
            this.service.sendMsg (user, output (CHAN_IS_CLOSED, result.getChanInfo().getNameStr ( ) ) ); 
            this.snoop.msg (false, CHAN_IS_CLOSED, result.getChanInfo().getName ( ), user, cmd );
            return;
        }
        
        /* We found the nickname */
        ChanInfo ci = result.getChanInfo ( );
        NickInfo founder = ci.getFounder ( ); 
        if ( founder == null )
            System.out.println("founder = null");
        
        this.showStart ( true, user, ci, f.b ( ) +"Info for: "+f.b ( )  ); 
     
        this.service.sendMsg ( user, "     Founder: "+founder.getName() +" ("+founder.getString ( USER )+"@"+founder.getString ( HOST )+") " );
        this.service.sendMsg ( user, "   Mode Lock: "+ci.getSettings().getModeLock().getModes ( ) );
        this.service.sendMsg ( user, "       Topic: "+ci.getString ( TOPIC )+" ("+ci.getString(TOPICNICK)+")");
        this.service.sendMsg ( user, " Description: "+ci.getString ( DESCRIPTION ) );
        this.service.sendMsg ( user, "    Settings: "+ci.getSettings().getInfoStr ( ) );
        this.service.sendMsg ( user, "  Registered: "+ci.getString ( REGTIME ) );
        this.service.sendMsg ( user, "   Last used: "+ci.getString ( LASTUSED ) );
        this.service.sendMsg ( user, "    Time now: "+dateFormat.format ( new Date ( ) ) );
  
        if ( user.isAtleast ( IRCOP ) ) {
            if ( ci.isSet ( FROZEN ) || ci.isSet ( MARKED ) || ci.isSet ( HELD ) || ci.isSet ( CLOSED ) || ci.isSet ( AUDITORIUM ) ) 
                this.service.sendMsg ( user, f.b()+"   --- IRCop ---" );
            if ( ci.isSet ( FROZEN ) )
                this.service.sendMsg ( user, f.b()+"      FROZEN: "+ci.getSettings().getInstater ( FREEZE ) );
            if ( ci.isSet ( MARKED ) )
                this.service.sendMsg ( user, f.b()+"      MARKED: "+ci.getSettings().getInstater ( MARK ) );
            if ( ci.isSet ( CLOSE ) )
                this.service.sendMsg ( user, f.b()+"      CLOSED: "+ci.getSettings().getInstater ( CLOSE ) );
            if ( ci.isSet ( HELD ) )
                this.service.sendMsg ( user, f.b()+"        HELD: "+ci.getSettings().getInstater ( HOLD ) );
            if ( ci.isSet ( AUDITORIUM ) ) 
                this.service.sendMsg ( user, f.b()+"  AUDITORIUM: "+ci.getSettings().getInstater ( AUDITORIUM ) );
        }
        
        this.showEnd ( user, "Info" );
        this.snoop.msg ( true, CHAN_INFO, ci.getName ( ), user, cmd );
    }

    private void access ( HashString access, User user, String[] cmd ) {
        if ( isShorterThanLen ( 6, cmd ) ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "<AOP|SOP> <#chan> <ADD|DEL|LIST> [<nick|#NUM>]" ) );
            return;
        }
        HashString command = new HashString ( cmd[5] );
        if ( command.is(LIST) ) {
            doListAccess ( user, cmd, access );
        
        } else if ( command.is(WIPE) ) {
            doWipeAccess ( user, cmd, access );
        
        } else {
            doAccess ( access, user, cmd );
        } 
    }
     
    /**
     *
     * @param user
     * @param cmd
     * @param access
     */
    public void doWipeAccess ( User user, String[] cmd, HashString access )  {
        //:Pintuz PRIVMSG ChanServ@services.avade.net :aop #avade list
        //   0       1        2                         3    4     5   = 6
        ChanInfo ci;
        NickInfo ni;
        
        if ( ! CSDatabase.checkConn ( )  )  {
            Handler.getChanServ().sendMsg ( user, "Database offline. Please try again in a little while." );
            return;
        }
        
        if ( cmd.length < 6 || ! ( access == AOP || access == SOP || access == AKICK )  )  {
            /* too short or wrong*/
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "AKICK <#chan> <ADD|DEL|LIST> [<nick|#NUM>]" )  );
            this.snoop.msg ( false, SYNTAX_ERROR, user.getName(), user, cmd );
        } 
        
        ci = ChanServ.findChan ( cmd[4] );
        ni = ci.getNickByUser ( user );
       
        if ( ni == null )  {
            /* no nick with access */
            this.service.sendMsg ( user, output ( ACCESS_DENIED, "NickServ" ) );
            this.snoop.msg ( false, ACCESS_DENIED, user.getName(), user, cmd );

        } else if ( ci == null )  {
            /* no channel */
            this.service.sendMsg ( user, output ( CHAN_NOT_REGISTERED, cmd[4] ) );
            this.snoop.msg ( false, CHAN_NOT_REGISTERED, new HashString ( cmd[4] ), user, cmd );
        
        } else if ( ! ci.isFounder ( ni )  )  {
            /* does not have access */
            this.service.sendMsg ( user, output ( ACCESS_DENIED, ci.getNameStr ( ) ) );
            this.snoop.msg ( false, ACCESS_DENIED, ci.getName ( ), user, cmd );

        } else {
            String acc = null;
            acc = accessToString ( access );
            ChanServ.addToWorkList ( CHANGE, ci );

            if ( CSDatabase.wipeAccessList ( ci, access )  )  {
                ci.wipeAccessList ( access );
                this.service.sendMsg ( user, output ( LIST_WIPED, acc, ci.getNameStr ( ) ) );
                this.service.sendOpMsg ( ci, output ( LIST_VERBOSE_WIPED, ni.getNameStr ( ), acc, ci.getNameStr ( ) ) );
                this.snoop.msg ( true, LIST_WIPED, ci.getName ( ), user, cmd );

            } else {
                this.service.sendMsg ( user, output ( LIST_NOT_WIPED, acc, ci.getNameStr ( ) ) );
                this.snoop.msg ( false, LIST_NOT_WIPED, ci.getName ( ), user, cmd );

            }
        }
    }
    
    /**
     *
     * @param user
     * @param cmd
     * @param access
     */
    public void doListAccess ( User user, String[] cmd, HashString access ) {
        ChanInfo ci;
        NickInfo ni;
        
        CMDResult result = this.validateCommandData ( user, LISTACCESS, cmd );
        String listName = getListName ( access );
        
        
        if ( result.was(SYNTAX_ERROR) ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, listName+" <#Chan>" ) );
            this.snoop.msg ( false, SYNTAX_ERROR, user.getName ( ), user, cmd );
            return;
            
        } else if ( result.was(CHAN_NOT_REGISTERED) ) {
            this.service.sendMsg (user, output (CHAN_NOT_REGISTERED, result.getString1().getString() ) ); 
            this.snoop.msg (false, CHAN_NOT_REGISTERED, result.getString1 ( ), user, cmd );
            return;
            
        } else if ( result.was(CHAN_IS_FROZEN) ) {
            this.service.sendMsg (user, output (CHAN_IS_FROZEN, result.getChanInfo().getNameStr ( ) ) ); 
            this.snoop.msg (false, CHAN_IS_FROZEN, result.getChanInfo().getName ( ), user, cmd );
            return;
            
        } else if ( result.was(CHAN_IS_CLOSED) ) {
            this.service.sendMsg (user, output (CHAN_IS_CLOSED, result.getChanInfo().getNameStr ( ) ) ); 
            this.snoop.msg (false, CHAN_IS_CLOSED, result.getChanInfo().getName ( ), user, cmd );
            return;
            
        } else if ( result.was(ACCESS_DENIED) ) {
            this.service.sendMsg ( user, output ( ACCESS_DENIED, "" ) ); 
            this.snoop.msg ( false, ACCESS_DENIED, user.getName ( ), user, cmd );
            return;
        }
         
        ci = result.getChanInfo ( );
        String accStr = accessToString ( access );
        this.showStart ( true, user, ci, f.b ( ) +accStr+" list for: "+f.b ( )  ); 
        ArrayList<CSAcc> list = ci.getAccessList ( access );
        for ( CSAcc acc : list )  {     
            if ( acc.getNick ( ) != null )  {
                if ( acc.getLastOped() != null ) {
                    this.service.sendMsg ( user,  " - "+acc.getNick().getName()+" ("+acc.getNick().getString ( FULLMASK )+") - [LastOped: "+acc.getLastOped()+"]" );
                } else {
                    this.service.sendMsg ( user,  " - "+acc.getNick().getName()+" ("+acc.getNick().getString ( FULLMASK )+")" );
                }
            } else {
                this.service.sendMsg ( user,  " - "+acc.getMask ( )+" (mask)" );
            }
        }
        this.showEnd ( user, accStr+" list" );
        this.snoop.msg ( true, ACCESS_LIST, ci.getName ( ), user, cmd );
    }

    private String getListName ( HashString access ) {
        if      ( access.is(SOP) )      { return "Sop";     }
        else if ( access.is(AOP) )      { return "Aop";     }
        else if ( access.is(AKICK) )    { return "AKick";   }
        else {
            return "";
        }
    }    
    private HashString getAddList ( HashString access ) {
        if      ( access.is(SOP) )      { return ADDSOP;    }
        else if ( access.is(AOP) )      { return ADDAOP;    }
        else if ( access.is(AKICK) )    { return ADDAKICK;  }
        else {
            return null;
        }
    }
    private HashString getDelList ( HashString access ) {
        if      ( access.is(SOP) )      { return DELSOP;    }
        else if ( access.is(AOP) )      { return DELAOP;    }
        else if ( access.is(AKICK) )    { return DELAKICK;  }
        else {
            return null;
        }
    }
    private void doAccess ( HashString access, User user, String[] cmd ) {
        //:Pintuz PRIVMSG ChanServ@services.avade.net :akick #avade add <nick|fullmask>
        //:DreamH PRIVMSG ChanServ@services.avade.net :akick #friends del 2
        //   0       1        2                         3     4       5   6    = 7
  
        CMDResult result = this.validateCommandData ( user, access, cmd );
        String listName = getListName ( access );
        
        if ( result.was(SYNTAX_ERROR) ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, listName+" <#Chan> <add|del|list> <nick|mask|#num>" ) );
            this.snoop.msg ( false, SYNTAX_ERROR, user.getName ( ), user, cmd );
            return;
            
        } else if ( result.was(NICK_NOT_REGISTERED) ) {
            this.service.sendMsg (user, output (NICK_NOT_REGISTERED, result.getString1().getString() ) ); 
            this.snoop.msg (false, NICK_NOT_REGISTERED, result.getString1 ( ), user, cmd );
            return; 
            
        } else if ( result.was(CHAN_NOT_REGISTERED) ) {
            this.service.sendMsg (user, output (CHAN_NOT_REGISTERED, result.getString1().getString() ) ); 
            this.snoop.msg (false, CHAN_NOT_REGISTERED, result.getString1 ( ), user, cmd );
            return;
            
        } else if ( result.was(CHAN_IS_FROZEN) ) {
            this.service.sendMsg (user, output (CHAN_IS_FROZEN, result.getChanInfo().getNameStr ( ) ) ); 
            this.snoop.msg (false, CHAN_IS_FROZEN, result.getChanInfo().getName ( ), user, cmd );
            return;
            
        } else if ( result.was(CHAN_IS_CLOSED) ) {
            this.service.sendMsg (user, output (CHAN_IS_CLOSED, result.getChanInfo().getNameStr() ) ); 
            this.snoop.msg (false, CHAN_IS_CLOSED, result.getChanInfo().getName(), user, cmd );
            return;

        } else if ( result.was(ACCESS_DENIED) ) {
            this.service.sendMsg (user, output (ACCESS_DENIED, result.getString1().getString() ) ); 
            this.snoop.msg (false, ACCESS_DENIED, result.getString1(), user, cmd );
            return;
        
        } else if ( result.was(NOT_ENOUGH_ACCESS) ) {
            this.service.sendMsg ( user, output ( NOT_ENOUGH_ACCESS, "" ) ); 
            this.snoop.msg ( false, NOT_ENOUGH_ACCESS, user.getName ( ), user, cmd );
            return;
               
        } else if ( result.was(NICK_HAS_NOOP) ) {
            this.service.sendMsg ( user, output ( NICK_HAS_NOOP, result.getNick2().getNameStr() ) ); 
            this.snoop.msg ( false, NICK_HAS_NOOP, user.getName ( ), user, cmd );
            return;
        
        } else if ( result.was(XOP_NOT_FOUND) ) {
            this.service.sendMsg ( user, output ( XOP_NOT_FOUND, "" ) ); 
            this.snoop.msg ( false, XOP_NOT_FOUND, user.getName ( ), user, cmd );
            return;
        
        } else if ( result.was(XOP_ADD_FAIL) ) {
            this.service.sendMsg ( user, output ( XOP_ADD_FAIL, "" ) ); 
            this.snoop.msg ( false, XOP_ADD_FAIL, user.getName ( ), user, cmd );
            return;
        
        } else if ( result.was(XOP_ALREADY_PRESENT) ) {
            this.service.sendMsg (user, output (XOP_ALREADY_PRESENT, result.getString1().getString(), this.getListStr ( access ) ) ); 
            this.snoop.msg (false, XOP_ALREADY_PRESENT, result.getString1(), user, cmd );
            return;
        }
         
        ChanInfo ci = result.getChanInfo();
        Chan c = Handler.findChan(ci.getName());
        NickInfo ni = result.getNick();
        NickInfo ni2;
        String what;
        String mask = null;
        HashString command = result.getCommand();
        HashString subcommand = result.getSubCommand();
        CSAcc acc = result.getAcc();
              
        if ( ( ni2 = result.getNick2() ) == null ) {
            mask = result.getString1().getString(); 
        } 
        
        if ( acc != null && acc.getNick() != null ) {
            what = acc.getNick().getNameStr();
        } else if ( acc != null && acc.getMask() != null ) {
            what = acc.getMask().getString();
        } else {
            what = "-";
        }

        if ( acc == null ) {
            acc = new CSAcc (ni2, access, null);
            what = ni2.getNameStr();
        }
        
        if ( subcommand.is(ADD) ) {
            ci.removeFromAll ( acc );
            ci.addAccess ( command, acc );
            ci.addAccessLog ( new CSAccessLogEvent ( ci.getName(), this.getAddList(access), what, user ) );
            this.service.sendMsg ( user, output ( NICK_ADDED, what, listName ) );
            if ( ci.getSettings().is ( VERBOSE ) ) {
                this.service.sendOpMsg ( ci, output ( NICK_VERBOSE_ADDED, ni.getNameStr(), what, listName ) );
            }
            if ( command.is ( AKICK ) && ci.isSet ( AUTOAKICK ) ) {
                c.addCheckUsers();
            }
            ci.changed(subcommand);
            this.snoop.msg ( true, ACCESS_ADDED, ci.getNameStr(), user, cmd );
            
        } else if ( subcommand.is(DEL) ) {
            ci.delAccess ( access, acc );
            ci.addAccessLog ( new CSAccessLogEvent ( ci.getName(), this.getDelList(access), what, user ) );
            this.service.sendMsg ( user, output ( NICK_DELETED, what, listName ) );
            if ( ci.getSettings().is(VERBOSE) ) {
                this.service.sendOpMsg ( ci, output ( NICK_VERBOSE_DELETED, ni.getNameStr(), what, listName ) );
            }
            ci.changed(subcommand);
            this.snoop.msg ( true, ACCESS_DELETED, ci.getName(), user, cmd );
        }
        ci.changed(LASTUSED);
    }
    
    private void unban ( User user, String[] cmd )  {
        //:Pintuz PRIVMSG ChanServ@services.avade.net :unban #avade fredde
        //   0       1        2                         3     4      5     = 6 
         
        CMDResult result = this.validateCommandData ( user, UNBAN, cmd );
 
        if ( result.was(SYNTAX_ERROR) ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "UNBAN <#Chan>" ) ); 
            this.snoop.msg ( false, SYNTAX_ERROR, user.getName(), user, cmd );
            return;
            
        } else if ( result.was(CHAN_NOT_EXIST) ) {
            this.service.sendMsg (user, output (CHAN_NOT_EXIST, result.getChan().getNameStr() ) ); 
            this.snoop.msg (false, CHAN_NOT_EXIST, result.getChan().getName(), user, cmd );
            return;
            
        } else if ( result.was(CHAN_NOT_REGISTERED) ) {
            this.service.sendMsg (user, output (CHAN_NOT_REGISTERED, result.getString1().getString() ) ); 
            this.snoop.msg (false, CHAN_NOT_REGISTERED, result.getString1 ( ), user, cmd );
            return;
            
        } else if ( result.was(NICK_NOT_EXIST) ) {
            this.service.sendMsg (user, output (NICK_NOT_EXIST, result.getString1().getString() ) ); 
            this.snoop.msg (false, NICK_NOT_EXIST, result.getString1 ( ), user, cmd );
            return;
            
        } else if ( result.was(NICK_ACCESS_DENIED) ) {
            this.service.sendMsg (user, output (NICK_ACCESS_DENIED, result.getChanInfo().getNameStr ( ) ) ); 
            this.snoop.msg (false, NICK_ACCESS_DENIED, result.getChanInfo().getName ( ), user, cmd );
            return;
            
        } else if ( result.was(CHAN_IS_FROZEN) ) {
            this.service.sendMsg (user, output (CHAN_IS_FROZEN, result.getChanInfo().getNameStr ( ) ) ); 
            this.snoop.msg (false, CHAN_IS_FROZEN, result.getChanInfo().getName ( ), user, cmd );
            return;
            
        } else if ( result.was(CHAN_IS_CLOSED) ) {
            this.service.sendMsg (user, output (CHAN_IS_CLOSED, result.getChanInfo().getNameStr ( ) ) ); 
            this.snoop.msg (false, CHAN_IS_CLOSED, result.getChanInfo().getName ( ), user, cmd );
            return;
            
        }
         
        Chan c = result.getChan ( );
        ChanInfo ci = result.getChanInfo ( );
        NickInfo ni = result.getNick ( );
        User target = result.getTarget ( );

        if ( ci.isSet(VERBOSE) ) {
            this.service.sendOpMsg ( ci, output ( NICK_VERBOSE_ADDED, ni.getNameStr() , target.getNameStr() , "UnBan" )  );
        } 
        Handler.getChanServ().unBanUser ( c, target );
        this.service.sendMsg ( user, output ( CHAN_UNBAN, target.getNameStr(), ci.getNameStr() ) );
        this.snoop.msg ( true, CHAN_UNBAN, ci.getName ( ), user, cmd );
        ci.changed(LASTUSED);
    }

    private void invite ( User user, String[] cmd )  {
        //:Pintuz PRIVMSG ChanServ@services.avade.net :unban #avade
        //   0       1        2                         3     4        = 5
        
        CMDResult result = this.validateCommandData ( user, INVITE, cmd );

        if ( result.was(SYNTAX_ERROR) ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "INVITE <#Chan>" ) ); 
            this.snoop.msg ( false, SYNTAX_ERROR, user.getName ( ), user, cmd );
            return;
            
        } else if ( result.was(CHAN_NOT_EXIST) ) {
            this.service.sendMsg (user, output (CHAN_NOT_EXIST, result.getChan().getNameStr() ) ); 
            this.snoop.msg (false, CHAN_NOT_EXIST, result.getChan().getName(), user, cmd );
            return;
            
        } else if ( result.was(CHAN_NOT_REGISTERED) ) {
            this.service.sendMsg (user, output (CHAN_NOT_REGISTERED, result.getString1().getString() ) ); 
            this.snoop.msg (false, CHAN_NOT_REGISTERED, result.getString1 ( ), user, cmd );
            return;
            
        } else if ( result.was(NICK_ACCESS_DENIED) ) {
            this.service.sendMsg (user, output (NICK_ACCESS_DENIED, result.getChanInfo().getNameStr ( ) ) ); 
            this.snoop.msg (false, NICK_ACCESS_DENIED, result.getChanInfo().getName ( ), user, cmd );
            return;
            
        } else if ( result.was(CHAN_IS_FROZEN) ) {
            this.service.sendMsg (user, output (CHAN_IS_FROZEN, result.getChanInfo().getNameStr ( ) ) ); 
            this.snoop.msg (false, CHAN_IS_FROZEN, result.getChanInfo().getName ( ), user, cmd );
            return;
            
        } else if ( result.was(CHAN_IS_CLOSED) ) {
            this.service.sendMsg (user, output (CHAN_IS_CLOSED, result.getChanInfo().getNameStr ( ) ) ); 
            this.snoop.msg (false, CHAN_IS_CLOSED, result.getChanInfo().getName ( ), user, cmd );
            return;
        }
         
        Chan c = result.getChan ( );
        ChanInfo ci = result.getChanInfo ( );
        NickInfo ni = result.getNick ( ); 
        if ( ci.isSet ( VERBOSE ) ) {
            this.service.sendOpMsg ( ci, output ( NICK_INVITED, ni.getNameStr(), ci.getNameStr ( ) )  );
        } 
        Handler.getChanServ().invite ( c, user );
        this.snoop.msg ( true, NICK_INVITED, ni.getName(), user, cmd );
        ci.changed(LASTUSED);
    }
     
    /* Tells a channel staff how a user has access to a channel */
    private void why ( User user, String[] cmd )  {
        //:Pintuz PRIVMSG ChanServ@services.avade.net :why #avade Pintuz  
        //   0       1        2                         3     4     5     = 6 
  
        CMDResult result = this.validateCommandData ( user, WHY, cmd );

        if ( result.was(SYNTAX_ERROR) ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "WHY <#Chan> <nick>" ) ); 
            this.snoop.msg ( false, SYNTAX_ERROR, user.getName ( ), user, cmd );
            return;
            
        } else if ( result.was(CHAN_NOT_REGISTERED) ) {
            this.service.sendMsg (user, output (CHAN_NOT_REGISTERED, result.getChan().getNameStr() ) ); 
            this.snoop.msg (false, CHAN_NOT_REGISTERED, result.getChan().getName(), user, cmd );
            return;
            
        } else if ( result.was(NICK_NOT_EXIST) ) {
            this.service.sendMsg (user, output (NICK_NOT_EXIST, result.getString1().getString() ) ); 
            this.snoop.msg (false, NICK_NOT_EXIST, result.getString1 ( ), user, cmd );
            return;
            
        } else if ( result.was(ACCESS_DENIED) ) {
            this.service.sendMsg ( user, output ( ACCESS_DENIED, "" ) ); 
            this.snoop.msg ( false, ACCESS_DENIED, user.getName ( ), user, cmd );
            return;
        }
         
        ChanInfo ci = result.getChanInfo ( );
        User target = result.getTarget ( );
        String access = ci.getAccessByUser ( target );
        String aNick = ci.getAccessHolderByUser ( target );

        if ( aNick == null )  {
            this.service.sendMsg ( user, "User "+target.getName() +" has no access to channel "+ci.getName ( )  );
        } else {
            this.service.sendMsg ( user, "User "+target.getName() +" has "+access+" access to channel "+ci.getName ( ) +" through the nickname "+aNick );
        }
        this.snoop.msg ( true, CHAN_WHY, user.getName ( ), user, cmd );
    }
      
    /**
     *
     * @param user
     * @param cmd
     */
    public void set ( User user, String[] cmd )  {
        /* :DreamHealer PRIVMSG NickServ@services.sshd.biz :set #chan CMD ON          */
        /*      0          1               2                 3     4   5   6 = 7      */
        
        CMDResult result = this.validateCommandData ( user, SET, cmd ); 
        
        if ( result.was(SYNTAX_ERROR) ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "SET <#Chan> <option> <on|off>" ) ); 
            this.snoop.msg ( false, SYNTAX_ERROR, user.getName ( ), user, cmd );
            return; 
            
        } else if ( result.was(NICK_NOT_REGISTERED) ) {
            this.service.sendMsg (user, output (NICK_NOT_REGISTERED, result.getString1().getString() ) ); 
            this.snoop.msg (false, NICK_NOT_REGISTERED, result.getString1().getString(), user, cmd );
            return;
            
        } else if ( result.was(CHAN_NOT_REGISTERED) ) {
            this.service.sendMsg (user, output (CHAN_NOT_REGISTERED, result.getString1().getString() ) ); 
            this.snoop.msg (false, CHAN_NOT_REGISTERED, result.getString1().getString(), user, cmd );
            return;
            
        } else if ( result.was(CHAN_IS_FROZEN) ) {
            this.service.sendMsg (user, output (CHAN_IS_FROZEN, result.getChanInfo().getNameStr() ) ); 
            this.snoop.msg (false, CHAN_IS_FROZEN, result.getChanInfo().getNameStr(), user, cmd );
            return;
            
        } else if ( result.was(CHAN_IS_CLOSED) ) {
            this.service.sendMsg (user, output (CHAN_IS_CLOSED, result.getChanInfo().getNameStr() ) ); 
            this.snoop.msg (false, CHAN_IS_CLOSED, result.getChanInfo().getNameStr(), user, cmd );
            return;
            
        } else if ( result.was(ACCESS_DENIED) ) {
            this.service.sendMsg (user, output (ACCESS_DENIED, result.getString1().getString() ) ); 
            this.snoop.msg (false, ACCESS_DENIED, result.getString1().getString(), user, cmd );
            return;
        }
         
        ChanInfo ci = result.getChanInfo ( );
        Chan c;
        boolean flag = false;
          
        HashString command = new HashString ( cmd[5] );
        HashString option = new HashString ( cmd[6] );
        HashString setting = null;

        if ( option.is(ON) ) {
            flag = true;
        } else if ( option.is(OFF) ) {
            flag = false;
            setting = option;
        } else if ( option.is(AOP) ||
                    option.is(SOP) ||
                    option.is(FOUNDER) ) {
            setting = option;
        }
         
        // chan  ( name, pass, desc, topic, regstamp, stamp ) 
        // chansetting  ( name,keeptopic,topiclock,ident,opguard,restrict,verbose,mailblock,leaveops,private ) 
        if ( command.is(DESCRIPTION) ) {
                doDescription ( user, ci, cmd );
                ci.changed ( DESCRIPTION );
                this.snoop.msg ( true, SET_DESCRIPTION, ci.getName(), user, cmd );
            
        } else if ( command.is(TOPICLOCK) ) {
                doTopicLock ( user, ci, setting );
                ci.changed ( TOPICLOCK );
                this.snoop.msg ( true, SET_TOPICLOCK, ci.getName(), user, cmd );
        
        } else if ( command.is(MODELOCK) ) {
            doModeLock ( user, ci, cmd );
                ci.changed ( MODELOCK );
                this.snoop.msg ( true, SET_MODELOCK, ci.getName(), user, cmd );
        
        } else if ( command.is(KEEPTOPIC) ) {
                this.sendWillOutput ( user, flag, "keep your topic if channel goes empty.", "forget the topic if the channel goes empty." );
                ci.getSettings().set ( KEEPTOPIC, flag );
                ci.changed ( KEEPTOPIC );
                this.snoop.msg ( true, SET_KEEPTOPIC, ci.getName(), user, cmd );
        
        } else if ( command.is(IDENT) ) {
                this.sendWillOutput ( user, flag, "require channel ops to identify to their nicks.", "require channel ops to identify to their nicks." );
                ci.getSettings().set ( IDENT, flag );
                ci.changed ( IDENT );
                this.snoop.msg ( true, SET_IDENT, ci.getName(), user, cmd );
        
        } else if ( command.is(OPGUARD) ) {
                this.sendWillOutput ( user, flag, "guard channel ops.", "require ops to identify to their nicks." );
                ci.getSettings().set ( OPGUARD, flag );
                ci.changed ( OPGUARD );
                this.snoop.msg ( true, SET_OPGUARD, ci.getName(), user, cmd );
        
        } else if ( command.is(RESTRICT) ) {
                this.sendWillOutput ( user, flag, "restrict users from entering the channel.", "restrict users from entering the channel." );
                ci.getSettings().set ( RESTRICT, flag );
                ci.changed ( RESTRICT );
                this.snoop.msg ( true, SET_RESTRICT, ci.getName(), user, cmd );
        
        } else if ( command.is(VERBOSE) ) {
                this.sendWillOutput ( user, flag, "notify current ops of channel changes.", "notify current ops of channel changes." );
                ci.getSettings().set ( VERBOSE, flag );
                ci.changed ( VERBOSE );
                this.snoop.msg ( true, SET_VERBOSE, ci.getName(), user, cmd );
        
        } else if ( command.is(MAILBLOCK) ) {
                this.sendWillOutput ( user, flag, "deny the channel password to be mailed to the founders email.", "deny the channel password to be mailed to the founders mail." );
                ci.getSettings().set ( MAILBLOCK, flag );
                ci.changed ( MAILBLOCK );
                this.snoop.msg ( true, SET_MAILBLOCK, ci.getName(), user, cmd );
        
        } else if ( command.is(LEAVEOPS) ) {
                this.sendWillOutput ( user, flag, "leave ops ( @ )  to the first user entering the channel after its been empty.", "leave ops(@)." );
                ci.getSettings().set ( LEAVEOPS, flag );
                ci.changed ( LEAVEOPS );
                this.snoop.msg ( true, SET_LEAVEOPS, ci.getName(), user, cmd );
        
        } else if ( command.is(AUTOAKICK) ) {
                this.sendWillOutput ( user, flag, "remove matching users on akick.", "leave ops(@)." );
                ci.getSettings().set ( AUTOAKICK, flag );
                ci.changed ( AUTOAKICK );
                this.snoop.msg ( true, SET_AUTOAKICK, ci.getName(), user, cmd );
        
        } else {
            this.service.sendMsg ( user, output ( SETTING_NOT_FOUND, cmd[5] ) );
            this.snoop.msg ( false, SETTING_NOT_FOUND, ci.getNameStr(), user, cmd );
            return;
        }
    }
    
    private void getPass ( User user, String[] cmd ) {
        CMDResult result = this.validateCommandData ( user, GETPASS, cmd );
        if ( result.was(SYNTAX_ERROR) ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "GETPASS <#chan>" )  );
            this.snoop.msg ( false, SYNTAX_ERROR, user.getName(), user, cmd );
            return;
            
        } else if ( result.was(ACCESS_DENIED) ) {
            this.service.sendMsg (user, output (ACCESS_DENIED, result.getString1().getString() ) ); 
            this.snoop.msg (false, ACCESS_DENIED, result.getString1 ( ), user, cmd );
            return;  
            
        } else if ( result.was(CHAN_NOT_REGISTERED) ) {
            this.service.sendMsg ( user, output ( CHAN_NOT_REGISTERED, cmd[4] ) );
            this.snoop.msg ( false, CHAN_NOT_REGISTERED, cmd[4], user, cmd );
            return;
            
        } else if ( result.was(IS_MARKED) ) {
            this.service.sendMsg ( user, output ( IS_MARKED, cmd[4] ) );
            this.snoop.msg ( false, IS_MARKED, cmd[4], user, cmd );
            return;
        }
         
        ChanInfo ci = result.getChanInfo ( ); 
        NickInfo oper = user.getOper().getNick ( );
        HashString command = result.getCommand ( );
        CSLogEvent log = new CSLogEvent ( ci.getName(), command, user.getFullMask(), oper.getNameStr() );
        ChanServ.addLog ( log );
        this.service.sendMsg ( user, output ( CHAN_GETPASS, ci.getPass() ) );
        this.service.sendGlobOp ( oper.getName()+" used GETPASS on: "+ci.getName() );
        this.snoop.msg ( true, CHAN_GETPASS, ci.getName(), user, cmd );
    }
    
    private void changeFlag ( HashString flag, User user, String[] cmd )  {
        //:Pintuz PRIVMSG ChanServ@services.avade.net :freeze #avade  30d
        //   0       1         2                         3       4    5      6    = 7
        
        CMDResult result = this.validateCommandData ( user, flag, cmd ); 
        
        if ( result.was(SYNTAX_ERROR) ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, ChanSetting.hashToStr ( flag )+" <[-]chan>" ) ); 
            this.snoop.msg ( false, SYNTAX_ERROR, user.getName(), user, cmd );
            return;
            
        } else if ( result.was(ACCESS_DENIED) ) {
            this.service.sendMsg (user, output (ACCESS_DENIED, result.getString1().getString() ) ); 
            this.snoop.msg (false, ACCESS_DENIED, result.getString1 ( ), user, cmd );
            return;
            
        } else if ( result.was(CHAN_NOT_REGISTERED) ) {
            this.service.sendMsg (user, output (CHAN_NOT_REGISTERED, result.getString1().getString() ) ); 
            this.snoop.msg (false, CHAN_NOT_REGISTERED, result.getString1 ( ), user, cmd );
            return;
            
        } else if ( result.was(CHANFLAG_EXIST) ) {
            this.service.sendMsg (user, output (CHANFLAG_EXIST, result.getChanInfo().getNameStr(), result.getString1().getString() ) ); 
            this.snoop.msg (false, CHANFLAG_EXIST, result.getString1 ( ), user, cmd );
            return; 
            
        } else if ( result.was(IS_MARKED) ) {
            this.service.sendMsg (user, output (IS_MARKED, result.getChanInfo().getNameStr ( ) ) ); 
            this.snoop.msg (false, IS_MARKED, result.getChanInfo().getNameStr(), user, cmd );
            return;
        }
         
        NickInfo oper = user.getOper().getNick ( );
        ChanInfo ci = result.getChanInfo ( );
        HashString command = result.getCommand ( );
        CSLogEvent log;
        NickInfo instater;
        Chan c;
        
        System.out.println("debug: "+command);
        
        /* UNAUDITORIUM */
        if ( command.is(UNAUDITORIUM) ) {
            instater = NickServ.findNick ( ci.getSettings().getInstater ( flag ) );
            if ( ! user.isIdented ( instater ) && ! user.isAtleast ( SRA ) ) {
                this.service.sendMsg ( user, "Error: flag can only be removed by: "+instater.getName()+" or a SRA+." );
                this.snoop.msg ( false, ACCESS_DENIED_SRA, ci.getName ( ), user, cmd );
                return;
            }
//            if ( ( relay = ChanServ.findChan ( ci.getName()+"-relay" ) ) != null ) {
//                Handler.getChanServ().dropChan ( relay );
//            }

            ci.getSettings().set ( AUDITORIUM, "" );
            this.service.sendMsg ( user, "Please note that the auditorium channel mode handles joins/parts differently than normal and will cause "+
                                         "users becoming out of sync. Its for that reason recommended to masskick the channel after removing the mode to sort the possible desync." );
            this.service.sendRaw( ":ChanServ MODE "+ci.getName()+" 0 :-A");
            this.snoop.msg ( true, CHAN_SET_FLAG, ci.getName ( ), user, cmd );
            ci.changed(command);
        
        /* UNMARK || UNFREEZE || REOPEN || UNHOLD*/
        } else if ( command.is(UNMARK) ||
                    command.is(UNFREEZE) ||
                    command.is(REOPEN) ||
                    command.is(UNHOLD) ) {
            instater = NickServ.findNick ( ci.getSettings().getInstater ( flag ) );
            if ( ! user.isIdented ( instater ) && ! user.isAtleast ( SRA ) ) {
                this.service.sendMsg ( user, "Error: flag can only be removed by: "+instater.getName()+" or a SRA+." );
                this.snoop.msg ( false, ACCESS_DENIED_SRA, ci.getName ( ), user, cmd );
                return;
            }
            ci.getSettings().set ( flag, "" );
            log = new CSLogEvent ( ci.getName(), command, user.getFullMask(), oper.getNameStr() );
            ChanServ.addLog ( log );
            this.service.sendMsg ( user, output ( CHAN_SET_FLAG, ci.getNameStr(), "Un"+ci.getSettings().modeString ( flag ) ) );
            this.service.sendGlobOp ( "Channel: "+ci.getName()+" has been Un"+ci.getSettings().modeString ( flag )+" by: "+oper.getName() );
            this.snoop.msg ( true, CHAN_SET_FLAG, ci.getName ( ), user, cmd );
            ci.changed(flag);
            
        /* AUDITORIUM */
        } else if ( command.is(AUDITORIUM) ) {
//            if ( ( relay = ChanServ.findChan ( ci.getName()+"-relay" ) ) != null ) {
//                this.service.sendMsg ( user, "Error: Relay channel: "+ci.getName()+"-relay is already registered." );
//                this.snoop.msg ( true, CHAN_ALREADY_REGGED, ci.getName ( ), user, cmd );
//                return;
//            }

//            Random rand = new Random ( );
//            String pass = "R"+rand.nextInt(99999999);
//            Topic topic = new Topic ( "Relay channel for "+ci.getName(), "ChanServ", System.currentTimeMillis() / 1000 );
//            relay = new ChanInfo ( ci.getName()+"-relay", user.getOper().getNick(), pass, "Relay channel for "+ci.getName(), topic );
//            ChanSetting settings = new ChanSetting ();
//            settings.setModeLock("+spt-n");
//            relay.setTopic ( topic );
//            relay.setSettings ( settings );
//            ChanServ.addChan ( relay );
//            this.service.sendMsg ( user, "Relay channel: "+relay.getName()+" has been registered to you. This is the channel " );
//            this.service.sendMsg ( user, "where regular user chat will end up instead of the main channel. Keep this channel secret." );
//            this.service.sendMsg ( user, "Relay chan password: "+pass );
//            this.service.sendMsg ( user, " " );
  

            ci.getSettings().set ( AUDITORIUM, oper.getNameStr() );
            if ( ( c = Handler.findChan ( ci.getNameStr()+"-relay" ) ) != null ) {
                Handler.getChanServ().checkAllUsers(c);
            }
            this.service.sendMsg ( user, "Channel message from users is now relayed to "+ci.getNameStr()+"-relay" );
            this.service.sendMsg ( user, " " );
            this.service.sendMsg ( user, "Please note that the auditorium channel mode handles joins/parts differently than normal and" );
            this.service.sendMsg ( user, "will cause users becoming out of sync. Its for that reason recommended to masskick the channel" );
            this.service.sendMsg ( user, "after removing the mode to sort the possible desync." );
            this.service.sendRaw( ":ChanServ MODE "+ci.getName()+" 0 :+A");
            this.snoop.msg ( true, CHAN_SET_FLAG, ci.getNameStr(), user, cmd );
            ci.changed(command);
//            ChanServ.addToWorkList ( REGISTER, ci );
        
        /* MARK || FREEZE || CLOSE || HOLD */
        } else if ( command.is(MARK) ||
                    command.is(FREEZE) ||
                    command.is(CLOSE) ||
                    command.is(HOLD) ) {
            ci.set ( flag, oper.getNameStr() );
            log = new CSLogEvent ( ci.getName(), command, user.getFullMask(), oper.getNameStr() );
            ChanServ.addLog ( log );
            this.service.sendMsg ( user, output ( CHAN_SET_FLAG, ci.getNameStr(), ci.getSettings().modeString ( flag ) )  );
            this.service.sendGlobOp ( "Channel: "+ci.getName()+" has been "+ci.getSettings().modeString(flag)+" by: "+oper.getName() );
            this.snoop.msg ( true, CHAN_SET_FLAG, ci.getName ( ), user, cmd );
            ci.changed(command);
        
        /* NO SUCH COMMAND */
        } else {
            this.snoop.msg ( false, SYNTAX_ERROR, ci.getName(), user, cmd );
        }
         
    }
      
    private void doDescription ( User user, ChanInfo ci, String[] cmd ) {
        String buf = Handler.cutArrayIntoString ( cmd, 6 );
        this.service.sendMsg ( user, ci.getNameStr()+" channel description is now set to: "+buf );
        ci.setDescription ( buf );
    }
       
    private void doTopicLock ( User user, ChanInfo ci, HashString state )  {
        if ( state.is(OFF) ) {
            this.sendWillOutput ( user, false, "", "lock the topic." );

        } else if ( state.is(AOP) ) {
            this.sendWillOutput ( user, true, "lock your topic for AOP or higher only.", "" );
            
        } else if ( state.is(SOP) ) {
            this.sendWillOutput ( user, true, "lock your topic for SOP or higher only.", "" );
        
        } else if ( state.is(FOUNDER) ) {
            this.sendWillOutput ( user, true, "lock your topic for FOUNDER only.", "" );
        }
        ci.getSettings().set ( TOPICLOCK, state ); 
    }

    
    private void doModeLock ( User user, ChanInfo ci, String[] cmd )  {
        Chan c = Handler.findChan(ci.getName());
        ci.getSettings().setModeLock ( cmd[6] );
        this.service.sendMsg ( user, output ( MODELOCK, ci.getNameStr ( ), cmd[6] ) );
        if ( c != null ) {
            this.service.sendRaw( ":ChanServ MODE "+ci.getName()+" 0 :"+ci.getSettings().getModeLock().getMissingModes ( c, ci ) );
        }
    }
 
    private void sendIsOutput ( User user, boolean enable, String str )  {
        HashString flag = enable ? IS_NOW : IS_NOT;
        this.service.sendMsg ( user, output ( flag, str ) );
    }
    
    private void sendWillOutput ( User user, boolean enable, String will, String willNot )  {
        HashString flag = enable ? WILL_NOW : WILL_NOW_NOT;
        this.service.sendMsg ( user, output ( flag, will ) );
    }
    
    private void showStart ( boolean online, User user, ChanInfo ci, String str )  {
        this.service.sendMsg ( user, str+ci.getName()  ); 
    }
   
    private void showEnd ( User user, String str ) { 
        this.service.sendMsg ( user, "*** End of "+str+" ***" );
    }
 
    private String accessToString ( HashString access )  {
        if ( access.is(AKICK) ) {
            return "akick";

        } else if ( access.is(SOP) ) {
            return "sop";

        } else if ( access.is(AOP) ) {
            return "aop";
        }
        return "";
    }
    
    /**
     *
     * @param user
     * @param cmd
     */
    public void mDeop ( User user, String[] cmd )  {
        //:Pintuz PRIVMSG ChanServ@services.avade.net :mdeop #avade 
        //  0      1        2                          3     4              = 5

        CMDResult result = this.validateCommandData ( user, MDEOP, cmd );
         
        if ( result.was(SYNTAX_ERROR) ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "MDEOP <#Chan>" ) ); 
            this.snoop.msg ( false, SYNTAX_ERROR, user.getName ( ), user, cmd );
            return;
            
        } else if ( result.was(CHAN_NOT_EXIST) ) {
            this.service.sendMsg (user, output (CHAN_NOT_EXIST, result.getString1().getString() ) ); 
            this.snoop.msg (false, CHAN_NOT_EXIST, result.getString1 ( ), user, cmd );
            return;
            
        } else if ( result.was(CHAN_NOT_REGISTERED) ) {
            this.service.sendMsg (user, output (CHAN_NOT_REGISTERED, result.getChan().getNameStr() ) ); 
            this.snoop.msg (false, CHAN_NOT_REGISTERED, result.getChan().getName(), user, cmd );
            return;
            
        } else if ( result.was(ACCESS_DENIED) ) {
            this.service.sendMsg ( user, output ( ACCESS_DENIED, "" ) ); 
            this.snoop.msg ( false, ACCESS_DENIED, user.getName ( ), user, cmd );
            return;
            
        } else if ( result.was(NOT_ENOUGH_ACCESS) ) {
            this.service.sendMsg (user, output (NOT_ENOUGH_ACCESS, result.getString1().getString() ) ); 
            this.snoop.msg (false, NOT_ENOUGH_ACCESS, result.getString1 ( ), user, cmd );
            return;
        }

        Chan c = result.getChan ( );
        ChanInfo ci = result.getChanInfo ( );
        NickInfo ni;
        boolean isOper = false;
        if ( ( ni = result.getOper ( ) ) == null ) {
            ni = result.getNick ( );
        } else {
            isOper = true;
        }

        if  ( ci != null && ci.getSettings().is ( VERBOSE )  )  {
            this.service.sendOpMsg ( ci, output ( NICK_MDEOP_CHAN, ni.getNameStr(), ci.getNameStr ( ) ) );
        }                                      
        this.service.sendMsg ( user, output ( NICK_MDEOP, c.getNameStr()  )  );
        CSLogEvent log = new CSLogEvent ( ci.getName(), MDEOP, user.getFullMask(), ( isOper ? ni.getNameStr() : null ) );
        ChanServ.addLog ( log );
        ChanServ.deopAll ( c );
        this.snoop.msg ( true, NICK_MDEOP_CHAN, ci.getName ( ), user, cmd );
        ci.changed(LASTUSED);
    }
    
    /**
     *
     * @param user
     * @param cmd
     */
    public void mKick ( User user, String[] cmd )  {
        //:Pintuz PRIVMSG ChanServ@services.avade.net :mkick #avade 
        //  0      1        2                          3     4              = 5
 
        CMDResult result = this.validateCommandData ( user, MKICK, cmd );
         
        if ( result.was(SYNTAX_ERROR) ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "MKICK <#Chan>" ) ); 
            this.snoop.msg ( false, SYNTAX_ERROR, user.getName ( ), user, cmd );
            return;
            
        } else if ( result.was(CHAN_NOT_EXIST) ) {
            this.service.sendMsg (user, output (CHAN_NOT_EXIST, result.getString1().getString() ) ); 
            this.snoop.msg (false, CHAN_NOT_EXIST, result.getString1 ( ), user, cmd );
            return;
            
        } else if ( result.was(CHAN_NOT_REGISTERED) ) {
            this.service.sendMsg (user, output (CHAN_NOT_REGISTERED, result.getString1().getString() ) ); 
            this.snoop.msg (false, CHAN_NOT_REGISTERED, result.getString1 ( ), user, cmd );
            return;
            
        } else if ( result.was(ACCESS_DENIED) ) {
            this.service.sendMsg ( user, output ( ACCESS_DENIED, "" ) ); 
            this.snoop.msg ( false, ACCESS_DENIED, user.getName ( ), user, cmd );
            return;
            
        } else if ( result.was(NOT_ENOUGH_ACCESS) ) {
            this.service.sendMsg (user, output (NOT_ENOUGH_ACCESS, result.getString1().getString() ) ); 
            this.snoop.msg (false, NOT_ENOUGH_ACCESS, result.getString1 ( ), user, cmd );
            return;
            
        }
         
        Chan c = result.getChan ( );
        ChanInfo ci = result.getChanInfo ( );
        NickInfo ni;
        boolean isOper = false;
        if ( ( ni = result.getOper ( ) ) == null ) {
            ni = result.getNick ( );
        } else {
            isOper = true;
        }
        
        if ( ci.getSettings().is ( VERBOSE ) )  {
            this.service.sendOpMsg ( ci, output ( NICK_MKICK_CHAN, ni.getNameStr(), ci.getNameStr ( )  )  );
        }                    
        this.service.sendMsg ( user, output ( NICK_MKICK, c.getNameStr() ) );
        CSLogEvent log = new CSLogEvent ( ci.getName(), MKICK, user.getFullMask(), ( isOper ? ni.getNameStr() : null )  );
        ChanServ.addLog ( log );
        ci.kickAll ( "Masskick by "+ni.getName() );
        this.snoop.msg ( true, NICK_MKICK_CHAN, ci.getName(), user, cmd );
        ci.changed(LASTUSED);
    }
    
    /**
     *
     * @param user
     * @param cmd
     */
    public void list ( User user, String[] cmd )  { /* DONE? */
        // :DreamHea1er PRIVMSG NickServ@services.sshd.biz :list dream*
        //      0          1            2                     3    4     = 5
        if ( ! ChanServ.enoughAccess ( user, LIST ) ) {
            return; 
        }
        
        CMDResult result = this.validateCommandData ( user, LIST, cmd );
        
        if ( result.was(SYNTAX_ERROR) ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "LIST <pattern>" ) ); 
            this.snoop.msg ( false, SYNTAX_ERROR, user.getName(), user, cmd );
            return;
        
        } else if ( result.was(ACCESS_DENIED) ) {
            this.service.sendMsg ( user, output ( ACCESS_DENIED, "" ) );
            this.snoop.msg ( false, ACCESS_DENIED, user.getName(), user, cmd );
            return;
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
        this.snoop.msg ( true, SHOW_LIST, user.getOper().getName(), user, cmd );
        
    }
    
    private void accesslog(User user, String[] cmd) {
        // :DreamHea1er PRIVMSG NickServ@services.sshd.biz :ACCESSLOG #chan
        // 0            1       2                          3          4       = 5
        
        if ( ! CSDatabase.checkConn() ) {
            this.service.sendMsg ( user, "Error: Database not available, try again later." );
            return;
        }
        
        CMDResult result = this.validateCommandData ( user, ACCESSLOG, cmd );
        
        if ( result.was(SYNTAX_ERROR) ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "ACCESSLOG <chan>" ) ); 
            this.snoop.msg ( false, SYNTAX_ERROR, user.getName(), user, cmd );
            return;
            
        } else if ( result.was(ACCESS_DENIED) ) {
            this.service.sendMsg ( user, output ( ACCESS_DENIED, "" ) );
            this.snoop.msg ( false, ACCESS_DENIED, user.getName(), user, cmd );
            return;
            
        } else if ( result.was(CHAN_NOT_REGISTERED) ) {
            this.service.sendMsg (user, output (CHAN_NOT_REGISTERED, result.getString1().getString() ) );
            this.snoop.msg (false, CHAN_NOT_REGISTERED, result.getString1(), user, cmd );
            return;
            
        } else if ( result.was(CHAN_IS_CLOSED) ) {
            this.service.sendMsg (user, output (CHAN_IS_CLOSED, result.getChanInfo().getNameStr() ) );
            this.snoop.msg (false, CHAN_IS_CLOSED, result.getChanInfo().getName(), user, cmd );
            return;
            
        }
         
        NickInfo ni = result.getNick ( );
        ChanInfo ci = result.getChanInfo ( );
        ArrayList<CSAccessLogEvent> csaList = CSDatabase.getChanAccLogList ( user, ci );
        
        this.service.sendMsg(user, "*** Access Log for "+ci.getName()+":");
        for ( CSAccessLogEvent log : csaList ) {
            if ( user.isAtleast ( IRCOP ) ) {
                this.service.sendMsg ( user, output ( SHOWACCESSLOGOPER, log.getStamp(), log.getFlagStr(), log.getTarget(), log.getUsermask(), log.getInstater() ) );
            } else {
                this.service.sendMsg ( user, output ( SHOWACCESSLOG, log.getStamp(), log.getFlagStr(), log.getTarget(), log.getInstater() ) );
            }
        }
        this.service.sendMsg ( user, "*** End of Logs ***" );
        this.snoop.msg ( true, SHOWACCESSLOG, ci.getName(), user, cmd );     
    }
 
    private void topiclog ( User user, String[] cmd ) {
        // :DreamHea1er PRIVMSG NickServ@services.sshd.biz :ACCESSLOG #chan
        // 0            1       2                          3          4       = 5
        if ( ! CSDatabase.checkConn() ) {
            this.service.sendMsg ( user, "Error: Database not available, try again later." );
        }
        CMDResult result = this.validateCommandData ( user, TOPICLOG, cmd );

        if ( result.was(SYNTAX_ERROR) ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "ACCESSLOG <chan>" ) ); 
            this.snoop.msg (false, SYNTAX_ERROR, result.getChanInfo().getName(), user, cmd );
            return;
            
        } else if ( result.was(CHAN_NOT_REGISTERED) ) {
            this.service.sendMsg (user, output (CHAN_NOT_REGISTERED, result.getString1().getString() ) );
            this.snoop.msg (false, CHAN_NOT_REGISTERED, result.getString1(), user, cmd );
            return;
            
        } else if ( result.was(CHAN_IS_CLOSED) ) {
            this.service.sendMsg (user, output (CHAN_IS_CLOSED, result.getChanInfo().getNameStr() ) );
            this.snoop.msg (false, CHAN_IS_CLOSED, result.getChanInfo().getName(), user, cmd );
            return;
            
        }
         
        NickInfo ni = result.getNick ( );
        ChanInfo ci = result.getChanInfo ( );
        ArrayList<Topic> tList = CSDatabase.getTopicList ( ci );
        this.service.sendMsg(user, "*** Access Log for "+ci.getName()+":");
        for ( Topic topic : tList ) {
            this.service.sendMsg ( user, output ( SHOWTOPICLOG, topic.getTimeStr(), topic.getSetter(), topic.getTopic() ) );
        }       
        this.service.sendMsg ( user, "*** End of Logs ***" );
        this.snoop.msg ( true, SHOWTOPICLOG, ci.getName(), user, cmd );
    }
    
    
    private void chanFlag(User user, String[] cmd) {
        // :DreamHea1er PRIVMSG NickServ@services.sshd.biz :CHANFLAG #chan MAX_BANS 500
        // 0            1       2                          3         4     5        6  = 7        
        CMDResult result = this.validateCommandData ( user, CHANFLAG, cmd );
        
        if ( result.was(SYNTAX_ERROR) ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "CHANFLAG <chan> <flag> <value>" ) ); 
                this.snoop.msg ( false, SYNTAX_ERROR, user.getName(), user, cmd );
                return;
            
        } else if ( result.was(CHAN_NOT_REGISTERED) ) {
            this.service.sendMsg (user, output (CHAN_NOT_REGISTERED, result.getString1().getString() ) );
                this.snoop.msg (false, CHAN_NOT_REGISTERED, result.getString1(), user, cmd );
                return;
            
        } else if ( result.was(CHAN_IS_FROZEN) ) {
            this.service.sendMsg (user, output (CHAN_IS_FROZEN, result.getChanInfo().getNameStr() ) );
                this.snoop.msg (false, CHAN_IS_FROZEN, result.getChanInfo().getName(), user, cmd );
                return;
            
        } else if ( result.was(CHAN_IS_CLOSED) ) {
            this.service.sendMsg (user, output (CHAN_IS_CLOSED, result.getChanInfo().getNameStr() ) );
                this.snoop.msg (false, CHAN_IS_CLOSED, result.getChanInfo().getName(), user, cmd );
                return;
            
        } else if ( result.was(ACCESS_DENIED) ) {
            this.service.sendMsg ( user, output ( ACCESS_DENIED, "" ) );
                this.snoop.msg ( false, ACCESS_DENIED, user.getName(), user, cmd );
                return;
            
        } else if ( result.was(NO_SUCH_CHANFLAG) ) {
            this.service.sendMsg (user, output (NO_SUCH_CHANFLAG, result.getString1().getString() ) );
                this.snoop.msg (false, NO_SUCH_CHANFLAG, result.getString1 ( ), user, cmd );
                return;
         
        } else if ( result.was(BAD_CHANFLAG_VALUE) ) {
            this.service.sendMsg ( user, output ( BAD_CHANFLAG_VALUE ) );
                this.snoop.msg ( false, BAD_CHANFLAG_VALUE, user.getName(), user, cmd );
                return;
        }
        
        ChanInfo ci = result.getChanInfo ( );
        NickInfo ni = result.getNick ( );
        HashString commandStr = result.getCommandStr ( );
        String commandVal = result.getCommandVal().getString();
        HashString command = result.getCommand ( );
        String value = result.getString2().getString();

        if ( command.is(JOIN_CONNECT_TIME) ||
             command.is(TALK_CONNECT_TIME) ||
             command.is(TALK_JOIN_TIME) ||
             command.is(MAX_BANS) ) {
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
            ci.changed(command);
            this.service.sendServ ( "SVSXCF "+ci.getName()+" "+commandStr+":"+commandVal );
            this.service.sendMsg ( user, "ChanFlag "+commandStr+" has now been set to: "+commandVal );
            this.snoop.msg ( true, CHAN_SET_FLAG, ci.getName(), user, cmd );
        
        } else if ( command.is(NO_NOTICE) ||
                command.is(NO_CTCP) ||
                command.is(NO_PART_MSG) ||
                command.is(NO_QUIT_MSG) ||
                command.is(EXEMPT_OPPED) ||
                command.is(EXEMPT_VOICED) ||
                command.is(EXEMPT_IDENTD) ||
                command.is(EXEMPT_REGISTERED) ||
                command.is(EXEMPT_INVITES) ) {
            boolean boo = ( commandVal.equalsIgnoreCase ( "ON" ) );
            ci.getChanFlag().setBooleanFlag ( command, boo );
            ci.getChanges().change ( command );
            ci.changed(command);
            this.service.sendServ ( "SVSXCF "+ci.getName()+" "+commandStr+":"+commandVal );
            this.service.sendMsg ( user, "ChanFlag "+commandStr+" has now been set to: "+commandVal );
            this.snoop.msg ( true, CHAN_SET_FLAG, ci.getName(), user, cmd );
        
        } else if ( command.is(GREETMSG) ) {
            String message = Handler.cutArrayIntoString ( cmd, 6 );
            ci.getChanFlag().setGreetmsg ( message );
            ci.getChanges().change ( command );
            ci.changed(command);
            this.service.sendServ ( "SVSXCF "+ci.getName()+" "+commandStr+" :"+message );
            this.service.sendMsg ( user, "ChanFlag "+commandStr+" has now been set to: "+message );
            this.snoop.msg ( true, CHAN_SET_FLAG, ci.getName(), user, cmd );
        
        } else if ( command.is(LIST) ) {
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
            this.snoop.msg ( true, SHOW_LIST, ci.getName(), user, cmd );
        
        }
    }
 
    
    private CSAcc getAcc ( HashString command, ChanInfo ci, NickInfo ni ) {
        return ci.getAccess ( command, ni );
    }
    private CSAcc getAcc ( HashString command, ChanInfo ci, String mask ) {
        return ci.getAccess ( command, mask );
    }

    
    private void listOps(User user, String[] cmd) {
        
        CMDResult result = this.validateCommandData ( user, LISTOPS, cmd );
         
        if ( result.was(SYNTAX_ERROR) ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "LISTOPS <#Chan>" ) ); 
            this.snoop.msg ( false, SYNTAX_ERROR, user.getName(), user, cmd );
            return; 
            
        } else if ( result.was(CHAN_NOT_REGISTERED) ) {
            this.service.sendMsg (user, output (CHAN_NOT_REGISTERED, result.getString1().getString() ) ); 
            this.snoop.msg (false, CHAN_NOT_REGISTERED, result.getString1 ( ), user, cmd );
            return;
            
        } else if ( result.was(CHAN_IS_FROZEN) ) {
            this.service.sendMsg (user, output (CHAN_IS_FROZEN, result.getChanInfo().getNameStr ( ) ) ); 
            this.snoop.msg (false, CHAN_IS_FROZEN, result.getChanInfo().getName ( ), user, cmd );
            return;
            
        } else if ( result.was(CHAN_IS_CLOSED) ) {
            this.service.sendMsg (user, output (CHAN_IS_CLOSED, result.getChanInfo().getNameStr ( ) ) ); 
            this.snoop.msg (false, CHAN_IS_CLOSED, result.getChanInfo().getName ( ), user, cmd );
            return;
            
        } else if ( result.was(ACCESS_DENIED) ) {
            this.service.sendMsg ( user, output ( ACCESS_DENIED, "" ) ); 
            this.snoop.msg ( false, ACCESS_DENIED, user.getName(), user, cmd );
            return;
      
        }
       
        HashString[] lists = { SOP, AOP };
        ChanInfo ci = result.getChanInfo ( );
        this.service.sendMsg ( user,  "FOUNDER: " );
        this.service.sendMsg ( user,  "  "+ci.getFounder().getName()+" ("+ci.getFounder().getString ( FULLMASK )+")" );

        for ( HashString access : lists ) {
            String accStr = accessToString ( access );
            this.service.sendMsg ( user,  accStr.toUpperCase()+": " );
            ArrayList<CSAcc> list = ci.getAccessList ( access );
            for ( CSAcc acc : list )  {     
                if ( acc.getNick ( ) != null )  {
                    this.service.sendMsg ( user,  "  "+acc.getNick().getName()+" ("+acc.getNick().getString ( FULLMASK ) +") - [LastOped: "+acc.getLastOped()+"]" );
                } else {
                    this.service.sendMsg ( user,  "  "+acc.getMask ( )+" (mask)" );
                }
            }
        }
        this.showEnd ( user, "LISTOPS" ); 
        this.snoop.msg ( true, SHOW_LIST, ci.getName(), user, cmd );
    }

    
    
    private CMDResult validateCommandData ( User user, HashString command, String[] cmd )  {
        Chan c;
        Chan c2;
        ChanInfo ci;
        NickInfo ni;
        NickInfo ni2 = null;
        String nick = new String ( );
        String pass = new String ( ); 
        CMDResult result = new CMDResult ( );
        User target = null;
        HashString subcommand;
        int num;
        String mask = null;
        CSAcc acc = null;
        CSAcc acc2 = null;
        boolean checkNick   = false;
        boolean checkPass   = false;
        boolean needAccess  = false;
        HashString str1 = null;
        HashString str2 = null;
        Config conf = Proc.getConf();
        
        if ( command.is(CHANFLAG) ) {
            
                // :DreamHea1er PRIVMSG NickServ@services.sshd.biz :CHANFLAG #chan MAX_BANS 500
                // 0            1       2                          3         4     5        6  = 7   
                if ( isShorterThanLen ( 6, cmd) ) {
                    result.setStatus ( SYNTAX_ERROR );
                } else if ( ( ci = ChanServ.findChan ( cmd[4] ) ) == null ) {
                    result.setString1 ( new HashString ( cmd[4] ) );
                    result.setStatus ( CHAN_NOT_REGISTERED );
                } else if ( ci.getSettings().is ( CLOSED ) ) {
                    result.setChanInfo ( ci );
                    result.setStatus ( CHAN_IS_CLOSED );
                } else if ( ci.getSettings().is ( FROZEN ) ) {
                    result.setChanInfo ( ci );
                    result.setStatus ( CHAN_IS_FROZEN );
                } else if ( ( ni = ci.getNickByUser ( user ) ) == null && ! user.isAtleast ( SA ) ) {
                    result.setString1 ( ci.getName() ); 
                    result.setStatus ( ACCESS_DENIED );
                } else if ( ! ci.isFounder ( ni ) && ! user.isAtleast ( SA ) ) {
                    result.setString1 ( ci.getName() );
                    result.setStatus ( ACCESS_DENIED );
                } else if ( ! CSFlag.isFlag ( cmd[5] ) ) {
                    result.setString1 ( new HashString ( cmd[5] ) );
                    result.setStatus ( NO_SUCH_CHANFLAG );
                } else if ( ! CSFlag.isOkValue ( cmd[5], ( cmd.length > 6 ? cmd[6] : "" ) ) ) {
                    result.setStatus ( BAD_CHANFLAG_VALUE );
                } else {
                    str1 = new HashString ( cmd[5] );
                    result.setChanInfo ( ci );
                    result.setCommandStr ( str1 );
                    result.setCommand ( new HashString ( cmd[5] ) );
                    result.setNick ( ni );
                    result.setCommandVal ( new HashString(cmd.length > 6 ? cmd[6] : "") );
                }
                
        } else if ( command.is(ACCESSLOG) ) {
            
                if ( isShorterThanLen ( 5, cmd) ) {
                    result.setStatus ( SYNTAX_ERROR );
                } else if ( ( ci = ChanServ.findChan ( cmd[4] ) ) == null ) {
                    result.setString1 ( new HashString ( cmd[4] ) );
                    result.setStatus ( CHAN_NOT_REGISTERED );
                } else if ( ci.getSettings().is ( CLOSED ) ) {
                    result.setChanInfo ( ci );
                    result.setStatus ( CHAN_IS_CLOSED );
                } else if ( ( ni = ci.getNickByUser ( user ) ) == null && ! user.isAtleast ( SA ) ) {
                    result.setString1 ( ci.getName() ); 
                    result.setStatus ( ACCESS_DENIED );
                } else if ( ! ci.isAtleastAop ( ni ) && ! user.isAtleast ( SA ) ) {
                    result.setString1 ( ci.getName() );
                    result.setStatus ( ACCESS_DENIED );
                } else {
                    result.setChanInfo ( ci );
                    result.setNick ( ni );
                }
        
        } else if ( command.is(TOPICLOG) ) {
                
                if ( isShorterThanLen ( 5, cmd) ) {
                    result.setStatus ( SYNTAX_ERROR );
                } else if ( ( ci = ChanServ.findChan ( cmd[4] ) ) == null ) {
                    result.setString1 ( new HashString ( cmd[4] ) );
                    result.setStatus ( CHAN_NOT_REGISTERED );
                } else if ( ci.getSettings().is ( CLOSED ) ) {
                    result.setChanInfo ( ci );
                    result.setStatus ( CHAN_IS_CLOSED );
                } else {
                    result.setChanInfo ( ci );
                }
            
            
        } else if ( command.is(SOP) ||
                    command.is(AOP) ||
                    command.is(AKICK) ) {
            
            //:DreamHealer PRIVMSG ChanServ@services.avade.net :akick #friends add *!*@10.0.1/24
                //       0       1                           2      3        4   5             6
                if ( isShorterThanLen ( 6, cmd ) ) {
                    result.setStatus ( SYNTAX_ERROR );
                } 
                ci = ChanServ.findChan ( cmd[4] );
                ni = null;
                if (ci != null) {
                    ni = ci.getNickByUser ( user );
                }
                
                if ( cmd.length > 6 && ( ni2 = NickServ.findNick ( cmd[6] ) ) == null ) {
                    mask = cmd[6];
                }
                
                subcommand = new HashString ( cmd[5] );
                
                if ( ! subcommand.is(ADD) && 
                     ! subcommand.is(DEL) ) {
                    result.setStatus ( SYNTAX_ERROR );

                } else if ( ni2 == null && ! ( mask.contains("!") && mask.contains("@") ) ) {
                    result.setString1 ( cmd[6] );
                    result.setStatus ( NICK_NOT_REGISTERED );
                } else if ( ci == null ) {
                    result.setString1 ( cmd[4] );
                    result.setStatus ( CHAN_NOT_REGISTERED );
                } else if ( ci.getSettings().is ( FROZEN ) ) {
                    result.setChanInfo ( ci );
                    result.setStatus ( CHAN_IS_FROZEN );  
                } else if ( ci.getSettings().is ( CLOSED ) ) {
                    result.setChanInfo ( ci );
                    result.setStatus ( CHAN_IS_CLOSED );
                } else if ( ni == null || ( ! ci.isAtleastSop ( ni ) && ! ni.is(ni2) ) ) {
                    result.setString1 ( user.getName() );
                    result.setStatus ( ACCESS_DENIED );
                } else if ( ni2 != null && ! ni2.isAuth ( ) ) {
                    result.setNick2 ( ni2 );
                    result.setStatus ( NICK_NOT_AUTHED );
                } else if ( ni2 != null && 
                            ( ci.getAccessByNick ( ni ) <= ci.getAccessByNick ( ni2 ) &&
                              ! ni.is(ni2) ) ) {
                    result.setStatus ( NOT_ENOUGH_ACCESS );
                } else if ( ( acc = getAcc ( command, ci, ni2 ) ) == null && 
                            ( acc = getAcc ( command, ci, mask ) ) == null &&
                            subcommand.is ( DEL ) ) {
                    result.setStatus ( XOP_NOT_FOUND );
                } else if ( subcommand.is(ADD) && acc != null ) {
                    if ( ni2 != null ) {
                        result.setString1 ( ni2.getName() );
                    } else {
                        result.setString1 ( mask );
                    }
                    result.setStatus ( XOP_ALREADY_PRESENT );
                } else if ( subcommand.is(ADD) &&
                            ni2 != null &&
                            ni2.isSet ( NOOP ) ) {
                    result.setNick2 ( ni2 );
                    result.setStatus ( NICK_HAS_NOOP );
                } else if ( subcommand.is ( ADD ) && 
                            ( ( ni2 != null && ( acc = new CSAcc ( ni2, command, null ) ) == null ) ||
                              ( mask != null && ( acc = new CSAcc ( mask, command, null ) ) == null ) ) ) {  
                    result.setStatus ( XOP_ADD_FAIL );
                } else {
                    result.setChanInfo ( ci );
                    result.setNick ( ni );
                    if ( ni2 != null ) {
                        result.setNick2 ( ni2 );
                    } else {
                        result.setString1 ( mask );
                    }
                    result.setAcc ( acc );
                    result.setCommand ( command );
                    result.setSubCommand ( subcommand );
                }
        
        } else if ( command.is(LISTOPS) ) {
                //:DreamHealer PRIVMSG ChanServ@services.avade.net :listops #friends
                //           0       1                           2        3        4              =   5
                if ( isShorterThanLen ( 5, cmd ) ) {
                    result.setStatus ( SYNTAX_ERROR );
                } else if ( ( ci = ChanServ.findChan ( cmd[4] ) ) == null ) {
                    result.setString1 ( cmd[4] );
                    result.setStatus ( CHAN_NOT_REGISTERED );
                } else if ( ci.isSet ( FROZEN ) && ! user.isAtleast ( IRCOP ) ) {
                    result.setString1 ( ci.getName() );
                    result.setStatus ( CHAN_IS_FROZEN );
                } else if ( ci.isSet ( CLOSED ) && ! user.isAtleast ( IRCOP ) ) {
                    result.setString1 ( ci.getName() );
                    result.setStatus ( CHAN_IS_CLOSED );
                } else if ( ! ci.isAtleastAop ( user ) && ! user.isAtleast ( IRCOP ) ) {
                    result.setStatus ( ACCESS_DENIED );
                } else {
                    result.setChanInfo ( ci );
                }            
            
            
        } else if ( command.is(LISTACCESS) ) {
                //:DreamHealer PRIVMSG ChanServ@services.avade.net :aop #friends list
                //           0       1                           2    3        4    5          =   6
                if ( isShorterThanLen ( 6, cmd ) ) {
                    result.setStatus ( SYNTAX_ERROR );
                } else if ( ( ci = ChanServ.findChan ( cmd[4] ) ) == null ) {
                    result.setString1 ( cmd[4] );
                    result.setStatus ( CHAN_NOT_REGISTERED );
                } else if ( ci.isSet ( FROZEN ) && ! user.isAtleast ( IRCOP ) ) {
                    result.setString1 ( ci.getName() );
                    result.setStatus ( CHAN_IS_FROZEN );
                } else if ( ci.isSet ( CLOSED ) && ! user.isAtleast ( IRCOP ) ) {
                    result.setString1 ( ci.getName() );
                    result.setStatus ( CHAN_IS_CLOSED );
                } else if ( ! ci.isAtleastAop ( user ) && ! user.isAtleast ( IRCOP ) ) {
                    result.setStatus ( ACCESS_DENIED );
                } else {
                    result.setChanInfo ( ci );
                }            
            
            
        } else if ( command.is(LIST) ) {
                if ( isShorterThanLen ( 5, cmd ) ) {
                    result.setStatus ( SYNTAX_ERROR );
                } else if ( ! user.isAtleast ( IRCOP ) ) {
                    result.setStatus ( ACCESS_DENIED );
                }
        
        } else if ( command.is(SET) ) {
                if ( isShorterThanLen ( 7, cmd ) ) {
                    result.setStatus ( SYNTAX_ERROR );
                } else if ( ( ni = NickServ.findNick ( user.getName() ) ) == null ) {
                    result.setString1 ( user.getName() );
                    result.setStatus ( NICK_NOT_REGISTERED );
                } else if ( ( ci = ChanServ.findChan ( cmd[4] ) ) == null ) {
                    result.setString1 ( cmd[4] );
                    result.setStatus ( CHAN_NOT_REGISTERED );
                } else if ( ci.isSet ( FROZEN )  && ! user.isAtleast ( CSOP )  ) {
                    result.setChanInfo ( ci );
                    result.setStatus ( CHAN_IS_FROZEN );
                } else if ( ci.isSet ( CLOSED )  && ! user.isAtleast ( CSOP )  ) {
                    result.setChanInfo ( ci );
                    result.setStatus ( CHAN_IS_CLOSED );
                } else if ( ! ci.isFounder ( user ) ) {
                    result.setChanInfo ( ci );
                    result.setStatus ( ACCESS_DENIED );
                } else {
                    result.setChanInfo ( ci );
                    result.setNick ( ni );
                }
        
        } else if ( command.is(WHY) ) {
                if ( isShorterThanLen ( 6, cmd ) ) {
                    result.setStatus ( SYNTAX_ERROR );
                } else if ( ( ci = ChanServ.findChan ( cmd[4] ) ) == null ) {
                    result.setString1 ( cmd[4] );
                    result.setStatus ( CHAN_NOT_REGISTERED );
                } else if ( ( target = Handler.findUser ( cmd[5] ) ) == null ) {
                    result.setString1 ( cmd[5] );
                    result.setStatus ( NICK_NOT_EXIST );
                } else if ( ( ni = ci.getNickByUser ( user ) ) == null && ! user.isAtleast ( IRCOP )) {
                    result.setChanInfo ( ci );
                    result.setStatus ( ACCESS_DENIED );
                } else {
                    result.setChanInfo ( ci );
                    result.setNick ( ni );
                    result.setTarget ( target );
                }
        
        } else if ( command.is(UNBAN) ) {
                if ( isShorterThanLen ( 6, cmd ) )  {
                    target = user;
                    result.setTarget ( target );
                } else if ( isShorterThanLen ( 7, cmd ) ) {
                    if ( ( target = Handler.findUser ( cmd[5] ) ) == null ) {                        
                        result.setString1 ( cmd[5] );
                        result.setStatus ( NICK_NOT_EXIST );
                        return result;
                    } else {
                        result.setTarget( target );
                    }
                } 
                if ( isShorterThanLen ( 5, cmd ) ) {
                    result.setStatus ( SYNTAX_ERROR );
                } else if ( ( c = Handler.findChan ( cmd[4] ) ) == null ) {
                    result.setString1 ( cmd[4] );
                    result.setStatus ( CHAN_NOT_EXIST );
                } else if ( ( ci = ChanServ.findChan ( cmd[4] ) ) == null ) {
                    result.setString1 ( cmd[4] );
                    result.setStatus ( CHAN_NOT_REGISTERED );
                } else if ( ( ni = ci.getNickByUser ( user ) ) == null ) {
                    result.setChanInfo ( ci );
                    result.setStatus ( NICK_ACCESS_DENIED ); 
                } else if ( ci.getSettings().is ( FROZEN ) ) {
                    result.setChanInfo ( ci );
                    result.setStatus ( CHAN_IS_FROZEN );  
                } else if ( ci.getSettings().is ( CLOSED ) ) {
                    result.setChanInfo ( ci );
                    result.setStatus ( CHAN_IS_CLOSED );  
                } else {
                    result.setChan ( c );
                    result.setChanInfo ( ci );
                    result.setNick ( ni );
                }
        
        } else if ( command.is(INVITE) ) {
                if ( isShorterThanLen ( 5, cmd ) ) {
                    result.setStatus ( SYNTAX_ERROR );
                } else if ( ( c = Handler.findChan ( cmd[4] ) ) == null ) {
                    result.setString1 ( cmd[4] );
                    result.setStatus ( CHAN_NOT_EXIST );
                } else if ( ( ci = ChanServ.findChan ( cmd[4] ) ) == null ) {
                    result.setString1 ( cmd[4] );
                    result.setStatus ( CHAN_NOT_REGISTERED );
                } else if ( ( ni = ci.getNickByUser ( user ) ) == null ) {
                    result.setChanInfo ( ci );
                    result.setStatus ( NICK_ACCESS_DENIED ); 
                } else if ( ci.getSettings().is ( FROZEN ) ) {
                    result.setChanInfo ( ci );
                    result.setStatus ( CHAN_IS_FROZEN );  
                } else if ( ci.getSettings().is ( CLOSED ) ) {
                    result.setChanInfo ( ci );
                    result.setStatus ( CHAN_IS_CLOSED );  
                } else {
                    result.setChan ( c );
                    result.setChanInfo ( ci );
                    result.setNick ( ni );
                }
        
        } else if ( command.is(INFO) ) {
                if ( isShorterThanLen ( 5, cmd ) ) {
                    result.setStatus ( SYNTAX_ERROR );
                } else if ( ( ci = ChanServ.findChan ( cmd[4] ) ) == null ) {
                    result.setString1 ( cmd[4] );
                    result.setStatus ( CHAN_NOT_REGISTERED );    
                } else if ( ci.getSettings().is ( FROZEN ) && ! user.isAtleast ( IRCOP ) ) {
                    result.setChanInfo ( ci );
                    result.setStatus ( CHAN_IS_FROZEN );  
                } else if ( ci.getSettings().is ( CLOSED ) && ! user.isAtleast ( IRCOP ) ) {
                    result.setChanInfo ( ci );
                    result.setStatus ( CHAN_IS_CLOSED );  
                } else {
                    result.setChanInfo(ci);
                }
       
        } else if ( command.is(DROP) ) {
                if ( isShorterThanLen ( 6, cmd ) ) {
                    result.setStatus ( SYNTAX_ERROR );
                } else if ( ( ci = ChanServ.findChan ( cmd[4] ) ) == null ) {
                    result.setString1 ( cmd[4] );
                    result.setStatus ( CHAN_NOT_REGISTERED );                
                } else if ( ci.getSettings().is ( FROZEN ) ) {
                    result.setChanInfo ( ci );
                    result.setStatus ( CHAN_IS_FROZEN ); 
                } else if ( ci.getSettings().is ( CLOSED ) ) {
                    result.setChanInfo ( ci );
                    result.setStatus ( CHAN_IS_CLOSED );
                } else if ( ! ci.identify ( user, cmd[5] ) ) {
                    result.setChanInfo ( ci );
                    result.setStatus ( INVALID_PASSWORD ); 
                } else if ( ci.isSet ( MARK ) ) {
                    result.setChanInfo ( ci );
                    result.setStatus ( IS_MARKED );
                } else {
                    result.setChanInfo(ci);
                }
       
        } else if ( command.is(IDENTIFY) ) {
                if ( isShorterThanLen ( 6, cmd ) ) {
                    result.setStatus ( SYNTAX_ERROR );
                } else if ( ( ci = ChanServ.findChan ( cmd[4] ) ) == null ) {
                    result.setString1 ( cmd[4] );
                    result.setStatus ( CHAN_NOT_REGISTERED );                
                } else if ( ci.getSettings().is ( FROZEN ) ) {
                    result.setChanInfo ( ci );
                    result.setStatus ( CHAN_IS_FROZEN ); 
                } else if ( ci.getSettings().is ( CLOSED ) ) {
                    result.setChanInfo ( ci );
                    result.setStatus ( CHAN_IS_CLOSED );
                } else if ( ! ci.isFounder(user) && ci.getThrottle().isThrottled() ) {
                    result.setChanInfo ( ci );
                    result.setStatus ( IS_THROTTLED );
                } else if ( ! ci.identify ( user, cmd[5] ) ) {
                    result.setChanInfo ( ci );
                    result.setStatus ( INVALID_PASSWORD ); 
                } else {
                    result.setChanInfo(ci);
                }
        
        } else if ( command.is(REGISTER) ) {
                ni = NickServ.findNick ( user.getName() );
                if ( isShorterThanLen ( 7, cmd ) ) {
                    result.setStatus ( SYNTAX_ERROR );
                } else if ( ni == null ) {
                    result.setUser ( user );
                    result.setStatus ( NICK_NOT_REGISTERED );
                } else if ( ! user.isIdented ( ni ) ) {
                    result.setNick ( ni );
                    result.setStatus ( NICK_NOT_IDENTIFIED );
                } else if ( ( c = Handler.findChan ( cmd[4] ) ) == null ) {
                    result.setString1 ( cmd[4] );
                    result.setStatus ( CHAN_NOT_EXIST );
                } else if ( ( ci = ChanServ.findChan ( cmd[4] ) ) != null ) {
                    result.setChanInfo ( ci );
                    result.setStatus ( CHAN_ALREADY_REGGED );
                } else if ( StringMatch.wild(c.getNameStr(), "*-relay" ) ) {
                    result.setChan(c);
                    result.setStatus(CHAN_IS_RELAY);
                } else if ( ! c.isOp ( user ) ) {
                    result.setChan ( c );
                    result.setStatus ( USER_NOT_OP );
                } else {
                    result.setChan(c);
                    result.setChanInfo(ci);
                    result.setNick(ni);
                }
        
        } else if ( command.is(OP) || 
                    command.is(DEOP) ) {
                if ( isShorterThanLen ( 5, cmd ) ) {
                    result.setStatus ( SYNTAX_ERROR );
                } else if ( ( c = Handler.findChan ( cmd[4] ) ) == null ) {
                    result.setString1 ( cmd[4] );
                    result.setStatus ( CHAN_NOT_EXIST );                
                } else if ( ( ci = ChanServ.findChan ( cmd[4] ) ) == null ) {
                    result.setString1 ( cmd[4] );
                    result.setStatus ( CHAN_NOT_REGISTERED );
                } else if ( ci.getSettings().is ( FROZEN ) ) {
                    result.setChanInfo ( ci );
                    result.setStatus ( CHAN_IS_FROZEN ); 
                } else if ( ci.getSettings().is ( CLOSED ) ) {
                    result.setChanInfo ( ci );
                    result.setStatus ( CHAN_IS_CLOSED );
                } else if ( ( ni = ci.getTopNickByUser ( user ) ) == null ) {
                    result.setChanInfo ( ci );
                    result.setStatus ( ACCESS_DENIED );                
                } else if ( cmd.length > 5 && ( ( target = Handler.findUser ( cmd[5] ) ) == null || ! c.nickIsPresent ( cmd[5] ) ) ) {
                    result.setChan ( c );
                    result.setStatus ( NICK_NOT_PRESENT );
                } else {
                    result.setChan ( c );
                    result.setChanInfo ( ci );
                    result.setNick ( ni );
                    result.setTarget ( target );
                }
        
        } else if ( command.is(MDEOP) ||
                    command.is(MKICK) ) {
                if ( isShorterThanLen ( 4, cmd )  )  {
                    result.setStatus ( SYNTAX_ERROR );                 
                } else if ( ( c = Handler.findChan ( cmd[4] ) ) == null ) {
                    result.setString1 ( cmd[4] );
                    result.setStatus ( CHAN_NOT_EXIST );                
                } else if ( ( ci = ChanServ.findChan ( cmd[4] ) ) == null ) {
                    result.setString1 ( cmd[4] );
                    result.setStatus ( CHAN_NOT_REGISTERED );
                } else if ( user.isAtleast ( CSOP ) && ( ni = user.getSID().getTopOperNick ( ) ) != null ) {
                    result.setChan ( c );
                    result.setChanInfo ( ci );
                    result.setOper ( ni );
                } else if ( ci.getSettings().is ( FROZEN ) ) {
                    result.setChanInfo ( ci );
                    result.setStatus ( CHAN_IS_FROZEN ); 
                } else if ( ci.getSettings().is ( CLOSED ) ) {
                    result.setChanInfo ( ci );
                    result.setStatus ( CHAN_IS_CLOSED );
                } else if ( ( ni = ci.getTopNickByUser ( user ) ) == null ) {
                    result.setChanInfo ( ci );
                    result.setStatus ( ACCESS_DENIED );
                } else { 
                    result.setChan ( c );
                    result.setChanInfo ( ci );
                    result.setNick ( ni );
                    if ( ci.isFounder ( ni ) ) {
                        return result;
                    } else if ( ci.isAccess ( SOP, ni ) ) {
                        for ( User u : c.getList ( ALL ) ) {
                            if ( ci.isFounder ( u ) ) { 
                                result.setString1 ( u.getName() );
                                result.setStatus ( NOT_ENOUGH_ACCESS );
                            }
                        }
                    } else if ( ci.isAccess ( AOP, ni ) ) {
                        for ( User u : c.getList ( ALL ) ) {
                            if ( ci.isFounder ( u )  || ci.isAccess ( SOP, u ) ) { 
                                result.setString1 ( u.getName() );
                                result.setStatus ( NOT_ENOUGH_ACCESS );
                            }
                        }
                    } else {
                        result.setStatus ( ACCESS_DENIED );
                    }
                }
       
        } else if ( command.is(AUDITORIUM) ||
                    command.is(MARK) ||
                    command.is(FREEZE) ||
                    command.is(CLOSE) ||
                    command.is(HOLD) ) {
            
                boolean remove = false;
                String name = null;
                if ( cmd.length > 4 ) {
                    remove = cmd[4].charAt(0) == '-';
                    if ( remove ) {
                        name = cmd[4].substring ( 1 );
                    } else {
                        name = cmd[4];
                    }
                }
                result.setCommand ( command );
                if ( ! ChanServ.enoughAccess ( user, command ) ) {
                    result.setStatus ( ACCESS_DENIED );                
                } else if ( isShorterThanLen ( 5, cmd ) ) {
                    result.setStatus ( SYNTAX_ERROR );                
                } else if ( ( ci = ChanServ.findChan ( cmd[4].replace ( "-", "" ) ) ) == null ) {
                    result.setString1 ( cmd[4].replace ( "-", "" ) );
                    result.setStatus ( CHAN_NOT_REGISTERED );                
                } else if ( ci.isSet ( MARK ) && ( !command.is(MARK) || (command.is(MARK) && ! remove) ) ) {
                    result.setChanInfo ( ci );
                    result.setStatus ( IS_MARKED );                
                } else if ( ci.isSet ( command ) && 
                            ! command.is(MARK) && 
                            ! remove ) {
                    result.setChanInfo ( ci );
                    result.setString1 ( this.getCommandStr ( command ) );
                    result.setStatus ( CHANFLAG_EXIST );                
                } else if ( ! ci.isSet ( command ) && remove ) {
                    result.setChanInfo ( ci );
                    result.setString1 ( this.getCommandStr ( this.getAntiCommand ( command ) ) );
                    result.setStatus ( CHANFLAG_EXIST );                
                } else {
                    result.setChanInfo ( ci );
                    if ( remove ) {
                        result.setCommand ( this.getAntiCommand ( command ) );
                    }
                }
        
        } else if ( command.is(DELETE) ||
                    command.is(GETPASS) ) {
                result.setCommand ( command );
                if ( ! ChanServ.enoughAccess ( user, command ) ) {
                    result.setStatus ( ACCESS_DENIED );
                } else if ( isShorterThanLen ( 5, cmd ) ) {
                    result.setStatus ( SYNTAX_ERROR );
                } else if ( ( ci = ChanServ.findChan ( cmd[4] ) ) == null ) {
                    result.setString1 ( cmd[4] );
                    result.setStatus ( CHAN_NOT_REGISTERED );
                } else if ( ci.isSet(MARK) ) {
                    result.setChanInfo ( ci );
                    result.setStatus ( IS_MARKED );
                } else {
                    result.setChanInfo ( ci );
                }
        }
        return result;
    }
    
    private HashString getAntiCommand ( HashString command ) {
        if      ( command.is(MARK) )            { return UNMARK; }
        else if ( command.is(FREEZE) )          { return UNFREEZE; }
        else if ( command.is(CLOSE) )           { return REOPEN; }
        else if ( command.is(HOLD) )            { return UNHOLD; }
        else if ( command.is(AUDITORIUM) )      { return UNAUDITORIUM; }
        else {
            return new HashString ( "0" );
        }
    }
    private String getCommandStr ( HashString command ) {
        if      ( command.is(MARK) )            { return "Marked"; }
        else if ( command.is(FREEZE) )          { return "Frozen"; }
        else if ( command.is(CLOSE) )           { return "Closed"; }
        else if ( command.is(HOLD) )            { return "Held"; }
        else if ( command.is(UNMARK) )          { return "UnMarked"; }
        else if ( command.is(UNFREEZE) )        { return "UnFrozen"; }
        else if ( command.is(REOPEN) )          { return "Open"; }
        else if ( command.is(UNHOLD) )          { return "UnHeld"; }
        else if ( command.is(AUDITORIUM) )      { return "Auditorium"; }
        else if ( command.is(UNAUDITORIUM) )    { return "UnAuditorium"; }
        else {
            return "";
        } 
    }
    
    private String getListStr ( HashString command ) {
        if ( command.is(SOP) ) {
            return "SOP";
        } else if ( command.is(AOP) ) {
            return "AOP";
        } else if ( command.is(AKICK) ) {
            return "AKick";
        } else {
            return "Undefined";
        }
    }
    
    private HashString isAddOrDel ( HashString command ) {
        if ( command == null ) {
            return null;
        }
        if ( command.is(ADD) ||
             command.is(DEL) ) {
            return command;
        } 
        return null;
    }

    /**
     *
     * @param output
     * @param args
     * @return
     */
    public String output ( HashString output, String... args )  {
        
        if ( output.is(SYNTAX_ERROR) ) 
            return "Syntax: /ChanServ "+args[0]+"";
        
        else if ( output.is(SYNTAX_ID_ERROR) ) 
            return "Syntax: /ChanServ IDENTIFY <channame> <password>";

        else if ( output.is(SYNTAX_REG_ERROR) ) 
            return "Syntax: /ChanServ REGISTER <#chan> <password> <description>";

        else if ( output.is(CMD_NOT_FOUND_ERROR) ) 
            return "Syntax Error! For information regarding commands please issue:";

        else if ( output.is(SHOW_HELP) ) 
            return "    /"+args[0]+" HELP";

        else if ( output.is(PASSWD_ERROR) ) 
            return "Error: Wrong password for chan: "+args[0];

        else if ( output.is(INVALID_EMAIL) ) 
            return "Error: "+args[0]+" is not a valid email-adress";

        else if ( output.is(INVALID_PASSWORD) ) 
            return "Error: invalid password.";

        else if ( output.is(ACCESS_DENIED) )
            return "Access denied.";

        else if ( output.is(NICK_ACCESS_DENIED) ) 
            return "Access denied to channel "+args[0]+".";

        else if ( output.is(NICK_NOT_IDENTIFIED) )
            return "Access denied. Please identify to nick "+args[0]+" before proceeding.";

        else if ( output.is(NOT_ENOUGH_ACCESS) ) 
            return "Access denied. Not enough access.";

        else if ( output.is(SETTING_NOT_FOUND) ) 
            return "Error: Setting "+args[0]+" not found.";
        
        else if ( output.is(NICK_NOT_REGISTERED) ) 
            return "Error: nick "+args[0]+" is not registered";

        else if ( output.is(NICK_NOT_AUTHED) )
            return "Error: nick "+args[0]+" is not authorized";

        else if ( output.is(CHAN_NOT_REGISTERED) ) 
            return "Error: chan "+args[0]+" is not registered";

        else if ( output.is(CHAN_ALREADY_REGGED) ) 
            return "Error: chan "+f.b ( ) +args[0]+f.b ( ) +" is already registered."; 
        
        else if ( output.is(CHAN_IS_RELAY) ) 
            return "Error: chan "+f.b()+args[0]+f.b()+" is a relay channel cannot be registered."; 

        else if ( output.is(CHAN_NOT_EXIST) ) 
            return "Error: chan "+f.b ( ) +args[0]+f.b ( ) +" does not exist.";

        else if ( output.is(NICK_NOT_EXIST) ) 
            return "Error: nick "+f.b ( ) +args[0]+f.b ( ) +" is not online.";
            
        else if ( output.is(USER_NOT_ONLINE) ) 
            return "Error: no such user "+f.b ( ) +args[0]+f.b ( ) +".";
            
        else if ( output.is(USER_NOT_OP) ) 
            return "Error: You need to be op(@) in "+f.b ( ) +args[0]+f.b ( ) +" to perform that command.";
            
        else if ( output.is(NICK_HAS_NOOP) ) 
            return "Error: "+args[0]+" does not wish to be added to any channel list (NOOP) ";
            
        else if ( output.is(WILL_NOW) ) 
            return "Your chan will now "+args[0];
            
        else if ( output.is(WILL_NOW_NOT) ) 
            return "Your chan will now not "+args[0];
            
        else if ( output.is(IS_NOW) ) 
            return "Your chan is now "+args[0]+".";
            
        else if ( output.is(IS_NOT) ) 
            return "Your chan is not "+args[0]+" anymore.";
            
        else if ( output.is(PASSWD_ACCEPTED) ) 
            return "Password accepted for chan "+args[0]+". You are now identified.";
         
        else if ( output.is(DB_ERROR) ) 
            return "Error: Database error. Please try register again in a few minutes.";

        else if ( output.is(DB_NICK_ERROR) ) 
            return "Error: Database Chan error. Please try again.";

        else if ( output.is(REGISTER_DONE) ) 
            return "Chan "+args[0]+" was successfully registered to you. Please remember your password.";

        else if ( output.is(REGISTER_SEC) ) 
            return "IMPORTANT: Never share your passwords, not even with network staff.";

        else if ( output.is(NICK_IS_FOUNDER) ) 
            return "Error: "+f.b ( ) +args[0]+f.b ( ) +" is the founder.";

        else if ( output.is(NICK_IS_SOP) ) 
            return "Error: "+f.b ( ) +args[0]+f.b ( ) +" is already "+args[1]+".";

        else if ( output.is(NICK_IS_OP) ) 
            return "Error: "+f.b ( ) +args[0]+f.b ( ) +" already has access to the channel.";

        else if ( output.is(NICK_NOT_FOUND) ) 
            return "Error: "+f.b ( ) +args[0]+f.b ( ) +" not found on "+args[1]+" list.";
         
        else if ( output.is(NICK_CHANGED) ) 
            return f.b ( ) +args[0]+f.b ( ) +" "+args[1]+" the "+args[2]+" list.";
         
        else if ( output.is(NICK_NOT_IDENTED_OP) ) 
            return f.b ( ) +args[0]+f.b ( ) +" has not identified for a nickname on Sop or Aop lists.";
            
        else if ( output.is(NICK_NEVEROP) ) 
            return f.b ( ) +args[0]+f.b ( ) +" never want to be oped(@) .";
         
        else if ( output.is(NICK_NOT_PRESENT) ) 
            return f.b ( ) +args[0]+f.b ( ) +" is not present in the channel.";
         
        else if ( output.is(NICK_ADDED) ) 
            return f.b ( ) +args[0]+f.b ( ) +" has been added to the "+args[1]+" list.";
         
        else if ( output.is(NICK_NOT_ADDED) ) 
            return f.b ( ) +args[0]+f.b ( ) +" has not been added to the "+args[1]+" list.";
         
        else if ( output.is(NICK_DELETED) ) 
            return f.b ( ) +args[0]+f.b ( ) +" has been deleted from the "+args[1]+" list.";
         
        else if ( output.is(NICK_NOT_DELETED) ) 
            return f.b ( ) +args[0]+f.b ( ) +" has not been deleted from the "+args[1]+" list.";
         
        else if ( output.is(NICK_VERBOSE_ADDED) ) 
            return f.b ( ) +args[0]+f.b ( ) +" has added "+args[1]+" to the "+args[2]+" list.";
         
        else if ( output.is(NICK_INVITED) ) 
            return f.b ( ) +args[0]+f.b ( ) +" was invited to "+args[2]+".";
         
        else if ( output.is(NICK_VERBOSE_DELETED) ) 
            return f.b ( ) +args[0]+f.b ( ) +" has removed "+args[1]+" from the "+args[2]+" list.";

        else if ( output.is(NICK_VERBOSE_OP) ) 
            return f.b ( ) +args[0]+f.b ( ) +" has oped(@) "+args[1]+" in "+args[2]+".";

        else if ( output.is(NICK_VERBOSE_DEOP) ) 
            return f.b ( ) +args[0]+f.b ( ) +" has deOped(@) "+args[1]+" in "+args[2]+".";

        else if ( output.is(NICK_OP) ) 
            return f.b ( ) +args[0]+f.b ( ) +" has been oped(@) in "+args[1]+".";

        else if ( output.is(NICK_DEOP) ) 
            return f.b ( ) +args[0]+f.b ( ) +" has been deOped(@) in "+args[1]+".";

        else if ( output.is(NICK_MDEOP_CHAN) ) 
            return f.b ( ) +args[0]+f.b ( ) +" used MDEOP on "+args[1]+".";

        else if ( output.is(NICK_MDEOP) ) 
            return "Channel "+f.b ( ) +args[0]+f.b ( ) +" has been Mass-DeOped(@) .";

        else if ( output.is(NICK_MKICK_CHAN) ) 
            return f.b ( ) +args[0]+f.b ( ) +" used MKICK on "+args[1]+".";

        else if ( output.is(NICK_MKICK) ) 
            return "Channel "+f.b ( ) +args[0]+f.b ( ) +" has been Mass-Kicked.";

        else if ( output.is(LIST_NOT_WIPED) ) 
            return args[0]+" list of "+args[1]+" was not wiped.";

        else if ( output.is(LIST_WIPED) ) 
            return args[0]+" list of "+args[1]+" has been wiped.";

        else if ( output.is(LIST_VERBOSE_WIPED) ) 
            return args[0]+" has wiped the "+args[1]+" list of "+args[2]+".";

        else if ( output.is(CHAN_IS_FROZEN) ) 
            return "Channel "+args[0]+" is Frozen by an IRC operator.";

        else if ( output.is(CHAN_IS_CLOSED) ) 
            return "Channel "+args[0]+" is Closed by an IRC operator.";

        else if ( output.is(IS_THROTTLED) ) 
            return "Error: Throttled login attempts.";

        else if ( output.is(MODELOCK) ) 
            return "Channel "+args[0]+" has now locked its modes as: "+args[1]+".";

        else if ( output.is(CHAN_SET_FLAG) ) 
            return "Channel "+args[0]+" is now "+args[1]+".";

        else if ( output.is(ALREADY_ON_LIST) ) 
            return args[0]+" is already on "+args[1]+" list";

        else if ( output.is(CHAN_GETPASS) ) 
            return "Password is: "+args[0]+".";

        else if ( output.is(CHAN_UNBAN) ) 
            return "Bans for "+args[0]+" has been cleared on "+args[1]+".";

        else if ( output.is(IS_MARKED) ) 
            return "Error: Chan "+args[0]+" is MARKed by a network staff.";

        else if ( output.is(SHOWACCESSLOG) ) 
            return "["+args[0]+"] "+args[1]+" "+args[2]+" ["+args[3]+"]";
            
        else if ( output.is(SHOWACCESSLOGOPER) ) 
            return "["+args[0]+"] "+args[1]+" "+args[2]+" - "+args[3]+" ["+args[4]+"]";

        else if ( output.is(SHOWTOPICLOG) ) 
            return "["+args[0]+"] "+args[1]+" : "+args[2];

        else if ( output.is(CHANNELDROPPED) ) 
            return "Channel: "+args[0]+" was successfully dropped.";

        else if ( output.is(CHANNELDELETED) ) 
            return "Channel: "+args[0]+" was successfully deleted.";

        else if ( output.is(CHANFLAG_EXIST) ) 
            return "Chan "+args[0]+" is already "+args[1]+".";

        else if ( output.is(NO_SUCH_CHANFLAG) ) 
            return "No such chanflag available: "+args[0]+".";

        else if ( output.is(BAD_CHANFLAG_VALUE) ) 
            return "Error: Invalid chanflag value.";

        else if ( output.is(XOP_NOT_FOUND) ) 
            return "Error: Entry not found.";

        else if ( output.is(XOP_ADD_FAIL) ) 
            return "Error: Failed to add entry.";

        else if ( output.is(XOP_ALREADY_PRESENT) ) 
            return "Error: "+args[0]+" is already present on "+args[1]+" list.";

        else
            return "";
 
    }
}