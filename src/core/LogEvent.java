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
package core;
 
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;



/**
 *
 * @author fredde
 */
public abstract class LogEvent extends HashNumeric {
    protected HashString name;
    protected HashString flag;
    protected String mask;
    protected String oper;
    protected String stamp;
    protected DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public LogEvent ( HashString name, HashString flag, String mask, String oper, String stamp ) {
        this.name = name;
        this.flag = flag;
        this.mask = mask;
        this.oper = oper;
        this.stamp = stamp.substring ( 0, 19 );
    }

    public LogEvent ( HashString name, String mask, String oper ) {
        this.name = name;
        this.mask = mask;
        this.oper = oper;
        this.stamp = dateFormat.format( new Date ( ) );
    }

    public void setStamp ( String stamp ) {
        this.stamp = stamp;
    }
    
    static public String getNickFlagByHash ( HashString hash ) {
        if      ( hash.is(AUTHMAIL) )       { return "Am";  }
        else if ( hash.is(AUTHPASS) )       { return "Ap";  }
        else if ( hash.is(DROP) )           { return "D";   }
        else if ( hash.is(DELETE) )         { return "D!";  }
        else if ( hash.is(EXPIRE) )         { return "E";   }
        else if ( hash.is(EXPIREAUTH) )     { return "Ea";  }
        else if ( hash.is(MAIL) )           { return "m";   }
        else if ( hash.is(PASS) )           { return "p";   }
        else if ( hash.is(MARK) )           { return "M+";  }
        else if ( hash.is(UNMARK) )         { return "M-";  }
        else if ( hash.is(FREEZE) )         { return "F+";  }
        else if ( hash.is(UNFREEZE) )       { return "F-";  }
        else if ( hash.is(HOLD) )           { return "H+";  }
        else if ( hash.is(UNHOLD) )         { return "H-";  }
        else if ( hash.is(GETEMAIL) )       { return "GE";  }
        else if ( hash.is(GETPASS) )        { return "GP";  }
        else if ( hash.is(REGISTER) )       { return "R";   }
        else if ( hash.is(SENDPASS) )       { return "SP";  }
        else if ( hash.is(WIPE) )           { return "W";   }
        else {
            return null;
        }
    }
    
    static public String getChanFlagByHash ( HashString hash ) {
        if      ( hash.is(AUDITORIUM) )     { return "A+";  }
        else if ( hash.is(UNAUDITORIUM) )   { return "A-";  }
        else if ( hash.is(CLOSE) )          { return "C+";  }
        else if ( hash.is(REOPEN) )         { return "C-";  }
        else if ( hash.is(DROP) )           { return "D";   }
        else if ( hash.is(DELETE) )         { return "D!";  }
        else if ( hash.is(EXPIRE) )         { return "E";   }
        else if ( hash.is(EXPIREFOUNDER) )  { return "Ef";  }
        else if ( hash.is(EXPIREINACTIVE) ) { return "Ei";  }
        else if ( hash.is(MARK) )           { return "M+";  }
        else if ( hash.is(UNMARK) )         { return "M-";  }
        else if ( hash.is(FREEZE) )         { return "F+";  }
        else if ( hash.is(UNFREEZE) )       { return "F-";  }
        else if ( hash.is(HOLD) )           { return "H+";  }
        else if ( hash.is(UNHOLD) )         { return "H-";  }
        else if ( hash.is(GETPASS) )        { return "GP";  }
        else if ( hash.is(MDEOP) )          { return "Md";  }
        else if ( hash.is(MKICK) )          { return "Mk";  }
        else if ( hash.is(REGISTER) )       { return "R";   }
        else if ( hash.is(SAJOIN) )         { return "SJ";  }
        else if ( hash.is(SAMODE) )         { return "SM";  }
        else if ( hash.is(SENDPASS) )       { return "SP";  }
        else if ( hash.is(TOPICWIPE) )      { return "Wt";  }
        else if ( hash.is(WIPE) )           { return "W";   }
        else if ( hash.is(WIPEAOP) )        { return "Wa";  }
        else if ( hash.is(WIPESOP) )        { return "Ws";  }
        else if ( hash.is(WIPEAKICK) )      { return "Wk";  }
        else {
            return null;
        }
    }
    
    static public String getOperFlagByHash ( HashString hash ) {
        if      ( hash.is(ADDMASTER) )      { return "Master+";     }
        else if ( hash.is(DELMASTER) )      { return "Master-";     }
        else if ( hash.is(ADDSRA) )         { return "SRA+";        }
        else if ( hash.is(DELSRA) )         { return "SRA-";        }
        else if ( hash.is(ADDCSOP) )        { return "CSop+";       }
        else if ( hash.is(DELCSOP) )        { return "CSop-";       }
        else if ( hash.is(ADDSA) )          { return "SA+";         }
        else if ( hash.is(DELSA) )          { return "SA-";         }
        else if ( hash.is(ADDIRCOP) )       { return "IRCop+";      }
        else if ( hash.is(DELIRCOP) )       { return "IRCop-";      }
        else if ( hash.is(GLOBAL) )         { return "Global";      }
        else if ( hash.is(FORCENICK) )      { return "FN";          }
        else {
            return null;
        }
    }
      
    public HashString getName ( ) {
        return name;
    }

    public HashString getFlag ( ) {
        return flag;
    }

    public String getMask ( ) {
        return mask;
    }
    
    public String getOper ( ) {
        return oper;
    }
    
    public String getStamp ( ) {
        return stamp;
    }
    
    public boolean isOper ( ) {
        return ( oper != null );
    }
      
}
