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
import core.ServicesID;
import core.HashNumeric;
import static core.HashNumeric.SOP;
import core.HashString;
import core.Throttle;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import nickserv.NickInfo;
import nickserv.NickServ;
import user.User;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 *  create table nick  ( name varchar ( 32 ) , mask varchar ( 128 ) ,pass varchar ( 32 ) ,mail varchar ( 64 ) ,regstamp int ( 11 ) , stamp int ( 11 ) , primary key  ( name )  )  ENGINE=InnoDB;
 * @author DreamHealer
 */
public class ChanInfo extends HashNumeric {
    private HashString      name;
    private NickInfo        founder;
    private String          desc; 
    private String          pass;
    private Topic           topic; 
    private ChanSetting     settings;
    private CSFlag          chanFlag;
    private String          regTime;
    private String          lastUsed; 
    private Throttle        throttle;       /* throttle login attempts */
    private boolean         relayed;

    private HashMap<BigInteger,CSAcc> klist;
    private HashMap<BigInteger,CSAcc> slist;
    private HashMap<BigInteger,CSAcc> alist;
    
    private ArrayList<CSAcc> addAccList;
    private ArrayList<CSAcc> remAccList;
    private ArrayList<CSAcc> updAccList;
    
    private ArrayList<CSAccessLogEvent> newLogList;
    private CSChanges        changes = new CSChanges ( );
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     *
     * @param name
     * @param founder
     * @param pass
     * @param desc
     * @param topic
     * @param regStamp
     * @param lastUsed
     * @param settings
     */
    public ChanInfo ( String name, String founder, String pass, String desc, Topic topic, String regStamp, String lastUsed, ChanSetting settings )  {
        /* Load chan */
        this.name       = new HashString ( name );
        this.pass       = pass;
        this.desc       = desc;
        this.topic      = topic;
        this.regTime    = regStamp.substring(0,19);
        this.lastUsed   = lastUsed.substring(0,19);
        this.settings   = settings;
        this.attachFounder ( founder );
        this.throttle   = new Throttle ( );
        this.klist      = new HashMap<>();
        this.slist      = new HashMap<>();
        this.alist      = new HashMap<>();
        this.addAccList = new ArrayList<>();
        this.remAccList = new ArrayList<>();
        this.updAccList = new ArrayList<>();
        this.newLogList = new ArrayList<>();
        this.checkRelay();
    }
    
    /**
     *
     * @param name
     * @param founder
     * @param pass
     * @param desc
     * @param topic
     */
    public ChanInfo ( String name, NickInfo founder, String pass, String desc, Topic topic )  {
        /* Register nickname */
        this.name       = new HashString ( name );
        this.founder    = founder;
        this.pass       = pass;
        this.desc       = desc;
        this.topic      = topic;
        this.settings   = new ChanSetting ( );
        this.throttle   = new Throttle ( );
        Date dateBuf    = new Date ( );
        this.regTime    = dateFormat.format ( dateBuf );
        this.lastUsed   = dateFormat.format ( dateBuf );
        this.klist      = new HashMap<>();
        this.slist      = new HashMap<>();
        this.alist      = new HashMap<>();
        this.addAccList = new ArrayList<>();
        this.remAccList = new ArrayList<>();
        this.updAccList = new ArrayList<>();
        this.newLogList = new ArrayList<>();
        this.settings.set ( OPGUARD, true );
    }

    /**
     *
     */
    public void maintenence ( ) {
        this.printStats ( );
        this.updateAccessChanges ( );
        this.updateLastOpedChanges ( );
    }
     
    private void printStats ( ) {
        System.out.println ( "STATS: addAccList:"+this.addAccList.size() );
        System.out.println ( "STATS: remAccList:"+this.remAccList.size() );
        System.out.println ( "STATS: updAccList:"+this.updAccList.size() );
        System.out.println ( "STATS: newLogList:"+this.newLogList.size() );
    }
    
