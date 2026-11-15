### Fixed

- fixed that clearing the history did not cancel data samples scheduled for storing
- fixed book keeping used in history storage
- fixed handling of hover events for toolbar buttons
- fixed updating the button state while dragging with the mouse
- fixed differentiation between logical and physical pixels when computing the translation of the canvas-rendering context; this also fixes the size and location of images under certain conditions

### Added

- the color of labels of a category axis may be set separately
- bars of a stacked bar chart may have a border
- text of entries of a legend may have an optimal width regarding their text length
- the visible area of any two charts may be synchronized
- added a simple gestalt to support charts with a slippy map
- time-line chart: the time axis became optional

### Changed

- the limits layer takes the horizontal and vertical axis orientation into account and switches up/down or left/right accordingly
- time-line chart: custom line styles are applied last and hence override any precomputed line-style property
- if an image could not be loaded a warning will be printed to the console
