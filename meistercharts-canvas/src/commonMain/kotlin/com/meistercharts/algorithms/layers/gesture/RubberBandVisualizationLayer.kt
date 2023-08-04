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
import com.meistercharts.color.Color
import com.meistercharts.canvas.fillRectCoordinates
import com.meistercharts.canvas.strokeRectCoordinates
import it.neckar.geometry.Coordinates

/**
 * A layer that visualizers the rubber band (mouse gesture)
 */
class RubberBandVisualizationLayer(
  val data: Data,
  styleConfiguration: Style.() -> Unit = {}
) : AbstractLayer() {
  val style: Style = Style().also(styleConfiguration)

  override val type: LayerType = LayerType.Notification

  override fun paint(paintingContext: LayerPaintingContext) {
    val startLocation = data.startLocation() ?: return
    val currentLocation = data.currentLocation() ?: return

    val gc = paintingContext.gc

    gc.stroke(style.stroke)
    gc.fill(style.fill)

    gc.fillRectCoordinates(startLocation, currentLocation)
    gc.strokeRectCoordinates(startLocation, currentLocation)
  }

  class Style {
    /**
     * The stroke of the rubber band
     */
    var stroke: Color = Color.orange

    /**
     * The fill of the rubber band
     */
    var fill: Color = Color.rgba(255, 165, 0, 0.5)
  }

  class Data(
    /**
     * The start location of the rubber band
     */
    val startLocation: () -> Coordinates?,

    /**
     * The current location of the rubber band
     */
    val currentLocation: () -> Coordinates?
  )
}
