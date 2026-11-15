### Added

- Every chart dispatches custom-events of type "ContentAreaSizeChanged" whenever the size of their content-area changes.
This size is independent of the zoom currently set for that chart.

### Changed

- Renamed property barWidth of the BarChartGroupedStyle interface to barSize; also the value will be treated as the actual bar size and not as the maximum bar size.
