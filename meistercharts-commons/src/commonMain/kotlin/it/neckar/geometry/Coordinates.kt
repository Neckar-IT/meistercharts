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
import it.neckar.open.formatting.CachedNumberFormat
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.kotlin.lang.betweenInclusive
import it.neckar.open.kotlin.lang.distance
import it.neckar.open.kotlin.lang.isPositive
import it.neckar.open.kotlin.lang.toDegrees
import it.neckar.open.kotlin.lang.toRadians
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.other.deg
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.si.mm
import it.neckar.open.unit.si.rad
import kotlinx.serialization.Serializable
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Represents a coordinate
 *
 */
@JsExport
@Serializable
data class Coordinates(
  val x: Double,
  val y: Double,
) {

  @JsExport.Ignore
  constructor(x: Int, y: Int) : this(x.toDouble(), y.toDouble())

  fun plus(deltaX: Double, deltaY: Double): Coordinates {
    return of(x + deltaX, y + deltaY)
  }

  fun coerceAtLeast(minimum: Coordinates): Coordinates {
    return of(
      x.coerceAtLeast(minimum.x),
      y.coerceAtLeast(minimum.y)
    )
  }

  fun coerceAtMost(maximum: Coordinates): Coordinates {
    return of(
      x.coerceAtMost(maximum.x),
      y.coerceAtMost(maximum.y)
    )
  }

  fun coerceIn(minimum: Coordinates, maximumX: Double, maximumY: Double): Coordinates {
    return of(
      x.coerceIn(minimum.x, maximumX),
      y.coerceIn(minimum.y, maximumY)
    )
  }

  /**
   * Adds the given vector and returns the resulting coordinate
   */
  @JsExport.Ignore
  operator fun plus(vector: Distance): Coordinates {
    if (vector.isZero()) {
      return this
    }

    return of(x + vector.x, y + vector.y)
  }

  /**
   * Adds the given size and returns the resulting coordinate
   */
  @JsName("plusSize")
  operator fun plus(size: Size): Coordinates {
    return of(x + size.width, y + size.height)
  }

  fun withMax(maxX: Double, maxY: Double): Coordinates {
    return of(min(x, maxX), min(y, maxY))
  }

  fun withMin(minX: Double, minY: Double): Coordinates {
    return of(max(x, minX), max(y, minY))
  }

  fun withX(value: Double): Coordinates {
    return of(value, y)
  }

  fun withY(value: Double): Coordinates {
    return of(x, value)
  }

  override fun toString(): String {
    return "$x/$y"
  }

  /**
   * Calculates the distance from the given coordinate to this coordinate
   */
  fun delta(other: Coordinates): Distance {
    return Distance.of(x - other.x, y - other.y)
  }

  /**
   * Calculates the distance from the given coordinate to this coordinate.
   *
   * The returned distance is guaranteed to have positive values.
   */
  fun deltaAbsolute(other: Coordinates): Distance {
    return Distance.of(abs(x - other.x), abs(y - other.y))
  }

  /**
   * Subtracts the given coordinate from this
   */
  @JsName("minusDistance")
  operator fun minus(other: Distance): Coordinates {
    return minus(other.x, other.y)
  }

  fun minus(deltaX: Double, deltaY: Double): Coordinates {
    return of(x - deltaX, y - deltaY)
  }

  /**
   * Calculates the delta between two coordinates
   */
  @JsName("minusCoordinates")
  operator fun minus(other: Coordinates): Distance {
    return Distance.of(x - other.x, y - other.y)
  }

  /**
   * Subtracts the given coordinate from this.
   *
   * Attention: Results the result as coordinates.
   * This is probably not the method you are looking for!
   */
  fun minusAsCoordinates(other: Coordinates): Coordinates {
    return of(x - other.x, y - other.y)
  }

  @JsName("plusCoordinates")
  @Deprecated("It does not make sense to add coordinates. Use plus(Distance) instead")
  operator fun plus(that: Coordinates): Coordinates = Coordinates(x + that.x, y + that.y)

  @JsExport.Ignore
  @Deprecated("Does not make any sense")
  operator fun times(that: Coordinates): Coordinates = Coordinates(x * that.x, y * that.y)

  @JsExport.Ignore
  operator fun times(scale: Double): Coordinates = Coordinates(x * scale, y * scale)

  fun times(scaleX: Double, scaleY: Double): Coordinates = Coordinates(x * scaleX, y * scaleY)

  @Deprecated("Does not make any sense")
  @JsExport.Ignore
  operator fun div(that: Coordinates): Coordinates = Coordinates(x / that.x, y / that.y)

  operator fun div(scale: Double): Coordinates = Coordinates(x / scale, y / scale)

  fun crossProduct(p1: Coordinates, p2: Coordinates): Double {
    return (p1.x - this.x) * (p2.y - this.y) - (p1.y - this.y) * (p2.x - this.x)
  }


  /**
   * Computes the center between these [Coordinates] and [other]
   */
  fun center(other: Coordinates): Coordinates {
    return Coordinates(
      0.5 * (this.x + other.x),
      0.5 * (this.y + other.y)
    )
  }

  /**
   *  Computes [PolarCoordinates] from [Coordinates].
   *  @see <a href="https://en.wikipedia.org/wiki/Polar_coordinate_system#Converting_between_polar_and_Cartesian_coordinates">Wikipedia</a>
   */
  fun toPolar(): PolarCoordinates {
    return PolarCoordinates(sqrt(x * x + y * y), atan2(y, x))
  }

  /**
   * Formats the coordinates
   */
  fun format(format: CachedNumberFormat = decimalFormat): String {
    return "${format.format(x)}/${format.format(y)}"
  }

  /**
   * Returns true if this coordinate is close to the given coordinate.
   */
  fun isCloseTo(other: Coordinates, deltaX: Double, deltaY: Double = deltaX): Boolean {
    return x.betweenInclusive(other.x - deltaX, other.x + deltaX)
      &&
      y.betweenInclusive(other.y - deltaY, other.y + deltaY)
  }

  /**
   * Normalizes the coordinates.
   *
   * Returns this size as percentage of the base size
   */
  fun normalize(base: Size): @pct Coordinates {
    return of(
      1.0 / base.width * x,
      1.0 / base.height * y,
    )
  }

  /**
   * Calculates the distance from this and the other coordinates
   */
  @JsName("distanceToCoordinates")
  fun distanceTo(target: Coordinates): Double {
    return distanceTo(target.x, target.y)
  }

  fun distanceToPoint(target: Coordinates): Double {
    return distanceTo(target)
  }

  fun distanceTo(targetX: Double, targetY: Double): Double {
    return distance(x, y, targetX, targetY)
  }

  /**
   * Calculates the distance of this coordinate to a line segment that is defined by start/end
   */
  fun distanceToLine(lineStartX: Double, lineStartY: Double, lineEndX: Double, lineEndY: Double): Double {
    if (lineStartX == lineEndX && lineStartY == lineEndY) {
      //If the line has the same start and end
      return this.distanceTo(lineStartX, lineStartY)
    }

    if (isPerpendicularToLineSegment(lineStartX, lineStartY, lineEndX, lineEndY).not()) {
      //Not perpendicular to the line segment, so the closest point is either the start point or end point
      return min(distanceTo(lineStartX, lineStartY), distanceTo(lineEndX, lineEndY))
    }

    //Link to the method used: https://en.wikipedia.org/wiki/Distance_from_a_point_to_a_line#Line_defined_by_two_points
    val doubleTheSizeOfTheTriangle = rectangleAreaFromThreePoints(lineStartX, lineStartY, lineEndX, lineEndY, x, y)
    val hypotenuseVectorX = lineEndX - lineStartX
    val hypotenuseVectorY = lineEndY - lineStartY
    val hypotenuseLength = sqrt(hypotenuseVectorX.pow(2) + hypotenuseVectorY.pow(2))

    (doubleTheSizeOfTheTriangle / hypotenuseLength).let { triangleHeight ->
      return triangleHeight
    }
  }

  @JsName("distanceToLineCoordinates")
  fun distanceToLine(lineStart: Coordinates, lineEnd: Coordinates): Double {
    return distanceToLine(lineStart.x, lineStart.y, lineEnd.x, lineEnd.y)
  }

  /**
   * Returns the coordinates % the given values
   */
  fun mod(modX: Double, modY: @mm Double): Coordinates {
    require(modX.isPositive()) { "Invalid modX: $modX" }
    require(modY.isPositive()) { "Invalid modY: $modY" }

    return (Coordinates(x % modX, y % modY))
  }

  fun isFinite(): Boolean {
    return x.isFinite() && y.isFinite()
  }

  /**
   * Helper function to rotate a point around a pivot.
   * Positive values rotate counter-clockwise, negative values rotate clockwise
   */
  fun rotateAround(pivot: Coordinates, angleDegrees: @deg Double): Coordinates {
    @rad val radians = angleDegrees.toRadians()

    val cosTheta = cos(radians)
    val sinTheta = sin(radians)
    val translatedX = this.x - pivot.x
    val translatedY = this.y - pivot.y

    val rotatedX = translatedX * cosTheta - translatedY * sinTheta + pivot.x
    val rotatedY = translatedX * sinTheta + translatedY * cosTheta + pivot.y
    return Coordinates(rotatedX, rotatedY)
  }

  companion object {
    @JvmField
    val origin: Coordinates = Coordinates(0, 0)

    /**
     * No translation at all
     */
    @JvmField
    val none: Coordinates = origin

    @JvmField
    val NaN: @MayBeNaN Coordinates = Coordinates(Double.NaN, Double.NaN)
    val invalid: @MayBeNaN Coordinates = NaN

    @JvmStatic
    fun of(x: Double, y: Double): Coordinates {
      return Coordinates(x, y)
    }

    /**
     * Returns the min of x an y of both coordinates
     */
    @JvmStatic
    fun minOf(first: Coordinates, second: Coordinates): Coordinates {
      return of(min(first.x, second.x), min(first.y, second.y))
    }

    /**
     * Returns the max of x and y of both coordinates
     */
    @JvmStatic
    fun maxOf(first: Coordinates, second: Coordinates): Coordinates {
      return of(max(first.x, second.x), max(first.y, second.y))
    }

    /**
     * Calculates the center between two coordinates
     */
    @JvmStatic
    fun center(first: Coordinates, second: Coordinates): Coordinates {
      return first.center(second)
    }

    /**
     * Comparator that compares by x and then by y
     */
    val CompareByYThenX: Comparator<Coordinates> = compareBy<Coordinates> { it.y }.thenBy { it.x }
    val CompareByXThenY: Comparator<Coordinates> = compareBy<Coordinates> { it.x }.thenBy { it.y }

    /**
     * Sorts the coordinates in CSS order (top left, top right, bottom right, bottom left)
     */
    fun sortCssOrder(unsorted: List<Coordinates>): CssOrderQuadrilateral {
      require(unsorted.size == 4) { "Expected exactly 4 coordinates, but got ${unsorted.size} coordinates" }

      return Quadrilateral.fromList(unsorted).inCssOrder()
    }
  }
}

