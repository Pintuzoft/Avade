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
package chanserv;
 
import core.*;
import user.User;



/**
 *
 * @author fredde
 */
public class CSAccessLogEvent extends HashNumeric {
    public static String wildUserHost = "!*@*.*";
    /**
     *
     */
    protected HashString name;

    /**
     *
     */
    protected HashString flag;

    /**
     *
     */
    protected String target;

    /**
     *
     */
    protected String instater;

    /**
     *
     */
    protected String usermask;

    /**
     *
     */
    protected String stamp;

    /**
     *
     * @param name
     * @param flag
     * @param target
     * @param instater
     * @param usermask
     * @param stamp
     */
    public CSAccessLogEvent ( HashString name, HashString flag, String target, String instater, String usermask, String stamp ) {
        this.name = name;
        this.flag = flag;
        this.target = target;
        this.instater = instater;
        this.usermask = usermask;
        this.stamp = stamp.substring ( 0, 19 );
    }

    /**
     *
     * @param name
     * @param flag
     * @param target
     * @param fullmask
     */
    public CSAccessLogEvent ( HashString name, HashString flag, String target, String fullmask ) {
        this.name = name;
        this.flag = flag;
        this.target = target;
        if ( fullmask == null || fullmask.length() == 0 ) {
            this.instater = target;
            this.usermask = target+wildUserHost;
        } else {
            this.instater = fullmask.substring(0, fullmask.indexOf("!") );
            this.usermask = fullmask;
        }
    }
     
    /**
     *
     * @param name
     * @param flag
     * @param target
     * @param user
     */
    public CSAccessLogEvent ( HashString name, HashString flag, String target, User user ) {
        this.name = name;
        this.flag = flag;
        this.target = target;
        if ( user == null ) {
            this.instater = target;
            this.usermask = target+wildUserHost;
        } else {
            this.instater = user.getString ( NAME );
            this.usermask = user.getString ( FULLMASK );
        }
    }
     
    /**
     *
     * @param name
     * @param flag
     * @param target
     */
    public CSAccessLogEvent ( HashString name, HashString flag, String target ) {
        this.name = name;
        this.flag = flag;
        this.target = target;
        this.instater = target;
        this.usermask = target+wildUserHost;
    }
 
    /**
     *
     * @param stamp
     */
    public void setStamp ( String stamp ) {
        this.stamp = stamp;
    }
    
    /**
     *
     * @return
     */
    public HashString getFlag ( ) {
        return this.flag;
    }
 
    /**
     *
     * @return
     */
    public String getFlagStr ( ) {
        return getFlagByHash ( this.flag );
    }
    
    /**
     *
     * @param hash
     * @return
     */
    public static String getFlagByHash ( HashString hash ) {
        if ( hash.is(ADDAOP) ) {
            return "AOP+";
        
        } else if ( hash.is(DELAOP) ) {
            return "AOP-";
        
        } else if ( hash.is(ADDSOP) ) {
            return "SOP+";
        
        } else if ( hash.is(DELSOP) ) {
            return "SOP-";
        
        } else if ( hash.is(ADDAKICK) ) {
            return "AKICK+";
        
        } else if ( hash.is(DELAKICK) ) {
            return "AKICK-";
        
        } else if ( hash.is(FOUNDER) ) {
            return "Founder";
        }
        return null;
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
    public String getTarget ( ) {
        return this.target;
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
    public String getUsermask ( ) {
        return this.usermask;
    }
    
    /**
     *
     * @return
     */
    public String getStamp ( ) {
        return this.stamp;
    }
     
}
