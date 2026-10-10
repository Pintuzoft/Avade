#!/bin/bash
# tests/ircd.sh start|stop|restart-hub|stop-hub|start-hub|stop-leaf|start-leaf
source "$(dirname "${BASH_SOURCE[0]}")/env.sh"
[ -f "$WORK/current-version" ] && IRCD_VERSION="$(cat "$WORK/current-version")" && IRCD="$WORK/ircd-$IRCD_VERSION"

pids() {    # ircd processes started with the given working directory
    for p in $(pgrep -x ircd); do
        [ "$(readlink /proc/$p/cwd)" == "$1" ] && echo $p
    done
}
# (the whole subshell is detached from our stdout, or callers reading it would wait forever)
start_hub()  { ( cd "$IRCD" && exec setsid ./ircd < /dev/null > "$WORK/hub.out" 2>&1 ) > /dev/null 2>&1 & }
start_leaf() { ( cd "$WORK/leaf" && exec setsid "$IRCD/ircd" -f "$WORK/leaf/ircd.conf" < /dev/null > "$WORK/leaf.out" 2>&1 ) > /dev/null 2>&1 & }
stop_dir()   { for p in $(pids "$1"); do kill $p; done; }

case "$1" in
    start)       start_hub; sleep 2; start_leaf; sleep 1 ;;
    stop)        stop_dir "$IRCD"; stop_dir "$WORK/leaf" ;;
    start-hub)   start_hub; sleep 2 ;;
    stop-hub)    stop_dir "$IRCD" ;;
    restart-hub) stop_dir "$IRCD"; sleep 2; start_hub; sleep 2 ;;
    start-leaf)  start_leaf; sleep 1 ;;
    stop-leaf)   stop_dir "$WORK/leaf" ;;
    *) echo "usage: $0 start|stop|start-hub|stop-hub|restart-hub|start-leaf|stop-leaf"; exit 1 ;;
esac
