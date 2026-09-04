/*
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

import assertk.*
import assertk.assertions.*
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.MockCanvas
import com.meistercharts.canvas.events.CanvasMouseEventHandler
import com.meistercharts.events.EventConsumption
import it.neckar.events.MouseButton
import it.neckar.events.MouseDownEvent
import it.neckar.events.MouseMoveEvent
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Distance
import org.junit.jupiter.api.Test

class CanvasDragSupportTest {

  private val chartSupport: ChartSupport = ChartSupport(MockCanvas())

  private val press: Coordinates = Coordinates.of(100.0, 100.0)

  private val handler: RecordingHandler = RecordingHandler(draggingAllowed = true)

  private val dragSupport: CanvasDragSupport = CanvasDragSupport().also { it.handle(handler) }

  private val mouseEventHandler: CanvasMouseEventHandler = dragSupport.connectedMouseEventHandler()

  @Test
  fun `the total distance is measured from the press, not from the previous event`() {
    assertThat(dragSupport.prepareForDragging(press, 0.0, chartSupport)).isEqualTo(EventConsumption.Consumed)
    assertThat(dragSupport.dragging(press.plus(10.0, 10.0), 16.0, chartSupport)).isEqualTo(EventConsumption.Consumed)

    assertThat(dragSupport.totalDistanceTo(press.plus(25.0, 40.0))).isEqualTo(Distance.of(25.0, 40.0))
  }

  @Test
  fun `a finished gesture leaves no start behind`() {
    assertThat(dragSupport.prepareForDragging(press, 0.0, chartSupport)).isEqualTo(EventConsumption.Consumed)
    assertThat(dragSupport.gestureStartLocation).isEqualTo(press)

    assertThat(dragSupport.finishDragging(press.plus(10.0, 10.0), chartSupport)).isEqualTo(EventConsumption.Consumed)
    assertThat(dragSupport.gestureStartLocation).isNull()
  }

  @Test
  fun `a rejected press starts no gesture`() {
    val rejecting = CanvasDragSupport().also { it.handle(RecordingHandler(draggingAllowed = false)) }

    assertThat(rejecting.prepareForDragging(press, 0.0, chartSupport)).isEqualTo(EventConsumption.Ignored)
    assertThat(rejecting.gestureStartLocation).isNull()
  }

  /**
   * The gesture start is what lets a handler answer with the total distance instead of the delta since the last event.
   */
  @Test
  fun `the start is available while the press is still being answered`() {
    assertThat(dragSupport.prepareForDragging(press, 0.0, chartSupport)).isEqualTo(EventConsumption.Consumed)

    assertThat(handler.startSeenOnPress).isEqualTo(press)
  }

  @Test
  fun `a move over the canvas ends a running gesture`() {
    pressPrimaryButton()

    assertThat(mouseEventHandler.onMove(MouseMoveEvent(16.0, press.plus(10.0, 10.0)), chartSupport)).isEqualTo(EventConsumption.Ignored)

    assertThat(handler.finishCount).isEqualTo(1)
    assertThat(dragSupport.gestureStartLocation).isNull()
  }

  @Test
  fun `leaving the canvas keeps a running gesture`() {
    pressPrimaryButton()

    assertThat(mouseEventHandler.onMove(MouseMoveEvent(16.0, null), chartSupport)).isEqualTo(EventConsumption.Ignored)

    assertThat(handler.finishCount).isEqualTo(0)
    assertThat(dragSupport.gestureStartLocation).isEqualTo(press)
  }

  @Test
  fun `a move without a gesture ends nothing`() {
    assertThat(mouseEventHandler.onMove(MouseMoveEvent(16.0, press), chartSupport)).isEqualTo(EventConsumption.Ignored)

    assertThat(handler.finishCount).isEqualTo(0)
  }

  private fun pressPrimaryButton() {
    assertThat(mouseEventHandler.onDown(MouseDownEvent(0.0, press, MouseButton.Primary), chartSupport)).isEqualTo(EventConsumption.Consumed)
  }

  private class RecordingHandler(val draggingAllowed: Boolean) : CanvasDragSupport.Handler {
    var startSeenOnPress: Coordinates? = null
    var finishCount: Int = 0

    override fun isDraggingAllowedFromHere(source: CanvasDragSupport, location: Coordinates, chartSupport: ChartSupport): Boolean {
      startSeenOnPress = source.gestureStartLocation
      return draggingAllowed
    }

    override fun onDrag(source: CanvasDragSupport, location: Coordinates, distance: Distance, deltaTime: Double, chartSupport: ChartSupport): EventConsumption {
      return EventConsumption.Consumed
    }

    override fun onFinish(source: CanvasDragSupport, location: Coordinates, chartSupport: ChartSupport): EventConsumption {
      finishCount++
      return EventConsumption.Consumed
    }
  }
}
