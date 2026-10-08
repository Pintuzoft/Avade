# Avade

IRC services (NickServ, ChanServ, MemoServ, OperServ, RootServ, GuestServ) for
the bahamut ircd, written in Java 17. MariaDB holds the data.

- Target: the latest bahamut 2.2.x release (see `bahamut` in `src/core/Version.java`).
  Production upgrades its ircd before an Avade release, so no workarounds for
  older bahamut versions. Bahamut 3.0 (gossip) is not released and out of scope.
- `docs/AUDIT-2026-09.md` tracks what was found and fixed, by number. Add what
  you fix or build there. Everything in the repository is written in English.

## Build and test

- `./make.sh` builds with ant. Without ant: `javac --release 17 -encoding UTF-8 -cp "lib/*"`.
- `dist/Avade.jar` is committed. Rebuild it when the code changes; the manifest
  `Class-Path` lists the jars in `dist/lib/` (see `.github/scripts/bahamut-support.sh`).
- `tests/start.sh [bahamut version]` starts a hub, a leaf, MariaDB (Docker) and
  Avade from `src/` with an empty database, `tests/run_tests.py [words]` runs the
  tests, `tests/stop.sh` stops. The first run registers users (about 5 minutes).
  Every new command or behaviour gets checks in `tests/run_tests.py` (add the
  test to `TESTS`). The last check fails on any `java.lang.` exception in
  `.work/run/avade.out`, so a caught NPE is a failure.

## Code style

- Spaced, aligned syntax everywhere: `foo ( bar )`, `if ( x ) {`,
  `this.service.sendMsg ( user, ... )`. Match the file you are in.
- `core.HashString` is a name with a precomputed hash; compare with `a.is ( b )`,
  not String compares. Constants live in `core.HashNumeric`. Do not build a new
  HashString in a hot path just to compare once.
- Comments explain why, in short English sentences. Commit messages: short
  English subject, a body with what changed and why.

## Adding a command

Look at a similar existing command and touch the same places:

1. `core/HashNumeric.java`: the command name, and new result codes if needed.
2. The service's `setCommands ( )` (`NickServ`, `ChanServ`, `OperServ`, ...):
   a `CommandInfo` with access `0` for users or `CMDAccess ( NAME )` for staff.
3. Staff commands: add the name to `cList` in `core/Config.java`, and to the
   access lists (`sra:`, `csop:`, `sa:`, `ircop:`) in `template.conf` and
   `reference.conf` (with a comment). A command missing in services.conf is SRA only.
4. The executor (`XSExecutor`): dispatch in `parse`, a method, the checks in
   `validateCommandData` (result codes), and texts in `output ( )`. Staff
   actions are logged (`XSLogEvent`, flag code in `core/LogEvent.java` and the
   legend in `OSHelper`) and sent as globops where similar commands do so.
5. The helper (`XSHelper`): a help page, and the command in the main help list.
6. Anything secret (passwords, codes) is masked in the snoop: `redact` in
   `NSSnoop`/`CSSnoop`. Passwords are never stored or shown in clear, use
   `security.Hash`.

Messages to the ircd go through `Service.sendServ`/`sendRaw`; what the ircd
sends is parsed in `core/Handler.java`. Chanflags are bahamut's XFLAGS (SVSXCF).

## Database

- Every schema or data change is a new step in `src/core/DBChanges.java`: a new
  `case` last (move the `break` there), its method, and a bump of the build in
  `Version.java` (builds 1-9 per month). Never change a step that has shipped.
  It must give the right result both when upgrading and on an empty database.
- AES encrypted columns (mail addresses) are `varbinary`.
- Until `Handler.isDataLoaded ( )`, nothing may touch anyone's access or
  identification (no -r, guest nicks, deop or kick).
