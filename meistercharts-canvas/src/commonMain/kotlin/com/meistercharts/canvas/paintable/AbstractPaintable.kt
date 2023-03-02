package com.meistercharts.canvas.paintable

import com.meistercharts.algorithms.layers.AbstractPaintingVariables
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LoopIndexAwarePaintingVariables
import com.meistercharts.algorithms.layers.PaintingVariables
import com.meistercharts.annotations.Zoomed
import com.meistercharts.model.Rectangle

/**
 * Abstract base class for a paintable that supports layout and painting variables.
 * Paintables that have complicated calculations - especially of the bounding box - should extend this class.
 *
 */
abstract class AbstractPaintable : Paintable {
  /**
   * Returns the painting variables for this paintable
   */
  abstract fun paintingVariables(): PaintablePaintingVariables

  /**
   * Do *not* override this method.
   * Instead, calculate the bounding box in the painting variables.
   */
  final override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle {
    layoutIfNecessary(paintingContext)
    return paintingVariables().boundingBox
  }

  /**
   * Recalculates the layout for this paintable.
   * The layout method has to be called once in every paint tick.
   *
   * This method is automatically called if necessary by [layoutIfNecessary]
   * from [paint] and [boundingBox].
   *
   * Therefore, usually it is *not* necessary to call [layout] manually.
   */
  fun layout(paintingContext: LayerPaintingContext) {
    val paintingVariables = paintingVariables()

    //Reset the bounding box (and all other values)
    paintingVariables.reset()
    paintingVariables.calculate(paintingContext)

    //Verify that the bounding box has been calculated
    require(paintingVariables.boundingBox.isFinite()) {
      "Invalid bounding box - It is necessary to update the bounding box in [calculate]"
    }
  }

  /**
   * Calls [layout] if necessary - by checking the loop index
   */
  fun layoutIfNecessary(paintingContext: LayerPaintingContext) {
    if (paintingVariables().loopIndex != paintingContext.loopIndex) {
      layout(paintingContext)
    }
  }

  /**
   * The paint method automatically calls [layout] if necessary.
   */
  final override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    layoutIfNecessary(paintingContext)
    paintingVariables().verifyLoopIndex(paintingContext)

    paintAfterLayout(paintingContext, x, y)
  }

  /**
   * Is called after the painting variables have been updated by calling [layout]
   */
  abstract fun paintAfterLayout(paintingContext: LayerPaintingContext, x: Double, y: Double)
}

/**
 * Provides the current bounding box. The bounding box *must* be updated within the [calculate] method!
 */
interface PaintablePaintingVariables : PaintingVariables, LoopIndexAwarePaintingVariables {
  /**
   * The current bounding box for this paintable
   */
  val boundingBox: @Zoomed Rectangle

  /**
   * Resets the variables - should be called at the start of the [calculate] method.
   */
  fun reset()
}

/**
 * Abstract base class for [PaintablePaintingVariables]
 */
abstract class PaintablePaintingVariablesImpl : AbstractPaintingVariables(), PaintablePaintingVariables {
  /**
   * The bounding box of the complete legend
   */
  override var boundingBox: Rectangle = Rectangle.zero

  override fun reset() {
    super.reset()
    boundingBox = Rectangle.invalid
  }
}
