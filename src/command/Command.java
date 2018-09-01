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

import core.HashNumeric;

/**
 *
 * @author DreamHealer
 */
public class Command extends HashNumeric {
    private String      id;
    private int         hash;
    private Object      target;
    private int         targetType;
    private int         command;
    private String      extra;
    
    public Command ( String id, Object target, int targetType, int command, String extra )  {
        this.id = id;
        this.hash = id.hashCode ( );
        this.target = target;
        this.targetType = targetType;
        this.command = command;
        this.extra = extra;
    }

    public String getID ( ) {
        return this.id;
    }
    
    public int getHashCode ( ) {
        return this.hash;
    }
    
    public Object getTarget ( ) {
        return this.target;
    }
    
    public int getTargetType ( ) { 
        return this.targetType;
    }
    
    public int getCommand ( ) {
        return this.command;
    } 

    public String getExtra() {
        return extra;
    }
    
}
