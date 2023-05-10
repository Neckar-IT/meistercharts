/**
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
