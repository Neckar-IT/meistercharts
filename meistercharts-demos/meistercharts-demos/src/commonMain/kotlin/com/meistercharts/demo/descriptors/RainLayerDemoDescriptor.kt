package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.custom.rainsensor.RainLayer
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble

/**
 *
 */
class RainLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Rain Layer"
  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {

    return ChartingDemo {
      meistercharts {

        configure {
          layers.addClearBackground()
          val layer = RainLayer(RainLayer.Data(3))
          layers.addLayer(layer)

          configurableDouble("Space horizontal", layer.style::spaceHorizontalPerDrop) {
            min = 1.0
            max = 200.0
          }
          configurableDouble("Space vertical", layer.style::spaceVerticalPerDrop) {
            min = 1.0
            max = 200.0
          }
        }

      }
    }
  }
}
