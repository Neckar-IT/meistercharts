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

import com.meistercharts.algorithms.impl.ZoomAndTranslationDefaults
import com.meistercharts.algorithms.layers.AxisTopTopTitleLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableColorPickerProvider
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableDoubleProvider
import com.meistercharts.demo.configurableEnumProvider
import com.meistercharts.demo.configurableFont

/**
 * A simple hello world demo.
 *
 * Can be used as template to create new demos
 */
class ValueAxisTopTopTitleLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Value Axis Top Top Title Layer"

  //language=HTML
  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {

        zoomAndTranslationDefaults {
          ZoomAndTranslationDefaults.tenPercentMargin
        }

        configure {
          debug.set(DebugFeature.ShowAnchors, true)

          layers.addClearBackground()

          val config = object {
            var x = 50.0
            var y = 70.0
          }

          val layer = AxisTopTopTitleLayer({ config.x }, { config.y }, { _, _ -> "The title!" })
          layers.addLayer(layer)

          configurableDouble("X", config::x) {
            max = 200.0
          }

          configurableDouble("Y", config::y) {
            max = 200.0
          }
          configurableDouble("Gap Horizontal", layer.configuration::titleGapHorizontal) {
            max = 50.0
          }
          configurableDouble("Gap Vertical", layer.configuration::titleGapVertical) {
            max = 50.0
          }

          configurableDoubleProvider("Title Max Width", layer.configuration::titleMaxWidth) {
            max = 250.0
          }

          configurableEnumProvider("Anchor Direction", layer.configuration::anchorDirection)

          configurableColorPickerProvider("Title Color", layer.configuration::titleColor)
          configurableFont("Title Font", layer.configuration::titleFont)
        }
      }
    }
  }
}
