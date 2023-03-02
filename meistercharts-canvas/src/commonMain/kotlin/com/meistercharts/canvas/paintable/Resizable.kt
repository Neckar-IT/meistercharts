package com.meistercharts.canvas.paintable

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.model.Rectangle
import com.meistercharts.model.Size
import it.neckar.open.unit.other.px

/**
 * Represents a paintable that is resizable
 */
@Deprecated("No longer supported")
interface ResizablePaintable : Paintable {
  var size: @px Size
}


/**
 * Abstract base class that automatically updates the bounding box
 */
@Deprecated("No longer supported")
abstract class AbstractResizablePaintable(
  initialSize: @px Size,

  /**
   * Updates the bounding box for a new size.
   * This method is called when the size has been updated
   */
  val calculateBoundingBox: (size: Size) -> @px Rectangle

) : ResizablePaintable {

  override var size: @px Size = initialSize
    set(value) {
      field = value

      boundingBox = calculateBoundingBox(value)
    }

  private var boundingBox: Rectangle = calculateBoundingBox(initialSize)

  final override fun boundingBox(paintingContext: LayerPaintingContext): @px Rectangle {
    return boundingBox
  }
}
