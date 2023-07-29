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
package core;

import chanserv.ChanInfo;
import java.math.BigInteger;
import nickserv.NickInfo;
import operserv.Oper;
import user.User;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;

/**
 *
 * @author DreamHealer
 */
public class ServicesID extends HashNumeric {
    private long                    id;
    private BigInteger              code;
    private ArrayList<NickInfo>     niList;    /* List of identified nicks from this serviceid */
    private ArrayList<ChanInfo>     ciList;    /* List of identified chans from this serviceid */
    private Random                  rand;
    private User                    user;      /* the owner of this servicesid */
    private long                    stamp;     /* timestamp  ( seconds )  lastseen */
    private Timer                   timer;
    private Timer                   adTimer;
    
    public ServicesID ( )  {
        this.rand       = new Random ( );
        this.id         = this.getUniqueID ( );
        this.niList     = new ArrayList<>( );
        this.ciList     = new ArrayList<>( );
        this.stamp      = System.currentTimeMillis();
        HashString buf  = new HashString ( ""+this.id );
        this.code       = buf.getCode ( );
    }

    public ServicesID ( long id )  {
        this.rand       = new Random ( );
        this.id         = id;
        this.niList     = new ArrayList<>( );
        this.ciList     = new ArrayList<>( );
        this.stamp      = System.currentTimeMillis();
        HashString buf  = new HashString ( ""+this.id );
        this.code       = buf.getCode ( );
    }
     
    private long getUniqueID ( ) {
        long idVal;
        while ( true ) {
            idVal = this.rand.nextInt ( ) + (long) ( 1L << 31 );
            if ( this.isUnique (idVal ) ) {
                return idVal;
            }
        }
    }
    private boolean isUnique ( long id ) {
        HashString target = new HashString ( ""+id );
        ServicesID sid = Handler.getSIDs().get ( target.getCode() );
        if ( sid != null ) {
            return false;
        }
        return true;
    }
    
    public BigInteger getCode ( ) {
        return this.code;
    }
    
    public void updateStamp ( ) {
        this.stamp =  ( System.currentTimeMillis ( ) /1000 );
    }
    
    public boolean hasExpired ( )  {
        long dayAgo =  ( long ) ( System.currentTimeMillis ( ) /1000 ) - ( 60*60*24 );
        if ( this.user == null && this.stamp < dayAgo ) {
            return true;
        }
        return false; 
    }
    
    public void add ( NickInfo ni )  {
        if ( ni == null ) {
            return;
        }
        for ( NickInfo nick : this.niList )  {
            if ( nick.is(ni) ) {
                return;
            }
        } 
        this.niList.add ( ni );
    }
      
    public void del ( NickInfo ni )  {
        NickInfo ni2 = null;
        if ( ni == null ) {
            return;
        }
       
        for ( NickInfo nick : this.niList )  {
            if ( nick.is(ni) ) {
                ni2 = nick;
            }
        } 
        if ( ni2 != null )  {
            this.niList.remove ( ni2 ); 
        } 
    }
     
    public void add ( ChanInfo ci )  {
        if ( ci == null ) {
            return;
        }
        for ( ChanInfo chan : this.ciList )  {
            if ( chan.is(ci) ) {
                this.printSID ( );
                return;
            }
        }
        this.ciList.add ( ci );
        this.printSID ( );
    }
    
    public boolean isIdentified ( NickInfo ni )  {
        if ( ni == null ) {
            return false;
        }
        for ( NickInfo nick : this.niList )  { 
            if ( nick.is(ni) ) { 
                return true;
            }
        }
        return false;  
    }
    
    public boolean isIdentified ( ChanInfo ci )  {
        for ( ChanInfo chan : this.ciList )  {
            if ( chan.is(ci) ) {
                return true;
            }
        }
        return false;
    }
    
    public void addUser ( User user ) { 
        this.user = user; 
        this.updateStamp ( ); 
    }
    
    public void remUser ( ) { 
        this.user = null; 
        this.updateStamp ( ); 
    }
    
    public long getID ( ) {
        return this.id;
    }
    public ArrayList<NickInfo> getNiList ( ) { 
        return this.niList;
    }
    public ArrayList<ChanInfo> getCiList ( ) { 
        return this.ciList;
    }  

    public void setNiList ( ArrayList<NickInfo> niList ) { 
        this.niList = niList;
    }
    public void setCiList ( ArrayList<ChanInfo> ciList ) { 
        this.ciList = ciList;
    }  

    public void printSID ( )  {
        System.out.println ( "ServicesID ( "+this.id+" )  {" );
        System.out.println ( "    niList ( "+this.niList.size ( ) +" ) " );
        System.out.println ( "    ciList ( "+this.ciList.size ( ) +" ) " );
        if ( this.user != null )  {
            System.out.println ( "    User ( "+user.getString ( NAME ) +" ) " );
        } else {
            //System.out.println ( "    User ( NULL ) " );
        }
        System.out.println ( "}" );
    }
    
    public Oper getOper ( )  { 
        Oper oper = null;
        for ( NickInfo ni : this.niList )  {
            if ( ni.getOper ( ) != null )  {
                if ( oper != null )  {
                    if ( ni.getOper().getAccess ( )  > oper.getAccess ( ) )  {
                        oper = ni.getOper ( );
                    }
                } else {
                    oper = ni.getOper ( );
                }
            }
        }
        return oper;
    }
    
    public void addTimer ( Timer timer ) { 
        this.timer = timer;
    }
    public void addAdTimer ( Timer timer ) { 
        this.adTimer = timer;
    }
     
    public void resetTimers ( ) {
        if ( this.timer != null ) {
            this.timer.cancel ( );
        }
        if ( this.adTimer != null ) {
            this.adTimer.cancel ( );
        }
        this.timer = null;
    }

    User getUser ( ) { 
        return this.user;
    }

    public NickInfo getTopOperNick ( ) {
        NickInfo top = null;
        for ( NickInfo ni : this.niList ) {
            if ( top == null ) {
                top = ni;
            } else if ( ni.getOper().getAccess ( ) > top.getOper().getAccess ( ) ) {
                top = ni;
            }
        }
        return top;
    }
 
    public int getAccess ( ) {
        if ( this.niList.size() > 0 &&
             this.getOper ( ) != null ) {
            return this.getOper().getAccess ( );
        }
        return 0;
    }

    public void unIdentify ( ChanInfo ci ) {
        ciList.remove ( ci );
    }

    public void unIdentify ( NickInfo ni ) {
        niList.remove ( ni );
        Handler.addUpdateSID ( this );
    }
    
    /* Set 1 hour limit */
//    public void setSplitExpire ( ) {
//        this.splitExpire = ( System.currentTimeMillis ( ) + ( 1000 * 60 * 60 ) ) ;
//    }

    /* Return true if expire time hasAccess in the past */
    public boolean timeToExpire() {
        return this.hasExpired();
//        System.out.println("timeToExpire: now:"+System.currentTimeMillis()+", splitExpire:"+this.splitExpire);
//        return System.currentTimeMillis() > this.splitExpire;
    }


}
