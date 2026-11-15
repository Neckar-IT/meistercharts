### Added

- grouped and stacked bar charts may have a logarithmic value range
- line charts may have a logarithmic value range

### Fixed

- fixed handling of mouse-drag events

### Changed

- introduction of client-specific releases; in this turn the MeisterCharts npm package has got an individual scope
- value-ranges are part of the style of a chart and hence have been moved from the model class to the corresponding style class
- value-ranges have a new property, called "scale", that denotes whether the value range is of a linear or a logarithmic scale
- removed unused charts from the API, namely the map with stacked bars, compass and circular chart
- adjusted the default colors of toolbar buttons
- corporate design has been extended with button colors
- buttons use colors of the corporate design as a default
- a toolbar layer does no longer need to be initialized separately
