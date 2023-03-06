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
import com.meistercharts.algorithms.layers.text.addText
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.style.BoxStyle

class ChartSizeClassificationDemo : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Chart size classification demo"
  override val category: DemoCategory = DemoCategory.Calculations

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          layers.addText({ _, _ ->
            val sizeClassification = chartSupport.canvas.chartSizeClassification
            listOf(
              "sizeClassification: ${sizeClassification.width} / ${sizeClassification.height}"
            )
          }) {
            boxStyle = BoxStyle(fill = Color.rgba(255, 255, 255, 0.9), borderColor = Color.gray)
            font = FontDescriptorFragment.DefaultSize
          }
        }
      }
    }
  }
}
