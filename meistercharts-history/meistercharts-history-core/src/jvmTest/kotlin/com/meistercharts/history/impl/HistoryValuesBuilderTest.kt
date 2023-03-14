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
import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.EnumDataSeriesIndex
import com.meistercharts.history.HistoryEnumOrdinal
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.ReferenceEntryData
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryId
import com.meistercharts.history.TimestampIndex
import it.neckar.open.i18n.TextKey
import org.junit.jupiter.api.Test

/**
 *
 */
class HistoryValuesBuilderTest {
  @Test
  fun testRefTypesResizeTest() {
    val builder = HistoryValuesBuilder(0, 0, 3, 2, RecordingType.Calculated)

    builder.let { builder ->
      assertThat(builder.timestampsCount).isEqualTo(2)
      builder.setReferenceEntryIdsForTimestamp(
        timestampIndex = TimestampIndex.zero,
        referenceEntryIds = intArrayOf(7, 8, 9),
        referenceEntryIdsCount = intArrayOf(1, 1, 1),
        referenceEntryStatuses = intArrayOf(1, 2, 3),
        referenceEntryDataSet = emptySet()
      )
      builder.setReferenceEntryIdsForTimestamp(
        timestampIndex = TimestampIndex.one,
        referenceEntryIds = intArrayOf(17, 18, 19),
        referenceEntryIdsCount = intArrayOf(2, 2, 2),
        referenceEntryStatuses = intArrayOf(4, 5, 6),
        referenceEntryDataSet = emptySet()
      )
      assertThat(builder.timestampsCount).isEqualTo(2)

      //assertThat(it)
      builder.build()
    }.let { values ->
      assertThat(values.referenceEntryDataSeriesCount).isEqualTo(3)
      assertThat(values.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex.zero)).isEqualTo(ReferenceEntryId(7))
    }

    //Add a third one! - resize
    assertThat(builder.referenceEntryIds?.height).isEqualTo(2)
    assertThat(builder.referenceEntryDifferentIdsCount?.height).isEqualTo(2)

    builder.resizeTimestamps(4)
    assertThat(builder.timestampsCount).isEqualTo(4)
    assertThat(builder.referenceEntryIds?.height).isEqualTo(4)
    assertThat(builder.referenceEntryDifferentIdsCount?.height).isEqualTo(4)

    builder.setReferenceEntryIdsForTimestamp(
      timestampIndex = TimestampIndex.two,
      referenceEntryIds = intArrayOf(27, 28, 29),
      referenceEntryIdsCount = intArrayOf(1, 1, 1),
      referenceEntryStatuses = intArrayOf(7, 8, 9),
      referenceEntryDataSet = emptySet()
    )
    builder.setReferenceEntryIdsForTimestamp(
      timestampIndex = TimestampIndex.three,
      referenceEntryIds = intArrayOf(37, 38, 39),
      referenceEntryIdsCount = intArrayOf(1, 1, 1),
      referenceEntryStatuses = intArrayOf(10, 11, 12),
      referenceEntryDataSet = emptySet()
    )

    builder.build().let { values ->
      assertThat(values.timeStampsCount).isEqualTo(4)
      assertThat(values.getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, TimestampIndex(3))).isEqualTo(ReferenceEntryId(37))

