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
package com.meistercharts.canvas.geometry

import com.meistercharts.geometry.Coordinates
import it.neckar.open.unit.other.pct


/**
 * Contains calculations for bézier curves
 */
object BezierCalculations {

  /**
   * Generates control coordinates for the given coordinates
   */
  fun generateControlCoordinates(coordinates: List<Coordinates>, tightness: Double = 0.5): List<Coordinates> {
    val size = coordinates.size

    val controlCoordinates = mutableListOf<Coordinates>()

    for (i in 0 until size - 1) {
      val p0 = if (i - 1 < 0) coordinates[i] else coordinates[i - 1]
      val p1 = coordinates[i]
      val p2 = coordinates[i + 1]
      val p3 = if (i + 2 >= size) coordinates[i + 1] else coordinates[i + 2]

      val cp1 = Coordinates(
        p1.x + (p2.x - p0.x) / 6 * tightness,
        p1.y + (p2.y - p0.y) / 6 * tightness
      )

      val cp2 = Coordinates(
        p2.x - (p3.x - p1.x) / 6 * tightness,
        p2.y - (p3.y - p1.y) / 6 * tightness
      )

      controlCoordinates.add(cp1)
      controlCoordinates.add(cp2)
    }

    return controlCoordinates
  }

  /**
   * Calculate the tangent vector at a given percentage along a bezier path.
   *
   * The bezier path is defined by a list of coordinatess and a corresponding list of control coordinatess. Each pair of control coordinatess
   * corresponds to one segment of the path. The `percent` parameter specifies a position along the entire path,
   * not just a single segment.
   *
   * The returned coordinates represents the x and y components of the tangent vector at the specified position.
   * You can convert this to an angle with `Math.atan2(tangent.y, tangent.x)`.
   *
   * @param percent The position along the path to calculate the tangent at, as a percentage of the total path length.
   * If this is 0, the tangent at the very start of the path is calculated. If this is 1, the tangent at the very end is calculated.
   * @param coordinates A list of coordinatess defining the bezier path. Each coordinates is one node of the path.
   * @param controlCoordinates A list of control coordinatess for the bezier path. Each pair of control coordinatess corresponds to the segment between two coordinatess in the `coordinatess` list.
   * @return The tangent at the specified position on the path, as a Coordinates where the x and y coordinates represent the x and y components of the tangent vector.
   *
   * @throws IllegalArgumentException if the `percent` parameter is less than 0 or greater than 1.
   *
   * Example usage:
   *
   * val coordinatess = listOf(Coordinates(0.0, 0.0), Coordinates(100.0, 200.0), Coordinates(200.0, -100.0), Coordinates(300.0, 0.0))
   * val controlCoordinatess = generateControlCoordinatess(coordinatess)
   *
   * // Calculate the tangent at the halfway coordinates of the bezier path
   * val tangent = calculateTangentOnPath(0.5, coordinatess, controlCoordinatess)
   *
   * // Convert the tangent vector to an angle
   * val angle = Math.atan2(tangent.y, tangent.x)
   */
  fun calculateTangentOnPath(percent: Double, coordinates: List<Coordinates>, controlCoordinates: List<Coordinates>): Coordinates {
    val numSegments = coordinates.size - 1
    val t = percent * numSegments

    val segmentIndex = t.toInt()
    val segmentT = t - segmentIndex

    val p0 = coordinates[segmentIndex]
    val p1 = controlCoordinates[segmentIndex * 2]
    val p2 = controlCoordinates[segmentIndex * 2 + 1]
    val p3 = if (segmentIndex + 1 < coordinates.size) coordinates[segmentIndex + 1] else coordinates.last()

    return calculateBezierTangent(segmentT, p0, p1, p2, p3)
  }

  fun calculateBezierTangent(percentage: @pct Double, start: Coordinates, control1: Coordinates, control2: Coordinates, end: Coordinates): Coordinates {
    val u = 1 - percentage
    val tt = percentage * percentage
    val uu = u * u

    return Coordinates(
      3 * uu * (control1.x - start.x) + 6 * u * percentage * (control2.x - control1.x) + 3 * tt * (end.x - control2.x),
      3 * uu * (control1.y - start.y) + 6 * u * percentage * (control2.y - control1.y) + 3 * tt * (end.y - control2.y)
    )
  }

