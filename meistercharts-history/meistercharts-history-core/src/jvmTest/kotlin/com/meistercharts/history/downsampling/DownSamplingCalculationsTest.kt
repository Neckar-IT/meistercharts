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

import assertk.*
import assertk.assertions.*
import com.meistercharts.algorithms.TimeRange
import com.meistercharts.algorithms.TimeRanges
import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.EnumDataSeriesIndex
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryBucketRange
import com.meistercharts.history.HistoryEnumOrdinal
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.TimestampIndex
import com.meistercharts.history.historyConfiguration
import com.meistercharts.history.impl.RecordingType
import com.meistercharts.history.impl.chunk
import it.neckar.open.collections.fastForEach
import it.neckar.open.formatting.formatUtc
import it.neckar.open.i18n.TextKey
import it.neckar.open.test.utils.isEqualComparingLinesTrim
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

/**
 *
 */
class DownSamplingCalculationsTest {
  @Test
  fun testIEA() {
    val now = 1.597758912367E12
    assertThat(now.formatUtc()).isEqualTo("2020-08-18T13:55:12.367")

    val parentBucketDescriptor = HistoryBucketDescriptor.forTimestamp(now, HistoryBucketRange.FiveSeconds)

    val childBucketDescriptors = parentBucketDescriptor.children()
    childBucketDescriptors.fastForEach {
      assertThat(it.start).isGreaterThanOrEqualTo(parentBucketDescriptor.start)
      assertThat(it.end).isLessThanOrEqualTo(parentBucketDescriptor.end)
    }

    childBucketDescriptors.let {
      assertThat(it).hasSize(50)

      assertThat(it.first().start).isEqualTo(parentBucketDescriptor.start)
      assertThat(it.last().end).isEqualTo(parentBucketDescriptor.end)
    }


    val iterator = DownSamplingTargetTimestampsIterator.create(parentBucketDescriptor)
    assertThat(iterator.slotStart).isEqualTo(parentBucketDescriptor.start)

    assertThat(iterator.index).isEqualTo(TimestampIndex.zero)
    iterator.next()
    assertThat(iterator.index).isEqualTo(TimestampIndex.one)
  }

  @Test
  fun testDecimals() {
    val historyChunk = historyConfiguration {
      decimalDataSeries(DataSeriesId(1000), TextKey.simple("A"))
      decimalDataSeries(DataSeriesId(1001), TextKey.simple("B"))
    }.chunk {
      addDecimalValues(50_000.0, 0.0, 1.0)
      addDecimalValues(50_099.9, 1.0, 2.0)
      addDecimalValues(50_100.0, 2.0, 3.0)
      addDecimalValues(50_100.1, 3.0, 4.0)
    }

    val historyStorage = InMemoryHistoryStorage()
    historyStorage.storeWithoutCache(historyChunk, SamplingPeriod.EveryMillisecond)

    assertThat(historyStorage.keys()).hasSize(2)
    historyStorage.keys().first().let {
      assertThat(it.start).isEqualTo(50_000.0)
      assertThat(it.end).isEqualTo(50_100.0)
      assertThat(it.bucketRange).isEqualTo(HistoryBucketRange.HundredMillis)
    }

    historyStorage.keys().last().let {
      assertThat(it.start).isEqualTo(50_100.0)
      assertThat(it.end).isEqualTo(50_200.0)
      assertThat(it.bucketRange).isEqualTo(HistoryBucketRange.HundredMillis)
    }
  }

