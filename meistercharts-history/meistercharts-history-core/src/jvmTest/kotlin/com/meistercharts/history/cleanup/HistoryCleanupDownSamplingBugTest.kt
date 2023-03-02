package com.meistercharts.history.cleanup

import assertk.*
import assertk.assertions.*
import com.meistercharts.algorithms.TimeRange
import com.meistercharts.algorithms.TimeRanges
import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.HistoryBucket
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryBucketRange
import com.meistercharts.history.HistoryUpdateInfo
import com.meistercharts.history.InMemoryBookKeeping
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.TimestampIndex
import com.meistercharts.history.downsampling.RefreshPartially
import com.meistercharts.history.impl.createSinusChunk
import it.neckar.open.kotlin.lang.fastFor
import it.neckar.open.formatting.formatUtc
import it.neckar.open.test.utils.VirtualTime
import it.neckar.open.test.utils.isNaN
import org.junit.jupiter.api.Test

/**
 *
 */
class HistoryCleanupDownSamplingBugTest {
  @Test
  fun testIt() {
    val historyStorage = InMemoryHistoryStorage()

    val cleanupService = historyStorage.historyCleanupService
    val bookKeeping: InMemoryBookKeeping = historyStorage.bookKeeping
    val downSamplingService = historyStorage.downSamplingService


    val firstDescriptorStart = VirtualTime.defaultNow.also {
      assertThat(it.formatUtc()).isEqualTo("2021-03-27T21:45:23.002")
    }

    val samplingPeriod = SamplingPeriod.EveryHundredMillis
    val historyBucketRange = samplingPeriod.toHistoryBucketRange()

    var descriptor = HistoryBucketDescriptor.forTimestamp(firstDescriptorStart, samplingPeriod)

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

    //End of the last descriptor
    val lastDescriptorEnd = descriptor.end.also {
      assertThat(it.formatUtc()).isEqualTo("2021-03-27T21:56:00.000")
    }

    //There should be 10 buckets
    assertThat(historyStorage.query(firstDescriptorStart, descriptor.start, samplingPeriod)).hasSize(10)

    //ensure no down sampling has happened
    assertThat(historyStorage.query(firstDescriptorStart, lastDescriptorEnd, samplingPeriod.above()!!)).hasSize(0)

    //calculate down sampling
    downSamplingService.recalculateDownSampling(TimeRanges.of(TimeRange(firstDescriptorStart, lastDescriptorEnd)), samplingPeriod.above()!!.toHistoryBucketRange())

    //Verify the downsampling jobs
    downSamplingService.createJobs(TimeRanges.of(TimeRange(firstDescriptorStart, lastDescriptorEnd)), samplingPeriod.above()!!.toHistoryBucketRange()).let {
      assertThat(it).hasSize(2)

      (it.first().refreshRange as RefreshPartially).let {
        assertThat(it.timeRanges.firstStart.formatUtc()).isEqualTo(firstDescriptorStart.formatUtc())
      }

      (it.last().refreshRange as RefreshPartially).let {
        assertThat(it.timeRanges.lastEnd.formatUtc()).isEqualTo(lastDescriptorEnd.formatUtc())
      }
    }

    //The stored values
    historyStorage.query(firstDescriptorStart, lastDescriptorEnd, samplingPeriod).let {
      assertThat(it).hasSize(10)
      val first = it.first()

      assertThat(first.bucketRange).isEqualTo(HistoryBucketRange.OneMinute)
      assertThat(first.start.formatUtc()).isEqualTo("2021-03-27T21:45:00.000")
      assertThat(first.end.formatUtc()).isEqualTo("2021-03-27T21:46:00.000")
    }


    //Ensure down sampling exists
    historyStorage.query(firstDescriptorStart, lastDescriptorEnd, samplingPeriod.above()!!).let {
      assertThat(it).hasSize(2)
      val first = it.first()
      val last = it.last()
      assertThat(first.bucketRange).isEqualTo(HistoryBucketRange.TenMinutes)
      assertThat(first.samplingPeriod).isEqualTo(SamplingPeriod.EverySecond)

      assertThat(first.start.formatUtc()).isEqualTo("2021-03-27T21:40:00.000")
      assertThat(first.end.formatUtc()).isEqualTo("2021-03-27T21:50:00.000")

      assertThat(last.start.formatUtc()).isEqualTo("2021-03-27T21:50:00.000")
      assertThat(last.end.formatUtc()).isEqualTo("2021-03-27T22:00:00.000")

      assertThat(first.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(0))).isNaN()

      assertThat(first.chunk.timeStamps[0].formatUtc()).isEqualTo("2021-03-27T21:40:00.500")
      assertThat(first.chunk.timeStamps[1].formatUtc()).isEqualTo("2021-03-27T21:40:01.500")

      (5 * 60 + 22).let {
        assertThat(first.chunk.timeStamps[it].formatUtc()).isEqualTo("2021-03-27T21:45:22.500")
        assertThat(first.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(it))).isCloseTo(-89.5969, 0.0001)
      }
      (5 * 60 + 23).let {
        assertThat(first.chunk.timeStamps[it].formatUtc()).isEqualTo("2021-03-27T21:45:23.500")
        assertThat(first.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(it))).isCloseTo(-77.241431, 0.00001)
      }
      (5 * 60 + 24).let {
        assertThat(first.chunk.timeStamps[it].formatUtc()).isEqualTo("2021-03-27T21:45:24.500")
        assertThat(first.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(it))).isCloseTo(6.129487, 0.00001)
      }
    }

    //############################################################
    //############################################################
    //############################################################
    //Cleaning up all but one bucket
    //############################################################
    //############################################################
    //############################################################

    cleanupService.cleanup(historyStorage, historyBucketRange, 1).let {
      assertThat(it.deletedDescriptors).hasSize(9)
    }
    assertThat(bookKeeping.earliestBound(historyBucketRange)?.start?.formatUtc()).isEqualTo("2021-03-27T21:54:00.000")

    //Just one bucket is remaining
    assertThat(historyStorage.query(firstDescriptorStart, lastDescriptorEnd, samplingPeriod)).hasSize(1)

    //Update down sampling
    downSamplingService.recalculateDownSampling(TimeRanges.of(TimeRange(firstDescriptorStart, lastDescriptorEnd)), samplingPeriod.above()!!.toHistoryBucketRange())


    //Check the values that are stored
    historyStorage.query(firstDescriptorStart, lastDescriptorEnd, samplingPeriod).let {
      assertThat(it).hasSize(1)
      val first = it.first()

      assertThat(first.bucketRange).isEqualTo(HistoryBucketRange.OneMinute)
      assertThat(first.start.formatUtc()).isEqualTo("2021-03-27T21:54:00.000")
      assertThat(first.end.formatUtc()).isEqualTo("2021-03-27T21:55:00.000")

      (0).let {
        assertThat(first.chunk.timeStamps[it].formatUtc()).isEqualTo("2021-03-27T21:54:00.000")
        assertThat(first.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(it))).isCloseTo(38.2727, 0.001)
      }
      (1).let {
        assertThat(first.chunk.timeStamps[it].formatUtc()).isEqualTo("2021-03-27T21:54:00.100")
        assertThat(first.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(it))).isCloseTo(47.3047, 0.001)
      }

      (5 * 10 + 4).let {
        assertThat(first.chunk.timeStamps[it].formatUtc()).isEqualTo("2021-03-27T21:54:05.400")
        assertThat(first.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(it))).isCloseTo(-47.101242, 0.001)
      }
    }


    //Down sampling cleaned up?
    historyStorage.query(firstDescriptorStart, lastDescriptorEnd, samplingPeriod.above()!!).let {
      assertThat(it).hasSize(1)
      val first = it.first()

      assertThat(first.bucketRange).isEqualTo(HistoryBucketRange.TenMinutes)
      assertThat(first.start.formatUtc()).isEqualTo("2021-03-27T21:50:00.000")
      assertThat(first.end.formatUtc()).isEqualTo("2021-03-27T22:00:00.000")


      (0).let {
        assertThat(first.chunk.timeStamps[it].formatUtc()).isEqualTo("2021-03-27T21:50:00.500")
        assertThat(first.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(it))).isNaN()
      }
      (5 * 60 + 22).let {
        assertThat(first.chunk.timeStamps[it].formatUtc()).isEqualTo("2021-03-27T21:55:22.500")
        assertThat(first.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(it))).isNaN()
      }
      (5 * 60 + 23).let {
        assertThat(first.chunk.timeStamps[it].formatUtc()).isEqualTo("2021-03-27T21:55:23.500")
        assertThat(first.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(it))).isNaN()
      }
      (5 * 60 + 24).let {
        assertThat(first.chunk.timeStamps[it].formatUtc()).isEqualTo("2021-03-27T21:55:24.500")
        assertThat(first.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(it))).isNaN()
      }

      (60 * 4).let {
        assertThat(first.chunk.timeStamps[it].formatUtc()).isEqualTo("2021-03-27T21:54:00.500")
        assertThat(first.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(it))).isCloseTo(71.605, 0.001)
      }
      (60 * 4 + 1).let {
        assertThat(first.chunk.timeStamps[it].formatUtc()).isEqualTo("2021-03-27T21:54:01.500")
        assertThat(first.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(it))).isCloseTo(92.39970, 0.001)
      }


      (first.chunk.timeStampsCount - 1).let {
        assertThat(first.chunk.timeStamps[it].formatUtc()).isEqualTo("2021-03-27T21:59:59.500")
        assertThat(first.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(it))).isNaN()
      }
    }
  }
}
