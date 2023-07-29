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
import core.HashString;

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
    private boolean lastused;
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
  
    private boolean joinconnecttime;
    private boolean talkconnecttime;
    private boolean talkjointime;
    private boolean maxbans;
    private boolean nonotice;
    private boolean noctcp;
    private boolean nopartmsg;
    private boolean exemptopped;
    private boolean exemptvoiced;
    private boolean exemptidentd;
    private boolean exemptregistered;
    private boolean exemptinvites;
    private boolean greetmsg;
    
    /**
     * CSChanges
     */
    public CSChanges ( ) {
        super ( );
        this.cleanUp ( );
    }
    
    /**
     * clean
     */
    public void cleanUp ( ) {
        this.freeze = false;
        this.mark = false;
        this.hold = false;
        this.mailblock = false;
        
        this.changed = false;
        this.close = false;
        this.auditorium = false;
        this.topic = false;
        this.lastused = false;
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
        
        this.joinconnecttime = false;
        this.talkconnecttime = false;
        this.talkjointime = false;
        this.maxbans = false;
        this.nonotice = false;
        this.noctcp = false;
        this.nopartmsg = false;
        this.exemptopped = false;
        this.exemptvoiced = false;
        this.exemptidentd = false;
        this.exemptregistered = false;
        this.exemptinvites = false;
        this.greetmsg = false;
    }
    
    /**
     * hasChanged
     * @param setting
     * @return
     */
    public boolean hasChanged ( HashString setting ) {
        if      ( setting.is(FREEZE) )              { return this.freeze;               } 
        else if ( setting.is(MARK) )                { return this.mark;                 }
        else if ( setting.is(HOLD) )                { return this.hold;                 }
        else if ( setting.is(MAILBLOCK) )           { return this.mailblock;            }
        else if ( setting.is(CLOSE) )               { return this.close;                }
        else if ( setting.is(AUDITORIUM) )          { return this.auditorium;           } 
        else if ( setting.is(TOPIC) )               { return this.topic;                } 
        else if ( setting.is(LASTUSED) )            { return this.lastused;             } 
        else if ( setting.is(DESCRIPTION) )         { return this.description;          }
        else if ( setting.is(TOPICLOCK) )           { return this.topiclock;            }
        else if ( setting.is(MODELOCK) )            { return this.modelock;             }
        else if ( setting.is(KEEPTOPIC) )           { return this.keeptopic;            }
        else if ( setting.is(IDENT) )               { return this.ident;                }
        else if ( setting.is(OPGUARD) )             { return this.opguard;              }
        else if ( setting.is(RESTRICT) )            { return this.restrict;             }
        else if ( setting.is(VERBOSE) )             { return this.verbose;              }
        else if ( setting.is(LEAVEOPS) )            { return this.leaveops;             }
        else if ( setting.is(AUTOAKICK) )           { return this.autoakick;            }
        else if ( setting.is(FOUNDER) )             { return this.founder;              }
        else if ( setting.is(JOIN_CONNECT_TIME) )   { return this.joinconnecttime;    }
        else if ( setting.is(TALK_CONNECT_TIME) )   { return this.talkconnecttime;    }
        else if ( setting.is(TALK_JOIN_TIME) )      { return this.talkjointime;       }
        else if ( setting.is(MAX_BANS) )            { return this.maxbans;             }
        else if ( setting.is(NO_NOTICE) )           { return this.nonotice;            }
        else if ( setting.is(NO_CTCP) )             { return this.noctcp;              }
        else if ( setting.is(NO_PART_MSG) )         { return this.nopartmsg;          }
        else if ( setting.is(EXEMPT_OPPED) )        { return this.exemptopped;         }
        else if ( setting.is(EXEMPT_VOICED) )       { return this.exemptvoiced;        }
        else if ( setting.is(EXEMPT_IDENTD) )       { return this.exemptidentd;        }
        else if ( setting.is(EXEMPT_REGISTERED) )   { return this.exemptregistered;    }
        else if ( setting.is(EXEMPT_INVITES) )      { return this.exemptinvites;       }
        else if ( setting.is(GREETMSG) )            { return this.greetmsg;             }
        return false;        
    }
    
    /**
     * change
     * @param setting
     */
    public void change ( HashString setting ) {
        if ( setting.is(FREEZE) ) {
            this.freeze = true;
            this.changed = true;
        
        } else if ( setting.is(MARK) ) {
            this.mark = true;
            this.changed = true;
        
        } else if ( setting.is(HOLD) ) {
            this.hold = true;
            this.changed = true;
        
        } else if ( setting.is(MAILBLOCK) ) {
            this.mailblock = true;
            this.changed = true;
        
        } else if ( setting.is(CLOSE) ) {
            this.close = true;
            this.changed = true;
        
        } else if ( setting.is(AUDITORIUM) ) {
            this.auditorium = true;
            this.changed = true;
        
        } else if ( setting.is(TOPIC) ) {
            this.topic = true;
            this.changed = true;
        
        } else if ( setting.is(LASTUSED) ) {
            this.lastused = true;
            this.changed = true;
        
        } else if ( setting.is(DESCRIPTION) ) {
            this.description = true;
            this.changed = true;
        
        } else if ( setting.is(TOPICLOCK) ) {
            this.topiclock = true;
            this.changed = true;
        
        } else if ( setting.is(MODELOCK) ) {
            this.modelock = true;
            this.changed = true;
        
        } else if ( setting.is(KEEPTOPIC) ) {
            this.keeptopic = true;
            this.changed = true;
        
        } else if ( setting.is(IDENT) ) {
            this.ident = true;
            this.changed = true;
        
        } else if ( setting.is(OPGUARD) ) {
            this.opguard = true;
            this.changed = true;
        
        } else if ( setting.is(RESTRICT) ) {
            this.restrict = true;
            this.changed = true;
        
        } else if ( setting.is(VERBOSE) ) {
            this.verbose = true;
            this.changed = true;
        
        } else if ( setting.is(LEAVEOPS) ) {
            this.leaveops = true;
            this.changed = true;
        
        } else if ( setting.is(AUTOAKICK) ) {
            this.autoakick = true;
            this.changed = true;
        
        } else if ( setting.is(FOUNDER) ) {
            this.founder = true;
            this.changed = true;
        
        } else if ( setting.is(JOIN_CONNECT_TIME) ) {
            this.joinconnecttime = true;
            this.changed = true;
        
        } else if ( setting.is(TALK_CONNECT_TIME) ) {
            this.talkconnecttime = true;
            this.changed = true;
        
        } else if ( setting.is(TALK_JOIN_TIME) ) {
            this.talkjointime = true;
            this.changed = true;
        
        } else if ( setting.is(MAX_BANS) ) {
            this.maxbans = true;
            this.changed = true;
        
        } else if ( setting.is(NO_NOTICE) ) {
            this.nonotice = true;
            this.changed = true;
        
        } else if ( setting.is(NO_CTCP) ) {
            this.noctcp = true;
            this.changed = true;
        
        } else if ( setting.is(NO_PART_MSG) ) {
            this.nopartmsg = true;
            this.changed = true;
        
        } else if ( setting.is(EXEMPT_OPPED) ) {
            this.exemptopped = true;
            this.changed = true;
        
        } else if ( setting.is(EXEMPT_VOICED) ) {
            this.exemptvoiced = true;
            this.changed = true;
        
        } else if ( setting.is(EXEMPT_IDENTD) ) {
            this.exemptidentd = true;
            this.changed = true;
        
        } else if ( setting.is(EXEMPT_REGISTERED) ) {
            this.exemptregistered = true;
            this.changed = true;
        
        } else if ( setting.is(EXEMPT_INVITES) ) {
            this.exemptinvites = true;
            this.changed = true;
            
        } else if ( setting.is(GREETMSG) ) {
            this.greetmsg = true;
            this.changed = true;
        }  
    }
    
    /**
     * printChanges
     */
    public void printChanges ( ) {
        System.out.println("***** Changes *****");
        if ( this.freeze )          { System.out.println("Changes: freeze!");       }
        if ( this.mark )            { System.out.println("Changes: mark!");         }
        if ( this.hold )            { System.out.println("Changes: hold!");         }
        if ( this.pass )            { System.out.println("Changes: pass!");         }
        if ( this.mailblock )       { System.out.println("Changes: mailblock!");    }
        if ( this.changed )         { System.out.println("Changes: changed!");      }
        if ( this.changed )         { System.out.println("Changes: changed!");      }
        if ( this.close )           { System.out.println("Changes: close!");        }
        if ( this.auditorium )      { System.out.println("Changes: auditorium!");   }
        if ( this.topic )           { System.out.println("Changes: topic!");        }
        if ( this.lastused )        { System.out.println("Changes: lastused!");     }
        if ( this.description )     { System.out.println("Changes: description!");  }
        if ( this.topiclock )       { System.out.println("Changes: topiclock!");    }
        if ( this.modelock )        { System.out.println("Changes: modelock!");     }
        if ( this.keeptopic )       { System.out.println("Changes: keeptopic!");    }
        if ( this.ident )           { System.out.println("Changes: ident!");        }
        if ( this.restrict )        { System.out.println("Changes: restrict!");     }
        if ( this.verbose )         { System.out.println("Changes: verbose!");      }
        if ( this.leaveops )        { System.out.println("Changes: leaveops!");     }
        if ( this.autoakick )       { System.out.println("Changes: autoakick!");    }
    }

    
}
