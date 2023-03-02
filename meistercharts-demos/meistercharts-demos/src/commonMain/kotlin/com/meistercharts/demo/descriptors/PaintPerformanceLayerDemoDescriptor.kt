package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.SlowLayer
import com.meistercharts.algorithms.layers.debug.addPerformanceLayer
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration

/**
 * Demos that visualizes the functionality of the PaintPerformanceLayerDemo layer
 */
class PaintPerformanceLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Paint Performance"
  override val description: String = "## Paint performance"
  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          layers.addLayer(SlowLayer(4.0, 4.0))
          layers.addLayer(SlowLayer(4.0, 4.0))
          layers.addPerformanceLayer()
        }
      }
    }
  }
}
