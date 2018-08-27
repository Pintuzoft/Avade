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
package chanserv;

import channel.Chan;
import channel.Topic;
import core.Handler;
import core.StringMatch;
import core.ServicesID;
import core.HashNumeric;
import static core.HashNumeric.SOP;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import nickserv.NickInfo;
import nickserv.NickServ;
import operserv.Oper;
import user.User;
import java.util.ArrayList;
import java.util.Date;

/**
 *  create table nick  ( name varchar ( 32 ) , mask varchar ( 128 ) ,pass varchar ( 32 ) ,mail varchar ( 64 ) ,regstamp int ( 11 ) , stamp int ( 11 ) , primary key  ( name )  )  ENGINE=InnoDB;
 * @author DreamHealer
 */
public class ChanInfo extends HashNumeric {
    private String          name;
    private NickInfo        founder;
    private String          desc; 
    private int             hashName;       /* Integer representation of nickname */ 
    private String          pass;
    private Topic           topic; 
    private ChanSetting     settings;
    private String          regTime;
    private String          lastSeen; 
    private ArrayList<CSAccess> klist;
    private ArrayList<CSAccess> slist;
    private ArrayList<CSAccess> alist;
    
    private ArrayList<CSAccess> addAccList;
    private ArrayList<CSAccess> remAccList;
    private ArrayList<CSAccessLogEvent> newLogList;
    
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public ChanInfo ( String name, String founder, String pass, String desc, Topic topic, String regStamp, String lastSeen, ChanSetting settings )  {
        /* Load chan */
        this.name       = name;
        this.hashName   = this.name.toUpperCase().hashCode ( );
        this.pass       = pass;
        this.desc       = desc;
        this.topic      = topic;
        this.regTime    = regStamp;
        this.lastSeen   = lastSeen;
        this.settings   = settings;
        this.attachFounder ( founder );
        this.klist      = new ArrayList<>();
        this.slist      = new ArrayList<>();
        this.alist      = new ArrayList<>();
        this.addAccList = new ArrayList<>();
        this.remAccList = new ArrayList<>();
        this.newLogList = new ArrayList<>();
    }
    
    public ChanInfo ( String name, NickInfo founder, String pass, String desc, Topic topic )  {
        /* Register nickname */
        this.name       = name;
        this.hashName   = this.name.toUpperCase().hashCode ( );
        this.founder    = founder;
        this.pass       = pass;
        this.desc       = desc;
        this.topic      = topic;
        this.settings   = new ChanSetting ( );
        Date dateBuf    = new Date ( );
        this.regTime    = dateFormat.format ( dateBuf );
        this.lastSeen   = dateFormat.format ( dateBuf );
        this.klist      = new ArrayList<>();
        this.slist      = new ArrayList<>();
        this.alist      = new ArrayList<>();
        this.addAccList = new ArrayList<>();
        this.remAccList = new ArrayList<>();
        this.newLogList = new ArrayList<>();
        this.settings.set ( OPGUARD, true );
    }

    public void maintenence ( ) {
        this.updateAccessChanges();
    }
    
    private void updateAccessChanges ( ) {
        if ( CSDatabase.checkConn() && this.addAccList.size() > 0 ) {
            ArrayList<CSAccess> access = new ArrayList<>();
            for ( CSAccess acc : this.addAccList ) {
                if ( CSDatabase.addChanAccess ( this, acc, acc.getAccess() ) == 1 ) {
                    access.add ( acc );
                }
            }
            for ( CSAccess acc : access ) {
                this.addAccList.remove ( acc );
            }
        }
        if ( CSDatabase.checkConn() && this.remAccList.size() > 0 ) {
            ArrayList<CSAccess> access = new ArrayList<>();
            for ( CSAccess acc : this.remAccList ) {
                if ( CSDatabase.removeChanAccess ( this, acc ) == 1 ) {
                    access.add ( acc );
                }
            }
            for ( CSAccess acc : access ) {
                this.remAccList.remove ( acc );
            }
        }
        if ( CSDatabase.checkConn() && this.newLogList.size() > 0 ) {
            ArrayList<CSAccessLogEvent> logs = new ArrayList<>();
            for ( CSAccessLogEvent log : this.newLogList ) {
                if ( CSDatabase.accesslogEvent ( log ) ) {
                    logs.add ( log );
                }
            }
            for ( CSAccessLogEvent log : logs ) {
                this.newLogList.remove ( log );
            }
        }
    }
    


    
    /* Identify nick in user */
    private void userIdent ( User user )  {
       // user.getSID ( ) .addNick ( this ); 

    }
    
