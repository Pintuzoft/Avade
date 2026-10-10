/* 
 * Copyright (C) 2020 Fredrik Karlsson aka DreamHealer & avade.net
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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import channel.Chan;
import chanserv.ChanInfo;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import nickserv.NickInfo;
import user.User;

/**
 * Hash String
 * 
 * This class created to cope with possible hash collisions and potential 
 * exploits that comes with that, hopefully this solution will allow us to 
 * create unique re-generative hash codes to attached to almost everywhere 
 * while not adding too much of overhead.
 *
 * @author Fredrik Karlsson aka DreamHealer - avade.net
 */
public class HashString {
    private String string = "";
    private BigInteger code;
    
    /**
     *
     * @param str
     */
    public HashString ( String str ) {
        this.string = ( str != null ? str.trim() : "" );
        this.generateCode ( );
    }
    
    private void generateCode ( ) {
        try {
            /* The digest as a positive number, no detour over a hex string */
            MessageDigest crypt = MessageDigest.getInstance ( "SHA-256" );
            crypt.update ( asciiUpper ( this.string ).getBytes ( StandardCharsets.UTF_8 ) );
            this.code = new BigInteger ( 1, crypt.digest ( ) );
        } catch ( NoSuchAlgorithmException ex ) {
            Logger.getLogger(HashString.class.getName()).log ( Level.SEVERE, null, ex );
        }
    }
    
    /**
     *
     * @return
     */
    /* Same case folding as bahamut (CASEMAPPING=ascii): only a-z, never
       depending on the locale, other characters are kept as they are */
    private static String asciiUpper ( String str ) {
        char[] chars = str.toCharArray ( );
        for ( int i = 0; i < chars.length; i++ ) {
            if ( chars[i] >= 'a' && chars[i] <= 'z' ) {
                chars[i] = (char) ( chars[i] - 32 );
            }
        }
        return new String ( chars );
    }
    
    public BigInteger getCode ( ) {
        return this.code;
    }

    
    /**
     *
     * @param code
     * @return
     */
    public boolean is ( HashString code ) {
        return code.getCode().compareTo(this.code) == 0;
    }

    /**
     * The same as is ( ): two HashStrings made from the same name are equal,
     * whatever the case of a-z. Without this Java compared the objects, so a
     * new HashString was never found in a list, or as the key of a map.
     * (== still compares the objects, use is ( ).)
     * @param other
     * @return
     */
    @Override
    public boolean equals ( Object other ) {
        return other instanceof HashString && this.code != null && this.code.equals ( ( (HashString) other ).code );
    }

    @Override
    public int hashCode ( ) {
        return this.code != null ? this.code.hashCode ( ) : 0;
    }
    
    /**
     *
     * @param ni
     * @return
     */
    public boolean is ( NickInfo ni ) {
        return this.code.equals ( ni.getName().getCode() );
    }
     
    /**
     *
     * @param ci
     * @return
     */
    public boolean is ( ChanInfo ci ) {
        return this.code.equals ( ci.getName().getCode() );
    }
    
    /**
     *
     * @param user
     * @return
     */
    public boolean is ( User user ) {
        return this.code.equals ( user.getName().getCode() );
    }
    
    /**
     *
     * @param chan
     * @return
     */
    public boolean is ( Chan chan ) {
        return this.code.equals ( chan.getName().getCode() );
    }
    
    /**
     *
     * @return
     */
    public String getString ( ) {
        return this.string;
    }
    
    /**
     *
     * @param word
     * @return
     */
    public boolean contains ( String word ) {
        return this.string.contains ( word );
    }
    
    /**
     *
     * @return
     */
    public int length ( ) {
        return this.string.length ( );
    }
    
    /**
     *
     * @return
     */
    @Override
    public String toString ( ) {
        return this.string;
    }
}
