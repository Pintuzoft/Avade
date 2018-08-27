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
package server;

/**
 *
 * @author DreamHealer
 */
public class Numeric { 
    public static final int RPL_STATSUPTIME = 242;      /* UPTIME */
    public static final int RPL_STATSOLINE  = 243;      /* OLINE */
    /* VERSION */
    public static final int RPL_VERSION     = 351;      /* VERSION */ 
    public static final int RPL_INFO        = 371;      /* INFO */ 
    public static final int RPL_MOTD        = 372;      /* MOTD */ 
    public static final int RPL_INFOSTART   = 373;      /* INFOSTART */
    public static final int RPL_INFOEND     = 374;      /* INFOEND */ 
    public static final int RPL_MOTDSTART   = 375;      /* MOTDSTART */
    public static final int RPL_MOTDEND     = 376;      /* MOTDEND */ 
    public static final int ERR_NOSUCHNICK  = 401;      /* NOSUCHNICK */ 
    public static final int RPL_NOMOTD      = 422;      /* NOMOTD */ 
}
