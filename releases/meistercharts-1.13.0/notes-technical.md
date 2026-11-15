### Changed

- renamed property LineChartSimpleStyle#tracesStyles to lineStyles
- renamed interface LineChartSimpleTraceStyle to LineChartLineStyle
- renamed property TimeLineChartStyle#lineChartTraceStyles to lineStyles
- renamed property TimeLineChartStyle#visibleTraces to visibleLines
- renamed interface TimeLineChartTraceStyle to TimeLineChartLineStyle
- changed type of property TimeLineChartStyle#crossWireFormatter from NumberFormatter to DataSeriesNumberFormatter
- renamed enum LineType to PointConnectionStyle

### Added

- interface DataSeriesNumberFormatter: provides the means to format numbers of a data-series
