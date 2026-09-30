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

import core.Handler;
import core.Proc;
import core.HashNumeric;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author DreamHealer
 */
public class ServSock extends HashNumeric {

    private Socket sock;
    private static PrintWriter out;
    private InputStream in;
    private final ByteArrayOutputStream lineBuf = new ByteArrayOutputStream ( 512 );
    private BufferedReader stdIn;
    private String buf;
    private long last;
    private static long pingTime;
    private static long lastPing;
    private static long defaultPing = 120000;

    /**
     *
     */
    public ServSock() {
        last = System.currentTimeMillis();
        lastPing = this.last;
        try {
            this.sock = new Socket(Proc.getConf().get(HUBHOST).getString(), Integer.parseInt(Proc.getConf().get(HUBPORT).getString()));
            this.sock.setKeepAlive(true);
            this.sock.setSoTimeout(200);
            out = new PrintWriter(new OutputStreamWriter(this.sock.getOutputStream(), StandardCharsets.UTF_8), true);
            this.in = new BufferedInputStream(this.sock.getInputStream());

        } catch (UnknownHostException e) {
            System.out.println("Don't know about host: " + Proc.getConf().get(HUBNAME));
            System.exit(1);

        } catch (IOException e) {
            System.out.println("Couldn't get I/O for the connection to: " + Proc.getConf().get(HUBNAME));
            System.exit(1);
        }

        // this.stdIn = new BufferedReader ( new InputStreamReader ( System.in )  );
        this.authenticate();
    }

    /**
     *
     * @return
     */
    public boolean isConnected() {
        return this.sock.isConnected();
    }

    /**
     *
     * @return
     */
    public String readLine() {
        try {
            int b;
            while ((b = this.in.read()) != -1) {
                if (b == '\n') {
                    this.buf = decode(this.lineBuf.toByteArray());
                    this.lineBuf.reset();
                    if (this.buf.length() > 0) {
                        this.last = System.currentTimeMillis();
                    }
                    return this.buf;
                } else if (b != '\r') {
                    this.lineBuf.write(b);
                }
            }
        } catch (SocketTimeoutException ex) {
            /* Nothing more to read right now, keep any partial line for the next call */
        } catch (IOException ex) {
            // Logger.getLogger(ServSock.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * IRC has no fixed charset. Decode lines as UTF-8 and fall back to
     * Windows-1252 (what old latin1 clients send) if it is not valid UTF-8.
     * @param bytes
     * @return
     */
    public static String decode(byte[] bytes) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        } catch (CharacterCodingException ex) {
            return new String(bytes, Charset.forName("windows-1252"));
        }
    }

    /**
     *
     */
    public void disconnect() {
        try {
            this.sock.close();
            out.close();
            this.in.close();
        } catch (IOException e) {
            Proc.log(ServSock.class.getName(), e);
        }
    }

    /**
     *
     * @param cmd
     */
    public static void sendCmd(String cmd) {
        try {
            if (!cmd.contains("PONG")) {
                //System.out.println ( "Sending: "+cmd );
            }
            out.println (cmd);
        } catch (Exception e) {
            Proc.log(ServSock.class.getName(), e);
        }
    }

    /**
     *
     */
    public void authenticate() {
        Handler.resetSync();
        sendCmd("PASS " + Proc.getConf().get(HUBPASS) + " :TS..");
        /* NICKIPSTR: get the IP of users as a string, needed for IPv6 */
        sendCmd("CAPAB NICKIPSTR");
        sendCmd("SERVER " + Proc.getConf().get(NAME) + " 1 :services");
        sendCmd("SERVER " + Proc.getConf().get(STATS) + " 1 :stats");
    }

    /**
     *
     * @return
     */
    public boolean timedOut() {
        return System.currentTimeMillis() - this.last > defaultPing;
    }
}
