package com.meistercharts.algorithms.layers

import com.meistercharts.algorithms.TimeRange
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.history.HistoryUpdateInfo
import it.neckar.open.unit.other.px

/**
 * Visualizes the updates to the history
 */
class HistoryUpdatesVisualizationLayer(val contentAreaTimeRange: TimeRange) : AbstractLayer() {
  override val type: LayerType = LayerType.Notification

  val style: Style = Style()

  var lastUpdateInfo: HistoryUpdateInfo? = null

  override fun paint(paintingContext: LayerPaintingContext) {
    lastUpdateInfo?.let { lastUpdateInfo ->
      val gc = paintingContext.gc
      val chartCalculator = paintingContext.chartCalculator

      lastUpdateInfo.updatedTimeRanges.fastForEach {
        val startX = chartCalculator.time2windowX(it.start, contentAreaTimeRange)
        val endX = chartCalculator.time2windowX(it.end, contentAreaTimeRange)

        gc.fill(style.fillColor)
        gc.fillRect(startX, 0.0, (endX - startX).coerceAtLeast(style.minimumWidth), gc.height)
      }
    }
  }

  @StyleDsl
  class Style {
    /**
     * The minimum width that is visualized
     */
    var minimumWidth: @px Double = 1.0

    var fillColor: Color = Color("#FF000055")
  }
}
