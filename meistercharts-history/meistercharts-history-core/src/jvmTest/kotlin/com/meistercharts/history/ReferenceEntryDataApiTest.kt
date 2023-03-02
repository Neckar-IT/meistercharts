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
