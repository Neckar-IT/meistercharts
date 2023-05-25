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
import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.TimestampIndex
import com.meistercharts.history.historyConfiguration
import com.meistercharts.history.impl.HistoryChunk
import com.meistercharts.history.impl.HistoryChunk.Companion.isPending
import com.meistercharts.history.impl.chunk
import it.neckar.open.i18n.TextKey
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

/**
 * Reproduces a down sampling bug related to corona dashboard
 */
class DownSamplingNegativeMinMaxBugTest {
  @Test
  fun testBug() {
    val historyStorage = InMemoryHistoryStorage()
    val expectedSamplingPeriod = SamplingPeriod.EveryHundredMillis

    historyStorage.naturalSamplingPeriod = expectedSamplingPeriod

    val chunk = historyConfiguration {
      decimalDataSeries(DataSeriesId(1), TextKey.simple("simple"))
    }.chunk(
      timeStamps = doubleArrayOf(1000.0, 2000.0, 3000.0, 4000.0),
      decimalValues = arrayOf(doubleArrayOf(100.0, 20.0, -50.0, -70.0))
    )

    val downSamplingDirtyRangesCollector = DownSamplingDirtyRangesCollector()
    downSamplingDirtyRangesCollector.observe(historyStorage)

    historyStorage.storeWithoutCache(
      chunk, SamplingPeriod.EveryHundredMillis
    )

    historyStorage.downSamplingService.calculateDownSamplingIfRequired(downSamplingDirtyRangesCollector)

    val result = historyStorage.map[HistoryBucketDescriptor.forTimestamp(1000.0, expectedSamplingPeriod.above()!!)]
    assertNotNull(result)

    assertThat(result.chunk.firstTimestamp).isEqualTo(500.0)
    assertThat(result.chunk.timestampCenter(TimestampIndex(0))).isEqualTo(500.0)
    assertThat(result.chunk.timestampCenter(TimestampIndex(1))).isEqualTo(1500.0)
    assertThat(result.chunk.timestampCenter(TimestampIndex(2))).isEqualTo(2500.0)
    assertThat(result.chunk.timestampCenter(TimestampIndex(3))).isEqualTo(3500.0)
    assertThat(result.chunk.timestampCenter(TimestampIndex(4))).isEqualTo(4500.0)

    assertThat(result.chunk.getMin(DecimalDataSeriesIndex.zero, TimestampIndex(0))).isEqualTo(Double.NaN)
    assertThat(result.chunk.getMin(DecimalDataSeriesIndex.zero, TimestampIndex(1))).isEqualTo(100.0)
    assertThat(result.chunk.getMin(DecimalDataSeriesIndex.zero, TimestampIndex(2))).isEqualTo(20.0)
    assertThat(result.chunk.getMin(DecimalDataSeriesIndex.zero, TimestampIndex(3))).isEqualTo(-50.0)
    assertThat(result.chunk.getMin(DecimalDataSeriesIndex.zero, TimestampIndex(4))).isEqualTo(-70.0)

    assertThat(result.chunk.getMax(DecimalDataSeriesIndex.zero, TimestampIndex(0))).isEqualTo(Double.NaN)
    assertThat(result.chunk.getMax(DecimalDataSeriesIndex.zero, TimestampIndex(1))).isEqualTo(100.0)
    assertThat(result.chunk.getMax(DecimalDataSeriesIndex.zero, TimestampIndex(2))).isEqualTo(20.0)
    assertThat(result.chunk.getMax(DecimalDataSeriesIndex.zero, TimestampIndex(3))).isEqualTo(-50.0)
    assertThat(result.chunk.getMax(DecimalDataSeriesIndex.zero, TimestampIndex(4))).isEqualTo(-70.0)
  }

  @Test
  fun testMinMax() {
    assertThat(HistoryChunk.Pending.isPending()).isTrue()
    assertThat(HistoryChunk.Pending).isEqualTo(1.7976931348623157E308)

    assertThat(HistoryChunk.maxHistoryAware(100.0, 200.0)).isEqualTo(200.0)
    assertThat(HistoryChunk.maxHistoryAware(-100.0, 200.0)).isEqualTo(200.0)
    assertThat(HistoryChunk.maxHistoryAware(-100.0, -50.0)).isEqualTo(-50.0)
  }
}
