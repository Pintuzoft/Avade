# Tests

Runs Avade against a real bahamut: a hub, a leaf, MariaDB (Docker) and Avade
built from `src/`, all on localhost with an empty database.

Needs: Docker, Java 17+, python3, git, gcc, make, autoconf.

    ./start.sh [version]     # build bahamut if needed and start everything (default 2.2.4)
    ./run_tests.py [name..]  # all tests, or the ones whose name contains a word
    ./stop.sh

The first `run_tests.py` after `start.sh` registers the test users, which takes
about five minutes since Avade writes to the database once a minute.
Run one `run_tests.py` at a time, they share the network.

Everything generated ends up in `.work/` (ignored by git): the bahamut builds,
configs and the logs. Avade's output is `.work/run/avade.out`.

`KEEP_DB=1 ./start.sh` keeps the database from the previous run.

| File | |
|---|---|
| `env.sh` | names, ports and passwords |
| `build-ircd.sh` | fetch and build a bahamut version |
| `ircd.sh`, `avade.sh` | start/stop the parts one at a time |
| `irctest.py` | small IRC client and helpers |
| `run_tests.py` | the tests |
