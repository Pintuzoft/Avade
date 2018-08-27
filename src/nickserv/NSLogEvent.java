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
package nickserv;

import operserv.*;
import static core.HashNumeric.*;
import core.LogEvent;
import nickserv.NickInfo;
import user.User;
 

/**
 *
 * @author fredde
 */
public class NSLogEvent extends LogEvent {
    
    public NSLogEvent ( String name, String flag, String mask, String oper, String stamp ) {
        super ( name, flag, mask, oper, stamp );
    }
    
    public NSLogEvent ( String name, int flag, User user, NickInfo oper ) {
        super ( name, user.getFullMask(), oper.getName() );
        this.setFlag ( flag );
    }
    
    public NSLogEvent ( String name, int flag, String mask, String oper ) {
        super ( name, mask, oper );
        this.setFlag ( flag );
    }
    
    public NSLogEvent ( String name, String flag, String mask, String oper ) {
        super ( name, mask, oper );
        this.setFlag ( flag.toUpperCase().hashCode ( ) );
    }

    private void setFlag ( int flag ) {
        this.flag = LogEvent.getNickFlagByHash ( flag );
    }
    
}
