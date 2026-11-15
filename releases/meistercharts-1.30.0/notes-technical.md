### Added

- `BarChartGrouped`: added a data-overflow indicator in the shape of a triangle
- `BulletChart`: added a data-overflow indicator in the shape of a triangle
- `BarChartStacked`: added support for value-axis
- `BarChartStackedStyle`: added property `remainderSegmentBackgroundColor` to set the background-color of the remainder-segments
- `LineChartSimple`: added property `tooltipWireStyle` to configure the appearance of the tooltip-wire
- `TimeLineChart`: added property `crossWireStyle` to configure the appearance of the cross-wire
- `TimeLineChart`: added property `enumsBackgroundColor` to configure the background of the part where the enum-values are shown
- `BalloonTooltipStyle`: added properties `headlineFont` and `headlineMarginBottom`
- `ValueAxisStyle`: added property `presentationType`
- `JustifyTickContent`: added value `SpaceGreedilyPreferRoundIndices` to the enum

### Changed

- adjusted the content area for all charts that support value-axes with their title on top
- `BarChartGrouped`: changed tooltips to being balloon-like
- `BulletChartConfiguration`: changed properties to configure thresholds
- `LineChartSimple`: changed properties to configure thresholds
- improved handling of NaN-values for tooltips
- `BalloonTooltipStyle`: changed property `symbolSize` of type `Size` to `symbolSizes` of type `Array<Size>`

### Fixed

- several minor bugfixes and improvements
