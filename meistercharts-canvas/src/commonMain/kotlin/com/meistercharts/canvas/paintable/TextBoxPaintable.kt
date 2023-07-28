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
package com.meistercharts.canvas.paintable

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.canvas.i18nConfiguration
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.textService
import it.neckar.geometry.Direction
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextService

/**
 * Paints a text box
 */
@Deprecated("Fix the calculation of bounds")
class TextBoxPaintable(
  lines: (textService: TextService, i18nConfiguration: I18nConfiguration) -> List<String>,
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractPaintable() {

  val configuration: Configuration = Configuration(lines).also(additionalConfiguration)

  override fun paintingVariables(): PaintablePaintingVariables {
    return paintingVariables
  }

  private val paintingVariables = object : AbstractPaintablePaintingVariables() {
    override fun calculate(paintingContext: LayerPaintingContext) {
      super.calculate(paintingContext)

      val gc = paintingContext.gc
      val chartSupport = paintingContext.chartSupport

      val texts = configuration.lines(chartSupport.textService, chartSupport.i18nConfiguration)
      //TODO find a better way to calculate the text box!
      boundingBox = gc.paintTextBox(texts, configuration.anchorDirection)
    }
  }

  override fun paintAfterLayout(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    val gc = paintingContext.gc
    val chartSupport = paintingContext.chartSupport

    gc.translate(x, y)

    val texts = configuration.lines(chartSupport.textService, chartSupport.i18nConfiguration)
    gc.paintTextBox(texts, configuration.anchorDirection)
  }

  class Configuration(
    /**
     * Provides the lines
     */
    var lines: (textService: TextService, i18nConfiguration: I18nConfiguration) -> List<String>,
  ) {
    var anchorDirection: Direction = Direction.TopLeft
  }
}
