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
    private short joinconnecttime = 0;
    private short talkconnecttime = 0;
    private short talkjointime = 0;
    private short maxbans = 200;
    private boolean nonotice = false;
    private boolean noctcp = false;
    private boolean nopartmsg = false;
    private boolean noquitmsg = false;
    private boolean exemptopped = false;
    private boolean exemptvoiced = false;
    private boolean exemptidentd = false;
    private boolean exemptregistered = false;
    private boolean exemptinvites = false;
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
        this.joinconnecttime = join_connect_time;
        this.talkconnecttime = talk_connect_time;
        this.talkjointime = talk_join_time;
        this.maxbans = max_bans;
        this.nonotice = no_notice;
        this.noctcp = no_ctcp;
        this.nopartmsg = no_part_msg;
        this.noquitmsg = no_part_msg;
        this.exemptopped = exempt_opped;
        this.exemptvoiced = exempt_voiced;
        this.exemptidentd = exempt_identd;
        this.exemptregistered = exempt_registered;
        this.exemptinvites = exempt_invites;
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
        if ( this.joinconnecttime != 0 ) {
            values = this.addToValues (values, "JOIN_CONNECT_TIME:"+this.joinconnecttime );
        }
        if ( this.talkconnecttime != 0 ) {
            values = this.addToValues (values, "TALK_CONNECT_TIME:"+this.talkconnecttime );
        }
        if ( this.talkjointime != 0 ) {
            values = this.addToValues (values, "TALK_JOIN_TIME:"+this.talkjointime );
        }
        if ( this.maxbans != 200 ) {
            values = this.addToValues (values, "MAX_BANS:"+this.maxbans );
        }
        if ( this.nonotice ) {
            values = this.addToValues (values, "NO_NOTICE:"+( this.nonotice ? "ON" : "OFF" ) );
        }
        if ( this.noctcp ) {
            values = this.addToValues (values, "NO_CTCP:"+( this.noctcp ? "ON" : "OFF" ) );
        }
        if ( this.nopartmsg ) {
            values = this.addToValues (values, "NO_PART_MSG:"+( this.nopartmsg ? "ON" : "OFF" ) );
        }
        if ( this.noquitmsg ) {
            values = this.addToValues (values, "NO_QUIT_MSG:"+( this.noquitmsg ? "ON" : "OFF" ) );
        }
        if ( this.exemptopped ) {
            values = this.addToValues (values, "EXEMPT_OPPED:"+( this.exemptopped ? "ON" : "OFF" ) );
        }
        if ( this.exemptvoiced ) {
            values = this.addToValues (values, "EXEMPT_VOICED:"+( this.exemptvoiced ? "ON" : "OFF" ) );
        }
        if ( this.exemptidentd ) {
            values = this.addToValues (values, "EXEMPT_IDENTD:"+( this.exemptidentd ? "ON" : "OFF" ) );
        }
        if ( this.exemptregistered ) {
            values = this.addToValues (values, "EXEMPT_REGISTERED:"+( this.exemptregistered  ? "ON" : "OFF" ));
        }
        if ( this.exemptinvites ) {
            values = this.addToValues (values, "EXEMPT_INVITES:"+( this.exemptinvites ? "ON" : "OFF" ) );
        }
        
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
    public short getJoinconnecttime() {
        return joinconnecttime;
    }

    /**
     * getTalk_connect_time
     * @return
     */
    public short getTalkconnecttime() {
        return talkconnecttime;
    }

    /**
     * getTalk_join_time
     * @return
     */
    public short getTalkjointime() {
        return talkjointime;
    }

    /**
     * getMax_bans
     * @return
     */
    public short getMaxbans() {
        return maxbans;
    }

    /**
     * isNo_notice
     * @return
     */
    public boolean isNonotice() {
        return nonotice;
    }

    /**
     * isNo_ctcp
     * @return
     */
    public boolean isNoctcp() {
        return noctcp;
    }

    /**
     * isNo_part_msg
     * @return
     */
    public boolean isNopartmsg() {
        return nopartmsg;
    }

    /**
     * isNo_quit_msg
     * @return
     */
    public boolean isNoquitmsg() {
        return noquitmsg;
    }

    /**
     * isExempt_opped
     * @return
     */
    public boolean isExemptopped() {
        return exemptopped;
    }

    /**
     * isExempt_voiced
     * @return
     */
    public boolean isExemptvoiced() {
        return exemptvoiced;
    }

    /**
     * isExempt_identd
     * @return
     */
    public boolean isExemptidentd() {
        return exemptidentd;
    }

    /**
     * isExempt_registered
     * @return
     */
    public boolean isExemptregistered() {
        return exemptregistered;
    }

    /**
     * isExempt_invites
     * @return
     */
    public boolean isExemptinvites() {
        return exemptinvites;
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
        if      ( flag.is(JOIN_CONNECT_TIME) )      { this.joinconnecttime = in;  }
        else if ( flag.is(TALK_CONNECT_TIME) )      { this.talkconnecttime = in;  }
        else if ( flag.is(TALK_JOIN_TIME) )         { this.talkjointime = in;     }
        else if ( flag.is(MAX_BANS) )               { this.maxbans = in;           }
    }

    /**
     * setBooleanFlag
     * @param flag
     * @param in
     */
    public void setBooleanFlag ( HashString flag, boolean in ) {
        if      ( flag.is(NO_NOTICE) )              { this.nonotice = in;          }
        else if ( flag.is(NO_CTCP) )                { this.noctcp = in;            }
        else if ( flag.is(NO_PART_MSG) )            { this.nopartmsg = in;        }
        else if ( flag.is(NO_QUIT_MSG) )            { this.noquitmsg = in;        }
        else if ( flag.is(EXEMPT_OPPED) )           { this.exemptopped = in;       }
        else if ( flag.is(EXEMPT_VOICED) )          { this.exemptvoiced = in;      }
        else if ( flag.is(EXEMPT_IDENTD) )          { this.exemptidentd = in;      }
        else if ( flag.is(EXEMPT_REGISTERED) )      { this.exemptregistered = in;  }
        else if ( flag.is(EXEMPT_INVITES) )         { this.exemptinvites = in;     }
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
        return ( 
            flag.is(JOIN_CONNECT_TIME) ||
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
            flag.is(LIST) 
        );
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
            value = "";
        }
        
        if ( flag.is(JOIN_CONNECT_TIME) ||
             flag.is(TALK_CONNECT_TIME) ||
             flag.is(TALK_JOIN_TIME) ) {
            try {
                val = Integer.parseInt ( value );
                return ( val > -1 && val < 1000000 );
            } catch ( NumberFormatException ex ) { 
                /* nothingness */
            }
            return false;
        
        } else if ( flag.is(MAX_BANS) ) {
            try {
                val = Integer.parseInt ( value );
                return ( val > 199 && val < 1000 );
            } catch ( NumberFormatException ex ) { 
                /* nothingness */
            }
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
            return ( value.length() < 240 );
        
        } else if ( flag.is(LIST) ) {
            return true;
        
        } 
        return false;
        
        
    }
    
}
