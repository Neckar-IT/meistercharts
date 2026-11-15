### Added

- Improve log output for several layers

#### E2E tests

- Improve VirtualNowProvider: Introduce time progression step for virtual time.
- Introduce CSS marker class "paused" that signals that the virtual time has paused.
- Additional E2E tests for several charts and low level tests.

### Changed

Internal:

- Updated most `Layer`s to use the new pattern (`Configuration` with secondary constructor)
- Updated some `Gestalt`s to use the new pattern with `Configuration`
- Updated dependencies
