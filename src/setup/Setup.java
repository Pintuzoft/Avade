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
package setup;

import core.Version;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

/**
 * "java -jar avade.jar setup": asks what is needed and writes services.conf
 * from template.conf. Run by "./avade.sh start" when there is no
 * services.conf yet. When there is one it is left as it is, and only the
 * commands a newer version added are offered for the access lists.
 *
 * @author DreamHealer
 */
public class Setup {
    private static final String     CONF        = "services.conf";
    private static final String     TEMPLATE    = "template.conf";
    private static final String     HUBFILE     = "hub-setup.txt";
    private static final String[]   LISTS       = { "sra", "csop", "sa", "ircop" };

    private Ask                     ask         = new Ask ( );
    private Map<String,String>      conf        = new LinkedHashMap<> ( );   /* setting -> value */

    /**
     * @return 0 when there is a services.conf to start with, 1 otherwise
     */
    public int run ( ) {
        out ( "" );
        out ( new Version().getVersion ( )+" - setup" );
        out ( "" );
        try {
            if ( Files.exists ( Paths.get ( CONF ) ) ) {
                return this.existing ( );
            }
            if ( ! Files.exists ( Paths.get ( TEMPLATE ) ) ) {
                out ( TEMPLATE+" is missing in this directory. Run ./make.sh install from the Avade source first." );
                return 1;
            }
            out ( "There is no services.conf yet. A few questions, and it is written for you." );
            out ( "Enter takes the answer in [brackets]. Nothing is written before the end, ctrl-c cancels." );
            this.network ( );
            this.hub ( );
            this.database ( );
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

    /* 1/3 */
    private void network ( ) {
        out ( "" );
        out ( "1/3  Your network" );
        this.conf.put ( "netname", this.ask.line ( "Name of the network, shown in mails and messages", "ExampleNET",
            v -> v.matches ( "\\S+" ) ? null : "One word, no spaces." ) );
        String domain = this.ask.line ( "Domain of the network, for example example.net", null,
            v -> v.matches ( "[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+" ) ? null : "A domain looks like example.net." ).toLowerCase ( );
        this.conf.put ( "domain", domain );
        this.conf.put ( "master", this.ask.line ( "Your nick on IRC. It becomes the services master, the highest access", null,
            v -> v.matches ( "[A-Za-z\\[\\]\\\\`_^{|}][A-Za-z0-9\\[\\]\\\\`_^{|}-]{0,29}" ) ? null : "That is not a nick an ircd would take." ) );
        this.conf.put ( "name",         "services."+domain );
        this.conf.put ( "stats",        "stats."+domain );
        this.conf.put ( "servicehost",  domain );
        this.conf.put ( "authurl",      "https://"+domain+"/auth/?code=" );
    }

    /* 2/3 */
    private void hub ( ) {
        out ( "" );
        out ( "2/3  The hub: the bahamut server that services link to" );
        this.conf.put ( "hubname", this.ask.line ( "Name of the hub server", "hub."+this.conf.get ( "domain" ),
            v -> v.matches ( "[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+" ) ? null : "A server name has a dot in it, like hub.example.net." ) );
        this.conf.put ( "hubhost", this.ask.line ( "Address of the hub (services on the same machine is best)", "127.0.0.1",
            v -> v.matches ( "[A-Za-z0-9.:-]+" ) ? null : "An ip address or a host name." ) );
        this.conf.put ( "hubport", this.ask.line ( "Port on the hub for services", "7015", Setup::port ) );
        this.conf.put ( "hubpass", this.ask.secret ( "Password of the link",
            v -> v.matches ( "[A-Za-z0-9._+=@%^~-]+" ) ? null : "Letters, digits and . _ + = @ % ^ ~ - only, it goes in ircd.conf too." ) );
        /* The hub is not tried from here: bahamut counts every connection, and
           a few in a row get the address of services refused for minutes */
        out ( "  What the hub needs in its ircd.conf is shown at the end." );
    }

    /* 3/3 */
    private void database ( ) {
        out ( "" );
        out ( "3/3  The database (MariaDB or MySQL)" );
        while ( true ) {
            this.conf.put ( "mysqlhost", this.ask.line ( "Host of the database", this.conf.getOrDefault ( "mysqlhost", "localhost" ),
                v -> v.matches ( "[A-Za-z0-9.:-]+" ) ? null : "An ip address or a host name." ) );
            this.conf.put ( "mysqlport", this.ask.line ( "Port", this.conf.getOrDefault ( "mysqlport", "3306" ), Setup::port ) );
            this.conf.put ( "mysqldb", this.ask.line ( "Name of the database", this.conf.getOrDefault ( "mysqldb", "avade" ),
                v -> v.matches ( "[A-Za-z0-9_]+" ) ? null : "Letters, digits and _ only." ) );
            this.conf.put ( "mysqluser", this.ask.line ( "User that services log in with", this.conf.getOrDefault ( "mysqluser", "services" ),
                v -> v.matches ( "[A-Za-z0-9_]+" ) ? null : "Letters, digits and _ only." ) );
            this.conf.put ( "mysqlpass", this.ask.secret ( "Password of that user",
                v -> v.matches ( "[^\\s'\"\\\\]+" ) ? null : "No spaces, quotes or backslashes." ) );

            while ( true ) {
                String problem = this.checkDatabase ( );
                if ( problem == null ) {
                    out ( "  ok: logged in to the database "+this.conf.get ( "mysqldb" )+"." );
                    return;
                }
                out ( "" );
                out ( "  "+problem );
                char c = this.ask.choice ( "Enter tries again, c changes the answers, s skips the check", "tcs" );
                if ( c == 's' ) {
                    out ( "  Skipped. Services wait for the database when they start." );
                    return;
                } else if ( c == 'c' ) {
                    break;
                }
            }
        }
    }

    /* null when services can log in, otherwise what to do about it */
    private String checkDatabase ( ) {
        String host = this.conf.get ( "mysqlhost" );
        String url  = "jdbc:mysql://"+host+":"+this.conf.get ( "mysqlport" )+"/"+this.conf.get ( "mysqldb" )
                    + "?connectTimeout=5000&socketTimeout=10000";
        try ( Connection sql = DriverManager.getConnection ( url, this.conf.get ( "mysqluser" ), this.conf.get ( "mysqlpass" ) ) ) {
            return null;
        } catch ( SQLException ex ) {
            String state = ex.getSQLState ( ) == null ? "" : ex.getSQLState ( );
            if ( state.startsWith ( "08" ) ) {
                return "No database answers on "+host+" port "+this.conf.get ( "mysqlport" )+". Is MariaDB running, and is that the right host and port?";
            }
            boolean local   = host.equals ( "localhost" ) || host.equals ( "127.0.0.1" ) || host.equals ( "::1" );
            String from     = local ? "localhost" : "THE-ADDRESS-OF-THIS-MACHINE";
            String who      = "'"+this.conf.get ( "mysqluser" )+"'@'"+from+"'";
            return "The database is there, but services can not log in yet ("+ex.getMessage ( )+").\n"
                 + "  Run this as the root user of the database (sudo mysql, or mysql -u root -p):\n\n"
                 + "      create database if not exists "+this.conf.get ( "mysqldb" )+";\n"
                 + "      create user if not exists "+who+" identified by '"+this.conf.get ( "mysqlpass" )+"';\n"
                 + "      alter user "+who+" identified by '"+this.conf.get ( "mysqlpass" )+"';\n"
                 + "      grant all privileges on "+this.conf.get ( "mysqldb" )+".* to "+who+";\n";
        }
    }

    private void write ( ) throws IOException {
        /* The mail addresses are encrypted with it, never the same on two networks */
        this.conf.put ( "secretsalt", Ask.random ( 48 ) );

        String text = new String ( Files.readAllBytes ( Paths.get ( TEMPLATE ) ), StandardCharsets.UTF_8 );
        for ( Map.Entry<String,String> e : this.conf.entrySet ( ) ) {
            Matcher m = Pattern.compile ( "(?m)^"+e.getKey ( )+":.*$" ).matcher ( text );
            if ( ! m.find ( ) ) {
                throw new IOException ( TEMPLATE+" has no setting "+e.getKey ( ) );
            }
            text = m.replaceFirst ( Matcher.quoteReplacement ( e.getKey ( )+": "+yaml ( e.getValue ( ) ) ) );
        }
        /* What Avade will read must be what was answered */
        Map<String,Object> check = load ( text );
        for ( Map.Entry<String,String> e : this.conf.entrySet ( ) ) {
            if ( ! e.getValue().equals ( String.valueOf ( check.get ( e.getKey ( ) ) ) ) ) {
                throw new IOException ( "the setting "+e.getKey ( )+" can not be written as it is" );
            }
        }
        writePrivate ( Paths.get ( CONF ), text );
        String hub = this.hubText ( );
        writePrivate ( Paths.get ( HUBFILE ), hub );

        Path dir = Paths.get ( "" ).toAbsolutePath ( );
        out ( "" );
        out ( "Written: "+dir.resolve ( CONF )+" (only you can read it)" );
        out ( "" );
        out ( "KEEP A COPY of services.conf in a safe place. It has the secretsalt that the mail" );
        out ( "addresses in the database are encrypted with. Without it they can not be read." );
        out ( "" );
        out ( "What the hub needs. Put this in the ircd.conf of "+this.conf.get ( "hubname" )+" and /REHASH it" );
        out ( "(also saved in "+HUBFILE+"):" );
        out ( "" );
        out ( hub );
        out ( "Next:" );
        out ( "  - Be online as "+this.conf.get ( "master" )+" when services link. Services register that nick for you and" );
        out ( "    tell you its password. Then set your mail: /NickServ SET EMAIL <password> <address>" );
        out ( "  - Mail to users: ./avade.sh mailer start   (asks for your mail server the first time)" );
        out ( "  - After a reboot or a crash: crontab -e, and add" );
        out ( "        */5 * * * * "+dir.resolve ( "avade.sh" )+" check" );
        out ( "  - Every setting is explained in reference.conf, more in INSTALL." );
        out ( "" );
    }

    /* The lines for the ircd.conf of the hub */
    private String hubText ( ) {
        String host     = this.conf.get ( "hubhost" );
        boolean local   = host.equals ( "localhost" ) || host.equals ( "127.0.0.1" ) || host.equals ( "::1" );
        String from     = local ? "127.0.0.1" : "THE-ADDRESS-OF-THE-SERVICES-MACHINE";
        return "    # inside the options { } block that is already there:\n"
             + "        services_name   "+this.conf.get ( "name" )+";\n"
             + "        stats_name      "+this.conf.get ( "stats" )+";\n"
             + "\n"
             + "    connect {\n"
             + "        name    "+this.conf.get ( "name" )+";\n"
             + "        host    "+from+";\n"
             + "        apasswd "+this.conf.get ( "hubpass" )+";\n"
             + "        cpasswd "+this.conf.get ( "hubpass" )+";\n"
             + "        class   servers;\n"
             + "        flags   H;\n"
             + "    };\n"
             + "    super { \""+this.conf.get ( "name" )+"\"; \""+this.conf.get ( "stats" )+"\"; };\n"
             + "\n"
             + "    # unless the hub already listens for servers on this port:\n"
             + "    port { port "+this.conf.get ( "hubport" )+";"+( local ? " bind 127.0.0.1;" : "" )+" };\n";
    }

    /* services.conf is there: leave it, only offer the commands a newer version added */
    private int existing ( ) throws IOException {
        Path path = Paths.get ( CONF );
        out ( "services.conf is already here, it is left as it is." );
        if ( ! Files.exists ( Paths.get ( TEMPLATE ) ) ) {
            return 0;
        }
        String text = new String ( Files.readAllBytes ( path ), StandardCharsets.UTF_8 );
        Map<String,Object> mine, template;
        try {
            mine        = load ( text );
            template    = load ( new String ( Files.readAllBytes ( Paths.get ( TEMPLATE ) ), StandardCharsets.UTF_8 ) );
        } catch ( RuntimeException ex ) {
            out ( "It can not be read as a config file though: "+ex.getMessage ( ) );
            return 1;
        }
        List<String> have = new ArrayList<> ( );
        for ( String list : LISTS ) {
            have.addAll ( items ( mine.get ( list ) ) );
        }
        List<String> all = new ArrayList<> ( );
        Map<String,List<String>> add = new LinkedHashMap<> ( );
        for ( String list : LISTS ) {
            for ( String cmd : items ( template.get ( list ) ) ) {
                all.add ( cmd );
                if ( ! have.contains ( cmd ) ) {
                    add.computeIfAbsent ( list, k -> new ArrayList<> ( ) ).add ( cmd );
                }
            }
        }
        List<String> gone = new ArrayList<> ( have );
        gone.removeAll ( all );
        if ( add.isEmpty ( ) && gone.isEmpty ( ) ) {
            out ( "Its access lists have every command of this version." );
            return 0;
        }
        out ( "" );
        if ( ! add.isEmpty ( ) ) {
            out ( "Commands of this version that are not in its access lists yet (until they are, only SRA" );
            out ( "can use them). They would be added like this:" );
            for ( Map.Entry<String,List<String>> e : add.entrySet ( ) ) {
                out ( "    "+e.getKey ( )+": "+String.join ( ", ", e.getValue ( ) ) );
            }
        }
        if ( ! gone.isEmpty ( ) ) {
            out ( "Commands that no longer exist and would be removed: "+String.join ( ", ", gone ) );
        }
        if ( ! this.askYes ( "Change services.conf like that? (the old one is kept as services.conf.old)" ) ) {
            out ( "Nothing changed." );
            return 0;
        }
        List<String> lines = new ArrayList<> ( List.of ( text.split ( "\n", -1 ) ) );
        for ( String cmd : gone ) {
            lines.removeIf ( l -> l.matches ( "\\s*-\\s+"+Pattern.quote ( cmd )+"\\s*(#.*)?" ) );
        }
        for ( Map.Entry<String,List<String>> e : add.entrySet ( ) ) {
            int at = -1;
            String indent = " ";
            for ( int i = 0; i < lines.size ( ); i++ ) {
                if ( lines.get ( i ).matches ( e.getKey ( )+":\\s*(#.*)?" ) ) {
                    at = i;
                    while ( at + 1 < lines.size ( ) && lines.get ( at + 1 ).matches ( "\\s+-\\s.*" ) ) {
                        at++;
                        indent = lines.get ( at ).replaceAll ( "^(\\s+)-.*", "$1" );
                    }
                    break;
                }
            }
            if ( at < 0 ) {
                lines.add ( e.getKey ( )+":" );
                at = lines.size ( ) - 1;
            }
            for ( String cmd : e.getValue ( ) ) {
                lines.add ( ++at, indent+"- "+cmd );
            }
        }
        Files.copy ( path, Paths.get ( CONF+".old" ), java.nio.file.StandardCopyOption.REPLACE_EXISTING );
        writePrivate ( path, String.join ( "\n", lines ) );
        out ( "services.conf is changed. It takes effect at the next start, or with /RootServ REHASH." );
        return 0;
    }

    private boolean askYes ( String question ) {
        try {
            return this.ask.yes ( question, true );
        } catch ( Ask.Cancelled ex ) {
            return false;
        }
    }

    private static List<String> items ( Object list ) {
        List<String> out = new ArrayList<> ( );
        if ( list instanceof List<?> ) {
            for ( Object o : (List<?>) list ) {
                if ( o != null ) {
                    out.add ( o.toString().trim ( ) );
                }
            }
        }
        return out;
    }

    @SuppressWarnings ( "unchecked" )
    private static Map<String,Object> load ( String text ) {
        Object obj = new Yaml ( new SafeConstructor ( new LoaderOptions ( ) ) ).load ( text );
        if ( obj instanceof Map<?,?> ) {
            return (Map<String,Object>) obj;
        }
        throw new IllegalStateException ( "no settings in it" );
    }

    /* A value as YAML reads it back unchanged: quoted unless it is a plain word.
       Words like no, on and null mean something else to YAML, also as a nick */
    private static String yaml ( String value ) {
        boolean word = value.matches ( "[A-Za-z][A-Za-z0-9._-]*" )
                    && ! value.matches ( "(?i)true|false|yes|no|on|off|null|y|n" );
        if ( word || value.matches ( "[1-9][0-9]{0,4}" ) ) {
            return value;
        }
        return "'"+value.replace ( "'", "''" )+"'";
    }

    private static String port ( String value ) {
        return value.matches ( "[1-9][0-9]{0,4}" ) && Integer.parseInt ( value ) < 65536 ? null : "A port is a number from 1 to 65535.";
    }

    /* The file has passwords: readable for its owner only */
    private static void writePrivate ( Path path, String text ) throws IOException {
        Files.deleteIfExists ( path );
        try {
            Files.createFile ( path, PosixFilePermissions.asFileAttribute ( PosixFilePermissions.fromString ( "rw-------" ) ) );
        } catch ( UnsupportedOperationException ex ) {
            Files.createFile ( path );
        }
        Files.write ( path, text.getBytes ( StandardCharsets.UTF_8 ) );
    }

    private static void out ( String line ) {
        System.out.println ( line );
    }
}
