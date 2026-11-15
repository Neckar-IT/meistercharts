### Added

- StringShortener: Use truncateToLength from common code (performance improvement)
- Extract KeyCode/KeyEvents to common code (improved test coverage)
- Log messages greatly improved
- End-to-End Tests: Added Playwright tests for several charts
- Extract geometry module (improved test coverage)

#### Virtual Time

Meistercharts supports a virtual time in all charts.
This allows for easier testing.

### Fixed

- Fix Category Chart: Category Axis repaint missing (#3)
- Update dependencies
- Remove IE11 workarounds (IE11 is no longer supported)
- Handling of default time zone improved (default time zone is now UTC - relevant for E2E tests)

#### Examples

- Updated the links within the examples.

### Changed

Build / Tests:

- Improve JSON comparisons (using skyscreamer/jsonassert)
