/**
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.events.gesture

import com.meistercharts.annotations.Window
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Distance
import com.meistercharts.model.Zoom
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.Pointer
import com.meistercharts.events.PointerDownEvent
import com.meistercharts.events.PointerEventBroker
import com.meistercharts.events.PointerMoveEvent
import com.meistercharts.events.PointerUpEvent
import com.meistercharts.events.distanceDeltaAbsolute
import it.neckar.open.unit.other.px
import it.neckar.logging.LoggerFactory

/**
 * Handles pinch gestures with pointer-events
 */
@Deprecated("currently unused")
class PinchGestureSupport(pointerEvents: PointerEventBroker) {
  /**
   * The current state the gesture is in.
   *
   * The state may change like this:
   *
   * [GestureState.Possible] => [GestureState.Began] => [GestureState.Changed] => [GestureState.Ended]/[GestureState.Cancelled] => [GestureState.Possible]
   */
  private var gestureState = GestureState.Possible

  /**
   * The distance between the two points at the start of the gesture
   */
  @px
  private var startDistance: Distance = Distance.zero

  /**
   * The 1st pinch pointer (e.g. first finger)
   */
  private var pinchPointer1st: Pointer? = null

  /**
   * The 2nd pinch pointer (e.g. second finger)
   */
  private var pinchPointer2nd: Pointer? = null

  /**
   * The center between the two pinch pointers
   */
  @px
  @Window
  private val pinchCenter: Coordinates
    get() {
      val c1 = pinchPointer1st?.coordinates ?: return Coordinates.none
      val c2 = pinchPointer2nd?.coordinates ?: return Coordinates.none
      return c1.center(c2)
    }

  /**
   * The zoom computed from the current span between [pinchPointer1st] and [pinchPointer2nd] (aka [pinchDistance])
   */
  private val pinchZoom: Zoom
    get() {
      if (pinchPointer1st == null || pinchPointer2nd == null) {
        return Zoom.default
      }
      if (startDistance == Distance.zero) {
        return Zoom.default
      }

      if (pinchDistance.isZero()) {
        throw UnsupportedOperationException("uups")
      }

      return pinchDistance.let { pinchDistance ->
        Zoom(
          pinchDistance.x / startDistance.x,
          pinchDistance.y / startDistance.y
        )
      }
    }

  /**
   * The distance between [pinchPointer1st] and [pinchPointer2nd]
   */
  private val pinchDistance: Distance
    get() {
      val c1 = pinchPointer1st?.coordinates ?: return Distance.none
      val c2 = pinchPointer2nd?.coordinates ?: return Distance.none
      return c1.deltaAbsolute(c2)
    }

  /**
   * Callbacks for [PinchGesture]s
   */
  private val pinchedCallbacks = mutableListOf<(PinchGesture) -> Unit>()

  /**
   * Register a callback that is notified about [PinchGesture]s
   */
  fun onPinched(callback: (PinchGesture) -> Unit): Boolean {
    return pinchedCallbacks.add(callback)
  }

  /**
   * Notify all registered callbacks about the current state of the pinch gesture
   */
  private fun notifyPinched(): EventConsumption {
    val pinchGesture = PinchGesture(gestureState, pinchZoom, pinchCenter)
    pinchedCallbacks.forEach {
      it(pinchGesture)
    }
    return EventConsumption.Ignored
  }

  init {
    pointerEvents.onDown {
      onDown(it)
    }
    pointerEvents.onMove {
      onMove(it)
    }
    pointerEvents.onUp {
      onUp(it)
    }
    pointerEvents.onCancel {
      cancelPinch()
    }
    pointerEvents.onOut {
      cancelPinch()
    }
    pointerEvents.onLeave {
      cancelPinch()
    }
  }

  private fun onDown(pointerEvent: PointerDownEvent): EventConsumption {
    if (pinchPointer1st?.pointerId == pointerEvent.pointer.pointerId) {
      // the pointer-event belongs to the first pointer -> update the first pointer
      pinchPointer1st = pointerEvent.pointer
      return EventConsumption.Ignored
    }

    if (pinchPointer2nd?.pointerId == pointerEvent.pointer.pointerId) {
      // the pointer-event belongs to the second pointer -> update the second pointer
      pinchPointer2nd = pointerEvent.pointer
      return EventConsumption.Ignored
    }

    if (pinchPointer1st == null) {
      // no pointer-down event so far -> update the first pointer
      check(gestureState == GestureState.Possible)
      pinchPointer1st = pointerEvent.pointer
      notifyPinched()
      return EventConsumption.Ignored
    }

    if (pinchPointer2nd == null) {
      // this must be the second pointer-down event -> update the second pointer
      check(gestureState == GestureState.Possible)
      pinchPointer2nd = pointerEvent.pointer
      notifyPinched()
      return EventConsumption.Ignored
    }

    // this must be at least the third pointer, so this can no longer be a pinch gesture
    return cancelPinch()
  }

