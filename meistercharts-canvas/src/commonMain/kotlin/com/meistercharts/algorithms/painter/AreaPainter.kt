package com.meistercharts.algorithms.painter

import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.model.SidesSelection
import it.neckar.open.unit.other.px
import kotlin.math.max
import kotlin.math.min


/**
 * Paints an area with a border
 *
 */
open class AreaPainter(
  snapXValues: Boolean,
  snapYValues: Boolean,
) : AbstractPainter(snapXValues, snapYValues) {

  /**
   * The background color to be used for the area
   */
  var fill: Color? = Color.gray

  /**
   * The color to be used for the border
   */
  var borderColor: Color? = null

  /**
   * The line width of the border
   */
  var borderWidth: @px Double = 1.0

  /**
   * Which sides have a border
   */
  var borderSides: SidesSelection = SidesSelection.topAndBottom

  fun setFill(fill: Color): AreaPainter {
    this.fill = fill
    return this
  }

  fun setBorderColor(borderColor: Color?): AreaPainter {
    this.borderColor = borderColor
    return this
  }

  fun paintArea(gc: CanvasRenderingContext, @Window @px fromX: Double, @Window @px fromY: Double, @Window @px toX: Double, @Window @px toY: Double) {
    @px @Window val largerY = max(fromY, toY)
    @px @Window val smallerY = min(fromY, toY)

    @px @Window val largerX = max(fromX, toX)
    @px @Window val smallerX = min(fromX, toX)

    @Zoomed @px val width = largerX - smallerX
    @Zoomed @px val height = largerY - smallerY

    // Fill the rect first
    fill?.let {
      gc.fill(it)
      gc.fillRect(smallerX, smallerY, width, height)
    }

    // Draw the borders
    borderColor?.let {
      gc.strokeStyle(it)
      gc.lineWidth = borderWidth
      if (borderSides.leftSelected) {
        gc.strokeLine(smallerX, smallerY, smallerX, largerY)
      }
      if (borderSides.topSelected) {
        gc.strokeLine(smallerX, smallerY, largerX, smallerY)
      }
      if (borderSides.rightSelected) {
        gc.strokeLine(largerX, smallerY, largerX, largerY)
      }
      if (borderSides.bottomSelected) {
        gc.strokeLine(smallerX, largerY, largerX, largerY)
      }
    }
  }
}
