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
import com.meistercharts.algorithms.layers.debug.MarkAsDirtyLayer
import com.meistercharts.animation.Easing
import com.meistercharts.canvas.layer.LayerSupport
import com.meistercharts.charts.PuristicCompassGestalt
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableInsets
import com.meistercharts.demo.timeBasedValueGenerator
import com.meistercharts.history.generator.forNow
import it.neckar.open.provider.DoubleProvider

class PuristicCompassGestaltDemoDescriptor : MeisterchartsDemoDescriptor<(LayerSupport, PuristicCompassGestalt) -> Unit> {
  override val uuid: Uuid = Uuid.parse("8b325a4d-40fa-4ecc-9c37-177b970bea2d")

  override val name: String = "Compass (Puristic)"
  override val description: String = "A puristic compass gestalt"
  override val category: DemoCategory = DemoCategory.Gestalt
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override val predefinedConfigurations: List<PredefinedConfiguration<(LayerSupport, PuristicCompassGestalt) -> Unit>> = listOf(
    PredefinedConfiguration(Uuid.parse("c5331c5e-69d9-44d4-9229-99266273ed09"), { _, _ -> }, "default"),
    neckarITHomePage
  )

  override fun prepareDemo(configuration: PredefinedConfiguration<(LayerSupport, PuristicCompassGestalt) -> Unit>?): MeisterchartsDemo {

    return MeisterchartsDemo(this) {

      val gestalt = PuristicCompassGestalt()

      val currentValue = object {
        var value: Double = gestalt.configuration.currentValueProvider()
      }

      gestalt.configuration.currentValueProvider = DoubleProvider { currentValue.value }

      meistercharts {
        gestalt.configure(this)

        configure {
          configuration?.payload?.invoke(this, gestalt)

          configurable("Current Value", currentValue::value) {
            min = gestalt.configuration.valueRangeProvider().start
            max = gestalt.configuration.valueRangeProvider().end
            markAsDirty()
          }

          configurable("Margin", gestalt.configuration::margin) {
          }
        }
      }
    }
  }

  companion object {
    val neckarITHomePage: PredefinedConfiguration<(LayerSupport, PuristicCompassGestalt) -> Unit> = PredefinedConfiguration(
      Uuid.parse("f383b24c-2370-43ee-85df-d0b93efdaff8"),
      { layerSupport, gestalt ->
        gestalt.configuration.currentValueProvider = timeBasedValueGenerator {
          startValue = 360.0
          minDeviation = 5.0
          maxDeviation = 60.0
          easing = Easing.inOut
          period = 5000.0
          valueRange = ValueRange.linear(Int.MIN_VALUE.toDouble(), Int.MAX_VALUE.toDouble())
        }.forNow()
        layerSupport.layers.addLayer(MarkAsDirtyLayer())
      }, "Neckar IT Home Page"
    )
  }
}
