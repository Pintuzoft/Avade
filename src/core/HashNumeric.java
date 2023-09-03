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
package core;
 
/**
 *
 * @author DreamHealer
 */
public abstract class HashNumeric extends TextFormat {
    /* CONFIG */

    /**
     *
     */

    
    public static HashString NAME = new HashString ( "NAME" );

    /**
     *
     */
    public static HashString DOMAIN = new HashString ( "DOMAIN" );

    /**
     *
     */
    public static HashString NETNAME = new HashString ( "NETNAME" );

    /**
     *
     */
    public static HashString HUBNAME = new HashString ( "HUBNAME" );

    /**
     *
     */
    public static HashString HUBHOST = new HashString ( "HUBHOST" );

    /**
     *
     */
    public static HashString HUBPASS = new HashString ( "HUBPASS" );

    /**
     *
     */
    public static HashString HUBPORT = new HashString ( "HUBPORT" );

    /**
     *
     */
    public static HashString MASTER = new HashString ( "MASTER" );

    /**
     *
     */
    public static HashString SECRETSALT = new HashString ( "SECRETSALT" );

    /**
     *
     */
    public static HashString STATS = new HashString ( "STATS" );

    /**
     *
     */
    public static HashString WHITELIST = new HashString ( "WHITELIST" );

    /**
     *
     */
    public static HashString PORT = new HashString ( "PORT" );

    /**
     *
     */
    public static HashString PASS = new HashString ( "PASS" );

    /**
     *
     */
    public static HashString DB = new HashString ( "DB" );

    /**
     *
     */
    public static HashString ADDRESS = new HashString ( "ADDRESS" );

    /**
     *
     */
    public static HashString GCOS = new HashString ( "GCOS" );

    /**
     *
     */
    public static HashString WARN = new HashString ( "WARN" );

    /**
     *
     */
    public static HashString ACTION = new HashString ( "ACTION" );

    /**
     *
     */
    public static HashString WARNIP = new HashString ( "WARNIP" );

    /**
     *
     */
    public static HashString WARNRANGE = new HashString ( "WARNRANGE" );

    /**
     *
     */
    public static HashString ACTIONIP = new HashString ( "ACTIONIP" );

    /**
     *
     */
    public static HashString ACTIONRANGE = new HashString ( "ACTIONRANGE" );

    /**
     *
     */
    public static HashString TRIGGERWARN = new HashString ( "TRIGGERWARN" );

    /**
     *
     */
    public static HashString TRIGGERACTION = new HashString ( "TRIGGERACTION" );

    /**
     *
     */
    public static HashString TRIGGERWARNIP = new HashString ( "TRIGGERWARNIP" );

    /**
     *
     */
    public static HashString TRIGGERWARNRANGE = new HashString ( "TRIGGERWARNRANGE" );

    /**
     *
     */
    public static HashString TRIGGERACTIONIP = new HashString ( "TRIGGERACTIONIP" );

    /**
     *
     */
    public static HashString TRIGGERACTIONRANGE = new HashString ( "TRIGGERACTIONRANGE" );

    /**
     *
     */
    public static HashString SNOOPROOTSERV = new HashString ( "SNOOPROOTSERV" );

    /**
     *
     */
    public static HashString SNOOPOPERSERV = new HashString ( "SNOOPOPERSERV" );

    /**
     *
     */
    public static HashString SNOOPNICKSERV = new HashString ( "SNOOPNICKSERV" );

    /**
     *
     */
    public static HashString SNOOPCHANSERV = new HashString ( "SNOOPCHANSERV" );

    /**
     *
     */
    public static HashString SNOOPMEMOSERV = new HashString ( "SNOOPMEMOSERV" );

    /**
     *
     */
    public static HashString SNOOPLOG = new HashString ( "SNOOPLOG" );

    /**
     *
     */
    public static HashString SERVICEUSER = new HashString ( "SERVICEUSER" );

    /**
     *
     */
    public static HashString SERVICEHOST = new HashString ( "SERVICEHOST" );

    /**
     *
     */
    public static HashString SERVICEGCOS = new HashString ( "SERVICEGCOS" );

    /**
     *
     */
    public static HashString MYSQLHOST = new HashString ( "MYSQLHOST" );

    /**
     *
     */
    public static HashString MYSQLUSER = new HashString ( "MYSQLUSER" );

    /**
     *
     */
    public static HashString MYSQLPASS = new HashString ( "MYSQLPASS" );

    /**
     *
     */
    public static HashString MYSQLPORT = new HashString ( "MYSQLPORT" );

    /**
     *
     */
    public static HashString MYSQLDB = new HashString ( "MYSQLDB" );

    /**
     *
     */
    public static HashString LOGFILE = new HashString ( "LOGFILE" );

    /**
     *
     */
    public static HashString EXPIRE = new HashString ( "EXPIRE" );

    /**
     *
     */
    public static HashString FORCEMODES = new HashString ( "FORCEMODES" );

    /**
     *
     */
    public static HashString STRING = new HashString ( "STRING" );

    /**
     *
     */
    public static HashString INTEGER = new HashString ( "INTEGER" );

    /**
     *
     */
    public static HashString BOOLEAN = new HashString ( "BOOLEAN" );

    /**
     *
     */
    public static HashString ERROR = new HashString ( "ERROR" );

    /**
     *
     */
    public static HashString CLOSING = new HashString ( "CLOSING" );

    /**
     *
     */
    public static HashString LINK = new HashString ( "LINK" );

    /**
     *
     */
    public static HashString SERVER = new HashString ( "SERVER" );

    /**
     *
     */
    public static HashString SERVICES = new HashString ( "SERVICES" );

    /**
     *
     */
    public static HashString SJOIN = new HashString ( "SJOIN" );

    /**
     *
     */
    public static HashString SQUIT = new HashString ( "SQUIT" );

    /**
     *
     */
    public static HashString SVINFO = new HashString ( "SVINFO" );

    /**
     *
     */
    public static HashString LUSERSLOCK = new HashString ( "LUSERSLOCK" );

    /**
     *
     */
    public static HashString PING = new HashString ( "PING" );

    /**
     *
     */
    public static HashString NICK = new HashString ( "NICK" );

    /**
     *
     */
    public static HashString PRIVMSG = new HashString ( "PRIVMSG" );

    /**
     *
     */
    public static HashString ROOTSERV = new HashString ( "ROOTSERV" );

    /**
     *
     */
    public static HashString OPERSERV = new HashString ( "OPERSERV" );

    /**
     *
     */
    public static HashString CHANSERV = new HashString ( "CHANSERV" );

    /**
     *
     */
    public static HashString NICKSERV = new HashString ( "NICKSERV" );

    /**
     *
     */
    public static HashString MEMOSERV = new HashString ( "MEMOSERV" );

    /**
     *
     */
    public static HashString GUESTSERV = new HashString ( "GUESTSERV" );

    /**
     *
     */
    public static HashString MODE = new HashString ( "MODE" );

    /**
     *
     */
    public static HashString PART = new HashString ( "PART" );

    /**
     *
     */
    public static HashString KICK = new HashString ( "KICK" );

