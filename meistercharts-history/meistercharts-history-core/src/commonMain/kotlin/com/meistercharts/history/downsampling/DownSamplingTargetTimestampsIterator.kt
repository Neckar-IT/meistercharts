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
package com.meistercharts.history.downsampling

import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.TimestampIndex
import it.neckar.open.unit.si.ms

/**
 * Manages the target time stamps for down sampling calculations.
 *
 */
class DownSamplingTargetTimestampsIterator(
  /**
   * The (center) time stamps that are iterated
   */
  val timeStamps: @ms DoubleArray,
  /**
   * The distance between two timestamps for each slot
   */
  val distance: @ms Double
) {

  private var _index: Int = 0

  /**
   * The current index for the timestamp
   */
  val index: TimestampIndex
    get() {
      return TimestampIndex(_index)
    }

  /**
   * The center of the current slot
   */
  val slotCenter: @ms Double
    get() {
      return timeStamps[_index]
    }

  /**
   * The start of the current slot
   */
  val slotStart: @ms Double
    get() = slotCenter - distance / 2.0

  /**
   * The end timestamp for the current slot
   */
  val slotEnd: @ms Double
    get() = slotCenter + distance / 2.0

  fun next() {
    _index++

    require(index < timeStamps.size) { "Invalid call to next. Index is now <$index> but timestamps size is <${timeStamps.size}>" }
  }

  companion object {
    /**
     * Returns the down sampling iterator for the given descriptor.
     */
    fun create(sourceDescriptor: HistoryBucketDescriptor): DownSamplingTargetTimestampsIterator {
      return DownSamplingTargetTimestampsIterator(sourceDescriptor.calculateTimeStamps(), sourceDescriptor.bucketRange.distance)
    }
  }
}
