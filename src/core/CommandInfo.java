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
    private HashString name;
    private int access;
    private String description;
    private String patch;
    private static String basePatch = "            ";
    
    /**
     *
     * @param name
     * @param access
     * @param description
     */
    public CommandInfo ( String name, int access, String description )  {
        this.name           = new HashString ( name );
        this.description    = description;
        this.access         = access;
        this.patch          = createPatch ( this.name );
    }
    
    /**
     *
     * @return
     */
    public HashString getName ( ) {
        return this.name;
    }

    /**
     *
     * @return
     */
    public int getAccess ( ) {
        return this.access;
    }
    
    /**
     *
     * @return
     */
    public String getDescription ( ) {
        return this.description;
    }
    
    /**
     *
     * @return
     */
    public String getPatch ( ) {
        return this.patch;
    }
    
    /* Return a suitable patch based on namn length */

    /**
     *
     * @param str
     * @return
     */

    public static String createPatch ( HashString str ) {
        return basePatch.substring ( 0, basePatch.length ( ) - str.length ( ) );
    }

    /**
     *
     * @param it
     * @return
     */
    public boolean isAcc ( HashString it )  {
        if      ( it.is(USER) )             { return this.access == 0;          } 
        else if ( it.is(IRCOP) )            { return this.access == 1;          }
        else if ( it.is(SA) )               { return this.access == 2;          }
        else if ( it.is(CSOP) )             { return this.access == 3;          }
        else if ( it.is(SRA) )              { return this.access == 4;          }
        else if ( it.is(MASTER) )           { return this.access == 5;          }
        else {
            return false;
        }
    }
     
    /**
     *
     * @return
     */
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
    
    /**
     *
     * @param ci
     * @return
     */
    public boolean is ( HashString ci ) {
        return this.name.is(ci);
    }
    
}
