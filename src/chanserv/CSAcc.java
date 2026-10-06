/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chanserv;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import core.CIDRUtils;
import core.HashNumeric;
import core.HashString;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import nickserv.NickInfo;
import user.User;

/**
 *
 * @author Fredrik Karlsson aka DreamHealer - avade.net
 */
public class CSAcc extends HashNumeric {
    private static Pattern IPv4 = Pattern.compile ( "^(1?(1?[0-9]{1,2}|2[0-4][0-9]|25[0-5])\\.){3}(1?[0-9]{1,2}|2[0-4][0-9]|25[0-5])$", Pattern.CASE_INSENSITIVE );
    private static Pattern IPv6 = Pattern.compile ( "^([0-9A-Fa-f]{0,4}:){2,7}([0-9A-Fa-f]{1,4}$|((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.|$)){4})$", Pattern.CASE_INSENSITIVE );
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private NickInfo ni;
    private HashString access;
    private String lastOped;
    private boolean isNick;
    
    private boolean isCidr = false;
    private boolean isIPv4 = false;
    private boolean isIPv6 = false;
    private boolean isHost = false;
    
    private boolean wildNick = false;
    private boolean wildUser = false;
    private boolean wildHost = false;
    
    private Pattern nickPattern;
    private Pattern userPattern;
    private Pattern hostPattern;
    private CIDRUtils cidrUtils;
    
    private HashString mask;
    private String nick;
    private String user;
    private String host;
    private int cidr = -1;
    

    private boolean broken;     /* a mask that is not nick!user@host */

    /**
     *
     * @param ni
     * @param access
     * @param lastOped
     */
    public CSAcc ( NickInfo ni, HashString access, String lastOped ) {
        this.ni = ni;
        this.isNick = true;
        this.access = access;
        if ( lastOped != null ) {
            this.lastOped = lastOped.substring(0,19);
        }
        this.mask = null;
    }
    
    /**
     *
     * @param mask
     * @param access
     * @param lastOped
     */
    public CSAcc ( String mask, HashString access, String lastOped ) {
        this.mask = new HashString(mask);
        String[] parts = mask.split("[!@/]");
        this.access = access;
        if ( parts.length < 3 || parts[0].isEmpty ( ) || parts[1].isEmpty ( ) || parts[2].isEmpty ( ) ) {
            /* Not nick!user@host: keep the entry so it can be listed and
               removed, but it never matches anyone */
            this.broken = true;
            this.lastOped = ( lastOped != null && lastOped.length ( ) >= 19 ? lastOped.substring ( 0, 19 ) : lastOped );
            return;
        }
        this.nick = parts[0];
        this.user = parts[1];
        this.host = parts[2];
      
        if ( lastOped != null ) {
            this.lastOped = lastOped.substring(0,19);
        } else {
            this.lastOped = lastOped;
        }
        try {
            if ( parts.length > 3 ) {
                this.cidr = Integer.parseInt ( parts[3] );
            }
        } catch ( NumberFormatException ex ) {
            /* do nothing */
        }
        this.parseMask ( );
    }
    
