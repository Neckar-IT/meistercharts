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
package com.meistercharts.model

import it.neckar.open.unit.number.MayBeNegative
import com.meistercharts.model.RightTriangleType.MissingCornerInFirstQuadrant
import com.meistercharts.model.RightTriangleType.MissingCornerInFourthQuadrant
import com.meistercharts.model.RightTriangleType.MissingCornerInSecondQuadrant
import com.meistercharts.model.RightTriangleType.MissingCornerInThirdQuadrant
import it.neckar.open.collections.fastForEach
import it.neckar.open.kotlin.lang.pointIsLeftOfLine
import it.neckar.open.annotations.Slow

/**
 * This is the enum describing the 4 possible right triangles inside a rectangle
 * Each corner of th rectangle is also a corner of the triangle, apart from the corner marked by this enum
 * The ordering of the possible missing corners is done along the Cartesian coordinate system as pictured below
 *
 *  IV  |  I
 *  ---------
 *  III | II

 */
enum class RightTriangleType {
  MissingCornerInFirstQuadrant,
  MissingCornerInSecondQuadrant,
  MissingCornerInThirdQuadrant,
  MissingCornerInFourthQuadrant,
}

/**
 * Represents a rectangle.
 *
 * The rectangle has a size that starts at a given location. The size may be negative.
 * Therefore, the upper left corner of the rectangle is:
 * * the location if the size is positive
 * * location - size if the size is negative
 */
