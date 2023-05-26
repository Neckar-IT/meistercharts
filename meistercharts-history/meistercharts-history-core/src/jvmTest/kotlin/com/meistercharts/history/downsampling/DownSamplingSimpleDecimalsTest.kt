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
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.TimestampIndex
import com.meistercharts.history.historyConfiguration
import com.meistercharts.history.impl.RecordingType
import com.meistercharts.history.impl.chunk
import it.neckar.open.formatting.formatUtc
import org.junit.jupiter.api.Test

class DownSamplingSimpleDecimalsTest {
  private val historyConfiguration = historyConfiguration {
    decimalDataSeries(DataSeriesId(17), "Dec Series A")
  }

  @Test
  fun testDownSamplingMinMaxRecording() {
    val samplingPeriodDownSampled = SamplingPeriod.EveryHundredMillis.above()
    assertThat(samplingPeriodDownSampled).isEqualTo(SamplingPeriod.EverySecond)

    val downSampledDescriptor = HistoryBucketDescriptor.forTimestamp(nowForTests, samplingPeriodDownSampled!!.toHistoryBucketRange())

    val childDescriptor0 = downSampledDescriptor.children()[0]
    val childDescriptor1 = downSampledDescriptor.children()[1]


    assertThat(childDescriptor0.start.formatUtc()).isEqualTo("2020-05-21T15:00:00.000")
    assertThat(childDescriptor0.end.formatUtc()).isEqualTo("2020-05-21T15:01:00.000")
    assertThat(childDescriptor1.start.formatUtc()).isEqualTo("2020-05-21T15:01:00.000")
    assertThat(childDescriptor1.end.formatUtc()).isEqualTo("2020-05-21T15:02:00.000")

    val bucket0 = historyConfiguration.chunk {
      addDecimalValuesWithMinMax(timestamp = childDescriptor0.start, decimalValues = doubleArrayOf(10.0), minValues = doubleArrayOf(7.0), maxValues = doubleArrayOf(12.0))
      addDecimalValuesWithMinMax(timestamp = childDescriptor0.start + 101, decimalValues = doubleArrayOf(20.0), minValues = doubleArrayOf(8.0), maxValues = doubleArrayOf(21.0))
    }.toBucket(childDescriptor0)

    val bucket1 = historyConfiguration.chunk {
      addDecimalValuesWithMinMax(timestamp = childDescriptor1.start, decimalValues = doubleArrayOf(9.5), minValues = doubleArrayOf(6.5), maxValues = doubleArrayOf(12.5))
      addDecimalValuesWithMinMax(timestamp = childDescriptor1.start + 101, decimalValues = doubleArrayOf(20.5), minValues = doubleArrayOf(8.5), maxValues = doubleArrayOf(21.5))
    }.toBucket(childDescriptor1)

    assertThat(bucket0.chunk.recordingType).isEqualTo(RecordingType.Measured)
    assertThat(bucket1.chunk.recordingType).isEqualTo(RecordingType.Measured)

    val downSampled = downSampledDescriptor.calculateDownSampled(listOf(bucket0, bucket1))

    assertThat(downSampled.start.formatUtc()).isEqualTo("2020-05-21T15:00:00.000")
    assertThat(downSampled.end.formatUtc()).isEqualTo("2020-05-21T15:10:00.000")

    //println(downSampled.chunk.dump())

    assertThat(downSampled.chunk.timestampCenter(TimestampIndex.zero).formatUtc()).isEqualTo("2020-05-21T15:00:00.500")
    assertThat(downSampled.chunk.getDecimalValue(DecimalDataSeriesIndex.zero, TimestampIndex.zero)).isEqualTo((10.0 + 20.0) / 2.0)
    assertThat(downSampled.chunk.getMin(DecimalDataSeriesIndex.zero, TimestampIndex.zero)).isEqualTo(7.0)
    assertThat(downSampled.chunk.getMax(DecimalDataSeriesIndex.zero, TimestampIndex.zero)).isEqualTo(21.0)

    assertThat(downSampled.chunk.timestampCenter(TimestampIndex(60)).formatUtc()).isEqualTo("2020-05-21T15:01:00.500")
    assertThat(downSampled.chunk.getDecimalValue(DecimalDataSeriesIndex.zero, TimestampIndex(60))).isEqualTo((9.5 + 20.5) / 2.0)
    assertThat(downSampled.chunk.getMin(DecimalDataSeriesIndex.zero, TimestampIndex(60))).isEqualTo(6.5)
    assertThat(downSampled.chunk.getMax(DecimalDataSeriesIndex.zero, TimestampIndex(60))).isEqualTo(21.5)
  }
}