    /**
     *
     */
    public static HashString QUIT = new HashString ( "QUIT" );

    /**
     *
     */
    public static HashString KILL = new HashString ( "KILL" );

    /**
     *
     */
    public static HashString MOTD = new HashString ( "MOTD" );

    /**
     *
     */
    public static HashString VERSION = new HashString ( "VERSION" );

    /**
     *
     */
    public static HashString INFO = new HashString ( "INFO" );

    /**
     *
     */
    public static HashString OLD = new HashString ( "OLD" );

    /**
     *
     */
    public static HashString SAMODE = new HashString ( "SAMODE" );

    /**
     *
     */
    public static HashString GLOBOPS = new HashString ( "GLOBOPS" );

    /**
     *
     */
    public static HashString SF = new HashString ( "SF" );

    /**
     *
     */
    public static HashString HELP = new HashString ( "HELP" );

    /**
     *
     */
    public static HashString REGISTER = new HashString ( "REGISTER" );

    /**
     *
     */
    public static HashString IDENTIFY = new HashString ( "IDENTIFY" );

    /**
     *
     */
    public static HashString SIDENTIFY = new HashString ( "SIDENTIFY" );

    /**
     *
     */
    public static HashString GHOST = new HashString ( "GHOST" );

    /**
     *
     */
    public static HashString SET = new HashString ( "SET" );

    /**
     *
     */
    public static HashString AUTH = new HashString ( "AUTH" );

    /**
     *
     */
    public static HashString NICKINFO = new HashString ( "NICKINFO" );

    /**
     *
     */
    public static HashString DROP = new HashString ( "DROP" );

    /**
     *
     */
    public static HashString AUTHMAIL = new HashString ( "AUTHMAIL" );

    /**
     *
     */
    public static HashString AUTHPASS = new HashString ( "AUTHPASS" );

    /**
     *
     */
    public static HashString UNSET = new HashString ( "UNSET" );

    /**
     *
     */
    public static HashString EMAIL = new HashString ( "EMAIL" );

    /**
     *
     */
    public static HashString PASSWD = new HashString ( "PASSWD" );

    /**
     *
     */
    public static HashString SETEMAIL = new HashString ( "SETEMAIL" );

    /**
     *
     */
    public static HashString SETPASSWD = new HashString ( "SETPASSWD" );

    /**
     *
     */
    public static HashString ON = new HashString ( "ON" );

    /**
     *
     */
    public static HashString OFF = new HashString ( "OFF" );

    /**
     *
     */
    public static HashString ENFORCE = new HashString ( "ENFORCE" );

    /**
     *
     */
    public static HashString SECURE = new HashString ( "SECURE" );

    /**
     *
     */
    public static HashString PRIVATE = new HashString ( "PRIVATE" );

    /**
     *
     */
    public static HashString NOOP = new HashString ( "NOOP" );

    /**
     *
     */
    public static HashString NEVEROP = new HashString ( "NEVEROP" );

    /**
     *
     */
    public static HashString MAILBLOCK = new HashString ( "MAILBLOCK" );

    /**
     *
     */
    public static HashString SHOWEMAIL = new HashString ( "SHOWEMAIL" );

    /**
     *
     */
    public static HashString SHOWHOST = new HashString ( "SHOWHOST" );

    /**
     *
     */
    public static HashString DELETE = new HashString ( "DELETE" );

    /**
     *
     */
    public static HashString CHANGE = new HashString ( "CHANGE" );

    /**
     *
     */
    public static HashString UINFO = new HashString ( "UINFO" );

    /**
     *
     */
    public static HashString CINFO = new HashString ( "CINFO" );

    /**
     *
     */
    public static HashString NINFO = new HashString ( "NINFO" );

    /**
     *
     */
    public static HashString SINFO = new HashString ( "SINFO" );

    /**
     *
     */
    public static HashString ULIST = new HashString ( "ULIST" );

    /**
     *
     */
    public static HashString CLIST = new HashString ( "CLIST" );

    /**
     *
     */
    public static HashString SLIST = new HashString ( "SLIST" );

    /**
     *
     */
    public static HashString UPTIME = new HashString ( "UPTIME" );

    /**
     *
     */
    public static HashString IGNORE = new HashString ( "IGNORE" );

    /**
     *
     */
    public static HashString ADDMASTER = new HashString ( "ADDMASTER" );

    /**
     *
     */
    public static HashString DELMASTER = new HashString ( "DELMASTER" );

    /**
     *
     */
    public static HashString ADDSRA = new HashString ( "ADDSRA" );

    /**
     *
     */
    public static HashString DELSRA = new HashString ( "DELSRA" );

    /**
     *
     */
    public static HashString ADDCSOP = new HashString ( "ADDCSOP" );

    /**
     *
     */
    public static HashString DELCSOP = new HashString ( "DELCSOP" );

    /**
     *
     */
    public static HashString ADDSA = new HashString ( "ADDSA" );

    /**
     *
     */
    public static HashString DELSA = new HashString ( "DELSA" );

    /**
     *
     */
    public static HashString ADDIRCOP = new HashString ( "ADDIRCOP" );

    /**
     *
     */
    public static HashString DELIRCOP = new HashString ( "DELIRCOP" );

    /**
     *
     */
    public static HashString HUB = new HashString ( "HUB" );

    /**
     *
     */
    public static HashString LEAF = new HashString ( "LEAF" );

    /**
     *
     */
    public static HashString OS = new HashString ( "OS" );

    /**
     *
     */
    public static HashString SFAKILL = new HashString ( "SFAKILL" );

    /**
     *
     */
    public static HashString BAHAMUT = new HashString ( "BAHAMUT" );

    /**
     *
     */
    public static HashString STOP = new HashString ( "STOP" );

    /**
     *
     */
    public static HashString PRIMARY = new HashString ( "PRIMARY" );

    /**
     *
     */
    public static HashString SECONDARY = new HashString ( "SECONDARY" );

    /**
     *
     */
    public static HashString MAKILL = new HashString ( "MAKILL" );

    /**
     *
     */
    public static HashString RESET = new HashString ( "RESET" );

    /**
     *
     */
    public static HashString COMMIT = new HashString ( "COMMIT" );

    /**
     *
     */
    public static HashString REHASH = new HashString ( "REHASH" );

    /**
     *
     */
    public static HashString SVSMODE = new HashString ( "SVSMODE" );

    /**
     *
     */
    public static HashString USER = new HashString ( "USER" );

    /**
     *
     */
    public static HashString REALNAME = new HashString ( "REALNAME" );

    /**
     *
     */
    public static HashString HOST = new HashString ( "HOST" );

    /**
     *
     */
    public static HashString REALHOST = new HashString ( "REALHOST" );

    /**
     *
     */
    public static HashString IP = new HashString ( "IP" );

    /**
     *
     */
    public static HashString SERVICESID = new HashString ( "SERVICESID" );

    /**
     *
     */
    public static HashString OP = new HashString ( "OP" );

    /**
     *
     */
    public static HashString DEOP = new HashString ( "DEOP" );

