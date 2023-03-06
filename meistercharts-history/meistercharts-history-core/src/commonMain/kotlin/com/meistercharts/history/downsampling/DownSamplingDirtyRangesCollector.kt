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

import com.meistercharts.algorithms.TimeRange
import com.meistercharts.algorithms.TimeRanges
import com.meistercharts.history.ObservableHistoryStorage
import com.meistercharts.history.SamplingPeriod

/**
 * A service that registers itself at a history access / storage and automatically calculates the down sampling if necessary
 */
class DownSamplingDirtyRangesCollector {
  /**
   * Contains all "dirty" time ranges for a given sampling period
   */
  private val dirtyTimeRanges: MutableMap<SamplingPeriod, TimeRanges> = mutableMapOf()

  /**
   * Marks the given time range as dirty
   */
  fun markAsDirty(samplingPeriod: SamplingPeriod, additionalDirtyTimeRange: TimeRange) {
    markAsDirty(samplingPeriod, TimeRanges.of(additionalDirtyTimeRange))
  }

  /**
   * Marks the given time range as dirty
   */
  fun markAsDirty(samplingPeriod: SamplingPeriod, additionalDirtyTimeRanges: TimeRanges) {
    val currentTimeRanges = dirtyTimeRanges[samplingPeriod] ?: TimeRanges.empty
    val merged = currentTimeRanges.merge(additionalDirtyTimeRanges, samplingPeriod.distance)

    this.dirtyTimeRanges[samplingPeriod] = merged
  }

  /**
   * Removes all dirty time ranges for the given [samplingPeriod] and returns them
   */
  fun remove(samplingPeriod: SamplingPeriod): TimeRanges? {
    return dirtyTimeRanges.remove(samplingPeriod)
  }

  /**
   * Returns the dirty time ranges for the given sampling period
   */
  operator fun get(samplingPeriod: SamplingPeriod): TimeRanges? {
    return dirtyTimeRanges[samplingPeriod]
  }
}

/**
 * Observes the given history storage and marks relevant areas as dirty
 */
fun DownSamplingDirtyRangesCollector.observe(historyStorage: ObservableHistoryStorage) {
  historyStorage.observe { _, updateInfo ->
    updateInfo.samplingPeriod.above()?.let {
      markAsDirty(it, updateInfo.updatedTimeRanges)
    }
  }
}
