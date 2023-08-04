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

import it.neckar.geometry.Coordinates

/**
 * Represents a path that can be applied to a graphics context ([com.meistercharts.canvas.CanvasRenderingContext])
 */
class Path : PathActions, SupportsPathActions {
  /**
   * The actions for the path
   */
  override val actions: MutableList<PathAction> = mutableListOf()

  /**
   * Returns the current point of the path
   */
  val currentPointOrNull: Coordinates?
    get() {
      return actions.lastOrNull()?.toCoordinates()
    }

  /**
   * Returns the current point or throws a [NoSuchElementException]
   */
  val currentPoint: Coordinates
    get() {
      return actions.last().toCoordinates()
    }

  /**
   * Returns the first point of the path
   */
  val firstPointOrNull: Coordinates?
    get() {
      return actions.firstOrNull()?.toCoordinates()
    }

  val firstPoint: Coordinates
    get() {
      return actions.first().toCoordinates()
    }

  /**
   * Returns the first point of the last part (the coordinates of move to)
   */
  val firstPointOfLastPart: Coordinates
    get() {
      return actions.last {
        it is MoveTo
      }.toCoordinates()
    }

  override fun beginPath() {
    actions.clear()
  }

  override fun moveTo(x: Double, y: Double) {
    actions.add(MoveTo(x, y))
  }

  override fun lineTo(x: Double, y: Double) {
    actions.add(LineTo(x, y))
  }

  /**
   * Adds a quadratic curve
   */
  override fun quadraticCurveTo(control1X: Double, control1Y: Double, x: Double, y: Double) {
    actions.add(QuadraticCurveTo(control1X, control1Y, x, y))
  }

  /**
   * Adds a bezier curve
   */
  override fun bezierCurveTo(control1X: Double, control1Y: Double, control2X: Double, control2Y: Double, x: Double, y: Double) {
    actions.add(BezierCurveTo(control1X, control1Y, control2X, control2Y, x, y))
  }

  @Deprecated("not yet implemented!")
  override fun arcTo(controlX: Double, controlY: Double, x: Double, y: Double, radius: Double) {
    TODO("Not yet implemented")
    //See other arcTo method below - maybe add this later
  }

  @Deprecated("not yet implemented!")
  override fun arcCenter(centerX: Double, centerY: Double, radius: Double, startAngle: Double, extend: Double) {
    TODO("Not yet implemented")
    //The code below is copied from [com.meistercharts.svg.SVGPathParser]
    //Update accordingly
    //actions.add(ArcTo(radiusX, radiusY, @rad xAxisRotation, largeArc, sweep, x, y))
  }

  fun isEmpty(): Boolean {
    return actions.isEmpty()
  }

  /**
   * Creates a new path that contains all actions
   */
  fun copy(): Path {
    return Path().also {
      it.actions.addAll(actions)
    }
  }

  /**
   * Closes the path
   */
  override fun closePath() {
    val firstPoint = firstPointOfLastPart
    lineTo(firstPoint.x, firstPoint.y)
  }

  companion object {
    /**
     * Returns an empty path
     */
    fun empty(): Path = Path()

    fun from(points: Iterable<Coordinates>): Path {
      return Path().also { path ->
        points.forEachIndexed { index, coords ->
          if (index == 0) {
            path.moveTo(coords.x, coords.y)
          } else {
            path.lineTo(coords.x, coords.y)
          }
        }
      }
    }
  }
}


