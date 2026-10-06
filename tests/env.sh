#!/bin/bash
# Common settings for the test environment. Sourced by the other scripts.
#
# Everything that is built or generated ends up in tests/.work/ (ignored by git).

TESTS="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO="$(dirname "$TESTS")"
WORK="$TESTS/.work"

# bahamut version to test against (a tag in github.com/DALnet/bahamut without the v)
IRCD_VERSION="${IRCD_VERSION:-2.2.4}"
IRCD="$WORK/ircd-$IRCD_VERSION"

# Test network
HUB_NAME=hub.test.net
LEAF_NAME=leaf.test.net
SERVICES_NAME=services.test.net
STATS_NAME=stats.test.net
LINK_PASS=secret
OPER_NAME=admin
OPER_PASS=secret
HUB_CLIENT_PORT=6667
HUB_SERVER_PORT=7015
LEAF_CLIENT_PORT=6670
LEAF_SERVER_PORT=7016

# Database (MariaDB in docker, same version as production)
DB_IMAGE="${DB_IMAGE:-mariadb:10.8.8}"
DB_CONTAINER=avade-test-db
DB_PORT=3307
DB_NAME=avade
DB_USER=avade
DB_PASS=avadepw

# Avade
MASTER_NICK=TestMaster
RUN="$WORK/run"
