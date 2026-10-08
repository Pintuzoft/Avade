#!/bin/bash
# tests/mailer.sh start|stop|restart|build          AvadeMailer and the test SMTP server
#                 smtp                              only the test SMTP server
#                 cmd <args>                        run the mailer once with these arguments
# start builds first. The mailer runs in .work/mailer with the mailer.conf
# start.sh makes, the mails the SMTP server gets end up in .work/mailer/smtp/.
source "$(dirname "${BASH_SOURCE[0]}")/env.sh"
M="$WORK/mailer"
CP="$WORK/mailer-classes:$REPO/lib/*:$REPO/lib/mail/*"

mailer_pids() {
    for p in $(pgrep -x java); do
        tr '\0' ' ' < /proc/$p/cmdline 2>/dev/null | grep -q 'mailer\.Main' && [ "$(readlink /proc/$p/cwd)" == "$M" ] && echo $p
    done
}
smtp_pids() {
    pgrep -f "^python3 .*smtp\.py $SMTP_PORT "
}
build() {
    rm -rf "$WORK/mailer-classes" && mkdir -p "$WORK/mailer-classes"
    javac -nowarn -d "$WORK/mailer-classes" -cp "$REPO/lib/*:$REPO/lib/mail/*" $(find "$REPO/mailer/src" -name '*.java') "$REPO/src/setup/Ask.java" 2>&1 | grep -v '^Note:'
    [ "${PIPESTATUS[0]}" == "0" ] || { echo "mailer build failed"; exit 1; }
}
stop() {
    for p in $(mailer_pids) $(smtp_pids); do kill $p; done
    for i in 1 2 3 4 5 6 7 8 9 10; do [ -z "$(mailer_pids)$(smtp_pids)" ] && break; sleep 0.5; done
}
smtp() {
    mkdir -p "$M/smtp"
    [ -n "$(smtp_pids)" ] || ( setsid python3 "$TESTS/smtp.py" "$SMTP_PORT" "$M/smtp" < /dev/null > "$M/smtp.out" 2>&1 & )
    sleep 0.5
}
start() {
    smtp
    ( cd "$M" && exec setsid java -cp "$CP" mailer.Main run < /dev/null > "$M/mailer.out" 2>&1 ) > /dev/null 2>&1 &
}
case "$1" in
    build)   build ;;
    start)   stop; build; start ;;
    restart) stop; start ;;
    stop)    stop ;;
    smtp)    smtp ;;
    cmd)     shift; cd "$M" && java -cp "$CP" mailer.Main "$@" ;;
    *) echo "usage: $0 start|stop|restart|build|smtp|cmd <args>"; exit 1 ;;
esac
