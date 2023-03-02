package com.meistercharts.history.impl

import assertk.*
import assertk.assertions.*
import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.ReferenceEntriesDataMap
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.historyConfiguration
import it.neckar.open.i18n.TextKey
import org.junit.jupiter.api.Test

class HistoryChunkMergeReferenceEntriesTest {
  val historyConfiguration: HistoryConfiguration = historyConfiguration {
    referenceEntryDataSeries(DataSeriesId(10), TextKey("state1"), ReferenceEntriesDataMap.generated)
    referenceEntryDataSeries(DataSeriesId(11), TextKey("state2"), ReferenceEntriesDataMap.generated)
    referenceEntryDataSeries(DataSeriesId(12), TextKey("state3"), ReferenceEntriesDataMap.generated)
  }

  @Test
  fun testWithEmpty() {
    val thisChunk = historyChunk(historyConfiguration) {
      addReferenceEntryValues(100.0, 1, 10, 101)
      addReferenceEntryValues(101.0, 10, 100, 1001)
      addReferenceEntryValues(102.0, 100, 1000, 10001)
      addReferenceEntryValues(103.0, 1000, 10000, 100001)
    }

    val merged = thisChunk.merge(historyChunk(historyConfiguration) {}, 0.0, 500.0)
    requireNotNull(merged)

    assertThat(merged.timeStampsCount).isEqualTo(4)
    assertThat(merged.timeStamps).containsExactly(100.0, 101.0, 102.0, 103.0)

    assertThat(merged).hasValues(ReferenceEntryDataSeriesIndex.zero, 1, 10, 100, 1000)
    assertThat(merged).hasValues(ReferenceEntryDataSeriesIndex.one, 10, 100, 1000, 10000)
    assertThat(merged).hasValues(ReferenceEntryDataSeriesIndex.two, 101, 1001, 10001, 100001)
  }

  @Test
  fun `this before that - all`() {
    val thisChunk = historyChunk(historyConfiguration) {
      addReferenceEntryValues(100.0, 1, 10, 101)
      addReferenceEntryValues(101.0, 10, 100, 1001)
      addReferenceEntryValues(102.0, 100, 1000, 10001)
      addReferenceEntryValues(103.0, 1000, 10000, 100001)
    }

    val thatChunk = historyChunk(historyConfiguration) {
      addReferenceEntryValues(105.0, 101, 1111, 11111)
      addReferenceEntryValues(106.0, 111, 1110, 11110)
      addReferenceEntryValues(107.0, 100, 1101, 11101)
      addReferenceEntryValues(108.0, 101, 1011, 11011)
    }

    val merged = thisChunk.merge(thatChunk, 0.0, 500.0)
    requireNotNull(merged)

    assertThat(merged.timeStampsCount).isEqualTo(8)
    assertThat(merged.timeStamps).containsExactly(100.0, 101.0, 102.0, 103.0, 105.0, 106.0, 107.0, 108.0)

    assertThat(merged).hasValues(ReferenceEntryDataSeriesIndex.zero, 1, 10, 100, 1000, 101, 111, 100, 101)
    assertThat(merged).hasValues(ReferenceEntryDataSeriesIndex.one, 10, 100, 1000, 10000, 1111, 1110, 1101, 1011)
    assertThat(merged).hasValues(ReferenceEntryDataSeriesIndex.two, 101, 1001, 10001, 100001, 11111, 11110, 11101, 11011)
  }

