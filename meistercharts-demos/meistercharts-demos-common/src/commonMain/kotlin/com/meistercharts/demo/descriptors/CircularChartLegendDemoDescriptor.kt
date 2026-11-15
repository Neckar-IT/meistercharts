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
import com.meistercharts.algorithms.layers.circular.CircularChartLegendLayer
import com.meistercharts.charts.CircularChartGestalt
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableSize
import it.neckar.open.provider.DefaultDoublesProvider

/**
 *
 */
class CircularChartLegendDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("442fedb6-0742-4075-894b-dfa217c6e59f")
  override val name: String = "Circular Chart Legend"

  //language=HTML
  override val description: String = "A legend for a circular chart"
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

          val layer = CircularChartLegendLayer(DefaultDoublesProvider(createCircularChartValues(4))) {
            segmentsImageProvider = CircularChartGestalt.createDefaultImageProvider(CircularChartGestalt.createDefaultColorsProvider())
            segmentsLabelProvider = CircularChartGestalt.createDefaultLabelProvider()
          }
          layers.addLayer(layer)

          configurableSize("Image Size", layer.configuration.paintableSize) {
            onChange {
              layer.configuration.paintableSize = it
              markAsDirty()
            }
          }

          configurableFont("Font", layer.configuration.font) {
            onChange {
              layer.configuration.font = it
              markAsDirty()
            }
          }

          configurable("Show Caption", layer.configuration::showCaption) {
          }
        }
      }
    }
  }
}
