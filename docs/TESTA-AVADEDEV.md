# Testa nya Avade på avadedev

Målet är att köra grenen `avade-2026-update` på testnätet mot en **kopia** av
produktionsdatabasen, innan något rörs i produktion.

Allt nedan görs på avadedev. Det enda som görs i produktion är steg 1 (dumpen),
och den läser bara.

## 1. Kopiera databasen från produktion

På produktionsmaskinen:

    mysqldump --single-transaction --hex-blob --default-character-set=binary \
        -u services -p avade > avade-prod.sql

`--default-character-set=binary` gör att texten kopieras byte för byte. Det är
viktigt eftersom lösen och mail ligger AES-krypterade i tabellerna.

Flytta filen till avadedev och läs in den i en egen databas:

    mysql -u root -p -e "create database avadetest"
    mysql -u root -p -e "grant all privileges on avadetest.* to 'avade'@'localhost' identified by 'VÄLJ-ETT-LÖSEN'"
    mysql -u root -p avadetest < avade-prod.sql

Spara `avade-prod.sql`. Om något går snett läser du bara in den igen.

## 2. Töm brevlådan i kopian

Kopian innehåller riktiga användares mailadresser. Se till att inget på
avadedev skickar mail till dem:

    mysql -u root -p avadetest -e "delete from mailbox"

Kör inte heller mailskriptet mot `avadetest`.

## 3. Bahamut 2.2.4

    git clone https://github.com/DALnet/bahamut.git
    cd bahamut
    git checkout v2.2.4
    ./configure --prefix=$HOME/ircd224
    make && make install

Kopiera `ircd.conf` från den gamla 2.2.2-installationen. Kontrollera att
connect-blocket för services har `flags H;` och att services- och statsnamnen
finns i `super`-blocket (U-lines).

Om bygget klagar på `__res_mkquery` eller `__dn_expand`: se
`tests/build-ircd.sh`, som har lösningen (gäller mest äldre bahamut).

## 4. Bygg Avade

Kräver Java 17 eller nyare och ant.

    git clone https://github.com/Pintuzoft/Avade.git
    cd Avade
    git checkout avade-2026-update
    ./make.sh install

Det lägger `avade.jar`, `lib/`, `template.conf` och `reference.conf` i `~/avade/`.

`make.sh` sätter `JAVA_HOME=/usr/lib/jvm/java17`. Ändra raden om Java ligger
någon annanstans.

## 5. services.conf

Utgå från produktionens `services.conf` och ändra:

| Inställning | Värde |
|---|---|
| `secretsalt` | **Samma som i produktion.** Uppgraderingen behöver det för att göra om lösenorden till hashar, och mailadresserna läses med det. Med fel salt vägrar uppgraderingen, och ingenting ändras. |
| `hubname`, `hubhost`, `hubport`, `hubpass` | testnätets hub, inte produktionens |
| `mysqldb`, `mysqluser`, `mysqlpass` | `avadetest` och användaren från steg 1 |
| `master` | ett nick som är registrerat i kopian och som du kan lösenordet till |

Lägg till de nya kommandona under `sa:` (annars får bara SRA använda dem, och
det står en varning om det vid start):

      - vhost
      - clone

Under `csop:` byts `getpass` mot `setpass`. GETPASS finns inte längre, och
SETPASS sätter ett nytt lösenord utan att någon ser det gamla:

      - setpass

Valfritt, ord som inte får ingå i användares vhostar:

    vhostforbidden:
      - "*admin*"
      - "*oper*"

Jämför gärna med `template.conf` för exakt utseende.

## 6. Starta

    cd ~/avade
    ./avade.sh start
    ./avade.sh log        # följ utskriften, ctrl-c avslutar bara visningen

Första starten uppgraderar databasen från produktionens version till 1.2609-8,
sju steg i ordning. Steget som byter teckenkodning till utf8mb4 går igenom alla
tabeller och kan ta en stund på en stor databas.

Kontrollera efteråt:

    mysql -u avade -p avadetest -e "select * from settings"

Versionen ska vara `1.2609-8`.

Det sista steget gör om alla lösenord till envägshashar. Det tar cirka 70 ms
per lösenord och kärna, så några tusen nick tar några minuter. Gamla lösenord
(historiken) tas bort. Efteråt ska den här ge 0:

    mysql -u avade -p avadetest -e "select count(*) from passlog where pass not like 'pbkdf2-sha256\$%'"

Efter uppgraderingen kan en äldre Avade inte längre köras mot databasen,
eftersom den inte kan läsa hasharna. Vägen tillbaka är dumpen från steg 1.

Stoppa med `./avade.sh stop` eller `/RootServ STOP`. Båda skriver klart till databasen först.
Undvik `kill -9`.

## 7. Att prova

Kör igenom det här med ett par klienter. Det som är markerat *viktigast* är
sådant som bara går att se med riktig data.

- *Viktigast:* identifiera dig med ditt vanliga lösen på ett gammalt nick, och
  med kanallösenordet (`/ChanServ IDENTIFY`) på en gammal kanal. Fungerar det
  har lösenorden överlevt flytten och gjorts om till hashar rätt.
- *Viktigast:* `/NickServ INFO` och `/ChanServ INFO` på nick och kanaler med
  å, ä, ö i topic eller annan text. Ska visas rätt.
- *Viktigast:* gå in i en gammal kanal där du är founder/SOP/AOP. Du ska få op,
  topic och modes ska sättas som förut.
- Starta om Avade medan du är identifierad. Du ska fortfarande vara
  identifierad efteråt, utan att skriva lösen.
- Starta om hubben medan Avade kör. Avade ska ansluta igen av sig själv inom
  en halv minut.
- `/ChanServ VOP #kanal ADD nick` och `HOP`. Nicket ska få +v respektive +h.
- `/NickServ SET VHOST min.egen.host` och titta i WHOIS.
- `/ChanServ CHANFLAG` på en kanal, starta om Avade, kontrollera att flaggorna
  är kvar.
- `/OperServ CLONE ADD`, `/OperServ SPAMFILTER` med target.
- `/NickServ RESETPASS <nick>` på ett nick med din egen mailadress. Mailet
  hamnar i `mailbox` (skicka det inte), och koden i det sätter ett nytt lösen
  med `/NickServ RESETPASS <nick> <kod> <nytt>`.
- `/NickServ SETPASS` och `/ChanServ SETPASS` som csop, och
  `/ChanServ SET #kanal PASSWD` som founder.

## 8. Hostmasking (valfritt)

Följ steg 10 i `INSTALL`. Med katalogerna från steg 3 ovan byggs modulen så här:

    cd Avade/bahamut-module
    ./build.sh ~/bahamut ~/ircd224

Finns det flera bahamut-källor på maskinen måste det vara den som den körande
ircd:n byggdes från, annars vägrar ircd:n ladda modulen.

Saltet (`uhmsalt`) ska vara ett eget för testnätet, inte produktionens.

## 9. Om något går fel

Spara `services.log` och det Avade skrev i terminalen. Läs sedan in dumpen på
nytt (steg 1) så är databasen tillbaka i utgångsläget.

## Den automatiska testsviten

I `tests/` finns en svit som startar ett eget litet nät (hub, leaf, MariaDB i
Docker, Avade) med tom databas och kör igenom funktionerna automatiskt:

    cd tests
    ./start.sh 2.2.4
    ./run_tests.py
    ./stop.sh

Den ersätter inte testet ovan, eftersom den inte har er riktiga data.
