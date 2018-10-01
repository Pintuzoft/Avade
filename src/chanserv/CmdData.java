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
package chanserv;

import channel.Chan;
import nickserv.NickInfo;
import user.User;

/**
 *
 * @author fredde
 */
public class CmdData {
    private Chan chan;
    private ChanInfo ci;
    private NickInfo nick;
    private NickInfo nick2;
    private NickInfo oper;
    private User user;
    private User target;
    private int status;
    private int command;
    private String str1;
    private String str2;
    private String commandStr;
    private String commandVal;
    
    public CmdData ( )  {
        this.chan       = null;
        this.status     = -1;
        this.str1       = "";
        this.str2       = "";
        this.command    = 0;
    }
    
    public void setChan ( Chan chan ) { 
        this.chan = chan;
    }
    
    public void setChanInfo ( ChanInfo ci ) {
        this.ci = ci;
    }
    
    public void setNick ( NickInfo nick ) {
        this.nick = nick; 
    }

    public void setNick2 ( NickInfo nick2 ) {
        this.nick2 = nick2; 
    }

    public void setUser ( User user ) {
        this.target = user; 
    }
    public void setTarget ( User target ) {
        this.target = target; 
    }
    
    public void setStatus ( int status ) {
        this.status = status;
    }    
    
    public void setCommand ( int command ) {
        this.command = command;
    }  
   
    public void setOper ( NickInfo ni ) {
        this.oper = ni;
    }    
    
    public void setString1 ( String str )  { 
        if  ( this.str1.length ( )  > 0 )  {
            this.str1 += ", "+str;
        } else {
            this.str1     = str;
        }
    }
    
    public Chan getChan ( ) {
        return this.chan;
    }
    
    public ChanInfo getChanInfo ( ) {
        return this.ci;
    }
    
    public NickInfo getNick ( ) {
        return this.nick;
    }
         
    public NickInfo getOper ( ) {
        return this.oper;
    }
        
    public NickInfo getNick2 ( ) {
        return this.nick2;
    }
    
    public User getTarget ( ) {
        return this.target;
    }
    
    public User getUser ( ) {
        return this.user;
    }
    
    public int getStatus ( ) {
        return this.status;
    }
    
    public int getCommand ( ) {
        return this.command;
    }

    public String getString1 ( ) {
        return this.str1;
    }
     
    public String getString2 ( ) {
        return this.str2;
    }
   
    public String getCommandStr ( ) {
        return this.commandStr;
    }
    
    public String getCommandVal ( ) {
        return this.commandVal;
    }
 
    void setCommandStr(String string) {
        this.commandStr = string;
    }

    void setCommandVal(String string) {
        this.commandVal = string;
    }
}
