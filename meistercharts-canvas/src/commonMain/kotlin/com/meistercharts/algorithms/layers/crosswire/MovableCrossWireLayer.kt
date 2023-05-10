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
package com.meistercharts.algorithms.layers.crosswire

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.painter.Path
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.WindowRelative
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.MouseCursor
import com.meistercharts.canvas.MouseCursorSupport
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.events.EventConsumption
import com.meistercharts.events.MouseEventBroker
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Distance
import com.meistercharts.model.Rectangle
import it.neckar.open.observable.clear
import it.neckar.open.unit.other.px

/**
 * Adds support for moving the cross wire
 */
class MovableCrossWireLayer(
  val mouseCursorSupport: MouseCursorSupport,
  val mouseEvents: MouseEventBroker,
  styleConfiguration: Style.() -> Unit = {},
) : AbstractLayer() {

  val style: Style = Style().also(styleConfiguration)

  override val type: LayerType
    get() = LayerType.Content

  /**
   * The cursor property for the cross wire
   */
  private val cursorProperty = mouseCursorSupport.cursorProperty(this).also {
    disposeSupport.onDispose {
      mouseCursorSupport.clearProperty(this)
    }
  }

  override fun initialize(paintingContext: LayerPaintingContext) {
    super.initialize(paintingContext)
    val layerSupport = paintingContext.layerSupport

    layerSupport.mouseEvents.onMove {
      updateCursorOnMouseMove(it.coordinates)
    }
  }

  /**
   * The last marker top translation
   */
  @px
  @Zoomed
  private var lastMarkerTopTranslation: Distance? = null

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    //Move to x center of wire, bottom of the window
    @Window @px val wireLocation = paintingContext.chartCalculator.windowRelative2WindowX(style.locationX())

    lastMarkerTopTranslation = Distance(wireLocation, gc.height - MarkerPath.totalHeight - style.paddingBottom)
      .also {
        gc.translate(it)
      }

    gc.fill(style.markerFill)
    gc.fill(MarkerPath.path)
  }

  /**
   * Check if the mouse is above one of hte labels
   */
  private fun updateCursorOnMouseMove(mousePosition: @Window Coordinates?): EventConsumption {
    //Enable later when dragging is implemented
    if (true) {
      return EventConsumption.Ignored
    }

    @px @Zoomed val translation = lastMarkerTopTranslation

    if (mousePosition == null || translation == null) {
      //Mouse outside if canvas, just clear
      cursorProperty.clear()
      return EventConsumption.Ignored
    }

    //Check if over path
    if (MarkerPath.approximationRect.contains(mousePosition.minus(translation))) {
      cursorProperty.value = MouseCursor.ResizeEast
    } else {
      cursorProperty.clear()
    }

    return EventConsumption.Ignored
  }

  /**
   * The style for the movable cross wire layer
   */
  @StyleDsl
  open class Style {
    /**
     * The location of the cross wire
     */
    var locationX: () -> @WindowRelative Double = { 0.75 }


    /**
     * The fill of the marker at the bottom
     */
    var markerFill: Color = Color.gray

    /**
     * The padding at the bottom
     */
    @px
    var paddingBottom: Double = 0.0
  }
}

/**
 * Wraps the given cross wire layer with mouse over cross wire layer
 */
fun CrossWireLayer.movable(
  mouseCursorSupport: MouseCursorSupport,
  mouseEvents: MouseEventBroker,
  styleConfiguration: MovableCrossWireLayer.Style.() -> Unit = {},
): MovableCrossWireLayer {
  return MovableCrossWireLayer(mouseCursorSupport, mouseEvents, styleConfiguration)
}

/**
 *
 */
private object MarkerPath {
  /**
   * The width of the marker
   */
  @px
  const val width = 20.0

  /**
   * The height of the base
   */
  @px
  const val baseHeight = 20.0

  /**
   * The height of the top (arrow)
   */
  @px
  const val topHeight = 10.0

  /**
   * The total height of the marker
   */
  @px
  const val totalHeight = baseHeight + topHeight

  /**
   * The marker path that is drawn
   */
  val path = Path().also {
    it.moveTo(-width / 2.0, totalHeight) //bottom left
    it.lineTo(-width / 2.0, topHeight) //left
    it.lineTo(00.0, 0.0) //top
    it.lineTo(width / 2.0, topHeight) //right
    it.lineTo(width / 2.0, totalHeight) //bottom right
    it.closePath()
  }

  /**
   * a rectangle that is a (good) approximation for the path.
   * This rect can be used for mouse events / mouse over effects
   */
  val approximationRect = Rectangle(-width / 2.0, 0.0, width, totalHeight)
}
