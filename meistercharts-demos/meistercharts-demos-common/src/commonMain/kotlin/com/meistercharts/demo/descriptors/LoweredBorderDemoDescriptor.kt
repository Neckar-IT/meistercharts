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
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType

/**
 */
class LoweredBorderDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("62bd6874-d6b5-4fed-83d6-bcc15d832a0c")
  override val name: String = "Lowered Border"
  override val category: DemoCategory = DemoCategory.Primitives
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable


  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()
          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType = LayerType.Content
            override fun paint(paintingContext: LayerPaintingContext) {
              val ctx = paintingContext.gc

              val x = 50.0
              val y = 50.0
              val width = 200.0
              val height = 200.0
              val borderSize = 4.0

              // Draw top and left border - lighter shade
              ctx.strokeStyle(Color.web("#CCCCCC"))
              ctx.lineWidth = borderSize

              // Top
              ctx.beginPath()
              ctx.moveTo(x, y)
              ctx.lineTo(x + width, y)
              ctx.stroke()

              // Left
              ctx.beginPath()
              ctx.moveTo(x, y)
              ctx.lineTo(x, y + height)
              ctx.stroke()

              // Draw bottom and right border - darker shade
              ctx.strokeStyle = "#666666"
              ctx.lineWidth = borderSize

              // Bottom
              ctx.beginPath()
              ctx.moveTo(x, y + height)
              ctx.lineTo(x + width, y + height)
              ctx.stroke()

              // Right
              ctx.beginPath()
              ctx.moveTo(x + width, y)
              ctx.lineTo(x + width, y + height)
              ctx.stroke()
            }
          })
        }
      }
    }
  }
}
