package com.meistercharts.algorithms.painter

import com.meistercharts.annotations.Window
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.model.Coordinates
import it.neckar.open.unit.other.px

/**
 * Paints a xy line with optional shadow
 *
 */
open class FancyXyLinePainter(
  snapXValues: Boolean,
  snapYValues: Boolean
) : AbstractPainter(snapXValues, snapYValues), XYPainter {

  /**
   * The path
   */
  protected val path: MutableList<Coordinates> = mutableListOf()

  /**
   * The color of the line
   */
  var color: Color = Color.black

  /**
   * The width
   */
  @px
  var width: Double = 1.0


  /**
   * The (optional) shadow color.
   * If set to null no shadow is painted
   */
  var shadowColor: Color? = null

  /**
   * The offset of the shadow
   */
  @px
  var shadowOffset: Double = 2.0

  override fun addCoordinate(gc: CanvasRenderingContext, @px @Window x: Double, @px @Window y: Double) {
    path.add(Coordinates(x, y))
  }

  override fun finish(gc: CanvasRenderingContext) {
    if (path.size < 2) {
      return
    }

    //Paint the shadow if there is one
    shadowColor?.let {
      gc.strokeStyle(it)
      gc.translate(shadowOffset, shadowOffset)

      gc.beginPath()
      path.forEach { coordinates ->
        gc.lineTo(coordinates)
      }

      gc.lineWidth = width
      gc.stroke()
      gc.translate(-shadowOffset, -shadowOffset)
    }


    //Paint the path
    gc.beginPath()
    path.forEach { coordinates ->
      gc.lineTo(coordinates)
    }

    gc.strokeStyle(color)
    gc.stroke()
  }
}
