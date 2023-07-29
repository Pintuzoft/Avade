/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package operserv;

import core.Handler;
import java.util.TimerTask;

/**
 *
 * @author Fredrik Karlsson aka DreamHealer - avade.net
 */
public class UnSQlineTask extends TimerTask {
    private String nick;
    public UnSQlineTask ( String nick ) {
        this.nick = nick;
    }
    
    @Override
    public void run() {
        Handler.getOperServ().sendServ ( "UNSQLINE "+this.nick );
    }
    
}
