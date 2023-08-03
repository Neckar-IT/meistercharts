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
package it.neckar.geometry

import it.neckar.open.kotlin.lang.isNegative
import it.neckar.open.unit.number.MayBeNegative
import it.neckar.open.unit.other.pct
import kotlin.math.absoluteValue

/**
 * Describes a rectangle. This can be interpreted as bounding box for other shapes.
 * See [Shape]
 */
interface Box {
  @MayBeNegative
  fun getX(): Double

  @MayBeNegative
  fun getY(): Double

  @MayBeNegative
  fun getWidth(): Double

  @MayBeNegative
  fun getHeight(): Double

  /**
   * Returns the right coordinate value of the rect (has the same value as x + width, or x if width is negative.)
   */
  val right: Double
    get() {
      if (getWidth().isNegative()) {
        return getX()
      }

      return getX() + getWidth()
    }


  /**
   * Returns the left coordinate value of the rect (has the same value as x, or x + width if width is negative.)
   */
  val left: Double
    get() {
      if (getWidth().isNegative()) {
        return getX() + getWidth()
      }

      return getX()
    }

  /**
   * Returns the top coordinate value of the rect (has the same value as y, or y + height if height is negative.)
   */
  val top: Double
    get() {
      if (getHeight().isNegative()) {
        return getY() + getHeight()
      }

      return getY()
    }

  /**
   * Returns the bottom coordinate value of the rect (has the same value as y + height, or y if height is negative.)
   */
  val bottom: Double
    get() {
      if (getHeight().isNegative()) {
        return getY()
      }

      return getY() + getHeight()
    }

  /**
   * Returns the absolute width
   */
  val widthAbs: Double
    get() {
      return getWidth().absoluteValue
    }

  /**
   * Returns the absolute height
   */
  val heightAbs: Double
    get() {
      return getHeight().absoluteValue
    }

  /**
   * Returns the center
   */
  val centerX: Double
    get() {
      return getX() + getWidth() / 2.0
    }

  /**
   * Returns the center
   */
  val centerY: Double
    get() {
      return getY() + getHeight() / 2.0
    }

  /**
   * Returns the upper left corner
   */
  fun topLeft(): Coordinates {
    return Coordinates(left, top)
  }

  fun topRight(): Coordinates {
    return Coordinates(right, top)
  }

  fun bottomLeft(): Coordinates {
    return Coordinates(left, bottom)
  }

  fun bottomRight(): Coordinates {
    return Coordinates(right, bottom)
  }

  /**
   * Returns the coordinates for the given direction.
   * Top left returns 0.0/0.0
   * Bottom right: right/bottom
   */
  fun findCoordinates(direction: Direction): Coordinates {
    val x = when (direction.horizontalAlignment) {
      HorizontalAlignment.Left -> left
      HorizontalAlignment.Right -> right
      HorizontalAlignment.Center -> centerX
    }

    val y = when (direction.verticalAlignment) {
      VerticalAlignment.Top -> top
      VerticalAlignment.Center -> centerY
      VerticalAlignment.Baseline -> centerY
      VerticalAlignment.Bottom -> bottom
    }

    return Coordinates.of(x, y)
  }

  /**
   * Returns true if this box overlaps with another box
   */
  fun overlapsBox(other: Box): Boolean {
    return !doesNotOverlapBox(other)
  }

  fun doesNotOverlapBox(other: Box): Boolean {
    if (right < other.left) {
      return true
    }
    if (left > other.right) {
      return true
    }
    if (bottom < other.top) {
      return true
    }
    if (top > other.bottom) {
      return true
    }

    return false
  }


  /**
   * Returns the coordinates using relative values for x and y
   */
  fun findCoordinatesRelative(xPercentage: @pct Double, yPercentage: @pct Double): Coordinates {
    return Coordinates.of(findCoordinatesRelativeX(xPercentage), findCoordinatesRelativeY(yPercentage))
  }

  fun findCoordinatesRelativeX(xPercentage: @pct Double): Double {
    return left + getWidth() * xPercentage
  }

  fun findCoordinatesRelativeY(yPercentage: @pct Double): Double {
    return top + getHeight() * yPercentage
  }
}
