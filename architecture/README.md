# MeisterCharts Architecture

Architecture notes and conventions for MeisterCharts. Each document states the rule and links to the
source of truth in code — the code is the documentation, these notes only explain intent and the
decisions behind it.

## Contents

- [Config objects: `Configuration` vs. `Style`](config-objects.md) — how configurable types expose
  their config object, and which DSL marker to use.
- [Configuration properties & the repaint model](configuration-properties.md) — plain value vs.
  provider vs. observable, the dirty-flag rendering model, and the `markAsDirty` contract.
- [Painters: layout/paint split and scratch memory](painters.md) — when a painter gets its own
  layout phase, and layout buffers vs. reused algorithm scratch.
- [Input events](input-events.md) — event types and consumption, browser listeners, tap detection,
  pressed state and the gesture state machine.

## Demos

- Every gestalt has a `MeisterchartsDemoDescriptor`, written in the merge request that adds the gestalt. The demo runs the gestalt
  on generated data and exposes its interactions through `configurable` and `declare` controls.
- A gestalt of an open module registers its descriptor in `DemoDescriptors`
  ([`meistercharts-demos-common`](../meistercharts-demos/meistercharts-demos-common/src/commonMain/kotlin/com/meistercharts/demo/DemoDescriptors.kt));
  `DemoDescriptorsRegistrationTest` rejects a descriptor class missing there.
- A gestalt outside the open modules registers its descriptor in the demo registry of its own source tree.

## Units, timestamps, angles

- Unit and coordinate-space annotations (`@px`, `@ms`, `@rad`, `@Window`, `@Domain`, …) live in
  [`internal/open/units/unit`](../../units/unit/), packages `it.neckar.open.unit.*` and
  `com.meistercharts.annotations`; its `dependencyFence` keeps it free of commons modules
  (ADL 0024).
- Timestamps are `@ms Double` (`NowProvider.nowMillis()`); `TimeRangeUtilsKtTest.testPrecision` in
  `meistercharts-fx` checks the sub-microsecond round trip
  (ADL 0001).
- Drawing angles are clockwise `@rad` values, as in `CanvasRenderingContext.strokeArcCenter`; domain values in degrees
  carry `@deg` (ADL 0026).

## Browser low-level experiments

- Standalone HTML experiments on canvas and browser behavior live in
  [`meistercharts-api/meistercharts-easy-api/src/test/html`](../meistercharts-api/meistercharts-easy-api/src/test/html/);
  Kotlin code that relies on a finding names the file in a comment
  (ADL 0037).

## Chart state, zoom and layout

- `ChartState.contentViewportMargin` holds the margin around the content area; axis layers paint into it. Gestalts
  expose it as `contentViewportMargin` or `margin` in their configuration
  (ADL 0016).
- `ChartState.windowTranslation` is `@Zoomed`; `ZoomAndTranslationSupport` converts between coordinate spaces
  (ADL 0015).
- `AbstractChartState` rejects non-finite zoom and translation values with `require`; sizes are `@MayBeZero`, and
  `ZoomAndTranslationSupport.fitX`/`fitY`/`calculateFitZoomX` start with an explicit `chartState.hasAnyZeroSize` check
  (ADL 0043).
- `MeisterchartBuilder` defaults to `BindContentAreaSize2ContentViewport` and `ZoomAndTranslationDefaults.noTranslation`
  (zoom 1.0); `FittingWithMargin` and its variants compute a fitting reset zoom for fixed-size content areas
  (ADL 0046).
- `CanvasStringShortener` shortens labels; `GreedyCategoryAxisLabelPainter` and `MultiValueAxisLayer` skip what does not
  fit, toolbar `Button.priority` decides which buttons remain
  (ADL 0050).
- `CanvasRenderingContext.fillText` and every anchored layer configuration carry `anchorGapHorizontal`/`anchorGapVertical`
  (ADL 0065).
- `FontCenterAlignmentStrategy.calculateCenterOffset` returns the 2 % capital-height lift used by
  `AbstractCanvasFontMetricsCalculator.calculateCorrectionValueCenter`
  (ADL 0049).
