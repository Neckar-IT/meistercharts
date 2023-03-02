package com.meistercharts.algorithms.painter

import com.meistercharts.annotations.Window
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.model.Coordinates
import it.neckar.open.unit.other.px


/**
 * Paints a xy area
 *
 */
class XyAreaPainter(
  /**
   * The base line height
   */
  @px @Window
  private val baseLineY: Double, snapXValues: Boolean, snapYValues: Boolean
) : FancyXyLinePainter(snapXValues, snapYValues) {

  private var fill: Color? = null

  /**
   * Stores the first x values to be able to finish the area
   */
  @px
  private var firstX: Double = 0.toDouble()

  override fun addCoordinate(gc: CanvasRenderingContext, @px @Window x: Double, @px @Window y: Double) {
    if (path.isEmpty()) {
      firstX = x
    }

    super.addCoordinate(gc, x, y)
  }

  override fun finish(gc: CanvasRenderingContext) {
    if (path.size < 2) {
      return
    }

    //Fill the area
    val toFill = path.toMutableList()

    val currentPoint = toFill.last()
    toFill.add(Coordinates(currentPoint.x, baseLineY))
    toFill.add(Coordinates(firstX, baseLineY))

    gc.beginPath()
    toFill.forEach {
      gc.lineTo(it)
    }

    fill?.let {
      gc.fill(it)
    }

    gc.fill()

    super.finish(gc)
  }
}
