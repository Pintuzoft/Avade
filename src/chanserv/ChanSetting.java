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
    private int         topicLock;
    private boolean     ident;
    private boolean     opGuard;
    private boolean     restrict;
    private boolean     verbose;
    private boolean     mailBlock;
    private boolean     leaveOps;
    private ModeLock    modeLock;

    private String      mark;
    private String      freeze;
    private String      close;
    private String      hold;
    private String      auditorium;
 
    public ChanSetting ( )  {
        /* Channel Registration */
        this.allFalse ( );
    }
   
    public boolean is ( int setting )  {
        System.out.println( "debug: is:"+setting );
        switch ( setting )  {
            case KEEPTOPIC :
                return this.keepTopic;

            case TOPICLOCK :
                return  (  this.topicLock == AOP || 
                           this.topicLock == SOP || 
                           this.topicLock == FOUNDER );

            case IDENT :
                return this.ident;

            case OPGUARD :
                return this.opGuard;

            case RESTRICT :
                return this.restrict;

            case VERBOSE :
                return this.verbose;

            case MAILBLOCKED :
                return this.mailBlock;

            case LEAVEOPS :
                return this.leaveOps;

            case MARKED :
            case MARK :
                return this.mark.length() > 0;

            case FROZEN :
            case FREEZE :
                return this.freeze.length() > 0;

            case CLOSED :
            case CLOSE :
                return this.close.length() > 0;

            case HELD :
            case HOLD :
                return this.hold.length() > 0;

            case AUDITORIUM :
                return this.auditorium.length() > 0;

            default :
                return false;
                 
         } 
    } 
    
    public int getTopicLock ( )  {
        return this.topicLock;
    }
    
    public void set ( int mode, boolean state )  {
         switch ( mode )  {
             case KEEPTOPIC :
                 this.keepTopic = state; 
                 break;
                 
             case IDENT :
                 this.ident = state;
                 break;
                 
             case OPGUARD :
                 this.opGuard = state;
                 break;
                 
             case RESTRICT :
                 this.restrict = state;
                 break;
                 
             case VERBOSE :
                 this.verbose = state;
                 break;
                 
             case MAILBLOCKED :
                 this.mailBlock = state;
                 break;
                 
             case LEAVEOPS :
                 this.leaveOps = state;
                 break;
                 
             default :
         } 
    }  
    
    public void set ( int mode, String instater )  {
        if ( instater == null ) {
            instater = new String ( );
        }
        switch ( mode )  {
            case MARK :
                this.mark = instater;
                break;
             
            case FREEZE :
                this.freeze = instater;
                break;
             
            case CLOSE :
                this.close = instater;
                break;
             
            case HOLD :
                this.hold = instater;
                break;
                 
            case AUDITORIUM :
                this.auditorium = instater;
                break;
                
            default :
                
        }
    }
    
    public String getInstater ( int mode ) {
        switch ( mode ) {
            case MARK :
                return this.mark;
            
            case HOLD :
                return this.hold;
            
            case FREEZE :
                return this.freeze;
            
            case CLOSE :
                return this.close;
            
            case AUDITORIUM :
                return this.auditorium;
                
            default :
                return "Unknown";
        }
    }
    
    public void set ( int mode, int state )  {
         switch ( mode )  {
             case TOPICLOCK :
                 this.topicLock = state;
                 break;
                 
             default :
         }
    }  
    
    public String modeString ( int mode )  {
        switch ( mode )  {
            case KEEPTOPIC :
                return "Keeptopic";
                 
            case TOPICLOCK :
                return "Topiclock";
                 
            case IDENT :
                return "Ident";
                 
            case OPGUARD :
                return "OpGuard";
                 
            case RESTRICT :
                return "Restrict";
                 
            case VERBOSE :
                return "Verbose"; 
                 
            case MAILBLOCKED :
                return "MailBlock";
                 
            case LEAVEOPS :
                return "LeaveOps";
                 
            /* Oper Only */
            case MARK :
                return "Marked";
                 
            case FREEZE :
                return "Frozen";
                 
            case CLOSE :
                return "Closed";
                 
            case HOLD :
                return "Held";
                 
            case AUDITORIUM :
                return "Auditorium";
                 
            default :
                return "";
                 
         } 
    } 
    
    public void setModeLock ( String modes )  {
        this.modeLock = new ModeLock ( modes );
    }
    
    public ModeLock getModeLock ( )  {
        return this.modeLock;
    }
    
    private String isFirst ( boolean first ) {
        return ! first ? ", " : ""; 
    }
    
    public String getInfoStr ( )  {
        String buf = new String ( );
        boolean first = true;
        try {
            if ( is ( KEEPTOPIC )  )  {
                buf += this.isFirst ( first );
                buf += this.modeString ( KEEPTOPIC );
                first = false;
            }

            if ( is ( TOPICLOCK )  && this.topicLock != OFF )  {
                String acc = new String ( );
                switch ( this.topicLock )  {
                    case AOP :
                        acc = "AOP";
                        break;
                        
                    case SOP :
                        acc = "SOP";
                        break; 
                        
                    case FOUNDER :
                        acc = "FOUNDER";
                        break;
                        
                    default :
                }
                buf += this.isFirst ( first );
                buf += this.modeString ( TOPICLOCK ) +" ( "+acc+" ) ";
                first = false;
            }

            if ( is ( IDENT ) ) {
                buf += this.isFirst ( first );
                buf += this.modeString ( IDENT );
                first = false;
            }

            if ( is ( OPGUARD ) ) {
                buf += this.isFirst ( first );
                buf += this.modeString ( OPGUARD );
                first = false;
            }

            if ( is ( RESTRICT ) ) {
                buf += this.isFirst ( first );
                buf += this.modeString ( RESTRICT );
                first = false;
            }

            if ( is ( MAILBLOCKED ) ) {
                buf += this.isFirst ( first );
                buf += this.modeString ( MAILBLOCKED );
                first = false;
            }

            if ( is ( VERBOSE ) ) {
                buf += this.isFirst ( first );
                buf += this.modeString ( VERBOSE );
                first = false;
            }

            if ( is ( LEAVEOPS ) ) {
                buf += this.isFirst ( first );
                buf += this.modeString ( LEAVEOPS );
            }      

            /* Oper only */
            if ( is ( MARKED ) ) {
                buf += this.isFirst ( first );
                buf += this.modeString ( MARK );
            }
            
            if ( is ( FROZEN ) ) {
                buf += this.isFirst ( first );
                buf += this.modeString ( FREEZE );
            }
            
            if ( is ( CLOSED ) ) {
                buf += this.isFirst ( first );
                buf += this.modeString ( CLOSE );
            }
            
            if ( is ( HELD ) ) {
                buf += this.isFirst ( first );
                buf += this.modeString ( HOLD );
            }

            if ( is ( AUDITORIUM ) ) {
                buf += this.isFirst ( first );
                buf += this.modeString ( AUDITORIUM );
            }
            return buf; 
        } catch ( Exception e )  {
            Proc.log ( ChanSetting.class.getName ( ) , e );
        }
        return "";
    }  

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
        /* Oper only */
        this.mark               = new String ( );
        this.freeze             = new String ( );
        this.close              = new String ( );
        this.hold               = new String ( );
        this.auditorium         = new String ( );
    } 

    boolean isTopicLock ( int state ) { 
        return this.topicLock == state;
    }

    public static String hashToStr ( int hash ) {
        switch ( hash ) {
            case FREEZE :
                return "FREEZE";

            case CLOSE :
                return "CLOSE";

            case HOLD :
                return "HOLD";

            case AUDITORIUM :
                return "AUDITORIUM";

        }
        return "";
    }
    
}
 