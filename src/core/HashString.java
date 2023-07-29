/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import channel.Chan;
import chanserv.ChanInfo;
import java.math.BigInteger;
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
    
    /**
     *
     * @param str
     * @param code
     */
    public HashString ( String str, BigInteger code ) {
        this.string = str.trim();
        this.code = code;
    }
    
    private void generateCode ( ) {
        try {
            String hex;
            MessageDigest crypt = MessageDigest.getInstance ( "SHA-256" );
            crypt.reset ( );
            crypt.update ( this.string.toUpperCase().getBytes ( ) );
            hex = String.format ( "%064x", new BigInteger ( 1, crypt.digest ( ) ) );
            this.code = new BigInteger ( hex, 16 );
        } catch ( NoSuchAlgorithmException ex ) {
            Logger.getLogger(HashString.class.getName()).log ( Level.SEVERE, null, ex );
        }
    }
    
    /**
     *
     * @return
     */
    public BigInteger getCode ( ) {
        return this.code;
    }
    
    /**
     *
     * @return
     */
    public String getCodeStr ( ) {
        return ""+this.code;
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
     *
     * @param ni
     * @return
     */
    public boolean is ( NickInfo ni ) {
        return this.code == ni.getName().getCode();
    }
     
    /**
     *
     * @param ci
     * @return
     */
    public boolean is ( ChanInfo ci ) {
        return this.code == ci.getName().getCode();
    }
    
    /**
     *
     * @param user
     * @return
     */
    public boolean is ( User user ) {
        return this.code == user.getName().getCode();
    }
    
    /**
     *
     * @param chan
     * @return
     */
    public boolean is ( Chan chan ) {
        return this.code == chan.getName().getCode();
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
