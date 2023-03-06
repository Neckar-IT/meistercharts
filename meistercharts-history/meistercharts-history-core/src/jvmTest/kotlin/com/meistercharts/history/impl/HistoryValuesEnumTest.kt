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
import com.meistercharts.history.HistoryEnum
import com.meistercharts.history.HistoryEnumOrdinal
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.TimestampIndex
import com.meistercharts.history.historyConfiguration
import it.neckar.open.collections.BitSet
import it.neckar.open.collections.emptyDoubleArray
import it.neckar.open.collections.emptyIntArray
import it.neckar.open.kotlin.lang.fastFor
import it.neckar.open.i18n.TextKey
import org.junit.jupiter.api.Test

/**
 *
 */
class HistoryValuesEnumTest {
  @Test
  fun testIt() {
    val value = MyEnum.Option1
    assertThat(value.ordinal).isEqualTo(0)
  }

  /**
   * Test which enum is active most of the time
   */
  @Test
  fun testMostTime() {
    val enumColor = HistoryEnum.create("EnumColor", listOf(TextKey("red"), TextKey("blue"), TextKey("orange")))

    val historyConfiguration = historyConfiguration {
      enumDataSeries(DataSeriesId(17), "Enum1", enumColor)
    }

    assertThat(historyConfiguration.enumDataSeriesCount).isEqualTo(1)

    val chunk = historyChunk(historyConfiguration) {
      addValues(100.0, emptyDoubleArray(), intArrayOf(HistoryEnumSet.forEnumValue(0).bitset), emptyIntArray())
      addValues(101.0, emptyDoubleArray(), intArrayOf(HistoryEnumSet.forEnumValue(1).bitset), emptyIntArray())
      addValues(102.0, emptyDoubleArray(), intArrayOf(HistoryEnumSet.forEnumValue(2).bitset), emptyIntArray())
      addValues(103.0, emptyDoubleArray(), intArrayOf(HistoryEnumSet.forEnumValue(1).bitset), emptyIntArray())

      addValues(107.0, emptyDoubleArray(), intArrayOf(HistoryEnumSet.NoValue.bitset), emptyIntArray())
    }

    assertThat(chunk.getEnumValue(EnumDataSeriesIndex.zero, TimestampIndex.zero)).isEqualTo(HistoryEnumSet.forEnumValue(0))
    assertThat(chunk.getEnumValue(EnumDataSeriesIndex.zero, TimestampIndex.one)).isEqualTo(HistoryEnumSet.forEnumValue(1))
    assertThat(chunk.getEnumValue(EnumDataSeriesIndex.zero, TimestampIndex.two)).isEqualTo(HistoryEnumSet.forEnumValue(2))
    assertThat(chunk.getEnumValue(EnumDataSeriesIndex.zero, TimestampIndex.three)).isEqualTo(HistoryEnumSet.forEnumValue(1))
    assertThat(chunk.getEnumValue(EnumDataSeriesIndex.zero, TimestampIndex.four)).isEqualTo(HistoryEnumSet.NoValue)

    assertThat(chunk.getEnumOrdinalMostTime(EnumDataSeriesIndex.zero, TimestampIndex.zero)).isEqualTo(HistoryEnumOrdinal(0))
    assertThat(chunk.getEnumOrdinalMostTime(EnumDataSeriesIndex.zero, TimestampIndex.one)).isEqualTo(HistoryEnumOrdinal(1))
    assertThat(chunk.getEnumOrdinalMostTime(EnumDataSeriesIndex.zero, TimestampIndex.two)).isEqualTo(HistoryEnumOrdinal(2))
    assertThat(chunk.getEnumOrdinalMostTime(EnumDataSeriesIndex.zero, TimestampIndex.three)).isEqualTo(HistoryEnumOrdinal(1))
    assertThat(chunk.getEnumOrdinalMostTime(EnumDataSeriesIndex.zero, TimestampIndex.four)).isEqualTo(HistoryEnumOrdinal.NoValue)
  }

