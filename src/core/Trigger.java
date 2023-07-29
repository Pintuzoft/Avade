/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

/**
 *
 * @author Fredrik Karlsson aka DreamHealer - avade.net
 */
public class Trigger extends HashNumeric {
    
    /**
     *
     */
    public Trigger ( ) { /* Nothing to do */ }

    /**
     *
     * @return
     */
    public static boolean isWarn() {
        return Proc.getConf().getBoolean ( TRIGGERWARN );
    }

    /**
     *
     * @return
     */
    public static HashString getAction() {
        return Proc.getConf().get ( TRIGGERACTION );
    }

    /**
     *
     * @return
     */
    public static int getWarnIP() {
        return Proc.getConf().getInt ( TRIGGERWARNIP );
    }

    /**
     *
     * @return
     */
    public static int getWarnRange() {
        return Proc.getConf().getInt ( TRIGGERWARNRANGE );
    }

    /**
     *
     * @return
     */
    public static int getActionIP() {
        return Proc.getConf().getInt ( TRIGGERACTIONIP );
    }

    /**
     *
     * @return
     */
    public static int getActionRange() {
        return Proc.getConf().getInt ( TRIGGERACTIONRANGE );
    }
    
    
    
    
    
}
