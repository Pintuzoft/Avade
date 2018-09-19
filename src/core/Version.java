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
package core;

/**
 *
 * @author DreamHealer
 */
public class Version extends HashNumeric {
    private String          name;
    private int             generation;
    private int             version;
    private int             subversion;
    private String          date;
    
    public Version ( )  {
        this.name           = "Avade";
        this.generation     = 1;
        this.version        = 0;
        this.subversion     = 20;
        this.date           = "2018-Sep";
    }
    public String getVersion ( )  {
        return this.name+"-"+this.generation+"."+this.version+"-"+this.subversion+"  ( "+this.date+" ) ";
    } 
}
