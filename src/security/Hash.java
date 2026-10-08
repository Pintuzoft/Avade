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
package security;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Passwords are stored as a one way hash, nobody can read them back. Also
 * the random codes sent in mails.
 *
 * Stored as: pbkdf2-sha256$<iterations>$<salt>$<hash> (salt and hash base64)
 *
 * @author DreamHealer
 */
public class Hash {
    private static final String         ALGORITHM   = "PBKDF2WithHmacSHA256";
    private static final String         PREFIX      = "pbkdf2-sha256";
    /* About 70 ms on one core. Services check passwords on the thread that
       talks to the hub, so this can not be as slow as on a web server */
    private static final int            ITERATIONS  = 210000;
    private static final int            SALTBYTES   = 16;
    private static final int            KEYBITS     = 256;
    private static final String         CODECHARS   = "abcdefghjkmnpqrstuvwxyz23456789";
    private static final SecureRandom   random      = new SecureRandom ( );

    /**
     * @param pass the password in clear
     * @return the hash to store
     */
    public static String password ( String pass ) {
        byte[] salt = new byte[SALTBYTES];
        random.nextBytes ( salt );
        return PREFIX+"$"+ITERATIONS+"$"+b64 ( salt )+"$"+b64 ( derive ( pass, salt, ITERATIONS ) );
    }

    /**
     * @param pass the password in clear
     * @param stored a hash from password ( )
     * @return true if it is the password
     */
    public static boolean verify ( String pass, String stored ) {
        String[] part = split ( stored );
        if ( pass == null || part == null ) {
            return false;
        }
        try {
            byte[] salt = Base64.getDecoder().decode ( part[2] );
            byte[] hash = Base64.getDecoder().decode ( part[3] );
            byte[] test = derive ( pass, salt, Integer.parseInt ( part[1] ) );
            return MessageDigest.isEqual ( hash, test );
        } catch ( IllegalArgumentException ex ) {
            return false;
        }
    }

    /**
     * @param stored
     * @return true if it is a hash from password ( ), not an old encrypted password
     */
    public static boolean isHashed ( String stored ) {
        return split ( stored ) != null;
    }

    /**
     * @param stored
     * @return true if it was made with fewer iterations than new hashes get
     */
    public static boolean needsRehash ( String stored ) {
        String[] part = split ( stored );
        try {
            return part == null || Integer.parseInt ( part[1] ) < ITERATIONS;
        } catch ( NumberFormatException ex ) {
            return true;
        }
    }

    /**
     * @return 32 random hex characters, for the links in auth mails
     */
    public static String token ( ) {
        byte[] buf = new byte[16];
        random.nextBytes ( buf );
        return HexFormat.of().formatHex ( buf );
    }

    /**
     * @param length
     * @return a random code that is easy to type, without 0/o, 1/l/i
     */
    public static String code ( int length ) {
        StringBuilder buf = new StringBuilder ( );
        for ( int i = 0; i < length; i++ ) {
            buf.append ( CODECHARS.charAt ( random.nextInt ( CODECHARS.length ( ) ) ) );
        }
        return buf.toString ( );
    }

    /**
     * Compare two codes without the time telling how much of them matched
     * @param a
     * @param b
     * @return
     */
    public static boolean same ( String a, String b ) {
        if ( a == null || b == null ) {
            return false;
        }
        return MessageDigest.isEqual ( a.getBytes ( StandardCharsets.UTF_8 ), b.getBytes ( StandardCharsets.UTF_8 ) );
    }

    private static String[] split ( String stored ) {
        if ( stored == null || ! stored.startsWith ( PREFIX+"$" ) ) {
            return null;
        }
        String[] part = stored.split ( "\\$" );
        return part.length == 4 ? part : null;
    }

    private static byte[] derive ( String pass, byte[] salt, int iterations ) {
        PBEKeySpec spec = new PBEKeySpec ( pass.toCharArray ( ), salt, iterations, KEYBITS );
        try {
            return SecretKeyFactory.getInstance ( ALGORITHM ).generateSecret ( spec ).getEncoded ( );
        } catch ( GeneralSecurityException ex ) {
            /* part of every Java since 8, can not happen */
            throw new IllegalStateException ( ex );
        } finally {
            spec.clearPassword ( );
        }
    }

    private static String b64 ( byte[] buf ) {
        return Base64.getEncoder().withoutPadding().encodeToString ( buf );
    }
}
