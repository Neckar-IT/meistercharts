# Painters: layout/paint split and scratch memory

Two guidelines for painter implementations. Both target the same goal: an allocation-free hot path
with a clear separation between computing a layout and drawing it.

## layout/paint split — not for every painter

The layer lifecycle ([Layer][layer]) is phased: first `layout()` is called on **all** layers, then
`paint()` on all layers. Painters are helpers **below** this phase boundary — the consuming layer
decides in which phase it calls them. Two painter kinds exist, both are correct:

| Kind | API | Call site in the layer | Living-code examples |
|------|-----|------------------------|----------------------|
| Two-phase | `layout(…)` fills painter-owned painting variables, `paint…(…)` only draws | `layout(…)` from `Layer.layout()`, `paint…(…)` from `Layer.paint()` | [`LabelPainter2`][labelPainter2] (consumer: [`CrossWireLayer`][crossWireLayer]), [`StripePainter`][stripePainter] (`layoutBegin`/`layoutValueChange`/`layoutFinish` + `paint`) |
| Immediate mode | `begin`/`addCoordinates`/`paint` — the coordinate stream goes directly into gc path ops | entirely within `Layer.paint()` | [`LinePainter`][linePainter] family (consumer: [`LineChartLayer`][lineChartLayer]), [`XYPainter`][xyPainter], area painters |

A painter gets its own layout phase **iff** at least one of these holds:

1. **Global view required before the first draw call** — the position of one element depends on all
   others (collision avoidance in [`LabelPainter2`][labelPainter2], segment building across value
   changes in [`StripePainter`][stripePainter] implementations).
2. **Geometry is needed outside `paint()`** — hit testing, mouse-over, tooltips (the open TODO
   "Save the label bounds (for mouse over)" in `LabelPainter2.paintLabels()` is this case).
3. **One layout, several paint passes** — the same computed coordinates are drawn multiple times
   (line + area + points).

If none applies, the split only costs: all coordinates must be buffered (memory + a second pass),
and the painter becomes stateful and no longer freely reusable — [`LineChartLayer`][lineChartLayer]
calls the same `linePainter` instance per line in a loop; a frame-wide split would require
per-line-indexed buffers.

Consequences:

- New painters with layout logic follow the [`LabelPainter2`][labelPainter2] /
  [`StripePainter`][stripePainter] pattern.
- Pure drawing primitives stay immediate mode; their input coordinates come from the painting
  variables of the consuming layer.
- Converting the existing [`LinePainter`][linePainter] / [`XYPainter`][xyPainter] family to
  two-phase is only justified when hit testing on lines/areas is wanted (criterion 2) — that is a
  feature of its own, not a consistency refactoring.

## Layout buffer package vs. algorithm scratch

The criterion is not "uses a type from [`com.meistercharts.canvas.layout.buffer`][bufferPackage]"
but the **role of the memory**. Both categories keep the hot path allocation-free:

| Role of the memory | Right tool | Living-code examples |
|--------------------|-----------|----------------------|
| Per-item layout state: filled in `layout()`, read in `paint()`, addressed by index | Buffer package ([`LayoutVariableWithSize`][layoutVariableWithSize], `prepare(size)` protocol, painting-variables pattern) | [`LabelPainter2`][labelPainter2] (`LayoutVariablesObjectBuffer<LayoutedLabel2>`), stripe painters, [`DirectLinePainter`][directLinePainter], [`FancyXyLinePainter`][fancyXyLinePainter], [`SplineLinePainter`][splineLinePainter], [`SimpleAreaBetweenLinesPainter`][simpleAreaBetweenLinesPainter] |
| Transient algorithm scratch within one computation (stacks, accumulators), never read in `paint()` | Reused grow-only fields (`DoubleArrayList`/`IntArrayList` from `it.neckar.open.collections`) | [`VerticalLabelPlacementSolver`][verticalLabelPlacementSolver] |

Why algorithm scratch does **not** belong into `DoubleMultiBuffer`/`IntMultiBuffer`:

1. **The protocol does not fit.** [`LayoutVariableWithSize`][layoutVariableWithSize] is
   `prepare(size)` → indexed overwrite. Append/stack semantics (`add`, push/merge/pop via
   `removeAt` — e.g. the PAVA cluster stack in
   [`VerticalLabelPlacementSolver`][verticalLabelPlacementSolver]) have no API there.
2. **No memory difference.** `DoubleMultiBuffer` wraps exactly one `DoubleArrayList` internally —
   grow-only reuse is identical.
3. **Wrong purpose level.** The buffer package is, per [`package.md`][bufferPackage], for the
   per-item `layout()`→`paint()` handoff; scratch is copied back into the painting variables after
   the computation.

Constraint: the buffer trim policy ([`HighWaterMarkTrimmer`][highWaterMarkTrimmer]) attaches to
the buffer package — raw `DoubleArrayList` fields are outside it.
Acceptable for scratch as long as it scales with small cardinality (e.g. the number of visible
labels), not with data-series length. If needed, a scratch class can implement
[`LayoutVariable`][layoutVariable] (`reset()` = `clear()`).

[layer]: ../meistercharts-canvas/src/commonMain/kotlin/com/meistercharts/algorithms/layers/Layer.kt
[labelPainter2]: ../meistercharts-canvas/src/commonMain/kotlin/com/meistercharts/algorithms/painter/LabelPainter2.kt
[crossWireLayer]: ../meistercharts-canvas/src/commonMain/kotlin/com/meistercharts/algorithms/layers/crosswire/CrossWireLayer.kt
[stripePainter]: ../meistercharts-canvas/src/commonMain/kotlin/com/meistercharts/algorithms/painter/stripe/StripePainter.kt
[linePainter]: ../meistercharts-canvas/src/commonMain/kotlin/com/meistercharts/painter/LinePainter.kt
[lineChartLayer]: ../meistercharts-canvas/src/commonMain/kotlin/com/meistercharts/algorithms/layers/linechart/LineChartLayer.kt
[xyPainter]: ../meistercharts-canvas/src/commonMain/kotlin/com/meistercharts/algorithms/painter/XYPainter.kt
[directLinePainter]: ../meistercharts-canvas/src/commonMain/kotlin/com/meistercharts/algorithms/painter/DirectLinePainter.kt
[fancyXyLinePainter]: ../meistercharts-canvas/src/commonMain/kotlin/com/meistercharts/algorithms/painter/FancyXyLinePainter.kt
[splineLinePainter]: ../meistercharts-canvas/src/commonMain/kotlin/com/meistercharts/algorithms/painter/SplineLinePainter.kt
[simpleAreaBetweenLinesPainter]: ../meistercharts-canvas/src/commonMain/kotlin/com/meistercharts/algorithms/painter/SimpleAreaBetweenLinesPainter.kt
[verticalLabelPlacementSolver]: ../meistercharts-canvas/src/commonMain/kotlin/com/meistercharts/algorithms/painter/VerticalLabelPlacementSolver.kt
[bufferPackage]: ../meistercharts-canvas/src/commonMain/kotlin/com/meistercharts/canvas/layout/buffer/package.md
[layoutVariableWithSize]: ../meistercharts-canvas/src/commonMain/kotlin/com/meistercharts/canvas/layout/buffer/LayoutVariableWithSize.kt
[layoutVariable]: ../meistercharts-canvas/src/commonMain/kotlin/com/meistercharts/canvas/layout/buffer/LayoutVariable.kt
[highWaterMarkTrimmer]: ../meistercharts-canvas/src/commonMain/kotlin/com/meistercharts/canvas/layout/buffer/HighWaterMarkTrimmer.kt
