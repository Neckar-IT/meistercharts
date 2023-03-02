package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.impl.FittingWithMargin
import com.meistercharts.algorithms.layers.ZeroLinesLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableColor
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.model.Insets

/**
 *
 */
class ZeroLinesLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Zero Lines Layer"

  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        zoomAndTranslationDefaults {
          FittingWithMargin(Insets.Companion.of(20.0))
        }

        configure {
          layers.addClearBackground()

          val zeroLinesLayer = ZeroLinesLayer()
          layers.addLayer(zeroLinesLayer)

          configurableEnum("Axis to paint", zeroLinesLayer.style::axisToPaint, enumValues())
          configurableDouble("Line width", zeroLinesLayer.style::lineWidth) {
            max = 10.0
          }
          configurableColor("Color", zeroLinesLayer.style::color)
        }
      }
    }
  }
}
