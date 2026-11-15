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
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.layers.PasspartoutLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.axis.ValueAxisWithOffsetLayer
import com.meistercharts.algorithms.layers.bind
import com.meistercharts.annotations.Domain
import com.meistercharts.axis.AxisEndConfiguration
import com.meistercharts.canvas.layout.buffer.TickLabelsBuffer
import com.meistercharts.canvas.paintTextBox
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBooleanProvider
import com.meistercharts.demo.configurableColorPickerProvider
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFontProvider
import com.meistercharts.demo.configurableInsets
import com.meistercharts.demo.configurableInt
import com.meistercharts.demo.section
import com.meistercharts.model.Vicinity
import com.meistercharts.range.ValueRange
import it.neckar.geometry.Direction
import it.neckar.geometry.Side
import it.neckar.open.collections.fastForEach
import it.neckar.open.formatting.format
import it.neckar.uuid.plus

/**
 * Demonstrates a value axis that factors large values into a coloured offset band plus a short remainder label.
 */
class ValueAxisWithOffsetDemoDescriptor : MeisterchartsDemoDescriptor<ValueAxisWithOffsetDemoConfig> {
  override val uuid: Uuid = Uuid.parse("3704522e-f024-459e-93c6-b079e478b720")
  override val name: String = "Value axis with offset"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  //language=HTML
  override val description: String = "<h3>Value axis that splits large values into an offset band + remainder label</h3>"

  override val category: DemoCategory = DemoCategory.Axis

  override val predefinedConfigurations: List<PredefinedConfiguration<ValueAxisWithOffsetDemoConfig>> = ValueAxisWithOffsetDemoConfig.createConfigs()

  override fun prepareDemo(configuration: PredefinedConfiguration<ValueAxisWithOffsetDemoConfig>?): MeisterchartsDemo {
    require(configuration != null) { "ValueAxisWithOffsetDemoDescriptor.prepareDemo requires a predefined configuration" }

    val config = configuration.payload
    var currentValueRange: ValueRange = config.valueRange

    return MeisterchartsDemo(this) {

      val valueAxisWithOffsetLayer = ValueAxisWithOffsetLayer("Offset Axis [m]", currentValueRange) {
        valueRangeProvider = { currentValueRange }
        side = config.side
        spaceForDigits = config.spaceForDigits
        size = 140.0
      }

      meistercharts {
        configure {
          layers.addClearBackground()

          val passpartoutLayer = PasspartoutLayer {
            color = { Color("rgba(69, 204, 112, 0.25)") } // use something different from white so the size of the axis can be better grasped
          }
          layers.addLayer(passpartoutLayer)
          layers.addLayer(valueAxisWithOffsetLayer)

          layers.addLayer(MyValueAxisWithOffsetDebugLayer(valueAxisWithOffsetLayer))

          passpartoutLayer.configuration.bind(valueAxisWithOffsetLayer.configuration)

          section("Offset")

          configurable("Space for Digits", valueAxisWithOffsetLayer.configuration::spaceForDigits) {
            min = 1
            max = 8
          }

          configurable("Offset area size", valueAxisWithOffsetLayer.configuration::offsetAreaSize) {
            max = 200.0
          }

          configurable("Offset gap", valueAxisWithOffsetLayer.configuration::offsetAreaTickLabelGap) {
            max = 40.0
          }

          section("Value Range")

          configurableDouble("Range Start", currentValueRange.start) {
            min = -200_000.0
            max = 200_000.0
            onChange {
              currentValueRange = ValueRange.linear(it.coerceAtMost(currentValueRange.end - 1.0), currentValueRange.end)
              markAsDirty()
            }
          }

          configurableDouble("Range End", currentValueRange.end) {
            min = -200_000.0
            max = 200_000.0
            onChange {
              currentValueRange = ValueRange.linear(currentValueRange.start, it.coerceAtLeast(currentValueRange.start + 1.0))
              markAsDirty()
            }
          }

          section("Layout")

          configurable("Side", valueAxisWithOffsetLayer.configuration::side, Side.entries)

          configurable("Axis size", valueAxisWithOffsetLayer.configuration::size) {
            max = 500.0
          }

          configurable("Axis margin", valueAxisWithOffsetLayer.configuration::margin) {
          }

          section("Title")

          configurable("Show Title", valueAxisWithOffsetLayer.configuration::titleVisible) {
          }

          configurable("Title Gap", valueAxisWithOffsetLayer.configuration::titleGap) {
            max = 20.0
          }

          section("Axis Config")

          configurable("Tick Orientation", valueAxisWithOffsetLayer.configuration::tickOrientation, Vicinity.entries)
          configurable("Axis End", valueAxisWithOffsetLayer.configuration::axisEndConfiguration, AxisEndConfiguration.entries)

          section("Axis")

          configurable("Line color", valueAxisWithOffsetLayer.configuration::lineColor) {
          }

          configurable("Axis line width", valueAxisWithOffsetLayer.configuration::axisLineWidth) {
            max = 20.0
          }
          configurable("Tick length", valueAxisWithOffsetLayer.configuration::tickLength) {
            max = 20.0
          }
          configurable("Tick width", valueAxisWithOffsetLayer.configuration::tickLineWidth) {
            max = 20.0
          }
          configurable("Tick Label Gap", valueAxisWithOffsetLayer.configuration::tickLabelGap) {
            max = 20.0
          }

          configurable("Offset label color", valueAxisWithOffsetLayer.configuration::offsetTickLabelColor) {
          }

          configurable("Background Color", passpartoutLayer.configuration::color) {
          }

          configurable("Tick font", valueAxisWithOffsetLayer.configuration::tickFont) {
          }

          configurable("Offset font", valueAxisWithOffsetLayer.configuration::offsetTickFont) {
          }

          configurable("Title font", valueAxisWithOffsetLayer.configuration::titleFont) {
          }
        }
      }
    }
  }
}

