package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addBackgroundChecker
import com.meistercharts.algorithms.layers.addShowLoadingOnMissingResources
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean

/**
 *
 */
class MissingResourceHandlerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Missing Resources Handler"

  override val category: DemoCategory = DemoCategory.LowLevelTests

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addBackgroundChecker()

          val missingResourceLayer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            var resourceMissing = true

            override fun paint(paintingContext: LayerPaintingContext) {
              if (resourceMissing) {
                paintingContext.missingResources.reportMissing("http://blafasel.com")
              }
            }
          }
          layers.addLayer(missingResourceLayer)

          layers.addShowLoadingOnMissingResources()

          configurableBoolean("Resource missing", missingResourceLayer::resourceMissing)
        }
      }
    }
  }
}
