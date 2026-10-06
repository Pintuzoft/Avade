#!/usr/bin/env python3
"""Tests for Avade against a real bahamut and MariaDB.

    tests/start.sh [bahamut version]     start the test network (empty database)
    tests/run_tests.py                   run everything
    tests/run_tests.py topic vhost       run only the tests whose name contains a word
    tests/stop.sh

The tests use the commands as a user or oper would and look at what the ircd
and the database end up with. Each test uses its own channels, the users are
registered once at the start (Avade writes new nicks to the database once a
minute, so that takes a little over a minute).
"""
import os
import socket
import sys
import time
import traceback

from irctest import (Client, ENV, SERVICES, close_all, MASTER, LEAF_PORT, avade, avade_log, auth_code, db, ircd,
                     ircd_version, link_count, wait_db, wait_relink)

USERS = ['Alice', 'Bob', 'Carol', 'Dave', 'Erin', 'Zed', MASTER]
UNAUTHED = 'Nomail'             # registered, email never confirmed
HALFOPS = ircd_version() >= (2, 1, 5)

results = []                    # (test, ok, text)
current = ['']


def pw(nick):
    return nick + 'pass1'      # at least 8 characters


def check(ok, text, detail=None):
    results.append((current[0], bool(ok), text))
    print('    %s  %s' % ('ok  ' if ok else 'FAIL', text))
    if not ok and detail is not None:
        print('          got: %r' % (detail,))


def has(notices, text):
    return any(text.lower() in n.lower() for n in notices)


def login(nick, port=None, host='127.0.0.1', identify=True):
    c = Client(nick, port, host) if port else Client(nick, host=host)
    if identify:
        r = c.svc('NickServ', 'IDENTIFY ' + pw(nick))
        if not has(r, 'Password accepted'):
            raise AssertionError('could not identify %s: %r' % (nick, r))
    return c


def master():
    c = login(MASTER)
    c.oper()
    time.sleep(1)
    return c


def close(*clients):
    for c in clients:
        if c:
            c.quit()
    time.sleep(0.5)


def register_chan(founder, chan, password='chanpw1'):
    founder.join(chan)
    r = founder.svc('ChanServ', 'REGISTER %s %s test channel' % (chan, password))
    if not has(r, 'successfully registered') and not has(r, 'already registered'):   # a second run
        raise AssertionError('could not register %s: %r' % (chan, r))


### Setup

def setup():
    """Register and confirm the test users once, make the master the services master."""
    if db("select name from nick where name = '%s'" % MASTER):
        print('users are already registered')
        return
    print('registering test users (takes about five minutes)')
    for nick in USERS + [UNAUTHED]:
        c = Client(nick)
        r = c.svc('NickServ', 'REGISTER %s %s@test.net' % (pw(nick), nick.lower()))
        assert has(r, 'successfully registered'), (nick, r)
        c.quit()
    want = len(USERS) + 1
    got = wait_db("select count(*) from maillog having count(*) >= %d" % want, 150)
    assert got, 'the new nicks were never written to the database'
    for nick in USERS:
        code = auth_code(nick.lower() + '@test.net', 240)
        assert code, 'no confirmation mail for ' + nick
        c = Client(nick)
        c.svc('NickServ', 'IDENTIFY ' + pw(nick))
        r = c.svc('NickServ', 'AUTH ' + code)
        assert has(r, 'fully authed'), (nick, r)
        c.quit()
    # Avade writes in batches once a minute: wait until everything is stored
    # before it is restarted, a kill would lose what is still pending
    assert wait_db("select count(*) from nick having count(*) >= %d" % want, 240), 'nicks not stored'
    time.sleep(130)             # the confirmations are written the same way
    # The master nick now exists, a restart makes it the services master
    avade('restart')


### Tests

def test_identify():
    s = Client('Shortpw')
    r = s.svc('NickServ', 'REGISTER short1 shortpw@test.net')
    check(has(r, 'password is not valid'), 'REGISTER needs a password of at least 8 characters', r)
    close(s)
    c = Client('Alice')
    check(has(c.svc('NickServ', 'IDENTIFY fel-losenord'), 'accepted') is False, 'wrong password is refused')
    check(has(c.svc('NickServ', 'IDENTIFY ' + pw('Alice')), 'Password accepted'), 'right password identifies')
    n = Client(UNAUTHED)
    n.svc('NickServ', 'IDENTIFY ' + pw(UNAUTHED))
    time.sleep(1)
    check(not n.saw(r' MODE %s :\+\S*r' % UNAUTHED, 0, 1), 'a nick without confirmed email does not get +r')
    close(c, n)


def test_throttle():
    c = Client('Zed')
    for i in range(4):
        c.svc('NickServ', 'IDENTIFY fel%d' % i, wait=0.6)
    r = c.svc('NickServ', 'IDENTIFY ' + pw('Zed'))
    check(not has(r, 'Password accepted'), 'the 5th attempt is throttled even with the right password', r)
    g = Client('Dave')
    r = g.svc('NickServ', 'GHOST Zed ' + pw('Zed'))
    check(not has(r, 'accepted'), 'GHOST is throttled too', r)
    close(c, g)


