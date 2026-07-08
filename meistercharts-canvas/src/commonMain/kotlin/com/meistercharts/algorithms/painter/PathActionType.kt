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

/**
 * The type of a path action.
 * Used for the flat path representation ([PathActions]) that avoids allocating one [PathAction] object per action.
 */
enum class PathActionType(
  /**
   * The number of coordinate pairs the action consumes.
   * The last pair is always the end point of the action.
   */
  val coordinatePairCount: Int,
) {
  MoveTo(1),

  LineTo(1),

  /**
   * Consumes two pairs: control point, end point
   */
  QuadraticCurveTo(2),

  /**
   * Consumes three pairs: first control point, second control point, end point
   */
  BezierCurveTo(3),
}
