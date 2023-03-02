package com.meistercharts.algorithms.time

import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.algorithms.ChartState
import com.meistercharts.algorithms.TimeRange
import com.meistercharts.algorithms.withZoom
import com.meistercharts.annotations.Domain
import com.meistercharts.model.Zoom
import it.neckar.open.unit.si.ms

/**
 * Describes the distance in milliseconds between two successive data points
 */
interface DataPointDistance {
  val description: String
  /**
   * The distance between two data points in milliseconds
   */
  @ms
  val distance: Double

  /**
   * Returns the next greater distance (or null if there is none)
   */
  fun getNextGreaterDistance(): DataPointDistance?

  /**
   * Returns the next smaller distance (or null if there is none)
   */
  fun getPreviousSmallerDistance(): DataPointDistance?
}

/**
 * Common distances
 */
enum class DataPointDistances(
  override val description: String,
  @ms override val distance: Double
) : DataPointDistance {
  /**
   * 1 data point every 20 milliseconds
   */
  TwentyMilliseconds("20ms", 20.0),
  /**
   * 1 data point every 100 milliseconds
   */
  HundredMilliseconds("100ms", 100.0),
  /**
   * 1 data point per sec
   */
  Second("1sec", 1000.0),
  /**
   * 1 data point per 10 sec
   */
  TenSeconds("10sec", 10 * 1000.0),
  /**
   * 1 data point per minute
   */
  Minute("1min", 60 * 1000.0),
  /**
   * 1 data point per 10 minutes
   */
  TenMinutes("10min", 10 * 60 * 1000.0),
  /**
   * 1 data point per hour
   */
  Hour("1h", 60 * 60 * 1000.0);

  override fun getNextGreaterDistance(): DataPointDistance? {
    val allDistances = values()
    val index = allDistances.indexOf(this)
    return if (index < allDistances.size - 1) allDistances[index + 1] else null
  }

  override fun getPreviousSmallerDistance(): DataPointDistance? {
    val allDistances = values()
    val index = allDistances.indexOf(this)
    return if (index > 0) allDistances[index - 1] else null
  }

  companion object {
    /**
     * The value of [DataPointDistances] with the greatest distance
     */
    val greatestDistance: DataPointDistance = values().last()

    /**
     * The value of [DataPointDistances] with the smallest distance
     */
    val smallestDistance: DataPointDistance = values().first()

    /**
     * The values of [DataPointDistances] ordered by their distance ascending
     */
    val valuesAscending = values()

    /**
     * The values of [DataPointDistances] ordered by their distance descending
     */
    val valuesDescending = values().reversedArray()

    /**
     * Computes the [DataPointDistance] that matches best the given [zoom] and [timeRange]
     * @param zoom the zoom to calculate the data-point distance for
     * @param timeRange the domain time-range
     * @param chartState needed for the computations
     */
    fun fromZoom(zoom: Zoom, @Domain timeRange: TimeRange, chartState: ChartState): DataPointDistance {
      val chartCalculatorZoomOverride = ChartCalculator(chartState.withZoom(zoom))
      @ms val spanFor1Pixel = timeRange.relative2timeDelta(chartCalculatorZoomOverride.zoomed2domainRelativeX(1.0))

      val distancesAscending = valuesAscending
      for (dataPointDistance in distancesAscending) {
        if (dataPointDistance.distance >= spanFor1Pixel) {
          return dataPointDistance
        }
      }
      return distancesAscending.last()
    }
  }
}

