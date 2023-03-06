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

import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.CanvasRenderingContext

/**
 * Paints one segment of a line
 */
fun interface LineSegmentPainter {
  /**
   * Paints one segment of a line
   */
  fun paintSegment(
    gc: CanvasRenderingContext,
    startX: @Zoomed Double,
    startY: @Zoomed Double,
    endX: @Zoomed Double,
    endY: @Zoomed Double
  )
}

/**
 * Default implementation that connects two coordinates with a direct line
 */
class DirectLineSegmentPainter(val lineStyle: LineStyle = LineStyle()) : LineSegmentPainter {
  override fun paintSegment(gc: CanvasRenderingContext, startX: @Zoomed Double, startY: @Zoomed Double, endX: @Zoomed Double, endY: @Zoomed Double) {
    lineStyle.apply(gc)

    gc.strokeLine(startX, startY, endX, endY)
  }
}

//TODO implementations for discrete values (Signal Edge)

class SignalEdgePainter(val lineStyle: LineStyle = LineStyle()) : LineSegmentPainter {
  override fun paintSegment(gc: CanvasRenderingContext, startX: Double, startY: Double, endX: Double, endY: Double) {
    lineStyle.apply(gc)

    TODO()
  }
}
