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
package chanserv;

import core.Changes;

/**
 *
 * @author Fredrik Karlsson aka DreamHealer & avade.net
 */
public class CSChanges extends Changes {
    private boolean freeze;
    private boolean mark;
    private boolean hold;
    private boolean pass;
    private boolean mailblock;

    private boolean close;
    private boolean auditorium;
    private boolean topic;
    private boolean lastoped;
    private boolean description;
    private boolean topiclock;
    private boolean modelock;
    private boolean keeptopic;
    private boolean ident;
    private boolean opguard;
    private boolean restrict;
    private boolean verbose;
    private boolean leaveops;
    private boolean founder;
  
    public CSChanges ( ) {
        super ( );
        clean ( );
    }
    
    public void clean ( ) {
        this.freeze = false;
        this.mark = false;
        this.hold = false;
        this.mailblock = false;
        
        this.changed = false;
        this.close = false;
        this.auditorium = false;
        this.topic = false;
        this.lastoped = false;
        this.description = false;
        this.topiclock = false;
        this.modelock = false;
        this.keeptopic = false;
        this.ident = false;
        this.opguard = false;
        this.restrict = false;
        this.verbose = false;
        this.founder = false;
    }
    
    public boolean hasChanged ( int what ) {
        switch ( what ) {
            case FREEZE :
                return this.freeze;

            case MARK :
                return this.mark;

            case HOLD :
                return this.hold;

            case MAILBLOCK :
                return this.mailblock;

            case CLOSE :
                return this.close;

            case AUDITORIUM :
                return this.auditorium;

            case TOPIC :
                return this.topic;

            case LASTOPED :
                return this.lastoped;

            case DESCRIPTION :
                return this.description;

            case TOPICLOCK :
                return this.topiclock;

            case MODELOCK :
                return this.modelock;

            case KEEPTOPIC :
                return this.keeptopic;

            case IDENT :
                return this.ident;

            case OPGUARD :
                return this.opguard;

            case RESTRICT :
                return this.restrict;

            case VERBOSE :
                return this.verbose;

            case LEAVEOPS :
                return this.leaveops;
                
            case FOUNDER :
                return this.founder;
                
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
                
            case MAILBLOCK :
                this.mailblock = true;
                this.changed = true;
                break;
 
            case CLOSE :
                this.close = true;
                this.changed = true;
                break;

            case AUDITORIUM :
                this.auditorium = true;
                this.changed = true;
                break;

            case TOPIC :
                this.topic = true;
                this.changed = true;
                break;

            case LASTOPED :
                this.lastoped = true;
                this.changed = true;
                break;

            case DESCRIPTION :
                this.description = true;
                this.changed = true;
                break;

            case TOPICLOCK :
                this.topiclock = true;
                this.changed = true;
                break;

            case MODELOCK :
                this.modelock = true;
                this.changed = true;
                break;

            case KEEPTOPIC :
                this.keeptopic = true;
                this.changed = true;
                break;

            case IDENT :
                this.ident = true;
                this.changed = true;
                break;

            case OPGUARD :
                this.opguard = true;
                this.changed = true;
                break;

            case RESTRICT :
                this.restrict = true;
                this.changed = true;
                break;

            case VERBOSE :
                this.verbose = true;
                this.changed = true;
                break;

            case LEAVEOPS :
                this.leaveops = true;
                this.changed = true;
                break;
                
            case FOUNDER :
                this.founder = true;
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
        if ( this.pass ) 
            System.out.println("Changes: pass!");
        if ( this.mailblock ) 
            System.out.println("Changes: mailblock!");
        if ( this.changed ) 
            System.out.println("Changes: changed!");
       
        if ( this.changed )
            System.out.println("Changes: changed!");
        if ( this.close )
            System.out.println("Changes: close!");
        if ( this.auditorium )
            System.out.println("Changes: auditorium!");
        if ( this.topic )
            System.out.println("Changes: topic!");
        if ( this.lastoped )
            System.out.println("Changes: lastoped!");
        if ( this.description )
            System.out.println("Changes: description!");
        if ( this.topiclock )
            System.out.println("Changes: topiclock!");
        if ( this.modelock )
            System.out.println("Changes: modelock!");
        if ( this.keeptopic )
            System.out.println("Changes: keeptopic!");
        if ( this.ident )
            System.out.println("Changes: ident!");
        if ( this.restrict )
            System.out.println("Changes: restrict!");
        if ( this.verbose )
            System.out.println("Changes: verbose!");
        if ( this.leaveops )
            System.out.println("Changes: leaveops!");
        if ( this.leaveops )
            System.out.println("Changes: leaveops!");
    }

    
}
