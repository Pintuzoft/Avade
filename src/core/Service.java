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
    protected String            name;
    protected String            user;
    protected String            host;
    protected String            server;
    protected String            realName;
    protected Date              date;
    protected int               hashCode;
    protected ArrayList<CommandInfo> cmdList;
    
    
  //  protected static Log log;

    public Service ( String name )  {
        this.name       = name; 
        this.hashCode   = name.toUpperCase().hashCode ( );
        this.init ( );
    }
    
    private void init ( )  {
        this.date       = new Date ( );
        this.user       = Proc.getConf().get ( SERVICEUSER );
        this.host       = Proc.getConf().get ( SERVICEHOST );
        this.realName   = this.name+" "+Proc.getConf().get ( SERVICEGCOS );
        this.server     = this.getServer ( );
        
        System.out.println ( "DEBUG: new arraylist from super" );
        this.cmdList    = new ArrayList<>();
        this.send ( NICK, this.name );
    }
    
    public void send ( int type, String data )  {
        try {
            switch ( type )  {
                case RAW :
                    ServSock.sendCmd ( data );
                    break;
                    
                case NICK :
                    ServSock.sendCmd ( 
                            "NICK "+this.name+
                            " 1 "+
                            Math.round ( this.date.getTime ( ) /1000 ) +
                            " + "+
                            this.user+" "+
                            this.host+" "+
                            this.server+
                            " 0 "+
                            Math.round ( this.date.getTime ( ) /1000 ) +
                            " :"+this.realName ); 
                    break; 
               
                default :
                    
            }
        } catch ( Exception e )  {
            Proc.log ( Service.class.getName ( ) , e );
        }
    }
    
    public void disconnect ( )  {
        ServSock.sendCmd ( ":"+this.name+" QUIT :Disconnected" );
    }
    
    protected boolean user ( User u ) { 
        try { 
            return  ( user != null ); 
        } catch ( Exception e )  { 
            Proc.log ( Service.class.getName ( ) , e );
        } 
        return false;
    }

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

    public void sendMsg ( User u, String msg ) { 
        this.send ( RAW, ":"+this.name+" NOTICE "+u.getString ( NAME ) +" :"+msg );
    }
    
    public void sendOpMsg ( ChanInfo ci, String msg ) { 
        this.send ( RAW, ":"+this.name+" NOTICE @"+ci.getString ( NAME ) +" :"+msg );
    }
       
    public void sendGlobOp ( String msg ) { 
        this.send ( RAW, ":"+this.name+" GLOBOPS :"+msg ); 
    }
    
    public void sendCmd (  String command ) { 
        this.send ( RAW, ":"+this.name+" "+command ); 
    }

    public void accessDenied ( User u ) { 
        this.sendMsg ( u, "Access Denied.!" ); 
    }
 
    public String getName ( ) { 
        return this.name; 
    }

    protected static int getIndexFromSize ( int size ) {
        return size > 5 ? 5 : size;
    }
    
    public void sendServ ( String cmd )  {
        switch ( this.hashCode )  {
            case OPERSERV :
                ServSock.sendCmd ( ":"+Proc.getConf().get ( STATS ) +" "+cmd );
                break;

            default :
                ServSock.sendCmd ( ":"+Proc.getConf().get ( NAME ) +" "+cmd ); 

        }
    }

    private String getServer ( )  {
        switch ( this.hashCode )  {
            case OPERSERV :
                return Proc.getConf().get ( STATS );

            default :
                return Proc.getConf().get ( NAME ); 

        }
    }

    public void sendRaw ( String command )  {
        System.out.println ( "Trying: "+command );
        this.send ( RAW, command );
    } 
    
    
    /* COMMANDS */
    public int CMDAccess ( int var ) { 
        System.out.println ( "Debug CMDAccess ( "+Proc.getConf().getInt ( var )+" )" );
        return Proc.getConf().getInt ( var ); 
    }
    
    /* Returns the list of added commands with its access and info */
    public ArrayList<CommandInfo> getCommandList ( int access )    { 
        ArrayList<CommandInfo> list = new ArrayList<> ( );

        for ( CommandInfo ci : cmdList )  {
            if ( ci.isAcc ( access )  )  {
                list.add ( ci );
            }
        }
        return list; 
    }
     
    /* find and return the correct commandInfo */
    public CommandInfo findCommandInfo ( int hash )  {
        for ( CommandInfo ci : this.cmdList )  {
             if ( hash == ci.hashCode ( )  )  {
                 return ci;
            }
        }
        return null;
    }
    
    
} 