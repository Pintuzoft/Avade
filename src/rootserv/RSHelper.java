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
package rootserv;

import core.CommandInfo;
import core.HashString;
import core.Helper;
import core.TextFormat;
import operserv.Oper;
import user.User;

/**
 *
 * @author DreamHealer
 */
class RSHelper extends Helper {
    private RSSnoop         snoop;
    private TextFormat      f;

    public RSHelper ( RootServ service, RSSnoop snoop )  {
        super ( service );
        this.snoop          = snoop;
        this.f              = new TextFormat ( );
    }

    public void parse ( User user, String[] cmd )  { 
        HashString command;
        this.found = true;
        if ( cmd.length <= 4 ) { 
            this.help ( user ); 
            return;
        } 
        Oper oper = user.getSID().getOper ( ); 
        if ( ! oper.isAtleast ( SRA )  )  {
            this.service.sendMsg ( user, "   RootServ is for Services Root Admins (SRA)  only .. *sigh*" );
            
        } else {
            command = new HashString ( cmd[4] );
            if ( command.is(HELP) ) {
                this.help ( user );
            
            } else if ( command.is(SRA) ) {
                this.sra ( user );
            
            } else if ( command.is(SHOWCONFIG) ) {
                this.showconfig ( user );

            } else if ( command.is(SRAW) ) {
                this.sraw ( user );
            
            } else if ( command.is(REHASH) ) {
                this.rehash ( user );
            
            } else if ( command.is(PANIC) ) {
                this.panic ( user );
            
            } else {
                this.found = false; 
                this.noMatch ( user, cmd[4] );
            } 
            
        } 
        this.snoop.msg ( this.found, user, cmd ); 
    }
    
    /* HELP */ 
    public void help ( User user )  {
        this.showStart ( user, "Help" ); 
        this.service.sendMsg ( user, "   RootServ allows you to manage the network, services and IRC Operators."            );
        this.service.sendMsg ( user, "   The purpose of Root Services is to aid you as network staff to manage the"         );
        this.service.sendMsg ( user, "   network"                                                                           );
        this.service.sendMsg ( user, "   For more help regarding a specific command please type:"                           );
        this.service.sendMsg ( user, "       "+f.b ( ) +"/RootServ HELP <command>"+f.b ( )                                );

        this.service.sendMsg ( user, "   "                                                                                  );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Root Administrator Commands"+f.b ( )                                 );
        this.service.sendMsg ( user, "   "                                                                                  );
        
        for ( CommandInfo ci : RootServ.getCMDList ( SRA )  )  {
            this.service.sendMsg ( user, "       "+f.b ( ) +ci.getName ( ) +f.b ( ) +ci.getPatch ( ) +ci.getDescription ( )  );
        }
        
        this.showEnd ( user );
    }
    
    /* SRA */ 
    public void sra ( User user )  { 
        this.showStart ( user, "SRA" );
        this.service.sendMsg ( user, "   "                                                                                  );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /RootServ SRA <ADD|DEL|LIST> [<nick>]"+f.b ( ) +""             );
        this.service.sendMsg ( user, "   "                                                                                  );
        this.service.sendMsg ( user, "   This command manage the root admin list which is a list with opers that _needs_"   );
        this.service.sendMsg ( user, "   the absolute access to services."                                                  );
        this.service.sendMsg ( user, "   "                                                                                  );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                     );
        this.service.sendMsg ( user, "   Do not add anyone here as it gives absolute access to services which includes"     );
        this.service.sendMsg ( user, "   potential harm and wreckage of the network and its reputation."                    );
        this.showEnd ( user );
    }
    
    /* SRAW */ 
    public void sraw ( User user )  { 
        this.showStart ( user, "SRAW" );
        this.service.sendMsg ( user, "   "                                                                                  );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /RootServ SRAW <command>"+f.b ( ) +""                          );
        this.service.sendMsg ( user, "   "                                                                                  );
        this.service.sendMsg ( user, "   This command enables raw server command interaction with the network"              );
        this.service.sendMsg ( user, "   Use with caution as it could break the network."                                   );
        this.service.sendMsg ( user, "   "                                                                                  );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                     );
        this.service.sendMsg ( user, "   If you dont have a clear purpose for it or dont know how to use this command"      );
        this.service.sendMsg ( user, "   then DONT USE IT. All is logged."                                                  );
        this.showEnd ( user );
    }
    
    /* REHASH */ 
    public void rehash ( User user )  { 
        this.showStart ( user, "REHASH" );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /RootServ REHASH"+f.b ( ) +""                                      );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   This command makes services reread its configuration file allowing reconfigurations"   );
        this.service.sendMsg ( user, "   on the fly."                                                                           );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                         );
        this.service.sendMsg ( user, "   Only load configurations you are suppose to load."                                     );
        this.showEnd ( user );
    }
    /* REHASH */ 
    public void showconfig ( User user )  { 
        this.showStart ( user, "SHOWCONFIG" );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /RootServ SHOWCONFIG"+f.b ( ) +""                                      );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   This command makes services print the current running config, "                         );
        this.service.sendMsg ( user, "   changing the config can only be done in the config file."                               );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                             );
        this.service.sendMsg ( user, "   Only run this in a secure client."                                                     );
        this.showEnd ( user );
    }
        /* REHASH */ 
    public void panic ( User user )  { 
        this.showStart ( user, "PANIC" );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   "+f.b ( ) +"Syntax: /RootServ PANIC [<OPER|IDENT|USER>]"+f.b ( ) +""                   );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   This command will set the services panic level on the network. The following"          );
        this.service.sendMsg ( user, "   states can be applied:"                                                                );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "      OPER : Only IRC operators can use services commands"                                );
        this.service.sendMsg ( user, "     IDENT : Only identified (+r) users can use services commands"                        );
        this.service.sendMsg ( user, "      USER : Services works as normal and all can use services"                           );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   When the command is issued without a state, the current state will be printed."        );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   Managing the services panic states will help you cope with services abuse such"        );
        this.service.sendMsg ( user, "   as services flooding. The normal state is USER, and as long as there is a different"   );
        this.service.sendMsg ( user, "   state services will announce it in globops to make sure the state is not permanently"  );
        this.service.sendMsg ( user, "   applied."                                                                              );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   A user will get a \"services down\" error when this is in a state that filters "       );
        this.service.sendMsg ( user, "   that user out from sending commands to services."                                                                                      );
        this.service.sendMsg ( user, "   "                                                                                      );
        this.service.sendMsg ( user, "   "+f.r ( ) +"Security recommendations:"+f.r ( )                                         );
        this.service.sendMsg ( user, "   This command should only be set in extreme circumstances. It can assist during"        );
        this.service.sendMsg ( user, "   services abuse such as floodings etc. Changing the state will filter services"         );
        this.service.sendMsg ( user, "   commands on an ircd level."           );
        this.showEnd ( user );
    }
}
