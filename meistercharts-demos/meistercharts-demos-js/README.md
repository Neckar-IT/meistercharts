# Demos JS

Runs the MeisterCharts demos in the browser for two Kotlin web targets: JavaScript (`js`) and
WebAssembly (`wasmJs`). The module carries the entry point `src/webMain/kotlin/main.kt` and the page
`index.html`; the demo runner and the shared styles live in
[`meistercharts-demos-common`](../meistercharts-demos-common/build.gradle.kts).

## Run demos

The launcher scripts start the webpack dev server, which recompiles and reloads on source changes:

```
./run-demos-js.sh                 # JavaScript, development mode
./run-demos-js.sh --production    # JavaScript, production mode

./run-demos-wasm.sh               # WebAssembly, development mode
./run-demos-wasm.sh --production  # WebAssembly, production mode
```

The JS dev server opens `/?all=true`, which lists all demos. The wasm dev server does not append
that parameter, so open the dev server URL (`http://localhost:8080` by default) with `?all=true`
by hand. `?uuid=<demo-uuid>` opens a single demo instead of the list.

Each script wraps one Gradle task:

| Script | Gradle task |
|---|---|
| `./run-demos-js.sh` | `:internal:open:meistercharts:meistercharts-demos:meistercharts-demos-js:jsBrowserDevelopmentRun` |
| `./run-demos-wasm.sh` | `-PwasmJs=true :internal:open:meistercharts:meistercharts-demos:meistercharts-demos-js:wasmJsBrowserDevelopmentRun` |

`--production` selects the `…ProductionRun` variant of the same task.

The `wasmJs` target is registered only when the build runs with `-PwasmJs=true`; without that
property none of the `wasmJs*` tasks exist.

## Regenerate meistercharts-demos-tailwind.css

```
./gradlew :internal:open:meistercharts:meistercharts-demos:meistercharts-demos-common:tailwindCompile
```

The task downloads the pinned TailwindCSS CLI and compiles
[`tailwind.input.css`](../meistercharts-demos-common/src/webMain/tailwind/tailwind.input.css) into
`../meistercharts-demos-common/src/webMain/resources/css/meistercharts-demos-tailwind.css`, which
`index.html` links.
