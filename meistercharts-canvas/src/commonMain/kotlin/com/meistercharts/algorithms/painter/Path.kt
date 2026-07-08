/*
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

import com.meistercharts.canvas.FillRule
import com.meistercharts.canvas.layout.buffer.CoordinatesMultiBuffer
import it.neckar.geometry.Coordinates
import it.neckar.open.collections.IntArrayList
import it.neckar.open.unit.other.px

/**
 * Represents a path that can be applied to a graphics context ([com.meistercharts.canvas.CanvasRenderingContext]).
 *
 * The path data is stored in flat buffers ([PathActionType] ordinals + coordinate pairs) - adding an action
 * does not allocate. Therefore, a [Path] instance can be reused and rebuilt every frame ([beginPath]).
 */
class Path : PathActions, SupportsPathActions {

  override var fillRule: FillRule = FillRule.NonZero

  override fun fillRule(fillRule: FillRule) {
    this.fillRule = fillRule
  }

  /**
   * The type of each action - stored as [PathActionType] ordinal
   */
  private val actionTypeOrdinals: IntArrayList = IntArrayList()

  /**
   * The coordinate pairs of all actions - stored flat.
   * Each action consumes [PathActionType.coordinatePairCount] pairs; the last pair of an action is its end point.
   */
  private val coordinates: CoordinatesMultiBuffer = CoordinatesMultiBuffer()

  override val actionCount: Int
    get() = actionTypeOrdinals.size

  override fun actionTypeAt(actionIndex: Int): PathActionType {
    return PathActionType.entries[actionTypeOrdinals.getAt(actionIndex)]
  }

  override fun coordinateXAt(coordinatePairIndex: Int): @px Double {
    return coordinates.x(coordinatePairIndex)
  }

  override fun coordinateYAt(coordinatePairIndex: Int): @px Double {
    return coordinates.y(coordinatePairIndex)
  }

  /**
   * The actions for the path - materialized as objects.
   * Allocates one [PathAction] per action on every access - use the flat buffer access on hot paths.
   */
  override val actions: List<PathAction>
    get() {
      val materialized = ArrayList<PathAction>(actionCount)

      var coordinatePairIndex = 0
      for (actionIndex in 0 until actionCount) {
        val actionType = actionTypeAt(actionIndex)

        materialized.add(
          when (actionType) {
            PathActionType.MoveTo -> MoveTo(
              coordinateXAt(coordinatePairIndex), coordinateYAt(coordinatePairIndex),
            )

            PathActionType.LineTo -> LineTo(
              coordinateXAt(coordinatePairIndex), coordinateYAt(coordinatePairIndex),
            )

            PathActionType.QuadraticCurveTo -> QuadraticCurveTo(
              coordinateXAt(coordinatePairIndex), coordinateYAt(coordinatePairIndex),
              coordinateXAt(coordinatePairIndex + 1), coordinateYAt(coordinatePairIndex + 1),
            )

            PathActionType.BezierCurveTo -> BezierCurveTo(
              coordinateXAt(coordinatePairIndex), coordinateYAt(coordinatePairIndex),
              coordinateXAt(coordinatePairIndex + 1), coordinateYAt(coordinatePairIndex + 1),
              coordinateXAt(coordinatePairIndex + 2), coordinateYAt(coordinatePairIndex + 2),
            )
          }
        )

        coordinatePairIndex += actionType.coordinatePairCount
      }

      return materialized
    }

  /**
   * Returns true if the current path is a new path.
   * This is the case if beginPath was called and no path actions were executed.
   */
  val isNewPath: Boolean
    get() {
      return actionTypeOrdinals.isEmpty()
    }

  /**
   * Returns the current point of the path
   */
  val currentPointOrNull: Coordinates?
    get() {
      if (isEmpty()) {
        return null
      }
      return Coordinates(currentPointXOrNaN(), currentPointYOrNaN())
    }

  /**
   * Returns the current point or throws a [NoSuchElementException]
   */
  val currentPoint: Coordinates
    get() {
      if (isEmpty()) {
        throw NoSuchElementException("Path is empty")
      }
      return Coordinates(currentPointXOrNaN(), currentPointYOrNaN())
    }

  /**
   * Returns the x value of the current point of the path - without allocating [Coordinates].
   * Returns [Double.NaN] if the path is empty.
   */
  fun currentPointXOrNaN(): @px Double {
    return coordinates.lastXOrNaN()
  }

  /**
   * Returns the y value of the current point of the path - without allocating [Coordinates].
   * Returns [Double.NaN] if the path is empty.
   */
  fun currentPointYOrNaN(): @px Double {
    return coordinates.lastYOrNaN()
  }

  /**
   * Returns the first point of the path
   */
  val firstPointOrNull: Coordinates?
    get() {
      if (isEmpty()) {
        return null
      }
      return Coordinates(firstPointXOrNaN(), firstPointYOrNaN())
    }

