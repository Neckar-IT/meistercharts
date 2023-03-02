package com.meistercharts.canvas.paintable

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Rectangle
import com.meistercharts.model.Size

/**
 * A paintable that has a given size, but does not paint anything
 */
class TransparentPaintable(val size: Size) : Paintable {
  override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle = Rectangle(Coordinates.origin, Size(size.width, size.height))

  override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
  }
}
