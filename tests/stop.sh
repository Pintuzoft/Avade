#!/bin/bash
# Stop the test network (the database container is stopped, not removed)
source "$(dirname "${BASH_SOURCE[0]}")/env.sh"
"$TESTS/mailer.sh" stop
"$TESTS/avade.sh" stop
"$TESTS/ircd.sh" stop
docker stop "$DB_CONTAINER" > /dev/null 2>&1 || true
