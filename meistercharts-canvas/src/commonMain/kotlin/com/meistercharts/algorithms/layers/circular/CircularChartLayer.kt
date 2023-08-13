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
package com.meistercharts.algorithms.layers.circular

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.ConfigurationDsl
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.other.px
import kotlin.math.min

/**
 * A circular chart layer
 */
class CircularChartLayer(
  val configuration: Configuration,
  additionalConfiguration: Configuration.() -> Unit = {}
) : AbstractLayer() {

  constructor(
    valuesProvider: @pct DoublesProvider,
    additionalConfiguration: Configuration.() -> Unit = {}
  ) : this(Configuration(valuesProvider), additionalConfiguration)

  init {
    configuration.additionalConfiguration()
  }

  override val type: LayerType
    get() = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val circularChartPaintable = CircularChartPaintable(configuration.valuesProvider, configuration)

    with(paintingContext.chartCalculator) {
      //The center of the domain relative area
      @Window val center = contentAreaRelative2window(0.5, 0.5)

      //The size of the outer circle - describes the *outer* side of the line
      @Zoomed val outerRadius = contentAreaRelative2zoomed(0.5, 0.5)
        .let {
          //take the smaller size
          min(it.width, it.height).coerceAtMost(configuration.maxDiameter / 2.0)
        }

      //Paint the donut using a painter
      circularChartPaintable.outerLineRadiusOutside = outerRadius
      circularChartPaintable.style.outerCircleWidth
      circularChartPaintable.paint(paintingContext, center)
    }
  }

  @ConfigurationDsl
  open class Configuration(
    /**
     * Provides values for circular chart segments.
     * The values are provided in percentage. The sum of the values must not be greater than 1.0
     */
    val valuesProvider: @pct DoublesProvider
  ): CircularChartPaintable.Style() {
    /**
     * The maximum size of the circle (outer width)
     */
    var maxDiameter: @px Double = 500.0
  }
}

