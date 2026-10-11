<?php
/* 
 * Copyright (C) 2026 Fredrik Karlsson aka DreamHealer & avade.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <https://www.gnu.org/licenses/>.
 */

/*
 * The page behind the link in the mails of Avade (authurl in services.conf):
 *
 *     https://example.net/auth/?code=<32 letters and digits>
 *
 * A mail address or a new password of a nick is confirmed with it. The page
 * does not change anything itself. It looks the code up, asks the visitor to
 * confirm with a button, and leaves a row in the table "command". Services
 * read that table every five seconds, check the code again, make the change
 * and tell the user on IRC.
 *
 * The button is there on purpose: mail programs and virus scanners open the
 * links in a mail by themselves, and a link alone must not confirm anything.
 */
declare ( strict_types = 1 );

header ( 'Content-Type: text/html; charset=utf-8' );
header ( 'Cache-Control: no-store' );
header ( 'Referrer-Policy: no-referrer' );       /* the code is in the address */
header ( 'X-Robots-Tag: noindex, nofollow' );
header ( 'X-Content-Type-Options: nosniff' );
header ( "Content-Security-Policy: default-src 'none'; style-src 'unsafe-inline'; form-action 'self'; base-uri 'none'; frame-ancestors 'none'" );

$config  = is_file ( __DIR__.'/config.php' ) ? require __DIR__.'/config.php' : null;
$network = is_array ( $config ) ? (string) ( $config['network'] ?? 'IRC' ) : 'IRC';
$home    = is_array ( $config ) ? (string) ( $config['home'] ?? '' ) : '';

/* What a code is for, by the table it is in and the command services know */
const KINDS = [
    'AUTH' => [ 'table' => 'maillog', 'what' => 'the mail address', 'done' => 'The mail address is confirmed.' ],
    'PASS' => [ 'table' => 'passlog', 'what' => 'the new password', 'done' => 'The new password is in use.'    ],
];

function h ( string $text ) : string {
    return htmlspecialchars ( $text, ENT_QUOTES | ENT_SUBSTITUTE, 'UTF-8' );
}

