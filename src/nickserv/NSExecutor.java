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
package nickserv;

import command.Command;
import core.Database;
import core.Executor;
import core.Handler;
import core.Proc;
import core.TextFormat;
import static core.HashNumeric.SA;
import static core.HashNumeric.SRA;
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
    
    public NSExecutor ( NickServ service, NSSnoop snoop )  {
        super ( );
        this.service        = service;
        this.snoop          = snoop;
        this.f              = new TextFormat ( );
    }

    public void parse ( User user, String[] cmd ) {
        if ( cmd == null || cmd[3].isEmpty ( ) ) {
            this.help ( user );
            return; 
        }
        
        switch ( cmd[3].toUpperCase().hashCode ( ) ) {
            case REGISTER :
                this.register ( user, cmd );
                break;
                
            case IDENTIFY :
                this.identify ( user, cmd );
                break;
                
             case AUTH :
                this.auth ( user, cmd );
                break;
                
            case DROP :
                this.drop ( user, cmd );
                break;
                
            case SIDENTIFY :
                this.sIdentify ( user, cmd );
                break;
                
            case GHOST :
                this.ghost ( user, cmd );
                break;
                
            case SET :
                this.set ( user, cmd );
                break;
                
            case INFO :
                this.info ( user, cmd );
                break;
                
            /* Oper */
            case MARK :
                this.changeFlag ( MARK, user, cmd );
                break;
                 
            case FREEZE :
                this.changeFlag ( FREEZE, user, cmd );
                break;
                
            case HOLD :
                this.changeFlag ( HOLD, user, cmd );
                break;
                
            case NOGHOST :
                this.changeFlag ( NOGHOST, user, cmd );
                break;
                
            case GETPASS :
                this.getPass ( user, cmd );
                break;
                          
            case GETEMAIL :
                this.getEmail ( user, cmd );
                break;
                
            case DELETE :
                this.delete ( user, cmd );
                break;
                

            default :
                this.help ( user );
        
        }  
    }
 
    public void help ( User user )  {
        this.service.sendMsg ( user, output ( CMD_NOT_FOUND_ERROR, "" )  );
        this.service.sendMsg ( user, output ( SHOW_HELP, new String[] {this.service.getName ( ) } )  );
    }

    public void register ( User user, String[] cmd )  { /* DONE? */
        // :DreamHea1er PRIVMSG NickServ@services.sshd.biz :register pass value
        //       0         1              2                     3      4    5 = 6

        CmdData cmdData = this.validateCommandData ( user, REGISTER, cmd );
 
        switch ( cmdData.getStatus ( )  )  {
            case SYNTAX_ERROR :
                this.service.sendMsg ( user, output ( SYNTAX_REG_ERROR, "" )  );
                return;
                               
            case INVALID_EMAIL :
                this.service.sendMsg ( user, output ( INVALID_EMAIL, cmdData.getString1() )  );
                return; 

            case NICK_ALREADY_REGGED :
                this.service.sendMsg ( user, output ( NICK_ALREADY_REGGED, user.getString ( NAME )  )  );
                return;  

            case INVALID_NICK :
                this.service.sendMsg ( user, output ( INVALID_NICK, user.getString ( NAME )  )  );
                return;  

            default :
                
        }
        NickInfo ni = new NickInfo ( user, cmd[4], cmd[5] );
        NickServ.addToWorkList ( REGISTER, ni );
        NickServ.addNick ( ni );
        SendMail.sendNickRegisterMail ( ni );
        user.getSID().add ( ni );
        NSLogEvent log = new NSLogEvent ( ni.getName(), REGISTER, user.getFullMask(), null );
        NickServ.addLog ( log );
        NickServ.fixIdentState ( user ); 
        this.service.sendMsg ( user, output ( REGISTER_DONE, ni.getString ( NAME ) ) );
        this.service.sendMsg ( user, f.b ( ) +output ( REGISTER_SEC, "" ) +f.b ( ) );
    }

    public void identify ( User user, String[] cmd )  {
        // :DreamHea1er PRIVMSG NickServ@services.sshd.biz :identify moew               = 5
        // :DreamHea1er PRIVMSG NickServ@services.sshd.biz :identify dreamhealer moew   = 6
        //       0         1               2                    3      4           5
 
        CmdData cmdData;
        
        if ( cmd.length > 5 ) {        
            cmdData = this.validateCommandData ( user, IDENTIFY_NICK, cmd );
        } else {
            cmdData = this.validateCommandData ( user, IDENTIFY, cmd );
        }
        
        System.out.println(":0");
        switch ( cmdData.getStatus ( )  )  {
            case SYNTAX_ERROR :
                this.service.sendMsg ( user, output ( SYNTAX_ID_ERROR, "" )  );
                return;
                
            case NICK_NOT_REGGED :
                this.service.sendMsg ( user, output ( NICK_NOT_REGISTERED, cmdData.getString1 ( )  )  );
                return;
                
            case IDENTIFY_FAIL :
                this.service.sendMsg ( user, output ( PASSWD_ERROR, cmdData.getNick ( ) .getName ( )  )  ); 
                return;

            case IS_FROZEN : 
                this.service.sendMsg ( user, output ( IS_FROZEN, cmdData.getString1 ( ) ) );
                return;
                             
            default :
                
        }
        NickInfo ni = cmdData.getNick ( );
        System.out.println("DEBUG: "+ni.getHashMask ( )+":"+user.getHashMask ( ));
        if ( ni.getHashMask ( ) != user.getHashMask ( ) ) {
            this.service.sendMsg ( user, output ( NICK_NEW_MASK, ni.getString ( FULLMASK ) ) );
            ni.setUserMask ( user );        
        }  
        this.service.sendMsg ( user, output ( PASSWD_ACCEPTED, ni.getString ( NAME ) ) );
        ni.setUserMask ( user );           /* Set user mask */
        user.getSID().add ( ni );          /* Add nick to user sid */
        NickServ.fixIdentState ( user );
        ni.getNickExp().reset ( );
        ni.getChanges().hasChanged ( LASTSEEN );
        NickServ.addToWorkList ( CHANGE, ni );
        Database.updateServicesID ( user.getSID() );
    }
 
    private void drop ( User user, String[] cmd ) {
        // :DreamHea1er PRIVMSG NickServ@services.sshd.biz :drop <pass>   = 5
        //       0         1               2                    3    4           
        CmdData cmdData = this.validateCommandData ( user, DROP, cmd );
        
        switch ( cmdData.getStatus ( ) ) {
            case SYNTAX_ERROR :
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "DROP <pass>" ) );
                return;
                
            case NICK_NOT_REGGED :
                this.service.sendMsg ( user, output ( NICK_NOT_REGISTERED, cmdData.getString1 ( ) ) );
                return;
                
            case IDENTIFY_FAIL :
                this.service.sendMsg ( user, output ( PASSWD_ERROR, cmdData.getNick().getName ( ) ) ); 
                return;
                 
            case IS_MARKED :
                this.service.sendMsg ( user, output ( IS_MARKED, cmdData.getNick().getName ( ) ) ); 
                return;     

            case IS_FROZEN : 
                this.service.sendMsg ( user, output ( IS_FROZEN, cmdData.getString1 ( ) ) );
                return;
                        
            default :
                
        }
        NickInfo ni = cmdData.getNick ( );
        
        Handler.getNickServ().dropNick ( ni );
        this.service.sendMsg ( user, output ( NICKDROPPED, ni.getString ( NAME ) ) );
        NSLogEvent log = new NSLogEvent ( ni.getName(), DROP, user.getFullMask(), "" );
        NickServ.addLog ( log );
        NickServ.addToWorkList ( DELETE, ni );
    }
    
    private void delete ( User user, String[] cmd ) {
        // :DreamHea1er PRIVMSG NickServ@services.sshd.biz :drop <pass>   = 5
        //       0         1               2                    3    4           
        CmdData cmdData = this.validateCommandData ( user, DELETE, cmd );
        
        switch ( cmdData.getStatus ( )  )  {
            case SYNTAX_ERROR :
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "DELETE <nick>" )  );
                return;
                
            case NICK_NOT_REGGED :
                this.service.sendMsg ( user, output ( NICK_NOT_REGISTERED, cmdData.getString1 ( )  )  );
                return;
                
            case IDENTIFY_FAIL :
                this.service.sendMsg ( user, output ( PASSWD_ERROR, cmdData.getNick ( ) .getName ( )  )  ); 
                return;
                 
            case IS_MARKED :
                this.service.sendMsg ( user, output ( IS_MARKED, cmdData.getNick().getName ( ) ) ); 
                return;     
            
            default :
                
        }
        NickInfo ni = cmdData.getNick ( );
        Handler.getNickServ().dropNick ( ni );
        this.service.sendMsg ( user, output ( NICKDELETED, ni.getString ( NAME ) ) );
        this.service.sendGlobOp ( "Nick "+ni.getName()+" has been DELETED by "+user.getOper().getName() );
        NSLogEvent log = new NSLogEvent ( ni.getName(), DELETE, user.getFullMask(), user.getOper().getName() );
        NickServ.addLog ( log );
        
    }
   
     
    public void list ( User user, String[] cmd )  { /* DONE? */
        // :DreamHea1er PRIVMSG NickServ@services.sshd.biz :list dream*
        //      0          1            2                     3    4     = 5
    
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
        ArrayList<NickInfo> nList = NickServ.searchNicks ( cmd[4] );
        this.service.sendMsg ( user, f.b ( ) +"List:"+f.b ( ) ); 
        for ( NickInfo ni2 : nList ) {
            this.service.sendMsg ( user, f.b ( ) +"    "+ni2.getName ( ) +" - ("+ni2.getString ( FULLMASK )+")" );
        }
        this.showEnd ( user, "Info" );
         
    }
  
    public void sIdentify ( User user, String[] cmd )  {
        // :DreamHea1er PRIVMSG NickServ@services.sshd.biz :sidentify moew               = 5
        // :DreamHea1er PRIVMSG NickServ@services.sshd.biz :sidentify dreamhealer moew   = 6
        //       0         1               2                    3      4           5

        NickInfo ni;
        String nick;
        String pass;
        CmdData cmdData;
        
        if ( cmd.length == 6 ) {        
            cmdData = this.validateCommandData ( user, IDENTIFY_NICK, cmd );
        } else {
            cmdData = this.validateCommandData ( user, IDENTIFY, cmd );
        }
          
        switch ( cmdData.getStatus ( ) ) {
            case SYNTAX_ERROR :      
                this.service.sendMsg ( user, output ( SYNTAX_ID_ERROR, "" )  );
                return;
                
            case NICK_NOT_REGGED :
                this.service.sendMsg ( user, output ( NICK_NOT_REGISTERED, cmdData.getString1 ( )  )  );
                return;
            
            case IDENTIFY_FAIL :
                this.service.sendMsg ( user, output ( PASSWD_ERROR, cmdData.getNick ( ) .getName ( )  )  ); 
                return;
             
            case IS_FROZEN : 
                this.service.sendMsg ( user, output ( IS_FROZEN, cmdData.getString1 ( ) ) );
                return;
            
            default : 
        
        }
        ni = cmdData.getNick ( );
        if ( ni.getHashMask ( ) != user.getHashMask ( ) ) {
            this.service.sendMsg ( user, output ( NICK_NEW_MASK, ni.getString ( FULLMASK ) ) );
            ni.setUserMask ( user );
        } else {
            this.service.sendMsg ( user, output ( PASSWD_ACCEPTED, ni.getString ( NAME ) ) );
        }
        user.getSID().add ( ni );
        NickServ.fixIdentState ( user );
        ni.getChanges().hasChanged ( LASTSEEN );
        NickServ.addToWorkList ( CHANGE, ni );
    }

    public void ghost ( User user, String[] cmd )  {
        NickInfo ni;
        User target;
        String nick = new String ( );
        String pass = new String ( );
        
        CmdData cmdData = this.validateCommandData ( user, GHOST, cmd );
 
        if ( cmdData.getStatus ( ) != SYNTAX_ERROR )  {
            /* We know where data is */
            nick        = cmd[4];       
            pass        = cmd[5];
            cmd[5]      = "HIDDEN";
        }
        switch ( cmdData.getStatus ( )  )  {
            case SYNTAX_ERROR : 
                this.service.sendMsg ( user, output ( SYNTAX_GHOST_ERROR, "" ) );
                return;
                
            case NICK_NOT_REGGED :
                this.service.sendMsg ( user, output ( NICK_NOT_REGISTERED, nick ) );
                return;
                
            case IDENTIFY_FAIL :
                this.service.sendMsg ( user, output ( PASSWD_ERROR, nick ) );
                this.snoop.msg ( false, cmdData.getNick().getName ( ), user, cmd );
                return;
            
            case NO_SUCH_NICK : 
                this.service.sendMsg ( user, output ( NO_SUCH_NICK, cmdData.getString1 ( ) ) );
                return;
            
            case IS_FROZEN : 
                this.service.sendMsg ( user, output ( IS_FROZEN, cmdData.getString1 ( ) ) );
                return;
            
            case IS_NOGHOST : 
                this.service.sendMsg ( user, output ( IS_NOGHOST, cmdData.getString1 ( ) ) );
                this.service.sendGlobOp ( output ( GLOB_IS_NOGHOST, user.getFullMask(), cmdData.getString1 ( ) ) );
                return;
            
            default:
                
        }
        ni = cmdData.getNick ( );
        this.service.sendMsg ( user, output ( PASSWD_ACCEPTED, ni.getString ( NAME ) ) );
        ServSock.sendCmd ( ":"+Proc.getConf().get ( NAME ) +" SVSKILL "+ni.getName ( ) +" :Ghost exorcised by: "+user.getString ( NAME ) ); /* kill the ghosted nick */
        this.snoop.msg ( true, ni.getName ( ), user, cmd );
        NickServ.fixIdentState ( user );
        user.getSID().add ( ni );
        ni.getChanges().hasChanged ( LASTSEEN );
        NickServ.addToWorkList ( CHANGE, ni );
    }
    
    public void info ( User user, String[] cmd )  { /* DONE? */
        // :DreamHea1er PRIVMSG NickServ@services.sshd.biz :info dreamhealer
        //      0          1            2                     3       4     = 5
        NickInfo ni;
        CmdData cmdData = this.validateCommandData ( user, INFO, cmd );
        switch ( cmdData.getStatus ( ) ) {
            case SYNTAX_ERROR :
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "" ) );
                return;
                
            case NICK_NOT_REGGED :
                this.service.sendMsg ( user, output ( NICK_NOT_REGISTERED, cmd[4] ) );
                return;
                
            default : 
        
        }
        ni = cmdData.getNick ( );
        this.showStart ( true, user, ni, f.b ( ) +"Info for: "+f.b ( ) ); 
        if ( ni.getSettings().is ( SHOWHOST ) ) {
            this.service.sendMsg ( user, f.b ( ) +"    Hostmask: "+f.b ( ) +ni.getString ( USER )+"@"+ni.getString ( HOST ) );
        } else {
            this.service.sendMsg ( user, f.b ( ) +"    Hostmask: "+f.b ( ) +ni.getString ( USER ) +"@"+ni.getName ( ) +"."+ ( ni.getOper().getString ( ACCSTRINGSHORT ) ) +"."+Proc.getConf().get (DOMAIN ) );
        }
        this.service.sendMsg ( user, f.b ( ) +"  Registered: "+f.b ( ) +ni.getString ( REGTIME ) );
        this.service.sendMsg ( user, f.b ( ) +"    Lastseen: "+f.b ( ) +ni.getString ( LASTSEEN ) );
        this.service.sendMsg ( user, f.b ( ) +"    Time now: "+f.b ( ) +dateFormat.format ( new Date ( ) ) );
        if ( ni.getSettings().getInfoStr().length() > 0 ) {
            this.service.sendMsg ( user, f.b ( ) +"    Settings: "+f.b ( ) +ni.getSettings().getInfoStr ( ) );
        }
        /* Show that the nick hasnt been authed if it hasnt */
        
        if ( user.isAtleast ( IRCOP ) ) {
            if ( ni.is(FROZEN) || ni.is(MARKED) || ni.is(HELD) ) {
                this.service.sendMsg ( user, f.b ( ) +"   --- IRCop ---" );
            }
            if ( ni.is ( FROZEN ) ) {
                this.service.sendMsg ( user, f.b ( ) +"      Frozen: "+ni.getSettings().getInstater ( FREEZE ) );
            }
            if ( ni.is ( MARKED ) ) {
                this.service.sendMsg ( user, f.b ( ) +"      Marked: "+ni.getSettings().getInstater ( MARK ) );
            }
            if ( ni.is ( HELD ) ) {
                this.service.sendMsg ( user, f.b ( ) +"        Held: "+ni.getSettings().getInstater ( HOLD ) );
            }
            if ( ni.is ( NOGHOST ) ) {
                this.service.sendMsg ( user, f.b ( ) +"     NoGhost: "+ni.getSettings().getInstater ( NOGHOST ) );
            }
        }
        this.showEnd ( user, "Info" );
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

       if ( cmd.length < 6 )  {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "SET <option> ON/OFF" ) );

        } else if ( ( ni = NickServ.findNick ( cmd[0] ) ) == null ) {
            this.service.sendMsg ( user, output ( NICK_NOT_REGISTERED, cmd[0] ) );

        } else if ( ! user.isIdented ( ni ) ) {
            /* User not idented */
            this.service.sendMsg ( user, output ( ACCESS_DENIED, ni.getString ( NAME ) ) );

        } else {
            boolean enable  = false; 
            switch ( cmd[5].toUpperCase().hashCode ( ) ) {
                case ON :
                    enable = true;
                    break;
                    
                case OFF :
                    enable = false;
                    break;
                    
                default :
            } 
            
            switch ( cmd[4].toUpperCase().hashCode ( ) ) {
                case NOOP :
                    doSetBoolean ( NOOP, "NoOp", user, ni, enable );
                    break;
                    
                case NEVEROP :
                    doSetBoolean ( NEVEROP, "NeverOp", user, ni, enable );
                    break;
                    
                case MAILBLOCK :
                    doSetBoolean ( MAILBLOCK, "MailBlock", user, ni, enable );
                    break;
                    
                case SHOWEMAIL :
                    doSetBoolean ( SHOWEMAIL, "ShowEmail", user, ni, enable );
                    break;
                    
                case SHOWHOST :
                    doSetBoolean ( SHOWHOST, "ShowHost", user, ni, enable );
                    break;
                    
                case EMAIL :
                    doSetString ( SETEMAIL, "Email", user, cmd );
                    break;
                             
                case PASSWD :
                    doSetString ( SETPASSWD, "Passwd", user, cmd );
                    break;
                    
                default : 
                    this.service.sendMsg ( user, output ( SETTING_NOT_FOUND, cmd[4] )  );
               
            }
        }
    }
    
    private void changeFlag ( int flag, User user, String[] cmd ) {
        CmdData cmdData = this.validateCommandData ( user, flag, cmd );
        switch ( cmdData.getStatus ( ) ) {
            case SYNTAX_ERROR :
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, NickSetting.hashToStr ( flag )+" <[-]nick>" )  );
                return;
                
            case ACCESS_DENIED :
                this.service.sendMsg ( user, output ( ACCESS_DENIED, cmdData.getString1 ( ) ) ); 
                return;     
                           
            case NICK_NOT_REGGED :
                this.service.sendMsg ( user, output ( NICK_NOT_REGISTERED, cmd[4] ) );
                return;
                
            case NICKFLAG_EXIST :
                this.service.sendMsg ( user, output ( NICKFLAG_EXIST, cmdData.getNick().getName(), cmdData.getString1 ( ) ) ); 
                return;     
                      
            case IS_MARKED :
                this.service.sendMsg ( user, output ( IS_MARKED, cmdData.getNick().getName ( ) ) ); 
                return;     
            
            default : 
                
        }
        
        NickInfo ni = cmdData.getNick ( );
        NickInfo oper = user.getOper().getNick ( );
        int command = cmdData.getCommand ( );
        NSLogEvent log;
        
        switch ( command ) {
            case UNNOGHOST :
            case UNMARK :
            case UNFREEZE :
            case UNHOLD :
                NickInfo instater = NickServ.findNick ( ni.getSettings().getInstater ( flag ) );
                if ( ! user.isIdented ( instater ) && ! user.isAtleast ( SRA ) ) {
                    this.service.sendMsg ( user, "Error: flag can only be removed by: "+instater.getName()+" or a SRA+." );
                    return;
                }
                ni.getSettings().set ( flag, "" );
                ni.getChanges().change ( flag );
                NickServ.addToWorkList ( CHANGE, ni );
                log = new NSLogEvent ( ni.getName(), command, user.getFullMask(), oper.getName() );
                NickServ.addLog ( log );
                this.service.sendMsg ( user, output ( NICK_SET_FLAG, ni.getName(), "Un"+ni.getSettings().modeString ( flag ) ) );
                this.service.sendGlobOp ( "Nick "+ni.getName()+" has been Un"+ni.getSettings().modeString(flag)+" by "+oper.getName() );
                break;
                
            case NOGHOST :
            case MARK :
            case FREEZE :
            case HOLD :
                ni.getSettings().set ( flag, oper.getName() );
                ni.getChanges().change ( flag ); 
                NickServ.addToWorkList ( CHANGE, ni );
                log = new NSLogEvent ( ni.getName(), command, user.getFullMask(), oper.getName() );
                NickServ.addLog ( log );
                this.service.sendMsg ( user, output ( NICK_SET_FLAG, ni.getName(), ni.getSettings().modeString ( flag ) )  );
                this.service.sendGlobOp ( "Nick "+ni.getName()+" has been "+ni.getSettings().modeString(flag)+" by "+oper.getName() );
                break;
                
            default :  
                
        } 
    }
    
    /* Can possibly merge getPass and getEmail methods in the future */
    private void getPass ( User user, String[] cmd ) {
        CmdData cmdData = this.validateCommandData ( user, GETPASS, cmd );
        switch ( cmdData.getStatus ( ) ) {
            case SYNTAX_ERROR :
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "" )  );
                return;
                
            case ACCESS_DENIED :
                this.service.sendMsg ( user, output ( ACCESS_DENIED, cmdData.getString1 ( ) ) ); 
                return;     
                           
            case NICK_NOT_REGGED :
                this.service.sendMsg ( user, output ( NICK_NOT_REGISTERED, cmd[4] ) );
                return;
                    
            case IS_MARKED :
                this.service.sendMsg ( user, output ( IS_MARKED, cmdData.getNick().getName ( ) ) ); 
                return;     
            
            default : 
        
        }
        NickInfo ni = cmdData.getNick ( ); 
        NickInfo oper = user.getOper().getNick ( );
        int command = cmdData.getCommand ( );
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
        NSLogEvent log = new NSLogEvent ( ni.getName(), command, user.getFullMask(), oper.getName() );
        NickServ.addLog ( log );
        this.service.sendGlobOp ( oper.getName()+" used GETPASS on: "+ni.getName() );
    }
 
    private void getEmail ( User user, String[] cmd ) {
        CmdData cmdData = this.validateCommandData ( user, GETEMAIL, cmd );
        switch ( cmdData.getStatus ( ) ) {
            case SYNTAX_ERROR :
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "" )  );
                return;
                
            case ACCESS_DENIED :
                this.service.sendMsg ( user, output ( ACCESS_DENIED, cmdData.getString1 ( ) ) ); 
                return;     
                           
            case NICK_NOT_REGGED :
                this.service.sendMsg ( user, output ( NICK_NOT_REGISTERED, cmd[4] ) );
                return;
                     
            case IS_MARKED :
                this.service.sendMsg ( user, output ( IS_MARKED, cmdData.getNick().getName ( ) ) ); 
                return;     
            
            default : 
        
        }
        NickInfo ni = cmdData.getNick ( ); 
        NickInfo oper = user.getOper().getNick ( );
        int command = cmdData.getCommand ( );
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
        NSLogEvent log = new NSLogEvent ( ni.getName(), command, user.getFullMask(), oper.getName() );
        NickServ.addLog ( log );
        this.service.sendGlobOp ( oper.getName()+" used GETEMAIL on: "+ni.getName() );
    }
 
    
    
    private void auth ( User user, String[] cmd ) {
        CmdData cmdData = this.validateCommandData ( user, AUTH, cmd );
        switch ( cmdData.getStatus ( ) ) {
            case SYNTAX_ERROR :
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "" )  );
                return;
                
            case NICK_NOT_REGGED :
                this.service.sendMsg ( user, output ( NICK_NOT_REGISTERED, cmd[4] ) );
                return;
                     
            case NO_AUTH_FOUND :
                this.service.sendMsg ( user, output ( NO_AUTH_FOUND, "" ) );
                return;
                     
            case IS_MARKED :
                this.service.sendMsg ( user, output ( IS_MARKED, cmdData.getNick().getName ( ) ) ); 
                return;     
            
            case IS_FROZEN :
                this.service.sendMsg ( user, output ( IS_FROZEN, cmdData.getNick().getName ( ) ) ); 
                return;
            
            default : 
        
        }
        
        NickInfo ni = cmdData.getNick();
        NSAuth auth = cmdData.getAuth();
        NickServ.addNewFullAuth ( auth );
        NSLogEvent log;
        switch ( auth.getType() ) {
            case MAIL :
                ni.setEmail ( auth.getValue() );
                log = new NSLogEvent ( ni.getName(), AUTHMAIL, user, null );
                NickServ.addLog ( log );
                NickServ.notifyIdentifiedUsers ( ni, "A new mail has been fully authed and added to nick: "+ni.getName() );
                break;
            case PASS :
                ni.setPass ( auth.getValue() );
                log = new NSLogEvent ( ni.getName(), AUTHPASS, user, null );
                NickServ.addLog ( log );
                NickServ.notifyIdentifiedUsers ( ni, "A new password has been fully authed and added to nick: "+ni.getName() );
                NickServ.unIdentifyAllButOne ( ni );
                break;
                
            default :
                
        }
    }

    
    public void doSetBoolean ( int cmd, String command, User user, NickInfo ni, boolean enable )  {
        this.sendIsOutput ( user, enable, command );
        ni.getSettings().set ( cmd, enable );
        System.out.println("DEBUG: changed item: "+cmd);
        ni.getChanges().change ( cmd );
        NickServ.addToWorkList ( CHANGE, ni );
    }

    public void doSetString ( int hash, String command, User user, String[] cmd ) {
        /* :DreamHea1er PRIVMSG NickServ@services.sshd.biz :set value <pass> <newemail>          */
        /*      0          1               2                 3     4      5           6 = 7      */
        
        CmdData cmdData = this.validateCommandData ( user, hash, cmd );

        switch ( cmdData.getStatus ( ) ) {
            case SYNTAX_ERROR :
                switch ( hash ) {
                    case SETEMAIL :
                        this.service.sendMsg ( user, output ( SYNTAX_ERROR, "SET EMAIL <pass> <email>" ) );
                        break;
                        
                    case SETPASSWD :
                        this.service.sendMsg ( user, output ( SYNTAX_ERROR, "SET PASSWD <current-pass> <new-pass>" ) );
                        break;
                        
                    default :

                }
                return;
                
            case NICK_NOT_REGGED :
                this.service.sendMsg ( user, output ( NICK_NOT_REGISTERED, cmd[4] ) );
                return;
                
            case IS_FROZEN :
                this.service.sendMsg ( user, output ( IS_FROZEN, cmdData.getNick().getName ( ) ) ); 
                return;
                         
            case IS_MARKED :
                this.service.sendMsg ( user, output ( IS_MARKED, cmdData.getNick().getName ( ) ) ); 
                return;
                
            case IDENTIFY_FAIL :
                this.service.sendMsg ( user, output ( PASSWD_ERROR, cmdData.getNick().getName ( ) ) ); 
                return;
                
            case INVALID_EMAIL :
                this.service.sendMsg ( user, output ( INVALID_EMAIL, cmdData.getString1() )  );
                return;
                
            case INVALID_PASS :
                this.service.sendMsg ( user, output ( INVALID_PASS, "" )  );
                return;
                
            default :

        }
        NickInfo ni = cmdData.getNick();
        String value = cmdData.getString1();
        NSAuth auth;
        NSLogEvent log;
        
        switch ( hash ) {
            case SETEMAIL :
                auth = new NSAuth ( MAIL, ni.getName(), value );
                NickServ.addNewAuth ( auth );
                ni.getChanges().hasChanged ( MAIL );
                NickServ.addToWorkList ( CHANGE, ni );
                log = new NSLogEvent ( ni.getName(), MAIL, user.getFullMask(), null );
                NickServ.addLog ( log );
                this.service.sendMsg ( user, "New mail has been set. A verification mail will shortly be sent, please follow the instruction in that mail." );
                break;
                
            case SETPASSWD :
                auth = new NSAuth ( PASS, ni.getName(), value );
                NickServ.addNewAuth ( auth );
                ni.getChanges().hasChanged ( PASS );
                NickServ.addToWorkList ( CHANGE, ni );
                log = new NSLogEvent ( ni.getName(), PASS, user.getFullMask(), null );
                NickServ.addLog ( log );
                this.service.sendMsg ( user, "New password has been set. A verification mail will shortly be sent, please follow the instruction in that mail." );
                break;
                
            default :
                
        }
        
    }

    public void sendIsOutput ( User user, boolean enable, String str )  {
        if ( enable ) { 
            this.service.sendMsg ( user, output ( NICK_IS_NOW, str ) );
            
        } else {
            this.service.sendMsg ( user, output ( NICK_IS_NOT, str ) );
        }
    }
       

    public void showStart ( boolean online, User user, NickInfo ni, String str )  {
        if ( Handler.findUser ( ni.getString ( NAME ) ) == null )  {
            this.service.sendMsg ( user, "*** "+str+ni.getString ( NickInfo.NAME ) + ( online?" [Offline]":"" ) +" ***" );
        } else {
            this.service.sendMsg ( user, "*** "+str+ni.getString ( NickInfo.NAME ) + ( online?" [Online]":"" ) +" ***" );
        }
    }
   
    public void showEnd ( User user, String str ) { 
        this.service.sendMsg ( user, "*** End of "+str+" ***" );
    }

    public static boolean validEmail ( String email )  {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher ( email );
        return matcher.find ( );
    }

    /* EXTERNAL COMMANDS */
    public boolean authMail ( NickInfo ni, Command command )  { 
        ArrayList<User> uList;
        //ni.getSettings().set ( AUTH, true );
        //ni.getChanges().hasChanged ( AUTH );
        //NickServ.addToWorkList ( CHANGE, ni );
        
        if ( NSDatabase.authMail ( ni, command ) ) {
             if ( ( uList = Handler.findUsersByNick ( ni ) ) != null ) {
                for ( User u : uList )  {
                    this.service.sendMsg ( u, output ( NICK_AUTHED, ni.getName ( )  )  );
                }
            }
            ni.setEmail( NSDatabase.getMailByNick ( ni.getName() ) );
            return true;
        }
        return false;
    }
 
    private boolean isGuestNick ( User user ) {
        return user.getString(NAME).toUpperCase().startsWith("GUEST");
    }
 
    private CmdData validateCommandData ( User user, int command, String[] cmd )  {
        NickInfo ni;
        String nick = new String ( );
        String pass = new String ( ); 
        CmdData cmdData = new CmdData ( );
        User target;
        int minlen = 4;
        boolean checkNick   = false;
        boolean checkPass   = false;
        boolean needAccess  = false;
         
        switch ( command )  {
            case REGISTER : 
                if ( isShorterThanLen ( 6, cmd )  )  {
                    cmdData.setStatus ( SYNTAX_ERROR );
                } else if ( ! validEmail ( cmd[5] )  )  {
                    cmdData.setString1 ( cmd[5] );
                    cmdData.setStatus ( INVALID_EMAIL );
                } else if ( ( ni = NickServ.findNick ( user.getString ( NAME ) ) ) != null ) {
                    cmdData.setNick ( ni );
                    cmdData.setStatus ( NICK_ALREADY_REGGED );
                } else if ( this.isGuestNick ( user ) ) {
                    cmdData.setString1 ( user.getString ( NAME ) );
                    cmdData.setStatus ( INVALID_NICK );
                }
                break;
                         
            case IDENTIFY_NICK :
                if ( isShorterThanLen ( 6, cmd ) ) {
                    cmdData.setStatus ( SYNTAX_ERROR );
                } else if ( ( ni = NickServ.findNick ( cmd[4] ) ) == null ) {
                    cmdData.setString1 ( cmd[4] );
                    cmdData.setStatus ( NICK_NOT_REGGED );
                } else if ( ni.is ( FROZEN ) ) {
                    cmdData.setNick ( ni );
                    cmdData.setStatus ( IS_FROZEN ); 
                } else if ( ! ni.identify ( user, cmd[5] ) ) {
                    cmdData.setNick ( ni );
                    cmdData.setStatus ( IDENTIFY_FAIL );
                } else {
                    cmdData.setNick ( ni );
                }
                break; 
               
            case DROP :
            case IDENTIFY :
                if ( isShorterThanLen ( 5, cmd ) ) {
                    cmdData.setStatus ( SYNTAX_ERROR );
                } else if ( ( ni = NickServ.findNick ( user.getString ( NAME ) ) ) == null ) {
                    cmdData.setString1 ( user.getString ( NAME ) );
                    cmdData.setStatus ( NICK_NOT_REGGED );
                } else if ( ni.is ( FROZEN ) ) {
                    cmdData.setNick ( ni );
                    cmdData.setStatus ( IS_FROZEN ); 
                } else if ( ! ni.identify ( user, cmd[4] ) ) {
                    cmdData.setNick ( ni );
                    cmdData.setStatus ( IDENTIFY_FAIL ); 
                } else {
                    cmdData.setNick ( ni );
                }
                break;
                
            case AUTH :
                /* :DreamHea1er PRIVMSG NickServ@services.sshd.biz :auth <authcode>          */
                /*      0          1               2                 3     4        = 5      */
                NSAuth auth;
                
                if ( isShorterThanLen ( 5, cmd ) ) {
                    cmdData.setStatus ( SYNTAX_ERROR );
                } else if ( ( ni = NickServ.findNick ( user.getString ( NAME ) ) ) == null ) {
                    cmdData.setString1 ( user.getString ( NAME ) );
                    cmdData.setStatus ( NICK_NOT_REGGED );
                } else if ( ni.is ( FROZEN ) ) {
                    cmdData.setNick ( ni );
                    cmdData.setStatus ( IS_FROZEN ); 
                } else if ( ni.is ( MARKED ) ) {
                    cmdData.setNick ( ni );
                    cmdData.setStatus ( IS_MARKED ); 
                } else if ( ( auth = NSDatabase.fetchAuth ( user, cmd[4] ) ) == null ) {
                    cmdData.setStatus ( NO_AUTH_FOUND );
                } else {
                    cmdData.setAuth ( auth );
                    cmdData.setNick ( ni );
                }
                break; 
                
            case GHOST :
                if ( isShorterThanLen ( 6, cmd )  )  {
                    cmdData.setStatus ( SYNTAX_ERROR );
                } else if ( ( ni = NickServ.findNick ( cmd[4] ) ) == null ) {
                    cmdData.setStatus ( NICK_NOT_REGGED );
                } else if ( ( target = Handler.findUser ( cmd[4] ) ) == null ) {
                    cmdData.setString1 ( cmd[4] );
                    cmdData.setStatus ( NO_SUCH_NICK );    
                } else if ( ni.is ( FROZEN ) ) {
                    cmdData.setNick ( ni );
                    cmdData.setStatus ( IS_FROZEN );
                } else if ( ni.is ( NOGHOST ) ) {
                    cmdData.setNick ( ni );
                    cmdData.setStatus ( IS_NOGHOST ); 
                } else if ( ! ni.identify ( user, cmd[5] )  )  {
                    cmdData.setStatus ( IDENTIFY_FAIL );
                } else {
                    cmdData.setNick ( ni );
                }
                break; 
            
            case INFO :
                if ( isShorterThanLen ( 4, cmd )  )  {
                    cmdData.setStatus ( SYNTAX_ERROR );
                } else if ( ( ni = NickServ.findNick ( cmd[4] ) ) == null ) {
                    cmdData.setStatus ( NICK_NOT_REGGED );
                } else {
                    cmdData.setNick ( ni );
                    cmdData.setStatus ( 0 );
                }
                break;
            
            case SET :
                needAccess = true; 
                if ( isShorterThanLen ( 6, cmd )  )  {
                    cmdData.setStatus ( SYNTAX_ERROR );
                } 
                break; 
                                
            /* OPER ONLY */
            case LIST :
                if ( isShorterThanLen ( 5, cmd ) ) {
                    cmdData.setStatus ( SYNTAX_ERROR );
                } else if ( ! user.isAtleast ( SA ) ) {
                    cmdData.setString1 ( "SA" );
                    cmdData.setStatus ( ACCESS_DENIED );
                } 
                break;
               
            case NOGHOST :
            case MARK :    
            case FREEZE :
            case HOLD :
                boolean unset = ( cmd.length > 4 && cmd[4].charAt(0) == '-' );
                cmdData.setCommand ( command );
                if ( isShorterThanLen ( 5, cmd ) ) {
                    cmdData.setStatus ( SYNTAX_ERROR );
                } else if ( ! NickServ.enoughAccess ( user, command ) ) {
                    cmdData.setStatus ( ACCESS_DENIED );
                } else if ( ( ni = NickServ.findNick ( cmd[4].replace ( "-", "" ) ) ) == null ) {
                    cmdData.setString1 ( cmd[4].replace ( "-", "" ) );
                    cmdData.setStatus ( NICK_NOT_REGISTERED );
                } else if ( ni.is ( MARK ) && ( command != MARK || command == MARK && cmd[4].charAt(0) != '-' ) ) {
                    cmdData.setNick ( ni );
                    cmdData.setStatus ( IS_MARKED );
                } else if ( ( ni.is ( command ) && ! unset ) || ( ! ni.is (command ) && unset ) ) {
                    cmdData.setNick ( ni );
                    String buf = unset ? "Un" : "";
                    cmdData.setString1 ( buf+this.getCommandStr ( command ) );
                    cmdData.setStatus ( NICKFLAG_EXIST );
                } else {
                    cmdData.setNick ( ni );
                    System.out.println ( "DEBUG:  - "+command );
                    if ( unset ) {
                        cmdData.setCommand ( this.getAntiCommand ( command ) );
                    }
                }
                break;
                
            case GETEMAIL :
            case GETPASS :
                cmdData.setCommand ( command );
                if ( isShorterThanLen ( 5, cmd ) ) {
                    cmdData.setStatus ( SYNTAX_ERROR );
                } else if ( ! NickServ.enoughAccess ( user, command ) ) {
                    cmdData.setStatus ( ACCESS_DENIED );
                } else if ( ( ni = NickServ.findNick ( cmd[4] ) ) == null ) {
                    cmdData.setString1 ( cmd[4] );
                    cmdData.setStatus ( NICK_NOT_REGISTERED );
                } else if ( ni.is ( MARK ) ) {
                    cmdData.setNick ( ni );
                    cmdData.setStatus ( IS_MARKED );
                } else {
                    cmdData.setNick ( ni );
                }
                break;

            case SETEMAIL :
                if ( isShorterThanLen ( 7, cmd ) ) {
                    cmdData.setStatus ( SYNTAX_ERROR );
                } else if ( ( ni = NickServ.findNick ( user.getString(NAME) ) ) == null ) {
                    cmdData.setString1 ( user.getString(NAME) );
                    cmdData.setStatus ( NICK_NOT_REGISTERED );
                } else if ( ni.is ( FROZEN ) ) {
                    cmdData.setNick ( ni );
                    cmdData.setStatus ( IS_FROZEN );
                } else if ( ni.is ( MARK ) ) {
                    cmdData.setNick ( ni );
                    cmdData.setStatus ( IS_MARKED );
                } else if ( ! ni.identify ( user, cmd[5] )  )  {
                    cmdData.setNick ( ni );
                    cmdData.setStatus ( IDENTIFY_FAIL );
                } else if ( ! validEmail ( cmd[6] )  )  {
                    cmdData.setString1 ( cmd[6] );
                    cmdData.setStatus ( INVALID_EMAIL );
                } else {
                    cmdData.setString1 ( cmd[6] );
                    cmdData.setNick ( ni );
                }
                break;
                
            case SETPASSWD :
                System.out.println("0:");
                if ( isShorterThanLen ( 7, cmd ) ) {
                System.out.println("1:");
                    cmdData.setStatus ( SYNTAX_ERROR );
                } else if ( ( ni = NickServ.findNick ( user.getString(NAME) ) ) == null ) {
                System.out.println("2:");
                    cmdData.setString1 ( user.getString(NAME) );
                    cmdData.setStatus ( NICK_NOT_REGISTERED );
                } else if ( ni.is ( FROZEN ) ) {
                System.out.println("3:");
                    cmdData.setNick ( ni );
                    cmdData.setStatus ( IS_FROZEN );
                } else if ( ni.is ( MARK ) ) {
                System.out.println("4:");
                    cmdData.setNick ( ni );
                    cmdData.setStatus ( IS_MARKED );
                } else if ( ! ni.identify ( user, cmd[5] )  )  {
                System.out.println("5:");
                    cmdData.setNick ( ni );
                    cmdData.setStatus ( IDENTIFY_FAIL );
                } else if ( cmd[6].length() < 8 ) {
                System.out.println("6:");
                    cmdData.setStatus ( INVALID_PASS );
                } else {
                System.out.println("7:");
                    cmdData.setString1 ( cmd[6] );
                    cmdData.setNick ( ni );
                }
                System.out.println("8:");
                break;
                
            case DELETE : 
                 cmdData.setCommand ( command );
                if ( isShorterThanLen ( 5, cmd ) ) {
                    cmdData.setStatus ( SYNTAX_ERROR );
                } else if ( ! NickServ.enoughAccess ( user, command ) ) {
                    cmdData.setStatus ( ACCESS_DENIED );
                } else if ( ( ni = NickServ.findNick ( cmd[4] ) ) == null ) {
                    cmdData.setString1 ( cmd[4] );
                    cmdData.setStatus ( NICK_NOT_REGISTERED );
                } else if ( ni.is ( MARK ) ) {
                    cmdData.setNick ( ni );
                    cmdData.setStatus ( IS_MARKED );
                } else {
                    cmdData.setNick ( ni );
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
            case NOGHOST :
                return UNNOGHOST;
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
            case NOGHOST :
                return "NoGhost";
            default :
                return "";
        }
    }
    
     public String output ( int code, String... args )  {
        switch ( code )  {
            case SYNTAX_ERROR :
                return "Syntax: /NickServ "+args[0]+"";
            
            case SYNTAX_ID_ERROR : 
                return "Syntax: /NickServ IDENTIFY <nickname> <password>";
            
            case SYNTAX_REG_ERROR : 
                return "Syntax: /NickServ REGISTER <password> <email>";
            
            case SYNTAX_GHOST_ERROR : 
                return "Syntax: /NickServ GHOST <nickname> <password>";
           
            case NO_SUCH_NICK : 
                return "Error: user "+args[0]+" is not online";
            
            case CMD_NOT_FOUND_ERROR : 
                return "Syntax Error! For information regarding commands please issue:";
           
            case SHOW_HELP :
                return "    /"+args[0]+" HELP";
            
            case PASSWD_ERROR : 
                return "Error: Wrong password for nick: "+args[0];
           
            case INVALID_EMAIL : 
                return "Error: "+args[0]+" is not a valid email-adress";
           
            case INVALID_PASS : 
                return "Error: password is not valid, it might be too short or too easy.";
           
            case INVALID_NICK : 
                return "Error: "+args[0]+" is not a valid nick for registration";
           
            case ACCESS_DENIED : 
                return "Access denied. Please identify to "+args[0]+" before proceeding.";
  
            case SETTING_NOT_FOUND :
                return "Error: Setting "+args[0]+" not found.";
            
            case NICK_NOT_REGISTERED :
                return "Error: nick "+args[0]+" is not registered";
           
            case NICK_ALREADY_REGGED : 
                return "Error: nick "+f.b ( ) +args[0]+f.b ( ) +" is already registered.";
   
            case NICK_IS_NOW : 
                return "Your nick now has option "+args[0]+" set.";
           
            case NICK_IS_NOT : 
                return "Your nick has been stripped from option "+args[0]+".";
  
            case PASSWD_ACCEPTED : 
                return "Password accepted for nick: "+args[0]+". You are now identified.";
            
            case DB_ERROR : 
                return "Error: Database error. Please try again in a few minutes."; 
            
            case REGISTER_DONE :
                return "Nick "+args[0]+" was successfully registered to you. Please remember your password. Please check your mail for further instructions.";
            
            case REGISTER_SEC :
                return "IMPORTANT: Never share your passwords, not even with network staff.";
            
            case NICK_AUTHED : 
                return "The nickname "+args[0]+" is now fully registered and authorized.";
                      
            case NO_AUTH_FOUND : 
                return "The authcode you provided did not match any pending objects.";
           
            case NICK_NEW_MASK : 
                return "Last login from "+args[0]+".";
            
            /*** OPER MESSAGES ***/
            case IDENT_NICK_DELETED : 
                return "You have been unindentified from nick "+args[0]+" as it was deleted from services.";
            
            case NICK_DELETED : 
                return "Nickname "+args[0]+" has been deleted from services.";
            
            case ACCESS_DENIED_SRA : 
                return "Access denied. You need to be atleast SRA to use this command.";
            
            case ACCESS_DENIED_DELETE_OPER :
                return "Access denied. You cannot delete a SA+.";
           
            case NICK_SET_FLAG :
                return "Nick "+args[0]+" is now "+args[1]+".";
                
            case NICKFLAG_EXIST :
                return "Nick "+args[0]+" is already "+args[1]+".";
                     
            case NICK_GETPASS :
                return "Password is: "+args[0]+".";
                      
            case NICK_GETEMAIL :
                return "["+args[0]+"] "+args[1]+" "+args[1]+" "+args[2]+"";
                       
            case IS_MARKED :
                return "Error: Nick "+args[0]+" is MARKed by a network staff blocking certain functionality.";
                        
            case IS_FROZEN :
                return "Error: Nick "+args[0]+" is frozen by a network staff and cannot be used.";
 
            case IS_NOGHOST :
                return "Error: Nick "+args[0]+" is set noghost by a network staff and cannot be ghosted.";
 
            case GLOB_IS_NOGHOST :
                return args[0]+" tried using GHOST on NOGHOST nick "+args[1]+".";
 
            case NICKDROPPED :
                return "Nick: "+args[0]+" was successfully dropped.";                    

            case NICKDELETED :
                return "Nick: "+args[0]+" was successfully deleted.";                    

            default: { 
                return "";
            }
        }
    }
    
    private final static int SYNTAX_ERROR             = 1001;
    private final static int SYNTAX_ID_ERROR          = 1002;
    private final static int SYNTAX_REG_ERROR         = 1003;
    private final static int SYNTAX_GHOST_ERROR       = 1004;
    private final static int NO_SUCH_NICK             = 1005;

    private final static int CMD_NOT_FOUND_ERROR      = 1021;
    private final static int SHOW_HELP                = 1022;

    private final static int ACCESS_DENIED            = 1101;
    private final static int SETTING_NOT_FOUND        = 1151;
    private final static int NICK_NOT_REGISTERED      = 1152;
    private final static int NICK_ALREADY_REGGED      = 1153;
    
    private final static int NICK_WILL_NOW            = 1201;
    private final static int NICK_WILL_NOW_NOT        = 1202;
    
    private final static int NICK_IS_NOW              = 1221;
    private final static int NICK_IS_NOT              = 1222;
    
    private final static int PASSWD_ERROR             = 1301;
    private final static int INVALID_EMAIL            = 1302;
    private final static int INVALID_NICK             = 1303;
    private final static int INVALID_PASS             = 1304;

    private final static int PASSWD_ACCEPTED          = 1351;
 
    private final static int DB_ERROR                 = 1401;
    private final static int DB_NICK_ERROR            = 1402;
    
    private final static int REGISTER_DONE            = 1501;
    private final static int REGISTER_SEC             = 1502;
    
    private final static int NICK_AUTHED              = 1601;
    private final static int NICK_NEW_MASK            = 1602;
    private final static int NO_AUTH_FOUND            = 1651;
    
    /*** OPER MESSAGES ***/
    private final static int IDENT_NICK_DELETED       = 1801;
    private final static int NICK_DELETED             = 1802; 
    private final static int ACCESS_DENIED_SRA          = 1804;
    private final static int ACCESS_DENIED_DELETE_OPER  = 1805;
    private final static int NICK_SET_FLAG            = 1811;
    private final static int NICKFLAG_EXIST           = 1812;
    private final static int NICK_GETPASS             = 1813;
    private final static int NICK_GETEMAIL            = 1814;
    private final static int IS_MARKED                = 2401; 
    private final static int IS_FROZEN                = 2402; 
    private final static int IS_NOGHOST               = 2403; 
    private final static int GLOB_IS_NOGHOST          = 2404; 

    private final static int NICKDROPPED              = 2501; 
    private final static int NICKDELETED              = 2502; 


 } 