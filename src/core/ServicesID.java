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
import java.util.concurrent.ScheduledFuture;

/**
 *
 * @author DreamHealer
 */
public class ServicesID extends HashNumeric {
    /* How long an ID that nobody uses is remembered, in seconds. One with
       a nick or a channel identified is kept three days: the users of a
       server that was split away, also over a weekend, are still identified
       when it comes back, and are not all sent to guest nicks at once. One
       with nothing to give back is kept a day, as before. (The tests start
       services with -Davade.sidexpire, these times are too long to wait for.) */
    private static final long       EXPIRE       = Long.getLong ( "avade.sidexpire", 3*24*60*60 );
    private static final long       EXPIRE_EMPTY = Math.min ( EXPIRE, 24*60*60 );
    
    private long                    id;
    private BigInteger              code;
    private ArrayList<NickInfo>     niList;    /* List of identified nicks from this serviceid */
    private ArrayList<ChanInfo>     ciList;    /* List of identified chans from this serviceid */
    private Random                  rand;
    private User                    user;      /* the owner of this servicesid */
    private long                    stamp;     /* timestamp  ( seconds )  lastseen */
    private boolean                 stored;    /* has a row in the servicesid table */
    private ScheduledFuture<?>      timer;      /* guest nick change */
    private ScheduledFuture<?>      adTimer;    /* identify reminder */
    
    /**
     *
     */
    public ServicesID ( )  {
        this.rand       = new Random ( );
        this.id         = this.getUniqueID ( );
        this.niList     = new ArrayList<>( );
        this.ciList     = new ArrayList<>( );
        this.stamp      = System.currentTimeMillis() / 1000;
        HashString buf  = new HashString ( ""+this.id );
        this.code       = buf.getCode ( );
    }

    /**
     *
     * @param id
     */
    public ServicesID ( long id )  {
        this.rand       = new Random ( );
        this.id         = id;
        this.stored     = true;     /* this one is read from the database */
        this.niList     = new ArrayList<>( );
        this.ciList     = new ArrayList<>( );
        this.stamp      = System.currentTimeMillis() / 1000;
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
    
    /**
     *
     * @return
     */
    public BigInteger getCode ( ) {
        return this.code;
    }
    
    /**
     *
     */
    public void updateStamp ( ) {
        this.stamp =  ( System.currentTimeMillis ( ) /1000 );
    }
    
    /**
     *
     * @return
     */
    public boolean hasExpired ( )  {
        if ( this.user != null ) {
            return false;
        }
        long keep = ( this.niList.isEmpty ( ) && this.ciList.isEmpty ( ) ? EXPIRE_EMPTY : EXPIRE );
        return this.stamp < System.currentTimeMillis ( ) / 1000 - keep;
    }

    /**
     * @return true when this ID has a row in the database
     */
    public boolean isStored ( ) {
        return this.stored;
    }

    /**
     * @param stored
     */
    public void setStored ( boolean stored ) {
        this.stored = stored;
    }
    
    /**
     *
     * @param ni
     */
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
      
    /**
     *
     * @param ni
     */
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
     
    /**
     *
     * @param ci
     */
    public void add ( ChanInfo ci )  {
        if ( ci == null ) {
            return;
        }
        for ( ChanInfo chan : this.ciList )  {
            if ( chan.is(ci) ) {
                return;
            }
        }
        this.ciList.add ( ci );
    }
    
    /**
     *
     * @param ni
     * @return
     */
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
    
    /**
     *
     * @param ci
     * @return
     */
    public boolean isIdentified ( ChanInfo ci )  {
        for ( ChanInfo chan : this.ciList )  {
            if ( chan.is(ci) ) {
                return true;
            }
        }
        return false;
    }
    
    /**
     *
     * @param user
     */
    public void addUser ( User user ) { 
        this.user = user; 
        this.updateStamp ( ); 
    }
    
    /**
     *
     */
    public void remUser ( ) { 
        this.user = null; 
        this.updateStamp ( ); 
    }
    
    /**
     *
     * @return
     */
    public long getID ( ) {
        return this.id;
    }

    /**
     *
     * @return
     */
    public ArrayList<NickInfo> getNiList ( ) { 
        return this.niList;
    }

    /**
     *
     * @return
     */
    public ArrayList<ChanInfo> getCiList ( ) { 
        return this.ciList;
    }  

    /**
     *
     * @param niList
     */
    public void setNiList ( ArrayList<NickInfo> niList ) { 
        this.niList = niList;
    }

    /**
     *
     * @param ciList
     */
    public void setCiList ( ArrayList<ChanInfo> ciList ) { 
        this.ciList = ciList;
    }

    
    /**
     *
     * @return
     */
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
    
    /**
     *
     * @param timer
     */
    public void addTimer ( ScheduledFuture<?> timer ) { 
        Scheduler.cancel ( this.timer );
        this.timer = timer;
    }

    /**
     *
     * @param timer
     */
    public void addAdTimer ( ScheduledFuture<?> timer ) { 
        Scheduler.cancel ( this.adTimer );
        this.adTimer = timer;
    }
     
    /**
     *
     */
    public void resetTimers ( ) {
        Scheduler.cancel ( this.timer );
        Scheduler.cancel ( this.adTimer );
        this.timer = null;
        this.adTimer = null;
    }

    User getUser ( ) { 
        return this.user;
    }

    /**
     *
     * @return
     */
    private static int operAccess ( NickInfo ni ) {
        return ( ni.getOper ( ) != null ? ni.getOper().getAccess ( ) : 0 );
    }

    public NickInfo getTopOperNick ( ) {
        NickInfo top = null;
        for ( NickInfo ni : this.niList ) {
            if ( top == null ) {
                top = ni;
            } else if ( operAccess ( ni ) > operAccess ( top ) ) {
                top = ni;
            }
        }
        return top;
    }
 
    /**
     *
     * @return
     */
    public int getAccess ( ) {
        if ( this.niList.size() > 0 &&
             this.getOper ( ) != null ) {
            return this.getOper().getAccess ( );
        }
        return 0;
    }

    /**
     *
     * @param ci
     */
    public void unIdentify ( ChanInfo ci ) {
        ciList.remove ( ci );
    }

    /**
     *
     * @param ni
     */
    public void unIdentify ( NickInfo ni ) {
        niList.remove ( ni );
        Handler.addUpdateSID ( this );
    }
    
    /* Set 1 hour limit */
//    public void setSplitExpire ( ) {
//        this.splitExpire = ( System.currentTimeMillis ( ) + ( 1000 * 60 * 60 ) ) ;
//    }

    /* Return true if expire time hasAccess in the past */

    /**
     *
     * @return
     */
}
