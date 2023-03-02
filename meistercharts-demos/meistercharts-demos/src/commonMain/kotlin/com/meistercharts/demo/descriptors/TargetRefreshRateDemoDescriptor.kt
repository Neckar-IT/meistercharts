package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.FramesPerSecondLayer
import com.meistercharts.algorithms.layers.debug.MarkAsDirtyLayer
import com.meistercharts.algorithms.layers.debug.PaintCounterLayer
import com.meistercharts.algorithms.layers.text.addText
import com.meistercharts.canvas.TargetRefreshRate
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableListWithProperty
import com.meistercharts.style.Palette.getChartColor

/**
 */
class TargetRefreshRateDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Target refresh rate"

  override val category: DemoCategory = DemoCategory.Calculations

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {

      meistercharts {
        configure {
          layers.addClearBackground()
          layers.addLayer(MyNewColorOnRepaintLayer())

          layers.addText { _, _ ->
            listOf(
              "Hello World"
            )
          }

          layers.addLayer(PaintCounterLayer())
          layers.addLayer(MarkAsDirtyLayer())
          layers.addLayer(FramesPerSecondLayer())

          //Enable recording of paint statistics - required for
          recordPaintStatistics = true
          configurableListWithProperty("Refresh rate", this.chartSupport::targetRefreshRate, TargetRefreshRate.predefined) {
            converter {
              "${it.refreshRate} FPS"
            }
          }
        }
      }
    }
  }
}

private class MyNewColorOnRepaintLayer : AbstractLayer() {
  override val type: LayerType = LayerType.Background

  var colorIndex = 0

  override fun paint(paintingContext: LayerPaintingContext) {

    val gc = paintingContext.gc
    gc.fill(getChartColor(colorIndex))
    colorIndex++


    gc.fillRect(0.0, 0.0, 100.0, 100.0)

  }
}
