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
import com.meistercharts.algorithms.layers.PasspartoutLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.axis.ValueAxisWithOffsetLayer
import com.meistercharts.algorithms.layers.bind
import com.meistercharts.axis.AxisEndConfiguration
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableColorPickerProvider
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableInt
import com.meistercharts.demo.section
import com.meistercharts.model.Vicinity
import com.meistercharts.range.ValueRange
import it.neckar.geometry.Side
import it.neckar.open.collections.fastForEach
import it.neckar.uuid.plus

/**
 * Demonstrates the value axis with offset when zoomed in far enough that the remainder labels use fraction digits.
 */
class ValueAxisWithOffsetFractionsDemoDescriptor : MeisterchartsDemoDescriptor<ValueAxisWithOffsetFractionsConfig> {
  override val uuid: Uuid = Uuid.parse("b9f0c4d2-8a3e-4c17-9b6d-2f4e1a5c8d33")
  override val name: String = "Value axis with offset (fractions)"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  //language=HTML
  override val description: String = "<h3>Offset axis zoomed in - remainder labels show fraction digits</h3>"

  override val category: DemoCategory = DemoCategory.Axis

  override val predefinedConfigurations: List<PredefinedConfiguration<ValueAxisWithOffsetFractionsConfig>> = ValueAxisWithOffsetFractionsConfig.createConfigs()

  override fun prepareDemo(configuration: PredefinedConfiguration<ValueAxisWithOffsetFractionsConfig>?): MeisterchartsDemo {
    require(configuration != null) { "ValueAxisWithOffsetFractionsDemoDescriptor.prepareDemo requires a predefined configuration" }

    val config = configuration.payload

    return MeisterchartsDemo(this) {

      val valueAxisWithOffsetLayer = ValueAxisWithOffsetLayer("Offset Axis", config.valueRange) {
        side = config.side
        spaceForDigits = config.spaceForDigits
        offsetAreaSize = 45.0
        size = 160.0
      }

      meistercharts {
        configure {
          layers.addClearBackground()

          val passpartoutLayer = PasspartoutLayer {
            color = { Color("rgba(69, 204, 112, 0.25)") }
          }
          layers.addLayer(passpartoutLayer)
          layers.addLayer(valueAxisWithOffsetLayer)

          passpartoutLayer.configuration.bind(valueAxisWithOffsetLayer.configuration)

          section("Offset")

          configurable("Space for Digits", valueAxisWithOffsetLayer.configuration::spaceForDigits) {
            min = 1
            max = 8
          }

          configurable("Offset area size", valueAxisWithOffsetLayer.configuration::offsetAreaSize) {
            max = 200.0
          }

          section("Layout")

          configurable("Side", valueAxisWithOffsetLayer.configuration::side, Side.entries)
          configurable("Tick Orientation", valueAxisWithOffsetLayer.configuration::tickOrientation, Vicinity.entries)
          configurable("Axis End", valueAxisWithOffsetLayer.configuration::axisEndConfiguration, AxisEndConfiguration.entries)

          configurable("Axis size", valueAxisWithOffsetLayer.configuration::size) {
            max = 500.0
          }

          section("Colors")

          configurable("Offset label color", valueAxisWithOffsetLayer.configuration::offsetTickLabelColor) {
          }
          configurable("Background Color", passpartoutLayer.configuration::color) {
          }
        }
      }
    }
  }
}

/**
 * The predefined configuration for [ValueAxisWithOffsetFractionsDemoDescriptor].
 */
data class ValueAxisWithOffsetFractionsConfig(
  val side: Side,
  val valueRange: ValueRange,
  val spaceForDigits: Int,
  val label: String,
) {
  override fun toString(): String {
    return "${side.name} - $label"
  }

  companion object {
    fun createConfigs(): List<PredefinedConfiguration<ValueAxisWithOffsetFractionsConfig>> {
      val ranges = listOf(
        Triple(ValueRange.linear(147_528.125, 147_537.578), 3, "147k + fraction"),
        Triple(ValueRange.linear(23.740, 23.746), 1, "decimal offset"),
      )

      return buildList {
        ranges.fastForEach { (valueRange, spaceForDigits, label) ->
          listOf(Side.Left, Side.Bottom).fastForEach { side ->
            add(
              PredefinedConfiguration(
                Uuid.parse("c2e7a1b4-9d63-4f28-8a51-3b6e0d4c7f92")
                  .plus(side)
                  .plus((valueRange.start * 1000).toInt() + spaceForDigits),
                ValueAxisWithOffsetFractionsConfig(side, valueRange, spaceForDigits, label),
              ),
            )
          }
        }
      }
    }
  }
}
