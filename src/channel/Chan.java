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
 * along with this program; if not, see <https://www.gnu.org/licenses/>.
 */
package channel;

import chanserv.CSAcc;
import chanserv.CSFlag;
import chanserv.ChanServ;
import core.CIDRUtils;
import core.Handler;
import core.Proc;
import core.HashNumeric;
import core.HashString;
import core.StringMatch;
import user.User;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author DreamHealer
 */
public class Chan extends HashNumeric {
    private HashString          name;
    private Topic               topic;
    
    private long                createdOn;
    private ArrayList<User>     members;    /* everyone in the channel, once */
    private ArrayList<User>     oList;      /* ops */
    private ArrayList<User>     hList;      /* halfops */
    private ArrayList<User>     vList;      /* voices */
    
    private ChanMode            modes;
    private boolean sajoin = false;
    
    /* What the modes with an argument are set to. Needed to remove a key
       (-k wants the key) and to decide a join ourselves (join requests) */
    private String              key;                                /* +k, null when not set */
    private int                 limit;                              /* +l, 0 when not set */
    private ArrayList<String>   bans        = new ArrayList<>( );   /* +b */
    private ArrayList<String>   excepts     = new ArrayList<>( );   /* +e */
    private ArrayList<String>   invites     = new ArrayList<>( );   /* +I */
    /* Bans that only hit someone through the host that is shown (a vhost, or
       the mask of the ircd): the real ip of those users, per ban, in lower
       case. A shown host can change, the ban should follow the person.
       Never shown to anyone. */
    private HashMap<String,HashSet<String>> followBans = new HashMap<>( );
    /* Join throttling like in the ircd (+j joins:seconds, 8:6 when not set):
       a bucket that gets <joins> a second up to joins*seconds, and every
       join takes <seconds> out of it */
    private int                 jrNum       = 8;
    private int                 jrTime      = 6;
    private int                 jrBucket;
    private long                jrLast;                             /* in seconds, 0 = full */
    
    private boolean             isRelay;
    private HashString          relay;
    
    /* STATIC */

    /**
     *
     * @param data
     */
    public Chan ( String[] data ) {
        this.name           = new HashString ( data[3] );
        this.modes          = new ChanMode ( );
        this.createdOn      = Long.parseLong ( data[2] );
        this.members        = new ArrayList<>( );
        this.oList          = new ArrayList<>( );    /* oplist */
        this.hList          = new ArrayList<>( );    /* halfoplist */
        this.vList          = new ArrayList<>( );    /* voicelist */
        this.modes.set ( ChanMode.SERVER, data );
        this.checkRelay();
        this.topic          = new Topic ( "", "", 0 );
    }
       
    /**
     *
     * @param code
     */
    public Chan ( HashString code )  {
        this.name = code;
    }
    
    private void checkRelay ( ) {
        if ( StringMatch.matches(this.name.getString(), "*-relay") ) {
            this.isRelay = true;
            this.relay = new HashString ( this.name.getString().substring(0, this.name.getString().length()-6) );
        }
    }
    
    /**
     *
     * @return
     */
    public HashString getRelay ( ) {
        return this.relay;
    }
    
    /**
     *
     * @return
     */
    public boolean isRelay ( ) {
        return this.isRelay;
    } 

