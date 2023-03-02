package com.meistercharts.algorithms.tile

import com.meistercharts.algorithms.TimeRange
import com.meistercharts.annotations.Zoomed
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.model.Size
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.ms

/**
 * Provides the sampling period that is used to render the history
 */
fun interface SamplingPeriodCalculator {
  /**
   * Returns the ideal period for the given canvas/tile size
   */
  fun calculateSamplingPeriod(
    /**
     * The visible time range for the canvas
     */
    visibleTimeRange: TimeRange,
    /**
     * The size of the canvas
     */
    canvasSize: @Zoomed Size
  ): SamplingPeriod
}

/**
 * Ensures the max distance between two data points.
 * This implementation is useful for [AverageHistoryCanvasTilePainter]
 */
class MaxDistanceSamplingPeriodCalculator(
  /**
   * The number of pixels per data point (maximum value)
   */
  val maxDistanceBetweenDataPoints: Double = 3.0
) : SamplingPeriodCalculator {
  override fun calculateSamplingPeriod(visibleTimeRange: TimeRange, canvasSize: @Zoomed Size): SamplingPeriod {
    //The ideal time stamp count - one time stamp every three pixels
    val idealTimestampCount = canvasSize.width / maxDistanceBetweenDataPoints

    //The ideal distance between two time stamps
    @ms val idealDistance = visibleTimeRange.span / idealTimestampCount

    return SamplingPeriod.withMaxDistance(idealDistance)
  }
}

/**
 * Ensures the min distance between two data points.
 * This implementation is useful for [CandleHistoryCanvasTilePainter]
 */
class MinDistanceSamplingPeriodCalculator(
  /**
   * The number of pixels per data point
   */
  val minDistanceBetweenDataPoints: @px Double = 5.0,
) : SamplingPeriodCalculator {
  override fun calculateSamplingPeriod(visibleTimeRange: TimeRange, canvasSize: @Zoomed Size): SamplingPeriod {
    //The ideal time stamp count - one time stamp every three pixels
    val idealTimestampCount = canvasSize.width / minDistanceBetweenDataPoints

    //The ideal distance between two time stamps
    @ms val idealDistance = visibleTimeRange.span / idealTimestampCount

    return SamplingPeriod.withMinDistance(idealDistance)
  }
}

/**
 * Ensures a minimum sampling period
 */
class MinSamplingPeriodCalculator(
  val delegate: SamplingPeriodCalculator,
  val minimum: () -> SamplingPeriod
) : SamplingPeriodCalculator {
  override fun calculateSamplingPeriod(visibleTimeRange: TimeRange, canvasSize: Size): SamplingPeriod {
    val preferredSamplingPeriod = delegate.calculateSamplingPeriod(visibleTimeRange, canvasSize)
    return maxOf(preferredSamplingPeriod, minimum())
  }
}

/**
 * Wraps this provider - returns no shorter sampling period than [minimum].
 */
fun SamplingPeriodCalculator.withMinimum(minimum: SamplingPeriod): MinSamplingPeriodCalculator {
  return withMinimum { minimum }
}

fun SamplingPeriodCalculator.withMinimum(minimum: () -> SamplingPeriod): MinSamplingPeriodCalculator {
  return MinSamplingPeriodCalculator(this, minimum)
}
