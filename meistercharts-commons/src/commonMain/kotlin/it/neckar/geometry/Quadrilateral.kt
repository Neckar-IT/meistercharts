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

import it.neckar.open.kotlin.lang.toDegrees
import it.neckar.open.unit.other.deg
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

/**
 * A Quadrilateral is a 4-sided polygon. It is defined by 4 points (coordinates of corners).
 * The points are sorted - clockwise, usually starting from the top left corner.
 *
 *
 * Usually [point1] is the top left corner, [point2] is the top right corner, [point3] is the bottom right corner and [point4] is the bottom left corner.
 * This might change if the [Quadrilateral] is rotated.
 */
@Serializable
data class Quadrilateral(
  val point1: Coordinates,
  val point2: Coordinates,
  val point3: Coordinates,
  val point4: Coordinates,
) {

  /**
   * Creates a new Quadrilateral with the given x/y values.
   */
  constructor(
    x1: Double,
    y1: Double,
    x2: Double,
    y2: Double,
    x3: Double,
    y3: Double,
    x4: Double,
    y4: Double,
  ) : this(
    Coordinates(x1, y1),
    Coordinates(x2, y2),
    Coordinates(x3, y3),
    Coordinates(x4, y4),
  )

  /**
   * Returns a list of vertices (coordinates of corners) in the order: topLeft, topRight, bottomRight, bottomLeft
   */
  fun vertices(): List<Coordinates> = listOf(point1, point2, point3, point4)

  /**
   * Moves the Quadrilateral by a specified deltaX and deltaY
   */
  fun move(deltaX: Double, deltaY: Double): Quadrilateral {
    return Quadrilateral(
      point1.plus(deltaX, deltaY),
      point2.plus(deltaX, deltaY),
      point3.plus(deltaX, deltaY),
      point4.plus(deltaX, deltaY)
    )
  }

  /**
   * Moves the Quadrilateral by a given Distance object
   */
  fun move(distance: Distance): Quadrilateral {
    return move(distance.x, distance.y)
  }

  /**
   * Calculates the centroid (average of all points) of the Quadrilateral
   */
  fun calculateCentroid(): Coordinates {
    val x = (point1.x + point2.x + point3.x + point4.x) / 4
    val y = (point1.y + point2.y + point3.y + point4.y) / 4
    return Coordinates(x, y)
  }

  /**
   * Rotates the Quadrilateral around its centroid by a given angle (in degrees)
   */
  fun rotateAroundCentroid(angleDegrees: @deg Double): Quadrilateral {
    val centroid = calculateCentroid()

    val first = point1.rotateAround(centroid, angleDegrees)
    val second = point2.rotateAround(centroid, angleDegrees)
    val third = point3.rotateAround(centroid, angleDegrees)
    val fourth = point4.rotateAround(centroid, angleDegrees)

    return Quadrilateral(
      first,
      second,
      third,
      fourth
    )
  }

  /**
   * Returns the angle in degrees needed to get it to a square Quadrilateral after rotating
   */
  fun getSquareRotationAngle(): Double {
    //TL
    // Calculate the difference in y-coordinates between the top-left and top-right corners
    val deltaYTL = point2.y - point1.y
    // Calculate the difference in x-coordinates between the top-left and top-right corners
    val deltaXTL = point2.x - point1.x

    //BR
    // Calculate the difference in y-coordinates between the bottom-right and bottom-left corners
    val deltaYBR = point4.y - point3.y
    val deltaXBR = point4.x - point3.x

    val degreesTL = atan2(deltaYTL, deltaXTL).toDegrees()
    var degreesBR = atan2(deltaYBR, deltaXBR).toDegrees()
    // The lower side's angle is rotated opposite to the upper side; align its sign with the top-left corner.
    degreesBR = if (degreesBR.sign == 1.0) {
      degreesBR - 180
    } else {
      degreesBR + 180
    }

    //println("degreesTL: $degreesTL degreesBR: $degreesBR")

    return (abs(degreesTL) + abs(degreesBR)) / 2 * degreesTL.sign * -1
  }


  /**
   * Returns the lowest y-coordinate of the top side of the Quadrilateral
   */
  fun getLowestPointOfTop(): Double {
    return max(point1.y, point2.y)
  }

  /**
   * Returns the highest y-coordinate of the bottom side of the Quadrilateral
   */
  fun getHighestPointOfBottom(): Double {
    return min(point3.y, point4.y)
  }

  /**
   * Returns the innermost x-coordinate of the left side of the Quadrilateral
   */
  fun getInnerMostXLeftSide(): Double {
    return max(point1.x, point4.x)
  }

  /**
   * Returns the innermost x-coordinate of the right side of the Quadrilateral
   */
  fun getInnerMostXRightSide(): Double {
    return min(point2.x, point3.x)
  }

  /**
   * Generates a rectangle for cropping a rotated quadrilateral.
   * This function calculates the bounding box of the quadrilateral and then determines the distances from the borders of the bounding box to the actual borders of the quadrilateral.
   * It then creates a new rectangle that is shifted by these distances and optionally padded.
   * The resulting rectangle can be used for cropping the quadrilateral from an image.
   * Used after rotating, which results in a white background in areas where the original image got rotated away from.
   *
   * @param width The width of the image from which the quadrilateral is to be cropped.
   * @param height The height of the image from which the quadrilateral is to be cropped.
   * @param padding An optional padding to be added to the rectangle. Defaults to 0.0.
   * @return A Rectangle object representing the area to be cropped from the image.
   */
  fun generateRectangleForRotatedCropping(width: Double, height: Double, padding: Double = 0.0): Rectangle {
    val rectangle = calculateBoundingBox()

    // Calculate the distances from the borders of the bounding box to the actual borders of the quadrilateral
    val distanceToBorderTop = abs(rectangle.top - getLowestPointOfTop())
    val distanceToBorderBottom = abs(getHighestPointOfBottom() - rectangle.bottom)
    val distanceToBorderLeft = abs(rectangle.left - getInnerMostXLeftSide())
    val distanceToBorderRight = abs(getInnerMostXRightSide() - rectangle.right)

    // Create a new rectangle that is shifted by these distances and optionally padded
    return Rectangle.fromCoords(
      topLeft = Coordinates(0.0 + distanceToBorderLeft - padding, distanceToBorderTop - padding),
      bottomRight = Coordinates(width - distanceToBorderRight + padding, height - distanceToBorderBottom + padding)
    )
  }


  /**
   * Calculates the bounding box of the Quadrilateral: The smallest rectangle that contains the Quadrilateral.
   */
  fun calculateBoundingBox(): Rectangle {
    val minX = minOf(point1.x, point2.x, point3.x, point4.x)
    val maxX = maxOf(point1.x, point2.x, point3.x, point4.x)
    val minY = minOf(point1.y, point2.y, point3.y, point4.y)
    val maxY = maxOf(point1.y, point2.y, point3.y, point4.y)

    return Rectangle.fromCoords(Coordinates(minX, minY), Coordinates(maxX, maxY))
  }

  /**
   * Returns a rectangle that is the inner rectangle of the Quadrilateral.
   *
   * Attention: This is a very simple implementation that only works for "near-rectangles" (e.g. rectangles that are not rotated too much).
   */
  fun guesstimateInnerRectangle(): Rectangle {
    val pointsByX = vertices().sortedBy { it.x }
    val pointsByY = vertices().sortedBy { it.y }

    val x1 = pointsByX[1].x
    val y1 = pointsByY[1].y

    val x2 = pointsByX[2].x
    val y2 = pointsByY[2].y

    return Rectangle.fromCoords(x1, y1, x2, y2)
  }

  /**
   * Returns a new instance that contains the coordinates in CSS order (topLeft, topRight, bottomRight, bottomLeft)
   */
  fun inCssOrder(): CssOrderQuadrilateral {
    return CssOrderQuadrilateral(fromList(vertices().inCssOrder()))
  }

  override fun toString(): String {
    return "[$point1, $point2, $point3, $point4]"
  }

  companion object {
    fun fromList(coordinates: List<Coordinates>): Quadrilateral {
      require(coordinates.size == 4) { "List must contain exactly 4 coordinates but had ${coordinates.size} elements" }
      return Quadrilateral(
        point1 = coordinates[0],
        point2 = coordinates[1],
        point3 = coordinates[2],
        point4 = coordinates[3]
      )
    }

    fun minimumBoundingBoxFromList(coordinates: List<Coordinates>): Quadrilateral {
      require(coordinates.size >= 3) { "List must contain at least 3 coordinates but had ${coordinates.size} elements" }

      val hull = convexHull(coordinates)
      return rotatingCalipers(hull)
    }
  }
}

