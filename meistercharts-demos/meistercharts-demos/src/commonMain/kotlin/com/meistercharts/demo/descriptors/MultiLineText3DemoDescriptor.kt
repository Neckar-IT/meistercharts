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

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.WindowDebugLayer
import com.meistercharts.algorithms.layers.text.addTextUnresolved
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.LineSpacing
import com.meistercharts.canvas.paintLocation
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.model.Direction
import com.meistercharts.model.DirectionBasedBasePointProvider
import com.meistercharts.model.Distance
import com.meistercharts.model.HorizontalAlignment
import com.meistercharts.model.Insets
import com.meistercharts.style.BoxStyle

/**
 * Demos that visualizes the functionality of a multi line text
 */
class MultiLineText3DemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Multi line text 3"
  override val description: String = "## Single text - all configurable"
  override val category: DemoCategory = DemoCategory.Text

  private val texts = listOf("FirstLineQQQggggg", "SecondLineQQQggggg", "ThirdLineQQQggggg", "FourthLineQQQQWWWgggg", "FifthLineQQQQWWWgggg", "SixthLineQQQQWWWgggg")

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {

        configure {
          layers.addClearBackground()

          layers.addLayer(WindowDebugLayer())

          val layer = layers.addTextUnresolved(texts) {
            margin = Insets.of(3.0)
            boxStyle = BoxStyle(Color.white, Color.darkgrey, padding = Insets.empty)
            font = FontDescriptorFragment(14.0)
            lineSpacing = LineSpacing(1.0)
          }

          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              layer.style.anchorPointProvider.calculateBasePoint(gc.boundingBox).let {
                gc.paintLocation(it.x, it.y, Color.darkolivegreen)
              }
            }
          })

          var anchorPointProviderDirection = Direction.Center
          var anchorPointProviderDistanceX = 0.0
          var anchorPointProviderDistanceY = 0.0

          configurableEnum("Anchor Point Provider Direction", anchorPointProviderDirection, enumValues()) {
            onChange {
              anchorPointProviderDirection = it

              layer.style.anchorPointProvider = DirectionBasedBasePointProvider(anchorPointProviderDirection, Distance.of(anchorPointProviderDistanceX, anchorPointProviderDistanceY))
              markAsDirty()

            }
          }

          configurableDouble("Anchor Point Provider Translation X", anchorPointProviderDistanceX) {
            min = -2000.0
            max = 2000.0
            onChange {
              anchorPointProviderDistanceX = it
              layer.style.anchorPointProvider = DirectionBasedBasePointProvider(anchorPointProviderDirection, Distance.of(anchorPointProviderDistanceX, anchorPointProviderDistanceY))
              markAsDirty()
            }
          }

          configurableDouble("Anchor Point Provider Translation Y", anchorPointProviderDistanceY) {
            min = -2000.0
            max = 2000.0
            onChange {
              anchorPointProviderDistanceY = it
              layer.style.anchorPointProvider = DirectionBasedBasePointProvider(anchorPointProviderDirection, Distance.of(anchorPointProviderDistanceY, anchorPointProviderDistanceY))
              markAsDirty()
            }
          }

          configurableEnum("Anchor Direction", layer.style.anchorDirection, enumValues()) {
            onChange {
              layer.style.anchorDirection = it
              markAsDirty()
            }
          }

          configurableDouble("Anchor Gap Horizontal", layer.style::anchorGapHorizontal) {
            max = 50.0
          }
          configurableDouble("Anchor Gap Vertical", layer.style::anchorGapVertical) {
            max = 50.0
          }

          configurableDouble("Line Spacing", layer.style.lineSpacing.percentage) {
            min = 0.5
            max = 2.0

            onChange { newValue ->
              layer.style.lineSpacing = LineSpacing(newValue)
              markAsDirty()
            }
          }

          configurableDouble(
            "Box Padding (px)",
            layer.style.boxStyle.padding.left
          ) {
            max = 20.0

            onChange { newValue ->
              layer.style.boxStyle.padding = Insets.of(newValue)
              markAsDirty()
            }
          }

          configurableEnum(
            "Text alignment",
            initial = layer.style.horizontalAlignment,
            possibleValues = HorizontalAlignment.values()
          ) {
            onChange { newValue ->
              layer.style.horizontalAlignment = newValue
              markAsDirty()
            }
          }

          configurableFont("Font", layer.style::font) {
          }
        }
      }
    }
  }
}
