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
import com.meistercharts.algorithms.layers.debug.MarkAsDirtyLayer
import com.meistercharts.animation.Easing
import com.meistercharts.canvas.LayerSupport
import com.meistercharts.charts.PuristicCompassGestalt
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableInsets
import com.meistercharts.demo.timeBasedValueGenerator
import com.meistercharts.history.generator.forNow
import it.neckar.open.provider.DoubleProvider

class PuristicCompassGestaltDemoDescriptor : ChartingDemoDescriptor<(LayerSupport, PuristicCompassGestalt) -> Unit> {

  override val name: String = "Compass (Puristic)"
  override val description: String = "Compass"
  override val category: DemoCategory = DemoCategory.Gestalt

  override val predefinedConfigurations: List<PredefinedConfiguration<(LayerSupport, PuristicCompassGestalt) -> Unit>> = listOf(
    PredefinedConfiguration({ _, _ -> }, "default"),
    neckarITHomePage
  )

  override fun createDemo(configuration: PredefinedConfiguration<(LayerSupport, PuristicCompassGestalt) -> Unit>?): ChartingDemo {

    return ChartingDemo {

      val gestalt = PuristicCompassGestalt()

      val currentValue = object {
        var value: Double = gestalt.data.currentValueProvider()
      }

      gestalt.data.currentValueProvider = DoubleProvider { currentValue.value }

      meistercharts {
        gestalt.configure(this)

        configure {
          configuration?.payload?.invoke(this, gestalt)

          configurableDouble("Current Value", currentValue::value) {
            min = gestalt.data.valueRangeProvider().start
            max = gestalt.data.valueRangeProvider().end
            markAsDirty()
          }

          configurableInsets("Margin", gestalt.style::margin) {
          }
        }
      }
    }
  }

  companion object {
    val neckarITHomePage: PredefinedConfiguration<(LayerSupport, PuristicCompassGestalt) -> Unit> = PredefinedConfiguration(
      { layerSupport, gestalt ->
        gestalt.data.currentValueProvider = timeBasedValueGenerator {
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
