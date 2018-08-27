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
package user;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fredde
 */

// create table host  ( ip varchar ( 64 ) , host varchar ( 128 ) , stamp int ( 11 ) , primary key  ( ip )  );

public class HostInfo {
    private String host;
    private String realHost;
    private String ip;
    
    public HostInfo ( long ip, String host )  {
        this.ip         = this.intToIP ( ip );
        this.host       = host;
        this.realHost   = this.ipToHost ( );
    }
    
    private String intToIP ( long bytes )  {
        return String.format ( "%d.%d.%d.%d",   ( bytes >> 24 & 0xff ) ,  ( bytes >> 16 & 0xff ) ,  ( bytes >> 8 & 0xff ) ,  ( bytes & 0xff )  );
    }

    private String ipToHost ( )  {
        String host = HostDatabase.getHost ( this.ip ) ;
       
        if ( host  == null ) {
            try {
                InetAddress a = InetAddress.getByName ( this.ip );
                host = a.getHostName ( );
               
            } catch  ( UnknownHostException ex )  {
                Logger.getLogger ( HostInfo.class.getName ( )  ) .log ( Level.SEVERE, null, ex );
                host = this.ip;
            }
            HostDatabase.addHost ( this.ip, host );
        } 
        return host;
    }
    
    public String getHost ( ) { 
        return this.host;
    }
    
    public String getIp ( ) { 
        return this.ip;
    }
    
    public String getRealHost ( ) { 
        return this.realHost;
    }
}
