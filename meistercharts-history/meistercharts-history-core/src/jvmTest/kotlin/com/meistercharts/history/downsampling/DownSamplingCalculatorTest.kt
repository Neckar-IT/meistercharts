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
import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.EnumDataSeriesIndex
import com.meistercharts.history.HistoryEnumOrdinal
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryDifferentIdsCount
import com.meistercharts.history.ReferenceEntryId
import com.meistercharts.history.impl.HistoryChunk
import com.meistercharts.history.isEnumSetPending
import com.meistercharts.history.isEqualToHistoryEnumSet
import com.meistercharts.history.isEqualToReferenceEntryId
import com.meistercharts.history.isEqualToReferenceEntryIdsCount
import org.junit.jupiter.api.Test

/**
 */
class DownSamplingCalculatorTest {
  @Test
  fun testInitialValues() {
    val calculator = DownSamplingCalculator(2, 1, 3)

    assertThat(calculator.averageValue(DecimalDataSeriesIndex.zero)).isEqualTo(HistoryChunk.Pending)
    assertThat(calculator.averageValue(DecimalDataSeriesIndex.one)).isEqualTo(HistoryChunk.Pending)
    assertThat(calculator.minDecimal(DecimalDataSeriesIndex.zero)).isEqualTo(HistoryChunk.Pending)
    assertThat(calculator.minDecimal(DecimalDataSeriesIndex.one)).isEqualTo(HistoryChunk.Pending)
    assertThat(calculator.maxDecimal(DecimalDataSeriesIndex.zero)).isEqualTo(HistoryChunk.Pending)
    assertThat(calculator.maxDecimal(DecimalDataSeriesIndex.one)).isEqualTo(HistoryChunk.Pending)

    assertThat(calculator.enumValue(EnumDataSeriesIndex.zero)).isEnumSetPending()
    assertThat(calculator.enumOrdinalMostTime(EnumDataSeriesIndex.zero)).isEqualTo(HistoryEnumOrdinal.Pending)

    assertThat(calculator.referenceEntryDifferentIdsCount(ReferenceEntryDataSeriesIndex.zero)).isEqualTo(ReferenceEntryDifferentIdsCount.Pending)
    assertThat(calculator.referenceEntryMostOfTheTime(ReferenceEntryDataSeriesIndex.zero)).isEqualTo(ReferenceEntryId.Pending)
    assertThat(calculator.maxDecimal(DecimalDataSeriesIndex.one)).isEqualTo(HistoryChunk.Pending)

    assertThat(calculator.referenceEntryStatus(ReferenceEntryDataSeriesIndex.zero)).isEnumSetPending()
    assertThat(calculator.referenceEntryDifferentIdsCount(ReferenceEntryDataSeriesIndex.zero)).isEqualTo(ReferenceEntryDifferentIdsCount.Pending)
  }

  @Test
  fun testMinMax2() {
    val calculator = DownSamplingCalculator(7, 0, 0)
    assertThat(calculator.averageValue(DecimalDataSeriesIndex(0))).isEqualTo(HistoryChunk.Pending)
    assertThat(calculator.averageCalculationCount(DecimalDataSeriesIndex(0))).isEqualTo(0)

    calculator.addDecimalsSample(DoubleArray(7) { 7.0 }, null, null)
    calculator.addDecimalsSample(DoubleArray(7) { 9.0 }, null, null)

    assertThat(calculator.averageValue(DecimalDataSeriesIndex(0))).isEqualTo(8.0)
    assertThat(calculator.minDecimal(DecimalDataSeriesIndex(0))).isEqualTo(7.0)
    assertThat(calculator.maxDecimal(DecimalDataSeriesIndex(0))).isEqualTo(9.0)
  }

  @Test
  fun testEnumBitSetMerge() {
    val dataSeriesIndex0 = EnumDataSeriesIndex(0)
    val dataSeriesIndex1 = EnumDataSeriesIndex(1)
    val dataSeriesIndex2 = EnumDataSeriesIndex(2)

    val calculator = DownSamplingCalculator(0, 3, 0)
    assertThat(calculator.enumValue(dataSeriesIndex0)).isEnumSetPending()
    assertThat(calculator.enumValue(dataSeriesIndex1)).isEnumSetPending()
    assertThat(calculator.enumValue(dataSeriesIndex2)).isEnumSetPending()

    calculator.addEnumSample(intArrayOf(0b0001, 0b0101, 0b1001))

    assertThat(calculator.enumValue(dataSeriesIndex0)).isEqualTo(HistoryEnumSet(0b0001))
    assertThat(calculator.enumValue(dataSeriesIndex1)).isEqualTo(HistoryEnumSet(0b0101))
    assertThat(calculator.enumValue(dataSeriesIndex2)).isEqualTo(HistoryEnumSet(0b1001))

    //Add the third bit everywhere
    calculator.addEnumSample(intArrayOf(0b100, 0b100, 0b100))

    assertThat(calculator.enumValue(dataSeriesIndex0)).isEqualTo(HistoryEnumSet(0b0101))
    assertThat(calculator.enumValue(dataSeriesIndex1)).isEqualTo(HistoryEnumSet(0b0101))
    assertThat(calculator.enumValue(dataSeriesIndex2)).isEqualTo(HistoryEnumSet(0b1101))
  }

