### Added

#### Timeline Chart

##### Min/Max Area

- add support for displaying min/max area

##### Points Painter

- add support for displaying points.
- Calculate optimal distance between data points depending on the size of the points

##### Tiles Precision improvements

The precision of the calculations related to tiles has been greatly improved.
Meistercharts support tiles with a resolution of microseconds in a time range of several hundred thousand years.

##### Downsampling Modes

There are now two downsampling modes available:

- Automatic
- None

When using the automatic mode, Meistercharts will automatically select the best downsampling mode for the current zoom level.
When using the none mode, no downsampling will be applied.
The user is responsible for providing the data in the correct resolution.

##### Custom Events for History Queries

- Custom events are now published when querying data from the history storage.

##### Timeline Chart Config Assistant

The new assistant allows a much simplified configuration of a timeline chart.
The assistant calculates the relevant properties and applies them to the chart automatically.

##### Reset Zoom Improvements

Reset zoom to defaults can be configured now: The affected axes can be configured now.

#### Startup behavior

- Meistercharts startup improvements: Meistercharts is now initialized *before* onLoad().
- Font loading is now done in parallel.
Repaint is triggered when all fonts are loaded.

#### New Colors API

A new colors API is available now.
This API reproduces the behavior of the HTML5 Canvas API.
All colors are now defined as RGBA values.
The alpha value is optional and defaults to 1.0.

Meisterchart supports all web colors and the following color formats:

- RGB
- RGBA
- HSL
- HSLA
- HEX
- HEXA
- CSS color names (with alpha)

- Add constants for Material Design colors

#### CSS classes

- All DIVs created by Meistercharts now have a CSS class.
This allows for easier styling.
- The Chart-ID is also added as CSS class.

#### Logging

Logging has been improved a lot.

##### Logging "CLI"

Meistercharts Log Levels can be configured using the Developer console:

- `meistercharts.rootLevel = "DEBUG"`
- `meistercharts.list()`

#### Virtual "Now"

Meistercharts now supports a virtual "now" for all charts.
Therefore, we have full control about the time now.
The is especially useful for end-to-end tests.

#### E2E Tests: Playwright Tests

- Meistercharts now supports end-to-end tests using Playwright.
- The tests are executed in our CI pipeline

##### Demos

- A lot of new demos have been created to visualize/test the new features
- Each Demo has a UUID.
This UUID can be used to create links to a specific demo.

### Changed

#### Performance Improvements

- Introduce cache for alpha colors
- Improved FPS calculations
- [TimelineChart] Tiles size has been optimized.
This improves the tile recreation performance by 65%.
- Improved precision for frame timestamps

#### Minor Improvements

- Sanitizing of API inputs has been improved: Additional checks have been added for min/max values
- Improve package structure
- [SlippyMap] Additional map providers implemented
- React integration: Add functional component

#### Deployment/Hosting

Meistercharts is now fully available on GitHub: https://github.com/Neckar-IT/meistercharts

#### Documentation

- Minimal examples for Vite, NPM, Yarn
- JS Fiddles added
- Add example for react application

### Fixed

- [Discrete Data Series] Fix visible data series for NaN and invisible data series
- [Thresholds] Improve mouse interaction handling: Avoid "flickering" when two thresholds are placed at the same location.
- [Timeline] Improve calculation of downsampling: Min/Max values are now calculated correctly using the lower levels, too
- [Timeline] Improve calculation of default zoom level: Viewport top is now calculated correctly.
- [Locale] Fix initial parsing of locale from browser
