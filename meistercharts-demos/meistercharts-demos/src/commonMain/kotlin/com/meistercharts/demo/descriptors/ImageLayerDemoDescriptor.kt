package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.addPaintable
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.resources.Icons

/**
 * Very simple demo that shows how to work with a time domain chart
 */
class ImageLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Image layer"
  override val description: String = "## Displays an image"
  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          layers.addPaintable(Icons.yAxis())
        }
      }
    }
  }
}
