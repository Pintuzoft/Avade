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

/**
 *
 * @author DreamHealer
 */
public class UserMode extends HashNumeric {
    private boolean oper;
    private boolean sadmin;
    private boolean admin;
    private boolean ident;
    
    public UserMode ( )  {
        this.oper   = false;
        this.sadmin = false;
        this.admin  = false;
        this.ident  = false;
    }

    public void set ( int var, String[] data )  {
        switch ( var )  {
            case SERVER :
                this.setModeString ( data[4] );
                break;
                
            case MODE :
                this.setModeString ( data[3] );
                break;
                
            default : 
        } 
    }
  
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
 
    public void set ( int mode, boolean state )  {
        switch ( mode )  {
            case OPER :
                this.oper = state;
                break;
                
            case SADMIN :
                this.sadmin = state;
                break;
                
            case ADMIN :
                this.admin = state;
                break;
                
            case IDENT :
                this.ident = state;
                break;
                
            default : 
        } 
    }
      
    public boolean is ( int mode )  {
        switch ( mode )  {
            case OPER :
                return this.oper;
                
            case SADMIN :
                return this.sadmin;
                
            case ADMIN :
                return this.admin;
                
            case IDENT :
                return this.ident;
                
            default: 
                return false;
            
        } 
    } 
   
    private void debug ( )  {
 //       System.out.println ( "DEBUG: a="+this.sadmin+", A="+this.admin+", o="+this.oper+", r="+this.ident );
    }
}