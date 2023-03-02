package com.meistercharts.demojs.descriptors

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.addText
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableFont
import com.meistercharts.js.debug.FontMetricsCacheDebugLayer

/**
 */
class FontCacheJSDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "HTML Font Cache"
  override val description: String = "## HTML Font Cache"
  override val category: DemoCategory = DemoCategory.Platform

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          val textLayer = layers.addText { _, _ -> listOf("Hello World") }

          val fontMetricsCacheDebugLayer = FontMetricsCacheDebugLayer()
          layers.addLayer(fontMetricsCacheDebugLayer)

          configurableFont("Font", textLayer.style.font) {
            onChange {
              textLayer.style.font = value
              fontMetricsCacheDebugLayer.style.font = value
              markAsDirty()
            }
          }
        }
      }
    }
  }
}
