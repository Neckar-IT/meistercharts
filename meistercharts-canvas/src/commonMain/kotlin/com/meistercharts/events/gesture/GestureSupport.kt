package com.meistercharts.events.gesture

import com.meistercharts.events.PointerEventBroker

/**
 * Handles various gestures from pointer events
 *
 */
@Deprecated("currently unused")
class GestureSupport(pointerEvents: PointerEventBroker) {

  private val pinchGestureSupport = PinchGestureSupport(pointerEvents)

  /**
   * Register a callback that is notified about [PinchGesture]s
   */
  fun onPinched(callback: (PinchGesture) -> Unit): Boolean {
    return pinchGestureSupport.onPinched(callback)
  }

}

/**
 * The state of a gesture
 */
enum class GestureState {
  /**
   * A gesture has not been recognized yet but may be possible
   */
  Possible,

  /**
   * Pointer events haven been received recognized as the begin of a continuous gesture
   */
  Began,

  /**
   * Pointer events haven been received recognized as a change to a continuous gesture
   */
  Changed,

  /**
   * Pointer events haven been received recognized as the end of a continuous gesture
   */
  Ended,

  /**
   * Pointer events have been received resulting in the cancellation of a continuous gesture
   */
  Cancelled
}
