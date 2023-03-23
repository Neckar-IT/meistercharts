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
