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
package user;

import channel.Chan;
import chanserv.ChanInfo;
import core.Handler;
import core.ServicesID;
import core.HashNumeric;
import nickserv.NickInfo;
import nickserv.NickServ;
import operserv.Oper;
import server.Server;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author DreamHealer
 */
public class User extends HashNumeric {

    private String                      name;
    private int                         hashName;           /* integer representation of nickname */
    private int                         hashMask;           /* integer representation of user@host */
    private String                      user;
//    private String                    host;
    private String                      realName;
//    private String                    realHost;
//    private String                    ip;
    private Server                      server;
 //   private Oper                        oper; 
    private long                        signOn;
    private Date                        date;
    private int                         state; 
    private UserMode                    modes; 
    private ArrayList<Chan>             cList;
    private ServicesID                  sid;
    private HostInfo                    hi;
    
    private UserFlood                   flood;
     
    /* STATIC */
    //public static Comparator<User>      comparator =  ( User u1, User u2 )  -> { return u1.hashCode ( )  - u2.hashCode ( ); };
    
    public User ( String[] data )  {
        // NICK NickServ 1 1320454528 + service sshd.biz services.sshd.biz 0 1320454528 :NickServ Services
        // 0    1        2 3          4 5       6        7                 8 9          9         10
        long sidBuf;
        this.name       = data[1];
        this.hashName   = this.name.toUpperCase ( ) .hashCode ( );
        this.user       = data[5]; 
        this.hi         = new HostInfo ( Long.parseLong ( data[9] ) ,data[6] ); 
       // this.host       = data[6];
        this.hashMask   =  ( this.user+"@"+this.getHost ( )  ) .toUpperCase ( ) .hashCode ( ); 
        this.server     = Handler.findServer ( data[7] );
        this.date       = new Date ( );
        this.state      = 0; 
        sidBuf          = Long.parseLong ( data[8] ); /* buffer */ 
        this.signOn     = Long.parseLong ( data[3] ); 
    //    this.oper       = new Oper ( ); /* Create oper object with user access */
        this.modes      = new UserMode ( );
        this.modes.set ( SERVER, data );
        this.cList      = new ArrayList<> ( );

        this.realName   = new String ( );
        
        data[10] = data[10].substring(1);
        
        for ( int index=10;  index < data.length; index++ )  {
            if ( this.realName.isEmpty ( )  )  {
                this.realName = data[index];
            } else {
                this.realName += " "+data[index];
            }
        }
      
        //this.ip         = this.intToIP ( Long.parseLong ( data[9] )  );
        
        //this.realHost   = this.ipToHost ( );
        
/*        if ( sidBuf > 999 )  {
            this.sid = Handler.findSid ( sidBuf );
            if ( this.sid == null )  {
                this.sid = new ServicesID ( sidBuf );
            }
        } else {
            this.sid = new ServicesID ( );
        }
  */       
        this.flood = new UserFlood ( this );
        /* identify user if +r is set */
/*        if ( this.modes.is ( IDENT )  )  {
            NickInfo ni = NickServ.findNick ( this.name );
            this.sid.addUser ( this );
            this.sid.add ( ni );
        } */
    //    this.sid.printSID();
       
    }

    public User ( int code )  {
        this.hashName = code;
    }
  
    public void serverConnect ( )  {
        this.server.addUser ( this ); /* We are connected so lets add ourself to the server */ 
    }
 
    public String getString ( int var )  {
        switch ( var )  {
            case NAME :
                return this.name;
                
            case USER :
                return this.user;
                
            case REALNAME :
                return this.realName;
                
            case HOST :
                return this.getHost ( );
                
            case REALHOST :
                return this.getRealHost ( );
                
            case IP :
                return this.getIp ( );
                
            case FULLMASK :
                return this.name+"!"+this.user+"@"+this.getHost();
                
            default: 
                return null;
            
        } 
    }
   
    /* Make hostinfo object transparent */
    private String getHost ( ) { 
        return this.hi.getHost ( );
    }
    
    private String getIp ( ) { 
        return this.hi.getIp ( );
    }
    
    private String getRealHost ( ) {
        return this.hi.getRealHost ( );
    }
    
