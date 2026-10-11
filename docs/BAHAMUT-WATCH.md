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

## What is looked at, every time

Claude reads both `CHANGES` (what the authors of bahamut say) and the diff of
the source (what happened), and goes through the same list for every version,
also when `CHANGES` says nothing about a point:

1. The server protocol: CAPAB, SERVER and SVINFO, the order of the burst, and
   numerics that services get.
2. The status of members: the prefixes in SJOIN (`@ % +`) and the status modes
   in MODE. A new one that Avade does not know makes its users no members at all.
3. Channel modes: new letters, which take a parameter, list modes.
4. User modes: new ones, and which only services may set.
5. Chanflags (SVSXCF).
6. SVS* commands and the other commands services send or get.
7. What decides a join (`can_join`, `m_join`), which Avade repeats for join
   requests.
8. Host masking: that the `avade_uhm` module still builds and gives the mask
   Avade computes.
9. What the ircd does not send back to services.
10. Build options and settings the test network needs.
11. The tests: every failing check, with the change that explains it.

What it finds is one of three kinds, and the kind says what Avade has to do:

- **Changed:** something that was there works in another way. Avade follows it.
- **New:** something that was not there. If the network wants to see or control
  it through services, the support is built.
- **Gone:** something that was removed or is deprecated. Avade stops sending and
  offering what is removed. What is only deprecated keeps working, and the
  report says that it will go. Nothing is removed from the database for it.

The answer is a report with one line for each of the eleven points: "nothing",
or the kind, what it is and what was done. It is in the pull request, in the
issue, or in the notes of the automatic release, so it can be seen that every
point was looked at. In the issue the lines are boxes to tick off while you
review the pull request; the points with nothing to do are ticked already.
There is one issue and one pull request for a version, not one for each thing. Where there is a choice only you can make (the name of a
command, who may use it, a new access list), Claude builds what is closest to
how Avade does the same today and names the choice in the pull request.

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
