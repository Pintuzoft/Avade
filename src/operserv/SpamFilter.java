/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package operserv;

import core.Handler;
import core.HashNumeric;
import core.HashString;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Fredrik Karlsson aka DreamHealer & avade.net
 */
public class SpamFilter extends HashNumeric {
    private long id;
    private String pattern;
    private int hashPattern;
    private String flags;
    private String instater;
    private String reason;
    private String stamp;
    private String expire;
    private long expireStamp;
    private int bits;
    public static final int SF_FLAG_NONE        = 000000;
    public static final int SF_FLAG_STRIPCTRL   = 0x0001;
    public static final int SF_FLAG_STRIPALL    = 0x0002;
    public static final int SF_FLAG_REGEXP      = 0x0004;
    public static final int SF_FLAG_BREAK       = 0x0008;   /* We dont use this */
    public static final int SF_FLAG_MATCHREG    = 0x20000;
    public static final int SF_CMD_PRIVMSG      = 0x0010;
    public static final int SF_CMD_NOTICE       = 0x0020;
    public static final int SF_CMD_KICK         = 0x0040;
    public static final int SF_CMD_QUIT         = 0x0080;
    public static final int SF_CMD_TOPIC        = 0x0100;
    public static final int SF_CMD_AWAY         = 0x0200;
    public static final int SF_CMD_PART         = 0x0400;
    public static final int SF_CMD_CHANNEL      = 0x0800;
    public static final int SF_ACT_WARN         = 0x1000;
    public static final int SF_ACT_LAG          = 0x2000;
    public static final int SF_ACT_REPORT       = 0x4000;
    public static final int SF_ACT_BLOCK        = 0x8000;
    public static final int SF_ACT_KILL         = 0x10000;
    public static final int SF_ACT_AKILL        = 0x40000;
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 

    public SpamFilter ( long id, String pattern, String flags, String instater, String reason, String stamp, String expire ) {
        this.id = id;
        this.pattern = pattern;
        this.hashPattern = pattern.toUpperCase().hashCode();
        this.flags = flags;
        this.instater = instater;
        this.reason = reason;
        this.stamp = stamp;
        this.expire = expire;
        
        /* Add proper time and expire if time is null */
        if ( stamp == null ) {
            this.stamp = dateFormat.format ( new Date ( ) );
            this.expire = Handler.expireToDateString ( this.stamp, expire );
        } else {
            this.stamp = stamp;
            this.expire = expire;
            
        }
        try {
            this.expireStamp = dateFormat.parse(this.expire).getTime();
        } catch (ParseException ex) {
            Logger.getLogger(SpamFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("expireStamp: "+this.stamp+":"+this.expireStamp);
        this.flagsToBits();
    }
    
    public long getID ( ) {
        return this.id;
    }
    
    public String getPattern() {
        return this.pattern;
    }

    public String getFlags() {
        return this.flags;
    }
    
    public int getBitFlags () {
        return this.bits;
    }
    
    public String getInstater() {
        return this.instater;
    }
    
    public String getReason() {
        return this.reason;
    }

    public String getStamp() {
        return this.stamp;
    }

    public String getExpire() {
        return this.expire;
    }
    
    public int getHashPattern ( ) {
        return this.hashPattern;
    }
    
    private void flagsToBits ( ) {
        HashString ch;
        this.bits = 0;
        for ( int index = 0; index < flags.length(); index++ ) {
            ch = new HashString ( String.valueOf(flags.charAt(index)) );
            
            if ( ch.is(s) ) {
                this.bits |= SF_FLAG_STRIPCTRL;
            
            } else if ( ch.is(S) ) {
                this.bits |= SF_FLAG_STRIPALL;
            
            } else if ( ch.is(r) ) {
                this.bits |= SF_FLAG_REGEXP;
            
            } else if ( ch.is(m) ) {
                this.bits |= SF_FLAG_MATCHREG;
            
            } else if ( ch.is(p) ) {
                this.bits |= SF_CMD_PRIVMSG;
            
            } else if ( ch.is(n) ) {
                this.bits |= SF_CMD_NOTICE;
            
            } else if ( ch.is(k) ) {
                this.bits |= SF_CMD_KICK;
            
            } else if ( ch.is(q) ) {
                this.bits |= SF_CMD_QUIT;
            
            } else if ( ch.is(t) ) {
                this.bits |= SF_CMD_TOPIC;
            
            } else if ( ch.is(a) ) {
                this.bits |= SF_CMD_AWAY;
            
            } else if ( ch.is(c) ) {
                this.bits |= SF_CMD_CHANNEL;
            
            } else if ( ch.is(P) ) {
                this.bits |= SF_CMD_PART;
            
            } else if ( ch.is(W) ) {
                this.bits |= SF_ACT_WARN;
            
            } else if ( ch.is(L) ) {
                this.bits |= SF_ACT_LAG;
            
            } else if ( ch.is(R) ) {
                this.bits |= SF_ACT_REPORT;
            
            } else if ( ch.is(B) ) {
                this.bits |= SF_ACT_BLOCK;
            
            } else if ( ch.is(K) ) {
                this.bits |= SF_ACT_KILL;
            
            } else if ( ch.is(A) ) {
                this.bits |= SF_ACT_AKILL;
            
            } else if ( ch.is(NUM_1) ) {
                /* Shortcut for: spnWR */
                this.bits |= SF_FLAG_STRIPCTRL;
                this.bits |= SF_CMD_PRIVMSG;
                this.bits |= SF_CMD_NOTICE;
                this.bits |= SF_ACT_WARN;
                this.bits |= SF_ACT_REPORT;
            
            } else if ( ch.is(NUM_2) ) {
                /* Shortcut for: spnWRBA */
                this.bits |= SF_FLAG_STRIPCTRL;
                this.bits |= SF_CMD_PRIVMSG;
                this.bits |= SF_CMD_NOTICE;
                this.bits |= SF_ACT_WARN;
                this.bits |= SF_ACT_REPORT;
                this.bits |= SF_ACT_BLOCK;
                this.bits |= SF_ACT_AKILL; 
            
            } else if ( ch.is(NUM_3) ) {
                /* Shortcut for: scWR */
                this.bits |= SF_FLAG_STRIPCTRL;
                this.bits |= SF_CMD_CHANNEL;
                this.bits |= SF_ACT_WARN;
                this.bits |= SF_ACT_REPORT;
            
            } else if ( ch.is(NUM_4) ) {
                /* Shortcut for: scWRBA */
                this.bits |= SF_FLAG_STRIPCTRL;
                this.bits |= SF_CMD_CHANNEL;
                this.bits |= SF_ACT_WARN;
                this.bits |= SF_ACT_REPORT;
                this.bits |= SF_ACT_BLOCK;
                this.bits |= SF_ACT_AKILL;
            }
             
        }
    }

    public boolean hasExpired ( ) {
        System.out.println(this.expireStamp+":"+System.currentTimeMillis());
        return this.expireStamp < System.currentTimeMillis();
    }
    
}
