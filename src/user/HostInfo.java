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
package user;

import java.net.InetAddress;
import java.net.UnknownHostException;

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
    private boolean isUnknown;
    
    /**
     * @param ipField the IP field of a NICK line. Either a string (NICKIPSTR:
     *                "1.2.3.4" or "2001:db8::1") or the old numeric IPv4 form,
     *                where bahamut sends 1 for IPv6 clients.
     * @param host
     */
    public HostInfo ( String ipField, String host )  {
        this.host = host;
        byte[] addr = parseIP ( ipField );
        if ( addr == null ) {
            /* Unknown IP, never match it against other users */
            this.isUnknown = true;
            this.isIPv4 = true;
            this.ip = "0.0.0.0";
            this.range = "0.0.0.*";
        } else if ( addr.length == 4 ) {
            this.isIPv4 = true;
            this.ip = ( ipField.contains(".") ? ipField : intToIP ( Long.parseLong ( ipField ) ) );
            this.range = IPv4ToCidr24 ( this.ip );
        } else {
            this.isIPv4 = false;
            this.ip = ipField;
            this.range = IPv6ToCidr64 ( addr );
        }
        this.ipHash = this.ip.hashCode();
        this.ipHash24 = this.range.hashCode();
    }

    /* Returns the address bytes, or null if the IP is unknown or invalid */
    private static byte[] parseIP ( String ipField ) {
        if ( ipField == null || ipField.isEmpty() ) {
            return null;
        }
        try {
            if ( ipField.matches ( "[0-9]+" ) ) {
                long num = Long.parseLong ( ipField );
                /* 0 = unknown, 1 = IPv6 client sent to a non NICKIPSTR server */
                if ( num <= 1 || num > 0xFFFFFFFFL ) {
                    return null;
                }
                return new byte[] { (byte) ( num >> 24 ), (byte) ( num >> 16 ), (byte) ( num >> 8 ), (byte) num };
            }
            /* Only accept IP literals so we never trigger a DNS lookup */
            if ( ! ipField.matches ( "[0-9a-fA-F:.]+" ) || ! ( ipField.contains(".") || ipField.contains(":") ) ) {
                return null;
            }
            byte[] addr = InetAddress.getByName ( ipField ).getAddress ( );
            for ( byte b : addr ) {
                if ( b != 0 ) {
                    return addr;
                }
            }
            return null; /* 0.0.0.0 or :: */
        } catch ( NumberFormatException | UnknownHostException ex ) {
            return null;
        }
    }

    private static String IPv4ToCidr24 ( String ip ) {
        return ip.substring(0,ip.lastIndexOf("."))+".*";
    }

    /* The /64 of an IPv6 address (what a single user usually has) in CIDR
       form, which both bahamut and ServicesBan match no matter how the
       address itself is written */
    private static String IPv6ToCidr64 ( byte[] addr ) {
        return String.format ( "%x:%x:%x:%x:0:0:0:0/64",
            ( ( addr[0] & 0xff ) << 8 ) | ( addr[1] & 0xff ),
            ( ( addr[2] & 0xff ) << 8 ) | ( addr[3] & 0xff ),
            ( ( addr[4] & 0xff ) << 8 ) | ( addr[5] & 0xff ),
            ( ( addr[6] & 0xff ) << 8 ) | ( addr[7] & 0xff ) );
    }        

    private static String intToIP ( long bytes )  {
        return String.format ( "%d.%d.%d.%d",   ( bytes >> 24 & 0xff ) ,  ( bytes >> 16 & 0xff ) ,  ( bytes >> 8 & 0xff ) ,  ( bytes & 0xff )  );
    }
    
    /**
     * @return true if the ircd did not tell us the IP of the user
     */
    public boolean isUnknown ( ) {
        return this.isUnknown;
    }
    
    /**
     * @return
     */
    public boolean isIPv4 ( ) {
        return this.isIPv4;
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
