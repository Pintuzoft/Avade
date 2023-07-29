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

    /**
     *
     * @param name
     * @param sender
     * @param message
     */
    public MemoInfo ( String name, String sender, String message )  {
        this.name           = name;
        this.message        = message;
        this.sender         = sender;
        this.read           = false;
    }
    
    /**
     *
     * @param id
     * @param name
     * @param sender
     * @param message
     * @param stamp
     * @param read
     */
    public MemoInfo ( int id, String name, String sender, String message,  long stamp, boolean read )  {
        this.id             = id;
        this.name           = name;
        this.message        = message;
        this.sender         = sender;
        this.stamp          = stamp;
        this.read           = read;
//        this.stampString    = Handler.getSdf ( ) .format ( new Date ( this.stamp*1000 )  );
    }
    
    /**
     *
     * @return
     */
    public int getID ( ) { 
        return this.id;
    } 
    
    /**
     *
     * @param id
     */
    public void setID ( int id ) { 
        this.id = id;
    } 
    
    /**
     *
     * @return
     */
    public String getName ( ) { 
        return this.name;
    } 
    
    /**
     *
     * @return
     */
    public String getMessage ( ) {
        return this.message;
    } 
    
    /**
     *
     * @return
     */
    public String getSender ( ) { 
        return this.sender;
    } 
    
    /**
     *
     * @return
     */
    public long getStamp ( ) { 
        return this.stamp;
    }

    /**
     *
     * @param stamp
     */
    public void setStamp ( long stamp ) { 
        Date date = new Date ( this.stamp * 1000 );
        this.stamp = stamp; 
        this.stampString = Handler.getSdf ( ) .format ( date );
    } 
  
    /**
     *
     * @return
     */
    public boolean isRead ( ) { 
        return this.read;
    }
    
    /**
     *
     * @return
     */
    public String getStampStr ( ) { 
        return this.stampString;
    }
    
    /**
     *
     */
    public void setRead ( ) { 
        this.read = true;
    }
}
