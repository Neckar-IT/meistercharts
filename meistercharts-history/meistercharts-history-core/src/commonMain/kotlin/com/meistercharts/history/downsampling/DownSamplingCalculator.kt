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
package com.meistercharts.history.downsampling

import com.meistercharts.annotations.Domain
import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.EnumDataSeriesIndex
import com.meistercharts.history.HistoryEnumOrdinal
import com.meistercharts.history.HistoryEnumOrdinalInt
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.HistoryEnumSetInt
import com.meistercharts.history.MayBeNoValueOrPending
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryDifferentIdsCount
import com.meistercharts.history.ReferenceEntryDifferentIdsCountInt
import com.meistercharts.history.ReferenceEntryId
import com.meistercharts.history.ReferenceEntryIdInt
import com.meistercharts.history.impl.HistoryChunk
import com.meistercharts.history.impl.HistoryChunk.Companion.isNoValue
import com.meistercharts.history.impl.HistoryChunk.Companion.isPending
import com.meistercharts.history.impl.HistoryChunk.Companion.maxHistoryAware
import com.meistercharts.history.impl.HistoryChunk.Companion.minHistoryAware
import it.neckar.open.annotations.TestOnly
import it.neckar.open.collections.fastForEach
import it.neckar.open.collections.fastForEachIndexed
import it.neckar.open.collections.mapToIntArray

/**
 * This class can be used to calculate down sampling values.
 *
 * For decimals, it calculates:
 * - average
 * - minimum
 * - maximum
 *
 * For enums, it calculates:
 * * union set
 *
 */
