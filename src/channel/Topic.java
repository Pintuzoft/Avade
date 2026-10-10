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
package channel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author DreamHealer
 */
public class Topic {
    private String text;
    private String setter;
    private String timeStr;
    private long stamp;
    private DateFormat dateFormat = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss" );
    
    /**
     *
     * @param topic
     * @param setter
     * @param stamp
     */
    public Topic ( String topic, String setter, long stamp )  {
        this.text = stripColon ( topic );
        this.setter = ( setter != null ? setter : "" );
        this.stamp = stamp;
        this.timeStr = dateFormat.format ( new Date ( stamp * 1000L ) );
    }

    /**
     *
     * @param topic
     * @param setter
     * @param stamp
     * @param timeStr
     */
    public Topic ( String topic, String setter, long stamp, String timeStr )  {
        this.text = stripColon ( topic );
        this.setter = ( setter != null ? setter : "" );
        this.stamp = stamp;
        if ( timeStr != null && timeStr.length() >= 19 ) {
            this.timeStr = timeStr.substring ( 0, 19 );
        } else {
            this.timeStr = dateFormat.format ( new Date ( stamp * 1000L ) );
        }
    }

    /* Remove the single leading ':' of an IRC trailing parameter */
    private static String stripColon ( String topic ) {
        if ( topic == null ) {
            return "";
        }
        return topic.startsWith ( ":" ) ? topic.substring ( 1 ) : topic;
    }

    /**
     * @return true if the topic has any text
     */
    public boolean hasText ( ) {
        return ! this.text.isEmpty ( );
    }

    /**
     * Versions before 1.2609 sent "TOPIC #chan null 0 :null" for channels
     * without a stored topic, the network and the topic log can still hold it
     * @return true if this is such a topic
     */
    public boolean isJunk ( ) {
        return "null".equals ( this.text ) && "null".equals ( this.setter );
    }

    /**
     * @param topic
     * @return true if text, setter and stamp are the same
     */
    public boolean isSame ( Topic topic ) {
        return topic != null &&
               this.text.equals ( topic.text ) &&
               this.setter.equals ( topic.setter ) &&
               this.stamp == topic.stamp;
    }
 
    /**
     *
     * @return
     */
    public String getText ( ) { 
        return text;
    } 
    
    /**
     *
     * @return
     */
    public String getSetter ( ) { 
        return setter;
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
     * @return
     */
    public String getTimeStr ( ) {
        return this.timeStr;
    }

    
    /**
     *
     * @param stamp
     */
    public void setStamp ( long stamp ) { 
        this.stamp = stamp;
    }
     
}
