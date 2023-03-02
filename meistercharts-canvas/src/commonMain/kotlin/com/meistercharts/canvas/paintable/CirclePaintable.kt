package com.meistercharts.canvas.paintable

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Rectangle
import com.meistercharts.model.Size
import it.neckar.open.unit.other.px

/**
 * Paints a solid circle with the given diameter
 */
class CirclePaintable(
  val color: Color,
  val diameter: @px Double
) : Paintable {

  val boundingBox: Rectangle = Rectangle(Coordinates.origin, Size(diameter, diameter))

  override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle {
    return boundingBox
  }

  override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    val gc = paintingContext.gc
    gc.fill(color)
    gc.fillOvalOrigin(x, y, diameter, diameter)
  }
}
