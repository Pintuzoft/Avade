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
package memoserv;

import chanserv.CSAcc;
import chanserv.ChanInfo;
import chanserv.ChanServ;
import nickserv.NickInfo;
import nickserv.NickServ;
import core.Executor;
import core.Handler;
import core.TextFormat;
import mail.SendMail;
import user.User;

/**
 *
 * @author DreamHealer
 */
 public class MSExecutor extends Executor {
    private MSSnoop             snoop;
    private TextFormat          f;
 
 
    public MSExecutor ( MemoServ service, MSSnoop snoop )  {
        super ( );
        this.service        = service;
        this.snoop          = snoop;
        this.f              = new TextFormat ( );
    }

    public void parse ( User user, String[] cmd )  {

        try {
            if ( cmd[3].isEmpty ( )  )  {
                this.help ( user );
                return; 
            }
        } catch ( Exception e )  {
            this.help ( user );
            return;
        }
        
        switch ( cmd[3].toUpperCase ( ) .hashCode ( )  )  {
            case SEND :
                this.doSend ( user, cmd );
                break;
                
            case CSEND :
                this.doCSend ( user, cmd );
                break;
                
            case LIST :
                this.doList ( user, cmd );
                break;
                
            case READ :
                this.doRead ( user, cmd );
                break;
                
            case DEL :
                this.doDelete ( user, cmd );
                break;
                
            default: 
                this.noMatch ( user, cmd[3] ); 
           
        }  
    }
 
    public void help ( User user )  {
        this.service.sendMsg ( user, output ( CMD_NOT_FOUND_ERROR, "" )  );
        this.service.sendMsg ( user, output ( SHOW_HELP, new String[] {this.service.getName ( ) } )  );
    }
   
    public void doSend ( User user, String[] cmd )  {
        //:DreamHealer PRIVMSG MemoServ@services.avade.net :send nick message
        //  0           1           2                       3     4     5   = 6
        NickInfo ni = NickServ.findNick ( user.getString ( NAME ) ) ; 
        NickInfo target;
        
        if ( cmd.length < 6 )  {
            this.service.sendMsg ( 
                user, 
                output ( SYNTAX_ERROR, "SEND <nick> <message>" )  
            );

        } else if ( ni == null )  {
            this.service.sendMsg ( 
                user, 
                output ( ACCESS_DENIED, user.getString ( NAME ) )  
            ); 
             
        } else if ( ! user.isIdented ( ni )  )  {
            this.service.sendMsg ( 
                user, 
                output ( ACCESS_DENIED, ni.getString ( NAME ) )
            );
            
        } else if ( ( target = NickServ.findNick ( cmd[4] ) ) == null )  {
            this.service.sendMsg ( 
                user, 
                output ( TARGET_NO_NICK, cmd[4] )
            );
         
        } else {
            this.sendToNick ( user,ni,target,cmd );
        }
    }
    
    private void sendToNick ( User user, NickInfo from, NickInfo to, String[] cmd )  {
        String message = Handler.cutArrayIntoString ( cmd, 5 );
        MemoInfo memo = new MemoInfo ( to.getName ( ), from.getName ( ), message );
        memo = MSDatabase.storeMemo ( memo );
        this.service.sendMsg ( user, output ( MEMO_SENT, to.getName ( )  )  );
        to.addMemo ( memo );
        SendMail.sendNewMemo ( to, memo );
    }
    
    public void doCSend ( User user, String[] cmd )  {
        //:DreamHealer PRIVMSG MemoServ@services.avade.net :csend chan message
        //  0           1           2                        3     4     5   = 6
        NickInfo ni = NickServ.findNick ( user.getString ( NAME ) );
        ChanInfo ci;
        
        if ( cmd.length < 6 )  {
            this.service.sendMsg ( 
                user, 
                output ( SYNTAX_ERROR, "SEND <nick> <message>" )
            );

        } else if ( ni == null )  {
            this.service.sendMsg ( 
                user, 
                output ( ACCESS_DENIED, user.getString ( NAME ) )
            ); 
             
        } else if ( ! user.isIdented ( ni )  )  {
            this.service.sendMsg ( 
                user, 
                output ( ACCESS_DENIED, ni.getString ( NAME ) )
            );
            
        } else if ( ( ci = ChanServ.findChan ( cmd[4] ) )  == null )  {
            this.service.sendMsg ( 
                user, 
                output ( TARGET_NO_CHAN, cmd[4] )
            );
         
        } else {
            /* User is idented and channel found */
            if ( ci.isAccess ( AOP, ni ) || ci.isAccess ( SOP, ni ) || ci.isFounder ( ni ) )  {
                /* Allow memo through */
                this.sendToNick ( user, ni, ci.getFounder ( ), cmd );
                
                for ( CSAcc op : ci.getAccessList ( SOP ) ) {
                    this.sendToNick ( user, ni, op.getNick ( ), cmd );
                }
                for ( CSAcc op : ci.getAccessList ( AOP ) ) {
                    this.sendToNick ( user, ni, op.getNick ( ), cmd );
                }
                
            } else {
                /* Do not allow memo */
                this.service.sendMsg ( 
                    user, 
                    output ( NO_ACCESS, ni.getString ( NAME ) )
                );
            }
        }
    }

    public void doList ( User user, String[] cmd )  {
        //:DreamHealer PRIVMSG MemoServ@services.avade.net :list
        //  0           1           2                       3   = 4        
        NickInfo ni = NickServ.findNick ( user.getString ( NAME ) );
        
        if ( cmd.length < 4 )  {
            this.service.sendMsg ( 
                user, 
                output ( SYNTAX_ERROR, "LIST" )
            );

        } else if ( ni == null )  {
            this.service.sendMsg ( 
                user, 
                output ( ACCESS_DENIED, user.getString ( NAME ) )
            ); 

        } else if ( ! user.isIdented ( ni )  )  {
            this.service.sendMsg ( 
                user, 
                output ( ACCESS_DENIED, ni.getString ( NAME ) )
            );
        
        } else {
            int read = 0;
            for ( MemoInfo m : ni.getMemos ( )  )  {
                if ( ! m.isRead ( )  )  {
                    read++;
                }
            }
            int index=1;
            this.service.sendMsg ( 
                user, 
                output ( 
                    LIST_START, 
                    ""+read, 
                    ""+ni.getMemos ( ) .size ( ) 
                )
            );
            for ( MemoInfo m : ni.getMemos ( )  )  {
                this.service.sendMsg ( 
                    user, 
                    output ( 
                        LIST_ENTRY,  
                        ( m.isRead ( ) ? " " : "*" ), 
                        ""+ ( index++ ), 
                        m.getSender ( ),
                        m.getStampStr ( )
                    )
                );
            }
            this.service.sendMsg ( 
                user, 
                output ( LIST_END, "LIST" ) );
             
        }
        
    }
    public void adNewMemo ( MemoInfo memo, User user )  {
        this.service.sendMsg ( 
            user, 
            output ( MEMO_RECIEVED, memo.getSender ( ) )
        );

    }

    public void doRead ( User user, String[] cmd )  {
        //:DreamHealer PRIVMSG MemoServ@services.avade.net :read 1
        //  0           1           2                       3    4 = 5
        NickInfo ni = NickServ.findNick ( user.getString ( NAME ) );
        MemoInfo memo = null;
       
        if ( cmd.length < 5 )  {
            this.service.sendMsg ( 
                user, 
                output ( SYNTAX_ERROR, "READ <#num>" ) 
            );
        
        } else if ( ni == null )  {
            this.service.sendMsg ( 
                user, 
                output ( ACCESS_DENIED, user.getString ( NAME ) )
            ); 
        
        } else if ( ! user.isIdented ( ni )  )  {
            this.service.sendMsg ( 
                user, 
                output ( ACCESS_DENIED, ni.getString ( NAME ) )
            );

        } else {
            try {
                memo = ni.getMemo ( Integer.parseInt ( cmd[4] )  );
            } catch ( NumberFormatException e )  {
                this.service.sendMsg ( 
                    user, 
                    output ( 
                        SYNTAX_ERROR, 
                        "READ <#num>  ( where #num is a number ) "
                    )
                );
            }
            
            if ( memo == null )  {
                this.service.sendMsg ( 
                    user, 
                    output ( NO_SUCH_MEMO, cmd[4] )
                );

            } else {
                /* we have a memo, lets print it */
                this.service.sendMsg ( 
                    user, 
                    output ( MEMO_START, cmd[4] )
                );
                this.service.sendMsg ( 
                    user, 
                    output ( MEMO_BODY, memo.getName ( ),
                    memo.getMessage ( ) )
                );
                if ( MSDatabase.readMemo ( memo )  )  {
                    memo.setRead ( );
                }
            }
        }
    }

    public void doDelete ( User user, String[] cmd )  {
        //:DreamHealer PRIVMSG MemoServ@services.avade.net :del 1
        //  0           1           2                       3    4 = 5
        NickInfo ni = NickServ.findNick ( user.getString ( NAME ) );
        MemoInfo memo = null;
       
        if ( cmd.length < 5 )  {
            this.service.sendMsg ( 
                user, 
                output ( SYNTAX_ERROR, "DEL <#num>" )
            );
        
        } else if ( ni == null )  {
            this.service.sendMsg ( 
                user, 
                output ( ACCESS_DENIED, user.getString ( NAME ) )
            ); 
        
        } else if ( ! user.isIdented ( ni ) )  {
            this.service.sendMsg ( 
                user, 
                output ( ACCESS_DENIED, ni.getString ( NAME ) )
            );

        } else {
            try {
                memo = ni.getMemo ( Integer.parseInt ( cmd[4] )  );
            } catch ( NumberFormatException e )  {
                this.service.sendMsg ( 
                    user, 
                    output ( 
                        SYNTAX_ERROR, 
                        "DEL <#num>  ( where #num is a number ) "
                    )
                );
            }
            
            if ( memo == null )  {
                this.service.sendMsg ( 
                    user, 
                    output ( NO_SUCH_MEMO, cmd[4] )
                );

            } else {
                /* we have a memo, lets print it */
                if ( MSDatabase.delMemo ( memo )  )  {
                    ni.delMemo ( memo );
                    this.service.sendMsg ( 
                        user, 
                        output ( DEL_SUCCESS, ""+cmd[4] )
                    );

                } else {
                    this.service.sendMsg ( 
                        user, 
                        output ( DEL_ERROR, "" )
                    );
                }
            }
        }
    }

     
    public String output ( int code, String... args )  {
        switch ( code )  {
            case SYNTAX_ERROR :
                return "Syntax: /MemoServ "+args[0]+"";
            
            case CMD_NOT_FOUND_ERROR :
                return "Syntax Error! For information regarding commands please issue:";
           
            case SHOW_HELP :
                return "    /"+args[0]+" HELP";
             
            case ACCESS_DENIED :
                return "Access denied. Please identify to "+args[0]+" before proceeding.";
           
            case TARGET_NO_NICK : 
                return "Nickname "+args[0]+" is not registered.";
           
            case TARGET_NO_CHAN :
                return "Channel "+args[0]+" is not registered.";
           
            case NO_ACCESS :
                return "Nickname "+args[0]+" has insufficient access to execute the command.";
  
            case NICK_NOT_REGISTERED : 
                return "Error: nick "+args[0]+" is not registered";
           
            case DB_ERROR : 
                return "Error: Database error. Please try register again in a few minutes.";
           
            case DB_NICK_ERROR :
                return "Error: Database Nick error. Please try again.";
            
            case LIST_START :
                return "Memos: "+args[0]+" unread of "+args[1]+" memos";
           
            case LIST_ENTRY : 
                return " "+args[0]+args[1]+" "+args[2]+" - "+args[3];
            
            case LIST_END :
                return "*** End of Memos ***";
                    
            case MEMO_SENT :
                return "Memo sent to: "+args[0];
            
            case MEMO_RECIEVED :
                return "New memo from: "+args[0];
            
            case NO_SUCH_MEMO :
                return "No memo found on position: "+args[0];

            case MEMO_START : 
                return "Memo "+args[0]+". To delete type: /MemoServ DEL "+args[0];
            
            case MEMO_BODY : 
                return "<"+args[0]+"> "+args[1];

            case DEL_ERROR :
                return "Something went wrong and memo was not deleted."; 
           
            case DEL_SUCCESS :
                return "Memo "+args[0]+" was successfully removed.";

            default:
                return ""; 
            
        }
    }
    
    private final static int SYNTAX_ERROR             = 1001; 

    private final static int CMD_NOT_FOUND_ERROR      = 1021;
    private final static int SHOW_HELP                = 1022;

    private final static int ACCESS_DENIED            = 1101;
    private final static int NICK_NOT_REGISTERED      = 1152;
    private final static int TARGET_NO_NICK           = 1153;
    private final static int TARGET_NO_CHAN           = 1154;
    private final static int NO_ACCESS                = 1155;
      
    private final static int DB_ERROR                 = 1401;
    private final static int DB_NICK_ERROR            = 1402;

    private final static int LIST_START               = 1431;    
    private final static int LIST_ENTRY               = 1432;
    private final static int LIST_END                 = 1433; 

    private final static int MEMO_SENT                = 1501; 
    private final static int MEMO_RECIEVED            = 1502;
    
    private final static int NO_SUCH_MEMO             = 1503; 

    private final static int MEMO_START               = 1551; 
    private final static int MEMO_BODY                = 1553; 

    private final static int DEL_ERROR                = 1554; 
    private final static int DEL_SUCCESS              = 1555; 

}