    /**
     *
     */
    public static HashString OPDEOP = new HashString ( "OPDEOP" );

    /**
     *
     */
    public static HashString VOICE = new HashString ( "VOICE" );

    /**
     *
     */
    public static HashString ALL = new HashString ( "ALL" );

    /**
     *
     */
    public static HashString TOPIC = new HashString ( "TOPIC" );

    /**
     *
     */
    public static HashString AKICK = new HashString ( "AKICK" );

    /**
     *
     */
    public static HashString AOP = new HashString ( "AOP" );

    /**
     *
     */
    public static HashString SOP = new HashString ( "SOP" );

    /**
     *
     */
    public static HashString FOUNDER = new HashString ( "FOUNDER" );

    /**
     *
     */
    public static HashString NONE = new HashString ( "NONE" );

    /**
     *
     */
    public static HashString ADD = new HashString ( "ADD" );

    /**
     *
     */
    public static HashString DEL = new HashString ( "DEL" );

    /**
     *
     */
    public static HashString LIST = new HashString ( "LIST" );

    /**
     *
     */
    public static HashString WIPE = new HashString ( "WIPE" );

    /**
     *
     */
    public static HashString UNBAN = new HashString ( "UNBAN" );

    /**
     *
     */
    public static HashString INVITE = new HashString ( "INVITE" );

    /**
     *
     */
    public static HashString WHY = new HashString ( "WHY" );

    /**
     *
     */
    public static HashString ACCESSLOG = new HashString ( "ACCESSLOG" );

    /**
     *
     */
    public static HashString CHANLIST = new HashString ( "CHANLIST" );

    /**
     *
     */
    public static HashString CHANFLAG = new HashString ( "CHANFLAG" );

    /**
     *
     */
    public static HashString MDEOP = new HashString ( "MDEOP" );

    /**
     *
     */
    public static HashString MKICK = new HashString ( "MKICK" );

    /**
     *
     */
    public static HashString NICK_ACCESS_DENIED = new HashString ( "NICK_ACCESS_DENIED" );

    /**
     *
     */
    public static HashString ADDAOP = new HashString ( "AOP+" );

    /**
     *
     */
    public static HashString DELAOP = new HashString ( "AOP-" );

    /**
     *
     */
    public static HashString ADDSOP = new HashString ( "SOP+" );

    /**
     *
     */
    public static HashString DELSOP = new HashString ( "SOP-" );

    /**
     *
     */
    public static HashString ADDAKICK = new HashString ( "AKICK+" );

    /**
     *
     */
    public static HashString DELAKICK = new HashString ( "AKICK-" );

    /**
     *
     */
    public static HashString KICKBAN = new HashString ( "KICKBAN" );

    /**
     *
     */
    public static HashString LISTACCESS = new HashString ( "LISTACCESS" );

    /**
     *
     */
    public static HashString LISTOPS = new HashString ( "LISTOPS" );

    /**
     *
     */
    public static HashString TYPE = new HashString ( "TYPE" );

    /**
     *
     */
    public static HashString REASON = new HashString ( "REASON" );

    /**
     *
     */
    public static HashString INSTATER = new HashString ( "INSTATER" );

    /**
     *
     */
    public static HashString STAMP = new HashString ( "STAMP" );

    /**
     *
     */
    public static HashString AK = new HashString ( "AK" );

    /**
     *
     */
    public static HashString IG = new HashString ( "IG" );

    /**
     *
     */
    public static HashString SQ = new HashString ( "SQ" );

    /**
     *
     */
    public static HashString SG = new HashString ( "SG" );

    /**
     *
     */
    public static HashString JOIN_CONNECT_TIME = new HashString ( "JOIN_CONNECT_TIME" );

    /**
     *
     */
    public static HashString TALK_CONNECT_TIME = new HashString ( "TALK_CONNECT_TIME" );

    /**
     *
     */
    public static HashString TALK_JOIN_TIME = new HashString ( "TALK_JOIN_TIME" );

    /**
     *
     */
    public static HashString MAX_BANS = new HashString ( "MAX_BANS" );

    /**
     *
     */
    public static HashString MAX_INVITES = new HashString ( "MAX_INVITES" );

    /**
     *
     */
    public static HashString MAX_MSG_TIME = new HashString ( "MAX_MSG_TIME" );

    /**
     *
     */
    public static HashString NO_NOTICE = new HashString ( "NO_NOTICE" );

    /**
     *
     */
    public static HashString NO_CTCP = new HashString ( "NO_CTCP" );

    /**
     *
     */
    public static HashString NO_PART_MSG = new HashString ( "NO_PART_MSG" );

    /**
     *
     */
    public static HashString NO_QUIT_MSG = new HashString ( "NO_QUIT_MSG" );

    /**
     *
     */
    public static HashString EXEMPT_OPPED = new HashString ( "EXEMPT_OPPED" );

    /**
     *
     */
    public static HashString EXEMPT_VOICED = new HashString ( "EXEMPT_VOICED" );

    /**
     *
     */
    public static HashString EXEMPT_IDENTD = new HashString ( "EXEMPT_IDENTD" );

    /**
     *
     */
    public static HashString EXEMPT_REGISTERED = new HashString ( "EXEMPT_REGISTERED" );

    /**
     *
     */
    public static HashString EXEMPT_INVITES = new HashString ( "EXEMPT_INVITES" );

    /**
     *
     */
    public static HashString EXEMPT_WEBIRC = new HashString ( "EXEMPT_WEBIRC" );

    /**
     *
     */
    public static HashString HIDE_MODE_LISTS = new HashString ( "HIDE_MODE_LISTS" );

    /**
     *
     */
    public static HashString NO_NICK_CHANGE = new HashString ( "NO_NICK_CHANGE" );

    /**
     *
     */
    public static HashString NO_UTF8 = new HashString ( "NO_UTF8" );

    /**
     *
     */
    public static HashString GREETMSG = new HashString ( "GREETMSG" );

    /**
     *
     */
    public static HashString ACCSTRING = new HashString ( "ACCSTRING" );

    /**
     *
     */
    public static HashString ACCSTRINGSHORT = new HashString ( "ACCSTRINGSHORT" );

    /**
     *
     */
    public static HashString IRCOP = new HashString ( "IRCOP" );

    /**
     *
     */
    public static HashString SO = new HashString ( "SO" );

    /**
     *
     */
    public static HashString SA = new HashString ( "SA" );

    /**
     *
     */
    public static HashString CSOP = new HashString ( "CSOP" );

    /**
     *
     */
    public static HashString SRA = new HashString ( "SRA" );

    /**
     *
     */
    public static HashString STAFF = new HashString ( "STAFF" );

    /**
     *
     */
    public static HashString AUDITORIUM = new HashString ( "AUDITORIUM" );

    /**
     *
     */
    public static HashString UNAUDITORIUM = new HashString ( "UNAUDITORIUM" );

    /**
     *
     */
    public static HashString MARK = new HashString ( "MARK" );

    /**
     *
     */
    public static HashString UNMARK = new HashString ( "UNMARK" );

    /**
     *
     */
    public static HashString FREEZE = new HashString ( "FREEZE" );

