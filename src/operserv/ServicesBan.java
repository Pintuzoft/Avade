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

import core.Handler;
import core.HashNumeric;
import core.StringMatch;
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
class ServicesBan extends HashNumeric {
    private int             type;
    private long            id;
    private String          mask;
    private String          host;
    private String          user;
    private String          instater;
    private String          reason;
    private String          time;
    private String          expire;
    private long            expireUT;
    private int             hashCode;
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); 
    
    
    public ServicesBan ( int type, long id, String mask, String reason, String instater, String time, String expire )  {
        this.type           = type;
        this.id             = id;        
        this.mask           = mask;        
        this.instater       = instater;
        this.reason         = reason;
        this.hashCode       = mask.toUpperCase().hashCode ( );
        
        /* Add proper time and expire if time is null */
        if ( time == null ) {
            this.time = dateFormat.format ( new Date ( ) );
            this.expire = Handler.expireToDateString ( this.time, expire );
        } else {
            this.time = time;
            this.expire = expire;
        }

        if ( mask.contains("@") ) {
            String[] buf = mask.split ( "@" );
            this.host = buf[1];
            this.user = buf[0];
        } else {
            this.user = "*";
            this.host = mask;
            
        }
        
        if ( ! expire.contains ( "INTERVAL" ) ) {
            this.setExpireUT ( );
        }
    }

    
    public boolean match ( String fullmask )  {
        return StringMatch.maskWild ( fullmask, this.mask );
    }
    
    public boolean matchNoWild ( String fullmask )  {
        return  ( fullmask.toUpperCase ( ) .hashCode ( )  == this.mask.toUpperCase ( ) .hashCode ( )  );
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
      
    public long getID ( ) { 
        System.out.println("debug: id:"+this.id);
        return this.id;
    }
    
    public String getMask ( ) { 
        return this.mask;
    }
    
    public String getInstater ( ) { 
        return this.instater;
    }
    
    public String getReason ( ) { 
        return this.reason;
    }
    
    public String getTime ( ) { 
        return this.time;
    }
    
    public String getExpire ( ) { 
        return this.expire;
    }

    public String getHost ( ) { 
        return this.host;
    }
    
    public String getUser ( ) { 
        return this.user;
    }

    public int getHashCode ( ) { 
        return this.hashCode;
    }
    
    public void setId ( int id ) { 
        this.id = id;
    }
    
    public void setTime ( String time ) { 
        this.time = time;
    }

    void setExpire ( String expire ) { 
        this.expire = expire;
        this.setExpireUT ( );
    }

    public long getExpireUT() {
        return this.expireUT;
    }

    private void setExpireUT() {
        try {
            Date date = dateFormat.parse ( this.expire );
            this.expireUT = ( long ) date.getTime ( ) / 1000;
        } catch (ParseException ex) {
            Logger.getLogger(ServicesBan.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
     
}