/**
 * Wraps a [Quadrilateral] and provides the coordinates in CSS order (topLeft, topRight, bottomRight, bottomLeft)
 */
@JvmInline
value class CssOrderQuadrilateral
/**
 * Do *not* call the constructor directly. Use [Quadrilateral.inCssOrder] instead to ensure the correct order.
 */
internal constructor(val quadrilateral: Quadrilateral) {
  fun topWidth(): Double {
    return topRight.x - topLeft.x
  }

  fun bottomWidth(): Double {
    return bottomRight.x - bottomLeft.x
  }

  fun leftHeight(): Double {
    return bottomLeft.y - topLeft.y
  }

  fun rightHeight(): Double {
    return bottomRight.y - topRight.y
  }

  /**
   * Returns the larger width of the quadrilateral (top or bottom)
   */
  fun largerWidth(): Double {
    return maxOf(topWidth(), bottomWidth())
  }

  fun largerHeight(): Double {
    return maxOf(leftHeight(), rightHeight())
  }

  val topLeft: Coordinates
    get() = quadrilateral.point1

  val topRight: Coordinates
    get() = quadrilateral.point2

  val bottomRight: Coordinates
    get() = quadrilateral.point3

  val bottomLeft: Coordinates
    get() = quadrilateral.point4

  override fun toString(): String {
    return "[$topLeft, $topRight, $bottomRight, $bottomLeft]"
  }
}
