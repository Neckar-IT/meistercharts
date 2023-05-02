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

import com.meistercharts.algorithms.environment
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.addText
import com.meistercharts.canvas.i18nConfiguration
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.i18n.DefaultSystemLocale

class EnvironmentLayerDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Environment"

  //language=HTML
  override val description: String = "## Debug info about the environment"
  override val category: DemoCategory = DemoCategory.LowLevelTests

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          layers.addText { _, _ ->
            val size = chartSupport.canvas.size
            listOf(
              "multiTouchSupported: ${environment.multiTouchSupported}",
              "devicePixelRatio: ${environment.devicePixelRatio}",
              "Canvas size: ${decimalFormat.format(size.width, chartSupport.i18nConfiguration)} / ${decimalFormat.format(size.height, chartSupport.i18nConfiguration)}",
              "Default System Locale $DefaultSystemLocale"
            )
          }
        }
      }
    }
  }
}
