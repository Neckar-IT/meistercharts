package com.meistercharts.history.downsampling

import assertk.*
import assertk.assertions.*
import com.meistercharts.algorithms.TimeRange
import com.meistercharts.algorithms.TimeRanges
import it.neckar.open.formatting.formatUtc
import it.neckar.open.test.utils.RandomWithSeed
import it.neckar.open.time.TimeConstants
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryDifferentIdsCount
import com.meistercharts.history.ReferenceEntryId
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.TimestampIndex
import com.meistercharts.history.generator.HistoryChunkGenerator
import com.meistercharts.history.generator.ReferenceEntryGenerator
import com.meistercharts.history.impl.timeRange
import com.meistercharts.history.isEqualToReferenceEntryId
import com.meistercharts.history.isEqualToReferenceEntryIdsCount
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class DownSamplingReferenceEntriesTest {
  @Test
  fun test45s() {
    val historyStorage = InMemoryHistoryStorage()

    val samplingPeriod = SamplingPeriod.EveryHundredMillis
    val samplingPeriodAbove = requireNotNull(samplingPeriod.above())

    val chunkGenerator = HistoryChunkGenerator(
      historyStorage = historyStorage,
      samplingPeriod = samplingPeriod,
      decimalValueGenerators = emptyList(),
      enumValueGenerators = emptyList(),
      referenceEntryGenerators = List(3) {
        ReferenceEntryGenerator.increasing(45.seconds)
      },
    )

    historyStorage.downSamplingService.dirtyRangesCollector.observe(historyStorage)

    val start = 1777777896777.0
    assertThat(start.formatUtc()).isEqualTo("2026-05-03T03:11:36.777")
    val chunk = requireNotNull(chunkGenerator.forTimeRange(TimeRange.fromStartAndDuration(start, 1.hours)))

    assertThat(chunk.start.formatUtc()).isEqualTo(start.formatUtc())


    historyStorage.storeWithoutCache(chunk, samplingPeriod)
    historyStorage.downSamplingService.calculateDownSamplingIfRequired()


    historyStorage.get(HistoryBucketDescriptor.forTimestamp(start, samplingPeriod.toHistoryBucketRange())).let { chunk ->
      requireNotNull(chunk)
      assertThat(chunk.chunk.timestampCenter(TimestampIndex.zero).formatUtc()).isEqualTo("2026-05-03T03:11:36.777")
      assertThat(chunk.chunk.timeStampsCount).isEqualTo(233)
      assertThat(chunk.chunk.lastTimeStamp().formatUtc()).isEqualTo("2026-05-03T03:11:59.977")

      assertThat(chunk.chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex.zero)).isEqualToReferenceEntryId(6175)
      assertThat(chunk.chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex.one)).isEqualToReferenceEntryId(6175)
      assertThat(chunk.chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(77))).isEqualToReferenceEntryId(6175)
      assertThat(chunk.chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, chunk.chunk.lastTimeStampIndex())).isEqualToReferenceEntryId(6175)
    }

    historyStorage.get(HistoryBucketDescriptor.forTimestamp(start + 45 * 1000, samplingPeriod.toHistoryBucketRange())).let { chunk ->
      requireNotNull(chunk)
      assertThat(chunk.chunk.timestampCenter(TimestampIndex.zero).formatUtc()).isEqualTo("2026-05-03T03:12:00.077")
      assertThat(chunk.chunk.timeStampsCount).isEqualTo(600)
      assertThat(chunk.chunk.lastTimeStamp().formatUtc()).isEqualTo("2026-05-03T03:12:59.977")

      assertThat(chunk.chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex.zero)).isEqualToReferenceEntryId(6176)
      assertThat(chunk.chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex.one)).isEqualToReferenceEntryId(6176)
      assertThat(chunk.chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(77))).isEqualToReferenceEntryId(6176)
      assertThat(chunk.chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, chunk.chunk.lastTimeStampIndex())).isEqualToReferenceEntryId(6177)
    }

    //Verify downsampled
    historyStorage.get(HistoryBucketDescriptor.forTimestamp(start, samplingPeriodAbove.toHistoryBucketRange())).let { chunk ->
      requireNotNull(chunk)
      if (false) {
        println(chunk.chunk.dump())
      }

      assertThat(chunk.chunk.timestampCenter(TimestampIndex.zero).formatUtc()).isEqualTo("2026-05-03T03:10:00.500")
      assertThat(chunk.chunk.timeStampsCount).isEqualTo(600)
      assertThat(chunk.chunk.start.formatUtc()).isEqualTo("2026-05-03T03:10:00.500")
      assertThat(chunk.chunk.end.formatUtc()).isEqualTo("2026-05-03T03:19:59.500")


      assertThat(chunk.chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(0))).isEqualTo(ReferenceEntryId.Pending)
      assertThat(chunk.chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(599))).isEqualToReferenceEntryId(6186)

      assertThat(chunk.chunk.getReferenceEntryIdsCount(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(0))).isEqualTo(ReferenceEntryDifferentIdsCount.Pending)
      assertThat(chunk.chunk.getReferenceEntryIdsCount(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(599))).isEqualToReferenceEntryIdsCount(1)

      assertThat(chunk.chunk.getReferenceEntryIdsCount(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(250))).isEqualToReferenceEntryIdsCount(1)
      assertThat(chunk.chunk.getReferenceEntryIdsCount(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(300))).isEqualToReferenceEntryIdsCount(1)
      assertThat(chunk.chunk.getReferenceEntryIdsCount(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(350))).isEqualToReferenceEntryIdsCount(1)
    }

    historyStorage.get(HistoryBucketDescriptor.forTimestamp(start, samplingPeriodAbove.above()!!.toHistoryBucketRange())).let { chunk ->
      requireNotNull(chunk)
      if (true) {
        println(chunk.chunk.dump())
      }

      assertThat(chunk.chunk.timestampCenter(TimestampIndex.zero).formatUtc()).isEqualTo("2026-05-03T03:00:05.000")
      assertThat(chunk.chunk.timeStampsCount).isEqualTo(360)
      assertThat(chunk.chunk.start.formatUtc()).isEqualTo("2026-05-03T03:00:05.000")
      assertThat(chunk.chunk.end.formatUtc()).isEqualTo("2026-05-03T03:59:55.000")


      assertThat(chunk.chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(0))).isEqualTo(ReferenceEntryId.Pending)
      assertThat(chunk.chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(359))).isEqualToReferenceEntryId(6239)

      assertThat(chunk.chunk.getReferenceEntryIdsCount(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(0))).isEqualTo(ReferenceEntryDifferentIdsCount.Pending)
      assertThat(chunk.chunk.getReferenceEntryIdsCount(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(359))).isEqualToReferenceEntryIdsCount(1)

      assertThat(chunk.chunk.getReferenceEntryIdsCount(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(250))).isEqualToReferenceEntryIdsCount(1)
      assertThat(chunk.chunk.getReferenceEntryIdsCount(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(300))).isEqualToReferenceEntryIdsCount(1)
      assertThat(chunk.chunk.getReferenceEntryIdsCount(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(350))).isEqualToReferenceEntryIdsCount(1)
    }
  }

  @Test
  fun testSameValue() {
    val historyStorage = InMemoryHistoryStorage()

    val samplingPeriod = SamplingPeriod.EveryHundredMillis
    val samplingPeriodAbove = requireNotNull(samplingPeriod.above())

    val chunkGenerator = HistoryChunkGenerator(
      historyStorage = historyStorage,
      samplingPeriod = samplingPeriod,
      decimalValueGenerators = emptyList(),
      enumValueGenerators = emptyList(),
      referenceEntryGenerators = List(1) {
        ReferenceEntryGenerator.always(ReferenceEntryId(45))
      },
    )

    val generated = chunkGenerator.forTimeRange(TimeRange.oneMinuteSinceReference)
    requireNotNull(generated)

    assertThat(generated.timeRange()).isEqualTo(TimeRange(TimeConstants.referenceTimestamp, TimeConstants.referenceTimestamp + 60_000 - 100)) //plus 1 minute, minus the last entry
    assertThat(generated.start.formatUtc()).isEqualTo("2001-09-09T01:46:40.000") //plus 1 minute, minus the last entry
    assertThat(generated.end.formatUtc()).isEqualTo("2001-09-09T01:47:39.900") //plus 1 minute, minus the last entry


    assertThat(generated.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex.one)).isEqualToReferenceEntryId(45)
    assertThat(generated.getReferenceEntryIdsCount(ReferenceEntryDataSeriesIndex.zero, TimestampIndex.one).value).isEqualTo(1)

    assertThat(historyStorage.downSamplingService.dirtyRangesCollector[samplingPeriod]).isNull()

    historyStorage.downSamplingService.dirtyRangesCollector.observe(historyStorage)
    assertThat(historyStorage.downSamplingService.dirtyRangesCollector[samplingPeriod]).isNull()

    assertThat(historyStorage.query(generated.timeRange(), samplingPeriod)).isEmpty()
    assertThat(historyStorage.query(generated.timeRange(), samplingPeriodAbove)).isEmpty()
    historyStorage.storeWithoutCache(generated, samplingPeriod)
    assertThat(historyStorage.query(generated.timeRange(), samplingPeriod)).hasSize(2)
    assertThat(historyStorage.query(generated.timeRange(), samplingPeriodAbove)).isEmpty() //no down sampling!

    assertThat(historyStorage.downSamplingService.dirtyRangesCollector[samplingPeriod]).isNull()
    assertThat(historyStorage.downSamplingService.dirtyRangesCollector[SamplingPeriod.EverySecond]).isEqualTo(TimeRanges.of(generated.timeRange()))

    historyStorage.downSamplingService.calculateDownSamplingIfRequired()

    assertThat(historyStorage.downSamplingService.dirtyRangesCollector[samplingPeriod]).isNull()
    assertThat(historyStorage.downSamplingService.dirtyRangesCollector[SamplingPeriod.EverySecond]).isNull() //not dirty anymore

    assertThat(historyStorage.query(generated.timeRange(), samplingPeriod)).hasSize(2)
    val downSampled = historyStorage.query(generated.timeRange(), samplingPeriodAbove)
    assertThat(downSampled).hasSize(1) //down sampling has been calculated

    downSampled.first().let { bucket ->
      assertThat(bucket.start.formatUtc()).isEqualTo("2001-09-09T01:40:00.000")
      assertThat(bucket.end.formatUtc()).isEqualTo("2001-09-09T01:50:00.000")

      val chunk = bucket.chunk
      val timestampIndex = TimestampIndex(chunk.bestTimestampIndexFor(generated.start).nearIndex)

      assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, timestampIndex)).isEqualToReferenceEntryId(45)
      assertThat(chunk.getReferenceEntryIdsCount(ReferenceEntryDataSeriesIndex.zero, timestampIndex)).isEqualToReferenceEntryIdsCount(1)
    }
  }

  @Test
  @RandomWithSeed
  fun testRandomGenerator() {
    val historyStorage = InMemoryHistoryStorage()

    val samplingPeriod = SamplingPeriod.EveryHundredMillis
    val samplingPeriodAbove = requireNotNull(samplingPeriod.above())

    val chunkGenerator = HistoryChunkGenerator(
      historyStorage = historyStorage,
      samplingPeriod = samplingPeriod,
      decimalValueGenerators = emptyList(),
      enumValueGenerators = emptyList(),
      referenceEntryGenerators = List(3) {
        ReferenceEntryGenerator.random()
      },
    )

    val generated = chunkGenerator.forTimeRange(TimeRange.oneMinuteSinceReference)
    requireNotNull(generated)

    assertThat(generated.timeRange()).isEqualTo(TimeRange(TimeConstants.referenceTimestamp, TimeConstants.referenceTimestamp + 60_000 - 100)) //plus 1 minute, minus the last entry
    assertThat(generated.start.formatUtc()).isEqualTo("2001-09-09T01:46:40.000") //plus 1 minute, minus the last entry
    assertThat(generated.end.formatUtc()).isEqualTo("2001-09-09T01:47:39.900") //plus 1 minute, minus the last entry


    assertThat(generated.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex.one)).isEqualToReferenceEntryId(17667)
    assertThat(generated.getReferenceEntryIdsCount(ReferenceEntryDataSeriesIndex.zero, TimestampIndex.one).value).isEqualTo(1)


    //Down sampling
    assertThat(historyStorage.downSamplingService.dirtyRangesCollector[samplingPeriod]).isNull()

    historyStorage.downSamplingService.dirtyRangesCollector.observe(historyStorage)
    assertThat(historyStorage.downSamplingService.dirtyRangesCollector[samplingPeriod]).isNull()

    assertThat(historyStorage.query(generated.timeRange(), samplingPeriod)).isEmpty()
    assertThat(historyStorage.query(generated.timeRange(), samplingPeriodAbove)).isEmpty()
    historyStorage.storeWithoutCache(generated, samplingPeriod)
    assertThat(historyStorage.query(generated.timeRange(), samplingPeriod)).hasSize(2)
    assertThat(historyStorage.query(generated.timeRange(), samplingPeriodAbove)).isEmpty() //no down sampling!

    assertThat(historyStorage.downSamplingService.dirtyRangesCollector[samplingPeriod]).isNull()
    assertThat(historyStorage.downSamplingService.dirtyRangesCollector[SamplingPeriod.EverySecond]).isEqualTo(TimeRanges.of(generated.timeRange()))

    historyStorage.downSamplingService.calculateDownSamplingIfRequired()

    assertThat(historyStorage.downSamplingService.dirtyRangesCollector[samplingPeriod]).isNull()
    assertThat(historyStorage.downSamplingService.dirtyRangesCollector[SamplingPeriod.EverySecond]).isNull() //not dirty anymore

    assertThat(historyStorage.query(generated.timeRange(), samplingPeriod)).hasSize(2)
    val downSampled = historyStorage.query(generated.timeRange(), samplingPeriodAbove)
    assertThat(downSampled).hasSize(1) //down sampling has been calculated

    downSampled.first().let { bucket ->
      assertThat(bucket.start.formatUtc()).isEqualTo("2001-09-09T01:40:00.000")
      assertThat(bucket.end.formatUtc()).isEqualTo("2001-09-09T01:50:00.000")

      val chunk = bucket.chunk
      val timestampIndex = TimestampIndex(chunk.bestTimestampIndexFor(generated.start).nearIndex)

      assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.one, timestampIndex)).isEqualToReferenceEntryId(82032)
      assertThat(chunk.getReferenceEntryIdsCount(ReferenceEntryDataSeriesIndex.one, timestampIndex)).isEqualToReferenceEntryIdsCount(10)
    }
  }

  @Test
  @RandomWithSeed
  fun testIncreasingGenerator() {
    val historyStorage = InMemoryHistoryStorage()

    val samplingPeriod = SamplingPeriod.EveryHundredMillis
    val samplingPeriodAbove = requireNotNull(samplingPeriod.above())

    val chunkGenerator = HistoryChunkGenerator(
      historyStorage = historyStorage,
      samplingPeriod = samplingPeriod,
      decimalValueGenerators = emptyList(),
      enumValueGenerators = emptyList(),
      referenceEntryGenerators = List(3) {
        ReferenceEntryGenerator.increasing(500.milliseconds)
      },
    )

    val generated = chunkGenerator.forTimeRange(TimeRange.oneMinuteSinceReference)
    requireNotNull(generated)

    assertThat(generated.timeRange()).isEqualTo(TimeRange(TimeConstants.referenceTimestamp, TimeConstants.referenceTimestamp + 60_000 - 100)) //plus 1 minute, minus the last entry
    assertThat(generated.start.formatUtc()).isEqualTo("2001-09-09T01:46:40.000") //plus 1 minute, minus the last entry
    assertThat(generated.end.formatUtc()).isEqualTo("2001-09-09T01:47:39.900") //plus 1 minute, minus the last entry


    assertThat(generated.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(0))).isEqualToReferenceEntryId(0)
    assertThat(generated.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(1))).isEqualToReferenceEntryId(0)
    assertThat(generated.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(3))).isEqualToReferenceEntryId(0)
    assertThat(generated.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(4))).isEqualToReferenceEntryId(0)
    assertThat(generated.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(5))).isEqualToReferenceEntryId(1)
    assertThat(generated.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(8))).isEqualToReferenceEntryId(1)
    assertThat(generated.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(9))).isEqualToReferenceEntryId(1)
    assertThat(generated.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(10))).isEqualToReferenceEntryId(2)
    assertThat(generated.getReferenceEntryIdsCount(ReferenceEntryDataSeriesIndex.zero, TimestampIndex.one).value).isEqualTo(1)


    //Down sampling
    assertThat(historyStorage.downSamplingService.dirtyRangesCollector[samplingPeriod]).isNull()

    historyStorage.downSamplingService.dirtyRangesCollector.observe(historyStorage)
    assertThat(historyStorage.downSamplingService.dirtyRangesCollector[samplingPeriod]).isNull()

    assertThat(historyStorage.query(generated.timeRange(), samplingPeriod)).isEmpty()
    assertThat(historyStorage.query(generated.timeRange(), samplingPeriodAbove)).isEmpty()
    historyStorage.storeWithoutCache(generated, samplingPeriod)
    assertThat(historyStorage.query(generated.timeRange(), samplingPeriod)).hasSize(2)
    assertThat(historyStorage.query(generated.timeRange(), samplingPeriodAbove)).isEmpty() //no down sampling!

    assertThat(historyStorage.downSamplingService.dirtyRangesCollector[samplingPeriod]).isNull()
    assertThat(historyStorage.downSamplingService.dirtyRangesCollector[SamplingPeriod.EverySecond]).isEqualTo(TimeRanges.of(generated.timeRange()))

    historyStorage.downSamplingService.calculateDownSamplingIfRequired()

    assertThat(historyStorage.downSamplingService.dirtyRangesCollector[samplingPeriod]).isNull()
    assertThat(historyStorage.downSamplingService.dirtyRangesCollector[SamplingPeriod.EverySecond]).isNull() //not dirty anymore

    assertThat(historyStorage.query(generated.timeRange(), samplingPeriod)).hasSize(2)
    val downSampled = historyStorage.query(generated.timeRange(), samplingPeriodAbove)
    assertThat(downSampled).hasSize(1) //down sampling has been calculated

    downSampled.first().let { bucket ->
      assertThat(bucket.start.formatUtc()).isEqualTo("2001-09-09T01:40:00.000")
      assertThat(bucket.end.formatUtc()).isEqualTo("2001-09-09T01:50:00.000")

      val chunk = bucket.chunk
      val timestampIndex = TimestampIndex(chunk.bestTimestampIndexFor(generated.start).nearIndex)
      assertThat(timestampIndex).isEqualTo(TimestampIndex(400))

      assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex.one, timestampIndex)).isEqualToReferenceEntryId(0)
      assertThat(chunk.getReferenceEntryIdsCount(ReferenceEntryDataSeriesIndex.one, timestampIndex)).isEqualToReferenceEntryIdsCount(2)
    }
  }
}
