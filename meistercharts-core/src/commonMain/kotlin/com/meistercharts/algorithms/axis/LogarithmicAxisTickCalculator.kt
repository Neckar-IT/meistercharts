package com.meistercharts.algorithms.axis

import com.meistercharts.annotations.Domain
import it.neckar.open.collections.fastMapDouble
import kotlin.math.log10
import kotlin.math.pow

/**
 * Calculates the ticks for a logarithmic axis
 */
object LogarithmicAxisTickCalculator {
  /**
   * Calculates the tick values for the given max tick count and minimum tick distance.
   * @param maxTickCount the maximum number of ticks this function may return
   * @param minTickDistance the minimum distance between two consecutive ticks
   */
  fun calculateTickValues(
    lower: @Domain Double,
    upper: @Domain Double,
    maxTickCount: Int,
    minTickDistance: @Domain Double = 0.0,
  ): @Domain DoubleArray {

    val logLower: Double = log10(lower)
    val logUpper: Double = log10(upper)

    return calculateExponents(logLower, logUpper, maxTickCount, minTickDistance).fastMapDouble {
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