/**
 * Returns true if this coordinates lies within a rectangle defined by the given location and size
 */
fun Coordinates.within(location: Coordinates, size: @Zoomed Size): Boolean {
  return within(location, location + size)
}

fun Coordinates.within(start: Coordinates, end: Coordinates): Boolean {
  return within(start.x, start.y, end.x, end.y)
}

fun Coordinates.within(startX: Double, startY: Double, endX: Double, endY: Double): Boolean {
  return x.betweenInclusive(startX, endX) && y.betweenInclusive(startY, endY)
}

fun Coordinates.withinSized(startX: Double, startY: Double, width: Double, height: Double): Boolean {
  return x.betweenInclusive(startX, startX + width) && y.betweenInclusive(startY, startY + height)
}

/**
 * Returns true of the [Coordinates] are perpendicular either above or below the line segment defined by the given two points
 */
fun Coordinates.isPerpendicularToLineSegment(lineSegmentStartX: Double, lineSegmentStartY: Double, lineSegmentEndX: Double, lineSegmentEndY: Double): Boolean {
  //If the line segment is only a single point, any other point is perpendicular
  if (lineSegmentStartX == lineSegmentEndX && lineSegmentStartY == lineSegmentEndY) return true
  //If the line segment is vertical, the y coordinate is the adjusted x coordinate
  if (lineSegmentStartX == lineSegmentEndX) return y.betweenInclusive(lineSegmentStartY, lineSegmentEndY)
  //If the line segment is horizontal, the x coordinate does not need to be adjusted
  if (lineSegmentStartY == lineSegmentEndY) return x.betweenInclusive(lineSegmentStartX, lineSegmentEndX)

  //The [Coordinates] must lie within the bounds defined by the inverse functions going through the start and end of the line segment
  val inverseSlopeBetweenPoints = -1 / getSlopeBetweenPoints(lineSegmentStartX, lineSegmentStartY, lineSegmentEndX, lineSegmentEndY)
  val adjustedStartX = lineSegmentStartX + (y - lineSegmentStartY) / inverseSlopeBetweenPoints
  val adjustedEndX = lineSegmentEndX + (y - lineSegmentEndY) / inverseSlopeBetweenPoints
  return x.betweenInclusive(adjustedStartX, adjustedEndX)
}

