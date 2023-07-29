/* 
 * Copyright (C) 2018 Fredrik Karlsson aka DreamHealer & avade.net
 *
 * This program isSet free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program isSet distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package core;

import chanserv.ChanInfo;
import java.util.ArrayList;
import server.ServSock;
import user.User;
import java.util.Date;

/**
 *
 * @author DreamHealer
 */
 public abstract class Service extends HashNumeric {

    /**
     *
     */
    protected HashString name;

    /**
     *
     */
    protected HashString user;

    /**
     *
     */
    protected HashString host;

    /**
     *
     */
    protected HashString server;

    /**
     *
     */
    protected HashString realName;

    /**
     *
     */
    protected Date       date;

    /**
     *
     */
    protected int        hashCode;

    /**
     *
     */
    protected ArrayList<CommandInfo> cmdList;
    
    /**
     *
     * @param name
     */
    protected Service ( String name )  {
        this.name = new HashString ( name ); 
        this.init ( );
    }
    
    private void init ( )  {
        this.date       = new Date ( );
        this.user       = Proc.getConf().get ( SERVICEUSER );
        this.host       = Proc.getConf().get ( SERVICEHOST );
        this.realName   = new HashString ( this.name+" "+Proc.getConf().get ( SERVICEGCOS ) );
        this.server     = this.getServer ( );
        this.cmdList    = new ArrayList<>();
        this.send ( NICK, this.name.getString() );
    }
    
    /**
     *
     * @param it
     * @param data
     */
    public void send ( HashString it, String data )  {
        try {
            if ( it.is(RAW) ) {
                ServSock.sendCmd ( data );
            
            } else if ( it.is(NICK) ) {
                ServSock.sendCmd ( 
                    "NICK "+this.name+
                    " 1 "+
                    Math.round ( this.date.getTime ( ) / 1000 ) +
                    " + "+
                    this.user+" "+
                    this.host+" "+
                    this.server+
                    " 0 "+
                    Math.round ( this.date.getTime ( ) / 1000 ) +
                    " :"+this.realName 
                ); 
            }
        } catch ( Exception e )  {
            Proc.log ( Service.class.getName ( ) , e );
        }
    }
    
    /**
     *
     */
    public void disconnect ( )  {
        ServSock.sendCmd ( ":"+this.name+" QUIT :Disconnected" );
    }
    
    /**
     *
     * @param u
     * @return
     */
    protected boolean user ( User u ) { 
        try { 
            return  ( user != null ); 
        } catch ( Exception e )  { 
            Proc.log ( Service.class.getName ( ) , e );
        } 
        return false;
    }

    /**
     *
     * @param str
     * @return
     */
    public String getCmd ( String str )  {
        String buf;
        try {
            if (  ( buf = str.substring ( 1 )  )  != null )  {
                return buf;
            }
        } catch ( Exception e )  {
            Proc.log ( Service.class.getName ( ) , e );
        }
        return null;
    }

    /**
     *
     * @param u
     * @param msg
     */
    public void sendMsg ( User u, String msg ) { 
        this.send ( RAW, ":"+this.name+" NOTICE "+u.getNameStr()+" :"+msg );
    }
    
    /**
     *
     * @param ci
     * @param msg
     */
    public void sendOpMsg ( ChanInfo ci, String msg ) { 
        this.send ( RAW, ":"+this.name+" NOTICE @"+ci.getNameStr()+" :"+msg );
    }
       
    /**
     *
     * @param msg
     */
    public void sendGlobOp ( String msg ) { 
        this.send ( RAW, ":"+this.name+" GLOBOPS :"+msg ); 
    }
    
    /**
     *
     * @param command
     */
    public void sendCmd (  String command ) { 
        this.send ( RAW, ":"+this.name+" "+command ); 
    }

    /**
     *
     * @param u
     */
    public void accessDenied ( User u ) { 
        this.sendMsg ( u, "Access Denied.!" ); 
    }
 
    /**
     *
     * @return
     */
    public HashString getName ( ) { 
        return this.name; 
    }
    
    /**
     *
     * @return
     */
    public String getNameStr ( ) { 
        return this.name.getString(); 
    }
    
    /**
     *
     * @param size
     * @return
     */
    protected static int getIndexFromSize ( int size ) {
        return size > 5 ? 5 : size;
    }
    
    /**
     *
     * @param cmd
     */
    public void sendServ ( String cmd )  {
        if ( this.name.is(OPERSERV) ) {
            ServSock.sendCmd ( ":"+Proc.getConf().get ( STATS ) +" "+cmd );
        
        } else {
            ServSock.sendCmd ( ":"+Proc.getConf().get ( NAME ) +" "+cmd );
        }
    }

    private HashString getServer ( )  {
        if ( name.is(OPERSERV) ) {
            return Proc.getConf().get(STATS);        
        } 
        return Proc.getConf().get ( NAME ); 
    }

    /**
     *
     * @param command
     */
    public void sendRaw ( String command )  {
        this.send ( RAW, command );
    } 
    
    
    /* COMMANDS */

    /**
     *
     * @param command
     * @return
     */

    public int CMDAccess ( HashString command ) {
        return Proc.getConf().getCommandAccess ( command ); 
    }
    
    /* Returns the list of added commands with its access and info */

    /**
     *
     * @param access
     * @return
     */

    public ArrayList<CommandInfo> getCommandList ( HashString access )    { 
        ArrayList<CommandInfo> list = new ArrayList<> ( );
        for ( CommandInfo ci : cmdList )  {
            if ( ci.isAcc ( access )  )  {
                list.add ( ci );
            }
        }
        return list; 
    }
     
    /* find and return the correct commandInfo */

    /**
     *
     * @param hash
     * @return
     */

    public CommandInfo findCommandInfo ( HashString hash )  {
        for ( CommandInfo ci : this.cmdList )  {
             if ( ci.is(hash) ) {
                 return ci;
            }
        }
        return null;
    }
    
    
} 