  @Test
  fun testEnumsPriority() {
    val historyChunk = historyConfiguration {
      enumDataSeries(DataSeriesId(1000), TextKey.simple("A"), createDemoEnumConfiguration(7))
      enumDataSeries(DataSeriesId(1001), TextKey.simple("B"), createDemoEnumConfiguration(8))
    }.chunk {
      addEnumValues(50_000.0, 0b001, 0b1011)

      addEnumValues(50_099.9, 0b010, 0b1100)

      addEnumValues(50_100.0, 0b100, 0b1110)
      addEnumValues(50_100.1, 0b111, 0b1001)
    }

    assertThat(historyChunk.timeStampsCount).isEqualTo(4)


    val historyStorage = InMemoryHistoryStorage()
    val samplingPeriod = SamplingPeriod.EveryMillisecond
    assertThat(samplingPeriod.toHistoryBucketRange()).isEqualTo(HistoryBucketRange.HundredMillis)

    historyStorage.storeWithoutCache(historyChunk, samplingPeriod)

    //Down sampling
    val below = samplingPeriod.above()!!
    assertThat(below).isEqualTo(SamplingPeriod.EveryTenMillis)
    historyStorage.downSamplingService.recalculateDownSampling(TimeRanges.of(TimeRange(50_000.0, 51_000.0)), below.toHistoryBucketRange())

    val bucket = historyStorage.query(50_099.9, 50_100.0, below).first()
    val chunk = bucket.chunk

    assertThat(chunk.dump(50_000.0, 50_200.0)).isEqualComparingLinesTrim(
      """
          Start: 1970-01-01T00:00:50.005
          End:   1970-01-01T00:00:54.995
          Series counts:
            Decimals: 0
            Enums:    2
            RefId:    0
          RecordingType:    Calculated
          ---------------------------------------
          Indices:                     |               0               1  |
          IDs:                         |            1000            1001  |

             0 1970-01-01T00:00:50.005 |           1 (0)        1011 (0)  |
             1 1970-01-01T00:00:50.015 |           ? (?)           ? (?)  |
             2 1970-01-01T00:00:50.025 |           ? (?)           ? (?)  |
             3 1970-01-01T00:00:50.035 |           ? (?)           ? (?)  |
             4 1970-01-01T00:00:50.045 |           ? (?)           ? (?)  |
             5 1970-01-01T00:00:50.055 |           ? (?)           ? (?)  |
             6 1970-01-01T00:00:50.065 |           ? (?)           ? (?)  |
             7 1970-01-01T00:00:50.075 |           ? (?)           ? (?)  |
             8 1970-01-01T00:00:50.085 |           ? (?)           ? (?)  |
             9 1970-01-01T00:00:50.095 |          10 (1)        1100 (2)  |
            10 1970-01-01T00:00:50.105 |         111 (2)        1111 (3)  |
            11 1970-01-01T00:00:50.115 |           ? (?)           ? (?)  |
            12 1970-01-01T00:00:50.125 |           ? (?)           ? (?)  |
            13 1970-01-01T00:00:50.135 |           ? (?)           ? (?)  |
            14 1970-01-01T00:00:50.145 |           ? (?)           ? (?)  |
            15 1970-01-01T00:00:50.155 |           ? (?)           ? (?)  |
            16 1970-01-01T00:00:50.165 |           ? (?)           ? (?)  |
            17 1970-01-01T00:00:50.175 |           ? (?)           ? (?)  |
            18 1970-01-01T00:00:50.185 |           ? (?)           ? (?)  |
            19 1970-01-01T00:00:50.195 |           ? (?)           ? (?)  |
      """
    )

    assertThat(bucket.chunk.getEnumValue(EnumDataSeriesIndex.zero, TimestampIndex.one)).isEqualTo(HistoryEnumSet.Pending)
    assertThat(bucket.chunk.getEnumValue(EnumDataSeriesIndex.zero, TimestampIndex(19))).isEqualTo(HistoryEnumSet.Pending)

    //Consists of a single value
    assertThat(bucket.chunk.getEnumValue(EnumDataSeriesIndex.zero, TimestampIndex(0))).isEqualTo(HistoryEnumSet(0b0001))
    assertThat(bucket.chunk.getEnumValue(EnumDataSeriesIndex.one, TimestampIndex(0))).isEqualTo(HistoryEnumSet(0b1011))
    assertThat(bucket.chunk.getEnumOrdinalMostTime(EnumDataSeriesIndex.zero, TimestampIndex(0))).isEqualTo(HistoryEnumOrdinal(0))
    assertThat(bucket.chunk.getEnumOrdinalMostTime(EnumDataSeriesIndex.one, TimestampIndex(0))).isEqualTo(HistoryEnumOrdinal(0))

    //Down sampled from one value
    assertThat(bucket.chunk.getEnumValue(EnumDataSeriesIndex.zero, TimestampIndex(9))).isEqualTo(HistoryEnumSet(0b10))
    assertThat(bucket.chunk.getEnumValue(EnumDataSeriesIndex.one, TimestampIndex(9))).isEqualTo(HistoryEnumSet(0b1100))
    assertThat(bucket.chunk.getEnumOrdinalMostTime(EnumDataSeriesIndex.zero, TimestampIndex(9))).isEqualTo(HistoryEnumOrdinal(1))
    assertThat(bucket.chunk.getEnumOrdinalMostTime(EnumDataSeriesIndex.one, TimestampIndex(9))).isEqualTo(HistoryEnumOrdinal(2))

    //Down sampled from two values
    assertThat(bucket.chunk.getEnumValue(EnumDataSeriesIndex.zero, TimestampIndex(10))).isEqualTo(HistoryEnumSet(0b111))
    assertThat(bucket.chunk.getEnumValue(EnumDataSeriesIndex.one, TimestampIndex(10))).isEqualTo(HistoryEnumSet(0b1111))
    assertThat(bucket.chunk.getEnumOrdinalMostTime(EnumDataSeriesIndex.zero, TimestampIndex(10))).isEqualTo(HistoryEnumOrdinal(2))
    assertThat(bucket.chunk.getEnumOrdinalMostTime(EnumDataSeriesIndex.one, TimestampIndex(10))).isEqualTo(HistoryEnumOrdinal(3))
  }

