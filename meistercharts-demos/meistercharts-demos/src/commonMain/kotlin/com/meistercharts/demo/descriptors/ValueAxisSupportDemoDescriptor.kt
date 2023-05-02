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
import com.meistercharts.algorithms.layers.AxisTitleLocation
import com.meistercharts.algorithms.layers.ValueAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.charts.ContentViewportGestalt
import com.meistercharts.charts.support.ValueAxisSupport
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableFont
import com.meistercharts.model.Insets
import com.meistercharts.model.Vicinity
import it.neckar.open.kotlin.lang.asProvider

class ValueAxisSupportDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Value Axes Support"

  //language=HTML
  override val category: DemoCategory = DemoCategory.Support

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {

        val myAxisConfiguration: ValueAxisLayer.Style.(MyKeyEnum, ValueAxisLayer, AxisTitleLocation) -> Unit = { myKeyEnum, axis, _ ->
          titleProvider = { textService, i18nConfiguration -> "Title for $myKeyEnum" }

          when (myKeyEnum) {
            MyKeyEnum.Axis0 -> size = 100.0

            MyKeyEnum.Axis1 -> {
              size = 100.0
              margin = Insets.of(120.0)
              axis.data.valueRangeProvider = ValueRange.linear(10.0, 75.0).asProvider()
              paintRange = AxisStyle.PaintRange.ContentArea
            }
          }
        }

        val support = ValueAxisSupport<MyKeyEnum>(
          valueRangeProvider = { myKeyEnum ->
            ValueRange.linear(10.0, 150.0)
          }
        ) {
          valueAxisConfiguration = myAxisConfiguration
        }

        ContentViewportGestalt(Insets.of(40.0, 10.0, 10.0, 10.0)).configure(this)

        configure {
          layers.addClearBackground()

          support.addLayers(this, MyKeyEnum.Axis0)
          support.addLayers(this, MyKeyEnum.Axis1)
        }

        configurableEnum("Title Location", support::preferredAxisTitleLocation)
        configurableEnum("Tick Location", Vicinity.Outside) {
          onChange {
            support.configuration.valueAxisConfiguration = { myKeyEnum, axis, valueAxisTitleLocation ->
              myAxisConfiguration(myKeyEnum, axis, valueAxisTitleLocation) //delegate to the "main" config
              axis.style.tickOrientation = it
              markAsDirty()
            }
          }
        }

        configurableFont("Title Font", support.getTopTitleLayer(MyKeyEnum.Axis1).configuration.titleFont) {
          onChange {
            support.configuration.topTitleLayerConfiguration = { myKeyEnum, axis ->
              axis.configuration.titleFont = it
            }
            markAsDirty()
          }
        }

      }
    }
  }

  enum class MyKeyEnum {
    Axis0,
    Axis1,
  }
}
