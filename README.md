# Avade
## Avade IRC Services

The Avade IRC Services is a project made in Java solely for IRC networks running the bahamut IRCd. As Avade has
been developed only for bahamut it the network will be able to get builtin features for the features bahamut offers.
This includes the AKill, SQline, SGline, Auditorium mode and also new features in the upcoming bahamut release.

### Special features included in Avade:

#### Reconnecting to services hub

Some services has the bad habit of exiting when the link to the services hub goes down for various reasons. Avade IRC 
services will notice this and try reconnect to the services hub if that happens.


#### Reconnecting MySQL server

Avade IRC services will also notice if the connection to the database goes down and work as normal until the connection 
is re-established. Please note that not everything will work when the database connection is down. Example of something
that will not work is searching any type of logs. A nick or channel will however work perfectly fine when the database
is down, and services will save information about what has changed, then push those changes to the database when the
connection returns. This means you preferably want to avoid restarting services when there is no database connection
to avoid losing data.


#### Excessive logging

Something that other services might not provide is a way to access logs which can describe how a nickname or channel
has been used. This include logs for register, set email, set pass, freeze, hold, close, auditorium etc. This information
is only available for IRCops.

Avade will also provide channel access logs in channels which will show when and who gave or removed access in the 
chan. The access log for a channel is open for AOP+ and IRCops.


#### External mailing functionality

Something that has shown to have been working poorly in different versions of services is how mailing is handled when there
is a problem with the smtp server. This has been known to be causing services to sit and wait for a timeout or some other
error during which it perhaps isnt doing anything else. This has been resolved by lifting out the mailing funcitonality
to its own small software. All this feature needs is a database connection, and as its java based aswell it can run offsite
from services keeping the location of services hopefully a secret.


#### Server command

The server command is a new feature that works in a very simple and beautiful way. Avade will automatically populate a list
of servers that has been seen connected to the network, and if a server is missing an IRCop will be able to list it using 
the missing server command. This is something that no other services has as far as I know, and it can be very useful for 
larger networks where there might or probably will be difficult to figure out which servers actually is split and gone.


#### Persistent services ID's

As long as services is online it will store services ID's for all users. At times there might be a split, this will cause a
user to be removed from services awareness, however a reconnecting server with identified users can retain their access to
all identified nicks and channels as services will remember these sessions for a whole hour before cleaning them out. If you
have servers split more than an hour you might have serious issues with your network. 

Other services might not figure out that an identified user is the same as the one that splitted and will only identify the user
with the users current nick (+r). Avade however will keep track of the actual servicesID handled by bahamut and treat that
services ID as an actual user session. This means of course that a user can use a different nick after a split and services
will still be able to identify the user.


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
  
  

## Contributing to the project

### Code

If you downloaded Avade and made changes to it to make it better or adding features to it, I do want you to take a diff and
send me the changes. If the changes is appealing and interesting I might add it to the project. People who submit code will
be credited for it. Send code to: dreamhealer [AT] avade [DOT] net

### Donating

This is a project that is worked on on my spare time, so any contributions are welcome. Donating to the project will be a 
boost for motivation and might be used for paying various bills as hosting fees etc, or they might go towards beer who 
knows ;). For donations use paypal and send it to: dreamhealer [AT] avade [DOT] net, any and all contributions are 
appreciated.