    public String getName ( ) {
        return this.name;
    }
    
    public String getString ( int var )  {
       
        switch ( var )  {
            case NAME :
                return this.name;

            case DESCRIPTION :
                return this.desc;

            case TOPIC :
                if  ( this.topic != null && this.topic.getTopic ( ) != null )  {
                    return this.topic.getTopic ( );
                } 
                return "";
                
            case TOPICNICK :
                if  ( this.topic != null && this.topic.getTopic ( ) != null )  {
                    return this.topic.getSetter();
                }
                return "";
                
            case LASTSEEN :
                return this.lastSeen;

            case REGTIME :
                return this.regTime;

            default :
                return "";

        } 
      
    }
 
    public boolean identify ( User user, String pass )  {
        if ( this.pass.compareTo ( pass )  == 0 )  {
            /* matches passwords */
            return true;
        }
        return false;
    }

    public boolean setDescription ( String desc )  {
        this.desc = desc;
        this.changed();
        return true;
    }

    public boolean setPass ( String oldPass, String newPass )  {
        if ( this.pass.compareTo ( oldPass )  == 0 )  {
            this.pass = newPass;
            this.changed();
            return true;
        }
        return false;
    }

     
    
    public String getPass ( )  {
        return this.pass;
    }

 
    public ChanSetting getSettings ( )  {
        return this.settings;
    }
    
    public void setSettings ( ChanSetting settings )  {
        this.settings = settings;
        this.changed();
    }
  
    public int getHashName ( ) { 
        return this.hashName;
    }

    private void attachFounder ( String founder )  {
        this.founder = NickServ.findNick ( founder );
    }

    public NickInfo getFounder ( )  {
        return this.founder;
    }

    public Topic getTopic ( )  { return topic; }

    public void setTopic ( Topic topic )  {
        this.topic = topic; 
        this.changed();
    }

    public NickInfo getNickByUser ( User user )  {
        if ( user == null || user.getSID ( )  == null )  {
            return null;
        }
        
        if ( user.getSID().isIdentified ( founder )  )  {
            return this.founder;
        }
        for ( CSAccess access : this.slist )  {
            if ( user.getSID().isIdentified ( access.getNick ( )  )  )  {
                return access.getNick ( );
            }
        }
        for ( CSAccess access : this.alist )  {
            if ( user.getSID().isIdentified ( access.getNick ( )  )  )  {
                return access.getNick ( );
            }
        }
        for ( CSAccess access : this.klist )  {
            if ( user.getSID().isIdentified ( access.getNick ( )  )  )  {
                return access.getNick ( );
            }
        }
        return null;
    }
    
    public int getAccessByNick ( NickInfo ni )  {
        if ( ni == null ) {
            return NONE; 
        }
        if ( ni.hashCode ( ) == this.founder.hashCode ( ) ) {
            return 3;
        } else if ( this.isAccess ( SOP, ni ) ) {
            return 2;
        } else if ( this.isAccess ( AOP, ni ) ) {
            return 1;
        } else if ( this.isAccess ( AKICK, ni ) ) {
            return -1;
        } else {
            return 0;
        }
    }

    public NickInfo getTopNickByUser ( User user )  {
        ServicesID sid = user.getSID ( );
        NickInfo ni = null;
        int buf;
        int access = 0;
        for  ( NickInfo ni2 : sid.getNiList ( )  )  {
            buf = this.getAccessByNick ( ni2 );
            if  ( buf > access )  {
                access  = buf;
                ni      = ni2;
            }
        }
        return ni;
    }

    public void addAccessLog ( CSAccessLogEvent log ) {
        this.newLogList.add ( log );
    }

    
    
    /*** AKICK *******************************/
 
    public boolean delAkick ( String mask )  {
        CSAccess del = null;
         
        for ( CSAccess akick : this.klist )  {
            if ( akick.getMask ( )  != null )  {
                if ( mask.toUpperCase().hashCode ( ) == akick.getMask().toUpperCase().hashCode ( ) ) {
                    del = akick;
                }
            }
        }
        if ( del != null )  {
            this.klist.remove ( del );
            this.remAccList.add ( del );
            return true;
        } 
        return false;
    }    
   
