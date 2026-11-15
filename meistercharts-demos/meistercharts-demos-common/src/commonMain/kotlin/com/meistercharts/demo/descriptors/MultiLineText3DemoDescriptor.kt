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
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.WindowDebugLayer
import com.meistercharts.algorithms.layers.text.addTextUnresolved
import com.meistercharts.color.Color
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.canvas.text.LineSpacing
import com.meistercharts.canvas.paintLocation
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
import it.neckar.geometry.Distance
import it.neckar.geometry.HorizontalAlignment
import com.meistercharts.model.Insets
import com.meistercharts.style.BoxStyle
import it.neckar.open.kotlin.lang.asProvider

/**
 * Demos that visualizes the functionality of a multi line text
 */
class MultiLineText3DemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("f9380a21-4cff-44ab-851e-4816f698e3d8")
  override val name: String = "Multi line text 3"
  override val description: String = "A single multi line text, fully configurable"
  override val category: DemoCategory = DemoCategory.Text
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  private val texts = listOf("FirstLineQQQggggg", "SecondLineQQQggggg", "ThirdLineQQQggggg", "FourthLineQQQQWWWgggg", "FifthLineQQQQWWWgggg", "SixthLineQQQQWWWgggg")

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {

        configure {
          layers.addClearBackground()

          layers.addLayer(WindowDebugLayer())

          val layer = layers.addTextUnresolved(texts) {
            margin = Insets.of(3.0)
            boxStyle = BoxStyle(Color.white, Color.darkgrey, padding = Insets.empty)
            font = FontDescriptorFragment(14.0).asProvider()
            lineSpacing = LineSpacing(1.0)
          }

          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              layer.configuration.anchorPointProvider.calculateBasePoint(gc.boundingBox).let {
                gc.paintLocation(it.x, it.y, Color.darkolivegreen())
              }
            }
          })

          var anchorPointProviderDirection = Direction.Center
          var anchorPointProviderDistanceX = 0.0
          var anchorPointProviderDistanceY = 0.0

          configurableEnum("Anchor Point Provider Direction", anchorPointProviderDirection, kotlin.enums.enumEntries<Direction>().toList()) {
            onChange {
              anchorPointProviderDirection = it

              layer.configuration.anchorPointProvider = DirectionBasedBasePointProvider(anchorPointProviderDirection, Distance.of(anchorPointProviderDistanceX, anchorPointProviderDistanceY))
              markAsDirty()

            }
          }

          configurableDouble("Anchor Point Provider Translation X", anchorPointProviderDistanceX) {
            min = -2000.0
            max = 2000.0
            onChange {
              anchorPointProviderDistanceX = it
              layer.configuration.anchorPointProvider = DirectionBasedBasePointProvider(anchorPointProviderDirection, Distance.of(anchorPointProviderDistanceX, anchorPointProviderDistanceY))
              markAsDirty()
            }
          }

          configurableDouble("Anchor Point Provider Translation Y", anchorPointProviderDistanceY) {
            min = -2000.0
            max = 2000.0
            onChange {
              anchorPointProviderDistanceY = it
              layer.configuration.anchorPointProvider = DirectionBasedBasePointProvider(anchorPointProviderDirection, Distance.of(anchorPointProviderDistanceY, anchorPointProviderDistanceY))
              markAsDirty()
            }
          }

          configurableEnum("Anchor Direction", layer.configuration.anchorDirection, kotlin.enums.enumEntries<Direction>().toList()) {
            onChange {
              layer.configuration.anchorDirection = it
              markAsDirty()
            }
          }

          configurable("Anchor Gap Horizontal", layer.configuration::anchorGapHorizontal) {
            max = 50.0
          }
          configurable("Anchor Gap Vertical", layer.configuration::anchorGapVertical) {
            max = 50.0
          }

          configurableDouble("Line Spacing", layer.configuration.lineSpacing.percentage) {
            min = 0.5
            max = 2.0

            onChange { newValue ->
              layer.configuration.lineSpacing = LineSpacing(newValue)
              markAsDirty()
            }
          }

          configurableDouble(
            "Box Padding (px)",
            layer.configuration.boxStyle.padding.left
          ) {
            max = 20.0

            onChange { newValue ->
              layer.configuration.boxStyle.padding = Insets.of(newValue)
              markAsDirty()
            }
          }

          configurableEnum(
            "Text alignment",
            initial = layer.configuration.horizontalAlignment,
            possibleValues = HorizontalAlignment.entries
          ) {
            onChange { newValue ->
              layer.configuration.horizontalAlignment = newValue
              markAsDirty()
            }
          }

          configurable("Font", layer.configuration::font) {
          }
        }
      }
    }
  }
}
