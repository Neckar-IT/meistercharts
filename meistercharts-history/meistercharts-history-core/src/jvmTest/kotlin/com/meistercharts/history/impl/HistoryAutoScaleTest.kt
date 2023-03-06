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

import assertk.*
import assertk.assertions.*
import com.meistercharts.algorithms.TimeRanges
import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.TimestampIndex
import com.meistercharts.history.downsampling.DownSamplingService
import com.meistercharts.history.historyConfiguration
import it.neckar.open.i18n.TextKey
import it.neckar.open.test.utils.isNaN
import it.neckar.open.unit.si.ms
import org.junit.jupiter.api.Test

/**
 *
 */
class HistoryAutoScaleTest {
  val storage: InMemoryHistoryStorage = InMemoryHistoryStorage()

  val historyConfiguration: HistoryConfiguration = historyConfiguration {
    decimalDataSeries(DataSeriesId(10), TextKey("temp", "Temperature"))
    decimalDataSeries(DataSeriesId(11), TextKey("height", "Height"))
  }

  @Test
  fun testSimpleMinMax() {
    @ms val start = 1000000.0

    val historyChunk = historyChunk(historyConfiguration) {
      addDecimalValues(start + 0, 31.0, 10.0)
      addDecimalValues(start + 10, 32.0, 11.0)
      addDecimalValues(start + 20, 33.0, 12.0)
    }

    assertThat(historyChunk.start).isEqualTo(1000000.0)
    assertThat(historyChunk.end).isEqualTo(1000020.0)

    storage.storeWithoutCache(historyChunk, SamplingPeriod.EveryHundredMillis)


    val result = storage.query(start, start + 20.0, SamplingPeriod.EveryHundredMillis)
    assertThat(result).hasSize(1)

    assertThat(result.first().chunk.getDecimalValue(DecimalDataSeriesIndex.zero, TimestampIndex(0))).isEqualTo(31.0)
    assertThat(result.first().chunk.getMax(DecimalDataSeriesIndex.zero, TimestampIndex(0))).isEqualTo(31.0)
    assertThat(result.first().chunk.getMin(DecimalDataSeriesIndex.zero, TimestampIndex(0))).isEqualTo(31.0)

    assertThat(result.first().chunk.getDecimalValue(DecimalDataSeriesIndex.one, TimestampIndex(1))).isEqualTo(11.0)
    assertThat(result.first().chunk.getMax(DecimalDataSeriesIndex.one, TimestampIndex(1))).isEqualTo(11.0)
    assertThat(result.first().chunk.getMin(DecimalDataSeriesIndex.one, TimestampIndex(1))).isEqualTo(11.0)
  }

  @Test
  fun testSimpleMinMaxDownSampled() {
    @ms val start = 1_000_000.0

    val measuredChunk = historyChunk(historyConfiguration) {
      addDecimalValues(start + 0, 31.0, 10.0)
      addDecimalValues(start + 10, 32.0, 11.0)
      addDecimalValues(start + 20, 33.0, 12.0)
    }

    assertThat(measuredChunk.start).isEqualTo(1000000.0)
    assertThat(measuredChunk.end).isEqualTo(1000020.0)

    val samplingPeriod = SamplingPeriod.EveryHundredMillis
    val samplingPeriodAbove = samplingPeriod.above()!!

    storage.storeWithoutCache(measuredChunk, samplingPeriod)


    val resultMeasured = storage.query(start, start + 20.0, samplingPeriod)
    assertThat(resultMeasured).hasSize(1)

    assertThat(resultMeasured.first().chunk.getDecimalValue(DecimalDataSeriesIndex.zero, TimestampIndex(0))).isEqualTo(31.0)
    assertThat(resultMeasured.first().chunk.getMax(DecimalDataSeriesIndex.zero, TimestampIndex(0))).isEqualTo(31.0)
    assertThat(resultMeasured.first().chunk.getMin(DecimalDataSeriesIndex.zero, TimestampIndex(0))).isEqualTo(31.0)

    assertThat(resultMeasured.first().chunk.getDecimalValue(DecimalDataSeriesIndex.one, TimestampIndex(1))).isEqualTo(11.0)
    assertThat(resultMeasured.first().chunk.getMax(DecimalDataSeriesIndex.one, TimestampIndex(1))).isEqualTo(11.0)
    assertThat(resultMeasured.first().chunk.getMin(DecimalDataSeriesIndex.one, TimestampIndex(1))).isEqualTo(11.0)


    val timeRanges = TimeRanges.of(measuredChunk.timeRange())
    DownSamplingService(storage).recalculateDownSampling(
      timeRanges, samplingPeriodAbove.toHistoryBucketRange()
    )

    val query = storage.query(start, start + 20.0, samplingPeriodAbove)
    assertThat(query.size).isEqualTo(1)

    val downSampledBucket = query[0]
    assertThat(downSampledBucket.samplingPeriod).isEqualTo(SamplingPeriod.EverySecond)

    assertThat(downSampledBucket.chunk.timeStampsCount).isEqualTo(600)
    assertThat(downSampledBucket.chunk.timestampCenter(TimestampIndex(0))).isEqualTo(600500.0)
    assertThat(downSampledBucket.chunk.start).isEqualTo(600_500.0)
    assertThat(downSampledBucket.chunk.end).isEqualTo(1199_500.0)

    val timestampIndex = downSampledBucket.chunk.bestTimestampIndexFor(start)
    assertThat(timestampIndex.nearIndex).isEqualTo(400)

    //println(measuredChunk.valuesAsMatrixString())
    //println("min")
    //println(measuredChunk.minValuesAsMatrixString())
    //println("Max")
    //println(measuredChunk.maxValuesAsMatrixString())
    //println("##############################")
    //
    //println("values")
    //println(downSampledBucket.chunk.valuesAsMatrixString())
    //
    //println("min:")
    //println(downSampledBucket.chunk.minValuesAsMatrixString())
    //
    //println("max:")
    //println(downSampledBucket.chunk.maxValuesAsMatrixString())

    assertThat(downSampledBucket.chunk.getDecimalValue(DecimalDataSeriesIndex.zero, TimestampIndex(0))).isNaN()

    assertThat(downSampledBucket.chunk.getDecimalValue(DecimalDataSeriesIndex.zero, TimestampIndex(400))).isEqualTo(32.0)
    assertThat(downSampledBucket.chunk.getMin(DecimalDataSeriesIndex.zero, TimestampIndex(400))).isEqualTo(31.0)
    assertThat(downSampledBucket.chunk.getMax(DecimalDataSeriesIndex.zero, TimestampIndex(400))).isEqualTo(33.0)

    println(downSampledBucket.chunk.toString())
  }
}
