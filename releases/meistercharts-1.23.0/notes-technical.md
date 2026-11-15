### Added

- `LineChartSimpleStyle`:
  - added property `visibleLines` to show/hide the lines of the chart
  - added property `tooltipBoxStyles` to set the styles for the tooltip for each line
- added support for tooltips to the the grouped bar chart; this lead to the following additions to `BarChartGroupedStyle`:
  - added property `showTooltip` to enable/disable tooltips
  - added property `tooltipFont` to set the font to be used for the tooltips
  - added property `tooltipFormat` to set the format to be used for values of the tooltips
  - added property `tooltipBoxStyles` to set the styles for the tooltip for each bar
  - added property `activeGroupBackgroundColor` to set the background-color to be used for the active group
- `TimeLineChartStyle`:
  - added property `crossWireLabelBoxStyles` to set how the boxes for the cross wire labels should be painted
  - added property `crossWireLabelTextColor` to set the color for the cross wire value labels

### Changed

- `TimeLineChartStyle`
  - set `visibleLines` to `[-1]` instead of `null` or `undefined` to imply that all lines should be visible
  - set `visibleValueAxes` to `[-1]` instead of `null` or `undefined` to imply that all value-axes should be visible
