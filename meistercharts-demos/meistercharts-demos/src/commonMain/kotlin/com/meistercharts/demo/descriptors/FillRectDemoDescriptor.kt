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
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.fillRect
import com.meistercharts.canvas.paintLocation
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.model.Direction

/**
 * A demo for [fillRect]
 */
class FillRectDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Fill Rect"

  override val description: String = """
  fun CanvasRenderingContext.fillRect(<BR/>
  &nbsp;&nbsp;&nbsp;<b>x</b>: @px Double,<BR/>
  &nbsp;&nbsp;&nbsp;<b>y</b>: @px Double,<BR/>
  &nbsp;&nbsp;&nbsp;<b>width</b>: @MayBeNegative @px @Zoomed Double,<BR/>
  &nbsp;&nbsp;&nbsp;<b>height</b>: @MayBeNegative @px @Zoomed Double,<BR/>
  &nbsp;&nbsp;&nbsp;<b>anchorDirection</b>: Direction,<BR/>
  &nbsp;&nbsp;&nbsp;<b>anchorGapHorizontal</b>: @px Double = 0.0<BR/>
  &nbsp;&nbsp;&nbsp;<b>anchorGapVertical</b>: @px Double = 0.0<BR/>
  )
  """.trimIndent()

  override val category: DemoCategory = DemoCategory.Primitives

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          val layer = MyFillRectLayer()
          layers.addLayer(layer)

          configurableDouble("X", layer::x) {
            min = -300.0
            max = 2000.0
          }

          configurableDouble("Y", layer::y) {
            min = -300.0
            max = 2000.0
          }

          configurableDouble("Width", layer::width) {
            min = -300.0
            max = 2000.0
          }

          configurableDouble("Height", layer::height) {
            min = -300.0
            max = 2000.0
          }

          configurableEnum("Anchor direction", layer::anchorDirection, enumValues())

          configurableDouble("Anchor gap horizontal", layer::anchorGapHorizontal) {
            min = -50.0
            max = 300.0
          }
          configurableDouble("Anchor gap vertical", layer::anchorGapVertical) {
            min = -50.0
            max = 300.0
          }
        }
      }
    }
  }

  private class MyFillRectLayer : AbstractLayer() {

    override val type: LayerType
      get() = LayerType.Content

    var x: @Window Double = 200.0
    var y: @Window Double = 200.0
    var width: @Zoomed Double = 200.0
    var height: @Zoomed Double = 200.0
    var anchorDirection: Direction = Direction.TopLeft
    var anchorGapHorizontal: @Zoomed Double = 0.0
    var anchorGapVertical: @Zoomed Double = 0.0

    private val fill = Color("#FF4500BB")

    override fun paint(paintingContext: LayerPaintingContext) {
      val gc = paintingContext.gc
      gc.paintLocation(x, y, Color.black)
      gc.fill(fill)
      gc.fillRect(x, y, width, height, anchorDirection, anchorGapHorizontal, anchorGapVertical)
    }
  }
}

