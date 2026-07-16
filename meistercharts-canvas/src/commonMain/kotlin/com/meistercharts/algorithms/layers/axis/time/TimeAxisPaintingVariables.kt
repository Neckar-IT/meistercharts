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
package com.meistercharts.algorithms.layers.axis.time

import com.meistercharts.algorithms.layers.AxisConfiguration
import com.meistercharts.algorithms.layers.AxisPaintingVariables
import com.meistercharts.algorithms.layers.AxisPaintingVariablesImpl
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.time.TimeRange
import com.meistercharts.axis.time.DistanceYears
import com.meistercharts.axis.time.TimeTickDistance
import com.meistercharts.annotations.Domain
import com.meistercharts.axis.time.DistanceMillis
import it.neckar.open.unit.number.MayBeNaN
import com.meistercharts.canvas.layout.buffer.TickLabelsBuffer
import it.neckar.geometry.Side
import it.neckar.open.annotations.Hot
import it.neckar.open.unit.other.px
import it.neckar.open.unit.quantity.Time
import it.neckar.open.unit.si.ms
import kotlin.math.max
import kotlin.math.min

/**
 * Contains painting properties related to the time axis
 */
interface TimeAxisPaintingVariables : AxisPaintingVariables {
  /**
   * The content area time range
   */
  val contentAreaTimeRange: TimeRange

  /**
   * The smallest visible timestamp
   */
  val startTimestamp: @Domain @Time @ms Double

  /**
   * The largest visible timestamp
   */
  val endTimestamp: @Domain @Time @ms Double

  /**
   * The distance between the offset ticks
   */
  val offsetTickDistance: TimeTickDistance

  /**
   * The distance between the ticks (*not* the offset)
   */
  val tickDistance: TimeTickDistance

  /**
   * The offset ticks (domain values and formatted labels)
   */
  val offsetTickLabels: @Domain @ms TickLabelsBuffer

  /**
   * The ticks (domain values and formatted labels).
   * The value is [Double.NaN] for all ticks that should not be painted - because they are contained within [offsetTickLabels]
   */
  val tickLabels: @MayBeNaN @ms @Domain TickLabelsBuffer

}

/**
 * Default implementation for time axis painting variables
 */
abstract class TimeAxisPaintingVariablesImpl : AxisPaintingVariablesImpl(), TimeAxisPaintingVariables {
  override var contentAreaTimeRange: TimeRange = TimeRange.oneMinuteSinceReference

  /**
   * The smallest visible timestamp
   */
  override var startTimestamp: @Domain @Time @ms Double = 0.0

  /**
   * The largest visible timestamp
   */
  override var endTimestamp: @Time @Domain Double = 0.0


  /**
   * The ticks for the offset
   */
  override val offsetTickLabels: @Domain @ms TickLabelsBuffer = TickLabelsBuffer()

  /**
   * The distance between the offset ticks
   */
  override var offsetTickDistance: TimeTickDistance = DistanceYears.OneYear

  /**
   * The distance between the ticks (*not* the offset)
   */
  override var tickDistance: TimeTickDistance = DistanceYears.OneYear


  override val tickLabels: @MayBeNaN @ms @Domain TickLabelsBuffer = TickLabelsBuffer()

  @Hot
  override fun reset() {
    super.reset()

    this.contentAreaTimeRange = TimeRange.oneMinuteSinceReference

    startTimestamp = Double.NaN
    endTimestamp = Double.NaN

    offsetTickLabels.reset()
    offsetTickDistance = DistanceYears.OneYear

    tickDistance = DistanceMillis.smallest

    tickLabels.reset()
  }

  @Hot
  override fun calculateTickLabelsMaxWidthHorizontal(): @px Double {
    //TODO improve calculation somehow!
    return 100.0 //manually measured
  }

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
        relevantUpperDomain = chartCalculator.window2domainY(axisStart, contentAreaTimeRange)
        relevantLowerDomain = chartCalculator.window2domainY(axisEnd, contentAreaTimeRange)
      }

      Side.Top, Side.Bottom -> {
        relevantUpperDomain = chartCalculator.window2domainX(axisStart, contentAreaTimeRange)
        relevantLowerDomain = chartCalculator.window2domainX(axisEnd, contentAreaTimeRange)
      }
    }

    startTimestamp = min(relevantUpperDomain, relevantLowerDomain)
    endTimestamp = max(relevantUpperDomain, relevantLowerDomain)
  }
}
