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
import com.meistercharts.charts.ModernCompassGestalt
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableInsets
import com.meistercharts.demo.section
import it.neckar.open.provider.DoubleProvider
import kotlin.math.PI

class ModernCompassGestaltDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("dbe074e6-936a-4ee1-8d9b-b49efc23e761")

  override val name: String = "Compass (Modern)"
  override val description: String = "A modern compass gestalt"
  override val category: DemoCategory = DemoCategory.Gestalt
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {

    return MeisterchartsDemo(this) {

      val gestalt = ModernCompassGestalt()

      val currentValue = object {
        var value: Double = gestalt.configuration.currentValueProvider()
      }

      gestalt.configuration.currentValueProvider = DoubleProvider { currentValue.value }

      meistercharts {
        gestalt.configure(this)

        configure {
          configurable("Current Value", currentValue::value) {
            min = gestalt.configuration.valueRangeProvider().start
            max = gestalt.configuration.valueRangeProvider().end
          }

          section("Needle")

          configurable("Height", gestalt.compassTriangleValuePainter.style::height) {
            min = 0.0
            max = 200.0
          }

          configurable("Width (rad)", gestalt.compassTriangleValuePainter.style::baseWidthRad) {
            min = -PI
            max = PI
          }

          configurable("Margin", gestalt.configuration::margin) {
          }
        }
      }
    }
  }
}
