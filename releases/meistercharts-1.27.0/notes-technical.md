### Added

- new chart `BulletChart`
- added `setConfiguration` to `MeisterChartsApi` which should be called instead of `setData` and `setStyle`

### Changed

- `BarChartGroupedStyle`: replaced properties `tooltipFont`, `tooltipFormat` and `tooltipBoxStyles` with property `tooltipStyle`
- `LineChartSimpleStyle`: replaced properties `tooltipFont`, `tooltipFormat` and `tooltipBoxStyles` with property `tooltipStyle`
- renamed `CategoriesData` to `CategoriesSeriesData` and `CategoryData` to `CategorySeriesData`
- improved layout of bars shown in `Histogram`
