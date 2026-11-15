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
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.canvas.saved
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableFont
import it.neckar.geometry.Direction

/**
 *
 */
class TextTickAlignmentDemoDescriptor : MeisterchartsDemoDescriptor<Any> {
  override val uuid: Uuid = Uuid.parse("b4c82f46-6db6-4594-9b36-5c2c6ed1e2fd")
  override val name: String = "Tick/Text Alignment"
  override val category: DemoCategory = DemoCategory.Text
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override val description: String = "Visualizes tick and text alignments."

  override fun prepareDemo(configuration: PredefinedConfiguration<Any>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()

          val myLayer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            val possibleChars = listOf(
              "\u2022",
              "\u00b7",
              "\u002d",
              "\u1806",
              "\u2010",
              "\u2011",
              "\u2027",
              "\u2043",
              "\ufe63",
              "\uff0d"
            )

            var fontDescriptor: FontDescriptorFragment = FontDescriptorFragment.empty

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.font(fontDescriptor)

              val translationForNextLine = fontDescriptor.size?.size ?: 10.0 * 1.2
              gc.translate(40.0, 20.0)

              gc.saved {
                possibleChars.forEach {
                  gc.translate(0.0, 10.0 + translationForNextLine)
                  gc.fillText("${it}3.0", 0.0, 0.0, Direction.CenterLeft)
                }
              }

              //Left side with alignment center to tick
              gc.translate(150.0, 10 + translationForNextLine)
              gc.strokeLine(-10.0, 0.0, 0.0, 0.0)
              gc.fillText("3.0", 0.0, 0.0, Direction.CenterLeft)

              gc.translate(0.0, 10.0 + translationForNextLine)
              gc.strokeLine(-10.0, 0.0, 0.0, 0.0)
              gc.fillText("Hello", 0.0, 0.0, Direction.CenterLeft)

              gc.translate(0.0, 10.0 + translationForNextLine)
              gc.strokeLine(-10.0, 0.0, 0.0, 0.0)
              gc.fillText("Á", 0.0, 0.0, Direction.CenterLeft)
            }
          }
          layers.addLayer(myLayer)

          configurable("Font", myLayer::fontDescriptor)
        }
      }
    }
  }
}
