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
import com.meistercharts.canvas.fill
import com.meistercharts.color.Color
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.paintable.TextBoxPaintable
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.stroke
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDouble

class TextBoxPaintableDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("b6c77da5-3994-4e48-bdb5-e0f7a6ab86d5")

  override val name: String = "Text Box Paintable"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  //language=HTML
  override val category: DemoCategory = DemoCategory.Paintables

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {

        configure {
          layers.addClearBackground()

          val layer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            var x: Double = 100.0
            var y: Double = 100.0

            val texts = listOf("Line 1", "Line 2", "", "after empty Line")

            val paintable = TextBoxPaintable({ _, _ ->
              texts
            }) {
            }

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              val chartSupport = paintingContext.chartSupport

              gc.paintMark(x, y)

              gc.saved {
                val boundingBox = paintable.boundingBox(paintingContext)

                gc.translate(x, y)
                gc.fill(Color.yellow)
                gc.fillRect(boundingBox)

                gc.stroke(Color.orangered)
                gc.strokeRect(boundingBox)
              }

              paintable.paint(paintingContext, x, y)
            }
          }
          layers.addLayer(layer)

          configurable("x", layer::x) {
            max = 400.0
          }
          configurable("y", layer::y) {
            max = 400.0
          }
        }

      }
    }
  }
}
