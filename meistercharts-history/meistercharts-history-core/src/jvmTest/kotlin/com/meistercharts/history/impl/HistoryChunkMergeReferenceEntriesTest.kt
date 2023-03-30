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
import com.meistercharts.history.DefaultReferenceEntriesDataMap
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.HistoryEnum
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.ReferenceEntriesDataMap
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.TimestampIndex
import com.meistercharts.history.historyConfiguration
import com.meistercharts.history.isEqualToHistoryEnumSet
import it.neckar.open.i18n.TextKey
import org.junit.jupiter.api.Test

class HistoryChunkMergeReferenceEntriesTest {
  val historyConfiguration: HistoryConfiguration = historyConfiguration {
    referenceEntryDataSeries(DataSeriesId(10), TextKey("state1"), statusEnum = HistoryEnum.Active)
    referenceEntryDataSeries(DataSeriesId(11), TextKey("state2"), statusEnum = HistoryEnum.Boolean)
    referenceEntryDataSeries(DataSeriesId(12), TextKey("state3"), statusEnum = HistoryEnum.Active)
  }

  @Test
  fun testWithEmpty() {
    val thisChunk = historyChunk(historyConfiguration) {
      addReferenceEntryValues(100.0, 1, 10, 101, referenceEntriesDataMap = ReferenceEntriesDataMap.generated, referenceEntryStatuses = intArrayOf(0b101, 0b1001, 0b10001))
      addReferenceEntryValues(101.0, 10, 100, 1001, referenceEntriesDataMap = ReferenceEntriesDataMap.generated, referenceEntryStatuses = intArrayOf(0b100, 0b1000, 0b10000))
      addReferenceEntryValues(102.0, 100, 1000, 10001, referenceEntriesDataMap = ReferenceEntriesDataMap.generated, referenceEntryStatuses = intArrayOf(HistoryEnumSet.NoValueAsInt, HistoryEnumSet.NoValueAsInt, HistoryEnumSet.NoValueAsInt))
      addReferenceEntryValues(103.0, 1000, 10000, 100001, referenceEntriesDataMap = ReferenceEntriesDataMap.generated, referenceEntryStatuses = intArrayOf(HistoryEnumSet.PendingAsInt, HistoryEnumSet.PendingAsInt, HistoryEnumSet.PendingAsInt))
    }

    assertThat(thisChunk.values.referenceEntryHistoryValues.statusesAsMatrixString()).isEqualTo(
      """
        0b101, 0b1001, 0b10001
        0b100, 0b1000, 0b10000
        -, -, -
        ?, ?, ?
    """.trimIndent()
    )

    assertThat(thisChunk.getReferenceEntryStatus(ReferenceEntryDataSeriesIndex(0), TimestampIndex(0))).isEqualToHistoryEnumSet(0b101)
    assertThat(thisChunk.getReferenceEntryStatus(ReferenceEntryDataSeriesIndex(1), TimestampIndex(0))).isEqualToHistoryEnumSet(0b1001)
    assertThat(thisChunk.getReferenceEntryStatus(ReferenceEntryDataSeriesIndex(2), TimestampIndex(0))).isEqualToHistoryEnumSet(0b10001)
    assertThat(thisChunk.getReferenceEntryStatus(ReferenceEntryDataSeriesIndex(0), TimestampIndex(1))).isEqualToHistoryEnumSet(0b100)
    assertThat(thisChunk.getReferenceEntryStatus(ReferenceEntryDataSeriesIndex(1), TimestampIndex(1))).isEqualToHistoryEnumSet(0b1000)
    assertThat(thisChunk.getReferenceEntryStatus(ReferenceEntryDataSeriesIndex(2), TimestampIndex(1))).isEqualToHistoryEnumSet(0b10000)


    val merged = thisChunk.merge(historyChunk(historyConfiguration) {}, 0.0, 500.0)
    requireNotNull(merged)

    assertThat(merged.timeStampsCount).isEqualTo(4)
    assertThat(merged.timeStamps).containsExactly(100.0, 101.0, 102.0, 103.0)

    assertThat(merged).hasValues(ReferenceEntryDataSeriesIndex.zero, 1, 10, 100, 1000)
    assertThat(merged).hasValues(ReferenceEntryDataSeriesIndex.one, 10, 100, 1000, 10000)
    assertThat(merged).hasValues(ReferenceEntryDataSeriesIndex.two, 101, 1001, 10001, 100001)

    assertThat((merged.referenceEntriesDataMap as DefaultReferenceEntriesDataMap).entries).hasSize(9)


    assertThat(merged.values.referenceEntryHistoryValues.statusesAsMatrixString()).isEqualTo(
      """
        0b101, 0b1001, 0b10001
        0b100, 0b1000, 0b10000
        -, -, -
        ?, ?, ?
    """.trimIndent()
    )

    assertThat(merged.getReferenceEntryStatus(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(0))).isEqualToHistoryEnumSet(0b101)
  }