  private fun onMove(pointerEvent: PointerMoveEvent): EventConsumption {
    if (pinchPointer1st == null || pinchPointer2nd == null) {
      // we need exactly two valid pointers for a pinch gesture
      return EventConsumption.Ignored
    }

    when (gestureState) {
      GestureState.Possible  -> {
        // this is the first move which marks the beginning of a pinch gesture
        gestureState = GestureState.Began
        startDistance = pinchPointer1st!!.distanceDeltaAbsolute(pinchPointer2nd!!)
        notifyPinched()
      }

      GestureState.Began     -> check(false) // should not happen
      GestureState.Changed   -> {
      }

      GestureState.Ended     -> check(false) // should not happen
      GestureState.Cancelled -> check(false) // should not happen
    }

    gestureState = GestureState.Changed
    updatePointers(pointerEvent.pointer)
    return notifyPinched()
  }

  private fun onUp(pointerEvent: PointerUpEvent): EventConsumption {
    val notifyEnded =
      when (gestureState) {
        GestureState.Possible  -> false // nothing really happened so far; not need to notify the handler about the end of a pinch
        GestureState.Began     -> true // the pinch gesture has already begun; we need to notify the handler about the end of a pinch
        GestureState.Changed   -> true // we are in the middle of a the pinch gesture which is ended by the pointer-up event; we need to notify the handler about the end of a pinch
        GestureState.Ended     -> false // the gesture is already completed (should not happen); no need to notify the handler about ending it
        GestureState.Cancelled -> false // the gesture is already cancelled (should not happen); no need to notify the handler about ending it
      }
    // update the current state before notifying the handler
    gestureState = GestureState.Ended
    if (notifyEnded) {
      // the coordinates of a pointer might have changed
      updatePointers(pointerEvent.pointer)
      notifyPinched()
    }
    reset()
    return EventConsumption.Ignored
  }

  /**
   * Cancels any pending gesture and notifies the callbacks if necessary
   */
  private fun cancelPinch(): EventConsumption {
    val notifyCancelled =
      when (gestureState) {
        GestureState.Possible  -> false // nothing really happened so far (should not happen); no need to notify the handler about cancelling
        GestureState.Began     -> true // the pinch gesture has already begun; we need to notify the handler about cancelling it
        GestureState.Changed   -> true // we are in the middle of a gesture; we need to notify the handler about cancelling it
        GestureState.Ended     -> false // the gesture is already completed (should not happen); no need to notify the handler about cancelling it
        GestureState.Cancelled -> false // the gesture is already cancelled (should not happen); no need to notify the handler about cancelling it again
      }
    // update the current state before notifying the handler
    gestureState = GestureState.Cancelled
    if (notifyCancelled) {
      notifyPinched()
    }
    reset()
    return EventConsumption.Ignored
  }

  /**
   * Updates [pinchPointer1st] or [pinchPointer2nd] with data provided by [pointer] if their corresponding pointer-id matches
   */
  private fun updatePointers(pointer: Pointer) {
    when {
      pinchPointer1st?.pointerId == pointer.pointerId -> pinchPointer1st = pointer
      pinchPointer2nd?.pointerId == pointer.pointerId -> pinchPointer2nd = pointer
    }
  }

  /**
   * Resets the state of this [PinchGestureSupport].
   *
   * Call this method after a gesture has been ended or cancelled.
   */
  private fun reset() {
    gestureState = GestureState.Possible
    startDistance = Distance.zero
    pinchPointer1st = null
    pinchPointer2nd = null
  }
}

/**
 * A gesture describing a pinch with two pointers
 */
data class PinchGesture(
  /**
   * The state the gesture is in
   */
  val gestureState: GestureState,
  /**
   * The scale factor relative to the [Pointer]s of two [PointerEvent]s
   */
  val zoom: Zoom,
  /**
   * The center of the gesture
   */
  val center: Coordinates
)
