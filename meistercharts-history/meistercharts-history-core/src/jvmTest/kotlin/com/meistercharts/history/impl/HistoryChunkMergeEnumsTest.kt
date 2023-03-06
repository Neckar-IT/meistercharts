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
import com.meistercharts.history.EnumDataSeriesIndex
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.downsampling.createDemoEnumConfiguration
import com.meistercharts.history.historyConfiguration
import it.neckar.open.i18n.TextKey
import org.junit.jupiter.api.Test

class HistoryChunkMergeEnumsTest {
  val historyConfiguration: HistoryConfiguration = historyConfiguration {
    enumDataSeries(DataSeriesId(10), TextKey("state1"), createDemoEnumConfiguration(10))
    enumDataSeries(DataSeriesId(11), TextKey("state2"), createDemoEnumConfiguration(20))
    enumDataSeries(DataSeriesId(12), TextKey("state3"), createDemoEnumConfiguration(27))
  }

  @Test
  fun testWithEmpty() {
    val thisChunk = historyChunk(historyConfiguration) {
      addEnumValues(100.0, 0b1, 0b10, 0b101)
      addEnumValues(101.0, 0b10, 0b100, 0b1001)
      addEnumValues(102.0, 0b100, 0b1000, 0b10001)
      addEnumValues(103.0, 0b1000, 0b10000, 0b100001)
    }

    val merged = thisChunk.merge(historyChunk(historyConfiguration) {}, 0.0, 500.0)
    requireNotNull(merged)

    assertThat(merged.timeStampsCount).isEqualTo(4)
    assertThat(merged.timeStamps).containsExactly(100.0, 101.0, 102.0, 103.0)

    assertThat(merged).hasValues(EnumDataSeriesIndex.zero, 0b1, 0b10, 0b100, 0b1000)
    assertThat(merged).hasValues(EnumDataSeriesIndex.one, 0b10, 0b100, 0b1000, 0b10000)
    assertThat(merged).hasValues(EnumDataSeriesIndex.two, 0b101, 0b1001, 0b10001, 0b100001)
  }

  @Test
  fun `this before that - all`() {
    val thisChunk = historyChunk(historyConfiguration) {
      addEnumValues(100.0, 0b1, 0b10, 0b101)
      addEnumValues(101.0, 0b10, 0b100, 0b1001)
      addEnumValues(102.0, 0b100, 0b1000, 0b10001)
      addEnumValues(103.0, 0b1000, 0b10000, 0b100001)
    }

    val thatChunk = historyChunk(historyConfiguration) {
      addEnumValues(105.0, 0b101, 0b1111, 0b11111)
      addEnumValues(106.0, 0b111, 0b1110, 0b11110)
      addEnumValues(107.0, 0b100, 0b1101, 0b11101)
      addEnumValues(108.0, 0b101, 0b1011, 0b11011)
    }

    val merged = thisChunk.merge(thatChunk, 0.0, 500.0)
    requireNotNull(merged)

    assertThat(merged.timeStampsCount).isEqualTo(8)
    assertThat(merged.timeStamps).containsExactly(100.0, 101.0, 102.0, 103.0, 105.0, 106.0, 107.0, 108.0)

    assertThat(merged).hasValues(EnumDataSeriesIndex.zero, 0b1, 0b10, 0b100, 0b1000, 0b101, 0b111, 0b100, 0b101)
    assertThat(merged).hasValues(EnumDataSeriesIndex.one, 0b10, 0b100, 0b1000, 0b10000, 0b1111, 0b1110, 0b1101, 0b1011)
    assertThat(merged).hasValues(EnumDataSeriesIndex.two, 0b101, 0b1001, 0b10001, 0b100001, 0b11111, 0b11110, 0b11101, 0b11011)
  }

