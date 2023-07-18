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
package com.meistercharts.algorithms.painter

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.fillRect
import com.meistercharts.color.Color
import com.meistercharts.model.Direction
import com.meistercharts.model.Insets

/**
 * Paints a passpartout
 */
class PasspartoutPainter {
  fun paintPasspartout(
    paintingContext: LayerPaintingContext,
    color: Color,
    margin: Insets,
    insets: Insets,
    strategy: PasspartoutPaintingStrategy = OverlappingPasspartoutPaintingStrategy,
  ) {
    val gc = paintingContext.gc

    //Top and bottom are painted using the complete width
    //Left and right do *not* paint the complete height, but only the remaining area

    gc.fill(color)
    gc.stroke(color)

    strategy.paintPasspartout(paintingContext, margin, insets)
  }
}

/**
 * How the passpartout is painted
 */
fun interface PasspartoutPaintingStrategy {
  fun paintPasspartout(paintingContext: LayerPaintingContext, margin: Insets, insets: Insets)
}

/**
 * This strategy paints overlapping rectangles.
 * This is useful to avoid antialiasing artifacts but does *not* work for transparent colors
 */
object OverlappingPasspartoutPaintingStrategy : PasspartoutPaintingStrategy {
  override fun paintPasspartout(paintingContext: LayerPaintingContext, margin: Insets, insets: Insets) {
    val gc = paintingContext.gc

    @Zoomed val netWidth = gc.width - margin.offsetWidth
    @Zoomed val netHeight = gc.height - margin.offsetHeight

    //top
    gc.fillRect(margin.left, margin.top, netWidth, insets.top)

    //right
    gc.fillRect(gc.width - margin.right, margin.top, insets.right, netHeight, Direction.TopRight)

    //bottom
    gc.fillRect(margin.left, gc.height - margin.bottom, netWidth, insets.bottom, Direction.BottomLeft)

    //left
    gc.fillRect(margin.left, margin.top, insets.left, netHeight)
  }
}

/**
 * Does paint *non* overlapping rectangles.
 *
 * Beware: It is necessary to ensure there are not antialiasing artifacts
 */
object NonOverlappingPasspartoutPaintingStrategy : PasspartoutPaintingStrategy {
  override fun paintPasspartout(paintingContext: LayerPaintingContext, margin: Insets, insets: Insets) {
    val gc = paintingContext.gc

    @Zoomed val netWidth = gc.width - margin.offsetWidth
    @Zoomed val netHeight = gc.height - margin.offsetHeight

    //top
    gc.fillRect(margin.left, margin.top, netWidth, insets.top)

    //right
    gc.fillRect(gc.width - margin.right, margin.top + insets.top, insets.right, netHeight - (insets.top + insets.bottom), Direction.TopRight)

    //bottom
    gc.fillRect(margin.left, gc.height - margin.bottom, netWidth, insets.bottom, Direction.BottomLeft)

    //left
    gc.fillRect(margin.left, margin.top + insets.top, insets.left, netHeight - (insets.top + insets.bottom))
  }
}
