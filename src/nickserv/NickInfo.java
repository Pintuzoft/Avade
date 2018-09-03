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
package nickserv;

import chanserv.ChanInfo;
import core.Expire;
import core.Handler;
import core.Proc;
import core.HashNumeric;
import memoserv.MSDatabase;
import memoserv.MemoInfo;
import operserv.Oper;
import security.Hash;
import server.ServSock;
import user.User;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;


/**
 *  create table nick  ( name varchar ( 32 ) , mask varchar ( 128 ) ,pass varchar ( 32 ) ,mail varchar ( 64 ) ,regstamp int ( 11 ) , stamp int ( 11 ) , primary key  ( name )  )  ENGINE=InnoDB;
 * @author DreamHealer
 */
public class NickInfo extends HashNumeric {
    private String                  name;
    private String                  user;
    private String                  host;
    private String                  ip;
    private InetAddress             iNet; 
    private int                     hashName;       /* Integer representation of nickname */
    private int                     hashMask;       /* Integer representation of user@mask */ 
    private String                  pass;
    private String                  mail; 
    private NickSetting             settings;
    private String                  regTime;
    private String                  lastSeen; 
    private Date                    date; 
    private Oper                    oper; 
    private ArrayList<MemoInfo>     mList;
    private Expire                  exp;
    private NSChanges               changes;
    private ArrayList<ChanInfo>     akickList = new ArrayList<>();
    private ArrayList<ChanInfo>     aopList = new ArrayList<>();
    private ArrayList<ChanInfo>     sopList = new ArrayList<>();
    private ArrayList<ChanInfo>     founderList = new ArrayList<>();
    private DateFormat  dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /* DATABASE */
    public NickInfo ( String name, String user, String host, String pass, String mail, String regStamp, String lastSeen, NickSetting settings, Expire exp )  {
        // System.out.println ( "Debug: NickInfo ( "+name+" )" );
        this.name       = name;
        this.hashName   = name.toUpperCase().hashCode ( );
        this.hashMask   =  ( user+"@"+host ).toUpperCase().hashCode ( );
        this.user       = user;
        //this.ip         = ip;
        this.date       = new Date ( );
        this.oper       = null; 
        this.pass       = pass;
        this.mail       = mail; 
        this.regTime    = regStamp.substring(0,19);
        this.lastSeen   = lastSeen.substring(0,19);
        this.settings   = settings;
        this.exp        = exp;
        this.changes    = new NSChanges ( );
        this.attachMemos ( ); 
        this.fixHost ( );
     }

    /* REGISTER */
    public NickInfo ( User user, String pass, String mail )  {
        /* Register nickname */ 
        this.name       = user.getString ( NAME );
        this.user       = user.getString ( USER );
        this.host       = user.getString ( HOST );
        this.ip         = user.getString ( IP );
        this.hashName   = this.name.toUpperCase().hashCode ( );
        this.hashMask   = ( user.getString(USER)+"@"+user.getString(IP) ).toUpperCase().hashCode ( ); 
        this.pass       = pass;
        this.mail       = mail; 
        this.settings   = new NickSetting ( );
        String date = this.dateFormat.format ( new Date ( ) );
        this.regTime    = date;
        this.lastSeen   = date; 
        this.date       = new Date ( );
        this.oper       = null;
        this.changes    = new NSChanges ( );
        this.userIdent ( user );
        this.attachMemos ( );
    }
    
    /* Master nick */
    public NickInfo ( String name ) {
        User u          = Handler.findUser ( name );
        Random random   = new Random ( );
        if ( u != null ) {
            String passwd   = "Master" + random.nextInt ( 9800 ) + 100;
            this.name       = name;
            this.pass       = passwd;
            this.ip         = u.getString ( IP );
            this.hashName   = this.name.toUpperCase().hashCode ( );
            this.user       = u.getString ( USER );
            this.host       = u.getString ( HOST );
            this.hashMask   = ( this.user +"@"+this.ip ).toUpperCase().hashCode ( ); 
            this.mail       = "master@localhost";
            String date = this.dateFormat.format ( new Date ( ) );
            this.regTime    = date;
            this.lastSeen   = date;
            this.date       = new Date ( );
            this.oper       = new Oper ( u.getString ( NAME ), 5, "" );
            this.exp        = new Expire ( );
            this.changes    = new NSChanges ( );

            this.userIdent ( u );
            this.attachMemos ( );
            this.settings = new NickSetting ( );
            this.fixHost ( );
            Handler.getRootServ().sendMsg ( u, "Nick: "+u.getString ( NAME )+" has been registered to you using password: "+passwd );
        }
    }
    
