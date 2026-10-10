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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import setup.Ask;

/**
 * "java -jar mailer.jar setup": asks for the database and the mail server,
 * sends a test mail and writes mailer.conf from mailer-template.conf. Run by
 * "./avade.sh mailer start" when there is no mailer.conf yet.
 *
 * @author DreamHealer
 */
public class Setup {
    private static final String     CONF        = "mailer.conf";
    private static final String     TEMPLATE    = "mailer-template.conf";
    private static final String     SERVICES    = "services.conf";

    private Ask                     ask         = new Ask ( );
    private Map<String,Object>      conf        = new LinkedHashMap<> ( );
    private String                  domain      = "example.net";

    /**
     * @return 0 when there is a mailer.conf to start with, 1 otherwise
     */
    public int run ( ) {
        out ( "" );
        out ( "AvadeMailer - setup" );
        out ( "" );
        if ( Files.exists ( Paths.get ( CONF ) ) ) {
            out ( "mailer.conf is already here, it is left as it is. Every setting is explained in" );
            out ( "mailer-reference.conf." );
            return 0;
        }
        if ( ! Files.exists ( Paths.get ( TEMPLATE ) ) ) {
            out ( TEMPLATE+" is missing in this directory. Run ./make.sh install from the Avade source first." );
            return 1;
        }
        try {
            out ( "The mailer sends the mails that services put in the database. There is no mailer.conf" );
            out ( "yet. A few questions, and it is written for you. Enter takes the answer in [brackets]." );
            this.database ( );
            this.mailServer ( );
            this.write ( );
            return 0;

        } catch ( Ask.Cancelled ex ) {
            out ( "" );
            out ( "Cancelled, nothing was written." );
            return 1;
        } catch ( IOException ex ) {
            out ( "" );
            out ( "Could not write the file: "+ex.getMessage ( ) );
            return 1;
        }
    }

    /**
     * "java -jar mailer.jar test <address>": one mail through the mail server of mailer.conf
     * @param config
     * @param to
     * @return 0 when the mail server took it
     */
    public static int test ( MailerConfig config, String to ) {
        String problem = sendTest ( config, to );
        out ( problem == null ? "ok: the mail server took the mail for "+to+". Check that it arrives." : problem );
        return problem == null ? 0 : 1;
    }

    /* 1/2 */
    private void database ( ) {
        out ( "" );
        out ( "1/2  The database of services" );
        Map<String,Object> services = this.servicesConf ( );
        boolean same = services != null
                    && this.ask.yes ( "services.conf is here. Use the same database login as services?", true );
        while ( true ) {
            if ( same ) {
                for ( String key : new String[] { "mysqlhost", "mysqlport", "mysqluser", "mysqlpass", "mysqldb" } ) {
                    this.conf.put ( key, String.valueOf ( services.get ( key ) ) );
                }
            } else {
                this.conf.put ( "mysqlhost", this.ask.line ( "Host of the database", str ( "mysqlhost", "localhost" ),
                    v -> v.matches ( "[A-Za-z0-9.:-]+" ) ? null : "An ip address or a host name." ) );
                this.conf.put ( "mysqlport", this.ask.line ( "Port", str ( "mysqlport", "3306" ), Setup::port ) );
                this.conf.put ( "mysqldb", this.ask.line ( "Name of the database", str ( "mysqldb", "avade" ),
                    v -> v.matches ( "[A-Za-z0-9_]+" ) ? null : "Letters, digits and _ only." ) );
                this.conf.put ( "mysqluser", this.ask.line ( "User that the mailer logs in with", str ( "mysqluser", "mailer" ),
                    v -> v.matches ( "[A-Za-z0-9_]+" ) ? null : "Letters, digits and _ only." ) );
                this.conf.put ( "mysqlpass", this.ask.secret ( "Password of that user",
                    v -> v.matches ( "[^\\s'\"\\\\]+" ) ? null : "No spaces, quotes or backslashes." ) );
            }
            while ( true ) {
                String problem = this.checkDatabase ( );
                if ( problem == null ) {
                    out ( "  ok: the mailer can read the mailbox." );
                    return;
                }
                out ( "" );
                out ( "  "+problem );
                char c = this.ask.choice ( "Enter tries again, c changes the answers, s skips the check", "tcs" );
                if ( c == 's' ) {
                    out ( "  Skipped. The mailer keeps trying when it runs." );
                    return;
                } else if ( c == 'c' ) {
                    same = false;
                    break;
                }
            }
        }
    }

