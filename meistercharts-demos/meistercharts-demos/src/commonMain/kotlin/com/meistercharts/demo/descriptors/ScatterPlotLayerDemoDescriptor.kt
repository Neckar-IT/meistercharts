package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.ResetToDefaultsOnWindowResize
import com.meistercharts.algorithms.impl.ZoomAndTranslationDefaults
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.addShowZoomLevel
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.algorithms.layers.scatterplot.ScatterPlotLayer
import com.meistercharts.charts.ScatterPlotGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration

class ScatterPlotLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {

  override val name: String = "Scatter Plot Layer Demo"
  override val description: String = "Scatter Plot Layer Demo"
  override val category: DemoCategory = DemoCategory.Layers


  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        zoomAndTranslationDefaults {
          ZoomAndTranslationDefaults.tenPercentMargin
        }

        val data = ScatterPlotGestalt.createDefaultData()

        configure {
          chartSupport.windowResizeBehavior = ResetToDefaultsOnWindowResize

          layers.addClearBackground()
          layers.addLayer(ContentAreaDebugLayer())

          val scatterPlotLayer = ScatterPlotLayer(data)
          layers.addLayer(scatterPlotLayer)

          layers.addShowZoomLevel()
        }
      }
    }
  }
}
