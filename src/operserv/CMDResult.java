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

import core.HashString;
import java.util.ArrayList;
import nickserv.*;

/**
 *
 * @author fredde
 */
public class CMDResult {
    private NickInfo        nick;
    private HashString      status;
    private String          str1;
    private String          str2;
    private String          str3;
    private String          str4;
    private HashString      command;
    private HashString      sub;
    private HashString      sub2;
    private ServicesBan     ban;
    private NetServer       server1;
    private String[]        cmd;
    private ArrayList<String> makill = new ArrayList<>();
    
    public CMDResult ( )  {
        this.nick       = null;
        this.status     = new HashString ( "" );
    }
    
    public void setNick ( NickInfo nick ) { 
        this.nick = nick;
    }
    
    public void setString1 ( String str ) { 
        this.str1 = str;
    }
       
    public void setString2 ( String str ) { 
        this.str2 = str;
    }
       
    public void setString3 ( String str ) { 
        this.str3 = str;
    }
       
    public void setString4 ( String str ) { 
        this.str4 = str;
    }
    
    public void setStatus ( HashString status ) { 
        this.status = status;
    }    
    
    public NickInfo getNick ( ) { 
        return this.nick;
    }
    
    public HashString getStatus ( ) { 
        return this.status;
    }
    
    public String getString1 ( ) { 
        return this.str1;
    }
    
    public String getString2 ( ) { 
        return this.str2;
    }
    public String getString3 ( ) { 
        return this.str3;
    }
    public String getString4 ( ) { 
        return this.str4;
    }

    public HashString getCommand ( ) {
        return this.command;
    }

    public void setCommand ( HashString command ) {
        this.command = command;
    }

    public HashString getSub ( ) {
        return sub;
    }

    public void setSub ( HashString sub ) {
        this.sub = sub;
    }

    public HashString getSub2 ( ) {
        return sub2;
    }

    public void setSub2 ( String sub2 ) {
        setSub2 ( new HashString ( sub2) );
    }
    
    public void setSub2 ( HashString sub2 ) {
        this.sub2 = sub2;
    }
       
    public ServicesBan getServicesBan ( ) {
        return this.ban;
    }

    public void setServicesBan ( ServicesBan ban ) {
        this.ban = ban;
    }

    public void setServer ( NetServer server ) {
        this.server1 = server;
    }
    
    public NetServer getServer ( ) {
        return this.server1;
    }
    
    public void setCmd ( String[] cmd ) {
        this.cmd = cmd;
    }
    
    public String[] getCmd ( ) {
        return this.cmd;
    }
    
    /* MAKILL */
    public void addMAKill ( String ban ) {
        this.makill.add ( ban );
    }
    
    public ArrayList<String> getMAkill ( ) {
        return this.makill;
    }
    
    public boolean is ( HashString status ) {
        return this.status.is(status);
    }
}
