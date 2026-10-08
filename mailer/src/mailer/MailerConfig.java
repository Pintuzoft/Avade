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
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
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
 * mailer.conf, see mailer-template.conf
 *
 * @author DreamHealer
 */
public class MailerConfig {
    private Map<String,Object>  conf;
    private Map<String,Object>  mysql;
    private Map<String,Object>  smtp;

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
        this.mysql  = map ( this.conf.get ( "mysql" ), "mysql" );
        this.smtp   = map ( this.conf.get ( "smtp" ), "smtp" );

        for ( String key : new String[] { "host", "user", "db" } ) {
            this.need ( this.mysql, "mysql", key );
        }
        if ( this.send ( ) ) {
            this.need ( this.smtp, "smtp", "host" );
            this.need ( this.smtp, "smtp", "from" );
            if ( this.smtpAuth ( ) ) {
                this.need ( this.smtp, "smtp", "user" );
                this.need ( this.smtp, "smtp", "pass" );
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

    public String dbHost ( )            { return str ( this.mysql, "host", "localhost" );   }
    public int dbPort ( )               { return num ( this.mysql, "port", 3306 );          }
    public String dbUser ( )            { return str ( this.mysql, "user", "" );            }
    public String dbPass ( )            { return str ( this.mysql, "pass", "" );            }
    public String dbName ( )            { return str ( this.mysql, "db", "" );              }

    public String smtpHost ( )          { return str ( this.smtp, "host", "" );             }
    public int smtpPort ( )             { return num ( this.smtp, "port", 587 );            }
    /** @return STARTTLS required, and the certificate must match the host */
    public boolean smtpTls ( )          { return bool ( this.smtp, "tls", true );           }
    public boolean smtpAuth ( )         { return bool ( this.smtp, "auth", true );          }
    public String smtpUser ( )          { return str ( this.smtp, "user", "" );             }
    public String smtpPass ( )          { return str ( this.smtp, "pass", "" );             }
    public String smtpFrom ( )          { return str ( this.smtp, "from", "" );             }

    @SuppressWarnings ( "unchecked" )
    private static Map<String,Object> map ( Object obj, String what ) {
        if ( obj instanceof Map<?,?> ) {
            return (Map<String,Object>) obj;
        }
        throw new IllegalStateException ( what+" is missing or not a section in the config" );
    }

    private void need ( Map<String,Object> section, String name, String key ) {
        if ( str ( section, key, "" ).isEmpty ( ) ) {
            throw new IllegalStateException ( name+"."+key+" is missing in the config" );
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
