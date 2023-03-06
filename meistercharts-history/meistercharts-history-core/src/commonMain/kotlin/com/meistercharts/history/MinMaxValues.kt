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
package com.meistercharts.history

import com.meistercharts.annotations.Domain
import it.neckar.open.collections.fastForEachIndexed
import com.meistercharts.history.impl.HistoryChunk

/**
 * Contains the min/max values for a given list of data series indices
 */
class MinMaxValues(
  val dataSeriesIndices: List<DecimalDataSeriesIndex>,
  val minValues: @Domain DoubleArray,
  val maxValues: @Domain DoubleArray,
) {

  init {
    require(minValues.size == maxValues.size) {
      "Invalid sizes. min values: ${minValues.size}; max values: ${maxValues.size}"
    }

    minValues.fastForEachIndexed { index, minValue ->
      val maxValue = maxValues[index]

      if (minValue.isNoValue().not()) {
        require(maxValue.isNoValue().not()) {
          "Max value required if there is a min value"
        }

        require(minValue <= maxValue) {
          "Min value $minValue must be <= max value $maxValue at index $index"
        }
      }
    }
  }

  fun min(dataSeriesIndex: DecimalDataSeriesIndex): @Domain Double? {
    val value = minValues[dataSeriesIndices.indexOf(dataSeriesIndex)]
    if (value.isNoValue()) {
      return null
    }
    return value
  }

  fun max(dataSeriesIndex: DecimalDataSeriesIndex): @Domain Double? {
    val value = maxValues[dataSeriesIndices.indexOf(dataSeriesIndex)]
    if (value.isNoValue()) {
      return null
    }
    return value
  }

  /**
   * Builder for min/max values
   */
  class Builder(val dataSeriesIndices: List<DecimalDataSeriesIndex>) {
    init {
      require(dataSeriesIndices.isNotEmpty()) { "need at least one dataSeriesIndices" }
    }

    /**
     * Contains the min values - [NoValue] is a marker for "no value"
     */
    private val minValues: @Domain DoubleArray = DoubleArray(dataSeriesIndices.size) { NoValue }
    private val maxValues: @Domain DoubleArray = DoubleArray(dataSeriesIndices.size) { NoValue }

    /**
     * Extracts the min/max values from the given chunk at the provided timestamp index
     */
    fun appendFrom(chunk: HistoryChunk, timeStampIndex: TimestampIndex) {
      dataSeriesIndices.fastForEachIndexed { index, dataSeriesIndex ->
        val min = chunk.getMin(dataSeriesIndex, timeStampIndex)
        val max = chunk.getMax(dataSeriesIndex, timeStampIndex)

        if (min.isFinite()) {
          val oldValue = minValues[index]
          minValues[index] = if (oldValue.isNoValue()) min else oldValue.coerceAtMost(min)
        }
        if (max.isFinite()) {
          val oldValue = maxValues[index]
          maxValues[index] = if (oldValue.isNoValue()) max else oldValue.coerceAtLeast(min)
        }
      }
    }

    /**
     * Builds a new min/max values object
     */
    fun build(): MinMaxValues {
      return MinMaxValues(
        dataSeriesIndices,
        minValues,
        maxValues
      )
    }
  }

  companion object {
    /**
     * This value implies that a value is pending.
     *
     * Magic value - randomly chosen.
     */
    private const val NoValue: Double = 1.2390871231231292E14

    private fun Double.isNoValue(): Boolean {
      return this == NoValue
    }
  }
}


