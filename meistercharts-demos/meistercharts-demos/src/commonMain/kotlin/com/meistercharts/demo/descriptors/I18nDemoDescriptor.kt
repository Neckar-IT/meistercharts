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

import com.meistercharts.algorithms.TimeRange
import com.meistercharts.algorithms.layers.TickDistanceAwareTickFormat
import com.meistercharts.algorithms.layers.TimeAxisLayer
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.addTimeAxis
import com.meistercharts.algorithms.layers.text.addText
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.textService
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.model.Insets
import it.neckar.open.i18n.Locale
import it.neckar.open.i18n.TextKey
import it.neckar.open.i18n.resolve
import it.neckar.open.i18n.resolve.MapBasedTextResolver
import com.meistercharts.style.BoxStyle

/**
 */

/**
 * Demos that visualizes the functionality of the FPS layer
 */
class I18nDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "I18n"
  override val description: String = "## How to translate text"
  override val category: DemoCategory = DemoCategory.Text

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        val textKey = TextKey("my.message", "Default Text 1")
        val timeAxisKey = TextKey("time.axis.key", "Default Text 2")

        configureAsTimeChart()

        configure {
          chartSupport.textService.addTextResolverAtFirst(MapBasedTextResolver().also {
            it.setText(Locale.US, textKey, "Message - US")
            it.setText(Locale.Germany, textKey, "Message - DE")

            it.setText(Locale.US, timeAxisKey, "Time")
            it.setText(Locale.Germany, timeAxisKey, "Zeit")
          })

          layers.addClearBackground()
          val boxStyle = BoxStyle(Color.gray, Color.darkgrey, padding = Insets.of(5.0))
          layers.addText(textKey) {
            margin = Insets.of(10.0)
            this.boxStyle = boxStyle
          }

          val contentAreaTimeRange = TimeRange.oneMinuteUntilNow()

          layers.addTimeAxis(contentAreaTimeRange) {
            titleProvider = { textService, i18nConfiguration -> timeAxisKey.resolve(textService, i18nConfiguration) }
            timestampsMode = TimeAxisLayer.TimestampsMode.Absolute
            absoluteTimestampTickFormat = TickDistanceAwareTickFormat
          }
        }
      }
    }
  }
}
