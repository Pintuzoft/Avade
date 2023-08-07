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
package user;

import channel.Chan;
import chanserv.ChanInfo;
import core.Handler;
import core.ServicesID;
import core.HashNumeric;
import core.HashString;
import nickserv.NickInfo;
import operserv.Oper;
import server.Server;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author DreamHealer
 */
public class User extends HashNumeric {

    private HashString                  name;
    private HashString                  mask;           
    private HashString                  user;
    private HashString                  gcos;
    private Server                      server;
    private long                        signOn;
    private Date                        date;
    private int                         state; 
    private UserMode                    modes; 
    private ArrayList<Chan>             cList;
    private ServicesID                  sid;
    private HostInfo                    hi;
    
    private UserFlood                   flood;
        
    /**
     *
     * @param data
     */
    public User ( String[] data )  {
        // NICK NickServ 1 1320454528 + service sshd.biz services.sshd.biz 0 1320454528 :NickServ Services
        // 0    1        2 3          4 5       6        7                 8 9          9         10
        long sidBuf;
        this.name       = new HashString ( data[1] );
        this.user       = new HashString ( data[5] ); 
        this.hi         = new HostInfo ( Long.parseLong ( data[9] ), data[6] ); 
        
        this.mask       = new HashString ( this.user+"@"+this.getHost ( ) ); 
        this.server     = Handler.findServer ( data[7] );
        this.date       = new Date ( );
        this.state      = 0; 
        sidBuf          = Long.parseLong ( data[8] ); /* buffer */ 
        this.signOn     = Long.parseLong ( data[3] ); 
        this.modes      = new UserMode ( );
        this.modes.set ( SERVER, data );
        this.cList      = new ArrayList<> ( );
        
        data[10] = data[10].substring(1);
        String buf = "";
        for ( int index=10;  index < data.length; index++ )  {
            if ( buf.isEmpty ( )  )  {
                buf = data[index];
            } else {
                buf += " "+data[index];
            }
        }
        this.gcos = new HashString ( buf );
        this.flood = new UserFlood ( this );
    }

    /**
     *
     * @param code
     */
    public User ( HashString code )  {
        this.name = code;
    }
  
    /**
     *
     */
    public void serverConnect ( )  {
        this.server.addUser ( this ); /* We are connected so lets add ourself to the server */ 
    }
 
    /**
     *
     * @param in
     * @return
     */
    public String getString ( HashString in )  {
        if ( in.is(NAME) ) {
            return this.name.getString();
        
        } else if ( in.is(USER) ) {
            return this.user.getString();
        
        } else if ( in.is(REALNAME) ) {
            return this.gcos.getString();
        
        } else if ( in.is(HOST) ) {
            return this.getHost();
        
        } else if ( in.is(REALHOST) ) {
            return this.getRealHost();
        
        } else if ( in.is(IP) ) {
            return this.getIp();
        
        } else if ( in.is(FULLMASK) ) {
            return this.name+"!"+this.user+"@"+this.getHost();
        }
        return null;
    }
   
    /* Make hostinfo object transparent */

    /**
     *
     * @return
     */

    public String getHost ( ) { 
        return this.hi.getHost ( );
    }

    /**
     *
     * @return
     */
    public HostInfo getHostInfo ( ) { 
        return this.hi;
    }
       
    /**
     *
     * @param ipHash
     * @return
     */
    public boolean ipMatch ( int ipHash ) { 
        return this.hi.ipMatch ( ipHash );
    }

    /**
     *
     * @param ipHash
     * @return
     */
    public boolean rangeMatch ( int ipHash ) { 
        return this.hi.rangeMatch ( ipHash );
    }
    
    /**
     *
     * @return
     */
    public String getIp ( ) { 
        return this.hi.getIp ( );
    }
    
