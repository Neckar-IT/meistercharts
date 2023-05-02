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

import com.meistercharts.algorithms.LinearValueRange
import com.meistercharts.algorithms.LogarithmicValueRange
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layers.PasspartoutLayer
import com.meistercharts.algorithms.layers.ValueAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.bind
import com.meistercharts.algorithms.layers.withMaxNumberOfTicks
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.charts.ContentViewportGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.button
import com.meistercharts.demo.configurableBooleanProvider
import com.meistercharts.demo.configurableColorPickerProvider
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.demo.configurableInsets
import com.meistercharts.demo.configurableInsetsSeparate
import com.meistercharts.demo.configurableInt
import com.meistercharts.demo.section
import com.meistercharts.model.Insets
import com.meistercharts.model.Side
import com.meistercharts.model.Vicinity
import it.neckar.open.collections.fastForEach
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.kotlin.lang.enumEntries

/**
 * Very simple demo that shows how to work with a value axis layer
 */
class ValueAxisDemoDescriptor : ChartingDemoDescriptor<ValueAxisDemoConfig> {
  override val name: String = "Value axis"

  //language=HTML
  override val description: String = "<h3>Visualizes a value axis</h3>"

  override val category: DemoCategory = DemoCategory.Axis

  override val predefinedConfigurations: List<PredefinedConfiguration<ValueAxisDemoConfig>> = ValueAxisDemoConfig.createConfigs()

  override fun createDemo(configuration: PredefinedConfiguration<ValueAxisDemoConfig>?): ChartingDemo {
    require(configuration != null)

    return ChartingDemo {

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

          valueAxisLayer.style.apply {
            ticks = ticks.withMaxNumberOfTicks(10)
            size = 120.0
          }
          layers.addLayer(valueAxisLayer)

          passpartoutLayer.style.bind(valueAxisLayer.style)

          declare {
            section("Layout")
          }

          configurableEnum("Side", valueAxisLayer.style.side, Side.entries) {
            onChange {
              valueAxisLayer.style.side = it
              markAsDirty()
            }
          }

          configurableInt("Max tick count") {
            min = 0
            max = 50
            value = 10
            onChange {
              valueAxisLayer.style.ticks = valueAxisLayer.style.ticks.withMaxNumberOfTicks(it)
              markAsDirty()
            }
          }

          configurableDouble("Axis size", valueAxisLayer.style::size) {
            max = 500.0
          }

          configurableInsets("Axis margin", valueAxisLayer.style::margin) {
          }

          configurableInsets("Content Viewport Margin", chartSupport.rootChartState::contentViewportMargin)

          declare {
            section("Title")
          }

          configurableBooleanProvider("Show Title", valueAxisLayer.style::titleVisible) {
          }

          configurableDouble("Title Gap", valueAxisLayer.style::titleGap) {
            max = 20.0
          }

          declare {
            section("Axis Config")
          }

          configurableEnum("Paint Range", valueAxisLayer.style::paintRange, enumEntries()) {
          }
          configurableEnum("Tick Orientation", valueAxisLayer.style::tickOrientation, enumEntries()) {
          }
          configurableEnum("Axis End", valueAxisLayer.style::axisEndConfiguration, enumEntries()) {
          }

          declare {
            section("Axis")
          }

          configurableColorPickerProvider("Line color", valueAxisLayer.style::lineColor) {
          }

          configurableDouble("Axis line width", valueAxisLayer.style::axisLineWidth) {
            max = 20.0
          }
          configurableDouble("Tick length", valueAxisLayer.style::tickLength) {
            max = 20.0
          }
          configurableDouble("Tick width", valueAxisLayer.style::tickLineWidth) {
            max = 20.0
          }
          configurableDouble("Tick Label Gap", valueAxisLayer.style::tickLabelGap) {
            max = 20.0
          }

          configurableColorPickerProvider("Background Color", passpartoutLayer.style::color) {}

          configurableFont("Tick font", valueAxisLayer.style::tickFont) {}

          configurableInt("Tick Format Decimals") {
            value = 2
            onChange {
              valueAxisLayer.style.ticksFormat = decimalFormat(it, it)
              markAsDirty()
            }
          }

          configurableFont("Title font", valueAxisLayer.style::titleFont) {}

          configurableInsetsSeparate("Content Viewport Margin", contentViewportGestalt::contentViewportMargin)


          section("Apply-Methods")

          button("Hide Ticks") {
            valueAxisLayer.style.hideTicks()
            markAsDirty()
          }
          button("Show Ticks") {
            valueAxisLayer.style.showTicks()
            markAsDirty()
          }
          button("Hide Axis Line") {
            valueAxisLayer.style.hideAxisLine()
            markAsDirty()
          }
          button("Show Axis Line") {
            valueAxisLayer.style.showAxisLine()
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
      else -> ValueAxisLayer(ValueAxisLayer.Data(valueRangeProvider = { valueRange }))
    }.also {
      it.style.side = side
      it.style.tickOrientation = axisTickOrientation
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
              add(PredefinedConfiguration(ValueAxisDemoConfig(side, axisTickOrientation, valueRange)))
            }
          }
        }
      }
    }
  }
}
