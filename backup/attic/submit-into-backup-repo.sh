#!/bin/sh

set -e
set -x

cd release-repo || exit 1
git checkout master
git pull

rsync -a ../from-webserver/* . --exclude snapshots --exclude "*-SNAPSHOT.tar.gz" --exclude "*-SNAPSHOT-sources.zip"

git add .
git commit -m "add binary releases"
git push origin master
