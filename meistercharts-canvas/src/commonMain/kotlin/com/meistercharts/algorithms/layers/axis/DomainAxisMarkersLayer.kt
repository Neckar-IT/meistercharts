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
package com.meistercharts.algorithms.layers.axis

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.color.Color
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.strokeCross
import com.meistercharts.provider.CoordinatesProvider

/**
 * Marks a coordinate on the domain axises by painting lines
 */
class DomainAxisMarkersLayer(
  val configuration: Configuration,
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractLayer() {

  constructor(
    coordinatesProvider: CoordinatesProvider,
    additionalConfiguration: Configuration.() -> Unit = {},
  ) : this(Configuration(coordinatesProvider), additionalConfiguration)

  init {
    configuration.additionalConfiguration()
  }

  override val type: LayerType
    get() = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val chartCalculator = paintingContext.chartCalculator

    val coordinates = configuration.coordinatesProvider()

    val x = chartCalculator.domainRelative2windowX(coordinates.x)
    val y = chartCalculator.domainRelative2windowY(coordinates.y)

    gc.stroke(configuration.lineColor)
    gc.strokeLine(x, y, chartCalculator.contentAreaRelative2windowX(0.0), y)
    gc.strokeLine(x, y, x, chartCalculator.contentAreaRelative2windowY(1.0))

    if (configuration.paintMarker) {
      gc.strokeCross(x, y, 15.0)
    }
  }

  @ConfigurationDsl
  class Configuration(
    val coordinatesProvider: CoordinatesProvider
  ) {
    /**
     * Color for the current position lines
     */
    var lineColor: Color = Color.web("#097CBC")

    /**
     * Whether to paint a marker at the Coordinate position
     */
    var paintMarker: Boolean = true
  }
}