    /* 2/2 */
    private void mailServer ( ) {
        out ( "" );
        out ( "2/2  The mail server (SMTP)" );
        out ( "  Amazon SES: email-smtp.<region>.amazonaws.com, port 587, with the SMTP credentials from the" );
        out ( "  SES console. A mail server on this machine (postfix, sendmail): localhost." );
        while ( true ) {
            String host = this.ask.line ( "Mail server", str ( "smtphost", null ),
                v -> v.matches ( "[A-Za-z0-9.:-]+" ) ? null : "An ip address or a host name." );
            boolean local = host.equals ( "localhost" ) || host.equals ( "127.0.0.1" ) || host.equals ( "::1" );
            this.conf.put ( "smtphost", host );
            this.conf.put ( "smtpport", this.ask.line ( "Port", local ? "25" : "587", Setup::port ) );
            this.conf.put ( "smtptls", this.ask.yes ( "Encrypt with STARTTLS (always, unless the server is on this machine)?", ! local ) );
            boolean auth = this.ask.yes ( "Does the server want a login?", ! local );
            this.conf.put ( "smtpauth", auth );
            this.conf.put ( "smtpuser", auth ? this.ask.line ( "User", str ( "smtpuser", null ), null ) : "" );
            this.conf.put ( "smtppass", auth ? this.ask.line ( "Password", null, null ) : "" );
            this.conf.put ( "smtpfrom", this.ask.line ( "Sender address of the mails", str ( "smtpfrom", "noreply@"+this.domain ),
                v -> v.matches ( "[^\\s@]+@[^\\s@]+\\.[A-Za-z]{2,63}" ) ? null : "A mail address, like noreply@example.net." ) );

            String to = this.ask.line ( "Send a test mail now? Your own address (Enter skips)", "",
                v -> v.matches ( "[^\\s@]+@[^\\s@]+\\.[A-Za-z]{2,63}" ) ? null : "A mail address, or just Enter." );
            if ( to.isEmpty ( ) ) {
                break;
            }
            boolean again = false;
            while ( true ) {
                out ( "  Sending ..." );
                this.conf.put ( "send", true );
                String problem = sendTest ( new MailerConfig ( this.conf ), to );
                if ( problem == null ) {
                    out ( "  ok: the mail server took the mail for "+to+". Check that it arrives." );
                    break;
                }
                out ( "" );
                out ( "  "+problem );
                char c = this.ask.choice ( "Enter tries again, c changes the answers, s skips the test", "tcs" );
                if ( c == 'c' ) {
                    again = true;
                }
                if ( c != 't' ) {
                    break;
                }
            }
            if ( ! again ) {
                break;
            }
        }
        out ( "" );
        out ( "  On a test network with a copy of real data, answer no here: the addresses belong to" );
        out ( "  real people. The mails are then only marked as not sent." );
        this.conf.put ( "send", this.ask.yes ( "Send the mails of services for real?", true ) );
    }

    private void write ( ) throws IOException {
        String text = new String ( Files.readAllBytes ( Paths.get ( TEMPLATE ) ), StandardCharsets.UTF_8 );
        for ( Map.Entry<String,Object> e : this.conf.entrySet ( ) ) {
            Matcher m = Pattern.compile ( "(?m)^"+e.getKey ( )+":.*$" ).matcher ( text );
            if ( ! m.find ( ) ) {
                throw new IOException ( TEMPLATE+" has no setting "+e.getKey ( ) );
            }
            text = m.replaceFirst ( Matcher.quoteReplacement ( e.getKey ( )+": "+yaml ( e.getValue ( ) ) ) );
        }
        /* What the mailer will read must be what was answered */
        Map<String,Object> check = load ( text );
        for ( Map.Entry<String,Object> e : this.conf.entrySet ( ) ) {
            Object got = check.get ( e.getKey ( ) );
            if ( ! String.valueOf ( e.getValue ( ) ).equals ( got == null ? "" : String.valueOf ( got ) ) ) {
                throw new IOException ( "the setting "+e.getKey ( )+" can not be written as it is" );
            }
        }
        Path path = Paths.get ( CONF );
        try {
            Files.createFile ( path, PosixFilePermissions.asFileAttribute ( PosixFilePermissions.fromString ( "rw-------" ) ) );
        } catch ( UnsupportedOperationException ex ) {
            Files.createFile ( path );
        }
        Files.write ( path, text.getBytes ( StandardCharsets.UTF_8 ) );

        out ( "" );
        out ( "Written: "+path.toAbsolutePath ( )+" (only you can read it)" );
        out ( "" );
        if ( Boolean.TRUE.equals ( this.conf.get ( "send" ) ) ) {
            out ( "The mailer sends what services put in the mailbox." );
        } else {
            out ( "send: false - nothing is sent. Change it in mailer.conf to send for real." );
        }
        out ( "  ./avade.sh mailer queue           mails per status" );
        out ( "  ./avade.sh mailer log             what it does" );
        out ( "  ./avade.sh mailer test <address>  send a test mail" );
        out ( "Every setting is explained in mailer-reference.conf, more under \"Mail\" in INSTALL." );
        out ( "" );
    }

