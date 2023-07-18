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
package com.meistercharts.algorithms.layers.linechart

import com.meistercharts.color.Color
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.LineJoin
import com.meistercharts.style.Palette
import it.neckar.open.unit.other.px
import kotlin.jvm.JvmField
import kotlin.jvm.JvmOverloads

/**
 * The style of a line
 *
 * Can be used to set dashes, stroke and line width
 */
data class LineStyle @JvmOverloads constructor(
  val color: Color = Palette.defaultGray,
  val lineWidth: @px Double = 1.0,
  val dashes: @px Dashes? = null,
  val lineJoin: LineJoin = LineJoin.Miter
) {
  /**
   * Applies the given line style to the graphics context
   */
  fun apply(gc: CanvasRenderingContext) {
    gc.stroke(color)
    gc.lineWidth = lineWidth
    gc.lineJoin = lineJoin
    dashes.apply(gc)
  }

  companion object {
    /**
     * Default line style (gray)
     */
    val DefaultGray: LineStyle = LineStyle()

    /**
     * Continuous line
     */
    val Continuous: LineStyle = LineStyle(dashes = null)

    /**
     * Dotted line
     */
    val Dotted: LineStyle = LineStyle(dashes = Dashes.Dotted)

    /**
     * Line with small dashes
     */
    val SmallDashes: LineStyle = LineStyle(dashes = Dashes.SmallDashes)

    /**
     * Line with large dashes
     */
    val LargeDashes: LineStyle = LineStyle(dashes = Dashes.LargeDashes)
  }
}

/**
 * A dash configuration for dashes
 */
data class Dashes(
  /**
   * The length of the filled strokes
   */
  val fill: @px Double,
  /**
   * The length of the gap
   */
  val gap: @px Double
) {

  init {
    require(fill > 0.0) { "fill must be greater than 0 but was <$fill>" }
    require(gap > 0.0) { "gap must be greater than 0 but was <$gap>" }
  }

  /**
   * The dash values
   */
  val dashes: @px DoubleArray = doubleArrayOf(fill, gap)

  companion object {
    @JvmField
    val Dotted: Dashes = Dashes(2.0, 3.0)

    @JvmField
    val SmallDashes: Dashes = Dashes(5.0, 7.0)

    @JvmField
    val LargeDashes: Dashes = Dashes(10.0, 10.0)


    val predefined: List<Dashes> = listOf(Dotted, SmallDashes, LargeDashes)
  }
}

/**
 * Applies the dash config to the rendering context; null means no dashes
 */
fun Dashes?.apply(gc: CanvasRenderingContext) {
  if (this == null) {
    gc.clearLineDash()
  } else {
    gc.setLineDash(*this.dashes)
  }
}