    /**
     *
     */
    public static HashString UNFREEZE = new HashString ( "UNFREEZE" );

    /**
     *
     */
    public static HashString NOGHOST = new HashString ( "NOGHOST" );

    /**
     *
     */
    public static HashString UNNOGHOST = new HashString ( "UNNOGHOST" );

    /**
     *
     */
    public static HashString CLOSE = new HashString ( "CLOSE" );

    /**
     *
     */
    public static HashString REOPEN = new HashString ( "REOPEN" );

    /**
     *
     */
    public static HashString HELD = new HashString ( "HELD" );

    /**
     *
     */
    public static HashString UNHELD = new HashString ( "UNHELD" );

    /**
     *
     */
    public static HashString GETPASS = new HashString ( "GETPASS" );

    /**
     *
     */
    public static HashString GETEMAIL = new HashString ( "GETEMAIL" );

    /**
     *
     */
    public static HashString SENDPASS = new HashString ( "SENDPASS" );

    /**
     *
     */
    public static HashString EXPIREFOUNDER = new HashString ( "EXPIREFOUNDER" );

    /**
     *
     */
    public static HashString EXPIREINACTIVE = new HashString ( "EXPIREINACTIVE" );

    /**
     *
     */
    public static HashString MASSDEOP = new HashString ( "MASSDEOP" );

    /**
     *
     */
    public static HashString MASSKICK = new HashString ( "MASSKICK" );

    /**
     *
     */
    public static HashString SAJOIN = new HashString ( "SAJOIN" );

    /**
     *
     */
    public static HashString TOPICWIPE = new HashString ( "TOPICWIPE" );

    /**
     *
     */
    public static HashString WIPEAOP = new HashString ( "WIPEAOP" );

    /**
     *
     */
    public static HashString WIPESOP = new HashString ( "WIPESOP" );

    /**
     *
     */
    public static HashString WIPEAKICK = new HashString ( "WIPEAKICK" );

    /**
     *
     */
    public static HashString TOPICNICK = new HashString ( "TOPICNICK" );

    /**
     *
     */
    public static HashString TOPICLOG = new HashString ( "TOPICLOG" );

    /**
     *
     */
    public static HashString FORCENICK = new HashString ( "FORCENICK" );

    /**
     *
     */
    public static HashString ENFORCED = new HashString ( "ENFORCED" );

    /**
     *
     */
    public static HashString SECURED = new HashString ( "SECURED" );

    /**
     *
     */
    public static HashString MAILBLOCKED = new HashString ( "MAILBLOCKED" );

    /**
     *
     */
    public static HashString KEEPTOPIC = new HashString ( "KEEPTOPIC" );

    /**
     *
     */
    public static HashString TOPICLOCK = new HashString ( "TOPICLOCK" );

    /**
     *
     */
    public static HashString OPGUARD = new HashString ( "OPGUARD" );

    /**
     *
     */
    public static HashString RESTRICT = new HashString ( "RESTRICT" );

    /**
     *
     */
    public static HashString VERBOSE = new HashString ( "VERBOSE" );

    /**
     *
     */
    public static HashString LEAVEOPS = new HashString ( "LEAVEOPS" );

    /**
     *
     */
    public static HashString AUTOAKICK = new HashString ( "AUTOAKICK" );

    /**
     *
     */
    public static HashString DYNAOP = new HashString ( "DYNAOP" );

    /**
     *
     */
    public static HashString MODELOCK = new HashString ( "MODELOCK" );

    /**
     *
     */
    public static HashString OHELP = new HashString ( "OHELP" );

    /**
     *
     */
    public static HashString AUTHURL = new HashString ( "AUTHURL" );

    /**
     *
     */
    public static HashString AUTHED = new HashString ( "AUTHED" );

    /**
     *
     */
    public static HashString EXPIREAUTH = new HashString ( "EXPIREAUTH" );

    /**
     *
     */
    public static HashString SYNTAX_ERROR = new HashString ( "SYNTAX_ERROR" );

    /**
     *
     */
    public static HashString NICK_NOT_REGGED = new HashString ( "NICK_NOT_REGGED" );

    /**
     *
     */
    public static HashString IDENTIFY_FAIL = new HashString ( "IDENTIFY_FAIL" );

    /**
     *
     */
    public static HashString IDENTIFY_SUCCESS = new HashString ( "IDENTIFY_SUCCESS" );

    /**
     *
     */
    public static HashString NO_SUCH_TARGET = new HashString ( "NO_SUCH_TARGET" );

    /**
     *
     */
    public static HashString IDENTIFY_NICK = new HashString ( "IDENTIFY_NICK" );

    /**
     *
     */
    public static HashString DESCRIPTION = new HashString ( "DESCRIPTION" );

    /**
     *
     */
    public static HashString CLOSED = new HashString ( "CLOSED" );

    /**
     *
     */
    public static HashString MAIL = new HashString ( "MAIL" );

    /**
     *
     */
    public static HashString FULLMASK = new HashString ( "FULLMASK" );

    /**
     *
     */
    public static HashString LASTUSED = new HashString ( "LASTUSED" );

    /**
     *
     */
    public static HashString REGTIME = new HashString ( "REGTIME" );

    /**
     *
     */
    public static HashString SEND = new HashString ( "SEND" );

    /**
     *
     */
    public static HashString CSEND = new HashString ( "CSEND" );

    /**
     *
     */
    public static HashString OSEND = new HashString ( "OSEND" );

    /**
     *
     */
    public static HashString READ = new HashString ( "READ" );

    /**
     *
     */
    public static HashString OINFO = new HashString ( "OINFO" );

    /**
     *
     */
    public static HashString OSET = new HashString ( "OSET" );

    /**
     *
     */
    public static HashString HOLD = new HashString ( "HOLD" );

    /**
     *
     */
    public static HashString UNHOLD = new HashString ( "UNHOLD" );

    /**
     *
     */
    public static HashString FROZEN = new HashString ( "FROZEN" );

    /**
     *
     */
    public static HashString MARKED = new HashString ( "MARKED" );

    /**
     *
     */
    public static HashString IDENT = new HashString ( "IDENT" );

    /**
     *
     */
    public static HashString OPER = new HashString ( "OPER" );

    /**
     *
     */
    public static HashString ADMIN = new HashString ( "ADMIN" );

    /**
     *
     */
    public static HashString SADMIN = new HashString ( "SADMIN" );

    /**
     *
     */
    public static HashString SRAW = new HashString ( "SRAW" );

    /**
     *
     */
    public static HashString PANIC = new HashString ( "PANIC" );

    /**
     *
     */
    public static HashString AKILL = new HashString ( "AKILL" );

    /**
     *
     */
    public static HashString AUTOKILL = new HashString ( "AUTOKILL" );

    /**
     *
     */
    public static HashString TIME = new HashString ( "TIME" );

    /**
     *
     */
    public static HashString SEARCHLOG = new HashString ( "SEARCHLOG" );

    /**
     *
     */
    public static HashString AUDIT = new HashString ( "AUDIT" );

    /**
     *
     */
    public static HashString FULL = new HashString ( "FULL" );

