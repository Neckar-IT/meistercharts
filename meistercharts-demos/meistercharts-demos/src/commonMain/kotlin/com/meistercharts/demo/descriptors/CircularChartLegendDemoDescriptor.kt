package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.axis.AxisOrientationY
import com.meistercharts.algorithms.impl.ZoomAndTranslationDefaults
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.circular.CircularChartLegendLayer
import com.meistercharts.charts.CircularChartGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableSize
import it.neckar.open.provider.DefaultDoublesProvider

/**
 *
 */
class CircularChartLegendDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Circular Chart Legend"

  //language=HTML
  override val description: String = "## Circular Chart Legend"
  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        zoomAndTranslationDefaults(ZoomAndTranslationDefaults.tenPercentMargin)


        configure {
          chartSupport.rootChartState.axisOrientationY = AxisOrientationY.OriginAtTop

          layers.addClearBackground()
          //layers.addLayer(ContentAreaDebugLayer())

          val layer = CircularChartLegendLayer(DefaultDoublesProvider(createCircularChartValues(4))) {
            segmentsImageProvider = CircularChartGestalt.createDefaultImageProvider(CircularChartGestalt.createDefaultColorsProvider())
            segmentsLabelProvider = CircularChartGestalt.createDefaultLabelProvider()
          }
          layers.addLayer(layer)

          configurableSize("Image Size", layer.style.paintableSize) {
            onChange {
              layer.style.paintableSize = it
              markAsDirty()
            }
          }

          configurableFont("Font", layer.style.font) {
            onChange {
              layer.style.font = it
              markAsDirty()
            }
          }

          configurableBoolean("Show Caption", layer.style::showCaption) {
          }

          //configurableDouble("Inner Circle Width") {
          //  value = layer.style.innerCircleWidth
          //  max = 50.0
          //
          //  onChange {
          //    layer.style.innerCircleWidth = it
          //    markAsDirty { "inner circle updated" }
          //  }
          //}
          //
          //configurableDouble("Gap Inner/Outer") {
          //  value = layer.style.gapInnerOuter
          //  max = 50.0
          //
          //  onChange {
          //    layer.style.gapInnerOuter = it
          //    markAsDirty { "gapInnerOuter updated" }
          //  }
          //}
          //
          //configurableDouble("Outer circle width") {
          //  value = layer.style.outerCircleWidth
          //  max = 50.0
          //
          //  onChange {
          //    layer.style.outerCircleWidth = it
          //    markAsDirty { "outerCircleWidth updated" }
          //  }
          //}
          //
          //configurableDouble("Outer circle value gap") {
          //  value = layer.style.outerCircleValueGap
          //  max = 50.0
          //
          //  onChange {
          //    layer.style.outerCircleValueGap = it
          //    markAsDirty { "outerCircleValueGap updated" }
          //  }
          //}
        }
      }
    }
  }
}
