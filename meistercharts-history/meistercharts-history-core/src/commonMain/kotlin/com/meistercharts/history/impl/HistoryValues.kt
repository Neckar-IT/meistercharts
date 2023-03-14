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
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.HistoryEnumOrdinal
import com.meistercharts.history.HistoryEnumOrdinalInt
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.HistoryEnumSetInt
import com.meistercharts.history.MayBeNoValueOrPending
import com.meistercharts.history.ReferenceEntriesDataMap
import com.meistercharts.history.ReferenceEntryData
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryDifferentIdsCount
import com.meistercharts.history.ReferenceEntryId
import com.meistercharts.history.ReferenceEntryIdInt
import com.meistercharts.history.TimestampIndex
import com.meistercharts.history.annotations.ForOnePointInTime
import com.meistercharts.history.impl.HistoryChunk.Companion.isPending
import it.neckar.open.annotations.TestOnly
import it.neckar.open.collections.DoubleArray2
import it.neckar.open.collections.IntArray2
import it.neckar.open.collections.invokeCols
import kotlinx.serialization.Serializable

/**
 * Contains the history data values. Should not be used directly.
 *
 *
 * Distinction to the other classes:
 * * [HistoryValues] does *only* contain the values - no timestamps
 * * [HistoryChunk] contains the [HistoryConfiguration], the [HistoryValues] *and* the timestamps. Also has a [RecordingType]
 * * [com.meistercharts.history.HistoryBucket] contains a [HistoryChunk] and a [com.meistercharts.history.HistoryBucketDescriptor]. Is placed on "event" borders!
 *
 *
 * Contains an int array (decimals are held in [HistoryConfiguration.decimalConfiguration]) for each data series.
 *
 * The values can be accessed like that:
 * ```
 * significantValues[dataSeriesIndex][timeStampIndex]
 * ```
 *
 * For enum values:
 * ```
 * enumValues[datasSeriesIndex][timeStampIndex]
 * ```
 */
