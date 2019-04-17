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
package core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.yaml.snakeyaml.Yaml;
/**
 *
 * @author DreamHealer
 */
public class Config extends HashNumeric {
    private boolean valid;
    private String fileName;

    private String name;
    private String domain;
    private String netname;
    private String stats;
    private String master;
    private String authurl;
    private String logfile;
    private String expire;
    private String secretsalt;
    private boolean forcemodes;
    
    private String connName;
    private String connHost;
    private String connPort;
    private String connPass;
    
    private String mysqlHost;
    private String mysqlPort;
    private String mysqlUser;
    private String mysqlPass;
    private String mysqlDB;
    
    private ArrayList<String> whiteList = new ArrayList<>();
    
    private String serviceUser;
    private String serviceHost;
    private String serviceGcos;
    
    private String snoopRootServ;
    private String snoopOperServ;
    private String snoopNickServ;
    private String snoopChanServ;
    private String snoopMemoServ;
    
    /* COMMANDS (set all to 5 - master) */
    private int rehash = 5;
    private int sraw = 5;
    private int panic = 5;
    private int uinfo = 5;
    private int cinfo = 5;
    private int sinfo = 5;
    private int ulist = 5;
    private int jupe = 5;
    private int delete = 5;
    private int sqline = 5;
    private int sgline = 5;
    private int close = 5;
    private int freeze = 5;
    private int hold = 5;
    private int mark = 5;
    private int noghost = 5;
    private int getpass = 5;
    private int getemail = 5;
    private int akill = 5;
    private int banlog = 5;
    private int global = 5;
    private int ignore = 5;
    private int audit = 5;
    private int server = 5;
    private int chanlist = 5;
    private int list = 5;
    private int auditorium = 5;
    private int staff = 5;
    private int searchlog = 5;
    private int uptime = 5;
    private int comment = 5;
    private int topiclog = 5;
    
    public Config ( ) { 
        fileName = "services.conf"; 
        this.init ( );
    }

    private void init ( ) {
        this.loadYamlConf();
    }
    
    private void printErrorAndExit ( String error ) {
        System.out.println ( "ConfigError: "+error );
        System.exit ( 1 );
    }
    private String parseKey ( Map<String,Object> result, String key ) {
        if ( result.get ( key ) == null ) {
            printErrorAndExit ( key );
        }
        return result.get(key).toString();
    }
    
