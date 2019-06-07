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
    private int             type;
    private String          id;
    private int             hash;
    private String          nick;
    private String          user;
    private String          host;
    private String          mask;
    private String          instater;
    private String          reason;
    private Date            time;
    private long            timeTS;
    private long            expireSec;
    private Date            expire;
    private long            expireStamp;
    private int             maskHash;
    private CIDRUtils       cidr;
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
    
    
    public ServicesBan ( int type, String id, boolean readyID, String mask, String reason, String instater, String timeStr, String expireStr ) {
        this.type = type;
        if ( ! readyID ) {
            this.id = getTypeStr(type)+id;
        } else {
            this.id = id;
        }
        this.hash     = this.id.toUpperCase().hashCode();
        this.mask     = mask;        
        this.instater = instater;
        this.reason   = reason;
        this.maskHash = mask.toUpperCase().hashCode ( );
        
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
        
        if ( mask.contains("!") ) {
            String[] buf1 = mask.split ( "!" );
            this.nick = buf1[0];
            if ( mask.contains("@") ) {
                String[] buf = buf1[1].split ( "@" );
                this.host = buf[1];
                this.user = buf[0];
            } else {
                this.user = "*";
                this.host = mask;
            }
        } else {
            this.nick = "*";
            if ( mask.contains("@") ) {
                String[] buf = mask.split ( "@" );
                this.host = buf[1];
                this.user = buf[0];
            } else {
                this.user = "*";
                this.host = mask;
            }
        }
        
        // CIDR
        if ( this.host.contains("/") ) {
            try {
                this.cidr = new CIDRUtils ( this.host );
            } catch (UnknownHostException ex) {
                Logger.getLogger(ServicesBan.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            this.cidr = null;
        }
        
        this.printData ( );

    }

    public void printData() {
        System.out.println("BAN: nick:"+this.nick);
        System.out.println("BAN: user:"+this.user);
        System.out.println("BAN: host:"+this.host);
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
    
    
    public boolean match ( String fullmask )  {
        return StringMatch.maskWild ( fullmask, this.mask );
    }
    
    public boolean matchNoWild ( String fullmask )  {
        return  ( fullmask.toUpperCase().hashCode ( ) == this.mask.toUpperCase().hashCode ( ) );
    }
    
    public String getListName ( ) {
        switch ( this.type ) {
            case AKILL :
                return "akill";
            
            case IGNORE :
                return "ignorelist";
                
            case SQLINE :
                return "sqline";
                         
            case SGLINE :
                return "sgline";
                         
            default :
                return "";
        }
    }
     
    public static String getCommandByHash ( int hash ) {
        switch ( hash ) {
            case AKILL:
                return "AKILL";
                
            case IGNORE :
                return "IGNORE";
        
            case SQLINE :
                return "SQLINE";
                   
            case SGLINE :
                return "SGLINE";
                   
            default :
                return "";
        }
    }
    
    public int getType ( ) { 
        return this.type;
    }
      
    public String getID ( ) { 
        System.out.println("debug: id:"+this.id);
        return this.id;
    }
    
    public String getMask ( ) { 
        return this.mask;
    }
    
    public CIDRUtils getCidr ( ) {
        return this.cidr;
    }
    
    public String getInstater ( ) { 
        return this.instater;
    }
    
    public String getReason ( ) { 
        return this.reason;
    }
    
    public String getTime ( ) { 
        return dateFormat.format ( this.time );
    }
    
    public String getExpire ( ) { 
        return dateFormat.format ( this.expire );
    }

    public String getHost ( ) { 
        return this.host;
    }
    
    public String getUser ( ) { 
        return this.user;
    }

    public int getMaskHash ( ) { 
        return this.maskHash;
    }
    
    public int getHash ( ) {
        return this.hash;
    }
    
    public void setId ( String id ) { 
        this.id = id;
    }
    
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

    public long getExpireStamp() {
        return this.expireStamp;
    }
    
    public long getExpireSec() {
        return this.expireSec;
    }
    
    private void setExpireStamp() {
        this.expireStamp = ( long ) this.expire.getTime ( ) / 1000;
    }

    private static String getTypeStr ( int type ) {
        switch ( type ) {
            case AKILL :
                return "AK";
            case SQLINE :
                return "SQ";
            case SGLINE :
                return "SG";
            case IGNORE :
                return "IG";
                
            default :
                return "AK";
        }
    }
    public boolean hasExpired ( ) {
        System.out.println(this.expireStamp+":"+System.currentTimeMillis());
        return this.expireStamp < System.currentTimeMillis();
    }
    public String getBanTypeStr ( ) {
        switch ( this.type ) {
            case AKILL :
                return "AutoKill";
            case SQLINE :
                return "SQLine";
            case SGLINE :
                return "SGLine";
            default :
                return "Unknown";
        }
    }
}