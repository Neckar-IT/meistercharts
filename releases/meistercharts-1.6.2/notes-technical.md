### Fixed

- Rectangle: fixed the computation of centerX and centerY
- fixed division by 0 in stacked bar chart
- double-click and mouse-wheel events are only consumed if they result in a zoom-related action
- mouse-down events are only consumed if the modifier matches the ones defined by the event handler
- fixed translation done by the TranslationLayer
- fixed processing events that occur when the dragging stops

### Added

- Paintable with resize-handles
- added SVG-icon for delete actions

### Changed

- introduction of the I18nConfiguration that contains all locales and the time zone needed to translate text and format data
- removed the padding from the empty box-style
- added git-commit date to version info
