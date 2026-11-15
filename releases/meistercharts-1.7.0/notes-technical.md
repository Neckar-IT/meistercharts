### Fixed

- TimeLineChart: line styles will be applied to value-axes no matter what the number of available data-series is

### Changed

- TimeLineChart: history-configuration must be set via API and is no longer computed from the line-styles
- TimeLineChart: setting line-styles will overwrite all previously set line-styles
- all number-formats must be of type NumberFormatter; ValueFormatterStyle is no longer supported
