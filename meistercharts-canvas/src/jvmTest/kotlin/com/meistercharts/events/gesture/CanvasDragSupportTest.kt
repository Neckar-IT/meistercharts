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
import com.meistercharts.events.EventConsumption
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Distance
import org.junit.jupiter.api.Test

/**
 * The gesture start is what lets a handler answer with the total distance of a gesture instead of the delta since the
 * last event.
 */
class CanvasDragSupportTest {

  private val chartSupport: ChartSupport = ChartSupport(MockCanvas())

  private val press: Coordinates = Coordinates.of(100.0, 100.0)

  @Test
  fun `the total distance is measured from the press, not from the previous event`() {
    val dragSupport = allowingDragSupport()

    assertThat(dragSupport.prepareForDragging(press, 0.0, chartSupport)).isEqualTo(EventConsumption.Consumed)
    assertThat(dragSupport.dragging(press.plus(10.0, 10.0), 16.0, chartSupport)).isEqualTo(EventConsumption.Consumed)

    assertThat(dragSupport.totalDistanceTo(press.plus(25.0, 40.0))).isEqualTo(Distance.of(25.0, 40.0))
  }

  @Test
  fun `a finished gesture leaves no start behind`() {
    val dragSupport = allowingDragSupport()

    assertThat(dragSupport.prepareForDragging(press, 0.0, chartSupport)).isEqualTo(EventConsumption.Consumed)
    assertThat(dragSupport.gestureStartLocation).isEqualTo(press)

    assertThat(dragSupport.finishDragging(press.plus(10.0, 10.0), chartSupport)).isEqualTo(EventConsumption.Consumed)
    assertThat(dragSupport.gestureStartLocation).isNull()
  }

  @Test
  fun `a rejected press starts no gesture`() {
    val dragSupport = CanvasDragSupport().also { it.handle(RecordingHandler(draggingAllowed = false)) }

    assertThat(dragSupport.prepareForDragging(press, 0.0, chartSupport)).isEqualTo(EventConsumption.Ignored)
    assertThat(dragSupport.gestureStartLocation).isNull()
  }

  @Test
  fun `the start is available while the press is still being answered`() {
    val handler = RecordingHandler(draggingAllowed = true)
    val dragSupport = CanvasDragSupport().also { it.handle(handler) }

    assertThat(dragSupport.prepareForDragging(press, 0.0, chartSupport)).isEqualTo(EventConsumption.Consumed)

    assertThat(handler.startSeenOnPress).isEqualTo(press)
  }

  private fun allowingDragSupport(): CanvasDragSupport {
    return CanvasDragSupport().also { it.handle(RecordingHandler(draggingAllowed = true)) }
  }

  private class RecordingHandler(val draggingAllowed: Boolean) : CanvasDragSupport.Handler {
    var startSeenOnPress: Coordinates? = null

    override fun isDraggingAllowedFromHere(source: CanvasDragSupport, location: Coordinates, chartSupport: ChartSupport): Boolean {
      startSeenOnPress = source.gestureStartLocation
      return draggingAllowed
    }

    override fun onDrag(source: CanvasDragSupport, location: Coordinates, distance: Distance, deltaTime: Double, chartSupport: ChartSupport): EventConsumption {
      return EventConsumption.Consumed
    }
  }
}