    /**
     *
     * @param data
     * @param offset
     */
    public void addUserList ( String[] data, int offset )  {
        // :irc.avade.net SJOIN 1374147654 #friends +c  :@Guest33015 @DreamHealer 
        //      0           1       2       3       4  5     6+
        // :irc.avade.net SJOIN 1374147654 #friends +c :@Guest33015 @%+DreamHealer 
        //      0           1       2       3       4       5+
        User u;

        try {

            this.modes.setModeString ( data[4] );
            this.setSjoinModeArgs ( data );
            /* The nicks are the trailing parameter. Modes with arguments put
               those in between ("+kl key 10 :@nick"), and a key that happens
               to be the nick of someone online must not become a member */
            for ( int i = 5; i < data.length; i++ ) {
                if ( data[i].startsWith ( ":" ) ) {
                    offset = i;
                    break;
                }
            }
            String[] nicks = Arrays.copyOfRange(data, offset, data.length);
            
            for ( String nick : nicks ) {
                String nickStr = nick.startsWith ( ":" ) ? nick.substring ( 1 ) : nick;
                boolean op = false;
                boolean hop = false;
                boolean vo = false;
                
                /* status prefixes: @ op, % halfop, + voice */
                while ( ! nickStr.isEmpty ( ) && "@%+".indexOf ( nickStr.charAt ( 0 ) ) >= 0 ) {
                    switch ( nickStr.charAt ( 0 ) ) {
                        case '@' : op = true;  break;
                        case '%' : hop = true; break;
                        default  : vo = true;  break;
                    }
                    nickStr = nickStr.substring ( 1 );
                }
                
                if ( ( u = Handler.findUser ( nickStr ) ) != null ) {
                    this.addUser ( USER, u );
                    if ( op ) {
                        this.addUser ( OP, u );
                    }
                    if ( hop ) {
                        this.addUser ( HALFOP, u );
                    }
                    if ( vo ) {
                        this.addUser ( VOICE, u );
                    }
                    u.addChan ( this );
                    ChanServ.addCheckUser ( this, u );
                }  
            }
        } catch ( Exception e )  { 
            Proc.log ( Chan.class.getName ( ) , e ); 
        }
    }
    
    /**
     *
     */
    public void addCheckUsers ( ) {
        for ( User u : members ) {
            ChanServ.addCheckUser ( this, u );
        }
    }
    
    /**
     *
     * @param cmd
     */
    public void chMode ( String[] cmd )  {
        // :DreamHealer MODE #friends 1374147654 +oooo Guest33015 Guest33015 Guest33015 Guest33015
        //      0        1      2       3         4      5+
        boolean state = false;
        User setter;
        User u;
        int param = 5;
        char ch;
        if ( cmd.length < 5 ) {
            return;
        }
        setter = Handler.findUser(cmd[0].substring(1));
        for ( int i=0; i < cmd[4].length ( ); i++ ) {
            ch = cmd[4].charAt ( i );
            if ( ch == '+' ) {
                state = true;
                continue;
            } else if ( ch == '-' ) {
                state = false;
                continue;
            }
            
            if ( ! takesParam ( ch, state ) ) {
                if ( ch == 'l' ) {
                    this.limit = 0;     /* -l has no argument */
                } else if ( ch == 'j' ) {
                    this.setJoinRate ( null );
                }
                continue;
            }
            if ( param >= cmd.length ) {
                return;
            }
            String arg = cmd[param++];
            
            if ( ch != 'o' && ch != 'h' && ch != 'v' ) {
                this.setModeArg ( ch, state, arg );
                continue;
            }
            if ( ( u = Handler.findUser ( arg ) ) == null ) {
                continue;
            }
            
            if ( ch == 'o' ) {
                if ( setter != null ) {
                    if ( state ) {
                        Handler.getChanServ().checkDynAopAdd ( this, setter, u );
                    } else {
                        Handler.getChanServ().checkDynAopDel ( this, setter, u );
                    }
                }
                this.chModeUser ( u, OP, ( state ? OP : USER ), u.isAtleast ( IRCOP ) );
            } else if ( ch == 'h' ) {
                this.chModeUser ( u, HALFOP, ( state ? HALFOP : USER ), u.isAtleast ( IRCOP ) );
            } else {
                this.chModeUser ( u, VOICE, ( state ? VOICE : USER ), u.isAtleast ( IRCOP ) );
            }
        }
    }

    /**
     * Channel modes that carry a parameter in bahamut
     * @param mode
     * @param adding
     * @return
     */
    /* Remember the argument of a mode that is not a status mode */
    private void setModeArg ( char mode, boolean adding, String arg ) {
        ArrayList<String> list = null;
        switch ( mode ) {
            case 'k' :
                this.key = ( adding ? arg : null );
                return;
            case 'l' :
                try {
                    this.limit = ( adding ? Integer.parseInt ( arg ) : 0 );
                } catch ( NumberFormatException ex ) {
                    this.limit = 0;
                }
                return;
            case 'j' :
                this.setJoinRate ( adding ? arg : null );
                return;
            case 'b' : list = this.bans;    break;
            case 'e' : list = this.excepts; break;
            case 'I' : list = this.invites; break;
            default  : return;
        }
        list.removeIf ( m -> m.equalsIgnoreCase ( arg ) );
        if ( adding ) {
            list.add ( arg );
        }
        if ( mode == 'b' ) {
            this.followBans.remove ( arg.toLowerCase ( ) );
            if ( adding ) {
                this.rememberBanned ( arg );
            }
        }
    }
    
