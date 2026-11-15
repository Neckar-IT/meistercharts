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
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.addTextUnresolved
import com.meistercharts.color.Color
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.canvas.text.LineSpacing
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFontProvider
import it.neckar.geometry.Direction
import com.meistercharts.geometry.DirectionBasedBasePointProvider
import it.neckar.geometry.HorizontalAlignment
import com.meistercharts.model.Insets
import com.meistercharts.style.BoxStyle
import it.neckar.open.kotlin.lang.asProvider

/**
 * Demos that visualizes the functionality of a multi line text
 */
class MultiLineText2DemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("8594d948-d148-421b-bca4-fee28028fef9")
  override val name: String = "Multi line Text 2"
  override val description: String = "Shows multi line text without padding or line spacing"
  override val category: DemoCategory = DemoCategory.Text
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable


  private val texts = listOf("FirstLineQQQggggg", "SecondLineQQQggggg", "ThirdLineQQQggggg", "FourthLineQQQQWWWgggg", "FifthLineQQQQWWWgggg", "SixthLineQQQQWWWgggg")

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {

        configure {
          layers.addClearBackground()

          val layers = Direction.entries.map { anchorDirection ->
            layers.addTextUnresolved(texts) {
              anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
              this.anchorDirection = anchorDirection //ensure the hole text is visible

              margin = Insets.of(3.0)
              boxStyle = BoxStyle(Color.white, Color.darkgrey, padding = Insets.empty)
              font = FontDescriptorFragment(14.0).asProvider()
              lineSpacing = LineSpacing(1.0)
            }
          }.also { list ->
            list.forEach {
              layers.addLayer(it)
            }
          }

          configurableDouble("Line Spacing", layers[0].configuration.lineSpacing.percentage) {
            min = 0.5
            max = 2.0

            onChange { newValue ->
              layers.forEach {
                it.configuration.lineSpacing = LineSpacing(newValue)
              }
              markAsDirty()
            }
          }

          configurableDouble(
            "Box Padding (px)",
            layers[0].configuration.boxStyle.padding.left
          ) {
            max = 20.0

            onChange { newValue ->
              layers.forEach {
                it.configuration.boxStyle.padding = Insets.of(newValue)
              }
              markAsDirty()
            }
          }

          configurableEnum(
            "Text alignment",
            initial = layers[0].configuration.horizontalAlignment,
            possibleValues = HorizontalAlignment.entries
          ) {
            onChange { newValue ->
              layers.forEach {
                it.configuration.horizontalAlignment = newValue
              }
              markAsDirty()
            }
          }

          configurable("Font", layers.first().configuration::font) {
            onChange { fontDescriptor ->
              layers.forEach {
                it.configuration.font = fontDescriptor.asProvider()
              }
              markAsDirty()
            }
          }
        }
      }
    }
  }
}