      assertThat(values.getReferenceEntryStatuses(TimestampIndex(0))).containsExactly(1, 2, 3)
      assertThat(values.getReferenceEntryStatuses(TimestampIndex(1))).containsExactly(4, 5, 6)
      assertThat(values.getReferenceEntryStatuses(TimestampIndex(2))).containsExactly(7, 8, 9)
      assertThat(values.getReferenceEntryStatuses(TimestampIndex(3))).containsExactly(10, 11, 12)
    }
  }

  @Test
  fun testType() {
    HistoryValuesBuilder(7, 0, 0, 200, RecordingType.Measured).let {
      assertThat(it.build().decimalHistoryValues.maxValues).isNull()
      assertThat(it.build().decimalHistoryValues.minValues).isNull()
      assertThat(it.build().enumHistoryValues.mostOfTheTimeValues).isNull()
      assertThat(it.build().enumHistoryValues.mostOfTheTimeValues).isNull()
    }

    assertThat(HistoryValuesBuilder(7, 0, 0, 200, RecordingType.Calculated).build().decimalHistoryValues.maxValues).isNotNull()
  }

  @Test
  fun testEnumValues() {
    val builder = HistoryValuesBuilder(0, 5, 0, 200, RecordingType.Measured)
    assertThat(builder.timestampsCount).isEqualTo(200)

    builder.setEnumValue(EnumDataSeriesIndex.zero, TimestampIndex(1), HistoryEnumSet(7))

    builder.build().also { historyValues ->
      assertThat(historyValues).isNotNull()
      assertThat(historyValues.enumDataSeriesCount).isEqualTo(5)
      assertThat(historyValues.timeStampsCount).isEqualTo(200)
      assertThat(historyValues.getEnumValue(EnumDataSeriesIndex(0), TimestampIndex(1)).bitset).isEqualTo(7)
    }

    //change size
    builder.resizeTimestamps(100)
    assertThat(builder.timestampsCount).isEqualTo(100)

    builder.build().also { historyValues ->
      assertThat(historyValues).isNotNull()
      assertThat(historyValues.enumDataSeriesCount).isEqualTo(5)
      assertThat(historyValues.timeStampsCount).isEqualTo(100)
      assertThat(historyValues.getEnumValue(EnumDataSeriesIndex(0), TimestampIndex(1)).bitset).isEqualTo(7)
    }
  }

  @Test
  fun testSize() {
    val builder = HistoryValuesBuilder(7, 0, 0, 200, RecordingType.Measured)
    assertThat(builder.timestampsCount).isEqualTo(200)

    builder.setDecimalValue(DecimalDataSeriesIndex.zero, TimestampIndex(1), 99.0)

    builder.build().also { historyValues ->
      assertThat(historyValues).isNotNull()
      assertThat(historyValues.decimalDataSeriesCount).isEqualTo(7)
      assertThat(historyValues.timeStampsCount).isEqualTo(200)
      assertThat(historyValues.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(1))).isEqualTo(99.0)
    }

    //change size
    builder.resizeTimestamps(100)
    assertThat(builder.timestampsCount).isEqualTo(100)

    builder.build().also { historyValues ->
      assertThat(historyValues).isNotNull()
      assertThat(historyValues.decimalDataSeriesCount).isEqualTo(7)
      assertThat(historyValues.timeStampsCount).isEqualTo(100)
      assertThat(historyValues.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(1))).isEqualTo(99.0)
    }
  }

  @Test
  fun testResizeSameOnlyDecimals() {
    val builder = HistoryValuesBuilder(7, 0, 0, 200, RecordingType.Measured)
    assertThat(builder.timestampsCount).isEqualTo(200)

    val ref = builder.decimalValues

    //Same size - instance should be the same
    builder.resizeTimestamps(200)
    assertThat(builder.decimalValues).isSameAs(ref)

    //Different size - new instance
    builder.resizeTimestamps(100)
    assertThat(builder.decimalValues).isNotSameAs(ref)
  }

  @Test
  fun testResizeSameAll() {
    val builder = HistoryValuesBuilder(7, 6, 5, 200, RecordingType.Measured)
    assertThat(builder.timestampsCount).isEqualTo(200)

    val ref = builder.decimalValues

    //Same size - instance should be the same
    builder.resizeTimestamps(200)
    assertThat(builder.decimalValues).isSameAs(ref)

    //Different size - new instance
    builder.resizeTimestamps(100)
    assertThat(builder.decimalValues).isNotSameAs(ref)
  }

  @Test
  fun testRefValues() {
    val builder = HistoryValuesBuilder(0, 0, 5, 200, RecordingType.Measured)
    assertThat(builder.timestampsCount).isEqualTo(200)

    val referenceEntryId = ReferenceEntryId(17)
    val label = TextKey.simple("DaLabelFor 17")

    builder.setReferenceEntryValue(
      dataSeriesIndex = ReferenceEntryDataSeriesIndex.zero,
      timestampIndex = TimestampIndex(1),
      referenceEntryId = referenceEntryId,
      referenceEntryStatus = HistoryEnumSet.forEnumOrdinal(HistoryEnumOrdinal.BooleanFalse),
      data = ReferenceEntryData(referenceEntryId, label)
    )

    builder.build().also { historyValues ->
      assertThat(historyValues).isNotNull()
      assertThat(historyValues.referenceEntryDataSeriesCount).isEqualTo(5)
      assertThat(historyValues.timeStampsCount).isEqualTo(200)
      assertThat(historyValues.getReferenceEntryId(ReferenceEntryDataSeriesIndex(0), TimestampIndex(1))).isEqualTo(referenceEntryId)
      assertThat(historyValues.getReferenceEntryData(ReferenceEntryDataSeriesIndex(0), referenceEntryId)?.label).isEqualTo(label)
    }

    //change size
    builder.resizeTimestamps(100)
    assertThat(builder.timestampsCount).isEqualTo(100)

    builder.build().also { historyValues ->
      assertThat(historyValues).isNotNull()
      assertThat(historyValues.referenceEntryDataSeriesCount).isEqualTo(5)
      assertThat(historyValues.timeStampsCount).isEqualTo(100)
      assertThat(historyValues.getReferenceEntryId(ReferenceEntryDataSeriesIndex(0), TimestampIndex(1))).isEqualTo(referenceEntryId)
      assertThat(historyValues.getReferenceEntryData(ReferenceEntryDataSeriesIndex(0), referenceEntryId)?.label).isEqualTo(label)
    }
  }
}
