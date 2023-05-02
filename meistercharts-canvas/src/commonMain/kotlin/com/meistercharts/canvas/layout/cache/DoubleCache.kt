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
package com.meistercharts.canvas.layout.cache

import it.neckar.open.collections.DoublePredicate
import it.neckar.open.collections.fastAny
import it.neckar.open.collections.fastForEach
import it.neckar.open.collections.fastForEachIndexed
import it.neckar.open.collections.fastForEachIndexedReverse
import it.neckar.open.provider.MultiDoublesProvider
import it.neckar.open.unit.other.Index

/**
 * Caches double values in an array.
 *
 */
class DoubleCache : LayoutVariableWithSize {
  /**
   * Contains the values
   */
  @PublishedApi
  internal var values: DoubleArray = doubleArrayOf()

  override fun reset() {
    values.fastForEachIndexed { index, _ ->
      values[index] = 0.0
    }
  }

  /**
   * Ensures the array has the given size
   */
  override fun ensureSize(size: Int) {
    if (values.size != size) {
      values = DoubleArray(size)
    }
  }

  override val size: Int
    get() = values.size

  /**
   * Sets the given value to all (depending on [size])
   */
  fun setAll(newValue: Double) {
    var n = 0
    val currentSize = values.size
    while (n < currentSize) {
      values[n] = newValue
      n++
    }
  }

  fun getOrElse(index: Int, defaultValue: MultiDoublesProvider<Index>): Double {
    return if (index >= 0 && index <= values.lastIndex) get(index) else defaultValue.valueAt(index)
  }

  fun lastOr(fallback: Double): Double {
    if (values.isEmpty()) {
      return fallback
    }

    return values.last()
  }

  inline fun lastOrNaN(): Double {
    return lastOr(Double.NaN)
  }

  operator fun set(index: Int, value: Double) {
    values[index] = value
  }

  operator fun get(index: Int): Double {
    return values[index]
  }

  inline fun fastForEachIndexed(callback: (index: Int, value: Double) -> Unit) {
    this.values.fastForEachIndexed(callback)
  }

  inline fun fastForEachIndexedReverse(callback: (index: Int, value: Double, isFirst: Boolean) -> Unit) {
    this.values.fastForEachIndexedReverse(callback)
  }

  inline fun fastForEach(callback: (value: Double) -> Unit) {
    this.values.fastForEach(callback)
  }

  override fun toString(): String {
    return values.contentToString()
  }

  inline fun fastAny(predicate: DoublePredicate): Boolean {
    return values.fastAny(predicate)
  }
}
