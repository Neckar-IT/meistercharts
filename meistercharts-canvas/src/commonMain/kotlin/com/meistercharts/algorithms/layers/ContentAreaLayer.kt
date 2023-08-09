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
import com.meistercharts.algorithms.painter.ContentAreaPainter
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.model.SidesSelection
import it.neckar.open.unit.other.px

/**
 * Strokes lines around the content are
 */
class ContentAreaLayer(
  additionalConfiguration: Configuration.() -> Unit = {}
) : AbstractLayer() {

  val configuration: Configuration = Configuration().also(additionalConfiguration)

  override val type: LayerType
    get() = LayerType.Content

  private val contentAreaPainter = ContentAreaPainter()

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    gc.lineWidth = configuration.lineWidth

    contentAreaPainter
      .also {
        it.stroke = configuration.color
        it.sidesToPaint = configuration.sidesToPaint
      }
      .paint(gc, paintingContext.chartCalculator)
  }

  @ConfigurationDsl
  open class Configuration {
    /**
     * The color for the lines
     */
    var color: Color = Color.silver

    var lineWidth: @px Double = 1.0

    /**
     * Which sides to paint
     */
    var sidesToPaint: SidesSelection = SidesSelection.all
  }
}
