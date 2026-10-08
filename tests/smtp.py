#!/usr/bin/env python3
"""A small SMTP server for the tests: tests/smtp.py <port> <directory>

Every mail it gets is saved as <directory>/<n>.eml. A recipient with
"reject" in the address is refused (550), one with "later" is turned away
for now (451). Every RCPT TO is counted in <directory>/rcpt.log, so the
tests can see how many times a mail was tried. No TLS and no login.
"""
import os
import socketserver
import sys
import threading

DIR = sys.argv[2]
lock = threading.Lock()


class Session(socketserver.StreamRequestHandler):
    def reply(self, line):
        self.wfile.write((line + '\r\n').encode())

    def handle(self):
        self.reply('220 test SMTP')
        rcpt, data = [], None
        while True:
            line = self.rfile.readline()
            if not line:
                return
            if data is not None:
                if line in (b'.\r\n', b'.\n'):
                    with lock:
                        n = len([f for f in os.listdir(DIR) if f.endswith('.eml')]) + 1
                        with open(os.path.join(DIR, '%d.eml' % n), 'wb') as f:
                            f.write(b''.join(data))
                    data, rcpt = None, []
                    self.reply('250 queued')
                else:
                    data.append(line[1:] if line.startswith(b'..') else line)
                continue
            cmd = line.decode('utf-8', 'replace').strip()
            word = cmd.split(' ', 1)[0].upper()
            if word in ('EHLO', 'HELO'):
                self.reply('250 test')
            elif word == 'MAIL':
                rcpt = []
                self.reply('250 ok')
            elif word == 'RCPT':
                addr = cmd.split(':', 1)[1].strip().strip('<>').lower()
                with lock, open(os.path.join(DIR, 'rcpt.log'), 'a') as f:
                    f.write(addr + '\n')
                if 'reject' in addr:
                    self.reply('550 no such user')
                elif 'later' in addr:
                    self.reply('451 try again later')
                else:
                    rcpt.append(addr)
                    self.reply('250 ok')
            elif word == 'DATA':
                if not rcpt:
                    self.reply('554 no valid recipients')
                else:
                    data = []
                    self.reply('354 go ahead')
            elif word in ('RSET', 'NOOP'):
                rcpt = []
                self.reply('250 ok')
            elif word == 'QUIT':
                self.reply('221 bye')
                return
            else:
                self.reply('502 not implemented')


class Server(socketserver.ThreadingTCPServer):
    allow_reuse_address = True
    daemon_threads = True


if __name__ == '__main__':
    os.makedirs(DIR, exist_ok=True)
    Server(('127.0.0.1', int(sys.argv[1])), Session).serve_forever()
