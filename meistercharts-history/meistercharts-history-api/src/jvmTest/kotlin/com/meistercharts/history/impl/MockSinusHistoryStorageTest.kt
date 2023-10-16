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
package com.meistercharts.history.impl

import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.SamplingPeriod
import assertk.*
import assertk.assertions.*

import org.junit.jupiter.api.Test

/**
 */
class MockSinusHistoryStorageTest {
  @Test
  fun testIt() {
    val storage = MockSinusHistoryStorage()
    val millis = 1.584360973214E12
    val bucket = storage.get(HistoryBucketDescriptor.Companion.forTimestamp(millis, SamplingPeriod.EveryTenSeconds))

    val chunk = bucket.chunk
    val historyValues = chunk.values


    for (dataSeriesIndex in 0 until chunk.decimalDataSeriesCount) {
      val (min, max) = chunk.findMinMaxValue(DecimalDataSeriesIndex(dataSeriesIndex))

      assertThat(min).isGreaterThan(-11000.0)
      assertThat(max).isLessThan(11000.0)

      assertThat(MockSinusHistoryStorage.valueRange.contains(min)).isTrue()
      assertThat(MockSinusHistoryStorage.valueRange.contains(max)).isTrue()
    }
  }

  @Test
  fun testCreateSinus() {
    val descriptor = HistoryBucketDescriptor.forTimestamp(100000.0, SamplingPeriod.EveryTenMillis)
    val historyChunk = createSinusChunk(descriptor)

    assertThat(historyChunk).isNotNull()
    assertThat(historyChunk.decimalDataSeriesCount).isEqualTo(3)
    assertThat(historyChunk.enumDataSeriesCount).isEqualTo(0)

    assertThat(historyChunk.timeStampsCount).isEqualTo(500)

    assertThat(historyChunk.values.decimalHistoryValues.values.size).isEqualTo(500 * 3)
    assertThat(historyChunk.values.enumHistoryValues.values.size).isEqualTo(0)
  }
}
