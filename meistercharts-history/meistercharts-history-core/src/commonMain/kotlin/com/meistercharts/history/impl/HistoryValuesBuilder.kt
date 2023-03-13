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
import com.meistercharts.history.DefaultReferenceEntriesDataMap
import com.meistercharts.history.EnumDataSeriesIndex
import com.meistercharts.history.HistoryDebug
import com.meistercharts.history.HistoryEnumOrdinal
import com.meistercharts.history.HistoryEnumOrdinalInt
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.HistoryEnumSetInt
import com.meistercharts.history.ReferenceEntriesDataMap
import com.meistercharts.history.ReferenceEntryData
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryDifferentIdsCountInt
import com.meistercharts.history.ReferenceEntryId
import com.meistercharts.history.ReferenceEntryIdInt
import com.meistercharts.history.TimestampIndex
import com.meistercharts.history.annotations.ForOnePointInTime
import it.neckar.open.collections.DoubleArray2
import it.neckar.open.collections.IntArray2
import it.neckar.open.collections.fastForEach
import kotlin.math.min

/**
 * Helper class to build history values.
 */
class HistoryValuesBuilder(
  /**
   * The number of data serie with decimal value
   */
  val decimalDataSeriesCount: Int,
  /**
   * The number of data series with enum value
   */
  val enumDataSeriesCount: Int,

  /**
   * The number of data series with reference entries
   */
  val referenceEntryDataSeriesCount: Int,

  /**
   * The number of (initial) entries (timestamps)
   */
  initialTimestampsCount: Int,

  /**
   * The recording type this builder creates an object for
   */
  val recordingType: RecordingType,
) {

  /**
   * First index: data series index
   * Second index: time stamp index
   *
   * Attention: The underlying array may be changed if it is resized ([resizeTimestamps]).
   *
   * Contains either the measured value or the average values
   */
  var decimalValues: DoubleArray2 = DoubleArray2(decimalDataSeriesCount, initialTimestampsCount) { HistoryChunk.Pending }
    private set

  /**
   * The min values - only set if [recordingType] is set to [RecordingType.Calculated]
   */
  var minValues: DoubleArray2? = if (recordingType == RecordingType.Calculated) DoubleArray2(decimalDataSeriesCount, initialTimestampsCount) { HistoryChunk.Pending } else null
    private set

  /**
   * The max values - only set if [recordingType] is set to [RecordingType.Calculated]
   */
  var maxValues: DoubleArray2? = if (recordingType == RecordingType.Calculated) DoubleArray2(decimalDataSeriesCount, initialTimestampsCount) { HistoryChunk.Pending } else null
    private set

  /**
   * First index: data series index
   * Second index: time stamp index
   *
   * Attention: The underlying array may be changed if it is resized ([resizeTimestamps])
   */
  var enumValues: IntArray2 = IntArray2(enumDataSeriesCount, initialTimestampsCount) { HistoryEnumSet.PendingAsInt }
    private set

  /**
   * Contains the ordinal that has been recorded for most of the time
   */
  var enumOrdinalsMostTime: IntArray2? = if (recordingType == RecordingType.Calculated) IntArray2(enumDataSeriesCount, initialTimestampsCount) { HistoryEnumOrdinal.Pending.value } else null
    private set

  /**
   * First index: data series index
   * Second index: time stamp index.
   *
   * Contains either the "measured" ids or the "most-of-the-time" id
   */
  var referenceEntryIds: IntArray2 = IntArray2(referenceEntryDataSeriesCount, initialTimestampsCount) { ReferenceEntryId.PendingAsInt }
    private set

  /**
   * Contains the counts for the reference entries.
   *
   * First index: data series index
   * Second index: time stamp index
   */
  var referenceEntryDifferentIdsCount: @ReferenceEntryDifferentIdsCountInt IntArray2? = if (recordingType == RecordingType.Calculated) IntArray2(referenceEntryDataSeriesCount, initialTimestampsCount) { ReferenceEntryId.PendingAsInt } else null
    private set

  /**
   * The [DefaultReferenceEntriesDataMap.Builder] that is later used to build the [ReferenceEntriesDataMap] (for all data series)
   */
  var referenceEntriesDataMapBuilder: DefaultReferenceEntriesDataMap.Builder = DefaultReferenceEntriesDataMap.Builder()
    private set

  /**
   * Returns the current count of time stamps in the [decimalValues] array
   */
  val timestampsCount: Int
    get() {
      return decimalValues.height
    }

  /**
   * Resize this builder for the new time stamps count.
   * This method preserves the currently stored significands.
   */
  fun resizeTimestamps(newTimestampsCount: Int) {
    if (decimalValues.height == newTimestampsCount) {
      //already correct size - do nothing
      return
    }

    decimalValues = decimalValues.resizedCopy(decimalDataSeriesCount, newTimestampsCount)
    minValues = minValues?.resizedCopy(decimalDataSeriesCount, newTimestampsCount)
    maxValues = maxValues?.resizedCopy(decimalDataSeriesCount, newTimestampsCount)
    enumValues = enumValues.resizedCopy(enumDataSeriesCount, newTimestampsCount)
    enumOrdinalsMostTime = enumOrdinalsMostTime?.resizedCopy(enumDataSeriesCount, newTimestampsCount)
    referenceEntryIds = referenceEntryIds.resizedCopy(referenceEntryDataSeriesCount, newTimestampsCount)
    referenceEntryDifferentIdsCount = referenceEntryDifferentIdsCount?.resizedCopy(referenceEntryDataSeriesCount, newTimestampsCount)
  }

  private fun IntArray2.resizedCopy(dataSeriesCount: Int, newTimestampsCount: Int): IntArray2 {
    val new = IntArray2(dataSeriesCount, newTimestampsCount) { HistoryEnumSet.PendingAsInt }
    data.copyInto(new.data, 0, 0, min(data.size, new.data.size))
    return new
  }

  private fun DoubleArray2.resizedCopy(dataSeriesCount: Int, newTimestampsCount: Int): DoubleArray2 {
    val new = DoubleArray2(dataSeriesCount, newTimestampsCount) { HistoryChunk.Pending }
    data.copyInto(new.data, 0, 0, min(data.size, new.data.size))
    return new
  }

  /**
   * Sets the [decimalValue] for the data series at index [dataSeriesIndex] for the timestamp at index [timestampIndex]
   */
  fun setDecimalValue(dataSeriesIndex: DecimalDataSeriesIndex, timestampIndex: TimestampIndex, decimalValue: Double) {
    require(recordingType == RecordingType.Measured) { "Only supported for measured" }
    decimalValues[dataSeriesIndex, timestampIndex] = decimalValue
  }

  /**
   * Sets the [enumValue] for the data series at index [dataSeriesIndex] for the timestamp at index [timestampIndex]
   */
  fun setEnumValue(dataSeriesIndex: EnumDataSeriesIndex, timestampIndex: TimestampIndex, enumValue: HistoryEnumSet) {
    require(recordingType == RecordingType.Measured) { "Only supported for measured" }
    enumValues[dataSeriesIndex, timestampIndex] = enumValue
  }

  /**
   * Sets a single reference entry value
   */
  fun setReferenceEntryValue(dataSeriesIndex: ReferenceEntryDataSeriesIndex, timestampIndex: TimestampIndex, referenceEntryId: ReferenceEntryId, data: ReferenceEntryData) {
    require(recordingType == RecordingType.Measured) { "Only supported for measured" }

    referenceEntryIds[dataSeriesIndex, timestampIndex] = referenceEntryId
    getEntriesDataMapBuilder().store(data)
  }

  /**
   * Returns the builder for this data series index
   */
  private fun getEntriesDataMapBuilder(): DefaultReferenceEntriesDataMap.Builder {
    return referenceEntriesDataMapBuilder
  }

  /**
   * Sets the [decimalValues] for all data series for the timestamp at index [timestampIndex].
   *
   * Attention: Depending on the [recordingType] [minValues] and [maxValues] must be provided or not
   */
  fun setDecimalValuesForTimestamp(timestampIndex: TimestampIndex, decimalValues: DoubleArray, minValues: DoubleArray? = null, maxValues: DoubleArray? = null) {
    //Verify the parameters
    when (recordingType) {
      RecordingType.Measured -> require(minValues == null && maxValues == null) { "Min/max values must only be provided when recordingType is Measured" }
      RecordingType.Calculated -> require(minValues != null && maxValues != null) { "Min/max values must be provided when recordingType is Calculated" }
    }

    require(decimalDataSeriesCount == decimalValues.size) {
      "Invalid size of decimal values array. Expected <$decimalDataSeriesCount> but was <${decimalValues.size}>"
    }

    val targetStartIndex = HistoryValues.calculateStartIndex(decimalDataSeriesCount, timestampIndex)

    decimalValues.copyInto(this.decimalValues.data, targetStartIndex, 0, decimalDataSeriesCount)
    @Suppress("ReplaceNotNullAssertionWithElvisReturn")
    maxValues?.copyInto(this.maxValues!!.data, targetStartIndex, 0, decimalDataSeriesCount)
    @Suppress("ReplaceNotNullAssertionWithElvisReturn")
    minValues?.copyInto(this.minValues!!.data, targetStartIndex, 0, decimalDataSeriesCount)
  }

  fun setEnumValuesForTimestamp(timestampIndex: TimestampIndex, enumValues: @HistoryEnumSetInt IntArray, enumOrdinalsMostTime: @HistoryEnumOrdinalInt IntArray? = null) {
    require(enumDataSeriesCount == enumValues.size) {
      "Invalid size of enum values array. Expected <$enumDataSeriesCount> but was <${enumValues.size}>"
    }

    when (recordingType) {
      RecordingType.Measured -> require(enumOrdinalsMostTime == null) { "enumOrdinalsMostTime must only be provided when recordingType is Measured" }
      RecordingType.Calculated -> require(enumOrdinalsMostTime!=null) { "enumOrdinalsMostTime must be provided when recordingType is Calculated" }
    }

    if (enumOrdinalsMostTime != null) {
      require(enumDataSeriesCount == enumOrdinalsMostTime.size) {
        "Invalid size of enum winners array. Expected <$enumDataSeriesCount> but was <${enumOrdinalsMostTime.size}>"
      }

      //Verify content
      if (HistoryDebug.additionalVerificationEnabled) {
        enumOrdinalsMostTime.forEachIndexed { index, mostOfTheTimeValueAsInt ->
          val mostOfTheTimeValue = HistoryEnumOrdinal(mostOfTheTimeValueAsInt)
          val historyEnumSetAsInt = enumValues[index]
          val historyEnumSet = HistoryEnumSet(historyEnumSetAsInt)

          //Check both should be most of the time value
          if (mostOfTheTimeValue.isNoValue()) {
            require(historyEnumSet.isNoValue()) { "expected no value but was $historyEnumSet" }
          }
          if (mostOfTheTimeValue.isPending()) {
            require(historyEnumSet.isPending()) { "expected pending but was $historyEnumSet" }
          }

          if (historyEnumSet.isNoValue()) {
            require(mostOfTheTimeValue.isNoValue()) { "expected no value but was $mostOfTheTimeValue" }
          }
          if (historyEnumSet.isPending()) {
            require(mostOfTheTimeValue.isPending()) { "expected pending but was $mostOfTheTimeValue" }
          }
        }
      }
    }

    //TODO Think about this check!
    enumValues.fastForEach {
      HistoryEnumSet.isValid(it)
    }

    val targetStartIndex = HistoryValues.calculateStartIndex(enumDataSeriesCount, timestampIndex)
    enumValues.copyInto(this.enumValues.data, targetStartIndex, 0, enumDataSeriesCount)

    enumOrdinalsMostTime?.copyInto(requireNotNull(this.enumOrdinalsMostTime).data, targetStartIndex, 0, enumDataSeriesCount)
  }

  /**
   * Sets the reference entry ids for the given timestamp.
   * Sets either the measured values or "most of the time"
   */
  @ForOnePointInTime
  fun setReferenceEntryIdsForTimestamp(
    timestampIndex: TimestampIndex,
    referenceEntryIds: @ReferenceEntryIdInt IntArray,
    referenceEntryIdsCount: @ReferenceEntryDifferentIdsCountInt IntArray? = null,
    /**
     * Each [referenceEntryIds] must contain exactly one entry in this list.
     * The values are added to [referenceEntriesDataMapBuilder]. Duplicates are automatically removed.
     */
    entryDataSet: Set<ReferenceEntryData>,
  ) {
    require(referenceEntryDataSeriesCount == referenceEntryIds.size) {
      "Invalid size of values array. Expected <$referenceEntryDataSeriesCount> but was <${referenceEntryIds.size}>"
    }

    when (recordingType) {
      RecordingType.Measured -> require(referenceEntryIdsCount == null) { "referenceEntryIdsCount must only be provided when recordingType is Measured" }
      RecordingType.Calculated -> require(referenceEntryIdsCount != null) { "referenceEntryIdsCount must be provided when recordingType is Calculated" }
    }

    if (referenceEntryIdsCount != null) {
      require(referenceEntryDataSeriesCount == referenceEntryIdsCount.size) {
        "Invalid size of enum winners array. Expected <$referenceEntryDataSeriesCount> but was <${referenceEntryIdsCount.size}>"
      }
    }

    val targetStartIndex = HistoryValues.calculateStartIndex(referenceEntryDataSeriesCount, timestampIndex)
    referenceEntryIds.copyInto(this.referenceEntryIds.data, targetStartIndex, 0, referenceEntryDataSeriesCount)

    referenceEntryIdsCount?.copyInto(requireNotNull(this.referenceEntryDifferentIdsCount).data, targetStartIndex, 0, referenceEntryDataSeriesCount)

    //Store all data elements
    referenceEntriesDataMapBuilder.storeAll(entryDataSet)
  }

  /**
   * Sets all values for a given timestamp index
   */
  fun setAllValuesForTimestamp(
    timestampIndex: TimestampIndex,

    /**
     * The measured decimal values - or calculated averages
     */
    decimalValues: @Domain DoubleArray,
    minValues: @Domain DoubleArray?,
    maxValues: @Domain DoubleArray?,

    /**
     * The enum values - measured or combined sets
     */
    enumValues: @HistoryEnumSetInt IntArray,
    enumOrdinalsMostTime: @HistoryEnumOrdinalInt IntArray?,

    /**
     * The reference entry ids - measured or "most of the time"
     */
    referenceEntryIds: @ReferenceEntryIdInt IntArray,
    referenceEntryDifferentIdsCount: @ReferenceEntryDifferentIdsCountInt IntArray? = null,

    entryDataSet: Set<ReferenceEntryData>,
  ) {
    setDecimalValuesForTimestamp(timestampIndex, decimalValues, minValues, maxValues)
    setEnumValuesForTimestamp(timestampIndex, enumValues, enumOrdinalsMostTime)
    setReferenceEntryIdsForTimestamp(timestampIndex, referenceEntryIds, referenceEntryDifferentIdsCount, entryDataSet)
  }

  /**
   * Builds the history values for the given recording type
   */
  fun build(): HistoryValues {
    return when (recordingType) {
      RecordingType.Measured -> HistoryValues(
        decimalValues = decimalValues,
        enumValues = enumValues,
        referenceEntryIds = referenceEntryIds,
        minValues = null,
        maxValues = null,
        mostOfTheTimeValues = null,
        referenceEntryIdsCount = null,
        referenceEntriesDataMap = referenceEntriesDataMapBuilder.build()
      )

      RecordingType.Calculated -> HistoryValues(
        decimalValues = decimalValues,
        enumValues = enumValues,
        referenceEntryIds = referenceEntryIds,
        minValues = minValues,
        maxValues = maxValues,
        mostOfTheTimeValues = enumOrdinalsMostTime,
        referenceEntryIdsCount = referenceEntryDifferentIdsCount,
        referenceEntriesDataMap = referenceEntriesDataMapBuilder.build()
      )
    }
  }
}

/**
 * Creates a new [HistoryValues] object
 */
fun historyValues(
  /**
   * The number of decimal data series
   */
  decimalDataSeriesCount: Int,
  /**
   * The number of enum data series
   */
  enumDataSeriesCount: Int,

  /**
   * The number of reference entry data series
   */
  referenceEntryDataSeriesCount: Int,


  /**
   * The number of entries (timestamps)
   */
  timestampsCount: Int,

  /**
   * The recording type for this history values object
   */
  recordingType: RecordingType = RecordingType.Measured,

  /**
   * The lambda that is used to configure the history values builder
   */
  config: HistoryValuesBuilder.() -> Unit,
): HistoryValues {
  return HistoryValuesBuilder(decimalDataSeriesCount, enumDataSeriesCount, referenceEntryDataSeriesCount, timestampsCount, recordingType).also(config).build()
}
