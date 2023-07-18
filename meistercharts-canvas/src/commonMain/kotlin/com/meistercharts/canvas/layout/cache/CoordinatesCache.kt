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

import com.meistercharts.annotations.Window
import it.neckar.open.collections.IterationOrder
import it.neckar.open.provider.CoordinatesProvider
import it.neckar.open.provider.CoordinatesProvider1

/**
 * Caches coordinates (x,y) without creating any objects
 *
 * Must only be used for layout objects in layers.
 *
 * Is *NOT* thread safe!
 */
class CoordinatesCache : LayoutVariableWithSize {
  /**
   * The x locations
   */
  @PublishedApi
  internal var xValues: @Window DoubleCache = DoubleCache()

  /**
   * The y locations
   */
  @PublishedApi
  internal var yValues: @Window DoubleCache = DoubleCache()

  @Deprecated("Use prepare instead")
  override fun reset() {
    xValues.reset()
    yValues.reset()
  }

  /**
   * Ensures all elements have the given size
   * Recreates the arrays if necessary.
   *
   * ATTENTION: Might lose the content!
   */
  @Deprecated("Use prepare instead")
  override fun ensureSize(size: Int) {
    xValues.ensureSize(size)
    yValues.ensureSize(size)
  }

  override val size: Int
    get() = xValues.size

  /**
   * Sets the x and y values for the given index
   */
  fun set(index: Int, x: Double, y: Double) {
    this.xValues[index] = x
    this.yValues[index] = y
  }

  /**
   * Increases the size by one and adds the new point
   */
  fun add(x: Double, y: Double) {
    val newSize = size + 1
    @Suppress("DEPRECATION")
    ensureSize(newSize)

    set(newSize - 1, x, y)
  }

  /**
   * Sets the x value for the given index
   */
  fun x(index: Int, value: Double) {
    this.xValues[index] = value
  }

  /**
   * Returns the x value for the given index
   */
  fun x(index: Int): Double {
    return this.xValues[index]
  }

  /**
   * Sets the y value for the given index
   */
  fun y(index: Int, value: Double) {
    this.yValues[index] = value
  }

  /**
   * Returns the y value for the given index
   */
  fun y(index: Int): Double {
    return this.yValues[index]
  }

  /**
   * Iterates over all elements
   */
  inline fun fastForEachIndexed(
    action: (
      index: Int, x: @Window Double, y: @Window Double,
    ) -> Unit,
  ) {

    return fastForEachIndexed(iterationOrder = IterationOrder.Ascending, action = action)
  }

  inline fun fastForEachIndexedReversed(
    action: (
      index: Int, x: @Window Double, y: @Window Double,
    ) -> Unit,
  ) {

    return fastForEachIndexed(iterationOrder = IterationOrder.Descending, action = action)
  }

  inline fun fastForEachIndexed(
    iterationOrder: IterationOrder,
    action: (
      index: Int, x: @Window Double, y: @Window Double,
    ) -> Unit,
  ) {

    xValues.fastForEachIndexed(iterationOrder) { index, x ->
      val y = yValues[index]

      action(index, x, y)
    }
  }

  /**
   * Creates a new coordinates provider that uses the values from the cache
   */
  fun asCoordinatesProvider(): @Window CoordinatesProvider {
    return object : CoordinatesProvider {
      override fun size(): Int {
        return this@CoordinatesCache.size
      }

      override fun xAt(index: Int): Double {
        return this@CoordinatesCache.x(index)
      }

      override fun yAt(index: Int): Double {
        return this@CoordinatesCache.y(index)
      }
    }
  }

  fun asCoordinatesProvider1(): @Window CoordinatesProvider1<Any> {
    return asCoordinatesProvider().as1()
  }
}
