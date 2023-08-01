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
import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.EnumDataSeriesIndex
import com.meistercharts.history.HistoryBucket
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.HistoryEnumOrdinal
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.HistoryEnumSet.Companion.NoValueAsInt
import com.meistercharts.history.HistoryEnumSet.Companion.PendingAsInt
import com.meistercharts.history.HistoryEnumSetInt
import com.meistercharts.history.MayBeNoValueOrPending
import com.meistercharts.history.ReferenceEntriesDataMap
import com.meistercharts.history.ReferenceEntryData
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryDifferentIdsCount
import com.meistercharts.history.ReferenceEntryId
import com.meistercharts.history.ReferenceEntryIdInt
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.TimestampIndex
import com.meistercharts.history.annotations.ForOnePointInTime
import com.meistercharts.time.TimeRange
import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.logging.debug
import it.neckar.open.annotations.Slow
import it.neckar.open.collections.BSearchResult
import it.neckar.open.collections.DoubleArrayList
import it.neckar.open.collections.IntArray2
import it.neckar.open.collections.binarySearch
import it.neckar.open.collections.fastForEach
import it.neckar.open.collections.fastForEachIndexed
import it.neckar.open.collections.sort
import it.neckar.open.formatting.formatUtc
import it.neckar.open.kotlin.lang.fastFor
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.other.Exclusive
import it.neckar.open.unit.other.Inclusive
import it.neckar.open.unit.other.Sorted
import it.neckar.open.unit.si.ms
import kotlinx.serialization.Serializable
import kotlin.math.max
import kotlin.math.min


/**
 * Contains a chunk of history data.
 *
 * Distinction to the other classes:
 * * [HistoryValues] does *only* contain the values - no timestamps
 * * [HistoryChunk] contains the [HistoryConfiguration], the [HistoryValues] *and* the timestamps.
 * * [com.meistercharts.history.HistoryBucket] contains a [HistoryChunk] and a [com.meistercharts.history.HistoryBucketDescriptor]. Is placed on "event" borders!
 */
