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
