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
package mail;

/**
 *
 * @author DreamHealer
 */
public class Mail {
    private String to;
    private String subject;
    private String auth;
    private String body;
    
    /**
     *
     * @param to
     * @param subject
     * @param auth
     * @param body
     */
    public Mail ( String to, String subject, String auth, String body )  {
        this.to         = to;
        this.subject    = subject;
        this.auth       = auth;
        this.body       = body;
        System.out.println ( "Mail ( "+this.to+", "+this.subject+", "+this.auth+", "+this.body+" );" );
    }
    
    /**
     *
     * @return
     */
    public String getTo ( ) { 
        return this.to;
    } 

    /**
     *
     * @return
     */
    public String getSubject ( ) { 
        return this.subject;
    }     

    /**
     *
     * @return
     */
    public String getAuth ( ) { 
        return this.auth;
    } 

    /**
     *
     * @return
     */
    public String getBody ( ) { 
        return this.body;
    }
}
