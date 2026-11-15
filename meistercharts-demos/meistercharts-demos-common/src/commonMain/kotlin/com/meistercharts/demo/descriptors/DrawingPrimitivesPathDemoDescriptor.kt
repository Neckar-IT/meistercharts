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


import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.color.Color
import com.meistercharts.algorithms.painter.Path
import com.meistercharts.canvas.ArcType
import com.meistercharts.canvas.fillStyle
import com.meistercharts.canvas.stroke
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import kotlin.math.PI

/**
 */
class DrawingPrimitivesPathDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("6369e7b2-de18-43ad-8777-0bf3b9cba621")
  override val name: String = "Drawing Primitives: Path"

  //language=HTML
  override val description: String = "Shows how to draw a path"
  override val category: DemoCategory = DemoCategory.Primitives
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()


          val radius = 150.0
          val startAngle = 0.0
          val arcExtent = 1.5 * PI
          val arcType = ArcType.Open

          val path = Path()
          path.moveTo(10.0, 10.0)
          path.lineTo(30.0, 10.0)
          path.lineTo(30.0, 30.0)
          path.lineTo(10.0, 30.0)
          path.closePath()

          path.moveTo(40.0, 10.0)
          path.lineTo(70.0, 10.0)
          path.lineTo(70.0, 30.0)
          path.lineTo(40.0, 30.0)
          path.closePath()

          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType
              get() = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.fillStyle(Color.transparent)
              gc.stroke(Color.orangered)
              gc.stroke(path)
            }
          })
        }
      }
    }
  }
}
