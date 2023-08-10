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
package nickserv;

import chanserv.ChanInfo;
import core.Expire;
import core.Handler;
import core.HashNumeric;
import core.HashString;
import core.Throttle;
import memoserv.MSDatabase;
import memoserv.MemoInfo;
import operserv.Oper;
import user.User;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;


/**
 *  create table nick  ( name varchar ( 32 ) , mask varchar ( 128 ) ,pass varchar ( 32 ) ,mail varchar ( 64 ) ,regstamp int ( 11 ) , stamp int ( 11 ) , primary key  ( name )  )  ENGINE=InnoDB;
 * @author DreamHealer
 */
public class NickInfo extends HashNumeric {
    private HashString              name;
    private HashString              user;
    private HashString              host;
    private HashString              ip;
    private InetAddress             iNet; 
    private HashString              hashMask;       /* Integer representation of user@mask */ 
    private String                  pass;
    private String                  mail; 
    private NickSetting             settings;
    private String                  regTime;
    private String                  lastUsed; 
    private Date                    date; 
    private Oper                    oper; 
    private ArrayList<MemoInfo>     mList = new ArrayList<>();
    private Expire                  exp;
    private NSChanges               changes;
    private Throttle                throttle;       /* throttle login attempts */
    private ArrayList<ChanInfo>     akickList = new ArrayList<>();
    private ArrayList<ChanInfo>     aopList = new ArrayList<>();
    private ArrayList<ChanInfo>     sopList = new ArrayList<>();
    private ArrayList<ChanInfo>     founderList = new ArrayList<>();
    private DateFormat  dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /* DATABASE */

    /**
     *
     * @param name
     * @param user
     * @param host
     * @param pass
     * @param mail
     * @param regStamp
     * @param lastSeen
     * @param settings
     * @param exp
     */

    public NickInfo ( String name, String user, String host, String pass, String mail, String regStamp, String lastSeen, NickSetting settings, Expire exp )  {
        this.name       = new HashString ( name );
        this.hashMask   = new HashString ( user+"@"+host );
        this.user       = new HashString ( user );
        this.host       = new HashString ( host );
        this.date       = new Date ( );
        this.oper       = null; 
        this.pass       = pass;
        this.mail       = mail; 
        this.regTime    = regStamp.substring(0,19);
        this.lastUsed   = lastSeen.substring(0,19);
        this.settings   = settings;
        this.exp        = exp;
        this.changes    = new NSChanges ( );
        this.throttle   = new Throttle ( );
        //this.attachMemos ( ); 
     }

    /* REGISTER */

    /**
     *
     * @param user
     * @param pass
     */

    public NickInfo ( User user, String pass )  {
        /* Register nickname */ 
        this.name       = new HashString ( user.getString ( NAME ) );
        this.user       = new HashString ( user.getString ( USER ) );
        this.host       = new HashString ( user.getString ( HOST ) );
        this.ip         = new HashString ( user.getString ( IP ) );
        this.hashMask   = new HashString ( user.getString(USER)+"@"+user.getString(IP) ); 
        this.pass       = pass;
        this.mail       = ""; 
        this.settings   = new NickSetting ( );
        String date = this.dateFormat.format ( new Date ( ) );
        this.regTime    = date;
        this.lastUsed   = date; 
        this.date       = new Date ( );
        this.oper       = new Oper ( );
        this.changes    = new NSChanges ( );
        this.throttle   = new Throttle ( );
        this.userIdent ( user );
        //this.attachMemos ( );
    }
    
    /* Master nick */

    /**
     *
     * @param name
     */
    public NickInfo ( String name ) {
        User u          = Handler.findUser ( name );
        Random random   = new Random ( );
        if ( u != null ) {
            String passwd   = "Master" + random.nextInt ( 9800 ) + 100;
            this.name       = new HashString ( name );
            this.pass       = passwd;
            this.ip         = new HashString ( u.getString ( IP ) );
            this.user       = new HashString ( u.getString ( USER ) );
            this.host       = new HashString ( u.getString ( HOST ) );
            this.hashMask   = new HashString ( this.user+"@"+this.ip ); 
            this.mail       = "master@localhost";
            String date = this.dateFormat.format ( new Date ( ) );
            this.regTime    = date;
            this.lastUsed   = date;
            this.date       = new Date ( );
            this.oper       = new Oper ( u.getString ( NAME ), 5, "" );
            this.exp        = new Expire ( );
            this.changes    = new NSChanges ( );
            this.throttle   = new Throttle ( );

            this.userIdent ( u );
            //this.attachMemos ( );
            this.settings = new NickSetting ( );

            Handler.getRootServ().sendMsg ( u, "Nick: "+u.getString ( NAME )+" has been registered to you using password: "+passwd );
        }
    }
   