    public void fixHost ( ) {
        User u;
        u = Handler.findUser ( this.name );
        if ( u == null ) {
            return;
        }
         
        if ( this.getSettings ( ) .is ( SHOWHOST )  )  {
            /* unset SVSHOST */
            try {
                this.iNet = InetAddress.getByName ( this.ip );
                this.host = this.iNet.getHostAddress();
            } catch ( UnknownHostException ex ) {
                Proc.log ( User.class.getName ( ) , ex );
            }

            ServSock.sendCmd ( ":"+Proc.getConf().get(NAME)+" SVSHOST "+u.getString(NAME)+" :"+this.host ); /* services Send modes and serviceID to user */ 
        } else {
            if ( u.isAtleast ( IRCOP ) ) {
                this.host = u.getOper().getName()+".ircop";
            } else {
                if ( u.getModes().is ( IDENT ) ) {
                    this.host = u.getString(NAME)+".user";
                } else {
                    try {
                        this.iNet = InetAddress.getByName ( this.ip );
                        this.host = this.iNet.getHostAddress();
                    } catch ( UnknownHostException ex ) {
                        Proc.log ( User.class.getName ( ) , ex );                
                    }
                }
            }
            ServSock.sendCmd ( ":"+Proc.getConf().get( NAME ) +" SVSHOST "+u.getString ( NAME ) +" :"+this.host );
//            ServSock.sendCmd ( ":"+Proc.getConf ( ) .get ( NAME ) +" SVSHOST "+u.getString ( NAME ) +" :"+this.getName ( ) +"."+ (  ( u.getSID ( ) .getOper ( ) !=null ) ?u.getSID ( ) .getOper ( ) .getString ( ACCSTRINGSHORT ) :"User" ) +"."+Proc.getConf ( ) .get ( DOMAIN )  ); /* services Send modes and serviceID to user */ 
//           :OperServ SVSHOST DreamHealer :DreamHealer.user.avade.net
        }
    }
 
    private void attachMemos ( )  {
        this.mList = MSDatabase.getMemosByNick ( this.name );
    }
    
    public void setUserMask ( User user )  {
        this.user       = user.getString ( USER );
        this.ip         = user.getString ( HOST );
        this.hashMask   =  ( user.getString(USER)+"@"+user.getString(HOST) ).toUpperCase().hashCode ( );
        this.fixHost ( );
        this.changes.hasChanged ( FULLMASK );
    }
 
    /* Identify nick in user */
    private void userIdent ( User user )  {
        user.getSID().add ( this ); 
    }
    
    public String getName ( )  { return this.name;}
    
    public String getString ( int var )  {
        switch ( var )  {
            case NAME :
                return this.name;
        
            case USER :
                return this.user;
                
            case HOST :
                return this.host;
                
            case IP :
                return this.ip;
                
            case MAIL :
                return this.mail;
            
            case FULLMASK :
                return this.name+"!"+this.user+"@"+this.host;
                
            case LASTSEEN :
                return this.lastSeen;
                
            case REGTIME :
                return this.regTime;
                
            default :
                return "";
           
        }
    }
 
    public boolean isState ( int var )  {
        switch  ( var )  {
            case AUTHED :
                return this.settings.is ( AUTH );
                
            case OLD :
                return this.olderThanExpireTime ( );
                
            default : 
        }
        return false;
    }
    
    public Expire getExp ( )  { return this.exp; }
    
    public boolean shouldExpire ( )  {
        return this.exp.shouldExpire ( );
    }
    
