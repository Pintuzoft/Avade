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

import core.Proc;
import core.HashNumeric;
import core.HashString;

/**
 * @author DreamHealer
 */
    /*   mysql> desc nicksetting;
        +-----------+-------------+------+-----+---------+-------+
        | Field     | Type        | Null | Key | Default | Extra |
        +-----------+-------------+------+-----+---------+-------+
        | name      | varchar ( 32 )  | YES  | MUL | NULL    |       |
        | enforce   | tinyint ( 1 )   | NO   |     | NULL    |       |
        | secure    | tinyint ( 1 )   | NO   |     | NULL    |       |
        | noop      | tinyint ( 1 )   | NO   |     | NULL    |       |
        | neverop   | tinyint ( 1 )   | NO   |     | NULL    |       |
        | mailblock | tinyint ( 1 )   | NO   |     | NULL    |       |
        | showemail | tinyint ( 1 )   | NO   |     | NULL    |       |
        | showhost  | tinyint ( 1 )   | NO   |     | NULL    |       |
        +-----------+-------------+------+-----+---------+-------+
        9 rows in set  ( 0.00 sec ) 
     */

public class ChanSetting extends HashNumeric {
    private boolean     keepTopic;
    private HashString  topicLock;
    private boolean     ident;
    private boolean     opGuard;
    private boolean     restrict;
    private boolean     verbose;
    private boolean     mailBlock;
    private boolean     leaveOps;
    private boolean     autoAkick;
    private ModeLock    modeLock;

    private String      mark;
    private String      freeze;
    private String      close;
    private String      hold;
    private String      auditorium;
 
    /**
     *
     */
    public ChanSetting ( )  {
        /* Channel Registration */
        this.allFalse ( );
    }
   
    /**
     *
     * @param it
     * @return
     */
    public boolean is ( HashString it )  {
        if ( it.is(TOPICLOCK) ) { 
            return  ( this.topicLock.is(AOP) || 
                      this.topicLock.is(SOP) || 
                      this.topicLock.is(FOUNDER) );
        }
        if      ( it.is(KEEPTOPIC) )    { return this.keepTopic;                }
        else if ( it.is(IDENT) )        { return this.ident;                    }
        else if ( it.is(OPGUARD) )      { return this.opGuard;                  }
        else if ( it.is(RESTRICT) )     { return this.restrict;                 }
        else if ( it.is(VERBOSE) )      { return this.verbose;                  }
        else if ( it.is(MAILBLOCKED) )  { return this.mailBlock;                }
        else if ( it.is(LEAVEOPS) )     { return this.leaveOps;                 }
        else if ( it.is(AUTOAKICK) )    { return this.autoAkick;                }
        else if ( it.is(MARKED) )       { return this.mark.length() > 0;        }
        else if ( it.is(MARK) )         { return this.mark.length() > 0;        }
        else if ( it.is(FROZEN) )       { return this.freeze.length() > 0;      }
        else if ( it.is(FREEZE) )       { return this.freeze.length() > 0;      }
        else if ( it.is(CLOSED) )       { return this.close.length() > 0;       }
        else if ( it.is(CLOSE) )        { return this.close.length() > 0;       }
        else if ( it.is(HELD) )         { return this.hold.length() > 0;        }
        else if ( it.is(HOLD) )         { return this.hold.length() > 0;        }
        else if ( it.is(AUDITORIUM) )   { return this.auditorium.length() > 0;  }
        else {
            return false;
        }
    } 
    
    /**
     *
     * @return
     */
    public HashString getTopicLock ( ) {
        return this.topicLock;
    }
    
    /**
     *
     * @param it
     * @param state
     */
    public void set ( HashString it, boolean state ) {
        if      ( it.is(KEEPTOPIC) )    { this.keepTopic    = state;            }
        else if ( it.is(IDENT) )        { this.ident        = state;            }
        else if ( it.is(OPGUARD) )      { this.opGuard      = state;            }
        else if ( it.is(RESTRICT) )     { this.restrict     = state;            }
        else if ( it.is(VERBOSE) )      { this.verbose      = state;            }
        else if ( it.is(MAILBLOCKED) )  { this.mailBlock    = state;            }
        else if ( it.is(LEAVEOPS) )     { this.leaveOps     = state;            }
        else if ( it.is(AUTOAKICK) )    { this.autoAkick    = state;            }
    }  
    
    /**
     *
     * @param it
     * @param instater
     */
    public void set ( HashString it, String instater ) {
        if ( instater == null ) {
            instater = new String ( );
        }
        if      ( it.is(MARKED) )       { this.mark         = instater;         }
        else if ( it.is(MARK) )         { this.mark         = instater;         }
        else if ( it.is(FROZEN) )       { this.freeze       = instater;         }
        else if ( it.is(FREEZE) )       { this.freeze       = instater;         }
        else if ( it.is(CLOSED) )       { this.close        = instater;         }
        else if ( it.is(CLOSE) )        { this.close        = instater;         }
        else if ( it.is(HELD) )         { this.hold         = instater;         }
        else if ( it.is(HOLD) )         { this.hold         = instater;         }
        else if ( it.is(AUDITORIUM) )   { this.auditorium   = instater;         }
    }
    
