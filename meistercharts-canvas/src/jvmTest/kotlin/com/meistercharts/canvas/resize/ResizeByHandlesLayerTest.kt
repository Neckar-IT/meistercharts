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
import com.meistercharts.canvas.MouseCursor
import com.meistercharts.canvas.mock.MockLayerPaintingContext
import com.meistercharts.canvas.resizeHandlesSupport
import com.meistercharts.events.EventConsumption
import it.neckar.events.MouseButton
import it.neckar.events.MouseDownEvent
import it.neckar.events.MouseDragEvent
import it.neckar.events.MouseMoveEvent
import it.neckar.events.MouseUpEvent
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

  private val contentBounds: Rectangle = Rectangle(100.0, 100.0, 200.0, 200.0)

  @BeforeEach
  fun setUp() {
    paintingContext.chartSupport.resizeHandlesSupport.onResize(layer, handler)
    registerResizable(paintingContext)

    //The first layout initializes the layer - calling initialize as well registers its observers twice
    layer.layout(paintingContext)
  }

  /**
   * A layer that stops laying out at all never reaches its [ResizeHandlesSupport.clear] - the registration has to
   * expire on its own.
   */
  @Test
  fun `a registration counts for its own paint loop only`() {
    assertThat(layoutInNextLoop().loopIndex).isNotEqualTo(paintingContext.loopIndex)

    assertThat(down(bottomRightHandle)).isEqualTo(EventConsumption.Ignored)
    assertThat(layer.uiState).isEqualTo(DefaultState)
  }

  @Test
  fun `a resizable registered again in the next paint loop keeps its handles`() {
    val nextLoop: LayerPaintingContext = layoutInNextLoop { registerResizable(it) }

    assertThat(nextLoop.loopIndex).isNotEqualTo(paintingContext.loopIndex)
    press(bottomRightHandle)
    dragBy(bottomRightHandle, 5.0, 5.0)

    assertThat(handler.distances.last()).isEqualTo(Distance.of(5.0, 5.0))
  }

  /**
   * Lays the layer out in a fresh paint loop, after whatever [beforeLayout] registers in it.
   */
  private fun layoutInNextLoop(beforeLayout: (LayerPaintingContext) -> Unit = {}): LayerPaintingContext {
    val nextLoop: LayerPaintingContext = paintingContext.copy(loopIndex = paintingContext.loopIndex.next())
    beforeLayout(nextLoop)
    layer.layout(nextLoop)
    return nextLoop
  }

  private fun registerResizable(paintingContext: LayerPaintingContext) {
    paintingContext.chartSupport.resizeHandlesSupport.setResizable(layer, contentBounds, paintingContext.loopIndex)
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

  @Test
  fun `a release ends the gesture`() {
    press(bottomRightHandle)
    dragBy(bottomRightHandle, 10.0, 10.0)
    release(bottomRightHandle, 10.0, 10.0)

    assertThat(handler.resizingFinishedCount).isEqualTo(1)
    assertThat(layer.uiState).isEqualTo(DefaultState)
  }

  @Test
  fun `a release over a handle leaves that handle armed`() {
    press(bottomRightHandle)
    dragBy(bottomRightHandle, 4.0, 4.0)
    release(bottomRightHandle, 0.0, 0.0)

    assertThat(layer.uiState).isEqualTo(HoveringOverHandle(Direction.BottomRight))
    assertThat(handler.armedDirections).containsExactly(Direction.BottomRight)
  }

  /**
   * A press on another handle hands the gesture over: one finish, two begins, and the distance measured from the new
   * handle.
   */
  @Test
  fun `a press ends a gesture whose release never arrived`() {
    press(bottomRightHandle)
    dragBy(bottomRightHandle, 10.0, 10.0)

    press(topLeftHandle)
    dragBy(topLeftHandle, 5.0, 5.0)

    assertThat(handler.resizingFinishedCount).isEqualTo(1)
    assertThat(handler.begunDirections).containsExactly(Direction.BottomRight, Direction.TopLeft)
    assertThat(handler.distances.last()).isEqualTo(Distance.of(5.0, 5.0))
  }

  /**
   * A second gesture from the same handle assigns an equal state, so only the [DefaultState] in between tells the two
   * apart.
   */
  @Test
  fun `a press on the same handle ends the gesture before it begins the next`() {
    press(bottomRightHandle)
    dragBy(bottomRightHandle, 10.0, 10.0)

    press(bottomRightHandle)

    assertThat(handler.resizingFinishedCount).isEqualTo(1)
    assertThat(handler.begunDirections).containsExactly(Direction.BottomRight, Direction.BottomRight)
  }

  /**
   * The fallback for an event stream that delivers a press without a move before it - the move is what usually ends a
   * gesture whose release happened outside the canvas.
   */
  @Test
  fun `a press away from every handle ends a gesture whose release never arrived`() {
    press(bottomRightHandle)
    dragBy(bottomRightHandle, 10.0, 10.0)

    pressAwayFromEveryHandle()

    assertThat(handler.resizingFinishedCount).isEqualTo(1)
    assertThat(layer.uiState).isEqualTo(DefaultState)
  }

  /**
   * Deleting the resized element clears the resizable while the pointer is still down.
   */
  @Test
  fun `the gesture ends when its resizable is gone`() {
    press(bottomRightHandle)
    dragBy(bottomRightHandle, 10.0, 10.0)

    paintingContext.chartSupport.resizeHandlesSupport.clear(layer)
    layer.layout(paintingContext)

    assertThat(handler.resizingFinishedCount).isEqualTo(1)
    assertThat(layer.uiState).isEqualTo(DefaultState)
  }

  @Test
  fun `the hover ends with the handles it names`() {
    moveTo(bottomRightHandle)
    assertThat(handler.armedDirections).containsExactly(Direction.BottomRight)

    paintingContext.chartSupport.resizeHandlesSupport.clear(layer)
    layer.layout(paintingContext)

    assertThat(handler.disarmedCount).isEqualTo(1)
    assertThat(layer.uiState).isEqualTo(DefaultState)
  }

  @Test
  fun `leaving the canvas ends the hover`() {
    moveTo(bottomRightHandle)

    leaveCanvas()

    assertThat(handler.disarmedCount).isEqualTo(1)
    assertThat(layer.uiState).isEqualTo(DefaultState)
  }

  /**
   * The gesture has to survive the pointer leaving the canvas - re-entering with the button still down arrives as a
   * drag, and it picks the gesture up where it left off.
   */
  @Test
  fun `leaving the canvas during a gesture keeps it`() {
    press(bottomRightHandle)

    leaveCanvas()
    dragBy(bottomRightHandle, 20.0, 20.0)

    assertThat(handler.resizingFinishedCount).isEqualTo(0)
    assertThat(handler.directions.last()).isEqualTo(Direction.BottomRight)
    assertThat(handler.distances.last()).isEqualTo(Distance.of(20.0, 20.0))
  }

  /**
   * A release outside the canvas never arrives, so the move that follows it is the first event that reports it - it
   * carries no pressed button, and there is no other way to learn that the gesture is over.
   */
  @Test
  fun `a move after a release outside the canvas ends the gesture`() {
    press(bottomRightHandle)
    dragBy(bottomRightHandle, 10.0, 10.0)
    leaveCanvas()

    moveTo(bottomRightHandle)

    assertThat(handler.resizingFinishedCount).isEqualTo(1)
    assertThat(layer.uiState).isEqualTo(HoveringOverHandle(Direction.BottomRight))
    assertThat(mouseCursor).isEqualTo(MouseCursor.ResizeSouthEast)

    //The gesture end and the move that reports it both assign the state - the handle is armed once, not twice
    assertThat(handler.armedDirections).containsExactly(Direction.BottomRight)
  }

  /**
   * The resize cursor outlives its gesture wherever the gesture outlives its release - it is the symbol of the handle
   * under the pointer, and the pointer is above none.
   */
  @Test
  fun `a move away from every handle after a release outside the canvas clears the resize cursor`() {
    press(bottomRightHandle)
    assertThat(mouseCursor).isEqualTo(MouseCursor.ResizeSouthEast)
    leaveCanvas()

    moveAwayFromEveryHandle()

    assertThat(handler.resizingFinishedCount).isEqualTo(1)
    assertThat(layer.uiState).isEqualTo(DefaultState)
    assertThat(mouseCursor).isEqualTo(MouseCursor.Default)
  }

  @Test
  fun `a drag event that belongs to no gesture is ignored`() {
    press(bottomRightHandle)
    paintingContext.chartSupport.resizeHandlesSupport.clear(layer)
    layer.layout(paintingContext)

    assertThat(dragTo(bottomRightHandle.plus(20.0, 20.0))).isEqualTo(EventConsumption.Ignored)
    assertThat(handler.distances).isEmpty()
  }

  /**
   * The layers below share the press, so a release this layer has nothing to end has to reach them.
   */
  @Test
  fun `a release that belongs to no gesture is left to the layers below`() {
    press(bottomRightHandle)
    paintingContext.chartSupport.resizeHandlesSupport.clear(layer)
    layer.layout(paintingContext)
    assertThat(handler.resizingFinishedCount).isEqualTo(1)

    assertThat(up(bottomRightHandle)).isEqualTo(EventConsumption.Ignored)

    assertThat(handler.resizingFinishedCount).isEqualTo(1)
  }

  private val mouseCursor: MouseCursor
    get() = paintingContext.chartSupport.canvas.mouseCursor.value

  /**
   * The event time only feeds the drag speed calculator, but it has to grow like a real event stream does.
   */
  private var eventTime: @ms Double = 0.0

  private fun press(handle: Coordinates) {
    eventTime += 16.0
    assertThat(down(handle)).isEqualTo(EventConsumption.Consumed)
  }

  private fun pressAwayFromEveryHandle() {
    eventTime += 16.0
    assertThat(down(AwayFromEveryHandle)).isEqualTo(EventConsumption.Ignored)
  }

  private fun down(location: Coordinates): EventConsumption {
    return layer.mouseEventHandler.onDown(MouseDownEvent(eventTime, location, MouseButton.Primary), paintingContext.chartSupport)
  }

  private fun moveTo(handle: Coordinates) {
    assertThat(move(handle)).isEqualTo(EventConsumption.Consumed)
  }

  private fun moveAwayFromEveryHandle() {
    assertThat(move(AwayFromEveryHandle)).isEqualTo(EventConsumption.Ignored)
  }

  /**
   * Leaving the canvas arrives as a move without coordinates - above no handle, so nothing consumes it.
   */
  private fun leaveCanvas() {
    assertThat(move(null)).isEqualTo(EventConsumption.Ignored)
  }

  private fun move(location: Coordinates?): EventConsumption {
    eventTime += 16.0
    return layer.mouseEventHandler.onMove(MouseMoveEvent(eventTime, location), paintingContext.chartSupport)
  }

  private fun dragBy(handle: Coordinates, deltaX: Double, deltaY: Double) {
    assertThat(dragTo(handle.plus(deltaX, deltaY))).isEqualTo(EventConsumption.Consumed)
  }

  private fun dragTo(location: Coordinates): EventConsumption {
    eventTime += 16.0
    return layer.mouseEventHandler.onDrag(MouseDragEvent(eventTime, location, MouseButton.Primary), paintingContext.chartSupport)
  }

  private fun release(handle: Coordinates, deltaX: Double, deltaY: Double) {
    assertThat(up(handle.plus(deltaX, deltaY))).isEqualTo(EventConsumption.Consumed)
  }

  private fun up(location: Coordinates): EventConsumption {
    eventTime += 16.0
    return layer.mouseEventHandler.onUp(MouseUpEvent(eventTime, location, MouseButton.Primary), paintingContext.chartSupport)
  }

  private class RecordingResizeHandler : ResizeHandler {
    val distances: MutableList<Distance> = mutableListOf()
    val directions: MutableList<Direction> = mutableListOf()

    /**
     * The per-axis totals, as the pair the handler receives them in.
     */
    val totals: MutableList<Coordinates> = mutableListOf()

    val begunDirections: MutableList<Direction> = mutableListOf()
    val armedDirections: MutableList<Direction> = mutableListOf()
    var disarmedCount: Int = 0
    var resizingFinishedCount: Int = 0

    override fun armed(handleDirection: Direction) {
      armedDirections.add(handleDirection)
    }

    override fun disarmed() {
      disarmedCount++
    }

    override fun beginResizing(handleDirection: Direction) {
      begunDirections.add(handleDirection)
    }

    override fun resizing(totalDistance: Distance, handleDirection: Direction, totalX: Double, totalY: Double) {
      distances.add(totalDistance)
      directions.add(handleDirection)
      totals.add(Coordinates.of(totalX, totalY))
    }

    override fun resizingFinished() {
      resizingFinishedCount++
    }
  }

  private companion object {
    /**
     * Inside the resizable content, far enough from every edge that no handle covers it.
     */
    val AwayFromEveryHandle: Coordinates = Coordinates.of(200.0, 200.0)
  }
}
