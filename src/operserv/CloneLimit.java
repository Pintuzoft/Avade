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
package operserv;

/**
 * A network wide clone limit for a host (1.2.3.4 or an IPv6 address) or a
 * site (1.2.3.*), sent to the ircd with SVSCLONE.
 *
 * @author DreamHealer
 */
public class CloneLimit {
    private final String mask;
    private final int limit;
    private final String reason;
    private final String instater;
    private final String stamp;

    /**
     * @param mask
     * @param limit
     * @param reason
     * @param instater
     * @param stamp
     */
    public CloneLimit ( String mask, int limit, String reason, String instater, String stamp ) {
        this.mask = mask;
        this.limit = limit;
        this.reason = reason;
        this.instater = instater;
        this.stamp = stamp;
    }

    /**
     * Check a host or site mask the way bahamut stores them
     * @param mask
     * @return true if it is 1.2.3.4, 1.2.3.* or an IPv6 address
     */
    public static boolean validMask ( String mask ) {
        if ( mask == null ) {
            return false;
        }
        String octet = "(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])";
        if ( mask.matches ( octet+"\\."+octet+"\\."+octet+"\\.("+octet+"|\\*)" ) ) {
            return true;
        }
        return mask.contains ( ":" ) && mask.matches ( "[0-9a-fA-F:.]{2,45}" );
    }

    public String getMask ( )       { return this.mask;     }
    public int getLimit ( )         { return this.limit;    }
    public String getReason ( )     { return this.reason;   }
    public String getInstater ( )   { return this.instater; }
    public String getStamp ( )      { return this.stamp;    }
}
