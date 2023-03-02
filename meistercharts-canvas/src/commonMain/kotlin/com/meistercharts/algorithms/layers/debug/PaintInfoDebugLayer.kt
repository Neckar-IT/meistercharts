package com.meistercharts.algorithms.layers.debug

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.Layers
import it.neckar.open.formatting.dateTimeFormat
import it.neckar.logging.LoggerFactory
import it.neckar.logging.debug

/**
 * This layer debugs the paint events
 */
class PaintInfoDebugLayer : AbstractLayer() {
  private val dateFormat = dateTimeFormat

  override val type: LayerType
    get() = LayerType.Notification

  override fun paint(paintingContext: LayerPaintingContext) {
    val chartState = paintingContext.chartSupport.currentChartState

    logger.debug {
      """Repaint called @ ${dateFormat.format(paintingContext.frameTimestamp, paintingContext.i18nConfiguration)} (delta: ${paintingContext.frameTimestampDelta}) ms
        |   Content Area Size: ${chartState.contentAreaSize}
        |   Window translation: ${chartState.windowTranslation}
        |   Zoom: ${chartState.zoom}
      """.trimMargin()
    }
  }

  companion object {
    private val logger = LoggerFactory.getLogger("com.meistercharts.events.gesture.PinchGestureSupport")
  }
}


/**
 * Registers a new paint info debug layer
 */
fun Layers.addPaintInfoDebug() {
  addLayer(PaintInfoDebugLayer())
}
