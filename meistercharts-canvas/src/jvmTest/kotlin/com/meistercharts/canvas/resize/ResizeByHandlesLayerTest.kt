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
package com.meistercharts.canvas.resize

import assertk.*
import assertk.assertions.*
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.canvas.mock.MockLayerPaintingContext
import com.meistercharts.canvas.resizeHandlesSupport
import com.meistercharts.events.EventConsumption
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Direction
import it.neckar.geometry.Distance
import it.neckar.geometry.Rectangle
import it.neckar.open.unit.si.ms
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ResizeByHandlesLayerTest {

  private val paintingContext: LayerPaintingContext = MockLayerPaintingContext()

  private val layer: ResizeByHandlesLayer = ResizeByHandlesLayer()

  private val handler: RecordingResizeHandler = RecordingResizeHandler()

  /**
   * The content is 200 x 200 at 100/100, so its handles sit on those edges.
   */
  private val bottomRightHandle: Coordinates = Coordinates.of(300.0, 300.0)
  private val topLeftHandle: Coordinates = Coordinates.of(100.0, 100.0)
  private val topCenterHandle: Coordinates = Coordinates.of(200.0, 100.0)

  @BeforeEach
  fun setUp() {
    val chartSupport = paintingContext.chartSupport
    chartSupport.resizeHandlesSupport.onResize(layer, handler)
    chartSupport.resizeHandlesSupport.setResizable(layer, Rectangle(100.0, 100.0, 200.0, 200.0))

    layer.initialize(paintingContext)
    layer.layout(paintingContext)
  }

  @Test
  fun `every event carries the distance since the press, not the one since the last event`() {
    press(bottomRightHandle)
    dragBy(bottomRightHandle, 10.0, 5.0)
    dragBy(bottomRightHandle, 25.0, 15.0)

    assertThat(handler.distances).containsExactly(Distance.of(10.0, 5.0), Distance.of(25.0, 15.0))
  }

  @Test
  fun `leading the pointer back reports the distance that is left, not the step back`() {
    press(bottomRightHandle)
    dragBy(bottomRightHandle, 400.0, 0.0)
    dragBy(bottomRightHandle, 30.0, 0.0)

    assertThat(handler.distances.last()).isEqualTo(Distance.of(30.0, 0.0))
  }

  @Test
  fun `a second gesture measures from its own handle, not from the one before it`() {
    press(bottomRightHandle)
    dragBy(bottomRightHandle, 40.0, 40.0)
    release(bottomRightHandle, 40.0, 40.0)

    layer.layout(paintingContext)
    press(topLeftHandle)
    dragBy(topLeftHandle, 5.0, 5.0)

    assertThat(handler.distances.last()).isEqualTo(Distance.of(5.0, 5.0))
    assertThat(handler.directions.last()).isEqualTo(Direction.TopLeft)
  }

  @Test
  fun `a center handle reports no distance on the axis it does not move`() {
    press(topCenterHandle)
    dragBy(topCenterHandle, 50.0, 30.0)

    assertThat(handler.totals.last()).isEqualTo(Coordinates.of(0.0, 30.0))
    assertThat(handler.distances.last()).isEqualTo(Distance.of(50.0, 30.0))
  }

  /**
   * The event time only feeds the drag speed calculator, but it has to grow like a real event stream does.
   */
  private var eventTime: @ms Double = 0.0

  private fun press(handle: Coordinates) {
    eventTime += 16.0
    assertThat(layer.dragSupport.prepareForDragging(handle, eventTime, paintingContext.chartSupport)).isEqualTo(EventConsumption.Consumed)
  }

  private fun dragBy(handle: Coordinates, deltaX: Double, deltaY: Double) {
    eventTime += 16.0
    assertThat(layer.dragSupport.dragging(handle.plus(deltaX, deltaY), eventTime, paintingContext.chartSupport)).isEqualTo(EventConsumption.Consumed)
  }

  private fun release(handle: Coordinates, deltaX: Double, deltaY: Double) {
    assertThat(layer.dragSupport.finishDragging(handle.plus(deltaX, deltaY), paintingContext.chartSupport)).isEqualTo(EventConsumption.Consumed)
  }

  private class RecordingResizeHandler : ResizeHandler {
    val distances: MutableList<Distance> = mutableListOf()
    val directions: MutableList<Direction> = mutableListOf()

    /**
     * The per-axis totals, as the pair the handler receives them in.
     */
    val totals: MutableList<Coordinates> = mutableListOf()

    override fun resizing(totalDistance: Distance, handleDirection: Direction, totalX: Double, totalY: Double) {
      distances.add(totalDistance)
      directions.add(handleDirection)
      totals.add(Coordinates.of(totalX, totalY))
    }
  }
}
