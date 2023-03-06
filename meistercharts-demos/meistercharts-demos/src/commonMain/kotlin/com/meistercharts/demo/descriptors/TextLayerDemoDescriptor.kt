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
 * Demos that visualizes the functionality of the FPS layer
 */
class TextLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Text"
  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          val boxStyle = BoxStyle(Color.gray, Color.darkgrey, padding = Insets.of(5.0))

          layers.addTextUnresolved("Center") {
            anchorDirection = Direction.Center
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
            margin = Insets.of(10.0)
            this.boxStyle = boxStyle
          }
          layers.addTextUnresolved("Top Right. Insets 10") {
            anchorDirection = Direction.TopRight
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
            margin = Insets.of(10.0)
            this.boxStyle = boxStyle
          }
          layers.addTextUnresolved("Top Center. Insets 10") {
            anchorDirection = Direction.TopCenter
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
            margin = Insets.of(10.0)
            this.boxStyle = boxStyle
          }
          layers.addTextUnresolved("Top Left. Insets 5") {
            anchorDirection = Direction.TopLeft
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
            margin = Insets.of(5.0)
            this.boxStyle = boxStyle
          }
          layers.addTextUnresolved("Bottom Right. Insets 50") {
            anchorDirection = Direction.BottomRight
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
            margin = Insets.of(50.0)
            this.boxStyle = boxStyle
          }
          layers.addTextUnresolved("Bottom Left. Insets 0") {
            anchorDirection = Direction.BottomLeft
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
            margin = Insets.of(0.0)
            this.boxStyle = boxStyle
          }
          layers.addTextUnresolved("Bottom Center. Insets 80") {
            anchorDirection = Direction.BottomCenter
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
            margin = Insets.of(80.0)
            this.boxStyle = boxStyle
          }
          layers.addTextUnresolved("Left Center. Insets 5") {
            anchorDirection = Direction.CenterLeft
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
            margin = Insets.of(5.0)
            this.boxStyle = boxStyle
          }
          layers.addTextUnresolved("Right Center. Insets 5") {
            anchorDirection = Direction.CenterRight
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
            margin = Insets.of(5.0)
            this.boxStyle = boxStyle
          }
        }
      }
    }
  }
}
