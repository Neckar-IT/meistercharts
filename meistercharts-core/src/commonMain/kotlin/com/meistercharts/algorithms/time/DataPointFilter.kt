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

import com.meistercharts.annotations.Domain
import it.neckar.open.unit.si.ms

/**
 * Filters the data points to be able to draw only the relevant data points
 *
 */
class DataPointFilter<T>(
  @ms
  private val from: Double,
  @ms
  private val to: Double,
  @Domain
  private val minDomain: Double,
  @Domain
  private val maxDomain: Double,
  /**
   * The extract for the domain value
   */
  private val domainValueExtractor: DomainValueExtractor<T>
) {

  /**
   * Returns the data points between from and to (including).
   * Ensures the data points are within the visible domain area, too.
   *
   * Additionally one data point *before* and one *after* is added to ensure the lines/areas are painted out of the clip
   */
  fun findRelevantForPaint(allDataPoints: List<DataPoint<T>>): List<DataPoint<T>> {
    if (allDataPoints.size <= 1) {
      //Return if there is only one data point
      return allDataPoints
    }

    val selected = mutableListOf<DataPoint<T>>()

    var before = true
    //Start iterating a 1. We add one element more
    for (i in 1 until allDataPoints.size) {
      val dataPoint = allDataPoints[i]

      if (before) {
        //We are in before mode, no elements have yet been added

        if (isVisible(dataPoint)) {
          //We reached the required area
          selected.add(allDataPoints[i - 1])
          before = false
        } else {
          //We are still before
          continue
        }
      }

      //Ad the selected element. Event it is after to t

      selected.add(dataPoint)

      //Check if the last data point is greater than to
      if (dataPoint.time > to) {
        break
      }
    }

    return selected
  }

  /**
   * Returns true if the given data point is visible
   */
  private fun isVisible(dataPoint: DataPoint<T>): Boolean {
    @ms val time = dataPoint.time

    //Too early
    if (time < from) {
      return false
    }

    //Too late
    if (time > to) {
      return false
    }

    @Domain val domainValue = domainValueExtractor.extract(dataPoint)
    return domainValue >= minDomain && domainValue <= maxDomain
  }

  /**
   * Extracts the relevant domain values
   */
  fun interface DomainValueExtractor<T> {
    /**
     * Extracts the domain value from the given data point
     */
    fun extract(dataPoint: DataPoint<T>): Double
  }
}
