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
package nickserv;

import command.Command;
import core.Executor;
import core.Handler;
import core.Proc;
import core.TextFormat;
import static core.HashNumeric.SA;
import static core.HashNumeric.SRA;
import core.HashString;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import mail.SendMail;
import server.ServSock;
import user.User;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author DreamHealer
 */
 public class NSExecutor extends Executor {
    private NSSnoop                 snoop;
    private TextFormat              f;
    private static final Pattern    VALID_EMAIL_ADDRESS_REGEX = Pattern.compile ( "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE );
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    /**
     *
     * @param service
     * @param snoop
     */
    public NSExecutor ( NickServ service, NSSnoop snoop )  {
        super ( );
        this.service        = service;
        this.snoop          = snoop;
        this.f              = new TextFormat ( );
    }

    /**
     *
     * @param user
     * @param cmd
     */
    public void parse ( User user, String[] cmd ) {
        HashString command;
        if ( cmd == null || cmd[3].isEmpty ( ) ) {
            this.help ( user );
            return; 
        }
        
        command = new HashString ( cmd[3] );
        
        if ( command.is(REGISTER) ) {
            this.register ( user, cmd );
        
        } else if ( command.is(IDENTIFY) ) {
            this.identify ( user, cmd );
        
        } else if ( command.is(AUTH) ) {
            this.auth ( user, cmd );
        
        } else if ( command.is(DROP) ) {
            this.drop ( user, cmd );
        
        } else if ( command.is(SIDENTIFY) ) {
            this.sIdentify ( user, cmd );
        
        } else if ( command.is(GHOST) ) {
            this.ghost ( user, cmd );
        
        } else if ( command.is(SET) ) {
            this.set ( user, cmd );
        
        } else if ( command.is(INFO) ) {
            this.info ( user, cmd );
        
        } else if ( command.is(LIST) ) {
            this.list ( user, cmd );
        
        } else if ( command.is(MARK) ) {
            this.changeFlag ( MARK, user, cmd );
        
        } else if ( command.is(FREEZE) ) {
            this.changeFlag ( FREEZE, user, cmd );
        
        } else if ( command.is(HOLD) ) {
            this.changeFlag ( HOLD, user, cmd );
        
        } else if ( command.is(NOGHOST) ) {
            this.changeFlag ( NOGHOST, user, cmd );
        
        } else if ( command.is(GETPASS) ) {
            this.getPass ( user, cmd );
        
        } else if ( command.is(GETEMAIL) ) {
            this.getEmail ( user, cmd );
        
        } else if ( command.is(DELETE) ) {
            this.delete ( user, cmd );
        
        } else {
            this.help ( user );
        }
         
    }
 
    /**
     *
     * @param user
     */
    public void help ( User user )  {
        this.service.sendMsg ( user, output ( CMD_NOT_FOUND_ERROR, "" )  );
        this.service.sendMsg ( user, output ( SHOW_HELP, new String[] { this.service.getNameStr() } )  );
    }

    /**
     *
     * @param user
     * @param cmd
     */
    public void register ( User user, String[] cmd )  { /* DONE? */
        // :DreamHea1er PRIVMSG NickServ@services.sshd.biz :register pass email
        //       0         1              2                     3      4     5      = 6
        String pass = "";
        String mail = "";
        
        CMDResult result = this.validateCommandData ( user, REGISTER, cmd );
        
        if ( cmd.length >= 6 ) {
            pass = cmd[4];
            cmd[4] = "pass_redacted";
            mail = cmd[5];
            cmd[5] = "email_redacted";
        }
        
        if ( result.is(SYNTAX_ERROR) ) {
                this.service.sendMsg ( user, output ( SYNTAX_REG_ERROR, "" ) );
                this.snoop.msg ( false, SYNTAX_REG_ERROR, user.getNameStr(), user, cmd );
                return;            
        
        } else if ( result.is(INVALID_EMAIL) ) {
                this.service.sendMsg (user, output (INVALID_EMAIL, result.getString1() ) );
                this.snoop.msg (false, INVALID_EMAIL, new HashString ( result.getString1() ), user, cmd );
                return;             
        
        } else if ( result.is(NICK_ALREADY_REGGED) ) {
                this.service.sendMsg ( user, output ( NICK_ALREADY_REGGED, user.getNameStr() ) );
                this.snoop.msg ( false, NICK_ALREADY_REGGED, user.getNameStr(), user, cmd );
                return;              
        
        } else if ( result.is(INVALID_NICK) ) {
                this.service.sendMsg ( user, output ( INVALID_NICK, user.getNameStr() ) );
                this.snoop.msg ( false, INVALID_NICK, user.getNameStr(), user, cmd );
                return;             
        }
          
        NickInfo ni = new NickInfo ( user, pass );
        NSAuth auth = new NSAuth ( MAIL, ni.getName(), mail );
        NickServ.addNewAuth ( auth );
        NickServ.addToWorkList ( REGISTER, ni );
        NickServ.addNick ( ni );
        SendMail.sendNickRegisterMail ( ni, auth );
        user.getSID().add ( ni );
        NSLogEvent log = new NSLogEvent ( ni.getName(), REGISTER, user.getFullMask(), null );
        NickServ.addLog ( log );
        NickServ.fixIdentState ( user ); 
        this.service.sendMsg ( user, output ( REGISTER_DONE, ni.getString ( NAME ) ) );
        this.service.sendMsg ( user, f.b ( ) +output ( REGISTER_SEC, "" ) +f.b ( ) );
        this.snoop.msg ( true, REGISTER, user.getName(), user, cmd );

    }

    /**
     *
     * @param user
     * @param cmd
     */
    public void identify ( User user, String[] cmd )  {
        // :DreamHea1er PRIVMSG NickServ@services.sshd.biz :identify moew               = 5
        // :DreamHea1er PRIVMSG NickServ@services.sshd.biz :identify dreamhealer moew   = 6
        //       0         1               2                    3      4           5
 
        CMDResult result;
        
        if ( cmd.length > 5 ) {        
            result = this.validateCommandData ( user, IDENTIFY_NICK, cmd );
            cmd[5] = "pass_redacted";
        } else {
            result = this.validateCommandData ( user, IDENTIFY, cmd );
            if ( cmd.length == 5 ) {
                cmd[4] = "pass_redacted";
            }
        }
        
        if ( result.is(SYNTAX_ERROR) ) {
                this.service.sendMsg ( user, output ( SYNTAX_ID_ERROR, "" ) );
                this.snoop.msg ( false, SYNTAX_ID_ERROR, user.getName(), user, cmd );
                return;            
        
        } else if ( result.is(NICK_NOT_REGGED) ) {
                this.service.sendMsg (user, output (NICK_NOT_REGISTERED, result.getString1 ( ) ) );
                this.snoop.msg (false, NICK_NOT_REGISTERED, new HashString(result.getString1()), user, cmd );
                return;            
        
        } else if ( result.is(IDENTIFY_FAIL) ) {
                this.service.sendMsg (user, output (PASSWD_ERROR, result.getNick().getNameStr() ) ); 
                this.snoop.msg (false, PASSWD_ERROR, result.getNick().getNameStr(), user, cmd );
                return;            
        
        } else if ( result.is(IS_FROZEN) ) {
                this.service.sendMsg (user, output (IS_FROZEN, result.getNick().getNameStr() ) );
                this.snoop.msg (false, IS_FROZEN, result.getNick().getNameStr(), user, cmd );
                return;            
        
        } else if ( result.is(IS_THROTTLED) ) {
                this.service.sendMsg (user, output (IS_THROTTLED, result.getNick().getNameStr() ) );
                this.snoop.msg (false, IS_THROTTLED, result.getNick().getNameStr(), user, cmd );
                return;            
        }  
         
        NickInfo ni = result.getNick ( );
        if ( ! ni.is(user) ) {
            this.service.sendMsg ( user, output ( NICK_NEW_MASK, ni.getString ( FULLMASK ) ) );
            ni.setUserMask ( user );        
        }
        this.service.sendMsg ( user, output ( PASSWD_ACCEPTED, ni.getString ( NAME ) ) );
        //ni.setUserMask ( user );           /* Set user mask */
        user.getSID().add ( ni );          /* Add nick to user sid */
        NickServ.fixIdentState ( user );
        ni.getNickExp().reset ( );
        ni.setLastUsed();
        ni.getChanges().hasChanged ( LASTUSED );
        NickServ.addToWorkList ( CHANGE, ni );
        Handler.addUpdateSID ( user.getSID() );
        this.snoop.msg ( true, IDENTIFY, ni.getName(), user, cmd );
    }
 
    private void drop ( User user, String[] cmd ) {
        // :DreamHea1er PRIVMSG NickServ@services.sshd.biz :drop <pass>   = 5
        //       0         1               2                    3    4           
        CMDResult result = this.validateCommandData ( user, DROP, cmd );
        
        if ( cmd.length == 5 ) {
            cmd[4] = "pass_redacted";
        }
        
        if ( result.is(SYNTAX_ERROR) ) {
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "DROP <pass>" ) );
                this.snoop.msg ( false, SYNTAX_ERROR, user.getName(), user, cmd );
                return;            
        
        } else if ( result.is(NICK_NOT_REGGED) ) {
                this.service.sendMsg (user, output (NICK_NOT_REGISTERED, result.getString1 ( ) ) );
                this.snoop.msg (false, NICK_NOT_REGISTERED, new HashString(result.getString1()), user, cmd );
                return;            
        
        } else if ( result.is(IDENTIFY_FAIL) ) {
                this.service.sendMsg (user, output (PASSWD_ERROR, result.getNick().getNameStr() ) ); 
                this.snoop.msg (false, PASSWD_ERROR, result.getNick().getNameStr(), user, cmd );
                return;            
        
        } else if ( result.is(IS_MARKED) ) {
                this.service.sendMsg (user, output (IS_MARKED, result.getNick().getNameStr() ) ); 
                this.snoop.msg (false, IS_MARKED, result.getNick().getNameStr(), user, cmd );
                return;             
        
        } else if ( result.is(IS_FROZEN) ) {
                this.service.sendMsg (user, output (IS_FROZEN, result.getNick().getNameStr() ) );
                this.snoop.msg (false, IS_FROZEN, result.getNick().getNameStr(), user, cmd );
                return;            
        }
         
        NickInfo ni = result.getNick ( );
        
        Handler.getNickServ().dropNick ( ni );
        this.service.sendMsg ( user, output ( NICKDROPPED, ni.getString ( NAME ) ) );
        NSLogEvent log = new NSLogEvent ( ni.getName(), DROP, user.getFullMask(), "" );
        NickServ.addLog ( log );
        NickServ.addToWorkList ( DELETE, ni );
        this.snoop.msg ( true, DROP, ni.getName(), user, cmd );

    }
    
    private void delete ( User user, String[] cmd ) {
        // :DreamHea1er PRIVMSG NickServ@services.sshd.biz :drop <pass>   = 5
        //       0         1               2                    3    4           
        CMDResult result = this.validateCommandData ( user, DELETE, cmd );
        
        if ( result.is(SYNTAX_ERROR) ) {
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "DELETE <nick>" ) );
                this.snoop.msg ( false, SYNTAX_ERROR, user.getNameStr(), user, cmd );
                return;
        
        } else if ( result.is(NICK_NOT_REGGED) ) {
                this.service.sendMsg (user, output (NICK_NOT_REGISTERED, result.getString1 ( ) ) );
                this.snoop.msg (false, NICK_NOT_REGISTERED, result.getString1(), user, cmd );
                return;
        
        } else if ( result.is(IDENTIFY_FAIL) ) {
                this.service.sendMsg (user, output (PASSWD_ERROR, result.getNick().getNameStr( ) ) ); 
                this.snoop.msg (false, PASSWD_ERROR, result.getNick().getNameStr(), user, cmd );
                return;
        
        } else if ( result.is(IS_MARKED) ) {
                this.service.sendMsg (user, output (IS_MARKED, result.getNick().getNameStr( ) ) ); 
                this.snoop.msg (false, IS_MARKED, result.getNick().getNameStr(), user, cmd );
                return;
        }  
        
        NickInfo ni = result.getNick();
        Handler.getNickServ().dropNick ( ni );
        this.service.sendMsg ( user, output ( NICKDELETED, ni.getNameStr() ) );
        this.service.sendGlobOp ( "Nick "+ni.getName()+" has been DELETED by "+user.getOper().getNameStr() );
        NSLogEvent log = new NSLogEvent ( ni.getName(), DELETE, user.getFullMask(), user.getOper().getNameStr() );
        NickServ.addLog ( log );
        this.snoop.msg ( true, DELETE, ni.getName(), user, cmd );
    }
   
    /**
     *
     * @param user
     * @param cmd
     */
    public void list ( User user, String[] cmd )  { /* DONE? */
        // :DreamHea1er PRIVMSG NickServ@services.sshd.biz :list dream*
        //      0          1            2                     3    4     = 5
    
        CMDResult result = this.validateCommandData ( user, LIST, cmd );
        
        if ( result.is(SYNTAX_ERROR) ) {
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "LIST <pattern>" ) );
                this.snoop.msg ( false, SYNTAX_ERROR, user.getName(), user, cmd );
                return;            
        
        } else if ( result.is(ACCESS_DENIED_OPER) ) {
                this.service.sendMsg ( user, output ( ACCESS_DENIED_OPER, "" ) );
                this.snoop.msg ( false, ACCESS_DENIED_OPER, "", user, cmd );
                return;            
        
        } else if ( result.is(ACCESS_DENIED_SA) ) {
                this.service.sendMsg ( user, output ( ACCESS_DENIED_SA, "" ) );
                this.snoop.msg ( false, ACCESS_DENIED_SA, "", user, cmd );
                return;            
        }
        
        ArrayList<NickInfo> nList = NickServ.searchNicks ( cmd[4] );
        this.service.sendMsg ( user, f.b ( ) +"List:"+f.b ( ) ); 
        for ( NickInfo ni2 : nList ) {
            this.service.sendMsg ( user, f.b ( ) +"    "+ni2.getName()+" - ("+ni2.getString ( FULLMASK )+")" );
        }
        this.showEnd ( user, "Info" );
        this.snoop.msg ( true, LIST, user.getOper().getName(), user, cmd );
    }
  
    /**
     *
     * @param user
     * @param cmd
     */
    public void sIdentify ( User user, String[] cmd )  {
        // :DreamHea1er PRIVMSG NickServ@services.sshd.biz :sidentify moew               = 5
        // :DreamHea1er PRIVMSG NickServ@services.sshd.biz :sidentify dreamhealer moew   = 6
        //       0         1               2                    3      4           5

        NickInfo ni;
        CMDResult result;
        
        if ( cmd.length == 6 ) {        
            result = this.validateCommandData ( user, IDENTIFY_NICK, cmd );
            cmd[5] = "pass_redacted";
        } else {
            result = this.validateCommandData ( user, IDENTIFY, cmd );
            if ( cmd.length == 5 ) {
                cmd[4] = "pass_redacted";
            }
        }
         
        if ( result.is(SYNTAX_ERROR) ) {
                this.service.sendMsg ( user, output ( SYNTAX_ID_ERROR, "" )  );
                this.snoop.msg ( false, SYNTAX_ID_ERROR, user.getNameStr(), user, cmd );
                return;            
        
        } else if ( result.is(NICK_NOT_REGGED) ) {
                this.service.sendMsg (user, output (NICK_NOT_REGISTERED, result.getString1() ) );
                this.snoop.msg (false, NICK_NOT_REGISTERED, result.getString1(), user, cmd );
                return;            
        
        } else if ( result.is(IDENTIFY_FAIL) ) {
                this.service.sendMsg (user, output (PASSWD_ERROR, result.getNick().getNameStr() ) ); 
                this.snoop.msg (false, PASSWD_ERROR, result.getNick().getNameStr(), user, cmd );
                return;            
        
        } else if ( result.is(IS_FROZEN) ) {
                this.service.sendMsg (user, output (IS_FROZEN, result.getNick().getNameStr() ) );
                this.snoop.msg (false, IS_FROZEN, result.getNick().getNameStr(), user, cmd );
                return;            
        
        } else if ( result.is(IS_THROTTLED) ) {
                this.service.sendMsg (user, output (IS_THROTTLED, result.getNick().getNameStr() ) );
                this.snoop.msg (false, IS_THROTTLED, result.getNick().getNameStr(), user, cmd );
                return;            
        }  
         
        ni = result.getNick ( );
        if ( ! ni.isMask(user) ) {
            this.service.sendMsg ( user, output ( NICK_NEW_MASK, ni.getString ( FULLMASK ) ) );
            ni.setUserMask ( user );
        } else {
            this.service.sendMsg ( user, output ( PASSWD_ACCEPTED, ni.getString ( NAME ) ) );
        }
        user.getSID().add ( ni );
        NickServ.fixIdentState ( user );
        ni.getChanges().hasChanged ( LASTUSED );
        NickServ.addToWorkList ( CHANGE, ni );
        this.snoop.msg ( true, SIDENTIFY, ni.getName(), user, cmd );
    }

    /**
     *
     * @param user
     * @param cmd
     */
    public void ghost ( User user, String[] cmd )  {
        // :DreamHea1er PRIVMSG NickServ@services.sshd.biz :ghost dreamhealer pass  
        //            0       1                          2      3           4    5  = 6
        NickInfo ni;
        String nick = "";
        
        CMDResult result = this.validateCommandData ( user, GHOST, cmd );
 
        if ( result.getStatus() != SYNTAX_ERROR )  {
            nick = cmd[4];       
            cmd[5] = "pass_redacted";
        }
        
        if ( result.is(SYNTAX_ERROR) ) {
                this.service.sendMsg ( user, output ( SYNTAX_GHOST_ERROR, "" ) );
                this.snoop.msg ( false, SYNTAX_GHOST_ERROR, user.getNameStr(), user, cmd );
                return;            
        
        } else if ( result.is(NICK_NOT_REGGED) ) {
                this.service.sendMsg ( user, output ( NICK_NOT_REGISTERED, nick ) );
                this.snoop.msg ( false, NICK_NOT_REGISTERED, user.getNameStr(), user, cmd );
                return;            
        
        } else if ( result.is(IDENTIFY_FAIL) ) {
                this.service.sendMsg ( user, output ( PASSWD_ERROR, nick ) );
                this.snoop.msg ( false, PASSWD_ERROR, nick, user, cmd );
                return;            
        
        } else if ( result.is(NO_SUCH_NICK) ) {
                this.service.sendMsg (user, output (NO_SUCH_NICK, result.getString1() ) );
                this.snoop.msg ( false, NO_SUCH_NICK, user.getNameStr(), user, cmd );
                return;            
        
        } else if ( result.is(IS_FROZEN) ) {
                this.service.sendMsg (user, output (IS_FROZEN, result.getNick().getNameStr() ) );
                this.snoop.msg ( false, IS_FROZEN, result.getNick().getNameStr(), user, cmd );
                return;            
        
        } else if ( result.is(IS_NOGHOST) ) {
                this.service.sendMsg (user, output (IS_NOGHOST, result.getNick().getNameStr() ) );
                this.service.sendGlobOp (output (GLOB_IS_NOGHOST, user.getFullMask(), result.getNick().getNameStr() ) );
                this.snoop.msg (false, IS_NOGHOST, result.getNick().getNameStr(), user, cmd );
                return;            
        }  
         
        ni = result.getNick ( );
        this.service.sendMsg ( user, output ( PASSWD_ACCEPTED, ni.getString ( NAME ) ) );
        ServSock.sendCmd ( ":"+Proc.getConf().get ( NAME ) +" SVSKILL "+ni.getNameStr()+" :Ghost exorcised by: "+user.getNameStr() ); /* kill the ghosted nick */
        NickServ.fixIdentState ( user );
        user.getSID().add ( ni );
        ni.getChanges().hasChanged ( LASTUSED );
        NickServ.addToWorkList ( CHANGE, ni );
        this.snoop.msg ( true, GHOST, ni.getNameStr(), user, cmd );
    }
    
    /**
     *
     * @param user
     * @param cmd
     */
    public void info ( User user, String[] cmd )  { /* DONE? */
        // :DreamHea1er PRIVMSG NickServ@services.sshd.biz :info dreamhealer
        //      0          1            2                     3       4     = 5
        NickInfo ni;
        CMDResult result = this.validateCommandData ( user, INFO, cmd );
        
        if ( result.is(SYNTAX_ERROR) ) {
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "" ) );
                this.snoop.msg ( false, SYNTAX_ERROR, user.getName(), user, cmd );
                return;            
        
        } else if ( result.is(NICK_NOT_REGGED) ) {
                this.service.sendMsg ( user, output ( NICK_NOT_REGISTERED, cmd[4] ) );
                this.snoop.msg ( false, NICK_NOT_REGISTERED, cmd[4], user, cmd );
                return;            
        }
         
        ni = result.getNick ( );
        this.showStart ( true, user, ni, f.b ( ) +"Info for: "+f.b ( ) ); 
        this.service.sendMsg ( user, f.b ( ) +"    Hostmask: "+f.b ( ) +ni.getString ( USER )+"@"+ni.getString ( HOST ) );        
        this.service.sendMsg ( user, f.b ( ) +"  Registered: "+f.b ( ) +ni.getString ( REGTIME ) );
        this.service.sendMsg ( user, f.b ( ) +"   Last seen: "+f.b ( ) +ni.getString ( LASTUSED ) );
        this.service.sendMsg ( user, f.b ( ) +"    Time now: "+f.b ( ) +dateFormat.format ( new Date ( ) ) );
        if ( ni.getSettings().is(SHOWEMAIL) ) {
            this.service.sendMsg(user, f.b ( ) +"       Email: "+f.b ( ) +ni.getEmail ( ) );
        }
        if ( ni.getSettings().getInfoStr().length() > 0 ) {
            this.service.sendMsg ( user, f.b ( ) +"    Settings: "+f.b ( ) +ni.getSettings().getInfoStr ( ) );
        }
        /* Show that the nick hasnt been authed if it hasnt */
        
        if ( user.isAtleast ( IRCOP ) ) {
            if ( ni.isSet(FROZEN) || ni.isSet(MARKED) || ni.isSet(HELD) || ni.isSet (NOGHOST) ) {
                this.service.sendMsg ( user, f.b ( ) +"   --- IRCop ---" );
            }
            if ( ni.isSet ( FROZEN ) ) {
                this.service.sendMsg ( user, f.b ( ) +"      Frozen: "+ni.getSettings().getInstater ( FREEZE ) );
            }
            if ( ni.isSet ( MARKED ) ) {
                this.service.sendMsg ( user, f.b ( ) +"      Marked: "+ni.getSettings().getInstater ( MARK ) );
            }
            if ( ni.isSet ( HELD ) ) {
                this.service.sendMsg ( user, f.b ( ) +"        Held: "+ni.getSettings().getInstater ( HOLD ) );
            }
            if ( ni.isSet ( NOGHOST ) ) {
                this.service.sendMsg ( user, f.b ( ) +"     NoGhost: "+ni.getSettings().getInstater ( NOGHOST ) );
            }
        }
        this.showEnd ( user, "Info" );
        this.snoop.msg ( true, INFO, ni.getName(), user, cmd );
    }


    /**
     *
     * @param user
     * @param cmd
     */
    public void set ( User user, String[] cmd )  {
        /* :DreamHea1er PRIVMSG NickServ@services.sshd.biz :set enforce on          */
        /*      0          1               2                 3     4     5 = 6      */
        NickInfo ni;
        boolean enable;
        HashString command;
        HashString subcommand;
        
        if ( cmd.length < 6 )  {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "SET <option> ON/OFF" ) );
            this.snoop.msg ( false, SYNTAX_ERROR, user.getName(), user, cmd );

        } else if ( ( ni = NickServ.findNick ( cmd[0] ) ) == null ) {
            this.service.sendMsg ( user, output ( NICK_NOT_REGISTERED, cmd[0] ) );
            this.snoop.msg ( false, NICK_NOT_REGISTERED, cmd[0], user, cmd );

        } else if ( ! user.isIdented ( ni ) ) {
            /* User not idented */
            this.service.sendMsg ( user, output ( ACCESS_DENIED, ni.getNameStr() ) );
            this.snoop.msg ( false, ACCESS_DENIED, ni.getName(), user, cmd );            

        } else {
            enable = false;
            command = new HashString ( cmd[4] );
            subcommand = new HashString ( cmd[5] );
            
            if ( subcommand.is(ON) ) {
                enable = true;
            } else if ( subcommand.is(OFF) ) {
                enable = false;
            }
             
            if ( command.is(NOOP) ) {
                doSetBoolean ( NOOP, "NoOp", user, ni, enable, cmd );
            } else if ( command.is(NEVEROP) ) {
                doSetBoolean ( NEVEROP, "NeverOp", user, ni, enable, cmd );
            } else if ( command.is(MAILBLOCK) ) {
                doSetBoolean ( MAILBLOCK, "MailBlock", user, ni, enable, cmd );
            } else if ( command.is(SHOWEMAIL) ) {
                doSetBoolean ( SHOWEMAIL, "ShowEmail", user, ni, enable, cmd );
            } else if ( command.is(SHOWHOST) ) {
                doSetBoolean ( SHOWHOST, "ShowHost", user, ni, enable, cmd );
            } else if ( command.is(EMAIL) ) {
                doSetString ( SETEMAIL, user, cmd );
            } else if ( command.is(PASSWD) ) {
                doSetString ( SETPASSWD, user, cmd );
            } else {
                this.service.sendMsg ( user, output ( SETTING_NOT_FOUND, cmd[4] ) );
                this.snoop.msg ( false, SET, ni.getName(), user, cmd );
            }  
             
        }
    }
    
    private void changeFlag ( HashString flag, User user, String[] cmd ) {
        CMDResult result = this.validateCommandData ( user, flag, cmd );
        
        if ( result.is(SYNTAX_ERROR) ) {
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, NickSetting.hashToStr ( flag )+" <[-]nick>" )  );
                this.snoop.msg ( false, SYNTAX_ERROR, user.getName(), user, cmd );
                return;            
        
        } else if ( result.is(ACCESS_DENIED) ) {
                if ( result.getString1() != null ) {
                    this.service.sendMsg (user, output (ACCESS_DENIED, result.getString1 ( ) ) ); 
                    this.snoop.msg (false, ACCESS_DENIED, new HashString ( result.getString1() ), user, cmd );
                }
                return;             
        
        } else if ( result.is(NICK_NOT_REGISTERED) ) {
                this.service.sendMsg ( user, output ( NICK_NOT_REGISTERED, result.getString1() ) );
                this.snoop.msg ( false, NICK_NOT_REGISTERED, new HashString ( result.getString1() ), user, cmd );
                return;            
        
        } else if ( result.is(NICKFLAG_EXIST) ) {
                this.service.sendMsg (user, output (NICKFLAG_EXIST, result.getNick().getNameStr(), result.getString1 ( ) ) ); 
                this.snoop.msg (false, NICKFLAG_EXIST, result.getNick().getName(), user, cmd );
                return;             
        
        } else if ( result.is(IS_MARKED) ) {
                this.service.sendMsg (user, output (IS_MARKED, result.getNick().getNameStr() ) ); 
                this.snoop.msg (false, IS_MARKED, result.getNick().getName ( ), user, cmd );
                return;             
        }
         
        NickInfo ni = result.getNick ( );
        NickInfo oper = user.getOper().getNick ( );
        HashString command = result.getCommand ( );
        NSLogEvent log;
        
        if ( command.is(UNNOGHOST) ||
             command.is(UNMARK) ||
             command.is(UNFREEZE) ||
             command.is(UNHOLD) ) {
                NickInfo instater = NickServ.findNick ( ni.getSettings().getInstater ( flag ) );
                if ( ! user.isIdented ( instater ) && ! user.isAtleast ( SRA ) ) {
                    this.service.sendMsg ( user, "Error: flag can only be removed by: "+instater.getName()+" or a SRA+." );
                    return;
                }
                ni.getSettings().set ( flag, "" );
                ni.getChanges().change ( flag );
                NickServ.addToWorkList ( CHANGE, ni );
                log = new NSLogEvent ( ni.getName(), command, user.getFullMask(), oper.getNameStr() );
                NickServ.addLog ( log );
                this.service.sendMsg ( user, output ( NICK_SET_FLAG, ni.getNameStr(), "Un"+ni.getSettings().modeString ( flag ) ) );
                this.service.sendGlobOp ( "Nick "+ni.getName()+" has been Un"+ni.getSettings().modeString(flag)+" by "+oper.getName() );
                this.snoop.msg (true, flag, result.getNick().getName ( ), user, cmd );            
        
        } else if ( command.is(NOGHOST) ||
                    command.is(MARK) ||
                    command.is(FREEZE) ||
                    command.is(HOLD) ) {
                ni.getSettings().set ( flag, oper.getName() );
                ni.getChanges().change ( flag ); 
                NickServ.addToWorkList ( CHANGE, ni );
                log = new NSLogEvent ( ni.getName(), command, user.getFullMask(), oper.getNameStr() );
                NickServ.addLog ( log );
                if ( command.is(FREEZE) ) {
                    NickServ.unIdentifyAllFromNick ( ni );
                }
                this.service.sendMsg ( user, output ( NICK_SET_FLAG, ni.getNameStr(), ni.getSettings().modeString ( flag ) ) );
                this.service.sendGlobOp ( "Nick "+ni.getName()+" has been "+ni.getSettings().modeString(flag)+" by "+oper.getName() );
                this.snoop.msg (true, flag, result.getNick().getName ( ), user, cmd );            
        
        } else {
            this.snoop.msg (false, flag, result.getNick().getName ( ), user, cmd );
        }
         
    }
    
    /* Can possibly merge getPass and getEmail methods in the future */
    private void getPass ( User user, String[] cmd ) {
        CMDResult result = this.validateCommandData ( user, GETPASS, cmd );
        
        if ( result.is(SYNTAX_ERROR) ) {
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "" ) );
                this.snoop.msg ( false, SYNTAX_ERROR, user.getName(), user, cmd );
                return;            
        
        } else if ( result.is(ACCESS_DENIED) ) {
                this.service.sendMsg (user, output (ACCESS_DENIED, result.getString1 ( ) ) ); 
                this.snoop.msg (false, ACCESS_DENIED, result.getString1 ( ), user, cmd );
                return;            
        
        } else if ( result.is(NICK_NOT_REGGED) ) {
                this.service.sendMsg ( user, output ( NICK_NOT_REGISTERED, cmd[4] ) );
                this.snoop.msg ( false, NICK_NOT_REGISTERED, cmd[4], user, cmd );
                return;            
        
        } else if ( result.is(IS_MARKED) ) {
                this.service.sendMsg (user, output (IS_MARKED, result.getNick().getNameStr() ) ); 
                this.snoop.msg (false, IS_MARKED, result.getNick().getName ( ), user, cmd );
                return;            
        }
         
        NickInfo ni = result.getNick ( ); 
        NickInfo oper = user.getOper().getNick ( );
        HashString command = result.getCommand ( );
        this.service.sendMsg ( user, "*** Password log of "+ni.getName()+":" );
        if ( ! NSDatabase.checkConn() ) {
            this.service.sendMsg ( user, "No passwords currently available as no database connection is present." );
        } else {
            ArrayList<NSAuth> pList = NSDatabase.getAuthsByNick ( PASS, ni.getName() );
            for ( NSAuth pass : pList ) {
                String auth = ( pass.getAuth() == null ? "A" : "N" );
                this.service.sendMsg ( user, output ( NICK_GETEMAIL, pass.getStamp(), auth, pass.getValue() ) );
            }
        }       
        this.service.sendMsg ( user, "*** End of log ***" );
        NSLogEvent log = new NSLogEvent ( ni.getName(), command, user.getFullMask(), oper.getNameStr() );
        NickServ.addLog ( log );
        this.service.sendGlobOp ( oper.getName()+" used GETPASS on: "+ni.getName() );
    }
 
    private void getEmail ( User user, String[] cmd ) {
        CMDResult result = this.validateCommandData ( user, GETEMAIL, cmd );
        
        if ( result.is(SYNTAX_ERROR) ) {
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "" ) );
                this.snoop.msg ( false, SYNTAX_ERROR, user.getName(), user, cmd );
                return;            
        } else if ( result.is(ACCESS_DENIED) ) {
                this.service.sendMsg (user, output (ACCESS_DENIED, result.getString1 ( ) ) );
                this.snoop.msg (false, ACCESS_DENIED, result.getString1 ( ), user, cmd );
                return;            
        } else if ( result.is(NICK_NOT_REGGED) ) {
                this.service.sendMsg ( user, output ( NICK_NOT_REGISTERED, cmd[4] ) );
                this.snoop.msg ( false, NICK_NOT_REGISTERED, cmd[4], user, cmd );
                return;            
        } else if ( result.is(IS_MARKED) ) {
                this.service.sendMsg (user, output (IS_MARKED, result.getNick().getNameStr() ) ); 
                this.snoop.msg (false, IS_MARKED, result.getNick().getName ( ), user, cmd );
                return;            
        }
        
        NickInfo ni = result.getNick ( ); 
        NickInfo oper = user.getOper().getNick ( );
        HashString command = result.getCommand ( );
        this.service.sendMsg ( user, "*** Email log of "+ni.getName()+":" );
        
        if ( ! NSDatabase.checkConn() ) {
            this.service.sendMsg ( user, "No mails currently available as no database connection is present." );
        } else {
            ArrayList<NSAuth> eList = NSDatabase.getAuthsByNick ( MAIL, ni.getName() );
            for ( NSAuth mail : eList ) {
                String auth = ( mail.getAuth() == null ? "A" : "N" );
                this.service.sendMsg ( user, output ( NICK_GETEMAIL, mail.getStamp(), auth, mail.getValue() ) );
            }
        }       
        this.service.sendMsg ( user, "*** End of log ***" );
        NSLogEvent log = new NSLogEvent ( ni.getName(), command, user.getFullMask(), oper.getNameStr() );
        NickServ.addLog ( log );
        this.service.sendGlobOp ( oper.getName()+" used GETEMAIL on: "+ni.getName() );
    }
 
    
    
    private void auth ( User user, String[] cmd ) {
        CMDResult result = this.validateCommandData ( user, AUTH, cmd );
        
        if ( result.is(SYNTAX_ERROR) ) {
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "" ) );
                this.snoop.msg ( false, SYNTAX_ERROR, user.getName(), user, cmd );                
                return;            
        } else if ( result.is(NICK_NOT_REGGED) ) {
                this.service.sendMsg ( user, output ( NICK_NOT_REGISTERED, cmd[4] ) );
                this.snoop.msg ( false, NICK_NOT_REGISTERED, cmd[4], user, cmd );
                return;            
        } else if ( result.is(NO_AUTH_FOUND) ) {
                this.service.sendMsg ( user, output ( NO_AUTH_FOUND, "" ) );
                this.snoop.msg ( false, NO_AUTH_FOUND, user.getName(), user, cmd );
                return;            
        } else if ( result.is(IS_MARKED) ) {
                this.service.sendMsg (user, output (IS_MARKED, result.getNick().getNameStr() ) ); 
                this.snoop.msg (false, IS_MARKED, result.getNick().getNameStr(), user, cmd );
                return;            
        } else if ( result.is(IS_FROZEN) ) {
                this.service.sendMsg (user, output (IS_FROZEN, result.getNick().getNameStr() ) ); 
                this.snoop.msg (false, IS_FROZEN, result.getNick().getName(), user, cmd );
                return;            
        }
        
        NickInfo ni = result.getNick();
        NSAuth auth = result.getAuth();
        NickServ.addNewFullAuth ( auth );
        NSLogEvent log;
        
        if ( auth.getType().is(MAIL) ) {
                ni.setEmail ( auth.getValue() );
                log = new NSLogEvent ( ni.getName(), AUTHMAIL, user, null );
                NickServ.addLog ( log );
                NickServ.notifyIdentifiedUsers ( ni, "A new mail has been fully authed and added to nick: "+ni.getName() );
                this.snoop.msg ( true, AUTHMAIL, ni.getName ( ), user, cmd );            
        
        } else if ( auth.getType().is(PASS) ) {
                ni.setPass ( auth.getValue() );
                log = new NSLogEvent ( ni.getName(), AUTHPASS, user, null );
                NickServ.addLog ( log );
                NickServ.notifyIdentifiedUsers ( ni, "A new password has been fully authed and added to nick: "+ni.getName() );
                NickServ.unIdentifyAllButOne ( ni );
                this.snoop.msg ( true, AUTHPASS, ni.getName ( ), user, cmd );            
        }
         
    }
 
    /**
     *
     * @param command
     * @param cmdStr
     * @param user
     * @param ni
     * @param enable
     * @param cmd
     */
    public void doSetBoolean ( HashString command, String cmdStr, User user, NickInfo ni, boolean enable, String[] cmd )  {
        this.sendIsOutput ( user, enable, cmdStr );
        ni.getSettings().set ( command, enable );
        ni.getChanges().change ( command );
        NickServ.addToWorkList ( CHANGE, ni );
        this.snoop.msg ( true, SET, ni.getName(), user, cmd );        
    }

    /**
     *
     * @param command
     * @param user
     * @param cmd
     */
    public void doSetString ( HashString command, User user, String[] cmd ) {
        /* :DreamHea1er PRIVMSG NickServ@services.sshd.biz :set command <pass> <newemail>            */
        /*      0          1               2                 3       4      5           6   = 7      */
        
        CMDResult result = this.validateCommandData (user, command, cmd );
        
        if ( cmd.length > 5 ) {
            cmd[5] = "pass_redacted";
        }
        if ( cmd.length > 6 ) {
            cmd[6] = "email_redacted";
        }
        
        if ( result.is(SYNTAX_ERROR) ) {
                if ( command.is(SETEMAIL) ) {
                    this.service.sendMsg ( user, output ( SYNTAX_ERROR, "SET EMAIL <pass> <email>" ) );
                    this.snoop.msg ( false, SETEMAIL, user.getName(), user, cmd );
                } else if ( command.is(SETPASSWD) ) {
                    this.service.sendMsg ( user, output ( SYNTAX_ERROR, "SET PASSWD <current-pass> <new-pass>" ) );
                    this.snoop.msg ( false, SETPASSWD, user.getName(), user, cmd );
                }
                return;
                
        } else if ( result.is(NICK_NOT_REGGED) ) {
                this.service.sendMsg ( user, output ( NICK_NOT_REGISTERED, cmd[4] ) );
                this.snoop.msg ( false, NICK_NOT_REGISTERED, cmd[4], user, cmd );
                return;            
        
        } else if ( result.is(IS_FROZEN) ) {
                this.service.sendMsg (user, output (IS_FROZEN, result.getNick().getNameStr() ) ); 
                this.snoop.msg (false, IS_FROZEN, result.getNick().getNameStr(), user, cmd );
                return;            
        
        } else if ( result.is(IS_MARKED) ) {
                this.service.sendMsg (user, output (IS_MARKED, result.getNick().getNameStr() ) ); 
                this.snoop.msg (false, IS_MARKED, result.getNick().getName(), user, cmd );
                return;            
        
        } else if ( result.is(IDENTIFY_FAIL) ) {
                this.service.sendMsg (user, output (PASSWD_ERROR, result.getNick().getNameStr() ) ); 
                this.snoop.msg (false, PASSWD_ERROR, result.getNick().getName(), user, cmd );
                return;            
        
        } else if ( result.is(INVALID_EMAIL) ) {
                this.service.sendMsg (user, output (INVALID_EMAIL, result.getString1 ( ) )  );
                this.snoop.msg (false, INVALID_EMAIL, result.getString1(), user, cmd );
                return;            
        
        } else if ( result.is(INVALID_PASS) ) {
                this.service.sendMsg ( user, output ( INVALID_PASS, "" )  );
                this.snoop.msg ( false, INVALID_PASS, user.getName(), user, cmd );
                return;            
        }
        
        NickInfo ni = result.getNick ( );
        String value = result.getString1 ( );
        NSAuth auth;
        NSLogEvent log;
        
        if ( command.is(SETEMAIL) ) {
                auth = new NSAuth ( MAIL, ni.getName(), value );
                NickServ.addNewAuth ( auth );
                ni.getChanges().hasChanged ( MAIL );
                NickServ.addToWorkList ( CHANGE, ni );
                log = new NSLogEvent ( ni.getName(), MAIL, user.getFullMask(), null );
                NickServ.addLog ( log );
                this.service.sendMsg ( user, "New mail has been set. A verification mail will shortly be sent, please follow the instruction in that mail." );
                this.snoop.msg ( true, SETEMAIL, ni.getName(), user, cmd );              
        
        } else if ( command.is(SETPASSWD) ) {
                auth = new NSAuth ( PASS, ni.getName(), value );
                NickServ.addNewAuth ( auth );
                ni.getChanges().hasChanged ( PASS );
                NickServ.addToWorkList ( CHANGE, ni );
                log = new NSLogEvent ( ni.getName(), PASS, user.getFullMask(), null );
                NickServ.addLog ( log );
                this.service.sendMsg ( user, "New password has been set. A verification mail will shortly be sent, please follow the instruction in that mail." );
                this.snoop.msg ( true, SETPASSWD, ni.getName(), user, cmd );             
        }
         
    }

    /**
     *
     * @param user
     * @param enable
     * @param str
     */
    public void sendIsOutput ( User user, boolean enable, String str )  {
        if ( enable ) { 
            this.service.sendMsg ( user, output ( NICK_IS_NOW, str ) );
            
        } else {
            this.service.sendMsg ( user, output ( NICK_IS_NOT, str ) );
        }
    }
       
    /**
     *
     * @param online
     * @param user
     * @param ni
     * @param str
     */
    public void showStart ( boolean online, User user, NickInfo ni, String str )  {
        if ( Handler.findUser ( ni.getString ( NAME ) ) == null )  {
            this.service.sendMsg ( user, "*** "+str+ni.getString ( NickInfo.NAME ) + ( online?" [Offline]":"" ) +" ***" );
        } else {
            this.service.sendMsg ( user, "*** "+str+ni.getString ( NickInfo.NAME ) + ( online?" [Online]":"" ) +" ***" );
        }
    }
   
    /**
     *
     * @param user
     * @param str
     */
    public void showEnd ( User user, String str ) { 
        this.service.sendMsg ( user, "*** End of "+str+" ***" );
    }

    /**
     *
     * @param email
     * @return
     */
    public static boolean validEmail ( String email )  {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher ( email );
        return matcher.find ( );
    }

    /* EXTERNAL COMMANDS */

    /**
     *
     * @param ni
     * @param command
     * @return
     */

    public boolean authMail ( NickInfo ni, Command command )  { 
        ArrayList<User> uList;
        //ni.getSettings().set ( AUTH, true );
        //ni.getChanges().hasChanged ( AUTH );
        //NickServ.addToWorkList ( CHANGE, ni );
        
        if ( NSDatabase.authMail ( ni, command ) ) {
            if ( ( uList = Handler.findUsersByNick ( ni ) ) != null ) {
                for ( User u : uList )  {
                    this.service.sendMsg ( u, output ( NICK_AUTHED, ni.getNameStr() ) );
                }
            }
            ni.setEmail ( NSDatabase.getMailByNick ( ni.getNameStr() ) );
            NSLogEvent log = new NSLogEvent ( ni.getName(), AUTHMAIL, "web!web@"+command.getExtra2 ( ), null );
            NickServ.addLog ( log );
            return true;
        }
        return false;
    }

    /**
     *
     * @param ni
     * @param command
     * @return
     */
    public boolean authPass ( NickInfo ni, Command command )  { 
        ArrayList<User> uList;
        //ni.getSettings().set ( AUTH, true );
        //ni.getChanges().hasChanged ( AUTH );
        //NickServ.addToWorkList ( CHANGE, ni );
        
        if ( NSDatabase.authPass ( ni, command ) ) {
            if ( ( uList = Handler.findUsersByNick ( ni ) ) != null ) {
                for ( User user : uList )  {
                    this.service.sendMsg (user, output ( PASS_AUTHED, ni.getNameStr() ) );
                    this.service.sendMsg (user, output ( PASS_AUTHED_UNIDENT, ni.getNameStr() ) );
                    user.unIdentify ( ni );
                    if ( user.is(ni) ) {
                        NickServ.fixIdentState (user );
                    }
                }
            }
            ni.setPass ( NSDatabase.getPassByNick ( ni.getNameStr() ) );
            NSLogEvent log = new NSLogEvent ( ni.getName(), AUTHPASS, "web!web@"+command.getExtra2 ( ), null );
            NickServ.addLog ( log );
            return true;
        }
        return false;
    }
 
    private boolean isGuestNick ( User user ) {
        return user.getString(NAME).toUpperCase().startsWith ( "GUEST" );
    }
 
    private CMDResult validateCommandData ( User user, HashString command, String[] cmd ) {
        NickInfo ni;
        String nick = new String ( );
        String pass = new String ( ); 
        CMDResult result = new CMDResult ( );
        User target;
        int minlen = 4;
        boolean checkNick   = false;
        boolean checkPass   = false;
        boolean needAccess  = false;
        
        if ( command.is(REGISTER) ) {
                if ( isShorterThanLen ( 6, cmd )  )  {
                    result.setStatus ( SYNTAX_ERROR );
                } else if ( ! validEmail ( cmd[5] )  )  {
                    result.setString1 ( cmd[5] );
                    result.setStatus ( INVALID_EMAIL );
                } else if ( ( ni = NickServ.findNick ( user.getName() ) ) != null ) {
                    result.setNick ( ni );
                    result.setStatus ( NICK_ALREADY_REGGED );
                } else if ( this.isGuestNick ( user ) ) {
                    result.setString1 ( user.getString ( NAME ) );
                    result.setStatus ( INVALID_NICK );
                }
        
        } else if ( command.is(IDENTIFY_NICK) ) {
                if ( isShorterThanLen ( 6, cmd ) ) {
                    result.setStatus ( SYNTAX_ERROR );
                } else if ( ( ni = NickServ.findNick ( cmd[4] ) ) == null ) {
                    result.setString1 ( cmd[4] );
                    result.setStatus ( NICK_NOT_REGGED );
                } else if ( ni.isSet ( FROZEN ) ) {
                    result.setNick ( ni );
                    result.setStatus ( IS_FROZEN ); 
                } else if ( ni.getThrottle().isThrottled() ) {
                    result.setNick ( ni );
                    result.setStatus ( IS_THROTTLED );
                 } else if ( ! ni.identify ( user, cmd[5] ) ) {
                    result.setNick ( ni );
                    result.setStatus ( IDENTIFY_FAIL );
                } else {
                    result.setNick ( ni );
                }
        
        } else if ( command.is(DROP) ||
                    command.is(IDENTIFY) ) {
                if ( isShorterThanLen ( 5, cmd ) ) {
                    result.setStatus ( SYNTAX_ERROR );
                } else if ( ( ni = NickServ.findNick ( user.getString ( NAME ) ) ) == null ) {
                    result.setString1 ( user.getString ( NAME ) );
                    result.setStatus ( NICK_NOT_REGGED );
                } else if ( ni.isSet ( FROZEN ) ) {
                    result.setNick ( ni );
                    result.setStatus ( IS_FROZEN );
                } else if ( ! ni.is(user) && ni.getThrottle().isThrottled() ) {
                    result.setNick ( ni );
                    result.setStatus ( IS_THROTTLED );
                } else if ( ! ni.identify ( user, cmd[4] ) ) {
                    result.setNick ( ni );
                    result.setStatus ( IDENTIFY_FAIL ); 
                } else {
                    result.setNick ( ni );
                }
        
        } else if ( command.is(AUTH) ) {
                /* :DreamHea1er PRIVMSG NickServ@services.sshd.biz :auth <authcode>          */
                /*      0          1               2                 3     4        = 5      */
                NSAuth auth;
                
                if ( isShorterThanLen ( 5, cmd ) ) {
                    result.setStatus ( SYNTAX_ERROR );
                } else if ( ( ni = NickServ.findNick ( user.getName() ) ) == null ) {
                    result.setString1 ( user.getString ( NAME ) );
                    result.setStatus ( NICK_NOT_REGGED );
                } else if ( ni.isSet ( FROZEN ) ) {
                    result.setNick ( ni );
                    result.setStatus ( IS_FROZEN ); 
                } else if ( ni.isSet ( MARKED ) ) {
                    result.setNick ( ni );
                    result.setStatus ( IS_MARKED ); 
                } else if ( ( auth = NSDatabase.fetchAuth ( user, cmd[4] ) ) == null ) {
                    result.setStatus ( NO_AUTH_FOUND );
                } else {
                    result.setAuth ( auth );
                    result.setNick ( ni );
                }   
        
        } else if ( command.is(GHOST) ) {
                if ( isShorterThanLen ( 6, cmd )  )  {
                    result.setStatus ( SYNTAX_ERROR );
                } else if ( ( ni = NickServ.findNick ( cmd[4] ) ) == null ) {
                    result.setStatus ( NICK_NOT_REGGED );
                } else if ( ( target = Handler.findUser ( cmd[4] ) ) == null ) {
                    result.setString1 ( cmd[4] );
                    result.setStatus ( NO_SUCH_NICK );    
                } else if ( ni.isSet ( FROZEN ) ) {
                    result.setNick ( ni );
                    result.setStatus ( IS_FROZEN );
                } else if ( ni.isSet ( NOGHOST ) ) {
                    result.setNick ( ni );
                    result.setStatus ( IS_NOGHOST ); 
                } else if ( ! ni.identify ( user, cmd[5] )  )  {
                    result.setStatus ( IDENTIFY_FAIL );
                } else {
                    result.setNick ( ni );
                }            
        
        } else if ( command.is(INFO) ) {
                if ( isShorterThanLen ( 4, cmd )  )  {
                    result.setStatus ( SYNTAX_ERROR );
                } else if ( ( ni = NickServ.findNick ( cmd[4] ) ) == null ) {
                    result.setStatus ( NICK_NOT_REGGED );
                } else {
                    result.setNick ( ni );
                }
                
        } else if ( command.is(SET) ) {
                needAccess = true; 
                if ( isShorterThanLen ( 6, cmd )  )  {
                    result.setStatus ( SYNTAX_ERROR );
                }
                
        } else if ( command.is(LIST) ) {
                if ( isShorterThanLen ( 5, cmd ) ) {
                    result.setStatus ( SYNTAX_ERROR );
                } else if ( ! user.isAtleast ( IRCOP ) ) {
                    result.setStatus ( ACCESS_DENIED_OPER );
                } else if ( ! user.isAtleast ( SA ) ) {
                    result.setStatus ( ACCESS_DENIED_SA );
                }
                
        } else if ( command.is(NOGHOST) ||
                    command.is(MARK) ||
                    command.is(FREEZE) ||
                    command.is(HOLD) ) {
                /* :DreamHea1er PRIVMSG NickServ@services.sshd.biz :mark [-]nick            */
                /*      0          1               2                 3         4   = 5      */
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
                if ( isShorterThanLen ( 5, cmd ) ) {
                    result.setStatus ( SYNTAX_ERROR );
                } else if ( ! NickServ.enoughAccess ( user, command ) ) {
                    result.setStatus ( ACCESS_DENIED );
                } else if ( ( ni = NickServ.findNick ( name ) ) == null ) {
                    result.setString1 ( name );
                    result.setStatus ( NICK_NOT_REGISTERED );
                } else if ( ni.isSet(MARK) && ( !command.is(MARK) || command.is(MARK) && ! remove ) ) {
                    result.setNick ( ni );
                    result.setStatus ( IS_MARKED );
                } else if ( ( ni.isSet(command) && ! remove ) || ( ! ni.isSet(command) && remove ) ) {
                    result.setNick ( ni );
                    String buf = remove ? "Un" : "";
                    result.setString1 ( buf+this.getCommandStr ( command ) );
                    result.setStatus ( NICKFLAG_EXIST );
                } else {
                    result.setNick ( ni );
                    if ( remove ) {
                        result.setCommand ( this.getAntiCommand ( command ) );
                    }
                }
                
        } else if ( command.is(GETEMAIL) ||
                    command.is(GETPASS) ) {
                result.setCommand ( command );
                if ( isShorterThanLen ( 5, cmd ) ) {
                    result.setStatus ( SYNTAX_ERROR );
                } else if ( ! NickServ.enoughAccess ( user, command ) ) {
                    result.setStatus ( ACCESS_DENIED );
                } else if ( ( ni = NickServ.findNick ( cmd[4] ) ) == null ) {
                    result.setString1 ( cmd[4] );
                    result.setStatus ( NICK_NOT_REGISTERED );
                } else if ( ni.isSet ( MARK ) ) {
                    result.setNick ( ni );
                    result.setStatus ( IS_MARKED );
                } else {
                    result.setNick ( ni );
                }            
            
        } else if ( command.is(SETEMAIL) ) {
                if ( isShorterThanLen ( 7, cmd ) ) {
                    result.setStatus ( SYNTAX_ERROR );
                } else if ( ( ni = NickServ.findNick ( user.getString(NAME) ) ) == null ) {
                    result.setString1 ( user.getString(NAME) );
                    result.setStatus ( NICK_NOT_REGISTERED );
                } else if ( ni.isSet ( FROZEN ) ) {
                    result.setNick ( ni );
                    result.setStatus ( IS_FROZEN );
                } else if ( ni.isSet ( MARK ) ) {
                    result.setNick ( ni );
                    result.setStatus ( IS_MARKED );
                } else if ( ! ni.identify ( user, cmd[5] )  )  {
                    result.setNick ( ni );
                    result.setStatus ( IDENTIFY_FAIL );
                } else if ( ! validEmail ( cmd[6] )  )  {
                    result.setString1 ( cmd[6] );
                    result.setStatus ( INVALID_EMAIL );
                } else {
                    result.setString1 ( cmd[6] );
                    result.setNick ( ni );
                }
                
        } else if ( command.is(SETPASSWD) ) {
                if ( isShorterThanLen ( 7, cmd ) ) {
                    result.setStatus ( SYNTAX_ERROR );
                } else if ( ( ni = NickServ.findNick ( user.getString(NAME) ) ) == null ) {
                    result.setString1 ( user.getString(NAME) );
                    result.setStatus ( NICK_NOT_REGISTERED );
                } else if ( ni.isSet ( FROZEN ) ) {
                    result.setNick ( ni );
                    result.setStatus ( IS_FROZEN );
                } else if ( ni.isSet ( MARK ) ) {
                    result.setNick ( ni );
                    result.setStatus ( IS_MARKED );
                } else if ( ! ni.identify ( user, cmd[5] )  )  {
                    result.setNick ( ni );
                    result.setStatus ( IDENTIFY_FAIL );
                } else if ( cmd[6].length() < 8 ) {
                    result.setStatus ( INVALID_PASS );
                } else {
                    result.setString1 ( cmd[6] );
                    result.setNick ( ni );
                }
                
        } else if ( command.is(DELETE) ) {
                 result.setCommand ( command );
                if ( isShorterThanLen ( 5, cmd ) ) {
                    result.setStatus ( SYNTAX_ERROR );
                } else if ( ! NickServ.enoughAccess ( user, command ) ) {
                    result.setStatus ( ACCESS_DENIED );
                } else if ( ( ni = NickServ.findNick ( cmd[4] ) ) == null ) {
                    result.setString1 ( cmd[4] );
                    result.setStatus ( NICK_NOT_REGISTERED );
                } else if ( ni.isSet ( MARK ) ) {
                    result.setNick ( ni );
                    result.setStatus ( IS_MARKED );
                } else {
                    Proc.log("Setting nick to: "+ni.getNameStr());
                    result.setNick ( ni );
                }
                
        }
        return result;
    }
    
    private HashString getAntiCommand ( HashString command ) {
        if      ( command.is(MARK) )        { return UNMARK;        } 
        else if ( command.is(FREEZE) )      { return UNFREEZE;      } 
        else if ( command.is(CLOSE) )       { return REOPEN;        } 
        else if ( command.is(HOLD) )        { return UNHOLD;        } 
        else if ( command.is(NOGHOST) )     { return UNNOGHOST;     } 
        else {
            return null;
        }
    }
    private String getCommandStr ( HashString command ) {
        if ( command.is(MARK) )             { return "Marked";      }
        else if ( command.is(FREEZE) )      { return "Frozen";      } 
        else if ( command.is(CLOSE) )       { return "Closed";      } 
        else if ( command.is(HOLD) )        { return "Held";        } 
        else if ( command.is(NOGHOST) )     { return "NoGhost";     } 
        else {
            return "";
        }
       
    }
    
    /**
     *
     * @param code
     * @param args
     * @return
     */
    public String output ( HashString code, String... args )  {
        
        if ( code.is(SYNTAX_ERROR) ) {
            return "Syntax: /NickServ "+args[0]+"";
        
        } else if ( code.is(SYNTAX_ID_ERROR) ) {
            return "Syntax: /NickServ IDENTIFY <nickname> <password>";
        
        } else if ( code.is(SYNTAX_REG_ERROR) ) {
            return "Syntax: /NickServ REGISTER <password> <email>";
        
        } else if ( code.is(SYNTAX_GHOST_ERROR) ) {
            return "Syntax: /NickServ GHOST <nickname> <password>";
        
        } else if ( code.is(NO_SUCH_NICK) ) {
            return "Error: user "+args[0]+" is not online";
        
        } else if ( code.is(CMD_NOT_FOUND_ERROR) ) {
            return "Syntax Error! For information regarding commands please issue:";
        
        } else if ( code.is(SHOW_HELP) ) {
            return "    /"+args[0]+" HELP";
        
        } else if ( code.is(PASSWD_ERROR) ) {
            return "Error: Wrong password for nick: "+args[0];
        
        } else if ( code.is(INVALID_EMAIL) ) {
            return "Error: "+args[0]+" is not a valid email-adress";
        
        } else if ( code.is(INVALID_PASS) ) {
            return "Error: password is not valid, it might be too short or too easy.";
        
        } else if ( code.is(INVALID_NICK) ) {
            return "Error: "+args[0]+" is not a valid nick for registration";
        
        } else if ( code.is(ACCESS_DENIED) ) {
            return "Access denied. Please identify to "+args[0]+" before proceeding.";
            
        } else if ( code.is(SETTING_NOT_FOUND) ) {
            return "Error: Setting "+args[0]+" not found.";
        
        } else if ( code.is(NICK_NOT_REGISTERED) ) {
            return "Error: nick "+args[0]+" is not registered";
        
        } else if ( code.is(NICK_ALREADY_REGGED) ) {
            return "Error: nick "+f.b ( ) +args[0]+f.b ( ) +" is already registered.";
        
        } else if ( code.is(NICK_IS_NOW) ) {
            return "Your nick now has option "+args[0]+" set.";
        
        } else if ( code.is(NICK_IS_NOT) ) {
            return "Your nick has been stripped from option "+args[0]+".";
        
        } else if ( code.is(PASSWD_ACCEPTED) ) {
            return "Password accepted for nick: "+args[0]+". You are now identified.";
        
        } else if ( code.is(DB_ERROR) ) {
            return "Error: Database error. Please try again in a few minutes.";
        
        } else if ( code.is(REGISTER_DONE) ) {
            return "Nick "+args[0]+" was successfully registered to you. Please remember your password. Please check your mail for further instructions.";
        
        } else if ( code.is(REGISTER_SEC) ) {
            return "IMPORTANT: Never share your passwords, not even with network staff.";
        
        } else if ( code.is(NICK_AUTHED) ) {
            return "The email for "+args[0]+" is now fully set and authorized.";
        
        } else if ( code.is(PASS_AUTHED) ) {
            return "The password for "+args[0]+" is now fully set and authorized.";
        
        } else if ( code.is(PASS_AUTHED_UNIDENT) ) {
            return "You have been unindentified from nick "+args[0]+" as a new password has been set.";
        
        } else if ( code.is(NO_AUTH_FOUND) ) {
            return "The authcode you provided did not match any pending objects.";
        
        } else if ( code.is(NICK_NEW_MASK) ) {
            return "Last login from "+args[0]+".";
        
        } else if ( code.is(IDENT_NICK_DELETED) ) {
            return "You have been unindentified from nick "+args[0]+" as it was deleted from services.";
        
        } else if ( code.is(NICK_DELETED) ) {
            return "Nickname "+args[0]+" has been deleted from services.";
        
        } else if ( code.is(ACCESS_DENIED_SRA) ) {
            return "Access denied. You need to be atleast SRA to use this command.";
        
        } else if ( code.is(ACCESS_DENIED_SA) ) {
            return "Access denied. You need to be atleast SA to use this command.";
        
        } else if ( code.is(ACCESS_DENIED_OPER) ) {
            return "No such command.";
        
        } else if ( code.is(ACCESS_DENIED_DELETE_OPER) ) {
            return "Access denied. You cannot delete a SA+.";
        
        } else if ( code.is(NICK_SET_FLAG) ) {
            return "Nick "+args[0]+" is now "+args[1]+".";
        
        } else if ( code.is(NICKFLAG_EXIST) ) {
            return "Nick "+args[0]+" is already "+args[1]+".";
        
        } else if ( code.is(NICK_GETPASS) ) {
            return "Password is: "+args[0]+".";
        
        } else if ( code.is(NICK_GETEMAIL) ) {
            return "["+args[0]+"] "+args[1]+" "+args[1]+" "+args[2]+"";
        
        } else if ( code.is(IS_MARKED) ) {
            return "Error: Nick "+args[0]+" is MARKed by a network staff blocking certain functionality.";
        
        } else if ( code.is(IS_FROZEN) ) {
            return "Error: Nick "+args[0]+" is frozen by a network staff and cannot be used.";
        
        } else if ( code.is(IS_NOGHOST) ) {
            return "Error: Nick "+args[0]+" is set noghost by a network staff and cannot be ghosted.";
        
        } else if ( code.is(IS_THROTTLED) ) {
            return "Error: Throttled login attempts.";
        
        } else if ( code.is(GLOB_IS_NOGHOST) ) {
            return args[0]+" tried using GHOST on NOGHOST nick "+args[1]+".";
        
        } else if ( code.is(NICKDROPPED) ) {
            return "Nick: "+args[0]+" was successfully dropped.";
        
        } else if ( code.is(NICKDELETED) ) {
            return "Nick: "+args[0]+" was successfully deleted.";
            
        } else {
            return "";
        }
         
    }
 } 