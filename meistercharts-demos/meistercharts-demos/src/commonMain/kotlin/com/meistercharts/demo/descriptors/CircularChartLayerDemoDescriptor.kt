package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.axis.AxisOrientationY
import com.meistercharts.algorithms.impl.ZoomAndTranslationDefaults
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.circular.CircularChartLayer
import com.meistercharts.algorithms.layers.circular.FixedPixelsGap
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import it.neckar.open.provider.DefaultDoublesProvider

/**
 */
class CircularChartLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Circular Chart Layer"

  //language=HTML
  override val description: String = "## Circular Chart Layer"
  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        zoomAndTranslationDefaults(ZoomAndTranslationDefaults.tenPercentMargin)


        configure {
          chartSupport.rootChartState.axisOrientationY = AxisOrientationY.OriginAtTop

          layers.addClearBackground()
          //layers.addLayer(ContentAreaDebugLayer())

          val layer = CircularChartLayer(DefaultDoublesProvider(createCircularChartValues(4)))
          layers.addLayer(layer)

          configurableDouble("Max Diameter", layer.style::maxDiameter) {
            max = 500.0
          }

          configurableDouble("Inner Circle Width", layer.style::innerCircleWidth) {
            max = 50.0
          }

          configurableDouble("Gap Inner/Outer", layer.style::gapInnerOuter) {
            max = 50.0
          }

          configurableDouble("Outer circle width", layer.style::outerCircleWidth) {
            max = 50.0
          }

          configurableDouble("Outer circle value gap (px)", (layer.style.outerCircleValueGap as? FixedPixelsGap)?.gap ?: 2.0) {
            value = 3.0
            max = 50.0

            onChange {
              layer.style.outerCircleValueGapPixels(it)
              markAsDirty()
            }
          }
        }
      }
    }
  }
}
