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
 * along with this program; if not, see <https://www.gnu.org/licenses/>.
 */
package command;

import core.Handler;
import core.WorkGuard;
import core.HashNumeric;
import core.HashString;
import nickserv.NickInfo;
import java.util.LinkedList;

/**
 *
 * @author DreamHealer
 */
public class Queue extends HashNumeric {
    private LinkedList<Command>     cList;
    private long                    time;
    
    /**
     *
     */
    public Queue ( ) {
        this.cList = new LinkedList<> ( );
    }
    
    /**
     * Run the commands the web interface put in the database (mail and
     * password confirmations)
     */
    public void maintenance ( ) {
        if ( time > System.currentTimeMillis ( ) || ! Handler.isDataLoaded ( ) ) {
            return;
        }
        time = System.currentTimeMillis ( ) + 5000;
        this.cList = CMDDatabase.getCommands ( );
        while ( ! this.cList.isEmpty ( ) ) {
            this.execute ( this.cList.pop ( ) );
        }
    }
    
    /**
     *
     */
    public void next ( )  {
        Command command;
        if ( ! cList.isEmpty ( ) &&
            ( command = this.cList.pop ( ) ) != null ) {
            this.execute ( command );
        }
    }

    private void execute ( Command command )  {
        boolean res = false;
        if ( command.getTargetType().is(NICKINFO) )  {
            /* Target is a nickname */ 
            NickInfo ni =  ( NickInfo )  command.getTarget ( );
            HashString cmd = command.getCommandData();
            
            if ( cmd.is(AUTH) ) {
                res = Handler.getNickServ().authorizeMail ( ni, command );
            
            } else if ( cmd.is(PASS) ) {
                res = Handler.getNickServ().authorizePass ( ni, command );
            }
             
        }
        String key = ""+command.getID ( );
        if ( res )  {
            /* Good result lets remove it from the database */
            WorkGuard.doneKey ( key );
            CMDDatabase.deleteCommand ( command.getID ( ) );
        } else if ( WorkGuard.failedKey ( key, "web command "+key ) ) {
            /* Keeps failing, don't let it be retried forever */
            CMDDatabase.deleteCommand ( command.getID ( ) );
        }
    }
}
