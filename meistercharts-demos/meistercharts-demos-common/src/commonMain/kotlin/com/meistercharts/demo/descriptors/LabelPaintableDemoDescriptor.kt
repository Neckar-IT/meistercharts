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
import com.meistercharts.canvas.paintable.LabelPaintable
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.stroke
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableColorPickerProvider
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.section

class LabelPaintableDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("38f0da0f-f0f3-4f41-a36a-f75883dad4b4")

  override val name: String = "Label Paintable"

  //language=HTML
  override val category: DemoCategory = DemoCategory.Paintables
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {

        val text = "Hello World!qx°A " + "xHpÁÄgqÅÁqÜgÖfÄPß " + "與朋友交而不信乎" + " 제는 보장"

        val paintable = LabelPaintable({ _, _ ->
          text
        }) {
        }

        configure {
          layers.addClearBackground()

          val layer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            var x: Double = 100.0
            var y: Double = 100.0

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

          section("Anchor")
          configurable("Anchor Direction", paintable.style::anchorDirection)
          configurable("Horizontal Gap", paintable.style::anchorGapHorizontal) {
            min = -100.0
            max = 100.0
          }
          configurable("Vertical Gap", paintable.style::anchorGapVertical) {
            min = -100.0
            max = 100.0
          }
          configurable("Max Width", paintable.style::maxWidth) {
            max = 200.0
          }

          configurable("Text Color", paintable.style::labelColor)
          configurable("Label", paintable.style::font)

          configurableList("Text", text, listOf(text, "", null, "Hello World")) {
            onChange {
              paintable.style.label = { _, _ -> it }
              markAsDirty()
            }
          }
        }
      }
    }
  }
}
