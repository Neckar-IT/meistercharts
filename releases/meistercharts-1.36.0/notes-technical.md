### Added

#### Replaced DateTime-API (Klock) with minimal implementation

- reduces the dependencies - Klock is no longer required
- Increased supported time range: now time ranges with negative years are supported
- improved handling of time zones: The ticks in the time axis are aligned correctly in all cases (hopefully)
- improved performance: Fewer objects are instantiated when calculating the ticks for the time axis
- Attention: New tradeoff: Only the current timezone of the browser is supported for the time axis.
This should be ok for 98% of the users / use cases

### Fixed

- Guaranteed History Duration: Fix edge case where the history duration was too short.
Related to the alignment of the history buckets.

### Changed

Internal:

- Updated dependencies
- additional checks introduced: Kotlin Compiler Plugin (KSP) to avoid accidental boxing of primitive values added.