    /* Who does this ban hit only because of the host they show right now? */
    private void rememberBanned ( String mask ) {
        HashSet<String> ips = new HashSet<>( );
        for ( User u : Handler.getUserList().values ( ) ) {
            String shown = u.getShownHost ( );
            String ip    = u.getIp ( );
            if ( shown == null || ! validIp ( ip ) ) {
                continue;
            }
            String who = u.getString ( NAME )+"!"+u.getString ( USER )+"@";
            if ( StringMatch.matches ( who+shown, mask ) &&
                 ! StringMatch.matches ( who+u.getHost ( ), mask ) &&
                 ! StringMatch.matches ( who+ip, mask ) ) {
                ips.add ( ip );
            }
        }
        if ( ! ips.isEmpty ( ) ) {
            this.followBans.put ( mask.toLowerCase ( ), ips );
        }
    }
    
    private static boolean validIp ( String ip ) {
        return ip != null && ip.length ( ) > 1 && ( ip.contains ( "." ) || ip.contains ( ":" ) );
    }
    
    /**
     * @param user someone who is in, or just joined, the channel
     * @return the ban this user got around by changing the host that is
     *         shown, null when there is none
     */
    public String evadedBan ( User user ) {
        String ip = user.getIp ( );
        if ( this.followBans.isEmpty ( ) || ! validIp ( ip ) ) {
            return null;
        }
        String shown = user.getShownHost ( );
        String who   = user.getString ( NAME )+"!"+user.getString ( USER )+"@"+( shown != null ? shown : user.getHost ( ) );
        for ( HashMap.Entry<String,HashSet<String>> entry : this.followBans.entrySet ( ) ) {
            /* Still hit by the ban as it is: that is for the ircd to enforce
               (someone banned while inside the channel stays until kicked) */
            if ( entry.getValue().contains ( ip ) && ! StringMatch.matches ( who, entry.getKey ( ) ) ) {
                return entry.getKey ( );
            }
        }
        return null;
    }
    
    /**
     * A ban we set ourselves (the ircd does not send our own modes back)
     * @param mask
     * @param sameAs the ban it continues, so it follows the same people
     */
    public void addOwnBan ( String mask, String sameAs ) {
        HashSet<String> ips = ( sameAs != null ? this.followBans.get ( sameAs.toLowerCase ( ) ) : null );
        this.setModeArg ( 'b', true, mask );
        if ( ips != null ) {
            this.followBans.computeIfAbsent ( mask.toLowerCase ( ), k -> new HashSet<>( ) ).addAll ( ips );
        }
    }
    
    /* The arguments of the modes in a SJOIN: "+ljk 10 8:6 key :nicks" */
    private void setSjoinModeArgs ( String[] data ) {
        int param = 5;
        for ( char ch : data[4].toCharArray ( ) ) {
            if ( ( ch == 'k' || ch == 'l' || ch == 'j' ) && param < data.length && ! data[param].startsWith ( ":" ) ) {
                this.setModeArg ( ch, true, data[param++] );
            }
        }
    }

    /* +j <joins>:<seconds>, +j 0 turns the throttling off and -j (null)
       gives the default back. The ircd starts with an empty bucket then */
    private void setJoinRate ( String arg ) {
        int num  = 8;
        int time = 6;
        if ( arg != null ) {
            try {
                String[] parts = arg.split ( ":" );
                num  = Integer.parseInt ( parts[0] );
                time = ( parts.length > 1 ? Integer.parseInt ( parts[1] ) : 0 );
            } catch ( NumberFormatException ex ) {
                num = 0;
            }
            if ( num < 1 || time < 1 ) {
                num  = 0;
                time = 0;
            }
        }
        this.jrNum      = num;
        this.jrTime     = time;
        this.jrBucket   = 0;
        this.jrLast     = System.currentTimeMillis ( ) / 1000;
    }