def test_access_security():
    chan = '#t_access'
    a, b, ca = login('Alice'), login('Bob'), login('Carol')
    register_chan(a, chan)
    check(has(a.svc('ChanServ', 'AOP %s ADD Bob' % chan), 'added to the Aop'), 'founder adds an AOP')
    b.svc('ChanServ', 'SOP %s ADD Bob' % chan)
    check(not has(a.svc('ChanServ', 'SOP %s LIST' % chan), '- Bob'), 'an AOP cannot make itself SOP')
    check(has(a.svc('ChanServ', 'AKICK %s ADD Carol' % chan), 'added'), 'founder adds an AKICK')
    ca.svc('ChanServ', 'AKICK %s DEL Carol' % chan)
    check(has(a.svc('ChanServ', 'AKICK %s LIST' % chan), '- Carol'), 'an akicked user cannot remove its own akick')
    check(has(ca.svc('ChanServ', 'UNBAN %s' % chan), 'denied'), 'an akicked user cannot use UNBAN')
    check(has(b.svc('ChanServ', 'TOPICLOG %s' % chan), 'denied'), 'TOPICLOG needs services access')
    check(has(b.svc('ChanServ', 'AOP %s DEL Bob' % chan), 'deleted') or
          not has(a.svc('ChanServ', 'AOP %s LIST' % chan), '- Bob'), 'an AOP can remove itself')
    close(a, b, ca)


def test_topic_sync():
    chan = '#t_topic'
    text = 'hej :) åäö ☕'
    a = login('Alice')
    register_chan(a, chan)
    a.svc('ChanServ', 'SET %s KEEPTOPIC ON' % chan)
    m = a.mark()
    a.send('TOPIC %s :%s' % (chan, text))
    a.wait(r' TOPIC ' + chan, 5, m)
    row = wait_db("select topic, unix_timestamp(stamp) from topiclog where name = '%s'" % chan, 30)
    check(row.split('\t')[0] == text, 'topic is stored as it was set (no extra colon, utf-8)', row)
    m = a.mark()
    a.send('TOPIC ' + chan)
    who = a.wait(r' 333 ', 5, m).split()
    check(row.split('\t')[-1] == who[-1], 'the IRC timestamp of the topic is stored', (row, who))

    b = login('Bob')
    m = a.mark()
    b.join(chan)
    check(not a.saw(r'^:ChanServ!\S+ TOPIC ' + chan, m, 2), 'ChanServ does not send the topic when someone joins')
    close(b)

    m = a.mark()
    a.send('PART ' + chan)
    time.sleep(1)
    a.send('JOIN ' + chan)
    check(a.saw(r'^:ChanServ!\S+ TOPIC %s :%s' % (chan, 'hej'), m, 5), 'KEEPTOPIC restores the topic in a recreated channel')
    m = a.mark()
    a.send('TOPIC ' + chan)
    who2 = a.wait(r' 333 ', 5, m).split()
    check(who2[-1] == who[-1] and who2[-2].startswith('Alice'), 'with the original setter and time', who2)

    # A restart must not touch the topic of a channel that exists, but still know it
    m = a.mark()
    avade('restart')
    check(not a.saw(r'^:ChanServ!\S+ TOPIC ' + chan, m, 3), 'a services restart does not overwrite the topic')
    m = a.mark()
    a.send('PART ' + chan)
    time.sleep(1)
    a.send('JOIN ' + chan)
    check(a.saw(r'^:ChanServ!\S+ TOPIC %s :%s' % (chan, 'hej'), m, 5), 'the topic is loaded from the database after a restart')
    close(a)


def test_old_null_topic():
    """Versions before 1.2609 left "null" as topic (set by "null") on channels without one."""
    chan = '#t_null'
    a = login('Alice')
    register_chan(a, chan)
    avade('stop')
    time.sleep(2)
    # Play the old services for a moment and leave that topic on the network
    s = socket.create_connection(('127.0.0.1', int(ENV['HUB_SERVER_PORT'])), timeout=10)
    s.sendall(('PASS %s :TS\r\nCAPAB NICKIPSTR\r\nSERVER %s 1 :old services\r\nSVINFO 5 3 0 :%d\r\n'
               % (ENV['LINK_PASS'], SERVICES, time.time())).encode())
    time.sleep(3)
    m = a.mark()
    s.sendall((':%s TOPIC %s null 0 :null\r\n' % (SERVICES, chan)).encode())
    check(a.saw(r' TOPIC %s :null' % chan, m, 5), '(the old topic "null" is on the network)')
    s.close()
    time.sleep(2)
    avade('restart')
    m = a.mark()
    a.send('TOPIC ' + chan)
    check(a.saw(r' 331 ', m, 5), 'the new version clears it', a.since(m))
    time.sleep(65)
    check(db("select count(*) from topiclog where name = '%s' and topic = 'null'" % chan) == '0',
          'and does not store it')
    close(a)


