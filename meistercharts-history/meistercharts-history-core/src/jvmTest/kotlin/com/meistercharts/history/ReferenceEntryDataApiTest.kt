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
import com.meistercharts.history.impl.ReferenceEntryHistoryValues
import com.meistercharts.history.impl.historyChunk
import org.junit.jupiter.api.Test

/**
 * Shows how the [HistoryConfiguration] and [ReferenceEntriesDataMap] relate to each other
 */
class ReferenceEntryDataApiTest {
  @Test
  fun testIt() {
    val dataSeriesId = DataSeriesId(17)

    val historyConfiguration = historyConfiguration {
      referenceEntryDataSeries(dataSeriesId, "ref1", ReferenceEntriesDataMap.generated)
    }

    val chunk = historyChunk(historyConfiguration) {
      addReferenceEntryValues(10.0, 17)
      addReferenceEntryValues(99.0, 18)
    }

    assertThat(chunk.timeStampsCount).isEqualTo(2)

    val historyValues = chunk.values
    val referenceEntryHistoryValues: ReferenceEntryHistoryValues = historyValues.referenceEntryHistoryValues
    assertThat(referenceEntryHistoryValues.idsAsMatrixString()).isEqualTo(
      """
      17
      18
      """.trimIndent()
    )

    val dataSeriesIndex = historyConfiguration.referenceEntryConfiguration.getDataSeriesIndex(dataSeriesId)

    val entryData = chunk.getReferenceEntryData(dataSeriesIndex, ReferenceEntryId(17))
    assertThat(entryData?.label?.fallbackText).isEqualTo("Label 17")
  }
}
