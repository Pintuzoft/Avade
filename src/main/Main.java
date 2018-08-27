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
package main;

import core.Handler;
import core.Proc;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class of Avade Services
 * @author DreamHealer
 */
public class Main {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main ( String[] args )  throws IOException {
        /* run main loop */
        try {
            Proc proc = new Proc ( );
        } catch ( IOException e )  {
            Logger.getLogger ( Handler.class.getName ( )  ) .log ( Level.SEVERE, null, e );   
        }
    }
}