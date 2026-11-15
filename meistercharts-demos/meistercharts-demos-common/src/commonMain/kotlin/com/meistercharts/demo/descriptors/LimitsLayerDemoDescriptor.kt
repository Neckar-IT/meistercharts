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
import com.meistercharts.algorithms.layers.Limit
import com.meistercharts.algorithms.layers.LimitsLayer
import com.meistercharts.algorithms.layers.LowerLimit
import com.meistercharts.algorithms.layers.UpperLimit
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.color.Color
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableColorPickerProviderNullable
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import it.neckar.geometry.Orientation
import it.neckar.open.provider.SizedProvider


class LimitsLayerDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("34348ff6-445d-4e47-8026-00d6274876ef")
  override val name: String = "Limits layer"
  override val description: String = "How to visualize limits"
  override val category: DemoCategory = DemoCategory.Layers
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable


  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()

          @DomainRelative var upperLimit = UpperLimit(0.94)
          @DomainRelative var lowerLimit = LowerLimit(0.25)

          val layerUpperLimit = LimitsLayer(object : SizedProvider<Limit> {
            override fun size(): Int = 1

            override fun valueAt(index: Int): Limit {
              return upperLimit
            }
          }) {
            fill = Color.orange
            stroke = Color.orangered
          }

          val layerLowerLimit = LimitsLayer(object : SizedProvider<Limit> {
            override fun size(): Int = 1

            override fun valueAt(index: Int): Limit {
              return lowerLimit
            }
          }) {
            fill = Color.greenyellow
            stroke = Color.darkgreen
          }

          layers.addLayer(layerLowerLimit)
          layers.addLayer(layerUpperLimit)

          configurableEnum("Orientation", layerLowerLimit.configuration.orientation, kotlin.enums.enumEntries<Orientation>().toList()) {
            onChange {
              layerLowerLimit.configuration.orientation = it
              layerUpperLimit.configuration.orientation = it
              markAsDirty()
            }
          }

          configurableDouble("Upper Limit", upperLimit.limit) {
            onChange {
              upperLimit = UpperLimit(it)
              markAsDirty()
            }
          }

          configurableDouble("Lower Limit", lowerLimit.limit) {
            onChange {
              lowerLimit = LowerLimit(it)
              markAsDirty()
            }
          }

          configurable("Area lower limit", layerLowerLimit.configuration::fill)
          configurable("Stroke lower limit", layerLowerLimit.configuration::stroke)

          configurable("Area upper limit", layerUpperLimit.configuration::fill)
          configurable("Stroke upper limit", layerUpperLimit.configuration::stroke)
        }
      }
    }
  }
}