fun Coordinates.isPerpendicularToLineSegment(lineSegmentStart: Coordinates, lineSegmentEnd: Coordinates): Boolean {
  return isPerpendicularToLineSegment(lineSegmentStart.x, lineSegmentStart.y, lineSegmentEnd.x, lineSegmentEnd.y)
}

fun getSlopeBetweenPoints(point1X: Double, point1Y: Double, point2X: Double, point2Y: Double): Double {
  return (point2Y - point1Y) / (point2X - point1X)
}

/**
 * Calculates the angle of the corner in a rectangle
 *
 * This function should be called on the corner point that you want to get the angle for.
 *
 * @param p1 one of the adjacent corners
 * @param p2 the other adjacent corner
 *
 * @return the angle in degrees
 */
fun Coordinates.calculateCornerAngles(p1: Coordinates, p2: Coordinates): @deg Double {
  val d1 = distanceTo(p1)
  val d2 = distanceTo(p2)
  val dotProduct = (this.x - p1.x) * (p2.x - this.x) + (this.y - p1.y) * (p2.y - this.y)
  return acos(dotProduct / (d1 * d2)).toDegrees()
}

/**
 * Sorts the coordinates in a clockwise order.
 */
fun List<Coordinates>.inCssOrder(): List<Coordinates> {
  // Calculate the center of the quadrilateral
  val centerX = sumOf { it.x } / size.toDouble()
  val centerY = sumOf { it.y } / size.toDouble()

  // Sort the coordinates based on the angle they form with the center
  return sortedWith(compareBy { atan2(it.y - centerY, it.x - centerX) })
}

