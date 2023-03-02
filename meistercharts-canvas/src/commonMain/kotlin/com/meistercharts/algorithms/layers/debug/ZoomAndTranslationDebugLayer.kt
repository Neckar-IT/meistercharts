package com.meistercharts.algorithms.layers.debug

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.saved
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Direction
import it.neckar.open.formatting.decimalFormat2digits
import it.neckar.open.formatting.percentageFormat2digits
import com.meistercharts.style.BoxStyle

/**
 * Shows the current zoom and pan state
 */
class ZoomAndTranslationDebugLayer : AbstractLayer() {
  override val type: LayerType = LayerType.Notification

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val chartCalculator = paintingContext.chartCalculator

    chartCalculator.window2domainRelative(Coordinates.origin).also {
      gc.paintTextBox("@DomainRelative: ${it.format(percentageFormat2digits)}", Direction.TopLeft, boxStyle = BoxStyle.gray)
    }


    gc.saved {
      gc.translateToCenter()
      gc.paintTextBox("Zoom: ${chartCalculator.chartState.zoom.format(decimalFormat2digits)}", Direction.Center, boxStyle = BoxStyle.gray)
    }


    chartCalculator.window2domainRelative(gc.width, gc.height).also {
      gc.translate(gc.width, gc.height)
      gc.paintTextBox("@DomainRelative: ${it.format(percentageFormat2digits)}", Direction.BottomRight, boxStyle = BoxStyle.gray)
    }

  }
}
