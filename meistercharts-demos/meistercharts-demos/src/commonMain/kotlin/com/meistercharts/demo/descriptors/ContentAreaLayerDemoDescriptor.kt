package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.impl.ZoomAndTranslationDefaults
import com.meistercharts.algorithms.layers.ContentAreaLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColor

/**
 * A demo for the [ContentAreaLayer]
 *
 */
class ContentAreaLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Content Area Layer Border"

  //language=HTML
  override val description: String = "## Shows a value area"
  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        zoomAndTranslationDefaults {
          ZoomAndTranslationDefaults.tenPercentMargin
        }

        configure {
          layers.addClearBackground()
          val contentAreaLayer = ContentAreaLayer()
          layers.addLayer(contentAreaLayer)

          configurableBoolean("Left side") {
            value = contentAreaLayer.style.sidesToPaint.leftSelected
            onChange {
              contentAreaLayer.style.sidesToPaint = contentAreaLayer.style.sidesToPaint.copy(leftSelected = it)
              markAsDirty()
            }
          }

          configurableBoolean("Top side") {
            value = contentAreaLayer.style.sidesToPaint.topSelected
            onChange {
              contentAreaLayer.style.sidesToPaint = contentAreaLayer.style.sidesToPaint.copy(topSelected = it)
              markAsDirty()
            }
          }

          configurableBoolean("Right side") {
            value = contentAreaLayer.style.sidesToPaint.rightSelected
            onChange {
              contentAreaLayer.style.sidesToPaint = contentAreaLayer.style.sidesToPaint.copy(rightSelected = it)
              markAsDirty()
            }
          }

          configurableBoolean("Bottom side") {
            value = contentAreaLayer.style.sidesToPaint.bottomSelected
            onChange {
              contentAreaLayer.style.sidesToPaint = contentAreaLayer.style.sidesToPaint.copy(bottomSelected = it)
              markAsDirty()
            }
          }

          configurableColor("Stroke", contentAreaLayer.style::color) {
            onChange {
              contentAreaLayer.style.color = it
              markAsDirty()
            }
          }
        }
      }
    }
  }
}
