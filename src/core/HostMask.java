/* 
 * Copyright (C) 2026 Fredrik Karlsson aka DreamHealer - avade.net
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
package core;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * User host-masking, the same calculation as the bahamut module avade_uhm
 * (bahamut-module/avade_uhm.c) so services know what the ircd shows. The two
 * must never differ: change one and you change the other.
 *
 * h(x) is the first 8 hex digits of HMAC-SHA256(salt, x):
 *
 *     IPv4 a.b.c.d      h("a.b.c.d").h("a.b.c").h("a.b").ip
 *     IPv6              h(32 hex).h(first 16 hex).h(first 12 hex).ip6
 *     host with at least three labels, x.y.z.tld
 *                       prefix-h("x.y.z.tld").y.z.tld
 *     anything else     as the ip
 *
 * @author DreamHealer
 */
public class HostMask {
    private static final int    HOSTLEN = 63;       /* as in the ircd */
    private static final int    HEXLEN  = 8;
    
    private HostMask ( ) {
        /* static only */
    }
    
    /**
     * @param salt
     * @param prefix
     * @param host what the ircd calls the host of the user (name, or the ip when it did not resolve)
     * @param ip
     * @return the masked host
     */
    public static String mask ( String salt, String prefix, String host, String ip ) {
        String out;
        if ( host != null && ip != null && ! host.equalsIgnoreCase ( ip ) && ( out = maskHost ( salt, prefix, host ) ) != null ) {
            return out;
        }
        if ( ip != null && ( out = maskIp ( salt, ip ) ) != null ) {
            return out;
        }
        if ( host != null && ( out = maskIp ( salt, host ) ) != null ) {
            return out;
        }
        /* Nothing we understand: never the real host */
        return prefix+"-"+hash ( salt, host != null ? host : "" )+".unknown";
    }
    
    private static String maskHost ( String salt, String prefix, String host ) {
        if ( host.length ( ) > HOSTLEN ) {
            host = host.substring ( 0, HOSTLEN );
        }
        String lower    = asciiLower ( host );
        int dots        = 0;
        boolean name    = false;
        for ( char ch : lower.toCharArray ( ) ) {
            if ( ch == '.' ) {
                dots++;
            } else if ( ch == ':' ) {
                return null;    /* an address, not a name */
            } else if ( ch < '0' || ch > '9' ) {
                name = true;
            }
        }
        if ( dots < 2 || ! name ) {
            return null;        /* the whole name would be left in the open, or it is an ip */
        }
        String rest = lower.substring ( lower.indexOf ( '.' ) );
        if ( prefix.length ( ) + 1 + HEXLEN + rest.length ( ) > HOSTLEN ) {
            return null;
        }
        return prefix+"-"+hash ( salt, lower )+rest;
    }
    
    private static String maskIp ( String salt, String ip ) {
        if ( ip.matches ( "(0|[1-9][0-9]{0,2})(\\.(0|[1-9][0-9]{0,2})){3}" ) ) {
            String[] p = ip.split ( "\\." );
            for ( String part : p ) {
                if ( Integer.parseInt ( part ) > 255 ) {
                    return null;
                }
            }
            return hash ( salt, p[0]+"."+p[1]+"."+p[2]+"."+p[3] )+"."+
                   hash ( salt, p[0]+"."+p[1]+"."+p[2] )+"."+
                   hash ( salt, p[0]+"."+p[1] )+".ip";
        }
        if ( ! ip.contains ( ":" ) || ! ip.matches ( "[0-9A-Fa-f:.]+" ) ) {
            return null;        /* never look a name up here */
        }
        byte[] addr;
        try {
            addr = InetAddress.getByName ( ip ).getAddress ( );
        } catch ( Exception ex ) {
            return null;
        }
        if ( addr.length == 4 ) {
            /* ::ffff:a.b.c.d comes back as IPv4 from Java, the ircd keeps it as IPv6 */
            byte[] mapped = new byte[16];
            mapped[10] = (byte) 0xff;
            mapped[11] = (byte) 0xff;
            System.arraycopy ( addr, 0, mapped, 12, 4 );
            addr = mapped;
        }
        StringBuilder hex = new StringBuilder ( );
        for ( byte b : addr ) {
            hex.append ( String.format ( "%02x", b ) );
        }
        return hash ( salt, hex.toString ( ) )+"."+
               hash ( salt, hex.substring ( 0, 16 ) )+"."+
               hash ( salt, hex.substring ( 0, 12 ) )+".ip6";
    }
    
    /* First 8 hex digits of HMAC-SHA256(salt, data) */
    private static String hash ( String salt, String data ) {
        try {
            Mac mac = Mac.getInstance ( "HmacSHA256" );
            mac.init ( new SecretKeySpec ( salt.getBytes ( StandardCharsets.ISO_8859_1 ), "HmacSHA256" ) );
            byte[] md = mac.doFinal ( data.getBytes ( StandardCharsets.ISO_8859_1 ) );
            StringBuilder hex = new StringBuilder ( );
            for ( int i = 0; i < HEXLEN / 2; i++ ) {
                hex.append ( String.format ( "%02x", md[i] ) );
            }
            return hex.toString ( );
        } catch ( Exception ex ) {
            throw new IllegalStateException ( ex );
        }
    }
    
    private static String asciiLower ( String str ) {
        char[] chars = str.toCharArray ( );
        for ( int i = 0; i < chars.length; i++ ) {
            if ( chars[i] >= 'A' && chars[i] <= 'Z' ) {
                chars[i] = (char) ( chars[i] + 32 );
            }
        }
        return new String ( chars );
    }
}
