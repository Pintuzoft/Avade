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
import re
import socket
import sys
import time
import traceback

from irctest import (Client, ENV, SERVICES, WORK, close_all, MASTER, LEAF_PORT, avade, avade_log, auth_code,
                     db, ircd, ircd_version, link_count, wait_db, wait_relink)
import email
import glob
import subprocess
from email import policy

USERS = ['Alice', 'Bob', 'Carol', 'Dave', 'Erin', 'Zed', MASTER]
UNAUTHED = 'Nomail'             # registered, email never confirmed
HALFOPS = ircd_version() >= (2, 1, 5)

HERE = os.path.dirname(os.path.abspath(__file__))
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


def link_leaf(oper):
    """Link the leaf to the hub when it is not: it does not come back by itself after a hub restart."""
    m = oper.mark()
    oper.send('LINKS')
    oper.wait(r' 365 ', 10, m)
    if not any(' 364 ' in l and ' %s ' % ENV['LEAF_NAME'] in l for l in oper.since(m)):
        oper.send('CONNECT %s %s' % (ENV['LEAF_NAME'], ENV['LEAF_SERVER_PORT']))
        time.sleep(8)


def register_chan(founder, chan, password='chanpw12'):
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

def test_config_files():
    """template.conf and reference.conf must have the same settings, and the ones the code reads."""
    repo = os.path.dirname(HERE)

    def read(name):
        out, section = {}, None
        for line in open(os.path.join(repo, name), encoding='utf-8'):
            m = re.match(r'^([a-z]+):', line)
            if m:
                section = m.group(1)
                out[section] = []
            elif section and re.match(r'^\s+- ', line):
                out[section].append(line.split('#')[0].strip()[2:].strip().strip('"'))
        return out

    t, r = read('template.conf'), read('reference.conf')
    optional = {'vhostforbidden', 'uhmsalt', 'uhmprefix', 'maillimit'}
    check(set(t) - optional == set(r) - optional, 'template.conf and reference.conf have the same settings',
          sorted(set(t) ^ set(r)))
    lists = ('sra', 'csop', 'sa', 'ircop')
    diff = [a for a in lists if sorted(t.get(a, [])) != sorted(r.get(a, []))]
    check(not diff, 'and the same commands in every access list', diff)
    src = open(os.path.join(repo, 'src', 'core', 'Config.java'), encoding='utf-8').read()
    code = set(c.lower() for c in re.findall(r'[A-Z]+', re.search(r'cList = \{(.*?)\};', src, re.S).group(1)))
    conf = set(c for a in lists for c in t.get(a, []))
    check(code == conf, 'every staff command of Config.java is in the access lists', sorted(code ^ conf))
    need = set(k.lower() for part in re.findall(r'key(?:Strings|Ints|Bools) = \{(.*?)\};', src, re.S)
               for k in re.findall(r'[A-Z]+', part))
    check(need <= set(t), 'every setting Config.java needs is in template.conf', sorted(need - set(t)))

    mt, mr = read('mailer-template.conf'), read('mailer-reference.conf')
    msrc = open(os.path.join(repo, 'mailer', 'src', 'mailer', 'MailerConfig.java'), encoding='utf-8').read()
    mcode = set(re.findall(r'this\.conf, "([a-z]+)"', msrc))
    check(set(mt) == set(mr) == mcode, 'mailer-template.conf, mailer-reference.conf and MailerConfig.java agree',
          sorted((set(mt) ^ set(mr)) | (set(mt) ^ mcode)))


def test_setup():
    """The setup rounds: "avade.jar setup" and "mailer.jar setup" (start.sh used both for this network)."""
    import shutil
    import stat
    import tempfile
    repo = os.path.dirname(HERE)
    cp = os.path.join(WORK, 'classes') + ':' + os.path.join(repo, 'lib', '*')

    def setup(answers, files=None):
        d = tempfile.mkdtemp(dir=WORK)
        shutil.copy(os.path.join(repo, 'template.conf'), d)
        for name, text in (files or {}).items():
            open(os.path.join(d, name), 'w').write(text)
        r = subprocess.run(['java', '-cp', cp, 'main.Main', 'setup'], cwd=d, input='\n'.join(answers) + '\n',
                           capture_output=True, text=True)
        return d, r.returncode, r.stdout

    run = os.path.join(WORK, 'run')
    mode = stat.S_IMODE(os.stat(os.path.join(run, 'services.conf')).st_mode)
    check(mode == 0o600, 'services.conf written by the setup can only be read by its owner', oct(mode))
    hub = open(os.path.join(run, 'hub-setup.txt')).read()
    check('flags   H;' in hub and 'services.test.net' in hub and 'stats.test.net' in hub,
          'the setup gives the lines for the ircd.conf of the hub (this hub uses them)')
    # (the setup never connects to the hub: bahamut refuses an address after a few connections in a row)

    net = ['TestNET', 'test.net', 'Someone', '', '', '', '']
    database = ['127.0.0.1', ENV['DB_PORT'], ENV['DB_NAME'], ENV['DB_USER']]
    d, code, out = setup(net[:2])
    check(code == 1 and 'nothing was written' in out and not os.path.exists(os.path.join(d, 'services.conf')),
          'when the answers stop, nothing is written', out[-200:])
    d, code, out = setup(['TestNET', 'not a domain', 'test.net', 'bad nick!', 'Someone', '', '', 'x', '', ''] +
                         database + [ENV['DB_PASS']])
    check(code == 0 and 'A domain looks like' in out and 'not a nick' in out and 'A port is a number' in out,
          'a wrong answer is explained and asked again', out[-300:])
    conf = open(os.path.join(d, 'services.conf')).read()
    salt = re.search(r"^secretsalt: (\S+)$", conf, re.M)
    check('master: Someone' in conf and 'name: services.test.net' in conf and salt and len(salt.group(1)) == 48
          and 'CHANGE-THIS' not in conf, 'the answers and a random secretsalt are in services.conf', conf[:400])
    d, code, out = setup(net + database + ['wrong-password', 's'])
    check(code == 0 and 'create user if not exists' in out and "identified by 'wrong-password'" in out,
          'when services can not log in to the database, the setup shows what to run as root', out[-600:])

    old = open(os.path.join(run, 'services.conf')).read().replace(' - setpass\n', ' - getpass\n')
    d, code, out = setup(['y'], {'services.conf': old})
    new = open(os.path.join(d, 'services.conf')).read()
    check(code == 0 and 'csop: setpass' in out and 'getpass' in out, 'with a services.conf from an older version it lists what changed', out)
    check(' - setpass' in new and 'getpass' not in new and new.replace(' - setpass\n', '') == old.replace(' - getpass\n', '')
          and os.path.exists(os.path.join(d, 'services.conf.old')),
          'adds the new command, removes the one that is gone, and keeps the old file')
    d, code, out = setup([], {'services.conf': new})
    check(code == 0 and 'every command of this version' in out and open(os.path.join(d, 'services.conf')).read() == new,
          'and leaves a services.conf that is up to date alone', out)

    d = tempfile.mkdtemp(dir=WORK)
    shutil.copy(os.path.join(repo, 'avade.sh'), d)
    open(os.path.join(d, 'avade.jar'), 'w').close()
    r = subprocess.run([os.path.join(d, 'avade.sh'), 'start'], stdin=subprocess.DEVNULL, capture_output=True, text=True)
    check(r.returncode != 0 and 'services.conf is missing' in r.stdout and 'avade.sh setup' in r.stdout,
          'avade.sh start without a terminal never asks, it tells what to run', r.stdout)

    m = os.path.join(WORK, 'mailer')
    mode = stat.S_IMODE(os.stat(os.path.join(m, 'mailer.conf')).st_mode)
    mails = [email.message_from_binary_file(open(f, 'rb')) for f in glob.glob(os.path.join(m, 'smtp', '*.eml'))]
    check(mode == 0o600 and any(x['To'] == 'setup@test.net' and 'Test mail' in x['Subject'] for x in mails),
          'the mailer setup wrote mailer.conf and its test mail reached the mail server', oct(mode))


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
    # bahamut holds back a client that sent many commands (2 s each, up to 10 s ahead)
    who = a.wait(r' 333 ', 20, m).split()
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
    who2 = a.wait(r' 333 ', 20, m).split()
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


