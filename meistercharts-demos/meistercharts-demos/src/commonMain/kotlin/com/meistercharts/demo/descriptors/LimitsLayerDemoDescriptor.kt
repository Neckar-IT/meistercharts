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

import com.meistercharts.algorithms.layers.Limit
import com.meistercharts.algorithms.layers.LimitsLayer
import com.meistercharts.algorithms.layers.LowerLimit
import com.meistercharts.algorithms.layers.UpperLimit
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.configurableColorPicker
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import it.neckar.open.kotlin.lang.enumEntries
import it.neckar.open.provider.SizedProvider


class LimitsLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Limits layer"
  override val description: String = "## How to visualize limits"
  override val category: DemoCategory = DemoCategory.Layers

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          @DomainRelative var upperLimit = UpperLimit(0.94)
          @DomainRelative var lowerLimit = LowerLimit(0.25)

          val layerUpperLimit = LimitsLayer(LimitsLayer.Data(object : SizedProvider<Limit> {
            override fun size(): Int = 1

            override fun valueAt(index: Int): Limit {
              return upperLimit
            }
          })) {
            fill = Color.orange
            stroke = Color.orangered
          }

          val layerLowerLimit = LimitsLayer(LimitsLayer.Data(object : SizedProvider<Limit> {
            override fun size(): Int = 1

            override fun valueAt(index: Int): Limit {
              return lowerLimit
            }
          })) {
            fill = Color.greenyellow
            stroke = Color.darkgreen
          }

          layers.addLayer(layerLowerLimit)
          layers.addLayer(layerUpperLimit)

          configurableEnum("Orientation", layerLowerLimit.style.orientation, enumEntries()) {
            onChange {
              layerLowerLimit.style.orientation = it
              layerUpperLimit.style.orientation = it
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

          configurableColorPicker("Area lower limit", layerLowerLimit.style::fill, Color.magenta) { }
          configurableColorPicker("Stroke lower limit", layerLowerLimit.style::stroke, Color.magenta) { }

          configurableColorPicker("Area upper limit", layerUpperLimit.style::fill, Color.magenta) { }
          configurableColorPicker("Stroke upper limit", layerUpperLimit.style::stroke, Color.magenta) { }
        }
      }
    }
  }
}
