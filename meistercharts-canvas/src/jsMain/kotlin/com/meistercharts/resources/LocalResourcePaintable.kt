package com.meistercharts.resources

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.painter.UrlPaintable
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Rectangle
import com.meistercharts.model.Size
import it.neckar.open.unit.other.px

/**
 * Loads a local resource
 */
actual class LocalResourcePaintable actual constructor(
  val relativePath: String,
  size: @px Size?,

  /**
   * The alignment point for the bounding box
   */
  val alignmentPoint: Coordinates
) : Paintable {

  /**
   * The paintable that is used
   */
  val delegate: Paintable = if (size != null) UrlPaintable.fixedSize(relativePath, size, alignmentPoint) else UrlPaintable.naturalSize(relativePath, alignmentPoint)

  override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle {
    return delegate.boundingBox(paintingContext)
  }

  override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    delegate.paint(paintingContext, x, y)
  }

  actual fun withSize(size: Size): LocalResourcePaintable {
    return LocalResourcePaintable(relativePath, size, alignmentPoint)
  }

  actual companion object {
  }
}
