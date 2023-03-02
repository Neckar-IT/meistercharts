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
   * Call [finish] when the line is complete
   *
   * Attention: Do *not* call with NaN or Infinity
   */
  fun addCoordinate(gc: CanvasRenderingContext, x: @Zoomed @IsFinite Double, y: @Zoomed @IsFinite Double)

  fun addCoordinate(gc: CanvasRenderingContext, location: @Zoomed @IsFinite Coordinates) {
    addCoordinate(gc, location.x, location.y)
  }

  /**
   * Finishes the line previously defined by [addCoordinate]
   */
  fun finish(gc: CanvasRenderingContext)
}
