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
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.font.FontFamily
import com.meistercharts.font.FontSize
import com.meistercharts.font.FontWeight
import it.neckar.geometry.Direction
import it.neckar.open.collections.fastForEach
import it.neckar.open.collections.fastForEachIndexed

/**
 * Currently correct way to use font awesome solid with unicodes is to use the font family 'Font Awesome 6 Free' with a font-weight of 900.
 * To get font awesome regular use a font weight of 400 instead.
 * These values were taken from the font awesome solid.css file, which is used in the documentation for SVG Unicodes.
 * Source: https://fontawesome.com/docs/web/add-icons/svg-unicodes
 *
 * To use font awesome brands use the font family 'Font Awesome 6 Brands' with a font weight of 400.
 */
class FontAwesomeFontDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("f4ab8e2f-580e-47a7-831d-2bfc69dc38ba")
  override val name: String = "Font Awesome"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable
  override val description: String = "Renders text using the Font Awesome icon font."
  override val category: DemoCategory = DemoCategory.Text
  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {

        configure {

          layers.addClearBackground()

          layers.addLayer(object : AbstractLayer() {
            val icons = "\uf0e0 \uf03e \uF002 \uF24E " + // at least since font awesome 4
                "\uf2b9 " + //  v4 icons
                "\uE573 \uE572 " + //  font awesome version 6
                "\ue185" //  font awesome version 6, available in regular and solid
            val brandIcons = "\uf09b \uf092 \uf16c \uf17c \uf1d1 \uf1d0"

            override val type: LayerType = LayerType.Content

            val weights = listOf(
              FontWeight(400),
              FontWeight(600),
              FontWeight(900)
            )

            val fontFamilies = listOf(
              FontFamily.FontAwesome,
              FontFamily.FontAwesome6Free,
              FontFamily.FontAwesome6Brands,
              FontFamily("Font Awesome 6 Free Regular"), //from TTF font (for JavaFX)
              FontFamily("Font Awesome 6 Free Solid"), //from TTF font (for JavaFX)
              FontFamily("Font Awesome 6 Brands Regular"), //from TTF font (for JavaFX)
            )

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              fontFamilies.fastForEachIndexed { index, family ->
                gc.translate(0.0, 40.0)
                when (index) {
                  0 -> {
                    gc.fillText("Should display solid icons:", 350.0, 0.0, Direction.CenterLeft)
                    gc.fillText("Brands work in JS, not in JavaFX:", 600.0, 0.0, Direction.CenterLeft)
                  }

                  1 -> {
                    gc.fillText("Top line: regular, others: solid", 350.0, 0.0, Direction.CenterLeft)
                    gc.fillText("Should not work:", 600.0, 0.0, Direction.CenterLeft)
                  }

                  2 -> {
                    gc.fillText("Should not work:", 350.0, 0.0, Direction.CenterLeft)
                    gc.fillText("Should display brands:", 600.0, 0.0, Direction.CenterLeft)
                  }

                  3 -> {
                    gc.translate(0.0, 30.0)
                    gc.fillText("For JavaFX:", 500.0, 0.0, Direction.CenterLeft)
                    gc.translate(0.0, 30.0)
                    gc.fillText("Should display regular icons:", 350.0, 0.0, Direction.CenterLeft)
                    gc.fillText("Should not work:", 600.0, 0.0, Direction.CenterLeft)
                  }

                  4 -> {
                    gc.fillText("Should display solid icons:", 350.0, 0.0, Direction.CenterLeft)
                    gc.fillText("Should not work:", 600.0, 0.0, Direction.CenterLeft)
                  }

                  5 -> {
                    gc.fillText("Should not work:", 350.0, 0.0, Direction.CenterLeft)
                    gc.fillText("Should display brands:", 600.0, 0.0, Direction.CenterLeft)
                  }
                }

                weights.fastForEach { fontWeight ->
                  gc.translate(0.0, 30.0)
                  paintLine(gc, FontDescriptorFragment(listOf(family), FontSize(20.0), weight = fontWeight))
                }
              }
            }

            private fun paintLine(gc: CanvasRenderingContext, fontFragment: FontDescriptorFragment) {
              gc.saved {
                gc.fillText("${fontFragment.families} - ${fontFragment.size?.size} - ${fontFragment.weight?.weight}", 0.0, 0.0, Direction.CenterLeft)
                gc.font(fontFragment)
                gc.fillText(icons, 350.0, 0.0, Direction.CenterLeft)
                gc.fillText(brandIcons, 600.0, 0.0, Direction.CenterLeft)
              }
            }
          })
        }
      }
    }
  }
}
