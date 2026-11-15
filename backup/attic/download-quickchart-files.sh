#!/bin/sh

set -e
set -x

rm -R from-webserver 2> /dev/null || true
mkdir -p from-webserver
cd from-webserver || exit 1

#Download the files
scp -r root@neckar.it:/var/www/neckar.it/MeisterChart/sources/ .
scp -r root@neckar.it:/var/www/neckar.it/MeisterChart/npm/ .
