# Avade
# Avade IRC Services

The Avade IRC Services is a new project made in Java solely for IRC networks running the bahamut IRCd. As Avade has
been developed only for bahamut it the network will be able to get builtin features for the features bahamut offers.
This includes the AKill, SQline, SGline, Auditorium mode and also new features in the upcoming bahamut release.

Command list

NickServ :

<table>
  <tr><td></td><td>Help</td><td>Show help</td></tr>
  <tr><td></td><td>Register</td><td>Register nick</td></tr>
  <tr><td></td><td>Identify</td><td>Identify nick</td></tr>
  <tr><td></td><td>SIdentify</td><td>Silently identify nick</td></tr>
  <tr><td></td><td>Ghost</td><td>Kill ghost nick</td></tr>  
  <tr><td></td><td>SET</td><td>Set nick options</td></tr>  
  <tr><td></td><td>Drop</td><td>Drop registered nick</td></tr>
  <tr><td></td><td>--- IRCop---</td><td></td></tr>
  <tr><td></td><td>List</td><td>List registered nicks</td></tr>
  <tr><td></td><td>Mark</td><td>Lock ownership features</td></tr>
  <tr><td></td><td>Freeze</td><td>Freeze a nick from being used</td></tr>
  <tr><td></td><td>Hold</td><td>Deny a nick from expiring</td></tr>
  <tr><td></td><td>Getpass</td><td>Show password log for nick</td></tr>
  <tr><td></td><td>Getemail</td><td>Show email log for nick</td></tr>
  <tr><td></td><td>Delete</td><td>Force drop a nick</td></tr>
</table>

ChanServ :
  Help            Show help
  Register        Register channel
  Identify        Identify channel
  Set             Set channel options
  Info            Show info about a nick
  AOP             Manage AOP list
  SOP             Manage SOP list
  AKICK           Manage AKICK list
  Op              Op nick
  Deop            Deop nick
  Unban           Remove all matching bans
  Invite          Invite yourself
  Why             Show why someone has access to a channel
  Chanlist        Show all channels you have access to (founder, sop, aop)
  Mdeop           Mass deop channel
  Mkick           Mass kick channel
  Drop            Drop registered channel
  Accesslog
  --- IRCop ---
  List            List registered channels
  Chanlist        Show all channels a user has access to (founder, sop, aop, akick)
  Mark            Lock ownership features
  Freeze          Freeze channel from being used
  Close           Close channel
  Hold            Deny channel from expiring
  Auditorium      Makes a channel an auditorium
  Getpass         Show password log
  Delete          Force drop a channel
  
MemoServ :
  Help            Show help
  Send            Send a memo to a user
  Csend           Send a memo to a channel
  List            List memos
  Read            Read memo
  Del             Delete memo
  
OperServ :
  Help            Show help
  Staff           Manage IRCop/SA/CSOP/SRA lists
  Global          Send global message
  Uinfo           Show debug information regarding a user
  Cinfo           Show debug information regarding a channel
  Ulist           Show user map (use only on smaller networks)
  Uptime          Show uptime information
  Akill           Manage AKill list
  Searchlog       Show ownership events and comments for a nick or channel
  Audit           Show staff events
  Comment         Attach a comment to a nick or channel
  Ignore          Manage the ignore list
  Sqline          Manage the SQline (restricted nick) list
  Sgline          Manage the SGline (restricted gcos) list
  Jupe            Jupiter a server to prevent it from linking
  Server          Serverlist purposed as missing server list (auto-populated)
  
RootServ :
  Rehash          Re-read the services configuration file        
  Sraw            Send a raw services command to the network
  


