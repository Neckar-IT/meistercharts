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
import com.meistercharts.history.impl.HistoryChunk
import com.meistercharts.history.impl.chunk
import it.neckar.open.i18n.TextKey
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 *
 */
class HistorySearchKtTest {
  val historyConfiguration: HistoryConfiguration = historyConfiguration {
    decimalDataSeries(DataSeriesId(2), TextKey.simple("DS 2"), HistoryUnit.None)
    decimalDataSeries(DataSeriesId(4), TextKey.simple("DS 4"), HistoryUnit.None)
    decimalDataSeries(DataSeriesId(6), TextKey.simple("DS 6"), HistoryUnit.None)
  }

  lateinit var chunk: HistoryChunk

  val samplingPeriod: SamplingPeriod = SamplingPeriod.EveryHundredMillis
  lateinit var bucket: HistoryBucket

  @BeforeEach
  internal fun setUp() {
    chunk = historyConfiguration.chunk {
      addDecimalValues(100.0, 1.0, 1.0, 1.0)
      addDecimalValues(200.0, 2.0, 2.0, 2.0)
      addDecimalValues(300.0, HistoryChunk.NoValue, HistoryChunk.NoValue, HistoryChunk.NoValue)
      addDecimalValues(400.0, 4.0, 4.0, 4.0)
      addDecimalValues(500.0, HistoryChunk.Pending, HistoryChunk.Pending, HistoryChunk.Pending)
    }

    val descriptor = HistoryBucketDescriptor.forTimestamp(chunk.firstTimeStamp(), samplingPeriod)
    bucket = HistoryBucket(descriptor, chunk)
  }

  @Test
  fun testSearchAndBeforeSingleBucket() {
    val buckets = listOf(bucket)

    //test exact hit
    buckets.search(100.0, AndBefore(0.0)).let {
      require(it != null)

      assertThat(it.chunk).isSameAs(chunk)
      assertThat(it.timeStampIndex.value).isEqualTo(0)
    }

    assertThat(buckets.search(101.0, AndBefore(0.0))).isNull()
    assertThat(buckets.search(99.0, AndBefore(0.0))).isNull()
    assertThat(buckets.search(99.0, AndBefore(50.0))).isNull()
    assertThat(buckets.search(1.0, AndBefore(50.0))).isNull()
    assertThat(buckets.search(1.0, AndBefore(500000000000.0))).isNull()

    assertThat(buckets.search(150.0, AndBefore(30.0))).isNull()
    assertThat(buckets.search(305.0, AndBefore(4.0))).isNull()


    buckets.search(101.0, AndBefore(1.0)).let {
      require(it != null)

      assertThat(it.chunk).isSameAs(chunk)
      assertThat(it.timeStampIndex.value).isEqualTo(0)
    }

    buckets.search(350.0, AndBefore(70.0)).let {
      require(it != null)

      assertThat(it.chunk).isSameAs(chunk)
      assertThat(it.timeStampIndex.value).isEqualTo(2)
    }

    buckets.search(250.0, AndBefore(70.0)).let {
      require(it != null)

      assertThat(it.chunk).isSameAs(chunk)
      assertThat(it.timeStampIndex.value).isEqualTo(1)
    }

    //sample at 500 is pending and max distance is 0
    assertThat(buckets.search(500.0, AndBefore(0.0))).isNull()
    //sample at 500 is pending and max distance is too small to find the previous sample at 400.0
    assertThat(buckets.search(500.0, AndBefore(99.0))).isNull()
    //sample at 500 is pending; expect to find the previous sample at 400.0
    buckets.search(500.0, AndBefore(100.0)).let {
      require(it != null)

      assertThat(it.chunk).isSameAs(chunk)
      assertThat(it.timeStampIndex.value).isEqualTo(3)
    }

  }

