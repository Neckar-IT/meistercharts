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
import com.meistercharts.canvas.FontDescriptor
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.model.Direction

class TextGapDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Text Gap"
  override val description: String = "## Gap between anchor and text"
  override val category: DemoCategory = DemoCategory.Text

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          val layer = MyTextLayer()
          layers.addLayer(layer)

          configurableEnum("Anchor Direction", layer.anchorDirection, Direction.values()) {
            onChange {
              layer.anchorDirection = it
              markAsDirty()
            }
          }

          configurableDouble("Gap Horizontal", layer::gapHorizontal) {
            max = 50.0
          }
          configurableDouble("Gap Vertical", layer::gapVertical) {
            max = 50.0
          }

          configurableFont("Font", layer::font) {
          }
        }
      }
    }
  }
}

private class MyTextLayer : AbstractLayer() {
  override val type: LayerType
    get() = LayerType.Content

  var anchorDirection = Direction.Center
  var font: FontDescriptorFragment = FontDescriptor.XL
  var gapHorizontal: Double = 0.0
  var gapVertical: Double = 0.0

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    val y = gc.height / 2.0
    val x = gc.width / 2.0

    gc.strokeLine(0.0, y, gc.width, y)
    gc.strokeLine(x, 0.0, x, gc.height)

    //translate to the center
    // gc.translate(x, y)

    gc.font(font)
    gc.fillText("Hello World Gap", x, y, anchorDirection, gapHorizontal, gapVertical)
  }
}
