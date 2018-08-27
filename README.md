# Avade
## Avade IRC Services

The Avade IRC Services is a new project made in Java solely for IRC networks running the bahamut IRCd. As Avade has
been developed only for bahamut it the network will be able to get builtin features for the features bahamut offers.
This includes the AKill, SQline, SGline, Auditorium mode and also new features in the upcoming bahamut release.

### Command list

#### NickServ :

  Help           - Show help

  Register       - Register nick
  
  Identify       - Identify nick
  
  SIdentify      - Silently identify nick
  
  Ghost          - Kill ghost nick
  
  SET            - Set nick options
  
  Drop           - Drop registered nick
  
  --- IRCop---
  
  List           - List registered nicks
  
  Mark           - Lock ownership features
  
  Freeze         - Freeze a nick from being used
  
  Hold           - Deny a nick from expiring
  
  Getpass        - Show password log for nick
  
  Getemail       - Show email log for nick
  
  Delete         - Force drop a nick


#### ChanServ :
  
  Help           - Show help
  
  Register       - Register channel
  
  Identify       - Identify channel
  
  Set            - Set channel options
  
  Info           - Show info about a nick
  
  AOP            - Manage AOP list
  
  SOP            - Manage SOP list
  
  AKICK          - Manage AKICK list
  
  Op             - Op nick
  
  Deop           - Deop nick
  
  Unban          - Remove all matching bans
  
  Invite         - Invite yourself
  
  Why            - Show why someone has access to a channel
  
  Chanlist       - Show all channels you have access to (founder, sop, aop)
  
  Mdeop          - Mass deop channel
  
  Mkick          - Mass kick channel
  
  Drop           - Drop registered channel
  
  Accesslog      - View SOP/AOP/AKICK logs
  
  --- IRCop ---
  
  List           - List registered channels
  
  Chanlist       - Show all channels a user has access to (founder, sop, aop, akick)
  
  Mark           - Lock ownership features
  
  Freeze         - Freeze channel from being used
  
  Close          - Close channel
  
  Hold           - Deny channel from expiring
  
  Auditorium     - Makes a channel an auditorium
  
  Getpass        - Show password log
  
  Delete         - Force drop a channel
  
  
#### MemoServ :
  
  Help           - Show help
  
  Send           - Send a memo to a user
  
  Csend          - Send a memo to a channel
  
  List           - List memos
  
  Read           - Read memo
  
  Del            - Delete memo
  
#### OperServ :
  
  Help           - Show help
  
  Staff          - Manage IRCop/SA/CSOP/SRA lists
  
  Global         - Send global message
  
  Uinfo          - Show debug information regarding a user
  
  Cinfo          - Show debug information regarding a channel
  
  Ulist          - Show user map (use only on smaller networks)
  
  Uptime         - Show uptime information
  
  Akill          - Manage AKill list
  
  Searchlog      - Show ownership events and comments for a nick or channel
  
  Audit          - Show staff events
  
  Comment        - Attach a comment to a nick or channel
  
  Ignore         - Manage the ignore list
  
  Sqline         - Manage the SQline (restricted nick) list
  
  Sgline         - Manage the SGline (restricted gcos) list
  
  Jupe           - Jupiter a server to prevent it from linking
  
  Server         - Serverlist purposed as missing server list (auto-populated)
  
  
#### RootServ :
  
  Rehash         - Re-read the services configuration file        
  
  Sraw           - Send a raw services command to the network
  
  




