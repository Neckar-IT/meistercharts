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
package com.meistercharts.canvas.slider

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.canvas.MouseCursor
import com.meistercharts.canvas.events.CanvasMouseEventHandler
import com.meistercharts.canvas.fillRoundedRect
import com.meistercharts.canvas.mouseCursorSupport
import com.meistercharts.canvas.paintLocation
import com.meistercharts.canvas.paintable.AbstractPaintable
import com.meistercharts.canvas.paintable.AbstractPaintablePaintingVariables
import com.meistercharts.canvas.paintable.Paintable2
import com.meistercharts.canvas.paintable.PaintablePaintingVariables
import com.meistercharts.color.Color
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.MouseDownEvent
import com.meistercharts.events.MouseDragEvent
import com.meistercharts.events.MouseMoveEvent
import com.meistercharts.events.MouseUpEvent
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.geometry.Coordinates
import com.meistercharts.geometry.Rectangle
import com.meistercharts.model.Direction
import it.neckar.open.kotlin.lang.abs
import it.neckar.open.kotlin.lang.setIfDifferent
import it.neckar.open.provider.DoubleProvider
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.fastForEachIndexed
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.other.px
import kotlin.math.max

/**
 * Base class for all sliders.
 *
 * Bounding box: Origin is placed at the left center of the slider
 *
 */
