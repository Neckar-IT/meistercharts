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
import com.meistercharts.history.HistoryBucket
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.HistoryEnumOrdinalInt
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.HistoryEnumSetInt
import com.meistercharts.history.MayBeNoValueOrPending
import com.meistercharts.history.ReferenceEntriesDataMap
import com.meistercharts.history.ReferenceEntryData
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryId
import com.meistercharts.history.ReferenceEntryIdInt
import com.meistercharts.history.TimestampIndex
import com.meistercharts.history.annotations.ForOnePointInTime
import com.meistercharts.history.impl.HistoryChunk.Companion.isNoValue
import com.meistercharts.history.impl.HistoryChunk.Companion.isPending
import it.neckar.open.annotations.Slow
import it.neckar.open.annotations.TestOnly
import it.neckar.open.collections.DoubleArrayList
import it.neckar.open.collections.fastForEachIndexed
import it.neckar.open.collections.mapInt
import it.neckar.open.kotlin.lang.fastFor
import it.neckar.open.kotlin.lang.requireFinite
import it.neckar.open.unit.number.IsFinite
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.si.ms
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Helper class to build [HistoryChunk]s
 */
class HistoryChunkBuilder(
  /**
   * The history configuration
   */
  val historyConfiguration: HistoryConfiguration,
  /**
   * The recording type for the builder
   */
  val recordingType: RecordingType = RecordingType.Measured,
  /**
   * The expected number of entries (timestamps) - is used to initialize the arrays
   */
  expectedTimestampsCount: Int = 1000,
) {

  /**
   * Contains the time stamps
   */
  internal var timestamps: @ms DoubleArrayList = DoubleArrayList(expectedTimestampsCount)

  /**
   * Contains the history values
   */
  internal val historyValuesBuilder: HistoryValuesBuilder = HistoryValuesBuilder(
    decimalDataSeriesCount = historyConfiguration.decimalDataSeriesCount,
    enumDataSeriesCount = historyConfiguration.enumDataSeriesCount,
    referenceEntryDataSeriesCount = historyConfiguration.referenceEntryDataSeriesCount,
    initialTimestampsCount = expectedTimestampsCount,
    recordingType = recordingType
  )

  /**
   * The index where to add the next data point
   */
  internal var nextTimestampIndex = TimestampIndex(0)
    private set

  @Deprecated("Use addValues")
  fun addEnumValues(
    timestamp: @ms @IsFinite Double,
    enumValues: @HistoryEnumSetInt IntArray,
    enumOrdinalsMostTime: @HistoryEnumOrdinalInt IntArray? = null,
  ) {
    requireTimestampFinite(timestamp)
    require(enumValues.size == historyConfiguration.enumDataSeriesCount) { "Invalid enumValues size. Was <${enumValues.size}> but expected <${historyConfiguration.enumDataSeriesCount}>" }

    val currentTimestampIndex = nextTimestampIndex
    nextTimestampIndex++
    setEnumValues(currentTimestampIndex, timestamp, enumValues, enumOrdinalsMostTime)
  }

  fun addEnumValues(timestamp: @ms Double, vararg enumValues: @HistoryEnumSetInt Int) {
    addEnumValues(timestamp, enumValues, null)
  }

  @TestOnly
  @ForOnePointInTime
  fun addReferenceEntryValues(
    timestamp: @ms @IsFinite Double,
    vararg referenceEntryValues: @ReferenceEntryIdInt Int,
    referenceEntryStatuses: @HistoryEnumSetInt IntArray = IntArray(referenceEntryValues.size) { HistoryEnumSet.NoValueAsInt },
    referenceEntriesDataMap: ReferenceEntriesDataMap = ReferenceEntriesDataMap.empty,
  ) {
    addReferenceEntryValues(
      timestamp = timestamp,
      referenceEntryValues = referenceEntryValues,
      referenceEntryIdsCount = null,
      referenceEntryStatuses = referenceEntryStatuses,
      referenceEntriesDataMap = referenceEntriesDataMap
    )
  }

  @TestOnly
  @ForOnePointInTime
  fun addReferenceEntryValues(
    timestamp: @ms @IsFinite Double,
    referenceEntryValues: @ReferenceEntryIdInt IntArray,
    referenceEntryIdsCount: @ReferenceEntryIdInt IntArray? = null,
    referenceEntryStatuses: @HistoryEnumSetInt IntArray,
    referenceEntriesDataMap: ReferenceEntriesDataMap = ReferenceEntriesDataMap.empty,
  ) {
    requireTimestampFinite(timestamp)
    require(referenceEntryValues.size == historyConfiguration.referenceEntryDataSeriesCount) { "Invalid referenceEntryValues size. Was <${referenceEntryValues.size}> but expected <${historyConfiguration.referenceEntryDataSeriesCount}>" }

    val currentTimestampIndex = nextTimestampIndex
    nextTimestampIndex++
    setReferenceEntryValues(currentTimestampIndex, timestamp, referenceEntryValues, referenceEntryStatuses, referenceEntryIdsCount, referenceEntriesDataMap)
  }

  /**
   * Sets values for the given time stamp
   */
  @Slow
  @Deprecated("Use addValues")
  @TestOnly
  fun addDecimalValues(timestamp: @ms @IsFinite Double, vararg values: @Domain Double) {
    requireTimestampFinite(timestamp)
    require(values.size == historyConfiguration.decimalDataSeriesCount) { "Invalid values count. Was <${values.size}> but expected <${historyConfiguration.decimalDataSeriesCount}>" }

    val currentTimestampIndex = nextTimestampIndex
    nextTimestampIndex++
    setDecimalValues(currentTimestampIndex, timestamp, values)
  }

  @Slow
  @Deprecated("Use addValues")
  @TestOnly
  fun addDecimalValuesWithMinMax(
    timestamp: @ms @IsFinite Double,
    decimalValues: DoubleArray,
    minValues: DoubleArray?,
    maxValues: DoubleArray?,
  ) {
    requireTimestampFinite(timestamp)
    require(decimalValues.size == historyConfiguration.decimalDataSeriesCount) { "Invalid values count. Was <${decimalValues.size}> but expected <${historyConfiguration.decimalDataSeriesCount}>" }

    val currentTimestampIndex = nextTimestampIndex
    nextTimestampIndex++
    setDecimalValues(currentTimestampIndex, timestamp, decimalValues, minValues, maxValues)
  }

  @Slow
  fun addValues(
    timestamp: @ms @IsFinite Double,
    decimalValues: @Domain DoubleArray,
    decimalMinValues: @Domain DoubleArray?,
    decimalMaxValues: @Domain DoubleArray?,
    enumValues: @HistoryEnumSetInt IntArray,
    referenceEntryIds: @ReferenceEntryIdInt IntArray,
    referenceEntryStatuses: @HistoryEnumSetInt IntArray,
    entryDataSet: Set<ReferenceEntryData>,
  ) {
    requireTimestampFinite(timestamp)
    require(decimalValues.size == historyConfiguration.decimalDataSeriesCount) { "Invalid values count. Was <${decimalValues.size}> but expected <${historyConfiguration.decimalDataSeriesCount}>" }

    val currentTimestampindex = nextTimestampIndex
    nextTimestampIndex++

    setValues(
      timestampIndex = currentTimestampindex,
      timestamp = timestamp,
      decimalValues = decimalValues,
      decimalMaxValues = decimalMaxValues,
      decimalMinValues = decimalMinValues,
      enumValues = enumValues,
      referenceEntryIds = referenceEntryIds,
      referenceEntryStatuses = referenceEntryStatuses,
      entryDataSet = entryDataSet,
    )
  }

  private fun requireTimestampFinite(timestamp: @ms @IsFinite Double) {
    timestamp.requireFinite()
    require(timestamp.isFinite()) { "Timestamp must be finite but was <$timestamp>" }
  }

  /**
   * Adds the values for one timestamp index
   */
  @Slow
  fun addValues(
    timestamp: @ms @IsFinite Double,
    decimalValues: @Domain DoubleArray,

    minValues: @Domain DoubleArray? = null,
    maxValues: @Domain DoubleArray? = null,

    enumValues: @HistoryEnumSetInt IntArray,
    enumOrdinalsMostTime: @HistoryEnumOrdinalInt IntArray? = null,

    referenceEntryIds: @ReferenceEntryIdInt IntArray,
    referenceEntryStatuses: @HistoryEnumSetInt IntArray,
    referenceEntryIdsCount: @ReferenceEntryIdInt IntArray? = null,
    entryDataSet: Set<ReferenceEntryData>,
  ) {
    requireTimestampFinite(timestamp)
    require(decimalValues.size == historyConfiguration.decimalDataSeriesCount) { "Invalid values count. Was <${decimalValues.size}> but expected <${historyConfiguration.decimalDataSeriesCount}>" }

    val currentTimestampindex = nextTimestampIndex
    nextTimestampIndex++

    setValues(
      timestampIndex = currentTimestampindex,
      timestamp = timestamp,
      decimalValues = decimalValues,
      decimalMinValues = minValues,
      decimalMaxValues = maxValues,
      enumValues = enumValues,
      enumOrdinalsMostTime = enumOrdinalsMostTime,
      referenceEntryIds = referenceEntryIds,
      referenceEntryStatuses = referenceEntryStatuses,
      referenceEntryIdsCount = referenceEntryIdsCount,
      entryDataSet = entryDataSet
    )
  }

  /**
   * Adds the values for the next timestamp index
   *
   * ATTENTION: Does *only* add decimal values
   */
  @Deprecated("Use addValues instead")
  @Slow
  @TestOnly
  fun addDecimalValues(timestamp: @ms @IsFinite Double, valuesProvider: (dataSeriesIndex: DecimalDataSeriesIndex) -> @Domain Double) {
    requireTimestampFinite(timestamp)
    addDecimalValues(timestamp, *DoubleArray(historyConfiguration.decimalDataSeriesCount) { i -> valuesProvider(DecimalDataSeriesIndex(i)) })
  }

  /**
   * Adds the values for the next timestamp index.
   *
   * ATTENTION: Does *only* add enum values
   */
  @Deprecated("Use addValues instead")
  @Slow
  fun addEnumValues(timestamp: @ms @IsFinite Double, valuesProvider: (dataStructureIndex: EnumDataSeriesIndex) -> HistoryEnumSet) {
    requireTimestampFinite(timestamp)
    addEnumValues(timestamp, *IntArray(historyConfiguration.enumDataSeriesCount) { i -> valuesProvider(EnumDataSeriesIndex(i)).bitset })
  }

  @Slow
  fun addValues(
    timestamp: @ms @IsFinite Double,
    decimalValuesProvider: (dataSeriesIndex: DecimalDataSeriesIndex) -> @Domain @MayBeNoValueOrPending Double,
    decimalMinValuesProvider: ((dataSeriesIndex: DecimalDataSeriesIndex) -> @Domain @MayBeNoValueOrPending Double)? = null,
    decimalMaxValuesProvider: ((dataSeriesIndex: DecimalDataSeriesIndex) -> @Domain @MayBeNoValueOrPending Double)? = null,
    enumValuesProvider: (dataSeriesIndex: EnumDataSeriesIndex) -> HistoryEnumSet,
    referenceEntryIdProvider: (dataSeriesIndex: ReferenceEntryDataSeriesIndex) -> ReferenceEntryId,
    referenceEntryStatusProvider: (referenceEntryId: ReferenceEntryId) -> HistoryEnumSet,
    referenceEntriesDataMap: ReferenceEntriesDataMap,
  ) {
    requireTimestampFinite(timestamp)

    val referenceEntryIds: @ReferenceEntryIdInt IntArray = IntArray(historyConfiguration.referenceEntryDataSeriesCount) { i -> referenceEntryIdProvider(ReferenceEntryDataSeriesIndex(i)).id }
    val entryDataSet = referenceEntryIds.map { idAsInt: @ReferenceEntryIdInt Int -> referenceEntriesDataMap.get(ReferenceEntryId(idAsInt)) }.filterNotNull().toSet()
    val referenceEntryStatuses = referenceEntryIds.map { idAsInt: @ReferenceEntryIdInt Int -> referenceEntryStatusProvider(ReferenceEntryId(idAsInt)) }.mapInt { it.bitset }.toIntArray()

    addValues(
      timestamp = timestamp,
      decimalValues = DoubleArray(historyConfiguration.decimalDataSeriesCount) { i -> decimalValuesProvider(DecimalDataSeriesIndex(i)) },
      decimalMinValues = decimalMinValuesProvider?.toDoubleArray(historyConfiguration.decimalDataSeriesCount),
      decimalMaxValues = decimalMaxValuesProvider?.toDoubleArray(historyConfiguration.decimalDataSeriesCount),
      enumValues = IntArray(historyConfiguration.enumDataSeriesCount) { i -> enumValuesProvider(EnumDataSeriesIndex(i)).bitset },
      referenceEntryIds = referenceEntryIds,
      referenceEntryStatuses = referenceEntryStatuses,
      entryDataSet = entryDataSet,
    )
  }

  /**
   * Converts to a double array
   */
  private fun ((dataSeriesIndex: DecimalDataSeriesIndex) -> @Domain Double).toDoubleArray(count: Int): @Domain DoubleArray? {
    var hasAtLeastOneValue = false

    val values = DoubleArray(count) { index ->
      this.invoke(DecimalDataSeriesIndex(index)).also { value ->
        if (value.isFinite() && value.isPending().not() && value.isNoValue().not()) {
          hasAtLeastOneValue = true
        }
      }
    }

    if (hasAtLeastOneValue) {
      return values
    }

    //Return null if there is at least one useful value
    return null
  }

  /**
   * Sets the values for a given timestamp index
   */
  @Deprecated("use setValues instead")
  fun setDecimalValues(
    timestampIndex: TimestampIndex,
    timestamp: @ms @IsFinite Double,
    decimalValues: DoubleArray,
    minValues: DoubleArray? = null,
    maxValues: DoubleArray? = null,
  ) {
    requireTimestampFinite(timestamp)
    setTimestamp(timestampIndex, timestamp)

    //ensure the size
    if (historyValuesBuilder.timestampsCount <= timestampIndex.value) {
      //Resize the values builder
      historyValuesBuilder.resizeTimestamps(timestampIndex.value * 2)
    }

    historyValuesBuilder.setDecimalValuesForTimestamp(timestampIndex, decimalValues, minValues, maxValues)
  }

  @Deprecated("use setValues instead")
  fun setEnumValues(
    timestampIndex: TimestampIndex,
    timestamp: @ms @IsFinite Double,
    enumValues: @HistoryEnumSetInt IntArray,
    enumOrdinalsMostTime: @HistoryEnumOrdinalInt IntArray? = null,
  ) {
    setTimestamp(timestampIndex, timestamp)

    //ensure the size
    if (historyValuesBuilder.timestampsCount <= timestampIndex.value) {
      //Resize the values builder
      historyValuesBuilder.resizeTimestamps(timestampIndex.value * 2)
    }

    historyValuesBuilder.setEnumValuesForTimestamp(timestampIndex, enumValues, enumOrdinalsMostTime)
  }

  @TestOnly
  @Deprecated("use setValues instead")
  @ForOnePointInTime
  fun setReferenceEntryValues(
    timestampIndex: TimestampIndex,
    timestamp: @ms @IsFinite Double,
    referenceEntryIds: @ReferenceEntryIdInt IntArray,
    referenceEntryStatuses: @HistoryEnumSetInt IntArray,
    referenceEntryIdsCount: @ReferenceEntryIdInt IntArray? = null,
    /**
     * The map is used to resolve the [ReferenceEntryData]s
     */
    referenceEntriesDataMap: ReferenceEntriesDataMap = ReferenceEntriesDataMap.empty,
  ) {
    setTimestamp(timestampIndex, timestamp)

    //ensure the size
    if (historyValuesBuilder.timestampsCount <= timestampIndex.value) {
      //Resize the values builder
      historyValuesBuilder.resizeTimestamps(timestampIndex.value * 2)
    }

    historyValuesBuilder.setReferenceEntryIdsForTimestamp(
      timestampIndex = timestampIndex,
      referenceEntryIds = referenceEntryIds,
      referenceEntryIdsCount = referenceEntryIdsCount,
      referenceEntryStatuses = referenceEntryStatuses,
      referenceEntryDataSet = referenceEntriesDataMap.getAll(referenceEntryIds)
    )
  }

  /**
   * Sets the values for one timestamp index
   */
  fun setValues(
    timestampIndex: TimestampIndex,
    timestamp: @ms @IsFinite Double,
    decimalValues: @Domain DoubleArray,

    decimalMinValues: @Domain DoubleArray? = null,
    decimalMaxValues: @Domain DoubleArray? = null,

    enumValues: @HistoryEnumSetInt IntArray,
    enumOrdinalsMostTime: @HistoryEnumOrdinalInt IntArray? = null,

    referenceEntryIds: @ReferenceEntryIdInt IntArray,
    referenceEntryStatuses: @HistoryEnumSetInt IntArray,
    referenceEntryIdsCount: @ReferenceEntryIdInt IntArray? = null,
    entryDataSet: Set<ReferenceEntryData>,
  ) {
    requireTimestampFinite(timestamp)

    //ensure the size
    if (historyValuesBuilder.timestampsCount <= timestampIndex.value) {
      //Resize the values builder
      historyValuesBuilder.resizeTimestamps(timestampIndex.value * 2)
    }

    setTimestamp(timestampIndex, timestamp)

    historyValuesBuilder.setDecimalValuesForTimestamp(timestampIndex, decimalValues, decimalMinValues, decimalMaxValues)
    historyValuesBuilder.setEnumValuesForTimestamp(timestampIndex, enumValues, enumOrdinalsMostTime)
    historyValuesBuilder.setReferenceEntryIdsForTimestamp(timestampIndex, referenceEntryIds, referenceEntryIdsCount, referenceEntryStatuses, entryDataSet)
  }

  /**
   * Sets the timestamp.
   * Does *not* update the [timestampIndex]
   */
  fun setTimestamp(timestampIndex: TimestampIndex, timestamp: @ms @IsFinite Double) {
    requireTimestampFinite(timestamp)

    require(nextTimestampIndex > timestampIndex) {
      "Invalid timestamp index $timestampIndex. Expected to be smaller than $nextTimestampIndex. Resize first!"
    }

    timestamps.setTimestamp(timestampIndex, timestamp)
  }

  /**
   * Sets a single [decimalValue] (the int value representation for the real value) for the data series at index [dataSeriesIndex] for the timestamp at index [timestampIndex]
   * @see setDecimalValue
   */
  private fun setDecimalValue(dataSeriesIndex: DecimalDataSeriesIndex, timestampIndex: TimestampIndex, decimalValue: @Domain Double) {
    historyValuesBuilder.setDecimalValue(dataSeriesIndex, timestampIndex, decimalValue)
  }

  fun build(): HistoryChunk {
    //Resize to the final size
    historyValuesBuilder.resizeTimestamps(nextTimestampIndex.value)

    return HistoryChunk(historyConfiguration, timestamps.toDoubleArray(), historyValuesBuilder.build(), recordingType)
  }

  /**
   * Returns the last reference entry value for the provided ID
   */
  fun getReferenceEntryId(dataSeriesIndex: ReferenceEntryDataSeriesIndex, timestampIndex: TimestampIndex): ReferenceEntryId {
    return historyValuesBuilder.getReferenceEntryId(dataSeriesIndex, timestampIndex)
  }

  fun getReferenceEntryStatus(dataSeriesIndex: ReferenceEntryDataSeriesIndex, timestampIndex: TimestampIndex): HistoryEnumSet {
    return historyValuesBuilder.getReferenceEntryStatus(dataSeriesIndex, timestampIndex)
  }

  fun getReferenceEntryData(id: ReferenceEntryId): ReferenceEntryData? {
    return historyValuesBuilder.getReferenceEntryData(id)
  }
}

