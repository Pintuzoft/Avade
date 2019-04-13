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
    private boolean autoakick;
    private boolean founder;
  
    private boolean join_connect_time;
    private boolean talk_connect_time;
    private boolean talk_join_time;
    private boolean max_bans;
    private boolean no_notice;
    private boolean no_ctcp;
    private boolean no_part_msg;
    private boolean exempt_opped;
    private boolean exempt_voiced;
    private boolean exempt_identd;
    private boolean exempt_registered;
    private boolean exempt_invites;
    private boolean greetmsg;
    
    
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
        this.leaveops = false;
        this.autoakick = false;
        this.founder = false;
        
        this.join_connect_time = false;
        this.talk_connect_time = false;
        this.talk_join_time = false;
        this.max_bans = false;
        this.no_notice = false;
        this.no_ctcp = false;
        this.no_part_msg = false;
        this.exempt_opped = false;
        this.exempt_voiced = false;
        this.exempt_identd = false;
        this.exempt_registered = false;
        this.exempt_invites = false;
        this.greetmsg = false;
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
                       
            case AUTOAKICK :
                return this.autoakick;
                
            case FOUNDER :
                return this.founder;
                    
            case JOIN_CONNECT_TIME :
                return this.join_connect_time;

            case TALK_CONNECT_TIME :
                return this.talk_connect_time;

            case TALK_JOIN_TIME :
                return this.talk_join_time;

            case MAX_BANS :
                return this.max_bans;
            
            case NO_NOTICE :
                return this.no_notice;

            case NO_CTCP :
                return this.no_ctcp;

            case NO_PART_MSG :
                return this.no_part_msg;

            case EXEMPT_OPPED :
                return this.exempt_opped;

            case EXEMPT_VOICED :
                return this.exempt_voiced;

            case EXEMPT_IDENTD :
                return this.exempt_identd;

            case EXEMPT_REGISTERED :
                return this.exempt_registered;            
            
            case EXEMPT_INVITES :
                return this.exempt_invites;
            
            case GREETMSG :        
                return this.greetmsg;
            
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
                
            case AUTOAKICK :
                this.autoakick = true;
                this.changed = true;
                break;
                
            case FOUNDER :
                this.founder = true;
                this.changed = true;
                break;
                
            case JOIN_CONNECT_TIME :
                this.join_connect_time = true;
                this.changed = true;
                break;

            case TALK_CONNECT_TIME :
                this.talk_connect_time = true;
                this.changed = true;
                break;

            case TALK_JOIN_TIME :
                this.talk_join_time = true;
                this.changed = true;
                break;

            case MAX_BANS :
                this.max_bans = true;
                this.changed = true;
                break;
            
            case NO_NOTICE :
                this.no_notice = true;
                this.changed = true;
                break;

            case NO_CTCP :
                this.no_ctcp = true;
                this.changed = true;
                break;

            case NO_PART_MSG :
                this.no_part_msg = true;
                this.changed = true;
                break;

            case EXEMPT_OPPED :
                this.exempt_opped = true;
                this.changed = true;
                break;

            case EXEMPT_VOICED :
                this.exempt_voiced = true;
                this.changed = true;
                break;

            case EXEMPT_IDENTD :
                this.exempt_identd = true;
                this.changed = true;
                break;

            case EXEMPT_REGISTERED :
                this.exempt_registered = true;
                this.changed = true;
                break;
            
            case EXEMPT_INVITES :
                this.exempt_invites = true;
                this.changed = true;
                break;
            
            case GREETMSG :        
                this.greetmsg = true;
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
        if ( this.autoakick )
            System.out.println("Changes: autoakick!");
    }

    
}
