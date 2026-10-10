# Avade
## Avade IRC Services

The Avade IRC Services is a project made in Java solely for IRC networks running 
the bahamut IRCd. As Avade has been developed only for bahamut it the network 
will be able to get builtin features for the features bahamut offers. This 
includes the AKill, SQline, SGline and Auditorium mode, and what bahamut 2.2 brought: 
halfops, channel flags, clone limits, spam filters, host-masking and join requests.

./DreamHealer

### Install

You need Java 17, a bahamut hub and MariaDB (or MySQL). Download
`Avade-<version>.tar.gz` from the [latest release](https://github.com/Pintuzoft/Avade/releases/latest),
and as the user that will run services:

    tar xzf Avade-<version>.tar.gz
    cd Avade-<version>
    ./install.sh

With git instead: `git clone https://github.com/Pintuzoft/Avade.git`, `cd Avade`
and `./install.sh`.

It puts everything in `~/avade/` and starts services. The first time it asks a
few questions (your network, the hub, the database), writes `services.conf`
for you and shows the lines the hub needs in its `ircd.conf`. Run it again in
a newer version to upgrade. Everything else is in [INSTALL](INSTALL).

### Special features included in Avade:

- Reconnecting to services hub
- Reconnecting MySQL server
- Persistent user sessions / services ID's
- Excessive logging
- Server command / list missing servers
- External mailing functionality
- NoGhost nickflag
- Auditorium channel option / mode
- Join requests
- Host-masking
- Vhosts
- Passwords stored as hashes
- VOP and HOP access levels
- Channel flags
- Clone limits
- Spam filters
- Memo limits
- Audit staff
- Topic logs
- Tested against a real bahamut

#### Reconnecting to services hub

Some services has the bad habit of exiting when the link to the services hub 
goes down for various reasons. Avade IRC services will notice this and try 
reconnect to the services hub if that happens.


#### Reconnecting MySQL server

Avade IRC services will also notice if the connection to the database goes down 
and work as normal until the connection is re-established. Please note that not 
everything will work when the database connection is down. Example of something
that will not work is searching any type of logs. A nick or channel will however
work perfectly fine when the database is down, and services will save information 
about what has changed, then push those changes to the database when the
connection returns. This means you preferably want to avoid restarting services 
when there is no database connection to avoid losing data.


#### Persistent user sessions / services ID's

Bahamut uses a simple tagging of users to keep track of which nicks and channels 
a user has access to. These are called services ID's and is set using usermode 
+d. Avade IRC Services now correctly track these "sessions" by making them 
persistent. What this means is that a user can identify more than nicks than 
current nick and channels and will be able to keep being identified to these 
nicks and channels during splits. Infact Avade will be able to restore a users 
access to nicks and channels even after services restarts which makes it very 
unique. 

Usually a services will forget all nicks and channels a user has identified to 
after a split or services restart and only trust usermode +r and automatically 
identify current nick only. Avade IRC Services will not work like these other 
services and rather trust the services id set on a user making Avade alot more 
user friendly.

A user who disappears in a split is remembered for three days. When the server 
comes back its users are still identified, and are not all sent to guest nicks 
at once.


#### Excessive logging / list missing servers

Something that other services might not provide is a way to access logs which 
can describe how a nickname or channel has been used. This include logs for 
register, set email, set pass, freeze, hold, close, auditorium etc. This 
information is only available for IRCops.

Avade will also provide channel access logs in channels which will show when and 
who gave or removed access in the chan. The access log for a channel is open for 
AOP+ and IRCops.


#### External mailing functionality

Something that has shown to have been working poorly in different versions of 
services is how mailing is handled when there is a problem with the smtp server. 
This has been known to be causing services to sit and wait for a timeout or some 
other error during which it perhaps isnt doing anything else. This has been 
resolved by lifting out the mailing funcitonality to its own small software,
AvadeMailer (`mailer/`). Services only put each mail in the database, and the
mailer sends it. All the mailer needs is a database connection, so it can be
restarted on its own or run on another machine. See "Mail" in INSTALL.


#### Server command

The server command is a new feature that works in a very simple and beautiful 
way. Avade will automatically populate a list of servers that has been seen 
connected to the network, and if a server is missing an IRCop will be able to 
list it using the missing server command. This is something that no other 
services has as far as I know, and it can be very useful for larger networks 
where there might or probably will be difficult to figure out which servers 
actually is split and gone.

#### NoGhost flag

The NoGhost flag is a very specific flag. It specifically disables a nickname 
from being ghosted, so if a user is in a ownership disspute where someone else 
knows the password and is trying to take over a nick that is in used an active 
oper is able to stop the take over by applying the flag. When the flag is set 
all other functionality of the nickis still available. Basically if a user is in 
a pickle and an oper is trying to help by trying to figure out whats going on and 
make sure the nick isnt gonna get taken over, the oper can stop that and have a 
normal conversation with the user.

#### Auditorium chanflag

IRC operators are able to enable the auditorium flag on a channel. The +A channel 
mode is then applied to the main channel, and chat from regular users (-ov) ends up 
in "#channame-relay". A relay channel can not be registered: while the flag is set 
ChanServ gives op in it to everyone with AOP or higher in the main channel, and 
removes everyone else from it.

#### Join requests

With services join requests the servers ask Avade before they let a user into a 
channel. A user on the AKICK list, or without access to a RESTRICT channel, is then 
stopped before the join and not kicked after it, and the channel never sees the user. 
The key, the limit, bans, exception and invite lists and the other modes are checked 
by Avade the way the ircd does. The staff turn it on for the network 
(/OperServ SJR ON), a founder for a channel (/ChanServ CHANFLAG #channel SJR ON).

#### Host-masking

Avade can hide the real host of users from other users, together with the 
`avade_uhm` module for bahamut that comes with Avade (`bahamut-module/`). bahamut 
itself is not changed.

    c-83-233-12-7.bredband.telia.com  ->  mynet-0cabd86f.bredband.telia.com
    192.0.2.44                        ->  6c13e277.8057c675.1b690abc.ip

The same address always gets the same mask, so a ban on a mask holds, and IRC 
operators still see the real host. Avade does the same calculation as the module 
and always knows which host a user is shown with: AKICKs and access masks match it, 
and a ban follows the person who gets another host. A user who wants to show the 
real host sets `/NickServ SET SHOWHOST ON`. It is turned on for the network with 
`/OperServ UHM`, see "Host-masking" in INSTALL.

#### Vhosts

A user with a confirmed email can choose the host that is shown: 
`/NickServ SET VHOST my.own.host`. It is set at every identify. An ip address, the 
names of the network and the words in `vhostforbidden` in the config are refused, 
and it can be changed once per ten minutes. Staff set or remove the vhost of any 
nick with `/OperServ VHOST`.

#### Passwords stored as hashes

Passwords of nicks and channels are stored as one way hashes (PBKDF2). Nobody can 
read them, not the staff and not someone who gets hold of the database. A user who 
lost a password gets a code by mail with `/NickServ RESETPASS <nick>` and sets a new 
one with it. Staff can set a new password with SETPASS, without seeing the old one.

#### VOP and HOP access levels

Next to SOP and AOP a channel has a HOP list (halfop) and a VOP list (voice): 
`/ChanServ HOP #channel ADD nick` and `/ChanServ VOP #channel ADD nick`. The nick 
gets +h or +v when it joins.

#### Channel flags

bahamut has extended channel flags against floods and spam: how long a user must 
have been connected to join or talk, how many messages in how many seconds, no 
CTCPs or notices, no nick changes, a greeting for those who join, and more. The 
ircd forgets them when the channel goes empty. With `/ChanServ CHANFLAG` the founder 
sets them, and ChanServ puts them back every time the channel is created.

#### Clone limits

`/OperServ CLONE ADD <ip|a.b.c.*> <limit>` sets how many clients may connect from an 
ip or a range on the whole network, for example a school behind one address. The 
servers enforce it, and it replaces the clone trigger of services for that address.

#### Spam filters

`/OperServ SPAMFILTER` adds patterns that the servers match against messages, with 
what should happen on a match: warn the opers, block the message, kill or akill the 
sender. A filter can be limited to one channel or nick, and has an id, so it can be 
seen which filter stopped a message.

#### Memo limits

A nick holds 30 memos, and 5 unread ones from the same sender. Every memo is also a 
mail to the receiver, so one user can not fill the memo box or the inbox of another.

#### Tested against a real bahamut

`tests/` starts a network of its own, a bahamut hub and leaf, MariaDB, Avade and the 
mailer, and goes through the features as a user and an oper would: about 300 checks. 
A weekly job runs the same suite against a new bahamut release when one comes out.

#### Audit staff

Ofcourse IRC is a text based power struggle game, and this also applies to your 
staff members. Using the audit command you will be able to get information about 
when and how a staff member has been added/removed and also what the staff has
been upto.


#### Topic logs

Tired of hearing rumours about whats going on in a channel and when you check 
someone already changed the topic and everyone is super nice?, well Avade IRC 
Services will log topics and list them back to a staff member in a chronical 
order so you will not need to trust shady logs to determine if a channel has 
been violated network rules or other types of abuse using the channel topic.



### Command list

#### NickServ :

- Help           - Show help
- Register       - Register nick
- Auth           - Confirm an email address or a new password
- Identify       - Identify nick
- SIdentify      - Silently identify nick
- Ghost          - Kill ghost nick
- ResetPass      - New password with a code sent to the confirmed email
- SET            - Set nick options
- Info           - Show info about a nick
- Drop           - Drop registered nick
  
--- IRCop---
  
- List           - List registered nicks
- Mark           - OperFlag to lock ownership functionality
- Freeze         - OperFlag to freeze a nick from being used
- Hold           - OperFlag to deny a nick from expiring
- NoGhost        - OperFlag to deny a nick from being ghosted
- Setpass        - Set a new password (nobody can see the old one)
- Getemail       - Show email log for nick
- Delete         - Force drop a nick


#### ChanServ :
  
- Help           - Show help
- Register       - Register channel
- Identify       - Identify channel
- Set            - Set channel options
- Chanflag       - Set extended channel flags of the ircd (flood limits, join requests ..)
- Info           - Show info about a channel
- AOP            - Manage AOP list
- SOP            - Manage SOP list
- HOP            - Manage HOP (halfop) list
- VOP            - Manage VOP (voice) list
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
- Listops        - View the founder and the SOP/AOP lists
  
--- IRCop ---
  
- List           - List registered channels
- Chanlist       - Show all channels a user has access to (founder,sop,aop,akick)
- Topiclog       - Show last 100 topics set in the channel
- Mark           - Lock ownership features
- Freeze         - Freeze channel from being used
- Close          - Close channel
- Hold           - Deny channel from expiring
- Auditorium     - Makes a channel an auditorium
- Setpass        - Set a new channel password
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
- Ninfo          - Show debug information regarding a nick
- Sinfo          - Show debug information regarding a server
- Ulist          - Show user map (use only on smaller networks)
- Clist          - Show the channels on the network
- Slist          - Show the servers on the network
- Uptime         - Show uptime information
- Akill          - Manage AKill list
- Makill         - Add many akills at once
- Banlog         - Search the log of services bans
- Searchlog      - Show ownership events and comments for a nick or channel
- Snooplog       - Search the snoop logs for a nick or channel
- Audit          - Show staff events
- Comment        - Attach a comment to a nick or channel
- Ignore         - Manage the ignore list
- Sqline         - Manage the SQline (restricted nick) list
- Sgline         - Manage the SGline (restricted gcos) list
- Spamfilter     - Manage the spam filters of the network
- Forcenick      - Change the nick of a user
- Vhost          - Set or remove the vhost of a nick
- Clone          - Manage clone limits for ips and ranges
- Uhm            - Control user host-masking on the network
- Sjr            - Control services join requests on the network
- Jupe           - Jupiter a server to prevent it from linking
- Server         - Serverlist purposed as missing server list (auto-populated)
- Bahamut        - Show the bahamut version services are made for
  
  
#### RootServ :
  
- Rehash         - Re-read the services configuration file
- Showconfig     - Show the services configuration (passwords and the salt are hidden)
- Sraw           - Send a raw services command to the network
- Sra            - Manage the Services Root Admin list
- Panic          - Limit who can use services commands
- Stop           - Stop services, pending changes are written first
  
  

## License

Avade is free software under the GNU General Public License, version 2 or
(at your option) any later version. The text is in `LICENSE`. The libraries
it comes with are the work of others under their own licenses, listed in
`lib/README.md`.

## Contributing to the project

### Code

If you downloaded Avade and made changes to it to make it better or adding 
features to it, I do want you to take a diff and send me the changes. If the 
changes is appealing and interesting I might add it to the project. People who 
submit code will be credited for it. Send to: dreamhealer [AT] avade [DOT] net

### Donating

This is a project that is worked on on my spare time, so any contributions are 
welcome. Donating to the project will be a boost for motivation and might be used 
for paying various bills as hosting fees etc, or they might go towards beer who 
knows ;). For donations use paypal and send to: dreamhealer [AT] avade [DOT] net, 
any and all contributions are appreciated.


