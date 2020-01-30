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
package nickserv;

import core.Changes;

/**
 *
 * @author Fredrik Karlsson aka DreamHealer & avade.net
 */
public class NSChanges extends Changes {
    private boolean freeze;
    private boolean mark;
    private boolean hold;
    private boolean noghost;
    private boolean pass;
    private boolean fullmask;
    private boolean mail;
    private boolean lastseen;
    private boolean noop;
    private boolean neverop;
    private boolean mailblock;
    private boolean showemail;
    private boolean showhost;
  
    public NSChanges ( ) {
        super ( );
        clean ( );
    }
    
    public void clean ( ) {
        this.freeze = false;
        this.mark = false;
        this.hold = false;
        this.noghost = false;
        this.pass = false;
        this.fullmask = false;
        this.mail = false;
        this.lastseen = false;
        this.noop = false;
        this.neverop = false;
        this.mailblock = false;
        this.showemail = false;
        this.showhost = false;
        this.changed = false;
    }
    
    public boolean hasChanged ( int what ) {
        switch ( what ) {
            case FREEZE :
                return this.freeze;

            case MARK :
                return this.mark;

            case HOLD :
                return this.hold;

            case NOGHOST :
                return this.noghost;

            case PASS :
                return this.pass;

            case FULLMASK :
                return this.fullmask;

            case MAIL :
                return this.mail;

            case LASTUSED :
                return this.lastseen;

            case NOOP :
                return this.noop;

            case NEVEROP :
                return this.neverop;

            case MAILBLOCK :
                return this.mailblock;

            case SHOWEMAIL :
                return this.showemail;

            case SHOWHOST :
                return this.showhost;

            default :
                return false;
        }
    }
    
    public void change ( int what ) {
        switch ( what ) {
            case FREEZE :
                this.freeze = true;
                this.changed = true;
                break;
                
            case MARK :
                this.mark = true;
                this.changed = true;
                break;

            case HOLD :
                this.hold = true;
                this.changed = true;
                break;
                
            case NOGHOST :
                this.noghost = true;
                this.changed = true;
                break;
                
            case PASS :
                this.pass = true;
                this.changed = true;
                break;

            case FULLMASK :
                this.fullmask = true;
                this.changed = true;
                break;

            case MAIL :
                this.mail = true;
                this.changed = true;
                break;

            case LASTUSED :
                this.lastseen = true;
                this.changed = true;
                break;

            case NOOP :
                this.noop = true;
                this.changed = true;
                break;

            case NEVEROP :
                this.neverop = true;
                this.changed = true;
                break;

            case MAILBLOCK :
                this.mailblock = true;
                this.changed = true;
                break;

            case SHOWEMAIL :
                this.showemail = true;
                this.changed = true;
                break;

            case SHOWHOST :
                this.showhost = true;
                this.changed = true;
                break;

            default :

        }
    }
    
    public void printChanges ( ) {
        System.out.println("***** Changes *****");
        if ( this.freeze ) 
            System.out.println("Changes: freeze!");
        if ( this.mark ) 
            System.out.println("Changes: mark!");
        if ( this.hold ) 
            System.out.println("Changes: hold!");
        if ( this.noghost ) 
            System.out.println("Changes: noghost!");
        if ( this.pass ) 
            System.out.println("Changes: pass!");
        if ( this.fullmask ) 
            System.out.println("Changes: fullmask!");
        if ( this.mail ) 
            System.out.println("Changes: mail!");
        if ( this.lastseen ) 
            System.out.println("Changes: lastseen!");
        if ( this.noop ) 
            System.out.println("Changes: noop!");
        if ( this.neverop ) 
            System.out.println("Changes: neverop!");
        if ( this.mailblock ) 
            System.out.println("Changes: mailblock!");
        if ( this.showemail ) 
            System.out.println("Changes: showemail!");
        if ( this.showhost ) 
            System.out.println("Changes: showhost!");
        if ( this.changed ) 
            System.out.println("Changes: changed!");
        System.out.println("***** End of Changes *****");
    }

    
}