def test_services_id_purge():
    """The servicesid table got a row for every identified connection, and none was ever removed."""
    b = login('Bob')
    time.sleep(3)                           # let the services id be written
    close(b)
    before = int(db("select count(*) from servicesid") or 0)
    check(before >= 1, '(there are services ids of users who have left)', before)
    # The same restart shows that a big output file is moved aside (avade.sh says where it is)
    out = os.path.join(WORK, 'run', 'rotate-test.out')
    with open(out, 'w') as f:
        f.write('old line\n' * 500)
    os.environ['AVADE_JAVA_OPTS'] = '-Davade.sidexpire=5 -Davade.out=%s -Davade.outmax=1000' % out   # an unused id is kept for a day
    try:
        avade('restart')
        a = login('Alice')
        gone = wait_db("select 1 from dual where (select count(*) from servicesid) = 1", 200)
        left = db("select nicks from servicesid")
        check(gone == '1' and left == 'Alice', 'the ids nobody uses are removed from the database, the one in use is kept',
              (before, left))
        check(os.path.getsize(out) == 0 and os.path.exists(out + '.1') and os.path.getsize(out + '.1') == 4500,
              'an output file that got big is copied aside and emptied while services run',
              (os.path.getsize(out), os.path.exists(out + '.1')))
    finally:
        del os.environ['AVADE_JAVA_OPTS']
        avade('restart')
    check(has(a.svc('NickServ', 'INFO Alice'), 'Hostmask'), 'and its user is still identified after a restart')
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


def test_join_requests():
    """Services join requests (SJR): the ircd asks, ChanServ decides who gets into the channel."""
    chan, new = '#t_sjr', '#t_sjr_new'
    mm, a = master(), login('Alice')
    register_chan(a, chan)

    def try_join(c, target=chan, key=None):
        """'joined', the numeric of the refusal, or 'nothing'."""
        m = c.mark()
        c.send('JOIN %s%s' % (target, ' ' + key if key else ''))
        try:
            line = c.wait(r'( JOIN :?%s\b| 47[1-7] \S+ %s | 488 )' % (re.escape(target), re.escape(target)), 8, m)
        except TimeoutError:
            return 'nothing'
        return 'joined' if ' JOIN ' in line else line.split()[1]

    def leave(*clients):
        for c in clients:
            c.send('PART ' + chan)
        time.sleep(1)

    def mode(change):
        a.send('MODE %s %s' % (chan, change))
        time.sleep(1)

    def on_ircd(target=chan):
        m = mm.mark()
        mm.send('CHECK CHANNEL ' + target)
        time.sleep(1.5)
        return ' | '.join(mm.since(m))

    mm.svc('OperServ', 'SJR OFF')           # whatever a run that was stopped left behind
    check(has(mm.svc('OperServ', 'SJR'), 'Join requests: OFF'), 'OperServ SJR shows that join requests are off')
    check(not has(a.svc('OperServ', 'SJR ON'), 'set join requests'), 'a user can not turn them on')
    check(has(mm.svc('OperServ', 'SJR maybe'), 'Syntax'), 'a value that is not OFF, ON or ALL is refused')
    r = a.svc('ChanServ', 'CHANFLAG %s SJR ON' % chan)
    check(has(r, 'has now been set') and has(r, 'turned off on this network'),
          'CHANFLAG SJR is set, with a note while the network has them off', r)
    check(has(a.svc('ChanServ', 'CHANFLAG %s LIST' % chan), 'SJR: ON'), 'CHANFLAG LIST shows it')
    row = wait_db("select sjr from chanflag where name = '%s' and sjr = 1" % chan, 90)
    check(row == '1', 'and it is stored in the database', row)
    check('SJR: On' in on_ircd(), 'the ircd has the flag on the channel', on_ircd()[-200:])
    a.svc('ChanServ', 'AKICK %s ADD Evil*!*@*' % chan)
    a.svc('ChanServ', 'AOP %s ADD Bob' % chan)

    r = mm.svc('OperServ', 'SJR ON')
    check(has(r, 'set join requests to ON'), 'an oper turns join requests on', r)
    check(has(mm.svc('OperServ', 'SJR'), 'Join requests: ON'), 'OperServ SJR shows it')
    check(db("select value from settings where name = 'sjr'") == '1', 'and it is stored in the database')
    check(not has(a.svc('ChanServ', 'CHANFLAG %s SJR ON' % chan), 'turned off'), 'no note about the network now')

    c = login('Carol')
    check(try_join(c) == 'joined', 'a user joins a channel with SJR, through services')
    check(has(mm.svc('OperServ', 'CINFO ' + chan), 'Carol'), 'services know that the user is in the channel')
    e = Client('EvilOne')
    m = a.mark()
    check(try_join(e) == '474', 'an akicked user is refused (474) instead of kicked')
    time.sleep(1)
    check(not any('EvilOne' in l for l in a.since(m)), 'and the channel never sees the user', a.since(m)[-3:])
    b = login('Bob')
    m = b.mark()
    check(try_join(b) == 'joined' and b.saw(r' MODE %s \+o Bob' % chan, m, 5), 'someone on the AOP list joins and gets op')

    mode('+k hemlig')
    d = login('Dave')
    check(try_join(d) == '475', 'services check the key: no key is refused (475)')
    check(try_join(d, key='fel') == '475', 'a wrong key too')
    check(try_join(d, key='hemlig') == 'joined', 'the right key gets in')
    mode('-k hemlig')
    leave(d)
    mode('+i')
    check(try_join(d) == '473', 'an invite only channel is refused (473)')
    a.send('INVITE Dave ' + chan)
    time.sleep(1)
    check(try_join(d) == 'joined', 'and an INVITE gets in')
    leave(d)
    mode('-i+I Dave!*@*')
    mode('+i')
    check(try_join(d) == 'joined', 'the invite list (+I) gets in too')
    mode('-iI Dave!*@*')
    leave(d)
    mode('+l 3')                            # Alice, Carol and Bob are in
    check(try_join(d) == '471', 'a full channel is refused (471)')
    mode('-l')
    mode('+b *!*dave@*')
    check(try_join(d) == '474', 'a ban is refused (474)')
    mode('+e Dave!*@*')
    check(try_join(d) == 'joined', 'and an exception (+e) gets around the ban')
    mode('-e Dave!*@*')
    leave(d)
    r = a.svc('ChanServ', 'UNBAN %s Dave' % chan)
    check(try_join(d) == 'joined', 'ChanServ UNBAN takes the ban away for services too', r)
    leave(d)
    p = Client('Passerby')
    mode('+R')
    check(try_join(p) == '477', 'a channel for registered nicks refuses a nick that is not identified (477)')
    check(try_join(d) == 'joined', 'and lets an identified nick in')
    mode('-R')
    leave(d)
    a.svc('ChanServ', 'CHANFLAG %s JOIN_CONNECT_TIME 3600' % chan)
    m = p.mark()
    check(try_join(p) == '473' and p.saw('You must wait', m, 3),
          'the chanflag JOIN_CONNECT_TIME is checked by services, and they say how long to wait')
    a.svc('ChanServ', 'CHANFLAG %s JOIN_CONNECT_TIME 0' % chan)
    mode('+j 2:10')
    check(try_join(p) == '471', 'the join rate (+j) is kept by services: nobody joins right after it is set')
    time.sleep(6)
    check(try_join(p) == 'joined', 'and someone does when the time has passed')
    mode('-j')
    close(p)

    a.svc('ChanServ', 'SET %s RESTRICT ON' % chan)
    m = d.mark()
    check(try_join(d) == '473' and d.saw('restricted to the users on its access lists', m, 3),
          'a RESTRICT channel refuses a user without access, and says why')
    leave(b)
    check(try_join(b) == 'joined', 'and lets someone on an access list in')
    a.svc('ChanServ', 'SET %s RESTRICT OFF' % chan)

    link_leaf(mm)                           # a server that links now gets the setting too
    x = login('Erin', port=LEAF_PORT, host='::1')
    check(try_join(x) == 'joined', 'a user on another server joins through services')
    y = Client('EvilTwo', LEAF_PORT, host='::1')
    check(try_join(y) == '474', 'and an akicked user there is refused')
    close(c, d, x, y)

    # A services restart: the setting is sent to the servers again
    close(mm, a, b)
    avade('restart')
    mm, a = master(), login('Alice')
    check(has(mm.svc('OperServ', 'SJR'), 'Join requests: ON'), 'join requests are still on after a restart')
    check(try_join(a) == 'joined', 'a user joins through the restarted services')
    time.sleep(1)       # the channel is new, services give it its flags when it is created
    check('SJR: On' in on_ircd(), 'a channel that is created again gets the flag from services', on_ircd()[-200:])
    check(try_join(e) == '474', 'and the akicked user is still refused')

    # ALL: also channels without the flag, and channels that do not exist yet
    check(has(mm.svc('OperServ', 'SJR ALL'), 'set join requests to ALL'), 'join requests for every channel')
    z = login('Zed')
    m = z.mark()
    check(try_join(z, new) == 'joined' and z.saw(r' 353 .* %s :@Zed' % new, m, 5),
          'a new channel is created through services, with op for the first user')
    check(has(mm.svc('OperServ', 'CINFO ' + new), 'Zed'), 'services know the new channel')
    z.send('MODE %s +b *!*carol@*' % new)
    time.sleep(1)
    c = login('Carol')
    check(try_join(c, new) == '474', 'a ban in a channel that is not registered is checked by services')
    z.send('MODE %s -b *!*carol@*' % new)
    time.sleep(1)
    check(try_join(c, new) == 'joined', 'and without the ban the user joins')

    check(has(mm.svc('OperServ', 'SJR OFF'), 'set join requests to OFF'), 'join requests are turned off')
    m = e.mark()
    e.send('JOIN ' + chan)
    check(e.saw(r' KICK %s EvilOne ' % chan, m, 8), 'the akicked user is let in by the ircd and kicked, like before')
    a.svc('ChanServ', 'CHANFLAG %s SJR OFF' % chan)
    a.svc('ChanServ', 'AKICK %s DEL Evil*!*@*' % chan)
    a.svc('ChanServ', 'AOP %s DEL Bob' % chan)
    close(mm, a, c, e, z)


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


