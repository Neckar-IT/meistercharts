package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.algorithms.layers.debug.ZoomAndTranslationDebugLayer
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoConfiguration
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.section

/**
 *
 */
class ZoomFitDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Zoom/Pan Fit"
  override val category: DemoCategory = DemoCategory.Calculations

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          layers.addLayer(ContentAreaDebugLayer())
          layers.addLayer(ZoomAndTranslationDebugLayer())

          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {

            }
          })

          section("Fit X")
          declare {
            fitXButton(it.chartSupport, 0.0, 1.0)
            fitXButton(it.chartSupport, 1.0, 2.0)
            fitXButton(it.chartSupport, 0.0, 2.0)
            fitXButton(it.chartSupport, 0.5, 1.0)
            fitXButton(it.chartSupport, 0.0, 0.5)
            fitXButton(it.chartSupport, -0.5, 0.5)
            fitXButton(it.chartSupport, -1.0, 0.0)
          }

          @DomainRelative var beginX = 0.0
          @DomainRelative var endX = 1.0

          configurableDouble("Begin X", beginX) {
            min = -2.0
            max = 2.0

            onChange {
              beginX = it
              chartSupport.zoomAndTranslationSupport.fitX(beginX, endX)
            }
          }
          configurableDouble("End X", endX) {
            min = -2.0
            max = 2.0

            onChange {
              endX = it
              chartSupport.zoomAndTranslationSupport.fitX(beginX, endX)
            }
          }
          section("Fit Y")
          declare {
            fitYButton(it.chartSupport, 0.0, 1.0)
            fitYButton(it.chartSupport, 1.0, 2.0)
            fitYButton(it.chartSupport, 0.0, 2.0)
            fitYButton(it.chartSupport, 0.5, 1.0)
            fitYButton(it.chartSupport, 0.0, 0.5)
            fitYButton(it.chartSupport, -0.5, 0.5)
            fitYButton(it.chartSupport, -1.0, 0.0)
          }

          @DomainRelative var beginY = 0.0
          @DomainRelative var endY = 1.0

          configurableDouble("Begin Y", beginY) {
            min = -2.0
            max = 2.0

            onChange {
              beginY = it
              chartSupport.zoomAndTranslationSupport.fitY(beginY, endY)
            }
          }
          configurableDouble("End Y", endY) {
            min = -2.0
            max = 2.0

            onChange {
              endY = it
              chartSupport.zoomAndTranslationSupport.fitY(beginY, endY)
            }
          }

        }
      }
    }
  }
}

private fun DemoConfiguration.fitXButton(chartSupport: ChartSupport, begin: Double, end: Double) {
  button("$begin / $end") {
    if (chartSupport.currentChartState.axisOrientationX.axisInverted) {
      chartSupport.zoomAndTranslationSupport.fitX(end, begin)
    } else {
      chartSupport.zoomAndTranslationSupport.fitX(begin, end)
    }
  }
}

private fun DemoConfiguration.fitYButton(chartSupport: ChartSupport, begin: Double, end: Double) {
  button("$begin / $end") {
    if (chartSupport.currentChartState.axisOrientationY.axisInverted) {
      chartSupport.zoomAndTranslationSupport.fitY(end, begin)
    } else {
      chartSupport.zoomAndTranslationSupport.fitY(begin, end)
    }
  }
}
