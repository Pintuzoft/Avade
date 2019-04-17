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

import core.Executor;
import core.Handler;
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

        switch ( cmd[3].toUpperCase ( ) .hashCode ( )  )  {
            case REHASH :
                this.rehash ( user, cmd );
                break;
                
            case SRAW :
                this.sraw ( user, cmd );
                break;
                
            case SRA :
                this.sra ( user, cmd );
                break;
                 
            case PANIC :
                this.panic ( user, cmd );
                break;
                
            default: 
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
        
        if ( cmd.length < 5 ) {
            this.service.sendMsg ( user, output ( SYNTAX_ERROR, "SRA <ADD|DEL|LIST> [<nick>]" ) );
        }

        if ( ! RSDatabase.checkConn ( )  )  {
            Handler.getRootServ().sendMsg ( user, "Database error. Please try again in a little while." );
            return;
        }

        if ( cmd[4].toUpperCase().hashCode() == LIST ) {
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
            switch ( cmd[4].toUpperCase().hashCode ( ) ) {
                case ADD :
                    if ( RSDatabase.addSra ( sra, target )  )  {
                        this.service.sendMsg ( user, output ( SRA_ADD, target.getName ( )  )  );
                        this.service.sendGlobOp ( output ( GLOB_SRA_ADD, sra.getName ( ) , target.getName ( )  )  );
                    } else {
                        this.service.sendMsg ( user, output ( SRA_NOT_ADD, target.getName ( )  )  );
                    }
                    break;
                    
                case DEL :
                    if ( RSDatabase.delSra ( target )  )  {
                        this.service.sendMsg ( user, output ( SRA_DEL, target.getName ( )  )  );
                        this.service.sendGlobOp ( output ( GLOB_SRA_DEL, sra.getName ( ) , target.getName ( )  )  );
                    } else {
                        this.service.sendMsg ( user, output ( SRA_NOT_DEL, target.getName ( )  )  );
                    }
                    break;
                    
                default:  
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
        int state;
        String panic;
        
        if ( cmd.length > 4 ) {
            state = cmd[4].toUpperCase().hashCode();
        } else {
            state = NONE;
        }
        
        sra = NickServ.findNick ( u.getSID().getOper().getString ( NAME ) );
        
        if ( sra == null )  {
            this.service.sendMsg ( u, output ( ACCESS_DENIED, "" )  );
            return;
        }
        
        switch ( state ) {
            case OPER :
            case IDENT :
            case USER :
                RootServ.setPanic ( state );
                panic = RootServ.getPanicStr ( state );
                break;

            case NONE :
                Handler.getRootServ().sendMsg ( u, "Panic state is: "+RootServ.getPanicStr ( NONE ) );
                return;
                
            default : 
                Handler.getRootServ().sendMsg ( u, "Error: not a valid panic state" );
                return;
        }
        Handler.getRootServ().sendGlobOp ( sra.getName()+" changed PANIC state to: "+panic );
    }
    
    /* OUTPUT */
    public String output ( int code, String... args )  {
        switch ( code )  {
            case ACCESS_DENIED :
                return "Access denied!";
                
            case SYNTAX_ERROR :
                return "Syntax: /OperServ "+args[0];
                
            case NICK_NOT_REGGED :
                return "Nick "+args[0]+" is not registered";
                
            case SRA_ADD :
                return "Nick "+args[0]+" was added to the SRA list";
                
            case SRA_NOT_ADD :
                return "Nick "+args[0]+" was NOT added to the SRA list"; 
                
            case SRA_DEL :
                return "Nick "+args[0]+" was deleted from the SRA list";
                
            case SRA_NOT_DEL :
                return "Nick "+args[0]+" was NOT deleted from the SRA list";
                
            case GLOB_SRA_ADD :
                return args[0]+" has added "+args[1]+" to the Services Root Admin list";
                
            case GLOB_SRA_DEL :
                return args[0]+" has removed "+args[1]+" from the Services Root Admin list";
                
            default: 
                return ""; 
            
        }
    }
    
    private static final int ACCESS_DENIED      = 1001;
    private static final int SYNTAX_ERROR       = 1002;
    private static final int NICK_NOT_REGGED    = 1003;
    private static final int SRA_ADD            = 1004;
    private static final int SRA_NOT_ADD        = 1005;
    private static final int SRA_DEL            = 1006;
    private static final int SRA_NOT_DEL        = 1007;
    
    private static final int GLOB_SRA_ADD       = 1101;
    private static final int GLOB_SRA_DEL       = 1102;
 
}
