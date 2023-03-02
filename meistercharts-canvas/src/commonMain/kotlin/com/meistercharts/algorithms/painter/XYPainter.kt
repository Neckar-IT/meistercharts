package com.meistercharts.algorithms.painter

import com.meistercharts.annotations.Window
import com.meistercharts.canvas.CanvasRenderingContext
import it.neckar.open.unit.other.px

/**
 * Painter that is able to draw coordinates.
 * This can be lines, areas or single points.
 *
 */
interface XYPainter : Painter {
  /**
   * Adds a coordinate that is painted
   */
  fun addCoordinate(gc: CanvasRenderingContext, @px @Window x: Double, @px @Window y: Double)

  /**
   * Finish painting the data points
   */
  fun finish(gc: CanvasRenderingContext)
}
