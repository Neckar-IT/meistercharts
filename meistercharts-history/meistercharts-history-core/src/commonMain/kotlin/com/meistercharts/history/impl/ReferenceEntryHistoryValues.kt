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

import it.neckar.open.collections.IntArray2
import it.neckar.open.kotlin.serializers.IntArray2Serializer
import com.meistercharts.history.MayBeNoValueOrPending
import com.meistercharts.history.ReferenceEntriesDataMap
import com.meistercharts.history.ReferenceEntryData
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryDifferentIdsCount
import com.meistercharts.history.ReferenceEntryDifferentIdsCountInt
import com.meistercharts.history.ReferenceEntryId
import com.meistercharts.history.ReferenceEntryIdInt
import com.meistercharts.history.TimestampIndex
import kotlinx.serialization.Serializable

/**
 * Contains the object values.
 *
 * Each entry contains a [ReferenceEntryId] which can be resolved to a source object.
 *
 *
 * ## Down sampling strategy
 * When calculating the down sampled values, it is necessary to reduce the amount of information.
 * Since the [ReferenceEntryId]s might all be different, it is not possible to keep a set of *all* [ReferenceEntryId]s.
 *
 * Therefore, we keep *one* [ReferenceEntryId] (e.g. the dominant one) and the total number of different values.
 */
@Serializable
data class ReferenceEntryHistoryValues(
  /**
   * Contains the object value IDs - for each timestamp
   *
   * Each row contains the values for one data series for each time stamp.
   * The rows represent the data series, the height the values for the different time stamps
   *
   * * `values.width == referenceEntrySeriesCount`
   * * `values.height == timeStampsCount`
   *
   *
   * For down sampled values this contains one entry (most-of-the-time)
   */
  val values: @ReferenceEntryIdInt @Serializable(with = IntArray2Serializer::class) IntArray2,
  /**
   * The count of different ids for each timestamp.
   * Only filled for down sampled values.
   *
   * Pending is represented by: [com.meistercharts.history.ReferenceEntryDifferentIdsCount.PendingAsInt]
   * NoValue is represented by: [com.meistercharts.history.ReferenceEntryDifferentIdsCount.NoValue]
   */
  val differentIdsCount: @ReferenceEntryDifferentIdsCountInt @Serializable(with = IntArray2Serializer::class) IntArray2?,

  /**
   * Contains the data maps for the reference entries.
   * The index corresponds to the [ReferenceEntryDataSeriesIndex]
   */
  private val dataMaps: List<ReferenceEntriesDataMap>,

  ) : HistoryValuesAspect {

  init {
    require(dataMaps.size == dataSeriesCount) {
      "Expected referenceEntriesDataMaps.size to be $dataSeriesCount but was ${dataMaps.size}"
    }
  }

  override val recordingType: RecordingType
    get() {
      return if (hasIdsCount) RecordingType.Calculated else RecordingType.Measured
    }

  /**
   * Returns the number of timestamps
   */
  override val timeStampsCount: Int
    get() {
      return values.height
    }

  /**
   * The number of data series
   */
  override val dataSeriesCount: Int
    get() {
      return values.width
    }

  override val isEmpty: Boolean
    get() {
      return values.isEmpty
    }

  /**
   * Returns true if there exist counts for the object values (the values have been down sampled)
   */
  val hasIdsCount: Boolean
    get() {
      return differentIdsCount != null
    }

  /**
   * Returns the reference entry id (measured or most-of-the-time)
   */
  fun getReferenceEntryId(dataSeriesIndex: ReferenceEntryDataSeriesIndex, timeStampIndex: TimestampIndex): @MayBeNoValueOrPending ReferenceEntryId {
    require(dataSeriesIndex.value < values.width) { "Invalid data series index <$dataSeriesIndex>. reference entries count: ${values.width}" }

    return values.getReferenceEntry(dataSeriesIndex, timeStampIndex)
  }

  /**
   * Returns the entries map for the provided data series index
   */
  fun getDataMap(dataSeriesIndex: ReferenceEntryDataSeriesIndex): ReferenceEntriesDataMap {
    return dataMaps[dataSeriesIndex.value]
  }

  /**
   * Returns the data for the given series index and id
   */
  fun getData(dataSeriesIndex: ReferenceEntryDataSeriesIndex, id: ReferenceEntryId): ReferenceEntryData? {
    return getDataMap(dataSeriesIndex).get(id)
  }

  /**
   * Returns the number of different IDs.
   * This method returns 1 when measured and a value is set.
   *
   * Will return [ReferenceEntryDifferentIdsCount.NoValue] if the measured value is [ReferenceEntryId.NoValue]
   */
  fun getDifferentIdsCount(dataSeriesIndex: ReferenceEntryDataSeriesIndex, timeStampIndex: TimestampIndex): @MayBeNoValueOrPending ReferenceEntryDifferentIdsCount {
    require(dataSeriesIndex.value < values.width) { "Invalid data series index <$dataSeriesIndex>. reference entries count: ${values.width}" }

    if (differentIdsCount == null) {
      //Measured!
      val referenceEntryId = getReferenceEntryId(dataSeriesIndex, timeStampIndex)

      if (referenceEntryId.isNoValue()) {
        return ReferenceEntryDifferentIdsCount.NoValue
      }
      if (referenceEntryId.isPending()) {
        return ReferenceEntryDifferentIdsCount.Pending
      }

      return ReferenceEntryDifferentIdsCount.one
    }

    return differentIdsCount.getReferenceEntryDifferentIdsCount(dataSeriesIndex, timeStampIndex)
  }

  /**
   * Returns the last object value
   * Returns null if this is empty.
   */
  fun getLastReferenceEntryId(dataSeriesIndex: ReferenceEntryDataSeriesIndex): ReferenceEntryId? {
    if (timeStampsCount == 0) {
      return null
    }

    require(dataSeriesIndex.value < values.width) { "Invalid data series index <$dataSeriesIndex>. referenceEntryIds size: ${values.width}" }
    return values.getReferenceEntry(dataSeriesIndex, TimestampIndex(timeStampsCount - 1))
  }

  fun referenceEntryIdsAsMatrixString(): String {
    return values.asEnumsMatrixString()
  }

  /**
   * Returns the reference entry ids for the given [timeStampIndex]
   */
  fun getReferenceEntryIds(timeStampIndex: TimestampIndex): @ReferenceEntryIdInt IntArray {
    require(timeStampIndex < values.height) { "Invalid time stamp index <$timeStampIndex>. Time stamp count <${values.height}>" }

    val startIndex = HistoryValues.calculateStartIndex(dataSeriesCount, timeStampIndex)
    val endIndex = startIndex + dataSeriesCount

    return values.data.copyOfRange(startIndex, endIndex)
  }

  /**
   * Returns the different ids counts for the given [timeStampIndex]
   */
  fun getDifferentIdsCounts(timeStampIndex: TimestampIndex): @ReferenceEntryIdInt IntArray? {
    if (differentIdsCount == null) {
      return null
    }

    require(timeStampIndex < differentIdsCount.height) { "Invalid time stamp index <$timeStampIndex>. Time stamp count <${values.height}>" }

    val startIndex = HistoryValues.calculateStartIndex(dataSeriesCount, timeStampIndex)
    val endIndex = startIndex + dataSeriesCount

    return differentIdsCount.data.copyOfRange(startIndex, endIndex)
  }

  fun idsAsMatrixString(): String {
    return values.asMatrixString()
  }

  fun countsAsMatrixString(): String? {
    return differentIdsCount?.asMatrixString()
  }
}