def test_common_errors():
    """The replies for not registered, access denied, frozen and closed are one method for 18 commands."""
    chan, none = '#t_err', '#t_err_never_registered'
    mm, a, b = master(), login('Alice'), login('Bob')
    register_chan(a, chan)
    for command in ('INFO %s', 'AOP %s LIST', 'CHANFLAG %s LIST', 'WHY %s Alice', 'ACCESSLOG %s', 'LISTOPS %s',
                    'IDENTIFY %s chanpw12', 'DROP %s chanpw12'):
        r = a.svc('ChanServ', command % none, wait=0.4)
        check(has(r, 'is not registered'), command.split()[0] + ' on a channel that is not registered says so', r)
    for command in ('AOP %s ADD Carol', 'CHANFLAG %s NO_CTCP ON', 'ACCESSLOG %s', 'MKICK %s', 'MDEOP %s'):
        r = b.svc('ChanServ', command % chan, wait=0.4)
        check(has(r, 'Access denied'), command.split()[0] + ' without access is denied', r)
    mm.svc('ChanServ', 'FREEZE ' + chan)
    for command in ('AOP %s LIST', 'CHANFLAG %s LIST', 'OP %s Alice', 'LISTOPS %s', 'MKICK %s'):
        r = a.svc('ChanServ', command % chan, wait=0.4)
        check(has(r, 'is Frozen'), command.split()[0] + ' on a frozen channel says that it is frozen', r)
    mm.svc('ChanServ', 'FREEZE -' + chan)
    check(has(a.svc('ChanServ', 'AOP %s LIST' % chan), 'End of'), 'and works again when the channel is not frozen')
    mm.svc('ChanServ', 'CLOSE ' + chan)
    for command in ('AOP %s LIST', 'CHANFLAG %s LIST', 'ACCESSLOG %s', 'IDENTIFY %s chanpw12'):
        r = a.svc('ChanServ', command % chan, wait=0.4)
        check(has(r, 'is Closed'), command.split()[0] + ' on a closed channel says that it is closed', r)
    mm.svc('ChanServ', 'CLOSE -' + chan)
    close(mm, a, b)


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


def test_memo_limits():
    """A nick holds 30 memos, and 5 unread ones from the same sender."""
    mm = master()                           # an oper: the ircd does not slow down its 30 DELs
    mm.svc('NickServ', 'SET MAILBLOCK ON')  # no mail per memo
    def box():
        m = re.search(r'of (\d+) memos', ' '.join(mm.svc('MemoServ', 'LIST', wait=0.5)))
        return int(m.group(1)) if m else -1
    def empty():
        for i in range(max(box(), 0)):
            mm.svc('MemoServ', 'DEL 1', wait=0.1)
    empty()                                 # what a run that was stopped left behind
    a = login('Alice')
    for i in range(5):
        r = a.svc('MemoServ', 'SEND %s memo %d' % (MASTER, i), wait=0.2)
    check(has(r, 'Memo sent'), 'five memos to the same nick are sent', r)
    r = a.svc('MemoServ', 'SEND %s one too many' % MASTER)
    check(has(r, 'not read yet') and not has(r, 'Memo sent'), 'the 6th unread memo from the same sender is refused', r)
    mm.svc('MemoServ', 'READ 1')
    r = a.svc('MemoServ', 'SEND %s after one was read' % MASTER)
    check(has(r, 'Memo sent'), 'and accepted when the receiver has read one', r)
    for nick in ('Bob', 'Carol', 'Dave', 'Erin'):
        c = login(nick)
        for i in range(5):
            c.svc('MemoServ', 'SEND %s memo %d' % (MASTER, i), wait=0.2)
        close(c)
    z = login('Zed')
    for i in range(4):
        r = z.svc('MemoServ', 'SEND %s memo %d' % (MASTER, i), wait=0.2)
    check(has(r, 'Memo sent') and box() == 30, 'a memo box takes 30 memos', (r, box()))
    r = z.svc('MemoServ', 'SEND %s number 31' % MASTER)
    check(has(r, 'is full') and box() == 30, 'the 31st is refused', r)
    count = db("select count(*) from memo where name = '%s'" % MASTER)
    check(count == '30', 'and not stored', count)
    mm.svc('MemoServ', 'DEL 1')
    check(has(z.svc('MemoServ', 'SEND %s room again' % MASTER), 'Memo sent'), 'until the receiver deletes one')
    empty()
    check(box() == 0, 'the box is empty again', box())
    mm.svc('NickServ', 'SET MAILBLOCK OFF')
    close(mm, a, z)


def test_nick_privacy_and_mail():
    a, c = login('Alice'), login('Carol')
    check(not has(c.svc('NickServ', 'INFO Alice'), 'Hostmask'), 'INFO hides the hostmask of someone else')
    check(has(a.svc('NickServ', 'INFO Alice'), 'Hostmask'), 'and shows it to the owner')
    f = [l for l in c.svc('ChanServ', 'INFO #t_topic') if 'Founder' in l]
    check(f and '@' not in f[0], 'ChanServ INFO does not show the host of the founder to others', f)
    f = [l for l in a.svc('ChanServ', 'INFO #t_topic') if 'Founder' in l]
    check(f and '@' in f[0], 'but shows it to the founder', f)
    check(has(a.svc('NickServ', 'SET NOOP banana'), 'Syntax'), 'SET needs ON or OFF')
    r = c.svc('NickServ', 'SET EMAIL %s carol-new@test.net' % pw('Carol'))
    mail = wait_db("select subject from mailbox where mail = 'carol-new@test.net'", 150)
    check(mail != '', 'SET EMAIL sends a confirmation mail to the new address', (r, mail))
    r = c.svc('NickServ', 'SET EMAIL %s carol@test.online' % pw('Carol'))
    check(not has(r, 'not a valid'), 'an address with a long top level domain (.online) is valid', r)
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
    link_leaf(mm)
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
        close(x, v)
        # SET SHOWHOST: someone with a host of their own to show gets the real one back
        e = login('Erin')
        time.sleep(1)
        host = lambda: [t.split()[2] for n, t in a.whois('Erin') if n == '311'][0]
        masked = host()
        check(masked.startswith('avade-') or masked.endswith('.ip'), '(an identified user is masked too)', masked)
        e.svc('NickServ', 'SET SHOWHOST ON')
        time.sleep(1)
        real = host()
        check(real != masked and not real.endswith('.ip') and not real.startswith('avade-'),
              'SET SHOWHOST ON shows the real host on IRC', (masked, real))
        e.svc('NickServ', 'SET SHOWHOST OFF')
        time.sleep(1)
        check(host() == masked, 'and SET SHOWHOST OFF puts the mask back', host())
        close(a, e)
    finally:
        mm.svc('OperServ', 'UHM 0 0')
    close(mm)


