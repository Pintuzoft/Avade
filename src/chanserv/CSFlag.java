/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chanserv;

import core.Handler;
import core.HashNumeric;
import core.HashString;

/**
 *
 * @author Fredrik Karlsson aka DreamHealer & avade.net
 */
public class CSFlag extends HashNumeric {
    private HashString name;
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
    
    /**
     *
     * @param name
     * @param join_connect_time
     * @param talk_connect_time
     * @param talk_join_time
     * @param max_bans
     * @param no_notice
     * @param no_ctcp
     * @param no_part_msg
     * @param no_quit_msg
     * @param exempt_opped
     * @param exempt_voiced
     * @param exempt_identd
     * @param exempt_registered
     * @param exempt_invites
     * @param greetmsg
     */
    public CSFlag ( HashString name, short join_connect_time, short talk_connect_time, short talk_join_time, short max_bans, 
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
    
    /**
     * CSFlag
     * @param name
     */
    public CSFlag ( String name ) {
        this.name = new HashString ( name );
    }
 
    /**
     * syncChangedValuesWithNetwork
     */
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
            Handler.getChanServ().sendServ ( "SVSXCF "+this.name.getString()+" "+values );
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
    
    /**
     * getJoin_connect_time
     * @return
     */
    public short getJoin_connect_time() {
        return join_connect_time;
    }

    /**
     * getTalk_connect_time
     * @return
     */
    public short getTalk_connect_time() {
        return talk_connect_time;
    }

    /**
     * getTalk_join_time
     * @return
     */
    public short getTalk_join_time() {
        return talk_join_time;
    }

    /**
     * getMax_bans
     * @return
     */
    public short getMax_bans() {
        return max_bans;
    }

    /**
     * isNo_notice
     * @return
     */
    public boolean isNo_notice() {
        return no_notice;
    }

    /**
     * isNo_ctcp
     * @return
     */
    public boolean isNo_ctcp() {
        return no_ctcp;
    }

    /**
     * isNo_part_msg
     * @return
     */
    public boolean isNo_part_msg() {
        return no_part_msg;
    }

    /**
     * isNo_quit_msg
     * @return
     */
    public boolean isNo_quit_msg() {
        return no_quit_msg;
    }

    /**
     * isExempt_opped
     * @return
     */
    public boolean isExempt_opped() {
        return exempt_opped;
    }

    /**
     * isExempt_voiced
     * @return
     */
    public boolean isExempt_voiced() {
        return exempt_voiced;
    }

    /**
     * isExempt_identd
     * @return
     */
    public boolean isExempt_identd() {
        return exempt_identd;
    }

    /**
     * isExempt_registered
     * @return
     */
    public boolean isExempt_registered() {
        return exempt_registered;
    }

    /**
     * isExempt_invites
     * @return
     */
    public boolean isExempt_invites() {
        return exempt_invites;
    }

    /**
     * getGreetmsg
     * @return
     */
    public String getGreetmsg() {
        return greetmsg;
    }

    /**
     * setShortFlag
     * @param flag
     * @param in
     */
    public void setShortFlag ( HashString flag, short in ) {
        if      ( flag.is(JOIN_CONNECT_TIME) )      { this.join_connect_time = in;  }
        else if ( flag.is(TALK_CONNECT_TIME) )      { this.talk_connect_time = in;  }
        else if ( flag.is(TALK_JOIN_TIME) )         { this.talk_join_time = in;     }
        else if ( flag.is(MAX_BANS) )               { this.max_bans = in;           }
    }

    /**
     * setBooleanFlag
     * @param flag
     * @param in
     */
    public void setBooleanFlag ( HashString flag, boolean in ) {
        if      ( flag.is(NO_NOTICE) )              { this.no_notice = in;          }
        else if ( flag.is(NO_CTCP) )                { this.no_ctcp = in;            }
        else if ( flag.is(NO_PART_MSG) )            { this.no_part_msg = in;        }
        else if ( flag.is(NO_QUIT_MSG) )            { this.no_quit_msg = in;        }
        else if ( flag.is(EXEMPT_OPPED) )           { this.exempt_opped = in;       }
        else if ( flag.is(EXEMPT_VOICED) )          { this.exempt_voiced = in;      }
        else if ( flag.is(EXEMPT_IDENTD) )          { this.exempt_identd = in;      }
        else if ( flag.is(EXEMPT_REGISTERED) )      { this.exempt_registered = in;  }
        else if ( flag.is(EXEMPT_INVITES) )         { this.exempt_invites = in;     }
    }

    /**
     * setGreetmsg
     * @param greetmsg
     */
    public void setGreetmsg ( String greetmsg ) {
        this.greetmsg = greetmsg;
    }
    
    /**
     * isGreetmsg
     * @return
     */
    public boolean isGreetmsg ( ) {
        return ( this.greetmsg != null && this.greetmsg.length() > 0 );
    }
    
    /**
     * isFlag
     * @param flag
     * @return
     */
    public static boolean isFlag ( String flag ) {
        return isFlag ( new HashString(flag) );
    }
    
    /**
     * isFlag
     * @param flag
     * @return
     */
    public static boolean isFlag ( HashString flag ) {
        if ( flag.is(JOIN_CONNECT_TIME) ||
             flag.is(TALK_CONNECT_TIME) ||
             flag.is(TALK_JOIN_TIME) ||
             flag.is(MAX_BANS) ||
             flag.is(NO_NOTICE) ||
             flag.is(NO_CTCP) ||
             flag.is(NO_PART_MSG) ||
             flag.is(NO_QUIT_MSG) ||
             flag.is(EXEMPT_OPPED) ||
             flag.is(EXEMPT_VOICED) ||
             flag.is(EXEMPT_IDENTD) ||
             flag.is(EXEMPT_REGISTERED) ||
             flag.is(EXEMPT_INVITES) ||
             flag.is(GREETMSG) ||
             flag.is(LIST) ) {
            return true;
        }
        return false;
    }
    
    /**
     * isOkValue
     * @param flag
     * @param value
     * @return
     */
    public static boolean isOkValue ( String flag, String value ) {
        return isOkValue ( new HashString (flag), value );
    }
    
    /**
     * isOkValue
     * @param flag
     * @param value
     * @return
     */
    public static boolean isOkValue ( HashString flag, String value ) {
        int val;
        HashString hashVal;
        if ( value == null ) {
            value = new String ( "" );
        }
        
        if ( flag.is(JOIN_CONNECT_TIME) ||
             flag.is(TALK_CONNECT_TIME) ||
             flag.is(TALK_JOIN_TIME) ) {
            try {
                val = Integer.parseInt ( value );
                return ( val > -1 && val < 1000000 );
            } catch ( NumberFormatException ex ) { }
            return false;
        
        } else if ( flag.is(MAX_BANS) ) {
            try {
                val = Integer.parseInt ( value );
                return ( val > 199 && val < 1000 );
            } catch ( NumberFormatException ex ) { }
            return false;
        
        } else if ( flag.is(NO_NOTICE) ||
                flag.is(NO_CTCP) ||
                flag.is(NO_PART_MSG) ||
                flag.is(NO_QUIT_MSG) ||
                flag.is(EXEMPT_OPPED) ||
                flag.is(EXEMPT_VOICED) ||
                flag.is(EXEMPT_IDENTD) ||
                flag.is(EXEMPT_REGISTERED) ||
                flag.is(EXEMPT_INVITES) ) {
            hashVal = new HashString ( value );
            return ( hashVal.is(ON) || hashVal.is(OFF) );
        
        } else if ( flag.is(GREETMSG) ) {
            return ( value != null && value.length() < 240 );
        
        } else if ( flag.is(LIST) ) {
            return true;
        
        } else {
            return false;
        }
        
    }
    
}
