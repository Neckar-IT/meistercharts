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
package com.meistercharts.events

import assertk.*
import assertk.assertions.*
import it.neckar.geometry.Coordinates
import com.meistercharts.events.gesture.GestureState
import com.meistercharts.events.gesture.PinchGestureSupport
import it.neckar.events.Pointer
import it.neckar.events.PointerCancelEvent
import it.neckar.events.PointerDownEvent
import it.neckar.events.PointerId
import it.neckar.events.PointerMoveEvent
import it.neckar.events.PointerUpEvent
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

/**
 * A class to test [PinchGestureSupport]
 */
@Disabled
class PinchGestureSupportTest {

  @Test
  fun testPinchGestureStateComplete() {
    val pointerEvents = DefaultPointerEventBroker()
    val pinchHandler = PinchGestureSupport(pointerEvents)
    var gestureState: GestureState = GestureState.Possible
    pinchHandler.onPinched {
      gestureState = it.gestureState
    }

    pointerEvents.notifyDown(PointerDownEvent(1.0, Pointer(PointerId(1), Coordinates(10.0, 10.0))))
    assertThat(gestureState).isEqualTo(GestureState.Possible)

    pointerEvents.notifyDown(PointerDownEvent(1.0, Pointer(PointerId(1), Coordinates(20.0, 30.0))))
    assertThat(gestureState).isEqualTo(GestureState.Possible)

    pointerEvents.notifyDown(PointerDownEvent(1.0, Pointer(PointerId(2), Coordinates(50.0, 80.0))))
    assertThat(gestureState).isEqualTo(GestureState.Possible)

    pointerEvents.notifyMove(PointerMoveEvent(1.0, Pointer(PointerId(2), Coordinates(70.0, 80.0))))
    assertThat(gestureState).isEqualTo(GestureState.Changed)

    pointerEvents.notifyUp(PointerUpEvent(1.0, Pointer(PointerId(2), Coordinates(70.0, 80.0))))
    assertThat(gestureState).isEqualTo(GestureState.Ended)
  }

  @Test
  fun testPinchGestureAllStates() {
    val pointerEvents = DefaultPointerEventBroker()
    val pinchHandler = PinchGestureSupport(pointerEvents)
    val expendedGestureStates = mutableListOf<GestureState>()
    val actualGestureStates = mutableListOf<GestureState>()
    pinchHandler.onPinched {
      actualGestureStates.add(it.gestureState)
    }

    pointerEvents.notifyDown(PointerDownEvent(1.0, Pointer(PointerId(1), Coordinates(10.0, 10.0))))
    expendedGestureStates.add(GestureState.Possible)

    pointerEvents.notifyDown(PointerDownEvent(1.0, Pointer(PointerId(2), Coordinates(50.0, 80.0))))
    expendedGestureStates.add(GestureState.Possible)

    pointerEvents.notifyMove(PointerMoveEvent(1.0, Pointer(PointerId(1), Coordinates(50.0, 80.0))))
    expendedGestureStates.add(GestureState.Began)
    expendedGestureStates.add(GestureState.Changed)

    pointerEvents.notifyMove(PointerMoveEvent(1.0, Pointer(PointerId(2), Coordinates(150.0, 180.0))))
    expendedGestureStates.add(GestureState.Changed)

    pointerEvents.notifyUp(PointerUpEvent(1.0, Pointer(PointerId(2), Coordinates(160.0, 180.0))))
    expendedGestureStates.add(GestureState.Ended)

    pointerEvents.notifyUp(PointerUpEvent(1.0, Pointer(PointerId(1), Coordinates(60.0, 70.0))))

    pointerEvents.notifyDown(PointerDownEvent(1.0, Pointer(PointerId(1), Coordinates(10.0, 10.0))))
    expendedGestureStates.add(GestureState.Possible)

    assertThat(actualGestureStates).isEqualTo(expendedGestureStates)
  }

  @Test
  fun testPinchGestureStateIncomplete() {
    val pointerEvents = DefaultPointerEventBroker()
    val pinchHandler = PinchGestureSupport(pointerEvents)
    var gestureState: GestureState = GestureState.Possible
    pinchHandler.onPinched {
      gestureState = it.gestureState
    }

    pointerEvents.notifyDown(PointerDownEvent(1.0, Pointer(PointerId(1), Coordinates(10.0, 10.0))))
    assertThat(gestureState).isEqualTo(GestureState.Possible)

    pointerEvents.notifyDown(PointerDownEvent(1.0, Pointer(PointerId(1), Coordinates(20.0, 30.0))))
    assertThat(gestureState).isEqualTo(GestureState.Possible)

    pointerEvents.notifyDown(PointerDownEvent(1.0, Pointer(PointerId(2), Coordinates(50.0, 80.0))))
    assertThat(gestureState).isEqualTo(GestureState.Possible)

    pointerEvents.notifyUp(PointerUpEvent(1.0, Pointer(PointerId(2), Coordinates(70.0, 80.0))))
    assertThat(gestureState).isEqualTo(GestureState.Possible)
  }

