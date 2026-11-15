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
import com.meistercharts.demo.configurableSeparate
import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.AxisConfiguration
import com.meistercharts.algorithms.layers.PasspartoutLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.axis.ValueAxisLayer
import com.meistercharts.algorithms.layers.axis.withMaxNumberOfTicks
import com.meistercharts.algorithms.layers.bind
import com.meistercharts.axis.AxisEndConfiguration
import com.meistercharts.charts.ContentViewportGestalt
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.button
import com.meistercharts.demo.configurableBooleanProvider
import com.meistercharts.demo.configurableColorPickerProvider
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFontProvider
import com.meistercharts.demo.configurableInsets
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.configurableInt
import com.meistercharts.demo.section
import com.meistercharts.model.Insets
import com.meistercharts.model.Vicinity
import com.meistercharts.range.LinearValueRange
import com.meistercharts.range.LogarithmicValueRange
import com.meistercharts.range.ValueRange
import it.neckar.geometry.Side
import it.neckar.open.collections.fastForEach
import it.neckar.open.formatting.decimalFormat
import it.neckar.uuid.plus

/**
 * Very simple demo that shows how to work with a value axis layer
 */
class ValueAxisDemoDescriptor : MeisterchartsDemoDescriptor<ValueAxisDemoConfig> {
  override val uuid: Uuid = Uuid.parse("75be8f2a-13ec-4300-91e1-f653f79f7d62")
  override val name: String = "Value axis"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  //language=HTML
  override val description: String = "Visualizes a value axis."

  override val category: DemoCategory = DemoCategory.Axis

  override val predefinedConfigurations: List<PredefinedConfiguration<ValueAxisDemoConfig>> = ValueAxisDemoConfig.createConfigs()

  override fun prepareDemo(configuration: PredefinedConfiguration<ValueAxisDemoConfig>?): MeisterchartsDemo {
    require(configuration != null) { "ValueAxisDemoDescriptor.prepareDemo requires a predefined configuration" }

    return MeisterchartsDemo(this) {

      val valueAxisLayer = configuration.payload.createValueAxis()

      meistercharts {
        val contentViewportGestalt = ContentViewportGestalt(Insets.all15)
        contentViewportGestalt.configure(this@meistercharts)

        configure {
          layers.addClearBackground()
          val passpartoutLayer = PasspartoutLayer {
            color = { Color("rgba(69, 204, 112, 0.25)") } // use something different from white so the size of the axis can be better grasped
          }
          layers.addLayer(passpartoutLayer)

          valueAxisLayer.configuration.apply {
            ticks = ticks.withMaxNumberOfTicks(10)
            size = 120.0
          }
          layers.addLayer(valueAxisLayer)

          passpartoutLayer.configuration.bind(valueAxisLayer.configuration)

          section("Layout")

          configurableEnum("Side", valueAxisLayer.configuration.side, Side.entries) {
            onChange {
              valueAxisLayer.configuration.side = it
              markAsDirty()
            }
          }

          configurableInt("Max tick count") {
            min = 0
            max = 50
            value = 10
            onChange {
              valueAxisLayer.configuration.ticks = valueAxisLayer.configuration.ticks.withMaxNumberOfTicks(it)
              markAsDirty()
            }
          }

          configurable("Axis size", valueAxisLayer.configuration::size) {
            max = 500.0
          }

          configurable("Axis margin", valueAxisLayer.configuration::margin) {
          }

          configurable("Content Viewport Margin", chartSupport.rootChartState::contentViewportMargin)

          section("Title")

          configurable("Show Title", valueAxisLayer.configuration::titleVisible) {
          }

          configurable("Title Gap", valueAxisLayer.configuration::titleGap) {
            max = 20.0
          }

          section("Axis Config")

          configurable("Paint Range", valueAxisLayer.configuration::paintRange, kotlin.enums.enumEntries<AxisConfiguration.PaintRange>().toList()) {
          }
          configurable("Tick Orientation", valueAxisLayer.configuration::tickOrientation, kotlin.enums.enumEntries<Vicinity>().toList()) {
          }
          configurable("Axis End", valueAxisLayer.configuration::axisEndConfiguration, kotlin.enums.enumEntries<AxisEndConfiguration>().toList()) {
          }

          section("Axis")

          configurable("Line color", valueAxisLayer.configuration::lineColor) {
          }

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

          configurable("Background Color", passpartoutLayer.configuration::color) {}

          configurable("Tick font", valueAxisLayer.configuration::tickFont) {}

          configurableInt("Tick Format Decimals") {
            value = 2
            onChange {
              valueAxisLayer.configuration.ticksFormat = decimalFormat(it, it)
              markAsDirty()
            }
          }

          configurable("Title font", valueAxisLayer.configuration::titleFont) {}

          configurableSeparate("Content Viewport Margin", contentViewportGestalt::contentViewportMargin)


          section("Apply-Methods")

          button("Hide Ticks") {
            valueAxisLayer.configuration.hideTicks()
            markAsDirty()
          }
          button("Show Ticks") {
            valueAxisLayer.configuration.showTicks()
            markAsDirty()
          }
          button("Hide Axis Line") {
            valueAxisLayer.configuration.hideAxisLine()
            markAsDirty()
          }
          button("Show Axis Line") {
            valueAxisLayer.configuration.showAxisLine()
            markAsDirty()
          }
        }
      }
    }
  }
}

data class ValueAxisDemoConfig(
  val side: Side,
  val axisTickOrientation: Vicinity,
  val valueRange: ValueRange
) {
  override fun toString(): String {
    val valueRangeLabel = if (valueRange is LogarithmicValueRange) " - log" else ""
    return "${side.name} - ${axisTickOrientation.name}$valueRangeLabel"
  }

  fun createValueAxis(): ValueAxisLayer {
    return when (valueRange) {
      is LinearValueRange -> ValueAxisLayer.linear("The Äxisq [m²/h]", valueRange)
      is LogarithmicValueRange -> ValueAxisLayer.logarithmic("The logarithmic Äxisq [m²/h]", valueRange)
      else -> ValueAxisLayer(ValueAxisLayer.Configuration(valueRangeProvider = { valueRange }))
    }.also {
      it.configuration.side = side
      it.configuration.tickOrientation = axisTickOrientation
    }
  }

  companion object {
    fun createConfigs(): List<PredefinedConfiguration<ValueAxisDemoConfig>> {
      return buildList {
        val valueRanges = listOf(
          ValueRange.linear(0.0, 100.0),
          ValueRange.logarithmic(0.01, 10000.0)
        )
        valueRanges.fastForEach { valueRange ->
          Side.entries.fastForEach { side ->
            Vicinity.entries.fastForEach { axisTickOrientation ->
              add(
                PredefinedConfiguration(
                  Uuid.parse("ab784a9d-3311-4366-9b5f-5adca9d4489f")
                    .plus(axisTickOrientation)
                    .plus(side)
                    .plus(valueRange.start.toInt() + valueRange.end.toInt() * 12), // do NOT use hashCode here, because it delivers different results for java and js, which would lead to a difference in uuids!
                  ValueAxisDemoConfig(side, axisTickOrientation, valueRange)
                )
              )
            }
          }
        }
      }
    }
  }
}
