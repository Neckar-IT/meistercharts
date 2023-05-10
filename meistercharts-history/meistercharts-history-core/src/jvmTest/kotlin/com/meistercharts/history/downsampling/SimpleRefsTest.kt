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
import com.meistercharts.history.HistoryEnum
import com.meistercharts.history.historyConfiguration
import com.meistercharts.history.impl.HistoryChunk
import com.meistercharts.history.impl.historyChunk
import it.neckar.open.test.utils.isEqualComparingLinesTrim
import org.junit.jupiter.api.Test

class SimpleRefsTest {
  @Test
  fun testSimpleRefEntry() {
    val historyConfiguration = historyConfiguration {
      referenceEntryDataSeries(DataSeriesId(17), "Series A", HistoryEnum.Active)
    }

    val chunk = historyChunk(historyConfiguration) {
      this.addReferenceEntryValues(timestamp = nowForTests, referenceEntryValues = intArrayOf(99), referenceEntryStatuses = intArrayOf(0b101))
    }

    assertThat(chunk.timeStampsCount).isEqualTo(1)
    assertThat(chunk.enumDataSeriesCount).isEqualTo(0)
    assertThat(chunk.referenceEntryDataSeriesCount).isEqualTo(1)

    println(chunk.dump())

    assertThat(chunk.dump()).isEqualComparingLinesTrim(
      """
        Start: 2020-05-21T15:00:41.500
        End:   2020-05-21T15:00:41.500
        Series counts:
          Decimals: 0
          Enums:    0
          RefId:    1
        RecordingType:    Measured
        ---------------------------------------
        Indices:                     |  |                       0
        IDs:                         |  |                      17

           0 2020-05-21T15:00:41.500 |  |    99       0b101   (1)
      """.trimIndent()
    )
  }

  @Test
  fun testMergeTwoChunks() {
    val historyConfiguration = historyConfiguration {
      referenceEntryDataSeries(DataSeriesId(17), "Series A", HistoryEnum.Active)
    }

    val chunk0 = historyChunk(historyConfiguration) {
      this.addReferenceEntryValues(timestamp = nowForTests, referenceEntryValues = intArrayOf(99), referenceEntryStatuses = intArrayOf(0b101))
    }
    val chunk1: HistoryChunk = historyChunk(historyConfiguration) {
      this.addReferenceEntryValues(timestamp = nowForTests + 100.0, referenceEntryValues = intArrayOf(100), referenceEntryStatuses = intArrayOf(0b011))
    }

    chunk0.merge(chunk1, nowForTests, nowForTests + 1_000).let { merged ->
      requireNotNull(merged)

      assertThat(merged.dump()).isEqualComparingLinesTrim(
        """
          Start: 2020-05-21T15:00:41.500
          End:   2020-05-21T15:00:41.600
          Series counts:
            Decimals: 0
            Enums:    0
            RefId:    1
          RecordingType:    Measured
          ---------------------------------------
          Indices:                     |  |                       0
          IDs:                         |  |                      17

             0 2020-05-21T15:00:41.500 |  |    99       0b101   (1)
             1 2020-05-21T15:00:41.600 |  |   100        0b11   (1)
        """.trimIndent()
      )
    }

    chunk1.merge(chunk0, nowForTests, nowForTests + 1_000).let { merged ->
      requireNotNull(merged)

      assertThat(merged.dump()).isEqualComparingLinesTrim(
        """
          Start: 2020-05-21T15:00:41.500
          End:   2020-05-21T15:00:41.600
          Series counts:
            Decimals: 0
            Enums:    0
            RefId:    1
          RecordingType:    Measured
          ---------------------------------------
          Indices:                     |  |                       0
          IDs:                         |  |                      17

             0 2020-05-21T15:00:41.500 |  |    99       0b101   (1)
             1 2020-05-21T15:00:41.600 |  |   100        0b11   (1)
        """.trimIndent()
      )
    }
  }

}
