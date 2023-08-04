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

import assertk.*
import assertk.assertions.*
import com.meistercharts.time.TimeRange
import com.meistercharts.history.cleanup.MaxHistorySizeConfiguration
import com.meistercharts.history.downsampling.DownSamplingDirtyRangesCollector
import com.meistercharts.history.impl.createSinusChunk
import it.neckar.open.collections.fastForEach
import it.neckar.open.formatting.formatUtc
import it.neckar.open.test.utils.VirtualTime
import org.junit.jupiter.api.Test


internal class InMemoryBookKeepingTest {
  @Test
  fun testClear() {
    val bookKeeping = InMemoryBookKeeping()
    assertThat(bookKeeping.getTimeRange(HistoryBucketRange.HundredMillis)).isNull()
    assertThat(bookKeeping.earliestBound(HistoryBucketRange.HundredMillis)).isNull()
    assertThat(bookKeeping.latestBound(HistoryBucketRange.HundredMillis)).isNull()

    bookKeeping.store(HistoryBucketDescriptor.forTimestamp(1500000000000.0, HistoryBucketRange.HundredMillis))
    assertThat(bookKeeping.getTimeRange(HistoryBucketRange.HundredMillis)).isNotNull()
    assertThat(bookKeeping.earliestBound(HistoryBucketRange.HundredMillis)).isNotNull()
    assertThat(bookKeeping.latestBound(HistoryBucketRange.HundredMillis)).isNotNull()

    bookKeeping.clear()
    assertThat(bookKeeping.getTimeRange(HistoryBucketRange.HundredMillis)).isNull()
    assertThat(bookKeeping.earliestBound(HistoryBucketRange.HundredMillis)).isNull()
    assertThat(bookKeeping.latestBound(HistoryBucketRange.HundredMillis)).isNull()
  }

  @Test
  fun testFixBounds() {
    val timestamp = VirtualTime.defaultNow.also {
      assertThat(it.formatUtc()).isEqualTo("2021-03-27T21:45:23.002Z")
    }

    val bookKeeping = InMemoryBookKeeping()

    bookKeeping.store(HistoryBucketDescriptor.forTimestamp(timestamp, HistoryBucketRange.OneMinute))
    assertThat(bookKeeping.getTimeRange(HistoryBucketRange.OneMinute)).isNotNull()

    bookKeeping.clear()
    assertThat(bookKeeping.getTimeRange(HistoryBucketRange.OneMinute)).isNull()

    HistoryBucketDescriptor.forTimestamp(timestamp, HistoryBucketRange.OneMinute).let {
      bookKeeping.store(it)
      assertThat(bookKeeping.getTimeRange(HistoryBucketRange.OneMinute)).isNotNull()
      bookKeeping.remove(it)
      assertThat(bookKeeping.getTimeRange(HistoryBucketRange.OneMinute)).isNull()
    }
  }

  @Test
  fun testSwitchingSamplingPeriod() {
    val historyStorage = InMemoryHistoryStorage()

    var expectedSamplingPeriod = SamplingPeriod.EveryHundredMillis
    prepareHistoryStorage(historyStorage, expectedSamplingPeriod)
    addAndDownSample(historyStorage)
    historyStorage.clear()
    assertBookKeepingIsEmpty(historyStorage.bookKeeping)

    expectedSamplingPeriod = SamplingPeriod.EveryTenMillis
    prepareHistoryStorage(historyStorage, expectedSamplingPeriod)
    addAndDownSample(historyStorage)
    historyStorage.clear()
    assertBookKeepingIsEmpty(historyStorage.bookKeeping)

    expectedSamplingPeriod = SamplingPeriod.EveryTenSeconds
    prepareHistoryStorage(historyStorage, expectedSamplingPeriod)
    addAndDownSample(historyStorage)
    historyStorage.clear()
    assertBookKeepingIsEmpty(historyStorage.bookKeeping)
  }

  private fun prepareHistoryStorage(historyStorage: InMemoryHistoryStorage, expectedSamplingPeriod: SamplingPeriod) {
    historyStorage.apply {
      naturalSamplingPeriod = expectedSamplingPeriod
      maxSizeConfiguration = MaxHistorySizeConfiguration.forDuration(3600 * 1000.0, expectedSamplingPeriod.toHistoryBucketRange())
    }
  }

  private fun assertBookKeepingIsEmpty(bookKeeping: InMemoryBookKeeping) {
    SamplingPeriod.entries.fastForEach { samplingPeriod ->
      samplingPeriod.toHistoryBucketRange().let { historyBucketRange ->
        assertThat(bookKeeping.earliestBound(historyBucketRange)).isNull()
        assertThat(bookKeeping.latestBound(historyBucketRange)).isNull()
      }
    }
  }

  private fun addAndDownSample(historyStorage: InMemoryHistoryStorage) {
    val samplingPeriod: SamplingPeriod = SamplingPeriod.EveryHundredMillis
    val startTimestamp = VirtualTime.defaultNow.also {
      assertThat(it.formatUtc()).isEqualTo("2021-03-27T21:45:23.002Z")
    }

    val descriptor = HistoryBucketDescriptor.forTimestamp(startTimestamp, samplingPeriod)
    createSinusChunk(descriptor).let { chunk ->
      historyStorage.storeWithoutCache(HistoryBucket(descriptor, chunk), HistoryUpdateInfo.fromChunk(chunk, samplingPeriod))
    }
    historyStorage.query(descriptor.start, descriptor.start, descriptor.bucketRange.samplingPeriod).let {
      assertThat(it).hasSize(1)
    }

    val endTimestamp = descriptor.end.also {
      assertThat(it.formatUtc()).isEqualTo("2021-03-27T21:46:00.000Z")
    }

    val downSamplingService = historyStorage.downSamplingService
    downSamplingService.calculateDownSamplingIfRequired(DownSamplingDirtyRangesCollector().also {
      it.markAsDirty(samplingPeriod, TimeRange(startTimestamp, endTimestamp))
    })
  }
}
