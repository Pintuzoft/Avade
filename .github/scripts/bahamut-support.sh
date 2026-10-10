#!/bin/bash
# Mark a bahamut version as supported and make a new Avade version for it:
#   .github/scripts/bahamut-support.sh 2.2.5
#
# Changes src/core/Version.java (version and bahamut) and tests/env.sh (the
# version the tests use), builds dist/Avade.jar and prints the new Avade
# version. The database does not change, so no step is added to DBChanges:
# the database keeps the version of its last change, and the next real
# change still runs.
set -e
cd "$(dirname "$0")/../.."
NEW="$1"
[[ "$NEW" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]] || { echo "usage: $0 <bahamut version>" >&2; exit 1; }

V=src/core/Version.java
get() { grep -oE "this\.$1 += [0-9]+;" "$V" | grep -oE '[0-9]+'; }
GEN=$(get generation); YEAR=$(get year); MONTH=$(get month); BUILD=$(get build)
NOWY=$((10#$(date -u +%y))); NOWM=$((10#$(date -u +%m)))

if [ $((YEAR * 100 + MONTH)) -lt $((NOWY * 100 + NOWM)) ]; then
    YEAR=$NOWY; MONTH=$NOWM; BUILD=1
else
    BUILD=$((BUILD + 1))
    if [ $BUILD -gt 9 ]; then           # DBChanges counts builds 1-9 per month
        BUILD=1; MONTH=$((MONTH + 1))
        [ $MONTH -gt 12 ] && MONTH=1 && YEAR=$((YEAR + 1))
    fi
fi

sed -i -E "s/(this\.year += )[0-9]+;/\1$YEAR;/; s/(this\.month += )[0-9]+;/\1$MONTH;/; s/(this\.build += )[0-9]+;/\1$BUILD;/" "$V"
sed -i -E "s/\"bahamut-[0-9.]+\"/\"bahamut-$NEW\"/" "$V"
sed -i -E "s/(IRCD_VERSION:-)[0-9.]+/\1$NEW/" tests/env.sh

# dist/Avade.jar like make.sh would, without needing ant
B=$(mktemp -d)
mkdir "$B/classes"
javac --release 17 -nowarn -encoding UTF-8 -d "$B/classes" -cp "lib/*" $(find src -name '*.java') 2>&1 | grep -v '^Note:' || true
[ -f "$B/classes/main/Main.class" ] || { echo "build failed" >&2; exit 1; }
printf 'Class-Path: %s\nMain-Class: main.Main\n' "$(cd dist && ls lib/*.jar | tr '\n' ' ' | sed 's/ $//')" > "$B/manifest.txt"
jar cfm dist/Avade.jar "$B/manifest.txt" -C "$B/classes" .
rm -rf "$B"

printf '%d.%d%02d-%d\n' "$GEN" "$YEAR" "$MONTH" "$BUILD"