    private boolean joinRateOk ( ) {
        int size = this.jrNum * this.jrTime;
        if ( size == 0 ) {
            return true;
        }
        long now = System.currentTimeMillis ( ) / 1000;
        if ( this.jrBucket < size && now > this.jrLast ) {
            long fill = now - this.jrLast;
            int room  = size - this.jrBucket;
            if ( fill < room ) {
                fill *= this.jrNum;
            }
            this.jrBucket  += (int) Math.min ( fill, room );
            this.jrLast     = now;
        }
        return this.jrBucket >= this.jrTime;
    }

    /**
     * Someone joined: count it for the join throttling
     */
    public void countJoin ( ) {
        int size = this.jrNum * this.jrTime;
        if ( size == 0 ) {
            return;
        }
        this.joinRateOk ( );    /* fills the bucket for the time that has passed */
        if ( this.jrBucket >= -( size - this.jrTime ) ) {
            this.jrBucket  -= this.jrTime;
            this.jrLast     = System.currentTimeMillis ( ) / 1000;
        }
    }

    /**
     * Decide a join the way the ircd does (can_join), for a join request:
     * with those the ircd checks nothing itself.
     * @param user
     * @param key the key the user gave, or null
     * @param flags the chanflags of the channel, null when it is not registered
     * @return what stops the user ("+b", "+i", "+k", "+l", "+j", "+O", "+S",
     *         "+R", or "+X" for JOIN_CONNECT_TIME), null when the user may join
     */
    public String joinRefusal ( User user, String key, CSFlag flags ) {
        String stop = null;
        boolean wait = false;
        boolean oper = user.getModes().is ( OPER );
        long now = System.currentTimeMillis ( ) / 1000;
        
        if ( flags != null && flags.getJoinconnecttime ( ) > 0 &&
             user.getSignOn ( ) + flags.getJoinconnecttime ( ) > now &&
             ! oper &&
             ! ( flags.isExemptregistered ( ) && user.getModes().is ( IDENT ) ) &&
             ! ( flags.isExemptidentd ( ) && ! user.getString ( USER ).startsWith ( "~" ) ) ) {
            stop = "+X";
            wait = true;
        } else if ( this.modes.is ( MODE_i ) ) {
            stop = "+i";
        } else if ( this.modes.is ( MODE_O ) && ! oper ) {
            stop = "+O";
        } else if ( this.limit > 0 && this.size ( ) >= this.limit ) {
            stop = "+l";
        } else if ( this.modes.is ( MODE_S ) && ! user.getModes().is ( SSL ) ) {
            stop = "+S";
        } else if ( this.modes.is ( MODE_R ) && ! user.getModes().is ( IDENT ) ) {
            stop = "+R";
        } else if ( this.key != null && ( key == null || ! this.key.equalsIgnoreCase ( key ) ) ) {
            stop = "+k";
        } else if ( ! this.joinRateOk ( ) ) {
            return "+j";    /* the invite list does not get around the throttling */
        }
        
        /* The invite list (+I) gets around all of the above, the wait only
           with the chanflag EXEMPT_INVITES */
        if ( stop != null && ( ! wait || flags.isExemptinvites ( ) ) && hits ( this.invites, user ) ) {
            stop = null;
        }
        if ( stop == null && hits ( this.bans, user ) && ! hits ( this.excepts, user ) ) {
            stop = "+b";
        }
        return stop;
    }

    /* Is the user in a +b, +e or +I list? */
    private static boolean hits ( ArrayList<String> masks, User user ) {
        return ! masks.isEmpty ( ) && ! matching ( masks, user, false ).isEmpty ( );
    }

