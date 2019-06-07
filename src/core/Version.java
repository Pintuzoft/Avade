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
    private String          name = "Avade";
    private int             generation;
    private int             year;
    private int             month;
    private int             build;
    private String          bahamut;
    
    public Version ( )  {
        this.generation     = 1;
        this.year           = 19;
        this.month          = 04;
        this.build          = 3;
        this.bahamut        = "bahamut-2.1.4";
    }
    
    public String getVersion ( )  {
        return this.name+"-"+this.generation+"."+this.year+this.zeroPrefix(this.month)+"-"+this.build;
    }
    
    private String zeroPrefix ( int num ) {
        String buf = String.valueOf ( num );
        return buf.length() < 2 ? "0"+buf : buf;
    }
    
    public int getGeneration ( ) {
        return this.generation;
    }
    public int getYear ( ) {
        return this.year;
    }
    public int getMonth ( ) {
        return this.month;
    }   
    public int getBuild ( ) {
        return this.build;
    }
    public String getBahamut ( ) {
        return this.bahamut;
    }
    
}
