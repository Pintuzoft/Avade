#!/bin/bash
# tests/avade.sh start|stop|restart|build     (start builds first)
# AVADE_JAVA_OPTS: extra options for the JVM, a test uses it to shorten a time
source "$(dirname "${BASH_SOURCE[0]}")/env.sh"

avade_pids() {
    for p in $(pgrep -x java); do
        tr '\0' ' ' < /proc/$p/cmdline | grep -q 'main\.Main' && [ "$(readlink /proc/$p/cwd)" == "$RUN" ] && echo $p
    done
}
build() {
    rm -rf "$WORK/classes" && mkdir -p "$WORK/classes"
    javac -nowarn -d "$WORK/classes" -cp "$REPO/lib/*" $(find "$REPO/src" -name '*.java') 2>&1 | grep -v '^Note:'
    [ "${PIPESTATUS[0]}" == "0" ] || { echo "build failed"; exit 1; }
}
stop() {
    for p in $(avade_pids); do kill $p; done
    for i in 1 2 3 4 5; do [ -z "$(avade_pids)" ] && break; sleep 1; done
}
start() {
    mkdir -p "$RUN"
    ( cd "$RUN" && exec setsid java $AVADE_JAVA_OPTS -cp "$WORK/classes:$REPO/lib/*" main.Main < /dev/null > "$RUN/avade.out" 2>&1 ) > /dev/null 2>&1 &
}
case "$1" in
    build)   build ;;
    start)   stop; build; start ;;
    restart) stop; start ;;
    stop)    stop ;;
    *) echo "usage: $0 start|stop|restart|build"; exit 1 ;;
esac