    /* The masks of a list that the user is in (the first one, or all). Like
       the ircd: the real host, the ip and the host that is shown are all
       tried, and ip/bits is a range */
    private static ArrayList<String> matching ( ArrayList<String> masks, User user, boolean all ) {
        ArrayList<String> found = new ArrayList<>( );
        String who   = user.getString ( NAME )+"!"+user.getString ( USER )+"@";
        String shown = user.getShownHost ( );
        String ip    = ( validIp ( user.getIp ( ) ) ? user.getIp ( ) : null );
        for ( String mask : masks ) {
            if ( StringMatch.matches ( who+user.getHost ( ), mask ) ||
                 ( ip != null && StringMatch.matches ( who+ip, mask ) ) ||
                 ( shown != null && StringMatch.matches ( who+shown, mask ) ) ||
                 ( ip != null && inRange ( mask, who, ip ) ) ) {
                found.add ( mask );
                if ( ! all ) {
                    break;
                }
            }
        }
        return found;
    }

    /**
     * Services asked the ircd to remove every ban on this user (SVSMODE -b
     * nick). It does not send our own changes back, so forget them here too.
     * @param user
     */
    public void removeBansOn ( User user ) {
        for ( String mask : matching ( this.bans, user, true ) ) {
            this.setModeArg ( 'b', false, mask );
        }
    }

    /* nick!user@ip/bits. Only an ip is accepted in the mask: looking up a
       host name would stop the main loop */
    private static boolean inRange ( String mask, String who, String ip ) {
        int at    = mask.lastIndexOf ( '@' );
        int slash = mask.lastIndexOf ( '/' );
        if ( at < 1 || slash < at ) {
            return false;
        }
        String addr = mask.substring ( at + 1, slash );
        if ( ! CSAcc.isIPv4Address ( addr ) && ! CSAcc.isIPv6Address ( addr ) ) {
            return false;
        }
        try {
            return StringMatch.matches ( who, mask.substring ( 0, at + 1 ) ) &&
                   new CIDRUtils ( mask.substring ( at + 1 ) ).isInRange ( ip );
        } catch ( UnknownHostException | RuntimeException ex ) {
            return false;   /* bits that are no number, or too many */
        }
    }

    public String getKey ( )                    { return this.key;      }
    public void clearKey ( )                    { this.key = null;      }
    public int getLimit ( )                     { return this.limit;    }

    
    public static boolean takesParam ( char mode, boolean adding ) {
        switch ( mode ) {
            case 'b' : case 'e' : case 'I' :
            case 'o' : case 'h' : case 'v' :
            case 'k' :
                return true;
            case 'l' : case 'j' :
                return adding;
            default :
                return false;
        }
    }

    /**
     * Give or take a status (OP, HALFOP, VOICE) from a user
     * @param user
     * @param mode OP, HALFOP or VOICE
     * @param access same as mode to give the status, anything else to take it
     * @param isIRCop
     */
    public void chModeUser ( User user, HashString mode, HashString access, boolean isIRCop )  { 
        try {            
            if ( user == null )  {
                 return;
            }
            ArrayList<User> list = this.statusList ( mode );
            if ( list == null ) {
                return;
            }
            if ( ! this.members.contains ( user ) ) {
                this.members.add ( user );
            }
            if ( access.is ( mode ) ) {
                if ( ! list.contains ( user ) ) {
                    list.add ( user );
                }
            } else {
                list.remove ( user );
            }
            
            if ( this.isOp ( user ) && ! isIRCop )  {
                 Handler.getChanServ().checkUser ( this, user );
            }
        } catch ( Exception e )  {
             Proc.log ( Chan.class.getName ( ) , e );
        }
    }

    private ArrayList<User> statusList ( HashString mode ) {
        if ( mode.is(OP) ) {
            return this.oList;
        } else if ( mode.is(HALFOP) ) {
            return this.hList;
        } else if ( mode.is(VOICE) ) {
            return this.vList;
        }
        return null;
    }
     
    /**
     *
     * @param user
     */
    public void remUser ( User user )  {
        this.members.removeIf ( u -> u.is ( user ) );
        this.oList.removeIf ( u -> u.is ( user ) );
        this.hList.removeIf ( u -> u.is ( user ) );
        this.vList.removeIf ( u -> u.is ( user ) );
    }
    
