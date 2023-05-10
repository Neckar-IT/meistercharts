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

import com.meistercharts.history.EnumDataSeriesIndex
import com.meistercharts.history.HistoryDebug
import com.meistercharts.history.HistoryEnumOrdinal
import com.meistercharts.history.HistoryEnumOrdinalInt
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.HistoryEnumSetInt
import com.meistercharts.history.MayBeNoValueOrPending
import com.meistercharts.history.TimestampIndex
import it.neckar.open.collections.IntArray2
import it.neckar.open.kotlin.serializers.IntArray2Serializer
import kotlinx.serialization.Serializable

/**
 * Contains the values for enums - this class is used in [HistoryValues].
 * It should never be used directly.
 */
@Serializable
data class EnumHistoryValues(
  /**
   * The values - measured values or combined values when down sampled
   *
   * Each row contains the values for one data series for each time stamp.
   * The rows represent the data series, the height the values for the different time stamps
   *
   * * `values.width == enumDataSeriesCount`
   * * `values.height == timeStampsCount`
   *
   * Contains the union set for down-sampled values.
   */
  val values: @HistoryEnumSetInt @Serializable(with = IntArray2Serializer::class) IntArray2,

  /**
   * The values that have been measured most of the time - not set for measured values
   */
  val mostOfTheTimeValues: @MayBeNoValueOrPending @HistoryEnumOrdinalInt @Serializable(with = IntArray2Serializer::class) IntArray2? = null,

  ) : HistoryValuesAspect {

  init {
    //Verify mostOfTheTimeValues plausibility - Slow, just enable if necessary
    if (HistoryDebug.additionalVerificationEnabled) {
      mostOfTheTimeValues?.data?.forEachIndexed { index, mostOfTheTimeValueAsInt ->
        val mostOfTheTimeValue = HistoryEnumOrdinal(mostOfTheTimeValueAsInt)
        val historyEnumSetAsInt = values.data[index]
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

  override val recordingType: RecordingType
    get() {
      return if (hasMostOfTheTimeValues) RecordingType.Calculated else RecordingType.Measured
    }

  /**
   * Returns the number of timestamps
   */
  override val timeStampsCount: Int
    get() {
      return values.height
    }

  override val dataSeriesCount: Int
    get() {
      return values.width
    }

  override val isEmpty: Boolean
    get() {
      return values.isEmpty
    }

  val hasMostOfTheTimeValues: Boolean
    get() {
      return mostOfTheTimeValues != null
    }


  fun getEnumValue(dataSeriesIndex: EnumDataSeriesIndex, timeStampIndex: TimestampIndex): HistoryEnumSet {
    require(dataSeriesIndex.value < values.width) { "Invalid data series index <$dataSeriesIndex>. enum values count: ${values.width}" }

    return values.getEnumSet(dataSeriesIndex, timeStampIndex)
  }

  fun getLastEnumValue(dataSeriesIndex: EnumDataSeriesIndex): HistoryEnumSet? {
    if (timeStampsCount == 0) {
      return null
    }

    require(dataSeriesIndex.value < values.width) { "Invalid data series index <$dataSeriesIndex>. enumValues size: ${values.width}" }
    return HistoryEnumSet(values[dataSeriesIndex.value, timeStampsCount - 1])
  }

  fun getEnumValues(timeStampIndex: TimestampIndex): @HistoryEnumSetInt IntArray {
    require(timeStampIndex < values.height) { "Invalid time stamp index <$timeStampIndex>. Time stamp count <${values.height}>" }

    val startIndex = HistoryValues.calculateStartIndex(dataSeriesCount, timeStampIndex)
    val endIndex = startIndex + dataSeriesCount

    return values.data.copyOfRange(startIndex, endIndex)
  }

  /**
   * Returns the enum value that has been measured most of the time - falls back to the measured value
   */
  fun getEnumOrdinalMostTime(dataSeriesIndex: EnumDataSeriesIndex, timeStampIndex: TimestampIndex): @MayBeNoValueOrPending HistoryEnumOrdinal {
    if (mostOfTheTimeValues == null) {
      val enumValue = getEnumValue(dataSeriesIndex, timeStampIndex)
      return enumValue.firstSetOrdinal()
    }

    return mostOfTheTimeValues.getEnumOrdinal(dataSeriesIndex, timeStampIndex)
  }

  fun valuesAsMatrixString(): String {
    return values.asEnumsMatrixString()
  }

  fun mostOfTheTimeValuesAsMatrixString(): String? {
    return mostOfTheTimeValues?.asEnumsMatrixString()
  }
}
