/*
 * Copyright (C) 2026 Fredrik Karlsson aka DreamHealer & avade.net
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
package mailer;

import jakarta.mail.MessagingException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AvadeMailer: sends the mails Avade puts in the mailbox table. A program of
 * its own, so services never wait for a mail server and the mailer can be
 * restarted on its own.
 *
 *   java -jar mailer.jar [-c mailer.conf] [run]     send until stopped
 *   java -jar mailer.jar [-c mailer.conf] once      one round, then exit
 *   java -jar mailer.jar [-c mailer.conf] status    mails per status
 *   java -jar mailer.jar [-c mailer.conf] resend <id|failed>
 *   java -jar mailer.jar [-c mailer.conf] test <address>    send a test mail
 *   java -jar mailer.jar setup                      ask what is needed and write mailer.conf
 *
 * @author DreamHealer
 */
public class Main {
    private static final int        ROUND       = 100;      /* mails per round at most */
    private static final int        CLEANEVERY  = 3600;     /* seconds between cleanups */
    private static final int        KEEPDAYS    = 14;       /* days finished mails are kept */
    private static volatile boolean running     = true;

    private MailerConfig            config;
    private MailBox                 box;
    private Smtp                    smtp;
    private Map<Integer,Integer>    tries       = new HashMap<> ( );    /* id -> times the server said later */
    private long                    lastClean;

    /**
     * @param args
     */
    public static void main ( String[] args ) {
        String file = "mailer.conf";
        int i = 0;
        if ( args.length > 1 && args[0].equals ( "-c" ) ) {
            file = args[1];
            i = 2;
        }
        String command = args.length > i ? args[i] : "run";
        String arg = args.length > i + 1 ? args[i + 1] : null;

        if ( command.equals ( "setup" ) ) {
            System.exit ( new Setup ( ).run ( ) );
        }
        MailerConfig config;
        try {
            config = new MailerConfig ( file );
        } catch ( IllegalStateException ex ) {
            System.out.println ( "Config error: "+ex.getMessage ( ) );
            System.exit ( 1 );
            return;
        }
        Main mailer = new Main ( config );
        switch ( command ) {
            case "run" :        mailer.run ( );                 break;
            case "once" :       mailer.once ( );                break;
            case "status" :     mailer.status ( );              break;
            case "resend" :     mailer.resend ( arg );          break;
            case "test" :
                if ( arg == null ) {
                    System.out.println ( "Syntax: test <address>" );
                    System.exit ( 1 );
                }
                System.exit ( Setup.test ( config, arg ) );
                break;
            default :
                System.out.println ( "Syntax: java -jar mailer.jar [-c mailer.conf] [run|once|status|resend <id|failed>|test <address>|setup]" );
                System.exit ( 1 );
        }
    }

    private Main ( MailerConfig config ) {
        this.config = config;
        this.box    = new MailBox ( config );
        this.smtp   = new Smtp ( config );
    }

    /* Until stopped: a stop (SIGTERM) lets the mail being sent finish */
    private void run ( ) {
        Log.open ( this.config.logFile ( ) );
        Log.msg ( "AvadeMailer started, "+( this.config.send ( ) ? "sending mail through "+this.config.smtpHost ( )
                                                                   : "send: false, no mail is sent" ) );
        Thread main = Thread.currentThread ( );
        Runtime.getRuntime().addShutdownHook ( new Thread ( ( ) -> {
            running = false;
            main.interrupt ( );
            try {
                main.join ( 30000 );
            } catch ( InterruptedException ex ) {
                /* stopping anyway */
            }
        } ) );

        boolean started = false;
        while ( running ) {
            if ( this.box.connect ( ) ) {
                try {
                    if ( ! started ) {
                        int n = this.box.interrupted ( );
                        if ( n > 0 ) {
                            Log.msg ( n+" mails were being sent when the mailer stopped. They may have gone out, and "+
                                      "are marked failed (status 500). Send them again with: resend failed" );
                        }
                        started = true;
                    }
                    this.round ( );
                    this.clean ( );
                } catch ( SQLException ex ) {
                    Log.msg ( "Database error: "+ex.getMessage ( ) );
                    this.box.close ( );
                }
            }
            this.sleep ( this.config.interval ( ) * 1000L );
        }
        this.smtp.close ( );
        this.box.close ( );
        Log.msg ( "AvadeMailer stopped" );
    }

