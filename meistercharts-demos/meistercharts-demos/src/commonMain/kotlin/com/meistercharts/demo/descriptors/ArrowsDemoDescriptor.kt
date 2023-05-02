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
import com.meistercharts.algorithms.painter.Arrows
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.painter.Path
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.paintMark
import com.meistercharts.canvas.saved
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.model.Direction
import it.neckar.open.unit.other.deg

/**
 */
class ArrowsDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Arrows"
  override val category: DemoCategory = DemoCategory.Primitives

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          val arrowConfig = MyArrowConfig()

          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              gc.translate(100.0, 100.0)
              gc.saved {
                paintArrow(gc, Arrows.to(Direction.TopCenter, arrowConfig.arrowLength, arrowConfig.arrowHeadHeight, arrowConfig.arrowHeadWidth))
              }

              gc.translate(100.0, 0.0)
              gc.saved {
                paintArrow(gc, Arrows.to(Direction.BottomCenter, arrowConfig.arrowLength, arrowConfig.arrowHeadHeight, arrowConfig.arrowHeadWidth))
              }

              gc.translate(100.0, 0.0)
              gc.saved {
                paintArrow(gc, Arrows.to(Direction.CenterRight, arrowConfig.arrowLength, arrowConfig.arrowHeadHeight, arrowConfig.arrowHeadWidth))
              }
              gc.translate(100.0, 0.0)
              gc.saved {
                paintArrow(gc, Arrows.to(Direction.CenterLeft, arrowConfig.arrowLength, arrowConfig.arrowHeadHeight, arrowConfig.arrowHeadWidth))
              }
            }

            private fun paintArrow(gc: CanvasRenderingContext, arrowPath: Path) {
              gc.paintMark(color = Color.gray)
              gc.stroke(Color.orange)

              gc.saved {
                gc.rotateDegrees(arrowConfig.rotation)
                gc.lineWidth = arrowConfig.lineWidth
                gc.stroke(arrowPath)
              }


              gc.translate(0.0, 100.0)
              gc.paintMark(color = Color.gray)
              gc.fill(Color.orange)

              gc.saved {
                gc.rotateDegrees(arrowConfig.rotation)
                gc.lineWidth = arrowConfig.lineWidth
                gc.fill(arrowPath)
              }
            }
          })

          configurableDouble("Arrow Length", arrowConfig::arrowLength) {
            max = 100.0
            onChange { markAsDirty() }
          }
          configurableDouble("Arrow Head Height", arrowConfig::arrowHeadHeight) {
            max = 100.0
            onChange { markAsDirty() }
          }
          configurableDouble("Arrow Head Width", arrowConfig::arrowHeadWidth) {
            max = 100.0
            onChange { markAsDirty() }
          }
          configurableDouble("Rotation", arrowConfig::rotation) {
            max = 360.0
            min = -360.0
            onChange { markAsDirty() }
          }
          configurableDouble("line width", arrowConfig::lineWidth) {
            max = 10.0
          }
        }
      }
    }
  }
}

private class MyArrowConfig {
  var rotation: @deg Double = 0.0

  var lineWidth = 1.0
  var arrowLength = 40.0
  var arrowHeadHeight = 15.0
  var arrowHeadWidth = 15.0
}
