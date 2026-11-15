#!/usr/bin/env sh
set -e
set -x

GRADLE="$(pwd)/../../../gradlew"
export GRADLE

cd meistercharts-api/meistercharts-easy-api || exit 7
$GRADLE publishNpmBundle
