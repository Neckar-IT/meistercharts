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
import com.meistercharts.demo.section
import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.axis.ValueAxisLayer
import com.meistercharts.algorithms.layers.axis.withMaxNumberOfTicks
import com.meistercharts.algorithms.layers.text.addTexts
import com.meistercharts.axis.AxisEndConfiguration
import com.meistercharts.charts.ContentViewportGestalt
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.configurableDouble
import com.meistercharts.demo.configurableEnum
import com.meistercharts.demo.configurableInt
import com.meistercharts.model.Insets
import com.meistercharts.range.ValueRange
import it.neckar.open.formatting.format
import kotlin.math.pow

/**
 * Very simple demo that shows how to work with a value axis layer
 */
class ValueAxisLogDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("b81a1951-4652-415b-81ed-b51eae7bf89e")
  override val name: String = "Value axis - Log"
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  //language=HTML
  override val description: String = "Visualizes a logarithmic value axis."

  override val category: DemoCategory = DemoCategory.Axis

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {

      val initialValueRange = ValueRange.logarithmic(100.0, 10_000.0)

      val valueAxisLayer = ValueAxisLayer.logarithmic("log", initialValueRange)
      valueAxisLayer.configuration.size = 150.0

      val config = object {
        var startExponent = initialValueRange.logStart
        var endExponent = initialValueRange.logEnd

        fun updateValueRange() {
          valueAxisLayer.configuration.valueRangeProvider = { ValueRange.logarithmic(10.0.pow(startExponent), 10.0.pow(endExponent)) }
        }
      }

      meistercharts {
        val contentViewportGestalt = ContentViewportGestalt(Insets.all15)
        contentViewportGestalt.configure(this@meistercharts)

        configure {
          layers.addClearBackground()

          layers.addLayer(valueAxisLayer)
          layers.addTexts { textService, i18nConfiguration ->
            val paintingVariables = valueAxisLayer.paintingVariables()

            buildList<String> {
              add("StartDomain: ${paintingVariables.startDomainValue.format(2)}")
              add("EndDomain: ${paintingVariables.endDomainValue.format(2)}")

              add("Tick Domain Values:")
              paintingVariables.tickLabels.fastForEachValue {
                add("Tick @ ${it.format(2)}")
              }
            }
          }

          section("Value Range")

          configurable("Start Exponent", config::startExponent) {
            max = 10.0
            onChange {
              config.updateValueRange()
            }
          }

          configurable("End Exponent", config::endExponent) {
            max = 10.0
            onChange {
              config.updateValueRange()
            }
          }

          configurableInt("Max tick count") {
            min = 0
            max = 20
            value = 11
            onChange {
              valueAxisLayer.configuration.ticks = valueAxisLayer.configuration.ticks.withMaxNumberOfTicks(it)
              markAsDirty()
            }
          }
          configurable("Axis End", valueAxisLayer.configuration::axisEndConfiguration, kotlin.enums.enumEntries<AxisEndConfiguration>().toList()) {
          }

        }
      }
    }
  }
}
