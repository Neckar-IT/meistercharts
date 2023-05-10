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

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.layout.cache.DoubleCache
import com.meistercharts.canvas.layout.cache.StringCache
import com.meistercharts.model.Side
import it.neckar.open.collections.fastForEachIndexed
import it.neckar.open.unit.other.px
import kotlin.math.max
import kotlin.math.min

/**
 * Provides the painting properties for a value axis layer
 */
interface ValueAxisPaintingVariables : AxisPaintingVariables {
  /**
   * The smallest visible domain value
   */
  val startDomainValue: @Domain Double

  /**
   * The largest visible domain value
   */
  val endDomainValue: @Domain Double

  /**
   * The value range for the *content* area
   */
  val contentAreaValueRange: @Domain ValueRange

  /**
   * Contains the tick values
   */
  val tickDomainValues: @Domain DoubleCache
}

/**
 * Implementation for a value axis (that has a value range!)
 */
abstract class ValueAxisPaintingVariablesImpl : AxisPaintingVariablesImpl(), ValueAxisPaintingVariables {
  /**
   * The smallest visible value - never larger than [endDomainValue]
   * This value might be located at the top or bottom / left or right - depending on the axis orientations
   */
  override var startDomainValue: @Domain Double = 0.0

  /**
   * The largest visible value - never smaller than [startDomainValue]
   * This value might be located at the top or bottom / left or right - depending on the axis orientations
   */
  override var endDomainValue: @Domain Double = 0.0

  /**
   * The value range
   */
  override var contentAreaValueRange: @Domain ValueRange = ValueRange.default

  override var tickDomainValues: @Domain DoubleCache = DoubleCache()


  /**
   * The estimated(!) maximum length of the formatted tick values.
   *
   */
  var estimatedTickFormatMaxLength: @px Double = 0.0

  /**
   * Contains the formatted tick labels
   */
  val ticksFormatted: StringCache = StringCache()

  /**
   * Resets all variables to their default values
   */
  override fun reset() {
    super.reset()

    estimatedTickFormatMaxLength = Double.NaN

    startDomainValue = Double.NaN
    endDomainValue = Double.NaN

    contentAreaValueRange = ValueRange.default

    tickDomainValues.reset()
    ticksFormatted.reset()
  }

  override fun calculateTickLabelsMaxWidthHorizontal(): Double {
    return estimatedTickFormatMaxLength
  }

  /**
   * Stores the ticks
   */
  fun storeTicks(
    tickValues: @Domain DoubleArray,
    paintingContext: LayerPaintingContext,
    style: ValueAxisLayer.Style,
  ) {
    tickDomainValues.ensureSize(tickValues.size)
    ticksFormatted.ensureSize(tickValues.size)

    tickValues.fastForEachIndexed { index, value ->
      tickDomainValues[index] = value
      ticksFormatted[index] = style.ticksFormat.format(value, paintingContext.i18nConfiguration)
    }
  }

  /**
   * Calculates the [estimatedTickFormatMaxLength].
   * ATTENTION: Requires the value range to be set!
   */
  fun calculateEstimatedTickFormatMaxLength(
    paintingContext: LayerPaintingContext,
    style: AxisStyle,
  ) {
    //Calculate the max length of/for the tick labels. We assume that the first (for negative values) or the last one (for positive values) is probably the largest value.
    //Also ensure that we have at least 6 characters if a broken tick format is provided.

    val startTickFormatted: String = style.ticksFormat.format(contentAreaValueRange.start, paintingContext.i18nConfiguration)
    val endTickFormatted: String = style.ticksFormat.format(contentAreaValueRange.end, paintingContext.i18nConfiguration)

    fun calcTickLength(tickFormatted: String): @Zoomed Double {
      val padEnd = tickFormatted.padEnd(6, 'M')
      return paintingContext.gc.calculateTextWidth(padEnd)
    }

    @Zoomed val startTickFormattedLength = calcTickLength(startTickFormatted)
    @Zoomed val endTickFormattedLength = calcTickLength(endTickFormatted)

    estimatedTickFormatMaxLength = max(startTickFormattedLength, endTickFormattedLength) * 1.3
  }


  /**
   * Calculates the domain start and end values.
   * Requires [axisStart] and [axisEnd] and [contentAreaValueRange] to be set!
   */
  fun calculateDomainStartEndValues(
    paintingContext: LayerPaintingContext,
    style: AxisStyle,
  ) {
    val chartCalculator = paintingContext.chartCalculator

    @Domain val relevantUpperDomain: @Domain Double
    @Domain val relevantLowerDomain: @Domain Double

    when (style.side) {
      Side.Left, Side.Right -> {
        relevantUpperDomain = chartCalculator.window2domainY(axisStart, contentAreaValueRange)
        relevantLowerDomain = chartCalculator.window2domainY(axisEnd, contentAreaValueRange)
      }

      Side.Top, Side.Bottom -> {
        relevantUpperDomain = chartCalculator.window2domainX(axisStart, contentAreaValueRange)
        relevantLowerDomain = chartCalculator.window2domainX(axisEnd, contentAreaValueRange)
      }
    }

    startDomainValue = min(relevantUpperDomain, relevantLowerDomain)
    endDomainValue = max(relevantUpperDomain, relevantLowerDomain)
  }
}
