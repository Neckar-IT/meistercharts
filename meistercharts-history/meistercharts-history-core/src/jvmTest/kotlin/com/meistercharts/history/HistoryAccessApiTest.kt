package com.meistercharts.history

import assertk.*
import assertk.assertions.*
import com.meistercharts.history.impl.chunk
import it.neckar.open.i18n.TextKey
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 *
 */
class HistoryAccessApiTest {
  lateinit var historyStorage: InMemoryHistoryStorage

  @BeforeEach
  internal fun setUp() {
    historyStorage = InMemoryHistoryStorage()
  }

  @Test
  fun testAdd() {
    historyStorage.query(124000.0, 125000.0, SamplingPeriod.EveryHour).let {
      assertThat(it).isEmpty()
    }

    val historyConfiguration = historyConfiguration {
      decimalDataSeries(DataSeriesId(10), TextKey.simple("daName"))
    }

    val newChunk = historyConfiguration.chunk() {
      addDecimalValues(124000.0, 770.0)
    }

    assertThat(newChunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(0))).isEqualTo(770.0)

    historyStorage.naturalSamplingPeriod = SamplingPeriod.EveryHour
    historyStorage.storeWithoutCache(newChunk, SamplingPeriod.EveryHour)

    historyStorage.query(124000.0, 125000.0, SamplingPeriod.EveryHour).let {
      assertThat(it).hasSize(1)
      val bucket = it[0]
      val chunk = bucket.chunk

      assertThat(chunk.decimalDataSeriesCount).isEqualTo(1)
      assertThat(chunk.isEmpty()).isFalse()

      assertThat(bucket.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(0))).isEqualTo(770.0)
    }
  }

  @Test
  fun testAddSpanning() {
    val samplingPeriod = SamplingPeriod.EveryMillisecond
    val bucketRange = samplingPeriod.toHistoryBucketRange()

    assertThat(bucketRange).isEqualTo(HistoryBucketRange.HundredMillis)
    assertThat(bucketRange.duration).isEqualTo(100.0)

    val start = 1000.0
    val end = start + bucketRange.duration + 1
    assertThat(end).isEqualTo(1101.0)


    historyStorage.query(start, end, samplingPeriod).let {
      assertThat(it).isEmpty()
    }

    val historyConfiguration = historyConfiguration {
      decimalDataSeries(DataSeriesId(10), TextKey.simple("daName"))
    }

    val newChunk = historyConfiguration.chunk() {
      addDecimalValues(1000.0, 770.0)
      addDecimalValues(1_170.0, 770.0)
    }

    assertThat(newChunk.getDecimalValue(DecimalDataSeriesIndex.zero, TimestampIndex(0))).isEqualTo(770.0)
    assertThat(newChunk.getDecimalValue(DecimalDataSeriesIndex.zero, TimestampIndex(1))).isEqualTo(770.0)

    historyStorage.naturalSamplingPeriod = samplingPeriod
    historyStorage.storeWithoutCache(newChunk, samplingPeriod)


    //Simulate the query
    val range = HistoryBucketRange.find(samplingPeriod)
    assertThat(range).isEqualTo(HistoryBucketRange.HundredMillis)
    HistoryBucketDescriptor.forRange(start, end, range).let { descriptors ->
      assertThat(descriptors.size).isEqualTo(2)
    }

    historyStorage.query(start, end, samplingPeriod).let {
      assertThat(it).hasSize(2)

      it[0].let { bucket ->
        val chunk = bucket.chunk

        assertThat(chunk.decimalDataSeriesCount).isEqualTo(1)
        assertThat(chunk.timeStampsCount).isEqualTo(1)
        assertThat(chunk.isEmpty()).isFalse()

        assertThat(bucket.chunk.getDecimalValue(DecimalDataSeriesIndex.zero, TimestampIndex(0))).isEqualTo(770.0)
      }

      it[1].let { bucket ->
        val chunk = bucket.chunk

        assertThat(chunk.decimalDataSeriesCount).isEqualTo(1)
        assertThat(chunk.timeStampsCount).isEqualTo(1)
        assertThat(chunk.isEmpty()).isFalse()

        assertThat(bucket.chunk.getDecimalValue(DecimalDataSeriesIndex.zero, TimestampIndex(0))).isEqualTo(770.0)
      }
    }
  }
}
