https://www.neckar.it/MeisterCharts/npm/meisterCharts-1.0.2.tar.gz

### Added

- Performance improvements
- Support of corporate designs and themes
- Adjustments of default colors and default fonts used in charts
- TimeLineChart: size of content area is adjusted when natural sampling period changes
- TimeLineChart: minimum gap factor is configurable
- Added values to SamplingPeriod to support greater distances between samples
- TimeLineChart: paint dots for lines that consist of only a single sample
- Zoom and translation: double click handling is configurable
- Logarithmic value axis

### Changed

- Colors provided to circular chart are used in a modulo fashion
- TimeLineChartData: new property minGapSizeFactor
- TimeLineChart: new function clearHistory()
- SamplingPeriod: new entries

### Fixed

- Fixed computation errors when canvas or content area has no size
- Fixed alignment of history-buckets between different sampling periods
