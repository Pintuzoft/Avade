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
package channel;

import core.HashNumeric;
import core.HashString;

/**
 *
 * @author DreamHealer
 */
public class ChanMode extends HashNumeric {
                    
    public ChanMode ( )  { 
        this.init ( ); 
    }

    private void init ( )  {
        this.set ( MODE_r, false );
        this.set ( MODE_R, false );
        this.set ( MODE_t, false );
        this.set ( MODE_n, false );
        this.set ( MODE_i, false );
        this.set ( MODE_k, false );
        this.set ( MODE_s, false );
        this.set ( MODE_p, false );
        this.set ( MODE_M, false );
        this.set ( MODE_l, false );
        this.set ( MODE_j, false );
        this.set ( MODE_c, false );
        this.set ( MODE_O, false );
        this.set ( MODE_m, false );
    }

    public void set ( HashString type, String[] data )  {
        if ( type.is(SERVER) ||
             type.is(MODE) ) {
            this.setModeString ( data[4] ); 
        }
        
    }
     
    public void setModeString ( String data ) {
        boolean state = false;
        for ( int index=0; index < data.length ( ); index++ )  {
             switch ( ( ""+data.charAt ( index ) ) .hashCode ( ) )  {
                case MODE_PLUS :
                    state = true;
                    break;

                case MODE_MINUS :
                    state = false;
                    break;

                case MODE_r :
                    this.set ( MODE_r, state );
                    break;

                case MODE_R :
                    this.set ( MODE_R, state );
                    break;

                case MODE_t :
                    this.set ( MODE_t, state );
                    break;

                case MODE_n :
                    this.set ( MODE_n, state );
                    break;

                case MODE_i :
                    this.set ( MODE_i, state );
                    break;

                case MODE_k :
                    this.set ( MODE_k, state );
                    break;

                case MODE_s :
                    this.set ( MODE_s, state );
                    break;

                case MODE_p :
                    this.set ( MODE_p, state );
                    break;

                case MODE_M :
                    this.set ( MODE_M, state );
                    break;

                case MODE_l :
                    this.set ( MODE_l, state );
                    break;

                case MODE_j :
                    this.set ( MODE_j, state );
                    break;

                case MODE_c :
                    this.set ( MODE_c, state );
                    break;

                case MODE_O :
                    this.set ( MODE_O, state );
                    break;

                case MODE_m :
                    this.set ( MODE_m, state );
                    break;

                default :
            }
        }   
    }

    /* 
     * Set channel mode to a specific boolean state
     */
    public void set ( int mode, boolean state )  {
        switch ( mode )  {
            case MODE_r :
                this.mode_r = state;
                break;
              
            case MODE_R :
                this.mode_R = state;
                break;
              
            case MODE_t :
                this.mode_t = state;
                break;

            case MODE_n :
                this.mode_n = state;
                break;

            case MODE_i :
                this.mode_i = state;
                break;

            case MODE_k :
                this.mode_k = state;
                break;

            case MODE_s :
                this.mode_s = state;
                break;

            case MODE_p :
                this.mode_p = state;
                break;

            case MODE_M :
                this.mode_M = state;
                break;

            case MODE_l :
                this.mode_l = state;
                break;

            case MODE_j :
                this.mode_j = state;
                break;

            case MODE_c :
                this.mode_c = state;
                break;

            case MODE_O :
                this.mode_O = state;
                break;

            case MODE_m :
                this.mode_m = state;
                break;

            default :

        }
    }
    
    /*
     * Returns the boolean state of a specific channel mode
     */
    public boolean is ( int mode )  {
        switch ( mode )  {
            case MODE_r :
                return this.mode_r;

            case MODE_R :
                return this.mode_R;

            case MODE_t :
                return this.mode_t;

            case MODE_n :
                return this.mode_n;

            case MODE_i : 
                return this.mode_i;

            case MODE_k :
                return this.mode_k;

            case MODE_s : 
                return this.mode_s;

            case MODE_p :
                return this.mode_p;

            case MODE_M :
                return this.mode_M;

            case MODE_l :
                return this.mode_l;

            case MODE_j : 
                return this.mode_j;

            case MODE_c :
                return this.mode_c;

            case MODE_O :
                return this.mode_O;

            case MODE_m :
                return this.mode_m;

            default :
                return false;

        }
    } 
    
    public String getModes ( ) {
        String buf = "+";
        buf += this.is ( MODE_r ) ? "r" : "";
        buf += this.is ( MODE_R ) ? "R" : "";
        buf += this.is ( MODE_t ) ? "t" : "";
        buf += this.is ( MODE_n ) ? "n" : "";
        buf += this.is ( MODE_i ) ? "i" : "";
        buf += this.is ( MODE_k ) ? "k" : "";
        buf += this.is ( MODE_s ) ? "s" : "";
        buf += this.is ( MODE_p ) ? "p" : "";
        buf += this.is ( MODE_M ) ? "M" : "";
        buf += this.is ( MODE_l ) ? "l" : "";
        buf += this.is ( MODE_j ) ? "j" : "";
        buf += this.is ( MODE_c ) ? "c" : "";
        buf += this.is ( MODE_O ) ? "O" : "";
        buf += this.is ( MODE_m ) ? "m" : "";
        return buf;  
    }

}