/**
 * Calculates the convex hull enclosing the given points
 * Uses Andrew's monotone chain algorithm https://en.wikibooks.org/wiki/Algorithm_Implementation/Geometry/Convex_hull/Monotone_chain
 */
fun convexHull(points: List<Coordinates>): Hull {
  if (points.size <= 1) return Hull(points)
  val sortedPoints = points.sortedWith(compareBy({ it.x }, { it.y }))

  val lowerBound = mutableListOf<Coordinates>()
  for (point in sortedPoints) {
    while (lowerBound.size >= 2 && lowerBound[lowerBound.size - 2].crossProduct(lowerBound[lowerBound.size - 1], point) <= 0) {
      lowerBound.removeAt(lowerBound.size - 1)
    }
    lowerBound.add(point)
  }

  val upperBound = mutableListOf<Coordinates>()
  for (point in sortedPoints.asReversed()) {
    while (upperBound.size >= 2 && upperBound[upperBound.size - 2].crossProduct(upperBound[upperBound.size - 1], point) <= 0) {
      upperBound.removeAt(upperBound.size - 1)
    }
    upperBound.add(point)
  }

  lowerBound.removeAt(lowerBound.size - 1)
  upperBound.removeAt(upperBound.size - 1)

  return Hull(lowerBound + upperBound)
}

@Serializable
data class Hull(val coordinates: List<Coordinates>)

/**
 * Calculates the smallest rectangle that encloses the given convex hull
 * Uses rotating calipers algorithm https://en.wikipedia.org/wiki/Rotating_calipers
 */
fun rotatingCalipers(hull: Hull): Quadrilateral {
  val coordinates = hull.coordinates
  var minArea = Double.MAX_VALUE
  var bestRectangle = listOf<Coordinates>()

  for (index in coordinates.indices) {
    val p1 = coordinates[index]
    val p2 = coordinates[(index + 1) % coordinates.size]
    val edge = Coordinates(p2.x - p1.x, p2.y - p1.y)
    val edgeLength = hypot(edge.x, edge.y)
    val edgeDir = Coordinates(edge.x / edgeLength, edge.y / edgeLength)

    var minDot = Double.MAX_VALUE
    var maxDot = Double.MIN_VALUE
    var minCross = Double.MAX_VALUE
    var maxCross = Double.MIN_VALUE

    for (p in coordinates) {
      val relativePoint = Coordinates(p.x - p1.x, p.y - p1.y)
      val dot = relativePoint.x * edgeDir.x + relativePoint.y * edgeDir.y
      val cross = relativePoint.x * edgeDir.y - relativePoint.y * edgeDir.x

      minDot = min(minDot, dot)
      maxDot = max(maxDot, dot)
      minCross = min(minCross, cross)
      maxCross = max(maxCross, cross)
    }

    val width = maxDot - minDot
    val height = maxCross - minCross
    val area = width * height

    if (area < minArea) {
      minArea = area
      val corner1 = Coordinates(p1.x + edgeDir.x * minDot, p1.y + edgeDir.y * minDot)
      val corner2 = Coordinates(p1.x + edgeDir.x * maxDot, p1.y + edgeDir.y * maxDot)
      val corner3 = Coordinates(corner2.x - edgeDir.y * height, corner2.y + edgeDir.x * height)
      val corner4 = Coordinates(corner1.x - edgeDir.y * height, corner1.y + edgeDir.x * height)
      bestRectangle = listOf(corner1, corner2, corner3, corner4)
    }
  }

  return Quadrilateral.fromList(bestRectangle.inCssOrder())
}
