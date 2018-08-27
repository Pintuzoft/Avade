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

/**
 *
 * @author DreamHealer
 */
public class Topic {
    private String topic;
    private String setter;
    private long   time;
    
    public Topic ( String topic, String setter, long time )  {
        this.topic              = topic;
        this.setter             = setter;
        this.time               = time;
    }

    public String getTopic ( ) { 
        return topic;
    } 
    
    public String getSetter ( ) { 
        return setter;
    } 
    
    public long getTime ( ) {
        return time;
    } 

    public void setTopic ( String topic ) { 
        this.topic = topic;
    } 
    
    public void setSetter ( String setter ) { 
        this.setter = setter;
    } 
    
    public void setTime ( long time ) { 
        this.time = time;
    }
     
}
