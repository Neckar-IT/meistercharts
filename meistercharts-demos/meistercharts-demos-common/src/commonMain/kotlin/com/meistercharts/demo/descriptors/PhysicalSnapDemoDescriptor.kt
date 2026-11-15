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
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.PaintingUtils
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.canvas.saved
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableDouble
import it.neckar.geometry.Direction

/**
 */
class PhysicalSnapDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("40df823e-77fc-4aad-ac8d-274bfaa7d85c")
  override val name: String = "Physical Snap"

  //language=HTML
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

            var snapToPhysical = true

            var translationX: Double = 7.123
            var translationY: Double = 9.922

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.translate(translationX, translationY)

              gc.translate(gc.width * 0.25, gc.height * 0.25)
              gc.saved {
                //Snap to physical
                if (snapToPhysical) {
                  gc.snapPhysicalTranslation()
                }

                strokeLines(gc)
                paintInfoBox(gc, "+0.5 Physical Pixels")
              }

              //0.5 pixel physical offset!
              gc.translate(gc.width * 0.25, gc.height * 0.25)
              gc.saved {
                if (snapToPhysical) {
                  gc.snapPhysicalTranslation(0.5, 0.5)
                }

                strokeLines(gc)
                paintInfoBox(gc, "+0.5 Physical Pixels")
              }
            }

            private fun strokeLines(gc: CanvasRenderingContext) {
              gc.lineWidth = PaintingUtils.snapSize(1.0, snapToPhysical)

              gc.strokeLine(0.0, 100.0, 0.0, -100.0)
              gc.strokeLine(100.0, 0.0, -100.0, 0.0)

              gc.strokeRect(-50.0, -50.0, 100.0, 100.0)
              gc.strokeRect(-25.0, -25.0, 50.0, 50.0)
            }

            private fun paintInfoBox(gc: CanvasRenderingContext, label: String) {
              gc.paintTextBox(
                listOf(
                  label,
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

          configurable("snap to physical pixels", layer::snapToPhysical)

          configurable("Translation X", layer::translationX) {
            min = -200.0
            max = 200.0
          }
          configurable("Translation Y", layer::translationY) {
            min = -200.0
            max = 200.0
          }

        }
      }
    }
  }
}
