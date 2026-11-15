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
import com.meistercharts.demo.section
import kotlin.uuid.Uuid
import com.meistercharts.range.ValueRange
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.compass.ArrowValuePainter
import com.meistercharts.algorithms.layers.compass.CompassBasePainter
import com.meistercharts.algorithms.layers.compass.CompassTriangleValuePainter
import com.meistercharts.algorithms.layers.compass.GaugePaintable
import com.meistercharts.algorithms.layers.compass.LineValuePainter
import com.meistercharts.algorithms.layers.compass.ModernCompassPainter
import com.meistercharts.color.Color
import com.meistercharts.annotations.Domain
import com.meistercharts.canvas.paintLocation
import com.meistercharts.canvas.saved
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableColor
import com.meistercharts.demo.configurableColorProvider
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableInt
import com.meistercharts.demo.configurableList
import it.neckar.geometry.RotationDirection
import it.neckar.geometry.Size
import com.meistercharts.provider.ValueRangeProvider

class GaugePaintableDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("8eaadb12-7136-4532-a89c-23a88d37d35e")
  override val name: String = "Compass"
  override val description: String = "Draws a compass"
  override val category: DemoCategory = DemoCategory.Paintables
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {

    return MeisterchartsDemo(this) {
      meistercharts {

        configure {
          layers.addClearBackground()

          @Domain var startValueRange = 0.0
          @Domain var endValueRange = 200.0


          val valueRangeProviders = listOf<ValueRangeProvider>(
            { ValueRange.linear(startValueRange, endValueRange) },
            { ValueRange.degrees }
          )

          var currentValueRangeProvider = valueRangeProviders[0]

          @Domain var currentValue = 0.0


          val arrowValuePainter = ArrowValuePainter {
            pointerLineWidth = 3.0
          }
          val lineValuePainter = LineValuePainter()
          val compassTriangleValuePainter = CompassTriangleValuePainter()

          val valuePainters = listOf(arrowValuePainter, lineValuePainter, compassTriangleValuePainter)

          val compassBasePainter = CompassBasePainter() {
            backgroundColor = Color.color(0.9, 0.9, 1.0, 0.5)
            numberInnerCircles = 3
          }
          val modernCompassPainter = ModernCompassPainter()
          val basePainters = listOf(modernCompassPainter, compassBasePainter)

          val paintable = GaugePaintable(
            currentValueRangeProvider,
            { currentValue },
            Size(300.0, 300.0)
          ) {
            basePainter = modernCompassPainter
            valuePainter = arrowValuePainter

            startAt = 3.0 * kotlin.math.PI / 4.0
            extend = 3.0 * kotlin.math.PI / 2.0
            rotationDirection = RotationDirection.Clockwise
          }

          val style = paintable.style

          layers.addLayer(object : AbstractLayer() {
            override val type: LayerType
              get() = LayerType.Content

            override fun paint(paintingContext: LayerPaintingContext) {
              val gc = paintingContext.gc

              gc.saved {
                gc.translateToCenter()
                gc.paintLocation()
                paintable.paint(paintingContext, 0.0, 0.0)
              }
            }
          })


          configurableList("Base Painter", style.basePainter, basePainters) {
            converter = {
              it::class.simpleName ?: toString()
            }

            onChange {
              style.basePainter = it
              markAsDirty()
            }
          }

          configurableList("Value Painter", style.valuePainter, valuePainters) {
            converter = {
              it::class.simpleName ?: toString()
            }

            onChange {
              style.valuePainter = it
              markAsDirty()
            }
          }
          configurableList("ValueRange/Ticks", currentValueRangeProvider, valueRangeProviders) {
            converter = { valueRangeProvider ->
              when (valueRangeProviders.indexOf(valueRangeProvider)) {
                0 -> "Manual"
                1 -> "Degree"
                else -> valueRangeProvider.toString()
              }

            }
            onChange {
              currentValueRangeProvider = it
              markAsDirty()
            }
          }


          configurableDouble("currentValue", currentValue) {
            min = 0.0
            max = 200.0

            onChange {
              currentValue = it
              markAsDirty()
            }
          }

          configurableDouble("startValueRange", startValueRange) {
            min = 0.0
            max = 200.0

            onChange {
              startValueRange = it
              markAsDirty()
            }
          }

          configurableDouble("endValueRange", endValueRange) {
            min = 0.0
            max = 400.0

            onChange {
              endValueRange = it
              markAsDirty()
            }
          }

          configurable("startAt", style::startAt) {
            max = 7.0
          }

          configurable("extend", style::extend) {
            max = 7.0
          }

          configurable("direction", style::rotationDirection, kotlin.enums.enumEntries<RotationDirection>().toList()) {
          }

          configurableDouble("width/height", paintable.size.height) {
            max = 800.0

            onChange {
              paintable.size = Size(it, it)
              markAsDirty()
            }
          }

          section("Compass Base")

          configurableColor("bg color", compassBasePainter.configuration::backgroundColor) {
          }

          configurable("numberInnerCircles", compassBasePainter.configuration::numberInnerCircles) {
            max = 20
          }

          configurableColorProvider("compass color", compassBasePainter.configuration::compassColor) {
          }

          configurableColorProvider("labels color", compassBasePainter.configuration::labelsColor) {
          }

          configurableColorProvider("tick color", compassBasePainter.configuration::tickColor) {
          }

          section("Arrow Pointer")

          configurable("pointerWidth", arrowValuePainter.style::pointerLineWidth) {
            max = 10.0
          }

          configurableColorProvider("pointer color", arrowValuePainter.style::pointerColor) {
          }

          section("Line Pointer")

          configurable("pointerWidth", lineValuePainter.style::pointerWidth) {
            max = 10.0
          }

          configurableColorProvider("pointer color", lineValuePainter.style::pointerColor) {
          }

          configurable("lineGap", lineValuePainter.style::lineGap) {
            max = 20.0
          }
        }
      }
    }
  }
}
