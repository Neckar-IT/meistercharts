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

import com.meistercharts.algorithms.painter.CanvasPaint
import com.meistercharts.algorithms.painter.Color
import it.neckar.open.unit.other.px

/**
 * Represents a radial gradient
 */
data class CanvasRadialGradient(
  /**
   * The position of the center
   */
  val positionX: @px Double,
  /**
   * The position of the center
   */
  val positionY: @px Double,

  /**
   * The radius of the gradient
   */
  val radius: @px Double,

  /**
   * The color at the center of the gradient
   */
  val color0: Color,
  /**
   * The outer color
   */
  val color1: Color,

  //TODO add color stops(?)

  //TODO add color hint(?)
) : CanvasPaint {


  @Deprecated("???")
  enum class Shape {
    Circle,
    Ellipse
  }

  /**
   * The extend of the radial gradient
   * [https://developer.mozilla.org/en-US/docs/Web/CSS/radial-gradient]
   */
  @Deprecated("???")
  enum class Extend {
    /**
     * The gradient's ending shape meets the side of the box closest to its center (for circles) or meets both the vertical and horizontal sides closest to the center (for ellipses).
     */
    ClosestSide,

    /**
     * The gradient's ending shape is sized so that it exactly meets the closest corner of the box from its center.
     */
    ClosestCorner,

    /**
     * Similar to closest-side, except the ending shape is sized to meet the side of the box farthest from its center (or vertical and horizontal sides).
     */
    FarthestSide,

    /**
     * The default value, the gradient's ending shape is sized so that it exactly meets the farthest corner of the box from its center.
     */
    FarthestCorner

  }
}
