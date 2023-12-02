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

import com.meistercharts.annotations.Zoomed
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.number.MayBeNegative
import it.neckar.open.unit.number.Positive
import it.neckar.open.unit.other.px
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

  companion object {
    fun withLTRB(left: Double, top: Double, right: Double, bottom: Double): Rectangle {
      return Rectangle(left, top, right - left, bottom - top)
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
      return Rectangle(Coordinates.of(-width / 2.0, -height / 2.0), Size(width, height))
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
      return Rectangle(Coordinates.of(0.0, -height / 2.0), Size(width, height))
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

    fun fromCoords(topLeft: @px Coordinates, bottomRight: @px Coordinates): Rectangle {
      return Rectangle(topLeft.x, topLeft.y, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y)
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
