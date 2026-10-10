#!/bin/bash
# Build avade_uhm.so against a bahamut source tree that has been configured
# and compiled (the module needs its headers and setup.h).
#
#   ./build.sh /path/to/bahamut-2.2.4 [/path/to/ircd-dir]
#
# With the second argument the module is also copied to <ircd-dir>/modules/.

SRC="$1"
DEST="$2"
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [ -z "$SRC" ] || [ ! -f "$SRC/include/hooks.h" ]; then
    echo "Syntax: $0 <bahamut source dir> [<ircd dir>]"
    exit 1
fi
if [ ! -f "$SRC/include/setup.h" ]; then
    echo "Error: run ./configure in $SRC first."
    exit 1
fi
# The hook got the ip as an argument in interface 1011 (bahamut 2.2.0), and an
# ircd only loads a module built for its own interface version.
IFVER=$(sed -n 's/^#define MODULE_INTERFACE_VERSION \([0-9]*\).*/\1/p' "$SRC/include/hooks.h")
if [ "${IFVER:-0}" -lt 1011 ]; then
    echo "Error: $SRC has module interface ${IFVER:-?}, bahamut 2.2.0 or newer (1011) is needed."
    echo "Use the source the running ircd was built from."
    exit 1
fi

gcc -g -O2 -Wall -fno-strict-aliasing -fgnu89-inline -fPIC -shared \
    -I"$SRC/include" -o "$HERE/avade_uhm.so" "$HERE/avade_uhm.c" -lcrypto || exit 1
echo "Built $HERE/avade_uhm.so"

if [ -n "$DEST" ]; then
    mkdir -p "$DEST/modules" && cp "$HERE/avade_uhm.so" "$DEST/modules/" || exit 1
    echo "Installed to $DEST/modules/avade_uhm.so"
fi
