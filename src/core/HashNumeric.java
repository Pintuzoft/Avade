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
    public final static int NAME                        = 2388619;
    public final static int DOMAIN                      = 2022099140;
    public final static int NETNAME                     = -1733781112;
    public final static int CONNNAME                     = 1972574432;
    public final static int CONNHOST                     = 1972409341;
    public final static int CONNPASS                     = 1972634214;
    public final static int CONNPORT                     = 1972647638;
    public final static int MASTER                      = -2027938206;
    public final static int SECRETSALT                  = -965467386;
    public final static int STATS                       = 79219839;
    public final static int WHITELIST                   = 1132744743;
    public final static int PORT                        = 2461825;
    public final static int PASS                        = 2448401;
    public final static int DB                          = 2174;
    public final static int ADDRESS                     = -429709356;
    public final static int GCOS                        = 2182080;
    public final static int WARN                        = 2656902;
    public final static int ACTION                      = 1925345846;
    public final static int WARNIP                      = -1741682131;
    public final static int WARNRANGE                   = 1055480407;
    public final static int ACTIONIP                     = -873544227;
    public final static int ACTIONRANGE                  = -541278041;
    public final static int TRIGGERWARN                 = 1473044510;
    public final static int TRIGGERACTION               = 1923596750;
    public final static int TRIGGERWARNIP               = -1743431227;
    public final static int TRIGGERWARNRANGE            = 487769023;
    public final static int TRIGGERACTIONIP             = 1740541813;
    public final static int TRIGGERACTIONRANGE          = -651071473;
    
    public final static int SNOOPROOTSERV               = 2080194861;
    public final static int SNOOPOPERSERV               = 1746096281;
    public final static int SNOOPNICKSERV               = -1977995474;
    public final static int SNOOPCHANSERV               = -616064099;
    public final static int SNOOPMEMOSERV               = 1609099653;
    public final static int SNOOPLOG                    = 1467300047;
    
    public final static int SERVICEUSER                 = 225567712;
    public final static int SERVICEHOST                 = 225177021;
    public final static int SERVICEGCOS                 = 225135573;

    public final static int MYSQLHOST                   = 1795990826;
    public final static int MYSQLUSER                   = 1796381517;
    public final static int MYSQLPASS                   = 1796215699;
    public final static int MYSQLPORT                   = 1796229123;
    public final static int MYSQLDB                     = -2049525632;

    public final static int LOGFILE                     = 1060233888;
 
    public final static int EXPIRE                      = 2059137311;
    public final static int FORCEMODES                  = 632649509;
    public final static int YES                         = 87751;

    /* ERROR */
    public final static int ERROR                       = 66247144;
    public final static int CLOSING                     = 1584523413;
    public final static int LINK                        = 2336762;
    
    /* HANDLER */
    public static final int SERVER                      = -1852497085;
    public static final int SERVICES                    = -2133131170;
    public static final int SJOIN                       = 78935037;
    public static final int SQUIT                       = 79149346;
    public static final int SVINFO                      = -1837073007;
    public static final int LUSERSLOCK                  = 1094235207;
    public static final int PING                        = 2455922;
    public static final int NICK                        = 2396003;
    public static final int PRIVMSG                     = 403496530;
    public static final int ROOTSERV                    = 616693752;
    public static final int OPERSERV                    = 282595172;
    public static final int CHANSERV                    = -2079565208;
    public static final int NICKSERV                    = 853470713;
    public static final int MEMOSERV                    = 145598544;
    public static final int GUESTSERV                   = 1899323854;
    public static final int MODE                        = 2372003;
    public static final int PART                        = 2448371;
    public static final int KICK                        = 2306630;
    public static final int QUIT                        = 2497103;
    public static final int KILL                        = 2306910; 
    public static final int MOTD                        = 2372498;
    public static final int VERSION                     = 1069590712;
    public static final int INFO                        = 2251950;
    public static final int OLD                         = 78343;
    public static final int SAMODE                      = -1856346895;
    public static final int GLOBOPS                     = 839070234;
    public static final int SF                          = 2643;

    /* NICKSERV */
    public final static int HELP                        = 2213697;
    public final static int REGISTER                    = 92413603;
    public final static int IDENTIFY                    = 646864652;
    public final static int SIDENTIFY                   = 951999583;
    public final static int GHOST                       = 67793519;
    public final static int SET                         = 81986;
    public final static int AUTH                        = 2020776;
    public final static int NICKINFO                    = 853181073;
    public final static int DROP                        = 2107119;
    
    public final static int AUTHMAIL                    = -2079342753;
    public final static int AUTHPASS                    = -2079253063;

    public final static int UNSET                       = 80904969;
    public final static int EMAIL                       = 66081660;
    public final static int PASSWD                      = -1942051170;
    public final static int SETEMAIL                    = -2091455366;
    public final static int SETPASSWD                   = -106222240;
    
    public static final int ON                          = 2527;
    public static final int OFF                         = 78159;
    
    public static final int ENFORCE                     = -886600766;
    public static final int SECURE                      = -1852944521;
    public static final int PRIVATE                     = 403485027;
    public static final int NOOP                        = 2402146;
    public static final int NEVEROP                     = -1732185779;
    public static final int MAILBLOCK                   = -1814116074;
    public static final int SHOWEMAIL                   = 888592031;
    public static final int SHOWHOST                    = 444398117;
    public static final int DELETE                      = 2012838315;
    public static final int CHANGE                      = 1986660272;
    
    /* OPERSERV */
    public static final int UINFO                       = 80751235;
    public static final int CINFO                       = 64127857;
    public static final int SINFO                       = 78904193;
    public static final int ULIST                       = 80836211;
    public static final int CLIST                       = 64212833;
    public static final int SLIST                       = 78989169;
    public static final int UPTIME                      = -1785032728;
    public static final int IGNORE                      = -2137067054;

    public static final int ADDMASTER                   = -780667357;
    public static final int DELMASTER                   = -1655158259;
    public static final int ADDSRA                      = 1925802401;
    public static final int DELSRA                      = 2012851703;
    public static final int ADDCSOP                     = -430142894;
    public static final int DELCSOP                     = -2026581828;
    public static final int ADDSA                       = 62122639;
    public static final int DELSA                       = 64930681;
    public static final int ADDIRCOP                    = -444027974;
    public static final int DELIRCOP                    = 1605972624;
    public static final int HUB                         = 71893;
    public static final int LEAF                        = 2332510;
    public static final int OS                          = 2532;
    public static final int SFAKILL                     = -1580229460;
    public static final int BAHAMUT                     = 375102260;
    public static final int STOP                        = 2555906;
    public static final int PRIMARY                     = 403216866;
    public static final int SECONDARY                   = 1968996692;
    public static final int MAKILL                      = -2028186894;
    public static final int RESET                       = 77866287;
    public static final int COMMIT                      = 1993481527;
    
    
    /* ROOTSERV */
    public static final int REHASH                      = -1881443903;

    /* SERVER */
    public static final int SVSMODE                     = -1105474573;

    /* USER */
    public final static int USER                        = 2614219;
    public final static int REALNAME                    = -76757719;
    public final static int HOST                        = 2223528;
    public final static int REALHOST                    = -76922810;
    public final static int IP                          = 2343;
    public final static int SERVICESID                  = -1239651847;
    
    /* CHAN */
    public final static int OP                          = 2529;
    public final static int DEOP                        = 2094626;
    public final static int OPDEOP                      = -1957288061;
    public final static int VOICE                       = 81848594;
    public final static int ALL                         = 64897;
    public final static int TOPIC                       = 80008463;
       
    /* CHANSERV */
    public final static int AKICK                       = 62335495;
    public final static int AOP                         = 64994;
    public final static int SOP                         = 82292;
    public final static int FOUNDER                     = 43341711;
    public final static int NONE                        = 2402104;
    public final static int ADD                         = 64641;
    public final static int DEL                         = 67563;
    public final static int LIST                        = 2336926;
    public final static int WIPE                        = 2664519;
    public final static int UNBAN                       = 80888502;
    public final static int INVITE                      = -2130369783;
    public final static int WHY                         = 85928;
    public final static int ACCESSLOG                   = 671568704;
    public final static int CHANLIST                    = -2079769872;
    public final static int CHANFLAG                    = -2079946306;
    public final static int MDEOP                       = 73205743;
    public final static int MKICK                       = 73417747;
    public final static int NICK_ACCESS_DENIED          = 1319854842;
    
    public final static int ADDAOP                      = 1925785025;
    public final static int DELAOP                      = 2012834327;
    public final static int ADDSOP                      = 1925802323;
    public final static int DELSOP                      = 2012851625;
    public final static int ADDAKICK                    = -451619290;
    public final static int DELAKICK                    = 1598381308;
    
    public final static int KICKBAN                     = -2596887;
    public final static int LISTACCESS                  = 1234756482;
    public final static int LISTOPS                     = 899964212;
     
    
    
    /* FLAG */
    public final static int TYPE                        = 2590522;
    public final static int REASON                      = -1881635260;
    public final static int INSTATER                    = 1337760614;
    public final static int STAMP                       = 79219619;
    public final static int AK                          = 2090;
    public final static int IG                          = 2334;
    public final static int SQ                          = 2654;
    public final static int SG                          = 2644;
    
    /* CHANFLAG */
    public final static int JOIN_CONNECT_TIME           = -556190185;
    public final static int TALK_CONNECT_TIME           = 393912789;
    public final static int TALK_JOIN_TIME              = -982443793;
    public final static int MAX_BANS                    = 1219737695;
    public final static int NO_NOTICE                   = 1717646102;
    public final static int NO_CTCP                     = -1437640612;
    public final static int NO_PART_MSG                 = 994710995;
    public final static int NO_QUIT_MSG                 = -1244903889;
    public final static int EXEMPT_OPPED               = 711234318;
    public final static int EXEMPT_VOICED              = 772697554;
    public final static int EXEMPT_IDENTD              = 390251732;
    public final static int EXEMPT_REGISTERED          = 1834356450;
    public final static int EXEMPT_INVITES             = -485255670;
    public final static int GREETMSG                    = 988053464;
    
    /* OPER */
    public final static int ACCSTRING                   = 1073267634;
    public final static int ACCSTRINGSHORT              = -260356118;
    
    public final static int IRCOP                       = 69926811;
    public final static int SO                          = 2652;
    public final static int SA                          = 2638;
    public final static int CSOP                        = 2078289;
    public final static int SRA                         = 82370;
    public final static int STAFF                       = 79219392;
    
    public final static int AUDITORIUM                  = -2102649597;
    public final static int UNAUDITORIUM                = 1851161692;
    public final static int MARK                        = 2358989;
    public final static int UNMARK                      = -1787095834;
    public final static int FREEZE                      = 2081894039;
    public final static int UNFREEZE                    = 402727536;
    public final static int NOGHOST                     = -1459660466;
    public final static int UNNOGHOST                   = -1974214507;
    public final static int CLOSE                       = 64218584;
    public final static int REOPEN                      = -1881221379;
    public final static int HELD                        = 2213685;
    public final static int UNHELD                      = -1787241138;
    public final static int GETPASS                     = 643687495;
    public final static int GETEMAIL                    = -1530342906;
    public final static int SENDPASS                    = 2030903193;
    public final static int EXPIREFOUNDER               = -1627605616;
    public final static int EXPIREINACTIVE              = 547532426;
    public final static int MASSDEOP                    = 1065573142;
    public final static int MASSKICK                    = 1065785146;
    public final static int SAJOIN                      = -1856436104;
    public final static int TOPICWIPE                   = -1118937642;
    public final static int WIPEAOP                     = 2069339195;
    public final static int WIPESOP                     = 2069356493;
    public final static int WIPEAKICK                   = 64984608;
    public final static int TOPICNICK                   = -1119206158;
    public final static int TOPICLOG                    = -174652491;
    public final static int FORCENICK                   = 20432046;

   
    /* NICKSETTING */
    public final static int ENFORCED                    = -1714819902;
    public final static int SECURED                     = -1606705235;
    public final static int MAILBLOCKED                 = 391177269;
   
    /* CHANSETTING */
    public final static int KEEPTOPIC                   = 1058282666;
    public final static int TOPICLOCK                   = -1119259974;
    public final static int OPGUARD                     = -543153852;
    public final static int RESTRICT                    = 446081724;
    public final static int VERBOSE                     = 1069090146;
    public final static int LEAVEOPS                    = -1934704485;
    public final static int AUTOAKICK                   = 154503608;
    public final static int MODELOCK                    = 163603790;

    /* NICKSERV */
    public static final int OHELP                       = 75171856;
    public static final int AUTHURL                     = 71479975;    
    public static final int AUTHED                      = 1941967943;    
    public static final int EXPIREAUTH                  = -349233337;    
    
    /* CMDDATA */
    public static final int SYNTAX_ERROR                = -1940937076;
    public static final int NICK_NOT_REGGED             = 771349402;
    public static final int IDENTIFY_FAIL               = -127225743;
    public static final int IDENTIFY_SUCCESS            = 1493501360;
    public static final int NO_SUCH_TARGET              = -482975477;
    public static final int IDENTIFY_NICK               = -126979914;
   
    
    
    /* CHANINFO */
    public static final int DESCRIPTION                 = 428414940;
        
    /* CHANFLAGS */
    public static final int CLOSED                      = 1990776172;
    
    /* NICKINFO */
    public final static int MAIL                        = 2358711;
    public final static int FULLMASK                    = 2114402811;
    public final static int LASTUSED                    = -675794093;
    public final static int REGTIME                     = 1804414273;
        
    /* MEMOSERV */
    public final static int SEND                        = 2541448;
    public final static int CSEND                       = 64417355;
    public final static int OSEND                       = 75499607;
    public final static int READ                        = 2511254;
     
    /* EXECUTOR */
    public static final int OINFO                       = 75210109;
    public static final int OSET                        = 2435475;

    /* NICKFLAGS */
    public final static int HOLD                        = 2223295;
    public final static int UNHOLD                      = -1787231528;
    public final static int FROZEN                      = 2082211488;
    public final static int MARKED                      = -2027976660;
    
    /* USERMODE */
    public final static int IDENT                       = 69511632;
    
    /* OPERSERV */
    public final static int OPER                        = 2432590;
    public final static int ADMIN                       = 62130991;
    public final static int SADMIN                      = -1856616772;
    public final static int SRAW                        = 2553557;
    public final static int PANIC                       = 75895383;
    public final static int AKILL                       = 62335775;
    public final static int AUTOKILL                    = -2072929907;
    public final static int TIME                        = 2575053;
    public final static int SEARCHLOG                   = 269847676;
    public final static int AUDIT                       = 62628795;
    public final static int FULL                        = 2169487;
    public final static int COMMENT                     = 1668381247;
    public final static int GLOBAL                      = 2105276323;
    public final static int BANLOG                      = 1951952085;
    public final static int SQLINE                      = -1841605806;
    public final static int SGLINE                      = -1850841016;
    public final static int SPAMFILTER                  = 1312445441;
    public final static int JUPE                        = 2288768;
    public final static int MISSING                     = 1787432262;

    /* SpamFilter flags */
    public final static int s                     = 115;
    public final static int S                     = 83;
    public final static int r                     = 114;
    public final static int m                     = 109;
    public final static int p                     = 112;
    public final static int n                     = 110;
    public final static int k                     = 107;
    public final static int q                     = 113;
    public final static int t                     = 116;
    public final static int a                     = 97;
    public final static int c                     = 99;
    public final static int P                     = 80;
    public final static int W                     = 87;
    public final static int L                     = 76;
    public final static int R                     = 82;
    public final static int B                     = 66;
    public final static int K                     = 75;
    public final static int A                     = 65;
    public final static int NUM_1                 = 49;
    public final static int NUM_2                 = 50;
    public final static int NUM_3                 = 51;
    public final static int NUM_4                 = 52;
    
    /* CHANNEL */
    public boolean mode_r;         /* mode r */
    public boolean mode_R;         /* mode R */
    public boolean mode_t;         /* mode t */
    public boolean mode_n;         /* mode n */
    public boolean mode_i;         /* mode i */
    public boolean mode_k;         /* mode k word */
    public boolean mode_s;         /* mode s */
    public boolean mode_p;         /* mode p */
    public boolean mode_M;         /* mode M */
    public boolean mode_l;         /* mode l # */
    public boolean mode_j;         /* mode j #:# */
    public boolean mode_c;         /* mode c */
    public boolean mode_O;         /* mode O */
    public boolean mode_m;         /* mode m */

 
    
    public static final int MODE_PLUS                   = 43;
    public static final int MODE_MINUS                  = 45;
    public static final int MODE_r                      = 114;
    public static final int MODE_R                      = 82;
    public static final int MODE_t                      = 116;
    public static final int MODE_n                      = 110;
    public static final int MODE_i                      = 105;
    public static final int MODE_k                      = 107;
    public static final int MODE_s                      = 115;
    public static final int MODE_p                      = 112;
    public static final int MODE_M                      = 77;
    public static final int MODE_l                      = 108;
    public static final int MODE_j                      = 106;
    public static final int MODE_c                      = 99;
    public static final int MODE_O                      = 79;
    public static final int MODE_m                      = 109;
    public static final int MODE_a                      = 97;
    public static final int MODE_A                      = 65;
    public static final int MODE_o                      = 111;
    public static final int MODE_v                      = 118;
    
    
    /* EXECUTOR */
           
    public static final int CHAR_U                      = 85;
    public static final int CHAR_O                      = 79;

    public static final int CHAR_m                      = 77;
    public static final int CHAR_h                      = 72;
    public static final int CHAR_d                      = 68;
    public static final int CHAR_y                      = 89;

    
    /* SERVICE */
      /* STATIC INT */
    public static final int RAW                         = 80904;
    
    /* SNOOP NICKSERV */
    public static final int SYNTAX_REG_ERROR            = 228863233;
    public static final int INVALID_EMAIL               = -1112393964;
    public static final int NICK_ALREADY_REGGED         = -1169624971;
    public static final int INVALID_NICK                = -1421092661;
    
    public static final int SYNTAX_ID_ERROR             = -107542880;
    public static final int NICK_NOT_REGISTERED         = 583065578;
    public static final int PASSWD_ERROR                = 2085745927;
    public static final int IS_FROZEN                   = 319895381;
    public static final int IS_THROTTLED                = 2049769861;
    
    public static final int IS_MARKED                   = 504674529;

    public static final int ACCESS_DENIED_OPER          = -1657667049;
    public static final int ACCESS_DENIED_SA            = -1655354281;
 
    public static final int SYNTAX_GHOST_ERROR          = -1629380164;
    public static final int NO_SUCH_NICK                = -630840995;
    public static final int IS_NOGHOST                  = -256884935;

    public static final int NO_AUTH_FOUND               = 716854089;
    public static final int ACCESS_DENIED               = 1006971606;
    public static final int SETTING_NOT_FOUND           = 391296903;

    public static final int CMD_NOT_FOUND_ERROR         = -173192678;
    public static final int NICK_NEW_MASK               = -287949913;
    public static final int SHOW_HELP                   = 912458563;
    public static final int REGISTER_SEC                = 610829461;
    public static final int REGISTER_DONE               = 1755407262;
    public static final int PASSWD_ACCEPTED             = 1917430824;
    public static final int NICKDROPPED                 = -518725923;
    public static final int NICKDELETED                 = -893999306;
    public static final int GLOB_IS_NOGHOST             = -1048802208;
    public static final int NICKFLAG_EXIST              = -101876761;
    public static final int NICK_SET_FLAG               = -249918811;
    public static final int NICK_GETEMAIL               = 287380322;
    public static final int INVALID_PASS                = -1421040263;
    public static final int NICK_IS_NOW                 = 1715513981;
    public static final int NICK_AUTHED                 = 1487994019;
    public static final int NICK_IS_NOT                 = 1715513978;
    public static final int DB_ERROR                    = -516264217;
    public static final int IDENT_NICK_DELETED          = -2018832084;
    public static final int NICK_DELETED                = 1080155933;
    public static final int ACCESS_DENIED_SRA           = 223625433;
    public static final int ACCESS_DENIED_DELETE_OPER   = 1463855225;
    public static final int NICK_GETPASS                = -544602261;
    
    /* SNOOP CHANSERV */
    public final static int NOT_ENOUGH_ACCESS          = -1745701241;
     
    public final static int NICK_NOT_AUTHED            = 299818415;
    public final static int NICK_NOT_IDENTIFIED        = 834563587;
    public final static int CHAN_NOT_REGISTERED        = -1958828005;
    public final static int CHAN_ALREADY_REGGED        = 583448742;
    public final static int CHAN_WHY                   = -2079190725;
    public final static int CHAN_NOT_EXIST             = 1742993822;
    public final static int NICK_NOT_EXIST             = 1537465455;
    public final static int USER_NOT_ONLINE            = -2081960365;

    public final static int USER_NOT_OP                = 190590433;
    
    public final static int NICK_HAS_NOOP              = -1524398237;
 
    public final static int WILL_NOW                   = -382153527;
    public final static int WILL_NOW_NOT               = -751854019;
    
    public final static int IS_NOW                     = -2125479391;
    public final static int IS_NOT                     = -2125479394;
     
    public final static int INVALID_PASSWORD           = 1094975491;
     
    public final static int DB_NICK_ERROR              = -1563547539;
     
    public final static int NICK_IS_FOUNDER            = 26449878;
    public final static int NICK_IS_SOP                = 1715518779;
    public final static int NICK_IS_OP                 = 193886522;
    public final static int NICK_NOT_FOUND             = 1538132218;
    public final static int NICK_CHANGED               = 268636600;
    public final static int NICK_NOT_PRESENT           = 1032677427;
    
    public final static int NICK_NOT_IDENTED_OP        = 830831385;
    public final static int NICK_NEVEROP               = 1374491761;

    public final static int NICK_ADDED                 = 186025220;
    public final static int NICK_NOT_ADDED             = 1533170296;
    public final static int NICK_NOT_DELETED           = -1393549423;
    public final static int NICK_VERBOSE_ADDED         = 1220179943;
    public final static int NICK_VERBOSE_DELETED       = -1529567936;
    public final static int NICK_INVITED               = 1489723775;

    public final static int NICK_OP                    = -1635024835;
    public final static int NICK_DEOP                  = 698828158;
    public final static int NICK_VERBOSE_OP            = 862466106;
    public final static int NICK_VERBOSE_DEOP          = -99096005;
    
    public final static int NICK_MDEOP_CHAN            = 1949119486;
    public final static int NICK_MDEOP                 = 197108755;
    public final static int NICK_MKICK_CHAN            = -1640108454;
    public final static int NICK_MKICK                 = 197320759;
    
    public final static int LIST_NOT_WIPED             = 1342428656;
    public final static int LIST_WIPED                 = 2112290300;
    public final static int LIST_VERBOSE_WIPED         = 44287455;

    public final static int CHAN_IS_FROZEN             = 1040268968;
    public final static int CHAN_IS_CLOSED             = 948833652;
   
    public final static int CHAN_SET_FLAG              = 1557826454; 
    public final static int CHANFLAG_EXIST             = 103651606; 
    
    public final static int ALREADY_ON_LIST            = -19278345; 
    
    public final static int CHAN_GETPASS               = -901929894;
     
    public final static int SHOWACCESSLOG              = -1644065565; 
    public final static int SHOWACCESSLOGOPER          = 1996456369; 
    public final static int SHOWTOPICLOG               = 443386290; 
    public final static int SHOW_LIST                  = 912581792; 

    public final static int CHANNELDROPPED             = 1559147869; 
    public final static int CHANNELDELETED             = 1183874486; 

    public final static int NO_SUCH_CHANFLAG           = 1591381688; 
    public final static int BAD_CHANFLAG_VALUE         = 1929103146; 

    public final static int XOP_NOT_FOUND              = -929875376; 
    public final static int XOP_ADD_FAIL               = -625622686; 
    public final static int XOP_ALREADY_PRESENT        = 2130003854; 

    public final static int CHAN_INFO                  = -30814853; 
    public final static int CHAN_UNBAN                 = -944182391; 
    public final static int ACCESS_LIST                = 1149892249; 
    public final static int ACCESS_ADDED               = 1276598853; 
    public final static int ACCESS_DELETED             = 1149397022; 

    public final static int SET_DESCRIPTION            = 979264351; 
    public final static int SET_TOPICLOCK              = -1494105347; 
    public final static int SET_MODELOCK               = -679771989; 
    public final static int SET_KEEPTOPIC              = 683437293; 
    public final static int SET_IDENT                  = 330142611; 
    public final static int SET_OPGUARD                = 815113799; 
    public final static int SET_RESTRICT               = -397294055; 
    public final static int SET_VERBOSE                = -1867609499; 
    public final static int SET_MAILBLOCK              = 2106005849; 
    public final static int SET_LEAVEOPS               = 1516887032; 
    public final static int SET_AUTOAKICK              = -220341765;

    public final static int ADDACCLIST                 = -448746274;
    public final static int REMACCLIST                 = 1498428101;
    public final static int UPDACCLIST                 = -298666154;
    
    public boolean iCmp ( String str1, String str2 )  {
        return  ( str1.equalsIgnoreCase ( str2 ) );
    }
}
