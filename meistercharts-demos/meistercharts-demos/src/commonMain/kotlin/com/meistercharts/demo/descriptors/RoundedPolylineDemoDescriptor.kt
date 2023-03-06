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
import com.meistercharts.canvas.StyleDsl
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.model.Coordinates
import it.neckar.open.kotlin.lang.getModulo
import it.neckar.open.unit.other.px

/**
 *
 */
class RoundedPolylineDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Rounded Polyline"
  override val description: String = "Rounded Polyline"
  override val category: DemoCategory = DemoCategory.Primitives

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {

    return ChartingDemo {
      meistercharts {

        configure {
          layers.addClearBackground()

          val gradientLayer = RoundedPolylineLayer()
          layers.addLayer(gradientLayer)

          configurableDouble("radius", gradientLayer.style::radius) {
            max = 50.0
          }
        }
      }
    }
  }
}

private class RoundedPolylineLayer : AbstractLayer() {
  val style: Style = Style()

  override val type: LayerType
    get() = LayerType.Content

  val coordinates = listOf(
    Coordinates.of(10.0, 10.0),
    Coordinates.of(70.0, 10.0),
    Coordinates.of(70.0, 170.0),
    Coordinates.of(150.0, 170.0),
    Coordinates.of(150.0, 200.0),
    Coordinates.of(10.0, 200.0)
  )

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    gc.beginPath()

    //Find the start point between the first two coords
    gc.moveTo(coordinates[0].center(coordinates[1]))

    for (i in 1..coordinates.size) {
      val controlPoint = coordinates.getModulo(i)
      val target = coordinates.getModulo(i + 1)

      gc.arcTo(controlPoint, target, style.radius)
    }

    gc.closePath()

    gc.fill(style.fill)
    gc.fill()
    gc.stroke(style.stroke)
    gc.stroke()
  }

  @StyleDsl
  class Style {
    var fill: Color = Color.orange
    var stroke: Color = Color.red

    /**
     * The radius for the rounded rect in pixels
     */
    var radius: @px Double = 30.0

  }
}
