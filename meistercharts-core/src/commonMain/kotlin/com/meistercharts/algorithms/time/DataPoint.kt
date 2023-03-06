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

import it.neckar.open.unit.si.ms

/**
 * Contains the data for one point in time
 */
data class DataPoint<T>(
  /**
   * The time for the data point in milliseconds
   */
  @ms
  val time: Double,
  /**
   * The value for that given point in time
   */
  val value: T
) : Comparable<DataPoint<T>> {

  override fun compareTo(other: DataPoint<T>): Int {
    return when {
      time > other.time  -> 1
      time == other.time -> 0
      else               -> -1
    }
  }
}


