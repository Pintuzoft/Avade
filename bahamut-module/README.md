# avade_uhm – user host-masking for bahamut

bahamut can hide the real host of users (umode `+H`, `SVSUHM`) but does not
decide what the masked host looks like: it asks a module. This is such a
module, made so that Avade does the very same calculation and always knows
the masked host of every user. No change to bahamut itself is needed.

    192.0.2.44                          ->  e7c5e988.2e1d861f.37c7a6fa.ip
    2001:db8::1                         ->  6f33aaa4.ec4dbf62.9bfdd9ca.ip6
    c-83-233-12-7.bredband.telia.com    ->  avade-89688695.bredband.telia.com

The same address always gets the same mask, and a user cannot change it, so
a ban on a mask holds. The three parts of an ip mask follow the address:
`*!*@*.2e1d861f.37c7a6fa.ip` is the whole /24, `*!*@*.37c7a6fa.ip` the /16.
IRC operators still see the real host.

## The salt

The masks are made with a secret salt. **Anyone who has it can work out which
mask belongs to which ip**, so treat it like a database password.

It is set in one place, `uhmsalt` in Avade's `services.conf`. Services send it
to the servers, and each server keeps it in `avade_uhm.salt` next to its
`ircd.conf` (readable by the ircd user only) so it survives a restart while
services are away. Nothing is put on a server by hand.

A server that has not been told the salt yet masks with a random one: no real
host is shown, but its masks differ until services have linked.

Changing the salt changes every mask. Bans, akicks and access masks set on
the old masks stop matching.

## Install

On every server of the network:

1. Build it against the bahamut source the server was built from (after
   `./configure` has been run there):

       ./build.sh /path/to/bahamut-2.2.4 /path/to/ircd

   which puts `avade_uhm.so` in `/path/to/ircd/modules/`.

2. Load it at start, in `ircd.conf`:

       modules { path modules; autoload avade_uhm; };

   and rehash, or load it at once as a server admin: `/MODULE LOAD avade_uhm`.

In Avade:

3. Generate a salt with `./avade.sh gensalt` and put it in `services.conf`:

       uhmsalt: <the salt>
       uhmprefix: mynet

4. Restart Avade (or `/RootServ REHASH` and relink). `/MODULE CMD avade_uhm`
   on a server should now say "salt set by services".

5. Turn masking on: `/OperServ UHM 1 1` (type 1, everyone gets `+H` when they
   connect). `/OperServ UHM 0 0` turns it off. The servers remember it.

Users that were already online keep the host they had until they reconnect.

## Check that services and the servers agree

    /MODULE CMD avade_uhm TEST <host> <ip>      on a server, as server admin
    /OperServ UHM TEST <host> <ip>              in Avade

must give the same answer.

## Details

`h(x)` is the first 8 hex digits of HMAC-SHA256(salt, x).

| The user's host | Mask |
|---|---|
| IPv4 `a.b.c.d` | `h("a.b.c.d").h("a.b.c").h("a.b").ip` |
| IPv6 | `h(32 hex digits).h(first 16).h(first 12).ip6` |
| a name with at least three labels | `prefix-h(name in lower case).rest.after.first.label` |
| anything else | as the ip |

Only masking type 1 is handled, other types are left to other modules.
The Java side is `src/core/HostMask.java`: the two must be changed together.
