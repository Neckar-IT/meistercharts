package com.meistercharts.algorithms.painter

import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.painter.LinePainter
import it.neckar.open.collections.DoubleArrayList
import it.neckar.open.collections.fastForEachIndexed

/**
 * A class for drawing a single line on a canvas.
 */
class DirectLinePainter(
  snapXValues: Boolean,
  snapYValues: Boolean,
) : AbstractPainter(snapXValues, snapYValues), LinePainter {

  private val xLocations = DoubleArrayList(10)
  private val yLocations = DoubleArrayList(10)

  /**
   * Clears the existing line coordinates.
   */
  override fun begin(gc: CanvasRenderingContext) {
    xLocations.clear()
    yLocations.clear()
  }

  /**
   * Adds coordinates to the line.
   *
   * @param x The x coordinate of the point to be added.
   * @param y The y coordinate of the point to be added.
   */
  override fun addCoordinates(gc: CanvasRenderingContext, x: Double, y: Double) {
    require(x.isFinite()) { "x must be a finite number but was $x" }
    require(y.isFinite()) { "y must be a finite number but was $y" }

    xLocations.add(x)
    yLocations.add(y)
  }

  /**
   * Draws the line on the canvas using the given [CanvasRenderingContext].
   *
   * @param gc The canvas rendering context used for drawing.
   */
  override fun paint(gc: CanvasRenderingContext) {
    if (xLocations.size < 2 || yLocations.size < 2) return

    gc.beginPath()
    gc.moveTo(xLocations[0], yLocations[0])

    xLocations.fastForEachIndexed { index, x ->
      if (index > 0) {
        gc.lineTo(x, yLocations[index])
      }
    }

    gc.stroke()
  }

  override fun toString(): String {
    return "SimpleLinePainter"
  }
}
