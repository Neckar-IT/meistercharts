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
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.HistoryEnum
import com.meistercharts.history.downsampling.createDemoEnumConfiguration
import com.meistercharts.history.historyConfiguration
import it.neckar.open.i18n.TextKey
import it.neckar.open.test.utils.isEqualComparingLinesTrim
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

class HistoryChunkRangeTest {
  @Test
  fun testRangeEnum() {
    val historyConfiguration: HistoryConfiguration = historyConfiguration {
      enumDataSeries(DataSeriesId(10), TextKey("state1"), createDemoEnumConfiguration(10))
      enumDataSeries(DataSeriesId(11), TextKey("state2"), createDemoEnumConfiguration(20))
      enumDataSeries(DataSeriesId(12), TextKey("state3"), createDemoEnumConfiguration(27))
    }

    val chunk = historyChunk(historyConfiguration, recordingType = RecordingType.Calculated) {
      addEnumValues(100.0, intArrayOf(0b1, 0b10, 0b101), intArrayOf(0b1, 0b10, 0b101))
      addEnumValues(101.0, intArrayOf(0b10, 0b100, 0b1001), intArrayOf(0b1, 0b10, 0b101))
      addEnumValues(102.0, intArrayOf(0b100, 0b1000, 0b10001), intArrayOf(0b1, 0b10, 0b101))
      addEnumValues(103.0, intArrayOf(0b1000, 0b10000, 0b100001), intArrayOf(0b1, 0b10, 0b101))
    }

    //All
    chunk.range(0.0, 10000000.0).let {
      assertNotNull(it)
      assertThat(it.timeStampsCount).isEqualTo(chunk.timeStampsCount)
      assertThat(it.timeStamps).isEqualTo(chunk.timeStamps)
      assertThat(it.enumValuesAsMatrixString()).isEqualTo(chunk.enumValuesAsMatrixString())
      assertThat(it.enumMostOfTheTimeValuesAsMatrixString()).isEqualTo(chunk.enumMostOfTheTimeValuesAsMatrixString())
      assertThat(it.dump()).isEqualComparingLinesTrim(chunk.dump())
    }

    //None before
    assertThat(chunk.range(0.0, 99.999)).isNull()
    //None after
    assertThat(chunk.range(200.0, 300.0)).isNull()

    chunk.range(100.5, 150.0).let {
      assertNotNull(it)
      assertThat(it.timeStampsCount).isEqualTo(3)
      assertThat(it.timeStamps).containsExactly(101.0, 102.0, 103.0)
      assertThat(it.enumValuesAsMatrixString().trim()).isEqualTo(
        """
          0b10, 0b100, 0b1001
          0b100, 0b1000, 0b10001
          0b1000, 0b10000, 0b100001
        """.trimIndent()
      )
      assertThat(it.enumMostOfTheTimeValuesAsMatrixString()?.trim()).isEqualTo(
        """
        0b1, 0b10, 0b101
        0b1, 0b10, 0b101
        0b1, 0b10, 0b101
        """.trimIndent()
      )
    }
  }

  @Test
  fun testReferenceEntries() {
    val historyConfiguration: HistoryConfiguration = historyConfiguration {
      referenceEntryDataSeries(DataSeriesId(10), TextKey("state1"), statusEnum = HistoryEnum.Active)
      referenceEntryDataSeries(DataSeriesId(11), TextKey("state2"), statusEnum = HistoryEnum.Active)
      referenceEntryDataSeries(DataSeriesId(12), TextKey("state3"), statusEnum = HistoryEnum.Boolean)
    }

    val chunk = historyChunk(historyConfiguration, recordingType = RecordingType.Calculated) {
      addReferenceEntryValues(timestamp = 100.0, referenceEntryValues = intArrayOf(1, 10, 101), referenceEntryIdsCount = intArrayOf(1, 2, 3), referenceEntryStatuses = intArrayOf(11, 12, 13))
      addReferenceEntryValues(timestamp = 101.0, referenceEntryValues = intArrayOf(10, 100, 1001), referenceEntryIdsCount = intArrayOf(1, 2, 4), referenceEntryStatuses = intArrayOf(11, 12, 14))
      addReferenceEntryValues(timestamp = 102.0, referenceEntryValues = intArrayOf(100, 1000, 10001), referenceEntryIdsCount = intArrayOf(1, 2, 5), referenceEntryStatuses = intArrayOf(11, 12, 15))
      addReferenceEntryValues(timestamp = 103.0, referenceEntryValues = intArrayOf(1000, 10000, 100001), referenceEntryIdsCount = intArrayOf(1, 2, 6), referenceEntryStatuses = intArrayOf(11, 12, 16))
    }

    //All
    chunk.range(0.0, 10000000.0).let {
      assertNotNull(it)
      assertThat(it.timeStampsCount).isEqualTo(chunk.timeStampsCount)
      assertThat(it.timeStamps).isEqualTo(chunk.timeStamps)
      assertThat(it.referenceEntryIdsAsMatrixString()).isEqualTo(chunk.referenceEntryIdsAsMatrixString())
      assertThat(it.dump()).isEqualComparingLinesTrim(chunk.dump())
    }

    //None before
    assertThat(chunk.range(0.0, 99.999)).isNull()
    //None after
    assertThat(chunk.range(200.0, 300.0)).isNull()

    chunk.range(100.5, 150.0).let {
      assertNotNull(it)
      assertThat(it.timeStampsCount).isEqualTo(3)
      assertThat(it.timeStamps).containsExactly(101.0, 102.0, 103.0)
      assertThat(it.referenceEntryIdsAsMatrixString().trim()).isEqualTo(
        """
          10, 100, 1001
          100, 1000, 10001
          1000, 10000, 100001
        """.trimIndent()
      )

      assertThat(it.referenceEntryCountsAsMatrixString()?.trim()).isEqualTo(
        """
          1, 2, 4
          1, 2, 5
          1, 2, 6
        """.trimIndent()
      )

      assertThat(it.referenceEntryStatusesAsMatrixString()?.trim()).isEqualTo(
        """
          11, 12, 14
          11, 12, 15
          11, 12, 16
        """.trimIndent()
      )
    }
  }
}
