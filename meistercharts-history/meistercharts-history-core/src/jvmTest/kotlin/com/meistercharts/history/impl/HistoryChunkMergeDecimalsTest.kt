package com.meistercharts.history.impl

import assertk.*
import assertk.assertions.*
import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.historyConfiguration
import it.neckar.open.i18n.TextKey
import org.junit.jupiter.api.Test

/**
 *
 */
class HistoryChunkMergeDecimalsTest {
  val historyConfiguration: HistoryConfiguration = historyConfiguration {
    decimalDataSeries(DataSeriesId(10), TextKey("temp", "Temperature"))
    decimalDataSeries(DataSeriesId(11), TextKey("height", "Height"))
    decimalDataSeries(DataSeriesId(12), TextKey("volume", "Volume"))
  }

  @Test
  fun `this before that - all`() {
    val thisChunk = historyChunk(historyConfiguration) {
      addDecimalValues(100.0, 10.0, 20.0, 30.0)
      addDecimalValues(101.0, 10.1, 20.1, 30.1)
      addDecimalValues(102.0, 10.2, 20.2, 30.2)
      addDecimalValues(103.0, 10.3, 20.3, 30.3)
    }

    val thatChunk = historyChunk(historyConfiguration) {
      addDecimalValues(105.0, 11.0, 21.0, 31.0)
      addDecimalValues(106.0, 11.1, 21.1, 31.1)
      addDecimalValues(107.0, 11.2, 21.2, 31.2)
      addDecimalValues(108.0, 11.3, 21.3, 31.3)
    }

    val merged = thisChunk.merge(thatChunk, 0.0, 500.0)
    requireNotNull(merged)

    assertThat(merged.timeStampsCount).isEqualTo(8)
    assertThat(merged.timeStamps).containsExactly(100.0, 101.0, 102.0, 103.0, 105.0, 106.0, 107.0, 108.0)

    assertThat(merged).hasValues(DecimalDataSeriesIndex.zero, 10.0, 10.1, 10.2, 10.3, 11.0, 11.1, 11.2, 11.3)
    assertThat(merged).hasValues(DecimalDataSeriesIndex.one, 20.0, 20.1, 20.2, 20.3, 21.0, 21.1, 21.2, 21.3)
    assertThat(merged).hasValues(DecimalDataSeriesIndex.two, 30.0, 30.1, 30.2, 30.3, 31.0, 31.1, 31.2, 31.3)
  }

  @Test
  fun `that before this - all`() {
    val thisChunk = historyChunk(historyConfiguration) {
      addDecimalValues(105.0, 10.0, 20.0, 30.0)
      addDecimalValues(106.0, 10.1, 20.1, 30.1)
      addDecimalValues(107.0, 10.2, 20.2, 30.2)
      addDecimalValues(108.0, 10.3, 20.3, 30.3)
    }

    val thatChunk = historyChunk(historyConfiguration) {
      addDecimalValues(100.0, 11.0, 21.0, 31.0)
      addDecimalValues(101.0, 11.1, 21.1, 31.1)
      addDecimalValues(102.0, 11.2, 21.2, 31.2)
      addDecimalValues(103.0, 11.3, 21.3, 31.3)
    }

    val merged = thisChunk.merge(thatChunk, 0.0, 500.0)
    requireNotNull(merged)

    assertThat(merged.timeStampsCount).isEqualTo(8)
    assertThat(merged.timeStamps).containsExactly(100.0, 101.0, 102.0, 103.0, 105.0, 106.0, 107.0, 108.0)

    assertThat(merged).hasValues(DecimalDataSeriesIndex.zero, 11.0, 11.1, 11.2, 11.3, 10.0, 10.1, 10.2, 10.3)
    assertThat(merged).hasValues(DecimalDataSeriesIndex.one, 21.0, 21.1, 21.2, 21.3, 20.0, 20.1, 20.2, 20.3)
    assertThat(merged).hasValues(DecimalDataSeriesIndex.two, 31.0, 31.1, 31.2, 31.3, 30.0, 30.1, 30.2, 30.3)
  }

  @Test
  fun `this interwoven with that - this outside`() {
    val thisChunk = historyChunk(historyConfiguration) {
      addDecimalValues(100.0, 10.0, 20.0, 30.0)
      addDecimalValues(101.0, 10.1, 20.1, 30.1)
      addDecimalValues(102.0, 10.2, 20.2, 30.2)
      addDecimalValues(103.0, 10.3, 20.3, 30.3)
    }

    val thatChunk = historyChunk(historyConfiguration) {
      addDecimalValues(100.5, 11.0, 21.0, 31.0)
      addDecimalValues(101.5, 11.1, 21.1, 31.1)
      addDecimalValues(102.5, 11.2, 21.2, 31.2)
    }

    val merged = thisChunk.merge(thatChunk, 0.0, 500.0)
    requireNotNull(merged)

    assertThat(merged.timeStampsCount).isEqualTo(7)
    assertThat(merged.timeStamps).containsExactly(100.0, 100.5, 101.0, 101.5, 102.0, 102.5, 103.0)

    assertThat(merged).hasValues(DecimalDataSeriesIndex.zero, 10.0, 11.0, 10.1, 11.1, 10.2, 11.2, 10.3)
    assertThat(merged).hasValues(DecimalDataSeriesIndex.one, 20.0, 21.0, 20.1, 21.1, 20.2, 21.2, 20.3)
    assertThat(merged).hasValues(DecimalDataSeriesIndex.two, 30.0, 31.0, 30.1, 31.1, 30.2, 31.2, 30.3)
  }

  @Test
  fun `this interwoven with that - that outside`() {
    val thisChunk = historyChunk(historyConfiguration) {
      addDecimalValues(100.5, 11.0, 21.0, 31.0)
      addDecimalValues(101.5, 11.1, 21.1, 31.1)
      addDecimalValues(102.5, 11.2, 21.2, 31.2)
    }

    val thatChunk = historyChunk(historyConfiguration) {
      addDecimalValues(100.0, 10.0, 20.0, 30.0)
      addDecimalValues(101.0, 10.1, 20.1, 30.1)
      addDecimalValues(102.0, 10.2, 20.2, 30.2)
      addDecimalValues(103.0, 10.3, 20.3, 30.3)
    }

    val merged = thisChunk.merge(thatChunk, 0.0, 500.0)
    requireNotNull(merged)

    assertThat(merged.timeStampsCount).isEqualTo(7)
    assertThat(merged.timeStamps).containsExactly(100.0, 100.5, 101.0, 101.5, 102.0, 102.5, 103.0)

    assertThat(merged).hasValues(DecimalDataSeriesIndex.zero, 10.0, 11.0, 10.1, 11.1, 10.2, 11.2, 10.3)
    assertThat(merged).hasValues(DecimalDataSeriesIndex.one, 20.0, 21.0, 20.1, 21.1, 20.2, 21.2, 20.3)
    assertThat(merged).hasValues(DecimalDataSeriesIndex.two, 30.0, 31.0, 30.1, 31.1, 30.2, 31.2, 30.3)
  }
}
