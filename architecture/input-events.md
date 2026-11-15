# Input events: listeners, taps and gesture state

Where the input-event decisions of the Architecture Decision Log live in code.

## Event types and consumption

ADL 0011

| Element | Code |
|---|---|
| Event families | sealed `MouseEvent`, `KeyEvent`, `TouchEvent`, `PointerEvent` in `internal/open/commons/kotlin-events/src/commonMain/kotlin/it/neckar/events/`, base `UiEvent` |
| Consumption | enum `EventConsumption` (`Ignored`, `Consumed`) in `meistercharts-canvas/.../events/EventConsumption.kt` |
| Dispatch | `LayerList` notifies layers with `consumeUntil(Consumed)` |

## Browser listeners

ADL 0058,
ADL 0061

- `CanvasJS.setUpTouchEventListeners()` and `CanvasJS.setUpMouseEventListeners()` register every listener with
  `passive = false`; `cancelIfConsumed` calls `stopImmediatePropagation` and `preventDefault` on a consumed event.
- The `touchend` listener calls both on every cancelable event, consumed or not.
- `setUpPointerEventListeners()` is deprecated and not called.
- `TouchEvent.touches` and `TouchEvent.touchesCount` are deprecated in favour of `targetTouches` and
  `targetTouchesCount`; `ifSingleTouch` and `ifNoTouch` count target touches.

## Taps

ADL 0059

| Gesture | Class | Parameter | Default |
|---|---|---|---|
| Single tap | `SingleTapGestureSupport` | `maxTapDuration` | 250 ms (`DefaultMaxTapDuration`) |
| Double tap | `DoubleTapGestureSupport` | `maxPressDuration` | 250 ms |
| Double tap | `DoubleTapGestureSupport` | `maxDelayDuration` | 400 ms |
| Double tap | `DoubleTapGestureSupport` | `maxDistanceBetweenTaps` | 20 px |

## Pressed state and actions

ADL 0055:
`Button.onDown()` sets `pressed`, `Button.onUp()` fires the `ButtonPressedEvent` actions only when `pressed` is set,
`Button.noHover()` clears it.

## Gesture state machine

ADL 0101: a gesture UI is a sealed state type whose transitions run `onExit` and `onEnter`. A small variant is
`ResizeByHandlesLayerState`, whose methods return the successor state.
