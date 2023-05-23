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
package com.meistercharts.algorithms.layers.circular

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.circular.CircularChartLegendLayer.CircleSegmentIndex
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.ArcType
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.model.Rectangle
import it.neckar.open.kotlin.lang.toRadians
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.fastForEachIndexed
import com.meistercharts.style.Palette.chartColors
import it.neckar.open.unit.other.deg
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.other.px
import kotlin.math.PI

/**
 * Paints a donut (circular chart)
 *
 */
class CircularChartPaintable(
  /**
   * Provides values for circular chart segments.
   * The values are provided in percentage. The sum of the values must not be greater than 1.0
   */
  val valuesProvider: DoublesProvider,
  /**
   * The style of the paintable
   */
  val style: Style = Style()
) : Paintable {

  /**
   * The radius of the circle (outer).
   * The *outer* side of the line.
   */
  var outerLineRadiusOutside: @Zoomed Double = 100.0

  override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle = Rectangle(0.0, 0.0, outerLineRadiusOutside * 2, outerLineRadiusOutside * 2)

  @Suppress("ReplaceManualRangeWithIndicesCalls")
  override fun paint(paintingContext: LayerPaintingContext, @Window x: Double, @Window y: Double) {
    val gc = paintingContext.gc


    val outerLineRadius = outerLineRadiusOutside - style.outerCircleWidth / 2.0

    //The amount the inner circle is smaller than the outer circle
    val innerCircleSmaller = style.outerCircleWidth / 2.0 + style.gapInnerOuter + style.innerCircleWidth / 2.0

    //The inner size -- center of the line
    @Window val innerRadius = outerLineRadius - innerCircleSmaller

    //Stroke the outer circle
    gc.lineWidth = style.outerCircleWidth

    //Draw the outer segments
    @deg var currentStart = -90.0 //start at the top (12 o'clock)

    valuesProvider.fastForEachIndexed { index, value ->
      gc.stroke(style.segmentsColorProvider.valueAt(index))

      @deg val gap = style.outerCircleValueGap.calculateGap(outerLineRadius)
      @deg val length = 360 * value - gap
      gc.strokeArcCenter(x, y, outerLineRadius, currentStart.toRadians(), length.toRadians(), ArcType.Open)

      currentStart += length
      currentStart += gap
    }

    //Stroke the inner circle
    if (style.innerCircleWidth > 0.0) {
      gc.lineWidth = style.innerCircleWidth
      gc.stroke(style.innerCircleColor)
      gc.strokeOvalCenter(x, y, innerRadius * 2, innerRadius * 2)
    }
  }

  @ConfigurationDsl
  open class Style {
    /**
     * Provides the color for the segment at a certain index
     */
    var segmentsColorProvider: MultiProvider<CircleSegmentIndex, Color> = MultiProvider.forListModulo(chartColors)

    var outerCircleWidth: @px Double = 23.0

    /**
     * The gap between outer circle values (in degrees).
     * Calculates the gap from the given radius
     */
    var outerCircleValueGap: @deg GapProvider = FixedPixelsGap(2.0)

    /**
     * Sets the gap to a fixed value in pixels
     */
    fun outerCircleValueGapPixels(gap: @px Double) {
      outerCircleValueGap = FixedPixelsGap(gap)
    }

    /**
     * The width of the inner (usually gray) circle
     */
    var innerCircleWidth: @px Double = 0.0

    /**
     * The color of the inner circle
     */
    var innerCircleColor: Color = Color.web("#eeeff0")

    /**
     * The gap between inner and outer circle
     */
    var gapInnerOuter: @px Double = 0.0
  }
}

fun interface GapProvider {
  /**
   * Returns the gap for a given radius
   */
  fun calculateGap(radius: @px Double): @deg Double
}

/**
 * Converts a fixed pixel values to degrees depending on the radius
 */
class FixedPixelsGap(val gap: @px Double) : GapProvider {
  override fun calculateGap(radius: @px Double): @deg Double {
    /**
     * The circumference in pixels
     */
    @px
    val circumference = PI * 2 * radius
    @pct val gapPercentage = 1 / circumference * gap

    return 360 * gapPercentage
  }
}