def test_topiclock():
    chan = '#t_lock'
    a, b = login('Alice'), login('Bob')
    register_chan(a, chan)
    a.svc('ChanServ', 'AOP %s ADD Bob' % chan)
    a.svc('ChanServ', 'SET %s TOPICLOCK FOUNDER' % chan)
    a.send('TOPIC %s :locked by the founder' % chan)
    time.sleep(1)
    b.join(chan)
    b.saw(r' MODE %s \+o Bob' % chan, 0, 3)
    m = a.mark()
    b.send('TOPIC %s :bob tries' % chan)
    check(a.saw(r'^:ChanServ!\S+ TOPIC %s :locked by the founder' % chan, m, 5), 'TOPICLOCK puts the locked topic back')
    check(has(a.svc('ChanServ', 'SET %s TOPICLOCK ON' % chan), 'Syntax'), 'an invalid TOPICLOCK value gives the syntax')
    close(a, b)


def test_sessions_survive_restart():
    chan = '#t_sess'
    a = login('Alice')
    register_chan(a, chan)
    time.sleep(3)       # let the services id be written
    m = a.mark()
    avade('restart')
    check(not a.saw(r' MODE %s -o Alice' % chan, m, 3), 'the founder is not deopped after a services restart')
    check(not a.saw(r' MODE Alice :-\S*r', m, 1), '+r is not removed')
    check(has(a.svc('ChanServ', 'AOP %s LIST' % chan), 'list for'), 'still identified without a new IDENTIFY')
    close(a)


def test_vop_hop():
    chan = '#t_xop'
    a, b, ca, e = login('Alice'), login('Bob'), login('Carol'), login('Erin')
    register_chan(a, chan)
    a.svc('ChanServ', 'AOP %s ADD Bob' % chan)
    check(has(a.svc('ChanServ', 'VOP %s ADD Erin' % chan), 'added to the Vop'), 'founder adds a VOP')
    m = a.mark()
    e.join(chan)
    check(a.saw(r' MODE %s \+v Erin' % chan, m, 4), 'a VOP gets +v on join')
    check(has(e.svc('ChanServ', 'OP %s Erin' % chan), 'denied'), 'a VOP cannot use OP')
    check(has(b.svc('ChanServ', 'SOP %s ADD Carol' % chan), 'denied'), 'an AOP cannot manage the SOP list')
    if HALFOPS:
        check(has(b.svc('ChanServ', 'HOP %s ADD Carol' % chan), 'added to the Hop'), 'an AOP adds a HOP')
        check(has(ca.svc('ChanServ', 'VOP %s ADD Dave' % chan), 'denied'), 'a HOP cannot manage lists')
        m = a.mark()
        ca.join(chan)
        check(a.saw(r' MODE %s \+h Carol' % chan, m, 4), 'a HOP gets +h on join')
        got = wait_db("select group_concat(access order by access) from chanaccess where name = '%s'" % chan, 30)
        check(got == 'aop,hop,vop', 'the lists are stored in the database', got)
        # the burst after a restart has %Carol and +Erin, nobody should be touched
        m = a.mark()
        avade('restart')
        check(not a.saw(r' MODE %s [+-][ohv]+ ' % chan, m, 3), 'halfops and voices in the burst are understood')
    else:
        print('    skip  halfops need bahamut 2.1.5+')
    close(a, b, ca, e)


def test_ipv6():
    c = login('Erin', host='::1')
    check(True, 'a user on IPv6 can identify')
    r = c.svc('NickServ', 'INFO Erin')
    check(has(r, 'Info for'), 'and use the services', r)
    close(c)


def test_chanflags():
    chan = '#t_flags'
    a = login('Alice')
    register_chan(a, chan)
    for flag in ('NO_QUIT_MSG ON', 'HIDE_MODE_LISTS ON', 'MAX_INVITES 42', 'MAX_MSG_TIME 5:10',
                 'GREETMSG Välkommen ☕'):
        r = a.svc('ChanServ', 'CHANFLAG %s %s' % (chan, flag), wait=1)
        check(has(r, 'has now been set'), 'CHANFLAG ' + flag.split()[0], r)
    check(has(a.svc('ChanServ', 'CHANFLAG %s OPER_VERBOSE ON' % chan), 'denied'), 'OPER_VERBOSE is not for founders')
    row = wait_db("select no_quit_msg, hide_mode_lists, max_invites, max_msg_time from chanflag "
                  "where name = '%s' and max_invites = 42 and max_msg_time = '5:10'" % chan, 30)
    check(row == '1\t1\t42\t5:10', 'the flags are stored in the database', row)
    avade('restart')
    mm = master()
    mm.join(chan)
    m = mm.mark()
    mm.send('CHECK CHANNEL ' + chan)
    time.sleep(2)
    out = ' | '.join(mm.since(m))
    msgtime = 'MAX_MSG_TIME: 5:10' in out or ircd_version() < (2, 1, 5)     # new in 2.1.5
    check('MAX_INVITES: 42' in out and msgtime and 'NO_QUIT_MSG: On' in out,
          'the ircd has the flags after a services restart', out[-300:])
    b = login('Bob')
    m = b.mark()
    b.join(chan)
    check(b.saw('Välkommen', m, 3), 'the greet message reaches a user that joins')
    close(a, b, mm)