  @Test
  fun testMostTime() {
    val dataSeriesIndex0 = EnumDataSeriesIndex(0)
    val dataSeriesIndex1 = EnumDataSeriesIndex(1)
    val dataSeriesIndex2 = EnumDataSeriesIndex(2)

    val calculator = DownSamplingCalculator(0, 3, 0)
    assertThat(calculator.enumOrdinalMostTime(dataSeriesIndex0)).isEqualTo(HistoryEnumOrdinal.Pending)
    assertThat(calculator.enumOrdinalMostTime(dataSeriesIndex1)).isEqualTo(HistoryEnumOrdinal.Pending)
    assertThat(calculator.enumOrdinalMostTime(dataSeriesIndex2)).isEqualTo(HistoryEnumOrdinal.Pending)

    calculator.addEnumSample(intArrayOf(0b0101, 0b0101, 0b1000))

    assertThat(calculator.enumOrdinalMostTime(dataSeriesIndex0)).isEqualTo(HistoryEnumOrdinal(0))
    assertThat(calculator.enumOrdinalMostTime(dataSeriesIndex1)).isEqualTo(HistoryEnumOrdinal(0))
    assertThat(calculator.enumOrdinalMostTime(dataSeriesIndex2)).isEqualTo(HistoryEnumOrdinal(3))

    //Add the third bit everywhere
    calculator.addEnumSample(intArrayOf(0b0100, 0b0101, 0b0110))

    assertThat(calculator.enumOrdinalMostTime(dataSeriesIndex0)).isEqualTo(HistoryEnumOrdinal(2))
    assertThat(calculator.enumOrdinalMostTime(dataSeriesIndex1)).isEqualTo(HistoryEnumOrdinal(0))
    assertThat(calculator.enumOrdinalMostTime(dataSeriesIndex2)).isEqualTo(HistoryEnumOrdinal(1))
  }

  @Test
  fun testOverFlow() {
    val calculator = DownSamplingCalculator(2, 0, 0)

    assertThat(calculator.averageCalculationCount(DecimalDataSeriesIndex(0))).isEqualTo(0)
    assertThat(calculator.averageCalculationCount(DecimalDataSeriesIndex(1))).isEqualTo(0)
    assertThat(calculator.averageValue(DecimalDataSeriesIndex(0))).isEqualTo(HistoryChunk.Pending)
    assertThat(calculator.averageValue(DecimalDataSeriesIndex(1))).isEqualTo(HistoryChunk.Pending)

    calculator.addDecimalsSample(doubleArrayOf(Integer.MAX_VALUE.toDouble()), null, null)
    assertThat(calculator.averageValue(DecimalDataSeriesIndex(0))).isEqualTo(Integer.MAX_VALUE.toDouble())

    calculator.addDecimalsSample(doubleArrayOf(7.0), null, null)
    assertThat(calculator.averageValue(DecimalDataSeriesIndex(0))).isEqualTo((Integer.MAX_VALUE + 7.0) / 2.0)

    calculator.addDecimalsSample(doubleArrayOf(1.0), null, null)
    assertThat(calculator.averageValue(DecimalDataSeriesIndex(0))).isEqualTo((Integer.MAX_VALUE + 7.0 + 1.0) / 3.0)
  }

