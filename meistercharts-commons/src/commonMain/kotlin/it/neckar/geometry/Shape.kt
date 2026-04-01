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

import it.neckar.open.unit.number.MayBeNegative
import it.neckar.open.unit.si.rad
import kotlin.math.tan

/**
 * Represents a shape.
 *
 * The shape has a size that starts at a given location. The size may be negative.
 *
 * Therefore, the upper left corner of the shape is defined as:
 * * the location if the size is positive
 * * location - size if the size is negative
 */
interface Shape : Box {
  /**
   * The location of the shape
   */
  val location: Coordinates

  @MayBeNegative
  val size: Size

  override fun getX(): Double = location.x

  override fun getY(): Double = location.y

  /**
   * The width which might be negative
   */
  @MayBeNegative
  override fun getWidth(): Double = size.width

  /**
   * The height which might be negative
   */
  @MayBeNegative
  override fun getHeight(): Double = size.height

  fun vertices(): List<Coordinates>

  fun contains(coordinates: Coordinates): Boolean {
    return contains(coordinates.x, coordinates.y)
  }

  fun contains(targetX: Double, targetY: Double): Boolean {
    return containsX(targetX) && containsY(targetY)
  }

  fun containsX(targetX: Double): Boolean = targetX in left..right

  fun containsY(targetY: Double): Boolean = targetY in top..bottom

  /**
   * Returns the x value for the given horizontal alignment
   */
  fun x(alignment: HorizontalAlignment): Double {
    return when (alignment) {
      HorizontalAlignment.Left -> left
      HorizontalAlignment.Right -> right
      HorizontalAlignment.Center -> centerX
    }
  }

  fun y(alignment: VerticalAlignment): Double {
    return when (alignment) {
      VerticalAlignment.Top -> top
      VerticalAlignment.Center -> centerY
      VerticalAlignment.Baseline -> centerY //TODO(?)
      VerticalAlignment.Bottom -> bottom
    }
  }

  /**
   * Moves the rectangle
   */
  fun move(deltaX: Double, deltaY: Double): Shape

  fun move(distance: Distance): Shape {
    return move(distance.x, distance.y)
  }

  /**
   * Returns the x value (relative to the rect) for this rectangle when following the given angle from origin.
   *
   * This method calculates the intersection (x value) between this rectangle and an (imaginary) line from origin 0/0 with the given angle.
   */
  fun xFromRadRelative(theta: @rad Double): Double {
    return if (PolarCoordinates.isToTheTop(theta)) {
      top / tan(theta)
    } else {
      bottom / tan(theta)
    }.coerceIn(left, right)
  }

  /**
   * Returns the y value (relative to the rect) for this rectangle box when following the given angle from origin
   *
   * This method calculates the intersection (y value) between this rectangle and an (imaginary) line from origin 0/0 with the given angle.
   */
  fun yFromRadRelative(theta: @rad Double): Double {
    return if (PolarCoordinates.isToTheRight(theta)) {
      (tan(theta) * right)
    } else {
      tan(theta) * left
    }.coerceIn(top, bottom)
  }

  fun withX(newX: Double): Shape

  fun withY(newY: Double): Shape

  /**
   * Creates a [Rectangle] from this [Rectangle] with width [newWidth]
   */
  fun withWidth(newWidth: Double): Shape

  /**
   * Creates a [Rectangle] from this [Rectangle] with height [newHeight]
   */
  fun withHeight(newHeight: Double): Shape

  /**
   * Returns a new rectangle object with the given location - but the same height/width
   */
  fun withLocation(location: Coordinates): Shape

  /**
   * Returns a new rectangle that has been extended with the given values
   */
  fun expand(left: Double = 0.0, top: Double = 0.0, right: Double = 0.0, bottom: Double = 0.0): Shape

  /**
   * Returns true if this rectangle overlaps with another rectangle
   */
  fun overlaps(other: Shape): Boolean {
    return !doesNotOverlap(other)
  }

  fun doesNotOverlap(other: Shape): Boolean {
    return doesNotOverlapBox(other)
  }
}
