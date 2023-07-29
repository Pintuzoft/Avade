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

import server.Numeric;

/**
 *
 * @author DreamHealer
 */
public class TextFormat extends Numeric {
    public char         bold       = 0x02;
    public char         underline  = 0x1f;
    public char         reversed   = 0x16;
    public TextFormat ( )  {
        /* nothingness */
    }

    public char b ( ) { 
        return this.bold;
    }
    public char u ( ) { 
        return this.underline;
    }
    public char r ( ) { 
        return this.reversed;
    }
}