    /**
     * Add a user to the channel (USER) or give it a status (OP, HALFOP, VOICE)
     * @param acc
     * @param u
     */
    public void addUser ( HashString acc, User u )  {
        if ( u == null )  {
             return;
        } 
        if ( ! this.members.contains ( u ) ) {
            this.members.add ( u );
        }
        ArrayList<User> list = this.statusList ( acc );
        if ( list != null && ! list.contains ( u ) ) {
            list.add ( u );
        }
    }
     
    /**
     *
     * @param type
     * @return
     */
    public HashString getString ( HashString type )  {
        if ( type.is(NAME) ) {
            return this.name;
            
        } else {
            return null;
        }
    }
  
    /**
     *
     * @param type
     * @return
     */
    public ArrayList<User> getList ( HashString type )  { 
        if ( type.is(ALL) ) {
            return new ArrayList<> ( this.members );
        } else if ( type.is(USER) ) {
            /* users without any status */
            ArrayList<User> users = new ArrayList<> ( );
            for ( User u : this.members ) {
                if ( ! this.oList.contains ( u ) && ! this.hList.contains ( u ) && ! this.vList.contains ( u ) ) {
                    users.add ( u );
                }
            }
            return users;
        }
        ArrayList<User> list = this.statusList ( type );
        return list != null ? new ArrayList<> ( list ) : new ArrayList<>( );
    }
    
    /**
     * @param user
     * @return
     */
    public boolean isOp ( User user )  {
        return this.oList.contains ( user );
    }
    
    /**
     * @param user
     * @return
     */
    public boolean isHop ( User user )  {
        return this.hList.contains ( user );
    }
    
    /**
     * @param user
     * @return
     */
    public boolean isVo ( User user )  {
        return this.vList.contains ( user );
    }

     
    /**
     *
     * @param nick
     * @return
     */
    public boolean nickIsPresent ( String nick ) {
        return this.nickIsPresent ( new HashString(nick) );
    }
    
    /**
     *
     * @param nick
     * @return
     */
    public boolean nickIsPresent ( HashString nick )  {
        for ( User user : this.getList ( ALL )  )  {
            if ( user.getName().is ( nick ) ) {
                return true;
            }
        } 
        return false;
    }
    
    /**
     *
     */
    public void clearUsers ( )  {
        this.members = new ArrayList<>( );
        this.oList = new ArrayList<>( );
        this.hList = new ArrayList<>( );
        this.vList = new ArrayList<>( );
    }
    
    /**
     *
     * @return
     */
    public ChanMode getModes ( )           { return this.modes; }
    
    /**
     *
     * @return
     */
    public int size ( )    { 
        try { 
            return  this.members.size ( );
        } catch ( Exception e )  { 
            return 1; 
        }  
    }
     
    /**
     *
     * @return
     */
    public boolean empty ( ) { 
        try { 
            return  ( this.size ( ) == 0 ); 
        } catch ( Exception e )  { 
            return false; 
        }  
    }
  
    /**
     *
     * @return
     */
    public Topic getTopic ( ) { 
        return topic;
    }
    
    /**
     *
     * @return
     */
    public HashString getName () {
        return this.name;
    }
    
    /**
     *
     * @return
     */
    public String getNameStr () {
        return this.name.getString();
    }
    
    /**
     *
     * @param topic
     */
    public void setTopic ( Topic topic ) { 
        this.topic = topic;
    } 
    
    /**
     *
     */
    public void toggleSaJoin ( ) {
        this.sajoin = ! this.sajoin;
    }
    
    /**
     *
     * @return
     */
    public boolean isSaJoin ( ) {
        return this.sajoin;
    }
    
    /**
     *
     * @param name
     * @return
     */
    public boolean is ( HashString name ) {
        return this.name.is(name);
    }
    
    /**
     *
     * @param chan
     * @return
     */
    public boolean is ( Chan chan ) {
        return this.name.is ( chan );
    }   
    
    /**
     *
     * @return
     */
    public Long getCreatedOn ( ) {
        return this.createdOn;
    }

    /**
     * The TS in a SJOIN for a channel we already know: the oldest one wins
     * @param stamp
     */
    public void sawStamp ( long stamp ) {
        if ( stamp > 0 && stamp < this.createdOn ) {
            this.createdOn = stamp;
        }
    }
}
