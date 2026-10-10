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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The mailbox table: Avade puts the mails there with status 1, the mailer
 * sends them and sets the status.
 *
 * @author DreamHealer
 */
public class MailBox {
    public static final int     SEND        = 1;        /* put there by Avade */
    public static final int     SENT        = 0;
    public static final int     SENDING     = 2;        /* taken, being sent */
    public static final int     NOT_SENT    = 3;        /* send: false in mailer.conf */
    public static final int     FAILED      = 500;

    private MailerConfig        config;
    private Connection          sql;
    private long                lastError;

    /**
     * One mail from the mailbox
     */
    public static class Mail {
        public final int        id;
        public final String     to;
        public final String     subject;
        public final String     body;

        Mail ( int id, String to, String subject, String body ) {
            this.id         = id;
            this.to         = to;
            this.subject    = subject == null ? "" : subject;
            this.body       = body == null ? "" : body;
        }
    }

    /**
     * @param config
     */
    public MailBox ( MailerConfig config ) {
        this.config = config;
    }

    /**
     * @return true if there is a working connection, makes a new one if needed
     */
    public boolean connect ( ) {
        try {
            if ( this.sql != null && this.sql.isValid ( 2 ) ) {
                return true;
            }
        } catch ( SQLException ex ) {
            /* broken, a new one below */
        }
        this.close ( );
        try {
            /* Timeouts so a database that stops answering can not hang the mailer */
            this.sql = DriverManager.getConnection (
                "jdbc:mysql://"+this.config.dbHost ( )+":"+this.config.dbPort ( )+"/"+this.config.dbName ( )
                +"?characterEncoding=UTF-8&connectTimeout=5000&socketTimeout=20000&tcpKeepAlive=true",
                this.config.dbUser ( ),
                this.config.dbPass ( )
            );
            if ( Log.isOpen ( ) ) {
                Log.msg ( "Database connection to "+this.config.dbHost ( )+"/"+this.config.dbName ( ) );
            }
            return true;
        } catch ( SQLException ex ) {
            this.sql = null;
            /* once a minute is enough while it is down */
            if ( System.currentTimeMillis ( ) - this.lastError > 60000 ) {
                Log.msg ( "No database connection: "+ex.getMessage ( ) );
                this.lastError = System.currentTimeMillis ( );
            }
            return false;
        }
    }

    /**
     *
     */
    public void close ( ) {
        if ( this.sql != null ) {
            try {
                this.sql.close ( );
            } catch ( SQLException ex ) {
                /* already gone */
            }
            this.sql = null;
        }
    }

    /**
     * @param limit
     * @return the oldest mails waiting to be sent
     * @throws SQLException
     */
    public List<Mail> waiting ( int limit ) throws SQLException {
        List<Mail> list = new ArrayList<> ( );
        String query = "select id, mail, subject, body from mailbox where status = ? order by id limit ?";
        try ( PreparedStatement ps = this.sql.prepareStatement ( query ) ) {
            ps.setInt ( 1, SEND );
            ps.setInt ( 2, limit );
            try ( ResultSet res = ps.executeQuery ( ) ) {
                while ( res.next ( ) ) {
                    list.add ( new Mail ( res.getInt ( 1 ), res.getString ( 2 ), res.getString ( 3 ), res.getString ( 4 ) ) );
                }
            }
        }
        return list;
    }

    /**
     * Take a mail before it is sent, so it is never sent twice
     * @param id
     * @return false if it was not waiting any more
     * @throws SQLException
     */
    public boolean take ( int id ) throws SQLException {
        return this.update ( "update mailbox set status = ? where id = ? and status = ?", SENDING, id, SEND ) == 1;
    }

    /**
     * @param id
     * @param status
     * @throws SQLException
     */
    public void set ( int id, int status ) throws SQLException {
        this.update ( "update mailbox set status = ? where id = ?", status, id );
    }

    /**
     * Mails that were being sent when the mailer stopped: they may have gone
     * out, so they are not sent again by themselves (resend sends them)
     * @return how many
     * @throws SQLException
     */
    public int interrupted ( ) throws SQLException {
        return this.update ( "update mailbox set status = ? where status = ?", FAILED, SENDING );
    }

    /**
     * @param id
     * @return how many mails will be sent again
     * @throws SQLException
     */
    public int resend ( int id ) throws SQLException {
        return this.update ( "update mailbox set status = ? where id = ? and status <> ?", SEND, id, SENDING );
    }

    /**
     * @return how many failed mails will be sent again
     * @throws SQLException
     */
    public int resendFailed ( ) throws SQLException {
        return this.update ( "update mailbox set status = ? where status = ?", SEND, FAILED );
    }

    /**
     * Sent, not sent and failed mails are removed after some days: the
     * mailbox holds addresses and codes in clear
     * @param days
     * @return how many were removed
     * @throws SQLException
     */
    public int clean ( int days ) throws SQLException {
        String query = "delete from mailbox where status in (?, ?, ?) and stamp < unix_timestamp ( ) - ?";
        try ( PreparedStatement ps = this.sql.prepareStatement ( query ) ) {
            ps.setInt ( 1, SENT );
            ps.setInt ( 2, NOT_SENT );
            ps.setInt ( 3, FAILED );
            ps.setLong ( 4, days * 86400L );
            return ps.executeUpdate ( );
        }
    }

    /**
     * @return status -> number of mails
     * @throws SQLException
     */
    public Map<Integer,Integer> count ( ) throws SQLException {
        Map<Integer,Integer> counts = new LinkedHashMap<> ( );
        try ( PreparedStatement ps = this.sql.prepareStatement ( "select status, count(*) from mailbox group by status order by status" );
              ResultSet res = ps.executeQuery ( ) ) {
            while ( res.next ( ) ) {
                counts.put ( res.getInt ( 1 ), res.getInt ( 2 ) );
            }
        }
        return counts;
    }

    private int update ( String query, int... values ) throws SQLException {
        try ( PreparedStatement ps = this.sql.prepareStatement ( query ) ) {
            for ( int i = 0; i < values.length; i++ ) {
                ps.setInt ( i + 1, values[i] );
            }
            return ps.executeUpdate ( );
        }
    }
}
