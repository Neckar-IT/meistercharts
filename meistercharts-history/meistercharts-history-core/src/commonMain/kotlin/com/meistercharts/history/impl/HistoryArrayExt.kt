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

import com.meistercharts.annotations.Domain
import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.EnumDataSeriesIndex
import com.meistercharts.history.HistoryEnumOrdinal
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.HistoryEnumSetInt
import com.meistercharts.history.MayBeNoValueOrPending
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryDifferentIdsCount
import com.meistercharts.history.ReferenceEntryId
import com.meistercharts.history.ReferenceEntryIdInt
import com.meistercharts.history.TimestampIndex
import com.meistercharts.history.impl.HistoryChunk.Companion.isNoValue
import com.meistercharts.history.impl.HistoryChunk.Companion.isPending
import it.neckar.open.collections.DoubleArray2
import it.neckar.open.collections.DoubleArrayList
import it.neckar.open.collections.IntArray2
import it.neckar.open.unit.si.ms

/**
 * Extension methods for arrays that are needed for the history
 */

/**
 * Copy of range for time stamps
 */
inline fun DoubleArray.copyOfRange(fromIndex: TimestampIndex, toIndex: TimestampIndex): DoubleArray {
  return this.copyOfRange(fromIndex.value, toIndex.value)
}

inline fun DoubleArray.getTimestamp(index: TimestampIndex): Double {
  return this[index.value]
}

/**
 * Sets the decimal value for one timestamp index
 */
inline operator fun DoubleArray2.set(dataSeriesIndex: DecimalDataSeriesIndex, timestampIndex: TimestampIndex, value: @Domain Double) {
  set(dataSeriesIndex.value, timestampIndex.value, value)
}

/**
 * Sets the value for an enum
 */
inline operator fun IntArray2.set(dataSeriesIndex: EnumDataSeriesIndex, timestampIndex: TimestampIndex, value: HistoryEnumSet) {
  set(dataSeriesIndex.value, timestampIndex.value, value.bitset)
}

/**
 * Used for the status enums
 */
inline operator fun IntArray2.set(dataSeriesIndex: ReferenceEntryDataSeriesIndex, timestampIndex: TimestampIndex, value: HistoryEnumSet) {
  set(dataSeriesIndex.value, timestampIndex.value, value.bitset)
}

/**
 * Sets the value for the reference entry
 */
inline operator fun IntArray2.set(dataSeriesIndex: ReferenceEntryDataSeriesIndex, timestampIndex: TimestampIndex, value: ReferenceEntryId) {
  set(dataSeriesIndex.value, timestampIndex.value, value.id)
}

/**
 * Sets the timestamp
 */
inline fun DoubleArrayList.setTimestamp(timestampIndex: TimestampIndex, timestamp: @ms Double) {
  this[timestampIndex.value] = timestamp
}

inline fun DoubleArray2.getDecimalValue(dataSeriesIndex: DecimalDataSeriesIndex, timeStampIndex: TimestampIndex): @Domain Double {
  return this[dataSeriesIndex.value, timeStampIndex.value]
}

inline fun IntArray2.getRawValue(dataSeriesIndex: EnumDataSeriesIndex, timeStampIndex: TimestampIndex): @HistoryEnumSetInt Int {
  return this[dataSeriesIndex.value, timeStampIndex.value]
}

inline fun IntArray2.getEnumSet(dataSeriesIndex: EnumDataSeriesIndex, timeStampIndex: TimestampIndex): HistoryEnumSet {
  return HistoryEnumSet(getRawValue(dataSeriesIndex, timeStampIndex))
}

inline fun IntArray2.getEnumOrdinal(dataSeriesIndex: EnumDataSeriesIndex, timeStampIndex: TimestampIndex): @MayBeNoValueOrPending HistoryEnumOrdinal {
  return HistoryEnumOrdinal(getRawValue(dataSeriesIndex, timeStampIndex))
}

inline fun IntArray2.getRawValue(dataSeriesIndex: ReferenceEntryDataSeriesIndex, timeStampIndex: TimestampIndex): @ReferenceEntryIdInt Int {
  return this[dataSeriesIndex.value, timeStampIndex.value]
}

inline fun IntArray2.getReferenceEntry(dataSeriesIndex: ReferenceEntryDataSeriesIndex, timeStampIndex: TimestampIndex): ReferenceEntryId {
  //TODO introduce sealed class for downsampling(?)
  return ReferenceEntryId(getRawValue(dataSeriesIndex, timeStampIndex))
}

/**
 * Extracts the IDs coutn for the provided data series index and timestamp index
 */
inline fun IntArray2.getReferenceEntryDifferentIdsCount(dataSeriesIndex: ReferenceEntryDataSeriesIndex, timeStampIndex: TimestampIndex): ReferenceEntryDifferentIdsCount {
  return ReferenceEntryDifferentIdsCount(getRawValue(dataSeriesIndex, timeStampIndex))
}

inline fun IntArray2.getReferenceStatus(dataSeriesIndex: ReferenceEntryDataSeriesIndex, timeStampIndex: TimestampIndex): HistoryEnumSet {
  return HistoryEnumSet(getRawValue(dataSeriesIndex, timeStampIndex))
}


/**
 * Formats the int array as matrix string
 */
fun IntArray2?.asEnumsMatrixString(): String {
  if (this == null) {
    return "- no values -"
  }

  return (0 until height).joinToString("\n") { y ->
    (0 until width).map { x -> this[x, y] }.joinToString(", ") {
      HistoryEnumSet(it).toString()
    }
  }
}

/**
 * Formats the int array as matrix string.
 * Uses decimals to represent the values
 */
fun IntArray2?.asMatrixString(): String {
  if (this == null) {
    return "- no values -"
  }

  return (0 until height).joinToString("\n") { y ->
    (0 until width).map { x -> this[x, y] }.joinToString(", ") {
      it.toString()
    }
  }
}

fun DoubleArray2?.asMatrixString(): String {
  if (this == null) {
    return "- no values -"
  }

  return (0 until height).joinToString("\n") { y ->
    (0 until width).map { x -> this[x, y] }.joinToString(", ") {
      when {
        it.isNoValue() -> "-"
        it.isPending() -> "?"
        else -> it.toString()
      }
    }
  }
}