    /**
     *
     */
    public static HashString COMMENT = new HashString ( "COMMENT" );

    /**
     *
     */
    public static HashString GLOBAL = new HashString ( "GLOBAL" );

    /**
     *
     */
    public static HashString BANLOG = new HashString ( "BANLOG" );

    /**
     *
     */
    public static HashString SQLINE = new HashString ( "SQLINE" );

    /**
     *
     */
    public static HashString SGLINE = new HashString ( "SGLINE" );

    /**
     *
     */
    public static HashString SPAMFILTER = new HashString ( "SPAMFILTER" );

    /**
     *
     */
    public static HashString JUPE = new HashString ( "JUPE" );

    /**
     *
     */
    public static HashString MISSING = new HashString ( "MISSING" );

    /**
     *
     */
    public static HashString s = new HashString ( "s" );

    /**
     *
     */
    public static HashString S = new HashString ( "S" );

    /**
     *
     */
    public static HashString r = new HashString ( "r" );

    /**
     *
     */
    public static HashString m = new HashString ( "m" );

    /**
     *
     */
    public static HashString p = new HashString ( "p" );

    /**
     *
     */
    public static HashString n = new HashString ( "n" );

    /**
     *
     */
    public static HashString k = new HashString ( "k" );

    /**
     *
     */
    public static HashString q = new HashString ( "q" );

    /**
     *
     */
    public static HashString t = new HashString ( "t" );

    /**
     *
     */
    public static HashString a = new HashString ( "a" );

    /**
     *
     */
    public static HashString c = new HashString ( "c" );

    /**
     *
     */
    public static HashString P = new HashString ( "P" );

    /**
     *
     */
    public static HashString W = new HashString ( "W" );

    /**
     *
     */
    public static HashString L = new HashString ( "L" );

    /**
     *
     */
    public static HashString R = new HashString ( "R" );

    /**
     *
     */
    public static HashString B = new HashString ( "B" );

    /**
     *
     */
    public static HashString K = new HashString ( "K" );

    /**
     *
     */
    public static HashString A = new HashString ( "A" );

    /**
     *
     */
    public static HashString NUM_1 = new HashString ( "1" );

    /**
     *
     */
    public static HashString NUM_2 = new HashString ( "2" );

    /**
     *
     */
    public static HashString NUM_3 = new HashString ( "3" );

    /**
     *
     */
    public static HashString NUM_4 = new HashString ( "4" );

    /**
     *
     */
    public static HashString RAW = new HashString ( "RAW" );

    /**
     *
     */
    public static HashString SYNTAX_REG_ERROR = new HashString ( "SYNTAX_REG_ERROR" );

    /**
     *
     */
    public static HashString INVALID_EMAIL = new HashString ( "INVALID_EMAIL" );

    /**
     *
     */
    public static HashString NICK_ALREADY_REGGED = new HashString ( "NICK_ALREADY_REGGED" );

    /**
     *
     */
    public static HashString INVALID_NICK = new HashString ( "INVALID_NICK" );

    /**
     *
     */
    public static HashString SYNTAX_ID_ERROR = new HashString ( "SYNTAX_ID_ERROR" );

    /**
     *
     */
    public static HashString NICK_NOT_REGISTERED = new HashString ( "NICK_NOT_REGISTERED" );

    /**
     *
     */
    public static HashString PASSWD_ERROR = new HashString ( "PASSWD_ERROR" );

    /**
     *
     */
    public static HashString IS_FROZEN = new HashString ( "IS_FROZEN" );

    /**
     *
     */
    public static HashString IS_THROTTLED = new HashString ( "IS_THROTTLED" );

    /**
     *
     */
    public static HashString IS_MARKED = new HashString ( "IS_MARKED" );

    /**
     *
     */
    public static HashString ACCESS_DENIED_OPER = new HashString ( "ACCESS_DENIED_OPER" );

    /**
     *
     */
    public static HashString ACCESS_DENIED_SA = new HashString ( "ACCESS_DENIED_SA" );

    /**
     *
     */
    public static HashString SYNTAX_GHOST_ERROR = new HashString ( "SYNTAX_GHOST_ERROR" );

    /**
     *
     */
    public static HashString NO_SUCH_NICK = new HashString ( "NO_SUCH_NICK" );

    /**
     *
     */
    public static HashString IS_NOGHOST = new HashString ( "IS_NOGHOST" );

    /**
     *
     */
    public static HashString NO_AUTH_FOUND = new HashString ( "NO_AUTH_FOUND" );

    /**
     *
     */
    public static HashString ACCESS_DENIED = new HashString ( "ACCESS_DENIED" );

    /**
     *
     */
    public static HashString SETTING_NOT_FOUND = new HashString ( "SETTING_NOT_FOUND" );

    /**
     *
     */
    public static HashString CMD_NOT_FOUND_ERROR = new HashString ( "CMD_NOT_FOUND_ERROR" );

    /**
     *
     */
    public static HashString NICK_NEW_MASK = new HashString ( "NICK_NEW_MASK" );

    /**
     *
     */
    public static HashString SHOW_HELP = new HashString ( "SHOW_HELP" );

    /**
     *
     */
    public static HashString REGISTER_SEC = new HashString ( "REGISTER_SEC" );

    /**
     *
     */
    public static HashString REGISTER_DONE = new HashString ( "REGISTER_DONE" );

    /**
     *
     */
    public static HashString PASSWD_ACCEPTED = new HashString ( "PASSWD_ACCEPTED" );

    /**
     *
     */
    public static HashString NICKDROPPED = new HashString ( "NICKDROPPED" );

    /**
     *
     */
    public static HashString NICKDELETED = new HashString ( "NICKDELETED" );

    /**
     *
     */
    public static HashString GLOB_IS_NOGHOST = new HashString ( "GLOB_IS_NOGHOST" );

    /**
     *
     */
    public static HashString NICKFLAG_EXIST = new HashString ( "NICKFLAG_EXIST" );

    /**
     *
     */
    public static HashString NICK_SET_FLAG = new HashString ( "NICK_SET_FLAG" );

    /**
     *
     */
    public static HashString NICK_GETEMAIL = new HashString ( "NICK_GETEMAIL" );

    /**
     *
     */
    public static HashString INVALID_PASS = new HashString ( "INVALID_PASS" );

    /**
     *
     */
    public static HashString NICK_IS_NOW = new HashString ( "NICK_IS_NOW" );

    /**
     *
     */
    public static HashString NICK_AUTHED = new HashString ( "NICK_AUTHED" );

    /**
     *
     */
    public static HashString NICK_IS_NOT = new HashString ( "NICK_IS_NOT" );

    /**
     *
     */
    public static HashString DB_ERROR = new HashString ( "DB_ERROR" );

    /**
     *
     */
    public static HashString IDENT_NICK_DELETED = new HashString ( "IDENT_NICK_DELETED" );

    /**
     *
     */
    public static HashString NICK_DELETED = new HashString ( "NICK_DELETED" );

    /**
     *
     */
    public static HashString ACCESS_DENIED_SRA = new HashString ( "ACCESS_DENIED_SRA" );

