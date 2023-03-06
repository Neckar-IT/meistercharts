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
import com.meistercharts.history.TimestampIndex
import it.neckar.open.collections.DoubleArray2
import it.neckar.open.kotlin.serializers.DoubleArray2Serializer
import kotlinx.serialization.Serializable

/**
 * Wrapper class for decimal history values
 */
@Serializable
data class DecimalHistoryValues(
  /**
   * The values - measured values or mean values when down sampled
   *
   * Each row contains the significant values for one data series for each time stamp.
   * The rows represent the data series, the height the values for the different time stamps
   *
   * * `values.width == decimalDataSeriesCount`
   * * `values.height == timeStampsCount`
   */
  val values: @Serializable(with = DoubleArray2Serializer::class) @Domain DoubleArray2,

  /**
   * The min values - not set for measured values
   */
  val minValues: @Serializable(with = DoubleArray2Serializer::class) @Domain DoubleArray2? = null,
  /**
   * The max values - not set for measured values
   */
  val maxValues: @Serializable(with = DoubleArray2Serializer::class) @Domain DoubleArray2? = null,
) : HistoryValuesAspect {

  init {
    if (minValues != null) {
      requireNotNull(maxValues)
    }

    if (minValues != null || maxValues != null) {
      require(minValues != null) { "minValues must not be null if maxValues have been set" }
      require(maxValues != null) { "maxValues must not be null if minValues have been set" }

      require(values.width == minValues.width) {
        "Invalid min values size. Was <${minValues.width}> but expected <${values.width}>"
      }
      require(values.height == minValues.height) {
        "Invalid min values size. Was <${minValues.height}> but expected <${values.height}>"
      }

      require(values.width == maxValues.width) {
        "Invalid min values size. Was <${maxValues.width}> but expected <${values.width}>"
      }
      require(values.height == maxValues.height) {
        "Invalid max values size. Was <${maxValues.height}> but expected <${values.height}>"
      }
    }
  }

  /**
   * Returns true if there exist min and max values
   */
  val hasMinMax: Boolean
    get() {
      return minValues != null && maxValues != null
    }

  override val recordingType: RecordingType
    get() {
      return if (hasMinMax) RecordingType.Calculated else RecordingType.Measured
    }

  /**
   * The amount of data series with decimal values
   */
  override val dataSeriesCount: Int
    get() {
      return values.width
    }

  override val timeStampsCount: Int
    get() {
      return values.height
    }

  override val isEmpty: Boolean
    get() {
      return values.isEmpty
    }

  /**
   * Returns the value at the given position.
   * ATTENTION: Might return [HistoryChunk.NoValue] or [HistoryChunk.Pending]
   */
  fun getDecimalValue(dataSeriesIndex: DecimalDataSeriesIndex, timeStampIndex: TimestampIndex): @Domain Double {
    require(dataSeriesIndex.value < values.width) { "Invalid data series index <$dataSeriesIndex>. decimalValues count: ${values.width}" }

    return values[dataSeriesIndex.value, timeStampIndex.value]
  }

  /**
   * Returns the *last* (youngest) value for the given data series index
   */
  fun getLastDecimalValue(dataSeriesIndex: DecimalDataSeriesIndex): @Domain Double? {
    if (timeStampsCount == 0) {
      return null
    }

    require(dataSeriesIndex.value < values.width) { "Invalid data series index <$dataSeriesIndex>. significantValues size: ${values.width}" }
    return values[dataSeriesIndex.value, timeStampsCount - 1]
  }

  /**
   * Returns the significands for the given timestamp (instantiates a new array)
   */
  fun getDecimalValues(timeStampIndex: TimestampIndex): @Domain DoubleArray {
    require(timeStampIndex < values.height) { "Invalid time stamp index <$timeStampIndex>. Time stamp count <${values.height}>" }

    val startIndex = HistoryValues.calculateStartIndex(dataSeriesCount, timeStampIndex)
    val endIndex = startIndex + dataSeriesCount

    return values.data.copyOfRange(startIndex, endIndex)
  }

  /**
   * Returns the min value - falls back to the value if no max value exists
   */
  fun getMin(dataSeriesIndex: DecimalDataSeriesIndex, timeStampIndex: TimestampIndex): @Domain Double {
    if (minValues == null) {
      return getDecimalValue(dataSeriesIndex, timeStampIndex)
    }

    return minValues.getDecimalValue(dataSeriesIndex, timeStampIndex)
  }

  /**
   * Returns the max value - falls back to the value if no max value exists
   */
  fun getMax(dataSeriesIndex: DecimalDataSeriesIndex, timeStampIndex: TimestampIndex): @Domain Double {
    if (maxValues == null) {
      return getDecimalValue(dataSeriesIndex, timeStampIndex)
    }

    return maxValues.getDecimalValue(dataSeriesIndex, timeStampIndex)
  }

  /**
   * Formats the history values as matrix string (human-readable)
   */
  fun decimalValuesAsMatrixString(): String {
    return values.asMatrixString()
  }

  fun minValuesAsMatrixString(): String {
    return minValues.asMatrixString()
  }

  fun maxValuesAsMatrixString(): String {
    return maxValues.asMatrixString()
  }

}
