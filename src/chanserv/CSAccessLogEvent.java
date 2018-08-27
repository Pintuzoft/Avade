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
    protected String name;
    protected String flag;
    protected String target;
    protected String instater;
    protected String usermask;
    protected String stamp;

    public CSAccessLogEvent ( String name, String flag, String target, String instater, String usermask, String stamp ) {
        this.name = name;
        this.flag = flag;
        this.target = target;
        this.instater = instater;
        this.usermask = usermask;
        this.stamp = stamp.substring ( 0, 19 );
    }

    public CSAccessLogEvent ( String name, int flag, String target, String fullmask ) {
        this.name = name;
        this.flag = CSAccessLogEvent.getFlagByHash ( flag );
        this.target = target;
        if ( fullmask == null || fullmask.length() == 0 ) {
            this.instater = target;
            this.usermask = target+"!*@*.*";
        } else {
            this.instater = fullmask.substring(0, fullmask.indexOf("!") );
            this.usermask = fullmask;
        }
    }
     
    public CSAccessLogEvent ( String name, int flag, String target, User user ) {
        this.name = name;
        this.flag = CSAccessLogEvent.getFlagByHash ( flag );
        this.target = target;
        if ( user == null ) {
            this.instater = target;
            this.usermask = target+"!*@*.*";
        } else {
            this.instater = user.getString ( NAME );
            this.usermask = user.getString ( FULLMASK );
        }
    }
     
    public CSAccessLogEvent ( String name, int flag, String target ) {
        this.name = name;
        this.flag = CSAccessLogEvent.getFlagByHash ( flag );
        this.target = target;
        this.instater = target;
        this.usermask = target+"!*@*.*";
    }
 
    public void setStamp ( String stamp ) {
        this.stamp = stamp;
    }
    
    public static String getFlagByHash ( int hash ) {
        switch ( hash ) {
            case ADDAOP :
                return "AOP+";
     
            case DELAOP :
                return "AOP-";
     
            case ADDSOP :
                return "SOP+";
     
            case DELSOP :
                return "SOP-";
     
            case ADDAKICK :
                return "AKICK+";
     
            case DELAKICK :
                return "AKICK-";
           
            case FOUNDER :
                return "Founder";
      
            default :
                return null;
                
        }
    }
    
    public String getName ( ) {
        return name;
    }

    public String getFlag ( ) {
        return flag;
    }

    public String getTarget ( ) {
        return target;
    }
    
    public String getInstater ( ) {
        return instater;
    }
       
    public String getUsermask ( ) {
        return usermask;
    }
    
    public String getStamp ( ) {
        return stamp;
    }
     
}
