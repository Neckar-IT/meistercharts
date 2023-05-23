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

    val chunk = historyConfiguration.chunk(RecordingType.Measured) {
      assertThat(nextTimestampIndex).isEqualTo(TimestampIndex.zero)

      addValues(
        timestamp = 10_000.0,
        decimalValues = doubleArrayOf(17.0), minValues = doubleArrayOf(14.0), maxValues = doubleArrayOf(21.0),
        enumValues = intArrayOf(),
        enumOrdinalsMostTime = null,
        referenceEntryIds = intArrayOf(),
        referenceEntryStatuses = intArrayOf(),
        referenceEntryIdsCount = null,
        entryDataSet = emptySet()
      )

      assertThat(timestamps.size).isEqualTo(1)
      assertThat(historyValuesBuilder.timestampsCount).isEqualTo(1000) //will be resized on build
      assertThat(historyValuesBuilder.decimalDataSeriesCount).isEqualTo(1)
      assertThat(historyValuesBuilder.decimalValues.size).isEqualTo(1000) //1000 * 1

      assertThat(nextTimestampIndex).isEqualTo(TimestampIndex.one)

      assertThat(this.historyValuesBuilder.maxValues).isNotNull()
      assertThat(this.historyValuesBuilder.minValues).isNotNull()
      assertThat(this.historyValuesBuilder.enumOrdinalsMostTime).isNull()
      assertThat(this.historyValuesBuilder.referenceEntryDifferentIdsCount).isNull()

      assertThat(this.historyValuesBuilder.minValues.asMatrixString()).contains("14.0")
      assertThat(this.historyValuesBuilder.maxValues.asMatrixString()).contains("21.0")
    }

    assertThat(chunk.timeStampsCount).isEqualTo(1)
    assertThat(chunk.recordingType).isSameAs(RecordingType.Measured)

    assertThat(chunk.values.decimalHistoryValues.hasMinMax).isTrue()
    assertThat(chunk.values.decimalHistoryValues.minValues).isNotNull()
    assertThat(chunk.values.decimalHistoryValues.maxValues).isNotNull()
    assertThat(chunk.values.hasMinMax).isTrue()

    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex.zero, TimestampIndex.zero)).isEqualTo(17.0)
    assertThat(chunk.getMin(DecimalDataSeriesIndex.zero, TimestampIndex.zero)).isEqualTo(14.0)
    assertThat(chunk.getMax(DecimalDataSeriesIndex.zero, TimestampIndex.zero)).isEqualTo(21.0)
  }

  @Test
  fun testEnsureNull() {
    val dataSeriesId = DataSeriesId(1001)

    val historyConfiguration = historyConfiguration {
      decimalDataSeries(dataSeriesId, "series with min/max")
    }

    val chunk = historyConfiguration.chunk(RecordingType.Measured) {
      assertThat(nextTimestampIndex).isEqualTo(TimestampIndex.zero)

      addValues(
        timestamp = 10_000.0,
        decimalValues = doubleArrayOf(17.0), minValues = null, maxValues = null,
        enumValues = intArrayOf(),
        enumOrdinalsMostTime = null,
        referenceEntryIds = intArrayOf(),
        referenceEntryStatuses = intArrayOf(),
        referenceEntryIdsCount = null,
        entryDataSet = emptySet()
      )

      assertThat(timestamps.size).isEqualTo(1)
      assertThat(historyValuesBuilder.timestampsCount).isEqualTo(1000) //will be resized on build
      assertThat(historyValuesBuilder.decimalDataSeriesCount).isEqualTo(1)
      assertThat(historyValuesBuilder.decimalValues.size).isEqualTo(1000) //1000 * 1

      assertThat(nextTimestampIndex).isEqualTo(TimestampIndex.one)

      assertThat(this.historyValuesBuilder.maxValues).isNull()
      assertThat(this.historyValuesBuilder.minValues).isNull()
      assertThat(this.historyValuesBuilder.enumOrdinalsMostTime).isNull()
      assertThat(this.historyValuesBuilder.referenceEntryDifferentIdsCount).isNull()
    }

    assertThat(chunk.timeStampsCount).isEqualTo(1)
    assertThat(chunk.recordingType).isSameAs(RecordingType.Measured)
    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex.zero, TimestampIndex.zero)).isEqualTo(17.0)

    assertThat(chunk.getMin(DecimalDataSeriesIndex.zero, TimestampIndex.zero)).isEqualTo(17.0) //same as value
    assertThat(chunk.getMax(DecimalDataSeriesIndex.zero, TimestampIndex.zero)).isEqualTo(17.0) //same as value
  }

  @Test
  fun testMerge() {
    val dataSeriesId = DataSeriesId(1001)

    val historyConfiguration = historyConfiguration {
      decimalDataSeries(dataSeriesId, "series with min/max")
    }

    val chunk0 = historyConfiguration.chunk(RecordingType.Measured) {
      assertThat(nextTimestampIndex).isEqualTo(TimestampIndex.zero)

      addValues(
        timestamp = 10_000.0,
        decimalValues = doubleArrayOf(17.0), minValues = doubleArrayOf(14.0), maxValues = doubleArrayOf(21.0),
        enumValues = intArrayOf(),
        enumOrdinalsMostTime = null,
        referenceEntryIds = intArrayOf(),
        referenceEntryStatuses = intArrayOf(),
        referenceEntryIdsCount = null,
        entryDataSet = emptySet()
      )
    }

    val chunk1 = historyConfiguration.chunk(RecordingType.Measured) {
      assertThat(nextTimestampIndex).isEqualTo(TimestampIndex.zero)

      addValues(
        timestamp = 11_000.0,
        decimalValues = doubleArrayOf(18.0), minValues = doubleArrayOf(15.0), maxValues = doubleArrayOf(22.0),
        enumValues = intArrayOf(),
        enumOrdinalsMostTime = null,
        referenceEntryIds = intArrayOf(),
        referenceEntryStatuses = intArrayOf(),
        referenceEntryIdsCount = null,
        entryDataSet = emptySet()
      )
    }


    assertThat(chunk0.hasDecimalMinMaxValues())
    assertThat(chunk1.hasDecimalMinMaxValues())

    chunk0.merge(chunk1, 0.0, 30_000.0).let { merged ->
      requireNotNull(merged)
      assertThat(merged.values.decimalHistoryValues.values).containsAll(17.0, 18.0)
      assertThat(merged.hasDecimalMinMaxValues()).isTrue()
      assertThat(merged.values.decimalHistoryValues.minValues!!).containsAll(14.0, 15.0)
      assertThat(merged.values.decimalHistoryValues.maxValues!!).containsAll(21.0, 22.0)
    }
  }
}
