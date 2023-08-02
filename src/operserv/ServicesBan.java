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

import core.CIDRUtils;
import core.Handler;
import core.HashNumeric;
import core.HashString;
import core.StringMatch;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author DreamHealer
 */
public class ServicesBan extends HashNumeric {
    private HashString      type;
    private HashString      id;
    private HashString      nick;
    private HashString      user;
    private HashString      host;
    private HashString      mask;
    private String          instater;
    private String          reason;
    private Date            time;
    private long            timeTS;
    private long            expireSec;
    private Date            expire;
    private long            expireStamp;
    private CIDRUtils       cidr;
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
    
    /**
     *
     * @param type
     * @param id
     * @param readyID
     * @param mask
     * @param reason
     * @param instater
     * @param timeStr
     * @param expireStr
     */
    public ServicesBan ( HashString type, HashString id, boolean readyID, HashString mask, String reason, String instater, String timeStr, String expireStr ) {
        this.type = type;
        if ( ! readyID ) {
            this.id = new HashString ( type.getString()+id );
        } else {
            this.id = id;
        }
        this.mask     = mask;        
        this.instater = instater;
        this.reason   = reason;
        
        /* Add proper time and expire if time is null */
        if ( timeStr == null || ! timeStr.contains("-") ) {
            this.time = new Date ( );
            this.expire = Handler.expireToDate ( this.time, expireStr );
            this.expireStamp = this.expire.getTime();
            
        } else {
            try {
                this.time = dateFormat.parse ( timeStr );
                this.timeTS =  this.time.getTime ( );
                this.expire = dateFormat.parse ( expireStr );
                this.expireStamp = this.expire.getTime();
            } catch ( ParseException ex ) {
                Logger.getLogger(ServicesBan.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        this.expireSec = ( this.expire.getTime() - this.time.getTime() ) / 1000;
        
        if ( mask.getString().contains("!") ) {
            String[] buf1 = mask.getString().split ( "!" );
            this.nick = new HashString ( buf1[0] );
            if ( mask.contains("@") ) {
                String[] buf = buf1[1].split ( "@" );
                this.host = new HashString ( buf[1] );
                this.user = new HashString ( buf[0] );
            } else {
                this.user = new HashString ( "*" );
                this.host = mask;
            }
        } else {
            this.nick = new HashString ( "*" );
            if ( mask.getString().contains("@") ) {
                String[] buf = mask.getString().split ( "@" );
                this.host = new HashString ( buf[1] );
                this.user = new HashString ( buf[0] );
            } else {
                this.user = new HashString ( "*" );
                this.host = mask;
            }
        }
        
        // CIDR
        if ( this.host.contains("/") ) {
            try {
                this.cidr = new CIDRUtils ( this.host.getString() );
            } catch (UnknownHostException ex) {
                Logger.getLogger(ServicesBan.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            this.cidr = null;
        }
        
     //   this.printData ( );

    }

    /**
     *
     */
    public void printData() {
        System.out.println("BAN: id: "+this.id.getString());
        System.out.println("BAN: nick:"+this.nick);
        System.out.println("BAN: user:"+this.user);
        System.out.println("BAN: host:"+this.host);
        System.out.println("BAN: mask: "+this.mask.getString());
        System.out.println("BAN: isCidr:"+(this.cidr != null?"1":"0") );
        
        if ( this.cidr != null ) {
            System.out.println("BAN: cidr-NETADDR:"+this.cidr.getNetworkAddress());
            System.out.println("BAN: cidr-BROADADDR:"+this.cidr.getBroadcastAddress());
        }
        
        System.out.println ( "BAN: instater:"+this.instater );
        System.out.println ( "BAN: reason:"+this.reason );
        if ( this.time == null ) {
            System.out.println ( "BAN: time:-" );
        } else {
            System.out.println ( "BAN: time:"+dateFormat.format ( this.time ) );
        }     
        if ( this.expire == null ) {
            System.out.println ( "BAN: expire:-" );
        } else {
            System.out.println ( "BAN: expire:"+dateFormat.format ( this.expire ) );
        }
   }
    
    /**
     *
     * @param fullmask
     * @return
     */
    public boolean match ( String fullmask )  {
        return StringMatch.maskWild ( fullmask, this.mask.getString() );
    }
    
    /**
     *
     * @param fullmask
     * @return
     */
    public boolean matchNoWild ( String fullmask )  {
        HashString it = new HashString ( fullmask );
        return it.is(this.mask);
    }
    
    /**
     *
     * @return
     */
    public String getListName ( ) {
        if      ( type.is(AKILL) )      { return "akill";           }
        else if ( type.is(IGNORE) )     { return "ignorelist";      }
        else if ( type.is(SQLINE) )     { return "sqline";          }
        else if ( type.is(SGLINE) )     { return "sgline";          }
        else if ( type.is(AKILL) )      { return "akill";           }
        else {
            return "";
        } 
    }
     
    /**
     *
     * @param hash
     * @return
     */
    public static String getCommandByHash ( HashString hash ) {
        if ( hash.is(AKILL) )           { return "AKILL";           }
        else if ( hash.is(IGNORE) )     { return "IGNORE";          }
        else if ( hash.is(SQLINE) )     { return "SQLINE";          }
        else if ( hash.is(SGLINE) )     { return "SGLINE";          }
        else {
            return "";
        } 
    }
    
    /**
     *
     * @return
     */
    public HashString getType ( ) { 
        return this.type;
    }
      
    /**
     *
     * @return
     */
    public HashString getID ( ) { 
        return this.id;
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
     * @return
     */
    public CIDRUtils getCidr ( ) {
        return this.cidr;
    }
    
    /**
     *
     * @return
     */
    public String getInstater ( ) { 
        return this.instater;
    }
    
    /**
     *
     * @return
     */
    public String getReason ( ) { 
        return this.reason;
    }
    
    /**
     *
     * @return
     */
    public String getTime ( ) { 
        return dateFormat.format ( this.time );
    }
    
    /**
     *
     * @return
     */
    public String getExpire ( ) { 
        return dateFormat.format ( this.expire );
    }

    /**
     *
     * @return
     */
    public HashString getHost ( ) { 
        return this.host;
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
     * @param id
     */
    public void setId ( HashString id ) { 
        this.id = id;
    }
    
    /**
     *
     * @param time
     */
    public void setTime ( String time ) {
        try {
            this.time = dateFormat.parse ( time );
        } catch (ParseException ex) {
            Logger.getLogger(ServicesBan.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void setExpire ( String expire ) {
        try {
            this.expire = dateFormat.parse ( expire );
        } catch (ParseException ex) {
            Logger.getLogger(ServicesBan.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.setExpireStamp ( );
    }

    /**
     *
     * @return
     */
    public long getExpireStamp() {
        return this.expireStamp;
    }
    
    /**
     *
     * @return
     */
    public long getExpireSec() {
        return this.expireSec;
    }
    
    private void setExpireStamp() {
        this.expireStamp = ( long ) this.expire.getTime ( ) / 1000;
    }

    private static String getTypeStr ( HashString type ) {
        if      ( type.is(AKILL) )          { return "AK"; }
        else if ( type.is(SQLINE) )         { return "SQ"; }
        else if ( type.is(SGLINE) )         { return "SG"; }
        else if ( type.is(IGNORE) )         { return "IG"; }
        else {
            return "AK";
        }
    }

    /**
     *
     * @return
     */
    public boolean hasExpired ( ) {
        return this.expireStamp < System.currentTimeMillis();
    }

    /**
     *
     * @return
     */
    public String getBanTypeStr ( ) {
        if      ( type.is(AKILL) )          { return "AutoKill"; }
        else if ( type.is(SQLINE) )         { return "AutoKill"; }
        else if ( type.is(SGLINE) )         { return "AutoKill"; }
        else {
            return "Unknown";
        }
    }

    /**
     *
     * @param type
     * @return
     */
    public boolean is ( HashString type ) {
        return this.type.is(type);
    }

    /**
     *
     * @param ban
     * @return
     */
    public boolean is ( ServicesBan ban ) {
        return this.getID().is(ban.getID());
    }

    /**
     *
     * @param mask
     * @return
     */
    public boolean isMask ( HashString mask ) {
        return this.mask.is(mask);
    }
}