    /**
     *
     * @param user
     */    
    public void setUserMask ( User user )  {
        this.user       = new HashString ( user.getString ( USER ) );
        this.host       = new HashString ( user.getString ( HOST ) );
        this.ip         = new HashString ( user.getString ( IP ) );
        this.hashMask   = new HashString ( user.getString(USER)+"@"+user.getString(HOST) );
        this.changes.change ( USER );
        this.changes.change ( HOST );
        this.changes.change ( IP );
        this.changes.change ( FULLMASK );
        NickServ.addToWorkList ( CHANGE, this );
    }
 
    /* Identify nick in user */
    private void userIdent ( User user )  {
        user.getSID().add ( this ); 
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
     * @param it
     * @return string
     */
    public String getString ( HashString it )  {
        if      ( it.is(NAME) )         { return this.name.getString();                     }
        else if ( it.is(USER) )         { return this.user.getString();                     }
        else if ( it.is(HOST) )         { return this.host.getString();                     }
        else if ( it.is(IP) )           { return this.host.getString();                     }
        else if ( it.is(MAIL) )         { return this.mail;                                 }
        else if ( it.is(FULLMASK) )     { return this.name+"!"+this.user+"@"+this.host;     }
        else if ( it.is(LASTUSED) )     { return this.lastUsed;                             }
        else if ( it.is(REGTIME) )      { return this.regTime;                              }
        else {
            return "";
        }
    }
 
    /**
     *
     * @param it
     * @return true/false
     */
    public boolean isState ( HashString it )  {
        if ( it.is(AUTHED) ) {
            return this.settings.is ( AUTH );
        
        } else if ( it.is(OLD) ) {
            return this.olderThanExpireTime ( );
        }
        return false;
    }
    
    /**
     *
     * @return exp
     */
    public Expire getExp ( ) {
        return this.exp;
    }
    
    /**
     *
     * @return true/false
     */
    public boolean shouldExpire ( )  {
        return this.exp.shouldExpire ( );
    }
    
    /* Returns true if nick hasAccess older than expiretime */

    /**
     *
     * @return false
     */
    public boolean olderThanExpireTime ( )  {
        return false;
   //     return  ( ( System.currentTimeMillis ( ) /1000 - this.lastUsed )  > Handler.expireToTime ( Proc.getConf ( ) .get ( EXPIRE ) ) );
    }
    
    /* Return true if last mail was sent more than a day ago */

    /**
     *
     * @return auth
     */  
    public boolean getAuth ( ) { 
        return this.settings.getAuth ( ); 
    }
     
    /**
     *
     * @param user
     * @param pass
     * @return true/false
     */
    public boolean identify ( User user, String pass )  {
        if ( pass == null || user == null ) {
            return false;
        }
        
        if ( this.pass.compareTo ( pass ) == 0 )  {
            this.changes.hasChanged ( LASTUSED );
            return true;
        }
        return false;
    }
    
    /* hasAccess commands */

    /**
     *
     * @param name
     * @return true/false
     */
    public boolean is ( HashString name ) {
        return this.name.is ( name );
    }
    
    /**
     *
     * @param ni
     * @return true/false
     */
    public boolean is ( NickInfo ni ) {
        return this.name.is ( ni.getName() );
    }
    
    /**
     *
     * @param setting
     * @return bool
     */
    public boolean isSet ( HashString setting ) {
        return this.settings.is ( setting );
    }
    
    /**
     *
     * @param newPass
     */
    public void setPass ( String newPass ) {
        this.pass = newPass;
    }
      
    /**
     *
     * @param pass
     * @return bool
     */
    public boolean isPass ( String pass )  {
        if ( pass == null ) {
            return false;
        }
        return ( this.pass.compareTo ( pass ) == 0 );
    }
 
    /**
     *
     * @return pass
     */
    public String getPass ( )  {
        return this.pass;
    }

    /**
     *
     * @return mail
     */
    public String getEmail ( )  {
        return this.mail;
    }

    /**
     *
     * @return settings
     */
    public NickSetting getSettings ( )  {
        return this.settings;
    }
    
    /**
     *
     * @return exp
     */
    public Expire getNickExp ( ) {
        return this.exp;
    }
     
    /**
     *
     * @return oper
     */
    public Oper getOper ( ) {
        return this.oper;
    }
    
    /**
     *
     * @return str
     */
    public String getIDOper ( ) {
        return  ( this.oper != null ) ? this.oper.getString ( NAME ) : null;
    }

    /**
     *
     * @return mList
     */
    public ArrayList<MemoInfo> getMemos ( ) {
        return this.mList; 
    }

    /**
     *
     * @param memo
     */
    public void addMemo ( MemoInfo memo ) {
        this.mList.add ( memo ); 
        Handler.getMemoServ().newMemo ( memo ); 
    }

    /**
     *
     * @param num
     * @return memo
     */
    public MemoInfo getMemo ( int num )  { 
        if ( num > 0 && num <= this.mList.size ( ) ) {
            return this.mList.get ( num-1 );
        } 
        return null;
    }

    /**
     *
     * @param memo
     */
    public void delMemo ( MemoInfo memo )  {
        this.mList.remove ( memo );
    } 
    
    /**
     *
     * @param access
     * @return bool
     */
    public boolean isAtleast ( HashString access ) {
        if ( this.oper == null ) {
            return false;
        }
        int acc;
        if      ( access.is(IRCOP) )            { acc = 1; }
        else if ( access.is(SA) )               { acc = 2; }
        else if ( access.is(CSOP) )             { acc = 3; }
        else if ( access.is(SRA) )              { acc = 4; }
        else if ( access.is(MASTER) )           { acc = 5; }
        else {
            acc = 0;
        }
        return ( this.oper.getAccess ( ) >= acc );       
    }

    /**
     *
     * @param oper
     */
    public void setOper ( Oper oper ) {
        this.oper = oper;
    }
    
    /**
     *
     * @param setting
     * @return bool
     */
    public boolean isSetting ( HashString setting ) {
        return this.settings.is ( setting );
    }

    /**
     *
     * @return access
     */
    public int getAccess ( ) {
        return this.oper.getAccess ( );
    }
    
    /**
     *
     * @param mail
     */
    public void setEmail ( String mail ) {
        this.mail = mail;
        this.settings.set ( AUTH, true );
    }
    
    /*** AOP/SOP/FOUNDER LISTS
     * @param it
     * @return list
     ***/
    
    public ArrayList<ChanInfo> getChanAccess ( HashString it ) {
        if      ( it.is(FOUNDER) )          { return founderList;               }
        else if ( it.is(SOP) )              { return sopList;                   }
        else if ( it.is(AOP) )              { return aopList;                   }
        else if ( it.is(AKICK) )            { return akickList;                 }
        else {
            return new ArrayList<>();
        }
    }
    
    /**
     *
     * @param list
     * @param ci
     */
    public void addToAccessList ( HashString list, ChanInfo ci ) {
        this.getChanAccess(list).add ( ci );
    }
    
    /**
     *
     * @param list
     * @param ci
     */
    public void remFromAccessList ( HashString list, ChanInfo ci ) {
        ArrayList<ChanInfo> chans = new ArrayList<>();
        for ( ChanInfo ci2 : getChanAccess ( list ) ) {
            if ( ci2.is(ci) ) {
                chans.add ( ci2 );
            }
        }
        for ( ChanInfo ci2 : chans ) {
            getChanAccess(list).remove ( ci2 );
        }
    }
    
    /****************/
    
    public void setLastUsed ( ) {
        Date dateBuf    = new Date ( );
        this.lastUsed   = dateFormat.format ( dateBuf );
        this.changes.change ( LASTUSED );
    }
     
    /**
     *
     * @return changes
     */
    public NSChanges getChanges ( ) {
        return this.changes;
    }
    
    /**
     *
     * @param it
     * @return bool
     */
    public boolean hasChanged ( HashString it ) {
        return this.changes.hasChanged(it);
    }
    
    /**
     *
     * @param user
     * @return bool
     */
    public boolean is ( User user ) {
        return this.name.is(user.getName());
    }
    
    /**
     *
     * @param user
     * @return bool
     */
    public boolean isMask ( User user ) {
        return this.host.is ( user.getMask() );
    }
    
    /**
     *
     * @return bool
     */
    public boolean isAuth ( ) {
        if ( this.mail == null ) {
            return false;
        }
        return this.mail.length() > 0;
    }
    
    /**
     *
     * @return throttle
     */
    public Throttle getThrottle ( ) {
        return this.throttle;
    }
}