/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package operserv;

import core.Handler;
import core.HashNumeric;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); 

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
        this.bits = 0;
        for ( int index = 0; index < flags.length(); index++ ) {
            switch ( String.valueOf(flags.charAt(index)).hashCode() ) {
                case s :
                    this.bits |= SF_FLAG_STRIPCTRL;
                    break;
                case S :
                    this.bits |= SF_FLAG_STRIPALL;
                    break;
                case r :
                    this.bits |= SF_FLAG_REGEXP;
                    break;
                case m :
                    this.bits |= SF_FLAG_MATCHREG;
                    break;
                case p :
                    this.bits |= SF_CMD_PRIVMSG;
                    break;
                case n :
                    this.bits |= SF_CMD_NOTICE;
                    break;
                case k :
                    this.bits |= SF_CMD_KICK;
                    break;
                case q :
                    this.bits |= SF_CMD_QUIT;
                    break;
                case t :
                    this.bits |= SF_CMD_TOPIC;
                    break;
                case a :
                    this.bits |= SF_CMD_AWAY;
                    break;
                case c :
                    this.bits |= SF_CMD_CHANNEL;
                    break;
                case P :
                    this.bits |= SF_CMD_PART;
                    break;
                case W :
                    this.bits |= SF_ACT_WARN;
                    break;
                case L :
                    this.bits |= SF_ACT_LAG;
                    break;
                case R :
                    this.bits |= SF_ACT_REPORT;
                    break;
                case B :
                    this.bits |= SF_ACT_BLOCK;
                    break;
                case K :
                    this.bits |= SF_ACT_KILL;
                    break;
                case A :
                    this.bits |= SF_ACT_AKILL;
                    break;
                case NUM_1 :
                    /* Shortcut for: spnWR */
                    this.bits |= SF_FLAG_STRIPCTRL;
                    this.bits |= SF_CMD_PRIVMSG;
                    this.bits |= SF_CMD_NOTICE;
                    this.bits |= SF_ACT_WARN;
                    this.bits |= SF_ACT_REPORT;
                    break;
                case NUM_2 :
                    /* Shortcut for: spnWRBA */
                    this.bits |= SF_FLAG_STRIPCTRL;
                    this.bits |= SF_CMD_PRIVMSG;
                    this.bits |= SF_CMD_NOTICE;
                    this.bits |= SF_ACT_WARN;
                    this.bits |= SF_ACT_REPORT;
                    this.bits |= SF_ACT_BLOCK;
                    this.bits |= SF_ACT_AKILL;
                    break;
                case NUM_3 :
                    /* Shortcut for: scWR */
                    this.bits |= SF_FLAG_STRIPCTRL;
                    this.bits |= SF_CMD_CHANNEL;
                    this.bits |= SF_ACT_WARN;
                    this.bits |= SF_ACT_REPORT;
                    break;
                case NUM_4 :
                    /* Shortcut for: scWRBA */
                    this.bits |= SF_FLAG_STRIPCTRL;
                    this.bits |= SF_CMD_CHANNEL;
                    this.bits |= SF_ACT_WARN;
                    this.bits |= SF_ACT_REPORT;
                    this.bits |= SF_ACT_BLOCK;
                    this.bits |= SF_ACT_AKILL;
                    break;

                default :
                    /* Unknown flag */
            }
        }
    }
    
}