  @Test
  fun testEnums() {
    val historyChunk = historyConfiguration {
      enumDataSeries(DataSeriesId(1000), TextKey.simple("A"), createDemoEnumConfiguration(7))
      enumDataSeries(DataSeriesId(1001), TextKey.simple("B"), createDemoEnumConfiguration(8))
    }.chunk {
      addEnumValues(50_000.0, 0b1, 0b1011)
      addEnumValues(50_099.9, 0b11, 0b1101)
      addEnumValues(50_100.0, 0b101, 0b1111)
      addEnumValues(50_100.1, 0b111, 0b10001)
    }

    //verify HistoryChunk

    assertThat(historyChunk.timeStampsCount).isEqualTo(4)
    assertThat(historyChunk.timestampCenter(TimestampIndex(0))).isEqualTo(50_000.0)
    assertThat(historyChunk.timestampCenter(TimestampIndex(1))).isEqualTo(50_099.9)
    assertThat(historyChunk.timestampCenter(TimestampIndex(2))).isEqualTo(50_100.0)
    assertThat(historyChunk.timestampCenter(TimestampIndex(3))).isEqualTo(50_100.1)

    assertThat(historyChunk.getEnumValue(EnumDataSeriesIndex(0), TimestampIndex(0))).isEqualTo(HistoryEnumSet(0b1))
    assertThat(historyChunk.getEnumValue(EnumDataSeriesIndex(0), TimestampIndex(1))).isEqualTo(HistoryEnumSet(0b11))

    assertThat(historyChunk.getEnumValue(EnumDataSeriesIndex(1), TimestampIndex(2))).isEqualTo(HistoryEnumSet(0b1111))
    assertThat(historyChunk.getEnumValue(EnumDataSeriesIndex(1), TimestampIndex(3))).isEqualTo(HistoryEnumSet(0b10001))

    assertThat(historyChunk.getEnumOrdinalMostTime(EnumDataSeriesIndex(1), TimestampIndex(3))).isEqualTo(HistoryEnumOrdinal(0))

    val historyStorage = InMemoryHistoryStorage()
    val samplingPeriod = SamplingPeriod.EveryMillisecond
    assertThat(samplingPeriod.toHistoryBucketRange()).isEqualTo(HistoryBucketRange.HundredMillis)

    historyStorage.storeWithoutCache(historyChunk, samplingPeriod)


    assertThat(historyStorage.keys()).hasSize(2)
    historyStorage.keys().first().let {
      assertThat(it.start).isEqualTo(50_000.0)
      assertThat(it.end).isEqualTo(50_100.0)
      assertThat(it.bucketRange).isEqualTo(HistoryBucketRange.HundredMillis)
    }

    historyStorage.keys().last().let {
      assertThat(it.start).isEqualTo(50_100.0)
      assertThat(it.end).isEqualTo(50_200.0)
      assertThat(it.bucketRange).isEqualTo(HistoryBucketRange.HundredMillis)
    }

    //Query
    val response = historyStorage.query(50_099.9, 50_100.0, samplingPeriod)
    assertThat(response).hasSize(2)

    if (false) {
      response.fastForEach {
        println("#################")
        println(it.chunk.dump())
      }
    }

    //Ensure the data has been added!
    response.first().let {
      assertThat(it.chunk.recordingType).isEqualTo(RecordingType.Measured)

      assertThat(it.chunk.timestampCenter(TimestampIndex(0))).isEqualTo(50_000.0)
      assertThat(it.chunk.timestampCenter(TimestampIndex(1))).isEqualTo(50_099.9)

      assertThat(it.chunk.getEnumValue(EnumDataSeriesIndex(0), TimestampIndex(0))).isEqualTo(HistoryEnumSet(0b1))
      assertThat(it.chunk.getEnumValue(EnumDataSeriesIndex(1), TimestampIndex(0))).isEqualTo(HistoryEnumSet(0b1011))


      assertThat(it.chunk.enumValuesAsMatrixString().trim()).isEqualTo(
        """
        0b1, 0b1011
        0b11, 0b1101
      """.trimIndent()
      )

    }
    response[1].let {
      assertThat(it.chunk.recordingType).isEqualTo(RecordingType.Measured)

      assertThat(it.chunk.timestampCenter(TimestampIndex(0))).isEqualTo(50_100.0)
      assertThat(it.chunk.timestampCenter(TimestampIndex(1))).isEqualTo(50_100.1)
      assertThat(it.chunk.getEnumValue(EnumDataSeriesIndex(0), TimestampIndex(1))).isEqualTo(HistoryEnumSet(0b111))
      assertThat(it.chunk.getEnumValue(EnumDataSeriesIndex(1), TimestampIndex(1))).isEqualTo(HistoryEnumSet(0b10001))

      assertThat(it.chunk.enumValuesAsMatrixString().trim()).isEqualTo(
        """
          0b101, 0b1111
          0b111, 0b10001
        """.trimIndent()
      )
    }

    val above = samplingPeriod.above()
    assertNotNull(above)
    assertThat(above).isEqualTo(SamplingPeriod.EveryTenMillis)

    historyStorage.query(50_099.9, 50_100.0, above).let {
      assertThat(it).isEmpty()
    }


    //Down sampling
    historyStorage.downSamplingService.recalculateDownSampling(TimeRanges.of(TimeRange(50_000.0, 51_000.0)), above.toHistoryBucketRange())

    historyStorage.query(50_099.9, 50_100.0, above).let {
      assertThat(it).hasSize(1)

      val bucket = it.first()
      val chunk = bucket.chunk

      assertThat(chunk.dump(to = 50_150.0)).isEqualComparingLinesTrim(
        """
           Start: 1970-01-01T00:00:50.005
           End:   1970-01-01T00:00:54.995
           Series counts:
             Decimals: 0
             Enums:    2
             RefId:    0
           RecordingType:    Calculated
           ---------------------------------------
           Indices:                     |               0               1  |
           IDs:                         |            1000            1001  |

              0 1970-01-01T00:00:50.005 |           1 (0)        1011 (0)  |
              1 1970-01-01T00:00:50.015 |           ? (?)           ? (?)  |
              2 1970-01-01T00:00:50.025 |           ? (?)           ? (?)  |
              3 1970-01-01T00:00:50.035 |           ? (?)           ? (?)  |
              4 1970-01-01T00:00:50.045 |           ? (?)           ? (?)  |
              5 1970-01-01T00:00:50.055 |           ? (?)           ? (?)  |
              6 1970-01-01T00:00:50.065 |           ? (?)           ? (?)  |
              7 1970-01-01T00:00:50.075 |           ? (?)           ? (?)  |
              8 1970-01-01T00:00:50.085 |           ? (?)           ? (?)  |
              9 1970-01-01T00:00:50.095 |          11 (0)        1101 (0)  |
             10 1970-01-01T00:00:50.105 |         111 (0)       11111 (0)  |
             11 1970-01-01T00:00:50.115 |           ? (?)           ? (?)  |
             12 1970-01-01T00:00:50.125 |           ? (?)           ? (?)  |
             13 1970-01-01T00:00:50.135 |           ? (?)           ? (?)  |
             14 1970-01-01T00:00:50.145 |           ? (?)           ? (?)  |
        """
      )

      assertThat(chunk.getEnumOrdinalMostTime(EnumDataSeriesIndex(0), TimestampIndex(3))).isEqualTo(HistoryEnumOrdinal.Pending)
      assertThat(chunk.getEnumOrdinalMostTime(EnumDataSeriesIndex(1), TimestampIndex(77))).isEqualTo(HistoryEnumOrdinal.Pending)

      TimestampIndex(0).let { timestampIndex ->
        chunk.timestampCenter(timestampIndex)
        assertThat(chunk.timestampCenter(timestampIndex)).isEqualTo(50_005.0)

        //addEnumValues(50_000.0, 0b1, 0b1011)
        assertThat(chunk.getEnumValue(EnumDataSeriesIndex(0), timestampIndex)).isEqualTo(HistoryEnumSet(0b1))
        assertThat(chunk.getEnumValue(EnumDataSeriesIndex(1), timestampIndex)).isEqualTo(HistoryEnumSet(0b1011))

        assertThat(chunk.getEnumOrdinalMostTime(EnumDataSeriesIndex(0), timestampIndex)).isEqualTo(HistoryEnumOrdinal(0))
        assertThat(chunk.getEnumOrdinalMostTime(EnumDataSeriesIndex(1), timestampIndex)).isEqualTo(HistoryEnumOrdinal(0))
      }

      TimestampIndex(9).let { timestampIndex ->
        chunk.timestampCenter(timestampIndex)
        assertThat(chunk.timestampCenter(timestampIndex)).isEqualTo(50_095.0)
        //addEnumValues(50_099.9, 0b11, 0b1101)
        assertThat(chunk.getEnumValue(EnumDataSeriesIndex(0), timestampIndex)).isEqualTo(HistoryEnumSet(0b11))
        assertThat(chunk.getEnumValue(EnumDataSeriesIndex(1), timestampIndex)).isEqualTo(HistoryEnumSet(0b1101))

        assertThat(chunk.getEnumOrdinalMostTime(EnumDataSeriesIndex(0), timestampIndex)).isEqualTo(HistoryEnumOrdinal(0))
        assertThat(chunk.getEnumOrdinalMostTime(EnumDataSeriesIndex(1), timestampIndex)).isEqualTo(HistoryEnumOrdinal(0))
      }

      TimestampIndex(10).let { timestampIndex ->
        chunk.timestampCenter(timestampIndex)
        assertThat(chunk.timestampCenter(timestampIndex)).isEqualTo(50_105.0)
        //addEnumValues(50_100.0, 0b101, 0b1111)
        //addEnumValues(50_100.1, 0b111, 0b10001)
        assertThat(chunk.getEnumValue(EnumDataSeriesIndex(0), timestampIndex)).isEqualTo(HistoryEnumSet(0b111))
        assertThat(chunk.getEnumValue(EnumDataSeriesIndex(1), timestampIndex)).isEqualTo(HistoryEnumSet(0b11111))

        assertThat(chunk.getEnumOrdinalMostTime(EnumDataSeriesIndex(0), timestampIndex)).isEqualTo(HistoryEnumOrdinal(0))
        assertThat(chunk.getEnumOrdinalMostTime(EnumDataSeriesIndex(1), timestampIndex)).isEqualTo(HistoryEnumOrdinal(0))
      }
    }

  }
}
