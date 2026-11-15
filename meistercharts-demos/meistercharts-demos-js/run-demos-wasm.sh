#!/usr/bin/env bash
#
# Runs the MeisterCharts demos in the browser - compiled to WebAssembly.
#
# Starts the webpack dev server (recompiles + reloads on source changes). Open
# http://localhost:8080/?all=true to list all demos (unlike the JS target, the
# wasm dev server does not auto-append ?all=true); ?uuid=<demo-uuid> opens a single demo.
#
# Usage:
#   ./run-demos-wasm.sh                 # development mode (~26 MB wasm, fast compile)
#   ./run-demos-wasm.sh --production    # production mode (Binaryen-optimized wasm)
#
# JS counterpart: ./run-demos-js.sh

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
GRADLE_WRAPPER="$SCRIPT_DIR/../../../../../gradlew"

MODULE=":internal:open:meistercharts:meistercharts-demos:meistercharts-demos-js"

TASK="wasmJsBrowserDevelopmentRun"
if [[ "${1:-}" == "--production" ]]; then
  TASK="wasmJsBrowserProductionRun"
elif [[ $# -gt 0 ]]; then
  printf 'ERROR: Unknown argument: %s (supported: --production)\n' "$1" >&2
  exit 1
fi

echo "Starting MeisterCharts demos (Wasm target) via $MODULE:$TASK"
exec "$GRADLE_WRAPPER" -PwasmJs=true "$MODULE:$TASK"
