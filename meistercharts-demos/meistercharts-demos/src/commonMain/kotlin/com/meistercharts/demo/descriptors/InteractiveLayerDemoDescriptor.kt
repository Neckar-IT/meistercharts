package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.layer.InteractiveKeyDemoLayer
import com.meistercharts.demo.InteractiveMouseClicksDemoLayer
import com.meistercharts.demo.InteractiveMouseDragsDemoLayer

class InteractiveLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Interactions"
  override val description: String = "## Handling of mouse-click and key events"
  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          /**
           * Mouse clicks result in gray dots
           */
          layers.addLayer(InteractiveMouseClicksDemoLayer())
          layers.addLayer(InteractiveMouseDragsDemoLayer())
          layers.addLayer(InteractiveKeyDemoLayer())
        }
      }
    }
  }
}
