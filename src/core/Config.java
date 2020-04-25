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
    private HashString fileName;

    private HashString name;
    private HashString domain;
    private HashString netname;
    private HashString stats;
    private HashString master;
    private HashString authurl;
    private HashString logfile;
    private HashString expire;
    private HashString secretsalt;
    private boolean forcemodes;
    
    private HashString connName;
    private HashString connHost;
    private HashString connPort;
    private HashString connPass;
    
    private HashString mysqlHost;
    private HashString mysqlPort;
    private HashString mysqlUser;
    private HashString mysqlPass;
    private HashString mysqlDB;
    
    private ArrayList<HashString> whiteList = new ArrayList<>();
    
    private HashString serviceUser;
    private HashString serviceHost;
    private HashString serviceGcos;
    
    private boolean triggerWarn;
    private HashString triggerAction;
    private int triggerWarnIP;
    private int triggerWarnRange;
    private int triggerActionIP;
    private int triggerActionRange;
 
    private HashString snoopRootServ;
    private HashString snoopOperServ;
    private HashString snoopNickServ;
    private HashString snoopChanServ;
    private HashString snoopMemoServ;
    
    /* COMMANDS (set all to 5 - master) */
    private int stop = 5;
    private int rehash = 5;
    private int bahamut = 5;
    private int sraw = 5;
    private int panic = 5;
    private int uinfo = 5;
    private int cinfo = 5;
    private int ninfo = 5;
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
    private int makill = 5;
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
    private int forcenick = 5;
    private int snooplog = 5;
    
    public Config ( ) { 
        fileName = new HashString ( "services.conf" ); 
        this.init ( );
    }

    private void init ( ) {
        this.loadYamlConf();
    }
    
    private void printErrorAndExit ( String error ) {
        System.out.println ( "ConfigError: "+error );
        System.exit ( 1 );
    }
    private HashString parseKey ( Map<String,Object> result, String key ) {
        if ( result.get ( key ) == null ) {
            printErrorAndExit ( key );
        }
        return new HashString ( result.get(key).toString() );
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
            this.forcemodes = parseKey ( result, "forcemodes" ).is(YES);
          
            /* CONNECT */
            String[] connect = result.get("connect").toString().replace("{", "").replace("}", "").split(",");
             
            for ( String str : connect ) {
                String[] data = str.split("=");
                HashString it = new HashString ( data[0] );
                HashString val = new HashString ( data[1] );
                
                System.out.println("DEBUG: "+it.getCode()+":"+NAME.getCode());

                if ( it.getCode() == NAME.getCode() ) {
                    System.out.println("DEBUG: it == NAME");
                } else {
                    System.out.println("DEBUG: NOT!!");
                }


                if ( it.is(NAME) ) {
                    this.connName = new HashString ( data[1] );
                } else if ( it.is(HOST) ) {
                    this.connHost = new HashString ( data[1] );
                } else if ( it.is(PORT) ) {
                    this.connPort = new HashString ( data[1] );
                } else if ( it.is(PASS) ) {
                    this.connPass = new HashString ( data[1] );
                }
            }
            System.out.println("DEBUG: this.connName: "+this.connName);
            if ( this.connName == null ) {
                printErrorAndExit ( "connect->name" );
            }
            if ( this.connHost == null ) {
                printErrorAndExit ( "connect->host" );
            }
            if ( this.connPort == null ) {
                printErrorAndExit ( "connect->port" );
            }
            if ( this.connPass == null ) {
                printErrorAndExit ( "connect->pass" );
            }
            
            /* MYSQL */
            String[] mysql = result.get("mysql").toString().replace("{", "").replace("}", "").split(",");            
            for ( String str : mysql ) {
                String[] data = str.split("=");
                HashString it = new HashString ( data[0] );
                if ( it.is(HOST) ) {
                    this.mysqlHost = new HashString ( data[1] );
                } else if ( it.is(PORT) ) {
                    this.mysqlPort = new HashString ( data[1] );
                } else if ( it.is(USER) ) {
                    this.mysqlUser = new HashString ( data[1] );
                } else if ( it.is(PASS) ) {
                    this.mysqlPass = new HashString ( data[1] );
                } else if ( it.is(DB) ) {
                    this.mysqlDB = new HashString ( data[1] );
                }
            }
            
            if ( this.mysqlHost == null ) {
                printErrorAndExit ( "mysql->host" );
            }
            if ( this.mysqlPort == null ) {
                printErrorAndExit ( "mysql->port" );
            }
            if ( this.mysqlUser == null ) {
                printErrorAndExit ( "mysql->user" );
            }
            if ( this.mysqlPass == null ) {
                printErrorAndExit ( "mysql->pass" );
            }
            if ( this.mysqlDB == null ) {
                printErrorAndExit ( "mysql->db" );
            }
            
            /* WHITELIST */
            String[] wlist = result.get("whitelist").toString().replace("{","").replace("}","").split(",");            
            for ( String str : wlist ) {
                String data = str.replace("[","").replace("]","");
                this.whiteList.add ( new HashString ( data.trim() ) );
            }
            if ( this.whiteList.isEmpty() ) {
                System.out.println("Warning!: The whitelist is empty, add services ip and staff addresses");
            }
                      
            /* SERVICE */
            String[] service = result.get("service").toString().replace("{","").replace("}","").split(",");            
            for ( String str : service ) {
                String[] data = str.split("=");
                HashString it = new HashString ( data[0] );
                if ( it.is(USER) ) {
                    this.serviceUser = new HashString ( data[1] );
                } else if ( it.is(HOST) ) {
                    this.serviceHost = new HashString ( data[1] );
                } else if ( it.is(GCOS) ) {
                    this.serviceGcos = new HashString ( data[1] );
                }
            }
            
            if ( this.serviceUser == null ) {
                printErrorAndExit ( "service->user" );
            }
            if ( this.serviceHost == null ) {
                printErrorAndExit ( "service->host" );
            }
            if ( this.serviceGcos == null ) {
                printErrorAndExit ( "service->gcos" );
            }

            /* TRIGGER */
            String[] trigger = result.get("trigger").toString().replace("{","").replace("}","").split(",");
            for ( String str : trigger ) {
                String[] data = str.split("=");
                HashString it = new HashString ( data[0] );
                
                if ( it.is(WARN) ) {
                    this.triggerWarn = data[1].equalsIgnoreCase ( "true" );
                } else if ( it.is(ACTION) ) {
                    this.triggerAction = new HashString ( data[1] );
                } else if ( it.is(WARNIP) ) {
                    this.triggerWarnIP = Integer.parseInt ( data[1] );
                } else if ( it.is(WARNRANGE) ) {
                    this.triggerWarnRange = Integer.parseInt ( data[1] );
                } else if ( it.is(ACTIONIP) ) {
                    this.triggerActionIP = Integer.parseInt ( data[1] );
                } else if ( it.is(ACTIONRANGE) ) {
                    this.triggerActionRange = Integer.parseInt ( data[1] );
                }
                
            }

            if ( this.triggerWarnIP <= 0 ) {
                printErrorAndExit ( "trigger->warnip" );
            }
            if ( this.triggerWarnRange <= 0 ) {
                printErrorAndExit ( "trigger->warnrange" );
            }
            if ( this.triggerActionIP <= 0 ) {
                printErrorAndExit ( "trigger->akillip" );
            }
            if ( this.triggerActionRange <= 0 ) {
                printErrorAndExit ( "trigger->akillrange" );
            }
         
            /* SNOOP */
            String[] snoop = result.get("snoop").toString().replace("{","").replace("}","").split(",");            
            for ( String str : snoop ) {
                String[] data = str.split("=");
                System.out.println("DEBUG: "+data[0]+" : "+data[1]);
                HashString it = new HashString ( data[0] );
                
                if ( it.is(ROOTSERV) ) { 
                    this.snoopRootServ = new HashString ( data[1] );
                } else if ( it.is(OPERSERV) ) {
                    this.snoopOperServ = new HashString ( data[1] );
                } else if ( it.is(NICKSERV) ) {
                    this.snoopNickServ = new HashString ( data[1] );
                } else if ( it.is(CHANSERV) ) {
                    this.snoopChanServ = new HashString ( data[1] );
                } else if ( it.is(MEMOSERV) ) {
                    this.snoopMemoServ = new HashString ( data[1] );
                }

            }
            if ( this.snoopRootServ == null ) {
                printErrorAndExit ( "snoop->rootserv" );
            }
            if ( this.snoopOperServ == null ) {
                printErrorAndExit ( "snoop->operserv" );
            }
            if ( this.snoopNickServ == null ) {
                printErrorAndExit ( "snoop->nickserv" );
            }
            if ( this.snoopChanServ == null ) {
                printErrorAndExit ( "snoop->chanserv" );
            }
            if ( this.snoopMemoServ == null ) {
                printErrorAndExit ( "snoop->memoserv" );
            }

            /* SRA */
            String[] sra = result.get("sra").toString().replace("{","").replace("}","").split(",");            
            for ( String str : sra ) {
                HashString data = new HashString ( str.replace("[","").replace("]","") );
                this.setCommand ( data, str2acc ( SRA ) );
            }
            
            /* CSOP */
            String[] csop = result.get("csop").toString().replace("{","").replace("}","").split(",");            
            for ( String str : csop ) {
                HashString data = new HashString ( str.replace("[","").replace("]","") );
                this.setCommand ( data, str2acc ( CSOP ) );
            }
            
            /* SA */
            String[] sa = result.get("sa").toString().replace("{","").replace("}","").split(",");            
            for ( String str : sa ) {
                HashString data = new HashString ( str.replace("[","").replace("]","") );
                this.setCommand ( data, str2acc ( SA ) );
            }
            
            /* IRCOP */
            String[] ircop = result.get("ircop").toString().replace("{","").replace("}","").split(",");            
            for ( String str : ircop ) {
                HashString data = new HashString ( str.replace("[","").replace("]","") );
                this.setCommand ( data, str2acc ( IRCOP ) );
            }
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
 
    private void setCommand ( HashString it, int access ) {
        if ( it.is(STOP) )                  { this.stop         = access;       }
        if ( it.is(SRAW) )                  { this.sraw         = access;       }
        if ( it.is(REHASH) )                { this.rehash       = access;       }
        if ( it.is(BAHAMUT) )               { this.bahamut      = access;       }
        if ( it.is(PANIC) )                 { this.panic        = access;       }
        if ( it.is(UINFO) )                 { this.uinfo        = access;       }
        if ( it.is(CINFO) )                 { this.cinfo        = access;       }
        if ( it.is(NINFO) )                 { this.ninfo        = access;       }
        if ( it.is(SINFO) )                 { this.sinfo        = access;       }
        if ( it.is(ULIST) )                 { this.ulist        = access;       }
        if ( it.is(JUPE) )                  { this.jupe         = access;       }
        if ( it.is(DELETE) )                { this.delete       = access;       }
        if ( it.is(SQLINE) )                { this.sqline       = access;       }
        if ( it.is(SGLINE) )                { this.sgline       = access;       }
        if ( it.is(CLOSE) )                 { this.close        = access;       }
        if ( it.is(FREEZE) )                { this.freeze       = access;       }
        if ( it.is(HOLD) )                  { this.hold         = access;       }
        if ( it.is(MARK) )                  { this.mark         = access;       }
        if ( it.is(NOGHOST) )               { this.noghost      = access;       }
        if ( it.is(GETPASS) )               { this.getpass      = access;       }
        if ( it.is(GETEMAIL) )              { this.getemail     = access;       }
        if ( it.is(AKILL) )                 { this.akill        = access;       }
        if ( it.is(MAKILL) )                { this.makill       = access;       }
        if ( it.is(BANLOG) )                { this.banlog       = access;       }
        if ( it.is(GLOBAL) )                { this.global       = access;       }
        if ( it.is(IGNORE) )                { this.ignore       = access;       }
        if ( it.is(AUDIT) )                 { this.audit        = access;       }
        if ( it.is(SERVER) )                { this.server       = access;       }
        if ( it.is(CHANLIST) )              { this.chanlist     = access;       }
        if ( it.is(LIST) )                  { this.list         = access;       }
        if ( it.is(AUDITORIUM) )            { this.auditorium   = access;       }
        if ( it.is(STAFF) )                 { this.staff        = access;       }
        if ( it.is(SEARCHLOG) )             { this.searchlog    = access;       }
        if ( it.is(UPTIME) )                { this.uptime       = access;       }
        if ( it.is(COMMENT) )               { this.comment      = access;       }
        if ( it.is(TOPICLOG) )              { this.topiclog     = access;       }
        if ( it.is(FORCENICK) )             { this.forcenick    = access;       }
        if ( it.is(SNOOPLOG) )              { this.snooplog     = access;       }
    }
 
    private void parseValue ( String key, String val ) {
        //System.out.println ( "DEBUG: key:"+key+", val:"+val );
    }
    
    /* return the int value of the string input, else secure the command */
    private static int str2acc ( HashString it )  {
        if ( it.is(MASTER) )                { return 5;                         }
        if ( it.is(SRA) )                   { return 4;                         }
        if ( it.is(CSOP) )                  { return 3;                         }
        if ( it.is(SA) )                    { return 2;                         }
        if ( it.is(IRCOP) )                 { return 1;                         }
        if ( it.is(OFF) )                   { return 9999;                      }
        return 4;
    }
      
    boolean isValid ( ) {
        return this.valid;
    }
      
    public ArrayList<HashString> getWhiteList ( ) {
        return this.whiteList;
    }
    
    public int getInt ( HashString it )  {
        if ( it.is(STOP) )                  { return this.stop;                 }
        if ( it.is(SRAW) )                  { return this.sraw;                 }
        if ( it.is(REHASH) )                { return this.rehash;               }
        if ( it.is(BAHAMUT) )               { return this.bahamut;              }
        if ( it.is(PANIC) )                 { return this.panic;                }
        if ( it.is(UINFO) )                 { return this.uinfo;                }
        if ( it.is(CINFO) )                 { return this.cinfo;                }
        if ( it.is(NINFO) )                 { return this.ninfo;                }
        if ( it.is(SINFO) )                 { return this.sinfo;                }
        if ( it.is(ULIST) )                 { return this.ulist;                }
        if ( it.is(UPTIME) )                { return this.uptime;               }
        if ( it.is(AKILL) )                 { return this.akill;                }
        if ( it.is(MAKILL) )                { return this.makill;               }
        if ( it.is(STAFF) )                 { return this.staff;                }
        if ( it.is(SEARCHLOG) )             { return this.searchlog;            }
        if ( it.is(AUDIT) )                 { return this.audit;                }
        if ( it.is(COMMENT) )               { return this.comment;              }
        if ( it.is(GLOBAL) )                { return this.global;               }
        if ( it.is(IGNORE) )                { return this.ignore;               }
        if ( it.is(BANLOG) )                { return this.banlog;               }
        if ( it.is(SQLINE) )                { return this.sqline;               }
        if ( it.is(SGLINE) )                { return this.sgline;               }
        if ( it.is(JUPE) )                  { return this.jupe;                 }
        if ( it.is(SERVER) )                { return this.server;               }
        if ( it.is(FREEZE) )                { return this.freeze;               }
        if ( it.is(CLOSE) )                 { return this.close;                }
        if ( it.is(HOLD) )                  { return this.hold;                 }
        if ( it.is(LIST) )                  { return this.list;                 }
        if ( it.is(CHANLIST) )              { return this.chanlist;             }
        if ( it.is(GETPASS) )               { return this.getpass;              }
        if ( it.is(GETEMAIL) )              { return this.getemail;             }
        if ( it.is(MARK) )                  { return this.mark;                 }
        if ( it.is(NOGHOST) )               { return this.noghost;              }
        if ( it.is(AUDITORIUM) )            { return this.auditorium;           }
        if ( it.is(DELETE) )                { return this.delete;               }
        if ( it.is(TOPICLOG) )              { return this.topiclog;             }
        if ( it.is(FORCENICK) )             { return this.forcenick;            }
        if ( it.is(SNOOPLOG) )              { return this.snooplog;             }
        if ( it.is(TRIGGERWARNIP) )         { return this.triggerWarnIP;        }
        if ( it.is(TRIGGERWARNRANGE) )      { return this.triggerWarnRange;     }
        if ( it.is(TRIGGERACTIONIP) )       { return this.triggerActionIP;      }
        if ( it.is(TRIGGERACTIONRANGE) )    { return this.triggerActionRange;   }
        return -1;
    }
    
    
    
    public HashString get ( HashString it )  {
        if ( it.is(NAME) )                  { return this.name;                 }
        if ( it.is(DOMAIN) )                { return this.domain;               }
        if ( it.is(NETNAME) )               { return this.netname;              }
        if ( it.is(MASTER) )                { return this.master;               }
        if ( it.is(SECRETSALT) )            { return this.secretsalt;           }
        if ( it.is(CONNNAME) )              { return this.connName;             }
        if ( it.is(CONNHOST) )              { return this.connHost;             }
        if ( it.is(CONNPASS) )              { return this.connPass;             }
        if ( it.is(CONNPORT) )              { return this.connPort;             }
        if ( it.is(STATS) )                 { return this.stats;                }
        if ( it.is(SNOOPROOTSERV) )         { return this.snoopRootServ;        }
        if ( it.is(SNOOPOPERSERV) )         { return this.snoopOperServ;        }
        if ( it.is(SNOOPNICKSERV) )         { return this.snoopNickServ;        }
        if ( it.is(SNOOPCHANSERV) )         { return this.snoopChanServ;        }
        if ( it.is(SNOOPMEMOSERV) )         { return this.snoopMemoServ;        }
        if ( it.is(SERVICEUSER) )           { return this.serviceUser;          }
        if ( it.is(SERVICEHOST) )           { return this.serviceHost;          }
        if ( it.is(SERVICEGCOS) )           { return this.serviceGcos;          }
        if ( it.is(MYSQLHOST) )             { return this.mysqlHost;            }
        if ( it.is(MYSQLUSER) )             { return this.mysqlUser;            }
        if ( it.is(MYSQLPASS) )             { return this.mysqlPass;            }
        if ( it.is(MYSQLPORT) )             { return this.mysqlPort;            }
        if ( it.is(MYSQLDB) )               { return this.mysqlDB;              }
        if ( it.is(AUTHURL) )               { return this.authurl;              }
        if ( it.is(LOGFILE) )               { return this.logfile;              }
        if ( it.is(EXPIRE) )                { return this.expire;               }       
        if ( it.is(TRIGGERACTION) )         { return this.triggerAction;        }

        return null;
    }   
     
    public boolean getBoolean ( HashString in ) {
        if ( in.is(FORCEMODES) )            { return this.forcemodes;           }
        if ( in.is(TRIGGERWARN) )           { return this.triggerWarn;          }
        return false;
    }

}

