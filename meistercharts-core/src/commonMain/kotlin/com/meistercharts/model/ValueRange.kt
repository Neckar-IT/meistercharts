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
package com.meistercharts.model

import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.DomainRelative
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic

/**
 * Describes a range a (domain) value usually lies within.
 * This range is used to draw axis and convert the [Domain] value to domain relative ( [DomainRelative]
 *
 * ATTENTION: A value range may have a delta of 0.0
 */
@Serializable
abstract class ValueRange protected constructor(
  @Domain
  val start: Double,
  @Domain
  val end: Double
) {

  init {
    require(start.isFinite()) {
      "Start must be finite but was $start"
    }
    require(end.isFinite()) {
      "End must be finite but was $start"
    }

    require(start <= end) {
      "The start ($start) must be smaller or same as the end ($end)."
    }
  }

  /**
   * Returns true if this value range contains the given value
   */
  operator fun contains(@Domain value: Double): Boolean = value in start..end

  /**
   * Returns the delta between start and end
   */
  @Domain
  val delta: Double
    get() = end - start

  /**
   * Returns true if [start] and [end] are the same (and therefore [delta] is 0.0)
   */
  fun isEmpty(): Boolean {
    return start == end
  }

  /**
   * The delta *above* zero.
   *
   * If the value range only contains negative values, this will return 0.0
   */
  @Domain
  val deltaPositive: Double
    get() {
      return end.coerceAtLeast(0.0) - start.coerceAtLeast(0.0)
    }

  /**
   * The delta *below* zero.
   *
   * If the value range only contains positive values, this will return 0.0
   */
  @Domain
  val deltaNegative: Double
    get() {
      return end.coerceAtMost(0.0) - start.coerceAtMost(0.0)
    }

  /**
   * Converts a domain value to domain relative.
   *
   * ATTENTION: Only works for *absolute* domain values.
   * For deltas between two domain values use [LinearValueRange.deltaToDomainRelative] instead.
   */
  abstract fun toDomainRelative(domainValue: @Domain Double): @DomainRelative Double

  /**
   * Converts a domain relative value back to a domain value
   */
  abstract fun toDomain(domainRelative: @DomainRelative Double): @Domain Double

  /**
   * Returns the base for the value range.
   *
   * The base is defined as the value as close to 0.0 as possible but within the value range.
   *
   * * If the value range contains 0.0, then 0.0 is returned.
   * * If the complete value range is above 0.0, the smaller value (`start`) is returned
   * * If the complete value range is below 0.0, the larger value (`end`) is returned
   *
   *
   * ## Usage
   * The base is for example useful when painting bar charts. The bars start at the baseline and are painted to the value.
   */
  fun base(): @Domain Double {
    //Keep as function to indicate that this method does some calculations
    return when {
      end <= 0.0 -> end
      start >= 0.0 -> start
      else -> 0.0
    }
  }

  /**
   * Returns the center value
   */
  fun center(): @Domain Double {
    //Keep as function to indicate that this method does some calculations
    return (start + end) / 2.0
  }

  val isLinear: Boolean
    get() {
      return this is LinearValueRange
    }

  val isLogarithmic: Boolean
    get() {
      return this is LogarithmicValueRange
    }

  override fun toString(): String {
    return "[$start, $end]"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as ValueRange

    if (start != other.start) return false
    if (end != other.end) return false

    return true
  }

  override fun hashCode(): Int {
    var result = start.hashCode()
    result = 31 * result + end.hashCode()
    return result
  }

  companion object {
    /**
     * Creates a linear value range
     */
    @JvmStatic
    fun linear(
      start: @Domain Double,
      end: @Domain Double
    ): LinearValueRange {
      return LinearValueRange(start, end)
    }

    @JvmStatic
    fun logarithmic(
      start: @Domain Double,
      end: @Domain Double
    ): LogarithmicValueRange {
      return LogarithmicValueRange(start, end)
    }

    /**
     * Value range between 0.0 and 1.0
     */
    @JvmField
    val percentage: LinearValueRange = linear(0.0, 1.0)

    /**
     * Between 0 and 360 degree
     */
    @JvmField
    val degrees: LinearValueRange = linear(0.0, 360.0)

    /**
     * A default value range from 0..100
     */
    @JvmStatic
    val default: LinearValueRange = linear(0.0, 100.0)

    /**
     * Default value range for boolean values (0..1)
     */
    @JvmStatic
    val boolean: LinearValueRange = linear(0.0, 1.0)
  }
}


/**
 * The type of the value range
 */
enum class RangeType {
  /**
   * Only positive values (and zero)
   */
  Positive,

  /**
   * Only negative values (and zero)
   */
  Negative,

  /**
   * Contains positive and negative values
   */
  PositiveAndNegative

}

/**
 * Coerces a double within the value range
 */
fun Double.coerceIn(valueRange: ValueRange): Double {
  return this.coerceIn(valueRange.start, valueRange.end)
}
