#!/bin/bash
#
# Control script for Avade IRC Services. Lives next to avade.jar and
# services.conf (./make.sh install puts it in ~/avade/).
#
#   ./avade.sh start      start in the background
#   ./avade.sh stop       stop, pending changes are written to the database first
#   ./avade.sh restart
#   ./avade.sh status
#   ./avade.sh check      start if not running, quiet otherwise (for cron)
#   ./avade.sh log        follow the output
#   ./avade.sh gensalt    print a random salt for uhmsalt in services.conf
#
# Cron, to bring services back after a reboot or a crash:
#   */5 * * * * $HOME/avade/avade.sh check
#
# Set JAVA to use another java than the one in PATH:
#   JAVA=/usr/lib/jvm/java17/bin/java ./avade.sh start

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR="$DIR/avade.jar"
PIDFILE="$DIR/avade.pid"
OUT="$DIR/avade.out"
JAVA="${JAVA:-java}"
STOPWAIT=90     # seconds to wait for a clean stop
KEEP=5          # old avade.out files to keep

if [ "$(whoami)" == "root" ]; then
    echo "Error: dont run this software as root."
    exit 1
fi

# pid of the running Avade, empty if it is not running
running_pid() {
    local pid
    [ -f "$PIDFILE" ] || return
    pid=$(cat "$PIDFILE" 2>/dev/null)
    [ -n "$pid" ] && [ -d "/proc/$pid" ] || return
    # the pid could belong to something else after a reboot
    tr '\0' ' ' < "/proc/$pid/cmdline" 2>/dev/null | grep -q 'avade\.jar' && echo "$pid"
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
        echo "Avade is already running (pid $pid)."
        return 0
    fi
    for f in "$JAR" "$DIR/services.conf"; do
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
    # services.conf is read from the current directory
    cd "$DIR" || return 1
    nohup "$JAVA" -jar "$JAR" < /dev/null >> "$OUT" 2>&1 &
    pid=$!
    echo "$pid" > "$PIDFILE"
    # a wrong config or an unreachable database makes it exit right away
    sleep 3
    if [ -z "$(running_pid)" ]; then
        rm -f "$PIDFILE"
        echo "Error: Avade did not start, last lines of $OUT:"
        tail -n 15 "$OUT"
        return 1
    fi
    echo "Avade started (pid $pid), output in $OUT"
}

stop() {
    local pid i
    pid=$(running_pid)
    if [ -z "$pid" ]; then
        rm -f "$PIDFILE"
        echo "Avade is not running."
        return 0
    fi
    echo -n "Stopping Avade (pid $pid), writing pending changes to the database "
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
    echo "last resort (changes not yet written are lost): kill -9 $pid"
    return 1
}

status() {
    local pid
    pid=$(running_pid)
    if [ -z "$pid" ]; then
        echo "Avade is not running."
        return 1
    fi
    echo "Avade is running (pid $pid, started $(ps -o lstart= -p "$pid"))."
}

case "${1,,}" in
    start)   start ;;
    stop)    stop ;;
    restart) stop && start ;;
    status)  status ;;
    check)   [ -n "$(running_pid)" ] || start ;;
    log)     tail -n 50 -f "$OUT" ;;
    gensalt) LC_ALL=C tr -dc 'A-Za-z0-9' < /dev/urandom | head -c 48; echo ;;
    *)
        echo "Syntax: $0 <start|stop|restart|status|check|log|gensalt>"
        exit 1
        ;;
esac