    private void updateAccessChanges ( ) {
        if ( CSDatabase.checkConn() && this.addAccList.size() > 0 ) {
            ArrayList<CSAcc> access = new ArrayList<>();
            for ( CSAcc acc : this.addAccList ) {
                if ( CSDatabase.addChanAccess ( this, acc ) == 1 ) {
                    access.add ( acc );
                }
            }
            for ( CSAcc acc : access ) {
                this.addAccList.remove ( acc );
            }
        }
        if ( CSDatabase.checkConn() && this.remAccList.size() > 0 ) {
            ArrayList<CSAcc> access = new ArrayList<>();
            for ( CSAcc acc : this.remAccList ) {
                if ( CSDatabase.removeChanAccess ( this, acc ) == 1 ) {
                    access.add ( acc );
                }
            }
            for ( CSAcc acc : access ) {
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
    
    private void updateLastOpedChanges ( ) {
        if ( CSDatabase.checkConn() && this.updAccList.size() > 0 ) {
            ArrayList<CSAcc> access = new ArrayList<>();
            for ( CSAcc acc : this.updAccList ) {
                if ( CSDatabase.updateChanAccessLastOped ( this, acc ) == 1 ) {
                    access.add ( acc );
                }
            }
            for ( CSAcc acc : access ) {
                this.updAccList.remove ( acc );
            }
        }
    }

    
    /* Identify nick in user */
    private void userIdent ( User user )  {
       // user.getSID ( ) .addNick ( this ); 

    }
    
    /**
     *
     * @return
     */
    public HashString getName ( ) {
        return this.name;
    }

    /**
     *
     * @return
     */
    public String getNameStr ( ) {
        return this.name.getString();
    }
    
    /**
     *
     * @param it
     * @return
     */
    public String getString ( HashString it )  {
        if ( it.is(TOPICNICK) ) { 
            if  ( this.topic != null && this.topic.getText ( ) != null )  {
                return this.topic.getSetter();
            } 
        } else if ( it.is(TOPIC) ) {
            if  ( this.topic != null && this.topic.getText ( ) != null )  {
                return this.topic.getText ( );
            } 
        } 
        else if ( it.is(NAME) )             { return this.name.getString();     }
        else if ( it.is(DESCRIPTION) )      { return this.desc;                 }
        else if ( it.is(LASTUSED) )         { return this.lastUsed;     }
        else if ( it.is(REGTIME) )          { return this.regTime;     }
        return "";
    }
 
    /**
     *
     * @param user
     * @param pass
     * @return
     */
    public boolean identify ( User user, String pass )  {
        return ( this.pass.compareTo ( pass )  == 0 );
    }

    /**
     *
     * @param desc
     * @return
     */
    public boolean setDescription ( String desc )  {
        this.desc = desc;
        this.changed(DESCRIPTION);
        return true;
    }

    /**
     *
     * @param oldPass
     * @param newPass
     * @return
     */
    public boolean setPass ( String oldPass, String newPass )  {
        if ( this.pass.compareTo ( oldPass )  == 0 )  {
            this.pass = newPass;
            this.changed(LASTUSED);
            return true;
        }
        return false;
    }

    /**
     *
     */
    public void setLastUsed ( ) {
        Date dateBuf    = new Date ( );
        this.lastUsed   = dateFormat.format ( dateBuf );
        this.changes.change ( LASTUSED );
    }
     
    /**
     *
     * @return
     */
    public String getPass ( )  {
        return this.pass;
    }

    /**
     *
     * @return
     */
    public ChanSetting getSettings ( )  {
        return this.settings;
    }
    
    /**
     *
     * @param settings
     */
    public void setSettings ( ChanSetting settings )  {
        this.settings = settings;
        this.changed(LASTUSED);
    }

    private void attachFounder ( String founder )  {
        this.founder = NickServ.findNick ( founder );
    }

    /**
     *
     * @return
     */
    public NickInfo getFounder ( )  {
        return this.founder;
    }

    /**
     *
     * @return
     */
    public Topic getTopic ( )  { return topic; }

    /**
     *
     * @param topic
     */
    public void setTopic ( Topic topic )  {
        this.topic = topic; 
        this.changed(LASTUSED);
    }

    /**
     *
     * @param user
     * @return
     */
    public NickInfo getNickByUser ( User user )  {
        NickInfo sop = null;
        NickInfo aop = null;
        NickInfo akick = null;
        
        if ( user == null || user.getSID ( )  == null )  {
            return null;
        }
        
        if ( user.getSID().isIdentified ( founder )  )  {
            return this.founder;
        }
        
        for ( NickInfo ni : user.getSID().getNiList() ) {
            if ( this.slist.get(ni.getName().getCode()) != null ) {
                sop = ni;
            } else if ( this.alist.get(ni.getName().getCode()) != null ) {
                aop = ni;
            } else if ( this.klist.get(ni.getName().getCode()) != null ) {
                akick = ni;
            }
        }
        
        if ( sop != null ) {
            return sop;
        } else if ( aop != null ) {
            return aop;
        } else if ( akick != null ) {
            return akick;
        }
        
        return null;
    }
    
    /**
     *
     * @param ni
     * @return
     */
    public int getAccessByNick ( NickInfo ni )  {
        if ( ni == null ) {
            return 0; 
        }
        
        if ( ni.is(this.founder) ) {
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

    /**
     *
     * @param user
     * @return
     */
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

    /**
     *
     * @param log
     */
    public void addAccessLog ( CSAccessLogEvent log ) {
        this.newLogList.add ( log );
    }

    
    
    /*** AKICK
     * @param mask
     * @return  *******************************/
 
    public boolean delAkick ( String in )  {
        HashString mask = new HashString (in);
        CSAcc del = null;
        
        for ( HashMap.Entry<BigInteger,CSAcc> entry : this.klist.entrySet() ) {
            CSAcc akick = entry.getValue();
            if ( akick.getMask ( ) != null )  {
                if ( mask.is(akick.getMask()) ) {
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
   
    /**
     *
     * @param user
     * @return
     */
    public boolean isAkick ( User user )  {
        for ( HashMap.Entry<BigInteger,CSAcc> entry : this.klist.entrySet() ) {
            CSAcc acc = entry.getValue();
            if ( acc.matchUser ( user ) )  {
                return true;
            }
        }    
        return false;
    }
    
    /**
     *
     * @param ni
     * @return
     */
    public boolean isAkick ( NickInfo ni )  {
        return ( this.klist.get(ni.getName().getCode()) != null );
    }
   
    /**
     *
     * @param user
     * @return
     */
    public CSAcc getAkickAccess ( User user )  {
        for ( HashMap.Entry<BigInteger,CSAcc> entry : this.klist.entrySet() ) {
            CSAcc acc = entry.getValue();
            if ( acc.matchUser ( user ) ) {
                return acc;
            }         
        }    
        return null;
    }
    
    /*** FOUNDER
     * @param ni  
     * @return  *******************************/  
    
    public boolean isFounder ( NickInfo ni )  {
        if ( ni == null ) {
            return false;
        }
        return this.founder.hashCode ( ) == ni.hashCode ( );
    }
    
    /**
     *
     * @param user
     * @return
     */
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

    /**
     *
     * @param user
     * @return
     */

    public String getIsFounder ( User user ) {
        for ( NickInfo ni : user.getSID().getNiList ( ) ) {
            if ( this.founder.is(ni) ) {
                return ni.getNameStr();
            }
        }
        return null;
    }
      
    
    
    
    /*** GENERIC LIST
     * @param access
     * @return  ********************/
    
    public HashMap<BigInteger,CSAcc> getAccessList ( HashString access ) {
        if      ( access.is(SOP) )          { return this.slist;                }
        else if ( access.is(AOP) )          { return this.alist;                }
        else if ( access.is(AKICK) )        { return this.klist;                }
        else {
            return new HashMap<>();
        }
    }

    /**
     *
     * @param access
     * @param acc
     */
    public void addAccess ( HashString access, CSAcc acc ) {
        if ( acc == null ) {
            return;
        }
        BigInteger code = ( acc.isNick() ? acc.getNick().getName().getCode() : acc.getMask().getCode() );
        this.removeFromAll ( acc );
        if      ( access.is(SOP) )          { this.slist.put ( code, acc );           }
        else if ( access.is(AOP) )          { this.alist.put ( code, acc );           }
        else if ( access.is(AKICK) )        { this.klist.put ( code, acc );           }
        
        this.addAccList.add ( acc );
        if ( acc.getNick() != null ) {
            acc.getNick().addToAccessList ( access, this );
        }
    }
    
    /**
     *
     * @param access
     * @param ni
     * @return
     */
    public CSAcc getAccessByNick ( HashString access, NickInfo ni ) {
        return getAccessList(access).get(ni.getName().getCode());
    }

    /**
     *
     * @param access
     * @param user
     * @return
     */
    public boolean isAccess ( HashString access, User user ) {
        for ( HashMap.Entry<BigInteger,CSAcc> entry : getAccessList(access).entrySet() ) {
            CSAcc acc = entry.getValue();
            if ( acc.matchUser ( user ) ) {
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
    public boolean isAtleastAop ( User user ) {
        HashString[] types = { AOP, SOP };
        for ( HashString type : types ) {
            for ( HashMap.Entry<BigInteger,CSAcc> entry : getAccessList(type).entrySet() ) {
                CSAcc acc = entry.getValue();
                if ( acc.matchUser ( user ) ) {
                    return true;
                }
            }
        }
        return isFounder ( user );
    }

    /**
     *
     * @param user
     */
    public void updateLastOped ( User user ) {
        HashString[] types = { AOP, SOP };
        for ( HashString type : types ) {
            for ( HashMap.Entry<BigInteger,CSAcc> entry : getAccessList(type).entrySet() ) {
                CSAcc acc = entry.getValue();
                if ( acc.matchUser ( user ) ) {
                    acc.updateLastOped ( );
                    this.addToAccList ( UPDACCLIST, acc );
                    ChanServ.addToWorkList ( CHANGE, this );
                }
            }
        }
    }
    private ArrayList<CSAcc> getAccList ( HashString it ) {
        if      ( it.is(ADDACCLIST) )           { return addAccList;            }
        else if ( it.is(UPDACCLIST) )           { return updAccList;            }
        else if ( it.is(REMACCLIST) )           { return remAccList;            }
        else {
            return new ArrayList<>();
        }
    }

    /**
     *
     * @param list
     * @param acc
     */
    public void addToAccList ( HashString list, CSAcc acc ) {
        for ( CSAcc a : getAccList ( list ) ) {
            if ( acc.getMask() == null && a.getMask() == null ) {
                if ( acc.getNick().is(a.getNick() ) ) {
                    return;
                }
            } else if ( acc.getMask() != null && a.getMask() != null ) {
                if ( acc.getMask() == a.getMask() ) {
                    return;
                }
            }
        }
        getAccList(list).add ( acc );
    }
    
    /**
     *
     * @param access
     * @param ni
     * @return
     */
    public boolean isAccess ( HashString access, NickInfo ni )  {
        return ( getAccessList(access).get(ni.getName().getCode()) != null );
    }

    /**
     *
     * @param access
     * @return
     */
    public String getAccessString ( HashString access ) {
        String buf = "";
        for ( HashMap.Entry<BigInteger,CSAcc> entry : getAccessList(access).entrySet() ) {
            CSAcc acc = entry.getValue();
        
            if ( buf.isEmpty ( ) ) {
                buf += acc.getNick().getString ( NAME );
            } else {
                buf += ", "+acc.getNick().getString ( NAME );
            }
        }      
        return buf;
    }

/*    public void addAccess ( int access, CSAcc acc )  {
       
        System.out.println(" - 0");
        if ( acc == null ) {
        System.out.println(" - 1");
            return;
        }
        System.out.println(" - 2");
        if ( acc.getNick() != null ) {
        System.out.println(" - 3");
            this.removeFromAll ( acc.getNick() );
        } else if ( acc.getMask() != null ) {
        System.out.println(" - 4");
            this.removeFromAll ( acc );
        }
        System.out.println(" - 5");
        this.getAccessList (access).add ( acc );
        System.out.println(" - 6");
        this.addAccList.add ( acc );
        System.out.println(" - 7");
        if ( acc.getNick() != null ) {
        System.out.println(" - 8");
            acc.getNick().addToAccessList ( access, this );
        }
        System.out.println(" - 9");

    }
  */  

    /**
     *
     * @param access
     * @param acc
     */
  
    public void delAccess ( HashString access, CSAcc acc )  {
        BigInteger code = ( acc.isNick() ? acc.getNick().getName().getCode() : acc.getMask().getCode() );
        getAccessList(access).remove ( code );
        this.remAccList.add ( acc );
        if ( acc.getNick() != null ) {
            acc.getNick().remFromAccessList ( access, this );
        }
    }
    
    /**
     *
     * @param subcommand
     * @param ni2
     * @return
     */
    public CSAcc getAccess ( HashString subcommand, NickInfo ni2 ) {
        CSAcc acc = null;
        if ( ni2 == null ) {
            return null;
        }
        int hash = ni2.hashCode();
        for ( HashMap.Entry<BigInteger,CSAcc> entry : getAccessList(subcommand).entrySet() ) {
            CSAcc a = entry.getValue();
            if ( a.getNick() != null && a.getNick().hashCode() == hash ) {
                acc = a;
            }
        }
        return acc;
    }
    
    /**
     *
     * @param subcommand
     * @param mask
     * @return
     */
    public CSAcc getAccess ( HashString subcommand, String mask ) {
        CSAcc acc = null;
        if ( mask == null ) {
            return null;
        }
        int hash = mask.hashCode();
        for ( HashMap.Entry<BigInteger,CSAcc> entry : getAccessList(subcommand).entrySet() ) {
            CSAcc a = entry.getValue();
            if ( a.getMask() != null && a.getMask().hashCode() == hash ) {
                acc = a;
            }
        }
        return acc;
    }
    
    /**
     *
     * @param access
     * @param mask
     */
    public void addAccess ( HashString access, String mask )  {
        CSAcc acc = new CSAcc ( mask, access, null );
        this.getAccessList(access).put(acc.getMask().getCode(), acc);
        this.addAccList.add ( acc );
    }
 
    /**
     *
     * @param acc
     */
    public void removeFromAll ( CSAcc acc ) {
        HashString[] accessList = { SOP, AOP, AKICK };
        for ( HashString access : accessList ) {
            if ( acc.isNick() ) {
                this.getAccessList(access).remove ( acc.getNick().getName().getCode() );
                acc.getNick().remFromAccessList ( access, this );
            } else {
                this.getAccessList(access).remove ( acc.getMask().getCode() );
            }
        }
    }
     
    /**
     *
     * @param ni
     */
    public void removeFromAll ( NickInfo ni ) {
        HashString[] accessList = { SOP, AOP, AKICK };
        for ( HashString access : accessList ) {
            this.getAccessList(access).remove ( ni.getName().getCode() );
            ni.remFromAccessList ( access, this );
        }
    }
    
    /* return a string of the nick user has identified to or the mask */

    /**
     *
     * @param access
     * @param user
     * @return
     */

    public String getIsAccess ( HashString access, User user )  {
        NickInfo ni;
        if ( (ni = this.getNickByUser(user)) == null ) {
            return null;
          
        } else if ( this.getAccessList(access).get(ni.getName().getCode()) != null ) {
            return ni.getNameStr();
            
        } else {
            for ( HashMap.Entry<BigInteger,CSAcc> entry : getAccessList(access).entrySet() ) {
                CSAcc acc = entry.getValue();
                if ( acc.matchUser ( user ) ) {
                    return acc.getMaskStr();
                }
            }
        }
        return null;
    }
     
    /**
     *
     * @param access
     * @param chanAccess
     */
    public void setAccessList ( HashString access, HashMap<BigInteger,CSAcc> chanAccess ) {
        this.getAccessList(access).putAll(chanAccess);
        for ( HashMap.Entry<BigInteger,CSAcc> entry : getAccessList(access).entrySet() ) {
            CSAcc csa = entry.getValue();
            if ( csa.getNick() != null ) {
                csa.getNick().addToAccessList ( csa.getAccess(), this );
            }
        }
    }
     
    /**
     *
     * @param access
     */
    public void wipeAccessList ( HashString access )  {
        if ( access.is(SOP) ) {
            this.slist.clear ( ); 
            this.slist.clear ( );
        
        } else if ( access.is(AOP) ) {
            this.alist.clear ( ); 
            this.alist.clear ( );
        
        } else if ( access.is(AKICK) ) {
            this.klist.clear ( ); 
            this.klist.clear ( );
        }
    }
    
    /**
     *
     * @param string
     * @return
     */
    public boolean validMask ( String string )  { 
        return string.matches ( "^(.*)([!])(.*)([@])(.*)$" );  
    } 
 
    /**
     *
     * @param user
     * @return
     */
    public String getAccessByUser ( User user ) {
        String access;
        if ( this.isFounder ( user ) ) {
            access = "Founder";
        } else if ( this.isAccess ( SOP, user ) ) {
            access = "Sop";
        } else if ( this.isAccess ( AOP, user ) ) {
            access = "Aop";
        } else if ( this.isAkick ( user ) ) {
            access = "AKick";
        } else {
            access = "User";
        }
        return access;
    }
 
    /**
     *
     * @param user
     * @return
     */
    public String getAccessHolderByUser ( User user ) { 
        String holder = null;
        if ( ( holder = this.getIsFounder ( user ) ) != null ) {
        } else if ( ( holder = this.getIsAccess ( SOP, user ) ) != null ) {
        } else if ( ( holder = this.getIsAccess ( AOP, user ) ) != null ) {
        } else if ( ( holder = this.getIsAccess ( AKICK, user ) ) != null ) {
        } 
        return holder;
    }
    
    /**
     *
     * @param reason
     */
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

    /**
     *
     * @param ni
     * @return
     */
    public boolean isAtleastSop ( NickInfo ni ) {
        return ( this.isFounder ( ni ) || this.isAccess ( SOP, ni ) );
    }

    /**
     *
     * @param ni
     * @return
     */
    public boolean isAtleastAop ( NickInfo ni ) {
        return ( this.isFounder ( ni ) || this.isAccess ( SOP, ni ) || this.isAccess ( AOP, ni ) );
    } 

    /* For Transparancy */

    /**
     *
     * @param setting
     * @return
     */

    public boolean isSet ( HashString setting ) {
        return this.settings.is ( setting );
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
     * @param ci
     * @return
     */
    public boolean is ( ChanInfo ci ) {
        return this.name.is(ci.getName());
    }
    
    /**
     *
     * @param setting
     * @param state
     */
    public void set ( HashString setting, boolean state ) {
        this.settings.set(setting, state);
        this.changes.change(setting);
    }

    /**
     *
     * @param setting
     * @param state
     */
    public void set ( HashString setting, HashString state ) {
        this.settings.set(setting, state);
        this.changes.change(setting);
    }
    
    /**
     *
     * @param setting
     * @param instater
     */
    public void set ( HashString setting, String instater ) {
        this.settings.set(setting, instater);
        this.changes.change(setting);
    }

    /**
     *
     * @param state
     */
    public void setModeLock ( String state ) {
        this.settings.setModeLock(state);
        this.changes.change(MODELOCK);
    }
    
    /**
     *
     */
    public void changed ( HashString setting ) {
        this.getChanges().change ( setting );
        ChanServ.addToWorkList ( CHANGE, this );
    }

    /**
     *
     * @return
     */
    public CSChanges getChanges ( ) {
        return this.changes;
    }

    /**
     *
     * @return
     */
    public CSFlag getChanFlag ( ) {
        return this.chanFlag;
    }

    void setChanFlag ( CSFlag chanFlag ) {
        this.chanFlag = chanFlag;
    }

    /**
     *
     * @return
     */
    public Throttle getThrottle ( ) {
        return this.throttle;
    }

    /**
     *
     * @return
     */
    public boolean isRelayed ( ) {
        return this.relayed;
    }

    private void checkRelay() {
        if ( this.settings == null ) {
            return;
        }
        if ( this.settings.is(AUDITORIUM) ) {
            this.relayed = true;
        } else {
            this.relayed = false;
        }
    }

}
