# Watching for new bahamut versions

`.github/workflows/bahamut-watch.yml` runs every Monday. It checks whether
DALnet/bahamut has a new release (a tag like `v2.2.5`, not an rc) that is newer
than the one Avade supports (`bahamut` in `src/core/Version.java`).

When there is a new version:

1. The test suite runs against it, on a test network of its own in GitHub
   Actions.
2. Claude reads what changed in bahamut since the version Avade supports, and
   decides whether anything affects services or should be controlled from
   services.
3. Then one of two things happens:
   - **The tests pass and nothing new has to be built:** Avade is released
     automatically. `Version.java` and `tests/env.sh` get the new bahamut
     version, Avade gets the next version number, `dist/Avade.jar` is rebuilt,
     and there is a commit on master, a tag and a release.
   - **Something has to be built, or the tests fail:** Claude builds the
     support as `CLAUDE.md` describes, on the branch `bahamut-<version>`: commands,
     config, access levels, help texts, database steps and tests. It runs the
     test suite and opens a pull request, and an issue tells what happened. A
     person reviews and merges, and the release is made by hand.

A new major version (for example 3.0) is never released automatically. It only
gets an issue and a pull request.

A version that already has an issue (open or closed) is not tested again every
week. Run the workflow by hand to test it again.

Automatic releases do not change the database and add no step to `DBChanges`.
The database keeps the version of its last change, and the next real change
runs as usual.

## Setting it up

The workflow runs from the default branch (master).

1. **Actions may write:** Settings → Actions → General → Workflow permissions:
   *Read and write permissions*.
2. **Claude (for the analysis and the pull requests):**
   - Install the Claude GitHub app on the repository:
     https://github.com/apps/claude. `/install-github-app` in Claude Code
     does both steps.
   - Add a secret under Settings → Secrets and variables → Actions: either
     `CLAUDE_CODE_OAUTH_TOKEN` (from `claude setup-token`, uses a Claude
     subscription) or `ANTHROPIC_API_KEY` (paid per use).
   - Without a secret only the tests run. When they pass Avade is released,
     without anyone having read the changes in bahamut.
3. **Try it:** Actions → *bahamut watch* → *Run workflow*. Enter a version,
   for example `2.2.4`, and leave *dry run* checked. The tests and the analysis
   run, nothing is created.

If master is protected so that it needs pull requests, the automatic release
can not push. It opens an issue instead.

## Cost and time

GitHub Actions is free for public repositories. A run with a new version takes
20–60 minutes: building bahamut, the test suite (about 15 minutes) and
Claude's work. Weeks without a new version take a few seconds and do not use
Claude.
