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
import java.math.*;
import java.security.*;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author DreamHealer
 */
public class Hash {
    private static MessageDigest enc; 
    
    public static String md5 ( String original )  {
        try {
            enc = MessageDigest.getInstance ( "MD5" );
            enc.update ( original.getBytes ( ), 0, original.length ( )  );
            String md5;
            md5 = new BigInteger(1,enc.digest()).toString ( 16 );
            return md5;
            
        } catch  ( NoSuchAlgorithmException ex )  {
            Logger.getLogger ( Hash.class.getName ( )  ) .log ( Level.SEVERE, null, ex );
        }
        return null;
    }
}
