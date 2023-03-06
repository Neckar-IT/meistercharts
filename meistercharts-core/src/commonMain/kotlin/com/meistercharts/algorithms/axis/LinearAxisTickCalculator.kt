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
package com.meistercharts.algorithms.axis

import com.meistercharts.annotations.Domain
import it.neckar.open.collections.DoubleArrayList
import it.neckar.open.collections.emptyDoubleArray
import it.neckar.open.kotlin.lang.roundDecimalPlaces
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

/**
 * Calculates the ticks for an axis. Tries to find "round" values (multiples of 10, 2 or 5)
 *
 */
object LinearAxisTickCalculator {
  /**
   * Calculates the tick values for the given max tick count and minimum tick distance.
   * @param maxTickCount the maximum number of ticks this function may return
   * @param minTickDistance the minimum distance between two consecutive ticks
   * @param axisEndConfiguration how the values at the end are calculated
   * @param intermediateValuesMode the type of intermediate values
   */
  fun calculateTickValues(
    lower: @Domain Double,
    upper: @Domain Double,
    axisEndConfiguration: AxisEndConfiguration = AxisEndConfiguration.Exact,
    maxTickCount: Int,
    minTickDistance: @Domain Double = 0.0,
    intermediateValuesMode: IntermediateValuesMode
  ): @Domain DoubleArray {
    require(maxTickCount >= 0) {
      "Max tick count must be greater than 0 but was <$maxTickCount>"
    }
    if (maxTickCount == 0) {
      return emptyDoubleArray()
    }
    require(minTickDistance >= 0) {
      "Min tick distance must be greater than or equal to 0 but was <$minTickDistance>"
    }

    require(lower.isFinite()) { "Lower must be finite but was <$lower>" }
    require(upper.isFinite()) { "Upper must be finite but was <$upper>" }

    require(lower <= upper) { "Lower $lower must be smaller than upper: $upper" }

    // Sometimes there are rounding errors that result in lower/upper values like 99.99999999999999
    // We try to work around this issues by rounding them. Add 0.0 at the end to avoid "-0.0"
    val maxDecimalPlaces = 10
    val lowerRounded = lower.roundDecimalPlaces(maxDecimalPlaces) + 0.0
    val upperRounded = upper.roundDecimalPlaces(maxDecimalPlaces) + 0.0

    val delta: @Domain Double = upper - lower
    val deltaRounded: @Domain Double = upperRounded - lowerRounded

    val tickDistance = if (deltaRounded == 0.0) {
      10.0.pow(-maxDecimalPlaces)
    } else {
      calculateTickDistance(delta, deltaRounded, maxTickCount, intermediateValuesMode)
    }.coerceAtLeast(minTickDistance)
    val tickBase = calculateTickBase(lowerRounded, tickDistance)

    @Domain val ticks = DoubleArrayList()
    var current = tickBase
    var index = 0
    while (current <= upperRounded) {
      ticks.add(current)

      //Calculate the next current
      index++
      current = tickBase + index * tickDistance
    }

    if (axisEndConfiguration == AxisEndConfiguration.Exact) {
      ensureContainsLowerUpper(ticks, lower, upper)
    }

    return ticks.toDoubleArray()
  }

  /**
   * This method ensures the lower and upper value are within the list.
   * Overwrites the first and last value if necessary
   */
  private fun ensureContainsLowerUpper(ticks: @Domain DoubleArrayList, lower: @Domain Double, upper: @Domain Double) {
    if (ticks.isEmpty()) {
      return
    }

    ticks[0] = lower
    ticks[ticks.size - 1] = upper
  }

  /**
   * Calculates the distance between ticks with the given max ticks.
   *
   * @param maxTickCount the maximum number of ticks
   *
   * This methods tries to find "nice" values between from/to, so that the amount of ticks
   * is same or lower than [maxTickCount].
   *
   *
   * ## How this algorithm works
   *
   * ### Step 1: Calculate the minimum distance between two ticks
   * Depending on the delta of the value range and the max ticks, we calculate the minimum distance
   * between ticks.
   * The returned distance between ticks may be larger than this value (then there will be fewer ticks
   * than [maxTickCount]. But never smaller (then there would be *more* ticks than [maxTickCount]).
   *
   * ### Step 2: Find a value larger (or same) as minTickDistance that is a power of 10
   * We want the ticks to be on nice/round values. Therefore we use log10 and power to get a value that is a power of 10.
   *
   * #### Larger/smaller
   * In most cases we look for a value that is larger than minTickDistance because minTickDistance is not a power of 10. --> greater is used
   * If minTickDistance is a power of 10, smaller is used
   *
   * ### Step 3: Optimize ticks
   * To improve the ticks, we check if we can apply some factors to get additional ticks.
   *
   */
  @Deprecated("Do not call this method from outside!")
  fun calculateTickDistance(
    delta: @Domain Double,
    deltaRounded: @Domain Double,
    maxTickCount: Int,
    intermediateValuesMode: IntermediateValuesMode = IntermediateValuesMode.Also5and2
  ): @Domain Double {
    require(maxTickCount > 0) {
      "Max tick count must be greater than 0 but was <$maxTickCount>"
    }
    require(deltaRounded > 0) {
      "The rounded delta must be greater than 0 but was <$deltaRounded>"
    }

    //Step 1:
    //The minimal distance between ticks. This value ensures the max ticks count is not exceeded
    @Domain val minTickDistance = (deltaRounded / maxTickCount)

    //Step 2
    //Guess the optimal tick distance that is just above or same as the min tick distance
    val floorLog10 = floor(log10(minTickDistance))

    //The tick distance that is the same or one magnitude greater than the min tick distance
    @Domain val smaller = 10.0.pow(floorLog10) //if we hit the min tick distance exactly
    @Domain val larger = 10.0.pow(floorLog10 + 1) //if min tick distance is too large

    @Domain val greaterTickDistance = if (smaller >= minTickDistance) smaller else larger

    //Calculate the tick count
    val tickCount = (delta / greaterTickDistance).toInt()

    require(tickCount <= maxTickCount) { "Invalid tick count: $tickCount" }

    //Step 3: Make some corrections to get the optimal result
    if (greaterTickDistance / minTickDistance > 10) {
      return greaterTickDistance / 10.0
    }

    if (intermediateValuesMode.also2s) {
      if (greaterTickDistance / minTickDistance > 5) {
        return greaterTickDistance / 5.0
      }
    }

    if (intermediateValuesMode.also5s) {
      if (greaterTickDistance / minTickDistance > 2) {
        return greaterTickDistance / 2.0
      }
    }

    return greaterTickDistance
  }

  /**
   * Calculates the first tick for the given tick distance.
   *
   * The first tick is always >= [lowerRounded].
   */
  @Domain
  fun calculateTickBase(@Domain lowerRounded: Double, @Domain tickDistance: Double): Double {
    return ceil(lowerRounded / tickDistance) * tickDistance
  }
}
