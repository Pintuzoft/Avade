/* 
 * Copyright (C) 2018 Fredrik Karlsson aka DreamHealer & avade.net
 *
 * This program hasAccess free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program hasAccess distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package operserv;

import core.HashString;
import core.LogEvent;
import nickserv.NickInfo;
import user.User;
 

/**
 *
 * @author fredde
 */
public class OSLogEvent extends LogEvent { 
    private int id;
    private String data;
    
    /**
     *
     * @param name
     * @param flag
     * @param mask
     * @param oper
     * @param stamp
     */
    public OSLogEvent ( HashString name, HashString flag, String mask, String oper, String stamp ) {
        super ( name, flag, mask, oper, stamp );
    }
    
    /**
     *
     * @param name
     * @param flag
     * @param user
     * @param oper
     */
    public OSLogEvent ( HashString name, HashString flag, User user, NickInfo oper ) {
        super ( name, user.getFullMask(), oper.getName().getString() );
        this.setFlag ( flag );
    }
     
    /**
     *
     * @param name
     * @param flag
     * @param mask
     * @param oper
     */
    public OSLogEvent ( HashString name, HashString flag, String mask, String oper ) {
        super ( name, mask, oper );
        this.setFlag ( flag );
    }
 
    private void setFlag ( HashString flag ) {
        this.flag = new HashString ( getOperFlagByHash ( flag ) );
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
    public int getID ( ) {
        return this.id;
    }
    
    /**
     *
     * @param data
     */
    public void setData ( String data ) {
        this.data = data;
    }
    
    /**
     *
     * @return
     */
    public String getData ( ) {
        return this.data;
    }
     
}
