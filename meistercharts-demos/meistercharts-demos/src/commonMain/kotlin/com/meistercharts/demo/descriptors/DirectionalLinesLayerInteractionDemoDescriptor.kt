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

import com.meistercharts.algorithms.layers.DirectionalLinesLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.mouseOverInteractions
import com.meistercharts.charts.ContentViewportGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.section
import com.meistercharts.model.Direction
import com.meistercharts.model.Insets
import it.neckar.open.provider.CoordinatesProvider1
import it.neckar.open.provider.MultiProvider

/**
 * A simple hello world demo
 */
class DirectionalLinesLayerInteractionDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Directional Lines Interaction"
  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {

        val contentViewportGestalt = ContentViewportGestalt(Insets.all15)
        contentViewportGestalt.configure(this)

        configure {
          layers.addClearBackground()

          val config = object {
            var x1: Double = 100.0
            var y1: Double = 150.0
            var x2: Double = 170.0
            var y2: Double = 250.0

            var direction1 = Direction.CenterRight
            var direction2 = Direction.CenterRight
            var lineEndsAtModes = DirectionalLinesLayer.LineEndsAtMode.WithinContentViewport
          }

          val location: CoordinatesProvider1<LayerPaintingContext> = object : CoordinatesProvider1<LayerPaintingContext> {
            override fun size(param1: LayerPaintingContext): Int = 2

            override fun xAt(index: Int, param1: LayerPaintingContext): Double {
              return when (index) {
                0 -> config.x1
                1 -> config.x2
                else -> throw IllegalArgumentException("Invalid index <$index>")
              }
            }

            override fun yAt(index: Int, param1: LayerPaintingContext): Double {
              return when (index) {
                0 -> config.y1
                1 -> config.y2
                else -> throw IllegalArgumentException("Invalid index <$index>")
              }
            }
          }

          val directionalLinesLayer = DirectionalLinesLayer(
            DirectionalLinesLayer.Configuration(
              locations = location,
              directions = MultiProvider.invoke { index ->
                when (index) {
                  0 -> config.direction1
                  1 -> config.direction2
                  else -> throw IllegalArgumentException("Invalid index <$index>")
                }
              }
            )
          ) {
            lineEndsAtMode = { config.lineEndsAtModes }
          }
          layers.addLayer(directionalLinesLayer)
          layers.addLayer(directionalLinesLayer.mouseOverInteractions())

          section("Line 1")
          configurableDouble("x:", config::x1) {
            max = 500.0
          }
          configurableDouble("y:", config::y1) {
            max = 500.0
          }
          configurableEnum("Direction", config::direction1)

          section("Line 2")
          configurableDouble("x:", config::x2) {
            max = 500.0
          }
          configurableDouble("y:", config::y2) {
            max = 500.0
          }
          configurableEnum("Direction", config::direction2)

          configurableEnum("Line Ends At Mode", config::lineEndsAtModes)

          configurableInsetsSeparate("Content Viewport", contentViewportGestalt.contentViewportMarginProperty)
        }
      }
    }
  }
}
