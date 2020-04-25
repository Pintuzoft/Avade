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
package memoserv;

import core.Handler;
import core.HashString;
import core.Proc;
import core.Service;
import core.TextFormat;
import nickserv.NickInfo;
import user.User;

/**
 *
 * @author DreamHealer
 */
public class MemoServ extends Service {
    private static boolean              is = false; 
    private MSExecutor                  executor;       /* Object that parse and execute commands */
    private MSHelper                    helper;         /* Object that parse and respond to help queries */
    private MSSnoop                     snoop;          /* Object that parse and respond to help queries */
    private TextFormat                  f;
    
    public MemoServ ( )  {
        super ( "MemoServ" );
        initMemoServ ( );    
    }
 
    private void initMemoServ ( )  {
        is              = true;
        this.snoop      = new MSSnoop       ( this ); 
        this.executor   = new MSExecutor    ( this, this.snoop );
        this.helper     = new MSHelper      ( this, this.snoop ); 
        this.f          = new TextFormat    ( );
    }
    
    public void parse ( User user, String[] cmd )  {
        //:DreamHea1er PRIVMSG NickServ@services.sshd.biz :help
        HashString command;
        try {
            if ( cmd[3].isEmpty ( )  )  { 
                return; 
            }
        } catch ( Exception e )  {
            Proc.log ( MemoServ.class.getName ( ) , e );
        }
        
        if ( ! MSDatabase.checkConn ( )  )  {
            Handler.getMemoServ ( ) .sendMsg ( user, "Database error. Please try again in a little while." );
            return;
        }
        
        user.getUserFlood().incCounter ( this );
         
        command = new HashString ( cmd[3].substring ( 1 ) );
        
        if ( command.is(OHELP) ) {
            this.doOHelp ( user, cmd );
        
        } else if ( command.is(HELP) ) {
            this.helper.parse ( user, cmd );
        
        } else {
            this.doDefault ( user, cmd );
        } 
    }
     
    
    public static void is ( boolean state ) { 
        is = state;
    }
    public static boolean isUp ( ) { 
        return is;
    } 
    public void setState ( boolean state ) {
        MemoServ.is = state;
    }

    
    public void adNick ( NickInfo ni, User u, int count )  {
        this.sendMsg ( u, "You have "+f.b ( ) +count+f.b ( ) +" new memo"+ ( count==1?"":"s" ) +"." );
    }
 
    public void doDefault ( User user, String[] cmd )  {
        /** We are suppose to execute **/
        this.executor.parse ( user, cmd );
    } 
    
    public void doOHelp ( User user, String[] cmd )  {
        if ( ! user.isOper ( )  )  {
            this.snoop.msg ( false, new HashString ( "NickServ" ),user, cmd ); 
            return;
        }
    }
    
    
    public void checkNick ( NickInfo ni, User u )  {
        if ( ni == null || u == null )  {
            return;
        }
        int count = 0;
        for ( MemoInfo m : ni.getMemos ( )  )  {
            if ( ! m.isRead ( )  )  {
                count++;
            }
        }

        if ( count > 0 )  {
            this.adNick ( ni, u, count );
        }  
    }
    
    public void newMemo ( MemoInfo memo )  {
        User u = Handler.findUser ( memo.getName ( ) );
        
        if ( u != null )  {
            this.executor.adNewMemo ( memo, u );
        }
    }
}
