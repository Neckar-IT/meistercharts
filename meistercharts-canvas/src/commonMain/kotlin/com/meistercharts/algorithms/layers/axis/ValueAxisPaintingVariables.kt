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
package com.meistercharts.algorithms.layers.axis

import com.meistercharts.algorithms.layers.AxisConfiguration
import com.meistercharts.algorithms.layers.AxisPaintingVariables
import com.meistercharts.algorithms.layers.AxisPaintingVariablesImpl
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.layout.buffer.TickLabelsBuffer
import it.neckar.geometry.Side
import com.meistercharts.range.ValueRange
import it.neckar.open.annotations.Hot
import it.neckar.open.annotations.HotAllocation
import it.neckar.open.collections.DoubleArrayList
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
   * Contains the tick values and their formatted labels
   */
  val tickLabels: @Domain TickLabelsBuffer
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

  override val tickLabels: @Domain TickLabelsBuffer = TickLabelsBuffer()

  /**
   * The estimated(!) maximum length of the formatted tick values.
   *
   */
  var estimatedTickFormatMaxLength: @px Double = 0.0

  /**
   * Reusable target for [TickProvider.fillTicks] - avoids allocating a fresh tick array on every layout pass.
   * Only valid during the current layout pass; the values are copied into [tickLabels] by [storeTicks].
   */
  val tickValuesScratch: @Domain DoubleArrayList = DoubleArrayList()

  /**
   * Resets all variables to their default values
   */
  @Hot
  override fun reset() {
    super.reset()

    estimatedTickFormatMaxLength = Double.NaN

    startDomainValue = Double.NaN
    endDomainValue = Double.NaN

    contentAreaValueRange = ValueRange.default

    tickLabels.reset()
  }

  @Hot
  override fun calculateTickLabelsMaxWidthHorizontal(): Double {
    return estimatedTickFormatMaxLength
  }

  /**
   * Stores the ticks
   */
  @Hot
  fun storeTicks(
    tickValues: @Domain DoubleArray,
    paintingContext: LayerPaintingContext,
    style: ValueAxisLayer.Configuration,
  ) {
    tickLabels.resize(tickValues.size)

    tickValues.fastForEachIndexed { index, value ->
      @HotAllocation("Once per tick per layout pass - formatted tick label string. Fix candidate: cache formatted labels per tick value; tick values only change on zoom/pan")
      val formatted = style.ticksFormat.format(value, paintingContext.i18nConfiguration)
      tickLabels.set(index, value, formatted)
    }
  }

  /**
   * Stores the ticks from the given list (usually [tickValuesScratch] filled by [TickProvider.fillTicks]) - allocation-free variant.
   */
  @Hot
  fun storeTicks(
    tickValues: @Domain DoubleArrayList,
    paintingContext: LayerPaintingContext,
    style: ValueAxisLayer.Configuration,
  ) {
    tickLabels.resize(tickValues.size)

    for (index in 0 until tickValues.size) {
      val value = tickValues[index]
      @HotAllocation("Once per tick per layout pass - formatted tick label string. Fix candidate: cache formatted labels per tick value; tick values only change on zoom/pan")
      val formatted = style.ticksFormat.format(value, paintingContext.i18nConfiguration)
      tickLabels.set(index, value, formatted)
    }
  }

  /**
   * Calculates the [estimatedTickFormatMaxLength].
   * ATTENTION: Requires the value range to be set!
   */
  @Hot
  fun calculateEstimatedTickFormatMaxLength(
    paintingContext: LayerPaintingContext,
    style: AxisConfiguration,
  ) {
    //Calculate the max length of/for the tick labels. We assume that the first (for negative values) or the last one (for positive values) is probably the largest value.
    //Also ensure that we have at least 6 characters if a broken tick format is provided.

    @HotAllocation("Twice per layout pass per axis - formatted range start/end; the value-range bounds rarely change, CachedNumberFormat caches per value")
    val startTickFormatted: String = style.ticksFormat.format(contentAreaValueRange.start, paintingContext.i18nConfiguration)

    @HotAllocation("Twice per layout pass per axis - formatted range start/end; the value-range bounds rarely change, CachedNumberFormat caches per value")
    val endTickFormatted: String = style.ticksFormat.format(contentAreaValueRange.end, paintingContext.i18nConfiguration)

    @Hot
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
  @Hot
  fun calculateDomainStartEndValues(
    paintingContext: LayerPaintingContext,
    style: AxisConfiguration,
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
