/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package operserv;

import core.HashNumeric;
import core.HashString;
import core.Proc;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Fredrik Karlsson aka DreamHealer - avade.net
 */
public class SpamFilter extends HashNumeric {
    private long id;
    private HashString pattern;
    private String flags;
    private String instater;
    private String reason;
    private String stamp;
    private int bits;

    /**
     *
     */
    public static final int SF_FLAG_NONE        = 0x000000;

    /**
     *
     */
    public static final int SF_FLAG_STRIPCTRL   = 0x00001;

    /**
     *
     */
    public static final int SF_FLAG_STRIPALL    = 0x00002;

    /**
     *
     */
    public static final int SF_FLAG_REGEXP      = 0x00004;

    /**
     *
     */
    public static final int SF_FLAG_BREAK       = 0x00008;   /* We dont use this */

    /**
     *
     */
    public static final int SF_FLAG_MATCHREG    = 0x20000;

    /**
     *
     */
    public static final int SF_CMD_PRIVMSG      = 0x00010;

    /**
     *
     */
    public static final int SF_CMD_NOTICE       = 0x00020;

    /**
     *
     */
    public static final int SF_CMD_KICK         = 0x00040;

    /**
     *
     */
    public static final int SF_CMD_QUIT         = 0x00080;

    /**
     *
     */
    public static final int SF_CMD_TOPIC        = 0x00100;

    /**
     *
     */
    public static final int SF_CMD_AWAY         = 0x00200;

    /**
     *
     */
    public static final int SF_CMD_PART         = 0x00400;

    /**
     *
     */
    public static final int SF_CMD_CHANNEL      = 0x00800;

    /**
     *
     */
    public static final int SF_ACT_WARN         = 0x01000;

    /**
     *
     */
    public static final int SF_ACT_LAG          = 0x02000;

    /**
     *
     */
    public static final int SF_ACT_REPORT       = 0x04000;

    /**
     *
     */
    public static final int SF_ACT_BLOCK        = 0x08000;

    /**
     *
     */
    public static final int SF_ACT_KILL         = 0x10000;

    /**
     *
     */
    public static final int SF_ACT_AKILL        = 0x40000;
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 

    /**
     *
     * @param id
     * @param pattern
     * @param flags
     * @param instater
     * @param reason
     * @param stamp
     */
    public SpamFilter ( long id, String pattern, String flags, String instater, String reason, String stamp ) {
        this.id = id;
        this.pattern = new HashString ( pattern );
        this.flags = flags;
        this.instater = instater;
        this.reason = reason;
        this.stamp = stamp;
        
        /* Add proper time and expire if time is null */
        if ( stamp == null ) {
            this.stamp = dateFormat.format ( new Date ( ) );
        } else {
            this.stamp = stamp;
        }
        this.flagsToBits();
    }
    
    /**
     *
     * @return
     */
    public long getID ( ) {
        return this.id;
    }
    
    /**
     *
     * @return
     */
    public HashString getPattern() {
        return this.pattern;
    }

    /**
     *
     * @return
     */
    public String getFlags() {
        return this.flags;
    }
    
    /**
     *
     * @return
     */
    public int getBitFlags () {
        return this.bits;
    }
    
    /**
     *
     * @return
     */
    public String getInstater() {
        return this.instater;
    }
    
    /**
     *
     * @return
     */
    public String getReason() {
        return this.reason;
    }

    /**
     *
     * @return
     */
    public String getStamp() {
        return this.stamp;
    }

    private void flagsToBits ( ) {
        char ch;
        this.bits = 0x0;
        for ( int index = 0; index < flags.length(); index++ ) {
             
            ch = flags.charAt(index);
            
            switch ( ch ) {
                case 's':
                    this.bits |= SF_FLAG_STRIPCTRL;
                    break;
                
                case 'S':
                    this.bits |= SF_FLAG_STRIPALL;
                    break;
                
                case 'r':
                    this.bits |= SF_FLAG_REGEXP;
                    break;
                
                case 'm':
                    this.bits |= SF_FLAG_MATCHREG;
                    break;
                
                case 'p':
                    this.bits |= SF_CMD_PRIVMSG;
                    break;
                
                case 'n':
                    this.bits |= SF_CMD_NOTICE;
                    break;
                
                case 'k':
                    this.bits |= SF_CMD_KICK;
                    break;
                
                case 'q':
                    this.bits |= SF_CMD_QUIT;
                    break;
                
                case 't':
                    this.bits |= SF_CMD_TOPIC;
                    break;
                
                case 'a':
                    this.bits |= SF_CMD_AWAY;
                    break;
                
                case 'c':
                    this.bits |= SF_CMD_CHANNEL;
                    break;
                
                case 'P':
                    this.bits |= SF_CMD_PART;
                    break;
                
                case 'W':
                    this.bits |= SF_ACT_WARN;
                    break;
                
                case 'L':
                    this.bits |= SF_ACT_LAG;
                    break;
                
                case 'R':
                    this.bits |= SF_ACT_REPORT;
                    break;
                
                case 'B':
                    this.bits |= SF_ACT_BLOCK;
                    break;
                
                case 'K':
                    this.bits |= SF_ACT_KILL;
                    break;
                
                case 'A':
                    this.bits |= SF_ACT_AKILL;
                    break;
                
                case '1':
                    this.bits |= SF_FLAG_STRIPCTRL;
                    this.bits |= SF_CMD_PRIVMSG;
                    this.bits |= SF_CMD_NOTICE;
                    this.bits |= SF_ACT_WARN;
                    this.bits |= SF_ACT_REPORT;
                    break;
                
                case '2':
                    this.bits |= SF_FLAG_STRIPCTRL;
                    this.bits |= SF_CMD_PRIVMSG;
                    this.bits |= SF_CMD_NOTICE;
                    this.bits |= SF_ACT_WARN;
                    this.bits |= SF_ACT_REPORT;
                    this.bits |= SF_ACT_BLOCK;
                    this.bits |= SF_ACT_AKILL;
                    break;
                
                case '3':
                    this.bits |= SF_FLAG_STRIPCTRL;
                    this.bits |= SF_CMD_CHANNEL;
                    this.bits |= SF_ACT_WARN;
                    this.bits |= SF_ACT_REPORT;
                    break;
                
                case '4':
                    this.bits |= SF_FLAG_STRIPCTRL;
                    this.bits |= SF_CMD_CHANNEL;
                    this.bits |= SF_ACT_WARN;
                    this.bits |= SF_ACT_REPORT;
                    this.bits |= SF_ACT_BLOCK;
                    this.bits |= SF_ACT_AKILL;
                    break;
            
                default:
                
            }
           
             
            Proc.log("bits: "+ch+":"+this.bits);
        }
    }    
}
