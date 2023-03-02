package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.addBackgroundChecker
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableColor
import com.meistercharts.demo.configurableDouble

/**
 * Very simple demo that shows how to clear the background
 */
class FillBackgroundCheckerLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Background checker layer"
  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          val layer = layers.addBackgroundChecker()

          configurableDouble("segment width", layer.style::segmentWith) {
            max = 100.0
          }
          configurableDouble("segment height", layer.style::segmentHeight) {
            max = 100.0
          }

          configurableColor("Color 0", layer.style::background0)
          configurableColor("Color 1", layer.style::background1)
        }
      }
    }
  }
}

