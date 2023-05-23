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

import com.meistercharts.algorithms.axis.AxisSelection
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.ConfigurationDsl
import it.neckar.open.unit.other.px

/**
 * Paints (endless lines) at zero
 */
class ZeroLinesLayer(
  styleConfiguration: Style.() -> Unit = {}
) : AbstractLayer() {

  val style: Style = Style().also(styleConfiguration)

  override val type: LayerType
    get() = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    gc.lineWidth = style.lineWidth
    gc.stroke(style.color)

    val chartCalculator = paintingContext.chartCalculator
    if (style.axisToPaint.containsX) {
      @Window val y = chartCalculator.domainRelative2windowY(0.0)
      gc.strokeLine(0.0, y, gc.width, y)
    }

    if (style.axisToPaint.containsY) {
      @Window val x = chartCalculator.domainRelative2windowX(0.0)
      gc.strokeLine(x, 0.0, x, gc.height)
    }
  }

  @ConfigurationDsl
  open class Style {
    /**
     * The color for the lines
     */
    var color: Color = Color.silver

    var lineWidth: @px Double = 1.0

    /**
     * Which axis to paint
     */
    var axisToPaint: AxisSelection = AxisSelection.Both
  }
}
