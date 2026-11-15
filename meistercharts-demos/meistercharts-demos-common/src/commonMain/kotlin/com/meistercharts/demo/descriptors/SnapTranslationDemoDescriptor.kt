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
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDouble
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Direction

/**
 */
class SnapTranslationDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("9f585fbe-5169-4ae6-9ecb-aaa73107bc82")
  override val name: String = "Snap translation"
  override val category: DemoCategory = DemoCategory.LowLevelTests
  override val description: String = """
    Shows snapping of the translation to the nearest integer value
  """.trimIndent()
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          val layer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            var translationX: Double = 107.123
            var translationY: Double = 109.922

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              gc.lineWidth = 1.0 / 1.27

              gc.translate(translationX, translationY)

              //Move to center of the line
              gc.translate(0.5, 0.5)
              gc.strokeLine(Coordinates.of(0.0, -100.0), Coordinates.of(0.0, 100.0))
              gc.strokeLine(Coordinates.of(-100.0, 0.0), Coordinates.of(100.0, 0.0))
              gc.fillText("Translation: ${gc.translation.format()}", 0.0, 0.0, Direction.TopLeft, 5.0, 5.0)

              gc.translate(200.0, 200.0)
              gc.snapPhysicalTranslation()

              gc.strokeLine(Coordinates.of(0.0, -100.0), Coordinates.of(0.0, 100.0))
              gc.strokeLine(Coordinates.of(-100.0, 0.0), Coordinates.of(100.0, 0.0))
              gc.paintTextBox(
                listOf(
                  "Translation: ${gc.translation.format()}",
                  "Physical translation: ${gc.translationPhysical.format()}"
                ),
                Direction.TopLeft, 5.0
              )
            }
          }

          layers.addClearBackground()
          layers.addLayer(layer)

          configurable("Translation X", layer::translationX) {
            max = 200.0
          }
          configurable("Translation Y", layer::translationY) {
            max = 200.0
          }
        }
      }
    }
  }
}
