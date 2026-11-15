/*
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.demo.descriptors

import com.meistercharts.demo.configurable
import com.meistercharts.demo.section
import kotlin.uuid.Uuid
import it.neckar.geometry.AxisOrientationY
import com.meistercharts.zoom.ZoomAndTranslationDefaults
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.circular.CircularChartLayer
import com.meistercharts.algorithms.layers.circular.CircularChartLegendLayer
import com.meistercharts.algorithms.layers.circular.FixedPixelsGap
import com.meistercharts.color.Color
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
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
import it.neckar.open.kotlin.lang.asProvider

/**
 */
class CircularChartDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("dd89f314-710b-4463-a306-e47b4b022dd9")
  override val name: String = "Circular Chart"

  //language=HTML
  override val description: String = "A circular chart"
  override val category: DemoCategory = DemoCategory.Layers
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  private val trafficLightColors = listOf(
    Color.web("#339900"), // green
    Color.web("#ffcc00"), // yellow
    Color.web("#cc3300"), // red
    Color.web("#878787")  // gray
  )

  private val icons = listOf(
    Icons.ok(fill = trafficLightColors[0].asProvider()),
    Icons.warning(fill = trafficLightColors[1].asProvider()),
    Icons.error(fill = trafficLightColors[2].asProvider()),
    Icons.questionmark(fill = trafficLightColors[3].asProvider()),

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


  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
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
          val legendLayer = CircularChartLegendLayer(valuesProvider) {
            segmentsImageProvider = MultiProvider.forListModulo(icons)
            segmentsLabelProvider = MultiProvider {
              TextKey.simple("Segment $it")
            }
          }
          layers.addLayer(layer)
          layers.addLayer(legendLayer)

          configurable("Max Diameter", layer.configuration::maxDiameter) {
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

          configurable("Inner Circle Width", layer.configuration::innerCircleWidth) {
            max = 50.0
          }

          configurable("Gap Inner/Outer", layer.configuration::gapInnerOuter) {
            max = 50.0
          }

          configurable("Outer circle width", layer.configuration::outerCircleWidth) {
            max = 50.0
          }

          configurableDouble("Outer circle value gap (pixels)", (layer.configuration.outerCircleValueGap as? FixedPixelsGap)?.gap ?: 2.0) {
            value = 2.0
            max = 50.0

            onChange {
              layer.configuration.outerCircleValueGapPixels(it)
              markAsDirty()
            }
          }

          section("Legend")

          configurableSize("Image Size", legendLayer.configuration.paintableSize) {
            onChange {
              legendLayer.configuration.paintableSize = it
              markAsDirty()
            }
          }

          configurableFont("Legend Font", legendLayer.configuration.font) {
            onChange {
              legendLayer.configuration.font = it
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

