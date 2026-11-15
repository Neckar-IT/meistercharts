#!/usr/bin/env sh
echo "Opens the JS source files in Visual Studio Code"

set -e
set -x

GRADLE="$(pwd)/../../../../../gradlew"
export GRADLE

rm -r /tmp/_karma_webpack* || true

$GRADLE jsTest
code /tmp/_karma_webpack_*/commons.js



