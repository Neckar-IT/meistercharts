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

import it.neckar.open.unit.number.IsFinite
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.model.Coordinates

/**
 * Represents painters that paints a line.
 * The line painter is responsible to calculate the *path* for the line.
 */
interface LinePainter {
  /**
   * Begins a new line
   */
  fun begin(gc: CanvasRenderingContext)

  /**
   * Adds the coordinate [x]/[y] to the line.
   *
   * Call [paint] when the line is complete
   *
   * Attention: Do *not* call with NaN or Infinity
   */
  fun addCoordinates(gc: CanvasRenderingContext, x: @Zoomed @IsFinite Double, y: @Zoomed @IsFinite Double)

  fun addCoordinate(gc: CanvasRenderingContext, location: @Zoomed @IsFinite Coordinates) {
    addCoordinates(gc, location.x, location.y)
  }

  /**
   * Finishes the line previously defined by [addCoordinates]
   */
  fun paint(gc: CanvasRenderingContext)
}
