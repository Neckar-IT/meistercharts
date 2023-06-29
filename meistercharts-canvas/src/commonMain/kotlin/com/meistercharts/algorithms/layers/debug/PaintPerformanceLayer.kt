/**
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.algorithms.layers.debug

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.Layers
import com.meistercharts.color.Color
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.FontSize
import com.meistercharts.canvas.ConfigurationDsl
import com.meistercharts.model.Direction
import com.meistercharts.geometry.Distance
import it.neckar.open.formatting.NumberFormat
import it.neckar.open.formatting.decimalFormat1digit
import it.neckar.open.i18n.I18nConfiguration
import com.meistercharts.style.Palette.getChartColor
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.ms

/**
 * Shows the paint durations of the last n paints
 *
 */
class PaintPerformanceLayer(
  /**
   * The amount of pixels that are painted per millisecond
   */
  @px
  val pixelsPerMilli: Double = 5.0,
  /**
   * The threshold that is acceptable. All values above are painted red
   */
  @ms
  val threshold: Double = 15.0,
  /**
   * The maximum amount of millis that are displayed
   */
  @ms
  val maxMillis: Int = 25,
  additionalConfiguration: Configuration.() -> Unit = {}
) : AbstractLayer() {

  /**
   * The style configuration
   */
  val configuration: Configuration = Configuration().also(additionalConfiguration)

  override val type: LayerType
    get() = LayerType.Notification

  /**
   * The width per value
   */
  @px
  private val pixelsPerValue = 4.0

  @px
  private val lineHeight = 12.0


  override fun paint(paintingContext: LayerPaintingContext) {
    val paintStatisticsSupport = paintingContext.layerSupport.paintStatisticsSupport

    if (!paintingContext.layerSupport.recordPaintStatistics) {
      paintingContext.gc.fill(Color.red)
      paintingContext.gc.font(FontDescriptorFragment.L)
      paintingContext.gc.fillText("LayerSupport#recordRepaintStatistics disabled", 10.0, 10.0, Direction.TopLeft)
      return
    }

    //The maximum height that is shown
    @px val maxHeight = maxMillis * pixelsPerMilli
    @px val valuesAreaWidth = paintStatisticsSupport.maxStatsSize * pixelsPerValue
    @px val marginAxis = 5.0

    //The height of the threshold
    @px val thresholdHeight = threshold * pixelsPerMilli

    val gc = paintingContext.gc
    gc.translate(configuration.margin) //margin
    gc.translate(0.0, maxHeight) //y axis to bottom

    //Paint the background
    gc.fill(configuration.backgroundColor)
    gc.fillRect(0.0, -maxHeight, valuesAreaWidth + marginAxis, maxHeight)

    //The y axis
    gc.stroke(configuration.axis)
    gc.strokeLine(0.0, 0.0, 0.0, -thresholdHeight)
    gc.stroke(configuration.warning)
    gc.strokeLine(0.0, -thresholdHeight, 0.0, -maxHeight)

    //The ticks
    gc.stroke(configuration.axis)
    for (i in 0..maxMillis step 5) {
      if (i >= threshold) {
        gc.stroke(configuration.warning)
      } else {
        gc.stroke(configuration.axis)
      }
      paintYTick(i.toDouble(), gc)
    }

    //The tick labels
    paintTickLabel(0.0, gc)
    paintTickLabel(threshold, gc)
    paintTickLabel(maxMillis.toDouble(), gc)


    //Margin between axis and values
    gc.translate(marginAxis, 0.0)


    //Horizontal threshold line
    gc.stroke(Color.gray)
    gc.strokeLine(0.0, -thresholdHeight, valuesAreaWidth, -thresholdHeight)


    paintStatisticsSupport.paintingStatsList.forEachIndexed { columnIndex, paintingStats ->
      @ms val total = paintingStats.layerPaintDurations.total
      @px val x = columnIndex * pixelsPerValue


      //Red background if over threshold
      if (total >= threshold) {
        gc.fill(configuration.warning)
        @px val totalHeight = total * pixelsPerMilli
        gc.fillRect(x - 1, -totalHeight, pixelsPerValue + 2, totalHeight)
      }

      //paint the lines stacked

      //The current base value
      @px var base: Double = 0.0
      paintingStats.layerPaintDurations.paintDurations.forEachIndexed { layerIndex, layerPaintDuration ->
        @px val height = layerPaintDuration.duration * pixelsPerMilli

        gc.fill(getChartColor(layerIndex))
        gc.fillRect(x, base - height, pixelsPerValue - 1, height)
        base -= height
      }
    }

    //Paint the times for each layer
    paintStatisticsSupport.lastPaintingStats?.let { paintingStats ->
      val paintDurations = paintingStats.layerPaintDurations.paintDurations
      if (paintDurations.isEmpty()) {
        return@let
      }

      //Background
      gc.fill(configuration.backgroundColor)

      val height = paintDurations.size * lineHeight + 5.0
      gc.fillRect(0.0, 4.0, valuesAreaWidth, height)

      gc.fill(configuration.value)
      val size = paintDurations.size
      gc.translate(28.0, size * lineHeight - 4)

      paintDurations.forEachIndexed { index, duration ->
        gc.fill(getChartColor(index))

        gc.fillText(duration.duration.format(paintingContext.i18nConfiguration), -4.0, index * -lineHeight, Direction.TopRight)
        gc.fillText(duration.layerDescription, 0.0, index * -lineHeight, Direction.TopLeft)
      }
    }
  }

  private fun Double.format(i18nConfiguration: I18nConfiguration) = configuration.format.format(this, i18nConfiguration)

  private fun paintTickLabel(@ms millis: Double, gc: CanvasRenderingContext) {
    @px val height = -millis * pixelsPerMilli
    gc.fill(configuration.axis)
    gc.font(configuration.tickLabelsFont)
    gc.fillText("${millis.toInt()}", -4.0, height, Direction.CenterRight)
  }

  private fun paintYTick(@ms millis: Double, gc: CanvasRenderingContext) {
    @px val height = -millis * pixelsPerMilli
    gc.strokeLine(-2.0, height, 0.0, height)
  }


  /**
   * The style configuration
   */
  @ConfigurationDsl
  open class Configuration {
    /**
     * The color the background is filled with
     */
    var backgroundColor: Color = Color.rgba(255, 255, 255, 0.8)

    /**
     * The color that is used for the axis and ticks
     */
    var axis: Color = Color.black

    /**
     * The color for the threshold
     */
    var threshold: Color = Color.red

    /**
     * The color that is used for values above the threshold
     */
    var warning: Color = Color.red

    /**
     * A normal value
     */
    var value: Color = Color.gray

    /**
     * The tick labels font
     */
    var tickLabelsFont: FontDescriptorFragment = FontDescriptorFragment(size = FontSize(10.0))

    /**
     * The margin (from top left)
     */
    var margin: Distance = Distance(50.0, 50.0)

    /**
     * The number format that is used to format the value
     */
    var format: NumberFormat = decimalFormat1digit
  }
}

/**
 * Adds a background layer of the given color
 */
fun Layers.addPerformanceLayer() {
  addLayer(PaintPerformanceLayer())
}
