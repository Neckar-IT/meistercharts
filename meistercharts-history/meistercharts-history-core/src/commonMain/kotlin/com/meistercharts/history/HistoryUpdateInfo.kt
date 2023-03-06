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
import com.meistercharts.algorithms.TimeRanges
import com.meistercharts.history.impl.HistoryChunk
import kotlinx.serialization.Serializable

/**
 * Describes updates to the history
 */
@Serializable
data class HistoryUpdateInfo(
  /**
   * The updated sampling period
   */
  val samplingPeriod: SamplingPeriod,

  /**
   * The time ranges that have been updated
   */
  val updatedTimeRanges: TimeRanges
) {

  constructor(
    samplingPeriod: SamplingPeriod,
    vararg updatedTimeRange: TimeRange
  ) : this(samplingPeriod, TimeRanges.createMerged(*updatedTimeRange))

  /**
   * Appends the given update
   */
  fun merge(other: TimeRange): HistoryUpdateInfo {
    return HistoryUpdateInfo(samplingPeriod, updatedTimeRanges.merge(other))
  }

  companion object {
    /**
     * Creates an update info that uses start and end of the given chunk
     */
    fun fromChunk(chunk: HistoryChunk, samplingPeriod: SamplingPeriod): HistoryUpdateInfo {
      return HistoryUpdateInfo(samplingPeriod, TimeRanges.of(TimeRange(chunk.start, chunk.end)))
    }

    fun from(descriptor: HistoryBucketDescriptor): HistoryUpdateInfo {
      return HistoryUpdateInfo(descriptor.bucketRange.samplingPeriod, descriptor.timeRange)
    }
  }
}

