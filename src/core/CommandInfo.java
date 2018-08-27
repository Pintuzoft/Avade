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
public class CommandInfo extends HashNumeric {
    private String name;
    private int hashCode;
    private int access;
    private String description;
    private String patch;
    private static String basePatch = "            ";
    
    public CommandInfo ( String name, int access, String description )  {
        this.name           = name;
        this.hashCode       = name.hashCode ( );
        this.description    = description;
        this.access         = access;
        this.patch          = createPatch ( this.name );
    }
    
    public String getName ( ) {
        return this.name;
    }
    
    @Override
    public int hashCode ( ) {
        return this.hashCode;
    }
    
    public int getAccess ( ) {
        return this.access;
    }
    
    public String getDescription ( ) {
        return this.description;
    }
    
    public String getPatch ( ) {
        return this.patch;
    }
    
    /* Return a suitable patch based on namn length */
    public static String createPatch ( String str )  {
        return basePatch.substring ( 0, basePatch.length ( ) - str.length ( ) );
    }

    public boolean isAcc ( int access )  {
        switch ( access )  {
            case USER :
                return this.access == 0;
            
            case IRCOP :
                return this.access == 1;
                    
            case SA :
                return this.access == 2;
                
            case CSOP :
                return this.access == 3;
                
            case SRA :
                return this.access == 4;
                
            case MASTER :
                return this.access == 5;
                
            default : 
                return false;
           
        }
    }
     
    public String getAccessStr ( )  {
        switch ( this.access )  {
            case 1 :
                return "IRCop";
                
            case 2 :
                return "SA";
                
            case 3 :
                return "CSop";
                
            case 4 :
                return "SRA";
            
            case 5 :
                return "Master";
                
            default :
                return "User";
                
        }
    }
}
