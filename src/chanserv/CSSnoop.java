/* 
 * Copyright (C) 2018 Fredrik Karlsson aka DreamHealer & avade.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation"; either version 2
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

import core.HashString;
import core.Proc;
import monitor.Snoop;
import user.User;

/**
 *
 * @author DreamHealer
 */
public class CSSnoop extends Snoop {

    /**
     *
     * @param service
     */
    public CSSnoop ( ChanServ service )  {
        super ( );
        this.chan  = Proc.getConf().get ( SNOOPCHANSERV );
        this.service        = service;
    }
 
    /**
     *
     * @param ok
     * @param target
     * @param user
     * @param cmd
     */
    public void msg ( boolean ok, HashString target, User user, String[] cmd )  { 
        this.log ( ok, target, user, cmd );
        this.sendTo ( ok, user, cmd );
    }
    
    /**
     *
     * @param ok
     * @param error
     * @param target
     * @param user
     * @param cmd
     */
    public void msg ( boolean ok, HashString error, String target, User user, String[] cmd ) {
        this.msg(ok, error, new HashString ( target ), user, cmd);
    }
    
    /**
     *
     * @param ok
     * @param error
     * @param target
     * @param user
     * @param cmd
     */
    public void msg ( boolean ok, HashString error, HashString target, User user, String[] cmd )  {
        String errstr = err2str(error);
        // System.out.println("DEBUG: msg: "+errstr+" ("+error+")");
        if ( ok ) {
            this.log ( ok, target, user, cmd ); 
        }
        this.sendTo ( ok, user, cmd, errstr ); 
    }
    private static String err2str ( HashString it ) {
            if      ( it.is(SYNTAX_ERROR) )             { return "SYNTAX_ERROR";            }
            else if ( it.is(SYNTAX_ID_ERROR) )          { return "SYNTAX_ID_ERROR";         }
            else if ( it.is(SYNTAX_REG_ERROR) )         { return "SYNTAX_REG_ERROR";        }
            else if ( it.is(CMD_NOT_FOUND_ERROR) )      { return "CMD_NOT_FOUND_ERROR";     }
            else if ( it.is(SHOW_HELP) )                { return "SHOW_HELP";               }
            else if ( it.is(ACCESS_DENIED) )            { return "ACCESS_DENIED";           }
            else if ( it.is(NOT_ENOUGH_ACCESS) )        { return "NOT_ENOUGH_ACCESS";       }
            else if ( it.is(SETTING_NOT_FOUND) )        { return "SETTING_NOT_FOUND";       }
            else if ( it.is(NICK_NOT_REGISTERED) )      { return "NICK_NOT_REGISTERED";     }
            else if ( it.is(NICK_NOT_AUTHED) )          { return "NICK_NOT_AUTHED";         }
            else if ( it.is(NICK_NOT_IDENTIFIED) )      { return "NICK_NOT_IDENTIFIED";     }
            else if ( it.is(CHAN_NOT_REGISTERED) )      { return "CHAN_NOT_REGISTERED";     }
            else if ( it.is(CHAN_ALREADY_REGGED) )      { return "CHAN_ALREADY_REGGED";     }
            else if ( it.is(CHAN_NOT_EXIST) )           { return "CHAN_NOT_EXIST";          }
            else if ( it.is(NICK_NOT_EXIST) )           { return "NICK_NOT_EXIST";          }
            else if ( it.is(USER_NOT_ONLINE) )          { return "USER_NOT_ONLINE";         }
            else if ( it.is(USER_NOT_OP) )              { return "USER_NOT_OP";             }
            else if ( it.is(NICK_HAS_NOOP) )            { return "NICK_HAS_NOOP";           }
            else if ( it.is(WILL_NOW) )                 { return "WILL_NOW";                }
            else if ( it.is(WILL_NOW_NOT) )             { return "WILL_NOW_NOT";            }
            else if ( it.is(IS_NOW) )                   { return "IS_NOW";                  }
            else if ( it.is(IS_NOT) )                   { return "IS_NOT";                  }
            else if ( it.is(PASSWD_ERROR) )             { return "PASSWD_ERROR";            }
            else if ( it.is(INVALID_EMAIL) )            { return "INVALID_EMAIL";           }
            else if ( it.is(INVALID_PASSWORD) )         { return "INVALID_PASSWORD";        }
            else if ( it.is(PASSWD_ACCEPTED) )          { return "PASSWD_ACCEPTED";         }
            else if ( it.is(DB_ERROR) )                 { return "DB_ERROR";                }
            else if ( it.is(DB_NICK_ERROR) )            { return "DB_NICK_ERROR";           }
            else if ( it.is(REGISTER_DONE) )            { return "REGISTER_DONE";           }
            else if ( it.is(REGISTER_SEC) )             { return "REGISTER_SEC";            }
            else if ( it.is(NICK_IS_FOUNDER) )          { return "NICK_IS_FOUNDER";         }
            else if ( it.is(NICK_IS_SOP) )              { return "NICK_IS_SOP";             }
            else if ( it.is(NICK_IS_OP) )               { return "NICK_IS_OP";              }
            else if ( it.is(NICK_NOT_FOUND) )           { return "NICK_NOT_FOUND";          }
            else if ( it.is(NICK_CHANGED) )             { return "NICK_CHANGED";            }
            else if ( it.is(NICK_NOT_PRESENT) )         { return "NICK_NOT_PRESENT";        }
            else if ( it.is(NICK_NOT_IDENTED_OP) )      { return "NICK_NOT_IDENTED_OP";     }
            else if ( it.is(NICK_NEVEROP) )             { return "NICK_NEVEROP";            }
            else if ( it.is(NICK_ADDED) )               { return "NICK_ADDED";              }
            else if ( it.is(NICK_NOT_ADDED) )           { return "NICK_NOT_ADDED";          }
            else if ( it.is(NICK_DELETED) )             { return "NICK_DELETED";            }
            else if ( it.is(NICK_NOT_DELETED) )         { return "NICK_NOT_DELETED";        }
            else if ( it.is(NICK_VERBOSE_ADDED) )       { return "NICK_VERBOSE_ADDED";      }
            else if ( it.is(NICK_VERBOSE_DELETED) )     { return "NICK_VERBOSE_DELETED";    }
            else if ( it.is(NICK_INVITED) )             { return "NICK_INVITED";            }
            else if ( it.is(NICK_OP) )                  { return "NICK_OP";                 }
            else if ( it.is(NICK_DEOP) )                { return "NICK_DEOP";               }
            else if ( it.is(NICK_VERBOSE_OP) )          { return "NICK_VERBOSE_OP";         }
            else if ( it.is(NICK_VERBOSE_DEOP) )        { return "NICK_VERBOSE_DEOP";       }
            else if ( it.is(NICK_MDEOP_CHAN) )          { return "NICK_MDEOP_CHAN";         }
            else if ( it.is(NICK_MDEOP) )               { return "NICK_MDEOP";              }
            else if ( it.is(NICK_MKICK_CHAN) )          { return "NICK_MKICK_CHAN";         }
            else if ( it.is(NICK_MKICK) )               { return "NICK_MKICK";              }
            else if ( it.is(LIST_NOT_WIPED) )           { return "LIST_NOT_WIPED";          }
            else if ( it.is(LIST_WIPED) )               { return "LIST_WIPED";              }
            else if ( it.is(LIST_VERBOSE_WIPED) )       { return "LIST_VERBOSE_WIPED";      }
            else if ( it.is(CHAN_IS_FROZEN) )           { return "CHAN_IS_FROZEN";          }
            else if ( it.is(CHAN_IS_CLOSED) )           { return "CHAN_IS_CLOSED";          }
            else if ( it.is(CHAN_IS_RELAY) )            { return "CHAN_IS_RELAY";           }
            else if ( it.is(CHAN_SET_FLAG) )            { return "CHAN_SET_FLAG";           } 
            else if ( it.is(CHANFLAG_EXIST) )           { return "CHANFLAG_EXIST";          } 
            else if ( it.is(ALREADY_ON_LIST) )          { return "ALREADY_ON_LIST";         } 
            else if ( it.is(CHAN_GETPASS) )             { return "CHAN_GETPASS";            }
            else if ( it.is(CHAN_INFO) )                { return "CHAN_INFO";               }
            else if ( it.is(CHAN_UNBAN) )               { return "CHAN_UNBAN";              }
            else if ( it.is(ACCESS_LIST) )              { return "ACCESS_LIST";             }
            else if ( it.is(ACCESS_ADDED) )             { return "ACCESS_ADDED";            }
            else if ( it.is(ACCESS_DELETED) )           { return "ACCESS_DELETED";          }
            else if ( it.is(ACCESS_DENIED_SRA) )        { return "ACCESS_DENIED_SRA";       }
            else if ( it.is(IS_MARKED) )                { return "IS_MARKED";               } 
            else if ( it.is(IS_THROTTLED) )             { return "IS_THROTTLED";            }
            else if ( it.is(SHOWACCESSLOG) )            { return "SHOWACCESSLOG";           }  
            else if ( it.is(SHOWACCESSLOGOPER) )        { return "SHOWACCESSLOGOPER";       } 
            else if ( it.is(SHOWTOPICLOG) )             { return "SHOWTOPICLOG";            } 
            else if ( it.is(CHANNELDROPPED) )           { return "CHANNELDROPPED";          } 
            else if ( it.is(CHANNELDELETED) )           { return "CHANNELDELETED";          } 
            else if ( it.is(NO_SUCH_CHANFLAG) )         { return "NO_SUCH_CHANFLAG";        } 
            else if ( it.is(BAD_CHANFLAG_VALUE) )       { return "BAD_CHANFLAG_VALUE";      } 
            else if ( it.is(XOP_NOT_FOUND) )            { return "XOP_NOT_FOUND";           } 
            else if ( it.is(XOP_ADD_FAIL) )             { return "XOP_ADD_FAIL";            } 
            else if ( it.is(XOP_ALREADY_PRESENT) )      { return "XOP_ALREADY_PRESENT";     } 
            else if ( it.is(SET_DESCRIPTION) )          { return "SET_DESCRIPTION";         } 
            else if ( it.is(SET_TOPICLOCK) )            { return "SET_TOPICLOCK";           } 
            else if ( it.is(SET_MODELOCK) )             { return "SET_MODELOCK";            } 
            else if ( it.is(SET_KEEPTOPIC) )            { return "SET_KEEPTOPIC";           } 
            else if ( it.is(SET_IDENT) )                { return "SET_IDENT";               } 
            else if ( it.is(SET_OPGUARD) )              { return "SET_OPGUARD";             } 
            else if ( it.is(SET_RESTRICT) )             { return "SET_RESTRICT";            } 
            else if ( it.is(SET_VERBOSE) )              { return "SET_VERBOSE";             } 
            else if ( it.is(SET_MAILBLOCK) )            { return "SET_MAILBLOCK";           } 
            else if ( it.is(SET_LEAVEOPS) )             { return "SET_LEAVEOPS";            } 
            else if ( it.is(SET_AUTOAKICK) )            { return "SET_AUTOAKICK";           }
            else if ( it.is(SHOW_LIST) )                { return "SHOW_LIST";               }
            else { 
                return "UnDefined"; 
            }
    } 
}
