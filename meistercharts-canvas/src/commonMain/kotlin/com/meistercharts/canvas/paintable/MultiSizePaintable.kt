package com.meistercharts.canvas.paintable

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.model.Size

/**
 * A paintable with multiple paintables
 */
class MultiSizePaintable(
  /**
   * All paintables - sorted by size (large to small)
   */
  val paintables: List<Paintable>

) {

  init {
    require(paintables.isNotEmpty()) { "Need at least one paintable" }
  }

  /**
   * Returns a paintable with the same size (or smaller)
   */
  fun sameOrSmaller(size: Size, paintingContext: LayerPaintingContext): Paintable {
    return sameOrSmaller(size.width, size.height, paintingContext)
  }

  fun sameOrSmaller(width: Double, height: Double, paintingContext: LayerPaintingContext): Paintable {
    return paintables.firstOrNull {
      it.boundingBox(paintingContext).size
        .bothSmallerThanOrEqual(width, height)
    } ?: paintables.last()
  }

  /**
   * Returns the paintable that has the same size (or the next larger one)
   */
  fun sameOrLarger(width: Double, height: Double, paintingContext: LayerPaintingContext): Paintable {
    return paintables.lastOrNull {
      it.boundingBox(paintingContext).size
        .bothLargerThanOrEqual(width, height)
    } ?: paintables.first()
  }
}