def test_vhost():
    b, a = login('Bob'), login('Alice')
    def host(nick):
        return [t.split()[2] for n, t in a.whois(nick) if n == '311'][0]
    real = host('Bob')
    check(has(b.svc('NickServ', 'SET VHOST 1.2.3.4'), 'ip address'), 'an ip address is refused')
    check(has(b.svc('NickServ', 'SET VHOST utanpunkt'), 'may only contain'), 'a host without a dot is refused')
    check(has(b.svc('NickServ', 'SET VHOST bob.test.net'), 'reserved'), 'the network domain is refused')
    check(has(b.svc('NickServ', 'SET VHOST superadmin.host.se'), 'not allowed'), 'vhostforbidden is used')
    check(has(b.svc('NickServ', 'SET VHOST bob.dricker.kaffe'), 'is now'), 'a valid vhost is set')
    check(host('Bob') == 'bob.dricker.kaffe', 'WHOIS shows the vhost', host('Bob'))
    check(has(b.svc('NickServ', 'SET VHOST annan.host.se'), '10 minutes'), 'one change per 10 minutes')
    n = login(UNAUTHED)
    check(has(n.svc('NickServ', 'SET VHOST no.mail.se'), 'confirm the email'), 'needs a confirmed email')
    close(b)
    b = Client('Bob')
    check(host('Bob') == real, 'the vhost is not shown before IDENTIFY', host('Bob'))
    b.svc('NickServ', 'IDENTIFY ' + pw('Bob'))
    check(host('Bob') == 'bob.dricker.kaffe', 'and is set again at IDENTIFY', host('Bob'))
    mm = master()
    check(has(mm.svc('OperServ', 'VHOST Bob OFF'), 'removed'), 'an oper removes the vhost')
    check(host('Bob') == real, 'WHOIS shows the real host again', host('Bob'))
    close(a, b, n, mm)


def test_clone_limit():
    mm = master()
    check(has(mm.svc('OperServ', 'CLONE ADD 1.2.* 5'), 'Error'), 'an invalid mask is refused')
    def hard_limits():
        m = mm.mark()
        mm.send('STATS d')
        return int(mm.wait(r'Hard global limits: \d+', 10, m).rsplit(' ', 1)[1])
    before = hard_limits()
    check(has(mm.svc('OperServ', 'CLONE ADD 0::1 2 test'), 'set the clone limit'), 'a limit is added')
    check(hard_limits() == before + 1, 'the ircd has the limit', hard_limits())
    def connect(n):
        ok = []
        for i in range(n):
            try:
                ok.append(Client('Clone%d' % i, host='::1'))
            except Exception:
                pass
        count = len(ok)
        close(*ok)
        return count
    check(connect(3) == 2, 'the ircd refuses the 3rd connection from that address')
    check(has(mm.svc('OperServ', 'CLONE LIST'), '0::1 - 2 clients'), 'CLONE LIST shows it')
    check(has(mm.svc('OperServ', 'CLONE DEL 0::1'), 'removed'), 'the limit is removed')
    # (connecting again here would only hit the ircd's own throttle for rejected hosts)
    check(hard_limits() == before, 'and the ircd has dropped it', hard_limits())
    close(mm)


def test_spamfilter_target():
    chan, other = '#t_sf', '#t_sf2'
    mm, b = master(), login('Bob')
    for c in (mm, b):
        c.join(chan)
        c.join(other)
    r = mm.svc('OperServ', 'SPAMFILTER ADD *avadetestspam* cmB target:%s no spam' % chan)
    check(has(r, 'added SpamFilter SF-'), 'a filter with a target is added and has an id', r)
    m = mm.mark()
    b.send('PRIVMSG %s :detta är avadetestspam här' % chan)
    b.send('PRIVMSG %s :detta är avadetestspam här' % other)
    time.sleep(2)
    got = [l for l in mm.since(m) if 'avadetestspam' in l and ' PRIVMSG ' in l]
    check(not any(' ' + chan + ' ' in l for l in got), 'the message is blocked in the target channel')
    check(any(' ' + other + ' ' in l for l in got), 'but not in another channel')
    check(has(mm.svc('OperServ', 'SPAMFILTER LIST'), '(target: %s)' % chan), 'SPAMFILTER LIST shows the target')
    mm.svc('OperServ', 'SPAMFILTER DEL *avadetestspam*')
    close(mm, b)


def test_staff_in_whois():
    mm, b = master(), login('Bob')
    def staff(c, nick):
        return [t for n, t in c.whois(nick) if n == '320']
    check(any('Services Master' in t for t in staff(b, MASTER)), 'WHOIS shows the services master')
    check(not staff(mm, 'Bob'), 'a normal user has no staff line')
    mm.svc('OperServ', 'STAFF SA ADD Bob')
    check(any('Services Administrator' in t for t in staff(mm, 'Bob')), 'STAFF ADD shows it at once')
    check(has(mm.svc('OperServ', 'STAFF LIST'), 'Services Admins'), 'STAFF LIST works')
    mm.svc('OperServ', 'STAFF SA DEL Bob')
    check(not staff(mm, 'Bob'), 'STAFF DEL removes it')
    close(mm, b)


def test_panic_without_state():
    """Used to recurse forever and take services down."""
    mm = master()
    r = mm.svc('RootServ', 'PANIC')
    check(has(r, 'Panic state is: USER'), 'RootServ PANIC shows the state', r)
    check(has(mm.svc('NickServ', 'INFO ' + MASTER), 'Info for'), 'and services are still alive')
    check(has(mm.svc('RootServ', 'NOSUCHCOMMAND'), 'no such command'), 'an unknown RootServ command gets an answer')
    close(mm)


