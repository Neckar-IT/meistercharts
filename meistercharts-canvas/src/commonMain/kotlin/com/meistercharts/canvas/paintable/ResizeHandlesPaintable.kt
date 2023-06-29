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
import com.meistercharts.color.Color
import com.meistercharts.canvas.resize.HandleBoundsProvider
import com.meistercharts.model.Direction
import com.meistercharts.geometry.Rectangle
import it.neckar.open.collections.fastForEach

/**
 * A paintable that draws resize handles
 */
open class ResizeHandlesPaintable(
  /**
   * Provides the bounds for the handles
   */
  val handleBoundsProvider: HandleBoundsProvider
) : Paintable {

  val style: Style = Style()

  override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle {
    return Rectangle.withLTRB(
      handleBoundsProvider.minX(Direction.TopLeft),
      handleBoundsProvider.minY(Direction.TopLeft),
      handleBoundsProvider.maxX(Direction.BottomRight),
      handleBoundsProvider.maxX(Direction.BottomRight),
    )
  }

  override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    val gc = paintingContext.gc

    gc.fill(style.handleFill)

    Direction.cornersAndSides.fastForEach({ it: Direction ->
      gc.fillOvalOrigin(
        handleBoundsProvider.minX(it),
        handleBoundsProvider.minY(it),
        handleBoundsProvider.width(it),
        handleBoundsProvider.height(it),
      )
    })
  }

  class Style {
    /**
     * The color of the handle
     */
    var handleFill: Color = Color.lightgray
  }
}
