### Changed

- bars are no longer clipped against the content area but only against the present axes
- bar-value labels are painted with a white outline to ensure their readability
- the direction of bar-value labels is automatically inverted if there is not enough space

### Added

- added properties to `BarChartGroupedStyle`
  - showValueLabels - whether to show the value of a bar in a separate label
  - valueLabelGap - the distance between a value label and its corresponding bar in pixels
  - valueLabelFormat - the format to be used for the value-label of a bar
