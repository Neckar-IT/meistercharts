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
import com.meistercharts.algorithms.layers.compass.ArrowValuePainter
import com.meistercharts.annotations.Domain
import com.meistercharts.charts.ClassicCompassGestalt
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableInsetsSeparate

class ClassicCompassGestaltDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("82a1e0c3-2d14-483e-8f70-fc99438c9b80")

  override val name: String = "Compass (Classic)"

  //language=HTML
  override val description: String = "Compass (Classic)"
  override val category: DemoCategory = DemoCategory.Gestalt
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {

    return MeisterchartsDemo(this) {
      var valueRangeStart = 0.0
      var valueRangeEnd = 200.0

      var currentValue: @Domain Double = 17.0

      val valueRangeProvider = { ValueRange.linear(valueRangeStart, valueRangeEnd) }
      val gestalt = ClassicCompassGestalt(
        valueRangeProvider = valueRangeProvider,
        currentValueProvider = { currentValue }
      )

      meistercharts {
        gestalt.configure(this)

        configure {
          configurableInsetsSeparate("Margin", gestalt.configuration.marginProperty) {
            onChange {
              markAsDirty()
            }
          }

          configurableDouble("Current Value", currentValue) {
            min = -360.0
            max = 360.0

            onChange {
              currentValue = it
              markAsDirty()
            }
          }

          configurableDouble("Value Range from", valueRangeStart) {
            min = -500.0
            max = 500.0

            onChange {
              valueRangeStart = it
              markAsDirty()
            }
          }
          configurableDouble("Value Range until", valueRangeEnd) {
            min = -500.0
            max = 500.0

            onChange {
              valueRangeEnd = it
              markAsDirty()
            }
          }

          val valuePainter = gestalt.gaugePaintable.style.valuePainter as ArrowValuePainter
          configurable("Pointer width", valuePainter.style::pointerLineWidth) {
            max = 20.0
          }
        }
      }
    }
  }
}
