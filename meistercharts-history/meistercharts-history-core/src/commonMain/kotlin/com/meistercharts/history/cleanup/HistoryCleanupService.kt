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
package com.meistercharts.history.cleanup

import com.meistercharts.history.InMemoryHistoryStorage
import it.neckar.open.kotlin.lang.toIntCeil
import it.neckar.open.dispose.Disposable
import it.neckar.open.dispose.DisposeSupport
import it.neckar.open.unit.si.ms
import com.meistercharts.history.DeletionReport
import com.meistercharts.history.HistoryBucketRange
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

/**
 * Cleans the history
 */
class HistoryCleanupService(
  val historyStorage: InMemoryHistoryStorage
) : Disposable {

  private val disposeSupport = DisposeSupport()

  override fun dispose() {
    disposeSupport.dispose()
  }

  /**
   * Cleans old data from the given storage
   */
  fun cleanup(
    historyStorage: InMemoryHistoryStorage,
    /**
     * The history bucket range that is cleaned
     */
    historyBucketRange: HistoryBucketRange,
    /**
     * The max number of buckets that will remain in the storage
     */
    keptBucketsCount: Int
  ): DeletionReport {
    require(keptBucketsCount >= 0) { "Kept buckets count invalid <$keptBucketsCount>" }

    val latest = historyStorage.bookKeeping.latestBound(historyBucketRange) ?: return DeletionReport.empty

    val lastToDelete = latest.previous(keptBucketsCount)
    return historyStorage.deleteAndBefore(lastToDelete)
  }

  /**
   * Schedules the cleanup service for the given history storage
   */
  fun schedule(
    /**
     * The duration between two down cleanup runs.
     */
    cleanupDelay: Duration = 10_000.milliseconds
  ) {

    it.neckar.open.time.repeat(cleanupDelay) {
      cleanup(historyStorage, historyStorage.naturalSamplingPeriod.toHistoryBucketRange(), historyStorage.maxSizeConfiguration.keptBucketsCount)
    }.also { timerId ->
      disposeSupport.onDispose(timerId)
    }
  }
}

/**
 * A configuration for the history size
 */
data class MaxHistorySizeConfiguration(
  /**
   * The minimum number of buckets that are kept
   */
  val keptBucketsCount: Int = 100
) {

  /**
   * The number of time stamps that are held (at least)
   */
  fun getGuaranteedTimeStampsCount(historyBucketRange: HistoryBucketRange): Int {
    return historyBucketRange.entriesCount * keptBucketsCount
  }

  /**
   * The guaranteed duration in milliseconds
   */
  fun getGuaranteedDuration(historyBucketRange: HistoryBucketRange): @ms Double {
    return historyBucketRange.duration * keptBucketsCount
  }

  companion object {
    /**
     * Returns a max history size configuration that allows *at least* the given maximum number of time stamps
     */
    fun maxEntries(guaranteedTimestampsCount: Int, historyBucketRange: HistoryBucketRange): MaxHistorySizeConfiguration {
      require(guaranteedTimestampsCount > 0) {
        "Invalid maxTimestampsCount: $guaranteedTimestampsCount"
      }

      val bucketsCount = (guaranteedTimestampsCount / historyBucketRange.entriesCount.toDouble()).toIntCeil().coerceAtLeast(2)
      return MaxHistorySizeConfiguration(bucketsCount)
    }

    /**
     * Returns a config that spans *at least* the given duration.
     * This method returns at least 2.
     */
    fun forDuration(guaranteedDuration: Duration, historyBucketRange: HistoryBucketRange): MaxHistorySizeConfiguration {
      return forDuration(guaranteedDuration.toDouble(DurationUnit.MILLISECONDS), historyBucketRange)
    }

    /**
     * Returns a config that spans *at least* the given duration.
     * This method returns at least 2.
     */
    fun forDuration(guaranteedDuration: @ms Double, historyBucketRange: HistoryBucketRange): MaxHistorySizeConfiguration {
      val bucketsCount = (guaranteedDuration / historyBucketRange.duration).toIntCeil().coerceAtLeast(2)
      return MaxHistorySizeConfiguration(bucketsCount)
    }
  }
}
