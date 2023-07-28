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

import com.meistercharts.calc.ChartCalculator
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.Window
import it.neckar.geometry.Coordinates

/**
 * Represents a rectangle that has *two* sides (top and bottom) that are defined by Bézier curves.
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
   * Scales the Bézier curve (returns a new instance)
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