@Serializable
data class HistoryChunk(

  /**
   * The history configuration - contains the IDs, decimal places and display names
   *
   * Each entry in [values] contains as many values as there are data series ids stored in the configuration.
   */
  val configuration: HistoryConfiguration,

  /**
   * The time stamps for the history chunk. Each entry in [values] corresponds to
   * one timestamp within this array.
   */
  val timeStamps: @ms @Sorted DoubleArray,

  /**
   * Contains the values.
   * Contains one entry for each timestamp in [timeStamps]. Each entry contains one value for each data series id stored in the [configuration]
   *
   * ATTENTION: Must not be modified!
   */
  val values: HistoryValues,

  /**
   * The type of the history chunk
   */
  val recordingType: RecordingType,
) {

  init {
    require(totalDataSeriesCount > 0) {
      "No data series provided"
    }

    require(timeStampsCount == 0 || configuration.totalDataSeriesCount == values.totalDataSeriesCount) {
      "data series count size mismatch - expected: ${configuration.totalDataSeriesCount} - values: ${values.totalDataSeriesCount}"
    }

    require(totalDataSeriesCount == 0 || values.timeStampsCount == timeStamps.size) {
      "size mismatch - time stamp count: ${values.timeStampsCount} - ${timeStamps.size}"
    }

    when (recordingType) {
      RecordingType.Measured -> {
      }

      RecordingType.Calculated -> {
        require(values.hasMinMax) { "Must contain min/max values if calculated" }
        require(values.hasMostOfTheTimeValues) { "Must contain most of the times values if calculated" }
      }
    }
  }

  /**
   * Returns the number of time stamps.
   * For each time stamp every data series has exactly one value.
   */
  val timeStampsCount: Int
    get() = timeStamps.size

  /**
   * Returns the number of data series
   */
  val totalDataSeriesCount: Int
    get() = configuration.totalDataSeriesCount

  val decimalDataSeriesCount: Int
    get() = configuration.decimalDataSeriesCount

  val enumDataSeriesCount: Int
    get() = configuration.enumDataSeriesCount

  val referenceEntryDataSeriesCount: Int
    get() = configuration.referenceEntryDataSeriesCount

  /**
   * Returns true if there are no time stamps in this chunk
   */
  fun isEmpty(): Boolean {
    return timeStampsCount == 0
  }

  /**
   * Returns the value of the *first* timestamp
   */
  @Inclusive
  val firstTimestamp: Double
    get() = timeStamps.first()

  @Deprecated("use firstTimestamp instead", ReplaceWith("firstTimestamp"))
  @Inclusive
  val start: Double
    get() = firstTimestamp

  /**
   * The time of the last timestamp
   */
  @Inclusive
  val lastTimestamp: Double
    get() = timeStamps.last()

  @Deprecated("use lastTimestamp instead", ReplaceWith("lastTimestamp"))
  @Inclusive
  val end: Double
    get() = lastTimestamp

  /**
   * Returns the time stamp for the given index.
   *
   * This is either:
   * * the exact timestamp ([RecordingType.Measured])
   * * the center of average / bit set
   */
  fun timestampCenter(timeStampIndex: TimestampIndex): @ms Double {
    if (timeStampIndex.value >= timeStamps.size) {
      throw IndexOutOfBoundsException("timeStampIndex: $timeStampIndex too large. Size is ${timeStamps.size}")
    }
    return timeStamps[timeStampIndex.value]
  }

  /**
   * Returns whether the sample for the timestamp at index [timeStampIndex] is pending
   *
   * @see PendingAsInt
   */
  fun isPending(timeStampIndex: TimestampIndex): Boolean {
    return values.isPending(timeStampIndex)
  }

  /**
   * Returns the significand for the data series at index [dataSeriesIndex] for the timestamp at index [timeStampIndex]
   * @param dataSeriesIndex the *INDEX* of the data series. This is *not* the data series id. Use [getDecimalDataSeriesIndex] to resolve the index.
   * @param timeStampIndex the index of the timestamp
   * @see getValue
   */
  fun getDecimalValue(dataSeriesIndex: DecimalDataSeriesIndex, timeStampIndex: TimestampIndex): @MayBeNaN Double {
    val rawValue = values.getDecimalValue(dataSeriesIndex, timeStampIndex)
    return rawValue.nanIfPendingOrNoValue()
  }

  private fun Double.nanIfPendingOrNoValue(): Double {
    if (isPending() || isNoValue()) {
      return Double.NaN
    }

    return this
  }

  /**
   * Returns the enum value for the given indices
   */
  fun getEnumValue(dataSeriesIndex: EnumDataSeriesIndex, timeStampIndex: TimestampIndex): @MayBeNoValueOrPending HistoryEnumSet {
    return values.getEnumValue(dataSeriesIndex, timeStampIndex)
  }

  /**
   * Returns the reference entry id (measured or most-of-the-time).
   *
   */
  fun getReferenceEntryId(dataSeriesIndex: ReferenceEntryDataSeriesIndex, timeStampIndex: TimestampIndex): @MayBeNoValueOrPending ReferenceEntryId {
    return values.getReferenceEntryId(dataSeriesIndex, timeStampIndex)
  }

  /**
   * Returns the count of different IDs.
   * Returns [ReferenceEntryDifferentIdsCount.one] for measured values.
   */
  fun getReferenceEntryIdsCount(dataSeriesIndex: ReferenceEntryDataSeriesIndex, timeStampIndex: TimestampIndex): @MayBeNoValueOrPending ReferenceEntryDifferentIdsCount {
    return values.getReferenceEntryDifferentIdsCount(dataSeriesIndex, timeStampIndex)
  }

  fun getReferenceEntryStatus(dataSeriesIndex: ReferenceEntryDataSeriesIndex, timeStampIndex: TimestampIndex): @MayBeNoValueOrPending HistoryEnumSet {
    return values.getReferenceEntryStatus(dataSeriesIndex, timeStampIndex)
  }

  /**
   * Returns the max value
   */
  fun getMax(dataSeriesIndex: DecimalDataSeriesIndex, timeStampIndex: TimestampIndex): @MayBeNaN Double {
    return values.getMax(dataSeriesIndex, timeStampIndex).nanIfPendingOrNoValue()
  }

  fun hasDecimalMinMaxValues(): Boolean {
    return values.hasDecimalMinMaxValues()
  }

  /**
   * Returns the min value
   */
  fun getMin(dataSeriesIndex: DecimalDataSeriesIndex, timeStampIndex: TimestampIndex): @MayBeNaN Double {
    return values.getMin(dataSeriesIndex, timeStampIndex).nanIfPendingOrNoValue()
  }

  /**
   * Returns the last value - or null if there are no values stored
   */
  @Deprecated("no longer used?")
  fun lastDecimalValueOrNull(dataSeriesIndex: DecimalDataSeriesIndex): Double? {
    return values.getLastDecimalValue(dataSeriesIndex)?.nanIfPendingOrNoValue()
  }

  /**
   * Returns the enum value that has been measured *most* of the time
   */
  fun getEnumOrdinalMostTime(dataSeriesIndex: EnumDataSeriesIndex, timeStampIndex: TimestampIndex): @MayBeNoValueOrPending HistoryEnumOrdinal {
    return values.getEnumOrdinalMostTime(dataSeriesIndex, timeStampIndex)
  }

  /**
   * This method returns min and max values for the given data series index.
   * Attention! This method iterates all values. This method is only useful for debugging purposes.
   *
   * @return min/max values
   */
  @Slow
  fun findMinMaxValue(dataSeriesIndex: DecimalDataSeriesIndex): Pair<Double, Double> {
    var max = -Double.MAX_VALUE
    var min = Double.MAX_VALUE

    for (timestampIndex in 0 until timeStampsCount) {
      val value = getDecimalValue(dataSeriesIndex, TimestampIndex(timestampIndex))
      max = max(max, value)
      min = min(min, value)
    }

    return min to max
  }

  /**
   * Returns the data series index for a given data series id
   */
  @Slow
  fun getDecimalDataSeriesIndex(dataSeriesId: DataSeriesId): DecimalDataSeriesIndex {
    return configuration.decimalConfiguration.getDataSeriesIndex(dataSeriesId)
  }

  /**
   * Returns the data series id at the given index
   */
  fun getDecimalDataSeriesId(dataSeriesIndex: DecimalDataSeriesIndex): DataSeriesId {
    return configuration.decimalConfiguration.getDataSeriesId(dataSeriesIndex)
  }

  @Slow
  fun getEnumDataSeriesIndex(dataSeriesId: DataSeriesId): EnumDataSeriesIndex {
    return configuration.enumConfiguration.getDataSeriesIndex(dataSeriesId)
  }

  fun getEnumDataSeriesId(dataSeriesIndex: EnumDataSeriesIndex): DataSeriesId {
    return configuration.enumConfiguration.getDataSeriesId(dataSeriesIndex)
  }

  @Slow
  fun getReferenceEntryDataSeriesIndex(dataSeriesId: DataSeriesId): ReferenceEntryDataSeriesIndex {
    return configuration.referenceEntryConfiguration.getDataSeriesIndex(dataSeriesId)
  }

  fun getReferenceEntryDataSeriesId(dataSeriesIndex: ReferenceEntryDataSeriesIndex): DataSeriesId {
    return configuration.referenceEntryConfiguration.getDataSeriesId(dataSeriesIndex)
  }

  /**
   * Returns the (best) index for the given time.
   *
   * This method works like the binarySearch methods in collection classes.
   *
   * It returns:
   * * positive values (>=0): Direct hit for the exact time stamp
   * * negative values (<0): representing the index where the new timestamp would be inserted into.
   *
   * ## Calculation of negative values
   * The returned index is calculated as: (-(potential insertion index) - 1)
   */
  fun bestTimestampIndexFor(@ms timeStamp: Double): BSearchResult {
    return timeStamps.binarySearch(timeStamp)
  }

  /**
   * Returns the significands for the given time stamp index.
   */
  @ForOnePointInTime
  fun getDecimalValues(timeStampIndex: TimestampIndex): @Domain DoubleArray {
    return values.getDecimalValues(timeStampIndex)
  }

  @ForOnePointInTime
  fun getDecimalMinValues(timeStampIndex: TimestampIndex): DoubleArray? {
    return values.getDecimalMinValues(timeStampIndex)
  }

  @ForOnePointInTime
  fun getDecimalMaxValues(timeStampIndex: TimestampIndex): DoubleArray? {
    return values.getDecimalMaxValues(timeStampIndex)
  }

  @ForOnePointInTime
  fun getEnumValues(timeStampIndex: TimestampIndex): @HistoryEnumSetInt IntArray {
    return values.getEnumValues(timeStampIndex)
  }

  @ForOnePointInTime
  fun getReferenceEntryIds(timeStampIndex: TimestampIndex): @ReferenceEntryIdInt IntArray {
    return values.getReferenceEntryIds(timeStampIndex)
  }

  @ForOnePointInTime
  fun getReferenceEntryStatuses(timeStampIndex: TimestampIndex): @HistoryEnumSetInt IntArray {
    return values.getReferenceEntryStatuses(timeStampIndex)
  }

  /**
   * Returns the counts for the different entry IDs
   */
  @ForOnePointInTime
  fun getReferenceEntryDifferentIdsCounts(timeStampIndex: TimestampIndex): @ReferenceEntryIdInt IntArray? {
    return values.getReferenceEntryDifferentIdsCounts(timeStampIndex)
  }

  /**
   * Returns the reference entry data for the provided index
   */
  val referenceEntriesDataMap: ReferenceEntriesDataMap
    get() {
      return values.referenceEntriesDataMap
    }

  /**
   * Returns the [ReferenceEntryData] for the provided [dataSeriesIndex] and [id]
   */
  fun getReferenceEntryData(dataSeriesIndex: ReferenceEntryDataSeriesIndex, id: ReferenceEntryId): ReferenceEntryData? {
    return values.getReferenceEntryData(dataSeriesIndex, id)
  }

  /**
   * Returns the first time stamp. Returns null if the chunk is empty
   */
  fun firstTimeStamp(): @ms Double {
    return timeStamps.first()
  }

  /**
   * Returns the last timestamp
   */
  fun lastTimeStamp(): @ms Double {
    return timeStamps.last()
  }

  fun lastTimeStampOrNull(): Double? {
    return timeStamps.lastOrNull()
  }

  fun lastTimeStampIndex(): TimestampIndex {
    return TimestampIndex(timeStampsCount - 1)
  }


  /**
   * Returns the reference entry data list for the reference entry ids
   */
  fun getReferenceEntryDataSet(referenceEntryIds: @ReferenceEntryIdInt IntArray): Set<ReferenceEntryData> {
    return values.getReferenceEntryDataSet(referenceEntryIds)
  }

  fun getReferenceEntryDataSet(referenceEntryIds: @ReferenceEntryIdInt IntArray2): Set<ReferenceEntryData> {
    return values.getReferenceEntryDataSet(referenceEntryIds.data)
  }

  /**
   * Creates a new chunk with the added values for the given timestamp
   */
  fun withAddedValues(
    additionalTimeStamp: @ms Double,

    additionalDecimalValues: @Domain @ForOnePointInTime DoubleArray,
    additionalDecimalMinValues: @Domain @ForOnePointInTime DoubleArray?,
    additionalDecimalMaxValues: @Domain @ForOnePointInTime DoubleArray?,

    additionalEnumValues: @HistoryEnumSetInt @ForOnePointInTime IntArray,

    additionalReferenceEntryIds: @ReferenceEntryIdInt @ForOnePointInTime IntArray,
    additionalReferenceEntryStatuses: @HistoryEnumSetInt IntArray,
    /**
     * Contains the data for the [additionalReferenceEntryIds]. Each ID must contain one entry (if there is data)
     */
    additionalReferenceEntryDataList: @ForOnePointInTime Set<ReferenceEntryData>,
  ): HistoryChunk {
    //Convert to significands array
    require(additionalDecimalValues.size == decimalDataSeriesCount)

    requireMeasuringMode()

    require(additionalDecimalValues.size == decimalDataSeriesCount) {
      "Invalid values size. Was <${additionalDecimalValues.size}> but expected <$decimalDataSeriesCount>"
    }

    val mergedTimeStamps = DoubleArrayList()
    mergedTimeStamps.add(timeStamps)
    mergedTimeStamps.add(additionalTimeStamp)
    mergedTimeStamps.sort() //sorts inline


    //Merge the values
    val newHistoryValuesBuilder = HistoryValuesBuilder(decimalDataSeriesCount, enumDataSeriesCount, referenceEntryDataSeriesCount, mergedTimeStamps.size, recordingType)

    mergedTimeStamps.fastForEachIndexed { index, timestamp: @ms Double ->
      //Add the values
      val decimalValues: @Domain @ForOnePointInTime DoubleArray
      val decimalMinValues: @Domain @ForOnePointInTime DoubleArray?
      val decimalMaxValues: @Domain @ForOnePointInTime DoubleArray?

      val enumValues: @HistoryEnumSetInt @ForOnePointInTime IntArray
      val referenceEntryIds: @ReferenceEntryIdInt @ForOnePointInTime IntArray
      val referenceEntryDataSet: @ForOnePointInTime Set<ReferenceEntryData>
      val referenceEntryStatuses: @HistoryEnumSetInt IntArray

      if (additionalTimeStamp == timestamp) {
        decimalValues = additionalDecimalValues
        decimalMinValues = additionalDecimalMinValues
        decimalMaxValues = additionalDecimalMaxValues
        enumValues = additionalEnumValues
        referenceEntryIds = additionalReferenceEntryIds
        referenceEntryStatuses = additionalReferenceEntryStatuses
        referenceEntryDataSet = additionalReferenceEntryDataList
      } else {
        //Use from us
        val timeStampIndex = TimestampIndex(bestTimestampIndexFor(timestamp).index)
        decimalValues = getDecimalValues(timeStampIndex)
        decimalMinValues = getDecimalMinValues(timeStampIndex)
        decimalMaxValues = getDecimalMaxValues(timeStampIndex)
        enumValues = getEnumValues(timeStampIndex)
        referenceEntryIds = getReferenceEntryIds(timeStampIndex)
        referenceEntryStatuses = getReferenceEntryStatuses(timeStampIndex)
        referenceEntryDataSet = getReferenceEntryDataSet(referenceEntryIds)
      }

      newHistoryValuesBuilder.setAllValuesForTimestamp(
        timestampIndex = TimestampIndex(index),
        decimalValues = decimalValues, minValues = decimalMinValues, maxValues = decimalMaxValues,
        enumValues = enumValues, enumOrdinalsMostTime = null,
        referenceEntryIds = referenceEntryIds, referenceEntryDifferentIdsCount = null, referenceEntryStatuses = referenceEntryStatuses,
        entryDataSet = referenceEntryDataSet,
      )
    }

    return HistoryChunk(configuration, mergedTimeStamps.toDoubleArray(), newHistoryValuesBuilder.build(), recordingType)
  }

  private fun requireMeasuringMode() {
    require(recordingType == RecordingType.Measured) {
      "Only supported for measuring mode"
    }
  }

  /**
   * Merges this with the given history chunk.
   *
   * Start and end describe the start and end of the resulting [HistoryChunk].
   *
   * To be clear:
   * This *could* contain data *before* [start] and/or data *after* [end].
   * [other] *could* contain data *before* [start] and/or data *after* [end].
   *
   * The resulting [HistoryChunk] *must* only contain data between [start] and [end]
   *
   */
  fun merge(other: HistoryChunk, start: @Inclusive Double, end: @Exclusive Double): HistoryChunk? {
    require(start < end) { "start must be before end. Was ${start.formatUtc()} - ${end.formatUtc()}" }
    logger.debug { "merge($other, ${start.formatUtc()}, ${end.formatUtc()})" }

    //The configurations must match - if they don't we can't merge them
    require(configuration == other.configuration) { "Configurations do not match: $configuration - ${other.configuration}." }

    val decimalDataSeriesCount = configuration.decimalDataSeriesCount
    val enumDataSeriesCount = configuration.enumDataSeriesCount
    val referenceEntryDataSeriesCount = configuration.referenceEntryDataSeriesCount

    require(recordingType == other.recordingType) { "recordingType must match. Was $recordingType - ${other.recordingType}" }

    //At the moment this method is only used for measured chunks, we can not merge calculate chunks at the moment
    require(recordingType == RecordingType.Measured) { "only supported for RecordingType.Measured at the moment" }
    require(other.recordingType == RecordingType.Measured) { "only supported for RecordingType.Measured at the moment" }


    //First test some (simple) special cases
    if (other.isEmpty() || other.containsAny(start, end).not()) {
      //other is empty or does not contain relevant data, just return this
      return this.range(start, end)
    }

    if (this.isEmpty()) {
      //This is empty (but other is *not*), just return other
      return other.range(start, end)
    }

    //Check if other is completely after this
    if (this.lastTimeStamp() < other.firstTimeStamp()) {
      //Just append the other chunk to this
      return append(other, start, end)
    }

    //Check if other is completely before this
    if (other.lastTimeStamp() < this.firstTimeStamp()) {
      //Just append this to the other timestamp
      return other.append(this, start, end)
    }


    //
    //ATTENTION: we know for sure, that we *have* to merge the chunks. All other cases have been handled above
    //

    //Calculate the timestamp indices that shall be used from this and other

    val thisStartIndex = this.bestTimestampIndexFor(start).nearIndex
    @Inclusive val thisEndIndex: Int = this.bestTimestampIndexFor(end).let {
      if (it.found) {
        //endFromOther is exclusive - use the previous entry
        it.index - 1
      } else {
        it.nearIndex - 1
      }
    }

    if (thisEndIndex == -1) {
      throw IllegalStateException("no index to add from this! $thisStartIndex - $thisEndIndex")
    }

    //Find the first index that is same or greater than start
    val otherStartIndex = other.bestTimestampIndexFor(start).nearIndex

    @Inclusive val otherEndIndex: Int = other.bestTimestampIndexFor(end).let {
      if (it.found) {
        //endFromOther is exclusive - use the previous entry
        it.index - 1
      } else {
        it.nearIndex - 1
      }
    }

    if (otherEndIndex == -1) {
      throw IllegalStateException("no index to add from other! $otherStartIndex - $otherEndIndex")
    }

    //Collect all timestamps
    val thisRelevantTimeStamps = this.timeStamps.copyOfRange(thisStartIndex, thisEndIndex + 1)
    val otherRelevantTimeStamps = other.timeStamps.copyOfRange(otherStartIndex, otherEndIndex + 1)

    //Contains all unique relevant time stamps
    val uniqueRelevantTimeStamps = buildSet<@ms Double> {
      thisRelevantTimeStamps.fastForEach {
        add(it)
      }
      otherRelevantTimeStamps.fastForEach {
        add(it)
      }
    }

    //merge the data
    @ms val mergedTimeStampsBuilder = DoubleArrayList(uniqueRelevantTimeStamps.size)
    val mergedHistoryValuesBuilder = HistoryValuesBuilder(decimalDataSeriesCount, enumDataSeriesCount, referenceEntryDataSeriesCount, uniqueRelevantTimeStamps.size, recordingType)

    var mergedHistoryValuesTimestampIndex = TimestampIndex(0)


    var thisRelevantTimeStampIndex = TimestampIndex(0)
    var otherRelevantTimeStampIndex = TimestampIndex(0)

    val thisRelevantTimestampsSize = thisRelevantTimeStamps.size
    val otherRelevantTimestampsSize = otherRelevantTimeStamps.size


    /**
     * Adds the current row from this.
     * This method also increases the indices
     */
    fun addRowFromThis() {
      @ms val thisRelevantTimeStamp = thisRelevantTimeStamps.getTimestamp(thisRelevantTimeStampIndex)

      //Add the timestamp
      mergedTimeStampsBuilder.add(thisRelevantTimeStamp)

      //Add the decimal values
      this.copyDecimalValuesTo(
        mergedHistoryValuesBuilder,
        HistoryValues.calculateStartIndex(decimalDataSeriesCount, mergedHistoryValuesTimestampIndex),
        HistoryValues.calculateStartIndex(decimalDataSeriesCount, thisRelevantTimeStampIndex),
        HistoryValues.calculateStartIndex(decimalDataSeriesCount, thisRelevantTimeStampIndex + 1),
      )

      this.copyEnumValuesTo(
        mergedHistoryValuesBuilder,
        HistoryValues.calculateStartIndex(enumDataSeriesCount, mergedHistoryValuesTimestampIndex),
        HistoryValues.calculateStartIndex(enumDataSeriesCount, thisRelevantTimeStampIndex),
        HistoryValues.calculateStartIndex(enumDataSeriesCount, thisRelevantTimeStampIndex + 1)
      )

      this.copyReferenceEntryValuesTo(
        mergedHistoryValuesBuilder,
        HistoryValues.calculateStartIndex(referenceEntryDataSeriesCount, mergedHistoryValuesTimestampIndex),
        HistoryValues.calculateStartIndex(referenceEntryDataSeriesCount, thisRelevantTimeStampIndex),
        HistoryValues.calculateStartIndex(referenceEntryDataSeriesCount, thisRelevantTimeStampIndex + 1),
      )

      //Increase the indices
      thisRelevantTimeStampIndex++
      mergedHistoryValuesTimestampIndex++
    }

    /**
     * Adds the current row from other.
     * This method also increases the indices
     */
    fun addRowFromOther() {
      @ms val otherRelevantTimeStamp = otherRelevantTimeStamps.getTimestamp(otherRelevantTimeStampIndex)

      //Add the timestamp
      mergedTimeStampsBuilder.add(otherRelevantTimeStamp)

      other.copyDecimalValuesTo(
        mergedHistoryValuesBuilder,
        HistoryValues.calculateStartIndex(decimalDataSeriesCount, mergedHistoryValuesTimestampIndex),
        HistoryValues.calculateStartIndex(decimalDataSeriesCount, otherRelevantTimeStampIndex),
        HistoryValues.calculateStartIndex(decimalDataSeriesCount, otherRelevantTimeStampIndex + 1),
      )

      other.copyEnumValuesTo(
        mergedHistoryValuesBuilder,
        HistoryValues.calculateStartIndex(enumDataSeriesCount, mergedHistoryValuesTimestampIndex),
        HistoryValues.calculateStartIndex(enumDataSeriesCount, otherRelevantTimeStampIndex),
        HistoryValues.calculateStartIndex(enumDataSeriesCount, otherRelevantTimeStampIndex + 1),
      )

      other.copyReferenceEntryValuesTo(
        mergedHistoryValuesBuilder,
        HistoryValues.calculateStartIndex(referenceEntryDataSeriesCount, mergedHistoryValuesTimestampIndex),
        HistoryValues.calculateStartIndex(referenceEntryDataSeriesCount, otherRelevantTimeStampIndex),
        HistoryValues.calculateStartIndex(referenceEntryDataSeriesCount, otherRelevantTimeStampIndex + 1),
      )

      //Increase the indices
      otherRelevantTimeStampIndex++
      mergedHistoryValuesTimestampIndex++
    }


    while (true) {
      val thisTimeStampAvailable = thisRelevantTimeStampIndex < thisRelevantTimestampsSize
      val otherTimeStampAvailable = otherRelevantTimeStampIndex < otherRelevantTimestampsSize

      if (thisTimeStampAvailable.not() && otherTimeStampAvailable.not()) {
        //No timestamps available, anymore
        break
      }

      //At least one of chunks contains anymore timestamps

      //First case: Only this is available
      if (otherTimeStampAvailable.not()) {
        //only this available
        addRowFromThis()
        continue
      }

      //Second case: Only other is available
      if (thisTimeStampAvailable.not()) {
        //only other available
        addRowFromOther()
        continue
      }

      //Third case: *BOTH* are available

      @ms val thisRelevantTimeStamp = thisRelevantTimeStamps.getTimestamp(thisRelevantTimeStampIndex)
      @ms val otherRelevantTimeStamp = otherRelevantTimeStamps.getTimestamp(otherRelevantTimeStampIndex)

      //Find the smaller one
      when {
        thisRelevantTimeStamp == otherRelevantTimeStamp -> {
          //if both values are the same, just use *this*
          addRowFromThis()
          //but also increase the index for other!
          otherRelevantTimeStampIndex++
        }

        thisRelevantTimeStamp < otherRelevantTimeStamp -> {
          //this is smaller - or same!
          addRowFromThis()
        }

        thisRelevantTimeStamp > otherRelevantTimeStamp -> {
          //this is smaller - or same!
          addRowFromOther()
        }

        else -> {
          throw IllegalStateException("invalid state $thisRelevantTimeStamp -- $otherRelevantTimeStamp")
        }
      }
    }

    //The merged time stamps
    val mergedTimeStamps = mergedTimeStampsBuilder.toDoubleArray()

    require(mergedTimeStamps.size == uniqueRelevantTimeStamps.size) {
      "Expected ${uniqueRelevantTimeStamps.size} timestamps - but got ${mergedTimeStamps.size} - missing ${
        uniqueRelevantTimeStamps.toHashSet().also {
          it.removeAll(mergedTimeStamps.asIterable().toSet())
        }
      }"
    }

    //Add the values from both chunks
    mergedHistoryValuesBuilder.referenceEntriesDataMapBuilder.storeAll(other.getReferenceEntryDataSet(mergedHistoryValuesBuilder.referenceEntryIds))
    mergedHistoryValuesBuilder.referenceEntriesDataMapBuilder.storeAll(this.getReferenceEntryDataSet(mergedHistoryValuesBuilder.referenceEntryIds))

    return HistoryChunk(other.configuration, mergedTimeStamps, mergedHistoryValuesBuilder.build(), RecordingType.Measured).also {
      //verify the created chunk - must be within start/end
      require(it.firstTimeStamp() >= start) { "Invalid first time stamp <${it.firstTimeStamp()} - must be greater/equal to start $start" }
      require(it.lastTimeStamp() < end) { "Invalid last time stamp <${it.lastTimeStamp()} - must be smaller than end $end" }
    }
  }

  /**
   * Merges this with [other] by appending [other] at the end.
   *
   * This method must only be called if [other] is *after *this.
   *
   * The returning chunk will only contain data between [start] and [end]
   */
  internal fun append(other: HistoryChunk, start: @Inclusive Double, end: @Exclusive Double): HistoryChunk? {
    require(this.lastTimeStamp() < other.firstTimeStamp()) {
      "other must be after this"
    }

    //Find the first index that is same or greater than start
    val otherStartIndex = TimestampIndex(other.bestTimestampIndexFor(start).nearIndex)

    @Inclusive val otherEndIndex: TimestampIndex = TimestampIndex(other.bestTimestampIndexFor(end).let {
      if (it.found) {
        //endFromOther is exclusive - use the previous entry
        it.index - 1
      } else {
        it.nearIndex - 1
      }
    })

    if (otherEndIndex.isNegative()) {
      //Nothing to add from other
      return this.range(start, end)
    }


    //The index for start - in this
    val thisStartIndex = TimestampIndex(bestTimestampIndexFor(start).nearIndex)

    //The index of end - in this
    @Inclusive val thisEndIndex = TimestampIndex(bestTimestampIndexFor(end).let {
      if (it.found) {
        it.index - 1
      } else {
        it.nearIndex - 1
      }
    })

    if (thisEndIndex.isNegative()) {
      throw IllegalArgumentException("No data found. Range: ${start.formatUtc()} - ${end.formatUtc()} ")
    }

    require(thisStartIndex <= thisEndIndex) {
      "Invalid startIndex $thisStartIndex and/or endIndex $thisEndIndex"
    }

    //The number of time stamps from this
    val timeStampsCountFromThis = thisEndIndex.value - thisStartIndex.value + 1
    val timeStampsCountFromOther = otherEndIndex.value - otherStartIndex.value + 1


    //merge the data - sorting is not necessary
    @ms val mergedTimeStampsBuilder = DoubleArrayList(timeStampsCountFromThis + timeStampsCountFromOther)

    //Add only the values that fit into start
    mergedTimeStampsBuilder.add(this.timeStamps.copyOfRange(thisStartIndex, thisEndIndex + 1))
    mergedTimeStampsBuilder.add(other.timeStamps.copyOfRange(otherStartIndex, otherEndIndex + 1))

    //The merged time stamps
    val mergedTimeStamps = mergedTimeStampsBuilder.toDoubleArray()

    //Merge the values
    val newHistoryValuesBuilder = HistoryValuesBuilder(decimalDataSeriesCount, enumDataSeriesCount, referenceEntryDataSeriesCount, mergedTimeStamps.size, recordingType)

    //Copy my data into the new builder
    val thisStartArrayIndexDecimal = HistoryValues.calculateStartIndex(decimalDataSeriesCount, thisStartIndex)
    val thisEndArrayIndexDecimal = HistoryValues.calculateStartIndex(decimalDataSeriesCount, thisEndIndex + 1)
    copyDecimalValuesTo(target = newHistoryValuesBuilder, targetOffset = 0, thisStartArrayIndexDecimal = thisStartArrayIndexDecimal, thisEndArrayIndexDecimal = thisEndArrayIndexDecimal)

    @Inclusive val thisStartArrayIndexEnum = HistoryValues.calculateStartIndex(this.enumDataSeriesCount, thisStartIndex)
    @Inclusive val thisEndArrayIndexEnum = HistoryValues.calculateStartIndex(this.enumDataSeriesCount, thisEndIndex + 1)
    copyEnumValuesTo(target = newHistoryValuesBuilder, targetOffset = 0, thisStartArrayIndexEnum = thisStartArrayIndexEnum, thisEndArrayIndexEnum = thisEndArrayIndexEnum)

    @Inclusive val thisStartArrayIndexReferenceEntry = HistoryValues.calculateStartIndex(referenceEntryDataSeriesCount, thisStartIndex)
    @Inclusive val thisEndArrayIndexReferenceEntry = HistoryValues.calculateStartIndex(this.referenceEntryDataSeriesCount, thisEndIndex + 1)
    copyReferenceEntryValuesTo(target = newHistoryValuesBuilder, targetOffset = 0, thisStartArrayIndexReferenceEntry = thisStartArrayIndexReferenceEntry, thisEndArrayIndexReferenceEntry = thisEndArrayIndexReferenceEntry)

    //Copy the data from the other chunk
    other.copyDecimalValuesTo(
      target = newHistoryValuesBuilder,
      targetOffset = thisEndArrayIndexDecimal - thisStartArrayIndexDecimal,
      thisStartArrayIndexDecimal = HistoryValues.calculateStartIndex(other.decimalDataSeriesCount, otherStartIndex),
      thisEndArrayIndexDecimal = HistoryValues.calculateStartIndex(other.decimalDataSeriesCount, otherEndIndex + 1)
    )

    other.copyEnumValuesTo(
      target = newHistoryValuesBuilder, targetOffset = thisEndArrayIndexEnum - thisStartArrayIndexEnum,
      thisStartArrayIndexEnum = HistoryValues.calculateStartIndex(other.enumDataSeriesCount, otherStartIndex),
      thisEndArrayIndexEnum = HistoryValues.calculateStartIndex(other.enumDataSeriesCount, otherEndIndex + 1)
    )

    other.copyReferenceEntryValuesTo(
      target = newHistoryValuesBuilder,
      targetOffset = thisEndArrayIndexReferenceEntry - thisStartArrayIndexReferenceEntry,
      thisStartArrayIndexReferenceEntry = HistoryValues.calculateStartIndex(other.referenceEntryDataSeriesCount, otherStartIndex),
      thisEndArrayIndexReferenceEntry = HistoryValues.calculateStartIndex(other.referenceEntryDataSeriesCount, otherEndIndex + 1)
    )

    return HistoryChunk(other.configuration, mergedTimeStamps, newHistoryValuesBuilder.build(), RecordingType.Measured).also {
      //verify the created chunk
      require(it.firstTimeStamp() >= start) { "Invalid first time stamp <${it.firstTimeStamp().formatUtc()} - must be greater/equal to start ${start.formatUtc()}" }
      require(it.lastTimeStamp() < end) { "Invalid last time stamp <${it.lastTimeStamp().formatUtc()} - must be smaller than end ${end.formatUtc()}" }
    }
  }

  /**
   * Returns true if this chunk contains any data from the given range
   */
  fun containsAny(start: @Inclusive @ms Double, end: @Exclusive @ms Double): Boolean {
    require(start < end) { "start <${start.formatUtc()}> must be smaller than end <${end.formatUtc()}>" }

    if (isEmpty()) {
      return false
    }

    if (start > lastTimeStamp() || end <= firstTimeStamp()) {
      return false
    }

    return true
  }

  /**
   * Returns only the given range
   */
  fun range(start: @Inclusive @ms Double, end: @Exclusive @ms Double): HistoryChunk? {
    require(start < end) { "start ${start.formatUtc()} must be smaller than end ${end.formatUtc()}" }

    //Handle simple cases first)
    if (start <= firstTimeStamp() && end > lastTimeStamp()) {
      //This is completely within start..end - just return this
      return this
    }

    if (start > lastTimeStamp()) {
      //This is completely *before* the requested range
      return null
    }

    if (end < firstTimeStamp()) {
      //This is completely *after* the requested range
      return null
    }

    //The index for start - in this
    val startIndex = TimestampIndex(bestTimestampIndexFor(start).nearIndex)

    //The index of end - in this
    @Inclusive val endIndex = TimestampIndex(bestTimestampIndexFor(end).let {
      if (it.found) {
        it.index - 1
      } else {
        it.nearIndex - 1
      }
    })

    if (startIndex > endIndex) {
      return null
    }

    if (endIndex.isNegative()) {
      throw IllegalArgumentException("No data found. Range: ${start.formatUtc()} - ${end.formatUtc()} ")
    }

    //select the data
    val newTimeStamps = timeStamps.copyOfRange(startIndex, endIndex + 1)

    //Merge the values
    val newHistoryValuesBuilder = HistoryValuesBuilder(decimalDataSeriesCount, enumDataSeriesCount, referenceEntryDataSeriesCount, newTimeStamps.size, recordingType)

    //Copy the data
    copyDecimalValuesTo(
      target = newHistoryValuesBuilder,
      targetOffset = 0,
      thisStartArrayIndexDecimal = HistoryValues.calculateStartIndex(decimalDataSeriesCount, startIndex),
      thisEndArrayIndexDecimal = HistoryValues.calculateStartIndex(decimalDataSeriesCount, endIndex + 1)
    )
    copyEnumValuesTo(
      target = newHistoryValuesBuilder,
      targetOffset = 0,
      thisStartArrayIndexEnum = HistoryValues.calculateStartIndex(enumDataSeriesCount, startIndex),
      thisEndArrayIndexEnum = HistoryValues.calculateStartIndex(enumDataSeriesCount, endIndex + 1)
    )
    copyReferenceEntryValuesTo(
      target = newHistoryValuesBuilder,
      targetOffset = 0,
      thisStartArrayIndexReferenceEntry = HistoryValues.calculateStartIndex(referenceEntryDataSeriesCount, startIndex),
      thisEndArrayIndexReferenceEntry = HistoryValues.calculateStartIndex(referenceEntryDataSeriesCount, endIndex + 1)
    )

    return HistoryChunk(configuration, newTimeStamps, newHistoryValuesBuilder.build(), recordingType).also {
      require(it.firstTimeStamp() >= start) { "Invalid first time stamp <${it.firstTimeStamp()} - must be greater/equal to start $start" }
      require(it.lastTimeStamp() < end) { "Invalid last time stamp <${it.lastTimeStamp()} - must be smaller than end $end" }
    }
  }

  /**
   * Copies the decimal values to the provided target
   */
  private fun copyDecimalValuesTo(target: HistoryValuesBuilder, targetOffset: Int, thisStartArrayIndexDecimal: @Inclusive Int, thisEndArrayIndexDecimal: @Inclusive Int) {
    this.values.decimalHistoryValues.values.data.copyInto(destination = target.decimalValues.data, destinationOffset = targetOffset, startIndex = thisStartArrayIndexDecimal, endIndex = thisEndArrayIndexDecimal)
    this.values.decimalHistoryValues.maxValues?.data?.copyInto(destination = target.maxValuesInitialized.data, destinationOffset = targetOffset, startIndex = thisStartArrayIndexDecimal, endIndex = thisEndArrayIndexDecimal)
    this.values.decimalHistoryValues.minValues?.data?.copyInto(destination = target.minValuesInitialized.data, destinationOffset = targetOffset, startIndex = thisStartArrayIndexDecimal, endIndex = thisEndArrayIndexDecimal)
  }

  /**
   * Copies the enum values to the provided target
   */
  private fun copyEnumValuesTo(target: HistoryValuesBuilder, targetOffset: Int, thisStartArrayIndexEnum: @Inclusive Int, thisEndArrayIndexEnum: @Inclusive Int) {
    this.values.enumHistoryValues.values.data.copyInto(target.enumValues.data, targetOffset, thisStartArrayIndexEnum, thisEndArrayIndexEnum)
    this.values.enumHistoryValues.mostOfTheTimeValues?.data?.copyInto(target.enumOrdinalsMostTimeInitialized.data, targetOffset, thisStartArrayIndexEnum, thisEndArrayIndexEnum)
  }

  private fun copyReferenceEntryValuesTo(target: HistoryValuesBuilder, targetOffset: Int, thisStartArrayIndexReferenceEntry: @Inclusive Int, thisEndArrayIndexReferenceEntry: @Inclusive Int) {
    this.values.referenceEntryHistoryValues.values.data.copyInto(target.referenceEntryIds.data, targetOffset, thisStartArrayIndexReferenceEntry, thisEndArrayIndexReferenceEntry)
    this.values.referenceEntryHistoryValues.statuses.data.copyInto(target.referenceEntryStatuses.data, targetOffset, thisStartArrayIndexReferenceEntry, thisEndArrayIndexReferenceEntry)
    this.values.referenceEntryHistoryValues.differentIdsCount?.data?.copyInto(target.referenceEntryDifferentIdsCountInitialized.data, targetOffset, thisStartArrayIndexReferenceEntry, thisEndArrayIndexReferenceEntry)
    target.referenceEntriesDataMapBuilder.storeAll(this.getReferenceEntryDataSet(target.referenceEntryIds))
  }

  override fun toString(): String {
    if (timeStampsCount == 0) {
      return "HistoryChunk(time stamps: $timeStampsCount, total data series: $totalDataSeriesCount)"
    }

    return "HistoryChunk(${firstTimestamp.formatUtc()} - ${lastTimestamp.formatUtc()}, time stamps: $timeStampsCount, total data series: $totalDataSeriesCount)"
  }

  /**
   * Returns a new history chunk without any values (and time stamps) but the same data model
   */
  fun withoutValues(): HistoryChunk {
    return HistoryChunk(configuration, doubleArrayOf(), HistoryValues.empty(recordingType), recordingType)
  }

  /**
   * Creates a matrix string
   */
  fun decimalValuesAsMatrixString(): String {
    return this.values.decimalValuesAsMatrixString()
  }

  fun minValuesAsMatrixString(): String {
    return this.values.minValuesAsMatrixString()
  }

  fun maxValuesAsMatrixString(): String {
    return this.values.maxValuesAsMatrixString()
  }

  fun enumValuesAsMatrixString(): String {
    return this.values.enumValuesAsMatrixString()
  }

  fun enumMostOfTheTimeValuesAsMatrixString(): String? {
    return this.values.enumMostOfTheTimeValuesAsMatrixString()
  }

  fun referenceEntryIdsAsMatrixString(): String {
    return this.values.referenceIdsAsMatrixString()
  }

  fun referenceEntryCountsAsMatrixString(): String? {
    return this.values.referenceEntryCountsAsMatrixString()
  }

  fun referenceEntryStatusesAsMatrixString(): String? {
    return this.values.referenceEntryStatusesAsMatrixString()
  }

  /**
   * Creates a verbose ascii art.
   * Useful for debugging
   */
  fun dump(from: @ms @Inclusive Double? = null, to: @ms @Inclusive Double? = null): String {
    val indexColumnWidth = 4
    val dateColumnWidth = 24

    val indexAndDateColumnsWidth = indexColumnWidth + 1 + dateColumnWidth

    val decimalsColumnContentWidth = 6

    val enumBitSetContentWidth = 11
    val enumWinnerContentWidth = 3
    val enumColumnContentWidth = enumBitSetContentWidth + 1 + enumWinnerContentWidth

    val referenceEntryIdWidth = 5
    val referenceEntryStatusBitSetWidth = 11
    val referenceEntryCountWidth = 5
    val referenceEntryColumnContentWidth = referenceEntryIdWidth + 1 + referenceEntryCountWidth + 1 + referenceEntryStatusBitSetWidth

    //Separates decimal from enum values
    val separator = " | "

    return buildString {
      appendLine("Start: ${firstTimestamp.formatUtc()}")
      appendLine("End:   ${lastTimestamp.formatUtc()}")
      appendLine("Series counts:")
      appendLine("  Decimals: $decimalDataSeriesCount")
      appendLine("  Enums:    $enumDataSeriesCount")
      appendLine("  RefId:    $referenceEntryDataSeriesCount")
      appendLine("RecordingType:    $recordingType")
      appendLine("---------------------------------------")


      append("Indices:".padEnd(indexAndDateColumnsWidth, ' '))
      decimalDataSeriesCount.fastFor {
        append(it.toString().padStart(decimalsColumnContentWidth, ' '))
        append(" ")
      }
      append(separator)
      enumDataSeriesCount.fastFor {
        append(it.toString().padStart(enumColumnContentWidth, ' '))
        append(" ")
      }
      append(separator)
      referenceEntryDataSeriesCount.fastFor {
        append(it.toString().padStart(referenceEntryColumnContentWidth, ' '))
        append(" ")
      }

      appendLine()

      append("IDs:".padEnd(indexAndDateColumnsWidth, ' '))
      decimalDataSeriesCount.fastFor {
        append(getDecimalDataSeriesId(DecimalDataSeriesIndex(it)).toString().padStart(decimalsColumnContentWidth, ' '))
        append(" ")
      }
      append(separator)
      enumDataSeriesCount.fastFor {
        append(getEnumDataSeriesId(EnumDataSeriesIndex(it)).toString().padStart(enumColumnContentWidth, ' '))
        append(" ")
      }
      append(separator)
      referenceEntryDataSeriesCount.fastFor {
        append(getReferenceEntryDataSeriesId(ReferenceEntryDataSeriesIndex(it)).toString().padStart(referenceEntryColumnContentWidth, ' '))
        append(" ")
      }
      appendLine()
      appendLine()

      timeStamps.fastForEachIndexed { timestampIndexAsInt, timestamp ->
        val timeStampIndex = TimestampIndex(timestampIndexAsInt)

        if (from != null && timestamp < from) {
          //Skip, because smaller than from
          return@fastForEachIndexed
        }

        if (to != null && timestamp > to) {
          //Skip, because greater than to
          return@fastForEachIndexed
        }

        append(timestampIndexAsInt.toString().padStart(indexColumnWidth, ' '))
        append(" ")
        append(timestamp.formatUtc().padEnd(dateColumnWidth, ' '))

        decimalDataSeriesCount.fastFor {
          append(getDecimalValue(DecimalDataSeriesIndex(it), timeStampIndex).toString().padStart(decimalsColumnContentWidth, ' '))
          append(" ")
        }

        append(separator)

        enumDataSeriesCount.fastFor {
          val dataSeriesIndex = EnumDataSeriesIndex(it)
          @MayBeNoValueOrPending val enumValue = getEnumValue(dataSeriesIndex, timeStampIndex)

          val formattedBitSet = when {
            enumValue.isNoValue() -> "-"
            enumValue.isPending() -> "?"
            else -> enumValue.bitset.toString(2)
          }

          append(formattedBitSet.padStart(enumBitSetContentWidth, ' '))
          append(" ")

          //Add the winner
          val mostTimerWinner = getEnumOrdinalMostTime(dataSeriesIndex, timeStampIndex)

          append("(${mostTimerWinner})".padStart(enumWinnerContentWidth, ' '))
          append(" ")
        }

        append(separator)

        referenceEntryDataSeriesCount.fastFor {
          val dataSeriesIndex = ReferenceEntryDataSeriesIndex(it)
          @MayBeNoValueOrPending val referenceId = getReferenceEntryId(dataSeriesIndex, timeStampIndex)

          append(referenceId.toString().padStart(referenceEntryIdWidth, ' '))
          append(" ")

          append(getReferenceEntryStatus(dataSeriesIndex, timeStampIndex).toString().padStart(referenceEntryStatusBitSetWidth, ' '))
          append(" ")

          //Add the winner
          val idsCount = getReferenceEntryIdsCount(dataSeriesIndex, timeStampIndex)
          append("(${idsCount})".padStart(referenceEntryCountWidth, ' '))
          append(" ")
        }

        appendLine()
      }
    }
  }

  /**
   * Wraps this chunk into a bucket.
   *
   * Will throw an exception if start/end do not match the descriptor
   */
  fun toBucket(descriptor: HistoryBucketDescriptor): HistoryBucket {
    return HistoryBucket(descriptor, this)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as HistoryChunk

    if (configuration != other.configuration) return false
    if (!timeStamps.contentEquals(other.timeStamps)) return false
    if (values != other.values) return false
    return recordingType == other.recordingType
  }

  override fun hashCode(): Int {
    var result = configuration.hashCode()
    result = 31 * result + timeStamps.contentHashCode()
    result = 31 * result + values.hashCode()
    result = 31 * result + recordingType.hashCode()
    return result
  }

  companion object {
    private val logger: Logger = LoggerFactory.getLogger("com.meistercharts.history.impl.HistoryChunk")

    /**
     * This value implies that a sample has been taken but the sample does not contain a valid value for the data series.
     */
    const val NoValue: Double = Double.NaN

    /**
     * This value implies that a sample for a given timestamp is pending.
     *
     * Consequently, there are no values for data series yet, but there might be values in the future.
     *
     * @see NoValueAsInt
     */
    const val Pending: Double = Double.MAX_VALUE


    fun Double.isNoValue(): Boolean {
      return isNaN()
    }

    fun Double.isPending(): Boolean {
      return this == Pending
    }

    fun maxHistoryAware(first: Double, second: Double): Double {
      if (first.isPending()) {
        return second
      }
      if (second.isPending()) {
        return first
      }


      if (first.isNoValue()) {
        return second
      }
      if (second.isNoValue()) {
        return first
      }

      return max(first, second)
    }

    fun minHistoryAware(first: Double, second: Double): Double {
      if (first.isPending()) {
        return second
      }
      if (second.isPending()) {
        return first
      }

      if (first.isNoValue()) {
        return second
      }
      if (second.isNoValue()) {
        return first
      }

      return min(first, second)
    }
  }
}

