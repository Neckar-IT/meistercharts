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

import com.meistercharts.model.LinearValueRange
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
