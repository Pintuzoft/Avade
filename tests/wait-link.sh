#!/bin/bash
# Wait until Avade has linked to the hub and received the whole burst
# (bahamut ends the burst with a 2nd PING). Fails after 120 seconds.
source "$(dirname "${BASH_SOURCE[0]}")/env.sh"
echo -n "waiting for Avade to link "
for i in $(seq 1 120); do
    if [ "$(grep -a -c "^PING :$HUB_NAME" "$RUN/avade.out" 2>/dev/null)" -ge 2 ]; then
        echo " ok"; exit 0
    fi
    if grep -a -q 'Change FAILED to apply' "$RUN/avade.out" 2>/dev/null; then
        echo " database migration failed:"; grep -a -B2 'FAILED' "$RUN/avade.out" | cut -c1-200; exit 1
    fi
    echo -n "."; sleep 1
done
echo " timeout"; tail -5 "$RUN/avade.out" | cut -c1-200; exit 1