  @Test
  fun `this before that - all`() {
    val thisChunk = historyChunk(historyConfiguration) {
      addReferenceEntryValues(100.0, 1, 10, 101, referenceEntriesDataMap = ReferenceEntriesDataMap.generated, referenceEntryStatuses = intArrayOf(0b101, 0b1001, 0b10001))
      addReferenceEntryValues(101.0, 10, 100, 1001, referenceEntriesDataMap = ReferenceEntriesDataMap.generated, referenceEntryStatuses = intArrayOf(0b100, 0b1000, 0b10000))
      addReferenceEntryValues(102.0, 100, 1000, 10001, referenceEntriesDataMap = ReferenceEntriesDataMap.generated)
      addReferenceEntryValues(103.0, 1000, 10000, 100001, referenceEntriesDataMap = ReferenceEntriesDataMap.generated)
    }

    val thatChunk = historyChunk(historyConfiguration) {
      addReferenceEntryValues(105.0, 101, 1111, 11111, referenceEntriesDataMap = ReferenceEntriesDataMap.generated, referenceEntryStatuses = intArrayOf(0b1001, 0b10001, 0b100001))
      addReferenceEntryValues(106.0, 111, 1110, 11110, referenceEntriesDataMap = ReferenceEntriesDataMap.generated)
      addReferenceEntryValues(107.0, 100, 1101, 11101, referenceEntriesDataMap = ReferenceEntriesDataMap.generated)
      addReferenceEntryValues(108.0, 101, 1011, 11011, referenceEntriesDataMap = ReferenceEntriesDataMap.generated)
    }

    val merged = thisChunk.merge(thatChunk, 0.0, 500.0)
    requireNotNull(merged)

    assertThat(merged.timeStampsCount).isEqualTo(8)
    assertThat(merged.timeStamps).containsExactly(100.0, 101.0, 102.0, 103.0, 105.0, 106.0, 107.0, 108.0)

    assertThat(merged).hasValues(ReferenceEntryDataSeriesIndex.zero, 1, 10, 100, 1000, 101, 111, 100, 101)
    assertThat(merged).hasValues(ReferenceEntryDataSeriesIndex.one, 10, 100, 1000, 10000, 1111, 1110, 1101, 1011)
    assertThat(merged).hasValues(ReferenceEntryDataSeriesIndex.two, 101, 1001, 10001, 100001, 11111, 11110, 11101, 11011)

    assertThat((merged.referenceEntriesDataMap as DefaultReferenceEntriesDataMap).entries).hasSize(18)

    assertThat(merged.values.referenceEntryHistoryValues.statusesAsMatrixString()).isEqualTo(
      """
        0b101, 0b1001, 0b10001
        0b100, 0b1000, 0b10000
        -, -, -
        -, -, -
        0b1001, 0b10001, 0b100001
        -, -, -
        -, -, -
        -, -, -
      """.trimIndent()
    )
  }