    public boolean isAkick ( String mask )  { 
        for ( CSAccess acc : this.klist )  {
            if ( acc.getMask ( )  != null )  {
                if ( StringMatch.maskWild ( mask, acc.getMask ( )  )  )  {
                    return true;
                }
            }
        } 
        return false;
    }
    public int isAkick ( User user )  {
        NickInfo ni = this.getNickByUser ( user );
        if ( ni != null )  {
            for ( CSAccess acc : this.klist )  {
                if ( acc.getNick ( ) .hashCode ( )  == ni.hashCode ( )  )  {
                    return 1;
                }
            }        
        }   
        String usermask = user.getFullMask ( );
        for ( CSAccess mask : this.klist )  {
            if ( mask.getMask ( )  != null )  {
                if ( StringMatch.maskWild ( usermask, mask.getMask ( )  )  )  {
                    return 2;
                }
            }
        }
        return 0;
    }
        
    public boolean isMaskAkicked ( String mask ) {
        CSAccess akick = new CSAccess ( mask, AKICK );
        for ( CSAccess a : this.klist ) {
            if ( a.isHash ( akick.getHash() ) ) {
                return true;
            }
        }
        return false;
    }
   
    public CSAccess getAkickAccess ( User user )  {
        NickInfo ni = this.getNickByUser ( user );
        for ( CSAccess acc : this.klist )  {
            if ( acc.getNick() != null && ni != null ) {
                if ( acc.getNick().hashCode ( ) == ni.hashCode ( ) ) {
                    return acc;
                }
            } else if ( acc.getMask() != null ) {
                if ( StringMatch.maskWild ( acc.getMask(), user.getFullMask() ) ) {
                    return acc;
                }
            }
        }        
        return null;
    }
    
    /*** FOUNDER *******************************/  
    
    public boolean isFounder ( NickInfo ni )  {
        if ( ni == null ) {
            return false;
        }
        return this.founder.hashCode ( ) == ni.hashCode ( );
    }
    
    public boolean isFounder ( User user )  {
        ServicesID sid = user.getSID ( );
        for  ( NickInfo ni : sid.getNiList ( )  )  {
            if  ( this.isFounder ( ni )  )  {
                return true;
            }
        }
        return false;
    }
    
    /* Return founder nick if user has identified to the founder nick */
    public String getIsFounder ( User user ) {
        for ( NickInfo ni : user.getSID().getNiList ( ) ) {
            if ( this.founder.hashCode ( ) == ni.hashCode ( ) ) {
                return ni.getName ( );
            }
        }
        return null;
    }
      
    
    
    
    /*** GENERIC LIST ********************/
    
    public ArrayList<CSAccess> getAccessList ( int access ) {
        switch ( access ) {
            case SOP :
                return this.slist;
            
            case AOP :
                return this.alist;
                
            case AKICK :
                return this.klist;
                
            default :
                return new ArrayList<>();
        }
    }

    public CSAccess getAccessByNick ( int access, NickInfo ni ) {
        for ( CSAccess acc : getAccessList ( access ) ) {
            if ( acc.getNick().hashCode ( ) == ni.hashCode ( ) ) {
                return acc;
            }
        }
        return null;
    }

