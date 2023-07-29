/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package user;

import channel.Chan;

/**
 *
 * @author Fredrik Karlsson aka DreamHealer - avade.net
 */
public class UserCheck {
    private User user;
    private Chan chan;
    
    /**
     *
     * @param chan
     * @param user
     */
    public UserCheck ( Chan chan, User user ) {
        this.chan = chan;
        this.user = user;
    }

    /**
     *
     * @return
     */
    public User getUser ( ) {
        return user;
    }

    /**
     *
     * @return
     */
    public Chan getChan ( ) {
        return chan;
    }
    
}
