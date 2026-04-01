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

import com.meistercharts.annotations.Zoomed
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.number.MayBeNegative
import it.neckar.open.unit.number.Positive
import it.neckar.open.unit.other.pct
import kotlinx.serialization.Serializable
import kotlin.math.abs

/**
 * Represents a rectangle.
 *
 * The rectangle has a size that starts at a given location. The size may be negative.
 * Therefore, the upper left corner of the rectangle is:
 * * the location if the size is positive
 * * location - size if the size is negative
 */
@Serializable
data class Rectangle(
  override val location: Coordinates,
  @MayBeNegative
  override val size: Size,
) : Shape {
  constructor(
    x: Double,
    y: Double,
    width: @MayBeNegative Double,
    height: @MayBeNegative Double,
  ) : this(Coordinates(x, y), Size(width, height))

  val orientation: Orientation
    get() = if (size.width >= size.height) Orientation.Horizontal else Orientation.Vertical

  override fun vertices(): List<Coordinates> {
    return listOf(topRight(), bottomRight(), bottomLeft(), topLeft())
  }

  /**
   * Moves the rectangle
   */
  override fun move(deltaX: Double, deltaY: Double): Rectangle {
    return Rectangle(location.plus(deltaX, deltaY), size)
  }

  override fun move(distance: Distance): Rectangle {
    return move(distance.x, distance.y)
  }

  override fun withX(newX: Double): Rectangle {
    return Rectangle(newX, getY(), getWidth(), getHeight())
  }

  override fun withY(newY: Double): Rectangle {
    return Rectangle(getX(), newY, getWidth(), getHeight())
  }

  /**
   * Creates a [Rectangle] from this [Rectangle] with width [newWidth]
   */
  override fun withWidth(newWidth: Double): Rectangle {
    return Rectangle(getX(), getY(), newWidth, getHeight())
  }

  /**
   * Creates a [Rectangle] from this [Rectangle] with height [newHeight]
   */
  override fun withHeight(newHeight: Double): Rectangle {
    return Rectangle(getX(), getY(), getWidth(), newHeight)
  }

  /**
   * Returns a new rectangle object with the given location - but the same height/width
   */
  override fun withLocation(location: Coordinates): Rectangle {
    if (this.location == location) {
      return this
    }

    return Rectangle(location, size)
  }

  /**
   * Returns a new rectangle that has been extended with the given values
   */
  override fun expand(left: Double, top: Double, right: Double, bottom: Double): Rectangle {
    return Rectangle(getX() - left, getY() - top, getWidth() + left + right, getHeight() + top + bottom)
  }

  fun isFinite(): Boolean {
    return location.isFinite() && size.isFinite()
  }

  /**
   * Returns a new object with the added values
   */
  fun plus(x: Double, y: Double): @Zoomed Rectangle {
    return Rectangle(this.location.plus(x, y), size)
  }

  /**
   * Returns a new object with the added values on each side.
   *
   * If a value of 0.05 percent is provided, the rectangle will be enlarged by 5% on each side.
   * Therefore, it will be 10% wider and 10% higher.
   */
  fun enlarge(additionalSpacePercentageOnEachSide: @pct Double): Rectangle {
    if (additionalSpacePercentageOnEachSide == 0.0) {
      return this
    }

    val additionalX = widthAbs * additionalSpacePercentageOnEachSide
    val additionalY = heightAbs * additionalSpacePercentageOnEachSide

    return enlarge(additionalX, additionalY, additionalX, additionalY)
  }

  /**
   * Enlarges the rectangle by the given values
   */
  fun enlarge(
    left: Double,
    top: Double,
    right: Double,
    bottom: Double,
  ): Rectangle {
    return Rectangle(
      getX() - left,
      getY() - top,
      getWidth() + left + right,
      getHeight() + top + bottom
    )
  }

  /**
   * Coerces the rectangle to fit inside the given [outer] [Rectangle]
   */
  fun coerceInside(outer: Rectangle): Rectangle {
    return Rectangle(
      getX().coerceAtLeast(outer.getX()),
      getY().coerceAtLeast(outer.getY()),
      getWidth().coerceAtMost(outer.getX() - getX() + outer.getWidth()),
      getHeight().coerceAtMost(outer.getY() - getY() + outer.getHeight()),
    )
  }

  companion object {
    fun withLTRB(left: Double, top: Double, right: Double, bottom: Double): Rectangle {
      return Rectangle(left, top, right - left, bottom - top)
    }

    fun fromOrigin(origin: Coordinates, size: Size, direction: Direction): Rectangle {
      return when (direction) {
        Direction.Center -> centered(size)
        Direction.CenterLeft -> centerLeft(size)
        Direction.CenterRight -> centerRight(size)
        Direction.TopLeft -> topLeft(size)
        Direction.TopCenter -> centerTop(size)
        Direction.TopRight -> topRight(size)
        Direction.BottomLeft -> bottomLeft(size)
        Direction.BottomCenter -> centerBottom(size)
        Direction.BottomRight -> bottomRight(size)
        else -> throw IllegalArgumentException("Invalid direction: $direction")
      }.plus(origin.x, origin.y)
    }

    /**
     * Returns a rectangle created from centered X/Y coordinates
     */
    fun fromCenter(centerX: Double, centerY: Double, width: Double, height: Double): Rectangle {
      return Rectangle(centerX - width / 2.0, centerY - height / 2.0, width, height)
    }

    /**
     * Returns a rectangle that has its origin centered with the given width/height
     */
    fun centered(width: Double, height: Double): Rectangle {
      return centered(Size(width, height))
    }

    /**
     * Returns a rectangle that has its origin centered with the given width/height
     */
    fun centered(size: Size): Rectangle {
      return Rectangle(Coordinates.of(-size.width / 2.0, -size.height / 2.0), size)
    }

    /**
     * Creates a new rectangle with its origin set to the bottom right corner
     */
    fun bottomRight(size: Size): Rectangle {
      return Rectangle(Coordinates(-size.width, -size.height), size)
    }

    /**
     * Creates a new rectangle with its origin set to the bottom left corner
     */
    fun bottomLeft(size: Size): Rectangle {
      return Rectangle(Coordinates(0.0, -size.height), size)
    }

    /**
     * Creates a new rectangle with its origin set to the top right corner
     */
    fun topRight(size: Size): Rectangle {
      return Rectangle(Coordinates(-size.width, 0.0), size)
    }

    /**
     * Creates a new rectangle with its origin set to the top right corner.
     */
    fun topLeft(size: Size): Rectangle {
      return Rectangle(Coordinates.origin, size)
    }

    fun centerLeft(width: Double, height: Double): Rectangle {
      return centerLeft(Size(width, height))
    }

    fun centerLeft(size: Size): Rectangle {
      return Rectangle(Coordinates.of(0.0, -size.height / 2.0), size)
    }

    fun centerRight(size: Size): Rectangle {
      return Rectangle(Coordinates.of(-size.width, -size.height / 2.0), size)
    }

    fun centerTop(size: Size): Rectangle {
      return Rectangle(Coordinates.of(-size.width / 2.0, size.height), size)
    }

    fun centerBottom(size: Size): Rectangle {
      return Rectangle(Coordinates.of(-size.width / 2.0, -size.height), size)
    }

    val zero: Rectangle = Rectangle(Coordinates.origin, Size.zero)
    val NaN: @MayBeNaN Rectangle = Rectangle(Coordinates.NaN, Size.NaN)
    val invalid: @MayBeNaN Rectangle = NaN

    /**
     * Returns true if the given point ([x] and [y]) is within the rectangle described by the given values
     */
    fun isPointWithin(
      x: Double, y: Double,
      rectX: Double, rectY: Double, rectWidth: @Positive Double, rectHeight: @Positive Double,
    ): Boolean {
      if (x < rectX) {
        return false
      }
      if (y < rectY) {
        return false
      }

      if (x > rectX + rectWidth) {
        return false
      }
      if (y > rectY + rectHeight) {
        return false
      }

      return true
    }

    fun fromCoords(topLeft: Coordinates, bottomRight: Coordinates): Rectangle {
      return Rectangle(topLeft.x, topLeft.y, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y)
    }

    fun fromCoords(topLeftX: Double, topLeftY: Double, bottomRightX: Double, bottomRightY: Double): Rectangle {
      return Rectangle(topLeftX, topLeftY, bottomRightX - topLeftX, bottomRightY - topLeftY)
    }
  }
}

/**
 * Creates a rectangle with the given size at this location
 */
infix fun Coordinates.with(size: Size): Rectangle {
  return Rectangle(this, size)
}

fun rectangleAreaFromThreePoints(point1X: Double, point1Y: Double, point2X: Double, point2Y: Double, point3X: Double, point3Y: Double): Double {
  //Link to the method used: https://en.wikipedia.org/wiki/Area_of_a_triangle#Using_coordinates
  val point1point2deltaX = point2X - point1X
  val point1point2deltaY = point2Y - point1Y
  val point1point3deltaX = point3X - point1X
  val point1point3deltaY = point3Y - point1Y
  return abs(point1point3deltaX * point1point2deltaY - point1point3deltaY * point1point2deltaX)
}
