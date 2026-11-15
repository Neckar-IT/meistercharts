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
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.saved
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.design.CurrentTheme
import com.meistercharts.font.FontDescriptorFragment
import it.neckar.geometry.Direction

/**
 *
 */
class ExplainFontDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("d2e6954c-38ef-405e-9d5a-72fea5e3431a")
  override val name: String = "Explain Fonts"

  //language=HTML
  override val description: String = "Explains how the fonts are calculated"
  override val category: DemoCategory = DemoCategory.Text

  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()

          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.translate(10.0, 10.0)

              gc.paintLine("gc.defaultFont():", gc.defaultFont())
              gc.paintLine("gc.font:", gc.font)
              gc.paintLine("CurrentTheme.textFont:", CurrentTheme.textFont)
            }

            private fun CanvasRenderingContext.paintLine(label: String, fontDescriptor: FontDescriptorFragment) {
              translate(0.0, 30.0)
              fillText(label, 0.0, 0.0, Direction.TopLeft)

              translate(0.0, 20.0)
              fillText(fontDescriptor.toString(), 30.0, 0.0, Direction.TopLeft)

              translate(0.0, 20.0)
              saved {
                this.font(fontDescriptor)
                fillText("Example String!", 30.0, 0.0, Direction.TopLeft)
              }
            }
          })
        }
      }
    }
  }
}
