/*
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
package com.meistercharts.algorithms.layers.axis

import com.meistercharts.axis.AxisEndConfiguration
import com.meistercharts.axis.IntermediateValuesMode
import com.meistercharts.axis.LinearAxisTickCalculator
import com.meistercharts.axis.LogarithmicAxisTickCalculator
import com.meistercharts.annotations.Domain
import it.neckar.open.annotations.AllocationCost
import it.neckar.open.annotations.Allocates
import it.neckar.open.annotations.Hot
import it.neckar.open.annotations.HotAllocation
import it.neckar.open.collections.DoubleArrayList
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
  @Allocates(AllocationCost.Linear)
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

  /**
   * Fills the ticks into [target] (cleared first) - same semantics as [getTicks].
   *
   * Use this variant on the hot path: the built-in providers fill the buffer without allocating
   * a fresh array on every layout pass. The default implementation delegates to [getTicks].
   */
  @Hot
  fun fillTicks(
    lowerValue: @Domain Double,
    upperValue: @Domain Double,
    maxTickCount: Int,
    minTickDistance: @Domain Double,
    axisEndConfiguration: AxisEndConfiguration,
    target: @Domain DoubleArrayList,
  ) {
    target.clear()
    @HotAllocation("Fallback for custom TickProvider implementations that only implement getTicks - all built-in providers override fillTicks allocation-free")
    val ticks = getTicks(lowerValue, upperValue, maxTickCount, minTickDistance, axisEndConfiguration)
    for (i in ticks.indices) {
      target.add(ticks[i])
    }
  }

  companion object {
    /**
     * Default implementation for linear value axis
     */
    val linear: TickProvider = object : TickProvider {
      override fun getTicks(lowerValue: @Domain Double, upperValue: @Domain Double, maxTickCount: Int, minTickDistance: @Domain Double, axisEndConfiguration: AxisEndConfiguration): @Domain DoubleArray {
        return LinearAxisTickCalculator.calculateTickValues(lowerValue, upperValue, axisEndConfiguration, maxTickCount, minTickDistance, IntermediateValuesMode.Also5and2)
      }

      @Hot
      override fun fillTicks(lowerValue: @Domain Double, upperValue: @Domain Double, maxTickCount: Int, minTickDistance: @Domain Double, axisEndConfiguration: AxisEndConfiguration, target: @Domain DoubleArrayList) {
        LinearAxisTickCalculator.calculateTickValuesInto(target, lowerValue, upperValue, axisEndConfiguration, maxTickCount, minTickDistance, IntermediateValuesMode.Also5and2)
      }
    }

    val logarithmic: TickProvider = object : TickProvider {
      override fun getTicks(lowerValue: @Domain Double, upperValue: @Domain Double, maxTickCount: Int, minTickDistance: @Domain Double, axisEndConfiguration: AxisEndConfiguration): @Domain DoubleArray {
        return LogarithmicAxisTickCalculator.calculateTickValues(lowerValue, upperValue, maxTickCount, minTickDistance)
      }

      @Hot
      override fun fillTicks(lowerValue: @Domain Double, upperValue: @Domain Double, maxTickCount: Int, minTickDistance: @Domain Double, axisEndConfiguration: AxisEndConfiguration, target: @Domain DoubleArrayList) {
        LogarithmicAxisTickCalculator.calculateTickValuesInto(target, lowerValue, upperValue, maxTickCount, minTickDistance)
      }
    }
  }
}

/**
 * Does not provide any ticks at all
 */
object NoTicksProvider : TickProvider {
  override fun getTicks(lowerValue: @Domain Double, upperValue: @Domain Double, maxTickCount: Int, minTickDistance: @Domain Double, axisEndConfiguration: AxisEndConfiguration): @Domain DoubleArray {
    return emptyDoubleArray()
  }

  @Hot
  override fun fillTicks(lowerValue: @Domain Double, upperValue: @Domain Double, maxTickCount: Int, minTickDistance: @Domain Double, axisEndConfiguration: AxisEndConfiguration, target: @Domain DoubleArrayList) {
    target.clear()
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

  @Hot
  override fun fillTicks(lowerValue: @Domain Double, upperValue: @Domain Double, maxTickCount: Int, minTickDistance: @Domain Double, axisEndConfiguration: AxisEndConfiguration, target: @Domain DoubleArrayList) {
    target.clear()
    for (i in ticks.indices) {
      target.add(ticks[i])
    }
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

  @Hot
  override fun fillTicks(lowerValue: @Domain Double, upperValue: @Domain Double, maxTickCount: Int, minTickDistance: @Domain Double, axisEndConfiguration: AxisEndConfiguration, target: @Domain DoubleArrayList) {
    delegate.fillTicks(lowerValue, upperValue, maxTickCount.coerceAtMost(this.maxTickCount), minTickDistance, axisEndConfiguration, target)
  }
}

/**
 * Wraps this in a [MaxNumberOfTicksProvider]
 */
fun TickProvider.withMaxNumberOfTicks(maxTickCount: Int): MaxNumberOfTicksProvider {
  if (this is MaxNumberOfTicksProvider) {
    //Avoid wrapping multiple times - keep the tighter of the two caps
    return MaxNumberOfTicksProvider(maxTickCount.coerceAtMost(this.maxTickCount), this.delegate)
  }

  return MaxNumberOfTicksProvider(maxTickCount, this)
}

/**
 * Returns 0.0 and 1.0
 */
object BinaryTicksProvider : ConstantTicksProvider(doubleArrayOf(0.0, 1.0))

