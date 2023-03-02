package com.meistercharts.algorithms.layers

import com.meistercharts.algorithms.TimeRange
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.tile.SamplingPeriodCalculator
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.fillRectCoordinates
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryBucketRange
import com.meistercharts.model.Direction
import it.neckar.open.collections.fastForEach
import it.neckar.open.formatting.dateTimeFormatWithMillis
import it.neckar.open.formatting.intFormat
import it.neckar.open.unit.si.ms
import kotlin.math.max

/**
 * Shows the sizes/locations of the history buckets
 */
class HistoryBucketsRangeDebugLayer(
  /**
   * The time range of the content area
   */
  val contentAreaTimeRange: TimeRange,
  /**
   * Provides the sampling period that is used
   */
  val samplingPeriodCalculator: SamplingPeriodCalculator
) : AbstractLayer() {
  override val type: LayerType = LayerType.Notification

  override fun paint(paintingContext: LayerPaintingContext) {
    val layerSupport = paintingContext.layerSupport

    val gc = paintingContext.gc
    val calculator = paintingContext.chartCalculator

    val visibleTimeRange = calculator.visibleTimeRangeXinWindow(contentAreaTimeRange)

    val samplingPeriod = samplingPeriodCalculator.calculateSamplingPeriod(visibleTimeRange, calculator.chartState.windowSize)

    val historyBucketRange = HistoryBucketRange.find(samplingPeriod)


    @ms val maxDistance = historyBucketRange.duration * HistoryBucketDescriptor.MaxSupportedDescriptorsCount
    val start = max(visibleTimeRange.start, visibleTimeRange.end - maxDistance)

    val descriptors = HistoryBucketDescriptor.forRange(start, visibleTimeRange.end, historyBucketRange)


    descriptors.fastForEach {
      gc.font(FontDescriptorFragment.DefaultSize)

      @Window val startX = calculator.time2windowX(it.start, contentAreaTimeRange)
      @Window val endX = calculator.time2windowX(it.end, contentAreaTimeRange)
      @Window val centerX = calculator.time2windowX(it.center, contentAreaTimeRange)

      gc.fill(Color.orange)
      gc.fillRectCoordinates(startX + 1, 0.0, endX - 1, 20.0)

      gc.fill(Color.white)
      gc.font(FontDescriptorFragment.S)
      gc.fillText("${intFormat.format(it.index)} start : ${dateTimeFormatWithMillis.format(it.start, paintingContext.i18nConfiguration)}: ${historyBucketRange.name} - ${historyBucketRange.samplingPeriod.label}", centerX, 0.0, Direction.TopCenter)
    }
  }
}
