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
package monitor;
 
import core.HashNumeric;
import core.HashString;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;



/**
 *
 * @author fredde
 */
public class SnoopLog extends HashNumeric {
    protected HashString target;
    protected String message;
    protected String stamp;
    protected DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public SnoopLog ( HashString target, String message ) {
        this.target = target;
        this.message = message;
        Date dateBuf = new Date ( );
        this.stamp = dateFormat.format ( dateBuf );
    }

    public HashString getTarget() {
        return this.target;
    }

    public String getMessage() {
        return this.message;
    }

    public String getStamp() {
        return this.stamp;
    }

}
