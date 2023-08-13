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
package com.meistercharts.algorithms.layers.debug

import com.meistercharts.state.contentViewportHeight
import com.meistercharts.state.contentViewportWidth
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.color.Color
import com.meistercharts.algorithms.painter.NonOverlappingPasspartoutPaintingStrategy
import com.meistercharts.algorithms.painter.PasspartoutPainter
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.model.Insets

/**
 * Shows some debug markers for the content viewport
 */
open class ContentViewportDebugLayer(
  val configuration: Configuration = Configuration(),
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractLayer() {

  init {
    configuration.additionalConfiguration()
  }

  override val type: LayerType
    get() = LayerType.Content

  val passpartoutPainter: PasspartoutPainter = PasspartoutPainter()

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val chartState = paintingContext.chartState

    passpartoutPainter.paintPasspartout(
      paintingContext = paintingContext,
      color = configuration.fill,
      margin = Insets.empty,
      insets = chartState.contentViewportMargin,
      strategy = NonOverlappingPasspartoutPaintingStrategy
    )

    gc.stroke(configuration.stroke)
    gc.strokeRect(
      x = chartState.contentViewportMarginLeft,
      y = chartState.contentViewportMarginTop,
      width = chartState.contentViewportWidth,
      height = chartState.contentViewportHeight
    )
  }

  @ConfigurationDsl
  class Configuration {
    var fill: Color = Color.blue.withAlpha(0.5)
    var stroke: Color = Color.blue
  }
}
