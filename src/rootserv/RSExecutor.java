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
import operserv.Oper;
import operserv.OperServ;
import user.User;

/**
 *
 * @author DreamHealer
 */
public class RSExecutor extends Executor {
    private RSSnoop         snoop;
    
    public RSExecutor ( RootServ service, RSSnoop snoop )  {
        super ( );
        this.service        = service;
        this.snoop          = snoop;
    }

    public void parse ( User user, String[] cmd )  {
        Oper oper = user.getSID().getOper ( );
  
        if ( ! oper.isAtleast ( SRA )  )  {
            this.service.sendMsg ( user, "Access denied!." );
            return;
        }
        
        this.found = true; /* Assume that everything will go correctly */

        HashString command = new HashString ( cmd[3] );
        
        if ( command.is(STOP) ) {
            this.stop ( user, cmd );
        
        } else if ( command.is(REHASH) ) {
            this.rehash ( user, cmd );
        
        } else if ( command.is(SRAW) ) {
            this.sraw ( user, cmd );
        
        } else if ( command.is(SRA) ) {
            this.sra ( user, cmd );
        
        } else if ( command.is(PANIC) ) {
            this.panic ( user, cmd );
        }
         
        this.snoop.msg ( this.found, user, cmd );
    }

    private void rehash ( User user, String[] cmd )  {
        if ( Proc.rehashConf ( )  )  {
            this.service.sendMsg ( user, "Rehashing services successful" );
        } else {
            this.service.sendMsg ( user, "Rehashing services failed" );
        }
    }

    private void stop(User user, String[] cmd) {
        if ( user == null || user.getOper() == null || user.getOper().getName() == null ) {
            this.service.sendGlobOp ( "SERVICES STOP!. Failed by: "+user.getFullMask() );
            return;
        }
        RootServ.setPanic ( OPER );
        this.service.sendGlobOp ( "SERVICES STOP!. Issued by: "+user.getFullMask()+" ["+user.getOper().getName()+"]" );
        Proc.stopServices();
    }
 
    
    private void sraw ( User user, String[] cmd )  {
        String buf = new String ( );
        for ( int i = 4; i < cmd.length; i++ )  {
            if ( buf.length() > 0 ) {
                buf += " ";
            }
            buf += cmd[i];
         }
        this.service.sendRaw ( buf );
    }
    
    /* SRA */
    private void sra ( User user, String[] cmd )  {
        // :DreamHea1er PRIVMSG RootServ@services.sshd.biz :SRA LIST
        // :DreamHea1er PRIVMSG RootServ@services.sshd.biz :SRA ADD Pintuz
        //  0           1       2                           3   4   5       = 6
        System.out.println ( "debug ( doSra );" );
        NickInfo sra;
        NickInfo target;
        HashString command;
        
        if ( cmd.length < 5 ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "SRA <ADD|DEL|LIST> [<nick>]" ) );
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
         
        sra     = NickServ.findNick ( user.getSID().getOper().getString ( NAME ) );
        target  = NickServ.findNick ( cmd[5] );
        
        if ( sra == null )  {
            this.service.sendMsg ( user, output ( ACCESS_DENIED, "" )  );

        } else if ( target == null )  {
            this.service.sendMsg ( user, output ( NICK_NOT_REGGED, cmd[5] )  );

        } else {
            if ( command.is(ADD) ) {
                if ( RSDatabase.addSra ( sra, target )  )  {
                    this.service.sendMsg ( user, output ( SRA_ADD, target.getNameStr() ) );
                    this.service.sendGlobOp ( output ( GLOB_SRA_ADD, sra.getNameStr(), target.getNameStr() ) );
                } else {
                    this.service.sendMsg ( user, output ( SRA_NOT_ADD, target.getNameStr() ) );
                }   
           
            } else if ( command.is(DEL) ) {
                if ( RSDatabase.delSra ( target )  )  {
                    this.service.sendMsg ( user, output ( SRA_DEL, target.getNameStr() ) );
                    this.service.sendGlobOp ( output ( GLOB_SRA_DEL, sra.getNameStr(), target.getNameStr() ) );
                } else {
                    this.service.sendMsg ( user, output ( SRA_NOT_DEL, target.getNameStr() ) );
                }
            
            } else {
                this.service.sendMsg ( user, output ( SYNTAX_ERROR, "SRA <ADD|DEL|LIST> [<nick>]" )  ); 
            }
            
        }
    }
     
    private void doListSra ( User user )  {
        System.out.println ( "debug ( doListSra );" );

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
        System.out.println ( "debug ( doPanic );" );
        HashString state;
        String panic;
        
        if ( cmd.length > 4 ) {
            state = new HashString ( cmd[4] );
        } else {
            state = NONE;
        }
        
        sra = NickServ.findNick ( u.getSID().getOper().getString ( NAME ) );
        
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
    public String output ( HashString code, String... args )  {
        if ( code.is(ACCESS_DENIED) ) {
            return "Access denied!";
        
        } else if ( code.is(SYNTAX_ERROR) ) {
            return "Syntax: /OperServ "+args[0];
        
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
