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
package com.meistercharts.history

import com.meistercharts.algorithms.TimeRange
import it.neckar.open.formatting.formatUtc
import it.neckar.open.unit.other.Exclusive
import it.neckar.open.unit.other.Inclusive
import it.neckar.open.unit.si.ms
import com.meistercharts.history.impl.HistoryChunk

/**
 * Represents a container for history data that has clear borders that can be calculated in constant time.
 * The borders are placed on "even" borders depending on the resolution of the given descriptor
 *
 * This is a high level object that should be used when working with the history.
 *
 */
data class HistoryBucket(
  /**
   * The descriptor of the bucket
   */
  val descriptor: HistoryBucketDescriptor,
  /**
   * The history chunk
   */
  val chunk: HistoryChunk,
) {

  val bucketRange: HistoryBucketRange
    get() {
      return descriptor.bucketRange
    }

  /**
   * The sampling period
   */
  val samplingPeriod: SamplingPeriod
    get() {
      return bucketRange.samplingPeriod
    }

  /**
   * The start of the bucket
   */
  @Inclusive
  val start: @ms Double
    get() {
      return descriptor.start
    }

  /**
   * The end of the bucket - exclusive
   */
  @Exclusive
  val end: @ms Double
    get() {
      return descriptor.end
    }

  init {
    if (!chunk.isEmpty()) {
      require(chunk.start >= descriptor.start) {
        "Invalid chunk start. Was <${chunk.start.formatUtc()}> but expected at least <${descriptor.start.formatUtc()}>"
      }
      require(chunk.end < descriptor.end) {
        "Invalid chunk end. Was <${chunk.end.formatUtc()}> but expected less than <${descriptor.end.formatUtc()}>"
      }
    }
  }

  /**
   * Returns true if this bucket contains data for the given time range
   */
  fun overlaps(timeRange: TimeRange): Boolean {
    return start < timeRange.end && end > timeRange.start
  }

  /**
   * Returns true if the provided timestamp lays within the time range of this bucket
   */
  fun contains(timestamp: @ms Double): Boolean {
    return start <= timestamp && end > timestamp
  }

  override fun toString(): String {
    return "HistoryBucket(descriptor=$descriptor)"
  }
}

