# Configuration properties & the repaint model

How a single configurable property inside a [`Configuration`](config-objects.md) should be declared —
plain value, provider, or observable — and why.

## The repaint model is dirty-gated

MeisterCharts does **not** repaint every frame. The canvas is only repainted after
[`ChartSupport.markAsDirty`][chartSupport] has been called; painting runs inside
`dirtySupport.ifDirty { … }`. The render loop notifies refresh listeners every tick, but the paint
listeners fire only when the chart is dirty. See [`DirtySupport`][dirtySupport].

Consequence: **a property setter alone changes nothing on screen.** Something must mark the chart
dirty for the new value to become visible.

## The `markAsDirty` contract

- **Whoever mutates a config from outside is responsible for calling `markAsDirty` afterwards.**
  A plain config property does not, and should not, repaint by itself.
- Automatic reaction to a change (a setter or observable that triggers work) is only acceptable
  **inside** the owning gestalt/layer — as an implementation detail, not as hidden behaviour of a
  public config property.
- The public wrappers already honour this: the easy-api setters call `markAsDirty`
  (see [`MeisterChartsApi`][easyApi]), and the demo framework marks dirty after every bound control
  change (see [`MeisterchartsDemo`][demo]).

## Which declaration style?

Ask one question: **does anything need to *know* when this value changes** — to notify an observer,
to recompute something, or to invalidate a cache?

| Answer | Tool | When |
|--------|------|------|
| No — set and forget | **plain `var x: T`** | inbound config; the caller marks the chart dirty. The default for the large majority of properties. |
| No — read fresh | **provider `var xProvider: () -> T`** (or `TProvider`) | value pulled from an external live model, or a cheap derivation recomputed at paint time. |
| Yes — notify | **`ObservableObject` (outbound)** | the gestalt publishes a value that external code observes (interactive/derived state: visible time range, active selection, zoom). Use it whenever it is sensible or necessary. |
| Yes — recompute / cache | **`ObservableObject` + `.map` (derived)** | an expensive derived value, recomputed only when its inputs change instead of on every (possibly unrelated) repaint; or internal cross-layer recompute wiring. |

Naming: an observable property is `xProperty` with a `var x by xProperty` delegate; a provider ends
in `xProvider`.

### On the "recompute every paint" cost

Because paint is dirty-gated, a provider is not evaluated *per frame* — but it is evaluated on every
repaint, including repaints triggered by something unrelated (e.g. a moving tooltip). For an
expensive derivation that is worth avoiding: cache it in a derived `ObservableObject` via `.map`, so
it recomputes only when *its* input changes. That is a case-by-case performance judgement, not a
default.

## Real examples

- Plain inbound flag: `showGrid` / `showTooltips` in
  [`BarChartGroupedGestalt`][barChartGrouped] — set by the caller, read via `visibleIf { … }`.
- Provider: `valueRangeProvider`, `lineStyles: (Double) -> LineStyle` — pulled per paint.
- Derived **and** outbound: `contentAreaTimeRangeProperty` in [`TimeLineChartGestalt`][timeLine] is
  `contentAreaDurationProperty.map { duration -> TimeRange.fromStartAndDuration(…, duration) }` — a
  cached derivation that external code (easy-api) also `.consume`s.
- Auto-dirty wiring: [`registerDirtyListener`][chartSupport] (= `consume { markAsDirty(reason) }`)
  connects an observable to the dirty flag; used sparingly for a few framework-internal properties.

## Layer visibility mirrors the same choice

- `visibleIf { configuration.flag }` — re-evaluated at paint time; works when the mutator marks the
  chart dirty. Prefer this for a plain config flag.
- `visibleIfWithState(configuration.flagProperty)` — reacts to an observable flag and marks dirty
  itself. Only needed when the flag must be observable for another reason.

## Anti-pattern

Reflexively wrapping a **cheap inbound flag** (`showGrid`, `showDots`, a simple `margin`) in an
`ObservableObject` when nothing observes it, nothing derives from it, and it is not published
outward. Use a plain `var` and rely on the caller's `markAsDirty`.

[chartSupport]: ../meistercharts-canvas/src/commonMain/kotlin/com/meistercharts/canvas/ChartSupport.kt
[dirtySupport]: ../meistercharts-canvas/src/commonMain/kotlin/com/meistercharts/canvas/DirtySupport.kt
[easyApi]: ../meistercharts-api/meistercharts-easy-api/src/jsMain/kotlin/com/meistercharts/api/MeisterChartsApi.kt
[demo]: ../meistercharts-demos/meistercharts-demos-common/src/commonMain/kotlin/com/meistercharts/demo/MeisterchartsDemo.kt
[barChartGrouped]: ../meistercharts-canvas/src/commonMain/kotlin/com/meistercharts/charts/BarChartGroupedGestalt.kt
[timeLine]: ../meistercharts-canvas/src/commonMain/kotlin/com/meistercharts/charts/timeline/TimeLineChartGestalt.kt
