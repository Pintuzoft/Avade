# Bevakning av nya bahamut-versioner

`.github/workflows/bahamut-watch.yml` körs varje måndag. Den kollar om
DALnet/bahamut har en ny release (en tagg som `v2.2.5`, inte rc) som är nyare
än den Avade stöder (`bahamut` i `src/core/Version.java`).

När en ny version finns:

1. Testsviten körs mot den nya versionen, på ett eget testnät i GitHub Actions.
2. Claude läser vad som ändrats i bahamut sedan den version Avade stöder, och
   avgör om något påverkar services eller behöver styras från services.
3. Utfallet blir ett av två:
   - **Testerna går igenom och inget nytt behöver byggas:** Avade släpps
     automatiskt. `Version.java` och `tests/env.sh` får den nya bahamut-versionen,
     Avade får nästa versionsnummer, `dist/Avade.jar` byggs om, och det blir en
     commit på master, en tagg och en release.
   - **Något behöver byggas eller testerna går inte igenom:** Claude bygger
     stödet enligt `CLAUDE.md` på grenen `bahamut-<version>`, kör testsviten och
     öppnar en pull request. Det gäller kommandon, konfig, access, hjälptexter,
     DB-steg och tester. Ett issue beskriver vad som hänt. Ni granskar och mergar,
     och releasen görs sedan för hand.

En ny huvudversion (till exempel 3.0) släpps aldrig automatiskt. Den ger bara
issue och pull request.

En version som redan har ett issue (öppet eller stängt) testas inte igen varje
vecka. Kör den för hand om ni vill testa igen.

Automatiska releaser ändrar inte databasen och lägger inget steg i `DBChanges`.
Databasen behåller versionen från sin senaste ändring, och nästa riktiga
ändring körs som vanligt.

## Att sätta upp

Workflowen körs från master, så den börjar gälla när den här grenen är mergad.

1. **Actions får skriva:** Settings → Actions → General → Workflow permissions:
   *Read and write permissions*.
2. **Claude (för analys och pull requests):**
   - Installera Claude-appen på repot: https://github.com/apps/claude.
     Enklast är `/install-github-app` i Claude Code, som gör båda stegen.
   - Lägg en secret under Settings → Secrets and variables → Actions. Antingen
     `CLAUDE_CODE_OAUTH_TOKEN` (från `claude setup-token`, använder ert
     Claude-abonnemang) eller `ANTHROPIC_API_KEY` (betalas per användning).
   - Utan secret körs bara testerna. När de går igenom släpps Avade, utan att
     någon har läst ändringarna i bahamut.
3. **Prova:** Actions → *bahamut watch* → *Run workflow*. Ange en version, till
   exempel `2.2.4`, och låt *dry run* vara ikryssad. Då körs testerna och
   analysen, men ingenting skapas.

Om master har skydd som kräver pull requests kan den automatiska releasen inte
pusha. Då blir det ett issue i stället.

## Kostnad och tid

GitHub Actions är gratis för publika repon. En körning med en ny version tar
20–60 minuter: bygget av bahamut, testsviten (cirka 15 minuter) och Claudes
arbete. Veckor utan ny version tar några sekunder och använder inte Claude.
