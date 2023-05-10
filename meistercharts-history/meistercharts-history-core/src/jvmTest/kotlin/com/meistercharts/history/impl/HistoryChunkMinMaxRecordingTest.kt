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
import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.TimestampIndex
import com.meistercharts.history.historyConfiguration
import org.junit.jupiter.api.Test

class HistoryChunkMinMaxRecordingTest {
  @Test
  fun testRecordMinMaxCalculated() {
    val dataSeriesId = DataSeriesId(1001)

    val historyConfiguration = historyConfiguration {
      decimalDataSeries(dataSeriesId, "series with min/max")
    }

    val chunk = historyConfiguration.chunk(RecordingType.Calculated) {
      assertThat(nextTimestampIndex).isEqualTo(TimestampIndex.zero)

      addValues(
        timestamp = 10_000.0,
        decimalValues = doubleArrayOf(17.0), minValues = doubleArrayOf(14.0), maxValues = doubleArrayOf(21.0),
        enumValues = intArrayOf(),
        enumOrdinalsMostTime = intArrayOf(),
        referenceEntryIds = intArrayOf(),
        referenceEntryStatuses = intArrayOf(),
        referenceEntryIdsCount = intArrayOf(),
        entryDataSet = emptySet()
      )

      assertThat(timestamps.size).isEqualTo(1)
      assertThat(historyValuesBuilder.timestampsCount).isEqualTo(1000) //will be resized on build
      assertThat(historyValuesBuilder.decimalDataSeriesCount).isEqualTo(1)
      assertThat(historyValuesBuilder.decimalValues.size).isEqualTo(1000) //1000 * 1

      assertThat(nextTimestampIndex).isEqualTo(TimestampIndex.one)
    }

    assertThat(chunk.timeStampsCount).isEqualTo(1)
    assertThat(chunk.recordingType).isSameAs(RecordingType.Calculated)
    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex.zero, TimestampIndex.zero)).isEqualTo(17.0)
    assertThat(chunk.getMin(DecimalDataSeriesIndex.zero, TimestampIndex.zero)).isEqualTo(14.0)
    assertThat(chunk.getMax(DecimalDataSeriesIndex.zero, TimestampIndex.zero)).isEqualTo(21.0)
  }
}