data class Triangle(
  override val location: Coordinates,
  @MayBeNegative
  override val size: Size,
  /**
   * Which corner of the bounding rectangle is missing to form this triangle
   */
  val rightTriangleType: RightTriangleType,
) : Shape {
  constructor(
    x: Double,
    y: Double,
    width: @MayBeNegative Double,
    height: @MayBeNegative Double,
    rightTriangleType: RightTriangleType,
  ) : this(Coordinates(x, y), Size(width, height), rightTriangleType)

  @Slow
  override fun vertices(): List<Coordinates> {
    return buildList {
      if (rightTriangleType != MissingCornerInFirstQuadrant) add(topRight())
      if (rightTriangleType != MissingCornerInSecondQuadrant) add(bottomRight())
      if (rightTriangleType != MissingCornerInThirdQuadrant) add(bottomLeft())
      if (rightTriangleType != MissingCornerInFourthQuadrant) add(topLeft())
    }
  }


  /**
   * Moves the rectangle
   */
  override fun move(deltaX: Double, deltaY: Double): Triangle {
    return Triangle(location.plus(deltaX, deltaY), size, rightTriangleType)
  }

  override fun move(distance: Distance): Triangle {
    return move(distance.x, distance.y)
  }

  override fun withX(newX: Double): Triangle {
    return Triangle(newX, getY(), getWidth(), getHeight(), rightTriangleType)
  }

  override fun withY(newY: Double): Triangle {
    return Triangle(getX(), newY, getWidth(), getHeight(), rightTriangleType)
  }

  /**
   * Creates a [Rectangle] from this [Rectangle] with width [newWidth]
   */
  override fun withWidth(newWidth: Double): Triangle {
    return Triangle(getX(), getY(), newWidth, getHeight(), rightTriangleType)
  }

  /**
   * Creates a [Rectangle] from this [Rectangle] with height [newHeight]
   */
  override fun withHeight(newHeight: Double): Triangle {
    return Triangle(getX(), getY(), getWidth(), newHeight, rightTriangleType)
  }

  /**
   * Returns a new rectangle object with the given location - but the same height/width
   */
  override fun withLocation(location: Coordinates): Triangle {
    if (this.location == location) {
      return this
    }

    return Triangle(location, size, rightTriangleType)
  }

  /**
   * Returns a new rectangle that has been extended with the given values
   */
  override fun expand(left: Double, top: Double, right: Double, bottom: Double): Triangle {
    return Triangle(getX() - left, getY() - top, getWidth() + left + right, getHeight() + top + bottom, rightTriangleType)
  }

  /**
   * Returns true if this rectangle overlaps with another rectangle
   */
  override fun overlaps(other: Shape): Boolean {
    return !doesNotOverlap(other)
  }

  /**
   * Checks if a [Shape] currently collides with this [Triangle]
   * Does NOT work with concave shapes
   */
  override fun doesNotOverlap(other: Shape): Boolean {
    /**
     * First, check if the other shape is outside the bounding rectangle
     * If yes, no further calculation is required
     */
    if (super.doesNotOverlap(other)) {
      return true
    }

    /**
     * Check for each point of the other shape that it lies "left" of the triangle's hypotenuse
     * We already know that at this point of the algorithm, the other shape is outside the triangle's bounding rectangle
     * This basically means that, if all the other shape's points are on the same side of the hypotenuse that is "outside" of the triangle, they do not overlap
     * In this case, if one point is "inside" the triangle, there is a collision
     */
    other.vertices().fastForEach { rectangleCorner ->
      when (rightTriangleType) {
        /**
         * Calculate with the start and end point for the hypotenuse for this triangle
         * Top and Bottom are switched as the planner defines y=0 as the bottom of the screen, not the top
         */
        MissingCornerInFirstQuadrant -> if (pointIsLeftOfLine(right, top, left, bottom, rectangleCorner.x, rectangleCorner.y)) return false
        MissingCornerInSecondQuadrant -> if (pointIsLeftOfLine(left, top, right, bottom, rectangleCorner.x, rectangleCorner.y)) return false
        MissingCornerInThirdQuadrant -> if (pointIsLeftOfLine(left, bottom, right, top, rectangleCorner.x, rectangleCorner.y)) return false
        MissingCornerInFourthQuadrant -> if (pointIsLeftOfLine(right, bottom, left, top, rectangleCorner.x, rectangleCorner.y)) return false
      }
    }

    return true
  }


  companion object {
    fun withLTRB(left: Double, top: Double, right: Double, bottom: Double, rightTriangleType: RightTriangleType): Triangle {
      return Triangle(left, top, right - left, bottom - top, rightTriangleType)
    }

    /**
     * Returns a rectangle that has its origin centered with the given width/height
     */
    fun centered(width: Double, height: Double, rightTriangleType: RightTriangleType): Triangle {
      return Triangle(Coordinates.of(-width / 2.0, -height / 2.0), Size(width, height), rightTriangleType)
    }

    /**
     * Returns a rectangle that has its origin centered with the given width/height
     */
    fun centered(size: Size, rightTriangleType: RightTriangleType): Triangle {
      return Triangle(Coordinates.of(-size.width / 2.0, -size.height / 2.0), size, rightTriangleType)
    }

    /**
     * Creates a new rectangle with its origin set to the bottom right corner
     */
    fun bottomRight(size: Size, rightTriangleType: RightTriangleType): Triangle {
      return Triangle(Coordinates(-size.width, -size.height), size, rightTriangleType)
    }

    /**
     * Creates a new rectangle with its origin set to the bottom left corner
     */
    fun bottomLeft(size: Size, rightTriangleType: RightTriangleType): Triangle {
      return Triangle(Coordinates(0.0, -size.height), size, rightTriangleType)
    }

    /**
     * Creates a new rectangle with its origin set to the top right corner
     */
    fun topRight(size: Size, rightTriangleType: RightTriangleType): Triangle {
      return Triangle(Coordinates(-size.width, 0.0), size, rightTriangleType)
    }

    /**
     * Creates a new rectangle with its origin set to the top right corner.
     */
    fun topLeft(size: Size, rightTriangleType: RightTriangleType): Triangle {
      return Triangle(Coordinates.origin, size, rightTriangleType)
    }

    fun centerLeft(width: Double, height: Double, rightTriangleType: RightTriangleType): Triangle {
      return Triangle(Coordinates.of(0.0, -height / 2.0), Size(width, height), rightTriangleType)
    }
  }
}
