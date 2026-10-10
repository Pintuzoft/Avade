#!/bin/bash
#
# Control script for Avade IRC Services and AvadeMailer. Lives next to
# avade.jar and services.conf (./make.sh install puts it in ~/avade/).
#
#   ./avade.sh start      start in the background. The first time, when there
#                         is no services.conf yet, it asks a few questions and
#                         writes it for you
#   ./avade.sh setup      only that: write services.conf, or with one that is
#                         already there, add the commands a new version brought
#   ./avade.sh stop       stop, pending changes are written to the database first
#   ./avade.sh restart
#   ./avade.sh status
#   ./avade.sh check      start what is not running, quiet otherwise (for cron).
#                         The mailer too, when mailer.conf exists
#   ./avade.sh log        follow the output
#   ./avade.sh gensalt    print a random salt for uhmsalt in services.conf
#
# The mailer (mailer.jar and mailer.conf, see "Mail" in INSTALL):
#   ./avade.sh mailer start|stop|restart|status|log
#                                            (start asks for the mail server and
#                                            writes mailer.conf the first time)
#   ./avade.sh mailer setup                  only that
#   ./avade.sh mailer queue                  mails in the mailbox per status
#   ./avade.sh mailer resend <id|failed>     send a mail, or all failed ones, again
#   ./avade.sh mailer test <address>         send a test mail
#
# Cron, to bring services back after a reboot or a crash:
#   */5 * * * * $HOME/avade/avade.sh check
#
# Set JAVA to use another java than the one in PATH:
#   JAVA=/usr/lib/jvm/java17/bin/java ./avade.sh start

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAVA="${JAVA:-java}"
KEEP=5          # old output files to keep

if [ "$(whoami)" == "root" ]; then
    echo "Error: dont run this software as root."
    exit 1
fi

# Which program the functions below work on
use() {
    if [ "$1" == "mailer" ]; then
        NAME="AvadeMailer"; JAR="$DIR/mailer.jar"; CONF="$DIR/mailer.conf"
        PIDFILE="$DIR/mailer.pid"; OUT="$DIR/mailer.out"; MATCH='mailer\.jar'
        ARGS="run"; STOPWAIT=30; SETUPCMD="mailer setup"
    else
        NAME="Avade"; JAR="$DIR/avade.jar"; CONF="$DIR/services.conf"
        PIDFILE="$DIR/avade.pid"; OUT="$DIR/avade.out"; MATCH='avade\.jar'
        ARGS=""; STOPWAIT=90; SETUPCMD="setup"
    fi
}

# pid of the running program, empty if it is not running
running_pid() {
    local pid
    [ -f "$PIDFILE" ] || return
    pid=$(cat "$PIDFILE" 2>/dev/null)
    [ -n "$pid" ] && [ -d "/proc/$pid" ] || return
    # the pid could belong to something else after a reboot
    tr '\0' ' ' < "/proc/$pid/cmdline" 2>/dev/null | grep -q "$MATCH" && echo "$pid"
}

rotate() {
    local i
    [ -s "$OUT" ] || return
    for (( i = KEEP - 1; i >= 1; i-- )); do
        [ -f "$OUT.$i" ] && mv -f "$OUT.$i" "$OUT.$(( i + 1 ))"
    done
    mv -f "$OUT" "$OUT.1"
}

start() {
    local pid
    pid=$(running_pid)
    if [ -n "$pid" ]; then
        echo "$NAME is already running (pid $pid)."
        return 0
    fi
    if [ ! -f "$JAR" ]; then
        echo "Error: $JAR is missing."
        return 1
    fi
    if ! command -v "$JAVA" > /dev/null 2>&1; then
        echo "Error: java not found, set JAVA=/path/to/java"
        return 1
    fi
    if [ ! -f "$CONF" ]; then
        # The first start: ask what is needed. Never from cron, it has nobody to ask
        if [ -t 0 ] && [ -t 1 ]; then
            setup || return 1
        fi
        if [ ! -f "$CONF" ]; then
            echo "Error: $CONF is missing. Run: $0 $SETUPCMD"
            return 1
        fi
    fi
    rotate
    # the config is read from the current directory
    cd "$DIR" || return 1
    nohup "$JAVA" -jar "$JAR" $ARGS < /dev/null >> "$OUT" 2>&1 &
    pid=$!
    echo "$pid" > "$PIDFILE"
    # a wrong config or an unreachable database makes it exit right away
    sleep 3
    if [ -z "$(running_pid)" ]; then
        rm -f "$PIDFILE"
        echo "Error: $NAME did not start, last lines of $OUT:"
        tail -n 15 "$OUT"
        return 1
    fi
    echo "$NAME started (pid $pid), output in $OUT"
    [ "$NAME" == "Avade" ] && linked
}

