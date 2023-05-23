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
import com.meistercharts.annotations.Zoomed
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Rectangle
import com.meistercharts.model.withinSized
import it.neckar.open.kotlin.lang.betweenInclusive
import it.neckar.open.unit.number.MayBeNegative

/**
 * Caches bounds (x,y,width,height) without creating any objects
 *
 * Must only be used for layout objects in layers.
 *
 * Is *NOT* thread safe!
 *
 * NOTE: For a single bounds object use [BoundsCache]
 */
open class BoundsLayoutCache : LayoutVariableWithSize {
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

  /**
   * The widths
   */
  @PublishedApi
  internal var widthValues: @Zoomed @MayBeNegative DoubleCache = DoubleCache()

  /**
   * The heights
   */
  @PublishedApi
  internal var heightValues: @Zoomed @MayBeNegative DoubleCache = DoubleCache()

  override fun reset() {
    xValues.reset()
    yValues.reset()
    widthValues.reset()
    heightValues.reset()
  }

  /**
   * Ensures all elements have the given size
   * Recreates the arrays if necessary
   */
  override fun ensureSize(size: Int) {
    xValues.ensureSize(size)
    yValues.ensureSize(size)
    widthValues.ensureSize(size)
    heightValues.ensureSize(size)
  }

  override val size: Int
    get() = xValues.size

  /**
   * Sets the location for the given index
   */
  fun location(index: Int, x: @Window Double, y: @Window Double) {
    x(index, x)
    y(index, y)
  }

  /**
   * Sets the size for the given index
   */
  fun size(index: Int, width: @Zoomed Double, height: @Zoomed Double) {
    width(index, width)
    height(index, height)
  }

  /**
   * Sets the interpreting the given coordinates as *center* values
   */
  fun centered(
    index: Int,
    centerX: @Window Double,
    centerY: @Window Double,
    width: @Zoomed Double,
    height: @Zoomed Double
  ) {
    x(index, centerX - width / 2.0)
    y(index, centerY - height / 2.0)
    width(index, width)
    height(index, height)
  }


  /**
   * Sets the x value for the given index
   */
  fun x(index: Int, value: @Window Double) {
    this.xValues[index] = value
  }

  /**
   * Returns the x value for the given index
   */
  fun x(index: Int): @Window Double {
    return this.xValues[index]
  }

  /**
   * Sets the y value for the given index
   */
  fun y(index: Int, value: @Window Double) {
    this.yValues[index] = value
  }

  /**
   * Returns the y value for the given index
   */
  fun y(index: Int): @Window Double {
    return this.yValues[index]
  }

  /**
   * Sets the width value for the given index
   */
  fun width(index: Int, value: @Zoomed Double) {
    this.widthValues[index] = value
  }

  /**
   * Returns the width value for the given index
   */
  fun width(index: Int): @Zoomed Double {
    return this.widthValues[index]
  }

  /**
   * Sets the height value for the given index
   */
  fun height(index: Int, value: @Zoomed Double) {
    this.heightValues[index] = value
  }

  /**
   * Returns the height value for the given index
   */
  fun height(index: Int): @Zoomed Double {
    return this.heightValues[index]
  }

  /**
   * Sets all values from the provided bounds
   */
  operator fun set(index: Int, boundingBox: @Zoomed Rectangle) {
    x(index, boundingBox.getX())
    y(index, boundingBox.getY())
    width(index, boundingBox.getWidth())
    height(index, boundingBox.getHeight())
  }

  /**
   * Iterates over all elements
   */
  inline fun fastForEachIndexed(
    action: (
      index: Int, x: @Window Double, y: @Window Double, width: @MayBeNegative @Zoomed Double, height: @MayBeNegative @Zoomed Double,
    ) -> Unit,
  ) {

    xValues.fastForEachIndexed { index, x ->
      val y = yValues[index]
      val width = widthValues[index]
      val height = heightValues[index]

      action(index, x, y, width, height)
    }
  }

  /**
   * Iterates over all elements
   */
  inline fun fastForEachIndexedReverse(
    action: (
      index: Int, x: @Window Double, y: @Window Double, width: @MayBeNegative @Zoomed Double, height: @MayBeNegative @Zoomed Double
    ) -> Unit
  ) {

    xValues.fastForEachIndexedReversed { index, x ->
      val y = yValues[index]
      val width = widthValues[index]
      val height = heightValues[index]

      action(index, x, y, width, height)
    }
  }

  /**
   * Returns the index
   */
  fun findIndex(location: Coordinates, matcher: (Int) -> Boolean = { true }): Int? {
    return findIndex(location.x, location.y, matcher)
  }

  /**
   * Returns the index for the bounds that contain the given coordinates.
   */
  open fun findIndex(locationX: Double, locationY: Double, matcher: (Int) -> Boolean = { true }): Int? {
    fastForEachIndexed { index, x, y, width: @MayBeNegative Double, height: @MayBeNegative Double ->
      if (locationX.betweenInclusive(x, x + width)
        && locationY.betweenInclusive(y, y + height)
        && matcher(index)
      ) {
        return index
      }
    }

    return null
  }

  /**
   * Returns the last index for the bounds that contain the given coordinates
   */
  fun findLastIndex(location: Coordinates, matcher: (Int) -> Boolean = { true }): Int? {
    return findLastIndex(location.x, location.y, matcher)
  }

  /**
   * Returns the last index for the bounds that contain the given coordinates.
   * Finds the *last* elements that fits
   *
   * Only elements the given [matcher] returns true for are considered
   */
  open fun findLastIndex(locationX: Double, locationY: Double, matcher: (Int) -> Boolean = { true }): Int? {
    fastForEachIndexedReverse { index, x, y, width: @MayBeNegative Double, height: @MayBeNegative Double ->
      if (locationX.betweenInclusive(x, x + width)
        && locationY.betweenInclusive(y, y + height)
        && matcher(index)
      ) {
        return index
      }
    }

    return null
  }

  /**
   * Returns a (new instance) rectangle representing the bounds for the given index
   *
   * Attention: A new object is instantiated every time this method is called
   */
  fun asRect(index: Int): Rectangle {
    return Rectangle(
      x(index),
      y(index),
      width(index),
      height(index)
    )
  }

  /**
   * Returns true if the provided coordinates are within the bounds
   */
  fun contains(index: Int, location: Coordinates): Boolean {
    return location.withinSized(
      startX = x(index),
      startY = y(index),
      width = width(index),
      height = height(index),
    )
  }
}
