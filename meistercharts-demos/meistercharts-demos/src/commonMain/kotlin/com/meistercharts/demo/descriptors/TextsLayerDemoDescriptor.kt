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
import com.meistercharts.algorithms.layers.text.TextsLayer
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.paintMark
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableColor
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import it.neckar.open.kotlin.lang.fastFor

/**
 * Very simple demo that shows how to work with a value axis layer
 */
class TextsLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Texts Demo"
  override val category: DemoCategory = DemoCategory.Text

  //language=HTML
  override val description: String = "<h3>Places a list of texts on the specified coordinates</h3>"

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      val textsLayer = TextsLayer()

      meistercharts {
        configure {
          layers.addClearBackground()
          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Background

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              textsLayer.data.texts.size().fastFor {
                val location = textsLayer.data.locationProvider.valueAt(it)
                gc.paintMark(location, color = Color.silver)
              }
            }
          })

          layers.addLayer(textsLayer)

          declare {
            section("Layout")
          }

          configurableEnum("Anchor Direction", textsLayer.style::anchorDirection)
          configurableDouble("Anchor Gap Horizontal", textsLayer.style::anchorGapHorizontal) {
            max = 100.0
          }
          configurableDouble("Anchor Gap Vertical", textsLayer.style::anchorGapVertical) {
            max = 100.0
          }
          configurableFont("Font", textsLayer.style::font)
          configurableColor("Text Color", textsLayer.style::textColor)
        }
      }
    }
  }
}