/**
 * Creates a bucket
 */
fun HistoryConfiguration.bucket(descriptor: HistoryBucketDescriptor, valueProvider: (dataSeriesIndex: DecimalDataSeriesIndex, timestamp: @ms @IsFinite Double) -> Double): HistoryBucket {
  @ms val distanceBetweenTimestamps = descriptor.bucketRange.distance

  val decimalDataSeriesCount = this.decimalDataSeriesCount

  val chunk = chunk {
    @ms var currentTimestamp = descriptor.start
    while (currentTimestamp < descriptor.end) {

      val values = DoubleArray(decimalDataSeriesCount) {
        valueProvider(DecimalDataSeriesIndex(it), currentTimestamp)
      }

      this.addDecimalValues(currentTimestamp, *values)
      currentTimestamp += distanceBetweenTimestamps
    }
  }

  return HistoryBucket(descriptor, chunk)
}

/**
 * Creates a history chunk
 */
fun historyChunk(
  /**
   * The history configuration
   */
  historyConfiguration: HistoryConfiguration,

  /**
   * The recording type
   */
  recordingType: RecordingType = RecordingType.Measured,

  /**
   * The lambda to configure the chunk
   */
  config: HistoryChunkBuilder.() -> Unit,
): HistoryChunk {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  return HistoryChunkBuilder(historyConfiguration, recordingType)
    .also(config)
    .build()
}

