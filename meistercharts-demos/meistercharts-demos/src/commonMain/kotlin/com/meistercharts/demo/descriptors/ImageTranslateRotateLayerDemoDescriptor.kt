package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.ResetToDefaultsOnWindowResize
import com.meistercharts.algorithms.impl.ZoomAndTranslationDefaults
import com.meistercharts.algorithms.layers.PaintableTranslateRotateLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.addShowZoomLevel
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.model.Direction
import com.meistercharts.model.Size
import com.meistercharts.resources.Icons

class ImageTranslateRotateLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {

  override val name: String = "Image Translate Rotate Layer Demo"
  override val description: String = "Image Translate Rotate Layer Demo"
  override val category: DemoCategory = DemoCategory.Layers


  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {

        zoomAndTranslationDefaults {
          ZoomAndTranslationDefaults.tenPercentMargin
        }

        val image = Icons.autoScale(Size.PX_120)

        configure {
          chartSupport.windowResizeBehavior = ResetToDefaultsOnWindowResize

          layers.addClearBackground()
          layers.addLayer(ContentAreaDebugLayer())

          var angle = 17.0
          @DomainRelative var x = 0.3
          @DomainRelative var y = 0.4

          val layer = PaintableTranslateRotateLayer(
            PaintableTranslateRotateLayer.Data(
              angle = { angle },
              image = { image },
              x = { x },
              y = { y }
            )
          ) {
            this.direction = Direction.Center
          }

          layers.addLayer(layer)
          layers.addShowZoomLevel()

          configurableDouble("angle", angle) {
            min = -7.0
            max = 7.0

            onChange {
              angle = it
              markAsDirty()
            }
          }

          configurableDouble("x", x) {
            min = 0.0
            max = 1.0

            onChange {
              x = it
              markAsDirty()
            }
          }

          configurableDouble("y", y) {
            min = 0.0
            max = 1.0

            onChange {
              y = it
              markAsDirty()
            }
          }

          configurableEnum("Direction", layer.style.direction, Direction.values()) {
            onChange {
              layer.style.direction = it
              markAsDirty()
            }
          }
        }
      }
    }
  }
}
