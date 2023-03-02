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
   * Returns the ticks
   * @param minTickDistance describes the minimum distance between two ticks. This parameter can be used to avoid too many ticks when zoomed in that have the same label (because of the precision of the used format)
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
    axisEndConfiguration: AxisEndConfiguration
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