  @Test
  fun testPinchGestureStateSinglePointer() {
    val pointerEvents = DefaultPointerEventBroker()
    val pinchHandler = PinchGestureSupport(pointerEvents)
    var gestureState: GestureState = GestureState.Possible
    pinchHandler.onPinched {
      gestureState = it.gestureState
    }

    pointerEvents.notifyDown(PointerDownEvent(1.0, Pointer(PointerId(1), Coordinates(10.0, 10.0))))
    assertThat(gestureState).isEqualTo(GestureState.Possible)

    pointerEvents.notifyDown(PointerDownEvent(1.0, Pointer(PointerId(1), Coordinates(20.0, 30.0))))
    assertThat(gestureState).isEqualTo(GestureState.Possible)

    pointerEvents.notifyMove(PointerMoveEvent(1.0, Pointer(PointerId(1), Coordinates(70.0, 80.0))))
    assertThat(gestureState).isEqualTo(GestureState.Possible)
  }

  @Test
  fun testPinchGestureStateCancelled() {
    val pointerEvents = DefaultPointerEventBroker()
    val pinchHandler = PinchGestureSupport(pointerEvents)
    var gestureState: GestureState = GestureState.Possible
    pinchHandler.onPinched {
      gestureState = it.gestureState
    }

    pointerEvents.notifyDown(PointerDownEvent(1.0, Pointer(PointerId(1), Coordinates(10.0, 10.0))))
    assertThat(gestureState).isEqualTo(GestureState.Possible)

    pointerEvents.notifyDown(PointerDownEvent(1.0, Pointer(PointerId(1), Coordinates(20.0, 30.0))))
    assertThat(gestureState).isEqualTo(GestureState.Possible)

    pointerEvents.notifyDown(PointerDownEvent(1.0, Pointer(PointerId(2), Coordinates(50.0, 80.0))))
    assertThat(gestureState).isEqualTo(GestureState.Possible)

    pointerEvents.notifyMove(PointerMoveEvent(1.0, Pointer(PointerId(2), Coordinates(70.0, 90.0))))
    assertThat(gestureState).isEqualTo(GestureState.Changed)

    pointerEvents.notifyCancel(PointerCancelEvent(1.0, Pointer(PointerId(2), Coordinates(70.0, 80.0))))
    assertThat(gestureState).isEqualTo(GestureState.Cancelled)
  }

  @Test
  fun testPinchZoomIn() {
    val pointerEvents = DefaultPointerEventBroker()
    val pinchHandler = PinchGestureSupport(pointerEvents)
    var scaleX = 1.0
    var scaleY = 1.0
    pinchHandler.onPinched {
      scaleX = it.zoom.scaleX
      scaleY = it.zoom.scaleY
    }

    val epsilon = Offset.offset(0.0001)

    pointerEvents.notifyDown(PointerDownEvent(1.0, Pointer(PointerId(1), Coordinates(10.0, 10.0))))
    pointerEvents.notifyDown(PointerDownEvent(1.0, Pointer(PointerId(2), Coordinates(20.0, 20.0))))
    assertThat(scaleX).isEqualTo(1.0)
    // no callback so far
    assertThat(scaleY).isEqualTo(1.0)
    // no callback so far

    // move both pointer the same distance right and down
    pointerEvents.notifyMove(PointerMoveEvent(1.0, Pointer(PointerId(1), Coordinates(40.0, 40.0))))
    pointerEvents.notifyMove(PointerMoveEvent(1.0, Pointer(PointerId(2), Coordinates(50.0, 50.0))))
    assertThat(scaleX).isEqualTo(1.0)
    // same distance along x-axis
    assertThat(scaleY).isEqualTo(1.0)
    // same distance along y-axis

    // zoom in
    pointerEvents.notifyMove(PointerMoveEvent(1.0, Pointer(PointerId(2), Coordinates(60.0, 70.0))))
    assertThat(scaleX).isEqualTo(2.0)
    assertThat(scaleY).isEqualTo(3.0)
  }

  @Test
  fun testPinchZoomOut() {
    val pointerEvents = DefaultPointerEventBroker()
    val pinchHandler = PinchGestureSupport(pointerEvents)
    var scaleX = 1.0
    var scaleY = 1.0
    pinchHandler.onPinched {
      scaleX = it.zoom.scaleX
      scaleY = it.zoom.scaleY
    }

    val epsilon = Offset.offset(0.0001)

    pointerEvents.notifyDown(PointerDownEvent(1.0, Pointer(PointerId(1), Coordinates(120.0, 120.0))))
    pointerEvents.notifyDown(PointerDownEvent(1.0, Pointer(PointerId(2), Coordinates(60.0, 60.0))))
    assertThat(scaleX).isEqualTo(1.0)
    // no callback so far
    assertThat(scaleY).isEqualTo(1.0)
    // no callback so far

    // move both pointer the same distance left and up
    pointerEvents.notifyMove(PointerMoveEvent(1.0, Pointer(PointerId(1), Coordinates(80.0, 80.0))))
    pointerEvents.notifyMove(PointerMoveEvent(1.0, Pointer(PointerId(2), Coordinates(20.0, 20.0))))
    assertThat(scaleX).isEqualTo(1.0)
    // same distance along x-axis
    assertThat(scaleY).isEqualTo(1.0)
    // same distance along y-axis

    // zoom out
    pointerEvents.notifyMove(PointerMoveEvent(1.0, Pointer(PointerId(2), Coordinates(40.0, 50.0))))
    assertThat(scaleX).isEqualTo(2.0 / 3.0)
    assertThat(scaleY).isEqualTo(0.5)
  }
}


