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

import com.meistercharts.time.TimeRange
import com.meistercharts.time.TimeRanges
import com.meistercharts.history.HistoryBucket
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryBucketRange
import com.meistercharts.history.HistoryUpdateInfo
import com.meistercharts.history.ObservableHistoryStorage
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.WritableHistoryStorage
import it.neckar.open.collections.fastForEach
import it.neckar.open.collections.fastMap
import it.neckar.open.dispose.Disposable
import it.neckar.open.dispose.DisposeSupport
import it.neckar.open.unit.other.Sorted
import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.logging.debug
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Updates the down sampling
 */
class DownSamplingService(val historyStorage: WritableHistoryStorage) : Disposable {
  private val disposeSupport: DisposeSupport = DisposeSupport()

  override fun dispose() {
    disposeSupport.dispose()
  }

  /**
   * Recalculates the down sampling for a sampling periods
   */
  fun recalculateDownSampling(dirtyRangesCollector: DownSamplingDirtyRangesCollector) {
    SamplingPeriod.entries.fastForEach {
      val dirtyTimeRanges = dirtyRangesCollector.remove(it) ?: return@fastForEach
      recalculateDownSampling(dirtyTimeRanges, it.toHistoryBucketRange())
    }
  }

  /**
   * Recalculates the down sampling for that specific sampling period
   */
  fun recalculateDownSampling(timeRanges: TimeRanges, historyBucketRange: HistoryBucketRange) {
    if (timeRanges.isEmpty()) {
      return
    }

    val jobs = createJobs(timeRanges, historyBucketRange)

    jobs.fastForEach { job ->
      logger.debug{"Running Job $job"}

      val historyUpdateInfo: HistoryUpdateInfo
      val newHistoryBucket: HistoryBucket

      when (job.refreshRange) {
        RefreshCompletely -> {
          //All descriptors for the children
          val childDescriptors = job.descriptor.children()
          val childBuckets = historyStorage.get(childDescriptors)

          if (childBuckets.isEmpty()) {
            //No child buckets have been found
            historyStorage.delete(job.descriptor)
            return@fastForEach
          }

          newHistoryBucket = job.descriptor.calculateDownSampled(childBuckets)
          val samplingPeriod = newHistoryBucket.descriptor.bucketRange.samplingPeriod

          historyUpdateInfo = HistoryUpdateInfo.fromChunk(newHistoryBucket.chunk, samplingPeriod)
        }

        is RefreshPartially -> {
          val original = historyStorage.get(job.descriptor)

          //TODO FIX ME! Use optimized calculation!
          val childDescriptors = job.descriptor.children()
          val childBuckets = historyStorage.get(childDescriptors)
          if (childBuckets.isEmpty()) {
            //No child buckets have been found
            historyStorage.delete(job.descriptor)
            return@fastForEach
          }

          newHistoryBucket = job.descriptor.calculateDownSampled(childBuckets)

          val samplingPeriod = newHistoryBucket.descriptor.bucketRange.samplingPeriod
          historyUpdateInfo = HistoryUpdateInfo(samplingPeriod, job.refreshRange.timeRanges)
        }
      }

      historyStorage.storeWithoutCache(newHistoryBucket, historyUpdateInfo)
    }
  }

  /**
   * Creates down sampling jobs for the given time ranges and bucket range
   */
  fun createJobs(dirtyRanges: TimeRanges, historyBucketRange: HistoryBucketRange): @Sorted List<DownSamplingJob> {
    logger.debug{ "Creating jobs for dirtyRanges: $dirtyRanges"}

    return dirtyRanges.flatMap { timeRange ->
      //Compute the descriptors that contain any part of the given time range.
      //If a user adds many samples at once that don't quite match the natural sampling period of the history we may exceed the MaxSupportedDescriptorsCount.
      //So we pass 10000 here to cover the complete dirty time-range. 10000 could still be too small but should suffice for the most scenarios until a more
      //elaborate implementation is available.
      val descriptorsToRecalculate = HistoryBucketDescriptor.forRange(timeRange.start, timeRange.end, historyBucketRange, true, 10000)
      DownSamplingJob.create(descriptorsToRecalculate, timeRange)
    }
      .toSet()
      .sortedBy {
        it.descriptor.start
      }.also {
        logger.debug {
          "Created ${it.size} jobs"
        }
      }
  }

