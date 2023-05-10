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
package com.meistercharts.canvas.paintable

import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.strokeCross
import it.neckar.open.unit.other.px

/**
 * Contains zoom buttons that are painted using the canvas (without images/SVGs)
 */
object ZoomButtons {

  /**
   * Paints a zoom in button with the given size
   */
  fun paintZoomIn(
    gc: CanvasRenderingContext,
    symbolSize: @px Double,
    @px width: Double,
    @px height: Double
  ) {
    gc.fillRect(0.0, 0.0, width, height)
    gc.strokeCross(width * 0.5, height * 0.5, symbolSize)
  }

  /**
   * Paints a zoom out button with the given size
   */
  fun paintZoomOut(
    gc: CanvasRenderingContext,
    symbolSize: @px Double,
    @px width: Double,
    @px height: Double

  ) {
    gc.strokeLine(
      width * 0.5 - symbolSize * 0.5, height * 0.5,
      width * 0.5 + symbolSize * 0.5, height * 0.5
    )
  }
}
