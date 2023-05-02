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
import com.meistercharts.canvas.StrokeLocation
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.canvas.roundedRect
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import it.neckar.open.kotlin.lang.enumEntries
import it.neckar.open.unit.other.px

/**
 *
 */
class RoundedRectsDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Rounded Rectangles"
  override val description: String = "Rounded Rectangles"
  override val category: DemoCategory = DemoCategory.Primitives

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {

    return ChartingDemo {
      meistercharts {

        configure {
          layers.addClearBackground()

          val gradientLayer = RoundedRectsLayer()
          layers.addLayer(gradientLayer)

          configurableDouble("radius", gradientLayer.style::radius) {
            max = 50.0
          }
          configurableDouble("lineWidth", gradientLayer.style::lineWidth) {
            max = 20.0
          }

          configurableEnum("Stroke Location", gradientLayer.style::strokeLocation, enumEntries())

          configurableDouble("width", gradientLayer::width) {
            min = -100.0
            max = 500.0
          }
          configurableDouble("height", gradientLayer::height) {
            min = -100.0
            max = 500.0
          }
        }
      }
    }
  }
}

private class RoundedRectsLayer : AbstractLayer() {
  val style: Style = Style()

  var width = 200.0
  var height = 300.0

  override val type: LayerType
    get() = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    gc.lineWidth = style.lineWidth
    gc.translateToCenter()
    gc.roundedRect(0.0, 0.0, width, height, style.radius, style.strokeLocation)
    gc.fill(style.fill)
    gc.fill()
    gc.stroke(style.stroke)
    gc.stroke()
  }


  @StyleDsl
  class Style {
    var fill: Color = Color.orange
    var stroke: Color = Color.red

    var lineWidth = 1.0

    /**
     * The radius for the rounded rect in pixels
     */
    var radius: @px Double = 30.0

    var strokeLocation: StrokeLocation = StrokeLocation.Center

  }
}
