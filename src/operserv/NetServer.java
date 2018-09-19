/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package operserv;

/**
 *
 * @author Fredrik Karlsson aka DreamHealer & avade.net
 */
class NetServer {
    private String name;
    private String primary;
    private String secondary;
    private int hashCode;
    
    public NetServer ( String name, String primary, String secondary ) {
        this.name = name;
        this.primary = primary == null ? "-" : primary;
        this.secondary = secondary == null ? "-" : secondary;
        this.hashCode = name.toUpperCase().hashCode();
    }

    public String getName() {
        return name;
    }

    public String getPrimary() {
        return primary;
    }

    public String getSecondary() {
        return secondary;
    }

    public int getHashCode ( ) {
        return this.hashCode;
    }
    
    
}
