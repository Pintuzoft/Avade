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
import channel.Chan;
import channel.ChanMode;
import core.HashNumeric;
/**
 * Class for mode lock. Values of modes can me -1, 0 or 1.
 * @author fredde
 */
class ModeLock extends HashNumeric {
    private String modes;
    private int m_registered;   /* +r */
    private int m_modregjoin;   /* +R */
    private int m_topic;        /* +t */
    private int m_noprivmsgs;   /* +n */
    private int m_invite;       /* +i */ 
    private int m_secret;       /* +s */
    private int m_private;      /* +p */
    private int m_modregchat;   /* +M */ 
    private int m_noctrl;       /* +c */
    private int m_oper;         /* +O */
    private int m_moderate;     /* +m */
    private int m_key;          /* -k */

    public ModeLock ( String modes )  {
        this.init ( ); 
        this.parseLock ( modes );
    }
    
    private void init ( )  {
        this.modes = new String ( );
        this.m_registered   = 1;
        this.m_modregjoin   = 0;
        this.m_topic        = 0;
        this.m_noprivmsgs   = 0;
        this.m_invite       = 0; 
        this.m_secret       = 0;
        this.m_private      = 0;
        this.m_modregchat   = 0; 
        this.m_noctrl       = 0;
        this.m_oper         = 0;
        this.m_moderate     = 0;
        this.m_key          = 0;
    }

    private void parseLock ( String data )  {
        int state = 0;
        this.init ( );
        this.modes = data;
        for ( int index = 0; index < data.length ( ); index++ ) {
            System.out.println("Parsing mlock: "+data.charAt(index));
            switch ( ( ""+data.charAt ( index ) ).hashCode ( ) ) {
               case MODE_PLUS :
                   state = 1;
                   break;
                   
               case MODE_MINUS :
                   state = -1;
                   break;
                   
               case MODE_R :
                   this.m_modregjoin = state;
                   break;
                   
               case MODE_t :
                   this.m_topic = state;
                   break;
                   
               case MODE_n :
                   this.m_noprivmsgs = state;
                   break;
                   
               case MODE_i : 
                   this.m_invite = state;
                   break;
                   
               case MODE_s :
                   this.m_secret = state;
                   break;
                   
               case MODE_p :
                   this.m_private = state; 
                   break;
                   
               case MODE_M :
                   this.m_modregchat = state;
                   break;
                   
               case MODE_c :
                   this.m_noctrl = state;
                   break;
                   
               case MODE_O :
                   this.m_oper = state;
                   break;
                   
               case MODE_m :
                   this.m_moderate = state;
                   break;
                   
               case MODE_k :
                   if ( this.m_key == -1 ) { 
                       this.m_key = -1; 
                   }  
                   break;
                   
               default : 
                   
           }   
       }
    } 
     
    public String getModes ( )  {
        return this.modes;
    }
    
    public String getMissingModes ( Chan c, ChanInfo ci ) {
        ChanMode modes = c.getModes();
        String missing;
 
        // Enable
        missing = "+";
        if ( this.m_invite == 1 && ! modes.is ( MODE_i ) ) 
            missing += "i";
        if ( this.m_moderate == 1 && ! modes.is ( MODE_m ) ) 
            missing += "m";
        if ( this.m_modregchat == 1 && ! modes.is ( MODE_M ) ) 
            missing += "M";
        if ( this.m_modregjoin == 1 && ! modes.is ( MODE_R ) ) 
            missing += "R";
        if ( this.m_noctrl == 1 && ! modes.is ( MODE_c ) ) 
            missing += "c";
        if ( this.m_noprivmsgs == 1 && ! modes.is ( MODE_n ) ) 
            missing += "n";
        if ( this.m_private == 1 && ! modes.is ( MODE_p ) ) 
            missing += "p";
        if ( this.m_secret == 1 && ! modes.is ( MODE_s ) ) 
            missing += "s";
        if ( this.m_topic == 1 && ! modes.is ( MODE_t ) ) 
            missing += "t";
        
        // Disable
        missing += "-";
        if ( this.m_invite == -1 && modes.is ( MODE_i ) )
            missing += "i";
        if ( this.m_moderate == -1 && modes.is ( MODE_m ) )
            missing += "i";
        if ( this.m_modregchat == -1 && modes.is ( MODE_M ) )
            missing += "M";
        if ( this.m_modregjoin == -1 && modes.is ( MODE_R ) )
            missing += "R";
        if ( this.m_noctrl == -1 && modes.is ( MODE_c ) )
            missing += "c";
        if ( this.m_noprivmsgs == -1 && modes.is ( MODE_n ) )
            missing += "n";
        if ( this.m_private == -1 && modes.is ( MODE_p ) )
            missing += "p";
        if ( this.m_secret == -1 && modes.is ( MODE_s ) )
            missing += "s";
        if ( this.m_topic == -1 && modes.is ( MODE_t ) )
            missing += "i";
        
        return missing;
    }
    
}
 