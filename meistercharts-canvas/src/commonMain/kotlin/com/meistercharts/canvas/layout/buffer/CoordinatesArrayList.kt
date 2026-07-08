/*
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
package com.meistercharts.canvas.layout.buffer

import it.neckar.open.collections.DoubleArrayList
import it.neckar.open.collections.fastForEachIndexed
import it.neckar.open.collections.last
import it.neckar.open.unit.other.px

/**
 * A growable list of coordinates (x,y) without creating any objects.
 * The growable counterpart to [CoordinatesMultiBuffer] (which is prepare/set based): points are appended with [add] and removed with [clear].
 *
 * Is *NOT* thread safe!
 */
class CoordinatesArrayList(initialCapacity: Int = 10) {
  /**
   * The x locations
   */
  @PublishedApi
  internal val xValues: DoubleArrayList = DoubleArrayList(initialCapacity)

  /**
   * The y locations
   */
  @PublishedApi
  internal val yValues: DoubleArrayList = DoubleArrayList(initialCapacity)

  /**
   * The number of points
   */
  val size: Int
    get() = xValues.size

  fun isEmpty(): Boolean {
    return xValues.isEmpty()
  }

  /**
   * Removes all points - the capacity is retained
   */
  fun clear() {
    xValues.clear()
    yValues.clear()
  }

  /**
   * Appends the point
   */
  fun add(x: @px Double, y: @px Double) {
    xValues.add(x)
    yValues.add(y)
  }

  /**
   * Returns the x value of the point at the given index
   */
  fun xAt(index: Int): @px Double {
    return xValues[index]
  }

  /**
   * Returns the y value of the point at the given index
   */
  fun yAt(index: Int): @px Double {
    return yValues[index]
  }

  /**
   * Returns the x value of the last point - throws if the list is empty
   */
  fun lastX(): @px Double {
    return xValues.last()
  }

  /**
   * Returns the y value of the last point - throws if the list is empty
   */
  fun lastY(): @px Double {
    return yValues.last()
  }

  /**
   * Iterates over all points
   */
  inline fun fastForEachIndexed(action: (index: Int, x: @px Double, y: @px Double) -> Unit) {
    xValues.fastForEachIndexed { index, x ->
      action(index, x, yValues[index])
    }
  }

  /**
   * Returns a copy of the x values - sized exactly to [size] (no capacity slack)
   */
  fun toXDoubleArray(): DoubleArray {
    return xValues.toDoubleArray()
  }

  /**
   * Returns a copy of the y values - sized exactly to [size] (no capacity slack)
   */
  fun toYDoubleArray(): DoubleArray {
    return yValues.toDoubleArray()
  }
}
