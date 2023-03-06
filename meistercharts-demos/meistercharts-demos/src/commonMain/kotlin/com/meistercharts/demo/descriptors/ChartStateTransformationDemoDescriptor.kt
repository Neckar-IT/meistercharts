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
import com.meistercharts.algorithms.layers.withUpdatedChartState
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.painter.NonOverlappingPasspartoutPaintingStrategy
import com.meistercharts.algorithms.painter.PasspartoutPainter
import com.meistercharts.algorithms.withAdditionalTranslation
import com.meistercharts.algorithms.withContentAreaSize
import com.meistercharts.algorithms.withContentViewportMargin
import com.meistercharts.algorithms.withZoom
import com.meistercharts.canvas.fillRectCoordinates
import com.meistercharts.canvas.paintLocation
import com.meistercharts.canvas.pixelSnapSupport
import com.meistercharts.canvas.strokeRectCoordinates
import com.meistercharts.charts.FitContentInViewportGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.model.Distance
import com.meistercharts.model.Insets
import com.meistercharts.model.Size
import com.meistercharts.model.Zoom

/**
 *
 */
class ChartStateTransformationDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Chart State Transformation"
  override val category: DemoCategory = DemoCategory.Calculations

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          //layers.addLayer(ContentAreaDebugLayer())

          val config = object {
            var additionalTranslationX = 0.0
            var additionalTranslationY = 0.0

            var width = 200.0
            var height = 200.0

            var zoomX = 1.0
            var zoomY = 1.0

            var contentViewport: Insets = Insets.all15
          }

          val myLayer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            val passpartoutPainter: PasspartoutPainter = PasspartoutPainter()

            override fun paint(paintingContext: LayerPaintingContext) {
              val layerSupport = paintingContext.layerSupport
              val rootChartState = layerSupport.chartSupport.rootChartState
              val chartState = layerSupport.chartSupport.currentChartState
              val calculator = paintingContext.chartCalculator
              val gc = paintingContext.gc
              val snapSupport = paintingContext.chartSupport.pixelSnapSupport

              val chartCalculator = layerSupport.chartSupport.chartCalculator

              gc.paintLocation()

              gc.stroke(Color.red)
              gc.strokeRectCoordinates(
                x0 = chartCalculator.contentAreaRelative2windowX(0.0),
                y0 = chartCalculator.contentAreaRelative2windowY(0.0),
                x1 = chartCalculator.contentAreaRelative2windowX(1.0),
                y1 = chartCalculator.contentAreaRelative2windowY(1.0),
              )
              gc.fill(Color.orange.withAlpha(0.4))

              gc.fillRectCoordinates(
                x0 = chartCalculator.contentAreaRelative2windowXInViewport(0.0),
                y0 = chartCalculator.contentAreaRelative2windowYInViewport(0.0),
                x1 = chartCalculator.contentAreaRelative2windowXInViewport(1.0),
                y1 = chartCalculator.contentAreaRelative2windowYInViewport(1.0),
              )

              passpartoutPainter.paintPasspartout(
                paintingContext = paintingContext,
                color = Color.blue.withAlpha(0.5),
                margin = Insets.empty,
                insets = chartState.contentViewportMargin,
                strategy = NonOverlappingPasspartoutPaintingStrategy
              )
            }
          }

          layers.addLayer(myLayer.withUpdatedChartState {
            it.withContentAreaSize(Size(config.width, config.height))
              .withAdditionalTranslation(Distance.of(config.additionalTranslationX, config.additionalTranslationY))
              .withZoom(Zoom(config.zoomX, config.zoomY))
              .withContentViewportMargin(config.contentViewport)
          })

          configurableDouble("offsetX", config::additionalTranslationX) {
            max = 1000.0
          }
          configurableDouble("offsetY", config::additionalTranslationY) {
            max = 1000.0
          }
          configurableDouble("Width", config::width) {
            max = 1000.0
          }
          configurableDouble("Height", config::height) {
            max = 1000.0
          }
          configurableDouble("Zoom X", config::zoomX) {
            min = 0.1
            max = 10.0
          }
          configurableDouble("Zoom Y", config::zoomY) {
            min = 0.1
            max = 10.0
          }
          configurableInsetsSeparate("Content Viewport", config::contentViewport)
        }

        FitContentInViewportGestalt(Insets.of(30.0)).configure(this)
      }
    }
  }
}
