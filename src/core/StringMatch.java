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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 *
 * @author DreamHealer
 */
public class StringMatch { 
    
    /* Compiled wildcard patterns, bans are matched against every user */
    private static final int CACHE_SIZE = 2048;
    private static final Map<String,Pattern> cache = new LinkedHashMap<String,Pattern>( 256, 0.75f, true ) {
        @Override
        protected boolean removeEldestEntry ( Map.Entry<String,Pattern> eldest ) {
            return this.size ( ) > CACHE_SIZE;
        }
    };

    /**
     * Wildcard match in one direction: * matches anything, ? one character,
     * everything else is literal. Case insensitive like the ircd.
     * @param str the data (nick, mask, realname..)
     * @param wild the pattern
     * @return
     */
    public static boolean matches ( String str, String wild ) {
        if ( str == null || wild == null ) {
            return false;
        }
        return toPattern ( wild ).matcher ( str ).matches ( );
    }

    private static Pattern toPattern ( String wild ) {
        Pattern p = cache.get ( wild );
        if ( p == null ) {
            StringBuilder regex = new StringBuilder ( );
            StringBuilder literal = new StringBuilder ( );
            for ( char ch : wild.toCharArray ( ) ) {
                if ( ch == '*' || ch == '?' ) {
                    if ( literal.length ( ) > 0 ) {
                        regex.append ( Pattern.quote ( literal.toString ( ) ) );
                        literal.setLength ( 0 );
                    }
                    regex.append ( ch == '*' ? ".*" : "." );
                } else {
                    literal.append ( ch );
                }
            }
            if ( literal.length ( ) > 0 ) {
                regex.append ( Pattern.quote ( literal.toString ( ) ) );
            }
            p = Pattern.compile ( regex.toString ( ), Pattern.CASE_INSENSITIVE | Pattern.DOTALL );
            cache.put ( wild, p );
        }
        return p;
    }

    /**
     * Either side may be a wildcard mask (used to compare bans with bans)
     * @param fullmask
     * @param wild
     * @return
     */
    public static boolean maskWild ( HashString fullmask, String wild ) {
        return maskWild ( fullmask.getString(), wild);
    }

    /**
     * Either side may be a wildcard mask (used to compare bans with bans)
     * @param fullmask
     * @param wild
     * @return
     */
    public static boolean maskWild ( String fullmask, String wild )  { 
        return matches ( fullmask, wild ) || matches ( wild, fullmask );
    }

    /**
     * Either side may contain wildcards
     * @param nick
     * @param wild
     * @return
     */
    public static boolean nickWild ( String nick, String wild ) {
        return matches ( nick, wild ) || matches ( wild, nick );
    }

    /**
     * Either side may contain wildcards
     * @param str
     * @param wild
     * @return
     */
    public static boolean wild ( String str, String wild ) {
        return matches ( str, wild ) || matches ( wild, str );
    }

    /**
     *
     * @param str
     * @return
     */
    public static boolean isInt ( String str ) {
        return str.matches ( "[0-9]+" );
    }
}