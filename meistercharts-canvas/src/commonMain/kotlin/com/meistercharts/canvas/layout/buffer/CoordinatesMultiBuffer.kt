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

import com.meistercharts.annotations.Window
import com.meistercharts.canvas.CanvasRenderingContext
import it.neckar.geometry.Coordinates
import it.neckar.open.collections.IterationOrder
import it.neckar.open.collections.fastForEachWithIndex
import it.neckar.open.provider.CoordinatesProvider
import it.neckar.open.provider.CoordinatesProvider1

/**
 * Buffers coordinates (x,y) without creating any objects
 *
 * Used for layout objects in layers and as reusable painting buffer (e.g. [com.meistercharts.algorithms.painter.Path]).
 *
 * Is *NOT* thread safe!
 */
class CoordinatesMultiBuffer : LayoutVariableWithSize {
  /**
   * The x locations
   */
  @PublishedApi
  internal var xValues: @Window DoubleMultiBuffer = DoubleMultiBuffer()

  /**
   * The y locations
   */
  @PublishedApi
  internal var yValues: @Window DoubleMultiBuffer = DoubleMultiBuffer()

  override fun reset() {
    xValues.reset()
    yValues.reset()
  }

  /**
   * Grows all elements to the given size, leaving the values undefined.
   *
   * ATTENTION: Might lose the content!
   */
  override fun resize(size: Int) {
    xValues.resize(size)
    yValues.resize(size)
  }

  override val size: Int
    get() = xValues.size

  /**
   * Sets the x and y values for the given index
   */
  fun set(index: Int, x: Double, y: Double) {
    verifyIndex(index)
    this.xValues[index] = x
    this.yValues[index] = y
  }

  /**
   * Increases the size by one and adds the new point
   */
  fun add(x: Double, y: Double) {
    val newSize = size + 1
    resize(newSize)

    set(newSize - 1, x, y)
  }

  /**
   * Increases the size by two and adds both points
   */
  fun add(x1: @Window Double, y1: @Window Double, x2: @Window Double, y2: @Window Double) {
    val startIndex = size
    resize(startIndex + 2)

    set(startIndex, x1, y1)
    set(startIndex + 1, x2, y2)
  }

  /**
   * Increases the size by three and adds all three points
   */
  fun add(x1: @Window Double, y1: @Window Double, x2: @Window Double, y2: @Window Double, x3: @Window Double, y3: @Window Double) {
    val startIndex = size
    resize(startIndex + 3)

    set(startIndex, x1, y1)
    set(startIndex + 1, x2, y2)
    set(startIndex + 2, x3, y3)
  }

  /**
   * Returns the x value of the last point - or [Double.NaN] if the buffer is empty
   */
  fun lastXOrNaN(): @Window Double {
    return xValues.lastOrNaN()
  }

  /**
   * Returns the y value of the last point - or [Double.NaN] if the buffer is empty
   */
  fun lastYOrNaN(): @Window Double {
    return yValues.lastOrNaN()
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
   * Sets all points to the buffer. Automatically prepares the buffer to the size of the given elements.
   *
   * ATTENTION: Do *not* create new instances of [Coordinates] to be able to call this method
   */
  fun setAll(elements: List<Coordinates>) {
    prepare(elements.size)
    elements.fastForEachWithIndex { index, coordinates ->
      set(index, coordinates.x, coordinates.y)
    }
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
   * Creates a new coordinates provider that uses the values from the buffer
   */
  fun asCoordinatesProvider(): @Window CoordinatesProvider {
    return object : CoordinatesProvider {
      override fun size(): Int {
        return this@CoordinatesMultiBuffer.size
      }

      override fun xAt(index: Int): Double {
        return this@CoordinatesMultiBuffer.x(index)
      }

      override fun yAt(index: Int): Double {
        return this@CoordinatesMultiBuffer.y(index)
      }
    }
  }

  fun asCoordinatesProvider1(): @Window CoordinatesProvider1<Any> {
    return asCoordinatesProvider().as1()
  }
}

/**
 * Adds all points to the path by calling [CanvasRenderingContext.lineTo] for each point
 */
fun CanvasRenderingContext.tracePath(coordinates: CoordinatesMultiBuffer) {
  coordinates.fastForEachIndexed { _, x: @Window Double, y: @Window Double ->
    lineTo(x, y)
  }
}
