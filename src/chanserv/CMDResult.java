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
import core.HashString;
import nickserv.NickInfo;
import user.User;

/**
 *
 * @author fredde
 */
public class CMDResult {
    private Chan chan;
    private ChanInfo ci;
    private NickInfo nick;
    private NickInfo nick2;
    private NickInfo oper;
    private User user;
    private User target;
    private HashString status;
    private HashString command;
    private HashString subcommand;
    private HashString str1;
    private HashString str2;
    private HashString commandStr;
    private HashString commandVal;
    private CSAcc acc;
    
    /**
     * CMDResult Object
     * 
     * For storing data from control checking a command
     */
    public CMDResult ( )  {
        this.chan       = null;
        this.status     = new HashString("");
        this.str1       = new HashString("");
        this.str2       = new HashString("");
        this.command    = new HashString("");
    }
    
    /**
     * Store a new channel
     * @param chan
     */
    public void setChan ( Chan chan ) { 
        this.chan = chan;
    }
    
    /**
     * Store a new ChanInfo
     * @param ci
     */
    public void setChanInfo ( ChanInfo ci ) {
        this.ci = ci;
    }
    
    /**
     * Store a new NickInfo
     * @param nick
     */
    public void setNick ( NickInfo nick ) {
        this.nick = nick; 
    }

    /**
     * Store a second NickInfo
     * @param nick2
     */
    public void setNick2 ( NickInfo nick2 ) {
        this.nick2 = nick2; 
    }

    /**
     * Store a user
     * @param user
     */
    public void setUser ( User user ) {
        this.target = user; 
    }

    /**
     * Store a target user
     * @param target
     */
    public void setTarget ( User target ) {
        this.target = target; 
    }
    
    /**
     * Store the status from parsing the command
     * @param status
     */
    public void setStatus ( HashString status ) {
        this.status = status;
    }    
    
    /**
     * Store the command
     * @param command
     */
    public void setCommand ( HashString command ) {
        this.command = command;
    }  
     
    /**
     * Store the subcommand
     * @param subcommand
     */
    public void setSubCommand ( HashString subcommand ) {
        this.subcommand = subcommand;
    }  
   
    /**
     * Store Oper NickInfo
     * @param ni
     */
    public void setOper ( NickInfo ni ) {
        this.oper = ni;
    }    
    
    /**
     * Store a string
     * @param str
     */
    public void setString1 ( String str ) {
        this.str1 = new HashString ( str );
    }
    
    /**
     * Store a HashString
     * @param str
     */
    public void setString1 ( HashString str ) {
        this.str1 = str;
    }
    
  /*  public void setString1 ( HashString str ) {
        String buf = this.str1.getString();
        if ( buf.length() > 0 ) {
            buf += ", "+str.getString();
            this.str1 = new HashString ( buf );
        } else {
            this.str1 = str;
        }
    }
    */

    /**
     * Return the stored Chan
     * @return
     */
    public Chan getChan ( ) {
        return this.chan;
    }
    
    /**
     * Return a stored ChanInfo
     * @return
     */
    public ChanInfo getChanInfo ( ) {
        return this.ci;
    }
    
    /**
     * Return a stored NickInfo
     * @return
     */
    public NickInfo getNick ( ) {
        return this.nick;
    }
         
    /**
     * Return a stored Oper NickInfo
     * @return
     */
    public NickInfo getOper ( ) {
        return this.oper;
    }
        
    /**
     * Return the second NickInfo
     * @return
     */
    public NickInfo getNick2 ( ) {
        return this.nick2;
    }
    
    /**
     * Return the target user
     * @return
     */
    public User getTarget ( ) {
        return this.target;
    }
    
    /**
     * Return a stored User
     * @return
     */
    public User getUser ( ) {
        return this.user;
    }
    
    /**
     * Return the status as HashString
     * @return
     */
    public HashString getStatus ( ) {
        return this.status;
    }
    
    /**
     * Return boolean status
     * @param it
     * @return
     */
    public boolean was ( HashString it ) {
        return this.status.is(it);
    }
    
    /**
     * Return command HashString
     * @return
     */
    public HashString getCommand ( ) {
        return this.command;
    }
    
    /**
     * Return subcommand HashString
     * @return
     */
    public HashString getSubCommand ( ) {
        return this.subcommand;
    }

    /**
     * Return stored HashString
     * @return
     */
    public HashString getString1 ( ) {
        return this.str1;
    }
     
    /**
     * Return stored secondary HashString
     * @return
     */
    public HashString getString2 ( ) {
        return this.str2;
    }
   
    /**
     * Return command string
     * @return
     */
    public HashString getCommandStr ( ) {
        return this.commandStr;
    }
    
    /**
     * Return command value HashString
     * @return
     */
    public HashString getCommandVal ( ) {
        return this.commandVal;
    }
 
    /**
     * Stores command string
     * @param string
     */
    public void setCommandStr ( HashString string ) {
        this.commandStr = string;
    }

    /**
     * Stores command value string
     * @param string
     */
    public void setCommandVal ( HashString string ) {
        this.commandVal = string;
    }
    
    /**
     * Set CSAcc object
     * @param acc
     */
    public void setAcc ( CSAcc acc ) {
        this.acc = acc;
    }
    
    /**
     * Return CSAcc object
     * @return
     */
    public CSAcc getAcc ( ) {
        return this.acc;
    }
}