def test_ban_follows():
    """A ban on a vhost must still hold when the user comes back showing another host."""
    chan = '#t_follow'
    mm = master()
    mm.svc('OperServ', 'VHOST Carol carol.har.en.vhost')
    close(mm)
    a = login('Alice')
    register_chan(a, chan)
    c = login('Carol')
    time.sleep(1)
    c.join(chan)
    time.sleep(1)
    seen = [l for l in a.since(0) if ' JOIN ' in l and l.startswith(':Carol!')][-1]
    check('@carol.har.en.vhost' in seen, '(the user is shown with a vhost)', seen)
    a.send('MODE %s +b *!*@carol.har.en.vhost' % chan)
    a.send('KICK %s Carol :ut' % chan)
    time.sleep(1)
    close(c)
    # Back without identifying: no vhost, the ban on it does not match any more
    c = Client('Carol')
    time.sleep(1)
    m, ma = c.mark(), a.mark()
    c.send('JOIN ' + chan)
    check(c.saw(r' KICK %s Carol ' % chan, m, 8), 'a ban on a vhost follows the user who comes back without it', c.since(m)[-3:])
    ban = [l for l in a.since(ma) if ' MODE %s ' % chan in l and '+b' in l]
    check(ban and 'carol.har.en.vhost' not in ban[-1] and '127.0.0.1' not in ban[-1],
          'and the new ban is on the host the user shows now', ban)
    # Somebody else from another address is not touched
    b = Client('Bystander', host='::1')
    m = b.mark()
    b.send('JOIN ' + chan)
    check(not b.saw(r' KICK %s Bystander ' % chan, m, 3), 'a user from another address can join', b.since(m)[-3:])
    # Remove the bans: the user is welcome again
    for l in ban:
        a.send('MODE %s -b %s' % (chan, l.split()[-1]))
    a.send('MODE %s -b *!*@carol.har.en.vhost' % chan)
    time.sleep(1)
    m = c.mark()
    c.send('JOIN ' + chan)
    check(c.saw(r' JOIN :?%s' % chan, m, 5) and not c.saw(r' KICK %s Carol ' % chan, m, 3),
          'when the ban is removed the user can join again', c.since(m)[-3:])
    close(a, b, c)
    mm = master()
    mm.svc('OperServ', 'VHOST Carol OFF')
    close(mm)


def test_passwords():
    # Stored as one way hashes, nothing in the database can be decrypted
    check(db("select count(*) from passlog where pass not like 'pbkdf2-sha256$%'") == '0',
          'every nick password is stored as a hash')
    check(db("select count(*) from chan where pass not like 'pbkdf2-sha256$%'") == '0',
          'every channel password is stored as a hash')
    m = master()
    r = m.svc('NickServ', 'GETPASS Alice') + m.svc('ChanServ', 'GETPASS #t_topic')
    check(not has(r, pw('Alice')) and not has(r, 'chanpw12') and not has(r, 'Password is'),
          'GETPASS is gone from NickServ and ChanServ', r)
    check(not has(m.svc('OperServ', 'NINFO Alice'), 'pass:'), 'NINFO does not show a password')

    # A pending SET PASSWD from before the upgrade still works with its code
    if db("select count(*) from passlog where nick = 'Bob' and auth = 'aaaabbbbccccddddeeeeffff00001111'") == '1':
        b = login('Bob')
        check(has(b.svc('NickServ', 'AUTH aaaabbbbccccddddeeeeffff00001111'), 'fully authed'),
              'a password change waiting from before the upgrade can be confirmed')
        close(b)
        b = Client('Bob')
        check(has(b.svc('NickServ', 'IDENTIFY Bobnewpass12'), 'Password accepted'), 'and the new password works')
        close(b)
        m.svc('NickServ', 'SETPASS Bob ' + pw('Bob'))

    # RESETPASS: a code to the confirmed mail, anyone can ask
    x = Client('Resetter')
    r = x.svc('NickServ', 'RESETPASS Erin')
    check(has(r, 'code has been mailed'), 'RESETPASS mails a code', r)
    body = wait_db("select body from mailbox where mail = 'erin@test.net' and subject = 'Reset your password' "
                   "order by id desc limit 1", 30)
    code = re.search(r'RESETPASS Erin (\w+)', body)
    check(code, 'the mail has the code', body)
    code = code.group(1) if code else 'none'
    check(has(x.svc('NickServ', 'RESETPASS Erin'), 'short while ago'), 'a second code is not sent at once')
    check(has(x.svc('NickServ', 'RESETPASS Erin wrongcode123 Erinreset123'), 'code is wrong'), 'a wrong code is refused')
    check(has(x.svc('NickServ', 'RESETPASS Erin %s short' % code), 'not valid'), 'a short password is refused')
    check(has(x.svc('NickServ', 'RESETPASS Erin %s Erinreset123' % code), 'has been changed'),
          'the code sets a new password')
    check(has(x.svc('NickServ', 'RESETPASS Erin %s Erinreset456' % code), 'code is wrong'), 'the code works once')
    check(has(x.svc('NickServ', 'RESETPASS Nomail'), 'no confirmed mail'), 'not without a confirmed mail')
    close(x)
    e = Client('Erin')
    check(not has(e.svc('NickServ', 'IDENTIFY ' + pw('Erin')), 'accepted'), 'the old password does not work')
    check(has(e.svc('NickServ', 'IDENTIFY Erinreset123'), 'Password accepted'), 'the new one does')
    close(e)

    # SETPASS by staff, never for staff with the same or higher access
    a = login('Alice')
    check(not has(a.svc('NickServ', 'SETPASS Bob Alicehack123'), 'has been set'), 'a user can not use SETPASS')
    check(has(m.svc('NickServ', 'SETPASS %s Masterhack123' % MASTER), 'Access denied'),
          'SETPASS refuses a staff nick with the same access')
    check(has(m.svc('NickServ', 'SETPASS Erin ' + pw('Erin')), 'has been set'), 'staff sets a new password')
    e = login('Erin')
    check(True, 'and the owner identifies with it')
    check(wait_db("select count(*) from passlog where nick = 'Erin' and pass like 'pbkdf2-sha256$%' "
                  "having count(*) >= 3", 150) != '', 'the new passwords are stored as hashes')
    check(db("select count(*) from passlog where pass like '%Erinreset%' or pass like '%Erinpass%'") == '0',
          'no password in clear in the database')

    # Channel: at least 8 characters, founder SET PASSWD, staff SETPASS
    a.join('#t_shortpw')
    check(has(a.svc('ChanServ', 'REGISTER #t_shortpw short77 test'), 'not valid'),
          'REGISTER needs a channel password of at least 8 characters')
    chan = '#t_passwd'
    register_chan(a, chan)
    b = login('Bob')
    b.join(chan)
    check(not has(b.svc('ChanServ', 'SET %s PASSWD Bobhack1234' % chan), 'has been set'), 'only the founder sets it')
    check(has(a.svc('ChanServ', 'SET %s PASSWD short' % chan), 'not valid'), 'a short channel password is refused')
    check(has(a.svc('ChanServ', 'SET %s PASSWD chanpw-new1' % chan), 'has been set'), 'the founder sets a new password')
    check(not has(b.svc('ChanServ', 'IDENTIFY %s chanpw12' % chan), 'accepted'), 'the old channel password does not work')
    check(has(b.svc('ChanServ', 'IDENTIFY %s chanpw-new1' % chan), 'Password accepted'), 'the new one does')
    mk = b.mark()
    check(has(m.svc('ChanServ', 'SETPASS %s chanpw-staff1' % chan), 'has been set'), 'staff sets a channel password')
    check(b.saw('unidentified from the channel', mk, 4), 'who identified with the old one is unidentified')
    check(has(b.svc('ChanServ', 'IDENTIFY %s chanpw-staff1' % chan), 'Password accepted'), 'the staff password works')
    check(not has(b.svc('ChanServ', 'DELETE %s' % chan), 'deleted'), 'a user can not DELETE a channel')
    close(a, b, e, m)


