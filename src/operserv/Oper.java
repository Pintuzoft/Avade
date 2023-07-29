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
import core.HashString;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import nickserv.NickInfo;
import nickserv.NickServ;

/**
 * create table oper  ( name varchar ( 32 ) , access int ( 11 ) , instater varchar ( 32 ) , primary key  ( name ) , constraint foreign key  ( name )  references nick  ( name )  on delete cascade on update cascade )  ENGINE=InnoDB;
 * @author DreamHealer
 */
public class Oper extends HashNumeric {
    private int             access;
    private HashString      name;
    private String          instater;
    private String[]        levels;
    private String[]        shortLevels;
    private ArrayList<String> makill = new ArrayList<>();
    
    private static String[] OPER_LEVEL = { 
        "User",
        "IRC Operator",
        "Services Administrator",
        "Channel Services Operator",
        "Services Root Administrator",
        "Services Master"
    };
    private static String[] OPER_LEVEL_SHORT = {
        "User",
        "IRCop",
        "SA",
        "CSop",
        "SRA",
        "Master"
    }; // User:0 IRCOp:1 SA:2 CSop:3 SRA:4 MASTER:5

    /**
     *
     * @param res
     */
    public Oper ( ResultSet res )  {
        if ( res == null )  {
            return;
        }
        try {
            /* We got a nick from the database, so lets turn it into an object.. */
            res.first ( );
            this.name           = new HashString ( res.getString ( "name" ) );
            this.access         = res.getInt ( "access" );
            this.instater       = res.getString ( "instater" );
            this.levels         = OPER_LEVEL;
            this.shortLevels    = OPER_LEVEL_SHORT;

        } catch ( SQLException ex )  {
            Logger.getLogger ( Oper.class.getName ( ) ) .log ( Level.SEVERE, null, ex );
        }
        this.printOper();
    }
    
    /**
     *
     * @param name
     * @param access
     * @param instater
     */
    public Oper ( String name, int access, String instater )  {
        this.name            = new HashString ( name );
        this.access          = access;
        this.instater        = instater;
        this.levels          = OPER_LEVEL;
        this.shortLevels     = OPER_LEVEL_SHORT;
    }
    
    /* user without access */

    /**
     *
     */

    public Oper ( ) {
        this.name           = new HashString ( "" );
        this.access         = 0;
        this.instater       = "";
        this.levels         = OPER_LEVEL;
        this.shortLevels    = OPER_LEVEL_SHORT;
    }
    
    /**
     *
     * @return
     */
    public int getAccess ( ) {
        if ( this.access > 0 ) {
            return this.access;
        } else {
            return 0;
        }
    }
 
    /**
     *
     * @param in
     * @return
     */
    public String getString ( HashString in )  {
        if ( in.is(INSTATER) ) {
            return this.instater;
            
        } else if ( in.is(ACCSTRING) ) {
            return this.levels[this.access];
            
        } else if ( in.is(NAME) ) {
            return this.name.getString();
            
        } else if ( in.is(ACCSTRINGSHORT) ) {
            return this.shortLevels[this.access];
        }
        return null;
    } 
    
    /**
     *
     * @param access
     */
    public void setAccess ( int access ) {
        this.access = access;
    }
     
    /**
     *
     * @param i
     * @return
     */
    public boolean getAccOper ( int i )  { 
        return  ( i >= this.access );
    }

    /**
     *
     * @param access
     * @return
     */
    public boolean isAtleast ( HashString access )  {
        if ( this.isMaster() ) {
            return true;
        }
        
        if ( access.is(MASTER) ) {
            return this.access == 5;
        
        } else if ( access.is(SRA) ) {
            return this.access >= 4;
        
        } else if ( access.is(CSOP) ) {
            return this.access >= 3;
        
        } else if ( access.is(SA) ) {
            return this.access >= 2;
            
        } else if ( access.is(IRCOP) ) {
            return this.access >= 1;
        } 
        
        return false;
        
    }
    
    /**
     *
     * @param hash
     * @return
     */
    public static int hashToAccess ( HashString hash ) {
        if ( hash.is(MASTER) ) {
            return 5;
            
        } else if ( hash.is(SRA) ) {
            return 4;
        
        } else if ( hash.is(CSOP) ) {
            return 3;
        
        } else if ( hash.is(SA) ) {
            return 2;
        
        } else if ( hash.is(IRCOP) ) {
            return 1;
        
        } else {
            return 0;
        }
    }
    
    static String hashToStr ( HashString hash ) {        
        if ( hash.is(MASTER) ) {
            return "Master";
            
        } else if ( hash.is(SRA) ) {
            return "SRA";
        
        } else if ( hash.is(CSOP) ) {
            return "CSop";
        
        } else if ( hash.is(SA) ) {
            return "SA";
        
        } else if ( hash.is(IRCOP) ) {
            return "IRCop";
        
        } else {
            return "User";
        }
    }
    static String accessToStr ( int access ) {
        switch ( access ) {
            case 5:
                return "Master";
            case 4:
                return "SRA";
            case 3:
                return "CSop";
            case 2:
                return "SA";
            case 1:
                return "IRCop";
            default :
                return "User";
        }
    }
    
    /**
     *
     * @return
     */
    public boolean isMaster ( )  {
        return this.access == 6;
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
    public String getNameStr ( ) {
        return this.name.getString();
    }

    /**
     *
     * @param name
     * @return
     */
    public boolean is ( HashString name ) {
        return this.name.is(name);
    }
    
    /**
     *
     */
    public void printOper ( ) {
        System.out.println ( "Oper: "+this.name );
        System.out.println ( " - instater: "+this.instater );
        System.out.println ( " - access: "+this.access );
    }

    /**
     *
     * @return
     */
    public NickInfo getNick() {
        return NickServ.findNick ( this.name );
    }
    
    /* MAKILL */

    /**
     *
     * @return
     */

    public ArrayList<String> getMAkill ( ) {
        return this.makill;
    }
    
    /**
     *
     * @param ban
     */
    public void addMAkill ( String ban ) {
        this.makill.add ( ban );
    }
    
    /**
     *
     */
    public void clearMakill ( ) {
        this.makill.clear();
    }

    /**
     *
     * @param string
     * @return
     */
    public boolean makillDuplicate(String string) {
        for ( String ban : this.makill ) {
            if ( ban.equalsIgnoreCase ( string ) ) {
                return true;
            }
        }
        return false;
    }
} 
