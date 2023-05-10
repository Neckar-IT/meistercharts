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
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Window
import com.meistercharts.canvas.fillRectCoordinates
import com.meistercharts.canvas.strokeRectCoordinates
import com.meistercharts.charts.FitContentInViewportGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.model.Insets

/**
 */
class ContentViewportDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Content Viewport Basics"

  //language=HTML
  override val description: String = """
    Fills the content area green - but only within the content viewport
  """.trimIndent()

  override val category: DemoCategory = DemoCategory.Calculations

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        val contentViewportGestalt = FitContentInViewportGestalt(Insets.all15)
        contentViewportGestalt.configure(this@meistercharts)

        configure {
          layers.addClearBackground()

          val layer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc
              val chartState = paintingContext.chartState
              val chartCalculator = paintingContext.chartCalculator

              gc.stroke(Color.red)

              gc.strokeRectCoordinates(
                x0 = chartCalculator.contentViewportMinX(),
                y0 = chartCalculator.contentViewportMinY(),
                x1 = chartCalculator.contentViewportMaxX(),
                y1 = chartCalculator.contentViewportMaxY(),
              )

              //Paint the content area - but only within the viewport

              @Window val x = chartCalculator.contentAreaRelative2windowXInViewport(0.0)
              @Window val y = chartCalculator.contentAreaRelative2windowYInViewport(0.0)

              @Window val x2 = chartCalculator.contentAreaRelative2windowXInViewport(1.0)
              @Window val y2 = chartCalculator.contentAreaRelative2windowYInViewport(1.0)

              gc.fill(Color.green)
              gc.fillRectCoordinates(x, y, x2, y2)
            }
          }
          layers.addLayer(layer)

          configurableInsetsSeparate("Content Viewport", contentViewportGestalt.contentViewportMarginProperty)
        }
      }
    }
  }
}
