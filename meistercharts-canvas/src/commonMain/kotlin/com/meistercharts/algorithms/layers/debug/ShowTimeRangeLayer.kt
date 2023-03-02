package com.meistercharts.algorithms.layers.debug

import com.meistercharts.algorithms.TimeRange
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.Layer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.model.Direction
import it.neckar.open.formatting.percentageFormat2digits

/**
 * A layer that paints the time range
 */
class ShowTimeRangeLayer(val contentAreaTimeRange: TimeRange) : AbstractLayer() {
  override val type: LayerType = LayerType.Notification

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    val calculator = paintingContext.chartCalculator

    val visibleTimeRange = calculator.visibleTimeRangeXinWindow(contentAreaTimeRange)

    gc.font(FontDescriptorFragment.DefaultSize)

    gc.fillText("Time Range: ${contentAreaTimeRange.format()}", gc.width, 0.0, Direction.TopRight, 20.0, 20.0)
    gc.fillText("Visible Time Range: ${visibleTimeRange.format()}", gc.width, 20.0, Direction.TopRight, 20.0, 20.0)

    val timeRelativeStart = calculator.window2timeRelativeX(0.0)
    val timeRelativeEnd = calculator.window2timeRelativeX(paintingContext.width)
    gc.fillText("Relative time: ${percentageFormat2digits.format(timeRelativeStart)} - ${percentageFormat2digits.format(timeRelativeEnd)}", 0.0, 0.0, Direction.TopLeft, 20.0, 20.0)
  }
}