  @Test
  fun testConfigWithEnums() {
    val enumColor = HistoryEnum.create("EnumColor", listOf(TextKey("red"), TextKey("blue"), TextKey("orange")))
    val enumDirection = HistoryEnum.create("EnumDirection", listOf(TextKey("top"), TextKey("bottom")))

    val historyConfiguration = historyConfiguration {
      enumDataSeries(DataSeriesId(17), "Enum1", enumColor)
      enumDataSeries(DataSeriesId(18), "Enum2", enumDirection)
      enumDataSeries(DataSeriesId(19), "Enum3", enumColor)
    }

    assertThat(historyConfiguration.enumDataSeriesCount).isEqualTo(3)

    assertThat(historyConfiguration.enumConfiguration.getDataSeriesIndex(DataSeriesId(17)).value).isEqualTo(0)
    assertThat(historyConfiguration.enumConfiguration.getDataSeriesIndex(DataSeriesId(18)).value).isEqualTo(1)
    assertThat(historyConfiguration.enumConfiguration.getDataSeriesIndex(DataSeriesId(19)).value).isEqualTo(2)

    assertThat(historyConfiguration.enumConfiguration.getEnum(EnumDataSeriesIndex(0))).isEqualTo(enumColor)
    assertThat(historyConfiguration.enumConfiguration.getEnum(EnumDataSeriesIndex(1))).isEqualTo(enumDirection)
    assertThat(historyConfiguration.enumConfiguration.getEnum(EnumDataSeriesIndex(2))).isEqualTo(enumColor)
  }

  @Test
  fun testEnumValues() {
    val historyValues = historyValues(0, 3, 0, 2, RecordingType.Measured) {
      setEnumValuesForTimestamp(TimestampIndex(0), intArrayOf(7, 1, 2))
      setEnumValuesForTimestamp(TimestampIndex(1), intArrayOf(1, 1, 1))
    }

    assertThat(historyValues.timeStampsCount).isEqualTo(2)
    assertThat(historyValues.decimalValuesAsMatrixString()).isEqualTo("\n")

    assertThat(historyValues.enumValuesAsMatrixString()).isEqualTo(
      """
        0b111, 0b1, 0b10
        0b1, 0b1, 0b1
      """.trimIndent()
    )
  }

  @Test
  fun testEnumChunk() {
    val enumColor = HistoryEnum.create("EnumColor", listOf(TextKey("red"), TextKey("blue"), TextKey("orange")))
    val enumDirection = HistoryEnum.create("EnumDirection", listOf(TextKey("top"), TextKey("bottom")))

    val historyConfiguration = historyConfiguration {
      enumDataSeries(DataSeriesId(11), "Enum1", HistoryEnum.Boolean)
      enumDataSeries(DataSeriesId(12), "Enum2", enumDirection)
      enumDataSeries(DataSeriesId(13), "Enum3", enumColor)
    }

    val historyChunk = historyChunk(historyConfiguration) {
      this.addEnumValues(1000.0, 4, 1, 2)
      this.addEnumValues(1100.0, 1, 1, 1)
    }

    assertThat(historyChunk.timeStampsCount).isEqualTo(2)
    assertThat(historyChunk.enumDataSeriesCount).isEqualTo(3)

    assertThat(historyChunk.getEnumValue(EnumDataSeriesIndex.zero, TimestampIndex(0)).bitset).isEqualTo(4)
    assertThat(historyChunk.getEnumValue(EnumDataSeriesIndex.one, TimestampIndex(0)).bitset).isEqualTo(1)
    assertThat(historyChunk.getEnumValue(EnumDataSeriesIndex.two, TimestampIndex(0)).bitset).isEqualTo(2)

    assertThat(historyChunk.getEnumValue(EnumDataSeriesIndex.zero, TimestampIndex(1)).bitset).isEqualTo(1)
    assertThat(historyChunk.getEnumValue(EnumDataSeriesIndex.one, TimestampIndex(1)).bitset).isEqualTo(1)
    assertThat(historyChunk.getEnumValue(EnumDataSeriesIndex.two, TimestampIndex(1)).bitset).isEqualTo(1)
  }

  @Test
  fun testBitset2Double() {
    val size = 32
    val bitSet = BitSet(size)

    assertThat(bitSet.size).isEqualTo(size)
    assertThat(bitSet.data.size).isEqualTo(1)

    size.fastFor {
      if (it % 2 == 0) {
        bitSet.set(it)
      }
    }

    assertThat(bitSet[0]).isTrue()
    assertThat(bitSet[1]).isFalse()
    assertThat(bitSet[2]).isTrue()
    assertThat(bitSet[30]).isTrue()
    assertThat(bitSet[31]).isFalse()

    assertThat(bitSet.data.first()).isEqualTo(1431655765)
  }

  enum class MyEnum {
    Option1,
    Option2,
    Option3,
    Option4,
  }
}
