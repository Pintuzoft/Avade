/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

/**
 *
 * @author Fredrik Karlsson aka DreamHealer & avade.net
 */
public class Trigger extends HashNumeric {
    private static boolean warn;
    private static HashString action;
    private static int warnIP;
    private static int warnRange;
    private static int actionIP;
    private static int actionRange;
    
    public Trigger ( ) {
        warn = Proc.getConf().getBoolean ( TRIGGERWARN );
        action = Proc.getConf().get ( TRIGGERACTION );
        warnIP = Proc.getConf().getInt ( TRIGGERWARNIP );
        warnRange = Proc.getConf().getInt ( TRIGGERWARNRANGE );
        actionIP = Proc.getConf().getInt ( TRIGGERACTIONIP );
        actionRange = Proc.getConf().getInt ( TRIGGERACTIONRANGE );
    }

    public static boolean isWarn() {
        return warn;
    }

    public static HashString getAction() {
        return action;
    }

    public static int getWarnIP() {
        return warnIP;
    }

    public static int getWarnRange() {
        return warnRange;
    }

    public static int getActionIP() {
        return actionIP;
    }

    public static int getActionRange() {
        return actionRange;
    }
    
    
    
    
    
}