    /**
     *
     */
    public static HashString ACCESS_DENIED_DELETE_OPER = new HashString ( "ACCESS_DENIED_DELETE_OPER" );

    /**
     *
     */
    public static HashString NICK_GETPASS = new HashString ( "NICK_GETPASS" );

    /**
     *
     */
    public static HashString NOT_ENOUGH_ACCESS = new HashString ( "NOT_ENOUGH_ACCESS" );

    /**
     *
     */
    public static HashString NICK_NOT_AUTHED = new HashString ( "NICK_NOT_AUTHED" );

    /**
     *
     */
    public static HashString NICK_NOT_IDENTIFIED = new HashString ( "NICK_NOT_IDENTIFIED" );

    /**
     *
     */
    public static HashString CHAN_NOT_REGISTERED = new HashString ( "CHAN_NOT_REGISTERED" );

    /**
     *
     */
    public static HashString CHAN_ALREADY_REGGED = new HashString ( "CHAN_ALREADY_REGGED" );

    /**
     *
     */
    public static HashString CHAN_WHY = new HashString ( "CHAN_WHY" );

    /**
     *
     */
    public static HashString CHAN_NOT_EXIST = new HashString ( "CHAN_NOT_EXIST" );

    /**
     *
     */
    public static HashString NICK_NOT_EXIST = new HashString ( "NICK_NOT_EXIST" );

    /**
     *
     */
    public static HashString USER_NOT_ONLINE = new HashString ( "USER_NOT_ONLINE" );

    /**
     *
     */
    public static HashString USER_NOT_OP = new HashString ( "USER_NOT_OP" );

    /**
     *
     */
    public static HashString NICK_HAS_NOOP = new HashString ( "NICK_HAS_NOOP" );

    /**
     *
     */
    public static HashString WILL_NOW = new HashString ( "WILL_NOW" );

    /**
     *
     */
    public static HashString WILL_NOW_NOT = new HashString ( "WILL_NOW_NOT" );

    /**
     *
     */
    public static HashString IS_NOW = new HashString ( "IS_NOW" );

    /**
     *
     */
    public static HashString IS_NOT = new HashString ( "IS_NOT" );

    /**
     *
     */
    public static HashString INVALID_PASSWORD = new HashString ( "INVALID_PASSWORD" );

    /**
     *
     */
    public static HashString DB_NICK_ERROR = new HashString ( "DB_NICK_ERROR" );

    /**
     *
     */
    public static HashString NICK_IS_FOUNDER = new HashString ( "NICK_IS_FOUNDER" );

    /**
     *
     */
    public static HashString NICK_IS_SOP = new HashString ( "NICK_IS_SOP" );

    /**
     *
     */
    public static HashString NICK_IS_OP = new HashString ( "NICK_IS_OP" );

    /**
     *
     */
    public static HashString NICK_NOT_FOUND = new HashString ( "NICK_NOT_FOUND" );

    /**
     *
     */
    public static HashString NICK_CHANGED = new HashString ( "NICK_CHANGED" );

    /**
     *
     */
    public static HashString NICK_NOT_PRESENT = new HashString ( "NICK_NOT_PRESENT" );

    /**
     *
     */
    public static HashString NICK_NOT_IDENTED_OP = new HashString ( "NICK_NOT_IDENTED_OP" );

    /**
     *
     */
    public static HashString NICK_NEVEROP = new HashString ( "NICK_NEVEROP" );

    /**
     *
     */
    public static HashString NICK_ADDED = new HashString ( "NICK_ADDED" );

    /**
     *
     */
    public static HashString NICK_NOT_ADDED = new HashString ( "NICK_NOT_ADDED" );

    /**
     *
     */
    public static HashString NICK_NOT_DELETED = new HashString ( "NICK_NOT_DELETED" );

    /**
     *
     */
    public static HashString NICK_VERBOSE_ADDED = new HashString ( "NICK_VERBOSE_ADDED" );

    /**
     *
     */
    public static HashString NICK_VERBOSE_DELETED = new HashString ( "NICK_VERBOSE_DELETED" );

    /**
     *
     */
    public static HashString NICK_INVITED = new HashString ( "NICK_INVITED" );

    /**
     *
     */
    public static HashString NICK_OP = new HashString ( "NICK_OP" );

    /**
     *
     */
    public static HashString NICK_DEOP = new HashString ( "NICK_DEOP" );

    /**
     *
     */
    public static HashString NICK_VERBOSE_OP = new HashString ( "NICK_VERBOSE_OP" );

    /**
     *
     */
    public static HashString NICK_VERBOSE_DEOP = new HashString ( "NICK_VERBOSE_DEOP" );

    /**
     *
     */
    public static HashString NICK_MDEOP_CHAN = new HashString ( "NICK_MDEOP_CHAN" );

    /**
     *
     */
    public static HashString NICK_MDEOP = new HashString ( "NICK_MDEOP" );

    /**
     *
     */
    public static HashString NICK_MKICK_CHAN = new HashString ( "NICK_MKICK_CHAN" );

    /**
     *
     */
    public static HashString NICK_MKICK = new HashString ( "NICK_MKICK" );

    /**
     *
     */
    public static HashString LIST_NOT_WIPED = new HashString ( "LIST_NOT_WIPED" );

    /**
     *
     */
    public static HashString LIST_WIPED = new HashString ( "LIST_WIPED" );

    /**
     *
     */
    public static HashString LIST_VERBOSE_WIPED = new HashString ( "LIST_VERBOSE_WIPED" );

    /**
     *
     */
    public static HashString CHAN_IS_FROZEN = new HashString ( "CHAN_IS_FROZEN" );

    /**
     *
     */
    public static HashString CHAN_IS_CLOSED = new HashString ( "CHAN_IS_CLOSED" );

    /**
     *
     */
    public static HashString CHAN_IS_RELAY = new HashString ( "CHAN_IS_RELAY" );

    /**
     *
     */
    public static HashString CHAN_SET_FLAG = new HashString ( "CHAN_SET_FLAG" );

    /**
     *
     */
    public static HashString CHANFLAG_EXIST = new HashString ( "CHANFLAG_EXIST" );

    /**
     *
     */
    public static HashString ALREADY_ON_LIST = new HashString ( "ALREADY_ON_LIST" );

    /**
     *
     */
    public static HashString CHAN_GETPASS = new HashString ( "CHAN_GETPASS" );

    /**
     *
     */
    public static HashString SHOWACCESSLOG = new HashString ( "SHOWACCESSLOG" );

    /**
     *
     */
    public static HashString SHOWACCESSLOGOPER = new HashString ( "SHOWACCESSLOGOPER" );

    /**
     *
     */
    public static HashString SHOWTOPICLOG = new HashString ( "SHOWTOPICLOG" );

    /**
     *
     */
    public static HashString SHOW_LIST = new HashString ( "SHOW_LIST" );

    /**
     *
     */
    public static HashString CHANNELDROPPED = new HashString ( "CHANNELDROPPED" );

    /**
     *
     */
    public static HashString CHANNELDELETED = new HashString ( "CHANNELDELETED" );

