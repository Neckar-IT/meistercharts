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
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.paintTextBox
import it.neckar.geometry.Direction
import com.meistercharts.model.Insets
import com.meistercharts.model.Zoom
import it.neckar.open.formatting.CachedNumberFormat
import it.neckar.open.formatting.decimalFormat2digits
import it.neckar.open.i18n.I18nConfiguration
import com.meistercharts.style.BoxStyle

/**
 * Shows the current zoom level
 *
 */
class ShowZoomLevelLayer(
  styleConfiguration: Style.() -> Unit = {}
) : AbstractLayer() {

  private val style: Style = Style().also(styleConfiguration)

  override val type: LayerType
    get() = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val text = paintingContext.chartSupport.currentChartState.zoom.getZoomFormatted(paintingContext.i18nConfiguration)

    val gc = paintingContext.gc
    gc.translate(gc.width, 0.0)
    //to top right corner

    gc.font(style.font)
    gc.paintTextBox(text, Direction.TopRight, 5.0, 5.0, style.boxStyle, style.textColor, 150.0)
  }

  /**
   * Returns the zoom as formatted string
   */
  private fun Zoom.getZoomFormatted(i18nConfiguration: I18nConfiguration): String {
    val formattedZoomX = style.decimalFormat.format(scaleX, i18nConfiguration)
    val formattedZoomY = style.decimalFormat.format(scaleY, i18nConfiguration)
    return "X: $formattedZoomX / Y: $formattedZoomY"
  }

  /**
   * The style configuration for a [ShowZoomLevelLayer]
   */
  @ConfigurationDsl
  open class Style {
    /**
     * The style for the box (background fill + border stroke + insets)
     */
    var boxStyle: BoxStyle = BoxStyle(Color.rgba(102, 102, 102, 0.5), null, padding = Insets(3.0, 5.0, 3.0, 5.0))

    /**
     * The color that is used for the text
     */
    var textColor: Color = Color.white

    /**
     * The font that is used for the text
     */
    var font: FontDescriptorFragment = FontDescriptorFragment.empty

    /**
     * The format that is used to format the zoom-values
     */
    var decimalFormat: CachedNumberFormat = decimalFormat2digits
  }
}

/**
 * Adds a [ShowZoomLevelLayer] to this [Layers]
 */
fun Layers.addShowZoomLevel() {
  addLayer(ShowZoomLevelLayer())
}
