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
package memoserv;

import core.Handler;
import java.util.Date;

/**
 *
 * @author DreamHealer
 */
public class MemoInfo {
    private int             id;
    private String          name;
    private String          message;
    private String          sender;
    private long            stamp;
    private boolean         read;
    private String          stampString;

    public MemoInfo ( String name, String sender, String message )  {
        this.name           = name;
        this.message        = message;
        this.sender         = sender;
        this.read           = false;
    }
    
    public MemoInfo ( int id, String name, String sender, String message,  long stamp, boolean read )  {
        this.id             = id;
        this.name           = name;
        this.message        = message;
        this.sender         = sender;
        this.stamp          = stamp;
        this.read           = read;
//        this.stampString    = Handler.getSdf ( ) .format ( new Date ( this.stamp*1000 )  );
    }
    
    public int getID ( ) { 
        return this.id;
    } 
    
    public void setID ( int id ) { 
        this.id = id;
    } 
    
    public String getName ( ) { 
        return this.name;
    } 
    
    public String getMessage ( ) {
        return this.message;
    } 
    
    public String getSender ( ) { 
        return this.sender;
    } 
    
    public long getStamp ( ) { 
        return this.stamp;
    }

    public void setStamp ( long stamp ) { 
        Date date = new Date ( this.stamp * 1000 );
        this.stamp = stamp; 
        this.stampString = Handler.getSdf ( ) .format ( date );
    } 
  
    public boolean isRead ( ) { 
        return this.read;
    }
    
    public String getStampStr ( ) { 
        return this.stampString;
    }
    
    public void setRead ( ) { 
        this.read = true;
    }
}
