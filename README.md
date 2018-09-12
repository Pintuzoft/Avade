# Avade
## Avade IRC Services

The Avade IRC Services is a project made in Java solely for IRC networks running the bahamut IRCd. As Avade has
been developed only for bahamut it the network will be able to get builtin features for the features bahamut offers.
This includes the AKill, SQline, SGline, Auditorium mode and also new features in the upcoming bahamut release.

./DreamHealer

### Special features included in Avade:

- Reconnecting to services hub
- Reconnecting MySQL server
- Persistent user sessions / services ID's
- Excessive logging
- Server command / list missing servers
- External mailing functionality
- NoGhost nickflag
- Auditorium channel option / mode
- Audit staff

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


#### Persistent user sessions / services ID's

Bahamut uses a simple tagging of users to keep track of which nicks and channels a user has access to. These are called
services ID's and is set using usermode +d. Avade IRC Services now correctly track these "sessions" by making them
persistent. What this means is that a user can identify more than nicks than current nick and channels and will be able
to keep being identified to these nicks and channels during splits. Infact Avade will be able to restore a users access
to nicks and channels even after services restarts which makes it very unique. 

Usually a services will forget all nicks and channels a user has identified to after a split or services restart and only
trust usermode +r and automatically identify current nick only. Avade IRC Services will not work like these other services
and rather trust the services id set on a user making Avade alot more user friendly.


#### Excessive logging / list missing servers

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

#### NoGhost flag

The NoGhost flag is a very specific flag. It specifically disables a nickname from being ghosted, so if a user is in a 
ownership disspute where someone else knows the password and is trying to take over a nick that is in used an active oper
is able to stop the take over by applying the flag. When the flag is set all other functionality of the nickis still 
available. Basically if a user is in a pickle and an oper is trying to help by trying to figure out whats going on and 
make sure the nick isnt gonna get taken over, the oper can stop that and have a normal conversation with the user.

#### Auditorium chanflag

IRC operators are able to enable the auditorium flag on a channel. The funcationality in services will automatically do
some initial checks and then register the relay channel as "#channame-relay" for where chat from regular users (-ov) will
end up. The +A channel mode will then be applied to the main channel.

#### Audit staff

Ofcourse IRC is a text based power struggle game, and this also applies to your staff members. Using the audit command
you will be able to get information about when and how a staff member has been added/removed and also what the staff has
been upto.

### Command list

#### NickServ :

- Help           - Show help
- Register       - Register nick
- Identify       - Identify nick
- SIdentify      - Silently identify nick
- Ghost          - Kill ghost nick
- SET            - Set nick options
- Drop           - Drop registered nick
  
--- IRCop---
  
- List           - List registered nicks
- Mark           - OperFlag to lock ownership functionality
- Freeze         - OperFlag to freeze a nick from being used
- Hold           - OperFlag to deny a nick from expiring
- NoGhost        - OperFlag to deny a nick from being ghosted
- Getpass        - Show password log for nick
- Getemail       - Show email log for nick
- Delete         - Force drop a nick


#### ChanServ :
  
- Help           - Show help
- Register       - Register channel
- Identify       - Identify channel
- Set            - Set channel options
- Info           - Show info about a nick
- AOP            - Manage AOP list
- SOP            - Manage SOP list
- AKICK          - Manage AKICK list
- Op             - Op nick
- Deop           - Deop nick
- Unban          - Remove all matching bans
- Invite         - Invite yourself
- Why            - Show why someone has access to a channel
- Chanlist       - Show all channels you have access to (founder, sop, aop)
- Mdeop          - Mass deop channel
- Mkick          - Mass kick channel
- Drop           - Drop registered channel
- Accesslog      - View SOP/AOP/AKICK logs
  
--- IRCop ---
  
- List           - List registered channels
- Chanlist       - Show all channels a user has access to (founder, sop, aop, akick)
- Mark           - Lock ownership features
- Freeze         - Freeze channel from being used
- Close          - Close channel
- Hold           - Deny channel from expiring
- Auditorium     - Makes a channel an auditorium
- Getpass        - Show password log
- Delete         - Force drop a channel
  
  
#### MemoServ :
  
- Help           - Show help
- Send           - Send a memo to a user
- Csend          - Send a memo to a channel
- List           - List memos
- Read           - Read memo
- Del            - Delete memo
  
#### OperServ :
  
- Help           - Show help
- Staff          - Manage IRCop/SA/CSOP/SRA lists
- Global         - Send global message
- Uinfo          - Show debug information regarding a user
- Cinfo          - Show debug information regarding a channel
- Ulist          - Show user map (use only on smaller networks)
- Uptime         - Show uptime information
- Akill          - Manage AKill list
- Searchlog      - Show ownership events and comments for a nick or channel
- Audit          - Show staff events
- Comment        - Attach a comment to a nick or channel
- Ignore         - Manage the ignore list
- Sqline         - Manage the SQline (restricted nick) list
- Sgline         - Manage the SGline (restricted gcos) list
- Jupe           - Jupiter a server to prevent it from linking
- Server         - Serverlist purposed as missing server list (auto-populated)
  
  
#### RootServ :
  
- Rehash         - Re-read the services configuration file
- Sraw           - Send a raw services command to the network
  
  

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


