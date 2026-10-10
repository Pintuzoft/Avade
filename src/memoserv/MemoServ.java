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
 * along with this program; if not, see <https://www.gnu.org/licenses/>.
 */
package memoserv;

import core.Handler;
import core.HashString;
import core.Service;
import core.TextFormat;
import core.WorkGuard;
import java.util.ArrayList;
import java.util.Arrays;
import nickserv.NickInfo;
import user.User;

/**
 *
 * @author DreamHealer
 */
public class MemoServ extends Service {
    private static boolean              state = false; 
    /* Memos live in memory like everything else, the database gets them
       when it is there: new ones, the ones that were read and the ones
       that were deleted, written every second. */
    private static final ArrayList<MemoInfo>    newMemos  = new ArrayList<> ( );
    private static final ArrayList<MemoInfo>    readMemos = new ArrayList<> ( );
    private static final ArrayList<MemoInfo>    delMemos  = new ArrayList<> ( );
    private MSExecutor                  executor;       /* Object that parse and execute commands */
    private MSHelper                    helper;         /* Object that parse and respond to help queries */
    private MSSnoop                     snoop;          /* Object that parse and respond to help queries */
    private TextFormat                  f;
    
    /**
     *
     */
    public MemoServ ( )  {
        super ( "MemoServ" );
        initMemoServ ( );    
    }

    private void initMemoServ ( )  {
        setState ( true );
        this.snoop      = new MSSnoop       ( this ); 
        this.executor   = new MSExecutor    ( this, this.snoop );
        this.helper     = new MSHelper      ( this, this.snoop ); 
        this.f          = new TextFormat    ( );
    }
    
    /**
     *
     * @param user
     * @param cmd
     */
    public void parse ( User user, String[] cmd )  {
        if ( ! Handler.isDataLoaded ( ) ) {
            this.sendMsg ( user, "Services are loading the nick and channel database, please try again in a moment." );
            return;
        }
        //:DreamHea1er PRIVMSG NickServ@services.sshd.biz :help
        if ( cmd == null || cmd[3].isEmpty ( )  )  { 
            return; 
        }
        
        if ( user.getUserFlood().tooFast ( this ) ) {
            return;
        }
        
        cmd[3] = cmd[3].substring ( 1 );
        HashString command = new HashString ( cmd[3] );
        if ( command.is(OHELP) ) {
            this.doOHelp ( user, cmd );
        
        } else if ( command.is(HELP) ) {
            this.helper.parse ( user, cmd );
        
        } else {
            this.executor.parse ( user, cmd, command );
        } 
         
    }
    

    /**
     *
     * @param stateVal
     */

    
    public static void addNewMemo ( MemoInfo memo ) {
        newMemos.add ( memo );
    }

    /**
     * The memo was read. One that is not stored yet is stored as read.
     * @param memo
     */
    public static void addReadMemo ( MemoInfo memo ) {
        if ( memo.getID ( ) > 0 && ! readMemos.contains ( memo ) ) {
            readMemos.add ( memo );
        }
    }

    /**
     * The memo was deleted. One that is not stored yet is just never stored.
     * @param memo
     */
    public static void addDelMemo ( MemoInfo memo ) {
        readMemos.remove ( memo );
        if ( ! newMemos.remove ( memo ) && memo.getID ( ) > 0 ) {
            delMemos.add ( memo );
        }
    }

    /**
     * The nick is dropped: its memos go with it in the database, and the
     * ones that still wait must not be stored for the next owner of the name
     * @param nick
     */
    public static void forget ( HashString nick ) {
        for ( ArrayList<MemoInfo> list : Arrays.asList ( newMemos, readMemos, delMemos ) ) {
            for ( MemoInfo memo : new ArrayList<> ( list ) ) {
                if ( nick.is ( new HashString ( memo.getName ( ) ) ) ) {
                    list.remove ( memo );
                }
            }
        }
    }

    /**
     * Write what waits to the database, called every second after NickServ
     * has written its nicks
     * @return how much still waits
     */
    public static int maintenance ( ) {
        if ( ( newMemos.isEmpty ( ) && readMemos.isEmpty ( ) && delMemos.isEmpty ( ) ) || ! MSDatabase.activateConnection ( ) ) {
            return newMemos.size ( ) + readMemos.size ( ) + delMemos.size ( );
        }
        write ( newMemos, 0 );
        write ( readMemos, 1 );
        write ( delMemos, 2 );
        return newMemos.size ( ) + readMemos.size ( ) + delMemos.size ( );
    }

    private static void write ( ArrayList<MemoInfo> list, int what ) {
        for ( int i = getIndexFromSize ( list.size ( ) ); i > 0; i-- ) {
            MemoInfo memo = list.get ( 0 );
            boolean ok = ( what == 0 ? MSDatabase.storeMemo ( memo ) != null : 
                           what == 1 ? MSDatabase.readMemo ( memo ) : MSDatabase.delMemo ( memo ) );
            if ( ok ) {
                WorkGuard.done ( memo );
                list.remove ( 0 );
            } else if ( WorkGuard.failed ( memo, "memo to "+memo.getName ( ) ) ) {
                list.remove ( 0 );
            } else {
                break; /* the database said no, try again next time */
            }
        }
    }

    public static void is ( boolean stateVal ) { 
        state = stateVal;
    }

    /**
     *
     * @return
     */
    public static boolean isUp ( ) { 
        return state;
    } 

    /**
     *
     * @param stateVal
     */
    public static void setState ( boolean stateVal ) {
        state = stateVal;
    }

    /**
     *
     * @param ni
     * @param u
     * @param count
     */
    public void adNick ( NickInfo ni, User u, int count )  {
        this.sendMsg ( u, "You have "+f.b ( ) +count+f.b ( ) +" new memo"+ ( count==1?"":"s" ) +"." );
    }
 
    
    /**
     *
     * @param user
     * @param cmd
     */
    public void doOHelp ( User user, String[] cmd )  {
        if ( ! user.isOper ( )  )  {
            this.snoop.msg ( false, new HashString ( "NickServ" ),user, cmd ); 
            return;
        }
    }
    
    /**
     *
     * @param ni
     * @param u
     */
    public void checkNick ( NickInfo ni, User u )  {
        if ( ni == null || u == null )  {
            return;
        }
        int count = 0;
        for ( MemoInfo m : ni.getMemos ( )  )  {
            if ( ! m.isRead ( )  )  {
                count++;
            }
        }

        if ( count > 0 )  {
            this.adNick ( ni, u, count );
        }  
    }
    
    /**
     *
     * @param memo
     */
    public void newMemo ( MemoInfo memo )  {
        User u = Handler.findUser ( memo.getName ( ) );
        
        if ( u != null )  {
            this.executor.adNewMemo ( memo, u );
        }
    }
}
