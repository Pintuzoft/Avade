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

import core.HashNumeric;

/**
 *
 * @author fredde
 */
class Comment extends HashNumeric {
    private String name;
    private String instater;
    private String stamp;
    private String commentStr;
    
    
    public Comment ( String name, String instater, String comment, String stamp ) {
        this.name = name;
        this.instater = instater;
        this.commentStr = comment;
        if ( stamp != null ) {
            this.stamp = stamp.substring ( 0, 19 );
        } else {
            this.stamp = stamp;
        }
    }

    public String getName() {
        return name;
    }

    public String getInstater() {
        return instater;
    }

    public String getStamp() {
        return stamp;
    }

    public String getCommentStr() {
        return commentStr;
    }
     
    
}
