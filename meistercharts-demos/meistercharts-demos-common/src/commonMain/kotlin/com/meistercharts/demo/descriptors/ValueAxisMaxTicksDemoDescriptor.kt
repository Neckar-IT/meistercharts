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
import com.meistercharts.algorithms.layers.PasspartoutLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.axis.ValueAxisLayer
import com.meistercharts.algorithms.layers.axis.withMaxNumberOfTicks
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
import com.meistercharts.demo.configurableInt
import com.meistercharts.model.Vicinity
import com.meistercharts.range.ValueRange
import it.neckar.geometry.Side
import it.neckar.open.provider.BooleanProvider

/**
 * Very simple demo that shows how to work with a value axis layer
 */
class ValueAxisMaxTicksDemoDescriptor() : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("69b84d3e-5c1a-45e7-877f-57e70a464757")
  override val name: String = "Value axis with max ticks"
  override val category: DemoCategory = DemoCategory.Axis
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {

      meistercharts {
        configure {
          layers.addClearBackground()

          val passpartoutLayer = PasspartoutLayer {
            color = { Color("rgba(69, 204, 112, 0.25)") } // use something different from white so the size of the axis can be better grasped
          }
          layers.addLayer(passpartoutLayer)

          val valueRange = ValueRange.linear(0.0, 100.0)
          val valueAxisLayer = ValueAxisLayer(ValueAxisLayer.Configuration(valueRangeProvider = { valueRange })) {
            titleProvider = { _, _ -> "The Äxisq [m²/h]" }
          }

          layers.addLayer(valueAxisLayer)

          passpartoutLayer.configuration.bind(valueAxisLayer.configuration)

          section("Layout")

          var maxTickCount = 5

          configurableInt("Max Tick count") {
            max = 15
            value = maxTickCount

            onChange {
              maxTickCount = it
              valueAxisLayer.configuration.ticks = valueAxisLayer.configuration.ticks.withMaxNumberOfTicks(maxTickCount)
              markAsDirty()
            }
          }

          configurableEnum("Side", valueAxisLayer.configuration.side, Side.entries) {
            onChange {
              valueAxisLayer.configuration.side = it
              markAsDirty()
            }
          }

          configurable("Width", valueAxisLayer.configuration::size) {
            max = 500.0
          }

          configurable("Margin", valueAxisLayer.configuration::margin) {
          }

          section("Title")

          configurableBoolean("Show Title") {
            value = valueAxisLayer.configuration.titleVisible()
            onChange {
              valueAxisLayer.configuration.titleVisible = BooleanProvider(it)
              markAsDirty()
            }
          }

          configurable("Title Gap", valueAxisLayer.configuration::titleGap) {
            max = 20.0
          }

          section("Axis Config")

          configurableEnum("Paint Range", valueAxisLayer.configuration.paintRange, AxisConfiguration.PaintRange.entries) {
            onChange {
              valueAxisLayer.configuration.paintRange = it
              markAsDirty()
            }
          }
          configurableEnum("Tick Orientation", valueAxisLayer.configuration.tickOrientation, Vicinity.entries) {
            onChange {
              valueAxisLayer.configuration.tickOrientation = it
              markAsDirty()
            }
          }
          configurableEnum("Axis End", valueAxisLayer.configuration.axisEndConfiguration, AxisEndConfiguration.entries) {
            onChange {
              valueAxisLayer.configuration.axisEndConfiguration = it
              markAsDirty()
            }
          }

          section("Widths")

          configurable("Axis line width", valueAxisLayer.configuration::axisLineWidth) {
            max = 20.0
          }
          configurable("Tick length", valueAxisLayer.configuration::tickLength) {
            max = 20.0
          }
          configurable("Tick width", valueAxisLayer.configuration::tickLineWidth) {
            max = 20.0
          }
          configurable("Tick Label Gap", valueAxisLayer.configuration::tickLabelGap) {
            max = 20.0
          }

          configurableColor("Background Color", passpartoutLayer.configuration.color()) {
            onChange {
              passpartoutLayer.configuration.color = { it }
              markAsDirty()
            }
          }

          configurable("Tick font", valueAxisLayer.configuration::tickFont) {
          }

          configurable("Title font", valueAxisLayer.configuration::titleFont) {
          }
        }
      }
    }
  }
}
