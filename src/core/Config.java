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
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.yaml.snakeyaml.Yaml;
import user.User;
/**
 *
 * @author DreamHealer
 */
public class Config extends HashNumeric {
    private HashMap<BigInteger,HashString> configStr; 
    private HashMap<BigInteger,Integer> configInt;
    private HashMap<BigInteger,Boolean> configBool;
    private HashMap<BigInteger,HashString> whiteList;
    private HashMap<BigInteger,Integer> commands;
    private static final HashString[] cList = { 
        STOP,REHASH,BAHAMUT,SRAW,PANIC,UINFO,CINFO,NINFO,SINFO,ULIST,JUPE,
        DELETE,SQLINE,SGLINE,CLOSE,FREEZE,HOLD,MARK,NOGHOST,GETPASS,GETEMAIL,
        AKILL,MAKILL,BANLOG,GLOBAL,IGNORE,AUDIT,SERVER,CHANLIST,LIST,AUDITORIUM,
        STAFF,SEARCHLOG,UPTIME,COMMENT,TOPICLOG,FORCENICK,SNOOPLOG 
    };
    private static final HashString[] keyStrings = {
        NAME,DOMAIN,NETNAME,STATS,MASTER,AUTHURL, LOGFILE, EXPIRE, SECRETSALT,
        HUBNAME,HUBHOST,HUBPORT,HUBPASS,MYSQLHOST,MYSQLPORT,MYSQLUSER,MYSQLPASS,
        MYSQLDB,SERVICEUSER,SERVICEHOST,SERVICEGCOS,TRIGGERACTION,SNOOPROOTSERV,
        SNOOPOPERSERV,SNOOPCHANSERV,SNOOPNICKSERV,SNOOPMEMOSERV 
    };
    private static final HashString[] keyInts = {
        TRIGGERWARNIP,TRIGGERWARNRANGE,TRIGGERACTIONIP,TRIGGERACTIONRANGE
    };
    private static final HashString[] keyBools = {
        FORCEMODES,TRIGGERWARN
    };
    private boolean valid;
    private static final HashString fileName = new HashString ( "services.conf" );
    
    public Config ( ) { 
        this.configStr = new HashMap<>();
        this.configInt = new HashMap<>();
        this.configBool = new HashMap<>();
        this.whiteList = new HashMap<>();
        this.commands = new HashMap<>();
        this.init ( );
        // this.printCommands ( );
    }

    private void printCommands ( ) {
        System.out.println("printCommands:");
        for ( HashMap.Entry<BigInteger, Integer> cmd : commands.entrySet() ) {
            System.out.println("  - "+cmd.getKey()+":"+cmd.getValue());
        }
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
     
    private HashString[] getKeys ( HashString type ) {
        if ( type.is(STRING) ) {
            return keyStrings;
            
        } else if ( type.is(BOOLEAN) ) {
            return keyBools;

        } else if ( type.is(INTEGER) ) {
            return keyInts;
        }
        return null;
    }
    
    private HashMap getHashMap ( HashString type ) {
        if ( type.is(STRING) ) {
            return this.configStr;
        } else if ( type.is(BOOLEAN) ) {
            return this.configBool;
        } else if ( type.is(INTEGER) ) {
            return this.configInt;
        }
        return null;
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
            
            HashString[] types = { STRING, BOOLEAN, INTEGER };
            for ( HashString type : types ) {
                //System.out.println(""+type.getString()+":");
                HashString[] keys = this.getKeys ( type );            
                for ( HashString key : keys ) {
                    //System.out.println("   - "+key.getString());
                    HashString parsed = parseKey ( result, key.getString().toLowerCase() );
                    if ( type.is(BOOLEAN) ) {
                        this.getHashMap(type).put( key.getCode(), parsed.is(YES) );

                    } else if ( type.is(INTEGER) ) {
                        this.getHashMap(type).put(key.getCode(), Integer.parseInt(parsed.getString()));
                        
                    } else {
                        this.getHashMap(type).put( key.getCode(), parsed );
                    }
                }
            }
             
            /* WHITELIST */
            String[] wlist = result.get("whitelist").toString().replace("{","").replace("}","").split(",");            
            for ( String str : wlist ) {
                HashString data = new HashString ( str.replace("[","").replace("]","").trim() );
                this.whiteList.put ( data.getCode(), data );
            }
            if ( this.whiteList.isEmpty() ) {
                System.out.println("Warning!: The whitelist is empty, add services ip and staff addresses");
            }
              
            /* COMMANDS */
            HashString[] accesses = { SRA, CSOP, SA, IRCOP };
            for ( HashString access : accesses ) {
                //System.out.println(""+access.getString()+":");
                String[] cmds = fixResult ( result.get(access.getString().toLowerCase()).toString() );
                for ( String cmd : cmds ) {
                    //System.out.println("   - "+cmd);
                    HashString data = new HashString ( cmd );
                    this.setCommand ( data, str2acc ( access ) );
                }
            }
            
            this.valid = true;
        } catch (Exception ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private String[] fixResult ( String result ) {
        return result.replace("{","").replace("}","").replace("[","").replace("]","").split(",");
    }
    
    private void setCommand ( HashString command, int access ) {
        this.commands.put(command.getCode(), access);
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
      
    public HashMap<BigInteger,HashString> getWhiteList ( ) {
        return this.whiteList;
    }
    
    public int getInt ( HashString name )  {
        int access = this.configInt.get(name.getCode());
        return access;
    }
    
    public HashString get ( HashString name )  {
        return ( this.configStr.get(name.getCode()) != null ? this.configStr.get(name.getCode()) : null );
    }   
     
    public boolean getBoolean ( HashString name ) {
        return ( this.configBool.get(name.getCode()) != null ? this.configBool.get(name.getCode()) : false );
    }
    
    public ArrayList<String> getConfigList ( User user ) {
        ArrayList<String> list = new ArrayList<>();
        System.out.println("DEBUG: HERE!!");

        HashString[] types = { STRING,INTEGER,BOOLEAN };
        for ( HashString type : types ) {
            for ( HashString key : this.getKeys(type) ) {
                list.add( ""+key.getString().toLowerCase()+": "+this.getHashMap(type).get(key.getCode()) );
            }
        }

        list.add ( "whiteList: " );
        for ( HashMap.Entry<BigInteger,HashString> entry : this.whiteList.entrySet() ) {
            list.add ( " - "+entry.getValue().getString() );
        }
        
        HashString[] accesses = { IRCOP, SA, CSOP, SRA, MASTER };
        for ( HashString access : accesses ) {
            list.add ( ""+access.getString()+" commands:" );
            list.addAll ( getCommandsByAccess ( str2acc ( access ) ) );
        }
        
        return list;
    }
    
    private ArrayList<String> getCommandsByAccess ( int access ) {
        ArrayList<String> list = new ArrayList<>();
        for ( HashString cmd : cList ) {
            if ( this.commands.get(cmd.getCode()) == access ) {
                list.add ( " - "+cmd.getString().toLowerCase() );
            }
        }
        return list;
    }
    
    public int getCommandAccess ( HashString name ) {
        //System.out.println("name: "+name.getString()+":"+name.getCodeStr());
        if ( name == null ) {
            System.out.println("name is null!!");
        }
        if ( commands == null ) {
            System.out.println("commands is null!!");
        }     
        if ( commands.get(name.getCode()) == null ) {
            System.out.println("commands.get is null!!");
        }     
        
        return commands.get(name.getCode());
    }
}