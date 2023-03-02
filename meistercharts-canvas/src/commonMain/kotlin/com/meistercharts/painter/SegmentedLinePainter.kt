package com.meistercharts.painter

import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.CanvasRenderingContext

/**
 * A line painter that uses LineSegmentStyle for each segment between two points
 */
class SegmentedLinePainter(
  val provider: LineSegmentPainterProvider = {
    DirectLineSegmentPainter()
  }
) : LinePainter {

  /**
   * Is set to false as soon as the first point has been added
   */
  private var empty = true
  private var segmentIndex = 0

  private var segmentStartX: @Zoomed Double = 0.0
  private var segmentStartY: @Zoomed Double = 0.0

  override fun begin(gc: CanvasRenderingContext) {
    empty = true
  }

  override fun addCoordinate(gc: CanvasRenderingContext, x: @Zoomed Double, y: @Zoomed Double) {
    if (!empty) {
      paintSegment(gc, x, y)
      segmentIndex++
    }

    //Prepare for the next segment
    segmentStartX = x
    segmentStartY = y
    empty = false
  }

  /**
   * Paints the current segment
   */
  private fun paintSegment(gc: CanvasRenderingContext, x: @Zoomed Double, y: @Zoomed Double) {
    provider(segmentIndex).paintSegment(gc, segmentStartX, segmentStartY, x, y)
  }

  override fun finish(gc: CanvasRenderingContext) {
  }
}


typealias LineSegmentPainterProvider = (segmentIndex: Int) -> LineSegmentPainter