  @Test
  fun testNan2() {
    val calculator = DownSamplingCalculator(2, 0, 0)
    assertThat(calculator.averageCalculationCount(DecimalDataSeriesIndex(0))).isEqualTo(0)
    assertThat(calculator.averageCalculationCount(DecimalDataSeriesIndex(1))).isEqualTo(0)
    assertThat(calculator.averageValue(DecimalDataSeriesIndex(0))).isEqualTo(HistoryChunk.Pending)
    assertThat(calculator.averageValue(DecimalDataSeriesIndex(1))).isEqualTo(HistoryChunk.Pending)

    assertThat(calculator.averageCalculationCount(DecimalDataSeriesIndex(0))).isEqualTo(0)
    assertThat(calculator.averageCalculationCount(DecimalDataSeriesIndex(1))).isEqualTo(0)
    calculator.addDecimalsSample(doubleArrayOf(HistoryChunk.NoValue, 1.0), null, null)
    assertThat(calculator.averageCalculationCount(DecimalDataSeriesIndex(0))).isEqualTo(0)
    assertThat(calculator.averageCalculationCount(DecimalDataSeriesIndex(1))).isEqualTo(1)
    assertThat(calculator.averageValue(DecimalDataSeriesIndex(0))).isEqualTo(HistoryChunk.NoValue)
    assertThat(calculator.averageValue(DecimalDataSeriesIndex(1))).isEqualTo(1.0)

    calculator.addDecimalsSample(doubleArrayOf(1.0, HistoryChunk.Pending), null, null)
    assertThat(calculator.averageCalculationCount(DecimalDataSeriesIndex(0))).isEqualTo(1)
    assertThat(calculator.averageCalculationCount(DecimalDataSeriesIndex(1))).isEqualTo(1)
    assertThat(calculator.averageValue(DecimalDataSeriesIndex(0))).isEqualTo(1.0)
    assertThat(calculator.averageValue(DecimalDataSeriesIndex(1))).isEqualTo(1.0)

    calculator.addDecimalsSample(doubleArrayOf(3.0, 3.0), null, null)
    assertThat(calculator.averageCalculationCount(DecimalDataSeriesIndex(0))).isEqualTo(2)
    assertThat(calculator.averageCalculationCount(DecimalDataSeriesIndex(1))).isEqualTo(2)
    assertThat(calculator.averageValue(DecimalDataSeriesIndex(0))).isEqualTo(2.0)
    assertThat(calculator.averageValue(DecimalDataSeriesIndex(1))).isEqualTo(2.0)
  }

  @Test
  fun testMultipleCounts() {
    val calculator = DownSamplingCalculator(2, 0, 0)
    assertThat(calculator.averageValue(DecimalDataSeriesIndex(0))).isEqualTo(HistoryChunk.Pending)
    assertThat(calculator.averageValue(DecimalDataSeriesIndex(1))).isEqualTo(HistoryChunk.Pending)
    assertThat(calculator.averageCalculationCount(DecimalDataSeriesIndex(0))).isEqualTo(0)

    calculator.addDecimalsSample(doubleArrayOf(HistoryChunk.NoValue), null, null)
    assertThat(calculator.averageValue(DecimalDataSeriesIndex(0))).isEqualTo(HistoryChunk.NoValue)

    calculator.addDecimalsSample(doubleArrayOf(HistoryChunk.Pending), null, null)
    assertThat(calculator.averageValue(DecimalDataSeriesIndex(0))).isEqualTo(HistoryChunk.NoValue)
  }

  @Test
  internal fun testMultipleEntries() {
    val calculator = DownSamplingCalculator(3, 4, 0)
    assertThat(calculator.averageCalculationCount(DecimalDataSeriesIndex(0))).isEqualTo(0)

    assertThat(calculator.averageValues()).containsExactly(HistoryChunk.Pending, HistoryChunk.Pending, HistoryChunk.Pending)
    assertThat(calculator.minValues()).containsExactly(HistoryChunk.Pending, HistoryChunk.Pending, HistoryChunk.Pending)
    assertThat(calculator.maxValues()).containsExactly(HistoryChunk.Pending, HistoryChunk.Pending, HistoryChunk.Pending)
    assertThat(calculator.enumUnionValues()).containsExactly(HistoryEnumSet.PendingAsInt, HistoryEnumSet.PendingAsInt, HistoryEnumSet.PendingAsInt, HistoryEnumSet.PendingAsInt)
  }

  @Test
  fun testMinMax() {
    val calculator = DownSamplingCalculator(7, 0, 0)
    assertThat(calculator.averageValue(DecimalDataSeriesIndex(0))).isEqualTo(HistoryChunk.Pending)
    assertThat(calculator.averageCalculationCount(DecimalDataSeriesIndex(0))).isEqualTo(0)


    calculator.addDecimalsSample(DoubleArray(7) { 7.0 }, null, null)

    assertThat(calculator.averageValue(DecimalDataSeriesIndex(0))).isEqualTo(7.0)
    assertThat(calculator.averageCalculationCount(DecimalDataSeriesIndex(0))).isEqualTo(1)

    calculator.addDecimalsSample(DoubleArray(7) { 9.0 }, null, null)

    assertThat(calculator.averageValue(DecimalDataSeriesIndex(0))).isEqualTo(8.0)
    assertThat(calculator.averageCalculationCount(DecimalDataSeriesIndex(0))).isEqualTo(2)

    assertThat(calculator.minDecimal(DecimalDataSeriesIndex(0))).isEqualTo(7.0)
    assertThat(calculator.maxDecimal(DecimalDataSeriesIndex(0))).isEqualTo(9.0)
  }

