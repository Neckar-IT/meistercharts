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
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.stroke
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.model.Vicinity
import com.meistercharts.style.BoxStyle
import it.neckar.geometry.Direction
import it.neckar.geometry.Rectangle

/**
 *
 */
class Anchor2DemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("e3788eb7-828f-4866-8af3-0784ed77015c")
  override val name: String = "Anchor Advanced Demo"

  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  //language=HTML
  override val description: String = "Advanced demo showing how to configure the anchor"
  override val category: DemoCategory = DemoCategory.Calculations

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()
          val layer = Anchor2DemoDescriptorLayer()
          layers.addLayer(layer)


          configurable("X", layer::x) {
            min = -400.0
            max = 400.0
          }
          configurable("y", layer::y) {
            min = -400.0
            max = 400.0
          }
          configurable("width", layer::width) {
            min = -400.0
            max = 400.0
          }
          configurable("height", layer::height) {
            min = -400.0
            max = 400.0
          }

          configurable("Direction in Shape", layer::directionInShape, kotlin.enums.enumEntries<Direction>().toList()) {
          }

          configurable("Label side", layer::vicinity, kotlin.enums.enumEntries<Vicinity>().toList()) {
          }

          configurable("Anchor Gap Horizontal", layer::anchorGapHorizontal) {
            max = 60.0
          }
          configurable("Anchor Gap Vertical", layer::anchorGapVertical) {
            max = 60.0
          }
        }
      }
    }
  }
}

private class Anchor2DemoDescriptorLayer : AbstractLayer() {
  var directionInShape: Direction = Direction.Center
  var vicinity: Vicinity = Vicinity.Outside
  var anchorGapHorizontal: Double = 0.0
  var anchorGapVertical: Double = 0.0

  var x = -75.0
  var y = -50.0
  var width = 100.0
  var height = 150.0

  override val type: LayerType
    get() = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    gc.translateToCenter()
    gc.stroke(Color.silver)
    gc.strokeLine(-gc.width, 0.0, gc.width, 0.0)
    gc.strokeLine(0.0, -gc.height, 0.0, gc.height)

    val rect = Rectangle(x, y, width, height)

    gc.stroke(Color.orange)
    gc.strokeRect(rect)

    //Paint the origin of the rectangle
    gc.strokeOvalCenter(x, y, 15.0, 15.0)

    val anchorX = rect.x(directionInShape.horizontalAlignment)
    val anchorY = rect.y(directionInShape.verticalAlignment)
    gc.stroke(Color.red)
    gc.paintMark(anchorX, anchorY)

    val anchorDirection = directionInShape.oppositeIf(vicinity == Vicinity.Outside)
    gc.translate(anchorX, anchorY)
    gc.paintTextBox("The Label", anchorDirection, anchorGapHorizontal, anchorGapVertical, BoxStyle.gray, Color.black())
  }
}
