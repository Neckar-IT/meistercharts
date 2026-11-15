/*
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

import com.meistercharts.demo.configurable
import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.color.Color
import com.meistercharts.canvas.StrokeLocation
import com.meistercharts.canvas.fill
import com.meistercharts.canvas.fillRect
import com.meistercharts.canvas.paintLocation
import com.meistercharts.canvas.stroke
import com.meistercharts.canvas.strokeRect
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import it.neckar.geometry.Direction

/**
 *
 */
class DrawingPrimitivesRectanglesDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("90df34ce-7cf7-45e5-9412-b645203d0d0d")
  override val name: String = "Drawing Primitives: Rectangles with anchor"

  //language=HTML
  override val description: String = "Shows how to draw rectangles with an anchor"
  override val category: DemoCategory = DemoCategory.Primitives
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {

    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()

          val layer = object : AbstractLayer() {
            var x = 300.0
            var y = 300.0
            var width = 220.0
            var height = 140.0
            var anchorDirection = Direction.Center
            var anchorGapHorizontal = 0.0
            var anchorGapVertical = 0.0
            var strokeLocation: StrokeLocation = StrokeLocation.Center
            var lineWidth = 1.0

            var debugRect = true

            override val type: LayerType
              get() = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              gc.paintLocation(x, y)

              gc.fill(Color.orangered)
              gc.fillRect(x, y, width, height, anchorDirection, anchorGapHorizontal, anchorGapVertical)

              gc.lineWidth = lineWidth
              gc.stroke(Color.blue)
              gc.strokeRect(x, y, width, height, anchorDirection, anchorGapHorizontal, anchorGapVertical, strokeLocation)

              if (debugRect) {
                gc.lineWidth = 1.0
                gc.stroke(Color.silver)
                gc.strokeRect(x, y, width, height, anchorDirection, anchorGapHorizontal, anchorGapVertical, StrokeLocation.Center)
              }
            }
          }
          layers.addLayer(
            layer
          )

          configurable("Debug Bounding Box", layer::debugRect)

          configurable("x", layer::x) {
            max = 800.0
          }
          configurable("y", layer::y) {
            max = 800.0
          }

          configurable("width", layer::width) {
            max = 800.0
          }
          configurable("height", layer::height) {
            max = 800.0
          }

          configurable("Anchor Direction", layer::anchorDirection, kotlin.enums.enumEntries<Direction>().toList())

          configurable("anchorGap Horizontal", layer::anchorGapHorizontal) {
            max = 800.0
          }
          configurable("anchorGap Vertical", layer::anchorGapVertical) {
            max = 800.0
          }

          configurable("stroke location", layer::strokeLocation, kotlin.enums.enumEntries<StrokeLocation>().toList())

          configurable("line width", layer::lineWidth) {
            max = 20.0
          }
        }
      }
    }
  }
}