def test_mailer():
    """AvadeMailer sends what Avade puts in the mailbox, here to tests/smtp.py."""
    smtp = os.path.join(WORK, 'mailer', 'smtp')

    def received():
        out = {}
        for f in glob.glob(os.path.join(smtp, '*.eml')):
            m = email.message_from_binary_file(open(f, 'rb'), policy=policy.default)
            out.setdefault(m['X-Avade-Mail-Id'], []).append(m)
        return out

    def add(to, subject, body):
        db("insert into mailbox (mail, subject, body, stamp, status) values ('%s', '%s', '%s', unix_timestamp(), 1)"
           % (to, subject, body))
        return db("select max(id) from mailbox where mail = '%s'" % to)

    def status(mail_id, want, timeout=20):
        return wait_db("select status from mailbox where id = %s and status = %d" % (mail_id, want), timeout) != ''

    def tried(addr):
        try:
            return open(os.path.join(smtp, 'rcpt.log')).read().split().count(addr)
        except OSError:
            return 0

    def mailer(*args):
        return subprocess.run([os.path.join(HERE, 'mailer.sh'), 'cmd'] + list(args), capture_output=True, text=True).stdout

    x = Client('Mailtest')
    x.svc('NickServ', 'RESETPASS Dave')
    close(x)
    mid = wait_db("select max(id) from mailbox where mail = 'dave@test.net' and subject = 'Reset your password'", 30)
    check(mid and mid != 'NULL' and status(mid, 0), 'a mail from services is sent and marked sent', mid)
    got = received().get(mid, [])
    check(len(got) == 1 and 'RESETPASS Dave' in got[0].get_content(), 'the mail server gets it once, with its text')

    mid = add('utf8@test.net', 'Hej \u00e5\u00e4\u00f6 \u2615', 'R\u00e4ksm\u00f6rg\u00e5s \U0001f990\\n.\\nslut')
    status(mid, 0)
    got = received().get(mid, [])
    check(got and got[0]['Subject'] == 'Hej \u00e5\u00e4\u00f6 \u2615', 'subject in UTF-8', got and got[0]['Subject'])
    check(got and got[0].get_content() == 'R\u00e4ksm\u00f6rg\u00e5s \U0001f990\n.\nslut\n',
          'text in UTF-8, a line with only a dot survives', got and got[0].get_content())

    rejects, laters = tried('reject@test.net'), tried('later@test.net')
    mid = add('reject@test.net', 'refused', 'x')
    check(status(mid, 500), 'a mail the server refuses (550) is marked failed')
    mid2 = add('later@test.net', 'later', 'y')
    check(status(mid2, 500, 30) and tried('later@test.net') - laters == 2,
          'a mail the server turns away for now (451) is tried twice (retries: 2), then failed',
          tried('later@test.net') - laters)
    check('1 mails will be sent again' in mailer('resend', mid), 'resend puts a failed mail back')
    check(status(mid, 500) and tried('reject@test.net') - rejects == 2, 'and it is tried again',
          tried('reject@test.net') - rejects)

    # send: false, a test network with real addresses
    subprocess.run([os.path.join(HERE, 'mailer.sh'), 'stop'], capture_output=True)
    conf = os.path.join(WORK, 'mailer', 'mailer.conf')
    dry = os.path.join(WORK, 'mailer', 'dry.conf')
    open(dry, 'w').write(open(conf).read().replace('send: true', 'send: false'))
    before = len(glob.glob(os.path.join(smtp, '*.eml')))
    mid = add('dry@test.net', 'not sent', 'z')
    mailer('-c', 'dry.conf', 'once')
    check(status(mid, 3, 5) and len(glob.glob(os.path.join(smtp, '*.eml'))) == before,
          'send: false sends nothing and marks the mail not sent (3)')
    subprocess.run([os.path.join(HERE, 'mailer.sh'), 'restart'], capture_output=True)

    dup = [i for i, ms in received().items() if len(ms) > 1]
    check(not dup, 'no mail is sent twice', dup)
    out = open(os.path.join(WORK, 'mailer', 'mailer.out')).read() + open(os.path.join(WORK, 'mailer', 'mailer.log')).read()
    check(ENV['DB_PASS'] not in out and 'RESETPASS Dave' not in out, 'the log has no password and no mail text')


def test_log_file():
    log = open(os.path.join(os.path.dirname(os.path.abspath(__file__)), '.work', 'run', 'services.log'),
               errors='replace').read()
    check(log.count('\n') > 3, 'services.log is written, one line per entry', log[:200])


def test_services_own_kill():
    """A user that services kill themselves stayed for ever: the ircd does not send such a kill back."""
    chan = '#t_ownkill'
    mm = master()
    v = Client('Gecosvic')                  # its realname is "Gecosvic test"
    v.join(chan)
    time.sleep(1)
    check(has(mm.svc('OperServ', 'CINFO ' + chan), 'Gecosvic'), '(services know the user and the channel it made)')
    mm.svc('OperServ', 'SGLINE TIME 10m Gecosvic* test of a kill by services', wait=3)
    check(v.saw(r'(Closing Link| KILL )', 0, 8), 'SGLINE kills the users it matches', v.since(0)[-2:])
    check(has(mm.svc('OperServ', 'UINFO Gecosvic'), 'offline'), 'and services have removed the user')
    check(not has(mm.svc('OperServ', 'CINFO ' + chan), 'Name:'), 'and the channel it was alone in')
    mm.svc('OperServ', 'SGLINE DEL Gecosvic*')
    close(mm)


def test_codes_of_a_former_owner():
    """A code that was mailed for a nick still worked when the nick had been dropped and registered by someone else."""
    t = Client('Oldowner')
    t.svc('NickServ', 'REGISTER oldpw1234 oldowner@test.net')
    check(wait_db("select name from nick where name = 'Oldowner'", 15) != '', 'a registration is written within seconds')
    check(wait_db("select auth from maillog where nick = 'Oldowner' and auth is not null", 15) != '', '(its mail code is waiting)')
    t.svc('NickServ', 'DROP oldpw1234')
    left = '1'
    for i in range(20):
        left = db("select count(*) from maillog where nick = 'Oldowner' and auth is not null")
        if left == '0':
            break
        time.sleep(1)
    check(left == '0', 'the unused codes of a dropped nick are removed with it', left)
    close(t)
    # A code from before the nick was registered (its former owner still has the mail) does nothing
    code = '0000111122223333444455556666ffff'
    db("insert into passlog (nick,pass,auth,stamp) values ('Alice','pbkdf2-sha256$1$AAAA$AAAA','%s','2001-01-01 00:00:00')" % code)
    a = login('Alice', identify=False)
    r = a.svc('NickServ', 'AUTH ' + code)
    check(has(r, 'did not match') and not has(r, 'fully authed'), 'a code that is older than the registration is refused', r)
    check(has(a.svc('NickServ', 'IDENTIFY ' + pw('Alice')), 'Password accepted'), 'and the password of the nick is what it was')
    db("delete from passlog where auth = '%s'" % code)
    close(a)


