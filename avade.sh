#!/bin/bash
#
# Control script for Avade IRC Services and AvadeMailer. Lives next to
# avade.jar and services.conf (./make.sh install puts it in ~/avade/).
#
#   ./avade.sh start      start in the background
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
#   ./avade.sh mailer queue                  mails in the mailbox per status
#   ./avade.sh mailer resend <id|failed>     send a mail, or all failed ones, again
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
        ARGS="run"; STOPWAIT=30
    else
        NAME="Avade"; JAR="$DIR/avade.jar"; CONF="$DIR/services.conf"
        PIDFILE="$DIR/avade.pid"; OUT="$DIR/avade.out"; MATCH='avade\.jar'
        ARGS=""; STOPWAIT=90
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
    for f in "$JAR" "$CONF"; do
        if [ ! -f "$f" ]; then
            echo "Error: $f is missing."
            return 1
        fi
    done
    if ! command -v "$JAVA" > /dev/null 2>&1; then
        echo "Error: java not found, set JAVA=/path/to/java"
        return 1
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
        queue)   mailer_cmd status ;;
        resend)  mailer_cmd resend "$3" ;;
        *)
            echo "Syntax: $0 mailer <start|stop|restart|status|log|queue|resend <id|failed>>"
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
        echo "Syntax: $0 <start|stop|restart|status|check|log|gensalt|mailer ...>"
        exit 1
        ;;
esac
