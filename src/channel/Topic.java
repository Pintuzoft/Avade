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
        this.text = topic.replace ("^:", "");
        this.setter = setter;
        this.stamp = stamp;
        this.timeStr = dateFormat.format ( new Date ( ) );
    }

    /**
     *
     * @param topic
     * @param setter
     * @param stamp
     * @param timeStr
     */
    public Topic ( String topic, String setter, long stamp, String timeStr )  {
        if ( topic != null ) {
            this.text = topic.replace ("^:", "" );
        }
        this.setter = setter;
        this.stamp = stamp;
        if ( timeStr != null ) {
            this.timeStr = timeStr.substring (0,19 );
        }
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
     * @param topic
     */
    public void setText ( String topic ) { 
        this.text = topic.replace ("^:", "");
    } 
    
    /**
     *
     * @param setter
     */
    public void setSetter ( String setter ) { 
        this.setter = setter;
    } 
    
    /**
     *
     * @param stamp
     */
    public void setStamp ( long stamp ) { 
        this.stamp = stamp;
    }
     
}
