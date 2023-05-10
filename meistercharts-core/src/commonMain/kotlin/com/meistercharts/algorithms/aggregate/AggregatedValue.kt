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
package com.meistercharts.algorithms.aggregate

import com.meistercharts.annotations.Domain
import it.neckar.open.unit.si.ms
import kotlin.math.max
import kotlin.math.min

/**
 * Combines several points in time to one point
 *
 */
class AggregatedValue(
  @ms val from: Double,
  @ms val to: Double,

  @Domain val min: Double,
  @Domain val max: Double,
  @Domain val average: Double
) {

  /**
   * Returns the center time
   */
  val time: Double
    @ms
    get() = if (from == to) from else from / 2.0 + to / 2.0

  /**
   * The Builder
   */
  class Builder(
    @ms
    private val maxSpan: Double
  ) {

    @ms
    private var first: Double? = null

    @ms
    private var last: Double? = null

    @Domain
    private val values = ArrayList<Double>() // TODO why not use the primitive type doubleArrayOf()?

    val isEmpty: Boolean
      get() = first == null

    fun fits(@ms time: Double): Boolean {
      if (first == null) { // TODO I would assume that an empty value does not contain anything
        //The first element, always accept // FIXME what if maxSpan equals 0?
        return true
      }

      @ms val delta = time - first!!
      return delta <= maxSpan
    }

    fun add(@ms time: Double, @Domain value: Double) {
      if (first == null) {
        first = time
      }

      last = time
      values.add(value)
    }

    fun clear() {
      first = null
      last = null
      values.clear()
    }

    fun build(): AggregatedValue {
      var min = Double.MAX_VALUE
      var max = -Double.MAX_VALUE
      var sum = 0.0

      for (value in values) {
        sum += value

        min = min(value, min)
        max = max(value, max)
      }

      return AggregatedValue(first!!, last!!, min, max, sum / values.size) // TODO I would not throw a NullPointerException at this point but a different exception if isEmpty holds (with an earlier check)
    }
  }
}
