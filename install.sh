#!/bin/bash
#
# Install or upgrade Avade IRC Services: ./install.sh
#
# Checks what this machine needs, puts everything in ~/avade/ and starts
# services. The first time it asks a few questions and writes services.conf
# for you. When Avade is already installed there, it is upgraded and
# services.conf is kept.
#
# To use a Java that is not in the PATH: JAVA=/path/to/java ./install.sh

cd "$(dirname "${BASH_SOURCE[0]}")" || exit 1
TARGET="$HOME/avade"
JAVA="${JAVA:-java}"
export JAVA

# y/n question, $2 is the answer for Enter (y or n). Without a terminal it is no
ask() {
    local answer
    [ -t 0 ] || return 1
    while true; do
        if [ "$2" == "y" ]; then read -r -p "$1 [Y/n]: " answer; else read -r -p "$1 [y/N]: " answer; fi
        case "${answer,,}" in
            "")      [ "$2" == "y" ]; return ;;
            y|yes)   return 0 ;;
            n|no)    return 1 ;;
        esac
    done
}

echo
echo "Avade IRC Services - install"
echo

### What this machine needs

if [ "$(whoami)" == "root" ]; then
    echo "Do not install or run Avade as root. Make a user for it and install as that user:"
    echo "    useradd -m avade"
    echo "    su - avade"
    exit 1
fi

VERSION=$("$JAVA" -version 2>&1 | sed -n 's/.*version "\([0-9]*\).*/\1/p' | head -1)
if [ -z "$VERSION" ] || [ "$VERSION" -lt 17 ]; then
    if [ -z "$VERSION" ]; then
        echo "Avade needs Java 17 or newer, and there is no java on this machine. Install it with:"
    else
        echo "Avade needs Java 17 or newer, this machine has an older one. Install it with:"
    fi
    if command -v dnf > /dev/null 2>&1; then
        echo "    sudo dnf install java-17-openjdk-headless"
    elif command -v yum > /dev/null 2>&1; then
        echo "    sudo yum install java-17-openjdk-headless"
    elif command -v apt-get > /dev/null 2>&1; then
        echo "    sudo apt-get install openjdk-17-jre-headless"
    else
        echo "    (the OpenJDK 17 package of your system)"
    fi
    echo "Then run ./install.sh again. A Java that is not in the PATH: JAVA=/path/to/java ./install.sh"
    exit 1
fi
echo "ok: Java $VERSION"

### A new installation or an upgrade

UPGRADE=0
if [ -f "$TARGET/services.conf" ]; then
    UPGRADE=1
    # a setting of services.conf, without quotes
    conf() {
        sed -n "s/^$1:[[:space:]]*//p" "$TARGET/services.conf" | head -1 \
            | sed -e "s/[[:space:]]*$//" -e "s/^'\(.*\)'$/\1/" -e 's/^"\(.*\)"$/\1/' -e "s/''/'/g"
    }
    DBHOST=$(conf mysqlhost); DBPORT=$(conf mysqlport); DBUSER=$(conf mysqluser); DBNAME=$(conf mysqldb)
    DUMP=$(command -v mariadb-dump || command -v mysqldump)
    BACKUP="$TARGET/backup/avade-$(date +%Y%m%d-%H%M%S).sql"

    echo
    echo "Avade is already installed in $TARGET. This upgrades it, services.conf is kept."
    echo
    echo "A new version can change the database in a way that an older version can not read. A backup"
    echo "of the database is the only way back."
    echo
    DONE=0
    if [ -n "$DUMP" ] && ask "Take a backup of the database $DBNAME now, to $BACKUP?" y; then
        mkdir -p "$TARGET/backup" && chmod 700 "$TARGET/backup"
        if MYSQL_PWD="$(conf mysqlpass)" "$DUMP" --single-transaction --hex-blob --default-character-set=binary \
               -h "$DBHOST" -P "$DBPORT" -u "$DBUSER" "$DBNAME" > "$BACKUP" && [ -s "$BACKUP" ]; then
            chmod 600 "$BACKUP"
            echo "ok: backup in $BACKUP ($(du -h "$BACKUP" | cut -f1))"
            DONE=1
        else
            rm -f "$BACKUP"
            echo "The backup failed, nothing was changed."
            exit 1
        fi
    fi
    if [ "$DONE" == "0" ]; then
        echo "Take a backup like this:"
        echo
        echo "    mysqldump --single-transaction --hex-blob --default-character-set=binary \\"
        echo "        -h $DBHOST -P $DBPORT -u $DBUSER -p $DBNAME > avade-backup.sql"
        echo
        if ! ask "Is there a backup, continue with the upgrade?" n; then
            echo "Nothing was changed."
            exit 1
        fi
    fi
fi

### Install

echo
echo "Installing to $TARGET ..."
LOG=$(mktemp)
if ! ./make.sh install > "$LOG" 2>&1 || [ ! -f "$TARGET/avade.jar" ] || [ ! -f "$TARGET/avade.sh" ]; then
    echo "The installation failed:"
    tail -n 20 "$LOG"
    rm -f "$LOG"
    exit 1
fi
rm -f "$LOG"
echo "ok: installed"

cd "$TARGET" || exit 1

if [ "$UPGRADE" == "0" ]; then
    ### The first time: avade.sh asks what is needed, writes services.conf and starts
    if [ ! -t 0 ] || [ ! -t 1 ]; then
        echo
        echo "Avade is installed. Now run this in a terminal, it asks a few questions and starts services:"
        echo "    $TARGET/avade.sh start"
        exit 0
    fi
    ./avade.sh start
    exit $?
fi

### An upgrade: new commands into services.conf, then the new version is started

./avade.sh setup
echo
if ./avade.sh status > /dev/null 2>&1; then
    if ask "Services are running the old version. Restart them now?" y; then
        ./avade.sh restart
    else
        echo "The new version is used at the next restart: $TARGET/avade.sh restart"
    fi
else
    echo "Services are not running. Start them with: $TARGET/avade.sh start"
fi
if ./avade.sh mailer status > /dev/null 2>&1; then
    ./avade.sh mailer restart
fi