  @Test
  fun `that before this - all`() {
    val thisChunk = historyChunk(historyConfiguration) {
      addEnumValues(105.0, 0b101, 0b1111, 0b11111)
      addEnumValues(106.0, 0b111, 0b1110, 0b11110)
      addEnumValues(107.0, 0b100, 0b1101, 0b11101)
      addEnumValues(108.0, 0b101, 0b1011, 0b11011)
    }

    val thatChunk = historyChunk(historyConfiguration) {
      addEnumValues(100.0, 0b1, 0b10, 0b101)
      addEnumValues(101.0, 0b10, 0b100, 0b1001)
      addEnumValues(102.0, 0b100, 0b1000, 0b10001)
      addEnumValues(103.0, 0b1000, 0b10000, 0b100001)
    }

    val merged = thisChunk.merge(thatChunk, 0.0, 500.0)
    requireNotNull(merged)

    assertThat(merged.timeStampsCount).isEqualTo(8)
    assertThat(merged.timeStamps).containsExactly(100.0, 101.0, 102.0, 103.0, 105.0, 106.0, 107.0, 108.0)

    assertThat(merged).hasValues(EnumDataSeriesIndex.zero, 0b1, 0b10, 0b100, 0b1000, 0b101, 0b111, 0b100, 0b101)
    assertThat(merged).hasValues(EnumDataSeriesIndex.one, 0b10, 0b100, 0b1000, 0b10000, 0b1111, 0b1110, 0b1101, 0b1011)
    assertThat(merged).hasValues(EnumDataSeriesIndex.two, 0b101, 0b1001, 0b10001, 0b100001, 0b11111, 0b11110, 0b11101, 0b11011)
  }


  @Test
  fun `this interwoven with that - this outside`() {
    val thisChunk = historyChunk(historyConfiguration) {
      addEnumValues(100.0, 0b1, 0b10, 0b101)
      addEnumValues(101.0, 0b10, 0b100, 0b1001)
      addEnumValues(102.0, 0b100, 0b1000, 0b10001)
      addEnumValues(103.0, 0b1000, 0b10000, 0b100001)
    }

    val thatChunk = historyChunk(historyConfiguration) {
      addEnumValues(100.5, 0b101, 0b1111, 0b11111)
      addEnumValues(101.5, 0b111, 0b1110, 0b11110)
      addEnumValues(102.5, 0b100, 0b1101, 0b11101)
    }

    val merged = thisChunk.merge(thatChunk, 0.0, 500.0)
    requireNotNull(merged)

    assertThat(merged.timeStampsCount).isEqualTo(7)
    assertThat(merged.timeStamps).containsExactly(100.0, 100.5, 101.0, 101.5, 102.0, 102.5, 103.0)

    assertThat(merged).hasValues(EnumDataSeriesIndex.zero, 0b1, 0b101, 0b10, 0b111, 0b100, 0b100, 0b1000)
    assertThat(merged).hasValues(EnumDataSeriesIndex.one, 0b10, 0b1111, 0b100, 0b1110, 0b1000, 0b1101, 0b10000)
    assertThat(merged).hasValues(EnumDataSeriesIndex.two, 0b101, 0b11111, 0b1001, 0b11110, 0b10001, 0b11101, 0b100001)
  }

  @Test
  fun `this interwoven with that - that outside`() {
    val thisChunk = historyChunk(historyConfiguration) {
      addEnumValues(100.5, 0b101, 0b1111, 0b11111)
      addEnumValues(101.5, 0b111, 0b1110, 0b11110)
      addEnumValues(102.5, 0b100, 0b1101, 0b11101)
    }

    val thatChunk = historyChunk(historyConfiguration) {
      addEnumValues(100.0, 0b1, 0b10, 0b101)
      addEnumValues(101.0, 0b10, 0b100, 0b1001)
      addEnumValues(102.0, 0b100, 0b1000, 0b10001)
      addEnumValues(103.0, 0b1000, 0b10000, 0b100001)
    }

    val merged = thisChunk.merge(thatChunk, 0.0, 500.0)
    requireNotNull(merged)

    assertThat(merged.timeStampsCount).isEqualTo(7)
    assertThat(merged.timeStamps).containsExactly(100.0, 100.5, 101.0, 101.5, 102.0, 102.5, 103.0)

    assertThat(merged).hasValues(EnumDataSeriesIndex.zero, 0b1, 0b101, 0b10, 0b111, 0b100, 0b100, 0b1000)
    assertThat(merged).hasValues(EnumDataSeriesIndex.one, 0b10, 0b1111, 0b100, 0b1110, 0b1000, 0b1101, 0b10000)
    assertThat(merged).hasValues(EnumDataSeriesIndex.two, 0b101, 0b11111, 0b1001, 0b11110, 0b10001, 0b11101, 0b100001)
  }

}
