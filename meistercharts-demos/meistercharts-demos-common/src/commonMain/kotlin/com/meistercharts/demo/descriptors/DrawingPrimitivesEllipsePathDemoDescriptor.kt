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
import com.meistercharts.canvas.stroke
import com.meistercharts.color.Color
import com.meistercharts.canvas.strokeCross
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDouble

/**
 *
 */
class DrawingPrimitivesEllipsePathDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("cb22b3f5-6862-4b5d-84b3-93886823d574")
  override val name: String = "Drawing Primitives: Ellipse Center Path"

  //language=HTML
  override val description: String = "Shows how to add an ellipse to a path"
  override val category: DemoCategory = DemoCategory.Primitives
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {

    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()


          val layer = object : AbstractLayer() {
            override val type: LayerType
              get() = LayerType.Content

            var width = 100.0
            var height = 140.0

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              val x = gc.center.x
              val y = gc.center.y

              gc.beginPath()
              gc.ovalCenter(x, y, width, height)
              gc.stroke(Color.blue)
              gc.stroke()

              gc.stroke(Color.silver)
              gc.strokeCross(x, y, 5.0)
            }
          }
          layers.addLayer(
            layer
          )

          configurable("width", layer::width) {
            max = 800.0
          }

          configurable("height", layer::height) {
            max = 800.0
          }
        }
      }
    }
  }
}
