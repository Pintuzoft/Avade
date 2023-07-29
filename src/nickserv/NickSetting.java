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

import core.HashNumeric;
import core.HashString;

/**
 * create table nicksetting  ( name varchar ( 32 ) , enforce bool not null, secure bool not null, private bool not null, noop bool not null, neverop bool not null, mailblock bool not null, showemail bool not null, showhost bool not null, primary key  ( name ) , constraint foreign key  ( name )  references nick  ( name )  on delete cascade on update cascade )  ENGINE=InnoDB;
 * @author DreamHealer
 */
    /*   mysql> desc nicksetting;
        +-----------+-------------+------+-----+---------+-------+
        | Field     | Type        | Null | Key | Default | Extra |
        +-----------+-------------+------+-----+---------+-------+
        | name      | varchar ( 32 )  | YES  | MUL | NULL    |       |
        | noop      | tinyint ( 1 )   | NO   |     | NULL    |       |
        | neverop   | tinyint ( 1 )   | NO   |     | NULL    |       |
        | mailblock | tinyint ( 1 )   | NO   |     | NULL    |       |
        | showemail | tinyint ( 1 )   | NO   |     | NULL    |       |
        | showhost  | tinyint ( 1 )   | NO   |     | NULL    |       |
        +-----------+-------------+------+-----+---------+-------+
        9 rows in set  ( 0.00 sec ) 
     */

public class NickSetting extends HashNumeric {
    private boolean noOp;
    private boolean neverOp;
    private boolean mailBlock;
    private boolean showEmail;
    private boolean showHost;
    private boolean auth;
    private int access;
    /* Oper */
    private String mark;
    private String freeze;
    private String hold;
    private String noghost;
     
 
    public NickSetting ( )  {
        /* Nickname Registration */
        this.allFalse ( );
    }
   
    public boolean is ( HashString it )  {
        if      ( it.is(NOOP) )             { return this.noOp;                 }
        else if ( it.is(NEVEROP) )          { return this.neverOp;              }
        else if ( it.is(MAILBLOCKED) )      { return this.mailBlock;            }
        else if ( it.is(SHOWEMAIL) )        { return this.showEmail;            }
        else if ( it.is(SHOWHOST) )         { return this.showHost;             }
        else if ( it.is(AUTH) )             { return this.auth;                 }
        else if ( it.is(MARKED) )           { return this.mark.length() > 0;    }
        else if ( it.is(MARK) )             { return this.mark.length() > 0;    }
        else if ( it.is(FROZEN) )           { return this.freeze.length() > 0;  }
        else if ( it.is(FREEZE) )           { return this.freeze.length() > 0;  }
        else if ( it.is(HELD) )             { return this.hold.length() > 0;    }
        else if ( it.is(HOLD) )             { return this.hold.length() > 0;    }
        else if ( it.is(NOGHOST) )          { return this.noghost.length() > 0; }
        else {
            return false;
        }
    }   

    public void set ( HashString it, boolean state )  {
        if      ( it.is(NOOP) )             { this.noOp         = state;        }
        else if ( it.is(NEVEROP) )          { this.neverOp      = state;        }
        else if ( it.is(MAILBLOCKED) )      { this.mailBlock    = state;        }
        else if ( it.is(SHOWEMAIL) )        { this.showEmail    = state;        }
        else if ( it.is(SHOWHOST) )         { this.showHost     = state;        }
        else if ( it.is(AUTH) )             { this.auth         = state;        }
    }
    
    public void set ( HashString it, String instater )  {
        this.set(it, new HashString ( instater ) );
    }
     
