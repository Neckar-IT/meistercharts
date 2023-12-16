package it.neckar.geometry

import it.neckar.open.unit.other.deg
import kotlinx.serialization.Serializable

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
  fun rotate(angleDegrees: @deg Double): Quadrilateral {
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
}
