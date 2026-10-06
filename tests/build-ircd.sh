#!/bin/bash
# Build a bahamut version for testing: tests/build-ircd.sh [version]
# Example: tests/build-ircd.sh 2.2.2
#
# Needs: git gcc make autoconf automake libssl-dev zlib1g-dev
set -e
source "$(dirname "${BASH_SOURCE[0]}")/env.sh"
[ -n "$1" ] && IRCD_VERSION="$1" && IRCD="$WORK/ircd-$IRCD_VERSION"
SRC="$WORK/src-$IRCD_VERSION"

if [ -x "$IRCD/ircd" ]; then
    echo "bahamut $IRCD_VERSION is already built in $IRCD"
    exit 0
fi

mkdir -p "$WORK"
[ -d "$WORK/bahamut" ] || git clone -q https://github.com/DALnet/bahamut.git "$WORK/bahamut"
git -C "$WORK/bahamut" fetch -q --tags
git -C "$WORK/bahamut" worktree prune
rm -rf "$SRC"
git -C "$WORK/bahamut" worktree add -f -q "$SRC" "v$IRCD_VERSION"

cd "$SRC"
# The repo has configure.in but not the generated files
AM=$(ls -d /usr/share/automake-* 2>/dev/null | head -1)
[ -n "$AM" ] || { echo "automake is needed for install-sh/config.guess/config.sub"; exit 1; }
cp "$AM/install-sh" "$AM/config.guess" "$AM/config.sub" .
autoconf 2>/dev/null
autoheader 2>/dev/null

# Before 2.2.3 the glibc internal resolver names are used, which newer
# glibc versions no longer let programs link against
sed -i '/^#define res_mkquery __res_mkquery$/d; /^#define dn_expand __dn_expand$/d' include/resolv.h

LIBS=-lresolv ./configure --prefix="$IRCD" > "$WORK/configure-$IRCD_VERSION.log" 2>&1
make -j4 > "$WORK/build-$IRCD_VERSION.log" 2>&1 || { tail -20 "$WORK/build-$IRCD_VERSION.log"; exit 1; }
make install >> "$WORK/build-$IRCD_VERSION.log" 2>&1

# The ircd refuses to start without a certificate
openssl req -x509 -newkey rsa:2048 -nodes -keyout "$IRCD/ircd.key" -out "$IRCD/ircd.crt" \
        -days 3650 -subj "/CN=$HUB_NAME" > /dev/null 2>&1

"$IRCD/ircd" -v 2>&1 | grep -m1 'bahamut-' | sed 's/ booting.*//'
echo "built in $IRCD"
