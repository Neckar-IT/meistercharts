package com.meistercharts.history

import assertk.*
import assertk.assertions.*
import com.meistercharts.history.cleanup.HistoryCleanupService
import com.meistercharts.history.impl.HistoryChunk
import com.meistercharts.history.impl.chunk
import it.neckar.open.formatting.formatUtc
import it.neckar.open.i18n.TextKey
import it.neckar.open.unit.si.ms
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 */
class InMemoryHistoryStorageTest {
  val start = 1000.0
  val samplingPeriod = SamplingPeriod.EveryHundredMillis

  val descriptor: HistoryBucketDescriptor = HistoryBucketDescriptor.forTimestamp(start, samplingPeriod)

  lateinit var storage: InMemoryHistoryStorage

  @BeforeEach
  internal fun setUp() {
    storage = InMemoryHistoryStorage().also {
      it.naturalSamplingPeriod = samplingPeriod
    }
  }

  @Test
  fun testBookKeeping() {
    val samplingPeriod = SamplingPeriod.EveryMillisecond

    storage.naturalSamplingPeriod = samplingPeriod

    assertThat(storage.bookKeeping.earliestBound(HistoryBucketRange.OneHour)).isNull()
    assertThat(storage.bookKeeping.latestBound(HistoryBucketRange.HundredMillis)).isNull()

    val chunk = createHistoryChunk(start, samplingPeriod)

    assertThat(chunk.start.formatUtc()).isEqualTo("1970-01-01T00:00:01.000")
    storage.storeWithoutCache(chunk, samplingPeriod)

    storage.query(chunk.start, chunk.start, samplingPeriod).let { queryResult ->
      assertThat(queryResult).hasSize(1)
      val bucket = queryResult[0]
      assertThat(bucket.start).isEqualTo(1000.0)
      assertThat(bucket.end).isEqualTo(1100.0)
    }

    assertThat(storage.bookKeeping.earliestBound(HistoryBucketRange.OneHour)).isNull()
    storage.bookKeeping.earliestBound(HistoryBucketRange.HundredMillis).let {
      requireNotNull(it)

      assertThat(it.bucketRange).isSameAs(HistoryBucketRange.HundredMillis)
      assertThat(it.start).isEqualTo(chunk.start)
    }

    storage.bookKeeping.latestBound(HistoryBucketRange.HundredMillis).let {
      requireNotNull(it)

      assertThat(it.bucketRange).isSameAs(HistoryBucketRange.HundredMillis)
      assertThat(it.end).isEqualTo(1100.0)
      assertThat(it.end.formatUtc()).isEqualTo("1970-01-01T00:00:01.100")
    }

    storage.clear()
    assertThat(storage.bookKeeping.earliestBound(HistoryBucketRange.OneHour)).isNull()
    assertThat(storage.bookKeeping.latestBound(HistoryBucketRange.HundredMillis)).isNull()
  }

  @Test
  fun testRemove() {
    val samplingPeriod = SamplingPeriod.EveryMillisecond
    storage.naturalSamplingPeriod = samplingPeriod

    val historyBucketRange = samplingPeriod.toHistoryBucketRange()

    assertThat(historyBucketRange).isSameAs(HistoryBucketRange.HundredMillis)


    for (i in 0..15) {
      storage.storeWithoutCache(createHistoryChunk(start + i * historyBucketRange.duration, samplingPeriod), samplingPeriod)
    }

    assertThat(storage.bookKeeping.earliestBound(HistoryBucketRange.HundredMillis)!!.start).isEqualTo(1000.0)
    assertThat(storage.bookKeeping.latestBound(HistoryBucketRange.HundredMillis)!!.end).isEqualTo(2600.0)

    storage.delete(HistoryBucketDescriptor.forTimestamp(1050.0, samplingPeriod.toHistoryBucketRange()))

    assertThat(storage.bookKeeping.earliestBound(HistoryBucketRange.HundredMillis)!!.start).isEqualTo(1100.0)
    assertThat(storage.bookKeeping.latestBound(HistoryBucketRange.HundredMillis)!!.end).isEqualTo(2600.0)

    storage.delete(HistoryBucketDescriptor.forTimestamp(2599.0, samplingPeriod.toHistoryBucketRange()))

    assertThat(storage.bookKeeping.earliestBound(HistoryBucketRange.HundredMillis)!!.start).isEqualTo(1100.0)
    assertThat(storage.bookKeeping.latestBound(HistoryBucketRange.HundredMillis)!!.end).isEqualTo(2500.0)
  }

