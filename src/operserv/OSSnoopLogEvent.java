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

/**
 *
 * @author fredde
 */
public class OSSnoopLogEvent { 
    private String target;
    private String body;
    private String stamp;
    
    /**
     *
     * @param target
     * @param body
     * @param stamp
     */
    public OSSnoopLogEvent ( String target, String body, String stamp ) {
        this.target = target;
        this.body = body;
        this.stamp = stamp;
        printThis();
    }

    /**
     *
     * @return
     */
    public String getTarget() {
        return this.target;
    }

    /**
     *
     * @return
     */
    public String getBody() {
        return this.body;
    }

    /**
     *
     * @return
     */
    public String getStamp() {
        return this.stamp;
    }
    
    private void printThis ( ) {
        System.out.println("DEBUG: target: "+this.target);
        System.out.println("DEBUG: body: "+this.body);
        System.out.println("DEBUG: stamp: "+this.stamp);
    }
}
