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

import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.color.CanvasPaint

/**
 * Combines a fill and a stroke
 */
data class FillAndStrokeStyle(
  /**
   * The fill
   */
  val fill: CanvasPaint,
  /**
   * The stroke
   */
  val stroke: CanvasPaint,
) {
  /**
   * Apply the fill and stroke to the given gc
   */
  fun apply(gc: CanvasRenderingContext) {
    gc.fillAndStroke(this)
  }
}

/**
 * Sets the fill and stroke
 */
fun CanvasRenderingContext.fillAndStroke(style: FillAndStrokeStyle) {
  fillAndStroke(style.fill, style.stroke)
}