    /**
     *
     */
    public static HashString NO_SUCH_CHANFLAG = new HashString ( "NO_SUCH_CHANFLAG" );

    /**
     *
     */
    public static HashString BAD_CHANFLAG_VALUE = new HashString ( "BAD_CHANFLAG_VALUE" );

    /**
     *
     */
    public static HashString XOP_NOT_FOUND = new HashString ( "XOP_NOT_FOUND" );

    /**
     *
     */
    public static HashString XOP_ADD_FAIL = new HashString ( "XOP_ADD_FAIL" );

    /**
     *
     */
    public static HashString XOP_ALREADY_PRESENT = new HashString ( "XOP_ALREADY_PRESENT" );

    /**
     *
     */
    public static HashString CHAN_INFO = new HashString ( "CHAN_INFO" );

    /**
     *
     */
    public static HashString CHAN_UNBAN = new HashString ( "CHAN_UNBAN" );

    /**
     *
     */
    public static HashString ACCESS_LIST = new HashString ( "ACCESS_LIST" );

    /**
     *
     */
    public static HashString ACCESS_ADDED = new HashString ( "ACCESS_ADDED" );

    /**
     *
     */
    public static HashString ACCESS_DELETED = new HashString ( "ACCESS_DELETED" );

    /**
     *
     */
    public static HashString SET_DESCRIPTION = new HashString ( "SET_DESCRIPTION" );

    /**
     *
     */
    public static HashString SET_TOPICLOCK = new HashString ( "SET_TOPICLOCK" );

    /**
     *
     */
    public static HashString SET_MODELOCK = new HashString ( "SET_MODELOCK" );

    /**
     *
     */
    public static HashString SET_KEEPTOPIC = new HashString ( "SET_KEEPTOPIC" );

    /**
     *
     */
    public static HashString SET_IDENT = new HashString ( "SET_IDENT" );

    /**
     *
     */
    public static HashString SET_OPGUARD = new HashString ( "SET_OPGUARD" );

    /**
     *
     */
    public static HashString SET_RESTRICT = new HashString ( "SET_RESTRICT" );

    /**
     *
     */
    public static HashString SET_VERBOSE = new HashString ( "SET_VERBOSE" );

    /**
     *
     */
    public static HashString SET_MAILBLOCK = new HashString ( "SET_MAILBLOCK" );

    /**
     *
     */
    public static HashString SET_LEAVEOPS = new HashString ( "SET_LEAVEOPS" );

    /**
     *
     */
    public static HashString SET_AUTOAKICK = new HashString ( "SET_AUTOAKICK" );

    /**
     *
     */
    public static HashString SET_DYNAOP = new HashString ( "SET_DYNAOP" );

    /**
     *
     */
    public static HashString ADDACCLIST = new HashString ( "ADDACCLIST" );

    /**
     *
     */
    public static HashString REMACCLIST = new HashString ( "REMACCLIST" );

    /**
     *
     */
    public static HashString UPDACCLIST = new HashString ( "UPDACCLIST" );

    /**
     *
     */
    public static HashString PASS_AUTHED = new HashString ( "PASS_AUTHED" );

    /**
     *
     */
    public static HashString PASS_AUTHED_UNIDENT = new HashString ( "PASS_AUTHED_UNIDENT" );
    
    /**
     *
     */
    public static HashString NICKREG_BODY = new HashString ( "NICKREG_BODY" );

    /**
     *
     */
    public static HashString NICKREG_SUBJECT = new HashString ( "NICKREG_SUBJECT" );

    /**
     *
     */
    public static HashString NEWMEMO_BODY = new HashString ( "NEWMEMO_BODY" );

    /**
     *
     */
    public static HashString NEWMEMO_SUBJECT = new HashString ( "NEWMEMO_SUBJECT" );

    /**
     *
     */
    public static HashString EXPNICK_BODY = new HashString ( "EXPNICK_BODY" );

    /**
     *
     */
    public static HashString EXPNICK_SUBJECT = new HashString ( "EXPNICK_SUBJECT" );
    
    /**
     *
     */
    public static HashString BAN_ADD = new HashString ( "BAN_ADD" );

    /**
     *
     */
    public static HashString BAN_ADD_GLOB = new HashString ( "BAN_ADD_GLOB" );

    /**
     *
     */
    public static HashString BAN_EXIST = new HashString ( "BAN_EXIST" );

    /**
     *
     */
    public static HashString BAN_TIME  = new HashString ( "BAN_TIME" );

    /**
     *
     */
    public static HashString BAN_TIME_GLOB = new HashString ( "BAN_TIME_GLOB" );

    /**
     *
     */
    public static HashString BAN_NO_EXIST = new HashString ( "BAN_NO_EXIST" );

    /**
     *
     */
    public static HashString BAN_DEL = new HashString ( "BAN_DEL" );

    /**
     *
     */
    public static HashString BAN_DEL_GLOB = new HashString ( "BAN_DEL_GLOB" );

    /**
     *
     */
    public static HashString BAN_LIST = new HashString ( "BAN_LIST" );

    /**
     *
     */
    public static HashString BAN_LIST_START = new HashString ( "BAN_LIST_START" );

    /**
     *
     */
    public static HashString BAN_LIST_STOP = new HashString ( "BAN_LIST_STOP" );

    /**
     *
     */
    public static HashString BAN_TIME_ERROR = new HashString ( "BAN_TIME_ERROR" );

    /**
     *
     */
    public static HashString BAN_MATCH_OPER = new HashString ( "BAN_MATCH_OPER" );

    /**
     *
     */
    public static HashString AKILL_LIST_NONE = new HashString ( "AKILL_LIST_NONE" );

    /**
     *
     */
    public static HashString WHITELISTED = new HashString ( "WHITELISTED" );

    /**
     *
     */
    public static HashString STAFF_ADD = new HashString ( "STAFF_ADD" );

    /**
     *
     */
    public static HashString STAFF_NOT_ADD = new HashString ( "STAFF_NOT_ADD" );

    /**
     *
     */
    public static HashString STAFF_DEL = new HashString ( "STAFF_DEL" );

    /**
     *
     */
    public static HashString STAFF_NOT_DEL = new HashString ( "STAFF_NOT_DEL" );

    /**
     *
     */
    public static HashString NICK_NOW_STAFF = new HashString ( "NICK_NOW_STAFF" );

    /**
     *
     */
    public static HashString NICK_NO_LONGER_STAFF = new HashString ( "NICK_NO_LONGER_STAFF" );

    /**
     *
     */
    public static HashString GLOB_STAFF_ADD = new HashString ( "GLOB_STAFF_ADD" );

    /**
     *
     */
    public static HashString GLOB_STAFF_DEL = new HashString ( "GLOB_STAFF_DEL" );

    /**
     *
     */
    public static HashString SHOWLOG = new HashString ( "SHOWLOG" );

    /**
     *
     */
    public static HashString SHOWSNOOPLOG = new HashString ( "SHOWSNOOPLOG" );

    /**
     *
     */
    public static HashString SHOWAUDIT = new HashString ( "SHOWAUDIT" );

