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
   
    public boolean is ( int mode )  {
         switch ( mode )  { 
            case NOOP :
                return this.noOp;

            case NEVEROP :
                return this.neverOp;

            case MAILBLOCKED :
                return this.mailBlock;

            case SHOWEMAIL :
                return this.showEmail;

            case SHOWHOST :
                return this.showHost;

            case AUTH :
                return this.auth;
                
            /* Oper */
            case MARKED :
            case MARK :
                return this.mark.length() > 0;
            
            case FROZEN :
            case FREEZE :
                return this.freeze.length() > 0;

            case HELD :
            case HOLD :
                return this.hold.length() > 0;

            case NOGHOST :
                return this.noghost.length() > 0;
            
            default: 
                return false;

         } 
    }   

    public void set ( int mode, boolean state )  {
        switch ( mode )  { 
            case NOOP :
               this.noOp = state; 
               break; 

            case NEVEROP :
               this.neverOp = state;
               break; 

            case MAILBLOCKED :
               this.mailBlock = state;
               break;

            case SHOWEMAIL :
               this.showEmail = state;
               break;

            case SHOWHOST :
               this.showHost = state;
               break;

            case AUTH :
               this.auth = state;
               break;

            default :

         }
    }
    
    public void set ( int mode, String instater )  {
        if ( instater == null ) {
            instater = new String ( );
        } 
        switch ( mode ) { 
            case MARKED :
            case MARK :
               this.mark = instater;
               break;
            
            case FROZEN :
            case FREEZE :
               this.freeze = instater;
               break;

            case HELD :
            case HOLD :
               this.hold = instater;
               break;
               
            case NOGHOST :
               this.noghost = instater;
               break;
               
            default :
                
        }
    }
    public String getInstater ( int mode ) {
        switch ( mode ) {
            case MARKED :
            case MARK :
                return this.mark;
            
            case FREEZE :
            case FROZEN :
                return this.freeze;
                
            case HOLD :
            case HELD :
                return this.hold;
                
            case NOGHOST :
                return this.noghost;
            
            default :
                return "Unknown";
        }
    }
    public String modeString ( int mode )  {
        switch ( mode )  { 
            case NOOP :
                return "NoOp";

            case NEVEROP :
                return "NeverOp";

            case MAILBLOCKED :
                return "MailBlock";

            case SHOWEMAIL :
                return "ShowEmail";

            case SHOWHOST :
                return "ShowHost";

            case MARKED :
            case MARK :
                return "Marked";

            case FROZEN :
            case FREEZE :
                return "Frozen";

            case HELD :
            case HOLD :
                return "Held";

            case NOGHOST :
                return "NoGhost";

            default:
                return "";

        } 
    } 
     
    private String isFirst ( boolean first ) {
        return ( ! first ? ", " : "" ); 
    }
    
    public String getInfoStr ( )  {
        String buf      = new String ( );
        boolean first   = true;         
        int[] sList = { NOOP, NEVEROP, MAILBLOCKED, 
                        SHOWEMAIL, SHOWHOST, MARK, 
                        FREEZE, HOLD, NOGHOST };
        
        for ( int setting : sList ) {
            if ( is ( setting ) ) {
                buf += this.isFirst ( first ); 
                buf += this.modeString ( setting );
                first = true;
            }
        }
        return buf; 
    }  
  
    public void allFalse ( )  {
        this.mailBlock  = false;
        this.neverOp    = false;
        this.noOp       = false; 
        this.showEmail  = false;
        this.showHost   = false;
        this.auth       = false;
        this.mark       = new String ( );
        this.freeze     = new String ( );
        this.hold       = new String ( );
        this.noghost    = new String ( );
    }

    public boolean getAuth ( )  {
        return this.auth;
    }
   
    public static String hashToStr ( int hash ) {
        switch ( hash ) {
            case FREEZE :
                return "FREEZE";
            case HOLD :
                return "HOLD";
        }
        return "";
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
 