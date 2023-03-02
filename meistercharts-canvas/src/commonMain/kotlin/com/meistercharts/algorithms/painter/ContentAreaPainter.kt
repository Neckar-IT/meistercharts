package com.meistercharts.algorithms.painter

import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.model.SidesSelection

/**
 * Painter that draws the valid area (x and y axis)
 *
 */
class ContentAreaPainter {
  var stroke: Color? = null
  var sidesToPaint: SidesSelection = SidesSelection.all

  private val areaPainter: AreaPainter = AreaPainter(snapXValues = false, snapYValues = false).apply {
    fill = null
  }

  fun paint(
    gc: CanvasRenderingContext,
    chartCalculator: ChartCalculator
  ) {

    areaPainter.borderSides = sidesToPaint
    areaPainter.borderColor = stroke

    @Window val fromX = chartCalculator.contentAreaRelative2windowX(0.0)
    @Window val toX = chartCalculator.contentAreaRelative2windowX(1.0)
    @Window val fromY = chartCalculator.contentAreaRelative2windowY(0.0)
    @Window val toY = chartCalculator.contentAreaRelative2windowY(1.0)
    areaPainter.paintArea(gc, fromX, fromY, toX, toY)
  }

}
