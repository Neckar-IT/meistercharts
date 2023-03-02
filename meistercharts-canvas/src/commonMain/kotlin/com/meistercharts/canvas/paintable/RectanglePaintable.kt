package com.meistercharts.canvas.paintable

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Rectangle
import com.meistercharts.model.Size
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.unit.other.px

/**
 * Paints a solid rectangle
 */
class RectanglePaintable(
  val width: @px Double,
  val height: @px Double,
  var color: () -> Color,
) : Paintable {

  constructor(size: Size, color: () -> Color) : this(size.width, size.height, color)
  constructor(size: Size, color: Color) : this(size.width, size.height, color.asProvider())

  val boundingBox: Rectangle = Rectangle(Coordinates.origin, Size(width, height))

  override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle {
    return boundingBox
  }

  override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    val gc = paintingContext.gc
    gc.fill(color())
    gc.fillRect(x, y, width, height)
  }

  companion object {
    operator fun invoke(
      width: @px Double,
      height: @px Double,
      color: Color,
    ): RectanglePaintable {
      return RectanglePaintable(width, height, color.asProvider())
    }
  }
}
