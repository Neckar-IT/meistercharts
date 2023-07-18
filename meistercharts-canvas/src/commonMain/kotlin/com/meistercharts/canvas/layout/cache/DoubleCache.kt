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

import it.neckar.open.collections.DoubleArrayList
import it.neckar.open.collections.DoublePredicate
import it.neckar.open.collections.IterationOrder
import it.neckar.open.collections.fastAny
import it.neckar.open.collections.fastForEach
import it.neckar.open.collections.fastForEachIndexed
import it.neckar.open.collections.fastForEachIndexedReversed
import it.neckar.open.collections.last
import it.neckar.open.collections.lastIndex
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
  internal var values: DoubleArrayList = DoubleArrayList()

  override val size: Int
    get() = values.size

  @Deprecated("Use prepare instead")
  override fun reset() {
    values.fastForEachIndexed { index, _ ->
      values[index] = 0.0
    }
  }

  /**
   * Ensures the array has the given size
   */
  @Deprecated("Use prepare instead")
  override fun ensureSize(size: Int) {
    values.size = size
  }

  fun resize(newSize: Int) {
    values.size = newSize
  }

  /**
   * Sets the given value to all (depending on [size])
   */
  fun setAll(newValue: Double) {
    var n = 0
    val currentSize = size
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

  inline fun fastForEachIndexed(iterationOrder: IterationOrder, callback: (index: Int, value: Double) -> Unit) {
    this.values.fastForEachIndexed(iterationOrder, callback)
  }

  inline fun fastForEachIndexedReversed(callback: (index: Int, value: Double) -> Unit) {
    this.values.fastForEachIndexedReversed(callback)
  }

  inline fun fastForEach(callback: (value: Double) -> Unit) {
    this.values.fastForEach(callback)
  }

  override fun toString(): String {
    return values.data.contentToString()
  }

  inline fun fastAny(predicate: DoublePredicate): Boolean {
    return values.fastAny(predicate)
  }
}
