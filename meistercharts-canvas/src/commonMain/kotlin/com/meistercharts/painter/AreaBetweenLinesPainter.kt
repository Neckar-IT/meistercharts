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
package com.meistercharts.painter

import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.CanvasRenderingContext
import it.neckar.open.unit.number.IsFinite

interface AreaBetweenLinesPainter {
  /**
   * Begins a new set of lines.
   */
  fun begin(gc: CanvasRenderingContext)

  /**
   * Adds a pair of y-coordinates ([y1] and [y2]) for the same x-coordinate [x] to define two lines.
   *
   * Call [paint] when the lines are complete.
   *
   * Attention: Do *not* call with NaN or Infinity.
   */
  fun addCoordinates(
    gc: CanvasRenderingContext,
    x: @Zoomed @IsFinite Double,
    y1: @Zoomed @IsFinite Double, y2: @Zoomed @IsFinite Double,
  )

  /**
   * Paints the two lines and fills the area between them.
   */
  fun paint(gc: CanvasRenderingContext, strokeLines: Boolean)
}
