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

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.TextLayer
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableFont
import com.meistercharts.model.Insets
import com.meistercharts.style.BoxStyle

/**
 */
class ConfigurableFontDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Configurable Fonts"

  //language=HTML
  override val description: String = "## Different fonts"
  override val category: DemoCategory = DemoCategory.Text

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          val layer = TextLayer({ _, _ -> listOf("The quick brown fox jumps over the lazy dog BpÅÁqÜgÖfÄPqLT") }) {
            margin = Insets.of(10.0)
            boxStyle = BoxStyle(Color.white, Color.gray, padding = Insets.of(5.0))
          }.also {
            layers.addLayer(it)
          }

          configurableFont("Font", layer.style.font) {
            onChange {
              layer.style.font = it
              markAsDirty()
            }
          }
        }
      }
    }
  }
}
