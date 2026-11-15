### Added

- added property `showMouseWheelModifierHint` to `TimeLineChartStyle` in order to show/hide the mouse-wheel-modifier-hint
- added properties `decimalMinValues` and `decimalMaxValues` to `Sample` in order to define min/max-values for decimal-values at the native sampling period
- added `TimeLineChartLineStyle` to configure the appearance of lines shown in `TimeLineChart`; this implies
  - the support for dots where a sample-point is supposed to be
  - the support for min/max-areas
- added support for thresholds becoming active when hovered with the mouse; the corresponding new properties can be found in `Threshold`:
  - `labelColorActive` - defines the color of the label when the threshold is active
  - `lineStyleActive` - defines the line-style when the threshold is active
  - `labelBoxStyleActive` - defines the box-style of the label when the threshold is active

### Changed

- removed `defaultEntryDuration` from `DiscreteTimelineChartData`
- changed type of `lineStyles`-property found in `TimeLineChartStyle` from `Array<LineStyle>?` to `Array<TimeLineChartLineStyle>?` (see above)

### Fixed

- `DiscreteTimelineChart`: fixed freeze when `defaultEntryDuration` did not match the data at hand
