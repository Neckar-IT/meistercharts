package com.meistercharts.history.impl

import assertk.*
import assertk.assertions.support.*
import com.meistercharts.annotations.Domain
import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.EnumDataSeriesIndex
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.HistoryEnumSetInt
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryId
import com.meistercharts.history.ReferenceEntryIdInt
import com.meistercharts.history.TimestampIndex
import it.neckar.open.collections.fastForEachIndexed
import it.neckar.open.kotlin.lang.isCloseTo

/**
 *
 */
fun Assert<HistoryChunk>.hasValues(dataSeriesIndex: DecimalDataSeriesIndex, vararg expectedValues: @Domain Double): Unit = given { actual ->
  expectedValues.fastForEachIndexed { index, expectedValue ->
    val actualValue = actual.getDecimalValue(dataSeriesIndex, TimestampIndex(index))

    if (actualValue.isCloseTo(expectedValue).not()) {
      expected("${show(expectedValue)} but was ${show(actualValue)} at time index $index for data series index ${dataSeriesIndex.value}")
    }
  }
}

fun Assert<HistoryChunk>.hasValues(dataSeriesIndex: EnumDataSeriesIndex, vararg expectedValues: @HistoryEnumSetInt Int): Unit = given { actual ->
  expectedValues.fastForEachIndexed { index, expectedValue ->
    val expectedEnumSet = HistoryEnumSet(expectedValue)

    val actualValue = actual.getEnumValue(dataSeriesIndex, TimestampIndex(index))

    if (actualValue != expectedEnumSet) {
      expected("${show(expectedEnumSet)} but was ${show(actualValue)} at time index $index for data series index ${dataSeriesIndex.value}")
    }
  }
}

fun Assert<HistoryChunk>.hasValues(dataSeriesIndex: ReferenceEntryDataSeriesIndex, vararg expectedValues: @ReferenceEntryIdInt Int): Unit = given { actual ->
  expectedValues.fastForEachIndexed { index, expectedValue ->
    val expectedId = ReferenceEntryId(expectedValue)

    val actualValue = actual.getReferenceEntryId(dataSeriesIndex, TimestampIndex(index))

    if (actualValue != expectedId) {
      expected("${show(expectedId)} but was ${show(actualValue)} at time index $index for data series index ${dataSeriesIndex.value}")
    }
  }
}
