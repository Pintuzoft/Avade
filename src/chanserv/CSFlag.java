/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chanserv;

import core.Handler;
import core.HashNumeric;

/**
 *
 * @author Fredrik Karlsson aka DreamHealer & avade.net
 */
public class CSFlag extends HashNumeric {
    private String name;
    private short join_connect_time = 0;
    private short talk_connect_time = 0;
    private short talk_join_time = 0;
    private short max_bans = 200;
    private boolean no_notice = false;
    private boolean no_ctcp = false;
    private boolean no_part_msg = false;
    private boolean no_quit_msg = false;
    private boolean exempt_opped = false;
    private boolean exempt_voiced = false;
    private boolean exempt_identd = false;
    private boolean exempt_registered = false;
    private boolean exempt_invites = false;
    private String greetmsg = null;
    
    public CSFlag ( String name, short join_connect_time, short talk_connect_time, short talk_join_time, short max_bans, 
                    boolean no_notice, boolean no_ctcp, boolean no_part_msg, boolean no_quit_msg, boolean exempt_opped,
                    boolean exempt_voiced, boolean exempt_identd, boolean exempt_registered, boolean exempt_invites, 
                    String greetmsg ) {
        this.name = name;
        this.join_connect_time = join_connect_time;
        this.talk_connect_time = talk_connect_time;
        this.talk_join_time = talk_join_time;
        this.max_bans = max_bans;
        this.no_notice = no_notice;
        this.no_ctcp = no_ctcp;
        this.no_part_msg = no_part_msg;
        this.no_quit_msg = no_part_msg;
        this.exempt_opped = exempt_opped;
        this.exempt_voiced = exempt_voiced;
        this.exempt_identd = exempt_identd;
        this.exempt_registered = exempt_registered;
        this.exempt_invites = exempt_invites;
        this.greetmsg = greetmsg;
    }
    
    public CSFlag ( String name ) {
        this.name = name;
    }
 
    public void syncChangedValuesWithNetwork ( ) {
        String values = new String ( );
        if ( this.join_connect_time != 0 ) 
            values = this.addToValues ( values, "JOIN_CONNECT_TIME:"+this.join_connect_time );
        if ( this.talk_connect_time != 0 ) 
            values = this.addToValues ( values, "TALK_CONNECT_TIME:"+this.talk_connect_time );
        if ( this.talk_join_time != 0 ) 
            values = this.addToValues ( values, "TALK_JOIN_TIME:"+this.talk_join_time );
        if ( this.max_bans != 200 ) 
            values = this.addToValues ( values, "MAX_BANS:"+this.max_bans );
        if ( this.no_notice ) 
            values = this.addToValues ( values, "NO_NOTICE:"+( this.no_notice ? "ON" : "OFF" ) );
        if ( this.no_ctcp ) 
            values = this.addToValues ( values, "NO_CTCP:"+( this.no_ctcp ? "ON" : "OFF" ) );
        if ( this.no_part_msg ) 
            values = this.addToValues ( values, "NO_PART_MSG:"+( this.no_part_msg ? "ON" : "OFF" ) );
        if ( this.no_quit_msg ) 
            values = this.addToValues ( values, "NO_QUIT_MSG:"+( this.no_quit_msg ? "ON" : "OFF" ) );
        if ( this.exempt_opped ) 
            values = this.addToValues ( values, "EXEMPT_OPPED:"+( this.exempt_opped ? "ON" : "OFF" ) );
        if ( this.exempt_voiced ) 
            values = this.addToValues ( values, "EXEMPT_VOICED:"+( this.exempt_voiced ? "ON" : "OFF" ) );
        if ( this.exempt_identd ) 
            values = this.addToValues ( values, "EXEMPT_IDENTD:"+( this.exempt_identd ? "ON" : "OFF" ) );
        if ( this.exempt_registered ) 
            values = this.addToValues ( values, "EXEMPT_REGISTERED:"+( this.exempt_registered  ? "ON" : "OFF" ));
        if ( this.exempt_invites ) 
            values = this.addToValues ( values, "EXEMPT_INVITES:"+( this.exempt_invites ? "ON" : "OFF" ) );
        
        if ( values.length() > 0 ) {
            Handler.getChanServ().sendServ ( "SVSXCF "+this.name+" "+values );
        }
        
        if ( this.greetmsg != null ) {
            Handler.getChanServ().sendServ ( "SVSXCF "+this.name+" GREETMSG:"+this.greetmsg );
        }
    }
    
    private String addToValues ( String values, String value ) {
        if ( values.length() > 0 ) {
            values += " "+value;
        } else {
            values = value;
        }
        return values;
    }
    
