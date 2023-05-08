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
