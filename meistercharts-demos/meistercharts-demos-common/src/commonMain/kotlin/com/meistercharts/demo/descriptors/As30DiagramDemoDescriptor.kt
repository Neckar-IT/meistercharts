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
import com.meistercharts.range.ValueRange
import com.meistercharts.algorithms.layers.LowerLimit
import com.meistercharts.algorithms.layers.UpperLimit
import com.meistercharts.charts.PixelValueVisualizationMode
import com.meistercharts.charts.PixelValuesGestalt
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableBoolean
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableList
import com.meistercharts.demo.section

/**
 *
 */
class As30DiagramDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("c3c6e47a-ca96-4809-96b3-3c28f6d2f0d5")

  override val name: String = "AS30 - Category Line"
  override val category: DemoCategory = DemoCategory.Automation
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  //language=HTML
  override val description: String = "Visualizes the output of an optical sensor (256 values)"

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        val gestalt = PixelValuesGestalt()
        gestalt.configure(this)

        configurableList(
          "Value Axis Units Override", gestalt.configuration.yValueAxisValueRangeOverride,
          listOf(ValueRange.default, ValueRange.linear(7.0, 203.0), ValueRange.percentage, ValueRange.linear(777.0, 33333.0))
        ) {
          onChange {
            gestalt.configuration.yValueAxisValueRangeOverride = it
            markAsDirty()
          }
        }

        configurable("held?", gestalt.model::mode, kotlin.enums.enumEntries<PixelValueVisualizationMode>().toList())
        configurable("Limits visible", gestalt.model::limitsVisible)

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

        configurable("Detected Edges", gestalt.model::detectedEdgesVisible)
        configurable("Live Edges", gestalt.model::liveEdgesVisible)
        configurable("Teach Edges", gestalt.model::teachEdgesVisible)

        section("Line Style")

        configurable("Show Dots", gestalt.configuration::showDots)
        configurable("Show Lines", gestalt.configuration::showLines)
      }
    }
  }
}
