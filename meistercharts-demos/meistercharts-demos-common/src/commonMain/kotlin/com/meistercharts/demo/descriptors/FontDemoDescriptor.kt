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
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.addTextUnresolved
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.font.FontDescriptor
import com.meistercharts.font.FontFamily
import com.meistercharts.font.FontSize
import com.meistercharts.font.FontStyle
import com.meistercharts.font.FontWeight
import com.meistercharts.geometry.DirectionBasedBasePointProvider
import com.meistercharts.model.Insets
import com.meistercharts.style.BoxStyle
import it.neckar.geometry.Direction
import it.neckar.open.kotlin.lang.asProvider

/**
 * Demos that visualizes the functionality of the FPS layer
 */
class FontDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("acfddf4c-3ec1-4e67-a1ab-8129644bbbe4")
  override val name: String = "Fonts"

  //language=HTML
  override val description: String = "Different fonts"
  override val category: DemoCategory = DemoCategory.Text
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()

          val boxStyle = BoxStyle(Color.orange, Color.gray, padding = Insets.of(5.0))

          val arial = FontFamily.Arial
          val courier = FontFamily.CourierNew
          val ubuntu = FontFamily("Ubuntu")
          val tahoma = FontFamily("Tahoma")

          layers.addTextUnresolved("No Font, ${FontSize.Default.size}") {
            font = FontDescriptor(size = FontSize.Default).asProvider()
            anchorDirection = Direction.Center
            margin = Insets.of(10.0)
            this.boxStyle = boxStyle
          }
          layers.addTextUnresolved("Arial 18px") {
            font = FontDescriptor(arial, FontSize(18.0)).asProvider()
            anchorDirection = Direction.TopRight
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
            margin = Insets.of(10.0)
            this.boxStyle = boxStyle
          }
          layers.addTextUnresolved("Arial 18px bold") {
            font = FontDescriptor(arial, FontSize(18.0), FontWeight.Bold).asProvider()
            anchorDirection = Direction.TopCenter
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
            margin = Insets.of(10.0)
            this.boxStyle = boxStyle
          }
          layers.addTextUnresolved("Arial 18px italic") {
            font = FontDescriptor(arial, FontSize(18.0), style = FontStyle.Italic).asProvider()
            anchorDirection = Direction.TopLeft
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
            margin = Insets.of(10.0)
            this.boxStyle = boxStyle
          }
          layers.addTextUnresolved("Arial 18px bold italic") {
            font = FontDescriptor(arial, FontSize(18.0), FontWeight.Bold, FontStyle.Italic).asProvider()
            anchorDirection = Direction.BottomRight
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
            margin = Insets.of(10.0)
            this.boxStyle = boxStyle
          }
          layers.addTextUnresolved("Courier New 18px") {
            font = FontDescriptor(courier, FontSize(18.0)).asProvider()
            anchorDirection = Direction.BottomLeft
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
            margin = Insets.of(10.0)
            this.boxStyle = boxStyle
          }
          layers.addTextUnresolved("Courier New 18px oblique") {
            font = FontDescriptor(courier, FontSize(18.0), style = FontStyle.Oblique).asProvider()
            anchorDirection = Direction.BottomCenter
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
            margin = Insets.of(10.0)
            this.boxStyle = boxStyle
          }
          layers.addTextUnresolved("Courier New 18px oblique bold") {
            font = FontDescriptor(
              courier,
              FontSize(18.0),
              style = FontStyle.Oblique,
              weight = FontWeight.Bold
            ).asProvider()
            anchorDirection = Direction.CenterLeft
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
            margin = Insets.of(10.0)
            this.boxStyle = boxStyle
          }
          layers.addTextUnresolved("Ubuntu 18px italic") {
            font = FontDescriptor(
              ubuntu,
              FontSize(18.0),
              style = FontStyle.Italic,
              weight = FontWeight.Normal
            ).asProvider()
            anchorDirection = Direction.CenterRight
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
            margin = Insets.of(10.0)
            this.boxStyle = boxStyle
          }
        }
      }
    }
  }
}