def test_channel_identify_is_stored():
    """An identification to a channel was not written to the session, and a drop left it in the sessions that were stored."""
    chan = '#t_cident'
    a, b = login('Alice'), login('Bob')
    register_chan(a, chan, 'cidentpw1')
    check(has(b.svc('ChanServ', 'IDENTIFY %s cidentpw1' % chan), 'Password accepted'), '(a user identifies to a channel)')
    row = wait_db("select chans from servicesid where nicks like '%%Bob%%' and chans like '%%%s%%'" % chan, 15)
    check(row != '', 'the session in the database has the channel, it is kept over a restart', row)
    a.svc('ChanServ', 'DROP %s cidentpw1' % chan)
    left = '1'
    for i in range(20):
        left = db("select count(*) from servicesid where chans like '%%%s%%'" % chan)
        if left == '0':
            break
        time.sleep(1)
    check(left == '0', 'and no session is identified to a channel that is dropped', left)
    close(a, b)


def test_web_command_without_nick():
    """A row in the web command table that named no nick ended the main loop of services."""
    db("insert into command (target,targettype,command,extra) values (NULL,'NICKINFO','AUTH','x'), ('Nosuchnick77','NICKINFO','AUTH','x')")
    left = '2'
    for i in range(20):
        left = db("select count(*) from command")
        if left == '0':
            break
        time.sleep(1)
    check(left == '0', 'web commands that name no registered nick are removed', left)
    a = login('Alice')
    check(has(a.svc('NickServ', 'INFO Alice'), 'Hostmask'), 'and services still answer')
    close(a)


def test_snoop_hides_passwords():
    """More than one space, or the arguments the wrong way round, put a password where the snoop did not mask it."""
    mm = master()
    mm.join('#Snoop')
    time.sleep(1)
    m = mm.mark()
    u = Client('Snooper1')
    u.svc('NickServ', 'IDENTIFY   hemligt91ord')                # three spaces
    u.svc('NickServ', 'IDENTIFY hemligt92ord Alice')            # password first
    u.svc('NickServ', 'GHOST hemligt93ord Alice')
    u.svc('NickServ', ' IDENTIFY hemligt94ord')                 # a space before the command
    u.svc('ChanServ', 'IDENTIFY #t_topic hemligt95 ord')        # a password in two words
    u.svc('ChanServ', 'IDENTIFY  #t_topic  hemligt96ord')       # two spaces
    time.sleep(2)
    lines = [l for l in mm.since(m) if ' PRIVMSG #Snoop ' in l and 'Snooper1' in l]
    check(len(lines) >= 5, '(the snoop channel shows what the user did)', lines)
    leaked = [l for l in lines if 'hemligt9' in l]
    check(not leaked, 'no password is shown in the snoop channel', leaked)
    check(any('#t_topic' in l for l in lines), 'what is not secret is still shown', lines[-2:])
    close(mm, u)


def test_mkick_forgets_channel():
    """After MKICK and CLOSE the channel is gone in the ircd, services kept it with its key and bans."""
    chan = '#t_mkick'
    mm, a, b = master(), login('Alice'), login('Bob')
    register_chan(a, chan)
    b.join(chan)
    a.send('MODE %s +k nyckel' % chan)
    time.sleep(1)
    check(has(mm.svc('OperServ', 'CINFO ' + chan), 'Name:'), '(services know the channel)')
    m = b.mark()
    a.svc('ChanServ', 'MKICK ' + chan)
    check(b.saw(r' KICK %s Bob ' % chan, m, 5), '(MKICK removes everyone)')
    r = mm.svc('OperServ', 'CINFO ' + chan)
    check(not has(r, 'Name:'), 'services have forgotten the channel that MKICK emptied', r)
    close(mm, a, b)


