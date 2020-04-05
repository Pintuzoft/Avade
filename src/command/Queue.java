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
package command;

import core.Handler;
import core.Proc;
import core.HashNumeric;
import nickserv.NickInfo;
import java.util.LinkedList;
import nickserv.NickServ;

/**
 *
 * @author DreamHealer
 */
public class Queue extends HashNumeric {
    private LinkedList<Command>     cList;
    private LinkedList<Command>     buf;
    private long                    time;
    
    public Queue ( ) {
        this.cList = new LinkedList<> ( );
    }
    
    public void maintenance ( ) {
        if ( time < ( System.currentTimeMillis ( ) ) ) {
            //System.out.println ( "Queue: "+time+" < "+System.currentTimeMillis ( ) );
            this.retrieve ( );
            this.next ( );
            time = System.currentTimeMillis ( ) + ( 5000 );
        }
    }
    
    public void retrieve ( ) {
        this.cList = CMDDatabase.getCommands ( );
        if ( ! this.cList.isEmpty ( ) ) {
            boolean found;
            this.buf = CMDDatabase.getCommands ( );
            for ( Command cmd : this.buf ) {
                found = false;
                try {
                    for ( Command cmd2 : this.cList )  {
                        if ( cmd.getHashCode ( ) == cmd2.getHashCode ( ) ) {
                            found = true;
                        }
                    }
                    if ( ! found )  {
                        this.cList.add ( cmd );
                    }
                } catch ( Exception e )  {
                    Proc.log ( Queue.class.getName ( ), e );
                }
            }
        }
    }
   
    
    public void next ( )  {
        Command command;
        if ( ! cList.isEmpty ( ) ) {
            /* Something is in queue */
            if (  ( command = this.cList.pop ( ) ) != null ) {
                this.execute ( command );
            }
        }
    }

    private void execute ( Command command )  {
        boolean res = false;
        if ( command.getTargetType ( ) == NICKINFO )  {
            /* Target is a nickname */ 
            NickInfo ni =  ( NickInfo )  command.getTarget ( );
            NickInfo ni2;
            switch ( command.getCommand ( ) ) {
                case AUTH : { 
                    ni2 = NickServ.findNick ( ni.getName ( )  );                  
                    res = Handler.getNickServ().authorizeNick ( ni2, command );
                }
            } 
        }
        if ( !res )  {
            /* bad result? lets readd it to queue */
            this.cList.add ( command );
        } else {
            /* Good result lets remove it from the database */
            CMDDatabase.deleteCommand ( command.getID ( ) );
        }
    }
}
