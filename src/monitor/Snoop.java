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
package monitor;

import core.Database;
import core.HashNumeric;
import core.HashString;
import core.Service;
import core.WorkGuard;
import java.util.ArrayList;
import user.User;

/**
 *
 * @author DreamHealer
 */
public class Snoop extends HashNumeric {

    /**
     *
     */
    protected HashString chan;

    /**
     *
     */
    protected Service service;

    /**
     *
     */
    protected static ArrayList<SnoopLog> logs = new ArrayList<>();
            
    /**
     *
     */
    public Snoop ( )  { 
        /* nothingness */
    }

    /**
     *
     * @param ok
     * @param user
     * @param arr
     * @return
     */
    public String fixArray ( boolean ok, User user, String[] arr )  {
         
        arr = this.redact ( words ( arr ) );
        String str = "";
        int index = 3;
        int start = 3;

        while ( index < arr.length )  {
            if ( index == start )  {
                str += ( ! ok ? "*" : "" );
                str += "["+arr[index].toUpperCase ( ) +"]:";
            
            } else {
                str += " "+arr[index];
            }
            ++index;
        }
        str += " - ("+ ( user != null ? user.getFullMask ( ) : "-" ) +")";
        return str;
    }

    /**
     *
     * @return
     */
    /**
     * Mask secrets (passwords, auth codes) in a command before it is
     * sent to the snoop channel or written to the snoop log.
     * Subclasses override this for their own commands.
     * @param arr
     * @return
     */
    protected String[] redact ( String[] arr ) {
        return arr;
    }

    /**
     * Returns a copy of arr where the given indexes are masked
     * @param arr
     * @param indexes
     * @return
     */
    protected static String[] mask ( String[] arr, int... indexes ) {
        String[] copy = arr.clone ( );
        for ( int index : indexes ) {
            if ( index > 3 && index < copy.length ) {
                copy[index] = "<redacted>";
            }
        }
        return copy;
    }

    /**
     * Returns a copy of arr where everything from the given index on is
     * masked: for commands where a secret follows, whatever comes after it
     * or instead of it (arguments in the wrong order) is not shown either
     * @param arr
     * @param from
     * @return
     */
    protected static String[] maskFrom ( String[] arr, int from ) {
        String[] copy = arr.clone ( );
        for ( int index = Math.max ( from, 4 ); index < copy.length; index++ ) {
            copy[index] = "<redacted>";
        }
        return copy;
    }

    /* The text as the words it has. It was split at every space, so two
       spaces in a row give an empty word, and that would move a password to
       a place that is not masked. */
    private static String[] words ( String[] arr ) {
        if ( arr == null || arr.length < 4 ) {
            return arr;
        }
        ArrayList<String> list = new ArrayList<> ( );
        for ( int index = 0; index < arr.length; index++ ) {
            String word = arr[index];
            if ( index == 3 && word.startsWith ( ":" ) ) {
                word = word.substring ( 1 );
            }
            if ( index < 3 || ( word != null && ! word.isEmpty ( ) ) ) {
                list.add ( word );
            }
        }
        if ( list.size ( ) < 4 ) {
            list.add ( "" );    /* a text of nothing but spaces */
        }
        return list.toArray ( new String[0] );
    }

    /**
     * Returns the command name (arr[3] without the leading ':') in upper case
     * @param arr
     * @return
     */
    protected static String commandOf ( String[] arr ) {
        if ( arr == null || arr.length < 4 ) {
            return "";
        }
        return arr[3].replaceFirst ( "^:", "" ).toUpperCase ( );
    }

    /**
     * @return the snoop log rows that wait for the database
     */
    public static int waiting ( ) {
        return logs.size ( );
    }

    public int maintenance ( ) {
        int todoAmount = 0;
        todoAmount += writeLogs ( );
        return todoAmount;
    }
    
    private static int writeLogs ( ) {
        if ( Database.activateConnection ( ) && !logs.isEmpty() ) {
            ArrayList<SnoopLog> eLogs = new ArrayList<>();
            for ( SnoopLog log : logs.subList ( 0, getIndexFromSize ( logs.size() ) ) ) {
                if ( Database.SnoopLog ( log ) ) {
                    WorkGuard.done ( log );
                    eLogs.add ( log );
                } else if ( WorkGuard.failed ( log, "snoop log" ) ) {
                    eLogs.add ( log );
                }
            }
            for ( SnoopLog log : eLogs ) {
                logs.remove ( log );
            }
        }
        return logs.size();
    }
    
    /**
     *
     * @param size
     * @return
     */
    protected static int getIndexFromSize ( int size ) {
        return size > 5 ? 5 : size;
    }
    
    /**
     *
     * @param ok
     * @param user
     * @param msg
     */
    protected void sendTo ( boolean ok, User user, String[] msg )  {
        this.service.send ( 
            Service.RAW, 
            ":"+this.service.getName()+
                    " PRIVMSG "+this.chan+" :"+this.fixArray ( ok, user, msg )
        );
    }

    /**
     *
     * @param ok
     * @param user
     * @param msg
     * @param error
     */
    protected void sendTo ( boolean ok, User user, String[] msg, String error )  {
        this.service.send ( 
            Service.RAW, 
            ":"+this.service.getName()+" PRIVMSG "+this.chan+" :"+this.fixArray ( ok, user, msg )+" ["+error+"]"
        );
    }

    /**
     *
     * @param ok
     * @param target
     * @param user
     * @param message
     */
    public void log ( boolean ok, HashString target, User user, String[] message )  {
        logs.add ( new SnoopLog ( target, this.fixArray ( ok, user, message ) ) );
    }
}
