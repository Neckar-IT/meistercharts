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
import com.meistercharts.canvas.paintable.ObjectFit
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.color.ColorProvider
import com.meistercharts.design.Theme
import it.neckar.geometry.Direction
import it.neckar.open.annotations.Hot
import it.neckar.open.annotations.HotAllocation

/**
 * Shows a background image - in the window.
 *
 * The image is aligned to the bottom left corner and is painted in the size of the canvas.
 */
class BackgroundImageLayer(
  val configuration: Configuration = Configuration(),
  additionalConfiguration: Configuration.() -> Unit = {},
) : AbstractLayer() {
  override val type: LayerType = LayerType.Background

  init {
    configuration.additionalConfiguration()
  }

  @Hot
  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    gc.fill(configuration.background)
    gc.fillRect(0.0, 0.0, gc.width, gc.height)

    configuration.backgroundImage?.let {
      @HotAllocation("optional static background image - one polymorphic bounding-box query per frame, not per data point")
      val imageSize = it.boundingBox(paintingContext).size

      @HotAllocation("one Size for the aspect-ratio fit per frame, not per data point")
      val boundingBoxSize = gc.canvasSize.containWithAspectRatio(imageSize.aspectRatio)

      @HotAllocation("optional static background image - one polymorphic Paintable.paintInBoundingBox per frame, not per data point")
      it.paintInBoundingBox(paintingContext, 0.0, gc.height, Direction.BottomLeft, 0.0, 0.0, boundingBoxSize.width, boundingBoxSize.height, ObjectFit.Contain)
    }
  }

  @ConfigurationDsl
  class Configuration(
    /**
     * The optional background image that is painted in origin.
     * The paintable is *not* resized
     */
    var backgroundImage: Paintable? = null,

    /**
     * The color to be used as a background
     */
    var background: ColorProvider = Theme.primaryBackgroundColor.provider(),
  ) {
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
