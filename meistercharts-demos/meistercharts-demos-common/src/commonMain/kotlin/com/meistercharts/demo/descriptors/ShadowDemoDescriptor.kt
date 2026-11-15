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
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.canvas.fill
import com.meistercharts.color.Color
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.stroke
import com.meistercharts.color.ColorProvider
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColorProvider
import com.meistercharts.demo.configurableDouble
import it.neckar.geometry.Direction
import com.meistercharts.resources.Icons
import com.meistercharts.style.BoxStyle
import com.meistercharts.style.Shadow

/**
 */
class ShadowDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("17c65972-9cf4-4613-bf04-821180319726")
  override val name: String = "Shadow"

  //language=HTML
  override val category: DemoCategory = DemoCategory.Primitives
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()
          val layer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            var shadowEnabled: Boolean = true
            var clearShadow: Boolean = false
            var shadowColor: ColorProvider = Color.black
            var blurRadius = 10.0
            var offsetX = 0.0
            var offsetY = 0.0

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              if (shadowEnabled) {
                gc.shadow(shadowColor(), blurRadius, offsetX, offsetY)
              }

              if (clearShadow) {
                gc.clearShadow()
              }

              gc.translate(200.0, 150.0)

              gc.fill(Color.darkgray)
              gc.fillRect(0.0, 0.0, 50.0, 58.0)


              gc.translate(0.0, 70.0)
              gc.fill(Color.black)
              gc.fillText("Hello World", 0.0, 0.0, Direction.TopLeft)

              gc.translate(0.0, 20.0)
              gc.stroke(Color.blue)
              gc.strokeText("Hello World", 0.0, 0.0, Direction.TopLeft)

              gc.translate(0.0, 40.0)
              Icons.hourglass(fill = Color.orange).paint(paintingContext)


              gc.translate(0.0, 40.0)
              paintingContext.gc.paintTextBox(listOf("This is a text!", "Shadow.Default"), Direction.TopLeft, boxStyle = BoxStyle(Color.lightgreen, borderColor = Color.red, shadow = Shadow.Default))

              gc.translate(0.0, 80.0)
              paintingContext.gc.paintTextBox(listOf("Shadow.Light"), Direction.TopLeft, boxStyle = BoxStyle(Color.lightgreen, borderColor = Color.red, shadow = Shadow.Light))

              gc.translate(0.0, 40.0)
              paintingContext.gc.paintTextBox(listOf("Shadow.LightDrop"), Direction.TopLeft, boxStyle = BoxStyle(Color.lightgreen, borderColor = Color.red, shadow = Shadow.LightDrop))

              gc.translate(0.0, 40.0)
              paintingContext.gc.paintTextBox(listOf("Shadow.DropHigh"), Direction.TopLeft, boxStyle = BoxStyle(Color.lightgreen, borderColor = Color.red, shadow = Shadow.DropHigh))

              gc.translate(0.0, 40.0)
              paintingContext.gc.paintTextBox(listOf("Shadow.Drop"), Direction.TopLeft, boxStyle = BoxStyle(Color.lightgreen, borderColor = Color.red, shadow = Shadow.Drop))

              gc.translate(0.0, 40.0)
              paintingContext.gc.paintTextBox(listOf("Shadow.DropSmall"), Direction.TopLeft, boxStyle = BoxStyle(Color.lightgreen, borderColor = Color.red, shadow = Shadow.DropSmall))
            }
          }
          layers.addLayer(layer)
          layers.addLayer(ContentAreaDebugLayer())

          configurable("Shadow enabled", layer::shadowEnabled)
          configurable("Clear Shadow", layer::clearShadow)
          configurableColorProvider("Shadow Color", layer::shadowColor)
          configurable("Blur Radius", layer::blurRadius) {
            max = 50.0
          }

          configurable("Offset X", layer::offsetX) {
            min = -10.0
            max = 10.0
          }
          configurable("Offset Y", layer::offsetY) {
            min = -10.0
            max = 10.0
          }
        }
      }
    }
  }
}