/**
 * Creates a new  chunk for the given configuration
 */
fun HistoryConfiguration.chunk(
  /**
   * The time stamps for the history chunk. Each entry in [values] corresponds to
   * one timestamp within this array.
   */
  timeStamps: @ms DoubleArray,

  /**
   * Contains the values.
   * Contains one entry for each timestamp in [timeStamps]. Each entry contains one value for each data series id stored in the configuration
   *
   * ATTENTION: Must not be modified!
   */
  values: HistoryValues,

  recordingType: RecordingType,
): HistoryChunk {
  return HistoryChunk(this, timeStamps, values, recordingType)
}

/**
 * Returns the time range for this history chunk
 */
fun HistoryChunk.timeRange(): TimeRange {
  return TimeRange(firstTimestamp, lastTimestamp)
}


/**
 * The start time stamp
 * This is either:
 * * the exact timestamp ([RecordingType.Measured])
 * * the start of average / bit set (set)
 */
fun HistoryChunk.timestampStart(
  timeStampIndex: TimestampIndex,
  samplingPeriod: SamplingPeriod,
): @ms Double {
  return when (recordingType) {
    RecordingType.Measured -> timestampCenter(timeStampIndex)
    RecordingType.Calculated -> timestampCenter(timeStampIndex) - samplingPeriod.distance / 2.0
  }
}

/**
 * The end time stamp
 */
fun HistoryChunk.timestampEnd(
  timeStampIndex: TimestampIndex,
  samplingPeriod: SamplingPeriod,
): @ms Double {
  return when (recordingType) {
    RecordingType.Measured -> timestampCenter(timeStampIndex) + samplingPeriod.distance
    RecordingType.Calculated -> timestampCenter(timeStampIndex) + samplingPeriod.distance / 2.0
  }
}
