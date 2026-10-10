/* 
 * Copyright (C) 2019 Fredrik Karlsson aka DreamHealer & avade.net
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
