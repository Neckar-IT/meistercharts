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
import com.meistercharts.algorithms.layers.AxisConfiguration
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.addPasspartout
import com.meistercharts.algorithms.layers.axis.ValueAxisLayer
import com.meistercharts.algorithms.layers.bind
import com.meistercharts.axis.AxisEndConfiguration
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableColor
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFontProvider
import com.meistercharts.demo.configurableInsets
import com.meistercharts.model.Insets
import com.meistercharts.model.Vicinity
import com.meistercharts.range.ValueRange
import it.neckar.geometry.Side
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.provider.BooleanProvider

/**
 * Very simple demo that shows how to work with a value axis layer
 */
class ValueAxisAllSidesDemoDescriptor(
  val vicinity: Vicinity = Vicinity.Inside,
) : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("47ae2983-087d-4717-beb6-1eaa6c982f3e")
  override val name: String = "Value axis (all 4 sides) ${vicinity.name}"
  override val description: String = "A value axis rendered on all four sides."
  override val category: DemoCategory = DemoCategory.Axis

  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {

      meistercharts {
        configure {
          layers.addClearBackground()

          val passpartoutLayers = Side.entries.map {
            layers.addPasspartout(Insets.empty, Color("rgba(69, 204, 112, 0.25)").asProvider()) // use something different from white so the size of the axis can be better grasped
          }

          val valueRange = ValueRange.linear(0.0, 100.0)

          val valueAxisLayers = Side.entries.map { axisSide ->
            ValueAxisLayer(ValueAxisLayer.Configuration(valueRangeProvider = { valueRange })) {
              titleProvider = { _, _ -> "Left [m²/h]" }
              side = axisSide
              tickOrientation = vicinity
            }.also {
              layers.addLayer(it)
            }
          }

          valueAxisLayers.forEachIndexed { index, valueAxisLayer ->
            passpartoutLayers[index].configuration.bind(valueAxisLayer.configuration)
          }

          section("Layout")

          configurableDouble("Width", valueAxisLayers[0].configuration.size) {
            max = 500.0

            onChange {
              valueAxisLayers.forEachIndexed { index, valueAxisLayer ->
                valueAxisLayer.configuration.size = it
              }
              markAsDirty()
            }
          }

          configurableInsets("Margin", valueAxisLayers[0].configuration.margin) {
            onChange {
              valueAxisLayers.forEachIndexed { index, valueAxisLayer ->
                valueAxisLayer.configuration.margin = it
              }
              markAsDirty()
            }
          }

          section("Title")

          configurableBoolean("Show Title") {
            value = valueAxisLayers[0].configuration.titleVisible()
            onChange {
              valueAxisLayers.forEach { valueAxisLayer ->
                valueAxisLayer.configuration.titleVisible = BooleanProvider(it)
              }
              markAsDirty()
            }
          }

          configurableDouble("Title Gap", valueAxisLayers[0].configuration.titleGap) {
            max = 20.0
            onChange {
              valueAxisLayers.forEach { valueAxisLayer ->
                valueAxisLayer.configuration.titleGap = it
              }
              markAsDirty()
            }
          }

          section("Axis Config")

          configurableEnum("Paint Range", valueAxisLayers[0].configuration.paintRange, AxisConfiguration.PaintRange.entries) {
            onChange {
              valueAxisLayers.forEach { valueAxisLayer ->
                valueAxisLayer.configuration.paintRange = it
              }
              markAsDirty()
            }
          }
          configurableEnum("Tick Orientation", valueAxisLayers[0].configuration.tickOrientation, Vicinity.entries) {
            onChange {
              valueAxisLayers.forEach { valueAxisLayer ->
                valueAxisLayer.configuration.tickOrientation = it
              }
              markAsDirty()
            }
          }
          configurableEnum("Axis End", valueAxisLayers[0].configuration.axisEndConfiguration, AxisEndConfiguration.entries) {
            onChange {
              valueAxisLayers.forEach { valueAxisLayer ->
                valueAxisLayer.configuration.axisEndConfiguration = it
              }
              markAsDirty()
            }
          }

          section("Widths")

          configurableDouble("Axis line width", valueAxisLayers[0].configuration.axisLineWidth) {
            max = 20.0
            onChange {
              valueAxisLayers.forEach { valueAxisLayer ->
                valueAxisLayer.configuration.axisLineWidth = it
              }
              markAsDirty()
            }
          }
          configurableDouble("Tick length", valueAxisLayers[0].configuration.tickLength) {
            max = 20.0
            onChange {
              valueAxisLayers.forEach { valueAxisLayer ->
                valueAxisLayer.configuration.tickLength = it
              }
              markAsDirty()
            }
          }
          configurableDouble("Tick width", valueAxisLayers[0].configuration.tickLineWidth) {
            max = 20.0
            onChange {
              valueAxisLayers.forEach { valueAxisLayer ->
                valueAxisLayer.configuration.tickLineWidth = it
              }
              markAsDirty()
            }
          }
          configurableDouble("Tick Label Gap", valueAxisLayers[0].configuration.tickLabelGap) {
            max = 20.0
            onChange {
              valueAxisLayers.forEach { valueAxisLayer ->
                valueAxisLayer.configuration.tickLabelGap = it
              }
              markAsDirty()
            }
          }

          configurableColor("Background Color", passpartoutLayers[0].configuration.color()) {
            onChange {
              passpartoutLayers.forEach { passpartoutLayer ->
                passpartoutLayer.configuration.color = { it }
              }
              markAsDirty()
            }
          }

          configurable("Tick font", valueAxisLayers[0].configuration::tickFont) {
            onChange {
              valueAxisLayers.forEach { valueAxisLayer ->
                valueAxisLayer.configuration.tickFont = it.asProvider()
              }
              markAsDirty()
            }
          }

          configurable("Title font", valueAxisLayers[0].configuration::titleFont) {
            onChange {
              valueAxisLayers.forEach { valueAxisLayer ->
                valueAxisLayer.configuration.titleFont = it.asProvider()
              }
              markAsDirty()
            }
          }
        }
      }
    }
  }
}
