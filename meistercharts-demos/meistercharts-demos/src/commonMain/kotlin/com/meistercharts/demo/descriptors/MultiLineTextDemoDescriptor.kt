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

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.addTextUnresolved
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.model.Direction
import com.meistercharts.model.DirectionBasedBasePointProvider
import com.meistercharts.model.Insets
import com.meistercharts.style.BoxStyle

/**
 * Demos that visualizes the functionality of a multi line text
 */
class MultiLineTextDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Multi line text"
  override val description: String = "## How to show multi line text"
  override val category: DemoCategory = DemoCategory.Text

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          layers.addTextUnresolved(
            listOf("First line - top left", "Second line - top left")
          ) {
            anchorDirection = Direction.TopLeft
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)

            margin = Insets.of(3.0)
            boxStyle = BoxStyle(Color.red, Color.darkgrey, padding = Insets(25.0, 5.0, 5.0, 25.0))
          }
          layers.addTextUnresolved(
            listOf("First line - top center", "Second line - top center", "Margin: 10px")
          ) {
            anchorDirection = Direction.TopCenter
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)

            margin = Insets.of(10.0)
            boxStyle = BoxStyle(Color.gray, Color.yellow, padding = Insets(25.0, 5.0, 5.0, 5.0))
          }
          layers.addTextUnresolved(
            listOf("First line - top right", "Second line - top right")
          ) {
            anchorDirection = Direction.TopRight
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)

            margin = Insets.of(3.0)
            boxStyle = BoxStyle(Color.olive, Color.darkgrey, padding = Insets(25.0, 25.0, 5.0, 5.0))
          }

          layers.addTextUnresolved(
            listOf("First line - center left", "Second line - center left")
          ) {
            anchorDirection = Direction.CenterLeft
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)

            margin = Insets.of(3.0)
            boxStyle = BoxStyle(Color.gray, Color.gold, padding = Insets(25.0, 5.0, 5.0, 25.0))
          }
          layers.addTextUnresolved(
            listOf("First line - center center", "Second line - center center")
          ) {
            anchorDirection = Direction.Center
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)

            margin = Insets.of(3.0)
            boxStyle = BoxStyle(Color.magenta, Color.darkgrey, padding = Insets(5.0, 5.0, 5.0, 5.0))
          }
          layers.addTextUnresolved(
            listOf("First line - center right", "Second line - center right")
          ) {
            anchorDirection = Direction.CenterRight
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)

            margin = Insets.of(3.0)
            boxStyle = BoxStyle(Color.gray, Color.blanchedalmond, padding = Insets(5.0, 25.0, 25.0, 5.0))
          }

          layers.addTextUnresolved(
            listOf("First line - bottom left", "Second line - bottom left")
          ) {
            anchorDirection = Direction.BottomLeft
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)

            margin = Insets.of(3.0)
            boxStyle = BoxStyle(Color.beige, Color.darkgrey, padding = Insets(5.0, 5.0, 25.0, 25.0))
          }
          layers.addTextUnresolved(
            listOf("First line - bottom, center", "Second line - bottom center")
          ) {
            anchorDirection = Direction.BottomCenter
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)

            margin = Insets.of(3.0)
            boxStyle = BoxStyle(Color.gray, Color.blue, padding = Insets(5.0, 5.0, 25.0, 5.0))
          }
          layers.addTextUnresolved(
            listOf("First line - bottom right", "Second line - bottom right")
          ) {
            anchorDirection = Direction.BottomRight
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)

            margin = Insets.of(3.0)
            boxStyle = BoxStyle(Color.white, Color.darkgrey, padding = Insets(5.0, 25.0, 25.0, 5.0))
          }
        }
      }
    }
  }
}
