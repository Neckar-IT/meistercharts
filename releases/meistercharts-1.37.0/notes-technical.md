### Added

- improved build scripts and performance
- Playwright End2End tests for most of the charts

### Fixed

- Fix edge case in timeline axis where rounding of hours was wrong.

### Changed

- Serialization of HistoryBuckets simplified
  - Start/End of Buckets calculated on the fly
- Removed a lot of old code that has been replaced with new layers
- Improved code style: Removed a lot of warnings

#### Build

- Update to Kotlin 1.9.0
- Update to Gradle 8.3
- Improved synchronization of source code to GitHub
- Simplified configuration of Gradle build
