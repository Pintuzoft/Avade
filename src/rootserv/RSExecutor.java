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

import core.Executor;
import core.Handler;
import core.HashString;
import core.Proc;
import nickserv.NickInfo;
import nickserv.NickServ;
import operserv.OSLogEvent;
import operserv.Oper;
import operserv.OperServ;
import user.User;

/**
 *
 * @author DreamHealer
 */
public class RSExecutor extends Executor {
    private RSSnoop         snoop;
    
    /**
     *
     * @param service
     * @param snoop
     */
    public RSExecutor ( RootServ service, RSSnoop snoop )  {
        super ( );
        this.service        = service;
        this.snoop          = snoop;
    }

    /**
     *
     * @param user
     * @param cmd
     */
    public void parse ( User user, String[] cmd, HashString command )  {
        Oper oper = user.getSID().getOper ( );
  
        if ( ! oper.isAtleast ( SRA )  )  {
            this.service.sendMsg ( user, "Access denied!." );
            return;
        }
        
        this.found = true; /* Assume that everything will go correctly */

        
        /* Enforce the command access levels from the config */
        if ( this.service.findCommandInfo ( command ) != null &&
             ! RootServ.enoughAccess ( user, command ) ) {
            this.snoop.msg ( false, user, cmd );
            return;
        }
        
        if ( command.is(STOP) ) {
            this.stop ( user, cmd );
        
        } else if ( command.is(REHASH) ) {
            this.rehash ( user, cmd );
        
        } else if ( command.is(SHOWCONFIG) ) {
            this.showConfig ( user, cmd );
                
        } else if ( command.is(SRAW) ) {
            this.sraw ( user, cmd );
        
        } else if ( command.is(SRA) ) {
            this.sra ( user, cmd );
        
        } else if ( command.is(PANIC) ) {
            this.panic ( user, cmd );
        
        } else {
            this.found = false;
            this.noMatch ( user, cmd[3] );
        }
         
        this.snoop.msg ( this.found, user, cmd );
    }

    private void rehash ( User user, String[] cmd )  {
        if ( Proc.rehashConf ( user )  )  {
            this.service.sendMsg ( user, "Rehashing services successful" );
        } else {
            this.service.sendMsg ( user, "Rehashing services failed" );
        }
    }