def test_akick_kicks():
    """The kick was skipped when nobody in the channel was identified, and had no reason."""
    chan = '#t_akick'
    a = login('Alice')
    register_chan(a, chan)
    r = a.svc('ChanServ', 'AKICK %s ADD Evil*!*@*' % chan)
    check(has(r, 'added'), 'an akick mask is added', r)
    a.send('PART ' + chan)
    time.sleep(1)
    e = Client('EvilOne')
    m = e.mark()
    e.send('JOIN ' + chan)
    kick = ''
    try:
        kick = e.wait(r' KICK %s EvilOne ' % chan, 8, m)
    except TimeoutError:
        pass
    check(kick != '', 'the akicked user is kicked from a channel with nobody identified in it', e.since(m)[-4:])
    check('AutoKicked' in kick, 'with the reason', kick)
    check(has(a.svc('ChanServ', 'AKICK %s ADD broken!mask' % chan), 'not registered') or
          not has(a.svc('ChanServ', 'AKICK %s ADD x!y@' % chan), 'added'), 'a mask that is not nick!user@host is refused')
    a.svc('ChanServ', 'AKICK %s DEL Evil*!*@*' % chan)
    close(a, e)


def test_mask_rank():
    """An AOP could move a mask from the SOP list down to the VOP list."""
    chan = '#t_rank'
    a, b = login('Alice'), login('Bob')
    register_chan(a, chan)
    a.svc('ChanServ', 'SOP %s ADD *!*@sopmask.test.example' % chan)
    a.svc('ChanServ', 'AOP %s ADD Bob' % chan)
    r = b.svc('ChanServ', 'VOP %s ADD *!*@sopmask.test.example' % chan)
    check(not has(r, 'added'), 'an AOP cannot move a SOP mask to the VOP list', r)
    r = a.svc('ChanServ', 'SOP %s LIST' % chan)
    check(has(r, 'sopmask.test.example'), 'the mask is still on the SOP list', r)
    close(a, b)


def test_dash_in_channel_name():
    chan, other = '#t-with-dash', '#twithdash'
    a = login('Alice')
    register_chan(a, chan)
    register_chan(a, other)
    mm = master()
    mm.svc('ChanServ', 'FREEZE %s test' % chan)
    frozen = lambda c: has(mm.svc('ChanServ', 'INFO ' + c), 'Frozen')
    check(frozen(chan), 'FREEZE works on a channel with - in the name', mm.svc('ChanServ', 'INFO ' + chan))
    check(not frozen(other), 'and does not hit the channel without the dashes')
    mm.svc('ChanServ', 'FREEZE -%s' % chan)
    check(not frozen(chan), 'and it can be unfrozen')
    close(a, mm)


def test_memo():
    a, b = login('Alice'), login('Bob')
    r = a.svc('MemoServ', 'SEND Bob hej Bob, det här är ett memo')
    check(has(r, 'Memo sent to: Bob'), 'a memo is sent', r)
    r = b.svc('MemoServ', 'LIST')
    check(has(r, 'Alice') and not has(r, 'null') and not has(r, '1970'), 'LIST shows the sender and a real date', r)
    r = b.svc('MemoServ', 'READ 1')
    check(has(r, '<Alice> hej Bob'), 'READ shows who sent it', r)
    r = b.svc('MemoServ', 'READ abc')
    check(len([x for x in r if 'abc' in x or 'READ' in x]) == 1, 'READ with a non-number gives one error', r)
    b.svc('MemoServ', 'DEL 1')
    close(a, b)


def test_nick_privacy_and_mail():
    a, c = login('Alice'), login('Carol')
    check(not has(c.svc('NickServ', 'INFO Alice'), 'Hostmask'), 'INFO hides the hostmask of someone else')
    check(has(a.svc('NickServ', 'INFO Alice'), 'Hostmask'), 'and shows it to the owner')
    check(has(a.svc('NickServ', 'SET NOOP banana'), 'Syntax'), 'SET needs ON or OFF')
    r = c.svc('NickServ', 'SET EMAIL %s carol-new@test.net' % pw('Carol'))
    mail = wait_db("select subject from mailbox where mail = 'carol-new@test.net'", 150)
    check(mail != '', 'SET EMAIL sends a confirmation mail to the new address', (r, mail))
    close(a, c)


