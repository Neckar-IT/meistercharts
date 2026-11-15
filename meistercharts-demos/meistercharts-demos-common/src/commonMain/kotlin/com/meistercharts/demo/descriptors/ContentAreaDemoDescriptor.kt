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

import com.meistercharts.demo.section
import kotlin.uuid.Uuid
import com.meistercharts.zoom.UpdateReason
import com.meistercharts.zoom.ZoomAndTranslationDefaults
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.algorithms.layers.debug.WindowDebugLayer
import com.meistercharts.canvas.fill
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import it.neckar.geometry.Direction
import com.meistercharts.model.Zoom
import it.neckar.open.formatting.decimalFormat1digit

/**
 */
class ContentAreaDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("cac4c6c7-4b4a-47d6-8401-0035ed51f676")
  override val name: String = "Content Area"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  //language=HTML
  override val description: String = "Visualizes the content area, whose size is bound to the window size"
  override val category: DemoCategory = DemoCategory.Calculations

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()
          layers.addLayer(WindowDebugLayer())
          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType
              get() = LayerType.Content

            val format = decimalFormat1digit

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              gc.fill(Color.darkgray)
              gc.fillText("${format.format(gc.width)}/${format.format(gc.height)}", gc.width, gc.height, Direction.BottomRight, 10.0, 10.0)
            }
          })
          layers.addLayer(ContentAreaDebugLayer())

          declare {
            val zoomAndPanSupport = chartSupport.zoomAndTranslationSupport

            section("Zoom")
            button("Reset") {
              zoomAndPanSupport.resetZoom(reason = UpdateReason.UserInteraction)
            }
            button("Reset - 10% Overscan") {
              zoomAndPanSupport.resetZoom(ZoomAndTranslationDefaults.tenPercentMargin, reason = UpdateReason.UserInteraction)
            }
            button("50%") {
              zoomAndPanSupport.setZoom(Zoom(0.5, 0.5), reason = UpdateReason.UserInteraction)
            }
            button("200%") {
              zoomAndPanSupport.setZoom(Zoom(2.0, 2.0), reason = UpdateReason.UserInteraction)
            }

            section("Translation")
            button("Reset") {
              zoomAndPanSupport.resetWindowTranslation(reason = UpdateReason.UserInteraction)
            }
            button("Reset - 10% Overscan") {
              zoomAndPanSupport.resetWindowTranslation(ZoomAndTranslationDefaults.tenPercentMargin, reason = UpdateReason.UserInteraction)
            }
          }
        }
      }
    }
  }
}
