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
import core.Proc;
import core.Services;
import operserv.Oper;
import operserv.OperServ;
import user.User;
import java.util.ArrayList;
import operserv.OSDatabase;

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
        switch ( cmd[1].hashCode ( ) ) {
            case MOTD :
                doMotd ( user, cmd );
                break;
                
            case VERSION :
                doVersion ( user, cmd );
                break;
                
            case INFO :
                doInfo ( user, cmd );
                break;
                
            case STATS : 
                doStats ( user, cmd );
                break;
                
            default : 
        } 
    }

    public void doMotd ( User user, String[] cmd )  { 
        if ( iCmp ( cmd[2], this.services.getString ( NAME )  )  )             { this.servicesMOTD ( user ); }
        else if ( iCmp ( cmd[2], this.services.getString ( STATS )  )  )       { this.statsMOTD ( user ); }         
    }
    
    public void doVersion ( User user, String[] cmd )  {
        if ( iCmp ( cmd[2], this.services.getString ( NAME )  )  )             { this.servicesVersion ( user ); }
        else if ( iCmp ( cmd[2], this.services.getString ( STATS )  )  )       { this.statsVersion ( user ); }   
    }
    
    public void doInfo ( User user, String[] cmd )  {
        if ( iCmp ( cmd[2], this.services.getString ( NAME )  )  )             { this.servicesInfo ( user ); }
        else if ( iCmp ( cmd[2], this.services.getString ( STATS )  )  )       { this.statsInfo ( user ); }   
    }
    
    public void doStats ( User user, String[] cmd )  {
        System.out.println ( "DEBUG: doStats!: "+cmd[2] );
        switch ( cmd[2].toUpperCase ( ) .hashCode ( )  )  {
            
            case CHAR_U : /* UPTIME */ 
                if ( iCmp ( cmd[3], this.services.getString ( NAME )  )  )  {
                    this.services.sendServicesCMD ( user, Numeric.RPL_STATSUPTIME,  "Server Up "+Proc.getUptime ( )  ); 
                } else if ( iCmp ( cmd[3], this.services.getString ( STATS )  )  )  {
                    this.services.sendStatsCMD ( user, Numeric.RPL_STATSUPTIME,  "Server Up "+Proc.getUptime ( )  ); 
                } else {
                    System.out.println ( "DEBUG: HERE!!" ); 
                } 
                break; 
            
            case CHAR_O : /* STAFF */
                ArrayList<Oper> oList = OperServ.findRootAdmins ( );
                oList.addAll ( OperServ.findCSOps ( )  );
                oList.addAll ( OperServ.findServicesAdmins ( )  );

                if ( iCmp ( cmd[3], this.services.getString ( NAME )  )  )  {
                    for ( Oper o : oList )  {
                        this.services.sendServicesCMD ( user, Numeric.RPL_STATSOLINE,  o.getString ( ACCSTRINGSHORT ) +" *@* "+o.getString ( NAME )  );
                    }
                } else if ( iCmp ( cmd[3], this.services.getString ( STATS )  )  )  {
                    for ( Oper o : oList )  {
                        this.services.sendStatsCMD ( user, Numeric.RPL_STATSOLINE,  o.getString ( ACCSTRINGSHORT ) +" *@* "+o.getString ( NAME )  );
                    }
                } 
                break; 
           
        }
    }
     
    /***  MOTD  ***/
    public void servicesMOTD ( User user )  {
        this.services.sendServicesCMD ( user, Numeric.RPL_MOTDSTART,  "***  ( Services )  Message of the day ***"                                   );
        this.services.sendServicesCMD ( user, Numeric.RPL_MOTD,       "     Services was created as a network bot to allow registration and"        );
        this.services.sendServicesCMD ( user, Numeric.RPL_MOTD,       "     administrating of nicks and channels. Without it any nick could"        );
        this.services.sendServicesCMD ( user, Numeric.RPL_MOTD,       "     used by anyone and users would not be able to maintain op(@) in"        );
        this.services.sendServicesCMD ( user, Numeric.RPL_MOTD,       "     their channels."                               );
        this.services.sendServicesCMD ( user, Numeric.RPL_MOTD,       "     "                                                                       );
        this.services.sendServicesCMD ( user, Numeric.RPL_MOTD,       "     For help with ownership issues speak to one of the CSop's in the"       );
        this.services.sendServicesCMD ( user, Numeric.RPL_MOTD,       "     list below. For help with floods or other disruptive issues feel"       );
        this.services.sendServicesCMD ( user, Numeric.RPL_MOTD,       "     free to message a services admin."                                      );
        this.services.sendServicesCMD ( user, Numeric.RPL_MOTD,       "     "                                                                       );

        /* CSOP */
        this.services.sendServicesCMD ( user, Numeric.RPL_MOTD,       "     "+b ( ) +"ChanServ Operators ( CSop )"+b ( )                        );
        String opers = new String ( );
        for ( Oper oper : OSDatabase.getCSopsPlus ( ) )  {
            if ( opers.length ( ) > 60 )  {
                this.services.sendServicesCMD ( user, Numeric.RPL_MOTD,       "      "+opers                                                          );
                opers = "";
            }
            opers += " "+oper.getString ( NAME );
        } 
        this.services.sendServicesCMD ( user, Numeric.RPL_MOTD,       "      "+opers                                                              );        
        this.services.sendServicesCMD ( user, Numeric.RPL_MOTD,       "     "                                                                       );
        
        /* SA */
        this.services.sendServicesCMD ( user, Numeric.RPL_MOTD,       "     "+b ( ) +"Services Admins ( SA ) "+b ( )                             );
        opers = new String ( );
        for ( Oper oper : OperServ.findServicesAdmins ( )  )  {
            if ( opers.length ( ) > 60 )  {
                this.services.sendServicesCMD ( user, Numeric.RPL_MOTD,       "      "+opers                                                          );
                opers = "";
            }
            opers += " "+oper.getString ( NAME );
        }
        this.services.sendServicesCMD ( user, Numeric.RPL_MOTD,       "      "+opers                                                               );        
        this.services.sendServicesCMD ( user, Numeric.RPL_MOTD,       "     "                                                                      );
        
        
        this.services.sendServicesCMD ( user, Numeric.RPL_MOTD,       "     Avade Services :"                                                      );
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
        this.services.sendServicesCMD ( user, Numeric.RPL_VERSION,    "Avade Services  ( aservices )  Version"                                      );
        this.services.sendServicesCMD ( user, Numeric.RPL_VERSION,    Proc.getVersion ( ) .getVersion ( )                                         );
    }
    public void statsVersion ( User user )  {
        this.services.sendServicesCMD ( user, Numeric.RPL_VERSION,    "Avade Services  ( aservices )  Version"                                      );
        this.services.sendServicesCMD ( user, Numeric.RPL_VERSION,    Proc.getVersion ( ) .getVersion ( )                                         );
    }

    /***  INFO  ***/
    public void servicesInfo ( User user )  {
        this.services.sendServicesCMD ( user, Numeric.RPL_INFOSTART,  "***  ( Services )  Information ***"                                                      );
        this.services.sendServicesCMD ( user, Numeric.RPL_INFO,       "     Avade Services  ( aservices )  User Services  ( NickServ, ChanServ, MemoServ ) "    );
        this.services.sendServicesCMD ( user, Numeric.RPL_INFO,       "     Version: qwerty"                                                                    );
        this.services.sendServicesCMD ( user, Numeric.RPL_INFO,       "     Developed by: DreamHealer  ( dreamhealer@avade.net ) "                              );
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
        this.services.sendStatsCMD ( user, Numeric.RPL_INFO,          "     Avade Services  ( aservices )  IRC Operator Services  ( OperServ, RootServ ) "      );
        this.services.sendStatsCMD ( user, Numeric.RPL_INFO,          "     Developed by: DreamHealer  ( dreamhealer@avade.net ) "                              );
        this.services.sendStatsCMD ( user, Numeric.RPL_INFO,          "     "                                                                                   );
        this.services.sendStatsCMD ( user, Numeric.RPL_INFO,          "     For IRC Operator assistance please join:"                                           );
        this.services.sendStatsCMD ( user, Numeric.RPL_INFO,          "           #OperHelp"                                                                    );
        this.services.sendStatsCMD ( user, Numeric.RPL_INFO,          "     " );
        this.services.sendStatsCMD ( user, Numeric.RPL_INFO,          "     Services uptime: "+Proc.getUptime ( )                                              );
        this.services.sendStatsCMD ( user, Numeric.RPL_INFOEND,       "*** End of Info ***"                                                                     );
    }
}