  /**
   * Calculates the coordinates of a point on a cubic Bézier curve.
   *
   * @param percentage The progression along the curve, ranging from 0 (start of the curve) to 1 (end of the curve).
   * @param startPoint The starting point of the cubic Bézier curve.
   * @param controlPoint1 The first control point for the cubic Bézier curve, influencing the curve's shape.
   * @param controlPoint2 The second control point for the cubic Bézier curve, influencing the curve's shape.
   * @param endPoint The end point of the cubic Bézier curve.
   *
   * @return The calculated coordinates of the point on the Bézier curve at the progression [percentage].
   */
  fun calculateBezierCoordinates(
    percentage: @pct Double,
    startPoint: Coordinates,
    controlPoint1: Coordinates,
    controlPoint2: Coordinates,
    endPoint: Coordinates,
  ): Coordinates {

    val remainingPercentage = 1 - percentage
    val percentageSquared = percentage * percentage
    val remainingPercentageSquared = remainingPercentage * remainingPercentage

    val percentageCubed = percentageSquared * percentage
    val remainingPercentageCubed = remainingPercentageSquared * remainingPercentage

    val xCoordinate = remainingPercentageCubed * startPoint.x +
      3 * remainingPercentageSquared * percentage * controlPoint1.x +
      3 * remainingPercentage * percentageSquared * controlPoint2.x +
      percentageCubed * endPoint.x

    val yCoordinate = remainingPercentageCubed * startPoint.y +
      3 * remainingPercentageSquared * percentage * controlPoint1.y +
      3 * remainingPercentage * percentageSquared * controlPoint2.y +
      percentageCubed * endPoint.y

    return Coordinates(xCoordinate, yCoordinate)
  }

  fun calculateCoordinatesOnPath(percent: @pct Double, points: List<Coordinates>, controlPoints: List<Coordinates>): Coordinates {
    val numSegments = points.size - 1
    val t = percent * numSegments

    val segmentIndex = t.toInt()
    val segmentT = t - segmentIndex

    val start = points[segmentIndex]
    val controlPoint1 = controlPoints[segmentIndex * 2]
    val controlPoint2 = controlPoints[segmentIndex * 2 + 1]
    val end = if (segmentIndex + 1 < points.size) points[segmentIndex + 1] else points.last()

    return calculateBezierCoordinates(segmentT, start, controlPoint1, controlPoint2, end)
  }

  /**
   * Estimates the length of a cubic Bézier curve segment.
   *
   * The length is approximated by dividing the curve into many small line segments and adding up their lengths. This is not an exact calculation, but it provides a good approximation that should be close to the actual length.
   *
   * The precision of the approximation can be adjusted with the `precision` parameter. A higher value will result in a more accurate estimation, but it will also take longer to compute.
   *
   * @param start The starting point of the Bézier curve segment.
   * @param control1 The first control point of the Bézier curve segment.
   * @param control2 The second control point of the Bézier curve segment.
   * @param end The ending point of the Bézier curve segment.
   * @param precision The number of line segments to divide the curve into for the approximation. Defaults to 100. Higher values will yield more accurate results, but will take longer to compute.
   *
   * @return An estimation of the length of the Bézier curve segment.
   *
   * Example usage:
   *
   * val p0 = Point(0.0, 0.0)
   * val p1 = Point(50.0, 100.0)
   * val p2 = Point(150.0, 100.0)
   * val p3 = Point(200.0, 0.0)
   * val length = estimateBezierLength(p0, p1, p2, p3, 100)
   */
  fun estimateBezierLength(start: Coordinates, control1: Coordinates, control2: Coordinates, end: Coordinates, precision: Int = 100): Double {
    var length = 0.0
    var previousCoordinates = start

    for (i in 1..precision) {
      val t = i.toDouble() / precision
      val currentCoordinates = calculateBezierCoordinates(t, start, control1, control2, end)
      length += previousCoordinates.distanceTo(currentCoordinates)
      previousCoordinates = currentCoordinates
    }

    val direct = start.distanceTo(end)
    val diff = direct - length
    println("Direct: $direct Length: $length Diff: $diff")

    return length
  }

}
