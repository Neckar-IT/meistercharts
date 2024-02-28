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

import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.color.Color
import com.meistercharts.color.ColorProvider
import com.meistercharts.color.ColorProviderNullable
import com.meistercharts.color.get
import com.meistercharts.model.SidesSelection
import it.neckar.open.unit.other.px
import kotlin.math.max
import kotlin.math.min


/**
 * A painter class that is responsible for drawing a quadrilateral area with specified fill and border properties.
 */
open class RectangleAreaPainter(
  snapXValues: Boolean,
  snapYValues: Boolean,
) : AbstractPainter(snapXValues, snapYValues) {

  /**
   * The background color to be used for the area
   */
  var fill: ColorProviderNullable = Color.gray

  /**
   * The color to be used for the border
   */
  var borderColor: ColorProviderNullable = { null }

  /**
   * The line width of the border
   */
  var borderWidth: @px Double = 1.0

  /**
   * Which sides have a border
   */
  var borderSides: SidesSelection = SidesSelection.topAndBottom

  /**
   * Set the fill color.
   *
   * @param fill Color to fill the area with.
   */
  fun setFill(fill: ColorProvider): RectangleAreaPainter {
    this.fill = fill
    return this
  }

  /**
   * Set the border color.
   *
   * @param borderColor Color to set as the border color.
   */
  fun setBorderColor(borderColor: ColorProviderNullable): RectangleAreaPainter {
    this.borderColor = borderColor
    return this
  }

  fun paintArea(gc: CanvasRenderingContext, @Window @px fromX: Double, @Window @px fromY: Double, @Window @px toX: Double, @Window @px toY: Double) {
    @px @Window val largerY = max(fromY, toY)
    @px @Window val smallerY = min(fromY, toY)

    @px @Window val largerX = max(fromX, toX)
    @px @Window val smallerX = min(fromX, toX)

    @Zoomed @px val width = largerX - smallerX
    @Zoomed @px val height = largerY - smallerY

    // Fill the rect first
    fill.get()?.let {
      gc.fill(it)
      gc.fillRect(smallerX, smallerY, width, height)
    }

    // Draw the borders
    borderColor.get()?.let {
      gc.strokeStyle(it)
      gc.lineWidth = borderWidth
      if (borderSides.leftSelected) {
        gc.strokeLine(smallerX, smallerY, smallerX, largerY)
      }
      if (borderSides.topSelected) {
        gc.strokeLine(smallerX, smallerY, largerX, smallerY)
      }
      if (borderSides.rightSelected) {
        gc.strokeLine(largerX, smallerY, largerX, largerY)
      }
      if (borderSides.bottomSelected) {
        gc.strokeLine(smallerX, largerY, largerX, largerY)
      }
    }
  }
}
