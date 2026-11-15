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

import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.FillRule
import com.meistercharts.canvas.fillStyle
import com.meistercharts.canvas.strokeStyle
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import it.neckar.geometry.Direction

/**
 * Visualizes the effects of the fill rule
 */
class FillRuleDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("94e7a08f-ad38-4232-99a4-995a4a0f4c28")
  override val name: String = "Fill Rule"

  override val variabilityType: VariabilityType = VariabilityType.Stable
  override val quality: DemoQuality = DemoQuality.High
  override val category: DemoCategory = DemoCategory.Primitives

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()

          val myLayer = object : AbstractLayer() {
            override val type: LayerType = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              kotlin.run {
                val fillRule = FillRule.EvenOdd

                gc.translate(30.0, 30.0)
                gc.fillText("Fill Rule: $fillRule", 0.0, 0.0, Direction.TopLeft)
                paintPaths(gc, fillRule)
              }

              gc.translate(0.0, 180.0)

              kotlin.run {
                gc.fillStyle(Color.black)
                val fillRule = FillRule.NonZero
                gc.fillText("Fill Rule: $fillRule", 0.0, 0.0, Direction.TopLeft)
                paintPaths(gc, fillRule)
              }
            }

            private fun paintPaths(gc: CanvasRenderingContext, fillRule: FillRule) {
              // First example: Rectangles with a hole
              gc.beginPath()

              // Outer rectangle
              gc.moveTo(50.0, 50.0)
              gc.lineTo(150.0, 50.0)
              gc.lineTo(150.0, 150.0)
              gc.lineTo(50.0, 150.0)
              gc.closePath()

              // Inner rectangle: Reverse direction for NonZero
              // Inner rect: Hole if fill rule is even-odd
              gc.moveTo(70.0, 70.0)
              gc.lineTo(130.0, 70.0)
              gc.lineTo(130.0, 130.0)
              gc.lineTo(70.0, 130.0)
              gc.closePath()

              gc.fillStyle(Color.darkblue)
              gc.fillRule(fillRule)
              gc.fill()

              gc.strokeStyle(Color.green)
              gc.stroke()

              // Second example: Self-overlapping star
              gc.beginPath()

              gc.moveTo(200.0, 50.0)
              gc.lineTo(240.0, 150.0)
              gc.lineTo(160.0, 90.0)
              gc.lineTo(240.0, 90.0)
              gc.lineTo(160.0, 150.0)
              gc.closePath()

              gc.fillStyle(Color.red)
              gc.fillRule(fillRule)
              gc.fill()

              gc.strokeStyle(Color.black)
              gc.stroke()

              // Third example: Overlapping circles
              gc.beginPath()

              // First circle
              gc.arcCenter(centerX = 300.0, centerY = 100.0, radius = 40.0, startAngle = 0.0, extend = 360.0)
              // Second circle, overlapping with the first
              gc.arcCenter(centerX = 330.0, centerY = 100.0, radius = 40.0, startAngle = 0.0, extend = 360.0)

              gc.fillStyle(Color.purple)
              gc.fillRule(fillRule)
              gc.fill()

              gc.strokeStyle(Color.black)
              gc.stroke()
            }
          }

          layers.addLayer(myLayer)
        }
      }
    }
  }
}