    /**
     *
     */
    public static HashString SHOWAUDITGLOBAL = new HashString ( "SHOWAUDITGLOBAL" );

    /**
     *
     */
    public static HashString SHOWCOMMENT = new HashString ( "SHOWCOMMENT" );

    /**
     *
     */
    public static HashString ADD_COMMENT = new HashString ( "ADD_COMMENT" );

    /**
     *
     */
    public static HashString SUB_SYNTAX_ERROR = new HashString ( "SUB_SYNTAX_ERROR" );

    /**
     *
     */
    public static HashString SUB2_SYNTAX_ERROR = new HashString ( "SUB2_SYNTAX_ERROR" );

    /**
     *
     */
    public static HashString SHOWLIST = new HashString ( "SHOWLIST" );

    /**
     *
     */
    public static HashString SHOWBANLOG = new HashString ( "SHOWBANLOG" );

    /**
     *
     */
    public static HashString BADFLAGS = new HashString ( "BADFLAGS" );

    /**
     *
     */
    public static HashString FILTER_EXISTS = new HashString ( "FILTER_EXISTS" );

    /**
     *
     */
    public static HashString SPAMFILTER_LIST = new HashString ( "SPAMFILTER_LIST" );

    /**
     *
     */
    public static HashString BADTIME = new HashString ( "BADTIME" );

    /**
     *
     */
    public static HashString BADREASON = new HashString ( "BADREASON" );

    /**
     *
     */
    public static HashString ADDED_MAKILL = new HashString ( "ADDED_MAKILL" );

    /**
     *
     */
    public static HashString RESET_MAKILL = new HashString ( "RESET_MAKILL" );

    /**
     *
     */
    public static HashString MAKILL_ADD_GLOB = new HashString ( "MAKILL_ADD_GLOB" );

    /**
     *
     */
    public static HashString MAKILL_NOBAN_GLOB = new HashString ( "MAKILL_NOBAN_GLOB" );

    /**
     *
     */
    public static HashString MAKILL_DUPLICATE = new HashString ( "MAKILL_DUPLICATE" );

    /**
     *
     */
    public static HashString NICK_ALREADY_PRESENT = new HashString ( "NICK_ALREADY_PRESENT" );

    /**
     *
     */
    public static HashString NICK_IS_OPER = new HashString ( "NICK_IS_OPER" );

    /**
     *
     */
    public static HashString SYNTAX_ERROR_DEL = new HashString ( "SYNTAX_ERROR_DEL" );

    /**
     *
     */
    public static HashString SYNTAX_ERROR_ADD = new HashString ( "SYNTAX_ERROR_ADD" );

    /**
     *
     */
    public static HashString SYNTAX_ERROR_INFO = new HashString ( "SYNTAX_ERROR_INFO" );

    /**
     *
     */
    public static HashString SYNTAX_ERROR_LIST = new HashString ( "SYNTAX_ERROR_LIST" );

    /**
     *
     */
    public static HashString SYNTAX_ERROR_SET = new HashString ( "SYNTAX_ERROR_SET" );

    /**
     *
     */
    public static HashString NO_SUCH_SERVER = new HashString ( "NO_SUCH_SERVER" );

    /**
     *
     */
    public static HashString SHOWCONFIG = new HashString ( "SHOWCONFIG" );
    
    /**
     *
     */
    public static HashString SRA_ADD          = new HashString ( "SRA_ADD" );

    /**
     *
     */
    public static HashString SRA_NOT_ADD      = new HashString ( "SRA_NOT_ADD" );

    /**
     *
     */
    public static HashString SRA_DEL          = new HashString ( "SRA_DEL" );

    /**
     *
     */
    public static HashString SRA_NOT_DEL      = new HashString ( "SRA_NOT_DEL" );
    
    /**
     *
     */
    public static HashString GLOB_SRA_ADD     = new HashString ( "GLOB_SRA_ADD" );

    /**
     *
     */
    public static HashString GLOB_SRA_DEL     = new HashString ( "GLOB_SRA_DEL" );

    /**
     *
     */
    public static final int MODE_PLUS                   = 43;

    /**
     *
     */
    public static final int MODE_MINUS                  = 45;

    /**
     *
     */
    public static final int MODE_r                      = 114;

    /**
     *
     */
    public static final int MODE_R                      = 82;

    /**
     *
     */
    public static final int MODE_t                      = 116;

    /**
     *
     */
    public static final int MODE_n                      = 110;

    /**
     *
     */
    public static final int MODE_i                      = 105;

    /**
     *
     */
    public static final int MODE_k                      = 107;

    /**
     *
     */
    public static final int MODE_s                      = 115;

    /**
     *
     */
    public static final int MODE_p                      = 112;

    /**
     *
     */
    public static final int MODE_M                      = 77;

    /**
     *
     */
    public static final int MODE_l                      = 108;

    /**
     *
     */
    public static final int MODE_j                      = 106;

    /**
     *
     */
    public static final int MODE_c                      = 99;

    /**
     *
     */
    public static final int MODE_O                      = 79;

    /**
     *
     */
    public static final int MODE_m                      = 109;

    /**
     *
     */
    public static final int MODE_a                      = 97;

    /**
     *
     */
    public static final int MODE_A                      = 65;

    /**
     *
     */
    public static final int MODE_o                      = 111;

    /**
     *
     */
    public static final int MODE_v                      = 118;

        /* CHANNEL */

    /**
     *
     */

    public boolean mode_r;         /* mode r */

    /**
     *
     */
    public boolean mode_R;         /* mode R */

    /**
     *
     */
    public boolean mode_t;         /* mode t */

    /**
     *
     */
    public boolean mode_n;         /* mode n */

    /**
     *
     */
    public boolean mode_i;         /* mode i */

    /**
     *
     */
    public boolean mode_k;         /* mode k word */

    /**
     *
     */
    public boolean mode_s;         /* mode s */

    /**
     *
     */
    public boolean mode_p;         /* mode p */

    /**
     *
     */
    public boolean mode_M;         /* mode M */

    /**
     *
     */
    public boolean mode_l;         /* mode l # */

    /**
     *
     */
    public boolean mode_j;         /* mode j #:# */

    /**
     *
     */
    public boolean mode_c;         /* mode c */

    /**
     *
     */
    public boolean mode_O;         /* mode O */

    /**
     *
     */
    public boolean mode_m;         /* mode m */


    /* EXECUTOR */

    /**
     *
     */

    public static HashString U  = new HashString ( "U" );

    /**
     *
     */
    public static HashString O  = new HashString ( "O" );

    /**
     *
     */
    public static HashString h  = new HashString ( "h" );

    /**
     *
     */
    public static HashString d  = new HashString ( "d" );

    /**
     *
     */
    public static HashString y  = new HashString ( "y" );

    /**
     *
     */
    public final static HashString TRUE = new HashString ( "TRUE" );

    /**
     *
     * @param str1
     * @param str2
     * @return bool
     */
    public boolean iCmp ( String str1, String str2 )  {
        return  ( str1.equalsIgnoreCase ( str2 ) );
    }
}
