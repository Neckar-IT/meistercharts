### Added

#### Logarithmic Value Axis

- Display only ticks corresponding to powers of ten
- Enhanced support for logarithmic axes with very small value ranges: Start and end are still displayed.
- New heuristic for very small tick counts (1 or 2 max ticks)

#### Demo Pages

- Improved URL for demo pages: Ability to filter visible demos by quality and stability

#### Generation of TypeScript definition files

- Using KSP to generate TypeScript definition files

### Fixed

#### Easy API: #dispose() remove holder

Calling `dispose()` clears up everything related to the chart:
The `holder` is now also removed from the DOM - even if it has not been added to the DOM by meistercharts itself.

#### E2E-Tests

- Virtual-Time Policy removed: Avoid strange errors related to date
- Added a lot of additional E2E tests

### Changed

Build:

- Update to Kotlin 1.9.10
- Update to Gradle 8.4
- Updated numerous libraries
- Improved synchronization of source code to GitHub
- Simplified configuration of Gradle build - using new syntax
- Improved code style (Detekt)
- Cleanup test dependencies: Removed unused dependencies (e.g. Assertj)
