package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.TimeRange
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.TimeRelative
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.translateOverTime
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.model.Direction

/**
 * Demos that visualizes the functionality of the FPS layer
 */
class TranslateOverTimeDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Translate over time service"
  override val category: DemoCategory = DemoCategory.Calculations

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          layers.addLayer(ContentAreaDebugLayer())
          layers.addLayer(MyTranslateOverTimeDebugLayer())

          chartSupport.translateOverTime.contentAreaTimeRangeX = TimeRange.oneMinuteUntilNow()
          configurableBoolean("Animated", chartSupport.translateOverTime::animated)
        }
      }
    }
  }
}


private class MyTranslateOverTimeDebugLayer : AbstractLayer() {
  override val type: LayerType = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val chartCalculator = paintingContext.chartCalculator
    val gc = paintingContext.gc

    val translateOverTime = paintingContext.chartSupport.translateOverTime
    val timeRange = translateOverTime.contentAreaTimeRangeX ?: return

    gc.fillText("Time range: ${timeRange.format()}", 10.0, 10.0, Direction.TopLeft)

    @TimeRelative val relative = timeRange.time2relative(paintingContext.frameTimestamp)
    @Window val nowX = chartCalculator.domainRelative2windowX(relative)

    gc.stroke(Color.red)
    gc.strokeLine(nowX, 0.0, nowX, gc.height)

  }
}
