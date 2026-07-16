/*
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
import com.meistercharts.canvas.fill
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.color.Color
import com.meistercharts.color.ColorProvider
import com.meistercharts.design.Theme
import com.meistercharts.zoom.OriginToContentViewport.provider
import it.neckar.open.annotations.Hot
import it.neckar.open.annotations.HotAllocation
import it.neckar.open.kotlin.lang.asProvider

/**
 * Fills the canvas with a background color
 */
class FillBackgroundLayer(
  additionalConfiguration: Configuration.() -> Unit = {}
) : AbstractLayer() {
  override val type: LayerType = LayerType.Background

  val configuration: Configuration = Configuration().also(additionalConfiguration)

  constructor(backgroundColor: Color) : this({
    this.background = backgroundColor.asProvider()
  })

  @Hot
  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    gc.fill(configuration.background)
    gc.fillRect(0.0, 0.0, gc.width, gc.height)

    @HotAllocation("optional static background image - one polymorphic Paintable.paint per frame, not per data point")
    configuration.backgroundImage?.paint(paintingContext)
  }

  @ConfigurationDsl
  class Configuration {
    /**
     * The color to be used as background
     */
    var background: ColorProvider = Theme.primaryBackgroundColor.provider()

    /**
     * The optional background image that is painted in origin.
     * The paintable is *not* resized
     */
    var backgroundImage: Paintable? = null

    /**
     * Switches to the primary background color
     */
    fun primary() {
      background = Theme.primaryBackgroundColor.provider()
    }

    /**
     * Switches to the secondary background color
     */
    fun secondary() {
      background = Theme.secondaryBackgroundColor.provider()
    }
  }
}

/**
 * Adds a [FillBackgroundLayer] to the layers that uses the canvas-background color provided by the theme
 */
fun Layers.addFillCanvasBackground(backgroundColor: () -> Color = Theme.canvasBackgroundColor.provider()): FillBackgroundLayer {
  return FillBackgroundLayer {
    background = backgroundColor
  }.also {
    addLayer(it)
  }
}
