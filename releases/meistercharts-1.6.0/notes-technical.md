### Added

- line chart supports category grid-lines
- line chart: grid lines of the value axis may be turned off
- added `valueLabelFont`-Property to `BarChartStackedStyle` to be able to set the font for the value labels
- added `valueLabelFont`-Property to `LineChartSimpleStyle` to be able to set the font for the value labels
- added `crossWireFont`-Property to `TimeLineChartStyle` to be able to set the font for the labels of the cross wire of the time-line chart
- added support for various point-connection types to line chart
- number-formatter may be specified for the labels of the cross wire of the time-line chart
- added property `justifyTickContent` to `CategoryAxisStyle` to be able to set how labels along the category axis are laid out

### Changed

- removed `font`-Property from `BarChartStyle`
- removed `font`-Property from `TimeLineChartStyle`
- `LineChartSimpleStyle`: split `gridStyle`-Property int separate properties to style grid lines for category axes and value axes
- line-colors provided for grids are no longer necessarily associated with a domain-value
- time-line chart has become more robust against extreme combinations of sampling-rates and acutally added samples