/* The whole page. $state is ok, ask, wait or stop: it picks the colour. */
function page ( string $state, string $title, string $text, string $form = '', int $status = 200 ) : void {
    global $network, $home;
    http_response_code ( $status );
    $back = ( $home !== '' ? '<a href="'.h ( $home ).'" rel="noreferrer">'.h ( $network ).'</a>' : h ( $network ) );
    echo '<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>'.h ( $title ).' - '.h ( $network ).'</title>
<style>
  :root { --bg: #f3f4f7; --card: #ffffff; --text: #1c2030; --soft: #5f667a; --line: #e2e5ec;
          --ok: #1f8a4c; --ask: #2f5fd0; --wait: #b7791f; --stop: #c0392b; }
  @media (prefers-color-scheme: dark) {
    :root { --bg: #12141b; --card: #1c1f29; --text: #e8eaf1; --soft: #9aa1b5; --line: #2c303d;
            --ok: #4cc27f; --ask: #7da2ff; --wait: #e0a94a; --stop: #ef7366; }
  }
  * { box-sizing: border-box; }
  body { margin: 0; min-height: 100vh; display: flex; align-items: center; justify-content: center;
         padding: 24px 16px; background: var(--bg); color: var(--text);
         font: 16px/1.55 system-ui, -apple-system, "Segoe UI", Roboto, sans-serif; }
  main { width: 100%; max-width: 460px; background: var(--card); border: 1px solid var(--line);
         border-top: 4px solid var(--mark); border-radius: 10px; padding: 30px 30px 22px; }
  .ok { --mark: var(--ok); } .ask { --mark: var(--ask); } .wait { --mark: var(--wait); } .stop { --mark: var(--stop); }
  .net { margin: 0 0 18px; font-size: 13px; letter-spacing: .08em; text-transform: uppercase; color: var(--soft); }
  h1 { margin: 0 0 10px; font-size: 22px; line-height: 1.3; color: var(--mark); }
  p { margin: 0 0 14px; }
  b { font-weight: 600; }
  code { font: 14px ui-monospace, SFMono-Regular, Menlo, Consolas, monospace; background: var(--bg);
         border: 1px solid var(--line); border-radius: 5px; padding: 2px 6px; }
  button { margin: 6px 0 14px; width: 100%; padding: 12px 16px; border: 0; border-radius: 7px; cursor: pointer;
           background: var(--ask); color: #fff; font: inherit; font-weight: 600; }
  button:hover, button:focus-visible { filter: brightness(1.08); }
  footer { margin-top: 12px; padding-top: 14px; border-top: 1px solid var(--line); font-size: 13px; color: var(--soft); }
  a { color: inherit; }
</style>
</head>
<body>
<main class="'.h ( $state ).'">
  <p class="net">'.h ( $network ).' services</p>
  <h1>'.h ( $title ).'</h1>
  '.$text.'
  '.$form.'
  <footer>'.$back.'</footer>
</main>
</body>
</html>
';
    exit;
}

$code = $_POST['code'] ?? $_GET['code'] ?? '';
if ( ! is_string ( $code ) || ! preg_match ( '/^[0-9a-f]{32}$/', $code ) ) {
    page ( 'stop', 'This link is not complete',
           '<p>The address should end with a code of 32 letters and digits. Please copy the whole link from the mail.</p>', '', 400 );
}
if ( ! is_array ( $config ) ) {
    error_log ( 'Avade confirmation page: config.php is missing, copy config-template.php' );
    page ( 'wait', 'Not available right now', '<p>The page is not set up yet. Please try again later.</p>', '', 503 );
}

mysqli_report ( MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT );
try {
    $db = new mysqli ( (string) $config['db_host'], (string) $config['db_user'], (string) $config['db_pass'],
                       (string) $config['db_name'], (int) ( $config['db_port'] ?? 3306 ) );
    $db->set_charset ( 'utf8mb4' );

    /* Whose code it is, and for what. Only a code of the registration the
       nick has now: the name can have had an owner before. */
    $found = null;
    foreach ( KINDS as $command => $kind ) {
        $look = $db->prepare ( 'select l.nick from '.$kind['table'].' l join nick n on n.name = l.nick '.
                               'where l.auth = ? and l.stamp >= n.regstamp limit 1' );
        $look->bind_param ( 's', $code );
        $look->execute ( );
        $look->bind_result ( $nick );
        if ( $look->fetch ( ) ) {
            $found = [ 'command' => $command, 'nick' => (string) $nick ] + $kind;
        }
        $look->close ( );
        if ( $found !== null ) {
            break;
        }
    }

    if ( $found === null ) {
        page ( 'stop', 'This link does not work any more',
               '<p>It has been used already, a newer mail has replaced it, or the nickname is gone.</p>'.
               '<p>If something is still waiting, the newest mail has the link that works.</p>', '', 404 );
    }
    $nick = '<b>'.h ( $found['nick'] ).'</b>';

    if ( ( $_SERVER['REQUEST_METHOD'] ?? 'GET' ) !== 'POST' ) {
        page ( 'ask', 'Confirm '.$found['what'],
               '<p>This confirms '.h ( $found['what'] ).' of the nickname '.$nick.'.</p>',
               '<form method="post"><input type="hidden" name="code" value="'.h ( $code ).'">'.
               '<button type="submit">Confirm</button></form>'.
               '<p>Not you? Then close this page, nothing has been changed.</p>' );
    }

    /* Leave the message for services, once: a reload must not add another */
    $there = $db->prepare ( 'select 1 from command where extra = ? limit 1' );
    $there->bind_param ( 's', $code );
    $there->execute ( );
    $waiting = (bool) $there->fetch ( );
    $there->close ( );
    if ( ! $waiting ) {
        $from = substr ( (string) ( $_SERVER['REMOTE_ADDR'] ?? '' ), 0, 64 );
        $add  = $db->prepare ( "insert into command ( target, targettype, command, extra, extra2 ) values ( ?, 'NICKINFO', ?, ?, ? )" );
        $add->bind_param ( 'ssss', $found['nick'], $found['command'], $code, $from );
        $add->execute ( );
        $add->close ( );
    }

    /* Services take it within five seconds and clear the code. Wait for
       that, so the page says what really happened. */
    $done = false;
    $gone = $db->prepare ( 'select 1 from '.$found['table'].' where auth = ? limit 1' );
    for ( $i = 0; $i < 18 && ! $done; $i++ ) {
        usleep ( 500000 );
        $gone->bind_param ( 's', $code );
        $gone->execute ( );
        $done = ! $gone->fetch ( );
        $gone->free_result ( );
    }
    $gone->close ( );

    if ( $done ) {
        page ( 'ok', $found['done'],
               '<p>Thank you. The nickname '.$nick.' is up to date, and if you are on IRC with it the services have just told you so.</p>'.
               ( $found['command'] === 'PASS' ? '<p>Everyone who was identified to the nickname has to identify again, with the new password.</p>' : '' ) );
    }
    page ( 'wait', 'Received, and waiting for the services',
           '<p>Your confirmation for the nickname '.$nick.' is saved, but the services have not picked it up yet. They may be restarting.</p>'.
           '<p>You do not have to do anything more. It is done as soon as they are back.</p>', '', 202 );

} catch ( mysqli_sql_exception $e ) {
    error_log ( 'Avade confirmation page: '.$e->getMessage ( ) );
    page ( 'wait', 'Not available right now',
           '<p>The database could not be reached. Nothing was changed, please try the link again in a few minutes.</p>'.
           '<p>You can also confirm on IRC, with the command at the end of the mail.</p>', '', 503 );
}