  @Test
  fun testSearchAndBeforeMultipleBuckets() {
    val firstChunk = historyConfiguration.chunk() {
      addDecimalValues(10.0, 100.0, 100.0, 100.0)
      addDecimalValues(20.0, HistoryChunk.NoValue, HistoryChunk.NoValue, HistoryChunk.NoValue)
      addDecimalValues(320.0, HistoryChunk.Pending, HistoryChunk.Pending, HistoryChunk.Pending)
    }

    val firstBucket = HistoryBucket(HistoryBucketDescriptor.forTimestamp(firstChunk.firstTimeStamp(), samplingPeriod), firstChunk)

    val buckets = listOf(firstBucket, bucket)

    //test exact hit first bucket
    buckets.search(20.0, AndBefore(0.0)).let {
      require(it != null)

      assertThat(it.chunk).isSameAs(firstChunk)
      assertThat(it.timeStampIndex.value).isEqualTo(1)
    }

    //test exact hit second bucket
    buckets.search(100.0, AndBefore(0.0)).let {
      require(it != null)

      assertThat(it.chunk).isSameAs(chunk)
      assertThat(it.timeStampIndex.value).isEqualTo(0)
    }

    // test misses with small max distance for first bucket
    assertThat(buckets.search(11.0, AndBefore(0.0))).isNull()
    assertThat(buckets.search(21.0, AndBefore(0.0))).isNull()
    assertThat(buckets.search(31.0, AndBefore(0.0))).isNull()

    // test misses with small max distance for second bucket
    assertThat(buckets.search(101.0, AndBefore(0.0))).isNull()
    assertThat(buckets.search(201.0, AndBefore(0.0))).isNull()
    assertThat(buckets.search(301.0, AndBefore(0.0))).isNull()

    // test too small a timestamp
    assertThat(buckets.search(9.0, AndBefore(100.0))).isNull()

    // test hit first bucket
    buckets.search(11.0, AndBefore(1.0)).let {
      require(it != null)

      assertThat(it.chunk).isSameAs(firstChunk)
      assertThat(it.timeStampIndex.value).isEqualTo(0)
    }

    // test miss first bucket with pending
    assertThat(buckets.search(30.0, AndBefore(0.0))).isNull()

    // test hit second bucket
    buckets.search(101.0, AndBefore(1.0)).let {
      require(it != null)

      assertThat(it.chunk).isSameAs(chunk)
      assertThat(it.timeStampIndex.value).isEqualTo(0)
    }

    // test turn around with too small a distance
    assertThat(buckets.search(99.0, AndBefore(1.0))).isNull()

    // test turn around with hit
    buckets.search(99.0, AndBefore(100.0)).let {
      require(it != null)

      assertThat(it.chunk).isSameAs(firstChunk)
      assertThat(it.timeStampIndex.value).isEqualTo(1) // 2 is pending
    }
  }

  @Test
  fun testSearchExact() {
    assertThat(chunk.start).isEqualTo(100.0)

    listOf(bucket).search(100.0, Exact).let {
      require(it != null)

      assertThat(it.chunk).isSameAs(chunk)
      assertThat(it.timeStampIndex.value).isEqualTo(0)
    }

    listOf(bucket).search(200.0, Exact).let {
      require(it != null)

      assertThat(it.chunk).isSameAs(chunk)
      assertThat(it.timeStampIndex.value).isEqualTo(1)
    }

    //The sample at 300 is a gap but should still be found
    listOf(bucket).search(300.0, Exact).let {
      require(it != null)

      assertThat(it.chunk).isSameAs(chunk)
      assertThat(it.timeStampIndex.value).isEqualTo(2)
    }

    //The sample at 500 is pending, hence it should not be found
    assertThat(listOf(bucket).search(500.0, Exact)).isNull()

    assertThat(listOf(bucket).search(301.0, Exact)).isNull()
    assertThat(listOf(bucket).search(299.0, Exact)).isNull()
    assertThat(listOf(bucket).search(0.1, Exact)).isNull()
    assertThat(listOf(bucket).search(0.7, Exact)).isNull()
  }
}