  @Test
  fun testReset() {
    val calculator = DownSamplingCalculator(7, 2, 3)
    assertThat(calculator.averageValue(DecimalDataSeriesIndex(0))).isEqualTo(HistoryChunk.Pending)
    assertThat(calculator.averageCalculationCount(DecimalDataSeriesIndex(0))).isEqualTo(0)

    assertThat(calculator.enumValue(EnumDataSeriesIndex(0))).isEnumSetPending()

    calculator.addDecimalsSample(DoubleArray(7) { 7.0 }, null, null)
    calculator.addEnumSample(IntArray(2) { 0b101 })
    calculator.addReferenceEntrySample(intArrayOf(1, 3, 5), intArrayOf(7, 8, 9), intArrayOf(0b1, 0b01, 0b001))

    assertThat(calculator.averageValue(DecimalDataSeriesIndex(0))).isEqualTo(7.0)
    assertThat(calculator.averageCalculationCount(DecimalDataSeriesIndex(0))).isEqualTo(1)

    assertThat(calculator.enumValue(EnumDataSeriesIndex(0))).isEqualTo(HistoryEnumSet(0b101))

    assertThat(calculator.referenceEntryMostOfTheTime(ReferenceEntryDataSeriesIndex.one)).isEqualToReferenceEntryId(3)
    assertThat(calculator.referenceEntryStatus(ReferenceEntryDataSeriesIndex.zero)).isEqualToHistoryEnumSet(0b1)
    assertThat(calculator.referenceEntryStatus(ReferenceEntryDataSeriesIndex.one)).isEqualToHistoryEnumSet(0b001)

    calculator.reset()

    assertThat(calculator.averageValue(DecimalDataSeriesIndex(0))).isEqualTo(HistoryChunk.Pending)
    assertThat(calculator.averageCalculationCount(DecimalDataSeriesIndex(0))).isEqualTo(0)
    assertThat(calculator.enumValue(EnumDataSeriesIndex(0))).isEnumSetPending()

    assertThat(calculator.referenceEntryStatus(ReferenceEntryDataSeriesIndex.zero)).isEnumSetPending()
    assertThat(calculator.referenceEntryMostOfTheTime(ReferenceEntryDataSeriesIndex.zero)).isEqualTo(ReferenceEntryId.Pending)
  }

  @Test
  fun testRefEntryCountFirstLayer() {
    val calculator = DownSamplingCalculator(0, 0, 2)

    assertThat(calculator.referenceEntryDifferentIdsCount(ReferenceEntryDataSeriesIndex.zero)).isEqualTo(ReferenceEntryDifferentIdsCount.Pending)
    assertThat(calculator.referenceEntryMostOfTheTime(ReferenceEntryDataSeriesIndex.zero)).isEqualTo(ReferenceEntryId.Pending)

    calculator.addReferenceEntrySample(
      newReferenceEntries = IntArray(2) { it + 10 },
      newDifferentIdsCount = null,
      newStatuses = IntArray(2) { it + 77 }
    )

    assertThat(calculator.referenceEntryDifferentIdsCount(ReferenceEntryDataSeriesIndex.zero)).isEqualToReferenceEntryIdsCount(1)
    assertThat(calculator.referenceEntryMostOfTheTime(ReferenceEntryDataSeriesIndex.zero)).isEqualTo(ReferenceEntryId(10))
    assertThat(calculator.referenceEntryStatus(ReferenceEntryDataSeriesIndex.zero)).isEqualToHistoryEnumSet(0b1001101)

    assertThat(calculator.referenceEntryDifferentIdsCount(ReferenceEntryDataSeriesIndex.one)).isEqualToReferenceEntryIdsCount(1)
    assertThat(calculator.referenceEntryMostOfTheTime(ReferenceEntryDataSeriesIndex.one)).isEqualTo(ReferenceEntryId(11))
    assertThat(calculator.referenceEntryStatus(ReferenceEntryDataSeriesIndex.one)).isEqualToHistoryEnumSet(0b1001110)
  }
}