def test_database_down():
    """Services work from memory while the database is away, and write every change when it is back."""
    chan, old, new = '#t_dbdown', '#t_dbdown_old', '#t_dbdown_new'

    def root(query):
        return subprocess.run(['docker', 'exec', ENV['DB_CONTAINER'], 'sh', '-c',
                               'mariadb -uroot -p"$MARIADB_ROOT_PASSWORD" -e "%s"' % query], capture_output=True)

    mm, a, b = master(), login('Alice'), login('Bob')
    for nick in ('Dbdownnick', 'Dbdownre', 'Dbdownro', 'Dbdownna'):
        mm.svc('NickServ', 'DELETE ' + nick)        # whatever a run that was stopped left behind
    register_chan(a, chan)
    register_chan(a, old, 'gammalpw1')
    o = Client('Dbdownre')
    o.svc('NickServ', 'REGISTER forstapw12 first@test.net')
    for c in (chan, old):
        check(wait_db("select name from chan where name = '%s'" % c, 30) != '', '(%s is in the database)' % c)
    check(wait_db("select name from nick where name = 'Dbdownre'", 30) != '', '(a nick that will get a new owner is in the database)')
    a.svc('NickServ', 'SET NOOP OFF')
    a.svc('ChanServ', 'AOP %s DEL Bob' % chan)
    a.svc('ChanServ', 'AKICK %s DEL Evil*!*@*' % chan)
    mm.svc('OperServ', 'AKILL DEL *!*@203.0.113.99')
    mm.svc('OperServ', 'SJR OFF')
    b.svc('MemoServ', 'DEL 1')
    time.sleep(3)
    memos = int(db("select count(*) from memo where name = 'Bob'") or 0)
    mails = int(db("select count(*) from mailbox where mail = 'bob@test.net'") or 0)

    mg = mm.mark()
    subprocess.run(['docker', 'stop', ENV['DB_CONTAINER']], capture_output=True)
    try:
        time.sleep(3)
        t0 = time.time()
        r = a.svc('NickServ', 'INFO Bob')
        check(has(r, 'Bob') and time.time() - t0 < 4, 'services answer at once without the database', time.time() - t0)
        # NickServ
        n = Client('Dbdownnick')
        check(has(n.svc('NickServ', 'REGISTER nerepw1234 dbdown@test.net'), 'successfully registered'), 'a nick is registered without the database')
        check(has(a.svc('NickServ', 'SET NOOP ON'), 'NOOP'), 'a nick setting is changed')
        c = login('Carol')
        check(c is not None, 'a user identifies')
        r = n.svc('NickServ', 'AUTH 00001111222233334444555566667777')
        check(has(r, 'Database not available'), 'AUTH says that it needs the database, not that the code is wrong', r)
        # a nick is dropped and registered by someone else, all while the database is away
        check(has(o.svc('NickServ', 'DROP forstapw12'), 'dropped'), 'a nick is dropped')
        close(o)
        o = Client('Dbdownre')
        check(has(o.svc('NickServ', 'REGISTER andrapw1234 second@test.net'), 'successfully registered'), 'and registered again by someone else')
        # ChanServ
        a.join(new)
        check(has(a.svc('ChanServ', 'REGISTER %s nykanalpw1 made without the database' % new), 'successfully registered'), 'a channel is registered')
        check(has(a.svc('ChanServ', 'AOP %s ADD Bob' % chan), 'added to the Aop'), 'an AOP is added')
        when = int(time.time())
        check(has(a.svc('ChanServ', 'AKICK %s ADD Evil*!*@*' % chan), 'added'), 'an AKICK is added')
        a.send('TOPIC %s :satt utan databas' % chan)
        m = b.mark()
        b.join(chan)
        check(b.saw(r' MODE %s \+o Bob' % chan, m, 5), 'the new AOP gets op: the change is in use at once')
        check(has(a.svc('ChanServ', 'DROP %s gammalpw1' % old), 'dropped'), 'a channel is dropped')
        # MemoServ
        r = a.svc('MemoServ', 'SEND Bob skickat utan databas')
        check(not has(r, 'rror') and not has(r, 'not available'), 'a memo is sent', r)
        check(has(b.svc('MemoServ', 'LIST'), 'Alice'), 'the receiver has it')
        check(has(b.svc('MemoServ', 'READ 1'), 'skickat utan databas'), 'and reads it')
        a.svc('MemoServ', 'SEND Bob den har tas bort igen')
        r = b.svc('MemoServ', 'DEL 2')
        check(not has(r, 'rror'), 'a memo is deleted', r)
        # OperServ
        r = mm.svc('OperServ', 'AKILL TIME 10m *!*@203.0.113.99 test without the database', wait=3)
        check(has(mm.svc('OperServ', 'AKILL LIST *203.0.113.99*'), '203.0.113.99'), 'an AKILL is added', r)
        check(has(mm.svc('OperServ', 'SJR ON'), 'set join requests to ON'), 'join requests are turned on')
        time.sleep(5)
        t0 = time.time()
        r = a.svc('NickServ', 'INFO Alice')
        check(has(r, 'Hostmask') and time.time() - t0 < 4, 'and services still answer at once after all that', time.time() - t0)
        # the staff are told what waits, in words
        told = ''
        end = time.time() + 40
        while time.time() < end:
            lines = [l for l in mm.since(mg) if 'Database down, waiting:' in l and ' oper ' in l]
            if lines:
                told = lines[-1]
                break
            time.sleep(1)
        check(re.search(r'waiting: nicks \d+, chans \d+, access \d+, memos \d+, mails \d+, oper \d+, other \d+$', told)
              and len(told.split('waiting:')[-1]) < 80,
              'the notice to the staff says what waits to be written, in one short line', told[-160:])
    finally:
        subprocess.run(['docker', 'start', ENV['DB_CONTAINER']], capture_output=True)
    check(wait_db("select 1", 60) == '1', '(the database is back)')
    check(mm.saw(r'Database back, writing: ', mg, 45), 'and that the database is back')

    check(wait_db("select name from nick where name = 'Dbdownnick'", 60) != '', 'the new nick is written when the database is back')
    check(wait_db("select count(*) from passlog where nick = 'Dbdownnick' having count(*) > 0", 30) != '', 'with its password')
    check(wait_db("select count(*) from maillog where nick = 'Dbdownnick' having count(*) > 0", 30) != '', 'and its mail code')
    check(wait_db("select count(*) from mailbox where subject like '%%Dbdownnick%%' or body like '%%Dbdownnick%%' having count(*) > 0", 30) != '',
          'the mail to confirm it is sent')
    check(wait_db("select noop from nicksetting where name = 'Alice' and noop = 1", 30) == '1', 'the nick setting')
    time.sleep(5)
    row = db("select (select count(*) from nick where name = 'Dbdownre'), (select count(*) from nicksetting where name = 'Dbdownre'), "
             "(select count(*) from maillog where nick = 'Dbdownre' and auth is not null), "
             "(select count(*) from passlog where nick = 'Dbdownre' and stamp >= (select regstamp from nick where name = 'Dbdownre'))")
    check(row.split() == ['1', '1', '1', '1'],
          'the nick with a new owner has its row, its settings, its mail code and its password, the old ones are gone', row)
    check(wait_db("select name from chan where name = '%s'" % new, 30) != '', 'the new channel')
    check(wait_db("select access from chanaccess where name = '%s' and access = 'aop'" % chan, 30) == 'aop', 'the AOP')
    check(wait_db("select access from chanaccess_mask where name = '%s' and access = 'akick'" % chan, 30) == 'akick', 'the AKICK')
    check(wait_db("select topic from topiclog where name = '%s' and topic = 'satt utan databas'" % chan, 30) != '', 'the topic')
    gone = '1'
    for i in range(30):
        gone = db("select count(*) from chan where name = '%s'" % old)
        if gone == '0':
            break
        time.sleep(1)
    check(gone == '0', 'the dropped channel is removed', gone)
    row = wait_db("select count(*), max(readflag) from memo where name = 'Bob' and message = 'skickat utan databas'", 30)
    check(row.split() == ['1', '1'], 'the memo is stored, as read', row)
    check(db("select count(*) from memo where name = 'Bob'") == str(memos + 1), 'the memo that was deleted again is not', memos)
    # what waited keeps the time it happened, not the time the database came back (a minute later)
    late = db("select (select stamp from memo where name = 'Bob' and message = 'skickat utan databas') - %d, "
              "(select unix_timestamp(max(stamp)) from chanacclog where name = '%s') - %d" % (when, chan, when))
    check(len(late.split()) == 2 and all(abs(int(x)) < 30 for x in late.split()),
          'the memo and the access log have the time they were made, not the time they were written', late)
    check(wait_db("select count(*) from mailbox where mail = 'bob@test.net' having count(*) > %d" % mails, 30) != '',
          'the mail about the new memo is sent')
    check(wait_db("select mask from akill where mask like '%%203.0.113.99%%'", 30) != '', 'the AKILL')
    check(wait_db("select value from settings where name = 'sjr' and value = '1'", 30) == '1', 'the join requests setting')
    check(wait_db("select count(*) from servicesid where nicks like '%%Carol%%' having count(*) > 0", 30) != '',
          'and the session of the user who identified')

    # A database that is there but can not write (read only, disk full, shutting down):
    # what waits must be kept, it used to be given up on after five tries
    root('set global read_only = 1')
    try:
        ro = Client('Dbdownro')
        check(has(ro.svc('NickServ', 'REGISTER skrivpw1234 readonly@test.net'), 'successfully registered'), 'a nick is registered while the database is read only')
        time.sleep(12)
        check(db("select count(*) from nick where name = 'Dbdownro'") == '0', '(it can not be written)')
    finally:
        root('set global read_only = 0')
    check(wait_db("select name from nick where name = 'Dbdownro'", 30) != '', 'and it is written when the database takes writes again')

    # The same when the account of services may not write for a while (a grant that was taken away)
    def reconnect():
        """Privileges of a database are read when a connection is made: end the ones services have."""
        ids = subprocess.run(['docker', 'exec', ENV['DB_CONTAINER'], 'sh', '-c',
                              'mariadb -uroot -p"$MARIADB_ROOT_PASSWORD" -N -e "select id from information_schema.processlist '
                              "where user = '%s' and command = 'Sleep'\"" % ENV['DB_USER']], capture_output=True).stdout.decode().split()
        for i in ids:
            root('kill %s' % i)

    root("revoke insert, update, delete on %s.* from '%s'@'%%'" % (ENV['DB_NAME'], ENV['DB_USER']))
    reconnect()
    try:
        na = Client('Dbdownna')
        check(has(na.svc('NickServ', 'REGISTER rattpw12345 noaccess@test.net'), 'successfully registered'),
              'a nick is registered while services may not write to the database')
        time.sleep(40)                      # the new connection, and more than five tries on it
        check(db("select count(*) from nick where name = 'Dbdownna'") == '0', '(it can not be written)')
    finally:
        root("grant all privileges on %s.* to '%s'@'%%'" % (ENV['DB_NAME'], ENV['DB_USER']))
        reconnect()
    check(wait_db("select name from nick where name = 'Dbdownna'", 90) != '', 'and it is written when the access is back')
    lost = [l for l in avade_log().split('\n') if 'giving up on' in l]
    check(not lost, 'nothing was given up on', lost[:3])

    a.svc('NickServ', 'SET NOOP OFF')
    a.svc('ChanServ', 'DROP %s nykanalpw1' % new)
    mm.svc('OperServ', 'AKILL DEL *!*@203.0.113.99')
    mm.svc('OperServ', 'SJR OFF')
    for nick in ('Dbdownnick', 'Dbdownre', 'Dbdownro', 'Dbdownna'):
        mm.svc('NickServ', 'DELETE ' + nick)
    b.svc('MemoServ', 'DEL 1')
    close(mm, a, b, c, n, o, ro, na)


