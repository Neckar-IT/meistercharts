package com.meistercharts.canvas.paintable

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.saved
import com.meistercharts.model.Distance
import com.meistercharts.model.Rectangle
import it.neckar.open.unit.other.pct

/**
 * A paintable that consists of multiple paintables.
 * The [main] is relevant for the size of this paintable
 */
class CombinedPaintable(
  /**
   * The main paintable that is painted first
   */
  val main: Paintable,
  /**
   * The secondary paintable that is put on top of the first paintable (usually has a smaller size)
   */
  val secondary: Paintable,
  /**
   * The offset (from the center) of the secondary.
   * If not set, the secondary paintable is placed in the center of the main paintable
   */
  @Zoomed
  val secondaryOffset: Distance = Distance.none
) : Paintable {

  override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle {
    return main.boundingBox(paintingContext)
  }

  override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    val gc = paintingContext.gc
    gc.saved {
      main.paint(paintingContext, x, y)
    }

    val mainBoundingBox = main.boundingBox(paintingContext)
    val secondaryBoundingBox = secondary.boundingBox(paintingContext)

    val deltaX = (mainBoundingBox.getWidth() - secondaryBoundingBox.getWidth()) / 2.0 + secondaryOffset.x + mainBoundingBox.getX()
    val deltaY = (mainBoundingBox.getHeight() - secondaryBoundingBox.getHeight()) / 2.0 + secondaryOffset.y + mainBoundingBox.getY()

    gc.saved {
      secondary.paint(paintingContext, x + deltaX, y + deltaY)
    }
  }

  companion object {
    fun relative(
      /**
       * Provides the main paintable
       */
      main: Paintable,
      /**
       * The secondary paintable that is painted above the main paintable
       */
      secondary: Paintable,
      /**
       * The offset from the center in percentage of the size
       */
      secondaryOffsetPercentage: @pct Distance = Distance(0.0, 0.0),

      /**
       * The painting context
       */
      paintingContext: LayerPaintingContext
    ): CombinedPaintable {
      val mainBoundingBox = main.boundingBox(paintingContext)

      val secondaryOffset = Distance(mainBoundingBox.getWidth() * secondaryOffsetPercentage.x, mainBoundingBox.getHeight() * secondaryOffsetPercentage.y)
      return CombinedPaintable(main, secondary, secondaryOffset)
    }
  }
}
