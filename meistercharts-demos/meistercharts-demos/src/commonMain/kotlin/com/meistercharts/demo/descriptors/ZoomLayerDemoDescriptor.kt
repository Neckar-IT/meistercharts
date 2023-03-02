package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.ShowZoomLevelLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration

class ZoomLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Show Zoom Level"
  override val description: String = "## How to create a toolbar"
  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          layers.addLayer(ShowZoomLevelLayer())
        }
      }
    }
  }
}