    public short getJoin_connect_time() {
        return join_connect_time;
    }

    public short getTalk_connect_time() {
        return talk_connect_time;
    }

    public short getTalk_join_time() {
        return talk_join_time;
    }

    public short getMax_bans() {
        return max_bans;
    }

    public boolean isNo_notice() {
        return no_notice;
    }

    public boolean isNo_ctcp() {
        return no_ctcp;
    }

    public boolean isNo_part_msg() {
        return no_part_msg;
    }
    public boolean isNo_quit_msg() {
        return no_quit_msg;
    }

    public boolean isExempt_opped() {
        return exempt_opped;
    }

    public boolean isExempt_voiced() {
        return exempt_voiced;
    }

    public boolean isExempt_identd() {
        return exempt_identd;
    }

    public boolean isExempt_registered() {
        return exempt_registered;
    }

    public boolean isExempt_invites() {
        return exempt_invites;
    }

    public String getGreetmsg() {
        return greetmsg;
    }

    public void setShortFlag ( int command, short in ) {
        switch ( command ) {
            case JOIN_CONNECT_TIME :
                this.join_connect_time = in;
                break;
            
            case TALK_CONNECT_TIME :
                this.talk_connect_time = in;
                break;

            case TALK_JOIN_TIME :
                this.talk_join_time = in;
                break;

            case MAX_BANS :
                this.max_bans = in;
                break;
                
            default :
                
        }
    }

    public void setBooleanFlag ( int command, boolean in ) {
        switch ( command ) {
            case NO_NOTICE :
                this.no_notice = in;
                break;
                
            case NO_CTCP :
                this.no_ctcp = in;
                break;
                
            case NO_PART_MSG :
                this.no_part_msg = in;
                break;
            
            case NO_QUIT_MSG :
                this.no_quit_msg = in;
                break;
                
            case EXEMPT_OPPED :
                this.exempt_opped = in;
                break;
                
            case EXEMPT_VOICED :
                this.exempt_voiced = in;
                break;
                
            case EXEMPT_IDENTD :
                this.exempt_identd = in;
                break;
                
            case EXEMPT_REGISTERED :
                this.exempt_registered = in;
                break;
                
            case EXEMPT_INVITES :
                this.exempt_invites = in;
                break;
                
            default :
                
        }
    }

    public void setGreetmsg ( String greetmsg ) {
        this.greetmsg = greetmsg;
    }
    
    public boolean isGreetmsg ( ) {
        return ( this.greetmsg != null && this.greetmsg.length() > 0 );
    }
    
    public static boolean isFlag ( String name ) {
        System.out.println("0:"+name+":"+name.toUpperCase().hashCode() );
        switch ( name.toUpperCase().hashCode() ) {
            case JOIN_CONNECT_TIME :
            case TALK_CONNECT_TIME :
            case TALK_JOIN_TIME :
            case MAX_BANS :
            case NO_NOTICE :
            case NO_CTCP :
            case NO_PART_MSG :
            case NO_QUIT_MSG :
            case EXEMPT_OPPED :
            case EXEMPT_VOICED :
            case EXEMPT_IDENTD :
            case EXEMPT_REGISTERED :
            case EXEMPT_INVITES :
            case GREETMSG :
            case LIST :
        System.out.println("1:"+name);
                return true;
                
            default :
        System.out.println("2:"+name);
                return false;
        }
    }
    public static boolean isOkValue ( String flag, String value ) {
        int val;
        if ( value == null ) {
            value = new String ( );
        }
        switch ( flag.toUpperCase().hashCode() ) {
            case JOIN_CONNECT_TIME :
            case TALK_CONNECT_TIME :                
            case TALK_JOIN_TIME :
                try {
                    val = Integer.parseInt ( value );
                    return ( val > -1 && val < 1000000 );
                } catch ( NumberFormatException ex ) {
                    
                }
                return false;

            case MAX_BANS :
                try {
                    val = Integer.parseInt ( value );
                    return ( val > 199 && val < 1000 );
                } catch ( NumberFormatException ex ) {
                    
                }
                return false;

            case NO_NOTICE :
            case NO_CTCP :
            case NO_PART_MSG :
            case NO_QUIT_MSG :
            case EXEMPT_OPPED :
            case EXEMPT_VOICED :
            case EXEMPT_IDENTD :
            case EXEMPT_REGISTERED :
            case EXEMPT_INVITES :
                val = value.toUpperCase().hashCode();
                return ( val == ON || val == OFF );

            case GREETMSG :
                return ( value != null && value.length() < 240 );
            
            case LIST :
                return true;
                
            default : 
                return false;
        }
    }
    
}