class Slider(
  /**
   * The configuration of the slider
   */
  val configuration: Configuration,

  /**
   * Additional configuration of the slider configuration
   */
  val additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractPaintable() {

  init {
    configuration.additionalConfiguration()
  }

  private val paintingVariables = object : AbstractPaintablePaintingVariables() {

    /**
     * Relative to the bounding box origin
     */
    var sliderLocationX: @Zoomed Double = Double.NaN

    /**
     * The width of the slider
     */
    var width: @Zoomed Double = Double.NaN

    override fun calculate(paintingContext: LayerPaintingContext) {
      super.calculate(paintingContext)

      configuration.handlePaintable.updateState(state.state)
      val handleBoundingBox = configuration.handlePaintable.layout(paintingContext)

      width = configuration.width()
      sliderLocationX = width * configuration.handlePosition()


      val top = max(configuration.sliderAreaHeight / 2.0, handleBoundingBox.top)
      val bottom = max(configuration.sliderAreaHeight / 2.0, handleBoundingBox.bottom)

      boundingBox = Rectangle.withLTRB(left = 0.0, top = top, right = width, bottom = bottom)
    }

    override fun reset() {
      super.reset()
      sliderLocationX = Double.NaN
      width = Double.NaN
    }
  }

  enum class State {
    Default,
    MouseOverHandle,
    Dragging,
  }

  override fun paintingVariables(): PaintablePaintingVariables {
    return paintingVariables
  }

  val currentState: State
    get() = state.state

  /**
   * Contains the state of the slider
   */
  private val state = object {
    /**
     * The location of the mouse down event
     */
    var mouseLocationOnMouseDown: Coordinates? = null

    /**
     * The slider position when dragging the slider started
     */
    var handlePositionOnMouseDown: @Zoomed Double = Double.NaN

    /**
     * The current state of the slider
     */
    var state: State = State.Default

    /**
     * The last painted x location
     */
    var lastX: @Zoomed Double = Double.NaN

    /**
     * The last painted y location
     */
    var lastY: @Zoomed Double = Double.NaN
  }

  val mouseEventHandler: CanvasMouseEventHandler = object : CanvasMouseEventHandler {
    override fun onDown(event: MouseDownEvent, chartSupport: ChartSupport): EventConsumption {
      when {
        onHandle(event.coordinates) -> {
          state.mouseLocationOnMouseDown = event.coordinates
          state.handlePositionOnMouseDown = configuration.handlePosition()

          state.state = State.Dragging
          chartSupport.markAsDirty(DirtyReason.UserInteraction)
          return EventConsumption.Consumed
        }

        onSlider(event.coordinates) -> {
          state.mouseLocationOnMouseDown = event.coordinates
          state.handlePositionOnMouseDown = configuration.handlePosition()

          return EventConsumption.Consumed
        }

        else -> return super.onDown(event, chartSupport)
      }
    }

    override fun onUp(event: MouseUpEvent, chartSupport: ChartSupport): EventConsumption {
      if (state.state == State.Dragging) {
        updateSliderPositionFromMouseEvent(event.x)

        updateStateWhileMouseUp(chartSupport, event.coordinates)
        return EventConsumption.Consumed
      } else {
        //No dragging, possibly a click
        val distanceFromDown = state.mouseLocationOnMouseDown?.distanceTo(event.coordinates) ?: Double.MAX_VALUE

        if (distanceFromDown < 20) {
          //set the slider position directly
          val deltaFromLastX = event.x - state.lastX
          @pct val newSliderLocation = 1 / paintingVariables.width * deltaFromLastX
          configuration.handlePositionChanged(newSliderLocation.coerceIn(0.0, 1.0))

          chartSupport.markAsDirty(DirtyReason.UserInteraction)
          return EventConsumption.Consumed
        }
      }

      //Reset the down location
      state.mouseLocationOnMouseDown = null
      state.handlePositionOnMouseDown = Double.NaN

      return super.onUp(event, chartSupport)
    }

    override fun onMove(event: MouseMoveEvent, chartSupport: ChartSupport): EventConsumption {
      updateStateWhileMouseUp(chartSupport, event.coordinates)
      return super.onMove(event, chartSupport)
    }

    override fun onDrag(event: MouseDragEvent, chartSupport: ChartSupport): EventConsumption {
      if (state.state == State.Dragging) {
        updateSliderPositionFromMouseEvent(event.x)
        chartSupport.markAsDirty(DirtyReason.UserInteraction)
      }

      return super.onDrag(event, chartSupport)
    }

    fun updateSliderPositionFromMouseEvent(x: @MayBeNaN Double) {
      @pct val newSliderLocation = state.handlePositionOnMouseDown + 1.0 / paintingVariables.width * (x - (state.mouseLocationOnMouseDown?.x ?: Double.NaN))
      configuration.handlePositionChanged(newSliderLocation.coerceIn(0.0, 1.0))
    }

    /**
     * Updates the state while mouse button is up
     */
    private fun updateStateWhileMouseUp(chartSupport: ChartSupport, coordinates: Coordinates?): EventConsumption {
      if (onHandle(coordinates)) {
        state::state.setIfDifferent(State.MouseOverHandle) {
          chartSupport.cursor().value = MouseCursor.Hand

          chartSupport.markAsDirty(DirtyReason.UserInteraction)
          return EventConsumption.Consumed
        }
      } else {
        state::state.setIfDifferent(State.Default) {
          chartSupport.cursor().value = null

          chartSupport.markAsDirty(DirtyReason.UserInteraction)
          return EventConsumption.Consumed
        }
      }

      return EventConsumption.Ignored
    }

  }

  private fun ChartSupport.cursor() = mouseCursorSupport.cursorProperty(this@Slider)

  private fun onHandle(mouseCoordinates: Coordinates?): Boolean {
    if (mouseCoordinates == null) {
      return false
    }

    @Zoomed val relativeX = mouseCoordinates.x - state.lastX - paintingVariables.sliderLocationX
    @Zoomed val relativeY = mouseCoordinates.y - state.lastY

    val handleBoundingBox = configuration.handlePaintable.paintingVariables().boundingBox

    return handleBoundingBox.contains(relativeX, relativeY)
  }

  /**
   * Returns true if this slider is on the slider (*not* the handle)
   */
  private fun onSlider(mouseCoordinates: Coordinates?): Boolean {
    if (mouseCoordinates == null) {
      return false
    }

    @Zoomed val relativeY = mouseCoordinates.y - state.lastY

    if (mouseCoordinates.x < state.lastX) {
      return false
    }
    if (mouseCoordinates.x > state.lastX + paintingVariables.width) {
      return false
    }

    @Zoomed val distanceFromSliderCenterY = relativeY.abs()
    return distanceFromSliderCenterY <= configuration.sliderAreaHeight / 2.0
  }

  override fun paintAfterLayout(paintingContext: LayerPaintingContext, x: @Zoomed Double, y: @Zoomed Double) {
    val gc = paintingContext.gc

    paintingVariables.let {
      state.lastX = gc.translationX + x
      state.lastY = gc.translationY + y
    }

    @px val height = configuration.sliderAreaHeight

    // Center the slider around the provided y value.
    gc.translate(x, y + height / 2.0) //Translate to the left center of the slider area

    paintingContext.ifDebug(DebugFeature.ShowAnchors) {
      gc.paintLocation(label = "origin", color = Color.cadetblue)
    }

    paintingContext.ifDebug(DebugFeature.ShowBounds) {
      gc.stroke(Color.blue)
      gc.strokeRect(paintingVariables.boundingBox)
    }

    //Paint the slider area
    gc.fill(configuration.areaFill)
    gc.fillRoundedRect(0.0, -height / 2.0, paintingVariables.width, height, height / 2.0)

    //Paint the ticks
    configuration.tickLocations.fastForEachIndexed { index, tickLocation: @pct Double ->
      @Zoomed val tickX = tickLocation * paintingVariables.width
      gc.stroke(configuration.tickColor())

      @px val tickLowerY = -height / 2.0
      val tickUpperY = height / 2.0
      gc.strokeLine(tickX, tickLowerY, tickX, tickUpperY)

      gc.fill(configuration.areaFill)
      gc.fillOvalCenter(tickX, 0.0, configuration.tickLength, configuration.tickLength)

      val tickLabel = configuration.ticksLabels.valueAt(index)
      if (tickLabel != null) {
        gc.fill(configuration.tickLabelColor)
        gc.font(configuration.tickLabelFont)
        gc.fillText(tickLabel, tickX, tickUpperY, Direction.BottomCenter, 0.0, configuration.tickLabelGap)
      }
    }

    //Paint the handle
    configuration.handlePaintable.paint(paintingContext, paintingVariables.sliderLocationX, 0.0)
  }

  class Configuration(
    /**
     * Returns the width of the slider
     */
    var width: @Zoomed DoubleProvider,

    /**
     * Returns the (relative) handle position.
     * In percentage of the slider width.
     */
    val handlePosition: @pct DoubleProvider,

    /**
     * Is called on drag
     */
    val handlePositionChanged: (@pct Double) -> Unit,
  ) {

    /**
     * Provides the tick locations (in percent)
     */
    var tickLocations: @pct DoublesProvider = DoublesProvider.empty

    /**
     * The tick labels. [tickLocations] provides the [TickIndex]
     */
    var ticksLabels: @pct MultiProvider<TickIndex, String?> = MultiProvider.alwaysNull()

    /**
     * The height of the slider area (*not* the handle)
     */
    var sliderAreaHeight: @px Double = 3.0

    /**
     * The fill color of the slider area
     */
    var areaFill: Color = Color.silver

    /**
     * The paintable that is used to paint the handle
     */
    val handlePaintable: HandlePaintable = RoundHandlePaintable()

    /**
     * The length of the ticks
     */
    var tickLength: @px Double = 7.0

    /**
     * The color for the tick
     */
    var tickColor: () -> Color = { areaFill }

    /**
     * The gap between tick and label
     */
    var tickLabelGap: @px Double = 10.0

    /**
     * The font of the tick label
     */
    var tickLabelFont: FontDescriptorFragment = FontDescriptorFragment.S

    /**
     * The color of the tick label
     */
    var tickLabelColor: Color = Color.darkgray
  }

  /**
   * Paintable for handles
   */
  interface HandlePaintable : Paintable2 {
    /**
     * Updates the current state. Is called before layout/paint
     */
    fun updateState(state: State)
  }

  annotation class TickIndex
}
