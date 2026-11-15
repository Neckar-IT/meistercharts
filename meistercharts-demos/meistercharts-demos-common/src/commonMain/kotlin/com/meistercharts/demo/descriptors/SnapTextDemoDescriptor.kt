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
import com.meistercharts.annotations.PhysicalPixel
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.font.FontFamily
import com.meistercharts.font.FontSize
import com.meistercharts.font.FontWeight
import com.meistercharts.canvas.SnapConfiguration
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.saved
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableColorPicker
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import it.neckar.geometry.Direction
import it.neckar.open.kotlin.lang.fastFor

/**
 */
class SnapTextDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("f1186bbb-2350-425c-b11a-9c42cfafb466")
  override val name: String = "Snapping Text Demo"

  override val category: DemoCategory = DemoCategory.LowLevelTests
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

            var fill: Color = Color.rgb(44, 49, 51) //copied from bug report
            var font: FontDescriptorFragment = FontDescriptorFragment(FontFamily("Open Sans"), size = FontSize(12.0), weight = FontWeight(400)) //copied from demo
            var snapConfiguration: SnapConfiguration = SnapConfiguration.None
            var direction = Direction.TopLeft

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              gc.fill(fill)
              gc.font(font)

              gc.saved {
                gc.translate(100.0, 100.0)
                gc.saved {
                  paintTexts(gc)
                }

                gc.translate(0.0, 100.0)
                gc.saved {
                  paintTexts(gc)
                }

                gc.translate(0.0, 100.5)
                gc.saved {
                  paintTexts(gc)
                }

                gc.translate(0.5, 100.0)
                gc.saved {
                  paintTexts(gc)
                }

                gc.translate(0.1, 100.1)
                gc.saved {
                  paintTexts(gc)
                }

                gc.translate(0.1, 100.1)
                gc.saved {
                  paintTexts(gc, 0.5, 0.5)
                }
              }

              gc.translate(400.0, 100.0)
              40.fastFor {
                gc.translate(0.0, 21.234234234)
                paintTexts(gc)
              }

            }

            private fun paintTexts(gc: CanvasRenderingContext, additionalValueX: @PhysicalPixel Double = 0.0, additionalValueY: @PhysicalPixel Double = 0.0) {
              gc.snapPhysicalTranslation(
                additionalValueX,
                additionalValueY,
                snapX = snapConfiguration.snapX,
                snapY = snapConfiguration.snapY
              )
              gc.paintMark()
              fillText(gc, direction)
            }

            private fun fillText(gc: CanvasRenderingContext, direction: Direction) {
              gc.fillText(gc.translation.toString() + " / Physical: ${gc.translationPhysical}", 0.0, 0.0, direction)
            }
          }
          layers.addLayer(layer)

          configurable("snap", layer::snapConfiguration)
          configurable("direction", layer::direction)

          configurable("Text Fill", layer::fill)
          configurable("Font", layer::font)

        }
      }
    }
  }
}