/**
 * The predefined configuration for [ValueAxisWithOffsetDemoDescriptor].
 */
data class ValueAxisWithOffsetDemoConfig(
  val side: Side,
  val valueRange: ValueRange,
  val spaceForDigits: Int,
  val label: String,
) {
  override fun toString(): String {
    return "${side.name} - $label"
  }

  companion object {
    fun createConfigs(): List<PredefinedConfiguration<ValueAxisWithOffsetDemoConfig>> {
      val ranges = listOf(
        Triple(ValueRange.linear(23_000.0, 24_000.0), 3, "large 23k..24k"),
        Triple(ValueRange.linear(-40_000.0, 140_000.0), 3, "crossing zero"),
      )

      return buildList {
        ranges.fastForEach { (valueRange, spaceForDigits, label) ->
          Side.entries.fastForEach { side ->
            add(
              PredefinedConfiguration(
                Uuid.parse("6a1d3f0c-2b4e-4f8a-9c1d-7e2f5a3b6c40")
                  .plus(side)
                  .plus(valueRange.start.toInt() + valueRange.end.toInt() * 12 + spaceForDigits),
                ValueAxisWithOffsetDemoConfig(side, valueRange, spaceForDigits, label),
              ),
            )
          }
        }
      }
    }
  }
}


private class MyValueAxisWithOffsetDebugLayer(val valueAxisWithOffsetLayer: ValueAxisWithOffsetLayer) : AbstractLayer() {
  override val type: LayerType = LayerType.Content

  override fun paint(paintingContext: LayerPaintingContext) {
    val lastTicks: @Domain TickLabelsBuffer = valueAxisWithOffsetLayer.paintingVariables().tickLabels

    val paintingVariables = valueAxisWithOffsetLayer.paintingVariables()

    val lines = mutableListOf<String>().also {
      it.add("Axis Start: ${paintingVariables.axisStart.format(1)}")
      it.add("Axis End: ${paintingVariables.axisEnd.format(1)}")
      it.add("Start Value: ${paintingVariables.startDomainValue.format(2)}")
      it.add("End Value: ${paintingVariables.endDomainValue.format(2)}")

      it.add("---")

      it.add("Delta Magnitude: ${paintingVariables.deltaMagnitude}")
      it.add("Integer Digits: ${paintingVariables.integerDigits}")
      it.add("Fraction Digits: ${paintingVariables.fractionDigits}")
      it.add("Offset Step: ${paintingVariables.offsetStep.format(0)}")

      it.add("---")

      it.add("Tick values: ${lastTicks.size}")
      lastTicks.fastForEachValue { tick ->
        it.add(tick.format(2))
      }

      it.add("---")

      it.add("Offset Ticks: ${paintingVariables.offsetTickLabels.size}")
      paintingVariables.offsetTickLabels.fastForEachValue { tick ->
        it.add(tick.format(0))
      }
    }

    val gc = paintingContext.gc
    gc.translate(gc.width, 0.0)
    gc.paintTextBox(
      lines, Direction.TopRight,
      20.0,
    )
  }
}