def test_oper_checks():
    mm = master()
    r = mm.svc('OperServ', 'AKILL ADD 99y *!*@203.0.113.99 test')
    check(has(r, 'Bad expire time'), 'a ban for more than ten years is refused', r)
    r = mm.svc('OperServ', 'AKILL ADD 30 Spammer!*@*.example.invalid test')
    check(has(r, 'nick part must be'), 'AKILL with a nick in the mask is refused', r)
    r = mm.svc('OperServ', 'AKILL ADD 30 *!*@ test')
    check(has(r, 'Syntax'), 'AKILL with a broken mask gets an answer', r)
    r = mm.svc('OperServ', 'JUPE ' + ENV['HUB_NAME'])
    check(has(r, 'cannot be juped') or has(r, 'linked right now'), 'JUPE of a linked server is refused', r)
    r = mm.svc('OperServ', 'JUPE ' + SERVICES)
    check(has(r, 'cannot be juped'), 'JUPE of services is refused', r)
    r = mm.svc('OperServ', 'SPAMFILTER ADD *avadebadflags* zzW test')
    check(not has(r, 'added SpamFilter'), 'SPAMFILTER with unknown flags is refused', r)
    r = mm.svc('OperServ', 'SPAMFILTER ADD * cB test')
    check(not has(r, 'added SpamFilter'), 'SPAMFILTER on everything is refused', r)
    r = mm.svc('OperServ', 'AKILL ADD 30 *!*@127.0.0.* test')
    check(not has(r, 'has been added') and not has(r, 'added to'), 'AKILL covering a whitelisted address is refused', r)
    # With forcemodes on (the default) nobody outside the staff list keeps +o,
    # so "an IRC operator" and "staff" are the same thing on the network
    o = Client('PlainOper')
    m = o.mark()
    o.oper()
    check(o.saw(r' MODE PlainOper :?-\S*o', m, 5), 'an oper that is not on the staff list loses +o', o.since(m)[-3:])
    close(o)
    r = mm.svc('OperServ', 'SQLINE ADD 10 %s test' % MASTER)
    check(has(r, 'matches oper'), 'a ban that hits an IRC operator is refused', r)
    r = mm.svc('OperServ', 'FORCENICK ' + MASTER)
    check(has(r, 'is an IRCop'), 'FORCENICK on an IRC operator is refused', r)
    mm.svc('OperServ', 'SGLINE ADD 10 *avadetestgcos* test', wait=2)
    check(has(mm.svc('OperServ', 'SGLINE LIST *'), 'avadetestgcos'), 'SGLINE LIST shows the sgline')
    mm.svc('OperServ', 'SGLINE DEL *avadetestgcos*')
    close(mm)


def test_dropped_nick_memos():
    """Memos stayed in the database and were given to the next owner of the nick."""
    a = login('Alice')
    t = Client('Tempnick')
    t.svc('NickServ', 'REGISTER temppw12 tempnick@test.net')
    check(wait_db("select name from nick where name = 'Tempnick'", 150) != '', '(a nick to drop is stored)')
    a.svc('MemoServ', 'SEND Tempnick hemligt memo')
    check(wait_db("select count(*) from memo where name = 'Tempnick' having count(*) > 0", 30) != '', '(it has a memo)')
    t.svc('NickServ', 'DROP temppw12')
    left = '1'
    for i in range(100):
        left = db("select count(*) from memo where name = 'Tempnick'")
        if left == '0':
            break
        time.sleep(1.5)
    check(left == '0', 'the memos of a dropped nick are removed', left)
    close(a, t)


def test_help_and_last_login():
    a = login('Alice')
    m = a.mark()
    a.send('PRIVMSG ChanServ@%s :HELP' % SERVICES)
    time.sleep(4)
    lines = [l for l in a.since(m) if ' NOTICE ' in l]
    check(lines and not any(l.startswith(':OperServ!') for l in lines),
          'ChanServ HELP is sent by ChanServ only', [l for l in lines if l.startswith(':OperServ!')][:2])
    close(a)
    b = Client('Alice', host='::1')
    r = b.svc('NickServ', 'IDENTIFY ' + pw('Alice'))
    check(has(r, 'Last login from') and has(r, 'Password accepted'),
          'IDENTIFY from another address shows where the last login was from', r)
    r = b.svc('NickServ', 'IDENTIFY ' + pw('Alice'))
    check(not has(r, 'Last login from'), 'but not when it is the same address again', r)
    close(b)


def test_modelock_key():
    chan = '#t_mlock'
    a = login('Alice')
    register_chan(a, chan)
    m = a.mark()
    r = a.svc('ChanServ', 'SET %s MODELOCK +nt' % chan)
    check(not a.saw(r' MODE %s .*null' % chan, m, 2) and not a.saw(r' MODE %s \S*l' % chan, m, 1),
          'SET MODELOCK sends nothing when the modes already are right', [l for l in a.since(m) if ' MODE ' in l])
    a.send('MODE %s +k hemlig' % chan)
    time.sleep(1)
    m = a.mark()
    a.svc('ChanServ', 'SET %s MODELOCK +nt-k' % chan)
    check(a.saw(r' MODE %s -k hemlig' % chan, m, 4), 'MODELOCK -k removes the key', [l for l in a.since(m) if ' MODE ' in l])
    m = a.mark()
    a.send('MODE %s +k igen' % chan)
    check(a.saw(r' MODE %s -k igen' % chan, m, 4), 'and removes a key that is set later', [l for l in a.since(m) if ' MODE ' in l])
    close(a)


def test_uhm():
    mm = master()
    r = mm.svc('OperServ', 'UHM')
    check(has(r, 'Host-masking type: 0 (off)'), 'OperServ UHM shows the host-masking of the network', r)
    r = mm.svc('OperServ', 'UHM 1 2')
    check(has(r, 'set host-masking to type 1'), 'UHM sets it', r)
    check(has(mm.svc('OperServ', 'UHM'), 'type: 1, umode +H: 2'), 'and remembers it')
    mm.svc('OperServ', 'UHM 0 0')
    check(has(mm.svc('OperServ', 'UHM x y'), 'Syntax'), 'bad values are refused')
    close(mm)


