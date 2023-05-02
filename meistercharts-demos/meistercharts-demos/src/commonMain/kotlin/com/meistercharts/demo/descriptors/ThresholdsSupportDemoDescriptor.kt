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
import com.meistercharts.algorithms.layers.AxisStyle
import com.meistercharts.algorithms.layers.MultipleLayersDelegatingLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.charts.support.ThresholdsSupport
import com.meistercharts.charts.support.ValueAxisSupport
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.model.HorizontalAlignment
import com.meistercharts.model.Vicinity
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.provider.DoublesProvider1
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.SizedProvider
import it.neckar.open.formatting.decimalFormat

class ThresholdsSupportDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Thresholds Support"

  //language=HTML
  override val category: DemoCategory = DemoCategory.Support

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
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
          valueAxisConfiguration = { myKeyEnum, axis, _ ->
            when (myKeyEnum) {
              MyKeyEnum.Axis0 -> {
                tickOrientation = Vicinity.Outside
                size = 130.0
              }

              MyKeyEnum.Axis1 -> {
                tickOrientation = Vicinity.Outside
                size = 130.0
                paintRange = AxisStyle.PaintRange.ContentArea
                lineColor = Color.red.asProvider()
              }

              MyKeyEnum.Axis2 -> {
                tickOrientation = Vicinity.Inside
                size = 130.0
                paintRange = AxisStyle.PaintRange.ContentArea
                lineColor = Color.blue.asProvider()
              }
            }
          }
        }

        val thresholdValueProvider = object : DoublesProvider1<MyKeyEnum> {
          override fun size(param1: MyKeyEnum): Int {
            return when (param1) {
              MyKeyEnum.Axis0 -> 1
              MyKeyEnum.Axis1 -> 3
              MyKeyEnum.Axis2 -> 1
            }
          }

          override fun valueAt(index: Int, param1: MyKeyEnum): Double {
            return when (param1) {
              MyKeyEnum.Axis0 -> 57.0
              MyKeyEnum.Axis1 -> 20.0 + index * 12
              MyKeyEnum.Axis2 -> 70.0
            }
          }
        }

        val support = ThresholdsSupport(
          valueAxisProvider = valueAxisSupport,
          thresholdValueProvider = thresholdValueProvider,
          thresholdLabelProvider = { index, param1, _ ->
            listOf("This is the Value for $index", "for enum $param1", decimalFormat.format(thresholdValueProvider.valueAt(index, param1)))
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

          val result = valueAxisSupport.addMultipleLayers(layers, MyKeyEnum.entries)
          result.multiValueAxisLayer.configuration.background = Color.lightgray.withAlpha(0.5).asProvider()

          val linesDelegateLayer = MultipleLayersDelegatingLayer(
            SizedProvider.forList(
              MyKeyEnum.entries.map {
                support.getThresholdLinesLayer(it)
              }
            )
          )
          val hudDelegateLayer = MultipleLayersDelegatingLayer(
            SizedProvider.forList(
              MyKeyEnum.entries.map {
                support.getHudLayer(it)
              }
            )
          )

          val hudDelegateLayerIndex = layers.addLayer(hudDelegateLayer)
          layers.addLayerAt(linesDelegateLayer, result.axisLayerIndex, hudDelegateLayerIndex + 1)
        }
      }
    }
  }
}

enum class MyKeyEnum {
  Axis0, Axis1, Axis2,
}
