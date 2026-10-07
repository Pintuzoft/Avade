#!/bin/bash
# Start a complete test network: tests/start.sh [bahamut version]
#   MariaDB (docker) + bahamut hub + bahamut leaf + Avade built from this repo
#
# The database is created empty every time, so Avade runs all its DBChanges.
# Use KEEP_DB=1 to keep the database from the last run.
set -e
source "$(dirname "${BASH_SOURCE[0]}")/env.sh"
[ -n "$1" ] && IRCD_VERSION="$1" && IRCD="$WORK/ircd-$IRCD_VERSION"

"$TESTS/stop.sh" > /dev/null 2>&1 || true
"$TESTS/build-ircd.sh" "$IRCD_VERSION"
mkdir -p "$RUN" "$WORK/leaf"
echo "$IRCD_VERSION" > "$WORK/current-version"

### Database
if [ -z "$KEEP_DB" ] || ! docker inspect "$DB_CONTAINER" > /dev/null 2>&1; then
    docker rm -f "$DB_CONTAINER" > /dev/null 2>&1 || true
    # latin1 as server default like an old installation, DBChanges converts it
    docker run -d --name "$DB_CONTAINER" \
        -e MARIADB_ROOT_PASSWORD=rootpw -e MARIADB_DATABASE="$DB_NAME" \
        -e MARIADB_USER="$DB_USER" -e MARIADB_PASSWORD="$DB_PASS" \
        -p "127.0.0.1:$DB_PORT:3306" "$DB_IMAGE" \
        --character-set-server=latin1 --collation-server=latin1_swedish_ci > /dev/null
else
    docker start "$DB_CONTAINER" > /dev/null
fi
echo -n "waiting for the database "
for i in $(seq 1 60); do
    if docker exec "$DB_CONTAINER" mariadb -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" -e 'select 1' > /dev/null 2>&1; then
        echo " ok"; break
    fi
    echo -n "."; sleep 1
done

### ircd configs
ircd_conf() {   # name, client port, server port, extra options, connect blocks
    cat <<CONF
global { name $1; info "Avade test server"; admin { "test"; "test"; "test@test"; }; };
options {
    network_name    TestNET;
    local_kline     admin@test;
    show_links;
    allow_split_ops;
    services_name   $SERVICES_NAME;
    stats_name      $STATS_NAME;
    network_kline   admin@test;
    nshelpurl       "http://help";
    spamfilterurl   "http://help";
$4
};
port { port $2; bind 127.0.0.1; };
port { port $2; bind ::1; };
port { port $3; bind 127.0.0.1; };
allow { host *@*; class users; flags T; };
class { name users; maxusers 200; pingfreq 90; maxsendq 100000; };
class { name opers; pingfreq 90; maxsendq 500000; };
class { name servers; pingfreq 90; connfreq 10; maxsendq 5000000; };
oper { name $OPER_NAME; passwd $OPER_PASS; access OAaRDCc; host *@*; class opers; };
super { "$SERVICES_NAME"; "$STATS_NAME"; };
$5
CONF
}
ircd_conf "$HUB_NAME" "$HUB_CLIENT_PORT" "$HUB_SERVER_PORT" "    servtype hub;" \
"connect { name $SERVICES_NAME; host 127.0.0.1; apasswd $LINK_PASS; cpasswd $LINK_PASS; class servers; flags H; };
connect { name $LEAF_NAME; host 127.0.0.1; apasswd $LINK_PASS; cpasswd $LINK_PASS; class servers; };" > "$IRCD/ircd.conf"
ircd_conf "$LEAF_NAME" "$LEAF_CLIENT_PORT" "$LEAF_SERVER_PORT" "" \
"connect { name $HUB_NAME; host 127.0.0.1; port $HUB_SERVER_PORT; apasswd $LINK_PASS; cpasswd $LINK_PASS; class servers; flags H; };" > "$WORK/leaf/ircd.conf"
cp -f "$IRCD/ircd.motd" "$IRCD/ircd.crt" "$IRCD/ircd.key" "$WORK/leaf/" 2>/dev/null || true

### The host-masking module, on both servers (bahamut 2.2.0 and newer, where the hook has the ip)
IFVER=$(sed -n 's/^#define MODULE_INTERFACE_VERSION \([0-9]*\).*/\1/p' "$WORK/src-$IRCD_VERSION/include/hooks.h" 2>/dev/null)
if [ "${IFVER:-0}" -ge 1011 ]; then
    rm -f "$IRCD/avade_uhm.salt" "$WORK/leaf/avade_uhm.salt"
    "$REPO/bahamut-module/build.sh" "$WORK/src-$IRCD_VERSION" "$IRCD" > "$WORK/module-$IRCD_VERSION.log" 2>&1 \
        || { tail -5 "$WORK/module-$IRCD_VERSION.log"; exit 1; }
    mkdir -p "$WORK/leaf/modules" && cp -f "$IRCD/modules/avade_uhm.so" "$WORK/leaf/modules/"
    echo 'modules { path modules; autoload avade_uhm; };' >> "$IRCD/ircd.conf"
    echo 'modules { path modules; autoload avade_uhm; };' >> "$WORK/leaf/ircd.conf"
fi

### Avade config, from the template in the repo
sed -e "s/^name: .*/name: $SERVICES_NAME/" \
    -e "s/^domain: .*/domain: test.net/" \
    -e "s/^stats: .*/stats: $STATS_NAME/" \
    -e "s/^master: .*/master: $MASTER_NICK/" \
    -e "s/^hubname: .*/hubname: $HUB_NAME/" \
    -e "s/^hubhost: .*/hubhost: 127.0.0.1/" \
    -e "s/^hubport: .*/hubport: $HUB_SERVER_PORT/" \
    -e "s/^hubpass: .*/hubpass: $LINK_PASS/" \
    -e "s/^mysqlhost: .*/mysqlhost: 127.0.0.1/" \
    -e "s/^mysqlport: .*/mysqlport: $DB_PORT/" \
    -e "s/^mysqluser: .*/mysqluser: $DB_USER/" \
    -e "s/^mysqlpass: .*/mysqlpass: $DB_PASS/" \
    -e "s/^mysqldb: .*/mysqldb: $DB_NAME/" \
    "$REPO/template.conf" > "$RUN/services.conf"
printf '\nvhostforbidden:\n  - "*admin*"\n  - "*oper*"\n' >> "$RUN/services.conf"
printf '\nuhmsalt: TestSalt1234567890abcdefGHIJ\nuhmprefix: avade\n' >> "$RUN/services.conf"

"$TESTS/ircd.sh" start
"$TESTS/avade.sh" start
"$TESTS/wait-link.sh" || exit 1
echo "test network is up: bahamut $IRCD_VERSION, hub 127.0.0.1:$HUB_CLIENT_PORT, leaf 127.0.0.1:$LEAF_CLIENT_PORT"