  @Test
  fun testDelete() {
    val samplingPeriod = SamplingPeriod.EveryMillisecond
    storage.naturalSamplingPeriod = samplingPeriod

    val historyBucketRange = samplingPeriod.toHistoryBucketRange()

    assertThat(historyBucketRange).isSameAs(HistoryBucketRange.HundredMillis)


    for (i in 0..15) {
      storage.storeWithoutCache(createHistoryChunk(start + i * historyBucketRange.duration, samplingPeriod), samplingPeriod)
    }

    assertThat(storage.bookKeeping.earliestBound(HistoryBucketRange.HundredMillis)!!.start).isEqualTo(1000.0)
    assertThat(storage.bookKeeping.latestBound(HistoryBucketRange.HundredMillis)!!.end).isEqualTo(2600.0)

    assertThat(storage.bucketCount).isEqualTo(16)
    storage.deleteAndBefore(HistoryBucketDescriptor.forTimestamp(1500.0, HistoryBucketRange.HundredMillis))
    assertThat(storage.bucketCount).isEqualTo(10)

    assertThat(storage.bookKeeping.earliestBound(HistoryBucketRange.HundredMillis)!!.start).isEqualTo(1600.0)
    assertThat(storage.bookKeeping.latestBound(HistoryBucketRange.HundredMillis)!!.end).isEqualTo(2600.0)
  }

  @Test
  fun testAutoDelete() {
    val samplingPeriod = SamplingPeriod.EveryMillisecond
    storage.naturalSamplingPeriod = samplingPeriod

    val cleanupService = HistoryCleanupService(storage)
    val historyBucketRange = samplingPeriod.toHistoryBucketRange()

    assertThat(historyBucketRange).isSameAs(HistoryBucketRange.HundredMillis)


    for (i in 0..15) {
      storage.storeWithoutCache(createHistoryChunk(start + i * historyBucketRange.duration, samplingPeriod), samplingPeriod)
    }

    assertThat(storage.bookKeeping.earliestBound(HistoryBucketRange.HundredMillis)!!.start).isEqualTo(1000.0)
    assertThat(storage.bookKeeping.latestBound(HistoryBucketRange.HundredMillis)!!.end).isEqualTo(2600.0)

    assertThat(storage.bucketCount).isEqualTo(16)
    cleanupService.cleanup(storage, HistoryBucketRange.HundredMillis, 5)
    assertThat(storage.bucketCount).isEqualTo(5)

    assertThat(storage.bookKeeping.earliestBound(HistoryBucketRange.HundredMillis)!!.start).isEqualTo(2100.0)
    assertThat(storage.bookKeeping.latestBound(HistoryBucketRange.HundredMillis)!!.end).isEqualTo(2600.0)
  }

  @Test
  fun testAdd() {
    val chunk = createHistoryChunk(start, samplingPeriod)
    storage.storeWithoutCache(HistoryBucket(descriptor, chunk), HistoryUpdateInfo.fromChunk(chunk, samplingPeriod))

    storage.query(descriptor.start, descriptor.start, descriptor.bucketRange.samplingPeriod).let {
      assertThat(it).hasSize(1)
      val bucket = it[0]
      assertThat(bucket.chunk.getDecimalDataSeriesId(DecimalDataSeriesIndex(0)).value).isEqualTo(1)

      assertThat(bucket.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(0))).isEqualTo(5.0)
      assertThat(bucket.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(1))).isEqualTo(6.0)
      assertThat(bucket.chunk.getDecimalValue(DecimalDataSeriesIndex(1), TimestampIndex(0))).isEqualTo(50.0)
      assertThat(bucket.chunk.getDecimalValue(DecimalDataSeriesIndex(1), TimestampIndex(1))).isEqualTo(60.0)
      assertThat(bucket.chunk.getDecimalValue(DecimalDataSeriesIndex(2), TimestampIndex(0))).isEqualTo(500.0)
      assertThat(bucket.chunk.getDecimalValue(DecimalDataSeriesIndex(2), TimestampIndex(1))).isEqualTo(600.0)
    }
  }

  @Test
  fun testEmptyGet() {
    storage.get(descriptor).let {
      assertThat(it).isNull()
    }

    storage.query(descriptor.start, descriptor.start, descriptor.bucketRange.samplingPeriod).let {
      assertThat(it).isEmpty()
    }

    storage.query(descriptor.start, descriptor.end - 1, descriptor.bucketRange.samplingPeriod).let {
      assertThat(it).isEmpty()
    }

    storage.query(descriptor.start, descriptor.end, descriptor.bucketRange.samplingPeriod).let {
      assertThat(it).isEmpty()
    }
  }

  @Test
  fun testClear() {
    val chunk = createHistoryChunk(start, samplingPeriod)
    storage.storeWithoutCache(HistoryBucket(descriptor, chunk), HistoryUpdateInfo.fromChunk(chunk, samplingPeriod))
    assertThat(storage.get(descriptor)!!.chunk.isEmpty()).isFalse()

    var notificationCount = 0
    storage.observe { _, _ ->
      ++notificationCount
    }
    storage.clear()
    assertThat(notificationCount).isEqualTo(1)

    assertThat(storage.get(descriptor)).isNull()
  }

  private fun createHistoryChunk(start: @ms Double, samplingPeriod: SamplingPeriod): HistoryChunk {
    return historyConfiguration {
      decimalDataSeries(DataSeriesId(1), TextKey.simple("Value 1"))
      decimalDataSeries(DataSeriesId(2), TextKey.simple("Value 2"))
      decimalDataSeries(DataSeriesId(3), TextKey.simple("Value 3"))
    }.chunk() {
      addDecimalValues(start, 5.0, 50.0, 500.0)
      addDecimalValues(start + samplingPeriod.distance, 6.0, 60.0, 600.0)
    }
  }
}
