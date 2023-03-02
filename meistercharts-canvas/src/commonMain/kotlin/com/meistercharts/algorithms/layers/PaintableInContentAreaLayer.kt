package com.meistercharts.algorithms.layers

import com.meistercharts.algorithms.paintable.ObjectFit
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.model.Direction

/**
 * Paints a paintable in the content area
 */
class PaintableInContentAreaLayer(var backgroundImage: Paintable) : AbstractLayer() {
  override val type: LayerType = LayerType.Background

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    val chartCalculator = paintingContext.chartCalculator

    val x = chartCalculator.contentAreaRelative2windowX(0.0)
    val y = chartCalculator.contentAreaRelative2windowY(0.0)

    val width = chartCalculator.contentAreaRelative2zoomedX(1.0)
    val height = chartCalculator.contentAreaRelative2zoomedY(1.0)

    backgroundImage.paintInBoundingBox(paintingContext, x, y, Direction.TopLeft, 0.0, 0.0, width, height, ObjectFit.Contain)
  }
}
