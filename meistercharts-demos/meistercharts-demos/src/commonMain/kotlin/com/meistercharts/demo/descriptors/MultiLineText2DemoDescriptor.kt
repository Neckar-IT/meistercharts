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
import com.meistercharts.algorithms.layers.text.addTextUnresolved
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.LineSpacing
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.model.Direction
import com.meistercharts.model.DirectionBasedBasePointProvider
import com.meistercharts.model.HorizontalAlignment
import com.meistercharts.model.Insets
import com.meistercharts.style.BoxStyle

/**
 * Demos that visualizes the functionality of a multi line text
 */
class MultiLineText2DemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Multi line Text 2"
  override val description: String = "## Shows multi line text without padding or line spacing"
  override val category: DemoCategory = DemoCategory.Text

  private val texts = listOf("FirstLineQQQggggg", "SecondLineQQQggggg", "ThirdLineQQQggggg", "FourthLineQQQQWWWgggg", "FifthLineQQQQWWWgggg", "SixthLineQQQQWWWgggg")

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {

        configure {
          layers.addClearBackground()

          val layers = Direction.entries.map { anchorDirection ->
            layers.addTextUnresolved(texts) {
              anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
              this.anchorDirection = anchorDirection //ensure the hole text is visible

              margin = Insets.of(3.0)
              boxStyle = BoxStyle(Color.white, Color.darkgrey, padding = Insets.empty)
              font = FontDescriptorFragment(14.0)
              lineSpacing = LineSpacing(1.0)
            }
          }.also { list ->
            list.forEach {
              layers.addLayer(it)
            }
          }

          configurableDouble("Line Spacing", layers[0].style.lineSpacing.percentage) {
            min = 0.5
            max = 2.0

            onChange { newValue ->
              layers.forEach {
                it.style.lineSpacing = LineSpacing(newValue)
              }
              markAsDirty()
            }
          }

          configurableDouble(
            "Box Padding (px)",
            layers[0].style.boxStyle.padding.left
          ) {
            max = 20.0

            onChange { newValue ->
              layers.forEach {
                it.style.boxStyle.padding = Insets.of(newValue)
              }
              markAsDirty()
            }
          }

          configurableEnum(
            "Text alignment",
            initial = layers[0].style.horizontalAlignment,
            possibleValues = HorizontalAlignment.entries
          ) {
            onChange { newValue ->
              layers.forEach {
                it.style.horizontalAlignment = newValue
              }
              markAsDirty()
            }
          }

          configurableFont("Font", layers.first().style.font) {
            onChange { fontDescriptor ->
              layers.forEach {
                it.style.font = fontDescriptor
              }
              markAsDirty()
            }
          }
        }
      }
    }
  }
}