  @Test
  fun `that before this - all`() {
    val thisChunk = historyChunk(historyConfiguration) {
      addReferenceEntryValues(105.0, 101, 1111, 11111, referenceEntriesDataMap = ReferenceEntriesDataMap.generated)
      addReferenceEntryValues(106.0, 111, 1110, 11110, referenceEntriesDataMap = ReferenceEntriesDataMap.generated)
      addReferenceEntryValues(107.0, 100, 1101, 11101, referenceEntriesDataMap = ReferenceEntriesDataMap.generated, referenceEntryStatuses = intArrayOf(0b101, 0b1001, 0b10001))
      addReferenceEntryValues(108.0, 101, 1011, 11011, referenceEntriesDataMap = ReferenceEntriesDataMap.generated)
    }

    val thatChunk = historyChunk(historyConfiguration) {
      addReferenceEntryValues(100.0, 1, 10, 101, referenceEntriesDataMap = ReferenceEntriesDataMap.generated)
      addReferenceEntryValues(101.0, 10, 100, 1001, referenceEntriesDataMap = ReferenceEntriesDataMap.generated, referenceEntryStatuses = intArrayOf(0b100, 0b1000, 0b10000))
      addReferenceEntryValues(102.0, 100, 1000, 10001, referenceEntriesDataMap = ReferenceEntriesDataMap.generated)
      addReferenceEntryValues(103.0, 1000, 10000, 100001, referenceEntriesDataMap = ReferenceEntriesDataMap.generated)
    }

    val merged = thisChunk.merge(thatChunk, 0.0, 500.0)
    requireNotNull(merged)

    assertThat(merged.timeStampsCount).isEqualTo(8)
    assertThat(merged.timeStamps).containsExactly(100.0, 101.0, 102.0, 103.0, 105.0, 106.0, 107.0, 108.0)

    assertThat(merged).hasValues(ReferenceEntryDataSeriesIndex.zero, 1, 10, 100, 1000, 101, 111, 100, 101)
    assertThat(merged).hasValues(ReferenceEntryDataSeriesIndex.one, 10, 100, 1000, 10000, 1111, 1110, 1101, 1011)
    assertThat(merged).hasValues(ReferenceEntryDataSeriesIndex.two, 101, 1001, 10001, 100001, 11111, 11110, 11101, 11011)

    assertThat((merged.referenceEntriesDataMap as DefaultReferenceEntriesDataMap).entries).hasSize(18)

    assertThat(merged.values.referenceEntryHistoryValues.statusesAsMatrixString()).isEqualTo(
      """
        -, -, -
        0b100, 0b1000, 0b10000
        -, -, -
        -, -, -
        -, -, -
        -, -, -
        0b101, 0b1001, 0b10001
        -, -, -
      """.trimIndent()
    )
  }