    private void parseMask ( ) {
        if ( this.nick.compareTo ( "*" ) == 0 ) {
            this.wildNick = true;
        } else {
            this.parseNick ( );
        }
        if ( this.user.compareTo ( "*" ) == 0 ) {
            this.wildUser = true;
        } else {
            this.parseUser ( );
        }
        if ( this.cidr > -1 ) {
            isCidr = true;
        }
        if ( this.host.compareTo ( "*" ) == 0 ) {
            this.wildHost = true;
        } else {
            if ( isIPv4Address ( this.host ) ) {
                isIPv4 = true;
                if ( ! this.isCidr ) {
                    this.isCidr = true;
                    this.cidr = 32;
                }
                try {
                    this.cidrUtils = new CIDRUtils ( this.host+"/"+this.cidr );
                } catch (UnknownHostException ex) {
                    Logger.getLogger(CSAcc.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if ( isIPv6Address ( this.host ) ) {
                isIPv6 = true;
                if ( ! this.isCidr ) {
                    this.isCidr = true;
                    this.cidr = 128;
                }
                try {
                    this.cidrUtils = new CIDRUtils ( this.host+"/"+this.cidr );
                } catch (UnknownHostException ex) {
                    Logger.getLogger(CSAcc.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else {
                isHost = true;
                isCidr = false;
                this.parseHost ( );
            }
        }
    }
     
    private void parseNick ( ) {
        String pattern = this.parsePattern ( this.nick );
        this.nickPattern = Pattern.compile ( pattern, Pattern.CASE_INSENSITIVE );
    }
    
    private void parseUser ( ) {
        String pattern = this.parsePattern ( this.user );
        this.userPattern = Pattern.compile ( pattern, Pattern.CASE_INSENSITIVE );
    }
    
    private void parseHost ( ) {
        String pattern = this.parsePattern ( this.host );
        this.hostPattern = Pattern.compile ( pattern, Pattern.CASE_INSENSITIVE );
    }
    
    /* An irc mask as a regex: * is anything, ? is one character, and
       everything else is literal (a dot is a dot, a [ is a [) */
    private String parsePattern ( String pattern ) {
        StringBuilder buf = new StringBuilder ( "^" );
        StringBuilder literal = new StringBuilder ( );
        for ( char ch : pattern.toCharArray ( ) ) {
            if ( ch == '*' || ch == '?' ) {
                if ( literal.length ( ) > 0 ) {
                    buf.append ( Pattern.quote ( literal.toString ( ) ) );
                    literal.setLength ( 0 );
                }
                buf.append ( ch == '*' ? ".*" : "." );
            } else {
                literal.append ( ch );
            }
        }
        if ( literal.length ( ) > 0 ) {
            buf.append ( Pattern.quote ( literal.toString ( ) ) );
        }
        return buf.append ( "$" ).toString ( );
    }
    
    /**
     *
     * @param str
     * @return
     */
    public static boolean isIPv4Address ( String str ) {
        Matcher match = IPv4.matcher ( str );
        return match.find ( );
        
    }
    
    /**
     *
     * @param str
     * @return
     */
    public static boolean isIPv6Address ( String str ) {
        Matcher match = IPv6.matcher ( str );
        return match.find ( );
        
    }
     
    /**
     *
     * @param user
     * @return
     */
    public boolean matchUser ( User user ) {
        boolean matchNick = false;
        boolean matchUser = false;
        boolean matchHost = false;
        /* Match registered nick */
        if ( this.ni != null ) {
            return user.isIdented ( ni );
        }
        
        if ( this.broken ) {
            return false;
        }
        
        /* Match nick!user@hostip */
        if ( wildNick ) {
            matchNick = true;
        } else {
           matchNick = this.nickPattern.matcher(user.getString(NAME)).find ( );
        }
        
        if ( wildUser ) {
            matchUser = true;
        } else {
            matchUser = this.userPattern.matcher(user.getString(USER)).find ( );
        }
        if ( wildHost ) {
            matchHost = true;
        } else {
            if ( isIPv4 || isIPv6 ) {
                try {
                    /* The ip of the user, never the host name: that would be
                       a DNS lookup in the middle of the main loop */
                    String ip = user.getString ( IP );
                    matchHost = ip != null &&
                                ( isIPv4Address ( ip ) || isIPv6Address ( ip ) ) &&
                                this.cidrUtils != null &&
                                this.cidrUtils.isInRange ( ip );
                } catch (UnknownHostException ex) {
                    Logger.getLogger(CSAcc.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                /* The real host, or the one everyone sees (vhost or the mask
                   of the ircd): a mask is written against what people see */
                String shown = user.getShownHost ( );
                matchHost = this.hostPattern.matcher ( user.getString(HOST) ).find ( ) ||
                            ( shown != null && this.hostPattern.matcher ( shown ).find ( ) );
            }
        }
        return ( matchNick && matchUser && matchHost );
    }
    
    /**
     *
     * @return
     */
    public NickInfo getNick ( ) {
        return this.ni;
    }
    
    /**
     *
     * @param ni
     * @return
     */
    public boolean matchNick ( NickInfo ni ) {
        if ( this.ni != null && ni != null ) {
            return ( this.ni.getName().is ( ni.getName() ) );
        }
        return false;
    }
    
    /**
     *
     * @param in
     * @return
     */
    public boolean matchMask ( String in ) {
        HashString maskVal = new HashString(in);
        if ( this.mask != null ) {
            return this.mask.is(maskVal);
        }
        return false;
    }
    
    /**
     *
     * @param in
     * @return
     */
    public boolean matchHashMask ( String in ) {
        HashString maskVal = new HashString ( in );
        if ( this.mask != null ) {
            return this.mask.is(maskVal);
        }
        return false;
    }
    
    /**
     * getHashMask
     * @return
     */
    public HashString getMask ( ) {
        return this.mask;
    }

    /**
     *
     * @return
     */
    public String getMaskStr() {
        return this.mask.getString();
    }
    
    /**
     * GetAccess
     * @return
     */
    public HashString getAccess ( ) {
        return this.access;
    }

    /**
     * getLastOped
     * @return
     */
    public String getLastOped ( ) {
        return this.lastOped;
    }

    /**
     * updateLastOped
     */
    public void updateLastOped ( ) {
        this.lastOped = dateFormat.format ( new Date ( ) );
    }
    
    /**
     * isNick
     * @return 
     */
    public boolean isNick ( ) {
        return this.ni != null;
    }

    
}
