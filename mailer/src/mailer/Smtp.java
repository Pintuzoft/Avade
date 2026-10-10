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

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.SendFailedException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;
import org.eclipse.angus.mail.smtp.SMTPAddressFailedException;
import org.eclipse.angus.mail.smtp.SMTPSendFailedException;

/**
 * Sends mails over SMTP (Jakarta Mail). One connection is used for all the
 * mails of a round.
 *
 * @author DreamHealer
 */
public class Smtp {
    private MailerConfig    config;
    private Session         session;
    private Transport       transport;

    /**
     * The server would not take this mail. permanent: never try it again
     */
    public static class MailRefused extends Exception {
        public final boolean permanent;

        MailRefused ( String msg, boolean permanent ) {
            super ( msg );
            this.permanent = permanent;
        }
    }

    /**
     * @param config
     */
    public Smtp ( MailerConfig config ) {
        this.config = config;
        Properties props = new Properties ( );
        props.put ( "mail.smtp.host",                   config.smtpHost ( ) );
        props.put ( "mail.smtp.port",                   String.valueOf ( config.smtpPort ( ) ) );
        props.put ( "mail.smtp.auth",                   String.valueOf ( config.smtpAuth ( ) ) );
        props.put ( "mail.smtp.starttls.enable",        String.valueOf ( config.smtpTls ( ) ) );
        props.put ( "mail.smtp.starttls.required",      String.valueOf ( config.smtpTls ( ) ) );
        props.put ( "mail.smtp.ssl.checkserveridentity", "true" );
        props.put ( "mail.smtp.connectiontimeout",      "10000" );
        props.put ( "mail.smtp.timeout",                "20000" );
        props.put ( "mail.smtp.writetimeout",           "20000" );
        this.session = Session.getInstance ( props );
    }

    /**
     * @param mail
     * @throws MailRefused the server would not take this mail
     * @throws MessagingException the server could not be reached or the
     *         connection broke: nothing is wrong with the mail
     */
    public void send ( MailBox.Mail mail ) throws MailRefused, MessagingException {
        MimeMessage msg = new MimeMessage ( this.session );
        try {
            msg.setFrom ( new InternetAddress ( this.config.smtpFrom ( ) ) );
            msg.setRecipients ( Message.RecipientType.TO, InternetAddress.parse ( mail.to, true ) );
        } catch ( AddressException ex ) {
            throw new MailRefused ( "not a valid address", true );
        }
        msg.setSubject ( mail.subject, "UTF-8" );
        msg.setText ( mail.body, "UTF-8" );
        msg.setSentDate ( new Date ( ) );
        msg.setHeader ( "X-Avade-Mail-Id", String.valueOf ( mail.id ) );
        msg.saveChanges ( );

        if ( this.transport == null || ! this.transport.isConnected ( ) ) {
            this.transport = this.session.getTransport ( "smtp" );
            if ( this.config.smtpAuth ( ) ) {
                this.transport.connect ( this.config.smtpUser ( ), this.config.smtpPass ( ) );
            } else {
                this.transport.connect ( );
            }
        }
        try {
            this.transport.sendMessage ( msg, msg.getAllRecipients ( ) );
        } catch ( SendFailedException ex ) {
            int code = replyCode ( ex );
            if ( code == 0 ) {
                throw ex;
            }
            /* 5xx: the server will never take it, 4xx: maybe later */
            throw new MailRefused ( "the server answered "+code, code >= 500 );
        }
    }

    /**
     * After a round, or when the connection broke
     */
    public void close ( ) {
        if ( this.transport != null ) {
            try {
                this.transport.close ( );
            } catch ( MessagingException ex ) {
                /* already gone */
            }
            this.transport = null;
        }
    }

    /* The SMTP reply code the server gave for this mail, 0 if there is none */
    private static int replyCode ( Exception ex ) {
        Exception e = ex;
        while ( e != null ) {
            if ( e instanceof SMTPAddressFailedException ) {
                return ( (SMTPAddressFailedException) e ).getReturnCode ( );
            } else if ( e instanceof SMTPSendFailedException ) {
                return ( (SMTPSendFailedException) e ).getReturnCode ( );
            }
            e = ( e instanceof MessagingException ) ? ( (MessagingException) e ).getNextException ( ) : null;
        }
        return 0;
    }
}
