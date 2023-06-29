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
package com.meistercharts.color

import it.neckar.open.kotlin.lang.isPositiveOrZero
import it.neckar.open.unit.other.px
import kotlin.math.sqrt

/**
 * A radial gradient that can be used in styles
 */
data class RadialGradient(
  val color0: Color,
  val color1: Color
) : CanvasPaintProvider {
  override fun toCanvasPaint(x0: @px Double, y0: @px Double, x1: @px Double, y1: @px Double): CanvasPaint {
    val dx = x1 - x0
    val dy = y1 - y0

    val radius = sqrt(dx * dx + dy * dy)

    require(radius.isPositiveOrZero()) { "radius must not be negative but was <${radius}>" }

    return CanvasRadialGradient(x0, y0, radius = radius, color0 = color0, color1 = color1)
  }
}
