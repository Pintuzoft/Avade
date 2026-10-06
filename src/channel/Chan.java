/* 
 * Copyright (C) 2018 Fredrik Karlsson aka DreamHealer & avade.net
 *
 * This program hasAccess free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program hasAccess distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package channel;

import chanserv.ChanServ;
import core.Handler;
import core.Proc;
import core.HashNumeric;
import core.HashString;
import core.StringMatch;
import user.User;
import java.util.ArrayList;
import java.util.Arrays;

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
                continue;
            }
            if ( param >= cmd.length ) {
                return;
            }
            String arg = cmd[param++];
            
            if ( ch != 'o' && ch != 'h' && ch != 'v' ) {
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
     * @param user
     * @return true if the user is in the channel without any status
     */
    public boolean isUser ( User user )  {
        return this.members.contains ( user ) && ! this.isOp ( user ) && ! this.isHop ( user ) && ! this.isVo ( user );
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
}