@Serializable
class HistoryValues(
  /**
   * Contains the decimal values
   */
  val decimalHistoryValues: DecimalHistoryValues,
  /**
   * Contains the enum values
   */
  val enumHistoryValues: EnumHistoryValues,
  /**
   * Contains the reference entry values
   */
  val referenceEntryHistoryValues: ReferenceEntryHistoryValues,
) {

  constructor(
    decimalValues: @Domain DoubleArray2,
    enumValues: @HistoryEnumSetInt IntArray2,
    referenceEntryIds: @ReferenceEntryIdInt IntArray2,

    minValues: @Domain DoubleArray2? = null,
    maxValues: @Domain DoubleArray2? = null,

    enumMostOfTheTimeValues: @MayBeNoValueOrPending @HistoryEnumOrdinalInt IntArray2? = null,

    referenceEntryIdsCount: @ReferenceEntryIdInt IntArray2? = null,
    referenceEntryStatuses: @HistoryEnumSetInt IntArray2,
    referenceEntriesDataMap: ReferenceEntriesDataMap,
  ) : this(
    decimalHistoryValues = DecimalHistoryValues(decimalValues, minValues, maxValues),
    enumHistoryValues = EnumHistoryValues(enumValues, enumMostOfTheTimeValues),
    referenceEntryHistoryValues = ReferenceEntryHistoryValues(referenceEntryIds, referenceEntryIdsCount, referenceEntryStatuses, referenceEntriesDataMap)
  )

  @TestOnly
  @Deprecated("only for tests")
  constructor(
    decimalsDataArray: Array<DoubleArray>,
    enumDataArray: Array<@HistoryEnumSetInt IntArray>,
    referenceEntryDataArray: Array<@ReferenceEntryIdInt IntArray>,
    referenceEntryStatuses: @HistoryEnumSetInt IntArray2,
  ) : this(
    decimalValues = DoubleArray2.invokeCols(decimalsDataArray),
    enumValues = IntArray2.invokeCols(enumDataArray),
    referenceEntryIds = IntArray2.invokeCols(referenceEntryDataArray),
    minValues = null,
    maxValues = null,
    enumMostOfTheTimeValues = null,
    referenceEntryIdsCount = null,
    referenceEntryStatuses = referenceEntryStatuses,
    referenceEntriesDataMap = ReferenceEntriesDataMap.generated,
  )

  init {
    if (decimalHistoryValues.minValues != null) {
      requireNotNull(enumHistoryValues.mostOfTheTimeValues) {
        "mostOfTheTimeValues must also be set if min values are set"
      }
      requireNotNull(referenceEntryHistoryValues.hasIdsCount) {
        "referenceEntryHistoryValues must also contain ids count if min values are set"
      }
    }

    require(enumHistoryValues.isEmpty || decimalHistoryValues.isEmpty || decimalHistoryValues.timeStampsCount == enumHistoryValues.timeStampsCount) {
      "Non matching timestamps count <${decimalHistoryValues.timeStampsCount}> - ${enumHistoryValues.timeStampsCount}"
    }
  }

  /**
   * Returns true if there exist min and max values
   */
  val hasMinMax: Boolean
    get() {
      return decimalHistoryValues.hasMinMax
    }

  val hasMostOfTheTimeValues: Boolean
    get() {
      return enumHistoryValues.hasMostOfTheTimeValues
    }

  /**
   * The amount of total data series - of all types
   */
  val totalDataSeriesCount: Int
    get() {
      return decimalDataSeriesCount + enumDataSeriesCount + referenceEntryDataSeriesCount
    }

  /**
   * The amount of data series with decimal values
   */
  val decimalDataSeriesCount: Int
    get() {
      return decimalHistoryValues.dataSeriesCount
    }

  val enumDataSeriesCount: Int
    get() {
      return enumHistoryValues.dataSeriesCount
    }

  val referenceEntryDataSeriesCount: Int
    get() {
      return referenceEntryHistoryValues.dataSeriesCount
    }

  val timeStampsCount: Int
    get() {
      //Same as enumValues.height
      return decimalHistoryValues.timeStampsCount
    }

  /**
   * Returns the entries data map for the given series
   */
  val referenceEntriesDataMap: ReferenceEntriesDataMap
    get() {
      return referenceEntryHistoryValues.dataMap
    }

  /**
   * Returns the value at the given position.
   * ATTENTION: Might return [HistoryChunk.NoValue] or [HistoryChunk.Pending]
   */
  fun getDecimalValue(dataSeriesIndex: DecimalDataSeriesIndex, timeStampIndex: TimestampIndex): Double {
    return decimalHistoryValues.getDecimalValue(dataSeriesIndex, timeStampIndex)
  }

  fun getEnumValue(dataSeriesIndex: EnumDataSeriesIndex, timeStampIndex: TimestampIndex): HistoryEnumSet {
    return enumHistoryValues.getEnumValue(dataSeriesIndex, timeStampIndex)
  }

  /**
   * Returns the reference entry id (measured or most-of-the-time)
   */
  fun getReferenceEntryId(dataSeriesIndex: ReferenceEntryDataSeriesIndex, timeStampIndex: TimestampIndex): @MayBeNoValueOrPending ReferenceEntryId {
    return referenceEntryHistoryValues.getReferenceEntryId(dataSeriesIndex, timeStampIndex)
  }

  /**
   * Returns the count of different IDs.
   * Returns [ReferenceEntryDifferentIdsCount.one] for measured values.
   */
  fun getReferenceEntryDifferentIdsCount(dataSeriesIndex: ReferenceEntryDataSeriesIndex, timeStampIndex: TimestampIndex): @MayBeNoValueOrPending ReferenceEntryDifferentIdsCount {
    return referenceEntryHistoryValues.getDifferentIdsCount(dataSeriesIndex, timeStampIndex)
  }

  fun getReferenceEntryStatus(dataSeriesIndex: ReferenceEntryDataSeriesIndex, timeStampIndex: TimestampIndex): @MayBeNoValueOrPending HistoryEnumSet {
    return referenceEntryHistoryValues.getReferenceEntryStatus(dataSeriesIndex, timeStampIndex)
  }

  /**
   * Returns the *last* (youngest) value for the given data series index
   */
  fun getLastDecimalValue(dataSeriesIndex: DecimalDataSeriesIndex): Double? {
    return decimalHistoryValues.getLastDecimalValue(dataSeriesIndex)
  }

  fun getLastEnumValue(dataSeriesIndex: EnumDataSeriesIndex): HistoryEnumSet? {
    return enumHistoryValues.getLastEnumValue(dataSeriesIndex)
  }

  /**
   * Returns the significands for the given timestamp (instantiates a new array)
   */
  fun getDecimalValues(timeStampIndex: TimestampIndex): @Domain DoubleArray {
    return decimalHistoryValues.getDecimalValues(timeStampIndex)
  }

  fun getEnumValues(timeStampIndex: TimestampIndex): @HistoryEnumSetInt IntArray {
    return enumHistoryValues.getEnumValues(timeStampIndex)
  }

  fun getReferenceEntryIds(timeStampIndex: TimestampIndex): @ReferenceEntryIdInt IntArray {
    return referenceEntryHistoryValues.getReferenceEntryIds(timeStampIndex)
  }

  @ForOnePointInTime
  fun getReferenceEntryStatuses(timeStampIndex: TimestampIndex): @HistoryEnumSetInt IntArray {
    return referenceEntryHistoryValues.getReferenceEntryStatuses(timeStampIndex)
  }

  /**
   * Returns the counts for the different entry IDs
   */
  fun getReferenceEntryDifferentIdsCounts(timeStampIndex: TimestampIndex): @ReferenceEntryIdInt IntArray? {
    return referenceEntryHistoryValues.getDifferentIdsCounts(timeStampIndex)
  }

  /**
   * Returns the [ReferenceEntryData] for the provided [dataSeriesIndex] and [id]
   */
  fun getReferenceEntryData(dataSeriesIndex: ReferenceEntryDataSeriesIndex, id: ReferenceEntryId): ReferenceEntryData? {
    return referenceEntryHistoryValues.getData(id)
  }

  fun getReferenceEntryDataSet(referenceEntryIds: @ReferenceEntryIdInt IntArray): Set<ReferenceEntryData> {
    return referenceEntryHistoryValues.getDataSet(referenceEntryIds)
  }

  /**
   * Returns the start index for the given timestamp index.
   * This index can be used to access the values directly.
   */
  @Deprecated("No longer needed?")
  fun calculateDecimalStartIndex(timeStampIndex: TimestampIndex): Int {
    return calculateStartIndex(decimalDataSeriesCount, timeStampIndex)
  }

  /**
   * Returns the start index for the given timestamp index.
   * This index can be used to access the values directly.
   */
  @Deprecated("No longer needed?")
  fun calculateEnumStartIndex(timeStampIndex: TimestampIndex): Int {
    return calculateStartIndex(enumDataSeriesCount, timeStampIndex)
  }

  /**
   * Returns the min value - falls back to the value if no max value exists
   */
  fun getMin(dataSeriesIndex: DecimalDataSeriesIndex, timeStampIndex: TimestampIndex): Double {
    return decimalHistoryValues.getMin(dataSeriesIndex, timeStampIndex)
  }

  /**
   * Returns the max value - falls back to the value if no max value exists
   */
  fun getMax(dataSeriesIndex: DecimalDataSeriesIndex, timeStampIndex: TimestampIndex): Double {
    return decimalHistoryValues.getMax(dataSeriesIndex, timeStampIndex)
  }

  /**
   * Returns the enum value that has been measured most of the time - falls back to the measured value
   */
  fun getEnumOrdinalMostTime(dataSeriesIndex: EnumDataSeriesIndex, timeStampIndex: TimestampIndex): @MayBeNoValueOrPending HistoryEnumOrdinal {
    return enumHistoryValues.getEnumOrdinalMostTime(dataSeriesIndex, timeStampIndex)
  }

  /**
   * Formats the history values as matrix string (human-readable)
   */
  fun decimalValuesAsMatrixString(): String {
    return decimalHistoryValues.decimalValuesAsMatrixString()
  }

  fun enumValuesAsMatrixString(): String {
    return enumHistoryValues.valuesAsMatrixString()
  }

  fun enumMostOfTheTimeValuesAsMatrixString(): String? {
    return enumHistoryValues.mostOfTheTimeValuesAsMatrixString()
  }

  fun minValuesAsMatrixString(): String {
    return decimalHistoryValues.minValuesAsMatrixString()
  }

  fun maxValuesAsMatrixString(): String {
    return decimalHistoryValues.maxValuesAsMatrixString()
  }

  fun referenceIdsAsMatrixString(): String {
    return referenceEntryHistoryValues.idsAsMatrixString()
  }

  fun referenceEntryCountsAsMatrixString(): String? {
    return referenceEntryHistoryValues.countsAsMatrixString()
  }

  fun referenceEntryStatusesAsMatrixString(): String? {
    return referenceEntryHistoryValues.statusesAsMatrixString()
  }

  /**
   * Returns true if the values at the given timestamp index are still pending
   */
  fun isPending(timeStampIndex: TimestampIndex): Boolean {
    if (decimalDataSeriesCount > 0) {
      return getDecimalValue(DecimalDataSeriesIndex.zero, timeStampIndex).isPending()
    }

    if (enumDataSeriesCount > 0) {
      return getEnumValue(EnumDataSeriesIndex.zero, timeStampIndex).isPending()
    }

    if (referenceEntryDataSeriesCount > 0) {
      return getReferenceEntryId(ReferenceEntryDataSeriesIndex.zero, timeStampIndex).isPending()
    }

    //No values
    return false
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as HistoryValues

    if (decimalHistoryValues != other.decimalHistoryValues) return false
    return enumHistoryValues == other.enumHistoryValues
  }

  override fun hashCode(): Int {
    var result = decimalHistoryValues.hashCode()
    result = 31 * result + enumHistoryValues.hashCode()
    return result
  }

  companion object {
    /**
     * Returns an empty values object
     */
    fun empty(recordingType: RecordingType): HistoryValues {
      return when (recordingType) {
        RecordingType.Measured -> HistoryValues(
          decimalValues = DoubleArray2(0, 0) { HistoryChunk.Pending },
          enumValues = IntArray2(0, 0) { HistoryEnumSet.PendingAsInt },
          referenceEntryIds = IntArray2(0, 0) { HistoryEnumSet.PendingAsInt },
          referenceEntryStatuses = IntArray2(0, 0) { HistoryEnumSet.PendingAsInt },
          referenceEntriesDataMap = ReferenceEntriesDataMap.empty,
        )

        RecordingType.Calculated -> HistoryValues(
          decimalValues = DoubleArray2(0, 0) { HistoryChunk.Pending },
          enumValues = IntArray2(0, 0) { HistoryEnumSet.PendingAsInt },
          referenceEntryIds = IntArray2(0, 0) { HistoryEnumSet.PendingAsInt },

          minValues = DoubleArray2(0, 0) { HistoryChunk.Pending },
          maxValues = DoubleArray2(0, 0) { HistoryChunk.Pending },
          enumMostOfTheTimeValues = IntArray2(0, 0) { HistoryEnumOrdinal.Pending.value },
          referenceEntryIdsCount = IntArray2(0, 0) { HistoryEnumOrdinal.Pending.value },

          referenceEntryStatuses = IntArray2(0, 0) { HistoryEnumSet.PendingAsInt },

          referenceEntriesDataMap = ReferenceEntriesDataMap.empty,
        )
      }
    }

    /**
     * Calculates the start index that can be used to access the values array directly
     */
    fun calculateStartIndex(dataSeriesCount: Int, timeStampIndex: TimestampIndex): Int {
      return dataSeriesCount * timeStampIndex.value
    }
  }
}
