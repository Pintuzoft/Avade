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
package core;

import user.User;
import server.ServSock;
import server.SExecutor;

/**
 *
 * @author DreamHealer
 */
public class Services extends HashNumeric {
    private String              name;
    private String              stats;
    private SExecutor           executor;
    
    public Services ( )  {  
        this.name       = Proc.getConf().get ( NAME );
        this.stats      = Proc.getConf().get ( STATS );
        this.executor   = new SExecutor ( this ); 
    }

    public void parse ( User user, String[] cmd )  {
        User u;

        try {
            switch ( cmd[1].hashCode ( )  )  {
                case STATS :
                    cmd[3] = cmd[3].substring ( 1 );
                    break;
                    
                default:
                    cmd[2] = cmd[2].substring ( 1 );
               
            }
            this.executor.parse ( user, cmd );
        } catch ( Exception e ) { 
            Proc.log ( Services.class.getName ( ) , e );
        }
    }

    public String getString ( int var )  {
        switch ( var )  {
            case NAME :
                return this.name;

            case STATS :
                return this.stats;

            default:
                return null;

        }
    }
    
 
    public void sendServicesCMD ( User u, int numeric, String msg )  {
        this.sendRaw ( ":"+this.name+" "+numeric+" "+u.getString ( NAME ) +" :"+msg );
    }

    public void sendStatsCMD ( User u, int numeric, String msg )  {
        this.sendRaw ( ":"+this.stats+" "+numeric+" "+u.getString ( NAME ) +" :"+msg );
    }

    public void sendRaw ( String cmd )  {
        try {
            ServSock.sendCmd ( cmd );
        } catch ( Exception e ) {
            Proc.log ( Services.class.getName ( ) , e );
        }
    } 
} 