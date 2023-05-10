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

import it.neckar.open.collections.fastForEach
import it.neckar.open.collections.fastForEachIndexed

/**
 * Contains a list of int.
 * This can be used to keep a sorted list of indices
 */
class IntListCache : LayoutVariableWithSize {
  /**
   * Contains the values
   *
   * Use this only for performance reasons.
   * Altering this instance alters the cache, too.
   */
  @PublishedApi
  internal var values: MutableList<Int> = mutableListOf()
    private set

  override fun reset() {
    values.forEachIndexed { index, _ ->
      values[index] = 0
    }
  }

  /**
   * Ensures the array has the given size
   */
  override fun ensureSize(size: Int) {
    if (values.size != size) {
      while (values.size < size) {
        values.add(0)
      }
    }
  }

  override val size: Int
    get() = values.size

  operator fun set(index: Int, value: Int) {
    values[index] = value
  }

  /**
   * Returns the n-th index
   */
  operator fun get(index: Int): Int {
    return values[index]
  }

  inline fun fastForEachIndexed(callback: (index: Int, value: Int) -> Unit) {
    this.values.fastForEachIndexed(callback)
  }

  inline fun fastForEach(callback: (value: Int) -> Unit) {
    this.values.fastForEach(callback)
  }

  /**
   * Sorts the list inline
   */
  inline fun <R : Comparable<R>> sortBy(crossinline selector: (Int) -> R?) {
    values.sortBy(selector)
  }
}
