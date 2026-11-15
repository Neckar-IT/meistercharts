### Changed

- renamed interface DataSeriesConfiguration to DataSeries
- renamed interface TimeLineChartLineStyle to DataSeriesConfiguration
- added property ticksFormat to DataSeriesConfiguration (formerly TimeLineChartLineStyle)
- renamed property lineStyles to dataSeriesConfigurations in TimeLineChartStyle
- renamed property visibleTracesIndices to visibleLinesIndices in TimeLineChartStyle
- renamed function setDataSeriesConfigurations to setDataSeries in TimeLineChart
- tick-labels of value axis will be truncated by an ellipsis if there is not enough space (instead of being hidden)