def test_host_masking():
    """The avade_uhm module in the ircd and Avade must agree on every mask."""
    if not os.path.exists(os.path.join(os.path.dirname(os.path.abspath(__file__)), '.work',
                                       'ircd-' + '.'.join(str(x) for x in ircd_version()), 'modules', 'avade_uhm.so')):
        print('    skip  this bahamut has no host-masking hook')
        return
    mm = master()
    mm.send('MODE %s +A' % MASTER)
    mm.send('LINKS')
    time.sleep(1)
    if not any(' 364 ' in l and ' leaf.test.net ' in l for l in mm.since(0)):
        mm.send('CONNECT leaf.test.net 7016')
        time.sleep(8)
    def module(c, args):
        m = c.mark()
        c.send('MODULE CMD avade_uhm ' + args)
        try:
            return c.wait(r'NOTICE \S+ :avade_uhm', 20, m).split(' :', 1)[1]
        except TimeoutError:
            return ''
    check('set by services' in module(mm, ''), 'the hub got the salt from services', module(mm, ''))
    check(has(mm.svc('OperServ', 'UHM'), 'salt for the avade_uhm module is set'), 'OperServ UHM says the salt is configured')

    cases = [('192.0.2.44', '192.0.2.44'), ('10.0.0.1', '10.0.0.1'), ('255.255.255.255', '255.255.255.255'),
             ('2001:db8::1', '2001:db8::1'), ('2001:0db8:0:0:0:0:0:1', '2001:0db8:0:0:0:0:0:1'), ('0::1', '0::1'),
             ('0::ffff:192.0.2.1', '0::ffff:192.0.2.1'), ('fe80::abcd:1234', 'fe80::abcd:1234'),
             ('c-83-233-12-7.bredband.telia.com', '83.233.12.7'), ('C-83-233-12-7.Bredband.TELIA.com', '83.233.12.7'),
             ('example.com', '192.0.2.9'), ('localhost', '127.0.0.1'), ('a.b.c', '192.0.2.10'),
             ('very-long-host-name-part-one.very-long-host-name-part-two.example.org', '192.0.2.11'),
             ('host.with_underscore.example.net', '2001:db8::2')]
    diff = []
    for host, ip in cases:
        ircd_mask = module(mm, 'TEST %s %s' % (host, ip)).rsplit(' -> ', 1)[-1]
        avade_mask = ''.join(mm.svc('OperServ', 'UHM TEST %s %s' % (host, ip), wait=0.4)).rsplit(' -> ', 1)[-1]
        if not ircd_mask or ircd_mask != avade_mask:
            diff.append((host, ip, ircd_mask, avade_mask))
    check(not diff, 'Avade and the ircd get the same mask for %d hosts and addresses' % len(cases), diff)

    # The same on the leaf: services sent the salt to every server
    close(mm)
    # (over ::1: bahamut throttles an address network wide, also when the allow block
    #  says no throttling, and too many test clients from 127.0.0.1 would get refused)
    mm = login(MASTER, port=LEAF_PORT, host='::1')
    mm.oper()
    mm.send('MODE %s +A' % MASTER)
    time.sleep(1)
    check('set by services' in module(mm, ''), 'the leaf got the salt too', module(mm, ''))
    diff = []
    for host, ip in cases[:1] + cases[3:4] + cases[8:9]:
        ircd_mask = module(mm, 'TEST %s %s' % (host, ip)).rsplit(' -> ', 1)[-1]
        avade_mask = ''.join(mm.svc('OperServ', 'UHM TEST %s %s' % (host, ip), wait=0.4)).rsplit(' -> ', 1)[-1]
        if not ircd_mask or ircd_mask != avade_mask:
            diff.append((host, ip, ircd_mask, avade_mask))
    check(not diff, 'and gives the same masks', diff)

    mm.svc('OperServ', 'UHM 1 1')
    try:
        time.sleep(1)
        x = Client('Maskedone', LEAF_PORT)
        v = Client('Viewer')
        time.sleep(1)
        shown = [t.split()[2] for n, t in v.whois('Maskedone') if n == '311'][0]
        check(shown.startswith('avade-') or shown.endswith('.ip'), 'a user that connects is shown with a masked host', shown)
        r = mm.svc('OperServ', 'UINFO Maskedone')
        check(has(r, 'Shown as: ' + shown), 'Avade knows that masked host', (shown, [l for l in r if 'Shown' in l]))
        chan = '#t_masked'
        a = login('Alice')
        register_chan(a, chan)
        r = a.svc('ChanServ', 'AKICK %s ADD *!*@%s' % (chan, shown))
        a.send('PART ' + chan)
        time.sleep(1)
        m = x.mark()
        x.send('JOIN ' + chan)
        check(x.saw(r' KICK %s Maskedone ' % chan, m, 8), 'an AKICK on the masked host kicks the user', (r, x.since(m)[-3:]))
        a.svc('ChanServ', 'AKICK %s DEL *!*@%s' % (chan, shown))
        close(x, v, a)
    finally:
        mm.svc('OperServ', 'UHM 0 0')
    close(mm)


