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
import it.neckar.open.collections.fastForEachIndexedReversed
import it.neckar.open.unit.other.px

/**
 * A growable list of area-band points without creating any objects: an x location with two y values
 * (e.g. the upper and lower line of an area between two lines).
 * The growable sibling of [CoordinatesArrayList] for (x, y1, y2) triples.
 *
 * Is *NOT* thread safe!
 */
class AreaBandArrayList(initialCapacity: Int = 10) {
  /**
   * The x locations
   */
  @PublishedApi
  internal val xValues: DoubleArrayList = DoubleArrayList(initialCapacity)

  /**
   * The first y values (e.g. the upper line)
   */
  @PublishedApi
  internal val y1Values: DoubleArrayList = DoubleArrayList(initialCapacity)

  /**
   * The second y values (e.g. the lower line)
   */
  @PublishedApi
  internal val y2Values: DoubleArrayList = DoubleArrayList(initialCapacity)

  /**
   * The number of band points
   */
  val size: Int
    get() = xValues.size

  fun isEmpty(): Boolean {
    return xValues.isEmpty()
  }

  /**
   * Removes all band points - the capacity is retained
   */
  fun clear() {
    xValues.clear()
    y1Values.clear()
    y2Values.clear()
  }

  /**
   * Appends the band point
   */
  fun add(x: @px Double, y1: @px Double, y2: @px Double) {
    xValues.add(x)
    y1Values.add(y1)
    y2Values.add(y2)
  }

  /**
   * Returns the x value of the band point at the given index
   */
  fun xAt(index: Int): @px Double {
    return xValues[index]
  }

  /**
   * Returns the first y value of the band point at the given index
   */
  fun y1At(index: Int): @px Double {
    return y1Values[index]
  }

  /**
   * Returns the second y value of the band point at the given index
   */
  fun y2At(index: Int): @px Double {
    return y2Values[index]
  }

  /**
   * Iterates over all band points
   */
  inline fun fastForEachIndexed(action: (index: Int, x: @px Double, y1: @px Double, y2: @px Double) -> Unit) {
    xValues.fastForEachIndexed { index, x ->
      action(index, x, y1Values[index], y2Values[index])
    }
  }

  /**
   * Iterates over all band points - in reverse order
   */
  inline fun fastForEachIndexedReversed(action: (index: Int, x: @px Double, y1: @px Double, y2: @px Double) -> Unit) {
    xValues.fastForEachIndexedReversed { index, x ->
      action(index, x, y1Values[index], y2Values[index])
    }
  }
}
