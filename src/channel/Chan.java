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
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package channel;

import chanserv.ChanServ;
import core.Handler;
import core.Proc;
import core.HashNumeric;
import user.User;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 *
 * @author DreamHealer
 */
public class Chan extends HashNumeric {
    private String              name;
    private Topic               topic;
    
    private int                 hashName;
    
   // private String modes;
    private long                createdOn;
    private ArrayList<User>     oList;
    private ArrayList<User>     vList;
    private ArrayList<User>     uList;
    
    private ChanMode            modes;
    private boolean sajoin = false;
    
    /* STATIC */
    //public static Comparator<Chan>      comparator =  ( Chan c1, Chan c2 )  -> { return c1.hashCode ( )  - c2.hashCode ( ); };


    public Chan ( String[] data ) {
        this.name           = data[3];
        this.hashName       = this.name.toUpperCase().hashCode ( );
        this.createdOn      = Long.parseLong ( data[2] );
        this.modes          = new ChanMode ( );
        this.oList          = new ArrayList<>( );    /* oplist */
        this.vList          = new ArrayList<>( );    /* voicelist */
        this.uList          = new ArrayList<>( );    /* userlist */
        this.modes.set ( ChanMode.SERVER, data );
        this.addUserList ( data );

        //System.out.println ( "IN CHAN..." );

    }
    
   
    public Chan ( int code )  {
        this.hashName = code;
    }
    
    public void addUserList ( String[] data )  {
        // :irc.avade.net SJOIN 1374147654 #friends +c  :@Guest33015 @DreamHealer 
        //      0           1       2       3       4       5+
        User u;
        boolean op, vo;
        String buf;
         
        try {
            String out = new String ( );
            this.modes.setModeString ( data[4] );
            int i = 0;
            for ( String nick : data )  {
                if ( ++i > 5 )  {
                    if ( ! nick.isEmpty ( )  )  {
                        op = false;
                        vo = false;
                        nick = nick.replaceAll ( Pattern.quote ( ":" ) , "" );
                        
                        if ( nick.contains ( "@" )  )  {
                            nick = nick.replaceAll ( "@", "" );
                            op = true;
                        }
                        if ( nick.contains ( "+" )  )  {
                            nick = nick.replaceAll ( "\\+", "" );
                            vo = true;
                        }

                        if (  ( u = Handler.findUser ( nick )  )  != null )  {
                            if ( op ) { 
                                this.addUser ( OP, u );
                            } else if ( vo ) {
                                this.addUser ( VOICE, u );
                            } else {
                                this.addUser ( USER, u );
                            }
                            ChanServ.addCheckUser ( this, u );
                        } 
                    }
                }
            }
    
        } catch ( Exception e )  { 
            Proc.log ( Chan.class.getName ( ) , e ); 
        }
    }
    
    public void addCheckUsers ( ) {
        for ( User u : uList ) {
            ChanServ.addCheckUser ( this, u );
        }
        for ( User u : vList ) {
            ChanServ.addCheckUser ( this, u );
        }
        for ( User u : vList ) {
            ChanServ.addCheckUser ( this, u );
        }
    }
    
