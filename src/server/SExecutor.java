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
package server;

import core.Executor;
import core.HashString;
import core.Proc;
import core.Services;
import operserv.Oper;
import operserv.OperServ;
import user.User;
import java.util.ArrayList;

/**
 *
 * @author DreamHealer
 */
public class SExecutor extends Executor {
    private Services services;

    public SExecutor ( Services services )  {
        super ( );
        this.services = services;
    }

    public void parse ( User user, String[] cmd )  {
        
        HashString command = new HashString ( cmd[1] );
        
        if ( command.is(MOTD) ) {
            doMotd ( user, cmd );
        } else if ( command.is(VERSION) ) {
            doVersion ( user, cmd );
        } else if ( command.is(INFO) ) {
            doInfo ( user, cmd );
        } else if ( command.is(STATS) ) {
            doStats ( user, cmd );
        }
        
    }
    public HashString nameToService ( String name ) {
        return nameToService ( new HashString ( name ) );
    }
    public HashString nameToService ( HashString name ) {
        if ( name.is(this.services.getName() ) ) {
            return SERVICES;
        } else if ( name.is(this.services.getString(STATS)) ) {
            return STATS;
        }
        return null;
    }
    
    public void doMotd ( User user, String[] cmd )  { 
        HashString name = nameToService ( cmd[2] );
        
        if ( name.is(SERVICES) ) {
            this.servicesMOTD ( user );
        
        } else if ( name.is(this.services.getString(STATS) ) ) {
            this.statsMOTD ( user );
        
        } else {
            this.servicesMOTD ( user );
        }
         
    }
    public void doVersion ( User user, String[] cmd )  { 
        HashString name = nameToService ( cmd[2] );

        if ( name.is(SERVICES) ) {
            this.servicesVersion ( user );
        
        } else if ( name.is(this.services.getString(STATS) ) ) {
            this.statsVersion ( user );
        
        } else {
            this.servicesVersion ( user );
        }
 
    }
    public void doInfo ( User user, String[] cmd )  { 
        HashString name = nameToService ( cmd[2] );
        
        if ( name.is(SERVICES) ) {
            this.servicesInfo ( user );
        
        } else if ( name.is(this.services.getString(STATS) ) ) {
            this.statsInfo ( user );
        
        } else {
            this.servicesInfo ( user );
        }
        
    }
   
    public void doStats ( User user, String[] cmd )  {
        HashString ch = new HashString ( cmd[2] );
        HashString name = new HashString ( cmd[3] );
        
        if ( ch.is(U) ) {
                if ( name.is(this.services.getName()) ) {
                    this.services.sendServicesCMD ( user, Numeric.RPL_STATSUPTIME,  "Server Up "+Proc.getUptime ( )  ); 
                } else if ( name.is(this.services.getString(STATS) ) ) {
                    this.services.sendStatsCMD ( user, Numeric.RPL_STATSUPTIME,  "Server Up "+Proc.getUptime ( )  ); 
                }     
        
        } else if ( ch.is(O) ) {
                ArrayList<Oper> oList = OperServ.findRootAdmins ( );
                oList.addAll ( OperServ.findCSOps ( )  );
                oList.addAll ( OperServ.findServicesAdmins ( )  );

                if ( name.is(this.services.getName()) ) {
                    for ( Oper o : oList )  {
                        this.services.sendServicesCMD ( user, Numeric.RPL_STATSOLINE,  o.getString ( ACCSTRINGSHORT ) +" *@* "+o.getString ( NAME )  );
                    }
                } else if ( name.is(this.services.getString(STATS) ) ) {
                    for ( Oper o : oList )  {
                        this.services.sendStatsCMD ( user, Numeric.RPL_STATSOLINE,  o.getString ( ACCSTRINGSHORT ) +" *@* "+o.getString ( NAME )  );
                    }
                }             
        }
        
    }
    
    /***  MOTD  ***/
    public void servicesMOTD ( User user )  {
        this.services.sendServicesCMD ( user, Numeric.RPL_MOTDSTART,  "***  ( Services )  Message of the day ***"                                   );
        this.services.sendServicesCMD ( user, Numeric.RPL_MOTD,       "     Avade IRC Services was created to assist users and staff with their"    );
        this.services.sendServicesCMD ( user, Numeric.RPL_MOTD,       "     daily interactions with the network allowing users to register nicks"   );
        this.services.sendServicesCMD ( user, Numeric.RPL_MOTD,       "     and channels while also assist staff to run and help users."            );
        this.services.sendServicesCMD ( user, Numeric.RPL_MOTD,       "     "                                                                       );
        this.services.sendServicesCMD ( user, Numeric.RPL_MOTD,       "     Below is listed staff members who may assist users with ownership"      );
        this.services.sendServicesCMD ( user, Numeric.RPL_MOTD,       "     questions. Feel free to contact one of the staff members if you have"   );
        this.services.sendServicesCMD ( user, Numeric.RPL_MOTD,       "     any question regarding ownership of nicks and channel or the network"   );
        this.services.sendServicesCMD ( user, Numeric.RPL_MOTD,       "     "                                                                       );

        /* CSOP */
        this.services.sendServicesCMD ( user, Numeric.RPL_MOTD,       "     "+b ( ) +"ChanServ Operators (CSop)"+b ( )                        );
        String opers = "";
        for ( Oper oper : OperServ.getStaffPlus ( CSOP ) )  {
            if ( opers.length ( ) > 60 )  {
                this.services.sendServicesCMD ( user, Numeric.RPL_MOTD,       "      "+opers                                                          );
                opers = "";
            }
            opers += " "+oper.getString ( NAME );
        } 
        this.services.sendServicesCMD ( user, Numeric.RPL_MOTD,       "      "+opers                                                              );        
        this.services.sendServicesCMD ( user, Numeric.RPL_MOTD,       "     "                                                                       );
         
        this.services.sendServicesCMD ( user, Numeric.RPL_MOTD,       "     Avade IRC Services :"                                                      );
        this.services.sendServicesCMD ( user, Numeric.RPL_MOTD,       "        Version: "+Proc.getVersion().getVersion()                           );
        this.services.sendServicesCMD ( user, Numeric.RPL_MOTD,       "          Coder: DreamHealer"                                               );
        this.services.sendServicesCMD ( user, Numeric.RPL_MOTD,       "         Uptime: "+Proc.getUptime ( )                                       );
        this.services.sendServicesCMD ( user, Numeric.RPL_MOTDEND,    "*** End of MOTD ***"                                                        );
    }
    
