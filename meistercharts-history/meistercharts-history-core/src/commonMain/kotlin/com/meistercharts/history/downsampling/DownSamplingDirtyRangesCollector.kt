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
