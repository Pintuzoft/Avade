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

import core.HashString;
import core.Proc;
import monitor.Snoop;
import user.User;

/**
 *
 * @author DreamHealer
 */
public class NSSnoop extends Snoop {

    public NSSnoop ( NickServ service )  {
        super ( );
        this.service            = service;
        this.chan               = Proc.getConf().get ( SNOOPNICKSERV );
    }
 
    public void msg ( boolean ok, HashString target, User user, String[] cmd )  { 
        this.log ( ok, target, user, cmd ); 
        this.sendTo ( ok, user, cmd ); 
    }
    public void msg ( boolean ok, HashString error, String target, User user, String[] cmd )  {
        this.msg(ok, error, new HashString(target), user, cmd);
    }
    public void msg ( boolean ok, HashString error, HashString target, User user, String[] cmd )  {
        String errstr = err2str(error);
        // System.out.println("DEBUG: msg: "+errstr+" ("+error+")");
        if ( ok ) {
            this.log ( ok, target, user, cmd ); 
        }
        this.sendTo ( ok, user, cmd, errstr ); 
    }
    
    private static String err2str ( HashString error ) {
        if      ( error.is(SYNTAX_ERROR) )          { return "SYNTAX_ERROR";        }
        else if ( error.is(SYNTAX_REG_ERROR) )      { return "SYNTAX_REG_ERROR";    }
        else if ( error.is(INVALID_EMAIL) )         { return "INVALID_EMAIL";       }
        else if ( error.is(NICK_ALREADY_REGGED) )   { return "NICK_ALREADY_REGGED"; }
        else if ( error.is(INVALID_NICK) )          { return "INVALID_NICK";        }
        else if ( error.is(REGISTER) )              { return "REGISTER";            }
        else if ( error.is(SYNTAX_ID_ERROR) )       { return "SYNTAX_ID_ERROR";     }
        else if ( error.is(NICK_NOT_REGISTERED) )   { return "NICK_NOT_REGISTERED"; }
        else if ( error.is(PASSWD_ERROR) )          { return "PASSWD_ERROR";        }
        else if ( error.is(IS_FROZEN) )             { return "IS_FROZEN";           }
        else if ( error.is(IS_THROTTLED) )          { return "IS_THROTTLED";        }
        else if ( error.is(IS_MARKED) )             { return "IS_MARKED";           }
        else if ( error.is(ACCESS_DENIED_OPER) )    { return "ACCESS_DENIED_OPER";  }
        else if ( error.is(ACCESS_DENIED_SA) )      { return "ACCESS_DENIED_SA";    }
        else if ( error.is(LIST) )                  { return "LIST";                }
        else if ( error.is(SIDENTIFY) )             { return "SIDENTIFY";           }
        else if ( error.is(SYNTAX_GHOST_ERROR) )    { return "SYNTAX_GHOST_ERROR";  }
        else if ( error.is(NO_SUCH_NICK) )          { return "NO_SUCH_NICK";        }
        else if ( error.is(IS_NOGHOST) )            { return "IS_NOGHOST";          }
        else if ( error.is(INFO) )                  { return "INFO";                }
        else if ( error.is(SET) )                   { return "SET";                 }
        else if ( error.is(AUTH) )                  { return "AUTH";                }
        else if ( error.is(AUTHMAIL) )              { return "AUTHMAIL";            }
        else if ( error.is(AUTHPASS) )              { return "AUTHPASS";            }
        else if ( error.is(NO_AUTH_FOUND) )         { return "NO_AUTH_FOUND";       }
        else if ( error.is(SETEMAIL) )              { return "SETEMAIL";            }
        else if ( error.is(SETPASSWD) )             { return "SETPASSWD";           }
        else {
            return "UnDefined";
        }
    }
} 
