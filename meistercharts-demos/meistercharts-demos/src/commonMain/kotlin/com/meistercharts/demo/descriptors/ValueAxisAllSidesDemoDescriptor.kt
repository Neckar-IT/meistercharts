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

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.axis.AxisEndConfiguration
import com.meistercharts.algorithms.layers.AxisStyle
import com.meistercharts.algorithms.layers.ValueAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.addPasspartout
import com.meistercharts.algorithms.layers.bind
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColor
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableInsets
import com.meistercharts.model.Insets
import com.meistercharts.model.Side
import com.meistercharts.model.Vicinity
import it.neckar.open.provider.BooleanProvider

/**
 * Very simple demo that shows how to work with a value axis layer
 */
class ValueAxisAllSidesDemoDescriptor(
  val vicinity: Vicinity = Vicinity.Inside
) : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Value axis (all 4 sides)) ${vicinity.name}"
  override val description: String = "## Axis on all sides"
  override val category: DemoCategory = DemoCategory.Axis

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {

      meistercharts {
        configure {
          layers.addClearBackground()

          val passpartoutLayers = Side.values().map {
            layers.addPasspartout(Insets.empty, Color("rgba(69, 204, 112, 0.25)")) // use something different from white so the size of the axis can be better grasped
          }

          val valueRange = ValueRange.linear(0.0, 100.0)

          val valueAxisLayers = Side.values().map { axisSide ->
            ValueAxisLayer(ValueAxisLayer.Data(valueRangeProvider = { valueRange })) {
              titleProvider = { _, _ -> "Left [m²/h]" }
              side = axisSide
              tickOrientation = vicinity
            }.also {
              layers.addLayer(it)
            }
          }

          valueAxisLayers.forEachIndexed { index, valueAxisLayer ->
            passpartoutLayers[index].style.bind(valueAxisLayer.style)
          }

          declare {
            section("Layout")
          }

          configurableDouble("Width", valueAxisLayers[0].style.size) {
            max = 500.0

            onChange {
              valueAxisLayers.forEachIndexed { index, valueAxisLayer ->
                valueAxisLayer.style.size = it
              }
              markAsDirty()
            }
          }

          configurableInsets("Margin", valueAxisLayers[0].style.margin) {
            onChange {
              valueAxisLayers.forEachIndexed { index, valueAxisLayer ->
                valueAxisLayer.style.margin = it
              }
              markAsDirty()
            }
          }

          declare {
            section("Title")
          }

          configurableBoolean("Show Title") {
            value = valueAxisLayers[0].style.titleVisible()
            onChange {
              valueAxisLayers.forEach { valueAxisLayer ->
                valueAxisLayer.style.titleVisible = BooleanProvider(it)
              }
              markAsDirty()
            }
          }

          configurableDouble("Title Gap", valueAxisLayers[0].style.titleGap) {
            max = 20.0
            onChange {
              valueAxisLayers.forEach { valueAxisLayer ->
                valueAxisLayer.style.titleGap = it
              }
              markAsDirty()
            }
          }

          declare {
            section("Axis Config")
          }

          configurableEnum("Paint Range", valueAxisLayers[0].style.paintRange, AxisStyle.PaintRange.values()) {
            onChange {
              valueAxisLayers.forEach { valueAxisLayer ->
                valueAxisLayer.style.paintRange = it
              }
              markAsDirty()
            }
          }
          configurableEnum("Tick Orientation", valueAxisLayers[0].style.tickOrientation, Vicinity.values()) {
            onChange {
              valueAxisLayers.forEach { valueAxisLayer ->
                valueAxisLayer.style.tickOrientation = it
              }
              markAsDirty()
            }
          }
          configurableEnum("Axis End", valueAxisLayers[0].style.axisEndConfiguration, AxisEndConfiguration.values()) {
            onChange {
              valueAxisLayers.forEach { valueAxisLayer ->
                valueAxisLayer.style.axisEndConfiguration = it
              }
              markAsDirty()
            }
          }

          declare {
            section("Widths")
          }

          configurableDouble("Axis line width", valueAxisLayers[0].style.axisLineWidth) {
            max = 20.0
            onChange {
              valueAxisLayers.forEach { valueAxisLayer ->
                valueAxisLayer.style.axisLineWidth = it
              }
              markAsDirty()
            }
          }
          configurableDouble("Tick length", valueAxisLayers[0].style.tickLength) {
            max = 20.0
            onChange {
              valueAxisLayers.forEach { valueAxisLayer ->
                valueAxisLayer.style.tickLength = it
              }
              markAsDirty()
            }
          }
          configurableDouble("Tick width", valueAxisLayers[0].style.tickLineWidth) {
            max = 20.0
            onChange {
              valueAxisLayers.forEach { valueAxisLayer ->
                valueAxisLayer.style.tickLineWidth = it
              }
              markAsDirty()
            }
          }
          configurableDouble("Tick Label Gap", valueAxisLayers[0].style.tickLabelGap) {
            max = 20.0
            onChange {
              valueAxisLayers.forEach { valueAxisLayer ->
                valueAxisLayer.style.tickLabelGap = it
              }
              markAsDirty()
            }
          }

          configurableColor("Background Color", passpartoutLayers[0].style.color()) {
            onChange {
              passpartoutLayers.forEach { passpartoutLayer ->
                passpartoutLayer.style.color = { it }
              }
              markAsDirty()
            }
          }

          configurableFont("Tick font", valueAxisLayers[0].style.tickFont) {
            onChange {
              valueAxisLayers.forEach { valueAxisLayer ->
                valueAxisLayer.style.tickFont = it
              }
              markAsDirty()
            }
          }

          configurableFont("Title font", valueAxisLayers[0].style.titleFont) {
            onChange {
              valueAxisLayers.forEach { valueAxisLayer ->
                valueAxisLayer.style.titleFont = it
              }
              markAsDirty()
            }
          }
        }
      }
    }
  }
}
