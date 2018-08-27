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
    
    private void clean ( ) {
        this.freeze = false;
        this.mark = false;
        this.hold = false;
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
    
    public boolean changed ( int what ) {
        switch ( what ) {
            case FREEZE :
                return this.freeze;

            case MARK :
                return this.mark;

            case HOLD :
                return this.hold;

            case PASS :
                return this.hold;

            case FULLMASK :
                return this.fullmask;

            case MAIL :
                return this.hold;

            case LASTSEEN :
                return this.hold;

            case NOOP :
                return this.hold;

            case NEVEROP :
                return this.hold;

            case MAILBLOCK :
                return this.hold;

            case SHOWEMAIL :
                return this.hold;

            case SHOWHOST :
                return this.showhost;

            default :
                return false;
        }
    }
    
    public void update ( int what ) {
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

            case LASTSEEN :
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
    

    
}