    public UserMode getModes ( ) { 
        return this.modes;
    }
    
    public Server getServ ( ) { 
        return this.server;
    }

    public void setName ( String name )     { 
        if ( name.toUpperCase ( ) .hashCode ( )  != this.hashName )  {
            this.getSID ( ) .resetTimers ( );
        } 
        this.name = name; 
        this.hashName = this.name.toUpperCase ( ) .hashCode ( );
        
    } /* /nick */
     
    public void setMode ( String[] data ) { 
        this.modes.set ( MODE, data );
    }

    public void addChan ( Chan chan ) { 
        this.cList.add ( chan );
    }

    public void remChan ( Chan chan ) { 
        this.cList.remove ( chan );
    }

    public boolean isOper ( ) {  
        return this.modes.is ( OPER );
    }

  
    public void partAll ( )  {
        /* We have to flush the user out from all channels */
        if ( this.cList.size ( ) >0 )  {
            for ( Chan c : this.cList )  {
                c.remUser ( this );
            }
            this.cList.clear ( );
        }
    }

    public boolean isIdented ( NickInfo ni )    { 
        return  (  ( ni != null && this.sid != null ) ? this.sid.isIdentified ( ni ) : false ); 
    }     

    public boolean isIdented ( ChanInfo ci ) { 
        return  ( ( ci != null && this.sid != null ) ? this.sid.isIdentified ( ci ) : false ); 
    } 
    
    public String getFullMask ( ) { 
        return this.name+"!"+this.user+"@"+this.getHost ( ); 
    } 
    
    public void quitServer ( ) { 
        this.server.remUser ( this );
    }

    public String getIDNicks ( )  {
        String buf = new String ( );
        buf = "";
 
        if ( this.sid != null )  {
            for ( NickInfo ni : this.sid.getNiList ( )  )  {
                buf +=  ( buf.isEmpty ( ) ?"":" " ) +ni.getString ( NAME );
            }
        }
        return buf;
    }
    
    public String getIDChans ( )  {
        String buf = new String ( );
        buf = ""; 
        
        if ( this.sid != null )  {
            for ( ChanInfo ci : this.sid.getCiList ( )  )  {
                buf +=  ( buf.isEmpty ( ) ?"":" " ) +ci.getString ( NAME );
            }
        }
        return buf;
    }


    public void unIdentify ( ChanInfo ci ) {
        if ( this.sid != null ) {
            this.sid.unIdentify ( ci );
        }
    }

    public void unIdentify(NickInfo ni) {
        if ( this.sid != null ) {
            this.sid.unIdentify ( ni );
        }
    }
    

    /* Attached user to an existing sid*/
    public void attachSid ( ServicesID sid )    {         
        this.sid = sid; 
    }

    
    
    /* Return sid */
    public ServicesID getSID ( )  { 
  //      this.fixSID ( );
        return this.sid; 
    }
    
    /* Create new sid */
//    private void newSID ( )         { this.sid = new ServicesID ( ); }
    
    public int getState ( ) { 
        return this.state;
    }
    public void setState ( ) {
        this.state++;
    }
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
    public int getHash ( )  {
       return this.hashName;
    }
 

    public boolean is ( int access )  {
        int numacc = Oper.hashToAccess ( access );
        for ( NickInfo ni : this.sid.getNiList() ) {
            if ( ni.getOper().getAccess() >= numacc ) {
                return true;        
            }
        }
        return false;
    }
    
    public ArrayList<Chan> getChans ( )  {
        return this.cList;
    }
   
    public int getHashMask ( ) { 
        return this.hashMask;
    }
  
    /* User has oper object and access higher or equal to specific access */
    public boolean isAtleast ( int access )  {
        if ( this.getOper() == null ) {
            return false;
        }
        return ( this.isOper() && this.getOper().isAtleast ( access ) );
    } 
  
    @Override
    public int hashCode ( ) {
        return this.hashName;
    }

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
    public Oper getOper ( ) {
        return this.sid.getOper ( );
    }
    
    public UserFlood getUserFlood ( ) {
        return this.flood;
    }

    public void secMaintenence() {
        this.flood.maintenence();
    }

    public void setSID ( ServicesID sid ) {
        this.sid = sid;
    }
    
}
