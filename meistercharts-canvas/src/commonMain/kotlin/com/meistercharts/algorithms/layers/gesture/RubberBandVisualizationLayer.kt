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
package com.meistercharts.algorithms.layers.gesture

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.fill
import com.meistercharts.color.Color
import com.meistercharts.canvas.fillRectCoordinates
import com.meistercharts.canvas.stroke
import com.meistercharts.canvas.strokeRectCoordinates
import com.meistercharts.color.ColorProvider
import it.neckar.geometry.Coordinates
import it.neckar.open.kotlin.lang.asProvider

/**
 * A layer that visualizers the rubber band (mouse gesture)
 */
class RubberBandVisualizationLayer(
  val configuration: Configuration,
  additionalConfiguration: Configuration.() -> Unit = {}
) : AbstractLayer() {

  constructor(
    /**
     * The start location of the rubber band
     */
    startLocation: () -> Coordinates?,

    /**
     * The current location of the rubber band
     */
    currentLocation: () -> Coordinates?,
    additionalConfiguration: Configuration.() -> Unit = {}
  ): this(Configuration(startLocation, currentLocation), additionalConfiguration)

  init {
    configuration.additionalConfiguration()
  }

  override val type: LayerType = LayerType.Notification

  override fun paint(paintingContext: LayerPaintingContext) {
    val startLocation = configuration.startLocation() ?: return
    val currentLocation = configuration.currentLocation() ?: return

    val gc = paintingContext.gc

    gc.stroke(configuration.stroke)
    gc.fill(configuration.fill)

    gc.fillRectCoordinates(startLocation, currentLocation)
    gc.strokeRectCoordinates(startLocation, currentLocation)
  }

  @ConfigurationDsl
  class Configuration(
    /**
     * The start location of the rubber band
     */
    val startLocation: () -> Coordinates?,

    /**
     * The current location of the rubber band
     */
    val currentLocation: () -> Coordinates?
  ) {
    /**
     * The stroke of the rubber band
     */
    var stroke: ColorProvider = Color.orange

    /**
     * The fill of the rubber band
     */
    var fill: ColorProvider = Color.rgba(255, 165, 0, 0.5).asProvider()
  }
}
