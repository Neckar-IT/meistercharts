/*
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

import com.meistercharts.history.ObservableHistoryStorage
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.time.TimeRange
import com.meistercharts.time.TimeRanges

/**
 * Collects the time ranges whose down sampled data has become stale, keyed by the sampling period that has to be recalculated.
 *
 * This is a pure book keeping structure: it neither observes a history storage nor calculates anything.
 * [observe] attaches it to a storage, [DownSamplingService] does the recalculation.
 */
class DownSamplingDirtyRangesCollector {
  /**
   * Contains all "dirty" time ranges for a given sampling period.
   * A sampling period without an entry has nothing to recalculate.
   */
  private val dirtyTimeRanges: MutableMap<SamplingPeriod, TimeRanges> = mutableMapOf()

  /**
   * Marks the given time range as dirty
   */
  fun markAsDirty(samplingPeriod: SamplingPeriod, additionalDirtyTimeRange: TimeRange) {
    markAsDirty(samplingPeriod, TimeRanges.of(additionalDirtyTimeRange))
  }

  /**
   * Marks the given time ranges as dirty.
   *
   * Ranges separated by at most one sample distance of [samplingPeriod] are merged into a single range. Recalculating
   * across such a gap costs at most one additional sample and keeps the number of ranges from growing with every update.
   */
  fun markAsDirty(samplingPeriod: SamplingPeriod, additionalDirtyTimeRanges: TimeRanges) {
    val currentTimeRanges = dirtyTimeRanges[samplingPeriod] ?: TimeRanges.empty
    val merged = currentTimeRanges.merge(additionalDirtyTimeRanges, samplingPeriod.distance)

    this.dirtyTimeRanges[samplingPeriod] = merged
  }

  /**
   * Removes all dirty time ranges for the given [samplingPeriod] and returns them - null if there is nothing dirty.
   *
   * Claims the work: the caller is responsible for recalculating the returned ranges, they are forgotten here.
   * Use [get] to look at the dirty ranges without taking them.
   */
  fun remove(samplingPeriod: SamplingPeriod): TimeRanges? {
    return dirtyTimeRanges.remove(samplingPeriod)
  }

  /**
   * Returns the dirty time ranges for the given sampling period - null if there is nothing dirty.
   * Leaves them in place; [remove] is what takes them.
   */
  operator fun get(samplingPeriod: SamplingPeriod): TimeRanges? {
    return dirtyTimeRanges[samplingPeriod]
  }
}

/**
 * Observes the given history storage and marks relevant areas as dirty.
 *
 * An update is recorded for the sampling period *above* the updated one: that is the level whose down sampled data was
 * calculated from the new values. The coarsest sampling period has nothing above it, so updates there are dropped.
 */
fun DownSamplingDirtyRangesCollector.observe(historyStorage: ObservableHistoryStorage) {
  historyStorage.observe { updateInfo ->
    updateInfo.samplingPeriod.above()?.let {
      markAsDirty(it, updateInfo.updatedTimeRanges)
    }
  }
}
