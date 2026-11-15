### Changed

- the precision of the samples stored into the history of the `TimeLineChart` has been tremendously increased.
Previously, every sample was stored as an 32-bit integer value.
This version stores every sample as a 64bit floating point value.
- improved visibility of labels painted at the cross-wire
- all lines of the `TimeLineChart` are visible by default
- all value-axes (up to 10) of the `TimeLineChart` are visible by default
- split property `thresholds` of `BarChartGroupedData` into property `thresholdValues` of `BarChartGroupedData` and property `thresholdLabels` of `BarChartGroupedStyle`
- split property `thresholds` of `LineChartSimpleData` into property `thresholdValues` of `LineChartSimpleData` and property `thresholdLabels` of `LineChartSimpleStyle`
- `DataSeries`: renamed property `dataStructureId` to `ìd`
- `DecimalDataSeriesStyle`: changed type of property `ticksFormat` from `TicksFormat` to `NumberFormat`
- `ValueAxisStyle`: changed type of property `ticksFormat` from `TicksFormat` to `NumberFormat`

### Added

- `CategoryLineChart`: added cross-wire
- `LineChartSimpleStyle`: added properties `showTooltip`, `tooltipFont` and `tooltipFormat`
- `DataSeries`: added property `name`
- `TimeLineChartStyle`: added property `lineStyles`

### Removed

- `LineChartSimpleStyle`: removed properties `valueLabelFont`, `valueLabelColor` and `valueLabelStrokeColor`
- `DataSeries`: removed property `fractionDigits`
- `TimeLineChartStyle`: removed property `valueAxesMaxCount`
- `DecimalDataSeriesStyle`: removed properties `pointType`, `pointSize`, `pointLineWidth`, `pointColor1`, `pointColor2`, `lineStyle`
- removed type `Threshold`

### Deprecated

- `TicksFormat`: marked as deprecated
