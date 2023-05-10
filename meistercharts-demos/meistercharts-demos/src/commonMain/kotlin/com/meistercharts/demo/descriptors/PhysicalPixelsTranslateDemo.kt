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
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.model.Direction

/**
 *
 */
class PhysicalPixelsTranslateDemo : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Physical Pixels Translate"
  override val category: DemoCategory = DemoCategory.LowLevelTests

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          val layer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            var translationX: Double = 7.123
            var translationY: Double = 9.922

            var translationType: TranslationType = TranslationType.Normal

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              when (translationType) {
                TranslationType.Normal -> {
                  gc.translate(translationX, translationY)
                }

                TranslationType.Physical -> {
                  gc.translatePhysical(translationX, translationY)
                }
              }

              gc.paintMark()
              gc.paintTextBox(
                listOf(
                  "Line width: ${gc.lineWidth}",
                  "Physical translation: ${gc.translationPhysical.format()}",
                  "Translation: ${gc.translation.format()}",
                  "Scale: ${gc.scale.format()}",
                  "Native Translation: ${gc.nativeTranslation?.format()}",

                  ), Direction.TopLeft, 5.0
              )
            }
          }
          layers.addLayer(layer)


          configurableEnum("Translation Type", layer::translationType)

          configurableDouble("Translation X", layer::translationX) {
            min = -200.0
            max = 200.0
          }
          configurableDouble("Translation Y", layer::translationY) {
            min = -200.0
            max = 200.0
          }
        }
      }
    }
  }

  enum class TranslationType {
    Normal,
    Physical
  }
}