    private void stop ( User user, String[] cmd ) {
        RootServ.setPanic ( OPER );
        this.service.sendGlobOp ( "SERVICES STOP!. Issued by: "+user.getFullMask()+" ["+user.getOper().getName()+"]" );
        Proc.stopServices();
    }
 
    
    private void sraw ( User user, String[] cmd )  {
        if ( cmd.length < 5 ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "SRAW <raw server line>" ) );
            return;
        }
        String buf = "";
        for ( int i = 4; i < cmd.length; i++ )  {
            if ( buf.length() > 0 ) {
                buf += " ";
            }
            buf += cmd[i];
         }
        this.service.sendRaw ( buf );
    }
        
    private void showConfig ( User user, String[] cmd )  {
        this.service.sendMsg ( user, "*** Services Config:" );
        for ( String c : Proc.getConf().getConfigList ( user ) ) {
            this.service.sendMsg ( user, c );
        }
        this.service.sendMsg ( user, "*** End of Config ***" );
    }
    
    /* SRA */
    private void sra ( User user, String[] cmd )  {
        // :DreamHea1er PRIVMSG RootServ@services.sshd.biz :SRA LIST
        // :DreamHea1er PRIVMSG RootServ@services.sshd.biz :SRA ADD Pintuz
        //  0           1       2                           3   4   5       = 6
        NickInfo sra;
        NickInfo target;
        HashString command;
        
        if ( cmd.length < 5 ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "SRA <ADD|DEL|LIST> [<nick>]" ) );
            return;
        }

        if ( ! RSDatabase.checkConn ( )  )  {
            Handler.getRootServ().sendMsg ( user, "Database error. Please try again in a little while." );
            return;
        }

        command = new HashString ( cmd[4] );
        
        if ( command.is(LIST) ) {
            this.doListSra ( user );
            return;
        }
         
        if ( cmd.length < 6 ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "SRA <ADD|DEL|LIST> [<nick>]" ) );
            return;
        }
        
        /* Only the services master may maintain the SRA list */
        if ( ! user.isAtleast ( MASTER ) ) {
            this.service.sendMsg ( user, output ( ACCESS_DENIED, "" )  );
            return;
        }
        
        sra     = NickServ.findNick ( user.getSID().getOper().getName ( ) );
        target  = NickServ.findNick ( cmd[5] );
        
        if ( sra == null )  {
            this.service.sendMsg ( user, output ( ACCESS_DENIED, "" )  );

        } else if ( target == null )  {
            this.service.sendMsg ( user, output ( NICK_NOT_REGGED, cmd[5] )  );

        } else if ( command.is(ADD) ) {
            /* The master stays master, and a SRA is not added twice */
            if ( target.getOper().isAtleast ( SRA ) ) {
                this.service.sendMsg ( user, output ( SRA_NOT_ADD, target.getNameStr() ) );
                return;
            }
            this.setStaff ( user, target, new Oper ( target.getNameStr(), 4, sra.getNameStr() ), ADDSRA );
            this.service.sendMsg ( user, output ( SRA_ADD, target.getNameStr() ) );
            this.service.sendGlobOp ( output ( GLOB_SRA_ADD, sra.getNameStr(), target.getNameStr() ) );
       
        } else if ( command.is(DEL) ) {
            /* Only a SRA is removed here: not the master, and not lower staff */
            if ( target.getOper().getAccess ( ) != 4 ) {
                this.service.sendMsg ( user, output ( SRA_NOT_DEL, target.getNameStr() ) );
                return;
            }
            this.setStaff ( user, target, new Oper ( ), DELSRA );
            this.service.sendMsg ( user, output ( SRA_DEL, target.getNameStr() ) );
            this.service.sendGlobOp ( output ( GLOB_SRA_DEL, sra.getNameStr(), target.getNameStr() ) );
        
        } else {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "SRA <ADD|DEL|LIST> [<nick>]" )  ); 
        }
    }
    
    /* Change the staff level of a nick the same way OperServ STAFF does it:
       in memory at once, to the database through OperServ, and on the
       users that are identified to the nick */
    private void setStaff ( User user, NickInfo target, Oper oper, HashString event ) {
        OperServ.addLogEvent ( new OSLogEvent ( target.getName(), event, user, user.getOper().getNick() ) );
        if ( oper.getAccess ( ) > 0 ) {
            OperServ.addOper ( oper );
        } else {
            OperServ.delOper ( target );
        }
        target.setOper ( oper );
        for ( User u : Handler.findUsersByNick ( target ) ) {
            Handler.forceOperModes ( u );
            NickServ.applyStaffTag ( u );
        }
    }
     
    private void doListSra ( User user )  {
        this.service.sendMsg ( user, "Services Root Admin list:" );
        for ( Oper sra : OperServ.getRootAdmins() ) {
            this.service.sendMsg ( user, "    "+sra.getString ( NAME ) +"  ( Instated by: "+sra.getString ( INSTATER ) +" ) " );
        }
        this.service.sendMsg ( user, "*** End of List ***" );
    }
    /* END SRA */

    /* PANIC */
    private void panic ( User u, String[] cmd )  {
        // :DreamHea1er PRIVMSG RootServ@services.sshd.biz :PANIC
        // :DreamHea1er PRIVMSG RootServ@services.sshd.biz :PANIC OPER
        //  0           1       2                           3     4   5       = 6
        NickInfo sra;
        HashString state;
        String panic;
        
        if ( cmd.length > 4 ) {
            state = new HashString ( cmd[4] );
        } else {
            state = NONE;
        }
        
        sra = NickServ.findNick ( u.getSID().getOper().getName ( ) );
        
        if ( sra == null )  {
            this.service.sendMsg ( u, output ( ACCESS_DENIED, "" )  );
            return;
        }
        
        if ( state.is(OPER) ||
             state.is(IDENT) ||
             state.is(USER) ) {
            RootServ.setPanic ( state );
            panic = RootServ.getPanicStr ( state );            
        
        } else if ( state.is(NONE) ) {
            Handler.getRootServ().sendMsg ( u, "Panic state is: "+RootServ.getPanicStr ( NONE ) );
            return;
            
        } else {
            Handler.getRootServ().sendMsg ( u, "Error: not a valid panic state" );
            return;
        }
        Handler.getRootServ().sendGlobOp ( sra.getName()+" changed PANIC state to: "+panic );
    }
    
    /* OUTPUT */

    /**
     *
     * @param code
     * @param args
     * @return
     */

    public String output ( HashString code, String... args )  {
        if ( code.is(ACCESS_DENIED) ) {
            return "Access denied!";
        
        } else if ( code.is(SYNTAX_ERROR) ) {
            return "Syntax: /RootServ "+args[0];
        
        } else if ( code.is(NICK_NOT_REGGED) ) {
            return "Nick "+args[0]+" is not registered";
        
        } else if ( code.is(SRA_ADD) ) {
            return "Nick "+args[0]+" was added to the SRA list";
        
        } else if ( code.is(SRA_NOT_ADD) ) {
            return "Nick "+args[0]+" was NOT added to the SRA list"; 
        
        } else if ( code.is(SRA_DEL) ) {
            return "Nick "+args[0]+" was deleted from the SRA list";
        
        } else if ( code.is(SRA_NOT_DEL) ) {
            return "Nick "+args[0]+" was NOT deleted from the SRA list";
        
        } else if ( code.is(GLOB_SRA_ADD) ) {
            return args[0]+" has added "+args[1]+" to the Services Root Admin list";
        
        } else if ( code.is(GLOB_SRA_DEL) ) {
            return args[0]+" has removed "+args[1]+" from the Services Root Admin list";
        
        } else {
            return "";
        }  
    }
    
}
