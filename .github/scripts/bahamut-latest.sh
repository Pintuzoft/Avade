#!/bin/bash
# The latest released bahamut version (a tag like v2.2.4, release candidates
# not counted), without the v: .github/scripts/bahamut-latest.sh
set -e
git ls-remote --tags --refs https://github.com/DALnet/bahamut.git 'v*' \
    | grep -oE 'refs/tags/v[0-9]+\.[0-9]+\.[0-9]+$' \
    | sed 's|refs/tags/v||' \
    | sort -V | tail -1