  @Test
  fun `that before this - all`() {
    val thisChunk = historyChunk(historyConfiguration) {
      addReferenceEntryValues(105.0, 101, 1111, 11111)
      addReferenceEntryValues(106.0, 111, 1110, 11110)
      addReferenceEntryValues(107.0, 100, 1101, 11101)
      addReferenceEntryValues(108.0, 101, 1011, 11011)
    }

    val thatChunk = historyChunk(historyConfiguration) {
      addReferenceEntryValues(100.0, 1, 10, 101)
      addReferenceEntryValues(101.0, 10, 100, 1001)
      addReferenceEntryValues(102.0, 100, 1000, 10001)
      addReferenceEntryValues(103.0, 1000, 10000, 100001)
    }

    val merged = thisChunk.merge(thatChunk, 0.0, 500.0)
    requireNotNull(merged)

    assertThat(merged.timeStampsCount).isEqualTo(8)
    assertThat(merged.timeStamps).containsExactly(100.0, 101.0, 102.0, 103.0, 105.0, 106.0, 107.0, 108.0)

    assertThat(merged).hasValues(ReferenceEntryDataSeriesIndex.zero, 1, 10, 100, 1000, 101, 111, 100, 101)
    assertThat(merged).hasValues(ReferenceEntryDataSeriesIndex.one, 10, 100, 1000, 10000, 1111, 1110, 1101, 1011)
    assertThat(merged).hasValues(ReferenceEntryDataSeriesIndex.two, 101, 1001, 10001, 100001, 11111, 11110, 11101, 11011)
  }

  @Test
  fun `this interwoven with that - this outside`() {
    val thisChunk = historyChunk(historyConfiguration) {
      addReferenceEntryValues(100.0, 1, 10, 101)
      addReferenceEntryValues(101.0, 10, 100, 1001)
      addReferenceEntryValues(102.0, 100, 1000, 10001)
      addReferenceEntryValues(103.0, 1000, 10000, 100001)
    }

    val thatChunk = historyChunk(historyConfiguration) {
      addReferenceEntryValues(100.5, 101, 1111, 11111)
      addReferenceEntryValues(101.5, 111, 1110, 11110)
      addReferenceEntryValues(102.5, 100, 1101, 11101)
    }

    val merged = thisChunk.merge(thatChunk, 0.0, 500.0)
    requireNotNull(merged)

    assertThat(merged.timeStampsCount).isEqualTo(7)
    assertThat(merged.timeStamps).containsExactly(100.0, 100.5, 101.0, 101.5, 102.0, 102.5, 103.0)

    assertThat(merged).hasValues(ReferenceEntryDataSeriesIndex.zero, 1, 101, 10, 111, 100, 100, 1000)
    assertThat(merged).hasValues(ReferenceEntryDataSeriesIndex.one, 10, 1111, 100, 1110, 1000, 1101, 10000)
    assertThat(merged).hasValues(ReferenceEntryDataSeriesIndex.two, 101, 11111, 1001, 11110, 10001, 11101, 100001)
  }

  @Test
  fun `this interwoven with that - that outside`() {
    val thisChunk = historyChunk(historyConfiguration) {
      addReferenceEntryValues(100.5, 101, 1111, 11111)
      addReferenceEntryValues(101.5, 111, 1110, 11110)
      addReferenceEntryValues(102.5, 100, 1101, 11101)
    }

    val thatChunk = historyChunk(historyConfiguration) {
      addReferenceEntryValues(100.0, 1, 10, 101)
      addReferenceEntryValues(101.0, 10, 100, 1001)
      addReferenceEntryValues(102.0, 100, 1000, 10001)
      addReferenceEntryValues(103.0, 1000, 10000, 100001)
    }

    val merged = thisChunk.merge(thatChunk, 0.0, 500.0)
    requireNotNull(merged)

    assertThat(merged.timeStampsCount).isEqualTo(7)
    assertThat(merged.timeStamps).containsExactly(100.0, 100.5, 101.0, 101.5, 102.0, 102.5, 103.0)

    assertThat(merged).hasValues(ReferenceEntryDataSeriesIndex.zero, 1, 101, 10, 111, 100, 100, 1000)
    assertThat(merged).hasValues(ReferenceEntryDataSeriesIndex.one, 10, 1111, 100, 1110, 1000, 1101, 10000)
    assertThat(merged).hasValues(ReferenceEntryDataSeriesIndex.two, 101, 11111, 1001, 11110, 10001, 11101, 100001)
  }
}
