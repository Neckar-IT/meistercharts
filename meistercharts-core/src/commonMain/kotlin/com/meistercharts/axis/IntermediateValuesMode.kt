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

/**
 * Whether to use any intermediate values
 */
enum class IntermediateValuesMode(
  val also5s: Boolean,
  val also2s: Boolean
) {
  /**
   * Do not use any intermediate values - only 10
   */
  Only10(false, false),

  /**
   * Also use 5s
   */
  Also5(true, false),

  /**
   * Also use 2s
   */
  Also2(false, true),

  /**
   * Also use 5s and 2s
   */
  Also5and2(true, true);


  /**
   * Returns the smallest *candidate* value, the given predicate returns true for - or the [upperBound] if the predicate returns false for all values.
   * The [upperBound] should be a power of 10. Calling this method with other values does not make any sense.
   *
   * The predicate will be called for all candidates - smaller to larger.
   *
   * Attention: The predicate will *not* be checked for the [upperBound]. The [predicate] will only
   * be checked for the candidates!
   */
  fun findSmaller(upperBound: Double, predicate: (Double) -> Boolean): Double {
    when (this) {
      Only10 -> {
        //Returns the upper bound directly
        return upperBound
      }

      Also5 -> {
        val candidate = upperBound / 2.0
        if (predicate(candidate)) {
          return candidate
        }

        return upperBound
      }

      Also2 -> {
        for (i in 1..4) {
          val candidate = upperBound / 5.0 * i
          if (predicate(candidate)) {
            return candidate
          }
        }
        return upperBound
      }

      Also5and2 -> {
        (upperBound / 5.0).let {
          if (predicate(it)) {
            return it
          }
        }
        (upperBound / 5.0 * 2).let {
          if (predicate(it)) {
            return it
          }
        }
        (upperBound / 2.0).let {
          if (predicate(it)) {
            return it
          }
        }
        (upperBound / 5.0 * 3).let {
          if (predicate(it)) {
            return it
          }
        }
        (upperBound / 5.0 * 4).let {
          if (predicate(it)) {
            return it
          }
        }
        return upperBound
      }
    }
  }

  /**
   * Returns the largest *candidate* value, the given predicate returns true for - or the [lowerBound] if the predicate returns false for all values.
   * The [lowerBound] should be a power of 10. Calling this method with other values does not make any sense.
   *
   * The predicate will be called for all candidates - largest to smallest.
   *
   * Attention: The predicate will *not* be checked for the [lowerBound]. The [predicate] will only
   * be checked for the candidates!
   */
  fun findLarger(lowerBound: Double, predicate: (Double) -> Boolean): Double {
    when (this) {
      Only10 -> {
        //Returns the lower bound directly
        return lowerBound
      }

      Also5 -> {
        val candidate = lowerBound * 5
        if (predicate(candidate)) {
          return candidate
        }

        return lowerBound
      }

      Also2 -> {
        for (i in 4 downTo 1) {
          val candidate = lowerBound * 2 * i
          if (predicate(candidate)) {
            return candidate
          }
        }
        return lowerBound
      }

      Also5and2 -> {
        (lowerBound * 8).let {
          if (predicate(it)) {
            return it
          }
        }
        (lowerBound * 6).let {
          if (predicate(it)) {
            return it
          }
        }
        (lowerBound * 5).let {
          if (predicate(it)) {
            return it
          }
        }
        (lowerBound * 4).let {
          if (predicate(it)) {
            return it
          }
        }
        (lowerBound * 2).let {
          if (predicate(it)) {
            return it
          }
        }

        return lowerBound
      }
    }
  }
}
