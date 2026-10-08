#!/bin/bash
# Build dist/AvadeMailer.jar: mailer/build.sh
# The libraries are those of Avade (lib/) and the mail ones (lib/mail/).
set -e
cd "$(dirname "$0")/.."
B=$(mktemp -d)
trap 'rm -rf "$B"' EXIT
mkdir "$B/classes"
javac --release 17 -nowarn -encoding UTF-8 -d "$B/classes" -cp "lib/*:lib/mail/*" $(find mailer/src -name '*.java')
printf 'Class-Path: %s\nMain-Class: mailer.Main\n' \
    "$(cd dist && ls lib/*.jar lib/mail/*.jar | tr '\n' ' ' | sed 's/ $//')" > "$B/manifest.txt"
jar cfm dist/AvadeMailer.jar "$B/manifest.txt" -C "$B/classes" .
echo "Built dist/AvadeMailer.jar"
