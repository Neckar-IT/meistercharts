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

import assertk.*
import assertk.assertions.*
import com.meistercharts.history.HistoryBucket
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryUpdateInfo
import com.meistercharts.history.InMemoryBookKeeping
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.impl.createSinusChunk
import it.neckar.open.kotlin.lang.fastFor
import it.neckar.open.formatting.formatUtc
import it.neckar.open.test.utils.VirtualTime
import org.junit.jupiter.api.Test

class HistoryCleanupServiceTest {
  @Test
  fun testBasics() {
    val historyStorage = InMemoryHistoryStorage()

    val cleanupService = historyStorage.historyCleanupService
    val bookKeeping: InMemoryBookKeeping = historyStorage.bookKeeping

    val start = VirtualTime.defaultNow
    val samplingPeriod = SamplingPeriod.EveryHundredMillis
    val historyBucketRange = samplingPeriod.toHistoryBucketRange()

    var descriptor = HistoryBucketDescriptor.forTimestamp(start, samplingPeriod)

    10.fastFor {
      createSinusChunk(descriptor).let { chunk ->
        historyStorage.storeWithoutCache(HistoryBucket(descriptor, chunk), HistoryUpdateInfo.fromChunk(chunk, samplingPeriod))
      }

      historyStorage.query(descriptor.start, descriptor.start, descriptor.bucketRange.samplingPeriod).let {
        assertThat(it).hasSize(1)
      }

      //Next descriptor
      descriptor = descriptor.next()
    }

    //There should be 10 buckets
    assertThat(historyStorage.query(start, descriptor.start, samplingPeriod)).hasSize(10)
    assertThat(bookKeeping.earliestBound(historyBucketRange)?.start?.formatUtc()).isEqualTo("2021-03-27T21:45:00.000Z")

    //Cleaning up all but one bucket
    cleanupService.cleanup(historyStorage, historyBucketRange, 1).let {
      assertThat(it.deletedDescriptors).hasSize(9)
    }
    assertThat(bookKeeping.earliestBound(historyBucketRange)?.start?.formatUtc()).isEqualTo("2021-03-27T21:54:00.000Z")

    //Just one bucket is remaining
    assertThat(historyStorage.query(start, descriptor.start, samplingPeriod)).hasSize(1)
  }
}
