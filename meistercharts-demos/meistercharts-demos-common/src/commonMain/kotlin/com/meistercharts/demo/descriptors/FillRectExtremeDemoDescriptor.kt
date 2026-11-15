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
import com.meistercharts.color.Color
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.fill
import com.meistercharts.canvas.fillRect
import com.meistercharts.canvas.paintLocation
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import it.neckar.geometry.Direction
import it.neckar.open.formatting.format

/**
 * A demo for [fillRect] with extreme values
 */
class FillRectExtremeDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("28607a98-e04b-4db3-a284-3e5871fa3913")
  override val name: String = "Fill Rect Extreme"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override val description: String = "Shows rendering artifacts of fillRect with extreme values."

  override val category: DemoCategory = DemoCategory.Primitives

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()
          val layer = MyFillRectLayer()
          layers.addLayer(layer)

          configurable("Y", layer::y) {
            min = -10_000_001_000.0
            max = -10_000_000_000.0
            step = 1.0
          }

          configurable("Height", layer::height) {
            min = 10_000_000_000.0
            max = 10_000_001_000.0
            step = 1.0
          }

          configurable("Anchor direction", layer::anchorDirection, kotlin.enums.enumEntries<Direction>().toList())

          configurable("Anchor gap Horizontal", layer::anchorGapHorizontal) {
            min = -50.0
            max = 300.0
          }
          configurable("Anchor gap Vertical", layer::anchorGapVertical) {
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

    var x: @Window Double = 100.0
    var y: @Window Double = -10_000_000_000.0
    var width: @Zoomed Double = 200.0
    var height: @Zoomed Double = 10_000_000_000.0
    var anchorDirection: Direction = Direction.TopLeft
    var anchorGapHorizontal: @Zoomed Double = 0.0
    var anchorGapVertical: @Zoomed Double = 0.0

    private val fill = Color("#FF4500BB")

    override fun paint(paintingContext: LayerPaintingContext) {
      val gc = paintingContext.gc
      gc.fill(Color.black);
      gc.fillText("y: ${y.format(useGrouping = true)}", 10.0, 10.0, Direction.TopLeft)
      gc.fillText("height: ${height.format(useGrouping = true)}", 10.0, 30.0, Direction.TopLeft)
      gc.paintLocation(x, y, Color.black())
      gc.paintLocation(x + width, y + height, Color.black())
      gc.fill(fill)
      gc.fillRect(x, y, width, height, anchorDirection, anchorGapHorizontal, anchorGapVertical)
    }
  }
}

