/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package operserv;

import core.HashString;

/**
 *
 * @author Fredrik Karlsson aka DreamHealer & avade.net
 */
class NetServer {
    private HashString name;
    private HashString primary;
    private HashString secondary;
    
    public NetServer ( String name, String primary, String secondary ) {
        this.name = new HashString ( name );
        this.primary = new HashString (primary == null ? "-" : primary );
        this.secondary = new HashString ( secondary == null ? "-" : secondary );
    }

    public HashString getName() {
        return name;
    }
    public String getNameStr() {
        return name.getString();
    }

    public HashString getPrimary() {
        return primary;
    }
    public String getPrimaryStr() {
        return primary.getString();
    }
    
    public void setPrimary ( String str ) {
        this.primary = new HashString ( str );
    }
    
    public HashString getSecondary() {
        return secondary;
    }
    public String getSecondaryStr() {
        return secondary.getString();
    }
    
    public void setSecondary ( String str ) {
        this.secondary = new HashString ( str );
    }
    
    public boolean is ( HashString name ) {
        return this.name.is(name);
    }

}