  /**
   * Collects the down sampling dirty ranges.
   * Is added as observer to the history storage in [scheduleDownSampling]
   */
  val dirtyRangesCollector: DownSamplingDirtyRangesCollector = DownSamplingDirtyRangesCollector()

  private var downSamplingScheduled = false

  /**
   * Schedules the down sampling.
   * Must only be called once!
   */
  fun scheduleDownSampling(
    historyStorage: ObservableHistoryStorage,
    /**
     * The duration between two down sampling calculations.
     * Down sampling is only calculated if necessary
     */
    downSamplingDelay: Duration = 500.milliseconds,
  ) {
    require(downSamplingScheduled.not()) {
      "Down sampling already scheduled"
    }

    dirtyRangesCollector.observe(historyStorage)
    scheduleDownSampling(dirtyRangesCollector, downSamplingDelay)

    downSamplingScheduled = true
  }

  /**
   * Schedules the downsampling using a timer.
   */
  fun scheduleDownSampling(
    downSamplingDirtyRangesCollector: DownSamplingDirtyRangesCollector,
    /**
     * The duration between two down sampling calculations.
     * Down sampling is only calculated if necessary
     */
    downSamplingDelay: Duration = 500.milliseconds,
  ) {
    it.neckar.open.time.repeat(downSamplingDelay) {
      //Check for each sampling period if there is work to do
      calculateDownSamplingIfRequired(downSamplingDirtyRangesCollector)
    }.also { timerId ->
      disposeSupport.onDispose(timerId)
    }
  }

  /**
   * Calculates down sampling for all levels where down sampling is required
   */
  fun calculateDownSamplingIfRequired(
    downSamplingDirtyRangesCollector: DownSamplingDirtyRangesCollector = dirtyRangesCollector,
  ) {
    SamplingPeriod.entries.fastForEach { samplingPeriod ->
      val dirtyTimeRanges = downSamplingDirtyRangesCollector[samplingPeriod] ?: return@fastForEach

      if (isDownSamplingRequired(dirtyTimeRanges, samplingPeriod)) {
        val dirtyRanges = downSamplingDirtyRangesCollector.remove(samplingPeriod) ?: return@fastForEach
        this.recalculateDownSampling(dirtyRanges, samplingPeriod.toHistoryBucketRange())
      }
    }
  }

  /**
   * Returns true if down sampling is required for the given sampling period
   */
  private fun isDownSamplingRequired(dirtyTimeRanges: TimeRanges, samplingPeriod: SamplingPeriod): Boolean {
    return dirtyTimeRanges.span >= samplingPeriod.distance / 2.0
  }

  companion object{
    private val logger: Logger = LoggerFactory.getLogger("com.meistercharts.history.downsampling.DownSamplingService")
  }
}

/**
 * A down sampling job - consists of a descriptor that has to be recalculated and
 * the time ranges that have changed.
 */
data class DownSamplingJob(
  val descriptor: HistoryBucketDescriptor,

  /**
   * What parts of the descriptor need to be refreshed
   */
  val refreshRange: RefreshRange
) {

  init {
    if (refreshRange is RefreshPartially) {
      require(!refreshRange.timeRanges.isEmpty()) {
        "Time ranges must not be empty"
      }

      //Ensure the time ranges only span
      refreshRange.timeRanges.fastForEach {
        require(descriptor.contains(it.start)) {
          "Invalid start of time range: $it for descriptor $descriptor"
        }
        require(descriptor.end >= it.end) {
          "Invalid end of time range: $it for descriptor $descriptor"
        }
      }
    }
  }

  companion object {
    /**
     * Creates down sampling jobs for the given descriptors and time range
     */
    fun create(forRange: List<HistoryBucketDescriptor>, timeRange: TimeRange): List<DownSamplingJob> {
      return forRange.fastMap {
        val refreshRange: RefreshRange =
          if (timeRange.contains(it.start, it.end)) {
            //Refresh the complete descriptor
            RefreshCompletely
          } else {
            RefreshPartially(TimeRanges.of(timeRange.fitWithin(it.start, it.end)))
          }

        DownSamplingJob(it, refreshRange)
      }
    }
  }
}


/**
 * Describes the range that has to be updated
 */
sealed class RefreshRange

/**
 * The complete descriptor has to be recalculated.
 */
data object RefreshCompletely : RefreshRange()

/**
 * Only parts of the descriptor need to be refreshed
 */
data class RefreshPartially(
  /**
   * The time ranges that should be recalculated.
   */
  val timeRanges: TimeRanges
) : RefreshRange()
