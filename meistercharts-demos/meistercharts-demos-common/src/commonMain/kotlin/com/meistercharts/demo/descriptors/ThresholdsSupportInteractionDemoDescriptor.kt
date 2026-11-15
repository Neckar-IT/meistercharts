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


import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.AxisConfiguration
import com.meistercharts.algorithms.layers.DirectionalLinesLayer
import com.meistercharts.algorithms.layers.MultipleLayersDelegatingLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.charts.support.ValueAxisSupport
import com.meistercharts.charts.support.threshold.ThresholdsSupport
import com.meistercharts.color.Color
import com.meistercharts.color.withAlpha
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.model.Vicinity
import com.meistercharts.range.ValueRange
import it.neckar.geometry.HorizontalAlignment
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.provider.DoublesProvider1
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.asSizedProvider

class ThresholdsSupportInteractionDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("382588fc-86be-4873-9e87-832a95c51212")
  override val name: String = "Thresholds Interaction Support"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  //language=HTML
  override val category: DemoCategory = DemoCategory.Support

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        val valueAxisSupport = ValueAxisSupport<MyKeyEnum>(
          valueRangeProvider = {
            when (it) {
              MyKeyEnum.Axis0 -> ValueRange.linear(10.0, 220.0)
              MyKeyEnum.Axis1 -> ValueRange.linear(17.0, 123.0)
              MyKeyEnum.Axis2 -> ValueRange.linear(-20.0, 100.0)
            }
          }
        ) {
          valueAxisConfiguration = { myKeyEnum, _, _ ->
            when (myKeyEnum) {
              MyKeyEnum.Axis0 -> {
                tickOrientation = Vicinity.Outside
                size = 130.0
              }

              MyKeyEnum.Axis1 -> {
                tickOrientation = Vicinity.Outside
                size = 130.0
                paintRange = AxisConfiguration.PaintRange.ContentArea
                lineColor = Color.red
              }

              MyKeyEnum.Axis2 -> {
                tickOrientation = Vicinity.Inside
                size = 130.0
                paintRange = AxisConfiguration.PaintRange.ContentArea
                lineColor = Color.blue
              }
            }
          }
        }

        val thresholdValueProvider = object : DoublesProvider1<MyKeyEnum> {
          override fun size(param1: MyKeyEnum): Int {
            return when (param1) {
              MyKeyEnum.Axis0 -> 3
              MyKeyEnum.Axis1 -> 3
              MyKeyEnum.Axis2 -> 5
            }
          }

          override fun valueAt(index: Int, param1: MyKeyEnum): Double {
            return when (param1) {
              MyKeyEnum.Axis0 -> {
                when (index) {
                  0 -> 57.0
                  else -> 150.0
                }
              }

              MyKeyEnum.Axis1 -> 20.0 + index * 12
              MyKeyEnum.Axis2 -> 70.0 + index
            }
          }
        }

        val support: ThresholdsSupport<MyKeyEnum> = ThresholdsSupport(
          valueAxisProvider = valueAxisSupport,
          thresholdValueProvider = thresholdValueProvider,
          thresholdLabelProvider = { index, axisEnum, _ ->
            when (axisEnum) {
              MyKeyEnum.Axis0 -> {
                when (index) {
                  0 -> listOf("This is the Value for $index for enum $axisEnum")
                  else -> listOf("$index One of two labels at the exact same location")
                }
              }

              else -> listOf("This is the Value for $index", "for enum $axisEnum", decimalFormat.format(thresholdValueProvider.valueAt(index, axisEnum)))
            }

          },
        ) {
          this.hudLayerConfiguration = { myKeyEnum, valueAxisHudLayer ->
            if (myKeyEnum == MyKeyEnum.Axis0) {
              valueAxisHudLayer.configuration.textAlignments = MultiProvider.always(HorizontalAlignment.Right)
            }
          }
        }

        configure {
          layers.addClearBackground()

          val addLayersResult: ValueAxisSupport.AddLayersResult = valueAxisSupport.addMultipleLayers(layers, MyKeyEnum.entries)
          addLayersResult.multiValueAxisLayer.configuration.background = Color.lightgray.withAlpha(0.5)

          val linesDelegateLayer: MultipleLayersDelegatingLayer<DirectionalLinesLayer> = MultipleLayersDelegatingLayer(
            MyKeyEnum.entries.map {
              support.getThresholdLinesLayer(it)
            }
              .asSizedProvider()
          )

          val hudLayers = MyKeyEnum.entries.map {
            support.getHudLayer(it)
          }
            .asSizedProvider()
          val hudDelegateLayer = MultipleLayersDelegatingLayer(
            hudLayers
          )

          val hudDelegateLayerIndex = layers.addLayer(hudDelegateLayer)
          layers.addLayerAt(linesDelegateLayer, addLayersResult.axisLayerIndex, hudDelegateLayerIndex + 1)

          ThresholdsSupport.createInteractionLayers(linesDelegateLayer, hudDelegateLayer).let {
            layers.addLayer(it.directionalLinesInteractionLayer)
            layers.addLayer(it.hudLayerInteractionLayer)
          }
        }
      }
    }
  }
}
