package com.meistercharts.algorithms.layers.debug

import com.meistercharts.algorithms.TimeRange
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryBucketDescriptor.Companion.MaxSupportedDescriptorsCount
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.model.Direction
import it.neckar.open.collections.fastForEachIndexed
import it.neckar.open.unit.si.ms
import kotlin.math.max

/**
 *
 */
class CleanupServiceDebugLayer(
  val historyStorage: InMemoryHistoryStorage,
  val contentAreaTimeRange: TimeRange
) : AbstractLayer() {
  override val type: LayerType = LayerType.Notification

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    val calculator = paintingContext.chartCalculator

    val visibleTimeRange = calculator.visibleTimeRangeXinWindow(contentAreaTimeRange)

    val historyBucketRange = historyStorage.naturalSamplingPeriod.toHistoryBucketRange()
    val latest = historyStorage.bookKeeping.latestBound(historyBucketRange) ?: return
    val lastToDelete = latest.previous(historyStorage.maxSizeConfiguration.keptBucketsCount)


    /*
     * The max distance that is supported
     */
    @ms val maxDistance = historyBucketRange.duration * MaxSupportedDescriptorsCount
    val start = max(visibleTimeRange.start, lastToDelete.end - maxDistance)

    val visibleDescriptors = HistoryBucketDescriptor.forRange(start, lastToDelete.end, historyBucketRange)

    visibleDescriptors.fastForEachIndexed { _, descriptor ->
      if (descriptor.end <= lastToDelete.end) {
        gc.fill(Color.red)
        gc.stroke(Color.red)

        @Window val startX = calculator.time2windowX(descriptor.start, contentAreaTimeRange)
        gc.strokeLine(startX, 0.0, startX, 20.0)

        gc.font(FontDescriptorFragment.S)
        gc.fillText("\uD83D\uDDD1", startX, 0.0, Direction.TopLeft, 2.0, 2.0)
      }
    }
  }
}
