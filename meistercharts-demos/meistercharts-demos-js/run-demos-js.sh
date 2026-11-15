#!/usr/bin/env bash
#
# Runs the MeisterCharts demos in the browser - compiled to JavaScript.
#
# Starts the webpack dev server (recompiles + reloads on source changes) and opens
# the browser at /?all=true. Append ?uuid=<demo-uuid> to the URL to open a single demo.
#
# Usage:
#   ./run-demos-js.sh                 # development mode (fast compile, readable output)
#   ./run-demos-js.sh --production    # production mode (optimized bundle)
#
# Wasm counterpart: ./run-demos-wasm.sh

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
GRADLE_WRAPPER="$SCRIPT_DIR/../../../../../gradlew"

MODULE=":internal:open:meistercharts:meistercharts-demos:meistercharts-demos-js"

TASK="jsBrowserDevelopmentRun"
if [[ "${1:-}" == "--production" ]]; then
  TASK="jsBrowserProductionRun"
elif [[ $# -gt 0 ]]; then
  printf 'ERROR: Unknown argument: %s (supported: --production)\n' "$1" >&2
  exit 1
fi

echo "Starting MeisterCharts demos (JS target) via $MODULE:$TASK"
exec "$GRADLE_WRAPPER" "$MODULE:$TASK"
