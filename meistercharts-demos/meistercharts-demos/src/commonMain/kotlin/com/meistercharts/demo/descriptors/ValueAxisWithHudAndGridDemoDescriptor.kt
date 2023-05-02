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

import com.meistercharts.algorithms.layers.DirectionalLinesLayer
import com.meistercharts.algorithms.layers.ValueAxisHudLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.hudLayer
import com.meistercharts.algorithms.layers.withMaxNumberOfTicks
import com.meistercharts.annotations.Domain
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBooleanProvider
import com.meistercharts.demo.configurableColorPickerProvider
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableInsets
import com.meistercharts.demo.configurableInt
import com.meistercharts.model.Side
import it.neckar.open.kotlin.lang.enumEntries
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.MultiProvider

/**
 * Very simple demo that shows how to work with a value axis layer
 */
@Suppress("DuplicatedCode")
class ValueAxisWithHudAndGridDemoDescriptor : ChartingDemoDescriptor<ValueAxisDemoConfig> {
  override val name: String = "Value axis - with HUD + Grid"

  override val category: DemoCategory = DemoCategory.Axis

  override val predefinedConfigurations: List<PredefinedConfiguration<ValueAxisDemoConfig>> = ValueAxisDemoConfig.createConfigs()

  override fun createDemo(configuration: PredefinedConfiguration<ValueAxisDemoConfig>?): ChartingDemo {
    require(configuration != null)

    return ChartingDemo {

      val valueAxisLayer = configuration.payload.createValueAxis()

      meistercharts {
        configure {
          layers.addClearBackground()

          val values = object {
            var domainValue0: @Domain Double = 85.0
            var domainValue1: @Domain Double = 15.0
            var domainValue2: @Domain Double = 115.0
          }

          valueAxisLayer.style.apply {
            ticks = ticks.withMaxNumberOfTicks(10)
          }
          layers.addLayer(valueAxisLayer)

          val hudLayer: ValueAxisHudLayer = valueAxisLayer.hudLayer(
            domainValues = object : DoublesProvider {
              override fun valueAt(index: Int): Double {
                return when (index) {
                  0 -> values.domainValue0
                  1 -> values.domainValue1
                  2 -> values.domainValue2
                  else -> throw IllegalArgumentException("invalid index $index")
                }
              }

              override fun size(): Int {
                return 3
              }
            }
          )

          /**
           * Creates a threshold lines layer for the HUD layer
           */
          val linesLayer = DirectionalLinesLayer.createForValueAxisAndHud(valueAxisLayer, hudLayer)

          layers.addLayer(hudLayer).also { hudLayerIndex ->
            //add the threshold layer
            layers.addLayerAt(
              linesLayer,
              paintingIndex = hudLayerIndex, //paint *under* hud layer
              layoutIndex = hudLayerIndex + 1 //layout *after* hud layer
            )
          }


          declare {
            section("Lines")
          }

          val baseLineStyle = linesLayer.configuration.lineStyles.valueAt(0)
          configurableDouble("Line Width", baseLineStyle.lineWidth) {
            max = 30.0

            onChange {
              val old = linesLayer.configuration.lineStyles.valueAt(0)
              linesLayer.configuration.lineStyles = MultiProvider.always(old.copy(lineWidth = it))
              markAsDirty()
            }
          }

          configurableDouble("@DomainRelative Value 0", values::domainValue0) {
            max = 150.0
          }
          configurableDouble("@DomainRelative Value 1", values::domainValue1) {
            max = 150.0
          }
          configurableDouble("@DomainRelative Value 2", values::domainValue2) {
            max = 150.0
          }

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
            onChange {
              valueAxisLayer.style.ticks = valueAxisLayer.style.ticks.withMaxNumberOfTicks(it)
              markAsDirty()
            }
            value = 10
          }

          configurableDouble("Axis size", valueAxisLayer.style::size) {
            max = 500.0
          }

          configurableInsets("Axis margin", valueAxisLayer.style::margin) {
          }

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
        }
      }
    }
  }
}
