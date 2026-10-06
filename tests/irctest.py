"""Small IRC client and helpers for testing Avade against a real bahamut.

Used by run_tests.py. The settings are read from env.sh so the scripts and
the tests always agree.
"""
import os
import re
import socket
import subprocess
import threading
import time

TESTS = os.path.dirname(os.path.abspath(__file__))
WORK = os.path.join(TESTS, '.work')
RUN = os.path.join(WORK, 'run')


def _env():
    """KEY=value lines from env.sh (defaults like ${X:-y} are resolved)."""
    conf = {}
    for line in open(os.path.join(TESTS, 'env.sh')):
        m = re.match(r'^([A-Z_]+)=(.*)$', line.strip())
        if not m:
            continue
        val = m.group(2).strip('"')
        d = re.match(r'^\$\{([A-Z_]+):-(.*)\}$', val)
        if d:
            val = os.environ.get(d.group(1), d.group(2))
        conf[m.group(1)] = val
    return conf


ENV = _env()
SERVICES = ENV['SERVICES_NAME']
STATS = ENV['STATS_NAME']
HUB_PORT = int(ENV['HUB_CLIENT_PORT'])
LEAF_PORT = int(ENV['LEAF_CLIENT_PORT'])
MASTER = ENV['MASTER_NICK']


def ircd_version():
    """Version of the bahamut the test network runs, as a tuple: (2, 2, 4)."""
    try:
        v = open(os.path.join(WORK, 'current-version')).read().strip()
    except OSError:
        v = ENV['IRCD_VERSION']
    return tuple(int(x) for x in v.split('.'))


OPEN = []                       # every client that has not quit yet


def close_all():
    """Disconnect whatever clients a test left behind."""
    if OPEN:
        for c in list(OPEN):
            c.quit()
        time.sleep(0.5)


class Client:
    """A connected and registered IRC client that collects everything it receives."""

    def __init__(self, nick, port=HUB_PORT, host='127.0.0.1'):
        self.nick = nick
        self.closed = False
        self.lines = []
        self.lock = threading.Lock()
        self.buf = b''
        self.s = socket.create_connection((host, port), timeout=15)
        self.s.settimeout(None)
        threading.Thread(target=self._read, daemon=True).start()
        OPEN.append(self)
        self.send('NICK ' + nick)
        self.send('USER %s 0 * :%s test' % (nick.lower()[:9], nick))
        self.wait(r' 001 ', 20)

    def _read(self):
        while True:
            try:
                data = self.s.recv(4096)
            except OSError:
                return
            if not data:
                self.closed = True
                return
            self.buf += data
            while b'\n' in self.buf:
                line, self.buf = self.buf.split(b'\n', 1)
                line = line.rstrip(b'\r').decode('utf-8', 'replace')
                if line.startswith('PING'):
                    self.send('PONG' + line[4:])
                    continue
                with self.lock:
                    self.lines.append(line)

    def send(self, line, encoding='utf-8'):
        self.s.sendall((line + '\r\n').encode(encoding))

    def mark(self):
        """Position in the received lines, use with since() and wait(start=)."""
        with self.lock:
            return len(self.lines)

    def since(self, mark):
        with self.lock:
            return list(self.lines[mark:])

    def wait(self, pattern, timeout=10, start=0):
        """Wait for a line matching the regex, raises TimeoutError."""
        rx = re.compile(pattern)
        end = time.time() + timeout
        while time.time() < end:
            with self.lock:
                for line in self.lines[start:]:
                    if rx.search(line):
                        return line
            if self.closed:
                raise ConnectionError('%s: the server closed the connection' % self.nick)
            time.sleep(0.05)
        raise TimeoutError('%s: nothing matched %r, last lines:\n  %s'
                           % (self.nick, pattern, '\n  '.join(self.since(0)[-6:])))

    def saw(self, pattern, start=0, wait=2.0):
        """True if a matching line arrives within `wait` seconds."""
        try:
            self.wait(pattern, wait, start)
            return True
        except TimeoutError:
            return False

    def svc(self, service, command, wait=1.5, server=None):
        """Send a command to a service and return the text of the notices it sent back.

        Waits for the first notice (the ircd delays a client that sends a lot),
        then `wait` seconds more for the rest."""
        if server is None:
            server = STATS if service.lower() == 'operserv' else SERVICES
        m = self.mark()
        self.send('PRIVMSG %s@%s :%s' % (service, server, command))
        self.saw(r'^:%s!\S+ NOTICE ' % re.escape(service), m, 10)
        time.sleep(wait)
        return [l.split(' :', 1)[1] for l in self.since(m) if ' NOTICE ' in l and ' :' in l]

    def join(self, chan):
        m = self.mark()
        self.send('JOIN ' + chan)
        self.wait(r' JOIN :?' + re.escape(chan), 10, m)

    def whois(self, nick):
        """All numeric lines of a WHOIS as (numeric, text)."""
        m = self.mark()
        self.send('WHOIS ' + nick)
        self.wait(r' 318 ', 10, m)
        out = []
        for l in self.since(m):
            p = l.split(' ', 3)
            if len(p) > 3 and p[1].isdigit():
                out.append((p[1], p[3]))
        return out

    def oper(self):
        m = self.mark()
        self.send('OPER %s %s' % (ENV['OPER_NAME'], ENV['OPER_PASS']))
        self.wait(r' 381 ', 10, m)

    def quit(self):
        if self in OPEN:
            OPEN.remove(self)
        try:
            self.send('QUIT :bye')
            self.s.close()
        except OSError:
            pass


