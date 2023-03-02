package com.meistercharts.canvas.geometry

import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.Window
import com.meistercharts.model.Coordinates

/**
 * Represents a rectangle that has *two* sides (top and bottom) that are defined by bezier curves.
 * The other sides are defined by connecting the:
 * - start points with each other
 * - end points with each other
 */
data class BezierCurveRect(
  val topCurve: BezierCurve,
  val bottomCurve: BezierCurve
) {

  /**
   * Returns the center between the start points
   */
  val centerStart: Coordinates
    get() {
      return topCurve.start.center(
        bottomCurve.start
      )
    }

  /**
   * Returns the center between the end points
   */
  val centerEnd: Coordinates
    get() {
      return topCurve.end.center(
        bottomCurve.end
      )
    }

  /**
   * Returns a new bezier curve rect that adds this and the given other curve.
   */
  operator fun plus(other: BezierCurveRect): BezierCurveRect {
    return BezierCurveRect(
      topCurve + other.topCurve,
      bottomCurve + other.bottomCurve
    )
  }

  /**
   * Scales the bezier curve (returns a new instance)
   */
  fun scale(scaleX: Double, scaleY: Double): BezierCurveRect {
    return BezierCurveRect(
      topCurve.scale(scaleX, scaleY),
      bottomCurve.scale(scaleX, scaleY)
    )
  }
}

/**
 * Returns a new BezierCurveRect that has been converted to window coordinates
 */
fun @DomainRelative BezierCurveRect.domainRelative2window(chartCalculator: ChartCalculator): @Window BezierCurveRect {
  return BezierCurveRect(
    topCurve.domainRelative2window(chartCalculator),
    bottomCurve.domainRelative2window(chartCalculator)
  )
}
