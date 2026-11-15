# Config objects: `Configuration` vs. `Style`

A configurable type nests its config object as an inner class and marks it with a DSL marker.
The name of the config object and its marker depend on the **kind** of the owning type.
Decisions: ADL 0017
(one mutable config object), ADL 0048
(`apply…` methods).

| Kind | Config object | DSL marker | Ctor lambda param | Contents |
|------|---------------|------------|-------------------|----------|
| [Layer][layer] / [Gestalt][gestalt] | `Configuration` | [`@ConfigurationDsl`][configurationDsl] | `additionalConfiguration` | full surface: data, behaviour, appearance |
| Renderer [painter][painter] / [paintable][paintable] (holds a data source or render-input provider) | `Configuration` | [`@ConfigurationDsl`][configurationDsl] | `additionalConfiguration` | full surface: data source, render inputs, appearance |
| Decorator [painter][painter] / [paintable][paintable] (purely visual) | `Style` | [`@StyleDsl`][styleDsl] | `styleConfiguration` | purely visual: colors, widths, fonts, gaps |

The kind of a painter/paintable follows from **what its config holds**:

- A painter/paintable whose config holds a **data source or a render-input provider** (a history
  storage, a time/value-range provider, a tick-value provider, a visible-series index provider,
  aggregation behaviour, value/color providers) is a **renderer-painter**. Its whole job is turning
  data into pixels, so its config carries data + behaviour + appearance — exactly like a Layer. It
  uses `Configuration` / [`@ConfigurationDsl`][configurationDsl] / `additionalConfiguration`. The
  render input often cannot move to a shared owner: e.g. the compass base painters each hold a
  different tick-provider type intrinsic to how they render, and `GaugeBasePainter.paintBase(...)`
  receives the value range as a paint parameter.
- A painter/paintable whose config is **purely visual** (colors, widths, fonts, gaps) is a
  **decorator-painter**. It uses `Style` / [`@StyleDsl`][styleDsl] / `styleConfiguration`. Data and
  behaviour belong in the owning Layer/Gestalt `Configuration`, never in a decorator `Style`.

Separate markers (rather than a single shared one) keep the DSL receiver scopes isolated: a painter
style builder nested inside a layer configuration builder cannot accidentally resolve the layer's
members, and vice versa.

## Shape

A layer/gestalt owns exactly one `Configuration` instance, built from the constructor defaults and
then the caller's `additionalConfiguration` lambda:

```kotlin
class XxxGestalt(
  initialFoo: Foo = …,
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractChartGestalt() {
  val configuration: Configuration = Configuration(initialFoo).also(additionalConfiguration)

  @ConfigurationDsl
  inner class Configuration(var foo: Foo) { /* … */ }
}
```

A renderer-painter has the same shape as a layer/gestalt (`Configuration` / `additionalConfiguration`
/ `@ConfigurationDsl`). A decorator-painter has the same shape with `Style` / `styleConfiguration` /
`@StyleDsl` instead.

There is exactly **one** config instance per owner. Do not keep a second instance under a different
name (e.g. `configuration` and `style` side by side) — the two drift apart and mutations applied to
one are invisible to code reading the other.

## JavaScript API

The easy-api declares each JavaScript config object as a `@JsExport external interface` with nullable
fields, e.g. `BarChartGroupedStyle : BarChartStyle` in
[`BarChartGroupedJsModel.kt`][barChartGroupedJsModel]. `generateTypeScriptDefinitions` in the
easy-api `build.gradle.kts` generates `meistercharts-easy-api.d.ts` from these declarations. An
extension such as `BarChartGroupedGestalt.applyStyle(jsStyle)` in
[`BarChartGroupedExtensions.kt`][barChartGroupedExtensions] copies every non-null field onto the
gestalt's `Configuration`. Decision: ADL 0014.

The generator in `internal/open/ksp/ts-declaration-generator` writes a nullable Kotlin field as
`field?: T | null | undefined` (`TsType.toTypescriptTypeString`) and an `@JsExport enum class` as
`export declare abstract class` with one static getter per entry (`TsEnumClass`). The conversion
reads every received value through `sanitize()` from `it.neckar.open.charting.api.sanitizing`
(`internal/open/commons/kotlin-js`): an enum resolves by `enumValueOf(toString())`, so the entry
and its name as a string both work, and a wrong value throws `SanitizingFailedException` with the
permitted entries. Decisions: ADL 0036
(patch semantics), ADL 0053 (enums).

## Real examples

- Gestalt: [`BulletChartGestalt`][bulletChartGestalt] — `configuration` + `additionalConfiguration`.
- Renderer-painter: [`HistoryCanvasTilePainter`][historyCanvasTilePainter] — `Configuration` +
  `@ConfigurationDsl`; its config holds the `historyStorage`, the content-area time range and the
  visible data-series indices.
- Decorator-painter: [`LabelPainter2`][labelPainter2] — `Style` + `styleConfiguration` +
  `@StyleDsl`; its config holds only colors, fonts and gaps.

[layer]: ../meistercharts-canvas/src/commonMain/kotlin/com/meistercharts/algorithms/layers/Layer.kt
[gestalt]: ../meistercharts-canvas/src/commonMain/kotlin/com/meistercharts/charts/ChartGestalt.kt
[painter]: ../meistercharts-canvas/src/commonMain/kotlin/com/meistercharts/algorithms/painter/Painter.kt
[bulletChartGestalt]: ../meistercharts-canvas/src/commonMain/kotlin/com/meistercharts/charts/bullet/BulletChartGestalt.kt
[historyCanvasTilePainter]: ../meistercharts-canvas/src/commonMain/kotlin/com/meistercharts/algorithms/tile/HistoryCanvasTilePainter.kt
[labelPainter2]: ../meistercharts-canvas/src/commonMain/kotlin/com/meistercharts/algorithms/painter/LabelPainter2.kt
[paintable]: ../meistercharts-canvas/src/commonMain/kotlin/com/meistercharts/canvas/paintable/Paintable.kt
[configurationDsl]: ../meistercharts-canvas/src/commonMain/kotlin/com/meistercharts/canvas/ConfigurationDsl.kt
[styleDsl]: ../meistercharts-canvas/src/commonMain/kotlin/com/meistercharts/canvas/StyleDsl.kt
[barChartGroupedJsModel]: ../meistercharts-api/meistercharts-easy-api/src/jsMain/kotlin/com/meistercharts/api/bar/BarChartGroupedJsModel.kt
[barChartGroupedExtensions]: ../meistercharts-api/meistercharts-easy-api/src/jsMain/kotlin/com/meistercharts/api/bar/BarChartGroupedExtensions.kt
