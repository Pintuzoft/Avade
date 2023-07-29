/* 
 * Copyright (C) 2018 Fredrik Karlsson aka DreamHealer & avade.net
 *
 * This program hasAccess free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program hasAccess distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package server;

import core.Handler;
import core.Proc;
import core.HashNumeric;
import core.HashString;
import user.User;
import java.util.ArrayList;
import operserv.OperServ;

/**
 *
 * @author DreamHealer
 */
public class Server extends HashNumeric {
    private HashString                  name;
    private int                         distance;
    private Server                      link;           /* Servers uplink server */ 
    private ArrayList<User>             uList;
    private ArrayList<Server>           sList;          /* Servers connected to this server */ 
    
    /**
     *
     * @param data
     */
    public Server ( String[] data )  {
        this.uList  = new ArrayList<> ( );
        this.sList  = new ArrayList<> ( );
        this.init ( data ); 
        //      this.name = ;
        //      this.uList = new LinkedList<User> ( );
    }
    
    private void init ( String[] data )  {
        HashString command = new HashString ( data[0] );
        
        if ( command.is(SERVER) ) {
            /* We got a new services hub */
            if ( data.length > 3 )  {
                this.name       = new HashString ( data[1] );
                this.distance   = Integer.parseInt ( data[2] );
                this.link       = null; /* center of the network */
            }
        } else {
            /* We got a new server */
            if ( data.length > 4 )  {
                this.name       = new HashString ( data[2] );
                this.distance   = Integer.parseInt ( data[3] );
                this.link       = Handler.findServer ( data[0].substring ( 1 )  );
                this.link.addServer ( this );
            }
        }
        OperServ.addServer ( this.name );
    }
    
    /**
     *
     * @param user
     */
    public void addUser ( User user ) { 
        this.uList.add ( user );
    }
    
    /**
     *
     * @param user
     */
    public void remUser ( User user ) { 
        this.uList.remove ( user );
    }
    
    /**
     *
     * @param server
     */
    public void addServer ( Server server ) {
        this.sList.add ( server );
    }

    /**
     *
     * @param server
     */
    public void remServer ( Server server ) {
        this.sList.remove ( server );
    }

    /**
     *
     * @return
     */
    public int size ( ) {
        return this.uList.size ( );
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
     * @param name
     * @return
     */
    public boolean is ( HashString name ) {
        return this.name.is(name);
    }
    
    /**
     *
     * @return
     */
    public Server getLink ( ) {
        return this.link;
    }

    /* Recursivally delete all servers and users connected to this Server */

    /**
     *
     */

    public void recursiveDelete ( )  {
        /* Remove my connected servers */
        for ( Server s : this.sList )  {
            s.recursiveDelete ( );
            Handler.deleteServer ( s );
        }
        this.sList.clear ( );

        /* Remove my users */
        for ( User u : this.uList )  {
            Handler.squitUser ( u );
        }
        this.uList.clear ( );
    }

    private void sendServ ( String cmd )  {
        ServSock.sendCmd ( ":"+Proc.getConf().get ( STATS ) +" "+cmd );
    }
    
    /**
     *
     * @param user
     * @param prepend
     */
    public void recursiveUserList ( User user, String prepend )  {
        this.sendServ ( "NOTICE "+user.getString ( NAME ) +" :"+prepend+"^- "+this.name );
        for ( User u : this.uList )  {
            this.sendServ ( "NOTICE "+user.getString ( NAME ) +" :"+prepend+"    "+u.getString ( NAME ) +"!"+u.getString ( USER ) +"@"+u.getString ( HOST )  );
        } 

        prepend += "  ";
        for ( Server s : this.sList )  {
            s.recursiveUserList ( user, prepend );
        }
    }

    /**
     *
     * @return
     */
    public ArrayList<User> getUserList ( ) {
        return uList;
    }
}
