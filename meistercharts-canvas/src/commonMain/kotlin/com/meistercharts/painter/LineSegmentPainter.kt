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
