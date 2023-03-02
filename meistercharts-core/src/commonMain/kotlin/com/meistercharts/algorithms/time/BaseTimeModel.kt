package com.meistercharts.algorithms.time

import com.meistercharts.algorithms.TimeRange
import it.neckar.open.unit.si.ms

/**
 * Base class for time model
 */
abstract class BaseTimeModel<T>(
  /**
   * The maximum amount of data points
   */
  val maxDataPointsCount: Int,

  /**
   * The expected distance between the data points
   */
  @ms
  val expectedDistanceBetweenDataPoints: Double
) {
  /**
   * Contains the data points (ordered by time)
   */
  private val dataPointsMutable: MutableList<DataPoint<T>> = ArrayList(maxDataPointsCount) //TODO replace with SortedList / BiTree

  val dataPoints: List<DataPoint<T>>
    get() = dataPointsMutable

  /**
   * Adds a new data point.
   *
   *
   * ATTENTION: The data points must be added in correct order. The list is not ordered automatically
   */
  fun addDataPoint(dataPoint: DataPoint<T>) {
    dataPointsMutable.add(dataPoint)
    ensureSize(dataPointsMutable, maxDataPointsCount)

    notifyUpdated(listOf(dataPoint))
  }

  fun setDataPoints(dataPoints: List<DataPoint<T>>) {
    dataPointsMutable.clear()
    dataPointsMutable.addAll(dataPoints)
    ensureSize(dataPointsMutable, maxDataPointsCount)

    notifyUpdated(dataPoints)
  }

  fun clear() {
    val oldList = dataPointsMutable.toList()
    dataPointsMutable.clear()
    notifyUpdated(oldList)
  }

  protected abstract fun notifyUpdated(dataPoints: List<DataPoint<T>>)

  fun isEmpty(): Boolean {
    return dataPoints.isEmpty()
  }

  @ms
  fun getMaxSpan(): Double {
    return expectedDistanceBetweenDataPoints * (maxDataPointsCount - 1)
  }

  /**
   * Deletes all elements until the max amount of entries are reached
   */
  private fun ensureSize(list: MutableList<DataPoint<T>>, maxSize: Int) {
    val removed = mutableListOf<DataPoint<T>>()
    while (list.size > maxSize) {
      removed.add(list.removeAt(0))
    }
    if (!removed.isEmpty()) {
      notifyUpdated(removed)
    }
  }

  /**
   * Creates a time range from smallest possible timestamp to the timestamp of the
   * latest data point (or the given timestamp if no data points are present)
   */
  fun createTimeRange(@ms now: Double): TimeRange {
    @ms val to = dataPoints.lastOrNull()?.time ?: now
    @ms val from = to - getMaxSpan()

    return TimeRange(from, to)
  }
}