    private void once ( ) {
        Log.open ( this.config.logFile ( ) );
        if ( ! this.box.connect ( ) ) {
            System.exit ( 1 );
        }
        try {
            this.round ( );
        } catch ( SQLException ex ) {
            Log.msg ( "Database error: "+ex.getMessage ( ) );
            System.exit ( 1 );
        }
        this.smtp.close ( );
        this.box.close ( );
    }

    /* Send what is waiting, at most ROUND mails */
    private void round ( ) throws SQLException {
        List<MailBox.Mail> mails = this.box.waiting ( ROUND );
        try {
            for ( MailBox.Mail mail : mails ) {
                if ( ! running ) {
                    return;
                }
                if ( ! this.box.take ( mail.id ) ) {
                    continue;
                }
                if ( ! this.config.send ( ) ) {
                    this.box.set ( mail.id, MailBox.NOT_SENT );
                    Log.msg ( "Not sent (send: false): mail "+mail.id+" \""+mail.subject+"\"" );
                    continue;
                }
                try {
                    this.smtp.send ( mail );
                    this.box.set ( mail.id, MailBox.SENT );
                    this.tries.remove ( mail.id );
                    Log.msg ( "Sent: mail "+mail.id+" \""+mail.subject+"\"" );

                } catch ( Smtp.MailRefused ex ) {
                    int n = this.tries.merge ( mail.id, 1, Integer::sum );
                    if ( ex.permanent || n >= this.config.retries ( ) ) {
                        this.box.set ( mail.id, MailBox.FAILED );
                        this.tries.remove ( mail.id );
                        Log.msg ( "Failed: mail "+mail.id+", "+ex.getMessage ( )+( ex.permanent ? "" : ", tried "+n+" times" ) );
                    } else {
                        this.box.set ( mail.id, MailBox.SEND );
                        Log.msg ( "Later: mail "+mail.id+", "+ex.getMessage ( )+", try "+n+" of "+this.config.retries ( ) );
                    }

                } catch ( MessagingException ex ) {
                    /* Nothing wrong with the mail: wait for the server, try again next round */
                    this.box.set ( mail.id, MailBox.SEND );
                    this.smtp.close ( );
                    Log.msg ( "Mail server problem, trying again later: "+ex.getMessage ( ) );
                    return;
                }
                this.sleep ( 1000L / this.config.rate ( ) );
            }
        } finally {
            this.smtp.close ( );
        }
    }

    private void clean ( ) throws SQLException {
        long now = System.currentTimeMillis ( );
        if ( now - this.lastClean < CLEANEVERY * 1000L ) {
            return;
        }
        this.lastClean = now;
        int n = this.box.clean ( KEEPDAYS );
        if ( n > 0 ) {
            Log.msg ( "Removed "+n+" mails older than "+KEEPDAYS+" days from the mailbox" );
        }
    }

    private void status ( ) {
        if ( ! this.box.connect ( ) ) {
            System.exit ( 1 );
        }
        try {
            Map<Integer,Integer> counts = this.box.count ( );
            String[] names = { "sent", "to send", "being sent", "not sent (send: false)" };
            for ( Map.Entry<Integer,Integer> e : counts.entrySet ( ) ) {
                int s = e.getKey ( );
                String name = s == MailBox.FAILED ? "failed" : ( s >= 0 && s < names.length ? names[s] : "status "+s );
                System.out.println ( String.format ( "%6d  %s", e.getValue ( ), name ) );
            }
        } catch ( SQLException ex ) {
            System.out.println ( "Database error: "+ex.getMessage ( ) );
            System.exit ( 1 );
        }
        this.box.close ( );
    }

    private void resend ( String what ) {
        if ( what == null || ! ( what.equals ( "failed" ) || what.matches ( "[0-9]+" ) ) ) {
            System.out.println ( "Syntax: resend <id|failed>" );
            System.exit ( 1 );
        }
        if ( ! this.box.connect ( ) ) {
            System.exit ( 1 );
        }
        try {
            int n = what.equals ( "failed" ) ? this.box.resendFailed ( ) : this.box.resend ( Integer.parseInt ( what ) );
            System.out.println ( n+" mails will be sent again" );
        } catch ( SQLException ex ) {
            System.out.println ( "Database error: "+ex.getMessage ( ) );
            System.exit ( 1 );
        }
        this.box.close ( );
    }

    private void sleep ( long ms ) {
        try {
            Thread.sleep ( ms );
        } catch ( InterruptedException ex ) {
            /* stopping */
        }
    }
}
