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
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.design.Theme
import it.neckar.geometry.Coordinates

/**
 * Fills the canvas with a background color
 */
class FillBackgroundLayer(
  configuration: Configuration.() -> Unit = {}
) : AbstractLayer() {
  override val type: LayerType = LayerType.Background

  val style: Configuration = Configuration().also(configuration)

  constructor(backgroundColor: Color) : this({
    this.background = backgroundColor
  })

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    gc.fill(style.background)
    gc.fillRect(gc.boundingBox)

    style.backgroundImage?.paint(paintingContext, Coordinates.origin)
  }

  class Configuration {
    /**
     * The color to be used as background
     */
    var background: Color = Theme.lightBackgroundColor()

    /**
     * The optional background image that is painted in origin.
     * The paintable is *not* resized
     */
    var backgroundImage: Paintable? = null

    /**
     * Switches to the light background color
     */
    fun light() {
      background = Theme.lightBackgroundColor()
    }

    fun dark() {
      background = Theme.darkBackgroundColor()
    }
  }
}