  val firstPoint: Coordinates
    get() {
      if (isEmpty()) {
        throw NoSuchElementException("Path is empty")
      }
      return Coordinates(firstPointXOrNaN(), firstPointYOrNaN())
    }

  /**
   * Returns the x value of the end point of the first action - without allocating [Coordinates].
   * Returns [Double.NaN] if the path is empty.
   */
  fun firstPointXOrNaN(): @px Double {
    if (isEmpty()) {
      return Double.NaN
    }
    return coordinateXAt(actionTypeAt(0).coordinatePairCount - 1)
  }

  /**
   * Returns the y value of the end point of the first action - without allocating [Coordinates].
   * Returns [Double.NaN] if the path is empty.
   */
  fun firstPointYOrNaN(): @px Double {
    if (isEmpty()) {
      return Double.NaN
    }
    return coordinateYAt(actionTypeAt(0).coordinatePairCount - 1)
  }

  /**
   * Returns the first point of the last part (the coordinates of move to)
   */
  val firstPointOfLastPart: Coordinates
    get() {
      val coordinatePairIndex = lastMoveToCoordinatePairIndex()
      if (coordinatePairIndex < 0) {
        throw NoSuchElementException("Path contains no MoveTo action")
      }
      return Coordinates(coordinateXAt(coordinatePairIndex), coordinateYAt(coordinatePairIndex))
    }

  /**
   * Returns the coordinate-pair index of the last [PathActionType.MoveTo] action - or -1 if there is none
   */
  private fun lastMoveToCoordinatePairIndex(): Int {
    var coordinatePairIndex = coordinates.size

    for (actionIndex in actionCount - 1 downTo 0) {
      val actionType = actionTypeAt(actionIndex)
      coordinatePairIndex -= actionType.coordinatePairCount

      if (actionType == PathActionType.MoveTo) {
        return coordinatePairIndex
      }
    }

    return -1
  }

  override fun beginPath() {
    actionTypeOrdinals.clear()
    coordinates.resize(0)
  }

  override fun moveTo(x: Double, y: Double) {
    actionTypeOrdinals.add(PathActionType.MoveTo.ordinal)
    coordinates.add(x, y)
  }

  override fun lineTo(x: Double, y: Double) {
    actionTypeOrdinals.add(PathActionType.LineTo.ordinal)
    coordinates.add(x, y)
  }

  /**
   * Adds a quadratic curve
   */
  override fun quadraticCurveTo(control1X: Double, control1Y: Double, x: Double, y: Double) {
    actionTypeOrdinals.add(PathActionType.QuadraticCurveTo.ordinal)
    coordinates.add(control1X, control1Y, x, y)
  }

  /**
   * Adds a bezier curve
   */
  override fun bezierCurveTo(control1X: Double, control1Y: Double, control2X: Double, control2Y: Double, x: Double, y: Double) {
    actionTypeOrdinals.add(PathActionType.BezierCurveTo.ordinal)
    coordinates.add(control1X, control1Y, control2X, control2Y, x, y)
  }

  @Deprecated("not yet implemented!")
  override fun arcTo(controlX: Double, controlY: Double, x: Double, y: Double, radius: Double) {
    TODO("Not yet implemented")
    //See other arcTo method below - maybe add this later
  }

  @Deprecated("not yet implemented!")
  override fun arcCenter(centerX: Double, centerY: Double, radius: Double, startAngle: Double, extend: Double) {
    TODO("Not yet implemented")
    //ArcTo carries radii, a rotation and two flags - these do not fit the flat coordinate-pair encoding.
    //When implementing, store the additional arc parameters out of band (e.g. in a separate buffer).
  }

  fun isEmpty(): Boolean {
    return actionTypeOrdinals.isEmpty()
  }

  /**
   * Removes the last [count] actions - including their coordinate pairs.
   * E.g. used to strip temporary fill-closing segments after filling ([BinaryPainter]).
   */
  fun removeLastActions(count: Int) {
    repeat(count) {
      val lastActionType = actionTypeAt(actionCount - 1)
      actionTypeOrdinals.removeAt(actionTypeOrdinals.size - 1)
      coordinates.resize(coordinates.size - lastActionType.coordinatePairCount)
    }
  }

  /**
   * Creates a new path that contains all actions
   */
  fun copy(): Path {
    return Path().also { copied ->
      copied.actionTypeOrdinals.add(actionTypeOrdinals)
      coordinates.fastForEachIndexed { _, x, y ->
        copied.coordinates.add(x, y)
      }
    }
  }

  /**
   * Closes the path
   */
  override fun closePath() {
    val coordinatePairIndex = lastMoveToCoordinatePairIndex()
    if (coordinatePairIndex < 0) {
      throw NoSuchElementException("Path contains no MoveTo action")
    }
    lineTo(coordinateXAt(coordinatePairIndex), coordinateYAt(coordinatePairIndex))
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
