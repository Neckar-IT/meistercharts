package com.meistercharts.canvas.paintable

import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.resize.HandleBoundsProvider
import com.meistercharts.model.Direction
import com.meistercharts.model.Rectangle
import it.neckar.open.collections.fastForEach

/**
 * A paintable that draws resize handles
 */
open class ResizeHandlesPaintable(
  /**
   * Provides the bounds for the handles
   */
  val handleBoundsProvider: HandleBoundsProvider
) : Paintable {

  val style: Style = Style()

  override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle {
    return Rectangle.withLTRB(
      handleBoundsProvider.minX(Direction.TopLeft),
      handleBoundsProvider.minY(Direction.TopLeft),
      handleBoundsProvider.maxX(Direction.BottomRight),
      handleBoundsProvider.maxX(Direction.BottomRight),
    )
  }

  override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
    val gc = paintingContext.gc

    gc.fill(style.handleFill)

    Direction.cornersAndSides.fastForEach({ it: Direction ->
      gc.fillOvalOrigin(
        handleBoundsProvider.minX(it),
        handleBoundsProvider.minY(it),
        handleBoundsProvider.width(it),
        handleBoundsProvider.height(it),
      )
    })
  }

  class Style {
    /**
     * The color of the handle
     */
    var handleFill: Color = Color.lightgray
  }
}