# Did services link to the hub? Says what is in the way when they did not
linked() {
    local i line=""
    for (( i = 0; i < 25; i++ )); do
        if grep -a -q 'Link with .* established' "$OUT" 2>/dev/null; then
            echo "ok: linked to the hub."
            return 0
        fi
        line=$(grep -a -m 1 -E 'Hub sent: ERROR|Could not connect to the hub|Database not available|Change FAILED to apply' "$OUT" 2>/dev/null)
        [ -n "$line" ] && break
        [ -z "$(running_pid)" ] && break
        sleep 1
    done
    case "$line" in
        *"Could not connect to the hub"*)
            echo "Not linked yet: the hub does not answer on the address and port in services.conf."
            echo "Services keep trying. Is the hub running, and does it listen on that port?" ;;
        *"Hub sent: ERROR"*)
            echo "Not linked yet, the hub closed the link: ${line##*ERROR :}"
            echo "Services keep trying. What the hub needs in its ircd.conf is in $DIR/hub-setup.txt" ;;
        *"Database not available"*)
            echo "Waiting for the database: services can not log in to it. Check the mysql settings in"
            echo "services.conf. Services link to the hub as soon as the database answers." ;;
        *"Change FAILED"*)
            echo "The upgrade of the database failed, see $OUT" ;;
        *)
            echo "Not linked to the hub yet. Follow it with: $0 log" ;;
    esac
}

stop() {
    local pid i
    pid=$(running_pid)
    if [ -z "$pid" ]; then
        rm -f "$PIDFILE"
        echo "$NAME is not running."
        return 0
    fi
    if [ "$NAME" == "Avade" ]; then
        echo -n "Stopping Avade (pid $pid), writing pending changes to the database "
    else
        echo -n "Stopping $NAME (pid $pid), the mail being sent is finished first "
    fi
    kill "$pid"
    for (( i = 0; i < STOPWAIT; i++ )); do
        if [ ! -d "/proc/$pid" ]; then
            rm -f "$PIDFILE"
            echo " stopped."
            return 0
        fi
        echo -n "."
        sleep 1
    done
    echo
    echo "Error: still running after $STOPWAIT seconds. Check $OUT, and as a"
    echo "last resort: kill -9 $pid"
    return 1
}

status() {
    local pid
    pid=$(running_pid)
    if [ -z "$pid" ]; then
        echo "$NAME is not running."
        return 1
    fi
    echo "$NAME is running (pid $pid, started $(ps -o lstart= -p "$pid"))."
}

# Ask what is needed and write the config (services.conf or mailer.conf)
setup() {
    if [ ! -f "$JAR" ]; then
        echo "Error: $JAR is missing."
        return 1
    fi
    if ! command -v "$JAVA" > /dev/null 2>&1; then
        echo "Error: java not found, set JAVA=/path/to/java"
        return 1
    fi
    ( cd "$DIR" && "$JAVA" -jar "$JAR" setup )
}

# A one time mailer command (queue, resend): needs the jar and the config, not a running mailer
mailer_cmd() {
    for f in "$JAR" "$CONF"; do
        [ -f "$f" ] || { echo "Error: $f is missing."; return 1; }
    done
    cd "$DIR" && "$JAVA" -jar "$JAR" "$@"
}

if [ "${1,,}" == "mailer" ]; then
    use mailer
    case "${2,,}" in
        start)   start ;;
        stop)    stop ;;
        restart) stop && start ;;
        status)  status ;;
        log)     tail -n 50 -f "$OUT" ;;
        setup)   setup ;;
        queue)   mailer_cmd status ;;
        resend)  mailer_cmd resend "$3" ;;
        test)    mailer_cmd test "$3" ;;
        *)
            echo "Syntax: $0 mailer <start|stop|restart|status|log|setup|queue|resend <id|failed>|test <address>>"
            exit 1
            ;;
    esac
    exit $?
fi

use avade
case "${1,,}" in
    start)   start ;;
    stop)    stop ;;
    restart) stop && start ;;
    status)  status ;;
    setup)   setup ;;
    check)
        [ -n "$(running_pid)" ] || start
        use mailer
        if [ -f "$CONF" ] && [ -z "$(running_pid)" ]; then
            start
        fi
        ;;
    log)     tail -n 50 -f "$OUT" ;;
    gensalt) LC_ALL=C tr -dc 'A-Za-z0-9' < /dev/urandom | head -c 48; echo ;;
    *)
        echo "Syntax: $0 <start|stop|restart|status|setup|check|log|gensalt|mailer ...>"
        exit 1
        ;;
esac