    private String getRealHost ( ) {
        return this.hi.getRealHost ( );
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
     * @return
     */
    public HashString getUser ( ) {
        return this.user;
    }
     
    /**
     *
     * @return
     */
    public UserMode getModes ( ) { 
        return this.modes;
    }
    
    /**
     *
     * @return
     */
    public Server getServ ( ) { 
        return this.server;
    }

    /**
     *
     * @param in
     */
    public void setName ( String in ) {
        HashString nameHash = new HashString ( in );
        if ( ! this.name.is(nameHash) ) {
            this.getSID().resetTimers ( );
        }
        this.name = nameHash;
    } /* /nick */
     
    /**
     *
     * @param data
     */
    public void setMode ( String[] data ) { 
        this.modes.set ( MODE, data );
    }

    /**
     *
     * @param chan
     */
    public void addChan ( Chan chan ) { 
        this.cList.add ( chan );
    }

    /**
     *
     * @param chan
     */
    public void remChan ( Chan chan ) { 
        this.cList.remove ( chan );
    }

    /**
     *
     * @return
     */
    public boolean isOper ( ) {  
        return this.modes.is ( OPER );
    }

    /**
     *
     */
    public void partAll ( )  {
        /* We have to flush the user out from all channels */
        if ( !this.cList.isEmpty() )  {
            for ( Chan c : this.cList )  {
                c.remUser ( this );
                Handler.deleteEmpty(c);
            }
            this.cList.clear ( );
        }
    }

    /**
     *
     * @param ni
     * @return
     */
    public boolean isIdented ( NickInfo ni ) {
        if ( ni != null && this.sid != null ) {
            return this.sid.isIdentified ( ni );
        }
        return false;
    }     

    /**
     *
     * @param ci
     * @return
     */
    public boolean isIdented ( ChanInfo ci ) {
        if ( ci != null && this.sid != null ) {
            return this.sid.isIdentified ( ci );
        }
        return false;
    } 
    
    /**
     *
     * @return
     */
    public String getFullMask ( ) { 
        return this.name+"!"+this.user+"@"+this.getHost ( ); 
    } 
    
    /**
     *
     */
    public void quitServer ( ) { 
        this.server.remUser ( this );
    }

    /**
     *
     * @return
     */
    public String getIDNicks ( )  {
        String buf = "";
        buf = "";
 
        if ( this.sid == null )  {
            this.sid = new ServicesID ( );
        }
        for ( NickInfo ni : this.sid.getNiList ( )  )  {
            buf +=  ( buf.isEmpty ( ) ?"":" " ) +ni.getString ( NAME );
        }
        
        return buf;
    }
    
    /**
     *
     * @return
     */
    public String getIDChans ( )  {
        String buf = "";
        buf = ""; 
        if ( this.sid == null )  {
            this.sid = new ServicesID ( );
        }
        for ( ChanInfo ci : this.sid.getCiList ( )  )  {
            buf +=  ( buf.isEmpty ( ) ?"":" " ) +ci.getString ( NAME );
        }
        return buf;
    }

    /**
     *
     * @param ci
     */
    public void unIdentify ( ChanInfo ci ) {
        if ( this.sid == null )  {
            this.sid = new ServicesID ( );
        }
        this.sid.unIdentify ( ci );
    }

    /**
     *
     * @param ni
     */
    public void unIdentify ( NickInfo ni ) {
        if ( this.sid == null )  {
            this.sid = new ServicesID ( );
        }
        this.sid.unIdentify ( ni );
    }
    

    /* Attached user to an existing sid*/

    /**
     *
     * @param sid
     */

    public void attachSid ( ServicesID sid )    {         
        this.sid = sid; 
    }

    
    
    /* Return sid */

    /**
     *
     * @return
     */

    public ServicesID getSID ( )  { 
        return this.sid; 
    }
    
    /* Create new sid */
//    private void newSID ( )         { this.sid = new ServicesID ( ); }

    /**
     *
     * @return
     */
    
    public int getState ( ) { 
        return this.state;
    }

    /**
     *
     */
    public void setState ( ) {
        this.state++;
    }

    /**
     *
     */
    public void resetState ( ) {
        this.state = 0;
    }

 /*   private boolean noSID ( )  {
        return  ( this.sid == null );
    }

    public void fixSID ( )  {
        if ( this.noSID ( )  )  {
            this.newSID ( );
        }
    } 
*/

    /**
     *
     * @param access
     * @return
     */


    public boolean hasAccess ( HashString access )  {
        int numacc = Oper.hashToAccess ( access );
        for ( NickInfo ni : this.sid.getNiList() ) {
            if ( ni.getOper().getAccess() >= numacc ) {
                return true;        
            }
        }
        return false;
    }
    
    /**
     *
     * @param user
     * @return
     */
    public boolean is ( User user ) {
        return this.name.is(user.getName());
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
     * @param ni
     * @return
     */
    public boolean is ( NickInfo ni ) {
        return this.name.is(ni);
    }
    
    /**
     *
     * @return
     */
    public ArrayList<Chan> getChans ( )  {
        return this.cList;
    }
   
    /* User has oper object and access higher or equal to specific access */

    /**
     *
     * @param access
     * @return
     */

    public boolean isAtleast ( HashString access )  {
        if ( this.getOper() == null ) {
            return false;
        }
        return ( this.isOper() && this.getOper().isAtleast ( access ) );
    } 
  
    /**
     *
     * @return
     */
    public int getAccess ( ) {
        if ( this.sid == null ) {
            System.out.println ( "DEBUG!!: getaccess().sid:null" );
        } else {
            System.out.println ( "DEBUG!!: getaccess().sid:!null" );
        }
         
        if ( this.sid == null ) {
            this.sid = new ServicesID ();
        }
        return this.sid.getAccess ( );
    }

    /**
     *
     * @return
     */
    public Oper getOper ( ) {
        return this.sid.getOper ( );
    }
    
    /**
     *
     * @return
     */
    public UserFlood getUserFlood ( ) {
        return this.flood;
    }

    /**
     *
     */
    public void secMaintenence() {
        this.flood.maintenence();
    }

    /**
     *
     * @param sid
     */
    public void setSID ( ServicesID sid ) {
        this.sid = sid;
    }
    
    /**
     *
     * @return
     */
    public HashString getMask ( ) {
        return this.mask;
    }

    /**
     *
     * @param mode
     * @param state
     */
    public void setMode(HashString mode, boolean state) {
        this.getModes().set ( mode, state );
    }
}
