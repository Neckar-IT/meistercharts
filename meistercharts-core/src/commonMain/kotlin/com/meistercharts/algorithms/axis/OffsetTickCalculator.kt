package com.meistercharts.algorithms.axis

import com.meistercharts.algorithms.LinearValueRange
import it.neckar.open.kotlin.lang.ceil
import it.neckar.open.kotlin.lang.floor
import kotlin.math.pow

/**
 * Calculates the offset ticks for an offset axis
 */
object OffsetTickCalculator {
  /**
   * Calculates the offsets for the given value range and the given exponent (to the base 10)
   */
  fun calculateOffsets(valueRange: LinearValueRange, exponentForTicks: Int): DoubleArray {
    val factor = 10.0.pow(exponentForTicks)

    val startReduced = (valueRange.start / factor).floor()
    val endReduced = (valueRange.end / factor).floor()

    return doubleArrayOf(startReduced * factor, endReduced * factor)
  }

  /**
   * Calculates a tick value that can be used together with an offset
   */
  fun calculateTickValueForOffset(value: Double, exponentForTicks: Int): Double {
    val factor = 10.0.pow(exponentForTicks)
    return value % factor
  }

  /**
   * Calculates the offset for the given number and the given integerDigits
   */
  fun offsetForNumber(value: Double, integerDigits: Int): Double {
    val magnitudeValue = 10.0.pow(integerDigits)
    val factor = (value / magnitudeValue)

    return magnitudeValue * if (value < 0.0) {
      factor.ceil()
    } else {
      factor.floor()
    }
  }
}