    /* Returns true if nick is older than expiretime */
    public boolean olderThanExpireTime ( )  {
        return false;
   //     return  ( ( System.currentTimeMillis ( ) /1000 - this.lastSeen )  > Handler.expireToTime ( Proc.getConf ( ) .get ( EXPIRE ) ) );
    }
    
    /* Return true if last mail was sent more than a day ago */
  
    public boolean getAuth ( ) { 
        return this.settings.getAuth ( ); 
    }
     
    public boolean identify ( User user, String pass )  {
        if ( pass == null || user == null ) {
            return false;
        }
        
        if ( this.pass.compareTo ( pass ) == 0 )  {
            this.changes.hasChanged ( LASTSEEN );
            return true;
        }
        return false;
    }
 
    public void setPass ( String newPass ) {
        this.pass = newPass;
    }
      
    public boolean isPass ( String pass )  {
        if ( pass == null ) {
            return false;
        }
        return ( this.pass.compareTo ( pass ) == 0 );
    }
 
    public String getPass ( )  {
        return this.pass;
    }
    public String getEmail ( )  {
        return this.mail;
    }
    public NickSetting getSettings ( )  {
        return this.settings;
    }
    
    public Expire getNickExp ( ) {
        return this.exp;
    }
     
    public Oper getOper ( ) {
        return this.oper;
    }
    
    public String getIDOper ( ) {
        return  ( this.oper != null ) ? this.oper.getString ( NAME ) : null;
    }

    public int getHashName ( ) {
        return this.hashName;
    }
 
    public int getHashMask ( ) {
        return this.hashMask;
    }

    public ArrayList<MemoInfo> getMemos ( ) {
        return this.mList; 
    }
    public void addMemo ( MemoInfo memo ) {
        this.mList.add ( memo ); 
        Handler.getMemoServ().newMemo ( memo ); 
    }
    public MemoInfo getMemo ( int num )  { 
        if ( num > 0 && num <= this.mList.size ( ) ) {
            return this.mList.get ( num-1 );
        } 
        return null;
    }

    public void delMemo ( MemoInfo memo )  {
        this.mList.remove ( memo );
    } 
    
    @Override
    public int hashCode ( )  {
       return this.hashName;
    }
   
    public boolean isAtleast ( int access ) {
        if ( this.oper == null ) {
            return false;
        }
        int acc;
        switch ( access ) {
            case IRCOP : 
                acc = 1;
                break;
            
            case SA :
                acc = 2;
                break;
                
            case CSOP :
                acc = 3;
                break;
                
            case SRA : 
                acc = 4;
                break;
                
            case MASTER :
                acc = 5;
                break;
                
            default :
                acc = 0;
                break;
        }
        return ( this.oper.getAccess ( ) >= acc );       
    }

    public void setOper ( Oper oper ) {
        this.oper = oper;
    }
    
    public boolean is ( int setting ) {
        return this.settings.is ( setting );
    }

    public int getAccess ( ) {
        return this.oper.getAccess ( );
    }
    
    public void setEmail ( String mail ) {
        this.mail = mail;
    }
    
    /*** AOP/SOP/FOUNDER LISTS ***/
    
    public ArrayList<ChanInfo> getChanAccess ( int list ) {
        switch ( list ) {
            case FOUNDER :
                return founderList;
            
            case SOP :
                return sopList;
            
            case AOP :
                return aopList;
                
            case AKICK :
                return akickList;
                
            default :
                return new ArrayList<>();
                        
        }
    }
    
    public void addToAccessList ( int list, ChanInfo ci ) {
        this.getChanAccess(list).add ( ci );
    }
    
    public void remFromAccessList ( int list, ChanInfo ci ) {
        ArrayList<ChanInfo> chans = new ArrayList<>();
        for ( ChanInfo ci2 : getChanAccess ( list ) ) {
            if ( ci2.getHashName() == ci.getHashName() ) {
                chans.add ( ci2 );
            }
        }
        for ( ChanInfo ci2 : chans ) {
            getChanAccess(list).remove ( ci2 );
        }
    }
    
    /****************/
    
    public NSChanges getChanges ( ) {
        return this.changes;
    }
}