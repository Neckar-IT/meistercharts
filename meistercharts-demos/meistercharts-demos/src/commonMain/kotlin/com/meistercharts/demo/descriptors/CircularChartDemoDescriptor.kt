package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.axis.AxisOrientationY
import com.meistercharts.algorithms.impl.ZoomAndTranslationDefaults
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.circular.CircularChartLayer
import com.meistercharts.algorithms.layers.circular.CircularChartLegendLayer
import com.meistercharts.algorithms.layers.circular.FixedPixelsGap
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableInt
import com.meistercharts.demo.configurableSize
import it.neckar.open.kotlin.lang.random
import it.neckar.open.kotlin.lang.toRelativeValues
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.MutableDoublesProvider
import it.neckar.open.i18n.TextKey
import com.meistercharts.resources.Icons

/**
 */
class CircularChartDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Circular Chart"

  //language=HTML
  override val description: String = "## Circular Chart"
  override val category: DemoCategory = DemoCategory.Layers

  private val trafficLightColors = listOf(
    Color.web("#339900"), // green
    Color.web("#ffcc00"), // yellow
    Color.web("#cc3300"), // red
    Color.web("#878787")  // gray
  )

  private val icons = listOf(
    Icons.ok(fill = trafficLightColors[0]),
    Icons.warning(fill = trafficLightColors[1]),
    Icons.error(fill = trafficLightColors[2]),
    Icons.questionmark(fill = trafficLightColors[3]),

    Icons.autoScale(),
    Icons.drag(),
    Icons.end(),
    Icons.first(),
    Icons.home(),
    Icons.last(),
    Icons.legend(),
    Icons.noAutoScale(),
    Icons.noLegend(),
    Icons.pause(),
    Icons.play(),
    Icons.resetZoom(),
    Icons.start(),
    Icons.timestampsAbsolute(),
    Icons.timestampsRelative(),
    Icons.trash(),
    Icons.visibility(),
    Icons.yAxis(),
    Icons.zoomIn(),
    Icons.zoomOut()
  )


  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {

        zoomAndTranslationDefaults(ZoomAndTranslationDefaults.tenPercentMargin)


        configure {
          chartSupport.rootChartState.axisOrientationY = AxisOrientationY.OriginAtTop

          layers.addClearBackground()
          //layers.addLayer(ContentAreaDebugLayer())

          val valuesProvider = MutableDoublesProvider().apply { addAll(createCircularChartValues(4)) }

          val layer = CircularChartLayer(valuesProvider) {
            segmentsColorProvider = MultiProvider.forListModulo(trafficLightColors)
            outerCircleWidth = 23.0
            gapInnerOuter = 0.0
            innerCircleWidth = 0.0
            outerCircleValueGap = FixedPixelsGap(4.0)
          }
          val legendLayer = CircularChartLegendLayer(CircularChartLegendLayer.Data(valuesProvider)) {
            segmentsImageProvider = MultiProvider.forListModulo(icons)
            segmentsLabelProvider = MultiProvider {
              TextKey.simple("Segment $it")
            }
          }
          layers.addLayer(layer)
          layers.addLayer(legendLayer)

          configurableDouble("Max Diameter", layer.style::maxDiameter) {
            max = 1000.0
          }

          configurableInt("Model size") {
            value = 4
            onChange {
              valuesProvider.clear()
              valuesProvider.addAll(createCircularChartValues(it))
              markAsDirty()
            }
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

          configurableDouble("Outer circle value gap (pixels)", (layer.style.outerCircleValueGap as? FixedPixelsGap)?.gap ?: 2.0) {
            value = 2.0
            max = 50.0

            onChange {
              layer.style.outerCircleValueGapPixels(it)
              markAsDirty()
            }
          }

          declare {
            section("Legend") {}
          }

          configurableSize("Image Size", legendLayer.style.paintableSize) {
            onChange {
              legendLayer.style.paintableSize = it
              markAsDirty()
            }
          }

          configurableFont("Legend Font", legendLayer.style.font) {
            onChange {
              legendLayer.style.font = it
              markAsDirty()
            }
          }
        }
      }
    }
  }

}

fun createCircularChartValues(segmentCount: Int): List<Double> {
  if (segmentCount < 1) {
    return listOf()
  }
  return IntRange(0, segmentCount - 1)
    .map { random.nextDouble() }
    .toList()
    .toRelativeValues()
}

