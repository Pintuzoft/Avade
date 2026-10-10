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
package core;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author DreamHealer
 */
public class Log extends HashNumeric {
    private FileWriter              fStream;
    private BufferedWriter          out;
    private SimpleDateFormat        dateFormat;
    private Date                    date;
    private String                  buf;
    
    /**
     *
     */
    public Log ( )  {
        this.dateFormat     = new SimpleDateFormat ( "yyyy MM/dd HH:mm:ss zzz" );
        this.connect ( );
    }
    
    private void connect ( )  {
        try { 
            this.fStream    = new FileWriter ( Proc.getConf().get(LOGFILE).getString(), true );
            this.out        = new BufferedWriter ( this.fStream );
        } catch  ( IOException ex )  {
            Logger.getLogger ( Log.class.getName ( )  ) .log ( Level.SEVERE, null, ex );
        }
    }
    private void reConnect ( )  {
        try {
            if ( this.out != null ) {
                this.out.close ( );
            }
            this.fStream    = new FileWriter ( Proc.getConf().get(LOGFILE).getString(), true );
            this.out        = new BufferedWriter ( this.fStream );
        } catch  ( IOException ex )  {
            Logger.getLogger ( Log.class.getName ( )  ) .log ( Level.SEVERE, null, ex );
        }
    }
    
    /* One line to the log file, written at once so the log can be followed
       and nothing is lost when services stop */
    private boolean write ( )  {
        if ( this.out == null ) {
            return false; /* the log file could not be opened */
        }
        try {
            this.out.write ( this.buf );
            this.out.newLine ( );
            this.out.flush ( );
            return true;
        } catch  ( IOException ex )  {
            return false;
        }
    }

    /**
     *
     * @param e
     */
    public void out ( Exception e )  {
        this.out ( "Error: "+e.getMessage ( ) );
    }

    /**
     *
     * @param message
     */
    public void out ( String message )  {
        this.buf = " ( "+this.getTime ( ) +" ) : "+message;
        if ( ! this.write ( ) ) {
            this.reConnect ( );
            this.write ( );
        }
        System.out.println ( this.buf );
    }
    
    private String getTime ( )  {
        this.date = new Date ( );
        return dateFormat.format ( this.date );
    }
    
    /**
     *
     */
    public void close ( )  {
        try {
            this.out.close ( );
            this.fStream.close ( );
        } catch  ( IOException ex )  { 
            Proc.log ( Log.class.getName ( ) , ex );
        }
    }
  
}
