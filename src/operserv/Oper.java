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

import core.Proc;
import core.HashNumeric;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    private String          name;
    private int             hash;
    private String          instater;
    private String[]        levels;
    private String[]        shortLevels;

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

    public Oper ( ResultSet res )  {
        if ( res == null )  {
            return;
        }
        try {
            /* We got a nick from the database, so lets turn it into an object.. */
            res.first ( );
            this.name           = res.getString ( "name" );
            this.hash           = this.name.toUpperCase().hashCode();
            this.access         = res.getInt ( "access" );
            this.instater       = res.getString ( "instater" );
            this.levels         = OPER_LEVEL;
            this.shortLevels    = OPER_LEVEL_SHORT;

        } catch ( SQLException ex )  {
            Logger.getLogger ( Oper.class.getName ( ) ) .log ( Level.SEVERE, null, ex );
        }
        this.printOper();
    }
    
    public Oper ( String name, int access, String instater )  {
        this.name            = name;
        this.access          = access;
        this.instater        = instater;
        this.levels          = OPER_LEVEL;
        this.shortLevels     = OPER_LEVEL_SHORT;
        this.hash            = this.name.toUpperCase().hashCode ( );
    }
    
    /* user without access */
    public Oper ( ) {
        this.name           = "";
        this.access         = 0;
        this.instater       = "";
        this.levels         = OPER_LEVEL;
        this.shortLevels    = OPER_LEVEL_SHORT;
        this.hash           = this.name.toUpperCase().hashCode ( );
    }
    
    public int getAccess ( ) {
        if ( this.access > 0 ) {
            return this.access;
        } else {
            return 0;
        }
    }

    public int getHashCode ( ) {
        return this.hash;
    }
    
    public String getString ( int var )  {
        switch ( var )  {
            case INSTATER :
                return this.instater;
                
            case ACCSTRING :
                return this.levels[this.access];
                
            case NAME :
                return this.name;
                
            case ACCSTRINGSHORT :
                return this.shortLevels[this.access];
                
            default: 
                return null; 
            
        }
    } 
    
    public void setAccess ( int access ) {
        this.access = access;
    }
     
    public boolean getAccOper ( int i )  { 
        return  ( i >= this.access );
    }

    public boolean isAtleast ( int acc )  {
        System.out.println("DEBUG: access found as: "+this.access);
        switch ( acc )  {
            case MASTER :
                return ( this.access == 5 || this.isMaster ( ) );
                
            case SRA :
                return ( this.access >= 4 || this.isMaster ( ) );
                
            case CSOP :
                return ( this.access >= 3 || this.isMaster ( ) );
                
            case SA :
                return ( this.access >= 2 || this.isMaster ( ) );
                
            case IRCOP :
                return ( this.access >= 1 || this.isMaster ( ) );
                
            default: 
                return false; 
            
        }
    }
    
    static public int hashToAccess ( int hash ) {
        switch ( hash ) {
            case MASTER :
                return 5;
            case SRA :
                return 4;
            case CSOP :
                return 3;
            case SA :
                return 2;
            case IRCOP :
                return 1;
            default :
                return 0;
        }
    }
    
    static String hashToStr ( int hash ) {
        switch ( hash ) {
            case MASTER :
                return "Master";
            case SRA :
                return "SRA";
            case CSOP :
                return "CSop";
            case SA :
                return "SA";
            case IRCOP :
                return "IRCop";
            default :
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
    
    public boolean isMaster ( )  {
        return this.access == 6;
    }
    public String getName ( ) {
        return this.name;
    }
    public void printOper ( ) {
        System.out.println ( "Oper: "+this.name );
        System.out.println ( "Oper: "+this.instater );
        System.out.println ( "Oper: "+this.access );
    }

    public NickInfo getNick() {
        return NickServ.findNick ( this.name );
    }
} 
