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

/**
 *
 * @author fredde
 */

// create table host  ( ip varchar ( 64 ) , host varchar ( 128 ) , stamp int ( 11 ) , primary key  ( ip )  );

public class HostInfo {
    private String host;
    private String realHost;
    private String ip;
    private String range;
    private int ipHash;
    private int ipHash24;
    private boolean isIPv4;
    
    /**
     *
     * @param ip
     * @param host
     */
    public HostInfo ( long ip, String host )  {
        this.ip   = intToIP ( ip );
        this.ipHash = this.ip.hashCode();
        this.host = host;
        if ( this.ip.contains(".") ) {
            this.isIPv4 = true;
            this.range = IPv4ToCidr24 ( this.ip );
        } else {
            this.isIPv4 = false;
            this.range = IPv6ToCidr24 ( this.ip );
        }
        this.ipHash24 = this.range.hashCode();
    }

    private static String IPv4ToCidr24 ( String ip ) {
        return ip.substring(0,ip.lastIndexOf("."))+".*";
    }

    private static String IPv6ToCidr24 ( String ip ) {
        return ip.substring(0,ip.lastIndexOf(":"))+":*";
    }        

    private static String intToIP ( long bytes )  {
        return String.format ( "%d.%d.%d.%d",   ( bytes >> 24 & 0xff ) ,  ( bytes >> 16 & 0xff ) ,  ( bytes >> 8 & 0xff ) ,  ( bytes & 0xff )  );
    }
    
    /**
     *
     * @return
     */
    public String getHost ( ) { 
        return this.host;
    }
    
    /**
     *
     * @return
     */
    public String getIp ( ) { 
        return this.ip;
    }

    /**
     *
     * @return
     */
    public String getRange ( ) { 
        return this.range;
    }

    /**
     *
     * @return
     */
    public int getIpHash ( ) { 
        return this.ipHash;
    }

    /**
     *
     * @return
     */
    public int getRangeHash ( ) { 
        return this.ipHash24;
    }
    
    /**
     *
     * @return
     */
    public String getRealHost ( ) { 
        return this.realHost;
    }
    
    /**
     *
     * @param hash
     * @return
     */
    public boolean ipMatch ( int hash ) {
        return this.ipHash == hash;
    }

    /**
     *
     * @param hash
     * @return
     */
    public boolean rangeMatch ( int hash ) {
        return this.ipHash24 == hash;
    }
    
}
