package com.meistercharts.algorithms.layers

import com.meistercharts.algorithms.TimeRange
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.HistoryStorage
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.TimestampIndex
import com.meistercharts.model.Direction
import it.neckar.open.unit.other.ID
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.ms
import kotlin.math.roundToInt

/**
 * Paints the history
 *
 * //TODO replace with tiling stuff - for performance reasons
 *
 */
class SlowHistoryLayer(
  val historyStorage: HistoryStorage,
  val valueRange: ValueRange,
  val contentAreaTimeRange: TimeRange
) : AbstractLayer() {

  override val type: LayerType
    get() = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    val calculator = paintingContext.chartCalculator

    @ms val leftTime = calculator.window2timeX(0.0, contentAreaTimeRange)
    @ms val rightTime = calculator.window2timeX(gc.width, contentAreaTimeRange)

    val resolution = findBestResolution(rightTime - leftTime, gc.width)
    val dataPointCount = (rightTime - leftTime) / resolution.distance

    if (dataPointCount > 5_000) {
      gc.font(FontDescriptorFragment.XL)
      gc.fill(Color.red)
      gc.fillText("Too many data points (${dataPointCount.roundToInt()}). Painting aborted", gc.center, Direction.Center)
      return
    }


    val buckets = historyStorage.query(leftTime, rightTime, resolution)

    if (buckets.isEmpty()) {
      return
    }

    //Use the data series ids from the first chunk
    @ID val dataSeriesIds = buckets[0].chunk.configuration.decimalConfiguration.dataSeriesIds
    for (dataSeriesIndex in dataSeriesIds.indices) {
      gc.beginPath()
      var empty = true

      //Iterate over all buckets - for this data series
      buckets.forEach {
        val historyChunk = it.chunk

        if (historyChunk.isEmpty()) {
          return@forEach
        }

        require(historyChunk.configuration.decimalConfiguration.dataSeriesIds.size == dataSeriesIds.size) {
          "Data series size does not match. Expected <${dataSeriesIds.size}> but this chunk had <${historyChunk.configuration.decimalConfiguration.dataSeriesIds.size}>"
        }

        for (timestampIndexAsInt in 0 until historyChunk.timeStampsCount) {
          val timestampIndex = TimestampIndex(timestampIndexAsInt)
          @ms val time = historyChunk.timestampCenter(timestampIndex)
          @Domain val value = historyChunk.getDecimalValue(DecimalDataSeriesIndex(dataSeriesIndex), timestampIndex)

          @DomainRelative val domainRelative = valueRange.toDomainRelative(value)

          @Window val x = calculator.time2windowX(time, contentAreaTimeRange)
          @Window val y = calculator.domainRelative2windowY(domainRelative)

          if (empty) {
            gc.moveTo(x, y)
            empty = false
          } else {
            gc.lineTo(x, y)
          }
        }
      }

      gc.stroke(Color.black50percent)
      gc.stroke()
    }

    gc.fillText("Left time: $leftTime", 0.0, 0.0, Direction.TopLeft, 10.0, 10.0)
    gc.fillText("Right time: $rightTime", gc.width, 0.0, Direction.TopRight, 10.0, 10.0)
    gc.fillText("Resolution: $resolution", gc.width, 15.0, Direction.TopRight, 10.0, 10.0)
    gc.fillText("Delta Time: ${rightTime - leftTime} ms", gc.width, 30.0, Direction.TopRight, 10.0, 10.0)
    gc.fillText("Visible Data Points: $dataPointCount", gc.width, 45.0, Direction.TopRight, 10.0, 10.0)
  }

  /**
   * Returns the best resolution
   */
  private fun findBestResolution(delta: @ms Double, canvasWidth: @px Double): SamplingPeriod {
    val minPointCount = canvasWidth / 5.0

    //The maximum distance between two data points
    @ms val maxDistance = delta / minPointCount

    return SamplingPeriod.withMaxDistance(maxDistance)
  }
}
