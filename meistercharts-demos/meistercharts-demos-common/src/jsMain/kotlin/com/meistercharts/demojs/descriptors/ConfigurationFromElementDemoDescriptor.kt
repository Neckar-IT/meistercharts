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
package com.meistercharts.demojs.descriptors

import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.addTexts
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.config.ChartConfigParsingSupport
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.js.MeisterChartClasses
import com.meistercharts.js.MeisterchartJS
import it.neckar.open.kotlin.serializers.JsonPretty
import kotlinx.html.dom.append
import kotlinx.html.js.script
import kotlinx.html.unsafe
import kotlinx.serialization.Serializable

/**
 * Depends on a script tag in the index.html file
 */
class ConfigurationFromElementDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("d847e68a-c0b9-42a0-a8e8-b40511519f97")
  override val name: String = "Configuration from element"
  override val category: DemoCategory = DemoCategory.Platform
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  private val json = JsonPretty

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()

          val parsingSupport = ChartConfigParsingSupport()

          //Read the preconfigured config from the script tag - that already exists in the HTML
          val myChartConfig = parsingSupport.parseChartsConfig<MyChartConfig>("meistercharts-config-myChart")

          val configObject = object {
            var message1: String = myChartConfig.message
            var message2: String = "unresolved"
            var message2Resolved = false
          }


          layers.addTexts { _, _ ->
            if (configObject.message2Resolved.not()) {

              val meisterchartJS = chartSupport.meisterchart as MeisterchartJS

              //Create the script tag here for the demo; in production it is created in HTML
              run {
                val jsonContent = json.encodeToString(MyChartConfig.serializer(), MyChartConfig("message from HTML"))

                //First create the config script tag - specific for this meistercharts instance
                val holder = meisterchartJS.holder
                holder.append {
                  script(type = "application/json") {
                    unsafe { +jsonContent }
                    attributes["class"] = MeisterChartClasses.Config
                  }
                }
              }

              val foundConfig = parsingSupport.parseChartsConfig<MyChartConfig>(chartSupport)

              configObject.message2Resolved = true
              configObject.message2 = foundConfig.message
              chartSupport.markAsDirty(DirtyReason.ChartStateChanged)
            }

            listOf(configObject.message1, configObject.message2)
          }

        }
      }
    }
  }

  @Serializable
  data class MyChartConfig(val message: String)
}
