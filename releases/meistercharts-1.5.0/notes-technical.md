### Added

- added interface `CategoryAxisStyle` to support styling of category axes
- added interface `TimeAxisStyle` to support styling of time axes
- new algorithm to layout category labels of a category axis of a line chart
- time axis with offset

### Fixed

- fixed collecting samples before adding them to the time-line chart

### Changed

- Time-line chart
  - moved properties `visibleTimeRange` and `crossWirePosition` from `TimeLineChartData` to `TimeLineChartStyle`
  - added properties to style the value axes to `TimeLineChartStyle`
  - removed the property `unit` from `TimeLineChartTraceStyle`
- renamed `ValueRangeScale` enum value `logarithmic` to `log10`
- add style-related properties to `AxisStyle`
- switched to Intl date-time format (removed IE11 support)
- category line chart uses new algorithm to layout category labels as default
- improved handling of mouse related drag and drop events
