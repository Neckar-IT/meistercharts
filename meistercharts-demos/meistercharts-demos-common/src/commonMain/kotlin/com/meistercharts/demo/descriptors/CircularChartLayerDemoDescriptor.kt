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
import kotlin.uuid.Uuid
import it.neckar.geometry.AxisOrientationY
import com.meistercharts.zoom.ZoomAndTranslationDefaults
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.circular.CircularChartLayer
import com.meistercharts.algorithms.layers.circular.FixedPixelsGap
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDouble
import it.neckar.open.provider.DefaultDoublesProvider

/**
 */
class CircularChartLayerDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("57ba11b7-d99f-4870-b5c2-8601b562bba9")
  override val name: String = "Circular Chart Layer"

  //language=HTML
  override val description: String = "The circular chart layer"
  override val category: DemoCategory = DemoCategory.Layers
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        zoomAndTranslationDefaults(ZoomAndTranslationDefaults.tenPercentMargin)


        configure {
          chartSupport.rootChartState.axisOrientationY = AxisOrientationY.OriginAtTop

          layers.addClearBackground()
          //layers.addLayer(ContentAreaDebugLayer())

          val layer = CircularChartLayer(DefaultDoublesProvider(createCircularChartValues(4)))
          layers.addLayer(layer)

          configurable("Max Diameter", layer.configuration::maxDiameter) {
            max = 500.0
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

          configurableDouble("Outer circle value gap (px)", (layer.configuration.outerCircleValueGap as? FixedPixelsGap)?.gap ?: 2.0) {
            value = 3.0
            max = 50.0

            onChange {
              layer.configuration.outerCircleValueGapPixels(it)
              markAsDirty()
            }
          }
        }
      }
    }
  }
}
