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
package user;

import core.HashNumeric;
import core.HashString;

/**
 *
 * @author DreamHealer
 */
public class UserMode extends HashNumeric {
    private boolean oper;
    private boolean sadmin;
    private boolean admin;
    private boolean ident;
    
    /**
     *
     */
    public UserMode ( )  {
        this.oper   = false;
        this.sadmin = false;
        this.admin  = false;
        this.ident  = false;
    }

    /**
     *
     * @param type
     * @param data
     */
    public void set ( HashString type, String[] data )  {
        if ( type.is(SERVER) ) {
            this.setModeString ( data[4] );
        } else if ( type.is(MODE) ) {
            this.setModeString ( data[3] ); 
        }
    }
  
    /**
     *
     * @param data
     */
    public void setModeString ( String data )  {
        boolean state = false;
        Character ch;
        for ( int index=0; index < data.length ( ); index++ )  {
            ch = data.charAt ( index );
            switch ( ch.hashCode ( )  )  {
                case MODE_PLUS :
                    state = true;
                    break;
                    
                case MODE_MINUS :
                    state = false;
                    break;
                    
                case MODE_o :
                    this.set ( OPER, state );
                    break;
                
                case MODE_a :
                    this.set ( SADMIN, state );
                    break;
                    
                case MODE_A :
                    this.set ( ADMIN, state );
                    break;
                    
                case MODE_r :
                    this.set ( IDENT, state );
                    break;
                    
                default : 
            } 
        }
        debug ( );
    }
 
    /**
     *
     * @param mode
     * @param state
     */
    public void set ( HashString mode, boolean state )  {
        if ( mode.is(OPER) ) {
            this.oper = state;

        } else if ( mode.is(SADMIN) ) {
            this.sadmin = state;

        } else if ( mode.is(ADMIN) ) {
            this.admin = state;

        } else if ( mode.is(IDENT) ) {
            this.ident = state;
        }
    }
      
    /**
     *
     * @param mode
     * @return
     */
    public boolean is ( HashString mode )  {
        if ( mode.is(OPER) ) {
            return this.oper;
        
        } else if ( mode.is(SADMIN) ) {
            return this.sadmin;
        
        } else if ( mode.is(ADMIN) ) {
            return this.admin;
        
        } else if ( mode.is(IDENT) ) {
            return this.ident;
        }
        return false;
    } 
   
    private void debug ( )  {
 //       System.out.println ( "DEBUG: a="+this.sadmin+", A="+this.admin+", o="+this.oper+", r="+this.ident );
    }
}