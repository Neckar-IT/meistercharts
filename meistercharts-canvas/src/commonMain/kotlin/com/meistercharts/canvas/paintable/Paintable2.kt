package com.meistercharts.canvas.paintable

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.annotations.Zoomed
import com.meistercharts.geometry.Rectangle
import it.neckar.open.unit.number.IsFinite
import it.neckar.open.unit.number.MayBeZero

/**
 * Advanced paintable that supports layout and painting variables.
 */
interface Paintable2 : Paintable {
  /**
   * Returns the painting variables for this paintable
   */
  fun paintingVariables(): PaintablePaintingVariables

  /**
   * Recalculates the layout for this paintable.
   * The layout method has to be called once in every paint tick.
   *
   * This method is automatically called if necessary by [layoutIfNecessary]
   * from [paint] and [boundingBox].
   *
   * Therefore, usually it is *not* necessary to call [layout] manually.
   *
   * Returns the bounding box - which might have a width and/or height of 0.0
   */
  fun layout(paintingContext: LayerPaintingContext): @Zoomed @IsFinite @MayBeZero Rectangle

  /**
   * Calls [layout] if necessary - by checking the loop index
   */
  fun layoutIfNecessary(paintingContext: LayerPaintingContext)

  /**
   * Is called after the painting variables have been updated by calling [layout]
   */
  fun paintAfterLayout(paintingContext: LayerPaintingContext, x: @Zoomed Double, y: @Zoomed Double)
}
