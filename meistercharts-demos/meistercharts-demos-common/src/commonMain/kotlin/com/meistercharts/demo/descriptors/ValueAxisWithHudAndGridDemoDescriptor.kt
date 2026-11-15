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
import com.meistercharts.algorithms.layers.DirectionalLinesLayer
import com.meistercharts.algorithms.layers.axis.ValueAxisHudLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.axis.hudLayer
import com.meistercharts.algorithms.layers.axis.withMaxNumberOfTicks
import com.meistercharts.annotations.Domain
import com.meistercharts.axis.AxisEndConfiguration
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBooleanProvider
import com.meistercharts.demo.configurableColorPickerProvider
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableInsets
import com.meistercharts.demo.configurableInt
import com.meistercharts.demo.mapWithPlusUuid
import com.meistercharts.model.Vicinity
import it.neckar.geometry.Side
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.MultiProvider

/**
 * Very simple demo that shows how to work with a value axis layer
 */
@Suppress("DuplicatedCode")
class ValueAxisWithHudAndGridDemoDescriptor : MeisterchartsDemoDescriptor<ValueAxisDemoConfig> {
  override val uuid: Uuid = Uuid.parse("dded2222-a5dd-46d8-8bbb-a47520ea92c2")
  override val name: String = "Value axis - with HUD + Grid"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override val category: DemoCategory = DemoCategory.Axis

  override val predefinedConfigurations: List<PredefinedConfiguration<ValueAxisDemoConfig>> = ValueAxisDemoConfig.createConfigs().mapWithPlusUuid(499)

  override fun prepareDemo(configuration: PredefinedConfiguration<ValueAxisDemoConfig>?): MeisterchartsDemo {
    require(configuration != null) { "ValueAxisWithHudAndGridDemoDescriptor.prepareDemo requires a predefined configuration" }

    return MeisterchartsDemo(this) {

      val valueAxisLayer = configuration.payload.createValueAxis()

      meistercharts {
        configure {
          layers.addClearBackground()

          val values = object {
            var domainValue0: @Domain Double = 85.0
            var domainValue1: @Domain Double = 15.0
            var domainValue2: @Domain Double = 115.0
          }

          valueAxisLayer.configuration.apply {
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
            layers.addLayerAt(
              linesLayer,
              paintingIndex = hudLayerIndex, //paint *under* hud layer
              layoutIndex = hudLayerIndex + 1 //layout *after* hud layer
            )
          }


          section("Lines")

          val baseLineStyle = linesLayer.configuration.lineStyles.valueAt(0)
          configurableDouble("Line Width", baseLineStyle.lineWidth) {
            max = 30.0

            onChange {
              val old = linesLayer.configuration.lineStyles.valueAt(0)
              linesLayer.configuration.lineStyles = MultiProvider.always(old.copy(lineWidth = it))
              markAsDirty()
            }
          }

          configurable("@DomainRelative Value 0", values::domainValue0) {
            max = 150.0
          }
          configurable("@DomainRelative Value 1", values::domainValue1) {
            max = 150.0
          }
          configurable("@DomainRelative Value 2", values::domainValue2) {
            max = 150.0
          }

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
            onChange {
              valueAxisLayer.configuration.ticks = valueAxisLayer.configuration.ticks.withMaxNumberOfTicks(it)
              markAsDirty()
            }
            value = 10
          }

          configurable("Axis size", valueAxisLayer.configuration::size) {
            max = 500.0
          }

          configurable("Axis margin", valueAxisLayer.configuration::margin) {
          }

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
        }
      }
    }
  }
}
