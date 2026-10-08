# Testing a new version with a copy of production data

Before a new Avade version runs in production, run it on a test server against
a **copy** of the production database. The automatic test suite (`tests/`) only
has an empty database. This test shows what happens to your real nicks,
channels and passwords, and how long the upgrade takes.

Everything below is done on the test server. The only step in production is
step 1, the dump, and it only reads.

## 1. Copy the database from production

On the production machine:

    mysqldump --single-transaction --hex-blob --default-character-set=binary \
        -u services -p avade > avade-prod.sql

`--default-character-set=binary` copies the text byte for byte. This matters
because the mail addresses (and in versions before 1.2609-8 the passwords) are
AES encrypted in the tables.

Move the file to the test server and load it into a database of its own:

    mysql -u root -p -e "create database avadetest"
    mysql -u root -p -e "grant all privileges on avadetest.* to 'avade'@'localhost' identified by 'CHOOSE-A-PASSWORD'"
    mysql -u root -p avadetest < avade-prod.sql

Keep `avade-prod.sql`. If something goes wrong, load it again.

## 2. Empty the mailbox in the copy

The copy has the mail addresses of real users. Make sure nothing on the test
server sends mail to them:

    mysql -u root -p avadetest -e "delete from mailbox"

Never run a mailer with `send: true` against the copy (see step 7).

## 3. bahamut

Use the bahamut version that production will run, for example 2.2.4:

    git clone https://github.com/DALnet/bahamut.git
    cd bahamut
    git checkout v2.2.4
    ./configure --prefix=$HOME/ircd224
    make && make install

Copy `ircd.conf` from the test server's current ircd. Check that the connect
block for services has `flags H;` and that the services and stats names are in
the `super` block (U-lines).

If the build complains about `__res_mkquery` or `__dn_expand`, see
`tests/build-ircd.sh`, which has the fix (mostly older bahamut versions).

## 4. Build Avade

Needs Java 17 or newer. With ant, `make.sh` compiles Avade, without it the
`dist/Avade.jar` from the source is used.

    git clone https://github.com/Pintuzoft/Avade.git
    cd Avade
    git checkout <the release tag or branch to test>
    ./make.sh install

This puts `avade.jar`, `mailer.jar`, `lib/`, `template.conf`, `reference.conf`,
`mailer-template.conf` and `mailer-reference.conf` in `~/avade/`.

`make.sh` looks for Java 17 in the usual places. To use another one:
`JAVA_HOME=/path/to/jdk ./make.sh install`.

## 5. services.conf

Start from the `services.conf` of production and change:

| Setting | Value |
|---|---|
| `secretsalt` | **The same as in production.** The upgrade needs it to turn the passwords into hashes, and the mail addresses are read with it. With a wrong salt the upgrade refuses and nothing changes. |
| `hubname`, `hubhost`, `hubport`, `hubpass` | the hub of the test network, not production's |
| `mysqldb`, `mysqluser`, `mysqlpass` | `avadetest` and the user from step 1 |
| `master` | a nick that is registered in the copy and whose password you know |

Commands that are new since older versions need an access level (otherwise
only SRA can use them, and a warning says so at start). `./avade.sh setup`
does this for you: with a `services.conf` that is already there it lists the
commands that are new or gone and changes the access lists when you say yes.
By hand, under `sa:`:

      - vhost
      - clone

Under `csop:`, `getpass` is replaced by `setpass`. GETPASS no longer exists,
SETPASS sets a new password without anyone seeing the old one:

      - setpass

Optional, words that may not be part of a user's vhost:

    vhostforbidden:
      - "*admin*"
      - "*oper*"

Compare with `template.conf` for the exact layout. `reference.conf` explains
every setting.

## 6. Start

    cd ~/avade
    ./avade.sh start
    ./avade.sh log        # follow the output, ctrl-c only stops the viewing

The first start upgrades the database from the version production runs to
1.2609-8, one step at a time. The step that changes the character set to
utf8mb4 goes through every table and can take a while on a big database.

Check afterwards:

    mysql -u avade -p avadetest -e "select * from settings"

The version must be `1.2609-8`.

The last step turns every password into a one way hash. It takes about 70 ms
per password and core, so a few thousand nicks take a few minutes. Old
passwords (the history) are removed. Afterwards this must give 0:

    mysql -u avade -p avadetest -e "select count(*) from passlog where pass not like 'pbkdf2-sha256\$%'"

After the upgrade an older Avade can not run against the database any more,
it can not read the hashes. The way back is the dump from step 1.

Stop with `./avade.sh stop` or `/RootServ STOP`. Both write everything to the
database first. Avoid `kill -9`.

## 7. The mailer, without sending

    cd ~/avade
    ./avade.sh mailer start

The first time it asks for the database login and the mail server. Take the
database login of services, skip the test mail, and answer **no** to "Send the
mails of services for real?" (that is `send: false` in `mailer.conf`). Then:

    ./avade.sh mailer queue

Every mail Avade makes is marked "not sent (send: false)" and logged in
`mailer.log` with its id and subject. Nothing leaves the machine. See "Mail"
in `INSTALL`.

## 8. What to try

Go through this with a couple of clients. The ones marked *most important* can
only be seen with real data.

- *Most important:* identify with your usual password on an old nick, and with
  the channel password (`/ChanServ IDENTIFY`) on an old channel. If it works,
  the passwords survived the copy and were turned into hashes correctly.
- *Most important:* `/NickServ INFO` and `/ChanServ INFO` on nicks and
  channels with å, ä, ö (or other non-ASCII text) in the topic or elsewhere.
  It must show correctly.
- *Most important:* join an old channel where you are founder/SOP/AOP. You
  must get op, and the topic and modes must be set as before.
- Restart Avade while identified. You must still be identified afterwards,
  without typing the password.
- Restart the hub while Avade runs. Avade must link again by itself within
  half a minute.
- `/ChanServ VOP #channel ADD nick` and `HOP`. The nick must get +v and +h.
- `/NickServ SET VHOST my.own.host` and look at WHOIS.
- `/ChanServ CHANFLAG` on a channel, restart Avade, check that the flags are
  still there.
- `/OperServ CLONE ADD`, `/OperServ SPAMFILTER` with a target.
- `/NickServ RESETPASS <nick>` on a nick with your own mail address. The mail
  is in the `mailbox` table with status 3 (not sent). The code in its text sets
  a new password with `/NickServ RESETPASS <nick> <code> <new-password>`.
- `/NickServ SETPASS` and `/ChanServ SETPASS` as csop, and
  `/ChanServ SET #channel PASSWD` as founder.

## 9. Host-masking (optional)

Follow step 10 in `INSTALL`. With the directories from step 3 the module is
built like this:

    cd Avade/bahamut-module
    ./build.sh ~/bahamut ~/ircd224

If there are several bahamut sources on the machine, it must be the one the
running ircd was built from, or the ircd refuses to load the module.

The salt (`uhmsalt`) must be one of its own for the test network, not
production's.

## 10. When something goes wrong

Keep `services.log`, `avade.out` and `mailer.log`. Then load the dump again
(step 1) and the database is back where it started.

## The automatic test suite

`tests/` has a suite that starts a small network of its own (hub, leaf,
MariaDB in Docker, Avade, the mailer and a test SMTP server) with an empty
database and goes through the features automatically:

    cd tests
    ./start.sh 2.2.4
    ./run_tests.py
    ./stop.sh

It does not replace the test above, it does not have your real data.
