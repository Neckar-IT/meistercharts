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
package com.meistercharts.algorithms.layers

import com.meistercharts.algorithms.axis.AxisEndConfiguration
import com.meistercharts.algorithms.axis.IntermediateValuesMode
import com.meistercharts.algorithms.axis.LinearAxisTickCalculator
import com.meistercharts.algorithms.axis.LogarithmicAxisTickCalculator
import com.meistercharts.annotations.Domain
import it.neckar.open.collections.emptyDoubleArray

/**
 * Provides the ticks for the value axis
 */
fun interface TickProvider {
  /**
   * Returns an array of ticks for the given range of domain values, with a maximum count and a minimum distance between ticks.
   *
   * @param lowerValue the lower value of the domain range.
   * @param upperValue the upper value of the domain range.
   * @param maxTickCount the maximum number of ticks to be generated.
   * @param minTickDistance the minimum distance between two ticks, used to avoid too many ticks with the same label when zoomed in. This parameter should not be used anymore.
   * @param axisEndConfiguration the configuration for the axis end points.
   * @return an array of ticks for the given range of domain values.
   */
  fun getTicks(
    /**
     * The lower value
     */
    lowerValue: @Domain Double,
    /**
     * The upper value
     */
    upperValue: @Domain Double,
    /**
     * The max tick count
     */
    maxTickCount: Int,
    /**
     * The minimum distance between ticks
     *
     * !!Do not use anymore!!
     */
    minTickDistance: @Domain Double,
    /**
     * The axis end configuration
     */
    axisEndConfiguration: AxisEndConfiguration,
  ): @Domain DoubleArray

  companion object {
    /**
     * Default implementation for linear value axis
     */
    val linear: TickProvider =
      TickProvider { lowerValue, upperValue, maxTickCount, minTickDistance, axisEndConfiguration ->
        LinearAxisTickCalculator.calculateTickValues(lowerValue, upperValue, axisEndConfiguration, maxTickCount, minTickDistance, IntermediateValuesMode.Also5and2)
      }

    val logarithmic: TickProvider =
      TickProvider { lowerValue, upperValue, maxTickCount, minTickDistance, _ ->
        LogarithmicAxisTickCalculator.calculateTickValues(lowerValue, upperValue, maxTickCount, minTickDistance)
      }
  }
}

/**
 * Does not provide any ticks at all
 */
object NoTicksProvider : TickProvider {
  override fun getTicks(lowerValue: Double, upperValue: Double, maxTickCount: Int, minTickDistance: Double, axisEndConfiguration: AxisEndConfiguration): DoubleArray {
    return emptyDoubleArray()
  }
}

/**
 * Returns the given ticks.
 */
open class ConstantTicksProvider(
  val ticks: @Domain DoubleArray
) : TickProvider {
  override fun getTicks(lowerValue: @Domain Double, upperValue: @Domain Double, maxTickCount: Int, minTickDistance: @Domain Double, axisEndConfiguration: AxisEndConfiguration): @Domain DoubleArray {
    return ticks
  }

  companion object {
    /**
     * Only provides 0.0
     */
    val only0: TickProvider = ConstantTicksProvider(doubleArrayOf(0.0))
  }
}

/**
 * Provides no more than [maxTickCount] ticks
 */
class MaxNumberOfTicksProvider(val maxTickCount: Int, val delegate: TickProvider) : TickProvider {
  override fun getTicks(lowerValue: @Domain Double, upperValue: @Domain Double, maxTickCount: Int, minTickDistance: @Domain Double, axisEndConfiguration: AxisEndConfiguration): @Domain DoubleArray {
    return delegate.getTicks(lowerValue, upperValue, maxTickCount.coerceAtMost(this.maxTickCount), minTickDistance, axisEndConfiguration)
  }
}

/**
 * Wraps this in a [MaxNumberOfTicksProvider]
 */
fun TickProvider.withMaxNumberOfTicks(maxTickCount: Int): MaxNumberOfTicksProvider {
  if (this is MaxNumberOfTicksProvider) {
    //Avoid wrapping multiple times
    return MaxNumberOfTicksProvider(maxTickCount, this.delegate)
  }

  return MaxNumberOfTicksProvider(maxTickCount, this)
}

/**
 * Returns 0.0 and 1.0
 */
object BinaryTicksProvider : ConstantTicksProvider(doubleArrayOf(0.0, 1.0))