    /**
     *
     * @param it
     * @return
     */
    public String getInstater ( HashString it ) {
        if      ( it.is(MARKED) )       { return this.mark;                     }
        else if ( it.is(MARK) )         { return this.mark;                     }
        else if ( it.is(FROZEN) )       { return this.freeze;                   }
        else if ( it.is(FREEZE) )       { return this.freeze;                   }
        else if ( it.is(CLOSED) )       { return this.close;                    }
        else if ( it.is(CLOSE) )        { return this.close;                    }
        else if ( it.is(HELD) )         { return this.hold;                     }
        else if ( it.is(HOLD) )         { return this.hold;                     }
        else if ( it.is(AUDITORIUM) )   { return this.auditorium;               }
        else {
            return "Unknown";
        }
    }
    
    /**
     *
     * @param it
     * @param state
     */
    public void set ( HashString it, HashString state ) {
        if ( it.is(TOPICLOCK) ) {
           this.topicLock = state;
        }
    }  
    
    /**
     *
     * @param it
     * @return
     */
    public String modeString ( HashString it ) {
        if ( it.is(TOPICLOCK) ) {
            if ( this.topicLock.is(AOP))        { return "TopicLock (AOP)";     }
            if ( this.topicLock.is(SOP))        { return "TopicLock (SOP)";     }
            if ( this.topicLock.is(FOUNDER))    { return "TopicLock (Founder)"; }
        }
        if      ( it.is(KEEPTOPIC) )    { return "KeepTopic";                   }
        else if ( it.is(IDENT) )        { return "Ident";                       }
        else if ( it.is(OPGUARD) )      { return "OpGuard";                     }
        else if ( it.is(RESTRICT) )     { return "Restrict";                    }
        else if ( it.is(VERBOSE) )      { return "Verbose";                     }
        else if ( it.is(MAILBLOCKED) )  { return "MailBlock";                   }
        else if ( it.is(LEAVEOPS) )     { return "LeaveOps";                    }
        else if ( it.is(AUTOAKICK) )    { return "AutoAkick";                   }
        else if ( it.is(MARKED) )       { return "Marked";                      }
        else if ( it.is(MARK) )         { return "Marked";                      }
        else if ( it.is(FROZEN) )       { return "Frozen";                      }
        else if ( it.is(FREEZE) )       { return "Frozen";                      }
        else if ( it.is(CLOSED) )       { return "Closed";                      }
        else if ( it.is(CLOSE) )        { return "Closed";                      }
        else if ( it.is(HELD) )         { return "Held";                        }
        else if ( it.is(HOLD) )         { return "Held";                        }
        else if ( it.is(AUDITORIUM) )   { return "Auditorium";                  }
        else {
            return "";     
        } 
    } 
    
    /**
     *
     * @param modes
     */
    public void setModeLock ( String modes )  {
        this.modeLock = new ModeLock ( modes );
    }
    
    /**
     *
     * @return
     */
    public ModeLock getModeLock ( )  {
        return this.modeLock;
    }
    
    private String isFirst ( boolean first ) {
        return ! first ? ", " : ""; 
    }
    
    /**
     *
     * @return
     */
    public String getInfoStr ( )  {
        String buf = new String ( );
        boolean first = true;
        HashString[] sList = { KEEPTOPIC, IDENT, OPGUARD, 
                               RESTRICT, MAILBLOCKED, VERBOSE,
                               LEAVEOPS, AUTOAKICK, MARKED,
                               FROZEN, CLOSED, HELD, 
                               AUDITORIUM };
        try { 
            for ( HashString setting : sList ) {
                if ( is ( setting ) ) {
                    buf += this.isFirst ( first );
                    buf += this.modeString ( setting );
                }
            }
            
            if ( is(TOPICLOCK) && ! this.topicLock.is(OFF) )  {
                String acc = new String ( );
                if ( this.topicLock.is(AOP) ) {
                    acc = "AOP";
                } else if ( this.topicLock.is(SOP) ) {
                    acc = "SOP";
                } else if ( this.topicLock.is(FOUNDER) ) {
                    acc = "FOUNDER";
                }
                 
                buf += this.isFirst ( first );
                buf += this.modeString ( TOPICLOCK ) +" ( "+acc+" ) ";
                first = false;
            }

            return buf; 
        } catch ( Exception e )  {
            Proc.log ( ChanSetting.class.getName ( ) , e );
        }
        return "";
    }  

    /**
     *
     */
    public void allFalse ( )  {
        this.keepTopic          = false;
        this.topicLock          = OFF;
        this.mailBlock          = false;
        this.modeLock           = new ModeLock ( "" );
        this.verbose            = false;
        this.restrict           = false;
        this.opGuard            = false;
        this.ident              = false;
        this.leaveOps           = false;
        this.autoAkick          = false;
        /* Oper only */
        this.mark               = new String ( );
        this.freeze             = new String ( );
        this.close              = new String ( );
        this.hold               = new String ( );
        this.auditorium         = new String ( );
    } 

    boolean isTopicLock ( HashString state ) { 
        return this.topicLock.is(state);
    }

    /**
     *
     * @param hash
     * @return
     */
    public static String hashToStr ( HashString hash ) {
        if ( hash.is(FROZEN) )          { return "FREEZE";                      }
        if ( hash.is(FREEZE) )          { return "FREEZE";                      }
        if ( hash.is(CLOSED) )          { return "CLOSE";                       }
        if ( hash.is(CLOSE) )           { return "CLOSE";                       }
        if ( hash.is(HELD) )            { return "HOLD";                        }
        if ( hash.is(HOLD) )            { return "HOLD";                        }
        if ( hash.is(AUDITORIUM) )      { return "AUDITORIUM";                  }
        return "";
    }
    
}
 