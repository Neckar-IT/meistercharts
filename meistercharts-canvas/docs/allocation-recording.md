# Allocation Recording

Diagnostic feature that shows **which objects are allocated during layer paints** — type, estimated
volume, and (optionally) the allocation stacktrace. Use it to hunt down GC-jank sources: boxing,
fresh collections/arrays allocated per frame, etc.

## Usage

The recording is process wide and toggled via [`AllocationRecordingEngine.mode`](../src/commonMain/kotlin/com/meistercharts/canvas/allocation/AllocationRecordingEngine.kt):

```kotlin
AllocationRecordingEngine.mode = AllocationRecordingMode.ByType            // count + bytes per type, per layer
AllocationRecordingEngine.mode = AllocationRecordingMode.ByTypeAndStacktrace // + allocation stacktrace
// ... let the chart paint ...
val report = AllocationRecordingEngine.currentReport()
AllocationRecordingEngine.mode = AllocationRecordingMode.Off               // stop, clears samples
```

Interactive toggle (cycles Off → ByType → ByTypeAndStacktrace → Off): **Ctrl+Shift+Alt+A** while the
[`ToggleDebuggingModeLayer`](../src/commonMain/kotlin/com/meistercharts/algorithms/layers/debug/ToggleDebuggingModeLayer.kt)
is present.

The [`AllocationReport`](../src/commonMain/kotlin/com/meistercharts/canvas/allocation/AllocationReport.kt)
is grouped by layer (from the sample stacktrace) and type, sorted by sample count.

## How it works

- **JVM:** backed by JFR (`jdk.ObjectAllocationSample`). Setting `mode` to a recording mode starts an
  in-process `RecordingStream`; `Off` stops it. No `-javaagent`, no start flag, no restart — a plain
  JDK API, toggleable at runtime in a shipped build. Zero cost while `Off` (the stream is not running).
  Each sample is kept only if it happened on the paint thread — the JavaFX Application Thread, the same
  detection the rest of the code base uses (`FxUtils.isFxApplicationThreadRunning`) — and is attributed
  to a layer via the `Layer.layout`/`Layer.paint` frame closest to the root of its stacktrace. **Both
  render phases count**: `Layers.paintLayers` runs a layout pass and then a paint pass every frame, and
  layers allocate in both (e.g. the history stripe layer boxes its value-class index in layout *and*
  paint), so recording only `paint` would miss the layout allocations.
- **Browser (JS, Wasm):** always inert (`currentReport()` returns empty). The browser has no generic allocation hook.

### Sampled, not exact

JFR **samples** allocations. A `samples` count is **not an object count** — one sample stands for tens
of thousands of objects (≈ one TLAB refill, see below). Counts are only meaningful *relative* to each
other (proportional to allocated bytes per type). For an actual volume use `estimatedBytes` (the sum of
the JFR sample weights ≈ real allocated bytes). Good for finding hotspots; not a precise per-paint
object count.

The recording also excludes its own allocations: the overlay and `currentReport()` allocate on the
paint thread while rendering, so samples whose stacktrace runs through the recording itself
(`com.meistercharts.canvas.allocation.*` or `AllocationRecordingLayer`) are dropped — otherwise the
tool would measure itself (e.g. `TypeAllocation` showing up under the overlay's `LayerVisibilityAdapter`).

### Why JFR and not an allocation agent

The obvious alternative — `java-allocation-instrumenter` (a `-javaagent` that instruments every `new`
for exact counts) — does **not work on Java 25**: its bundled ASM cannot read class file major
version 69 (`IllegalArgumentException: Unsupported class file major version 69`), and version 3.3.4
(2022) is the last release. On the project's JVM 25 target it instruments almost nothing. JFR is the
Java-25-native, production-safe replacement.

## Trying it out

Run [`AllocationRecordingDemo`](../src/jvmTest/kotlin/com/meistercharts/canvas/allocation/AllocationRecordingDemo.kt)
(IDE: *Run `AllocationRecordingDemoKt`*). It paints a layer that allocates on every paint and prints the
report — the types allocated per layer, with the allocation site. To profile a real chart, paint your
gestalt's layers the same way (headless via `MockCanvasRenderingContext`, or drive the real render loop)
while `mode` is set.

Example output:

```
=== Allocation report - 757 samples, ~... bytes ===
[DemoAllocatingLayer] 757 samples
    321x com.meistercharts.model.Insets  @ ...DemoAllocatingLayer.paint(...)
    150x it.neckar.geometry.Coordinates  @ ...DemoAllocatingLayer.paint(...)
    150x java.util.ArrayList             @ ...DemoAllocatingLayer.paint(...)
```

**Note:** the browser demos (JS and Wasm) are inert — allocation recording only produces data on the JVM.

## Interactive: the JavaFX demo app

The interactive **JVM** host is the JavaFX demo app — run `ChartingDemosFX`.

1. Launch `ChartingDemosFX`, open any chart.
2. Press **Ctrl+Shift+Alt+A** to cycle the mode (Off → ByType → ByTypeAndStacktrace).
3. The [`AllocationRecordingLayer`](../src/commonMain/kotlin/com/meistercharts/algorithms/layers/debug/AllocationRecordingLayer.kt)
   overlay (top-right) shows the sampled types per layer live. It is added by default via
   `MeisterchartBuilder` and is visible while recording.
4. Press **Ctrl+Shift+Alt+S** to dump the full report — layer → type → the top stacktraces with sample
   counts (via [`AllocationReport.format()`](../src/commonMain/kotlin/com/meistercharts/canvas/allocation/AllocationReportFormat.kt))
   — to the log/console. The overlay cannot show stacktraces readably; use the dump to find the exact
   allocation site (e.g. which line boxes an `EnumDataSeriesIndex`).

The demo app already launches with `-XX:TLABSize=16k -XX:-ResizeTLAB` (in its `build.gradle.kts`
`applicationDefaultJvmArgs`) so the sampling is fine-grained — see below.

### Sampling granularity (why so few samples)

JFR samples ~once per **TLAB refill**, not per allocation and not on a timer. The default TLAB grows to
hundreds of KB, so a single paint frame (which allocates far less) is usually **not** sampled; a sample
lands only once the cumulative allocation crosses a TLAB boundary. The `throttle` setting only caps the
max event rate and is not the limiting factor here — the TLAB size is. Measured (8M `double[8]`):

| JVM flags | samples |
|---|---|
| default TLAB | 312 |
| `-XX:TLABSize=16k -XX:-ResizeTLAB` | 40 444 |
| `-XX:-UseTLAB` | 2 519 162 (≈30× slower) |

So: recording is **statistical** over many frames (the relative type/layer distribution is
representative), and a small fixed TLAB is what makes small per-paint allocations visible. `-XX:-UseTLAB`
catches almost everything but is far too slow for anything but a short measurement.

## Verification

The deterministic aggregation and layer attribution are covered by
[`AllocationRecordingEngineTest`](../src/jvmTest/kotlin/com/meistercharts/canvas/allocation/AllocationRecordingEngineTest.kt).
The end-to-end JFR pipeline (stream → sample parsing → attribution) is timing dependent and sampled, so
it is exercised by the runnable demo above rather than a unit test.
