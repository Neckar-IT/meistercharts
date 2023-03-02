package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration

/**
 *
 */
class RubberBandZoomDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Rubber Band Zoom"
  override val category: DemoCategory = DemoCategory.Interaction

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        zoomAndTranslationConfiguration {
          enableRubberBandZoom()
        }

        configure {
          layers.addClearBackground()
        }
      }
    }
  }
}
