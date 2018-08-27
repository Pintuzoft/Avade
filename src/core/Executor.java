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

/**
 *
 * @author DreamHealer
 */
public abstract class Executor extends HashNumeric {
    protected Service       service;
    protected boolean       found;
    protected CommandInfo   cmdInfo;
    
    public Executor ( )  {  } 

    public void noMatch ( User user, String str )  { 
        this.service.sendMsg ( user, "Sorry, no such command: "+str ); 
    } 
    
    /* Return true if cmd string is shorter than minlen */
    protected boolean isShorterThanLen ( int minlen, String[] cmd )  {
        return cmd.length < minlen;
    }
    
}