/**
 * Creates a new history chunk for this configuration
 */
fun HistoryConfiguration.chunk(
  recordingType: RecordingType = RecordingType.Measured,
  /**
   * The lambda to configure the chunk
   */
  config: HistoryChunkBuilder.() -> Unit,
): HistoryChunk {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  return historyChunk(this, recordingType, config = config)
}

/**
 * Creates a new chunk for this configuration.
 * Calls the given lambda for each time stamp index
 *
 * ATTENTION: It is necessary to call [HistoryChunkBuilder.addDecimalValues] in the given [initializer]
 */
fun HistoryConfiguration.chunk(
  numberOfTimestamps: Int,
  /**
   * The lambda to add the data for one time stamp index
   */
  initializer: HistoryChunkBuilder.(timestampIndex: TimestampIndex) -> Unit,
): HistoryChunk {
  contract {
    callsInPlace(initializer, InvocationKind.UNKNOWN)
  }

  return historyChunk(this) {
    numberOfTimestamps.fastFor { timestampIndex ->
      initializer(TimestampIndex(timestampIndex))
    }
  }
}

/**
 * Creates a new chunk from the given decimals arrays.
 *
 * Each array contains the values for one data series
 *
 * ATTENTION: Does not support
 */
@TestOnly
@Slow
fun HistoryConfiguration.chunk(
  timeStamps: DoubleArray,
  /**
   * The values for each time stamp.
   * The first array contains the values for the *first* data series (for all time stamps)
   */
  vararg decimalValues: DoubleArray,
): HistoryChunk {

  val numberOfDecimalDataSeries = decimalValues.size
  require(numberOfDecimalDataSeries == this.decimalDataSeriesCount) {
    "Invalid array size. Expected <${this.decimalDataSeriesCount}> but was <$numberOfDecimalDataSeries>"
  }

  //Verify that each entry has the exact size
  decimalValues.fastForEachIndexed { index, array ->
    require(timeStamps.size == array.size) {
      "Invalid size for decimal values[$index]. Was <${array.size}> but expected <${timeStamps.size}>"
    }
  }

  return chunk(timeStamps.size) { timestampIndex ->
    val timestamp = timeStamps.getTimestamp(timestampIndex)

    val valuesForTimeStamp: @Domain DoubleArray = DoubleArray(numberOfDecimalDataSeries) {
      val arrayForDataSeries = decimalValues[it]
      arrayForDataSeries.getTimestamp(timestampIndex)
    }

    addDecimalValues(timestamp, *valuesForTimeStamp)
  }
}