    public void set ( HashString it, HashString instater ) {
        if ( instater == null ) {
            instater = new HashString ( "" );
        }
        
        if ( it.is(MARKED) )                { this.mark = instater.getString();     }
        else if ( it.is(MARK) )             { this.mark = instater.getString();     }
        else if ( it.is(FROZEN) )           { this.freeze = instater.getString();   }
        else if ( it.is(FREEZE) )           { this.freeze = instater.getString();   }
        else if ( it.is(HELD) )             { this.hold = instater.getString();     }
        else if ( it.is(HOLD) )             { this.hold = instater.getString();     }
        else if ( it.is(NOGHOST) )          { this.noghost = instater.getString();  }
    }
    
    public String getInstater ( HashString it ) {
        if      ( it.is(MARKED) )           { return this.mark;                 }
        else if ( it.is(MARK) )             { return this.mark;                 }
        else if ( it.is(FROZEN) )           { return this.freeze;               }
        else if ( it.is(FREEZE) )           { return this.freeze;               }
        else if ( it.is(HELD) )             { return this.hold;                 }
        else if ( it.is(HOLD) )             { return this.hold;                 }
        else if ( it.is(NOGHOST) )          { return this.noghost;              }
        else {
            return "Unknown";
        }
    }
    
    public String modeString ( HashString it )  {
        if      ( it.is(NOOP) )             { return "NoOp";                    }
        else if ( it.is(NEVEROP) )          { return "NeverOp";                 }
        else if ( it.is(MAILBLOCKED) )      { return "MailBlock";               }
        else if ( it.is(SHOWEMAIL) )        { return "ShowEmail";               }
        else if ( it.is(SHOWHOST) )         { return "ShowHost";                }
        else if ( it.is(MARKED) )           { return "Marked";                  }
        else if ( it.is(MARK) )             { return "Marked";                  }
        else if ( it.is(FROZEN) )           { return "Frozen";                  }
        else if ( it.is(FREEZE) )           { return "Frozen";                  }
        else if ( it.is(HELD) )             { return "Held";                    }
        else if ( it.is(HOLD) )             { return "Held";                    }
        else if ( it.is(NOGHOST) )          { return "NoGhost";                 }
        else {
            return "";
        }
    } 
     
    
    public String getInfoStr ( )  {
        String buf = "";
        boolean first = true;         
        HashString[] sList = { 
            NOOP, NEVEROP, MAILBLOCKED, 
            SHOWEMAIL, SHOWHOST, MARK, 
            FREEZE, HOLD, NOGHOST 
        };
        
        for ( HashString setting : sList ) {
            if ( is ( setting ) ) {
                buf += this.isFirst ( first ); 
                buf += this.modeString ( setting );
                first = false;
            }
        }
        return buf; 
    }  
    private String isFirst ( boolean first ) {
        return ( first ? "" : ", " ); 
    }
    
    public void allFalse ( )  {
        this.mailBlock  = false;
        this.neverOp    = false;
        this.noOp       = false; 
        this.showEmail  = false;
        this.showHost   = false;
        this.auth       = false;
        this.mark       = "";
        this.freeze     = "";
        this.hold       = "";
        this.noghost    = "";
    }

    public boolean getAuth ( )  {
        return this.auth;
    }
   
    public static String hashToStr ( HashString it ) {
        if      ( it.is(FREEZE) )           { return "FREEZE";                  }
        else if ( it.is(HOLD) )             { return "HOLD";                    }
        else {
            return "";
        }    
    }
    
    public void printSettings ( ) {
        System.out.println ( "NickSetting Mailblock: "+this.mailBlock );
        System.out.println ( "NickSetting NeverOp: "+this.neverOp );
        System.out.println ( "NickSetting NoOp: "+this.noOp );
        System.out.println ( "NickSetting ShowEmail: "+this.showEmail );
        System.out.println ( "NickSetting ShowHost: "+this.showHost );
        System.out.println ( "NickSetting Mark: "+this.mark );
        System.out.println ( "NickSetting Freeze: "+this.freeze );
        System.out.println ( "NickSetting Hold: "+this.hold );
        System.out.println ( "NickSetting NoGhost: "+this.noghost );
    }
}
 