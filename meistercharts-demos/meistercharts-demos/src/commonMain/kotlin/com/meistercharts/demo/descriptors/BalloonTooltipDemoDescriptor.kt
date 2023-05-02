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
import com.meistercharts.algorithms.layers.addBackgroundChecker
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.tooltip.balloon.BalloonTooltipPaintable
import com.meistercharts.canvas.DebugFeature
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoxStyle
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnumProvider
import com.meistercharts.demo.configurableNosePosition
import com.meistercharts.demo.section
import com.meistercharts.model.Rectangle
import com.meistercharts.model.Size

/**
 */
class BalloonTooltipDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Balloon Tooltips"

  //language=HTML
  override val category: DemoCategory = DemoCategory.Primitives

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {

          val config = object {
            var x = 200.0
            var y = 200.0

            var width = 120.0
            var height = 80.0

            var roundedCornerRadius = 2.0
          }

          val contentPaintable: Paintable = object : Paintable {
            override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle {
              return Rectangle.topLeft(Size(config.width, config.height))
            }

            override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
              val gc = paintingContext.gc
              gc.fill(Color.pink)
              gc.fillRect(0.0, 0.0, config.width, config.height)
            }
          }

          val layer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            val tooltipPainter = BalloonTooltipPaintable(contentPaintable)

            override fun layout(paintingContext: LayerPaintingContext) {
              super.layout(paintingContext)
              tooltipPainter.layoutIfNecessary(paintingContext)
            }

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              gc.translate(config.x, config.y)
              gc.paintMark()

              tooltipPainter.paint(paintingContext)

              paintingContext.ifDebug(DebugFeature.ShowBounds) {
                gc.stroke(Color.orange)
                gc.strokeRect(tooltipPainter.boundingBox(paintingContext))
              }
            }
          }

          configurableEnumProvider("Direction", layer.tooltipPainter.configuration::noseSide) {}

          section("Nose Position")
          configurableNosePosition(layer.tooltipPainter)

          configurableDouble("Nose Width", layer.tooltipPainter.configuration::noseWidth) {
            max = 20.0
          }
          configurableDouble("Nose Length", layer.tooltipPainter.configuration::noseLength) {
            max = 20.0
          }

          configurableDouble("x", config::x) {
            max = 300.0
          }
          configurableDouble("y", config::y) {
            max = 300.0
          }
          configurableDouble("width", config::width) {
            max = 1000.0
          }
          configurableDouble("height", config::height) {
            max = 1000.0
          }

          configurableBoxStyle("Box Style", layer.tooltipPainter.configuration::boxStyle, true)

          layers.addClearBackground()
          layers.addBackgroundChecker()
          layers.addLayer(layer)
        }
      }
    }
  }
}
