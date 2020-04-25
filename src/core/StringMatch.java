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

/**
 *
 * @author DreamHealer
 */
public class StringMatch { 
    public static boolean maskWild ( HashString fullmask, String wild ) {
        return maskWild ( fullmask.getString(), wild);
    }
    public static boolean maskWild ( String fullmask, String wild )  { 
        return fullmask.matches ( wild.replace("?","(.)").replace("!","([!])").replace("@","([@])").replace("*","(.*)")) ||
               wild.matches ( fullmask.replace("?","(.)").replace("!","([!])").replace("@","([@])").replace("*","(.*)") );
    }
    
    public static boolean nickWild ( String nick, String wild ) {
        return nick.matches ( wild.replace("?","(.)").replace("*","(.*)") ) ||
                wild.matches ( nick.replace("?","(.)").replace("*","(.*)") );
    }
    
    public static boolean wild ( String str, String wild ) {
        return str.matches ( wild.replace("?","(.)").replace("*","(.*)") ) ||
                wild.matches (str.replace("?","(.)").replace("*","(.*)") );
    }
    
    public static boolean isInt ( String str ) {
        return str.matches ( "[0-9]+" );
    }
}