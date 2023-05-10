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
import com.meistercharts.history.impl.bucket
import it.neckar.open.test.utils.isNaN
import it.neckar.open.time.toMillis
import it.neckar.open.unit.si.ms
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

class HistoryBucketFindTest {
  val historyConfiguration: HistoryConfiguration = historyConfiguration(
    decimalDataSeriesCount = 1,
    enumDataSeriesCount = 0,
    referenceEntrySeriesCount = 0,
    decimalDataSeriesInitializer = { dataSeriesIndex ->
      decimalDataSeries(DataSeriesId(dataSeriesIndex.value), "Foo")

    },
    enumDataSeriesInitializer = { dataSeriesIndex: EnumDataSeriesIndex -> },
    referenceEntryDataSeriesInitializer = { dataSeriesIndex: ReferenceEntryDataSeriesIndex -> })


  val millis = 1312003123123.0
  val instant = Instant.ofEpochMilli(millis.toLong())
  val dateTime = OffsetDateTime.ofInstant(instant, ZoneOffset.UTC)

  val time = OffsetDateTime.of(2020, 12, 24, 5, 18, 43, 123 * 1_000_000, ZoneOffset.UTC)


  lateinit var bucket0: HistoryBucket
  lateinit var bucket1: HistoryBucket

  lateinit var buckets: List<HistoryBucket>

  private val bucketRange: HistoryBucketRange = HistoryBucketRange.OneMinute
  private val samplingPeriod = bucketRange.samplingPeriod


  @BeforeEach
  fun setUp() {
    val descriptor0 = HistoryBucketDescriptor.forTimestamp(time.toMillis(), bucketRange)
    val descriptor1 = descriptor0.next()

    bucket0 = historyConfiguration.bucket(descriptor0) { dataSeriesIndex: DecimalDataSeriesIndex, timestamp: @ms Double ->
      7 + timestamp % 999.0
    }

    bucket1 = historyConfiguration.bucket(descriptor1) { dataSeriesIndex: DecimalDataSeriesIndex, timestamp: @ms Double ->
      8 + timestamp % 999.0
    }

    assertThat(bucket0.chunk.timeStamps).hasSize(600)
    assertThat(bucket1.chunk.timeStamps).hasSize(600)


    buckets = listOf(bucket0, bucket1)
  }

  @Test
  fun testChecks() {
    assertThat(bucket0.chunk.timestampCenter(TimestampIndex.zero)).isEqualToTimeStamp(bucket0.start)
    assertThat(bucket0.chunk.timestampCenter(TimestampIndex(599))).isEqualToTimeStamp(bucket0.end - samplingPeriod.distance)

    assertThat(bucket1.chunk.timestampCenter(TimestampIndex.zero)).isEqualToTimeStamp(bucket1.start)
    assertThat(bucket1.chunk.timestampCenter(TimestampIndex(599))).isEqualToTimeStamp(bucket1.end - samplingPeriod.distance)
  }

  @Test
  fun testFindDecimalValue() {
    assertThat(buckets.findDecimalValueAt(DecimalDataSeriesIndex.zero, bucket0.start)).isEqualTo(484.0)
    assertThat(buckets.findDecimalValueAt(DecimalDataSeriesIndex.zero, bucket0.end)).isEqualTo(545.0)
    assertThat(buckets.findDecimalValueAt(DecimalDataSeriesIndex.zero, bucket1.start)).isEqualTo(545.0)
    assertThat(buckets.findDecimalValueAt(DecimalDataSeriesIndex.zero, bucket1.end)).isNaN()

    assertThat(buckets.findDecimalValueAt(DecimalDataSeriesIndex.zero, bucket1.end - 100.0)).isEqualTo(505.0)
  }

  @Test
  fun testFindExactHits() {
    assertFound(bucket0.start, TimestampIndex(0), bucket0.start, bucket0)
    assertFound(bucket0.end, TimestampIndex(0), bucket0.end, bucket1) //finds the first element of the second bucket
    assertFound(bucket1.start, TimestampIndex(0), bucket1.start, bucket1) //finds the first element of the second bucket
    assertFoundNothing(bucket1.end)

    //
    assertFound(bucket0.chunk.firstTimestamp, TimestampIndex(0), bucket0.start, bucket0)
    assertFound(bucket0.chunk.lastTimestamp, TimestampIndex(599), bucket0.chunk.lastTimestamp, bucket0)
    assertFound(bucket1.chunk.firstTimestamp, TimestampIndex(0), bucket1.chunk.firstTimestamp, bucket1)
    assertFound(bucket1.chunk.lastTimestamp, TimestampIndex(599), bucket1.chunk.lastTimestamp, bucket1)
  }

  @Test
  fun testFindBefore() {
    assertFoundNothing(bucket0.chunk.firstTimestamp - 1.0)
    assertFound(bucket0.chunk.firstTimestamp + 1.0, TimestampIndex(0), bucket0.chunk.firstTimestamp, bucket0)

    assertFound(bucket0.chunk.lastTimestamp - 1.0, TimestampIndex(598), bucket0.chunk.lastTimestamp - samplingPeriod.distance, bucket0)
    assertFound(bucket0.chunk.lastTimestamp + 1.0, TimestampIndex(599), bucket0.chunk.lastTimestamp, bucket0)


    assertFound(bucket1.chunk.firstTimestamp + 1.0, TimestampIndex(0), bucket1.chunk.firstTimestamp, bucket1)

    assertFound(bucket1.chunk.lastTimestamp - 1.0, TimestampIndex(598), bucket1.chunk.lastTimestamp - samplingPeriod.distance, bucket1)
    assertFound(bucket1.chunk.lastTimestamp + 1.0, TimestampIndex(599), bucket1.chunk.lastTimestamp, bucket1)
  }

  private fun assertFoundNothing(timestamp: @ms Double) {
    buckets.find(timestamp) { bucket, timestampIndex ->
      fail("Expected to find nothing but found $timestampIndex")
    }
  }

  private fun assertFound(timestamp: @ms Double, expectedTimestampIndex: TimestampIndex, expectedTime: @ms Double, expectedBucket: HistoryBucket) {
    //exact hit
    var called = false
    var foundBucket: HistoryBucket? = null
    var foundTimestampIndex: TimestampIndex? = null

    buckets.find(timestamp) { bucket, timestampIndex ->
      called = true

      foundBucket = bucket
      foundTimestampIndex = timestampIndex
    }

    assertThat(called, "nothing found").isTrue()

    assertThat(foundBucket).isSameAs(expectedBucket)
    assertThat(foundTimestampIndex).isEqualTo(expectedTimestampIndex)
    requireNotNull(foundBucket)
    assertThat(foundBucket!!.chunk.timestampCenter(expectedTimestampIndex)).isEqualToTimeStamp(expectedTime)
  }
}