    /*
     * Load config and validate it at the same time
     */
    private void loadYamlConf ( ) {
        Yaml yaml = new Yaml();
        String fileName = "services.conf";
        try {
            InputStream ios = new FileInputStream ( new File ( fileName ) );
            Map<String,Object> result = ( Map<String,Object> ) yaml.load ( ios );
            this.name = parseKey ( result, "name" );
            this.domain = parseKey ( result, "domain" );
            this.netname = parseKey ( result, "netname" );
            this.stats = parseKey ( result, "stats" );
            this.master = parseKey ( result, "master" );
            this.authurl = parseKey ( result, "authurl" );
            this.logfile = parseKey ( result, "logfile" );
            this.expire = parseKey ( result, "expire" );
            this.secretsalt = parseKey ( result, "secretsalt" );
            this.forcemodes = parseKey ( result, "forcemodes" ).toUpperCase().hashCode() == YES;
          
            /* CONNECT */
            String[] connect = result.get("connect").toString().replace("{", "").replace("}", "").split(",");            
            for ( String str : connect ) {
                String[] data = str.split("=");
                switch ( data[0].trim().toUpperCase().hashCode() ) {
                    case NAME :
                        this.connName = data[1];
                        break;
                    
                    case HOST :
                        this.connHost = data[1];
                        break;
                    
                    case PORT :
                        this.connPort = data[1];
                        break;
                    
                    case PASS :
                        this.connPass = data[1];
                        break;
                        
                    default :
                        
                }
            }
            if ( this.connName == null ) 
                printErrorAndExit ( "connect->name" );
            if ( this.connHost == null ) 
                printErrorAndExit ( "connect->host" );
            if ( this.connPort == null ) 
                printErrorAndExit ( "connect->port" );
            if ( this.connPass == null ) 
                printErrorAndExit ( "connect->pass" );
            
            /* MYSQL */
            String[] mysql = result.get("mysql").toString().replace("{", "").replace("}", "").split(",");            
            for ( String str : mysql ) {
                String[] data = str.split("=");
                switch ( data[0].trim().toUpperCase().hashCode() ) {
                    case HOST :
                        this.mysqlHost = data[1];
                        break;
                    
                    case PORT :
                        this.mysqlPort = data[1];
                        break;
                    
                    case USER :
                        this.mysqlUser = data[1];
                        break;
                    
                    case PASS :
                        this.mysqlPass = data[1];
                        break;
                                    
                    case DB :
                        this.mysqlDB = data[1];
                        break;
                        
                    default :
                        
                }
            }
            if ( this.mysqlHost == null )
                printErrorAndExit ( "mysql->host" );
            if ( this.mysqlPort == null ) 
                printErrorAndExit ( "mysql->port" );
            if ( this.mysqlUser == null )
                printErrorAndExit ( "mysql->user" );
            if ( this.mysqlPass == null ) 
                printErrorAndExit ( "mysql->pass" );
            if ( this.mysqlDB == null ) 
                printErrorAndExit ( "mysql->db" );
            
            /* WHITELIST */
            String[] wlist = result.get("whitelist").toString().replace("{","").replace("}","").split(",");            
            for ( String str : wlist ) {
                String data = str.replace("[","").replace("]","");
                this.whiteList.add ( data.trim() );
            }
            if ( this.whiteList.size() == 0 ) 
                System.out.println("Warning!: The whitelist is empty, add services ip and staff addresses");
                      
            /* SERVICE */
            String[] service = result.get("service").toString().replace("{","").replace("}","").split(",");            
            for ( String str : service ) {
                String[] data = str.split("=");
                switch ( data[0].trim().toUpperCase().hashCode() ) {
                    case USER :
                        this.serviceUser = data[1];
                        break;
                    
                    case HOST :
                        this.serviceHost = data[1];
                        break;
                    
                    case GCOS :
                        this.serviceGcos = data[1];
                        break;
                        
                    default :
                        
                }
            }
            if ( this.serviceUser == null ) 
                printErrorAndExit ( "service->user" );
            if ( this.serviceHost == null ) 
                printErrorAndExit ( "service->host" );
            if ( this.serviceGcos == null ) 
                printErrorAndExit ( "service->gcos" );

            /* SNOOP */
            String[] snoop = result.get("snoop").toString().replace("{","").replace("}","").split(",");            
            for ( String str : snoop ) {
                String[] data = str.split("=");
                switch ( data[0].trim().toUpperCase().hashCode() ) {
                    case ROOTSERV :
                        this.snoopRootServ = data[1];
                        break;
                        
                    case OPERSERV :
                        this.snoopOperServ = data[1];
                        break;
                    
                    case NICKSERV :
                        this.snoopNickServ = data[1];
                        break;
                    
                    case CHANSERV :
                        this.snoopChanServ = data[1];
                        break;
                    
                    case MEMOSERV :
                        this.snoopMemoServ = data[1];
                        break;
                        
                    default :
                        
                }
            }
            if ( this.snoopRootServ == null ) 
                printErrorAndExit ( "snoop->rootserv" );
            if ( this.snoopOperServ == null ) 
                printErrorAndExit ( "snoop->operserv" );
            if ( this.snoopNickServ == null ) 
                printErrorAndExit ( "snoop->nickserv" );
            if ( this.snoopChanServ == null ) 
                printErrorAndExit ( "snoop->chanserv" );
            if ( this.snoopMemoServ == null ) 
                printErrorAndExit ( "snoop->memoserv" );

            /* SRA */
            String[] sra = result.get("sra").toString().replace("{","").replace("}","").split(",");            
            for ( String str : sra ) {
                String data = str.replace("[","").replace("]","");
                this.setCommand ( data, str2acc( "SRA" ) );
            }
            
            /* CSOP */
            String[] csop = result.get("csop").toString().replace("{","").replace("}","").split(",");            
            for ( String str : csop ) {
                String data = str.replace("[","").replace("]","");
                this.setCommand ( data, str2acc( "CSOP" ) );
            }
            
            /* SA */
            String[] sa = result.get("sa").toString().replace("{","").replace("}","").split(",");            
            for ( String str : sa ) {
                String data = str.replace("[","").replace("]","");
                this.setCommand ( data, str2acc( "SA" ) );
            }
            
            /* IRCOP */
            String[] ircop = result.get("ircop").toString().replace("{","").replace("}","").split(",");            
            for ( String str : ircop ) {
                String data = str.replace("[","").replace("]","");
                this.setCommand ( data, str2acc( "IRCOP" ) );
            }
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
 
    private void setCommand ( String command, int access ) {
        switch ( command.trim().toUpperCase().hashCode() ) {
                
            case SRAW :
                this.sraw = access;
                break;
                
            case REHASH :
                this.rehash = access;
                break;
                
            case PANIC :
                this.panic = access;
                break;
                
            case UINFO :
                this.uinfo = access;
                break;
                
            case CINFO :
                this.cinfo = access;
                break;
                
            case SINFO :
                this.sinfo = access;
                break;
                
            case ULIST :
                this.ulist = access;
                break;
                
            case JUPE :
                this.jupe = access;
                break;
                
            case DELETE :
                this.delete = access;
                break;
                
            case SQLINE :
                this.sqline = access;
                break;
                
            case SGLINE :
                this.sgline = access;
                break;
                
            case CLOSE :
                this.close = access;
                break;
                
            case FREEZE :
                this.freeze = access;
                break;
                
            case HOLD :
                this.hold = access;
                break;
                
            case MARK :
                this.mark = access;
                break;
                    
            case NOGHOST :
                this.noghost = access;
                break;
                
            case GETPASS :
                this.getpass = access;
                break;
                
            case GETEMAIL :
                this.getemail = access;
                break;
                
            case AKILL :
                this.akill = access;
                break;
                
            case BANLOG :
                this.banlog = access;
                break;
                
            case GLOBAL :
                this.global = access;
                break;
                
            case IGNORE :
                this.ignore = access;
                break;
                
            case AUDIT :
                this.audit = access;
                break;
                
            case SERVER :
                this.server = access;
                break;
                
            case CHANLIST :
                this.chanlist = access;
                break;
                
            case LIST :
                this.list = access;
                break;
                
            case AUDITORIUM :
                this.auditorium = access;
                break;
                
            case STAFF :
                this.staff = access;
                break;
                
            case SEARCHLOG :
                this.searchlog = access;
                break;
                
            case UPTIME :
                this.uptime = access;
                break;
                
            case COMMENT :
                this.comment = access;
                break;
            
            case TOPICLOG :
                this.topiclog = access;
                break;

            default :
        }
    }
 
    private void parseValue ( String key, String val ) {
        //System.out.println ( "DEBUG: key:"+key+", val:"+val );
    }
    
    /* return the int value of the string input, else secure the command */
    private static int str2acc ( String str )  {
        switch ( str.toUpperCase().hashCode ( ) ) {
            case MASTER :
                return 5;
                
            case SRA :
                return 4;
                
            case CSOP :
                return 3;
                
            case SA :
                return 2;
                
            case IRCOP :
                return 1;
                
            case OFF :
                return 9999;
                
            default : 
                return 4; 
                
        }
    }
      
    boolean isValid ( ) {
        return this.valid;
    }
    
//    public boolean isMaster ( int hash ) { 
//        return this.master.toUpperCase().hashCode() == hash;
//    }

    public ArrayList<String> getWhiteList ( ) {
        return this.whiteList;
    }
    
    public int getInt ( int var )  {
        switch ( var )  {
            /* RootServ */
            case REHASH : 
                return this.rehash;
                
            case SRAW : 
                return this.sraw;
                
            case PANIC : 
                return this.panic;
                
            /* OperServ */
            case UINFO : 
                return this.uinfo;
                
            case CINFO :
                return this.cinfo;
                
            case SINFO :
                return this.sinfo;
                
            case ULIST :
                return this.ulist;
                
            case UPTIME :
                return this.uptime;
                
            case AKILL :
                return this.akill;
                
            case STAFF :
                return this.staff;
                      
            case SEARCHLOG :
                return this.searchlog;
            
            case AUDIT :
                return this.audit;
                         
            case COMMENT :
                return this.comment;
                       
            case GLOBAL :
                return this.global;
           
            case IGNORE :
                return this.ignore;
                        
            case BANLOG :
                return this.banlog;
                      
            case SQLINE :
                return this.sqline;
                
            case SGLINE :
                return this.sgline;
                
            case JUPE :
                return this.jupe;
                           
            case SERVER :
                return this.server;
                
            /* ChanServ */
            case FREEZE :
                return this.freeze;
                
            case CLOSE :
                return this.close;
            
            case HOLD :
                return this.hold;
            
            case LIST :
                return this.list;
            
            case CHANLIST :
                return this.chanlist;     
            
            case GETPASS :
                return this.getpass;
                          
            case GETEMAIL :
                return this.getemail;
               
            case MARK :
                return this.mark;
                     
            case NOGHOST :
                return this.noghost;
                
            case AUDITORIUM :
                return this.auditorium;
                    
            case DELETE :
                return this.delete;
                
            case TOPICLOG :
                return this.topiclog;
                
            default : 
                return -1;
                
        }
    }
    
    public String get ( int var )  {
        switch ( var )  {
            case NAME :
                return this.name;
                
            case DOMAIN :
                return this.domain;
                
            case NETNAME :
                return this.netname;
                
            case MASTER :
                return this.master;

            case SECRETSALT :
                return this.secretsalt;                

            case CONNNAME :
                return this.connName;
                
            case CONNHOST :
                return this.connHost;
                
            case CONNPASS :
                return this.connPass;
                
            case CONNPORT :
                return this.connPort;
                
            case STATS :
                return this.stats;
                
            case SNOOPROOTSERV :
                return this.snoopRootServ;
                
            case SNOOPOPERSERV :
                return this.snoopOperServ;
                
            case SNOOPNICKSERV :
                return this.snoopNickServ;
                
            case SNOOPCHANSERV :
                return this.snoopChanServ;
                
            case SNOOPMEMOSERV :
                return this.snoopMemoServ;
                
            case SERVICEUSER :
                return this.serviceUser;
                
            case SERVICEHOST :
                return this.serviceHost; 
                
            case SERVICEGCOS :
                return this.serviceGcos;
                
            case MYSQLHOST :
                return this.mysqlHost;
                
            case MYSQLUSER :
                return this.mysqlUser;
                
            case MYSQLPASS :
                return this.mysqlPass;
                
            case MYSQLPORT :
                return this.mysqlPort;
                
            case MYSQLDB :
                return this.mysqlDB;
                
            case AUTHURL :
                return this.authurl;
                
            case LOGFILE :
                return this.logfile;
                
            case EXPIRE :
                return this.expire;
                
            default :
                return null;
                
        } 
       
    }   
     
    public boolean getBoolean ( int command ) {
        switch ( command ) {
            case FORCEMODES :
                return this.forcemodes;
                
            default :
                return false;
                
        }
    }

}