    /* null when the mailer can read the mailbox, otherwise what to do about it */
    private String checkDatabase ( ) {
        String host = String.valueOf ( this.conf.get ( "mysqlhost" ) );
        String url  = "jdbc:mysql://"+host+":"+this.conf.get ( "mysqlport" )+"/"+this.conf.get ( "mysqldb" )
                    + "?connectTimeout=5000&socketTimeout=10000";
        try ( Connection sql = DriverManager.getConnection ( url, String.valueOf ( this.conf.get ( "mysqluser" ) ),
                                                                  String.valueOf ( this.conf.get ( "mysqlpass" ) ) ) ) {
            try {
                sql.createStatement().executeQuery ( "select count(*) from mailbox" ).close ( );
                return null;
            } catch ( SQLException ex ) {
                return "Logged in, but the mailbox table can not be read ("+ex.getMessage ( )+").\n"
                     + "  If services have never been started, start them once first: they make the tables.";
            }
        } catch ( SQLException ex ) {
            String state = ex.getSQLState ( ) == null ? "" : ex.getSQLState ( );
            if ( state.startsWith ( "08" ) ) {
                return "No database answers on "+host+" port "+this.conf.get ( "mysqlport" )+". Is it running, and is that the right host and port?";
            }
            boolean local   = host.equals ( "localhost" ) || host.equals ( "127.0.0.1" ) || host.equals ( "::1" );
            String who      = "'"+this.conf.get ( "mysqluser" )+"'@'"+( local ? "localhost" : "THE-ADDRESS-OF-THIS-MACHINE" )+"'";
            return "The database is there, but the mailer can not log in ("+ex.getMessage ( )+").\n"
                 + "  For a user that can only use the mailbox, run this as the root user of the database:\n\n"
                 + "      create user if not exists "+who+" identified by '"+this.conf.get ( "mysqlpass" )+"';\n"
                 + "      alter user "+who+" identified by '"+this.conf.get ( "mysqlpass" )+"';\n"
                 + "      grant select, update, delete on "+this.conf.get ( "mysqldb" )+".mailbox to "+who+";\n";
        }
    }

    /* null when the mail server took the mail, otherwise what went wrong */
    private static String sendTest ( MailerConfig config, String to ) {
        Smtp smtp = new Smtp ( config );
        try {
            smtp.send ( new MailBox.Mail ( 0, to, "Test mail from AvadeMailer",
                "This is a test mail from AvadeMailer.\n\nIf you can read it, the mail server settings work." ) );
            return null;
        } catch ( Smtp.MailRefused ex ) {
            return "The mail server refused the mail: "+ex.getMessage ( )+".\n"
                 + "  With Amazon SES that is often a sender address that is not verified, or an account still\n"
                 + "  in the sandbox, which only sends to verified addresses.";
        } catch ( MessagingException ex ) {
            return "The mail could not be handed to the mail server: "+ex.getMessage ( )+"\n"
                 + "  Check the server name and port, the login, and that the certificate is valid for the\n"
                 + "  server name when STARTTLS is on.";
        } finally {
            smtp.close ( );
        }
    }

    /* The settings of services.conf when it is in this directory, for the database login */
    private Map<String,Object> servicesConf ( ) {
        try {
            Map<String,Object> services = load ( new String ( Files.readAllBytes ( Paths.get ( SERVICES ) ), StandardCharsets.UTF_8 ) );
            if ( services.get ( "domain" ) != null ) {
                this.domain = String.valueOf ( services.get ( "domain" ) );
            }
            return services.get ( "mysqlhost" ) != null && services.get ( "mysqluser" ) != null ? services : null;
        } catch ( IOException | RuntimeException ex ) {
            return null;
        }
    }

    private String str ( String key, String def ) {
        Object value = this.conf.get ( key );
        return value == null || value.toString().isEmpty ( ) ? def : value.toString ( );
    }

    @SuppressWarnings ( "unchecked" )
    private static Map<String,Object> load ( String text ) {
        Object obj = new Yaml ( new SafeConstructor ( new LoaderOptions ( ) ) ).load ( text );
        if ( obj instanceof Map<?,?> ) {
            return (Map<String,Object>) obj;
        }
        throw new IllegalStateException ( "no settings in it" );
    }

    /* A value as YAML reads it back unchanged: quoted unless it is a plain word */
    private static String yaml ( Object value ) {
        String text = String.valueOf ( value );
        if ( value instanceof Boolean || text.matches ( "[1-9][0-9]{0,4}" ) ) {
            return text;
        }
        boolean word = text.matches ( "[A-Za-z][A-Za-z0-9._-]*" )
                    && ! text.matches ( "(?i)true|false|yes|no|on|off|null|y|n" );
        return word ? text : "'"+text.replace ( "'", "''" )+"'";
    }

    private static String port ( String value ) {
        return value.matches ( "[1-9][0-9]{0,4}" ) && Integer.parseInt ( value ) < 65536 ? null : "A port is a number from 1 to 65535.";
    }

    private static void out ( String line ) {
        System.out.println ( line );
    }
}
