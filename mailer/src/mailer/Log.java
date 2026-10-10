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

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * One line per event, to the log file and to stdout. Never passwords or
 * the text of a mail (it can hold auth codes)
 *
 * @author DreamHealer
 */
public class Log {
    private static final DateTimeFormatter  STAMP   = DateTimeFormatter.ofPattern ( "yyyy-MM-dd HH:mm:ss" );
    private static PrintWriter              file;

    /**
     * @param fileName the log file, null for stdout only
     */
    public static synchronized void open ( String fileName ) {
        if ( fileName == null || fileName.isEmpty ( ) ) {
            return;
        }
        try {
            file = new PrintWriter ( new FileWriter ( fileName, StandardCharsets.UTF_8, true ), true );
        } catch ( IOException ex ) {
            System.out.println ( "Can not write the log file "+fileName+": "+ex.getMessage ( ) );
        }
    }

    /**
     * @return true when running (run, once), false for a one time command
     */
    public static synchronized boolean isOpen ( ) {
        return file != null;
    }

    /**
     * @param msg
     */
    public static synchronized void msg ( String msg ) {
        String line = STAMP.format ( LocalDateTime.now ( ) )+" "+msg;
        System.out.println ( line );
        if ( file != null ) {
            file.println ( line );
        }
    }
}
