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
import it.neckar.geometry.RightTriangleType
import it.neckar.open.kotlin.lang.betweenInclusive
import it.neckar.open.kotlin.lang.pointIsLeftOfLine
import it.neckar.open.unit.number.MayBeNegative

class TriangleBoundsLayoutCache : BoundsLayoutCache() {
  /**
   * The x locations
   */
  @PublishedApi
  internal var rightTriangleTypeValues: @Window RightTriangleTypeCache = RightTriangleTypeCache()

  override fun reset() {
    super.reset()
    rightTriangleTypeValues.reset()
  }

  /**
   * Ensures all elements have the given size
   * Recreates the arrays if necessary
   */
  override fun ensureSize(size: Int) {
    super.ensureSize(size)
    rightTriangleTypeValues.ensureSize(size)
  }

  /**
   * Sets the rightTriangleType value for the given index
   */
  fun rightTriangleType(index: Int, rightTriangleType: RightTriangleType) {
    this.rightTriangleTypeValues[index] = rightTriangleType
  }

  /**
   * Returns the rightTriangleType value for the given index
   */
  fun rightTriangleType(index: Int): RightTriangleType? {
    return this.rightTriangleTypeValues[index]
  }

  /**
   * Iterates over all elements
   */
  inline fun fastForEachIndexed(
    action: (
      index: Int, x: @Window Double, y: @Window Double, width: @MayBeNegative @Zoomed Double, height: @MayBeNegative @Zoomed Double, rightTriangleType: RightTriangleType?
    ) -> Unit
  ) {

    xValues.fastForEachIndexed { index, x ->
      val y = yValues[index]
      val width = widthValues[index]
      val height = heightValues[index]
      val rightTriangleType = rightTriangleTypeValues[index]

      action(index, x, y, width, height, rightTriangleType)
    }
  }

  /**
   * Iterates over all elements
   */
  inline fun fastForEachIndexedReverse(
    action: (
      index: Int, x: @Window Double, y: @Window Double, width: @MayBeNegative @Zoomed Double, height: @MayBeNegative @Zoomed Double, rightTriangleType: RightTriangleType?
    ) -> Unit
  ) {

    xValues.fastForEachIndexedReversed { index, x ->
      val y = yValues[index]
      val width = widthValues[index]
      val height = heightValues[index]
      val rightTriangleType = rightTriangleTypeValues[index]

      action(index, x, y, width, height, rightTriangleType)
    }
  }

  /**
   * Returns the index for the bounds that contain the given coordinates.
   */
  override fun findIndex(locationX: Double, locationY: Double, matcher: (Int) -> Boolean): Int? {
    fastForEachIndexed { index, x, y, width: @MayBeNegative Double, height: @MayBeNegative Double, rightTriangleType: RightTriangleType? ->
      if (locationX.betweenInclusive(x, x + width)
        && locationY.betweenInclusive(y, y + height)
        && rightTriangleType?.doesOverlap(locationX, locationY, x, y, width, height) == true
        && matcher(index)
      ) {
        return index
      }
    }

    return null
  }

  /**
   * Returns the last index for the bounds that contain the given coordinates.
   * Finds the *last* elements that fits
   *
   * Only elements the given [matcher] returns true for are considered
   */
  override fun findLastIndex(locationX: Double, locationY: Double, matcher: (Int) -> Boolean): Int? {
    fastForEachIndexedReverse { index, x, y, width: @MayBeNegative Double, height: @MayBeNegative Double, rightTriangleType: RightTriangleType? ->
      if (locationX.betweenInclusive(x, x + width)
        && locationY.betweenInclusive(y, y + height)
        && rightTriangleType?.doesOverlap(locationX, locationY, x, y, width, height) == true
        && matcher(index)
      ) {
        return index
      }
    }

    return null
  }

  private fun RightTriangleType.doesOverlap(locationX: Double, locationY: Double, x: Double, y: Double, width: @MayBeNegative Double, height: @MayBeNegative Double): Boolean {
    val right = x + width
    val top = y
    val left = x
    val bottom = y + height
    /**
     * Check for each point of the other shape that it lies "left" of the triangle's hypotenuse
     * We already know that at this point of the algorithm, the other shape is outside the triangle's bounding rectangle
     * This basically means that, if all the other shape's points are on the same side of the hypotenuse that is "outside" of the triangle, they do not overlap
     * In this case, if one point is "inside" the triangle, there is a collision
     */
    when (this) {
      /**
       * Calculate with the start and end point for the hypotenuse for this triangle
       * Top and Bottom are switched as the planner defines y=0 as the bottom of the screen, not the top
       */
      RightTriangleType.MissingCornerInFirstQuadrant -> if (pointIsLeftOfLine(right, top, left, bottom, locationX, locationY)) return false
      RightTriangleType.MissingCornerInSecondQuadrant -> if (pointIsLeftOfLine(left, top, right, bottom, locationX, locationY)) return false
      RightTriangleType.MissingCornerInThirdQuadrant -> if (pointIsLeftOfLine(left, bottom, right, top, locationX, locationY)) return false
      RightTriangleType.MissingCornerInFourthQuadrant -> if (pointIsLeftOfLine(right, bottom, left, top, locationX, locationY)) return false
    }

    return true
  }
}
