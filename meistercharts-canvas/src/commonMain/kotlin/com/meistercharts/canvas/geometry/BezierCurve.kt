package com.meistercharts.canvas.geometry

import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.Window
import com.meistercharts.model.Coordinates

/**
 * Represents a bezier curve
 */
data class BezierCurve(
  /**
   * The start point
   */
  val start: Coordinates,

  /**
   * The first control point
   */
  val control1: Coordinates,
  /**
   * The second control point
   */
  val control2: Coordinates,

  /**
   * The end point of the curve
   */
  val end: Coordinates
) {
  /**
   * Returns a new bezier curve that adds this with the given bezier curve
   */
  operator fun plus(other: BezierCurve): BezierCurve {
    return BezierCurve(
      start + other.start,
      control1 + other.control1,
      control2 + other.control2,
      end + other.end
    )
  }
}

/**
 * Returns a bezier curve that has been converted to window
 */
fun @DomainRelative BezierCurve.domainRelative2window(chartCalculator: ChartCalculator): @Window BezierCurve {
  return BezierCurve(
    chartCalculator.domainRelative2window(start),
    chartCalculator.domainRelative2window(control1),
    chartCalculator.domainRelative2window(control2),
    chartCalculator.domainRelative2window(end)
  )
}

/**
 * Scales the curve
 */
fun BezierCurve.scale(factorX: Double, factorY: Double): BezierCurve {
  return BezierCurve(
    start.times(factorX, factorY),
    control1.times(factorX, factorY),
    control2.times(factorX, factorY),
    end.times(factorX, factorY)
  )
}
