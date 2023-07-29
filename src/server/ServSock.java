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

import core.Proc;
import core.HashNumeric;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 *
 * @author DreamHealer
 */
public class ServSock extends HashNumeric {
    private Socket                      sock;
    private static PrintWriter          out;
    private BufferedReader              in;
    private BufferedReader              stdIn;
    private String                      buf;
    private long                        last;
    private static long                 pingTime;
    private static long                 lastPing;
    private static long                 defaultPing = 120000;
    
    /**
     *
     */
    public ServSock ( )  {
        last = System.currentTimeMillis();
        lastPing = this.last;
        try {
            this.sock = new Socket ( Proc.getConf().get(HUBHOST).getString(), Integer.parseInt ( Proc.getConf().get(HUBPORT).getString() ) );
            this.sock.setKeepAlive ( true );
            this.sock.setSoTimeout ( 200 );
            out = new PrintWriter ( this.sock.getOutputStream ( ) , true );
            this.in = new BufferedReader ( new InputStreamReader ( this.sock.getInputStream ( ) ) );

        } catch  ( UnknownHostException e )  {
            System.out.println ( "Don't know about host: "+Proc.getConf().get ( HUBNAME ) );
            System.exit ( 1 );

        } catch  ( IOException e )  {
            System.out.println ( "Couldn't get I/O for the connection to: "+Proc.getConf().get ( HUBNAME )  );
            System.exit ( 1 );
        }

       // this.stdIn = new BufferedReader ( new InputStreamReader ( System.in )  );

        this.authenticate ( );
    }

    /**
     *
     * @return
     */
    public boolean isConnected ( )  { 
        return this.sock.isConnected ( );
    }

    /**
     *
     * @return
     */
    public String readLine ( )  {
        try { 
            this.buf = this.in.readLine ( );
            if ( this.buf != null && this.buf.length ( ) > 0 ) {
                this.last = System.currentTimeMillis();
            }
            return this.buf;
            
        } catch (IOException ex) {
           // Logger.getLogger(ServSock.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return null;
    }

    /**
     *
     */
    public void disconnect ( )  {
        try {
            this.sock.close ( );
            out.close ( );
            this.in.close ( );
        } catch  ( IOException e )  {
            Proc.log ( ServSock.class.getName ( ) , e );
        }
    }

    /**
     *
     * @param cmd
     */
    public static void sendCmd ( String cmd )  {
        try {   
            if ( ! cmd.contains ( "PONG" )  )  {
                //System.out.println ( "Sending: "+cmd );
            } 
            out.println ( cmd ); 
        } catch ( Exception e )  {
            Proc.log ( ServSock.class.getName ( ) , e ); 
        }
    }

    /**
     *
     */
    public void authenticate ( )  {
        sendCmd ( "PASS "+Proc.getConf().get ( HUBPASS ) +" :TS.."          );
        sendCmd ( "SERVER "+Proc.getConf().get ( NAME ) +" 1 :services"     );
        sendCmd ( "SERVER "+Proc.getConf().get ( STATS ) +" 1 :stats"       );
    }

    /**
     *
     * @return
     */
    public boolean timedOut ( ) {
        return System.currentTimeMillis() - this.last > defaultPing;
    }
}
