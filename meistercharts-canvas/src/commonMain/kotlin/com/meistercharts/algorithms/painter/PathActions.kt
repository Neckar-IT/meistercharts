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
import it.neckar.open.unit.other.px

/**
 * Represents a list of path actions that can be painted on the rendering context.
 *
 * The path data is exposed as flat buffers ([actionCount], [actionTypeAt], [coordinateXAt], [coordinateYAt])
 * to allow allocation-free iteration on paint hot paths.
 */
interface PathActions {
  /**
   * The number of path actions
   */
  val actionCount: Int

  /**
   * Returns the type of the action at the given index
   */
  fun actionTypeAt(actionIndex: Int): PathActionType

  /**
   * Returns the x value of the coordinate pair at the given index.
   *
   * The coordinate pairs of all actions are stored flat: each action consumes
   * [PathActionType.coordinatePairCount] pairs; the last pair of an action is its end point.
   */
  fun coordinateXAt(coordinatePairIndex: Int): @px Double

  /**
   * Returns the y value of the coordinate pair at the given index.
   * See [coordinateXAt] for the pair layout.
   */
  fun coordinateYAt(coordinatePairIndex: Int): @px Double

  /**
   * Returns the path actions - materialized as objects.
   *
   * Allocates one [PathAction] per action on every access.
   * Use the flat buffer access ([actionCount], [actionTypeAt], [coordinateXAt], [coordinateYAt]) on hot paths.
   */
  val actions: List<PathAction>

  /**
   * The (optional) fill rule for these path actions
   */
  val fillRule: FillRule?
}