class DownSamplingCalculator(
  /**
   * How many decimal data series are supported
   */
  val decimalDataSeriesCount: Int,
  /**
   * How many enum data series are supported
   */
  val enumDataSeriesCount: Int,

  /**
   * How many reference entry data series are supported
   */
  val referenceEntryDataSeriesCount: Int,
) {

  //
  //Fields for down sampling of decimal values
  //

  /**
   * Contains the current average for each data series.
   */
  private val averages: @Domain DoubleArray = DoubleArray(decimalDataSeriesCount) { HistoryChunk.Pending }

  /**
   * The current min value for each data series
   */
  private val minValues: @Domain DoubleArray = DoubleArray(decimalDataSeriesCount) { HistoryChunk.Pending }

  /**
   * The current max value for each data series
   */
  private val maxValues: @Domain DoubleArray = DoubleArray(decimalDataSeriesCount) { HistoryChunk.Pending }

  /**
   * The current count of added samples for each data series. These are required to be able to update the average correctly.
   *
   * It is necessary to save the count of entries, because these might differ for the decimal series.
   *
   * Example:
   * Temperature and distance are measured. The distance is invalid (the sensor could not measure a value) - while the temperature is valid.
   * When calculating the averages, NoValue is ignored for the distance average calculation.
   */
  private val averageCalculationCounts: IntArray = IntArray(decimalDataSeriesCount) { 0 }


  //
  //Fields for down sampling of enum values
  //

  /**
   * A bit set that contains the union of all enum options
   */
  private val enumUnionValues: @HistoryEnumSetInt IntArray = IntArray(enumDataSeriesCount) { HistoryEnumSet.PendingAsInt }

  /**
   * Contains the *ordinal* counters to identify which ordinal has been active the most time
   * The array contains a counter for each enum data series
   */
  private val enumMostTimeOrdinalCounters: @HistoryEnumOrdinalInt Array<HistoryEnumOrdinalCounter> = Array(enumDataSeriesCount) { HistoryEnumOrdinalCounter() }

  /**
   * The current no-value-state ([HistoryChunk.NoValue]) for each data series.
   * If at least one entry with [HistoryChunk.NoValue] has been added, this array contains true at the corresponding position
   */
  private val containsNoValueDecimal: BooleanArray = BooleanArray(decimalDataSeriesCount)

  /**
   * The current no-value-state ([HistoryEnumSet.NoValue]) for each data series.
   * If at least one entry with [HistoryEnumSet.NoValue] has been added, this array contains true at the corresponding position
   */
  private val containsNoValueEnum: BooleanArray = BooleanArray(enumDataSeriesCount)

  /**
   * The current no-value-state for each reference entry data series.
   * If at least one entry with [ReferenceEntryId.NoValue] has been added, this array contains true at the corresponding position.
   */
  private val containsNoValueReferenceEntry: BooleanArray = BooleanArray(referenceEntryDataSeriesCount)

  //
  //Fields for down sampling of reference entries
  //

  /**
   * Contains the counters for the most of the time entries
   */
  private val referenceEntryCounters: @ReferenceEntryIdInt Array<ReferenceEntryCounter> = Array(referenceEntryDataSeriesCount) { ReferenceEntryCounter() }

  /**
   * Contains the bit set representing the union of all enum options for the statuses of a reference entry
   */
  private val referenceEntryStatusesUnionValues: @HistoryEnumSetInt IntArray = IntArray(referenceEntryDataSeriesCount) { HistoryEnumSet.PendingAsInt }

  /**
   * Returns the number of entries for the given data series index that have been used
   * to calculate the current average.
   */
  internal fun averageCalculationCount(dataSeriesIndex: DecimalDataSeriesIndex): Int {
    return averageCalculationCounts[dataSeriesIndex.value]
  }

  /**
   * Returns whether the data series at the given index contains at least one no value ([HistoryChunk.NoValue])
   */
  fun containsNoValue(dataSeriesIndex: DecimalDataSeriesIndex): Boolean = containsNoValueDecimal[dataSeriesIndex.value]

  fun containsNoValue(dataSeriesIndex: EnumDataSeriesIndex): Boolean = containsNoValueEnum[dataSeriesIndex.value]

  fun containsNoValue(dataSeriesIndex: ReferenceEntryDataSeriesIndex): Boolean = containsNoValueReferenceEntry[dataSeriesIndex.value]

  /**
   * Calculates the average significand for the data series at the given index.
   *
   * Returns [HistoryChunk.NoValue] if there are no real values and at least *one* [HistoryChunk.NoValue]
   * Returns [HistoryChunk.Pending] if *all* entries are [HistoryChunk.Pending]
   */
  @TestOnly
  fun averageValue(dataSeriesIndex: DecimalDataSeriesIndex): Double {
    val count = averageCalculationCount(dataSeriesIndex)
    if (count != 0) {
      //We have at least one value - therefore we are able to calculate the average
      return averages[dataSeriesIndex.value]
    }

    //No numeric values found at all

    if (containsNoValue(dataSeriesIndex)) {
      //We have at least one entry of NoValue. Therefore, return no value
      return HistoryChunk.NoValue
    }

    //must be pending at this point
    return HistoryChunk.Pending
  }

  @TestOnly
  fun minDecimal(dataSeriesIndex: DecimalDataSeriesIndex): @Domain Double {
    val count = averageCalculationCount(dataSeriesIndex)
    if (count != 0) {
      //We have at least one value - therefore there exists a min value
      return minValues[dataSeriesIndex.value]
    }

    //No numeric values found at all
    if (containsNoValue(dataSeriesIndex)) {
      //We have at least one entry of NoValue. Therefore, return no value
      return HistoryChunk.NoValue
    }

    //must be pending at this point
    return HistoryChunk.Pending
  }

  /**
   * Returns the max decimal value
   */
  @TestOnly
  fun maxDecimal(dataSeriesIndex: DecimalDataSeriesIndex): @Domain Double {
    val count = averageCalculationCount(dataSeriesIndex)
    if (count != 0) {
      //We have at least one value - therefore there exists a min value
      return maxValues[dataSeriesIndex.value]
    }

    //No numeric values found at all
    if (containsNoValue(dataSeriesIndex)) {
      //We have at least one entry of NoValue. Therefore, return no value
      return HistoryChunk.NoValue
    }

    //must be pending at this point
    return HistoryChunk.Pending
  }

  /**
   * Returns the enum set.
   *
   * Possible return values (in order):
   * * if at least one value has been recorded: Returns the union set of all values
   * * if at least one [HistoryEnumSet.NoValue] has been recorded: Returns [HistoryEnumSet.NoValue]
   * * else: [HistoryEnumSet.Pending]
   */
  @TestOnly
  fun enumValue(dataSeriesIndex: EnumDataSeriesIndex): HistoryEnumSet {
    val value = HistoryEnumSet(enumUnionValues[dataSeriesIndex.value])

    if (value.isPending().not()) {
      //We found a valid value, return immediately
      return value
    }

    //No values calculated so far! Check if no value has been added
    if (containsNoValue(dataSeriesIndex)) {
      //We have at least one entry of NoValue. Therefore, return no value
      return HistoryEnumSet.NoValue
    }

    //must be pending at this point
    return HistoryEnumSet.Pending
  }

  /**
   * Returns the most time enum ordinal
   */
  @TestOnly
  fun enumOrdinalMostTime(dataSeriesIndex: EnumDataSeriesIndex): HistoryEnumOrdinal {
    return enumMostTimeOrdinalCounters[dataSeriesIndex.value].winner()
  }

  /**
   * Returns the count of reference entries for the given data series index
   */
  @TestOnly
  fun referenceEntryDifferentIdsCount(dataSeriesIndex: ReferenceEntryDataSeriesIndex): @MayBeNoValueOrPending ReferenceEntryDifferentIdsCount {
    val counter = referenceEntryCounters.get(dataSeriesIndex)
    return counter.differentIdsCount()
  }

  /**
   * Returns the reference entry id that has been active for most of the time
   */
  @TestOnly
  fun referenceEntryMostOfTheTime(dataSeriesIndex: ReferenceEntryDataSeriesIndex): @MayBeNoValueOrPending ReferenceEntryId {
    val counter = referenceEntryCounters[dataSeriesIndex.value]
    return counter.winnerMostOfTheTime()
  }

  @TestOnly
  fun referenceEntryStatus(dataSeriesIndex: ReferenceEntryDataSeriesIndex): HistoryEnumSet {
    val value = HistoryEnumSet(referenceEntryStatusesUnionValues[dataSeriesIndex.value])

    if (value.isPending().not()) {
      //We found a valid value, return immediately
      return value
    }

    //No values calculated so far! Check if no value has been added
    if (containsNoValue(dataSeriesIndex)) {
      //We have at least one entry of NoValue. Therefore, return no value
      return HistoryEnumSet.NoValue
    }

    //must be pending at this point
    return HistoryEnumSet.Pending
  }

  /**
   * Returns the averages for each data series
   */
  internal fun averageValues(): @Domain DoubleArray {
    return averages
  }

  internal fun minValues(): @Domain DoubleArray {
    return minValues
  }

  internal fun maxValues(): @Domain DoubleArray {
    return maxValues
  }

  /**
   * Returns the union values for each data series
   */
  internal fun enumUnionValues(): @HistoryEnumSetInt IntArray {
    return enumUnionValues
  }

  /**
   * Returns the enum ordinal values that have been recorded most of the time
   */
  internal fun enumMostTimeOrdinalValues(): @HistoryEnumOrdinalInt IntArray {
    return IntArray(enumMostTimeOrdinalCounters.size) {
      enumMostTimeOrdinalCounters[it].winner().value
    }
  }

  /**
   * Returns the "winners"
   */
  internal fun referenceEntryIds(): @ReferenceEntryIdInt @MayBeNoValueOrPending IntArray {
    return referenceEntryCounters.mapToIntArray { it.winnerMostOfTheTime().id }
  }

  /**
   * Returns the count of the different entry ids
   */
  internal fun referenceEntryDifferentIdsCount(): @ReferenceEntryDifferentIdsCountInt @MayBeNoValueOrPending IntArray {
    return referenceEntryCounters.mapToIntArray { it.differentIdsCount().value }
  }

  internal fun referenceEntryStatuses(): @HistoryEnumSetInt IntArray {
    return referenceEntryStatusesUnionValues
  }

  /**
   * Adds a new sample
   * @param newDecimalValues the values for each data series of the sample (at *one* point in time)
   */
  fun addDecimalsSample(newDecimalValues: DoubleArray) {
    newDecimalValues.fastForEachIndexed { dataSeriesIndex, value ->
      when {
        value.isNoValue() -> {
          containsNoValueDecimal[dataSeriesIndex] = true
        }

        value.isPending() -> {
          //ignore
        }

        else -> {
          val oldCount = averageCalculationCounts[dataSeriesIndex]
          averageCalculationCounts[dataSeriesIndex] = oldCount + 1

          val newAverage: Double = if (oldCount == 0) {
            value
          } else {
            val currentAverage = averages[dataSeriesIndex]
            val deltaCurrentValue = value - currentAverage

            currentAverage + deltaCurrentValue / (oldCount + 1)
          }

          averages[dataSeriesIndex] = newAverage

          maxValues[dataSeriesIndex] = maxHistoryAware(value, maxValues[dataSeriesIndex])
          minValues[dataSeriesIndex] = minHistoryAware(value, minValues[dataSeriesIndex])
        }
      }
    }
  }

  /**
   * Adds a new sample - at one point in time
   * @param newEnumValues the values for each data series of the sample (at *one* point in time)
   */
  fun addEnumSample(newEnumValues: @HistoryEnumSetInt IntArray) {
    require(newEnumValues.size == enumUnionValues.size) {
      "Invalid size. Was ${newEnumValues.size} but expected ${enumUnionValues.size}"
    }

    newEnumValues.fastForEachIndexed { dataSeriesIndex, historyEnumSetAsInt: @HistoryEnumSetInt Int ->
      when (val historyEnumSet = HistoryEnumSet(historyEnumSetAsInt)) {
        HistoryEnumSet.NoValue -> {
          containsNoValueEnum[dataSeriesIndex] = true
        }

        HistoryEnumSet.Pending -> {
          //ignore
        }

        else -> {
          @HistoryEnumSetInt val currentBitSet = enumUnionValues[dataSeriesIndex]

          val newBitSet = if (HistoryEnumSet.isPending(currentBitSet)) {
            historyEnumSetAsInt
          } else {
            currentBitSet or historyEnumSetAsInt
          }

          enumUnionValues[dataSeriesIndex] = newBitSet

          //Add to the counter
          enumMostTimeOrdinalCounters[dataSeriesIndex].addAll(historyEnumSet)
        }
      }
    }
  }

  /**
   * Adds a sample of reference entries to the counter
   *
   * @param newReferenceEntries an array of reference entry IDs
   * @param newDifferentIdsCount an optional array of counts of different IDs for each reference entry
   * @param newStatuses the new statuses
   *
   * @throws IllegalArgumentException if the size of newReferenceEntries does not match the size of the referenceEntryCounters
   */
  fun addReferenceEntrySample(
    newReferenceEntries: @ReferenceEntryIdInt IntArray,
    newDifferentIdsCount: @ReferenceEntryIdInt IntArray?,
    newStatuses: @HistoryEnumSetInt IntArray,
  ) {
    require(newReferenceEntries.size == referenceEntryCounters.size) {
      "Invalid size. Was ${newReferenceEntries.size} but expected ${referenceEntryCounters.size}"
    }
    require(newStatuses.size == referenceEntryCounters.size) {
      "Invalid size. Was ${newStatuses.size} but expected ${referenceEntryCounters.size}"
    }
    require(newDifferentIdsCount == null || newDifferentIdsCount.size == referenceEntryCounters.size) {
      "Invalid size. Was ${newDifferentIdsCount?.size} but expected ${referenceEntryCounters.size}"
    }

    newReferenceEntries.fastForEachIndexed { dataSeriesIndex, referenceEntryIdAsInt: @ReferenceEntryIdInt Int ->
      val referenceEntryId = ReferenceEntryId(referenceEntryIdAsInt)

      if (referenceEntryId == ReferenceEntryId.NoValue) {
        containsNoValueReferenceEntry[dataSeriesIndex] = true
      }

      val differentIdsCount = newDifferentIdsCount?.get(dataSeriesIndex)?.let { ReferenceEntryDifferentIdsCount(it) }
      referenceEntryCounters[dataSeriesIndex].add(referenceEntryId, differentIdsCount)

      @HistoryEnumSetInt val newStatusesAsInt = newStatuses[dataSeriesIndex]
      when (newStatusesAsInt) {
        HistoryEnumSet.NoValueAsInt -> {
        }

        HistoryEnumSet.PendingAsInt -> {
          //ignore
        }

        else -> {
          @HistoryEnumSetInt val currentBitSet = referenceEntryStatusesUnionValues[dataSeriesIndex]

          val newBitSet = if (HistoryEnumSet.isPending(currentBitSet)) {
            newStatusesAsInt
          } else {
            currentBitSet or newStatusesAsInt
          }

          referenceEntryStatusesUnionValues[dataSeriesIndex] = newBitSet
        }
      }
    }
  }

  /**
   * Resets the down sampling calculator
   */
  fun reset() {
    averages.fastForEachIndexed { dataSeriesIndex, _ ->
      averageCalculationCounts[dataSeriesIndex] = 0
      averages[dataSeriesIndex] = HistoryChunk.Pending
      minValues[dataSeriesIndex] = Double.MAX_VALUE
      maxValues[dataSeriesIndex] = Double.MIN_VALUE
      containsNoValueDecimal[dataSeriesIndex] = false
    }

    enumUnionValues.fastForEachIndexed { dataSeriesIndex, _ ->
      containsNoValueEnum[dataSeriesIndex] = false
      enumUnionValues[dataSeriesIndex] = HistoryEnumSet.PendingAsInt
    }
    enumMostTimeOrdinalCounters.fastForEach {
      it.reset()
    }

    referenceEntryCounters.fastForEach {
      it.reset()
    }

    containsNoValueReferenceEntry.fastForEachIndexed { dataSeriesIndex, _ ->
      containsNoValueReferenceEntry[dataSeriesIndex] = false
    }

    referenceEntryStatusesUnionValues.fastForEachIndexed { dataSeriesIndex, _ ->
      referenceEntryStatusesUnionValues[dataSeriesIndex] = HistoryEnumSet.PendingAsInt
    }
  }
}

private fun Array<ReferenceEntryCounter>.get(dataSeriesIndex: ReferenceEntryDataSeriesIndex): ReferenceEntryCounter {
  return this[dataSeriesIndex.value]
}
