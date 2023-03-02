package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.RefreshListener
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.model.Direction
import it.neckar.open.formatting.dateTimeFormatWithMillis

/**
 *
 */
class RefreshDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Refresh Demo"
  override val category: DemoCategory = DemoCategory.Calculations

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          layers.addLayer(MyRefreshDebugLayer().also {
            chartSupport.onRefresh(it)
          })
        }
      }
    }
  }
}

class MyRefreshDebugLayer : AbstractLayer(), RefreshListener {
  override val type: LayerType
    get() = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    gc.paintTextBox(
      listOf(
        "now: ${paintingContext.frameTimestamp}",
        "now delta: ${paintingContext.frameTimestampDelta}",
        "now formatted: ${dateTimeFormatWithMillis.format(paintingContext.frameTimestamp, paintingContext.i18nConfiguration)}",
        "Last refresh time: $lastRefreshTime",
        "Last refresh delta: $lastRefreshDelta",
        "Last refresh delta formatted: ${dateTimeFormatWithMillis.format(lastRefreshTime, paintingContext.i18nConfiguration)}"
      ),
      Direction.TopLeft
    )
  }

  var lastRefreshTime: Double = 0.0
  var lastRefreshDelta: Double = Double.NaN

  override fun refresh(chartSupport: ChartSupport, frameTimestamp: Double, refreshDelta: Double) {
    lastRefreshTime = frameTimestamp
    lastRefreshDelta = refreshDelta
    chartSupport.markAsDirty()
  }
}
