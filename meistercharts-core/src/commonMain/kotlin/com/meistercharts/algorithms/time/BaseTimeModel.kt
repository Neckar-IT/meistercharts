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
