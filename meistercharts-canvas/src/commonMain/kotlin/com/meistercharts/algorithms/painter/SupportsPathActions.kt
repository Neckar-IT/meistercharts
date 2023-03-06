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
package com.meistercharts.algorithms.painter

import it.neckar.open.unit.number.PositiveOrZero
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.model.Coordinates
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.rad

/**
 * Interface that supports path actions (lineTo, pathTo...)
 */
interface SupportsPathActions {
  fun beginPath()

  fun moveTo(point: Coordinates) {
    moveTo(point.x, point.y)
  }

  fun moveTo(x: Double, y: Double)

  /**
   * Adds a line to the current path.
   * If the current path is empty moveTo will automatically be used
   */
  fun lineTo(point: Coordinates) {
    lineTo(point.x, point.y)
  }

  /**
   * Adds a line to the current path.
   * If the current path is empty moveTo will automatically be used
   */
  fun lineTo(x: Double, y: Double)

  fun quadraticCurveTo(control: Coordinates, endPoint: Coordinates) {
    quadraticCurveTo(control.x, control.y, endPoint.x, endPoint.y)
  }

  /**
   * Adds a quadratic curve to the current path
   */
  fun quadraticCurveTo(controlX: Double, controlY: Double, x: Double, y: Double)

  fun bezierCurveTo(control1: Coordinates, control2: Coordinates, endPoint: Coordinates) {
    bezierCurveTo(control1.x, control1.y, control2.x, control2.y, endPoint.x, endPoint.y)
  }

  /**
   * Adds a Bézier curve to the current path
   *
   * @param controlX1 The x-axis coordinate of the first control point.
   * @param controlY1 The y-axis coordinate of the first control point.
   * @param controlX2 The x-axis coordinate of the second control point.
   * @param controlY2 The y-axis coordinate of the second control point.
   * @param x2 The x-axis coordinate of the end point.
   * @param y2 The y-axis coordinate of the end point.
   */
  fun bezierCurveTo(@Window controlX1: Double, @Window controlY1: Double, @Window controlX2: Double, @Window controlY2: Double, @Window x2: Double, @Window y2: Double)

  /**
   * Adds an arc to the current path
   */
  fun arcTo(control: Coordinates, endPoint: Coordinates, radius: @PositiveOrZero Double) {
    arcTo(control.x, control.y, endPoint.x, endPoint.y, radius)
  }

  /**
   * Adds an arc to the current path
   */
  fun arcTo(controlX: Double, controlY: Double, x: Double, y: Double, radius: @PositiveOrZero Double)

  /**
   * Adds an arc segment to the current path
   */
  fun arcCenter(
    centerX: @Window Double,
    centerY: @Window Double,
    radius: @Zoomed Double,
    startAngle: @rad Double,
    extend: @rad Double,
  )

  /**
   * Draws an arc
   */
  @Deprecated("use arcCenter instead")
  fun arcTo(radiusX: Double, radiusY: Double, @rad xAxisRotation: Double, largeArc: Boolean, sweep: Boolean, x: Double, y: Double) {
    throw UnsupportedOperationException("")
  }

  /**
   * Adds an arc segment to the current path
   */
  fun arcCenter(
    center: Coordinates,
    radius: @Zoomed Double,
    startAngle: @rad Double,
    /**
     * The length of the angle
     */
    extend: @rad Double,
  ) {
    arcCenter(center.x, center.y, radius, startAngle, extend)
  }

  /**
   * Closes the path:
   * Adds a straight line from the current point to the start of the current sub-path.
   */
  fun closePath()
}

/**
 * "Special" epsilon that is used when creating paths containing arcs.
 * Without this epsilon there exist path fragments when:
 * * arcTo
 * * lineTo
 *
 * Attention: This value is *only* required for JavaFX - HTML canvas does not have this bug.
 * The workaround value is set in com.meistercharts.fx.MeisterChartsPlatform.
 *
 * Keep the default value to 0.0
 */
var ArcPathWorkaroundEpsilon: @px Double = 0.0
