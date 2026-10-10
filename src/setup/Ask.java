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
package setup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * Questions on the terminal. The answers can also come from a file
 * (./avade.sh setup < answers), one line per question.
 *
 * @author DreamHealer
 */
public class Ask {
    private static final String         CHARS   = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom   random  = new SecureRandom ( );
    private BufferedReader              in      = new BufferedReader ( new InputStreamReader ( System.in, StandardCharsets.UTF_8 ) );

    /**
     * No more input (ctrl-d, or the answers ran out): nothing is written
     */
    public static class Cancelled extends RuntimeException {
    }

    /**
     * Tells what is wrong with an answer, null when it is fine
     */
    public interface Check {
        String problem ( String value );
    }

    /**
     * @param question
     * @param def the answer when only Enter is pressed, null if there must be an answer
     * @param check
     * @return the answer
     */
    public String line ( String question, String def, Check check ) {
        while ( true ) {
            System.out.print ( "  "+question+( def != null && ! def.isEmpty ( ) ? " ["+def+"]" : "" )+": " );
            System.out.flush ( );
            String value = this.read ( );
            if ( value.isEmpty ( ) && def != null ) {
                value = def;
            }
            String problem = value.isEmpty ( ) ? "An answer is needed here." : ( check != null ? check.problem ( value ) : null );
            if ( problem == null ) {
                return value;
            }
            System.out.println ( "    "+problem );
        }
    }

    /**
     * A password: Enter makes a random one
     * @param question
     * @param check
     * @return the password
     */
    public String secret ( String question, Check check ) {
        while ( true ) {
            System.out.print ( "  "+question+" [Enter makes one]: " );
            System.out.flush ( );
            String value = this.read ( );
            if ( value.isEmpty ( ) ) {
                return random ( 24 );
            }
            String problem = check != null ? check.problem ( value ) : null;
            if ( problem == null ) {
                return value;
            }
            System.out.println ( "    "+problem );
        }
    }

    /**
     * @param question
     * @param def the answer when only Enter is pressed
     * @return yes or no
     */
    public boolean yes ( String question, boolean def ) {
        while ( true ) {
            System.out.print ( "  "+question+( def ? " [Y/n]: " : " [y/N]: " ) );
            System.out.flush ( );
            String value = this.read().toLowerCase ( );
            if ( value.isEmpty ( ) ) {
                return def;
            } else if ( value.equals ( "y" ) || value.equals ( "yes" ) ) {
                return true;
            } else if ( value.equals ( "n" ) || value.equals ( "no" ) ) {
                return false;
            }
            System.out.println ( "    Answer y or n." );
        }
    }

    /**
     * @param question
     * @param choices the letters that can be answered, the first is used for Enter
     * @return the letter
     */
    public char choice ( String question, String choices ) {
        while ( true ) {
            System.out.print ( "  "+question+": " );
            System.out.flush ( );
            String value = this.read().toLowerCase ( );
            if ( value.isEmpty ( ) ) {
                return choices.charAt ( 0 );
            } else if ( value.length ( ) == 1 && choices.indexOf ( value.charAt ( 0 ) ) >= 0 ) {
                return value.charAt ( 0 );
            }
        }
    }

    /**
     * @param length
     * @return random letters and digits
     */
    public static String random ( int length ) {
        StringBuilder buf = new StringBuilder ( );
        for ( int i = 0; i < length; i++ ) {
            buf.append ( CHARS.charAt ( random.nextInt ( CHARS.length ( ) ) ) );
        }
        return buf.toString ( );
    }

    private String read ( ) {
        try {
            String line = this.in.readLine ( );
            if ( line == null ) {
                System.out.println ( );
                throw new Cancelled ( );
            }
            return line.trim ( );
        } catch ( IOException ex ) {
            throw new Cancelled ( );
        }
    }
}
