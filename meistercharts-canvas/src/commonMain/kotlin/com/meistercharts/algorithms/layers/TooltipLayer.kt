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
package com.meistercharts.algorithms.layers

import com.meistercharts.color.Color
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.tooltipSupport
import it.neckar.geometry.Direction
import com.meistercharts.model.Insets
import it.neckar.geometry.Rectangle
import it.neckar.geometry.Size
import it.neckar.geometry.with
import com.meistercharts.style.BoxStyle
import it.neckar.open.unit.si.ms

/**
 * Paints a tooltip if one is configured
 *
 */
class TooltipLayer(
  configuration: Configuration.() -> Unit = {}
) : AbstractLayer() {
  val configuration: Configuration = Configuration().also(configuration)

  override val type: LayerType
    get() = LayerType.Content

  /**
   * The last visible tooltip info
   */
  var lastVisibleTooltipInfo: VisibleTooltipInfo? = null

  /**
   * Initialize the listeners
   */
  override fun initialize(paintingContext: LayerPaintingContext) {
    //initialize the tool tip layer
    super.initialize(paintingContext)

    val layerSupport = paintingContext.layerSupport

    //Update if the mouse has been moved
    layerSupport.mouseEvents.mousePositionProperty.consumeImmediately {
      //Repaint if the mouse has been moved and a tooltip is shown
      if (lastVisibleTooltipInfo != null) {
        layerSupport.markAsDirty(DirtyReason.UserInteraction)
      }
    }
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val tooltipSupport = paintingContext.chartSupport.tooltipSupport
    val mousePosition = paintingContext.layerSupport.mouseEvents.mousePosition ?: return

    //Check for (new) tooltip
    val tooltipContent = tooltipSupport.tooltip.value

    if (tooltipContent != null) {
      lastVisibleTooltipInfo = VisibleTooltipInfo(
        mousePosition with Size(100.0, 15.0), paintingContext.frameTimestamp
      )

      gc.translate(mousePosition.x, mousePosition.y)
      gc.paintTextBox(tooltipContent.lines, Direction.BottomLeft, 3.0, 3.0, configuration.boxStyle, configuration.textColor, gc.width)

    } else {
      lastVisibleTooltipInfo = null
    }
  }

  @ConfigurationDsl
  open class Configuration {
    /**
     * The style for the box
     */
    val boxStyle: BoxStyle = BoxStyle(fill = Color.lightgray, borderColor = Color.darkgrey, padding = Insets.of(5.0))
    val textColor: Color = Color.black

    var font: FontDescriptorFragment = FontDescriptorFragment.empty
  }
}

/**
 * Contains information about the visible tooltip
 */
data class VisibleTooltipInfo(
  /**
   * The tooltip bounds
   */
  val bounds: Rectangle,

  /**
   * The time when the tooltip has been made visible
   */
  @ms
  val creationTime: Double

) {

}

/**
 * Adds a tooltip layer
 */
fun Layers.addTooltipLayer() {
  addLayer(TooltipLayer())
}
