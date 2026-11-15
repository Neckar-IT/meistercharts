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
package com.meistercharts.demojs.descriptors

import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.font
import com.meistercharts.canvas.saved
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.design.CurrentTheme
import com.meistercharts.font.FontDescriptor
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.font.FontFamily
import com.meistercharts.font.FontSize
import com.meistercharts.js.CanvasJS
import com.meistercharts.js.CanvasRenderingContextJS
import com.meistercharts.js.convertToHtmlFontString
import it.neckar.geometry.Direction

/**
 */
class ExplainFontsJSDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("f9986858-f847-417b-8333-214ea6958be1")
  override val name: String = "Explain Fonts (JS)"

  //language=HTML
  override val description: String = "Explains how the fonts are calculated"
  override val category: DemoCategory = DemoCategory.Platform

  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()

          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Content
            val offsetXColumn3 = 410.0

            override fun paint(paintingContext: LayerPaintingContext) {

              val gc = paintingContext.gc
              gc.translate(10.0, 10.0)

              gc.paintLine("gc.defaultFont():", gc.defaultFont())
              gc.paintLine("gc.font:", gc.font)
              gc.paintLine("CurrentTheme.textFont:", CurrentTheme.textFont)

              gc.paintLine("HtmlCanvas.font()", (chartSupport.canvas as CanvasJS).canvasElement.font())

              gc.paintLine("Oswald Font", FontDescriptor(FontFamily.Oswald, FontSize(18.0)))

              gc.paintLine("Multiple Fonts (should display Open Sans)", FontDescriptor(listOf(FontFamily("Open Sans" ), FontFamily("Oswald" )), FontSize(18.0)))

              gc.paintLine("Multiple Fonts with nonexistent font (should display Oswald)", FontDescriptor(
                listOf(FontFamily("test"), FontFamily("Oswald"), FontFamily("Open Sans")),
                FontSize(18.0)
              ))

              gc.paintLine("Not existing font (should display sans-serif generic font)", FontDescriptor(FontFamily("thisFontDoesNotExist"), FontSize(18.0)))



            }

            private fun CanvasRenderingContext.paintLine(label: String, fontDescriptorFragment: FontDescriptorFragment) {
              translate(0.0, 30.0)
              fillText(label, 0.0, 0.0, Direction.TopLeft)

              translate(0.0, 20.0)
              fillText(fontDescriptorFragment.toString(), 30.0, 0.0, Direction.TopLeft)

              translate(0.0, 20.0)
              val currentFont: FontDescriptor
              val jsStringFont: String
              val jsGc = this.canvas.gc as CanvasRenderingContextJS

              saved {
                font(fontDescriptorFragment)
                currentFont = font
                jsStringFont = jsGc.canvasJsFontString()

              }

              fillText("Font with applied descriptor/fragment: $currentFont", 30.0, 0.0, Direction.TopLeft)
              translate(0.0, 20.0)
              fillText("FontDescriptor.toHTML():" + currentFont.convertToHtmlFontString(), 30.0, 0.0, Direction.TopLeft)
              translate(0.0, 20.0)
              fillText("canvas.font: $jsStringFont", 30.0, 0.0, Direction.TopLeft)





              translate(0.0, 20.0)
              saved {
                font(fontDescriptorFragment)
                fillText("Example String!", 30.0, 0.0, Direction.TopLeft)
              }
            }
          })
        }
      }
    }
  }
}

