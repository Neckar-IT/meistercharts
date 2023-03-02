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
    assertThat(bookKeeping.earliestBound(historyBucketRange)?.start?.formatUtc()).isEqualTo("2021-03-27T21:45:00.000")

    //Cleaning up all but one bucket
    cleanupService.cleanup(historyStorage, historyBucketRange, 1).let {
      assertThat(it.deletedDescriptors).hasSize(9)
    }
    assertThat(bookKeeping.earliestBound(historyBucketRange)?.start?.formatUtc()).isEqualTo("2021-03-27T21:54:00.000")

    //Just one bucket is remaining
    assertThat(historyStorage.query(start, descriptor.start, samplingPeriod)).hasSize(1)
  }
}
