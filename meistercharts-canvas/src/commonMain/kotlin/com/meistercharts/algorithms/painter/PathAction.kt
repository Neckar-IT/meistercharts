package com.meistercharts.algorithms.painter

import com.meistercharts.model.Coordinates

/**
 * Represents an action within the path
 */
sealed class PathAction(
  /**
   * The end point X
   */
  val endPointX: Double,
  /**
   * The end point y
   */
  val endPointY: Double
) {
  fun toCoordinates(): Coordinates = Coordinates(endPointX, endPointY)
}

class MoveTo(
  endPointX: Double,
  endPointY: Double
) : PathAction(endPointX, endPointY) {
  override fun toString(): String {
    return "MoveTo $endPointX/$endPointY"
  }
}

class LineTo(
  endPointX: Double,
  endPointY: Double
) : PathAction(endPointX, endPointY) {
  override fun toString(): String {
    return "LineTo $endPointX/$endPointY"
  }
}

class QuadraticCurveTo(
  val controlX: Double,
  val controlY: Double,
  endPointX: Double,
  endPointY: Double
) : PathAction(endPointX, endPointY) {
  override fun toString(): String {
    return "QuadraticCurveTo $endPointX/$endPointY - Control: $controlX/$controlY"
  }
}

class BezierCurveTo(
  val control1X: Double,
  val control1Y: Double,
  val control2X: Double,
  val control2Y: Double,
  endPointX: Double,
  endPointY: Double
) : PathAction(endPointX, endPointY) {
  override fun toString(): String {
    return "BezierCurveTo $endPointX/$endPointY - Control 1: $control1X/$control1Y, Control 2: $control2X/$control2Y"
  }
}

@Deprecated("Not implemented at the moment!")
class ArcTo(
  val radiusX: Double,
  val radiusY: Double,
  val xAxisRotation: Double,
  val largeArc: Boolean,
  val sweep: Boolean,
  endPointX: Double,
  endPointY: Double
) : PathAction(endPointX, endPointY) {
  override fun toString(): String {
    return "ArcTo $endPointX/$endPointY - Radius: $radiusX/$radiusY, Rotation: $xAxisRotation, largeArc: $largeArc, sweep: $sweep"
  }
}
