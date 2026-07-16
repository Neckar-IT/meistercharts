/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package it.neckar.geometry

import it.neckar.open.annotations.Hot
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

  @Hot
  @MayBeNegative
  fun getWidth(): Double

  @Hot
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

  fun center(): Coordinates {
    return Coordinates(centerX, centerY)
  }

  /**
   * Returns the coordinates for the given direction.
   * Top left returns 0.0/0.0
   * Bottom right: right/bottom
   */
  @Hot
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
  @Hot
  fun findCoordinatesRelative(xPercentage: @pct Double, yPercentage: @pct Double): Coordinates {
    return Coordinates.of(findCoordinatesRelativeX(xPercentage), findCoordinatesRelativeY(yPercentage))
  }

  @Hot
  fun findCoordinatesRelativeX(xPercentage: @pct Double): Double {
    return left + getWidth() * xPercentage
  }

  @Hot
  fun findCoordinatesRelativeY(yPercentage: @pct Double): Double {
    return top + getHeight() * yPercentage
  }
}
