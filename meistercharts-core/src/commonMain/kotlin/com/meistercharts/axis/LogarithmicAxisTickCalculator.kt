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
package com.meistercharts.axis

import com.meistercharts.annotations.Domain
import it.neckar.open.collections.fastMapDouble
import it.neckar.open.kotlin.lang.or0ifNaN
import it.neckar.open.kotlin.lang.or0ifNanOrInfinite
import it.neckar.open.kotlin.lang.or1ifInfinite
import it.neckar.open.kotlin.lang.or1ifNaN
import kotlin.math.log10
import kotlin.math.pow

/**
 * Calculates the ticks for a logarithmic axis
 */
object LogarithmicAxisTickCalculator {
  /**
   * Calculates the tick values for the given max tick count and minimum tick distance.
   *
   * @param lower The lower bound of the axis.
   * @param upper The upper bound of the axis.
   * @param maxTickCount The maximum number of tick values to return.
   * @param minTickDistance The minimum distance between tick values. If the calculated distance is less than this value, the distance will be increased to this value.
   *
   * @return An array of tick values.
   */
  fun calculateTickValues(
    lower: @Domain Double,
    upper: @Domain Double,
    maxTickCount: Int,
    minTickDistance: @Domain Double = 0.0,
  ): @Domain DoubleArray {

    val logLower: Double = log10(lower)
    val logUpper: Double = log10(upper)
    val minTickDistanceLog = log10(minTickDistance).or1ifInfinite()

    return calculateExponents(logLower, logUpper, maxTickCount, minTickDistanceLog).fastMapDouble {
      10.0.pow(it)
    }
  }

  /**
   * Calculates the exponent ticks
   */
  fun calculateExponents(logLower: Double, logUpper: Double, maxTickCount: Int, minTickDistance: @Domain Double): DoubleArray {
    return LinearAxisTickCalculator.calculateTickValues(
      logLower, logUpper, AxisEndConfiguration.Default,
      maxTickCount, minTickDistance, IntermediateValuesMode.Only10
    )
  }
}
