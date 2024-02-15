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

import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.paintable.ObjectFit
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.color.Color
import com.meistercharts.design.Theme
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Direction

/**
 * Shows a background image - in the window.
 *
 * The image is aligned to the bottom left corner and is painted in the size of the canvas.
 */
class BackgroundImageLayer(
  val configuration: Configuration = Configuration(),
  additionalConfiguration: Configuration.() -> Unit = {}
) : AbstractLayer() {
  override val type: LayerType = LayerType.Background

  init {
    configuration.additionalConfiguration()
  }

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    gc.fill(configuration.background)
    gc.fillRect(gc.boundingBox)

    configuration.backgroundImage?.let {
      val imageSize = it.boundingBox(paintingContext).size
      val boundingBoxSize = gc.canvasSize.containWithAspectRatio(imageSize.aspectRatio)

      it.paintInBoundingBox(paintingContext, Coordinates.of(0.0, gc.height), Direction.BottomLeft, boundingBoxSize, ObjectFit.Contain)
    }
  }

  @ConfigurationDsl
  class Configuration {
    /**
     * The color to be used as background
     */
    var background: Color = Theme.primaryBackgroundColor()

    /**
     * The optional background image that is painted in origin.
     * The paintable is *not* resized
     */
    var backgroundImage: Paintable? = null

    /**
     * Switches to the primary background color
     */
    fun primary() {
      background = Theme.primaryBackgroundColor()
    }

    /**
     * Switches to the secondary background color
     */
    fun secondary() {
      background = Theme.secondaryBackgroundColor()
    }
  }

}
