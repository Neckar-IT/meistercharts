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
package com.meistercharts.js.debug

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.Layers
import com.meistercharts.algorithms.layers.toggleShortcut
import com.meistercharts.algorithms.layers.visible
import com.meistercharts.color.Color
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.canvas.Image
import com.meistercharts.canvas.saved
import com.meistercharts.events.KeyCode
import com.meistercharts.events.KeyStroke
import com.meistercharts.events.ModifierCombination
import com.meistercharts.js.FontMetricsCacheJS
import com.meistercharts.model.Direction
import it.neckar.open.unit.other.px
import size

/**
 * This layer paints the canvas of the [com.meistercharts.js.FontMetricsCacheJS]
 */
class FontMetricsCacheDebugLayer(
  styleConfiguration: Style.() -> Unit = {}
) : AbstractLayer() {
  override val type: LayerType
    get() = LayerType.Content

  val style: Style = Style().also(styleConfiguration)

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val canvas = FontMetricsCacheJS.fontMetricsCalculator.canvas.canvasElement

    gc.stroke(Color.orangered)
    gc.lineWidth = 5.0
    gc.strokeRect(0.0, 0.0, canvas.width.toDouble(), canvas.height.toDouble())

    gc.saved {
      gc.scale(1.0, 1.0 / FontMetricsCacheJS.fontMetricsCalculator.scaleFactorY)
      val image = Image(canvas, canvas.size)
      paintingContext.gc.saved {
        image.paintInBoundingBox(paintingContext, 0.0, 0.0, Direction.TopLeft)
      }

      @px val baseLineY = FontMetricsCacheJS.fontMetricsCalculator.anchorY.toDouble()

      gc.lineWidth = 1.0
      gc.stroke(Color.blueviolet)
      gc.strokeLine(0.0, baseLineY, canvas.width.toDouble(), baseLineY)
    }

    val font = style.font.withDefaultValues()
    val fontMetrics = FontMetricsCacheJS.get(font)

    gc.fill(Color.darkgray)
    gc.fillText("Font metrics Content", 10.0, 10.0, Direction.TopLeft)
    gc.fillText("Cache size: ${FontMetricsCacheJS.cacheSize}", 10.0, 40.0, Direction.TopLeft)
    gc.fillText("font: $font", 10.0, 70.0, Direction.TopLeft)
    gc.fillText("ascent: ${fontMetrics.accentLine}", 10.0, 100.0, Direction.TopLeft)
    gc.fillText("ascentPercentage: ${fontMetrics.ascentPercentage}", 10.0, 130.0, Direction.TopLeft)
    gc.fillText("descent: ${fontMetrics.pLine}", 10.0, 160.0, Direction.TopLeft)
    gc.fillText("totalHeight: ${fontMetrics.totalHeight}", 10.0, 190.0, Direction.TopLeft)
  }

  class Style {
    /**
     * The font to retrieve font metrics for
     */
    var font: FontDescriptorFragment = FontDescriptorFragment.DefaultSize
  }
}

/**
 * Adds the debug panel
 */
fun Layers.addFontMetricsDebugLayer() {
  addLayer(
    FontMetricsCacheDebugLayer()
      .visible(true)
      .toggleShortcut(KeyStroke(KeyCode('F'), ModifierCombination.CtrlShiftAlt))
  )
}
