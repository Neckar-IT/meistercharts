package com.meistercharts.history.impl

import it.neckar.open.annotations.Slow
import com.meistercharts.annotations.Domain
import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.EnumDataSeriesIndex
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.HistoryEnumOrdinalInt
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.HistoryEnumSetInt
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryId
import com.meistercharts.history.ReferenceEntryIdInt
import com.meistercharts.history.TimestampIndex
import it.neckar.open.collections.DoubleArrayList
import it.neckar.open.collections.fastForEachIndexed
import it.neckar.open.kotlin.lang.fastFor
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
  private var timestamps: @ms DoubleArrayList = DoubleArrayList(expectedTimestampsCount)

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
  private var nextTimestampIndex = TimestampIndex(0)

  @Deprecated("Use addValues")
  fun addEnumValues(
    timestamp: @ms Double,
    enumValues: @HistoryEnumSetInt IntArray,
    enumOrdinalsMostTime: @HistoryEnumOrdinalInt IntArray? = null,
  ) {
    require(enumValues.size == historyConfiguration.enumDataSeriesCount) { "Invalid enumValues size. Was <${enumValues.size}> but expected <${historyConfiguration.enumDataSeriesCount}>" }

    setEnumValues(nextTimestampIndex, timestamp, enumValues, enumOrdinalsMostTime)
    nextTimestampIndex++
  }

  fun addEnumValues(timestamp: @ms Double, vararg enumValues: @HistoryEnumSetInt Int) {
    addEnumValues(timestamp, enumValues, null)
  }

  @Deprecated("Use addValues")
  fun addReferenceEntryValues(timestamp: @ms Double, vararg referenceEntryValues: @ReferenceEntryIdInt Int) {
    addReferenceEntryValues(timestamp, referenceEntryValues, null)
  }

  @Deprecated("Use addValues")
  fun addReferenceEntryValues(timestamp: @ms Double, referenceEntryValues: @ReferenceEntryIdInt IntArray, referenceEntryIdsCount: @ReferenceEntryIdInt IntArray? = null) {
    require(referenceEntryValues.size == historyConfiguration.referenceEntryDataSeriesCount) { "Invalid referenceEntryValues size. Was <${referenceEntryValues.size}> but expected <${historyConfiguration.referenceEntryDataSeriesCount}>" }

    setReferenceEntryValues(nextTimestampIndex, timestamp, referenceEntryValues, referenceEntryIdsCount)
    nextTimestampIndex++
  }

  /**
   * Sets values for the given time stamp
   */
  @Slow
  @Deprecated("Use addValues")
  fun addDecimalValues(timestamp: @ms Double, vararg values: @Domain Double) {
    require(values.size == historyConfiguration.decimalDataSeriesCount) { "Invalid values count. Was <${values.size}> but expected <${historyConfiguration.decimalDataSeriesCount}>" }

    setDecimalValues(nextTimestampIndex, timestamp, values)
    nextTimestampIndex++
  }

  @Slow
  fun addValues(
    timestamp: @ms Double,
    decimalValues: @Domain DoubleArray,
    enumValues: @HistoryEnumSetInt IntArray,
    referenceEntryIds: @ReferenceEntryIdInt IntArray,
  ) {
    require(decimalValues.size == historyConfiguration.decimalDataSeriesCount) { "Invalid values count. Was <${decimalValues.size}> but expected <${historyConfiguration.decimalDataSeriesCount}>" }

    setValues(nextTimestampIndex, timestamp, decimalValues = decimalValues, enumValues = enumValues, referenceEntryIds = referenceEntryIds)
    nextTimestampIndex++
  }

  /**
   * Adds the values for the next timestamp index
   *
   * ATTENTION: Does *only* add decimal values
   */
  @Deprecated("Use addValues instead")
  @Slow
  fun addDecimalValues(timestamp: @ms Double, valuesProvider: (dataSeriesIndex: DecimalDataSeriesIndex) -> @Domain Double) {
    addDecimalValues(timestamp, *DoubleArray(historyConfiguration.decimalDataSeriesCount) { i -> valuesProvider(DecimalDataSeriesIndex(i)) })
  }

  /**
   * Adds the values for the next timestamp index.
   *
   * ATTENTION: Does *only* add enum values
   */
  @Deprecated("Use addValues instead")
  @Slow
  fun addEnumValues(timestamp: @ms Double, valuesProvider: (dataStructureIndex: EnumDataSeriesIndex) -> HistoryEnumSet) {
    addEnumValues(timestamp, *IntArray(historyConfiguration.enumDataSeriesCount) { i -> valuesProvider(EnumDataSeriesIndex(i)).bitset })
  }

  /**
   * Adds the values for the next timestamp index.
   *
   * ATTENTION: Does *only* add referenceEntry values
   */
  @Deprecated("Use addValues instead")
  @Slow
  fun addReferenceEntryValues(timestamp: @ms Double, valuesProvider: (dataStructureIndex: ReferenceEntryDataSeriesIndex) -> ReferenceEntryId) {
    addReferenceEntryValues(timestamp, *IntArray(historyConfiguration.referenceEntryDataSeriesCount) { i ->
      valuesProvider(ReferenceEntryDataSeriesIndex(i)).id
    })
  }


  @Slow
  fun addValues(
    timestamp: @ms Double,
    decimalValuesProvider: (dataSeriesIndex: DecimalDataSeriesIndex) -> @Domain Double,
    enumValuesProvider: (dataSeriesIndex: EnumDataSeriesIndex) -> HistoryEnumSet,
    referenceEntryIdProvider: (dataSeriesIndex: ReferenceEntryDataSeriesIndex) -> ReferenceEntryId,
  ) {
    addValues(
      timestamp,
      DoubleArray(historyConfiguration.decimalDataSeriesCount) { i -> decimalValuesProvider(DecimalDataSeriesIndex(i)) },
      IntArray(historyConfiguration.enumDataSeriesCount) { i -> enumValuesProvider(EnumDataSeriesIndex(i)).bitset },
      IntArray(historyConfiguration.referenceEntryDataSeriesCount) { i -> referenceEntryIdProvider(ReferenceEntryDataSeriesIndex(i)).id },
    )
  }

  /**
   * Sets the values for a given timestamp index
   */
  @Deprecated("use setValues instead")
  fun setDecimalValues(
    timestampIndex: TimestampIndex,
    timestamp: @ms Double,
    decimalValues: DoubleArray,
    minValues: DoubleArray? = null,
    maxValues: DoubleArray? = null,
  ) {
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
    timestamp: @ms Double,
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

  @Deprecated("use setValues instead")
  fun setReferenceEntryValues(
    timestampIndex: TimestampIndex,
    timestamp: @ms Double,
    referenceEntryValues: @ReferenceEntryIdInt IntArray,
    referenceEntryIdsCount: @ReferenceEntryIdInt IntArray? = null,
  ) {
    setTimestamp(timestampIndex, timestamp)

    //ensure the size
    if (historyValuesBuilder.timestampsCount <= timestampIndex.value) {
      //Resize the values builder
      historyValuesBuilder.resizeTimestamps(timestampIndex.value * 2)
    }

    historyValuesBuilder.setReferenceEntryIdsForTimestamp(timestampIndex, referenceEntryValues, referenceEntryIdsCount)
  }

  /**
   * Sets the values
   */
  fun setValues(
    timestampIndex: TimestampIndex,
    timestamp: @ms Double,
    decimalValues: @Domain DoubleArray,

    minValues: @Domain DoubleArray? = null,
    maxValues: @Domain DoubleArray? = null,

    enumValues: @HistoryEnumSetInt IntArray,
    enumOrdinalsMostTime: @HistoryEnumOrdinalInt IntArray? = null,

    referenceEntryIds: @ReferenceEntryIdInt IntArray,
    referenceEntryIdsCount: @ReferenceEntryIdInt IntArray? = null,
  ) {
    //ensure the size
    if (historyValuesBuilder.timestampsCount <= timestampIndex.value) {
      //Resize the values builder
      historyValuesBuilder.resizeTimestamps(timestampIndex.value * 2)
    }

    setTimestamp(timestampIndex, timestamp)

    historyValuesBuilder.setDecimalValuesForTimestamp(timestampIndex, decimalValues, minValues, maxValues)
    historyValuesBuilder.setEnumValuesForTimestamp(timestampIndex, enumValues, enumOrdinalsMostTime)
    historyValuesBuilder.setReferenceEntryIdsForTimestamp(timestampIndex, referenceEntryIds, referenceEntryIdsCount)
  }

  /**
   * Sets the timestamp
   */
  private fun setTimestamp(timestampIndex: TimestampIndex, timestamp: @ms Double) {
    timestamp.requireIsFinite { "timestamp for <$timestampIndex>" }

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

  return HistoryChunkBuilder(historyConfiguration, recordingType).also(config).build()
}

/**
 * Creates a new history chunk for this configuration
 */
fun HistoryConfiguration.chunk(
  /**
   * The lambda to configure the chunk
   */
  config: HistoryChunkBuilder.() -> Unit,
): HistoryChunk {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  return historyChunk(this, config = config)
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


/**
 * Throws an exception if the given value is not a valid number
 */
fun Double.requireIsFinite(descriptionProvider: () -> String) {
  require(isFinite()) {
    "<${descriptionProvider()}> is not finite but $this"
  }
}