    public void statsMOTD ( User user )  {
        this.services.sendStatsCMD ( user, Numeric.RPL_MOTDSTART,     "***  ( Stats )  Message of the day ***"                                      );
        this.services.sendStatsCMD ( user, Numeric.RPL_MOTD,          "     Stats is a psudo-server linked to services."                            );
        this.services.sendStatsCMD ( user, Numeric.RPL_MOTDEND,       "*** End of MOTD ***"                                                         );
    }

    /***  MOTD  ***/
    public void servicesVersion ( User user )  {
        this.services.sendServicesCMD ( user, Numeric.RPL_VERSION,    "Avade IRC Services (Avade) Version"                                      );
        this.services.sendServicesCMD ( user, Numeric.RPL_VERSION,    Proc.getVersion().getVersion ( )                                         );
    }
    public void statsVersion ( User user )  {
        this.services.sendServicesCMD ( user, Numeric.RPL_VERSION,    "Avade IRC Services (Avade) Version"                                      );
        this.services.sendServicesCMD ( user, Numeric.RPL_VERSION,    Proc.getVersion().getVersion ( )                                         );
    }

    /***  INFO  ***/
    public void servicesInfo ( User user )  {
        this.services.sendServicesCMD ( user, Numeric.RPL_INFOSTART,  "***  ( Services )  Information ***"                                                      );
        this.services.sendServicesCMD ( user, Numeric.RPL_INFO,       "     Avade IRC Services (Avade) User Services (NickServ, ChanServ, MemoServ) "         );
        this.services.sendServicesCMD ( user, Numeric.RPL_INFO,       "     Version: "+Proc.getVersion().getVersion ( )                                         );
        this.services.sendServicesCMD ( user, Numeric.RPL_INFO,       "     Developed by: DreamHealer (dreamhealer@avade.net) "                              );
        this.services.sendServicesCMD ( user, Numeric.RPL_INFO,       "     "                                                                                   );
        this.services.sendServicesCMD ( user, Numeric.RPL_INFO,       "     For services related assistance please join:"                                       );
        this.services.sendServicesCMD ( user, Numeric.RPL_INFO,       "           #Help"                                                                        );
        this.services.sendServicesCMD ( user, Numeric.RPL_INFO,       "     "                                                                                   );
        this.services.sendServicesCMD ( user, Numeric.RPL_INFO,       "     For IRC Operator assistance please join:"                                           );
        this.services.sendServicesCMD ( user, Numeric.RPL_INFO,       "           #OperHelp"                                                                    );
        this.services.sendServicesCMD ( user, Numeric.RPL_INFO,       "     "                                                                                   );
        this.services.sendServicesCMD ( user, Numeric.RPL_INFO,       "     Services uptime: "+Proc.getUptime ( )                                              );
        this.services.sendServicesCMD ( user, Numeric.RPL_INFOEND,    "*** End of Info ***"                                                                     );
    }
    public void statsInfo ( User user )  {
        this.services.sendStatsCMD ( user, Numeric.RPL_INFOSTART,     "***  ( Stats )  Information ***"                                                         );
        this.services.sendStatsCMD ( user, Numeric.RPL_INFO,          "     Avade IRC Services (Avade) IRC Operator Services (OperServ, RootServ)"              );
        this.services.sendStatsCMD ( user, Numeric.RPL_INFO,          "     Developed by: DreamHealer (dreamhealer@avade.net) "                                 );
        this.services.sendStatsCMD ( user, Numeric.RPL_INFO,          "     "                                                                                   );
        this.services.sendStatsCMD ( user, Numeric.RPL_INFO,          "     For IRC Operator assistance please join:"                                           );
        this.services.sendStatsCMD ( user, Numeric.RPL_INFO,          "           #OperHelp"                                                                    );
        this.services.sendStatsCMD ( user, Numeric.RPL_INFO,          "     " );
        this.services.sendStatsCMD ( user, Numeric.RPL_INFO,          "     Services uptime: "+Proc.getUptime ( )                                               );
        this.services.sendStatsCMD ( user, Numeric.RPL_INFOEND,       "*** End of Info ***"                                                                     );
    }
}