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
    protected String name;
    protected String flag;
    protected int    flagInt;
    protected String mask;
    protected String oper;
    protected String stamp;
    protected DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public LogEvent ( String name, String flag, String mask, String oper, String stamp ) {
        this.name = name;
        this.flag = flag;
        if ( flag != null ) {
            this.flagInt = flag.toUpperCase().hashCode();
        }
        this.mask = mask;
        this.oper = oper;
        this.stamp = stamp.substring ( 0, 19 );
    }

    public LogEvent (  String name, String mask, String oper ) {
        this.name = name;
        this.mask = mask;
        this.oper = oper;
        this.stamp = dateFormat.format( new Date ( ) );
    }

    public void setStamp ( String stamp ) {
        this.stamp = stamp;
    }
    
    static public String getNickFlagByHash ( int hash ) {
        switch ( hash ) {
            case AUTHMAIL :
                return "Am";
                
            case AUTHPASS :
                return "Ap";
                
            case DROP :
                return "D";
                
            case DELETE :
                return "D!";
            
            case EXPIRE :
                return "E";
              
            case EXPIREAUTH :
                return "Ea";
              
            case MAIL :
                return "m";
            
            case MARK :
                return "M+";
            
            case UNMARK :
                return "M-";
                      
            case FREEZE :
                return "F+";
            
            case UNFREEZE :
                return "F-";
            
            case HOLD :
                return "H+";
            
            case UNHOLD :
                return "H-";
            
            case GETEMAIL :
                return "GE";
            
            case GETPASS :
                return "GP";
            
            case REGISTER :
                return "R";
 
            case SENDPASS :
                return "SP";
            
            case WIPE :
                return "W";
 
             default :
                return null;
        }
    }
    static public String getChanFlagByHash ( int hash ) {
        switch ( hash ) {
            case AUDITORIUM :
                return "A+";
                
            case UNAUDITORIUM :
                return "A-";
                
            case CLOSE :
                return "C+";
                
            case REOPEN :
                return "C-";
            
            case DROP :
                return "D";

            case DELETE :
                return "D!";
            
            case EXPIRE :
                return "E";
              
            case EXPIREFOUNDER :
                return "Ef";
              
            case EXPIREINACTIVE :
                return "Ei";
              
            case MARK :
                return "M+";
            
            case UNMARK :
                return "M-";
            
            case FREEZE :
                return "F+";
            
            case UNFREEZE :
                return "F-";
            
            case HOLD :
                return "H+";
            
            case UNHOLD :
                return "H-";
            
            case GETPASS :
                return "GP";
            
            case MDEOP :
                return "Md";
            
            case MKICK :
                return "Mk";
            
            case REGISTER :
                return "R";
 
            case SAJOIN :
                return "SJ";
            
            case SAMODE :
                return "SM";
            
            case SENDPASS :
                return "SP";
            
            case TOPICWIPE :
                return "T";
 
            case WIPE :
                return "W";
 
            case WIPEAOP :
                return "Wa";
 
            case WIPESOP :
                return "Ws";
 
            case WIPEAKICK :
                return "Wk";
 
             default :
                return null;
        }
    }
    static public String getOperFlagByHash ( int hash ) {
        switch ( hash ) {
            case ADDMASTER :
                return "Master+";
     
            case DELMASTER :
                return "Master-";
     
            case ADDSRA :
                return "SRA+";
     
            case DELSRA :
                return "SRA-";
     
            case ADDCSOP :
                return "CSop+";
     
            case DELCSOP :
                return "CSop-";
     
            case ADDSA :
                return "SA+";
     
            case DELSA :
                return "SA-";
     
            case ADDIRCOP :
                return "IRCop+";
     
            case DELIRCOP :
                return "IRCop-";
     
            case GLOBAL :
                return "Global";
     
            default :
                return null;
                
        }
    }
      
    public String getName ( ) {
        return name;
    }

    public String getFlag ( ) {
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
     
    public int getFlagInt ( ) {
        return this.flagInt;
    }
    
    
}
