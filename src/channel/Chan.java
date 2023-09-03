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
    private ArrayList<User>     oList;
    private ArrayList<User>     vList;
    private ArrayList<User>     uList;
    
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
        this.oList          = new ArrayList<>( );    /* oplist */
        this.vList          = new ArrayList<>( );    /* voicelist */
        this.uList          = new ArrayList<>( );    /* userlist */
        this.modes.set ( ChanMode.SERVER, data );
        this.init ( data );
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
        if ( StringMatch.wild(this.name.getString(), "*-relay") ) {
            this.isRelay = true;
            this.relay = new HashString ( this.name.getString().substring(0, this.name.getString().length()-6) );
        }
    }
    
    private void init ( String[] data ) {
        this.addUserList ( data, 6 );
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
        // :irc.avade.net SJOIN 1374147654 #friends +c :@Guest33015 @DreamHealer 
        //      0           1       2       3       4       5+
        User u;
        boolean op;
        boolean vo;

        try {

            this.modes.setModeString ( data[4] );
            String[] nicks = Arrays.copyOfRange(data, offset, data.length);
            
            for ( String nick : nicks ) {
                String nickStr = nick.replace (  ":", "" );
                op = false;
                vo = false;
                
                if ( nickStr.contains("@") ) {
                    nickStr = nickStr.replace ( "@", "" );
                    op = true;
                }
                if ( nick.contains("+") ) {
                    nickStr = nickStr.replace ( "+", "" );
                    vo = true;
                }
                
                if ( ( u = Handler.findUser ( nickStr ) ) != null ) {
                    if ( op && vo ) { 
                        this.addUser (OP, u );
                        this.addUser (VOICE, u );
                    } else if ( op ) { 
                        this.addUser ( OP, u );
                    } else if ( vo ) {
                        this.addUser ( VOICE, u );
                    } else {
                        this.addUser ( USER, u );
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
        for ( User u : uList ) {
            ChanServ.addCheckUser ( this, u );
        }
        for ( User u : vList ) {
            ChanServ.addCheckUser ( this, u );
        }
        for ( User u : oList ) {
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
        int m=1;
        if ( cmd.length < 6 ) {
            return;
        }
        setter = Handler.findUser(cmd[0].substring(1));
        for ( int i=0; i < cmd[4].length ( ); i++ ) {
            u = Handler.findUser ( cmd[4+m] );
            switch ( ( ""+cmd[4].charAt(i)).hashCode ( ) ) {
               case MODE_PLUS :
                   state = true;
                   break;
                   
               case MODE_MINUS :
                   state = false;
                   break;
                   
               case MODE_o :
                   if ( setter != null ) {
                        if ( state ) {
                            Handler.getChanServ().checkDynAopAdd ( this, setter, u );
                        } else {
                            Handler.getChanServ().checkDynAopDel(this, setter, u);
                        }
                   }
                    this.chModeUser ( u, OP, ( state ? OP : USER ), u.isAtleast ( IRCOP ) );

                   m++;
                   break;
                   
               case MODE_v :
                   this.chModeUser ( u, VOICE, ( state ? VOICE : USER ), u.isAtleast ( IRCOP ) ); 
                   m++;
                   break;
                   
               default :
                   
           } 
        }
    }

    /**
     *
     * @param user
     * @param access
     * @param isop
     * @param isvoice
     */
    public void setModeUserOP ( User user, HashString access, boolean isop, boolean isvoice ) {
        if ( access.is(OP) && ! isop )  {
            this.uList.remove ( user );
            this.oList.add ( user );

        } else {
            if ( isop )  {
                if ( ! isvoice )  {
                    this.uList.add ( user );
                }
                this.oList.remove ( user );
            }
        }
    }

    /**
     *
     * @param user
     * @param access
     * @param isop
     * @param isvoice
     */
    public void setModeUserVoice ( User user, HashString access, boolean isop, boolean isvoice ) {
         if ( access.is(VOICE) && ! isvoice )  {
            this.uList.remove ( user );
            this.vList.add ( user );

        } else {
            if ( isvoice )  {
                if ( ! isop )  {
                    this.uList.add ( user );
                }
                this.vList.remove ( user );
            }
        }
    }

    /**
     *
     * @param user
     * @param mode
     * @param access
     * @param isIRCop
     */
    public void chModeUser ( User user, HashString mode, HashString access, boolean isIRCop )  { 
        try {            
            if ( user == null )  {
                 return;
            }
            boolean isop        = this.isOp ( user );
            boolean isvoice     = this.isVo ( user );
            
            /* if we want to change OP */
                       
            if ( mode.is(OP) )  {
                setModeUserOP ( user, access, isop, isvoice );

            } else if ( mode.is(VOICE) ) {
                setModeUserVoice ( user, access, isop, isvoice );

            } 
            if ( this.isOp ( user ) && ! isIRCop )  {
                 Handler.getChanServ().checkUser ( this, user );
            }
        } catch ( Exception e )  {
             Proc.log ( Chan.class.getName ( ) , e );
        }
    }

    /**
     *
     * @param user
     */
    public void remUser ( User user )  {
        ArrayList<User> rList = new ArrayList<>();
        for ( User o : oList ) {
            if ( o.is(user) ) {
                rList.add ( o );
            }
        }
        for ( User v : vList ) {
            if ( v.is(user) ) {
                rList.add ( v );
            }
        } 
        for ( User u : uList ) {
            if ( u.is(user) ) {
                rList.add ( u );
            }
        } 
        
        for ( User rem : rList ) {
            this.oList.remove ( rem );
            this.vList.remove ( rem );
            this.uList.remove ( rem );
        }
    }

    /**
     *
     * @param acc
     * @param u
     */
    public void addUser ( HashString acc, User u )  {
        if ( u == null )  {
             return;
        } 
        
        if ( acc.is (OP) && !this.oList.contains(u)) {
            this.oList.add ( u );
            
        } else if ( acc.is (VOICE) && !this.vList.contains(u) ) {
            this.vList.add ( u );
        
        } else if ( acc.is ( USER ) && !this.uList.contains(u) ) {
            this.uList.add ( u );
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
        if ( type.is(OP) ) {
            return this.oList;
        
        } else if ( type.is(VOICE) ) {
            return this.vList;
        
        } else if ( type.is(USER) ) {
            return this.uList;
        
        } else if ( type.is(ALL) ) {
            ArrayList<User> all = new ArrayList<> ( );
            all.addAll ( this.oList );
            all.addAll ( this.vList );
            all.addAll ( this.uList );
            return all;
        }
        return new ArrayList<>( );
    }
      
    /**
     *
     * @param user
     * @return
     */
    public boolean isOp ( User user )  {
        for ( User u : this.oList )  {
            if ( user.hashCode ( ) == u.hashCode ( )  )  {
                return true;
            }
        }
        return false;
    }
    
    /**
     *
     * @param user
     * @return
     */
    public boolean isVo ( User user )  {
        for ( User u : this.vList )  {
            if ( user.hashCode ( )  == u.hashCode ( )  )  {
                return true;
            }
        }
        return false;
    }
    
    /**
     *
     * @param user
     * @return
     */
    public boolean isUser ( User user )  {
        for ( User u : this.uList )  {
            if ( user.hashCode ( )  == u.hashCode ( )  )  {
                return true;
            }
        }
        return false;
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
            if ( user.hasAccess(nick) ) {
                return true;
            }
        } 
        return false;
    }
    
    /**
     *
     */
    public void clearUsers ( )  {
        this.oList = new ArrayList<>( );
        this.vList = new ArrayList<>( );
        this.uList = new ArrayList<>( );
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
            return  ( this.uList.size ( )  + this.oList.size ( )  + this.vList.size ( )  );
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