### Database

def db(query):
    """Run a query in the test database, returns the output as text."""
    out = subprocess.run(
        ['docker', 'exec', ENV['DB_CONTAINER'], 'mariadb', '--default-character-set=utf8mb4',
         '-u' + ENV['DB_USER'], '-p' + ENV['DB_PASS'], ENV['DB_NAME'], '-N', '-B', '-e', query],
        capture_output=True)
    return out.stdout.decode('utf-8', 'replace').strip()


def wait_db(query, timeout=90):
    """Wait until the query returns something (Avade writes with a delay), returns it."""
    end = time.time() + timeout
    while time.time() < end:
        out = db(query)
        if out:
            return out
        time.sleep(1)
    return ''


def auth_code(email, timeout=90):
    """The mail confirmation code Avade put in the mailbox for a new nick."""
    body = wait_db("select body from mailbox where mail = '%s' order by id desc limit 1" % email, timeout)
    m = re.search(r'auth/([0-9a-f]{32})', body)
    return m.group(1) if m else None


### Controlling the test network

def _script(name, *args):
    subprocess.run([os.path.join(TESTS, name)] + list(args), check=False,
                   stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)


def avade(action):
    """start (rebuilds), restart or stop Avade. start/restart wait for the link."""
    open(os.path.join(RUN, 'avade.out'), 'a').close()
    _script('avade.sh', action)
    if action in ('start', 'restart'):
        wait_link()


def wait_link(timeout=120):
    """Wait until Avade has received the whole burst after its latest start."""
    path = os.path.join(RUN, 'avade.out')
    end = time.time() + timeout
    while time.time() < end:
        try:
            data = open(path, errors='replace').read()
        except OSError:
            data = ''
        if data.count('\nPING :' + ENV['HUB_NAME']) >= 2:
            time.sleep(1)
            return
        time.sleep(0.5)
    raise TimeoutError('Avade did not link to the hub')


def link_count():
    """How many times Avade has linked to the hub since it was started."""
    return avade_log().count('GNOTICE :Link with ' + SERVICES + ' established')


def wait_relink(before, timeout=90):
    """Wait for a new link after link_count() returned `before`, and for the burst."""
    end = time.time() + timeout
    while time.time() < end:
        log = avade_log()
        if log.count('GNOTICE :Link with ' + SERVICES + ' established') > before:
            tail = log[log.rindex('GNOTICE :Link with ' + SERVICES + ' established'):]
            if tail.count('\nPING :' + ENV['HUB_NAME']) >= 2:
                time.sleep(1)
                return True
        time.sleep(0.5)
    return False


def ircd(action):
    _script('ircd.sh', action)


def avade_log():
    try:
        return open(os.path.join(RUN, 'avade.out'), errors='replace').read()
    except OSError:
        return ''