def test_flood_protection():
    """Someone who is not identified gets a few commands at a time, and one address only so many mails in an hour."""
    mm = master()
    for nick in ('Floodnick', 'Floodnick2'):
        mm.svc('NickServ', 'DELETE ' + nick)        # whatever a run that was stopped left behind
    u = Client('Floodnick')
    m = u.mark()
    for i in range(20):
        u.svc('NickServ', 'INFO Alice', wait=1.0)
    told = [l for l in u.since(m) if 'too fast' in l]
    answers = [l for l in u.since(m) if 'Hostmask' in l or 'is not online' in l or 'Last seen' in l]
    check(len(told) >= 1, 'an unidentified user who sends command after command is told to slow down', len(told))
    check(8 <= len(answers) < 20, 'the first commands are answered, the ones over the budget are not', len(answers))
    time.sleep(9)
    check(has(u.svc('NickServ', 'HELP'), 'IDENTIFY'), 'a few seconds later commands are answered again')
    a = login('Alice')
    m = a.mark()
    for i in range(20):
        a.svc('NickServ', 'INFO Bob', wait=1.0)
    check(not any('too fast' in l for l in a.since(m)), 'someone who is identified is not limited by services')

    # The mails: with a limit of one, an address that has had its mail gets no second one
    conf = os.path.join(WORK, 'run', 'services.conf')
    text = open(conf).read()
    u2 = Client('Floodnick2')
    try:
        open(conf, 'w').write(re.sub(r'(?m)^maillimit:.*$', 'maillimit: 1', text))
        mm.svc('RootServ', 'REHASH', wait=3)
        u.svc('NickServ', 'REGISTER floodpw1234 flood@test.net')        # the one mail of this hour, if it was not used before
        r = u2.svc('NickServ', 'REGISTER floodpw1234 flood2@test.net')
        check(has(r, 'Too many mails') and not has(r, 'successfully registered'),
              'REGISTER is refused when the address has asked for too many mails this hour', r)
        r = a.svc('NickServ', 'SET EMAIL %s alice2@test.net' % pw('Alice'))
        check(has(r, 'Too many mails'), 'and so is SET EMAIL', r)
        time.sleep(3)
        check(db("select count(*) from nick where name = 'Floodnick2'") == '0', 'no nick is made')
    finally:
        open(conf, 'w').write(text)
        mm.svc('RootServ', 'REHASH', wait=3)
    r = u2.svc('NickServ', 'REGISTER floodpw1234 flood2@test.net')
    check(has(r, 'successfully registered'), 'with the limit of the test network the same user registers', r)
    for nick in ('Floodnick', 'Floodnick2'):
        mm.svc('NickServ', 'DELETE ' + nick)
    close(mm, u, u2, a)


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
    register_chan(a, chan, 'droppw12')
    a.svc('ChanServ', 'DROP %s droppw12' % chan)
    check(has(a.svc('ChanServ', 'REGISTER %s droppw12 again' % chan), 'successfully registered'),
          'a dropped channel can be registered again at once')
    mm = master()
    mm.svc('ChanServ', 'HOLD %s test' % chan)
    check(wait_db("select hold from chansetting where name = '%s' and hold is not null" % chan, 30) != '',
          'HOLD is stored in the database')
    close(a, mm)


def test_split_keeps_identification():
    """The users of a server that was split away are still identified when it comes back."""
    mm = master()
    link_leaf(mm)
    d = login('Dave', port=LEAF_PORT, host='::1')    # see test_host_masking about the throttle
    time.sleep(3)                           # let the services id be written
    m = d.mark()
    mm.send('SQUIT %s :split test' % ENV['LEAF_NAME'])
    time.sleep(5)
    check(has(mm.svc('OperServ', 'UINFO Dave'), 'offline'), '(the leaf is split away, services have removed its users)')
    link_leaf(mm)
    check(not has(mm.svc('OperServ', 'UINFO Dave'), 'offline'), '(and is linked again)')
    check(not d.saw(r' MODE Dave :-\S*r', m, 3), 'a user who comes back after a netsplit keeps +r')
    check(has(d.svc('NickServ', 'INFO Dave'), 'Hostmask'), 'and is still identified, without a new IDENTIFY')
    close(mm, d)


def test_changed_while_split():
    """What changed while a server was split away: a nick that was frozen, a nick that got a new owner, a channel that was made again."""
    chan = '#t_tsreset'
    mm = master()
    link_leaf(mm)
    mm.svc('NickServ', 'DELETE Splitowner')     # whatever a run that was stopped left behind
    mm.svc('NickServ', 'FREEZE -Dave')
    mm.svc('OperServ', 'SJR ALL')           # services decide every join, with the key and the bans they know
    d = login('Dave', port=LEAF_PORT, host='::1')    # see test_host_masking about the throttle
    o = Client('Splitowner', LEAF_PORT)
    check(has(o.svc('NickServ', 'REGISTER splitpw123 splitowner@test.net'), 'successfully registered'), '(a nick is registered on the leaf)')
    d.send('JOIN ' + chan)
    time.sleep(2)
    mm.send('JOIN ' + chan)
    time.sleep(3)                           # let the services ids be written
    md = d.mark()
    mm.send('SQUIT %s :split test' % ENV['LEAF_NAME'])
    time.sleep(5)
    check(has(mm.svc('OperServ', 'UINFO Dave'), 'offline'), '(the leaf is split away)')
    # On the hub side: one nick is frozen, the other is deleted and registered by someone else
    mm.svc('NickServ', 'FREEZE Dave')
    mm.svc('NickServ', 'DELETE Splitowner')
    n = Client('Splitowner')
    check(has(n.svc('NickServ', 'REGISTER nyagarepw1 newowner@test.net'), 'successfully registered'), '(the deleted nick has a new owner)')
    n.send('NICK Movedaway')                # no nick collision when the leaf is back
    # and the channel is made again, with a key the other side does not have
    mm.send('PART ' + chan)
    time.sleep(2)
    mm.send('JOIN ' + chan)
    time.sleep(1)
    mm.send('MODE %s +k nyckel' % chan)
    mm.send('MODE %s +b *!*@banned.example.org' % chan)
    time.sleep(1)
    check(has(mm.svc('OperServ', 'CINFO ' + chan), 'k'), '(services know the key of the new channel)')
    link_leaf(mm)
    check(not has(mm.svc('OperServ', 'UINFO Dave'), 'offline'), '(and is linked again)')
    check(d.saw(r' MODE Dave :-\S*r', md, 5), 'a nick that was frozen during the split loses +r when its user comes back')
    check(not has(d.svc('NickServ', 'INFO Dave'), 'Hostmask'), 'and its user is not identified any more')
    check(not has(o.svc('NickServ', 'INFO Splitowner'), 'Hostmask'),
          'the old owner of a nick that was deleted and registered again is not identified to the new registration')
    # The ircd dropped the key and the ban of the newer channel, services must not refuse on them
    e = login('Erin')
    m = e.mark()
    e.send('JOIN ' + chan)
    try:
        line = e.wait(r'( JOIN :?%s\b| 47[1-7] \S+ %s )' % (re.escape(chan), re.escape(chan)), 8, m)
    except TimeoutError:
        line = 'nothing'
    check(' JOIN ' in line, 'after a netjoin with an older channel, services have dropped the key like the ircd', line)
    mm.svc('OperServ', 'SJR OFF')
    mm.svc('NickServ', 'FREEZE -Dave')
    mm.svc('NickServ', 'DELETE Splitowner')
    close(mm, d, o, n, e)


def test_leaf_split():
    chan = '#t_split'
    mm = master()
    link_leaf(mm)
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


TESTS = [test_config_files, test_setup, test_identify, test_throttle, test_access_security, test_topic_sync, test_old_null_topic, test_topiclock,
         test_sessions_survive_restart, test_services_id_purge, test_vop_hop, test_ipv6, test_chanflags, test_vhost,
         test_clone_limit, test_join_requests, test_spamfilter_target, test_staff_in_whois, test_panic_without_state,
         test_akick_kicks, test_common_errors, test_mask_rank, test_dash_in_channel_name, test_memo, test_memo_limits, test_nick_privacy_and_mail,
         test_oper_checks, test_dropped_nick_memos, test_help_and_last_login, test_modelock_key, test_uhm, test_host_masking, test_ban_follows, test_passwords, test_mailer, test_log_file,
         test_services_own_kill, test_codes_of_a_former_owner, test_channel_identify_is_stored, test_web_command_without_nick,
         test_snoop_hides_passwords, test_mkick_forgets_channel, test_database_down, test_flood_protection, test_bans,
         test_drop_and_hold, test_split_keeps_identification, test_changed_while_split, test_leaf_split, test_services_relink, test_hub_restart]


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