    public boolean isAccess ( int access, User user )  {
        NickInfo ni = this.getNickByUser ( user );
        if ( ni != null ) {
            return isAccess ( access, ni );
        } else {
            for ( CSAccess acc : getAccessList ( access ) ) {
                if ( acc.getMask() != null ) {
                    if ( StringMatch.maskWild ( acc.getMask(), user.getFullMask() ) ) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isAccess ( int access, NickInfo ni )  {
        ArrayList<CSAccess> aList = getAccessList ( access );
        for ( CSAccess acc : aList )  {
            if (  ni != null && acc.getNick() != null )  {
                if ( acc.getNick().hashCode ( ) == ni.hashCode ( ) ) {
                    return true;
                }
            } 
        }
        return false;
    }

    public String getAccessString ( int access ) {
        String buf = new String ( );
        ArrayList<CSAccess> list = this.getAccessList ( access );
        for ( CSAccess acc : list ) {
            if ( buf.isEmpty ( ) ) {
                buf += acc.getNick().getString ( NAME );
            } else {
                buf += ", "+acc.getNick().getString ( NAME );
            }
        }      
        return buf;
    }

    public boolean addAccess ( int access, NickInfo ni )  {
        CSAccess acc = new CSAccess ( ni, access );
        CSAccess rem = null;
        ArrayList<CSAccess> list = this.getAccessList ( access );
        this.removeFromAll ( ni );
        list.add ( acc );
        this.addAccList.add ( acc );
        ni.addToAccessList ( access, this );
        return true;        
    }
    public boolean delAccess ( int access, NickInfo ni )  {
        CSAccess del = null;
        ArrayList<CSAccess> list = this.getAccessList ( access );
        for ( CSAccess acc : list )  {
            if ( acc.getNick().hashCode ( ) == ni.hashCode ( )  )  {
                del = acc;
            }
        }
        if ( del != null )  {
            list.remove ( del );
            this.remAccList.add ( del );
            ni.remFromAccessList ( access, this );
            return true;
        } 
        return false;
    }
  
       
    public void addAccess ( int access, String mask )  {
        CSAccess acc = new CSAccess ( mask, access );
        ArrayList<CSAccess> list = this.getAccessList ( access );
        list.add ( acc );
        this.addAccList.add ( acc );
    }
 
    public void removeFromAll ( NickInfo ni ) {
        int[] accessList = { SOP, AOP, AKICK };
        for ( int access : accessList ) {
            CSAccess rem = null;
            for ( CSAccess entry : getAccessList ( access ) ) {
                if ( entry.isHash ( ni.hashCode() ) ) {
                    rem = entry;
                }
            }
            getAccessList(access).remove ( rem );
            ni.remFromAccessList( access, this );
        }
    }
    
    /* return a string of the nick user has identified to or the mask */
    public String getIsAccess ( int access, User user )  {
        NickInfo ni = this.getNickByUser ( user );
        for ( CSAccess acc : getAccessList ( access ) )  {
            if ( acc.getNick() != null && ni != null ) {
                if ( acc.getNick().hashCode ( ) == ni.hashCode ( ) ) {
                    return acc.getNick().getName ( );
                }
            } else if ( acc.getMask() != null ) {
                if ( StringMatch.maskWild ( acc.getMask(), user.getString ( FULLMASK ) ) ) {
                    return acc.getMask();
                }
            }
        }
        return null;
    }
     
    public void setAccessList ( int access, ArrayList<CSAccess> chanAccess ) {
        getAccessList(access).addAll ( chanAccess );
        for ( CSAccess csa : chanAccess ) {
            if ( csa.getNick() != null ) {
                csa.getNick().addToAccessList( csa.getAccess(), this );
            }
        }
    }
     
    public void wipeAccessList ( int access )  {
        switch ( access )  {
            case SOP :
                this.slist.clear ( ); 
                this.slist.clear ( );
                break;
                
            case AOP :
                this.alist.clear ( ); 
                this.alist.clear ( );
                break;
                
            case AKICK :
                this.klist.clear ( ); 
                this.klist.clear ( );
                break;
                
            default :
                
        }
    }
    
    public boolean validMask ( String string )  { 
        return string.matches ( "^(.*)([!])(.*)([@])(.*)$" );  
    } 
 
    public String getAccessByUser ( User user ) {
        String access;
        if ( this.isFounder ( user ) ) {
            access = "Founder";
        } else if ( this.isAccess ( SOP, user ) ) {
            access = "Sop";
        } else if ( this.isAccess ( AOP, user ) ) {
            access = "Aop";
        } else if ( this.isAkick ( user ) > 0 ) {
            access = "AKick";
        } else {
            access = "User";
        }
        return access;
    }
 
    public String getAccessHolderByUser ( User user ) { 
        String holder = null;
        if ( ( holder = this.getIsFounder ( user ) ) != null ) {
        } else if ( ( holder = this.getIsAccess ( SOP, user ) ) != null ) {
        } else if ( ( holder = this.getIsAccess ( AOP, user ) ) != null ) {
        } else if ( ( holder = this.getIsAccess ( AKICK, user ) ) != null ) {
        } 
        return holder;
    }
    
    public void kickAll ( String reason )  {
        // :Pintuz MODE #avade 0 -o Pintuz
        if ( reason == null ) {
            reason = "Masskick";
        }
        Chan c = Handler.findChan ( this.name );
        c.clearUsers ( );
        Handler.getChanServ().sendCmd ( "SVSHOLD "+c.getString ( NAME ) +" 60 :"+reason );
        Handler.getChanServ().sendCmd ( "CHANKILL "+c.getString ( NAME ) +" :"+reason );
    }

    public boolean isAtleastSop ( NickInfo ni ) {
        return ( this.isFounder ( ni ) || this.isAccess ( SOP, ni ) );
    }
    public boolean isAtleastAop ( NickInfo ni ) {
        return ( this.isFounder ( ni ) || this.isAccess ( SOP, ni ) || this.isAccess ( AOP, ni ) );
    } 

    /* For Transparancy */
    public boolean is ( int setting ) {
        return this.settings.is ( setting );
    }
    
    public void changed ( ) {
        ChanServ.addToWorkList ( CHANGE, this );
    }
}
