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
import com.meistercharts.algorithms.layers.LowerLimit
import com.meistercharts.algorithms.layers.UpperLimit
import com.meistercharts.charts.PixelValuesGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.section

/**
 *
 */
class As30DiagramDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "AS30 - Category Line"
  override val category: DemoCategory = DemoCategory.Automation

  //language=HTML
  override val description: String = "Visualizes the output of a optical sensor (256 values)"

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        val gestalt = PixelValuesGestalt()
        gestalt.configure(this)

        configurableList(
          "Value Axis Units Override", gestalt.style.yValueAxisValueRangeOverride,
          listOf(ValueRange.default, ValueRange.linear(7.0, 203.0), ValueRange.percentage, ValueRange.linear(777.0, 33333.0))
        ) {
          onChange {
            gestalt.style.yValueAxisValueRangeOverride = it
            markAsDirty()
          }
        }

        configurableEnum("held?", gestalt.model::mode, enumValues())
        configurableBoolean("Limits visible", gestalt.model::limitsVisible)

        configurableDouble("lower limit", gestalt.model.lowerLimit.limit) {
          onChange {
            gestalt.model.lowerLimit = LowerLimit(it)
            markAsDirty()
          }
        }
        configurableDouble("upper limit", gestalt.model.upperLimit.limit) {
          onChange {
            gestalt.model.upperLimit = UpperLimit(it)
            markAsDirty()
          }
        }

        configurableBoolean("Detected Edges", gestalt.model::detectedEdgesVisible)
        configurableBoolean("Live Edges", gestalt.model::liveEdgesVisible)
        configurableBoolean("Teach Edges", gestalt.model::teachEdgesVisible)

        section("Line Style")

        configurableBoolean("Show Dots", gestalt.style::showDots)
        configurableBoolean("Show Lines", gestalt.style::showLines)
      }
    }
  }
}
