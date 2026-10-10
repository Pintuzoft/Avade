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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

/**
 * mailer.conf, every setting is explained in mailer-reference.conf. The
 * database settings have the same names as in services.conf
 *
 * @author DreamHealer
 */
public class MailerConfig {
    private Map<String,Object>  conf;

    /**
     * Settings that are not in a file yet (setup)
     * @param conf
     */
    MailerConfig ( Map<String,Object> conf ) {
        this.conf = conf;
    }

    /**
     * @param fileName
     * @throws IllegalStateException with what is wrong in the file
     */
    public MailerConfig ( String fileName ) {
        /* Only maps, lists and plain values, never objects named by a tag in the file */
        Yaml yaml = new Yaml ( new SafeConstructor ( new LoaderOptions ( ) ) );
        try ( InputStream in = new FileInputStream ( fileName ) ) {
            this.conf = map ( yaml.load ( in ), fileName );
        } catch ( IOException ex ) {
            throw new IllegalStateException ( "can not read "+fileName+": "+ex.getMessage ( ) );
        }
        for ( String key : new String[] { "mysqlhost", "mysqluser", "mysqldb" } ) {
            this.need ( key );
        }
        if ( this.send ( ) ) {
            this.need ( "smtphost" );
            this.need ( "smtpfrom" );
            if ( this.smtpAuth ( ) ) {
                this.need ( "smtpuser" );
                this.need ( "smtppass" );
            }
        }
    }

    /** @return false: nothing is sent, the mails are only marked as not sent */
    public boolean send ( )             { return bool ( this.conf, "send", false );         }
    public String logFile ( )           { return str ( this.conf, "logfile", "mailer.log" ); }
    /** @return seconds between looks in the mailbox */
    public int interval ( )             { return num ( this.conf, "interval", 5 );          }
    /** @return at most this many mails a second */
    public int rate ( )                 { return Math.max ( 1, num ( this.conf, "rate", 10 ) ); }
    /** @return times a mail the server turns away for now is tried */
    public int retries ( )              { return Math.max ( 1, num ( this.conf, "retries", 5 ) ); }

    public String dbHost ( )            { return str ( this.conf, "mysqlhost", "localhost" ); }
    public int dbPort ( )               { return num ( this.conf, "mysqlport", 3306 );      }
    public String dbUser ( )            { return str ( this.conf, "mysqluser", "" );        }
    public String dbPass ( )            { return str ( this.conf, "mysqlpass", "" );        }
    public String dbName ( )            { return str ( this.conf, "mysqldb", "" );          }

    public String smtpHost ( )          { return str ( this.conf, "smtphost", "" );         }
    public int smtpPort ( )             { return num ( this.conf, "smtpport", 587 );        }
    /** @return STARTTLS required, and the certificate must match the host */
    public boolean smtpTls ( )          { return bool ( this.conf, "smtptls", true );       }
    public boolean smtpAuth ( )         { return bool ( this.conf, "smtpauth", true );      }
    public String smtpUser ( )          { return str ( this.conf, "smtpuser", "" );         }
    public String smtpPass ( )          { return str ( this.conf, "smtppass", "" );         }
    public String smtpFrom ( )          { return str ( this.conf, "smtpfrom", "" );         }

    @SuppressWarnings ( "unchecked" )
    private static Map<String,Object> map ( Object obj, String what ) {
        if ( obj instanceof Map<?,?> ) {
            return (Map<String,Object>) obj;
        }
        throw new IllegalStateException ( what+" has no settings" );
    }

    private void need ( String key ) {
        if ( str ( this.conf, key, "" ).isEmpty ( ) ) {
            throw new IllegalStateException ( key+" is missing in the config" );
        }
    }

    private static String str ( Map<String,Object> section, String key, String def ) {
        Object value = section.get ( key );
        return value == null ? def : value.toString().trim ( );
    }

    private static int num ( Map<String,Object> section, String key, int def ) {
        String value = str ( section, key, "" );
        try {
            return value.isEmpty ( ) ? def : Integer.parseInt ( value );
        } catch ( NumberFormatException ex ) {
            throw new IllegalStateException ( key+" must be a number, not "+value );
        }
    }

    private static boolean bool ( Map<String,Object> section, String key, boolean def ) {
        Object value = section.get ( key );
        if ( value == null ) {
            return def;
        }
        return value instanceof Boolean ? (Boolean) value : Boolean.parseBoolean ( value.toString().trim ( ) );
    }
}