  @Test
  fun `this interwoven with that - this outside`() {
    val thisChunk = historyChunk(historyConfiguration) {
      addReferenceEntryValues(100.0, 1, 10, 101, referenceEntriesDataMap = ReferenceEntriesDataMap.generated, referenceEntryStatuses = intArrayOf(0b101, 0b1001, 0b10001))
      addReferenceEntryValues(101.0, 10, 100, 1001, referenceEntriesDataMap = ReferenceEntriesDataMap.generated)
      addReferenceEntryValues(102.0, 100, 1000, 10001, referenceEntriesDataMap = ReferenceEntriesDataMap.generated)
      addReferenceEntryValues(103.0, 1000, 10000, 100001, referenceEntriesDataMap = ReferenceEntriesDataMap.generated)
    }

    val thatChunk = historyChunk(historyConfiguration) {
      addReferenceEntryValues(100.5, 101, 1111, 11111, referenceEntriesDataMap = ReferenceEntriesDataMap.generated, referenceEntryStatuses = intArrayOf(0b100, 0b1000, 0b10000))
      addReferenceEntryValues(101.5, 111, 1110, 11110, referenceEntriesDataMap = ReferenceEntriesDataMap.generated)
      addReferenceEntryValues(102.5, 100, 1101, 11101, referenceEntriesDataMap = ReferenceEntriesDataMap.generated)
    }

    val merged = thisChunk.merge(thatChunk, 0.0, 500.0)
    requireNotNull(merged)

    assertThat(merged.timeStampsCount).isEqualTo(7)
    assertThat(merged.timeStamps).containsExactly(100.0, 100.5, 101.0, 101.5, 102.0, 102.5, 103.0)

    assertThat(merged).hasValues(ReferenceEntryDataSeriesIndex.zero, 1, 101, 10, 111, 100, 100, 1000)
    assertThat(merged).hasValues(ReferenceEntryDataSeriesIndex.one, 10, 1111, 100, 1110, 1000, 1101, 10000)
    assertThat(merged).hasValues(ReferenceEntryDataSeriesIndex.two, 101, 11111, 1001, 11110, 10001, 11101, 100001)

    assertThat((merged.referenceEntriesDataMap as DefaultReferenceEntriesDataMap).entries).hasSize(16)

    assertThat(merged.values.referenceEntryHistoryValues.statusesAsMatrixString()).isEqualTo(
      """
        0b101, 0b1001, 0b10001
        0b100, 0b1000, 0b10000
        -, -, -
        -, -, -
        -, -, -
        -, -, -
        -, -, -
      """.trimIndent()
    )
  }

  @Test
  fun `this interwoven with that - that outside`() {
    val thisChunk = historyChunk(historyConfiguration) {
      addReferenceEntryValues(100.5, 101, 1111, 11111, referenceEntriesDataMap = ReferenceEntriesDataMap.generated)
      addReferenceEntryValues(101.5, 111, 1110, 11110, referenceEntriesDataMap = ReferenceEntriesDataMap.generated, referenceEntryStatuses = intArrayOf(0b101, 0b1001, 0b10001))
      addReferenceEntryValues(102.5, 100, 1101, 11101, referenceEntriesDataMap = ReferenceEntriesDataMap.generated)
    }

    val thatChunk = historyChunk(historyConfiguration) {
      addReferenceEntryValues(100.0, 1, 10, 101, referenceEntriesDataMap = ReferenceEntriesDataMap.generated)
      addReferenceEntryValues(101.0, 10, 100, 1001, referenceEntriesDataMap = ReferenceEntriesDataMap.generated, referenceEntryStatuses = intArrayOf(0b100, 0b1000, 0b10000))
      addReferenceEntryValues(102.0, 100, 1000, 10001, referenceEntriesDataMap = ReferenceEntriesDataMap.generated)
      addReferenceEntryValues(103.0, 1000, 10000, 100001, referenceEntriesDataMap = ReferenceEntriesDataMap.generated)
    }

    val merged = thisChunk.merge(thatChunk, 0.0, 500.0)
    requireNotNull(merged)

    assertThat(merged.timeStampsCount).isEqualTo(7)
    assertThat(merged.timeStamps).containsExactly(100.0, 100.5, 101.0, 101.5, 102.0, 102.5, 103.0)

    assertThat(merged).hasValues(ReferenceEntryDataSeriesIndex.zero, 1, 101, 10, 111, 100, 100, 1000)
    assertThat(merged).hasValues(ReferenceEntryDataSeriesIndex.one, 10, 1111, 100, 1110, 1000, 1101, 10000)
    assertThat(merged).hasValues(ReferenceEntryDataSeriesIndex.two, 101, 11111, 1001, 11110, 10001, 11101, 100001)

    assertThat((merged.referenceEntriesDataMap as DefaultReferenceEntriesDataMap).entries).hasSize(16)

    assertThat(merged.values.referenceEntryHistoryValues.statusesAsMatrixString()).isEqualTo(
      """
        -, -, -
        -, -, -
        0b100, 0b1000, 0b10000
        0b101, 0b1001, 0b10001
        -, -, -
        -, -, -
        -, -, -
      """.trimIndent()
    )
  }
}
