package com.meistercharts.algorithms.layers

import com.meistercharts.algorithms.TimeRange
import com.meistercharts.algorithms.axis.DistanceYears
import com.meistercharts.algorithms.axis.TimeTickDistance
import com.meistercharts.annotations.Domain
import it.neckar.open.unit.number.MayBeNaN
import com.meistercharts.canvas.layout.cache.DoubleCache
import com.meistercharts.canvas.layout.cache.StringCache
import com.meistercharts.model.Side
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
   * The domain values for the offset ticks
   */
  val offsetTickDomainValues: @Domain @ms DoubleCache

  /**
   * The formatted values for the offset ticks
   */
  val offsetTicksFormatted: @Domain @ms StringCache

  /**
   * The domain values for the ticks.
   * Contains [Double.NaN] for all ticks that should not be painted - because they are contained within [offsetTickDomainValues]
   */
  val tickDomainValues: @MayBeNaN @ms @Domain DoubleCache

  /**
   * The formatted ticks (same size as [tickDomainValues])
   */
  val ticksFormatted: StringCache

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
  override var offsetTickDomainValues: @Domain @ms DoubleCache = DoubleCache()

  override var offsetTicksFormatted: @Domain @ms StringCache = StringCache()

  /**
   * The distance between the offset ticks
   */
  override var offsetTickDistance: TimeTickDistance = DistanceYears.OneYear


  override var tickDomainValues: @MayBeNaN @ms @Domain DoubleCache = DoubleCache()

  override val ticksFormatted: StringCache = StringCache()

  override fun reset() {
    super.reset()

    this.contentAreaTimeRange = TimeRange.oneMinuteSinceReference

    startTimestamp = Double.NaN
    endTimestamp = Double.NaN

    offsetTickDomainValues.reset()
    offsetTicksFormatted.reset()
    offsetTickDistance = DistanceYears.OneYear

    tickDomainValues.reset()
    ticksFormatted.reset()
  }

  override fun calculateTickLabelsMaxWidthHorizontal(): @px Double {
    //TODO improve calculation somehow!
    return 100.0 //manually measured
  }

  fun calculateDomainStartEndValues(
    paintingContext: LayerPaintingContext,
    style: AxisStyle,
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