    public void chMode ( String[] cmd )  {
        // :DreamHealer MODE #friends 1374147654 +oooo Guest33015 Guest33015 Guest33015 Guest33015
        //      0        1      2       3         4      5+
        boolean state = false;
        User u;
        if ( cmd.length < 6 ) {
            return;
        } 
        for ( int i=0, m=1; i < cmd[4].length ( ); i++ ) {
            u = Handler.findUser ( cmd[4+m] );
            switch ( ( ""+cmd[4].charAt(i)).hashCode ( ) ) {
               case MODE_PLUS :
                   state = true;
                   break;
                   
               case MODE_MINUS :
                   state = false;
                   break;
                   
               case MODE_o :
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
    public void chModeUser ( User user, int mode, int acc, boolean isIRCop )  { 
        try {            
            if ( user == null )  {
                 return;
            }
            boolean isop        = this.isOp ( user );
            boolean isvoice     = this.isVo ( user );
            boolean isuser      = this.isUser ( user );
            
            /* if we want to change OP */
            if ( mode == OP )  {
                if ( acc == OP )  {
                    if ( ! isop )  {
                        this.uList.remove ( user );
                        this.oList.add ( user );
                    }
                } else {
                    if ( isop )  {
                        if ( ! isvoice )  {
                            this.uList.add ( user );
                        }
                        this.oList.remove ( user );
                    }
                }

            } else if ( mode == VOICE )  {
                if ( acc == VOICE )  {
                    if ( ! isvoice )  {
                        this.uList.remove ( user );
                        this.vList.add ( user );
                    } 
                } else {
                    if ( isvoice )  {
                        if ( ! isop )  {
                            this.uList.add ( user );
                        }
                        this.vList.remove ( user );
                    }
                }
            } 
            if ( this.isOp ( user ) && ! isIRCop )  {
                 Handler.getChanServ().checkUser ( this, user );
            }
        } catch ( Exception e )  {
             Proc.log ( Chan.class.getName ( ) , e );
        }
    }
    public void remUser ( User u )  {
        if ( this.oList.contains ( u ) ) {
            this.oList.remove ( u );
        }
        if ( this.vList.contains ( u ) ) {
            this.vList.remove ( u );
        }
        if ( this.uList.contains ( u ) ) {
            this.uList.remove ( u );
        }
    }

    public void addUser ( int acc, User u )  {
        if ( u == null )  {
             return;
        } 
        
        switch ( acc )  {
            case OP :
                this.oList.add ( u );
                break;

            case VOICE :
                this.vList.add ( u );
                break;

            case USER :
                this.uList.add ( u );
                break;

            default :
        } 
    }
    
   
    
    public String getString ( int type )  {
        switch ( type )  {
            case NAME :
                return this.name;
                
            default :
                return null;
        }
    }
  
    public ArrayList<User> getList ( int type )  { 
        switch ( type )  {
            case OP :
                return this.oList;
                
            case VOICE :
                return this.vList;
                
            case USER :
                return this.uList;
                
            case ALL : 
                ArrayList<User> all = new ArrayList<> ( );
                all.addAll ( this.oList );
                all.addAll ( this.vList );
                all.addAll ( this.uList );
                return all;
                
            default :
                return new ArrayList<>( );
                
        }
    }
      
    public boolean isOp ( User user )  {
        for ( User u : this.oList )  {
            if ( user.hashCode ( ) == u.hashCode ( )  )  {
                return true;
            }
        }
        return false;
    }
    
    public boolean isVo ( User user )  {
        for ( User u : this.vList )  {
            if ( user.hashCode ( )  == u.hashCode ( )  )  {
                return true;
            }
        }
        return false;
    }
    
    public boolean isUser ( User user )  {
        for ( User u : this.uList )  {
            if ( user.hashCode ( )  == u.hashCode ( )  )  {
                return true;
            }
        }
        return false;
    }
    
    public boolean nickIsPresent ( String nick )  {
        int hashCode = nick.toUpperCase ( ) .hashCode ( );
        for ( User u : this.getList ( ALL )  )  {
            if ( u.getHash ( )  == hashCode )  {
                return true;
            }
        } 
        return false;
    }
    
    public void clearUsers ( )  {
        this.oList = new ArrayList<>( );
        this.vList = new ArrayList<>( );
        this.uList = new ArrayList<>( );
    }
    
    public ChanMode getModes ( )           { return this.modes; }
    
    public int size ( )    { 
        try { 
            return  ( this.uList.size ( )  + this.oList.size ( )  + this.vList.size ( )  );
        } catch ( Exception e )  { 
            return 1; 
        }  
    }
     
    public boolean empty ( ) { 
        try { 
            return  ( this.size ( ) == 0 ); 
        } catch ( Exception e )  { 
            return false; 
        }  
    }
 
    public int getHashName ( ) { 
        return this.hashName;
    } 
    
    public int getHash ( ) { 
        return this.hashName;
    } 
    
    @Override
    public int hashCode ( ) { 
        return this.hashName;
    } 
    
    public Topic getTopic ( ) { 
        return topic;
    }
    
    public void setTopic ( Topic topic ) { 
        this.topic = topic;
    } 
    
    public void toggleSaJoin ( ) {
        this.sajoin = ! this.sajoin;
    }
    
    public boolean isSaJoin ( ) {
        return this.sajoin;
    }
}
