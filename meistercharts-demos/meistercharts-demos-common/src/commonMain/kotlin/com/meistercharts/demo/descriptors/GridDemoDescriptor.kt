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


import kotlin.uuid.Uuid
import com.meistercharts.zoom.ZoomAndTranslationDefaults
import com.meistercharts.algorithms.layers.DomainRelativeGridLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.linechart.Dashes
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableColor
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnumProvider
import com.meistercharts.demo.configurableList
import com.meistercharts.model.Insets
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.kotlin.lang.asProvider1
import it.neckar.open.provider.DoublesProvider

/**
 */
class GridDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("6171b92d-0168-463f-98e1-e49d11f40eb0")
  override val name: String = "Grid"
  override val description: String = "Shows a configurable grid"
  override val category: DemoCategory = DemoCategory.Layers
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        zoomAndTranslationDefaults {
          ZoomAndTranslationDefaults.tenPercentMargin
        }

        configure {
          layers.addClearBackground()
          val gridLayer = DomainRelativeGridLayer(
            DoublesProvider.forValues(
              0.0, 0.1, 0.3, 0.4, 0.5, 1.0
            )
          )
          layers.addLayer(
            gridLayer
          )

          var (gridColor, gridLineWidth, gridLineStyle) = gridLayer.configuration.lineStyles(0.0)

          configurableEnumProvider("Orientation", gridLayer.configuration::orientationProvider)


          configurableColor("Grid Color", gridColor()) {
            onChange {
              gridColor = it.asProvider()
              gridLayer.configuration.lineStyles = LineStyle(color = gridColor, lineWidth = gridLineWidth, dashes = gridLineStyle).asProvider1()
              markAsDirty()
            }
          }

          configurableDouble("Line Width", gridLineWidth) {
            max = 10.0

            onChange {
              gridLineWidth = it
              gridLayer.configuration.lineStyles = LineStyle(color = gridColor, lineWidth = gridLineWidth, dashes = gridLineStyle).asProvider1()
              markAsDirty()
            }
          }

          configurableList("Line style", gridLineStyle, Dashes.predefined) {
            onChange {
              gridLineStyle = it
              gridLayer.configuration.lineStyles = LineStyle(color = gridColor, lineWidth = gridLineWidth, dashes = gridLineStyle).asProvider1()
              markAsDirty()
            }
          }

          configurableDouble("Passpartout", gridLayer.configuration.passpartout.left) {
            max = 100.0

            onChange {
              gridLayer.configuration.passpartout = Insets.of(it)
              markAsDirty()
            }
          }
        }
      }
    }
  }
}