def test_log_file():
    log = open(os.path.join(os.path.dirname(os.path.abspath(__file__)), '.work', 'run', 'services.log'),
               errors='replace').read()
    check(log.count('\n') > 3, 'services.log is written, one line per entry', log[:200])


def test_bans():
    mm = master()
    mm.svc('OperServ', 'AKILL TIME 60d *!*@203.0.113.7 test of 60 days')
    row = wait_db("select timestampdiff(day, stamp, expire) from akill where mask like '%203.0.113.7%'", 30)
    check(row == '60', 'AKILL TIME 60d expires in 60 days', row)
    mm.svc('OperServ', 'AKILL DEL *!*@203.0.113.7')
    q = Client('{Qtest}')
    r = mm.svc('OperServ', 'SQLINE ADD 10 {Qtest} reserved', wait=3)
    check(has(r, 'sqline for {Qtest}'), 'SQLINE on a nick with special characters is added', r)
    check(q.saw(r' NICK :', 0, 5), 'and the user is renamed')
    mm.svc('OperServ', 'SQLINE DEL {Qtest}')
    close(mm, q)


def test_drop_and_hold():
    chan = '#t_drop'
    a = login('Alice')
    register_chan(a, chan, 'droppw1')
    a.svc('ChanServ', 'DROP %s droppw1' % chan)
    check(has(a.svc('ChanServ', 'REGISTER %s droppw1 again' % chan), 'successfully registered'),
          'a dropped channel can be registered again at once')
    mm = master()
    mm.svc('ChanServ', 'HOLD %s test' % chan)
    check(wait_db("select hold from chansetting where name = '%s' and hold is not null" % chan, 30) != '',
          'HOLD is stored in the database')
    close(a, mm)


def test_leaf_split():
    chan = '#t_split'
    mm = master()
    mm.send('LINKS')
    time.sleep(1)
    if not any(' 364 ' in l and ' leaf.test.net ' in l for l in mm.since(0)):
        mm.send('CONNECT leaf.test.net 7016')
        time.sleep(8)
    d = login('Dave', port=LEAF_PORT, host='::1')    # see test_host_masking about the throttle
    mm.join(chan)
    d.join(chan)
    time.sleep(2)
    check(not has(mm.svc('OperServ', 'UINFO Dave'), 'offline'), 'services know a user on the leaf')
    mm.send('SQUIT leaf.test.net :split test')
    time.sleep(5)
    check(has(mm.svc('OperServ', 'UINFO Dave'), 'offline'), 'the user is removed when the leaf splits')
    ircd('stop-leaf')
    ircd('start-leaf')
    close(mm, d)


def test_services_relink():
    chan = '#t_relink'
    a = login('Alice')
    register_chan(a, chan)
    mm = master()
    time.sleep(3)
    before = link_count()
    m = a.mark()
    mm.send('SQUIT services.test.net :relink test')
    check(wait_relink(before), 'services link again after ERROR from the hub')
    check(not a.saw(r' MODE %s -o Alice' % chan, m, 3), 'the founder is not deopped after the relink')
    check(has(a.svc('NickServ', 'INFO Alice'), 'Info for'), 'NickServ answers again')
    check(has(a.svc('ChanServ', 'AOP %s LIST' % chan), 'list for'), 'and the user is still identified')
    close(a, mm)


def test_hub_restart():
    before = link_count()
    ircd('restart-hub')
    check(wait_relink(before, 150), 'services wait for the hub and link again when it is back')
    c = login('Alice')
    check(True, 'users can identify after the hub restart')
    close(c)


TESTS = [test_identify, test_throttle, test_access_security, test_topic_sync, test_old_null_topic, test_topiclock,
         test_sessions_survive_restart, test_vop_hop, test_ipv6, test_chanflags, test_vhost,
         test_clone_limit, test_spamfilter_target, test_staff_in_whois, test_panic_without_state,
         test_akick_kicks, test_mask_rank, test_dash_in_channel_name, test_memo, test_nick_privacy_and_mail,
         test_oper_checks, test_dropped_nick_memos, test_help_and_last_login, test_modelock_key, test_uhm, test_host_masking, test_log_file, test_bans,
         test_drop_and_hold, test_leaf_split, test_services_relink, test_hub_restart]


def main():
    words = sys.argv[1:]
    print('bahamut %s' % '.'.join(str(x) for x in ircd_version()))
    setup()
    for test in TESTS:
        name = test.__name__[5:]
        if words and not any(w in name for w in words):
            continue
        current[0] = name
        print('\n' + name)
        try:
            test()
        except Exception as e:
            results.append((name, False, 'error: %s' % e))
            print('    FAIL  error: %s' % e)
            traceback.print_exc(limit=3)
            time.sleep(2)
        finally:
            close_all()             # a failed test must not leave its nicks online
    current[0] = 'avade'
    print('\navade')
    errors = sorted(set(l for l in avade_log().split('\n') if l.startswith('java.lang.')))
    check(not errors, 'no internal errors in .work/run/avade.out since the last start', errors[:5])
    failed = [r for r in results if not r[1]]
    print('\n%d checks, %d failed' % (len(results), len(failed)))
    for name, ok, text in failed:
        print('  FAIL  %s: %s' % (name, text))
    sys.exit(1 if failed else 0)


if __name__ == '__main__':
    main()
