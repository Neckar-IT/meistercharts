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

import com.meistercharts.algorithms.impl.ZoomAndTranslationDefaults
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.debug.ContentAreaDebugLayer
import com.meistercharts.algorithms.layers.debug.WindowDebugLayer
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.model.Direction
import com.meistercharts.model.Zoom
import it.neckar.open.formatting.decimalFormat1digit

/**
 */
class ContentAreaDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Content Area"

  //language=HTML
  override val description: String = "## Visualizes the size of the content area\nThe size of the content area is bound to the window size"
  override val category: DemoCategory = DemoCategory.Calculations

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
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

            section("Zoom") {
              button("Reset") {
                zoomAndPanSupport.resetZoom()
              }
              button("Reset - 10% Overscan") {
                zoomAndPanSupport.resetZoom(ZoomAndTranslationDefaults.tenPercentMargin)
              }
              button("50%") {
                zoomAndPanSupport.setZoom(Zoom(0.5, 0.5))
              }
              button("200%") {
                zoomAndPanSupport.setZoom(Zoom(2.0, 2.0))
              }
            }

            section("Translation") {
              button("Reset") {
                zoomAndPanSupport.resetWindowTranslation()
              }
              button("Reset - 10% Overscan") {
                zoomAndPanSupport.resetWindowTranslation(ZoomAndTranslationDefaults.tenPercentMargin)
              }
            }
          }
        }
      }
    }
  }
}
